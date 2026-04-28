import { Injectable, signal } from "@angular/core";

export type NotificationType = 'success'|'error'|'info'|'warning';

export interface AppNotification{
    message: string;
    type: NotificationType;
    id: number;
}

@Injectable({
    providedIn:'root'
})
export class NotificationService{
    
    readonly notifications = signal<AppNotification[]>([]);

    show(message:string, type:NotificationType = 'info'){
        const id=Date.now();
        this.notifications.update(prev => [...prev,{ id,message, type}]);
        setTimeout(() => this.remove(id),5000);
    }

    remove(id: number){
        this.notifications.update(prev => prev.filter(n => n.id !== id));
    }

}
