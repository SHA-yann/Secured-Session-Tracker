import { CanActivateFn, Router } from "@angular/router";
import { inject } from "@angular/core";
import { NotificationService } from "../../shared/services/notification-service";
import { AuthService } from "./authService";

export const guard: CanActivateFn = (route,state) =>{
    const router= inject(Router);
    const authService = inject(AuthService);
    const notify = inject(NotificationService);
    

    if(!authService.getToken()){
        notify.show("You are not authenticated!",'warning');
        if(!router.url.includes('/login')){
            notify.show("Redirecting to login page.",'info');
            router.navigate(['/login'], {queryParams:{reason:'Unauthorized user'}});
        }
        return  false;
    } 
     const requiredRoles = route.data['roles'] as string[] | undefined;
     const userRoles = authService.userPayload()?.Role || [];
     
      //console.log('la valeur de requiredRoles est:{}',requiredRoles);
      //console.log('la valeur de userRoles est:{}',userRoles);
     if(!requiredRoles || requiredRoles.length === 0)
        return true;

     if(userRoles.some(role => requiredRoles.includes(role))){
        return true;
     }else{
        notify.show("Forbidden, you can't perform this action",'warning');
        return false;
     }
}
