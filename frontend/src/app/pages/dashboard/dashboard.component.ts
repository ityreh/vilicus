import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

/**
 * Dashboard page — overview of accounts, spending, and recent activity.
 *
 * Components:
 * - Header with date-range selector
 * - Account cards (grid, clickable)
 * - Category donut chart + legend
 * - Spending trend line chart (6 months)
 * - Recent transactions table
 */

interface Account {
  id: number;
  name: string;
  balance: number;
  lastImportDate: string;
  type: string;
}

interface CategorySpending {
  category: string;
  amount: number;
  percentage: number;
  color: string;
}

interface MonthlyTrend {
  month: string;
  income: number;
  expense: number;
}

interface RecentTransaction {
  id: number;
  date: string;
  description: string;
  amount: number;
  category: string | null;
  direction: 'income' | 'expense';
  status: 'imported' | 'categorized' | 'archived';
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  // Date range selection
  selectedRange: '30d' | 'month' | '3m' = '30d';

  // Data
  accounts: Account[] = [];
  spendingByCategory: CategorySpending[] = [];
  monthlyTrend: MonthlyTrend[] = [];
  recentTransactions: RecentTransaction[] = [];

  // Totals
  totalSpend = 0;

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    // TODO: Replace with API calls via DashboardService
    this.mockLoadData();
  }

  // Mock data for development
  mockLoadData() {
    // Mock accounts
    this.accounts = [
      {
        id: 1,
        name: 'Checking',
        balance: 5234.50,
        lastImportDate: 'Aug 15, 2026',
        type: 'Checking'
      },
      {
        id: 2,
        name: 'Savings',
        balance: 12000.00,
        lastImportDate: 'Aug 10, 2026',
        type: 'Savings'
      }
    ];

    // Mock spending by category (top 5 + "Other")
    this.spendingByCategory = [
      { category: 'Groceries', amount: 450.00, percentage: 25, color: '#d2cefd' },
      { category: 'Rent', amount: 1200.00, percentage: 35, color: '#b5abfc' },
      { category: 'Transport', amount: 200.00, percentage: 12, color: '#968ae0' },
      { category: 'Dining', amount: 300.00, percentage: 15, color: '#796cbf' },
      { category: 'Shopping', amount: 150.00, percentage: 8, color: '#5d5294' },
      { category: 'Other', amount: 30.00, percentage: 5, color: '#75798c' }
    ];

    this.totalSpend = this.spendingByCategory.reduce((sum, cat) => sum + cat.amount, 0);

    // Mock monthly trend (last 6 months)
    this.monthlyTrend = [
      { month: 'Mar', income: 3500, expense: 2100 },
      { month: 'Apr', income: 3500, expense: 2350 },
      { month: 'May', income: 3500, expense: 1950 },
      { month: 'Jun', income: 3500, expense: 2800 },
      { month: 'Jul', income: 3500, expense: 2230 },
      { month: 'Aug', income: 2100, expense: 2330 }
    ];

    // Mock recent transactions
    this.recentTransactions = [
      {
        id: 1,
        date: '2026-08-15',
        description: 'Amazon Purchase',
        amount: 45.99,
        category: 'Shopping',
        direction: 'expense',
        status: 'categorized'
      },
      {
        id: 2,
        date: '2026-08-14',
        description: 'Salary Deposit',
        amount: 3500.00,
        category: null,
        direction: 'income',
        status: 'imported'
      },
      {
        id: 3,
        date: '2026-08-14',
        description: 'Whole Foods Market',
        amount: 87.50,
        category: 'Groceries',
        direction: 'expense',
        status: 'categorized'
      },
      {
        id: 4,
        date: '2026-08-13',
        description: 'Uber Trip',
        amount: 18.75,
        category: 'Transport',
        direction: 'expense',
        status: 'categorized'
      },
      {
        id: 5,
        date: '2026-08-13',
        description: 'Restaurant',
        amount: 65.00,
        category: 'Dining',
        direction: 'expense',
        status: 'categorized'
      },
      {
        id: 6,
        date: '2026-08-12',
        description: 'Coffee Shop',
        amount: 5.50,
        category: null,
        direction: 'expense',
        status: 'imported'
      },
      {
        id: 7,
        date: '2026-08-12',
        description: 'Rent Payment',
        amount: 1200.00,
        category: 'Rent',
        direction: 'expense',
        status: 'categorized'
      },
      {
        id: 8,
        date: '2026-08-11',
        description: 'Netflix Subscription',
        amount: 15.99,
        category: 'Entertainment',
        direction: 'expense',
        status: 'categorized'
      },
      {
        id: 9,
        date: '2026-08-10',
        description: 'Pharmacy Purchase',
        amount: 32.00,
        category: 'Health',
        direction: 'expense',
        status: 'categorized'
      },
      {
        id: 10,
        date: '2026-08-10',
        description: 'Bank Transfer',
        amount: 500.00,
        category: null,
        direction: 'expense',
        status: 'imported'
      }
    ];
  }

  onDateRangeChange(range: '30d' | 'month' | '3m') {
    this.selectedRange = range;
    // TODO: Reload data for new date range
  }

  getAccountCardCursor(): string {
    return 'cursor-pointer';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getConiGradient(): string {
    // Generate conic-gradient for donut chart
    // Colors: accent-300 → accent-700, with "Other" in neutral-600
    const stops: string[] = [];
    let currentDegree = 0;

    for (let i = 0; i < this.spendingByCategory.length; i++) {
      const cat = this.spendingByCategory[i];
      const degrees = (cat.percentage / 100) * 360;
      stops.push(`${cat.color} ${currentDegree}deg ${currentDegree + degrees}deg`);
      currentDegree += degrees;
    }

    return `conic-gradient(${stops.join(', ')})`;
  }

  getTrendSVGPath(): string {
    // Generate SVG polyline path for trend chart
    // X-axis: months, Y-axis: amounts
    // Scale to fit 400x200 box
    const width = 400;
    const height = 200;
    const padding = 20;
    const plotWidth = width - padding * 2;
    const plotHeight = height - padding * 2;

    const maxAmount = Math.max(
      ...this.monthlyTrend.map(m => Math.max(m.income, m.expense))
    ) * 1.1;

    const expensePoints = this.monthlyTrend.map((trend, i) => {
      const x = padding + (i / (this.monthlyTrend.length - 1)) * plotWidth;
      const y = height - padding - (trend.expense / maxAmount) * plotHeight;
      return `${x},${y}`;
    }).join(' ');

    return expensePoints;
  }

  getTrendIncomeeSVGPath(): string {
    const width = 400;
    const height = 200;
    const padding = 20;
    const plotWidth = width - padding * 2;
    const plotHeight = height - padding * 2;

    const maxAmount = Math.max(
      ...this.monthlyTrend.map(m => Math.max(m.income, m.expense))
    ) * 1.1;

    const incomePoints = this.monthlyTrend.map((trend, i) => {
      const x = padding + (i / (this.monthlyTrend.length - 1)) * plotWidth;
      const y = height - padding - (trend.income / maxAmount) * plotHeight;
      return `${x},${y}`;
    }).join(' ');

    return incomePoints;
  }
}
