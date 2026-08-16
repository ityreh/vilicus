import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Account {
  id: number;
  name: string;
  iban: string;
  balance: number;
  lastImportDate: string;
  type: string;
  balanceHistory: { date: string; balance: number }[];
  importLog: { date: string; fileName: string; importedCount: number; duplicatesCount: number }[];
}

@Component({
  selector: 'app-accounts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.css']
})
export class AccountsComponent implements OnInit {
  accounts: Account[] = [];
  selectedAccount: Account | null = null;
  showDetailView = false;
  showAddModal = false;

  newAccount = {
    name: '',
    iban: '',
    openingBalance: 0
  };

  ngOnInit() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.mockLoadAccounts();
  }

  mockLoadAccounts() {
    this.accounts = [
      {
        id: 1,
        name: 'Checking',
        iban: 'DE89370400440532013000',
        balance: 5234.50,
        lastImportDate: '2026-08-15',
        type: 'Checking',
        balanceHistory: [
          { date: '2026-03-01', balance: 3000 },
          { date: '2026-04-01', balance: 4500 },
          { date: '2026-05-01', balance: 3200 },
          { date: '2026-06-01', balance: 5100 },
          { date: '2026-07-01', balance: 4800 },
          { date: '2026-08-01', balance: 5050 },
          { date: '2026-08-15', balance: 5234.50 }
        ],
        importLog: [
          { date: '2026-08-15', fileName: 'statement_aug.xml', importedCount: 45, duplicatesCount: 3 },
          { date: '2026-07-15', fileName: 'statement_jul.xml', importedCount: 52, duplicatesCount: 2 },
          { date: '2026-06-15', fileName: 'statement_jun.xml', importedCount: 38, duplicatesCount: 5 }
        ]
      },
      {
        id: 2,
        name: 'Savings',
        iban: 'DE89370400440532013001',
        balance: 12000.00,
        lastImportDate: '2026-08-10',
        type: 'Savings',
        balanceHistory: [
          { date: '2026-03-01', balance: 10000 },
          { date: '2026-04-01', balance: 10500 },
          { date: '2026-05-01', balance: 11000 },
          { date: '2026-06-01', balance: 11200 },
          { date: '2026-07-01', balance: 11500 },
          { date: '2026-08-01', balance: 11800 },
          { date: '2026-08-10', balance: 12000.00 }
        ],
        importLog: [
          { date: '2026-08-10', fileName: 'savings_aug.xml', importedCount: 12, duplicatesCount: 0 },
          { date: '2026-07-10', fileName: 'savings_jul.xml', importedCount: 10, duplicatesCount: 1 }
        ]
      }
    ];
  }

  onSelectAccount(account: Account) {
    this.selectedAccount = account;
    this.showDetailView = true;
  }

  onBackToList() {
    this.showDetailView = false;
    this.selectedAccount = null;
  }

  onOpenAddModal() {
    this.newAccount = { name: '', iban: '', openingBalance: 0 };
    this.showAddModal = true;
  }

  onCloseAddModal() {
    this.showAddModal = false;
  }

  onSaveNewAccount() {
    if (!this.newAccount.name || !this.newAccount.iban) {
      return;
    }
    // TODO: Call API to create account
    console.log('Create account:', this.newAccount);
    this.showAddModal = false;
    this.loadAccounts();
  }

  onDeleteAccount(account: Account) {
    if (confirm(`Are you sure you want to delete ${account.name}?`)) {
      // TODO: Call API to delete account
      console.log('Delete account:', account.id);
      this.loadAccounts();
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getTrendSVGPath(): string {
    if (!this.selectedAccount || !this.selectedAccount.balanceHistory.length) return '';

    const width = 400;
    const height = 150;
    const padding = 20;
    const plotWidth = width - padding * 2;
    const plotHeight = height - padding * 2;

    const history = this.selectedAccount.balanceHistory;
    const maxBalance = Math.max(...history.map(h => h.balance)) * 1.1;

    return history.map((point, i) => {
      const x = padding + (i / (history.length - 1)) * plotWidth;
      const y = height - padding - (point.balance / maxBalance) * plotHeight;
      return `${x},${y}`;
    }).join(' ');
  }
}
