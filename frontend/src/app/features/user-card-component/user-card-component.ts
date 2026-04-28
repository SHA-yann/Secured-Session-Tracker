import { Component, computed, inject, input, output } from '@angular/core';
import { UserResponse } from '../../core/api-client';
import { PresenceStore } from '../../shared/services/presenceStore';
import { CommonModule } from '@angular/common';
import { UserAvatarComponent } from '../user-avatar-component/user-avatar-component';
import { AuthService } from '../../core/secure/authService';

@Component({
  selector: 'app-user-card',
  standalone:true,
  imports: [CommonModule,UserAvatarComponent],
  templateUrl: './user-card-component.html'
})
export class UserCardComponent {
  user = input.required<UserResponse>();
  presenceStore = inject(PresenceStore)
  private readonly auth = inject(AuthService);
  select = output<UserResponse>();
  isSelected = input<boolean>(false);
  
}
