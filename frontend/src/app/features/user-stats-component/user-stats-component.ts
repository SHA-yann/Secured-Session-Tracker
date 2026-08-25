import { CommonModule } from '@angular/common';
import { Component, computed, inject, input, signal } from '@angular/core';
import { StatusEnum, UsersApiService } from '../../core/api-client';
import { AuthService } from '../../core/secure/authService';
import { NotificationService } from '../../core/services/notification-service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

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
  userApi = inject(UsersApiService);
  notify = inject(NotificationService);
  http = inject(HttpClient);
  isProcessing = signal(false);

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
        this.notify.show('User disabled',"success");
        this.isProcessing.set(false);
      },
      error:(err) =>{
        this.notify.show('User disabled',"error");
        this.isProcessing.set(false);
      }
    })
  }
  onEnableUser(id:number | undefined): void{
    if(!id) return;
    this.isProcessing.set(true);
    this.http.delete<void>(`${environment.backUrl}/users/{id}/enable`)
        .subscribe({
          next:() =>{
        this.notify.show('User enabled',"success");
        this.isProcessing.set(false);
      },
      error:(err) =>{
        this.notify.show('User enabled',"error");
        this.isProcessing.set(false);
      }
        })
  }
}
