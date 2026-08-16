import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Account, CreateAccountRequest, Transaction, TransactionFilter, TransactionPage,
  UpdateTransactionCategoryRequest, BulkRecategorizeRequest, BulkRecategorizeResponse,
  ImportPreviewDto, ImportResultDto, DashboardDto, AnalyticsDto, Category,
  LoginRequest, LoginResponse
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // ==================== Auth ====================
  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, request);
  }

  register(request: { email: string; password: string; name: string }): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/auth/register`, request);
  }

  // ==================== Accounts ====================
  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.apiUrl}/accounts`);
  }

  getAccount(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/accounts/${id}`);
  }

  createAccount(request: CreateAccountRequest): Observable<Account> {
    return this.http.post<Account>(`${this.apiUrl}/accounts`, request);
  }

  updateAccount(id: number, request: Partial<Account>): Observable<Account> {
    return this.http.put<Account>(`${this.apiUrl}/accounts/${id}`, request);
  }

  deleteAccount(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/accounts/${id}`);
  }

  // ==================== Transactions ====================
  getTransactions(filter?: TransactionFilter): Observable<TransactionPage> {
    let params = new HttpParams();
    if (filter) {
      if (filter.accountId) params = params.set('accountId', filter.accountId.toString());
      if (filter.search) params = params.set('search', filter.search);
      if (filter.direction && filter.direction !== 'all') params = params.set('direction', filter.direction);
      if (filter.dateFrom) params = params.set('dateFrom', filter.dateFrom);
      if (filter.dateTo) params = params.set('dateTo', filter.dateTo);
      if (filter.page) params = params.set('page', filter.page.toString());
      if (filter.size) params = params.set('size', filter.size.toString());
      if (filter.sort) params = params.set('sort', filter.sort);
    }
    return this.http.get<TransactionPage>(`${this.apiUrl}/transactions`, { params });
  }

  getTransaction(id: number, accountId: number): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.apiUrl}/transactions/${id}`, {
      params: new HttpParams().set('accountId', accountId.toString())
    });
  }

  updateTransactionCategory(accountId: number, transactionId: number, request: UpdateTransactionCategoryRequest): Observable<Transaction> {
    return this.http.put<Transaction>(
      `${this.apiUrl}/transactions/${transactionId}/category`,
      request,
      { params: new HttpParams().set('accountId', accountId.toString()) }
    );
  }

  bulkRecategorize(accountId: number, request: BulkRecategorizeRequest): Observable<BulkRecategorizeResponse> {
    return this.http.post<BulkRecategorizeResponse>(
      `${this.apiUrl}/transactions/recategorize`,
      request,
      { params: new HttpParams().set('accountId', accountId.toString()) }
    );
  }

  archiveTransaction(accountId: number, transactionId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/transactions/${transactionId}`,
      { params: new HttpParams().set('accountId', accountId.toString()) }
    );
  }

  // ==================== Import ====================
  uploadFile(accountId: number, file: File): Observable<ImportPreviewDto> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('accountId', accountId.toString());
    return this.http.post<ImportPreviewDto>(`${this.apiUrl}/import/preview`, formData);
  }

  confirmImport(accountId: number): Observable<ImportResultDto> {
    return this.http.post<ImportResultDto>(
      `${this.apiUrl}/import/confirm`,
      {},
      { params: new HttpParams().set('accountId', accountId.toString()) }
    );
  }

  // ==================== Dashboard ====================
  getDashboard(dateRange?: string): Observable<DashboardDto> {
    const params = dateRange ? new HttpParams().set('dateRange', dateRange) : undefined;
    return this.http.get<DashboardDto>(`${this.apiUrl}/dashboard`, { params });
  }

  // ==================== Analytics ====================
  getAnalytics(dateRange?: string): Observable<AnalyticsDto> {
    const params = dateRange ? new HttpParams().set('dateRange', dateRange) : undefined;
    return this.http.get<AnalyticsDto>(`${this.apiUrl}/analytics`, { params });
  }

  getCategoryBreakdown(dateRange?: string): Observable<any> {
    const params = dateRange ? new HttpParams().set('dateRange', dateRange) : undefined;
    return this.http.get<any>(`${this.apiUrl}/analytics/categories`, { params });
  }

  getTopMerchants(limit: number = 10): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/analytics/merchants`, {
      params: new HttpParams().set('limit', limit.toString())
    });
  }

  getMonthlyTrend(months: number = 6): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/analytics/trends`, {
      params: new HttpParams().set('months', months.toString())
    });
  }

  // ==================== Categories ====================
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories`);
  }

  createCategory(name: string): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/categories`, { name });
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`);
  }
}
