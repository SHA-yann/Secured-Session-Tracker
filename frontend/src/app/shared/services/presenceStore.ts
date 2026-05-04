import { DestroyRef, inject, Injectable, signal } from "@angular/core";
import { SseService } from "../../shared/services/sse-sevice";
import { env } from "../../../env";
import { AuthService } from "../../core/secure/authService";
import { NotificationService } from "./notification-service";
import { HttpClient } from "@angular/common/http";

@Injectable({ providedIn: 'root'})
export class PresenceStore {
    
    private readonly sseService = inject(SseService);
    private readonly auth = inject(AuthService);
    private readonly notify = inject(NotificationService)
    private readonly destroyRef = inject(DestroyRef);
    private abortController?: AbortController;
    private readonly http = inject(HttpClient);

    private isInitialized = false;
    private readonly _presence= signal<Record<string,boolean>>({});
    readonly presence = this._presence.asReadonly();
    private readonly _lastEvent = signal<string | null>(null);
    readonly lastEvent = this._lastEvent.asReadonly();

    constructor(){
        this.init();
        this.setupVisibilityListener();
        
    }

    setupVisibilityListener(){
        document.addEventListener('visibilitychange',
            () => {
                if(document.visibilityState === 'visible'){
                    this.http.get<string[]>(`${env.backUrl}/notifications/onlineList`)
                        .subscribe(users => {
                            const state : Record<string,boolean> = {};
                            users.forEach(u => {
                                const id = u.split(':')[0];
                                if(id)
                                    state[id] = true;
                            });
                            this._presence.set(state);
                        })
                }
        })
    }

    isUserOnline(id:number) {
        console.log('voilà ce que retourne le this.presence()',this.presence());

        return !!this.presence()[id];
    }

    init(): void{
        if(this.isInitialized)
            return;

        const token=this.auth.getToken();
        if(token && !this.abortController){
            this.isInitialized = true;
        this.abortController = new AbortController();
        this.sseService.connect(`${env.backUrl}/notifications/stream`,token,this.abortController);
        this.sseService.presence$.subscribe(delta => {
            this.updatePresence(delta.username, delta.userId, delta.online);
        });
        this.destroyRef.onDestroy(() => this.disconnect());
        }
    }

    private updatePresence(username:string, userId:number, isOnline:boolean){
        this._presence.update(current =>{
            if(isOnline)
                return{...current,[userId]:true};
            else{
                const next ={...current};
                delete next[userId];
                return next;
            }
        });
        this._lastEvent.set(`${username} is now ${isOnline ? 'Online': 'Offline'}`);
    }

    disconnect(): void{
        if(this.abortController){
            this.abortController.abort();
            this.abortController = undefined;
        }
    }

}
