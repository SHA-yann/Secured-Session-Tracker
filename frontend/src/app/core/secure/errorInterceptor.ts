import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { NotificationService } from "../services/notification-service";
import { catchError, throwError } from "rxjs";
import { inject } from "@angular/core";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const notify = inject(NotificationService);

    if(req.url.includes('/notifications/stream'))
                return next(req);

    return next(req).pipe(
        catchError((error:HttpErrorResponse) =>{

            let errorMessage='';

            if(error.error.status === 401){
                errorMessage = error.error?.message ?? "User unauthorized";
                notify.show(errorMessage,'warning');
            }else if(error.status === 0){
                errorMessage = error.error?.message ?? "unable to contact Server...Network Error.";
                notify.show(errorMessage,'error');
            }else if(error.status >= 500){
                errorMessage = error.error?.message ?? "Server side error";
                notify.show(errorMessage,'error');
            }else if(error.status === 403){
                errorMessage = error.error?.message ?? "Forbidden, you can't perform this action";
                notify.show(errorMessage,'warning');
            }else if(error.status === 429){
                errorMessage = error.error?.message ?? "Too much requests, please wait";
                notify.show(errorMessage,'info');
            }else
                notify.show(errorMessage,'error');
        return throwError(() => error);
        })
    );
};