import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter,withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { BASE_PATH } from './core/api-client';

import { routes } from './app.routes';
import { authInterceptor } from './core/secure/authInterceptor';
import { errorInterceptor } from './core/secure/errorInterceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes,withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor,errorInterceptor])),
    {
      provide: BASE_PATH,
      useValue:'http://localhost:8080'
    }
  ]
};
