import { Routes } from '@angular/router';
import { LoginComponent } from './features/login-component/login-component';
import { guard } from './core/secure/guard';
import { DashboardComponent } from './features/dashboard-component/dashboard-component';

export const routes: Routes = [
{ path: 'login', component: LoginComponent},
{ path: '', redirectTo: 'login', pathMatch: 'full'},
{
    path: 'dashboard',
    component: DashboardComponent,
    canActivate:[guard],
    data: {roles: ['ROLE_USER', 'ROLE_ADMIN']}  // RBAC: Les utilisateurs avec les rôles 'Role_User' ou 'Role_Admin' peuvent accéder à cette route
},

];