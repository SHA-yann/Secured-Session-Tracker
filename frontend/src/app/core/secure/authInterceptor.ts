import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from "@angular/common/http";
import { BehaviorSubject, catchError, filter, Observable, switchMap, take, throwError } from "rxjs";
import { AuthenticationApiService } from "../api-client";
import { Router } from "@angular/router";
import { inject } from "@angular/core";
import { AuthResponse } from "../api-client/model/authResponse";
import { NotificationService } from "../services/notification-service";
import { AuthService } from "./authService";

/** Global state flag indicating whether a JWT token refresh operation is currently in progress. */
let isRefreshing = false;

/** RxJS Subject acting as a synchronization lock to queue concurrent requests during token refresh. */
const refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

/**
 * Functional HTTP interceptor handling automatic JWT Bearer token injection
 * and transparent retry of requests blocked by 401 Unauthorized errors.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const auth = inject(AuthService);
    const authApi = inject(AuthenticationApiService);
    const router = inject(Router);
    const token = auth.getToken();
    const notify = inject(NotificationService);

    // Inject initial authorization header into the original request
    const authReq = addTokenHeader(req, token);

    return (next(authReq).pipe(catchError((error: HttpErrorResponse) => {
        // Intercept 401 HTTP errors excluding base authentication endpoints
        if (error.status === 401 && !req.url.includes('/auth/login') && !req.url.includes('/auth/refresh')) {
            return handle401Error(authReq, next, authApi, router);
        }
        
        // Propagate global error notifications to the user interface
        let errorMessage = error.error?.message ?? "Unknown User";
        notify.show(errorMessage, 'warning');
        return throwError(() => error);
    })));

    /**
     * Handles the JWT expiration lifecycle by initiating a token refresh or queuing concurrent requests.
     * 
     * @param request Original HTTP request that triggered the 401 status.
     * @param next Next HTTP handler within the execution chain.
     * @param authApi Remote API service executing the token refresh endpoint.
     * @param router Angular Router service for navigation to the login page upon critical failure.
     */
    function handle401Error(request: HttpRequest<unknown>, next: HttpHandlerFn,
        authApi: AuthenticationApiService, router: Router): Observable<any> {
            
            if (!isRefreshing) {
                // Lock concurrent requests and trigger token refresh
                isRefreshing = true;
                refreshTokenSubject.next(null);

                return authApi.refresh('body', true).pipe(
                    switchMap((response: any) => {
                        const data = response as AuthResponse;
                        const newToken = data.accessToken;
                        
                        // Update access token in the authentication service and release queued requests
                        auth.setToken(newToken);
                        refreshTokenSubject.next(newToken);
                        isRefreshing = false;

                        // Replay original request using the updated access token
                        return next(request.clone({
                            setHeaders: { Authorization: `Bearer ${newToken}` },
                            withCredentials: true
                        }));
                    }),
                    catchError((err) => {
                        // Refresh failure: enforce forced logout and redirect to login page
                        auth.logout();
                        isRefreshing = false;
                        if (!router.url.includes('/login'))
                            router.navigate(['/login'], { queryParams: { reason: 'Refreshing token failed' } });

                        return throwError(() => err);
                    })
                );
            } else {
                // Queue concurrent requests issued while the token refresh operation is active
                return refreshTokenSubject.pipe(
                    filter(token => token !== null),
                    take(1),
                    switchMap(token => next(addTokenHeader(request, token)))
                );
            }
        }
    
    /**
     * Clones the HTTP request to attach the `Authorization: Bearer` header and enable credentialed requests (`withCredentials`).
     * 
     * @param request Initial HTTP request to mutate.
     * @param token Active JWT access token string or `null`.
     */
    function addTokenHeader(request: HttpRequest<unknown>, token: string | null) {
        if (!request.url.includes("/refresh"))
            return request.clone({
                setHeaders: { Authorization: `Bearer ${token}` },
                withCredentials: true
            });
        return request.clone({ withCredentials: true });
    }
}