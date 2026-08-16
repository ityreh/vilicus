import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { AnalyticsDto } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  constructor(private api: ApiService) {}

  /**
   * Get analytics data
   */
  getAnalytics(dateRange?: string): Observable<AnalyticsDto> {
    return this.api.getAnalytics(dateRange);
  }

  /**
   * Get category breakdown
   */
  getCategoryBreakdown(dateRange?: string): Observable<any> {
    return this.api.getCategoryBreakdown(dateRange);
  }

  /**
   * Get top merchants
   */
  getTopMerchants(limit: number = 10): Observable<any> {
    return this.api.getTopMerchants(limit);
  }

  /**
   * Get monthly trend
   */
  getMonthlyTrend(months: number = 6): Observable<any> {
    return this.api.getMonthlyTrend(months);
  }
}
