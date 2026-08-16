import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ImportComponent } from './import.component';

describe('ImportComponent', () => {
  let component: ImportComponent;
  let fixture: ComponentFixture<ImportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should start at step 1', () => {
    expect(component.state.currentStep as number).toBe(1);
  });

  it('should have 2 accounts available', () => {
    expect(component.accounts.length).toBe(2);
  });

  it('should initialize with no selected account', () => {
    expect(component.state.selectedAccount).toBeNull();
  });

  it('should initialize with no selected file', () => {
    expect(component.state.selectedFile).toBeNull();
  });

  it('should reject non-XML files', () => {
    const file = new File(['test'], 'test.csv', { type: 'text/csv' });
    component.handleFile(file);
    expect(component.state.error).toContain('xml');
  });

  it('should reject files larger than 25MB', () => {
    const largeFile = new File(['x'.repeat(26 * 1024 * 1024)], 'large.xml', { type: 'application/xml' });
    component.handleFile(largeFile);
    expect(component.state.error).toContain('25MB');
  });

  it('should accept valid XML files', () => {
    const file = new File(['<?xml'], 'test.xml', { type: 'application/xml' });
    component.handleFile(file);
    expect(component.state.selectedFile).toBe(file);
    expect(component.state.error).toBeNull();
  });

  it('should reject preview without account selected', () => {
    const file = new File(['<?xml'], 'test.xml', { type: 'application/xml' });
    component.state.selectedFile = file;
    component.onPreview();
    expect(component.state.error).toBeTruthy();
    expect(component.state.currentStep as number).toBe(1);
  });

  it('should reject preview without file selected', () => {
    component.state.selectedAccount = '1';
    component.onPreview();
    expect(component.state.error).toBeTruthy();
    expect(component.state.currentStep as number).toBe(1);
  });

  it('should move to step 2 when preview called with valid state', () => {
    const file = new File(['<?xml'], 'test.xml', { type: 'application/xml' });
    component.state.selectedAccount = '1';
    component.state.selectedFile = file;
    component.onPreview();
    expect(component.state.currentStep as number).toBe(2);
  });

  it('should generate preview data', () => {
    component.mockGeneratePreview();
    expect(component.state.totalTransactions).toBe(45);
    expect(component.state.duplicateCount).toBe(3);
    expect(component.state.previewTransactions.length).toBeGreaterThan(0);
  });

  it('should mark first 2 transactions as duplicates', () => {
    component.mockGeneratePreview();
    const duplicates = component.state.previewTransactions.filter(t => t.isDuplicate);
    expect(duplicates.length).toBe(2);
  });

  it('should move to step 3 when import confirmed', () => {
    component.state.currentStep = 2;
    component.state.totalTransactions = 45;
    component.state.duplicateCount = 3;
    component.onConfirmImport();
    expect(component.state.currentStep as number).toBe(3);
  });

  it('should calculate imported count correctly', () => {
    component.state.totalTransactions = 45;
    component.state.duplicateCount = 3;
    component.mockConfirmImport();
    expect(component.state.importedCount).toBe(42);
    expect(component.state.skippedCount).toBe(3);
  });

  it('should reset state on reset', () => {
    component.state.currentStep = 3;
    component.state.selectedAccount = '1';
    component.onReset();
    expect(component.state.currentStep as number).toBe(1);
    expect(component.state.selectedAccount).toBeNull();
    expect(component.state.selectedFile).toBeNull();
  });

  it('should go back to step 1 on cancel', () => {
    component.state.currentStep = 2;
    component.onCancel();
    expect(component.state.currentStep as number).toBe(1);
    expect(component.state.selectedFile).toBeNull();
  });

  it('should format currency correctly', () => {
    const formatted = component.formatCurrency(1234.56);
    expect(formatted).toBe('$1,234.56');
  });

  it('should format file size correctly', () => {
    expect(component.getFileSize(0)).toBe('0 Bytes');
    expect(component.getFileSize(1024)).toBe('1 KB');
    expect(component.getFileSize(1024 * 1024)).toBe('1 MB');
  });

  it('should handle drag over event', () => {
    const event = new DragEvent('dragover', { bubbles: true });
    spyOn(event, 'preventDefault');
    spyOn(event, 'stopPropagation');
    component.onDragOver(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.isDragOver).toBe(true);
  });

  it('should handle drag leave event', () => {
    component.isDragOver = true;
    const event = new DragEvent('dragleave', { bubbles: true });
    spyOn(event, 'preventDefault');
    component.onDragLeave(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.isDragOver).toBe(false);
  });

  it('should handle drop event', () => {
    const file = new File(['<?xml'], 'test.xml', { type: 'application/xml' });
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    const event = new DragEvent('drop', { dataTransfer, bubbles: true });
    spyOn(event, 'preventDefault');
    component.onDrop(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.state.selectedFile).toBe(file);
  });

  it('should render step indicator', () => {
    const compiled = fixture.nativeElement;
    const indicator = compiled.querySelector('.step-indicator');
    expect(indicator).toBeTruthy();
  });

  it('should render upload step when currentStep is 1', () => {
    component.state.currentStep = 1;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const stepContent = compiled.querySelector('.step-content');
    expect(stepContent).toBeTruthy();
  });

  it('should render preview step when currentStep is 2', () => {
    component.state.currentStep = 2;
    component.mockGeneratePreview();
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const summary = compiled.querySelector('.summary-box');
    expect(summary).toBeTruthy();
  });

  it('should render result step when currentStep is 3', () => {
    component.state.currentStep = 3;
    component.state.importedCount = 42;
    component.state.skippedCount = 3;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const resultCard = compiled.querySelector('.result-card');
    expect(resultCard).toBeTruthy();
  });

  it('should disable preview button when no account selected', () => {
    component.state.selectedFile = new File(['<?xml'], 'test.xml');
    component.state.selectedAccount = null;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const previewBtn = compiled.querySelector('.step-actions .btn-primary');
    expect(previewBtn?.disabled).toBe(true);
  });

  it('should disable preview button when no file selected', () => {
    component.state.selectedAccount = '1';
    component.state.selectedFile = null;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const previewBtn = compiled.querySelector('.step-actions .btn-primary');
    expect(previewBtn?.disabled).toBe(true);
  });

  it('should show file chosen confirmation when file selected', () => {
    component.state.selectedFile = new File(['<?xml'], 'test.xml');
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const fileChosen = compiled.querySelector('.file-chosen');
    expect(fileChosen).toBeTruthy();
  });

  it('should display error message', () => {
    component.state.error = 'Test error';
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const errorMsg = compiled.querySelector('.error-message');
    expect(errorMsg?.textContent).toContain('Test error');
  });

  it('should render duplicate transactions with different styling', () => {
    component.state.currentStep = 2;
    component.mockGeneratePreview();
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const duplicateRows = compiled.querySelectorAll('.table tbody tr.duplicate');
    expect(duplicateRows.length).toBeGreaterThan(0);
  });

  it('should render step connector as active when on step 3', () => {
    component.state.currentStep = 3;
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const connectors = compiled.querySelectorAll('.step-connector.active');
    expect(connectors.length).toBeGreaterThan(0);
  });
});
