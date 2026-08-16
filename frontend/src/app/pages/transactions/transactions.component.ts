import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

/**
 * Transactions page — browse, filter, search, and categorize transactions.
 *
 * Features:
 * - Filter card (search, account, category, date range, direction)
 * - Sortable table (date, description, counterparty, category, amount, status)
 * - Bulk action bar (visible when rows selected)
 * - Pagination (50 rows/page)
 * - Transaction detail modal
 * - Checkbox select
 */

interface Transaction {
  id: number;
  date: string;
  description: string;
  counterparty: string;
  amount: number;
  category: string | null;
  direction: 'income' | 'expense';
  status: 'imported' | 'categorized' | 'archived';
}

interface FilterState {
  search: string;
  account: string;
  category: string;
  direction: 'all' | 'income' | 'expense';
  dateFrom: string;
  dateTo: string;
}

interface SortState {
  field: 'date' | 'amount' | null;
  direction: 'asc' | 'desc';
}

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.css']
})
export class TransactionsComponent implements OnInit {
  // Make Math available in template
  Math = Math;

  // All transactions (from backend)
  allTransactions: Transaction[] = [];

  // Filtered & paginated transactions (displayed)
  displayedTransactions: Transaction[] = [];

  // Filters
  filters: FilterState = {
    search: '',
    account: 'all',
    category: 'all',
    direction: 'all',
    dateFrom: '',
    dateTo: ''
  };

  // Sort
  sort: SortState = {
    field: 'date',
    direction: 'desc'
  };

  // Pagination
  currentPage = 1;
  pageSize = 50;
  totalPages = 1;
  totalCount = 0;

  // Bulk select
  selectedIds = new Set<number>();
  isSelectAllChecked = false;

  // Modal
  selectedTransaction: Transaction | null = null;
  showDetailModal = false;

  // UI state
  bulkRecategorizeCategory: string | null = null;
  showBulkActionBar = false;

  ngOnInit() {
    this.loadTransactions();
  }

  loadTransactions() {
    // TODO: Replace with API call
    this.mockLoadTransactions();
    this.applyFiltersAndSort();
  }

  mockLoadTransactions() {
    // Generate mock transactions (100 for pagination testing)
    this.allTransactions = [];
    const categories = ['Groceries', 'Rent', 'Transport', 'Dining', 'Shopping', 'Utilities', 'Entertainment', 'Health', null];
    const descriptions = [
      'Amazon Purchase', 'Whole Foods', 'Uber Trip', 'Restaurant', 'Coffee Shop',
      'Netflix', 'Pharmacy', 'Gas Station', 'Electric Bill', 'Water Bill',
      'Salary Deposit', 'Freelance Income', 'ATM Withdrawal', 'Bank Transfer'
    ];
    const counterparties = ['Amazon', 'Whole Foods', 'Uber', 'Restaurant', 'Coffee', 'Netflix', 'CVS', 'Shell', 'utility', null];

    for (let i = 1; i <= 100; i++) {
      const isIncome = Math.random() < 0.1; // 10% income
      const daysAgo = Math.floor(Math.random() * 60);
      const date = new Date();
      date.setDate(date.getDate() - daysAgo);

      this.allTransactions.push({
        id: i,
        date: date.toISOString().split('T')[0],
        description: descriptions[Math.floor(Math.random() * descriptions.length)],
        counterparty: counterparties[Math.floor(Math.random() * counterparties.length)] || 'Unknown',
        amount: isIncome ? Math.random() * 5000 + 1000 : Math.random() * 500 + 10,
        category: categories[Math.floor(Math.random() * categories.length)],
        direction: isIncome ? 'income' : 'expense',
        status: Math.random() > 0.3 ? 'categorized' : 'imported'
      });
    }

    // Sort by date desc
    this.allTransactions.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  }

