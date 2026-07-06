import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, Validators, NonNullableFormBuilder } from '@angular/forms';
import { AuthRequest } from '../../core/api-client';
import { AuthService } from "../../core/secure/authService";
import { NotificationService } from '../../core/services/notification-service';
import { Router } from '@angular/router';
import { PresenceStore } from '../../core/services/presenceStore';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login-component.html'
})
export class LoginComponent {
  // 2. Utilisez 'NonNullableFormBuilder' pour éviter les types 'string | null'
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly authService = inject(AuthService);
  private readonly notify = inject(NotificationService);
  private readonly router = inject(Router)
  private readonly presence = inject(PresenceStore);

  // 3. Déclaration et initialisation immédiate
  loginForm = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  // Signal typé explicitement
  isLoading = signal<boolean>(false);

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading.set(true);
      const credentials: AuthRequest = this.loginForm.getRawValue();

      this.authService.login(credentials).pipe(
      ).subscribe({
        next: () => {
          if(this.authService.userPayload()?.uStatus === 'ACTIVE' ){
            this.presence.init();
            this.router.navigate(['/dashboard']);
            this.notify.show('Connected!','success');
          }else{
            this.isLoading.set(false);
            this.notify.show('This user is disabled','warning');
          }
        },
        error: (err)=>{
          this.isLoading.set(false);
        }
      })
    }
  }
}
