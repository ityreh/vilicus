import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AnalyticsComponent } from './analytics.component';

describe('AnalyticsComponent', () => {
  let component: AnalyticsComponent;
  let fixture: ComponentFixture<AnalyticsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnalyticsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AnalyticsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load analytics data on init', () => {
    expect(component.categorySpending.length).toBeGreaterThan(0);
    expect(component.topMerchants.length).toBeGreaterThan(0);
    expect(component.monthlyData.length).toBeGreaterThan(0);
  });

  it('should calculate total spending', () => {
    expect(component.totalSpending).toBeGreaterThan(0);
  });

  it('should format currency', () => {
    const result = component.formatCurrency(1234.56);
    expect(result).toBe('$1,234.56');
  });

  it('should render charts', () => {
    fixture.detectChanges();
    const charts = fixture.nativeElement.querySelectorAll('.trend-chart');
    expect(charts.length).toBeGreaterThan(0);
  });
});
