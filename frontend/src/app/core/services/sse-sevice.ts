import { inject, Injectable, NgZone } from "@angular/core";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import { Subject } from "rxjs";

/**
 * Represents the structure of a user presence change event received via SSE.
 */
export interface PresenceDelta {
    /** Unique user identifier. */
    userId: number;
    /** Username of the targeted user. */
    username: string;
    /** Connection status of the user. */
    status: 'CONNECTED' | 'DISCONNECTED';
}

/**
 * Critical error that halts automatic reconnection attempts (e.g., 4xx authentication error).
 */
export class FatalError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'FatalError';
    }
}

/**
 * Temporary error allowing the SSE library to attempt automatic reconnection (e.g., 5xx server error, network interruption).
 */
export class RetriableError extends Error {
    constructor(message: string) {
        super(message);
        this.name = 'RetriableError';
    }
}

/**
 * Low-level service handling HTTP Server-Sent Events (SSE) connections with Bearer token authentication.
 * Automatically reconnects the stream and re-enters incoming events into the Angular zone (`NgZone`).
 */
@Injectable({
    providedIn: 'root'
})
export class SseService {
    private readonly zone = inject(NgZone);

    /** Internal event emitter propagating received presence deltas. */
    readonly presenceSubject = new Subject<PresenceDelta>();

    /** Public Observable stream exposing real-time user presence updates. */
    readonly presence$ = this.presenceSubject.asObservable();

    /**
     * Establishes an SSE stream connection to the server with JWT Bearer authentication and automatic retry management.
     * 
     * @param url Remote SSE endpoint URL.
     * @param token JWT Bearer authentication token.
     * @param abortController AbortController instance used to cancel the connection on demand.
     */
    async connect(url: string, token: string, abortController: AbortController): Promise<void> {

        fetchEventSource(url, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'text/event-stream'
            },
            signal: abortController.signal,

            /**
             * Intercepts the initial HTTP response from the server to validate headers and status codes.
             */
            onopen: async (response) => {
                if (response.ok && response.headers.get('content-type')?.includes('text/event-stream')) {
                    console.log("SSE flow opened");
                    return;
                }
                if (response.status >= 400 && response.status < 500 && response.status !== 429) {
                    throw new FatalError(`Authentication required on stream:${response.status}`);
                } else {
                    throw new RetriableError("Server unavailable");
                }
            },

            /**
             * Processes incoming messages from the SSE stream and triggers updates within the Angular execution context.
             */
            onmessage: (msg) => {
                if (!msg.data || msg.data.trim() === '')
                    return;

                // Re-entering the event into the Angular Zone to trigger Change Detection
                this.zone.run(() => {
                    try {
                        const serverEvent = JSON.parse(msg.data);
                        if (msg.event === 'online-users' || msg.event === 'presence-update') {

                            this.presenceSubject.next({
                                userId: Number(serverEvent.Id),
                                username: String(serverEvent.name),
                                status: serverEvent.status as 'CONNECTED' | 'DISCONNECTED'
                            });
                        }
                    } catch (error) {
                        console.error("Error while parsing sse payload :", error);
                    }
                });
            },

            /**
             * Notification of clean stream closure initiated by the server.
             */
            onclose: () => {
                console.warn("Flow has been closed by the server.");
            },

            /**
             * Error handler determining the retry or abort strategy for the SSE connection.
             */
            onerror: (err: unknown) => {
                if (err instanceof FatalError) {
                    abortController.abort();
                    throw err;
                }
                console.warn("SSE reconnection...");
            }
        });
    }
}