import { Component, inject, OnInit, signal } from "@angular/core";
import { UserResponse, UsersApiService, GetUserByIdRequestParams } from "../../core/api-client";
import { UserDetailsViewComponent } from "../user-details-view-component/user-details-view-component";
import { UserListComponent } from "../user-list-component/user-list-component";
import { AuthService } from "../../core/secure/authService";
import { NavBarComponent } from "../nav-bar-component/nav-bar-component";
import { PresenceStore } from "../../shared/services/presenceStore";

@Component({
    selector:'app-dashboard',
    standalone:true,
    imports: [UserDetailsViewComponent, UserListComponent, NavBarComponent],
    templateUrl:'./dashboard-component.html'
})

export class DashboardComponent implements OnInit{
    
    private readonly userService = inject(UsersApiService);
    private readonly authService = inject(AuthService);
    private readonly presenceStore = inject(PresenceStore);
    selectedUser = signal<UserResponse|null>(null);
     

    ngOnInit(): void {
        const reqParam:GetUserByIdRequestParams={
            id:this.authService.userId()!
        }
        this.userService.getUserById(reqParam)
                        .subscribe({
                            next:(found) => {
                                this.selectedUser.set(found);
                            },
                            error:(err) => console.log('An error occured during retrieval of user information at login',err)
                        });
        this.presenceStore.init();
    }

    UserSelection(user:UserResponse){
        this.selectedUser.set(user);
    }
}