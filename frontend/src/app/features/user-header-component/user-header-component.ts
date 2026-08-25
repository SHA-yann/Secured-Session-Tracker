import { Component, inject, input } from '@angular/core';
import { RoleEnum, StatusEnum } from '../../core/api-client';
import { AuthService } from '../../core/secure/authService';

@Component({
  selector: 'app-user-header',
  standalone:true,
  templateUrl: './user-header-component.html'
})
export class UserHeaderComponent {
  username = input.required<string>();
  role = input.required<RoleEnum>();
  email= input.required<string>();
  status = input.required<StatusEnum>();
  isSelf = input<boolean>(false);
  isEditing = input<boolean>(false);
  auth = inject(AuthService);
}
