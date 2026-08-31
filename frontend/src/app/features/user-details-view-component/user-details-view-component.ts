import { Component, computed, effect, inject, input, linkedSignal, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserResponse, UsersApiService, UpdateUserRequestParams, UpdateRequest, RoleEnum, StatusEnum } from '../../core/api-client';
import { AuthService } from '../../core/secure/authService';
import { CommonModule } from '@angular/common';
import { UserAvatarComponent } from '../user-avatar-component/user-avatar-component';
import { UserHeaderComponent } from '../user-header-component/user-header-component';
import { UserStatsComponent } from '../user-stats-component/user-stats-component';
import { UserStore } from '../../core/services/userStore';

@Component({
  selector: 'app-user-details-view',
  imports: [CommonModule,ReactiveFormsModule,UserAvatarComponent
    ,UserHeaderComponent,UserStatsComponent],
  templateUrl: './user-details-view-component.html'
})
export class UserDetailsViewComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UsersApiService);
  readonly authService = inject(AuthService);
  private readonly userStore = inject(UserStore);

  user = this.userStore.selectedUser;

  isEditing = linkedSignal({
    source: this.user,
    computation: () => false
  })
  isSelf = computed(() => this.user()?.username === this.authService.username());

  profileForm = this.fb.group({
    email:['',[Validators.required, Validators.email]],
    role:[{value:null as RoleEnum|null,disabled:!this.authService.isAdmin()}],
    status:[{value:null as StatusEnum|null,disabled:!this.authService.isAdmin()}]
  });

  constructor(){

    effect(() => {
      const u = this.user()!;
      this.profileForm.patchValue({
        email: u.email,
        role: u.role,
        status: u.status
      });
    })
  }

  toggleEdit():void{
    if(this.isEditing()){
      const u = this.user()!;
      this.profileForm.patchValue({
        email: u.email,
        role: u.role,
        status: u.status
      });
      this.isEditing.set(false);
    }else
      this.isEditing.set(true);
  }

  saveChanges():void{
    if(this.profileForm.valid){
      const raw = this.profileForm.getRawValue();
      const updateData :UpdateRequest={};

      if(raw.email)
        updateData.email = raw.email;

      if(this.authService.isAdmin()){
        if(raw.role)
          updateData.role = raw.role;
        if(raw.status)
          updateData.status = raw.status;
      }

    const updateReqParam:UpdateUserRequestParams = {
      id : this.user()?.id!,
      updateRequest : updateData
    };

      this.userService.updateUser(updateReqParam)
                      .subscribe({
                        next:(updated) => {
                          this.userStore.updateUser(updated);
                          this.isEditing.set(false);
                          console.log('User {} updated',updated.username);
                        },
                        error: (err) => console.log('update error:',err)
                      });
    }
  }

}
