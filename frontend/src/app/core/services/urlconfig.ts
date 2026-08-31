import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { lastValueFrom } from 'rxjs';

/**
 * Interface representing the application's global configuration structure.
 */
export interface AppConfig {
  /** Base URL for the backend API. */
  apiUrl: string;
}

/**
 * Service responsible for loading and managing the application's dynamic runtime configuration.
 * Typically executed during startup via `provideAppInitializer` or custom configuration providers.
 */
@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  private readonly http = inject(HttpClient);
  private config!: AppConfig;

  /**
   * Loads the external JSON configuration file.
   * Appends a cache-busting timestamp parameter to bypass browser caching.
   * 
   * @returns A promise resolving to the application configuration (`AppConfig`).
   */
  loadConfig(): Promise<AppConfig> {
    const timestamp = new Date().getTime();

    return lastValueFrom(
      this.http.get<AppConfig>(`/assets/urlconfig.json?v=${timestamp}`)
    )
      .then((config) => {
        this.config = config;
        return this.config;
      })
      .catch((err) => {
        console.error(
          'Failed to load configuration; falling back to default value (localhost)',
          err
        );
        this.config = { apiUrl: 'http://localhost:8080' };
        return this.config;
      });
  }

  /**
   * Retrieves the backend API base URL once the configuration has loaded.
   */
  get apiUrl(): string {
    return this.config?.apiUrl;
  }
}