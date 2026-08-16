import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SettingsComponent } from './settings.component';

describe('SettingsComponent', () => {
  let component: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SettingsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have profile data', () => {
    expect(component.profile.name).toBeTruthy();
    expect(component.profile.email).toBeTruthy();
  });

  it('should have categories', () => {
    expect(component.categories.length).toBeGreaterThan(0);
  });

  it('should add category', () => {
    const initialCount = component.categories.length;
    component.newCategory = 'Gym';
    component.onAddCategory();
    expect(component.categories.length).toBe(initialCount + 1);
    expect(component.categories).toContain('Gym');
  });

  it('should remove category', () => {
    const toRemove = component.categories[0];
    component.onRemoveCategory(toRemove);
    expect(component.categories).not.toContain(toRemove);
  });

  it('should show export message', (done) => {
    component.onExportCSV();
    expect(component.exportMessage).toBeTruthy();
    setTimeout(() => {
      expect(component.exportMessage).toBe('');
      done();
    }, 2600);
  });

  it('should render profile section', () => {
    fixture.detectChanges();
    const profileCard = fixture.nativeElement.querySelector('.card');
    expect(profileCard).toBeTruthy();
  });
});
