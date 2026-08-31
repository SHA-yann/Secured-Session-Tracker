import { CanActivateFn, Router } from "@angular/router";
import { inject } from "@angular/core";
import { NotificationService } from "../services/notification-service";
import { AuthService } from "./authService";
import { PresenceStore } from "../services/presenceStore";

/**
 * Functional route guard managing JWT token authentication and Role-Based Access Control (RBAC).
 */
export const guard: CanActivateFn = (route, state) => {
    const router = inject(Router);
    const authService = inject(AuthService);
    const presence = inject(PresenceStore);
    const notify = inject(NotificationService);

    const payload = authService.userPayload();

    // 1. Verify authentication and JWT token validity
    if (!payload) {
        notify.show("You are not authenticated!", 'warning');
        router.navigate(['/login'], { 
            queryParams: { reason: 'Unauthorized user', returnUrl: state.url } 
        });
        return false;
    }

    // 2. Idempotent initialization of the SSE presence service
    presence.init();

    // 3. Verify Role-Based Access Control (RBAC) permissions
    const requiredRoles = route.data['roles'] as string[] | undefined;

    if (!requiredRoles || requiredRoles.length === 0) {
        return true;
    }

    const userRoles = payload.Role || [];
    const hasRole = userRoles.some(role => requiredRoles.includes(role));

    if (hasRole) {
        return true;
    }

    notify.show("Forbidden, you can't perform this action", 'warning');
    return false;
};