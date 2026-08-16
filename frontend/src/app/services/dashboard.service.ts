import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { ApiService } from './api.service';
import { DashboardDto, Account, Transaction, CategorySpending, MonthlyTrend } from '../models';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private dashboardSubject = new BehaviorSubject<DashboardDto | null>(null);
  public dashboard$ = this.dashboardSubject.asObservable();

  constructor(private api: ApiService) {}

  /**
   * Fetch dashboard data from API
   */
  loadDashboard(dateRange: string = '30d'): Observable<DashboardDto> {
    return new Observable(observer => {
      this.api.getDashboard(dateRange).subscribe({
        next: (data) => {
          this.dashboardSubject.next(data);
          observer.next(data);
          observer.complete();
        },
        error: (err) => {
          console.error('Error loading dashboard:', err);
          observer.error(err);
        }
      });
    });
  }

  /**
   * Get cached dashboard data
   */
  getDashboard(): DashboardDto | null {
    return this.dashboardSubject.getValue();
  }

  /**
   * Get accounts from dashboard data
   */
  getAccounts(): Account[] {
    return this.dashboardSubject.getValue()?.accounts || [];
  }

  /**
   * Get recent transactions from dashboard data
   */
  getRecentTransactions(): Transaction[] {
    return this.dashboardSubject.getValue()?.recentTransactions || [];
  }

  /**
   * Get spending by category
   */
  getSpendingByCategory(): CategorySpending[] {
    return this.dashboardSubject.getValue()?.spendingByCategory || [];
  }

  /**
   * Get monthly trend
   */
  getMonthlyTrend(): MonthlyTrend[] {
    return this.dashboardSubject.getValue()?.monthlyTrend || [];
  }
}
