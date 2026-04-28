import { Component, input } from '@angular/core';
import { UserAvatarComponent } from '../user-avatar-component/user-avatar-component';
import { RoleEnum, StatusEnum } from '../../core/api-client';

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
}
