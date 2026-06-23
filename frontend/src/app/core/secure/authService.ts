import { computed, inject, Injectable, signal } from "@angular/core";
import { jwtDecode } from 'jwt-decode';
import { AuthenticationApiService } from "../api-client/api/api";
import { AuthRequest } from "../api-client";
import { BehaviorSubject, tap } from "rxjs";

export interface TokenPayload {
    sub: string; // username
    ID: number; //userid
    Role: string[]; // roles de l'utilisateur
    uStatus: string
    iat: number; // issued at
    exp: number; // expiration date
}
@Injectable({
    providedIn: 'root'
})
export class AuthService{

    private userSubject = new BehaviorSubject<any|null>(null);
    private readonly authApi = inject(AuthenticationApiService);   
    private readonly _token = signal<string | null>(localStorage.getItem('jwt_token'));
    
    readonly userPayload = computed<TokenPayload | null>(() => {
        const token = this._token();
        if(!token) return null;
        try{
            
            const decoded =jwtDecode<TokenPayload>(token);
            const isValid = decoded.exp*1000 > Date.now();
            return isValid ? decoded : null;
        }catch(e){
            console.error('Token decoding error:',e);
            return null;
        }
    });

    readonly username = computed(() => this.userPayload()?.sub ?? null);
    readonly user$ = this.userSubject.asObservable();
    readonly userId = computed(() => this.userPayload()?.ID ?? null);
    readonly isAdmin = computed(() => this.userPayload()?.Role.includes('ROLE_ADMIN') || false);
    
    login(credentials: AuthRequest){
        return this.authApi.login({authRequest: credentials}).pipe(
            tap((response:any) =>{
                const token = response?.accessToken;
                if(token) this.setToken(token);
            })
        );
    }

    setToken(token: string): void{
        localStorage.setItem('jwt_token',token);
        this._token.set(token);
    }

    getToken(): string | null{
        return this._token();
    }

    logout(): void{
        localStorage.removeItem('jwt_token');
        this._token.set(null);
    }
}