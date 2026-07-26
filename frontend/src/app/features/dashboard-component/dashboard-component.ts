import { Component, inject, OnInit, signal } from "@angular/core";
import { UserResponse, UsersApiService, GetUserByIdRequestParams } from "../../core/api-client";
import { UserDetailsViewComponent } from "../user-details-view-component/user-details-view-component";
import { UserListComponent } from "../user-list-component/user-list-component";
import { AuthService } from "../../core/secure/authService";
import { NavBarComponent } from "../nav-bar-component/nav-bar-component";
import { InfosTickerComponent } from "../infos-ticker-component/infos-ticker-component";

@Component({
    selector:'app-dashboard',
    standalone:true,
    imports: [UserDetailsViewComponent, UserListComponent,
    NavBarComponent, InfosTickerComponent],
    templateUrl:'./dashboard-component.html'
})

export class DashboardComponent implements OnInit{
    
    private readonly userService = inject(UsersApiService);
    private readonly authService = inject(AuthService);
    selectedUser = signal<UserResponse|null>(null);
     

    ngOnInit(): void {
        const reqParam:GetUserByIdRequestParams={
            id:this.authService.userId()!
        }
        if(this.authService.isAdmin())
            this.userService.getUserById(reqParam)
                        .subscribe({
                            next:(found) => {
                                this.selectedUser.set(found);
                            },
                            error:(err) => console.log('An error occured during retrieval of user information at login',err)
                        });
        
    }

    UserSelection(user:UserResponse){
        this.selectedUser.set(user);
    }
}