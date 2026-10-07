import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../../../shared/models';



@Injectable({ providedIn: 'root' })

export class AuthService {

  // Using your API Gateway endpoint
  private readonly API = `http://localhost:8081/api/auth`;

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getStoredUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  /**
   * LOGIN: Matches backend AuthService.loginUser
   * Returns AuthResponse (with token)
   */
  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API}/login`, req).pipe(
      tap(res => this.storeUser(res))
    );
  }

  /**
   * REGISTER: Matches backend AuthController.register -> UserClient.registerUser
   * Returns UserResponse (NO token)
   */
  register(req: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.API}/register`, req);
    // Note: No .pipe(tap) here because the backend doesn't return a token on registration.
    // The user will need to log in manually after registering.
  }

  logout(): void {
    localStorage.removeItem('js_user');
    localStorage.removeItem('js_token');
    this.currentUserSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  // Helper getters
  get currentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  get token(): string | null {
    return localStorage.getItem('js_token');
  }

  get role(): string {
    return this.currentUser?.role ?? '';
  }

  isLoggedIn(): boolean {
    return !!this.token;
  }

  private storeUser(res: AuthResponse): void {
    localStorage.setItem('js_token', res.token);
    localStorage.setItem('js_user', JSON.stringify(res));
    this.currentUserSubject.next(res);
  }

  private getStoredUser(): AuthResponse | null {
    const user = localStorage.getItem('js_user');
    if (!user) return null;
    try {
      return JSON.parse(user);
    } catch {
      return null;
    }
  }

  hasRole(requiredRole: string): boolean {
    const userRole = this.role; // This uses your existing 'get role()' getter
    return userRole.toUpperCase() === requiredRole.toUpperCase();
  }  

  // Optional: If a user can have multiple roles or you want to check against a list
  hasAnyRole(roles: string[]): boolean {
    return roles.some(r => this.hasRole(r));
  }

}

