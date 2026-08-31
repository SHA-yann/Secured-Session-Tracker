import { CommonModule } from '@angular/common';
import { Component, computed, inject, input, linkedSignal, model, Output, signal } from '@angular/core';
import { StatusEnum, UsersApiService } from '../../core/api-client';
import { AuthService } from '../../core/secure/authService';
import { NotificationService } from '../../core/services/notification-service';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from '../../core/services/urlconfig';
import { UserStore } from '../../core/services/userStore';

@Component({
  selector: 'app-user-stats',
  standalone:true,
  imports: [CommonModule],
  templateUrl: './user-stats-component.html'
})
export class UserStatsComponent {
  userId = input.required<number>();
  status = input.required<StatusEnum>();
  createdBy = input.required<string>();
  updatedBy = input.required<string>();
  createdAt = input.required<string>();
  updatedAt = input.required<string>();
  isSelf = input<boolean>(false);
  auth = inject(AuthService);
  private readonly userApi = inject(UsersApiService);
  private readonly notify = inject(NotificationService);
  private readonly http = inject(HttpClient);
  private readonly configService = inject(ConfigService);
  private readonly userStore = inject(UserStore);
  isProcessing = signal(false);

  deletionButtonText = computed(() => {
    return this.status() === StatusEnum.Inactive ? 'Enable' : 'Disable';
  })
  formattedCreation = computed(() => {
    const date = this.createdAt();
    return date ? new Date(date).toLocaleDateString('fr-FR', { 
      day: '2-digit', month: 'long', year: 'numeric' 
    }) : 'N/A';
  });

  formattedUpdate = computed(() => {
    const date = this.updatedAt();
    return date ? new Date(date).toLocaleDateString('fr-FR', { 
      hour: '2-digit', minute: '2-digit' 
    }) : 'No modification';
  });

  onDisableUser(id:number | undefined): void{
    if(!id) return;
    
    this.isProcessing.set(true);
    this.userApi.deleteUser({id}).subscribe({
      next:() =>{
        this.userStore.updateUserStatus(id,StatusEnum.Inactive);
        this.notify.show('User disabled',"success");
        this.isProcessing.set(false);
      },
      error:(err) =>{
        this.notify.show(`Disabling error:${err}`,"error");
        this.isProcessing.set(false);
      }
    })
  }
  onEnableUser(id:number | undefined): void{
    if(!id) return;
    this.isProcessing.set(true);
    this.http.delete<void>(`${this.configService.apiUrl}/users/${id}/enable`)
        .subscribe({
          next:() =>{
            this.userStore.updateUserStatus(id,StatusEnum.Active);
            this.notify.show('User enabled',"success");
            this.isProcessing.set(false);
          },
          error:(err) =>{
            this.notify.show(`Enabling error:${err}`,"error");
            this.isProcessing.set(false);
          }
        })
  }
}
