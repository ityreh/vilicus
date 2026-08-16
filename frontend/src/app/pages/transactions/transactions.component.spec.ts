import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransactionsComponent } from './transactions.component';

describe('TransactionsComponent', () => {
  let component: TransactionsComponent;
  let fixture: ComponentFixture<TransactionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(TransactionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load transactions on init', () => {
    expect(component.allTransactions.length).toBe(100);
    expect(component.displayedTransactions.length).toBeGreaterThan(0);
  });

  it('should have pagination set correctly', () => {
    expect(component.pageSize).toBe(50);
    expect(component.currentPage).toBe(1);
    expect(component.totalPages).toBeGreaterThan(1);
  });

  it('should display first page with 50 transactions', () => {
    expect(component.displayedTransactions.length).toBe(50);
  });

  it('should paginate to second page', () => {
    component.onNextPage();
    expect(component.currentPage).toBe(2);
    expect(component.displayedTransactions.length).toBe(50);
  });

  it('should not paginate beyond last page', () => {
    component.currentPage = component.totalPages;
    component.onNextPage();
    expect(component.currentPage).toBe(component.totalPages);
  });

  it('should paginate back to first page', () => {
    component.currentPage = 2;
    component.onPreviousPage();
    expect(component.currentPage).toBe(1);
  });

  it('should not paginate before first page', () => {
    component.currentPage = 1;
    component.onPreviousPage();
    expect(component.currentPage).toBe(1);
  });

  it('should filter by search term', () => {
    component.filters.search = 'Amazon';
    component.onFilterChange();
    const amazonTxns = component.displayedTransactions.filter(t => t.description.includes('Amazon'));
    expect(amazonTxns.length).toBeGreaterThan(0);
  });

  it('should filter by category', () => {
    const categories = component.getUniqueCategoryNames();
    if (categories.length > 0) {
      component.filters.category = categories[0];
      component.onFilterChange();
      const filtered = component.displayedTransactions.filter(t => t.category === categories[0]);
      expect(filtered.length).toBeGreaterThan(0);
    }
  });

  it('should filter by direction (income)', () => {
    component.filters.direction = 'income';
    component.onFilterChange();
    const incomeOnly = component.displayedTransactions.every(t => t.direction === 'income');
    expect(incomeOnly).toBe(true);
  });

  it('should filter by direction (expense)', () => {
    component.filters.direction = 'expense';
    component.onFilterChange();
    const expenseOnly = component.displayedTransactions.every(t => t.direction === 'expense');
    expect(expenseOnly).toBe(true);
  });

  it('should sort by date descending', () => {
    component.sort.field = 'date';
    component.sort.direction = 'desc';
    component.applyFiltersAndSort();
    for (let i = 1; i < component.displayedTransactions.length; i++) {
      const prev = new Date(component.displayedTransactions[i - 1].date).getTime();
      const curr = new Date(component.displayedTransactions[i].date).getTime();
      expect(prev >= curr).toBe(true);
    }
  });

  it('should sort by date ascending', () => {
    component.sort.field = 'date';
    component.sort.direction = 'asc';
    component.applyFiltersAndSort();
    for (let i = 1; i < component.displayedTransactions.length; i++) {
      const prev = new Date(component.displayedTransactions[i - 1].date).getTime();
      const curr = new Date(component.displayedTransactions[i].date).getTime();
      expect(prev <= curr).toBe(true);
    }
  });

  it('should sort by amount descending', () => {
    component.sort.field = 'amount';
    component.sort.direction = 'desc';
    component.applyFiltersAndSort();
    for (let i = 1; i < component.displayedTransactions.length; i++) {
      expect(component.displayedTransactions[i - 1].amount >= component.displayedTransactions[i].amount).toBe(true);
    }
  });

  it('should toggle sort direction', () => {
    component.sort.field = 'date';
    component.sort.direction = 'asc';
    component.onColumnHeaderClick('date');
    expect(component.sort.direction).toBe('desc');
  });

  it('should change sort field', () => {
    component.sort.field = 'date';
    component.onColumnHeaderClick('amount');
    expect(component.sort.field).toBe('amount');
  });

  it('should select all transactions when checkbox checked', () => {
    component.isSelectAllChecked = true;
    component.onSelectAllChange();
    expect(component.selectedIds.size).toBe(component.displayedTransactions.length);
  });

  it('should deselect all when checkbox unchecked', () => {
    component.selectedIds.add(1);
    component.isSelectAllChecked = false;
    component.onSelectAllChange();
    expect(component.selectedIds.size).toBe(0);
  });

  it('should select individual transaction', () => {
    component.onRowCheckboxChange(1);
    expect(component.isRowSelected(1)).toBe(true);
  });

  it('should deselect individual transaction', () => {
    component.selectedIds.add(1);
    component.onRowCheckboxChange(1);
    expect(component.isRowSelected(1)).toBe(false);
  });

  it('should show bulk action bar when rows selected', () => {
    component.onRowCheckboxChange(1);
    expect(component.showBulkActionBar).toBe(true);
  });

  it('should hide bulk action bar when selection cleared', () => {
    component.selectedIds.add(1);
    component.showBulkActionBar = true;
    component.onClearSelection();
    expect(component.showBulkActionBar).toBe(false);
    expect(component.selectedIds.size).toBe(0);
  });

  it('should open transaction detail modal', () => {
    const txn = component.displayedTransactions[0];
    component.openTransactionDetail(txn);
    expect(component.showDetailModal).toBe(true);
    expect(component.selectedTransaction).toBe(txn);
  });

  it('should close transaction detail modal', () => {
    component.showDetailModal = true;
    component.closeDetailModal();
    expect(component.showDetailModal).toBe(false);
    expect(component.selectedTransaction).toBe(null);
  });

  it('should format currency correctly', () => {
    const formatted = component.formatCurrency(1234.56);
    expect(formatted).toBe('$1,234.56');
  });

  it('should get unique category names', () => {
    const categories = component.getUniqueCategoryNames();
    expect(categories.length).toBeGreaterThan(0);
    expect(categories).toEqual(categories.sort());
  });

  it('should return correct sort indicator for ascending', () => {
    component.sort.field = 'date';
    component.sort.direction = 'asc';
    expect(component.getSortIndicator('date')).toBe('↑');
  });

  it('should return correct sort indicator for descending', () => {
    component.sort.field = 'date';
    component.sort.direction = 'desc';
    expect(component.getSortIndicator('date')).toBe('↓');
  });

  it('should return empty string for non-active sort field', () => {
    component.sort.field = 'date';
    expect(component.getSortIndicator('amount')).toBe('');
  });

  it('should render filter card', () => {
    const compiled = fixture.nativeElement;
    const filterCard = compiled.querySelector('.filter-card');
    expect(filterCard).toBeTruthy();
  });

  it('should render transactions table', () => {
    const compiled = fixture.nativeElement;
    const table = compiled.querySelector('.table');
    expect(table).toBeTruthy();
  });

  it('should render table rows for displayed transactions', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const rows = compiled.querySelectorAll('.table tbody tr');
    expect(rows.length).toBe(component.displayedTransactions.length);
  });

  it('should render pagination controls', () => {
    const compiled = fixture.nativeElement;
    const pagination = compiled.querySelector('.pagination-footer');
    expect(pagination).toBeTruthy();
  });

  it('should update filter state when search input changes', (done) => {
    component.filters.search = 'test';
    component.onFilterChange();
    fixture.detectChanges();
    fixture.whenStable().then(() => {
      expect(component.currentPage).toBe(1);
      done();
    });
  });

  it('should show bulk action bar in DOM when selected', () => {
    component.selectedIds.add(1);
    component.showBulkActionBar = true;
    fixture.detectChanges();
    const bulkBar = fixture.nativeElement.querySelector('.bulk-action-bar');
    expect(bulkBar).toBeTruthy();
  });

  it('should display correct transaction count in bulk bar', () => {
    component.selectedIds.add(1);
    component.selectedIds.add(2);
    component.selectedIds.add(3);
    component.showBulkActionBar = true;
    fixture.detectChanges();
    const countText = fixture.nativeElement.querySelector('.action-count');
    expect(countText.textContent).toContain('3');
  });

  it('should disable bulk apply button when no category selected', () => {
    component.selectedIds.add(1);
    component.showBulkActionBar = true;
    component.bulkRecategorizeCategory = null;
    fixture.detectChanges();
    const applyBtn = fixture.nativeElement.querySelector('.bulk-action-bar .btn-secondary');
    expect(applyBtn.disabled).toBe(true);
  });
});