  applyFiltersAndSort() {
    // Filter
    let filtered = this.allTransactions.filter(txn => {
      const matchSearch = this.filters.search === '' ||
        txn.description.toLowerCase().includes(this.filters.search.toLowerCase()) ||
        (txn.counterparty && txn.counterparty.toLowerCase().includes(this.filters.search.toLowerCase()));

      const matchCategory = this.filters.category === 'all' || txn.category === this.filters.category;
      const matchDirection = this.filters.direction === 'all' || txn.direction === this.filters.direction;
      const matchDateFrom = this.filters.dateFrom === '' || txn.date >= this.filters.dateFrom;
      const matchDateTo = this.filters.dateTo === '' || txn.date <= this.filters.dateTo;

      return matchSearch && matchCategory && matchDirection && matchDateFrom && matchDateTo;
    });

    // Sort
    if (this.sort.field) {
      filtered.sort((a, b) => {
        let aVal: any, bVal: any;

        if (this.sort.field === 'date') {
          aVal = new Date(a.date).getTime();
          bVal = new Date(b.date).getTime();
        } else if (this.sort.field === 'amount') {
          aVal = a.amount;
          bVal = b.amount;
        }

        const comparison = aVal > bVal ? 1 : aVal < bVal ? -1 : 0;
        return this.sort.direction === 'asc' ? comparison : -comparison;
      });
    }

    // Pagination
    this.totalCount = filtered.length;
    this.totalPages = Math.ceil(this.totalCount / this.pageSize);
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.displayedTransactions = filtered.slice(start, end);

    // Clear selection on filter change
    this.selectedIds.clear();
    this.isSelectAllChecked = false;
    this.showBulkActionBar = false;
  }

  // Filter handlers
  onFilterChange() {
    this.currentPage = 1;
    this.applyFiltersAndSort();
  }

  // Sort handlers
  onColumnHeaderClick(field: 'date' | 'amount') {
    if (this.sort.field === field) {
      // Toggle direction
      this.sort.direction = this.sort.direction === 'asc' ? 'desc' : 'asc';
    } else {
      // Change field
      this.sort.field = field;
      this.sort.direction = 'desc';
    }
    this.applyFiltersAndSort();
  }

  getSortIndicator(field: 'date' | 'amount'): string {
    if (this.sort.field !== field) return '';
    return this.sort.direction === 'asc' ? '↑' : '↓';
  }

  // Pagination handlers
  onPreviousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.applyFiltersAndSort();
    }
  }

  onNextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.applyFiltersAndSort();
    }
  }

  // Selection handlers
  onSelectAllChange() {
    if (this.isSelectAllChecked) {
      this.displayedTransactions.forEach(txn => this.selectedIds.add(txn.id));
    } else {
      this.displayedTransactions.forEach(txn => this.selectedIds.delete(txn.id));
    }
    this.showBulkActionBar = this.selectedIds.size > 0;
  }

  onRowCheckboxChange(txnId: number) {
    if (this.selectedIds.has(txnId)) {
      this.selectedIds.delete(txnId);
    } else {
      this.selectedIds.add(txnId);
    }
    this.isSelectAllChecked = this.selectedIds.size === this.displayedTransactions.length;
    this.showBulkActionBar = this.selectedIds.size > 0;
  }

  isRowSelected(txnId: number): boolean {
    return this.selectedIds.has(txnId);
  }

  // Bulk actions
  onBulkRecategorize() {
    if (!this.bulkRecategorizeCategory || this.selectedIds.size === 0) return;
    // TODO: Call API to bulk update categories
    console.log(`Bulk recategorize ${this.selectedIds.size} transactions to ${this.bulkRecategorizeCategory}`);
    this.selectedIds.clear();
    this.isSelectAllChecked = false;
    this.showBulkActionBar = false;
    this.bulkRecategorizeCategory = null;
  }

  onBulkArchive() {
    if (this.selectedIds.size === 0) return;
    // TODO: Call API to archive transactions
    console.log(`Archive ${this.selectedIds.size} transactions`);
    this.selectedIds.clear();
    this.isSelectAllChecked = false;
    this.showBulkActionBar = false;
  }

  onClearSelection() {
    this.selectedIds.clear();
    this.isSelectAllChecked = false;
    this.showBulkActionBar = false;
  }

  // Modal handlers
  openTransactionDetail(txn: Transaction) {
    this.selectedTransaction = txn;
    this.showDetailModal = true;
  }

  closeDetailModal() {
    this.selectedTransaction = null;
    this.showDetailModal = false;
  }

  // Utility
  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getUniqueCategoryNames(): string[] {
    const categories = new Set<string>();
    this.allTransactions.forEach(txn => {
      if (txn.category) categories.add(txn.category);
    });
    return Array.from(categories).sort();
  }
}
