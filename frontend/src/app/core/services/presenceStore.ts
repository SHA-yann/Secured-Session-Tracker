import { computed, DestroyRef, inject, Injectable, signal } from "@angular/core";
import { PresenceDelta, SseService } from "./sse-sevice";
import { Subscription } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { ConfigService } from "./urlconfig";

/**
 * Service managing user presence state via Server-Sent Events (SSE).
 * Ensures real-time data reactivity and lifecycle connection management.
 */
@Injectable({ providedIn: 'root'})
export class PresenceStore {
    
    private readonly sseService = inject(SseService);
    private readonly destroyRef = inject(DestroyRef);
    private readonly configService = inject(ConfigService);

    private abortController?: AbortController;
    private isInitialized = false;
    
    /** Unique identifier for the current SSE connection session. */
    connectionId: string = '';
    
    private sseSubscription?: Subscription;
    private backgroundTimestamp: number | null = null;
    
    /** Maximum allowed background duration before forcing a reconnection (45s). */
    private readonly MAX_ABSENCE_GRACE_MS = 45000;

    /** Reactive internal state mapping present users (User ID -> Presence). */
    private readonly _presence = signal<Map<number, PresenceDelta>>(new Map<number, PresenceDelta>());
    
    /** Reactive map of connected users (read-only). */
    readonly presence = this._presence.asReadonly();

    /** Reactive computed list of currently online users. */
    readonly onLineUsersList = computed<PresenceDelta[]>(() => Array.from(this._presence().values()));
    
    /** Reactive internal state tracking the last recorded presence event message. */
    private readonly _lastEvent = signal<string | null>(null);
    
    /** Last recorded presence event message (read-only). */
    readonly lastEvent = this._lastEvent.asReadonly();

    constructor() {
        this.setVisibilityListener();
        this.destroyRef.onDestroy(() => this.disconnect());
    }

    /**
     * Initializes the SSE connection and subscribes the store to presence event streams.
     */
    init(): void {
        if (this.isInitialized) return;

        const token = localStorage.getItem('jwt_token');
        if (token && !this.abortController) {
            this.isInitialized = true;
            this.abortController = new AbortController();

            this.connectionId = `conn_${Date.now()}`;

            this.sseService.connect(`${this.configService.apiUrl}/notifications/stream?id=${this.connectionId}`, token, this.abortController);
            
            this.sseSubscription = this.sseService.presence$
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                    next: (delta) => this.handleDelta(delta),
                    error: (err) => console.log('sse flow error: ', err)
                });
        }
    }

    /**
     * Configures the Page Visibility API listener to manage automated reconnections upon tab focus.
     */
    setVisibilityListener(): void {
        document.addEventListener('visibilitychange', () => {
            
            if (document.visibilityState === 'hidden') {
                this.backgroundTimestamp = Date.now();
                console.log('app hidden');
            } else if (document.visibilityState === 'visible') {
                console.log('app visible');
                if (this.backgroundTimestamp) {
                    const elapsed = Date.now() - this.backgroundTimestamp;
                    if (elapsed > this.MAX_ABSENCE_GRACE_MS) {
                        console.warn('grace period exhausted, reconnecting...');
                        this.disconnect();
                        this.init();
                    }
                }

                this.backgroundTimestamp = null;
            }
        });
    }

    /**
     * Processes an individual presence delta to update the local state.
     * 
     * @param delta Single user presence state modification.
     */
    private handleDelta(delta: PresenceDelta): void {
        this._presence.update((currentMap) => {
            const nextMap = new Map<number, PresenceDelta>(currentMap);

            if (delta.status === 'CONNECTED')
                nextMap.set(delta.userId, delta);
            else
                nextMap.delete(delta.userId);

            return nextMap;
        });

        this._lastEvent.set(`${delta.username} is now ${delta.status === 'CONNECTED' ? 'Online' : 'Offline'}`);
    }

    /**
     * Synchronizes the entire online user list from a complete state snapshot.
     * 
     * @param rawUsers Complete array of user presence states.
     */
    private syncFullList(rawUsers: PresenceDelta[]): void {
        this._presence.update(() => {
            const nextMap = new Map<number, PresenceDelta>();
            rawUsers.forEach(u => nextMap.set(u.userId, u));
            return nextMap;
        });

        this._lastEvent.set(`Presence list synchronized from server`);
    }

    /**
     * Closes the active SSE connection and resets resources and internal state.
     */
    disconnect(): void {
        if (this.abortController) {
            this.abortController.abort();
            this.abortController = undefined;
        }
        if (this.sseSubscription) {
            this.sseSubscription.unsubscribe();
            this.sseSubscription = undefined;
        }
        
        this.isInitialized = false;
        this._presence.set(new Map<number, PresenceDelta>());
    }

}