import { AfterViewInit, Component, computed, ElementRef, inject, OnInit, output, signal, viewChild } from '@angular/core';
import { GetAllUsersRequestParams, UserResponse, UsersApiService } from '../../core/api-client';
import { CommonModule } from '@angular/common';
import { UserCardComponent } from '../user-card-component/user-card-component';
import { UserCreateComponent } from '../user-create-component/user-create-component';
import { AuthService } from '../../core/secure/authService';
import { UserStore } from '../../core/services/userStore';

@Component({
  selector: 'app-user-list',
  standalone:true,
  imports: [CommonModule,UserCardComponent,UserCreateComponent],
  templateUrl: './user-list-component.html',
  styleUrl: './user-list-component.css'
})
export class UserListComponent implements OnInit,AfterViewInit{
  private readonly userService = inject(UsersApiService);
  private readonly userStore = inject(UserStore);
  auth = inject(AuthService);

  readonly users = this.userStore.users;
  readonly selectedUser = this.userStore.selectedUser;
  selectedId = computed (() => this.selectedUser()?.id ?? 0);
  currentPage = signal(0);
  isLastPage = signal(false);
  isLoading = signal(false);
  isCreateModalOpen = signal(false);
  scrollAnchor = viewChild<ElementRef>('anchor');

  ngOnInit(): void {
      this.loadUsers();
  }
  ngAfterViewInit(): void {
      this.setupIntersectionObserver();
  }
    handleUserCreated(){
    this.loadUsers();
  }
  loadUsers():void{
    if(this.isLastPage() || this.isLoading()) return;
    this.isLoading.set(true);

    const params:GetAllUsersRequestParams = {
      pageable:{
        page:this.currentPage(),
        size:10,
        sort:['status,asc']
      }
    };
    this.userService.getAllUsers(params).subscribe({
        next:(page) => {
          this.userStore.setUsers(page.content ?? []);
          if(page.last)
            this.isLastPage.set(page.last);
          this.currentPage.update(p => p+1);
          this.isLoading.set(false);
        },
        error: (err) => {
          console.error('Pagination error:',err);
          this.isLoading.set(false);
        }
    });
  }

  private setupIntersectionObserver(): void{
    const observer = new IntersectionObserver((entries) => {
      if(entries[0].isIntersecting && !this.isLastPage())
        this.loadUsers();
    },{threshold:0.5});

    const anchor = this.scrollAnchor()?.nativeElement;
    if(anchor)
      observer.observe(anchor);
  }

}
