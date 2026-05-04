import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from "@angular/common/http";
import { BehaviorSubject, catchError, filter, Observable, switchMap, take, throwError } from "rxjs";
import { AuthenticationApiService } from "../api-client";
import { Router } from "@angular/router";
import { inject } from "@angular/core";
import { AuthResponse } from "../api-client/model/authResponse";
import { NotificationService } from "../../shared/services/notification-service";
import { AuthService } from "./authService";

let isRefreshing=false;
const refreshTockenSubject:BehaviorSubject<any> = new BehaviorSubject<any>(null);

export const authInterceptor: HttpInterceptorFn = (req,next) =>{
    const auth = inject(AuthService);
    const authApi = inject(AuthenticationApiService);
    const router = inject(Router);
    const token = auth.getToken();
    const notify = inject(NotificationService);

    const authReq= addTokenHeader(req,token);

    return(next(authReq).pipe(catchError((error:HttpErrorResponse) =>{
        if(error.status === 401 && !req.url.includes('/auth/login') && !req.url.includes('/auth/refresh')){
            
            
            return handle401Error(authReq,next,authApi,router);
        }
        let errorMessage = error.error?.message ?? "Unknown User";
            notify.show(errorMessage,'warning');
        return throwError(() =>error);
    })));

    function handle401Error(request:HttpRequest<unknown>, next:HttpHandlerFn,
        authApi:AuthenticationApiService, router:Router): Observable<any>{
            
            if(!isRefreshing){
                
                refreshTockenSubject.next(null);
                return authApi.refresh('body',true).pipe(switchMap((response:any) =>{
                    
                    const data =response as AuthResponse;
                    const newToken= data.accessToken;
                    auth.setToken(newToken);
                    refreshTockenSubject.next(newToken);
                    isRefreshing=false;
                    return next(request.clone({setHeaders:{Authorization:`Bearer ${newToken}`},
                            withCredentials:true}));
                }),
                catchError((err) =>{
                    auth.logout();
                    isRefreshing=false;
                    if(!router.url.includes('/login'))
                        router.navigate(['/login'], {queryParams:{reason:'Refreshing token failed'}});

                    return throwError(() => err);
                })
            );
            }else{
                return refreshTockenSubject.pipe(filter(token =>token!==null),
                                                take(1),
                                                switchMap(token => next(addTokenHeader(request, token)))
            );
            }
        }
    
    function addTokenHeader(request:HttpRequest<unknown>, token:string|null){

        if(!request.url.includes("/refresh"))
            return request.clone({setHeaders:{Authorization:`Bearer ${token}`},
            withCredentials:true});
        return request.clone({withCredentials:true});
    }
   
}
