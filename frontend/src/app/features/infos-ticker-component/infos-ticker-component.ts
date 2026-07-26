import { Component, signal, effect, inject } from '@angular/core';
import { PresenceStore } from '../../core/services/presenceStore';

interface TickerMessage {
  id: string;
  text: string;
  delay: string; // Stocke le délai en secondes (ex: "0s", "3s")
}

@Component({
  selector: 'app-infos-ticker',
  imports: [],
  templateUrl: './infos-ticker-component.html',
  styleUrl: './infos-ticker-component.css',
})
export class InfosTickerComponent {
  private readonly presenceStore = inject(PresenceStore);
  messagesSignal = signal<TickerMessage[]>([]);
  
  // Permet de suivre quand se termine théoriquement l'entrée du dernier message
  private lastMessageEndTime = 0;

  constructor() {
    effect(() => {
      const lastEvent = this.presenceStore.lastEvent();
      if (lastEvent) {
        this.pushMessage(lastEvent);
      }
    });
  }

  private pushMessage(text: string) {
    const now = Date.now();
    const id = `msg_${now}_${Math.random().toString(36).substring(2, 7)}`;
    
    // Estimation du temps nécessaire pour que le texte libère le point d'entrée (à affiner selon la longueur)
    const spacingDelay = 4000; // 4 secondes d'écart minimum entre le début de deux messages
    
    let targetTime = now;
    if (this.lastMessageEndTime > now) {
      targetTime = this.lastMessageEndTime + spacingDelay;
    }
    
    this.lastMessageEndTime = targetTime;
    const delayInSeconds = Math.max(0, (targetTime - now) / 1000);

    // Injection du message avec son délai propre
    this.messagesSignal.update(msgs => [
      ...msgs, 
      { id, text, delay: `${delayInSeconds}s` }
    ]);

    // Nettoyage du DOM une fois que l'animation de 22s + son délai d'attente sont terminés
    const totalLifetime = (22 + delayInSeconds) * 1000;
    setTimeout(() => {
      this.messagesSignal.update(msgs => msgs.filter(m => m.id !== id));
    }, totalLifetime);
  }
}
