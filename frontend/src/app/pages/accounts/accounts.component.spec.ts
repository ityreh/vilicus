import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AccountsComponent } from './accounts.component';

describe('AccountsComponent', () => {
  let component: AccountsComponent;
  let fixture: ComponentFixture<AccountsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AccountsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load accounts on init', () => {
    expect(component.accounts.length).toBe(2);
  });

  it('should have Checking and Savings accounts', () => {
    const names = component.accounts.map(a => a.name);
    expect(names).toContain('Checking');
    expect(names).toContain('Savings');
  });

  it('should start in list view', () => {
    expect(component.showDetailView).toBe(false);
  });

  it('should select account and show detail view', () => {
    const account = component.accounts[0];
    component.onSelectAccount(account);
    expect(component.selectedAccount).toBe(account);
    expect(component.showDetailView).toBe(true);
  });

  it('should go back to list view', () => {
    component.showDetailView = true;
    component.selectedAccount = component.accounts[0];
    component.onBackToList();
    expect(component.showDetailView).toBe(false);
    expect(component.selectedAccount).toBeNull();
  });

  it('should open add modal', () => {
    component.onOpenAddModal();
    expect(component.showAddModal).toBe(true);
  });

  it('should close add modal', () => {
    component.showAddModal = true;
    component.onCloseAddModal();
    expect(component.showAddModal).toBe(false);
  });

  it('should format currency correctly', () => {
    const formatted = component.formatCurrency(1234.56);
    expect(formatted).toBe('$1,234.56');
  });

  it('should have import log for Checking account', () => {
    const checking = component.accounts[0];
    expect(checking.importLog.length).toBeGreaterThan(0);
  });

  it('should have balance history for accounts', () => {
    component.accounts.forEach(account => {
      expect(account.balanceHistory.length).toBeGreaterThan(0);
    });
  });

  it('should generate trend SVG path when selected account has balance history', () => {
    component.selectedAccount = component.accounts[0];
    const path = component.getTrendSVGPath();
    expect(path.length).toBeGreaterThan(0);
  });

  it('should render list view when not in detail mode', () => {
    component.showDetailView = false;
    fixture.detectChanges();
    const header = fixture.nativeElement.querySelector('.accounts-header');
    expect(header).toBeTruthy();
  });

  it('should render detail view when in detail mode', () => {
    component.showDetailView = true;
    component.selectedAccount = component.accounts[0];
    fixture.detectChanges();
    const detailView = fixture.nativeElement.querySelector('.detail-view');
    expect(detailView).toBeTruthy();
  });

  it('should render accounts table', () => {
    component.showDetailView = false;
    fixture.detectChanges();
    const table = fixture.nativeElement.querySelector('.table');
    expect(table).toBeTruthy();
  });

  it('should render 2 account rows', () => {
    component.showDetailView = false;
    fixture.detectChanges();
    const rows = fixture.nativeElement.querySelectorAll('.table tbody tr');
    expect(rows.length).toBe(2);
  });

  it('should show add account modal', () => {
    component.showAddModal = true;
    fixture.detectChanges();
    const modal = fixture.nativeElement.querySelector('.dialog-backdrop');
    expect(modal).toBeTruthy();
  });

  it('should display balance history chart when viewing detail', () => {
    component.showDetailView = true;
    component.selectedAccount = component.accounts[0];
    fixture.detectChanges();
    const chart = fixture.nativeElement.querySelector('.trend-chart');
    expect(chart).toBeTruthy();
  });

  it('should display import log table in detail view', () => {
    component.showDetailView = true;
    component.selectedAccount = component.accounts[0];
    fixture.detectChanges();
    const logTable = fixture.nativeElement.querySelectorAll('.table')[1];
    expect(logTable).toBeTruthy();
  });

  it('should have add button in list view', () => {
    component.showDetailView = false;
    fixture.detectChanges();
    const addBtn = fixture.nativeElement.querySelector('.btn-primary');
    expect(addBtn).toBeTruthy();
  });

  it('should have back button in detail view', () => {
    component.showDetailView = true;
    component.selectedAccount = component.accounts[0];
    fixture.detectChanges();
    const backBtn = fixture.nativeElement.querySelector('.back-button');
    expect(backBtn).toBeTruthy();
  });

  it('should display account IBAN in detail view', () => {
    component.showDetailView = true;
    component.selectedAccount = component.accounts[0];
    fixture.detectChanges();
    const iban = fixture.nativeElement.querySelector('.iban-text');
    expect(iban?.textContent).toContain('DE89370400440532013000');
  });

  it('should display account balance in detail view', () => {
    component.showDetailView = true;
    component.selectedAccount = component.accounts[0];
    fixture.detectChanges();
    const balance = fixture.nativeElement.querySelector('.card-title-large');
    expect(balance?.textContent).toContain('$5,234.50');
  });
});
