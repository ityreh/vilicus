import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from './navbar.component';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarComponent, FormsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display user initials', () => {
    const compiled = fixture.nativeElement;
    const avatar = compiled.querySelector('.avatar');
    expect(avatar.textContent).toContain('JD');
  });

  it('should display user name', () => {
    const compiled = fixture.nativeElement;
    const userName = compiled.querySelector('.user-name');
    expect(userName.textContent).toContain('John Doe');
  });

  it('should have account selector', () => {
    const compiled = fixture.nativeElement;
    const select = compiled.querySelector('.account-select');
    expect(select).toBeTruthy();
  });

  it('should have logout button', () => {
    const compiled = fixture.nativeElement;
    const logoutBtn = compiled.querySelector('.logout-btn');
    expect(logoutBtn).toBeTruthy();
  });

  it('should call onAccountChange when select changes', () => {
    spyOn(component, 'onAccountChange');
    const select = fixture.nativeElement.querySelector('.account-select');
    select.value = 'Savings';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(component.onAccountChange).toHaveBeenCalledWith('Savings');
  });

  it('should call logout when logout button clicked', () => {
    spyOn(component, 'logout');
    const logoutBtn = fixture.nativeElement.querySelector('.logout-btn');
    logoutBtn.click();
    expect(component.logout).toHaveBeenCalled();
  });
});
