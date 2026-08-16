import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load dashboard data on init', () => {
    expect(component.accounts.length).toBeGreaterThan(0);
    expect(component.spendingByCategory.length).toBeGreaterThan(0);
    expect(component.monthlyTrend.length).toBeGreaterThan(0);
    expect(component.recentTransactions.length).toBeGreaterThan(0);
  });

  it('should have 2 accounts', () => {
    expect(component.accounts.length).toBe(2);
    expect(component.accounts[0].name).toBe('Checking');
    expect(component.accounts[1].name).toBe('Savings');
  });

  it('should have 6 spending categories', () => {
    expect(component.spendingByCategory.length).toBe(6);
  });

  it('should calculate total spending correctly', () => {
    const expectedTotal = component.spendingByCategory.reduce((sum, cat) => sum + cat.amount, 0);
    expect(component.totalSpend).toBe(expectedTotal);
  });

  it('should have 6 monthly trend data points', () => {
    expect(component.monthlyTrend.length).toBe(6);
  });

  it('should have 10 recent transactions', () => {
    expect(component.recentTransactions.length).toBe(10);
  });

  it('should have initial date range set to "30d"', () => {
    expect(component.selectedRange).toBe('30d');
  });

  it('should change date range when onDateRangeChange is called', () => {
    component.onDateRangeChange('3m');
    expect(component.selectedRange).toBe('3m');

    component.onDateRangeChange('month');
    expect(component.selectedRange).toBe('month');
  });

  it('should render dashboard header', () => {
    const compiled = fixture.nativeElement;
    const header = compiled.querySelector('.dashboard-header');
    expect(header).toBeTruthy();
  });

  it('should render date range selector with 3 buttons', () => {
    const compiled = fixture.nativeElement;
    const buttons = compiled.querySelectorAll('.seg-opt');
    expect(buttons.length).toBe(3);
  });

  it('should render account cards', () => {
    const compiled = fixture.nativeElement;
    const accountCards = compiled.querySelectorAll('.account-card');
    expect(accountCards.length).toBe(2);
  });

  it('should render account card with correct data', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const firstCard = compiled.querySelector('.account-card');
    expect(firstCard.textContent).toContain('Checking');
    expect(firstCard.textContent).toContain('$5,234.50');
  });

  it('should render donut chart', () => {
    const compiled = fixture.nativeElement;
    const donutChart = compiled.querySelector('.donut-chart');
    expect(donutChart).toBeTruthy();
  });

  it('should render donut chart with correct total spend', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const donutValue = compiled.querySelector('.donut-value');
    expect(donutValue.textContent).toContain('$3,330.50');
  });

  it('should render trend chart SVG', () => {
    const compiled = fixture.nativeElement;
    const trendChart = compiled.querySelector('.trend-chart');
    expect(trendChart).toBeTruthy();
  });

  it('should render SVG polylines for trend chart', () => {
    const compiled = fixture.nativeElement;
    const polylines = compiled.querySelectorAll('.trend-chart polyline');
    expect(polylines.length).toBe(2); // income and expense
  });

  it('should render recent transactions table', () => {
    const compiled = fixture.nativeElement;
    const table = compiled.querySelector('.table');
    expect(table).toBeTruthy();
  });

  it('should render 10 transaction rows', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const rows = compiled.querySelectorAll('.table tbody tr');
    expect(rows.length).toBe(10);
  });

  it('should render category tags for categorized transactions', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const tags = compiled.querySelectorAll('.tag-accent');
    expect(tags.length).toBeGreaterThan(0);
  });

  it('should render uncategorized tag for transactions without category', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const uncategorized = compiled.querySelectorAll('.tag-outline');
    expect(uncategorized.length).toBeGreaterThan(0);
  });

  it('should format currency correctly', () => {
    const formatted = component.formatCurrency(1234.56);
    expect(formatted).toBe('$1,234.56');
  });

  it('should generate conic gradient for donut chart', () => {
    const gradient = component.getConiGradient();
    expect(gradient).toContain('conic-gradient');
    expect(gradient).toContain('deg');
  });

  it('should generate trend SVG path', () => {
    const path = component.getTrendSVGPath();
    expect(path.length).toBeGreaterThan(0);
    expect(path).toContain(',');
  });

  it('should render legend for category chart', () => {
    const compiled = fixture.nativeElement;
    const legendItems = compiled.querySelectorAll('.chart-legend .legend-item');
    expect(legendItems.length).toBe(6);
  });

  it('should render active state for selected date range', () => {
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll('.seg-opt');
    expect(buttons[0].classList.contains('active')).toBe(true);
    expect(buttons[1].classList.contains('active')).toBe(false);
  });

  it('should update active button when date range changes', () => {
    component.onDateRangeChange('3m');
    fixture.detectChanges();
    const buttons = fixture.nativeElement.querySelectorAll('.seg-opt');
    expect(buttons[2].classList.contains('active')).toBe(true);
  });

  it('should display income amount in accent color', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    const incomeAmount = compiled.querySelector('.income-amount');
    expect(incomeAmount).toBeTruthy();
    expect(incomeAmount.textContent).toContain('+');
  });
});
