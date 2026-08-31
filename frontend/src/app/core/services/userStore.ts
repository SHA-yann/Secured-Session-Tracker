import { Injectable, signal } from '@angular/core';
import { StatusEnum, UserResponse } from '../api-client';

@Injectable({
  providedIn: 'root',
})
export class UserStore {

  private readonly _users = signal<UserResponse[]>([]);
  readonly users = this._users.asReadonly();

  private readonly _selectedUser = signal<UserResponse|null>(null);
  readonly selectedUser = this._selectedUser.asReadonly();


  setUsers(users: UserResponse[]) {
    if(users.length === 0) return;
    this._users.set(users);
  }

  selectUser(user:UserResponse | null){
    this._selectedUser.set(user);
  }

  updateUserStatus(userId:number, newStatus:StatusEnum): void {
    this._users.update(currentList => currentList.map(user => 
      user.id === userId ? { ...user, status: newStatus } : user));
    
    const currentSelected = this._selectedUser();
    if(currentSelected && currentSelected.id === userId)
      this._selectedUser.set({...currentSelected, status: newStatus});
  }

  updateUser(updatedUser:UserResponse): void {
    this._users.update(currentList => currentList.map(user =>
      user.id === updatedUser.id ? updatedUser : user ));
    
    const currentSelected =this._selectedUser();
    if(currentSelected && currentSelected.id === updatedUser.id)
      this._selectedUser.set(updatedUser);
  }
}
