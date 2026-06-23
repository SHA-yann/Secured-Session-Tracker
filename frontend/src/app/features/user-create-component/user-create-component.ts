import { Component, HostListener, inject, output, signal } from '@angular/core';
import { RegisterRequestParams, RoleEnum, StatusEnum, UsersApiService } from '../../core/api-client';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
@Component({
  selector: 'app-user-create-component',
  standalone:true,
  imports: [ReactiveFormsModule],
  templateUrl: './user-create-component.html',
  styleUrl: './user-create-component.css',
})
export class UserCreateComponent {

  protected readonly RoleEnum = RoleEnum;
  protected readonly StatusEnum = StatusEnum;

  private readonly fb = inject(NonNullableFormBuilder);
  private readonly userApi = inject(UsersApiService);
  closed = output<void>();
  userCreated = output<void>();

  isSubmitting = signal(false);

  @HostListener('document:keydown.escape')
  onKeydownHandler():void{
    this.close()
  }

  userForm = this.fb.group({
    username:['',[
      Validators.required,Validators.minLength(3),
      Validators.pattern(/^\w+$/)]],
    email:['',[Validators.required,Validators.email]],
    password:['Temp123!',[Validators.required,Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/)]],
    role:[RoleEnum.User,[Validators.required]],
    status:[StatusEnum.Active,[Validators.required]]
  });

  onSubmit(){
    if(this.userForm.valid){
      this.isSubmitting.set(true);
      const params:RegisterRequestParams = {
        userRequest:this.userForm.getRawValue()
      };
      this.userApi.register(params).subscribe({
        next:() => {
          this.userCreated.emit();
          this.close();
        },
        error:(err) =>{
          console.error('Error while creating user',err);
          this.isSubmitting.set(false);
        }
      });
    }
  }

  close():void{
    this.closed.emit();
  }
}
