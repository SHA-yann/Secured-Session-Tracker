import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/secure/authService';
import { PresenceStore } from '../../shared/services/presenceStore';

@Component({
  selector: 'app-nav-bar',
  standalone:true,
  imports: [CommonModule,RouterLink],
  templateUrl: './nav-bar-component.html'
})
export class NavBarComponent {
  readonly authService = inject(AuthService);
  public presenceStore = inject(PresenceStore);
  private readonly router= inject(Router);
  

  handleAuthAction():void{ 
    if(this.authService.userId()){
      if(this.presenceStore.isOnline()){
        this.authService.logout();
        this.presenceStore.disconnect();
        this.router.navigate(['/login']);
      }
    }else{
      this.router.navigate(['/login']);
    }
  }
}
