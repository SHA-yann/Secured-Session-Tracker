import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-user-avatar',
  standalone:true,
  imports: [],
  templateUrl: './user-avatar-component.html'
})
export class UserAvatarComponent {
  username = input.required<string>();
  size = input<string>('md');

  initials=computed(() => this.username().charAt(0).toUpperCase());

  bgColor = computed(() => {
    const colors = ['#3b82f6','#10b981','#f59e0b','#ef4444','#8b5cf6','#ec4899'];
  let hash=0;
  for(let i=0; i<this.username().length;i++){
    hash=this.username().charCodeAt(i) + ((hash<<5)-hash);
  }
  return colors[Math.abs(hash) % colors.length];
  });
  
  getSizeClasses() {
  const sizes: Record<string, string> = {
    'sm': 'w-8 h-8',
    'md': 'w-12 h-12',
    'lg': 'w-16 h-16',
    'xl': 'w-24 h-24'
  };
  return sizes[this.size()] || sizes['md'];
}

getTextSizeClasses() {
  const textSizes: Record<string, string> = {
    'sm': 'text-xs',
    'md': 'text-lg',
    'lg': 'text-2xl',
    'xl': 'text-4xl'
  };
  return textSizes[this.size()] || textSizes['md'];
}
}
