import { computed, DestroyRef, inject, Injectable, signal } from "@angular/core";
import { PresenceDelta, SseService } from "./sse-sevice";
import { env } from "../../../env";
import { NotificationService } from "./notification-service";
import { HttpClient } from "@angular/common/http";
import { Subscription } from "rxjs";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";

@Injectable({ providedIn: 'root'})
export class PresenceStore {
    
    private readonly sseService = inject(SseService);
    private readonly notify = inject(NotificationService)
    private readonly destroyRef = inject(DestroyRef);
    private readonly http = inject(HttpClient);

    private abortController?: AbortController;
    private isInitialized = false;
    connectionId: string = '';
    private sseSubscription?: Subscription;
    private backgroundTimestamp: number|null=null ;
    private readonly MAX_ABSENCE_GRACE_MS = 45000;

    private readonly _presence= signal<Map<number,PresenceDelta>>(new Map<number,PresenceDelta>());
    readonly presence = this._presence.asReadonly();

    readonly onLineUsersList = computed<PresenceDelta[]>(() => Array.from(this._presence().values()));
    
    private readonly _lastEvent = signal<string | null>(null);
    readonly lastEvent = this._lastEvent.asReadonly();

    constructor(){

        this.setVisibilityListener();
        this.destroyRef.onDestroy(() => this.disconnect());
        
    }
    init(): void{
        if(this.isInitialized) return;

        const token=localStorage.getItem('jwt_token');
        if(token && !this.abortController){
            this.isInitialized = true;
            this.abortController = new AbortController();

            this.connectionId = `conn_${Date.now()}`;

            this.sseService.connect(`${env.backUrl}/notifications/stream?id=${this.connectionId}`,token,this.abortController);
            
            this.sseSubscription =this.sseService.presence$
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                next: (delta) => this.handleDelta(delta)
                ,
                error: (err) => console.log('sse flow error: ',err)
                });
        }
    }

    setVisibilityListener(): void {
        document.addEventListener('visibilitychange', () => {
            
            if(document.visibilityState == 'hidden'){
                this.backgroundTimestamp = Date.now();
                console.log('app hidden');
            }else if (document.visibilityState === 'visible') {
                console.log('app visible');
                if(this.backgroundTimestamp){
                    const elapsed = Date.now() - this.backgroundTimestamp;
                    if(elapsed > this.MAX_ABSENCE_GRACE_MS){
                        console.warn('grace period exhausted,reconnecting...');
                        this.disconnect();
                        this.init();
                    }
                }

                this.backgroundTimestamp = null;
                
            }
        });
    }


    private handleDelta(delta:PresenceDelta):void{
        this._presence.update((currentMap) =>{
            const nextMap = new Map<number,PresenceDelta>(currentMap);

            if(delta.status === 'CONNECTED')
                nextMap.set(delta.userId,delta);
            else
                nextMap.delete(delta.userId);

            return nextMap;
        });

        this._lastEvent.set(`${delta.username} is now ${delta.status === 'CONNECTED' ? 'Online' : 'Offline'}`);
    }

    private syncFullList(rawUsers:PresenceDelta[]):void{
        this._presence.update(() =>{
            const nextMap = new Map<number,PresenceDelta>();
            rawUsers.forEach(u => nextMap.set(u.userId, u));
            return nextMap;
        });

        this._lastEvent.set(`Presence list synchronized from server'}`);
    }

    disconnect(): void{
        if(this.abortController){
            this.abortController.abort();
            this.abortController = undefined;
        }
        if(this.sseSubscription){
            this.sseSubscription.unsubscribe();
            this.sseSubscription = undefined;
        }
        
        this.isInitialized = false;
        this._presence.set(new Map<number,PresenceDelta>());
    }

}
