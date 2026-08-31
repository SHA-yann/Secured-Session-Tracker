import { Component, computed, inject, input, output } from '@angular/core';
import { UserResponse } from '../../core/api-client';
import { PresenceStore } from '../../core/services/presenceStore';
import { CommonModule } from '@angular/common';
import { UserAvatarComponent } from '../user-avatar-component/user-avatar-component';
import { AuthService } from '../../core/secure/authService';
import { UserStore } from '../../core/services/userStore';

@Component({
  selector: 'app-user-card',
  standalone:true,
  imports: [CommonModule,UserAvatarComponent],
  templateUrl: './user-card-component.html'
})
export class UserCardComponent {
  private readonly presenceStore = inject(PresenceStore)
  readonly auth = inject(AuthService);
  readonly userStore = inject(UserStore);
  readonly isSelected = input<boolean>(false);
  readonly id = input.required<number>();
  readonly user = input.required<UserResponse>();
  isOnLine = computed (() => this.presenceStore.presence().has(this.id()));
}
