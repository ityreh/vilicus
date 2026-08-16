import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiService } from './api.service';
import { LoginRequest, LoginResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private currentUserSubject = new BehaviorSubject<any>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private api: ApiService) {}

  /**
   * Login user
   */
  login(request: LoginRequest): Observable<LoginResponse> {
    return new Observable(observer => {
      this.api.login(request).subscribe({
        next: (response) => {
          this.setToken(response.token);
          this.setUser(response.user);
          this.isAuthenticatedSubject.next(true);
          this.currentUserSubject.next(response.user);
          observer.next(response);
          observer.complete();
        },
        error: (err) => {
          console.error('Login failed:', err);
          observer.error(err);
        }
      });
    });
  }

  /**
   * Register new user
   */
  register(email: string, password: string, name: string): Observable<LoginResponse> {
    return new Observable(observer => {
      this.api.register({ email, password, name }).subscribe({
        next: (response) => {
          this.setToken(response.token);
          this.setUser(response.user);
          this.isAuthenticatedSubject.next(true);
          this.currentUserSubject.next(response.user);
          observer.next(response);
          observer.complete();
        },
        error: (err) => {
          console.error('Registration failed:', err);
          observer.error(err);
        }
      });
    });
  }

  /**
   * Logout user
   */
  logout(): void {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('current_user');
    this.isAuthenticatedSubject.next(false);
    this.currentUserSubject.next(null);
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.hasToken();
  }

  /**
   * Get current user
   */
  getCurrentUser(): any {
    return this.currentUserSubject.getValue();
  }

  /**
   * Get JWT token
   */
  getToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  // Private helper methods
  private setToken(token: string): void {
    localStorage.setItem('auth_token', token);
  }

  private setUser(user: any): void {
    localStorage.setItem('current_user', JSON.stringify(user));
  }

  private getStoredUser(): any {
    const user = localStorage.getItem('current_user');
    return user ? JSON.parse(user) : null;
  }

  private hasToken(): boolean {
    return !!localStorage.getItem('auth_token');
  }
}
