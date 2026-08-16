import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  profile = {
    name: 'John Doe',
    email: 'john@example.com'
  };

  categories: string[] = ['Groceries', 'Rent', 'Transport', 'Dining', 'Shopping', 'Utilities', 'Entertainment', 'Health'];
  newCategory = '';

  accounts = [
    { id: 1, name: 'Checking' },
    { id: 2, name: 'Savings' }
  ];

  exportMessage = '';

  ngOnInit() {
    // Load profile and categories from API
  }

  onSaveProfile() {
    // TODO: Call API to save profile
    console.log('Save profile:', this.profile);
  }

  onAddCategory() {
    if (!this.newCategory || this.newCategory.trim() === '') return;
    if (!this.categories.includes(this.newCategory)) {
      this.categories.push(this.newCategory);
      this.categories.sort();
      this.newCategory = '';
    }
  }

  onRemoveCategory(category: string) {
    this.categories = this.categories.filter(c => c !== category);
  }

  onExportCSV() {
    this.exportMessage = 'CSV export ready';
    setTimeout(() => this.exportMessage = '', 2500);
  }

  onExportJSON() {
    this.exportMessage = 'JSON export ready';
    setTimeout(() => this.exportMessage = '', 2500);
  }

  onDeleteAccount(accountId: number) {
    if (confirm('Are you sure?')) {
      console.log('Delete account:', accountId);
    }
  }
}
