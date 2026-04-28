import { HttpErrorResponse, HttpInterceptorFn } from "@angular/common/http";
import { NotificationService } from "../../shared/services/notification-service";
import { catchError, throwError } from "rxjs";
import { inject } from "@angular/core";

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const notify = inject(NotificationService);

    if(req.url.includes('/notifications/stream'))
                return next(req);

    return next(req).pipe(
        catchError((error:HttpErrorResponse) =>{

            if(error.status !== 401){
                let errorMessage = "Ooops! An error occured,";
            if(error.status === 0)
                errorMessage = "unable to contact Server...Network Error.";
            else if(error.status >= 500)
                errorMessage = "Server side error";
            else if(error.status === 403){
                errorMessage = "Forbidden, you can't perform this action";
                notify.show(errorMessage,'warning');
            }else if(error.status === 429){
                errorMessage = "Too much requests at the time, please wait";
                notify.show(errorMessage,'info');
            }
                notify.show(errorMessage,'error');
            }
        return throwError(() => error);

        })
    );
};