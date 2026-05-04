import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { NotificationService } from '../../shared/services/notification-service';

@Component({
  selector: 'app-toast',
  standalone:true,
  imports: [CommonModule],
  templateUrl: './toast-component.html',
  styleUrl: './toast-component.css',
})
export class ToastComponent {
  readonly notify = inject(NotificationService);

  getNotificationClass(type:string): string{
    const baseClasses = 'p-2 rounded-lg shadow-lg border-l-2 flex items-center min-w-[200px] transition-all duration-200';
    const typeClasses : Record <string, string> = {
      success:'bg-green-50 border-green-400 text-green-500',
      error:'bg-red-50 border-red-300 text-red-500',
      info:'bg-blue-50 border-blue-300 text-blue-500',
      warning:'bg-yellow-50 border-yellow-300 text-yellow-500'
    };
    return `${baseClasses} ${typeClasses[type] ?? typeClasses['info']}`;
  }
}
