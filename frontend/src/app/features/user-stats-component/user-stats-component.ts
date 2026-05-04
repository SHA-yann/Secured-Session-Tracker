import { CommonModule } from '@angular/common';
import { Component, computed, inject, input } from '@angular/core';
import { StatusEnum } from '../../core/api-client';
import { AuthService } from '../../core/secure/authService';

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
  auth = inject(AuthService);

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
}
