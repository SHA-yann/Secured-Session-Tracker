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
  readonly user = input.required<UserResponse>();
  readonly presenceStore = inject(PresenceStore)
  readonly auth = inject(AuthService);
  readonly select = output<UserResponse>();
  readonly isSelected = input<boolean>(false);
  readonly id = input.required<number>();
  isOnLine = computed (() => this.presenceStore.isUserOnline(this.id()));
}
