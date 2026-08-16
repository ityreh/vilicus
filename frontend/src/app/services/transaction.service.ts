import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Transaction, TransactionPage, TransactionFilter, BulkRecategorizeRequest, BulkRecategorizeResponse, UpdateTransactionCategoryRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  constructor(private api: ApiService) {}

  /**
   * Get paginated transactions with filters
   */
  getTransactions(filter?: TransactionFilter): Observable<TransactionPage> {
    return this.api.getTransactions(filter);
  }

  /**
   * Get single transaction
   */
  getTransaction(id: number, accountId: number): Observable<Transaction> {
    return this.api.getTransaction(id, accountId);
  }

  /**
   * Update transaction category
   */
  updateCategory(accountId: number, transactionId: number, categoryId: number | null): Observable<Transaction> {
    const request: UpdateTransactionCategoryRequest = { categoryId };
    return this.api.updateTransactionCategory(accountId, transactionId, request);
  }

  /**
   * Bulk recategorize multiple transactions
   */
  bulkRecategorize(accountId: number, transactionIds: number[], categoryId: number): Observable<BulkRecategorizeResponse> {
    const request: BulkRecategorizeRequest = {
      transactionIds,
      categoryId
    };
    return this.api.bulkRecategorize(accountId, request);
  }

  /**
   * Archive/delete transaction
   */
  archive(accountId: number, transactionId: number): Observable<void> {
    return this.api.archiveTransaction(accountId, transactionId);
  }
}
