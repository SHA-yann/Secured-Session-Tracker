import { inject, Injectable, NgZone } from "@angular/core";
import { fetchEventSource } from "@microsoft/fetch-event-source"
import { Subject } from "rxjs";

 interface PresenceDelta{
    userId:number;
    online:boolean;
 }

 export class FatalError extends Error{
    constructor(message:string){
        super(message);
        this.name='FatalError';
    }
 }

 export class RetriableError extends Error{
    constructor(message:string){
        super(message)
        this.name='RetriableError';
    }
 }

@Injectable({
    providedIn: 'root'
})
export class SseService {
    private readonly zone = inject(NgZone);
    readonly presenceSubject = new Subject<PresenceDelta>();
    readonly presence$ = this.presenceSubject.asObservable();

    async connect(url: string, token: string, abortController: AbortController){

        await fetchEventSource(url,{
            method:'GET',
            headers:{'Authorization':`Bearer ${token}`,
                    'Accept':'text/event-stream'
            },
            signal: abortController.signal,
            async onopen(response){
                if(response.ok && response.headers.get('content-type')?.includes('text/event-stream')){
                    console.log("SSE flow opened");
                    return;
                }
                if(response.status>=400 && response.status<500 && response.status!== 429){
                    throw new FatalError(`Authentication required on stream:${response.status}`);
                }else{
                    throw new RetriableError("Server unavailable");
                }
            },
            onmessage: (msg) => {
                if(msg.event === 'presence-update'){
                    this.zone.run(() => {
                        const [userid,status] = msg.data.split(":");
                    this.presenceSubject.next({
                        userId:+userid,
                        online: status === 'CONNECTED'
                        });
                    });
                }
            },
            onclose: () => {
                console.warn("Flow has been closed by the server.");
            },
            onerror: (err) => {
                if(err instanceof FatalError){
                    abortController.abort()
                    throw err;
                }
                console.warn("SSE reconnection...")
            }
         }
        );
    }

}