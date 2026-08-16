// Auth Models
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    email: string;
    name: string;
  };
}

// Account Models
export interface Account {
  id: number;
  name: string;
  iban: string;
  balance: number;
  lastImportDate: string;
  type: string;
}

export interface CreateAccountRequest {
  name: string;
  iban: string;
  openingBalance: number;
}

// Transaction Models
export interface Transaction {
  id: number;
  accountId: number;
  txId: string;
  txDate: string;
  date: string;
  description: string;
  counterparty: string;
  amount: number;
  balance?: number;
  category: string | null;
  categoryId?: number;
  direction: 'income' | 'expense';
  status: 'imported' | 'categorized' | 'archived';
  reference?: string;
  notes?: string;
  importSource?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface TransactionFilter {
  search?: string;
  accountId?: number;
  categoryId?: number;
  direction?: 'all' | 'income' | 'expense';
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface TransactionPage {
  content: Transaction[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface UpdateTransactionCategoryRequest {
  categoryId: number | null;
}

export interface BulkRecategorizeRequest {
  transactionIds: number[];
  categoryId: number;
}

export interface BulkRecategorizeResponse {
  updatedCount: number;
  categoryId: number;
  message: string;
}

// Import Models
export interface ImportPreviewTransaction {
  id: number;
  date: string;
  description: string;
  amount: number;
  isDuplicate: boolean;
}

export interface ImportPreviewDto {
  transactions: ImportPreviewTransaction[];
  totalTransactions: number;
  duplicateCount: number;
  openingBalance?: number;
}

export interface ImportResultDto {
  importedCount: number;
  skippedCount: number;
  totalCount: number;
  message: string;
}

// Dashboard Models
export interface DashboardDto {
  accounts: Account[];
  recentTransactions: Transaction[];
  spendingByCategory: CategorySpending[];
  monthlyTrend: MonthlyTrend[];
}

export interface CategorySpending {
  category: string;
  amount: number;
  percentage: number;
}

export interface MonthlyTrend {
  month: string;
  income: number;
  expense: number;
}

// Analytics Models
export interface AnalyticsDto {
  spendingByCategory: CategorySpending[];
  topMerchants: TopMerchant[];
  monthlyData: MonthlyTrend[];
  totalSpending: number;
}

export interface TopMerchant {
  name: string;
  total: number;
  count: number;
}

// Category Models
export interface Category {
  id: number;
  name: string;
  userId: number;
}

// Error Response
export interface ErrorResponse {
  status: number;
  message: string;
  timestamp?: string;
}
