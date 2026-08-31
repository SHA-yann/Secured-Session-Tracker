import { Injectable, signal } from '@angular/core';
import { StatusEnum, UserResponse } from '../api-client';

/**
 * Centralized reactive store managing the local state of users and the currently selected user.
 * Leverages the Angular Signals API to ensure optimal immutability and fine-grained reactivity.
 */
@Injectable({
  providedIn: 'root',
})
export class UserStore {
  /** Internal reactive state containing the global user list. */
  private readonly _users = signal<UserResponse[]>([]);
  
  /** Read-only signal exposing the global user list. */
  readonly users = this._users.asReadonly();

  /** Internal reactive state storing the user currently selected in the UI. */
  private readonly _selectedUser = signal<UserResponse | null>(null);
  
  /** Read-only signal exposing the currently selected user. */
  readonly selectedUser = this._selectedUser.asReadonly();

  /**
   * Replaces the entire user collection held within the store.
   * 
   * @param users New list of users to apply. Ignored if the array is empty.
   */
  setUsers(users: UserResponse[]): void {
    if (users.length === 0) return;
    this._users.set(users);
  }

  /**
   * Sets the currently active user or clears the selection by setting it to `null`.
   * 
   * @param user Targeted user entity or `null` to clear selection.
   */
  selectUser(user: UserResponse | null): void {
    this._selectedUser.set(user);
  }

  /**
   * Immutably updates the status of a specific user.
   * Automatically synchronizes state if the modified user matches the currently selected entity.
   * 
   * @param userId Unique identifier of the target user.
   * @param newStatus New application status to assign.
   */
  updateUserStatus(userId: number, newStatus: StatusEnum): void {
    this._users.update((currentList) =>
      currentList.map((user) =>
        user.id === userId ? { ...user, status: newStatus } : user
      )
    );

    const currentSelected = this._selectedUser();
    if (currentSelected && currentSelected.id === userId) {
      this._selectedUser.set({ ...currentSelected, status: newStatus });
    }
  }

  /**
   * Replaces an existing user's data with its updated instance.
   * Maintains state consistency for the selected user if it references the same entity.
   * 
   * @param updatedUser New instance representing the updated user data.
   */
  updateUser(updatedUser: UserResponse): void {
    this._users.update((currentList) =>
      currentList.map((user) =>
        user.id === updatedUser.id ? updatedUser : user
      )
    );

    const currentSelected = this._selectedUser();
    if (currentSelected && currentSelected.id === updatedUser.id) {
      this._selectedUser.set(updatedUser);
    }
  }
}