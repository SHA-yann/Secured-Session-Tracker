import { AfterViewInit, Component, ElementRef, inject, OnInit, output, signal, viewChild } from '@angular/core';
import { GetAllUsersRequestParams, UserResponse, UsersApiService } from '../../core/api-client';
import { CommonModule } from '@angular/common';
import { UserCardComponent } from '../user-card-component/user-card-component';

@Component({
  selector: 'app-user-list',
  standalone:true,
  imports: [CommonModule,UserCardComponent],
  templateUrl: './user-list-component.html',
  styleUrl: './user-list-component.css'
})
export class UserListComponent implements OnInit,AfterViewInit{
  users = signal<UserResponse[]>([]);
  readonly userService = inject(UsersApiService);
  selectedUser = output<UserResponse>();
  selectedId = signal<number|undefined>(undefined);
  currentPage = signal(0);
  isLastPage = signal(false);
  isLoading = signal(false);
  scrollAnchor = viewChild<ElementRef>('anchor');

  ngOnInit(): void {
      this.loadUsers();
  }

  ngAfterViewInit(): void {
      this.setupIntersectionObserver();
  }

  loadUsers():void{
    if(this.isLastPage() || this.isLoading()) return;
    this.isLoading.set(true);

    const params:GetAllUsersRequestParams = {
      pageable:{
        page:this.currentPage(),
        size:15,
        sort:['status,asc']
      }
    };
    this.userService.getAllUsers(params).subscribe({
        next:(page) => {
          this.users.update(prev => [...prev,...page.content ?? []]);
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

  onUserSelect(user:UserResponse): void{
    this.selectedId.set(user.id);
    this.selectedUser.emit(user);
  }
}
