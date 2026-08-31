import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { NotificationService } from "../services/notification-service";
import { catchError, throwError } from "rxjs";
import { inject } from "@angular/core";

/**
 * Functional HTTP interceptor handling centralized interception and notification feedback
 * for network disruptions, system errors (5xx), and client application errors (4xx).
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const notify = inject(NotificationService);

    // Bypass SSE/EventSource streaming endpoints to prevent noisy error notifications during reconnection loops
    if (req.url.includes('/notifications/stream')) {
        return next(req);
    }

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            let errorMessage = error.error?.message;

            switch (error.status) {
                case 0:
                    errorMessage ||= "Unable to contact server... Network error.";
                    notify.show(errorMessage, 'error');
                    break;
                case 401:
                    // Primarily handled by authInterceptor; acts as a fallback if the request bypasses authentication logic
                    if (!req.url.includes('/auth/login') && !req.url.includes('/auth/refresh')) {
                        errorMessage ||= "User unauthorized";
                        notify.show(errorMessage, 'warning');
                    }
                    break;
                case 403:
                    errorMessage ||= "Forbidden, you can't perform this action";
                    notify.show(errorMessage, 'warning');
                    break;
                case 429:
                    errorMessage ||= "Too many requests, please wait";
                    notify.show(errorMessage, 'info');
                    break;
                default:
                    if (error.status >= 500) {
                        errorMessage ||= "Internal server error";
                        notify.show(errorMessage, 'error');
                    } else {
                        errorMessage ||= `An unexpected error occurred (${error.status})`;
                        notify.show(errorMessage, 'error');
                    }
                    break;
            }

            return throwError(() => error);
        })
    );
};