import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/secure/authService';
import { PresenceStore } from '../../shared/services/presenceStore';
import { UserAvatarComponent } from '../user-avatar-component/user-avatar-component';

@Component({
  selector: 'app-nav-bar',
  standalone:true,
  imports: [CommonModule,RouterLink,UserAvatarComponent],
  templateUrl: './nav-bar-component.html'
})
export class NavBarComponent {
  readonly auth = inject(AuthService);
  public presenceStore = inject(PresenceStore);
  private readonly router= inject(Router);
  currentUserId = computed(() => this.auth.userId());
  isAuthenticated = computed(() => !!this.currentUserId());

  handleLogAction():void{ 
    if(this.currentUserId()){
      if(this.isAuthenticated()){
        this.auth.logout();
        this.presenceStore.disconnect();
        this.router.navigate(['/login']);
      }
    }else{
      this.router.navigate(['/login']);
    }
  }
}
