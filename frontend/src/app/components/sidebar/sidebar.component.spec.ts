import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { SidebarComponent } from './sidebar.component';

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarComponent, RouterTestingModule],
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have 6 navigation items', () => {
    expect(component.navItems.length).toBe(6);
  });

  it('should have correct navigation routes', () => {
    const routes = component.navItems.map(item => item.route);
    expect(routes).toContain('/dashboard');
    expect(routes).toContain('/transactions');
    expect(routes).toContain('/import');
    expect(routes).toContain('/accounts');
    expect(routes).toContain('/analytics');
    expect(routes).toContain('/settings');
  });

  it('should render all nav items', () => {
    const compiled = fixture.nativeElement;
    const navItems = compiled.querySelectorAll('.nav-item');
    expect(navItems.length).toBe(6);
  });

  it('should render nav icons for each item', () => {
    const compiled = fixture.nativeElement;
    const icons = compiled.querySelectorAll('.nav-icon');
    expect(icons.length).toBe(6);
  });

  it('should call closeSidebar when nav item clicked', () => {
    spyOn(component, 'closeSidebar');
    const navItem = fixture.nativeElement.querySelector('.nav-item');
    navItem.click();
    expect(component.closeSidebar).toHaveBeenCalled();
  });

  it('should have isOpen property', () => {
    expect(component.isOpen).toBe(true);
  });
});
