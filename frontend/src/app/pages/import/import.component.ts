import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

/**
 * Import Wizard — 3-step file upload and preview workflow.
 *
 * Steps:
 * 1. Upload: Account selector, drag-drop file zone
 * 2. Preview: Summary of transactions, duplicate detection
 * 3. Result: Success message, link to view imported transactions
 */

interface PreviewTransaction {
  id: number;
  date: string;
  description: string;
  amount: number;
  isDuplicate: boolean;
}

interface ImportState {
  currentStep: 1 | 2 | 3;
  selectedAccount: string | null;
  selectedFile: File | null;
  previewTransactions: PreviewTransaction[];
  totalTransactions: number;
  duplicateCount: number;
  importedCount: number;
  skippedCount: number;
  error: string | null;
}

@Component({
  selector: 'app-import',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './import.component.html',
  styleUrls: ['./import.component.css']
})
export class ImportComponent {
  accounts = [
    { id: '1', name: 'Checking' },
    { id: '2', name: 'Savings' }
  ];

  state: ImportState = {
    currentStep: 1,
    selectedAccount: null,
    selectedFile: null,
    previewTransactions: [],
    totalTransactions: 0,
    duplicateCount: 0,
    importedCount: 0,
    skippedCount: 0,
    error: null
  };

  isDragOver = false;

  // File upload handlers
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.handleFile(file);
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.isDragOver = false;

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFile(files[0]);
    }
  }

  handleFile(file: File) {
    // Validate file type
    if (!file.name.endsWith('.xml')) {
      this.state.error = 'Please select a .xml file (CAMT.052 format)';
      return;
    }

    // Validate file size (25MB max)
    if (file.size > 25 * 1024 * 1024) {
      this.state.error = 'File size exceeds 25MB limit';
      return;
    }

    this.state.selectedFile = file;
    this.state.error = null;
  }

  // Step handlers
  onPreview() {
    if (!this.state.selectedAccount || !this.state.selectedFile) {
      this.state.error = 'Please select an account and file';
      return;
    }

    // TODO: Call API to get preview
    // For now, mock the preview
    this.mockGeneratePreview();
    this.state.currentStep = 2;
  }

  onConfirmImport() {
    // TODO: Call API to confirm import
    // For now, mock the import
    this.mockConfirmImport();
    this.state.currentStep = 3;
  }

  onReset() {
    this.state = {
      currentStep: 1,
      selectedAccount: null,
      selectedFile: null,
      previewTransactions: [],
      totalTransactions: 0,
      duplicateCount: 0,
      importedCount: 0,
      skippedCount: 0,
      error: null
    };
  }

  onCancel() {
    this.state.currentStep = 1;
    this.state.selectedFile = null;
    this.state.previewTransactions = [];
    this.state.error = null;
  }

  // Mock data generation
  mockGeneratePreview() {
    // Generate 50 mock preview transactions
    const descriptions = ['Amazon', 'Whole Foods', 'Uber', 'Restaurant', 'Coffee', 'Netflix', 'Pharmacy', 'Gas', 'Electric Bill'];
    this.state.previewTransactions = [];
    this.state.totalTransactions = 45;
    this.state.duplicateCount = 3;

    for (let i = 0; i < 10; i++) {
      const isDuplicate = i < 2; // First 2 are duplicates
      this.state.previewTransactions.push({
        id: i + 1,
        date: new Date(Date.now() - Math.random() * 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        description: descriptions[Math.floor(Math.random() * descriptions.length)],
        amount: Math.random() * 500 + 10,
        isDuplicate
      });
    }
  }

  mockConfirmImport() {
    this.state.importedCount = this.state.totalTransactions - this.state.duplicateCount;
    this.state.skippedCount = this.state.duplicateCount;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }
}
