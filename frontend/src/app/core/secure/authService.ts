import { computed, inject, Injectable, signal } from "@angular/core";
import { jwtDecode } from 'jwt-decode';
import { AuthenticationApiService } from "../api-client/api/api";
import { AuthRequest } from "../api-client";
import { tap } from "rxjs";
import { PresenceStore } from "../services/presenceStore";
import { Router } from "@angular/router";
import { HttpClient } from "@angular/common/http";
import { ConfigService } from "../services/urlconfig";

/**
 * Represents the decoded payload structure of the authentication JWT token.
 */
export interface TokenPayload {
    /** Primary subject identifier / username. */
    sub: string;
    /** Unique user identifier. */
    ID: number;
    /** List of roles assigned to the user. */
    Role: string[];
    /** Current user status. */
    uStatus: string;
    /** Token issuance timestamp (in seconds / Unix epoch). */
    iat: number;
    /** Token expiration timestamp (in seconds / Unix epoch). */
    exp: number;
}

/**
 * Centralized authentication service for the application.
 * Manages the JWT token lifecycle, user session state, and global logout procedures.
 */
@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly authApi = inject(AuthenticationApiService);
    private readonly http = inject(HttpClient);
    private readonly _token = signal<string | null>(localStorage.getItem('jwt_token'));
    private readonly presence = inject(PresenceStore);
    private readonly router = inject(Router);
    private readonly configService = inject(ConfigService);
    
    /**
     * Derived signal computing the decoded and valid JWT payload.
     * Returns `null` if the token is invalid, unparsable, or expired.
     */
    readonly userPayload = computed<TokenPayload | null>(() => {
        const token = this._token();
        if (!token) return null;
        try {
            const decoded = jwtDecode<TokenPayload>(token);
            const isValid = decoded.exp * 1000 > Date.now();
            return isValid ? decoded : null;
        } catch (e) {
            console.error('Token decoding error:', e);
            return null;
        }
    });

    /** Computed signal returning the username of the current active session. */
    readonly username = computed(() => this.userPayload()?.sub ?? null);

    /** Computed signal returning the unique user ID of the current active session. */
    readonly userId = computed(() => this.userPayload()?.ID ?? null);

    /** Computed signal returning the application status of the user. */
    readonly userStatus = computed(() => this.userPayload()?.uStatus ?? null);

    /** Computed signal indicating whether the user holds the administrator role (`ROLE_ADMIN`). */
    readonly isAdmin = computed(() => this.userPayload()?.Role.includes('ROLE_ADMIN') || false);
    
    /**
     * Authenticates the user against the remote API and stores the returned JWT access token.
     * 
     * @param credentials Login credentials payload (`AuthRequest`).
     */
    login(credentials: AuthRequest) {
        return this.authApi.login({ authRequest: credentials }).pipe(
            tap((response: any) => {
                const token = response?.accessToken;
                if (token) this.setToken(token);
            })
        );
    }

    /**
     * Persists the JWT token into local storage and updates internal reactive state.
     * 
     * @param token JWT access token string to persist.
     */
    setToken(token: string): void {
        localStorage.setItem('jwt_token', token);
        this._token.set(token);
    }

    /**
     * Retrieves the current raw value of the JWT access token.
     */
    getToken(): string | null {
        return this._token();
    }

    /**
     * Executes the global logout process: terminates the SSE stream, notifies the backend server,
     * and clears local reactive state and persistent session storage.
     */
    logout(): void {

        this.presence.disconnect();
        this.http.post<void>(`${this.configService.apiUrl}/auth/logout?id=${this.presence.connectionId}`, {})
            .subscribe({
                next: (response) => {
                    console.log('Backend logout successful :' + `${response}`);
                    localStorage.removeItem('jwt_token');
                    this._token.set(null);
                    this.router.navigate(['/login']);
                    console.log('logout finalized');
                },
                error: (err) => {
                    console.log('Backend returned an error ', err);
                    localStorage.removeItem('jwt_token');
                    this._token.set(null);
                    this.router.navigate(['/login']);
                    console.log('local logout finalized');
                }
            });
    }
}