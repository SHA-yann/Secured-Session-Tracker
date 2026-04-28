import { computed, DestroyRef, inject, Injectable, signal } from "@angular/core";
import { SseService } from "../../shared/services/sse-sevice";
import { env } from "../../../env";
import { AuthService } from "../../core/secure/authService";

@Injectable({ providedIn: 'root'})
export class PresenceStore{
    private readonly sseService = inject(SseService);
    private readonly auth = inject(AuthService);
    private readonly destroyRef = inject(DestroyRef);
    private abortController?: AbortController;

    private readonly _presence= signal<Record<string,boolean>>({});
    readonly presence = this._presence.asReadonly();
    private readonly _lastEvent = signal<string | null>(null);
    readonly lastEvent = this._lastEvent.asReadonly();
    readonly id = this.auth.userId();

    isOnline = computed(() => {
        if(!this.id) return false;

        return this.presence()[this.id] ?? false;
    });

    init(): void{
        const token=this.auth.getToken();
        if(token){
        this.disconnect();
        this.abortController = new AbortController();
        this.sseService.connect(`${env.sseUrl}/notifications/stream`,token,this.abortController);
        this.sseService.presence$.subscribe(delta => {
            this.updatePresence(delta.userId, delta.online);
        });
        this.destroyRef.onDestroy(() => this.disconnect());
        }
    }

    private updatePresence(userId:number, isOnline:boolean){
        this._presence.update(current => ({...current,[userId]:isOnline}));
        this._lastEvent.set(`${userId} is now ${isOnline ? 'Online': 'Offline'}`);
    }

    disconnect(): void{
        if(this.abortController){
            this.abortController.abort();
            this.abortController = undefined;
        }
    }

}
