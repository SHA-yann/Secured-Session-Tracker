import { Injectable, signal } from '@angular/core';

/**
 * Represents the notification types supported by the application.
 */
export type NotificationType = 'success' | 'error' | 'info' | 'warning';

/**
 * Data structure representing an active notification item.
 */
export interface AppNotification {
  /** Unique notification identifier (creation timestamp). */
  id: number;
  /** Text message displayed to the end-user. */
  message: string;
  /** Severity level or visual category of the notification. */
  type: NotificationType;
}

/**
 * Global service managing transient notifications (toasts/snackbars).
 * Leverages the Angular Signals API to expose a reactive, immutable state.
 */
@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  /**
   * Signal exposing the current array of active notifications.
   * Intended to be consumed by UI components via the modern control flow (`@for`).
   */
  readonly notifications = signal<AppNotification[]>([]);

  /**
   * Pushes a new notification to the state queue and schedules its automatic removal after 5 seconds.
   * 
   * @param message Text payload to display within the notification.
   * @param type Notification severity level (`'info'` by default).
   */
  show(message: string, type: NotificationType = 'info'): void {
    const id = Date.now();

    // Immutable update of the notification Signal
    this.notifications.update((prev) => [...prev, { id, message, type }]);

    // Auto-dismiss the notification after a 5000ms delay
    setTimeout(() => this.remove(id), 5000);
  }

  /**
   * Manually dismisses an active notification from the queue using its unique identifier.
   * 
   * @param id Unique identifier (`timestamp`) of the target notification.
   */
  remove(id: number): void {
    this.notifications.update((prev) => prev.filter((n) => n.id !== id));
  }
}