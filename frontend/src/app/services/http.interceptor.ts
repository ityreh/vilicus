import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

/**
 * HTTP Interceptor for JWT token injection and error handling
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Get JWT token from localStorage
    const token = localStorage.getItem('auth_token');

    // Clone request and add JWT token to headers if it exists
    if (token && !req.url.includes('/auth/')) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        // Handle 401 Unauthorized - redirect to login
        if (error.status === 401) {
          localStorage.removeItem('auth_token');
          // TODO: Redirect to login
          console.error('Unauthorized - redirecting to login');
        }

        // Handle other errors
        const errorMessage = error.error?.message || error.statusText || 'An error occurred';
        console.error(`HTTP Error ${error.status}: ${errorMessage}`);

        return throwError(() => ({
          status: error.status,
          message: errorMessage
        }));
      })
    );
  }
}
