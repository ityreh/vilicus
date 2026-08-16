import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

/**
 * Navbar component — top bar with logo, account selector, user menu, logout.
 * Responsive: shows hamburger on mobile (< 880px).
 */
@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  userInitials = 'JD';
  userName = 'John Doe';
  selectedAccount = 'Checking';

  accounts = [
    { id: 1, name: 'Checking' },
    { id: 2, name: 'Savings' }
  ];

  onAccountChange(account: string) {
    this.selectedAccount = account;
  }

  logout() {
    console.log('Logout clicked');
    // TODO: Implement logout via AuthService
  }

  toggleSidebar() {
    // Emit event to toggle sidebar drawer on mobile
    console.log('Toggle sidebar');
  }
}
