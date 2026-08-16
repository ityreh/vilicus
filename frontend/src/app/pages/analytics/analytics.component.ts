import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface CategorySpending {
  category: string;
  total: number;
  percentage: number;
}

interface TopMerchant {
  name: string;
  total: number;
  count: number;
}

interface MonthlyData {
  month: string;
  income: number;
  expense: number;
}

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './analytics.component.html',
  styleUrls: ['./analytics.component.css']
})
export class AnalyticsComponent implements OnInit {
  dateRange: '3m' | '6m' | '12m' = '6m';
  selectedCategory: string | null = null;

  categorySpending: CategorySpending[] = [];
  topMerchants: TopMerchant[] = [];
  monthlyData: MonthlyData[] = [];
  totalSpending = 0;

  ngOnInit() {
    this.loadAnalytics();
  }

  loadAnalytics() {
    this.mockLoadAnalytics();
  }

  mockLoadAnalytics() {
    this.categorySpending = [
      { category: 'Rent', total: 3600, percentage: 35 },
      { category: 'Groceries', total: 1200, percentage: 12 },
      { category: 'Transport', total: 500, percentage: 5 },
      { category: 'Dining', total: 800, percentage: 8 },
      { category: 'Shopping', total: 1500, percentage: 15 },
      { category: 'Utilities', total: 600, percentage: 6 },
      { category: 'Entertainment', total: 700, percentage: 7 },
      { category: 'Health', total: 400, percentage: 4 },
      { category: 'Other', total: 400, percentage: 4 }
    ];

    this.totalSpending = this.categorySpending.reduce((sum, cat) => sum + cat.total, 0);

    this.topMerchants = [
      { name: 'Amazon', total: 1200, count: 15 },
      { name: 'Whole Foods', total: 850, count: 22 },
      { name: 'Uber', total: 450, count: 28 },
      { name: 'Restaurant A', total: 320, count: 8 },
      { name: 'Netflix', total: 96, count: 8 },
      { name: 'CVS Pharmacy', total: 280, count: 12 },
      { name: 'Shell Gas', total: 400, count: 16 },
      { name: 'Starbucks', total: 120, count: 24 },
      { name: 'Target', total: 340, count: 9 },
      { name: 'Trader Joe\'s', total: 580, count: 18 }
    ];

    this.monthlyData = [
      { month: 'Mar', income: 3500, expense: 2100 },
      { month: 'Apr', income: 3500, expense: 2350 },
      { month: 'May', income: 3500, expense: 1950 },
      { month: 'Jun', income: 3500, expense: 2800 },
      { month: 'Jul', income: 3500, expense: 2230 },
      { month: 'Aug', income: 2100, expense: 2330 }
    ];
  }

  onDateRangeChange() {
    // Reload data for new range
    this.mockLoadAnalytics();
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getBarWidth(merchant: TopMerchant): number {
    const maxTotal = Math.max(...this.topMerchants.map(m => m.total));
    return (merchant.total / maxTotal) * 100;
  }

  getConiGradient(): string {
    const colors = ['#d2cefd', '#b5abfc', '#968ae0', '#796cbf', '#5d5294', '#75798c'];
    const stops: string[] = [];
    let currentDegree = 0;

    for (let i = 0; i < this.categorySpending.length; i++) {
      const cat = this.categorySpending[i];
      const degrees = (cat.percentage / 100) * 360;
      const color = colors[i % colors.length];
      stops.push(`${color} ${currentDegree}deg ${currentDegree + degrees}deg`);
      currentDegree += degrees;
    }

    return `conic-gradient(${stops.join(', ')})`;
  }

  getMonthlyChartPath(dataKey: 'income' | 'expense'): string {
    const width = 500;
    const height = 250;
    const padding = 30;
    const plotWidth = width - padding * 2;
    const plotHeight = height - padding * 2;

    const maxAmount = Math.max(
      ...this.monthlyData.map(m => Math.max(m.income, m.expense))
    ) * 1.1;

    return this.monthlyData.map((point, i) => {
      const amount = dataKey === 'income' ? point.income : point.expense;
      const x = padding + (i / (this.monthlyData.length - 1)) * plotWidth;
      const y = height - padding - (amount / maxAmount) * plotHeight;
      return `${x},${y}`;
    }).join(' ');
  }
}
