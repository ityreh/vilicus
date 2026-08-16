import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterLinkActive } from '@angular/router';

interface NavItem {
  label: string;
  route: string;
  icon: string; // SVG path or icon name
}

/**
 * Sidebar component — left navigation panel.
 * Desktop: always visible. Mobile (< 880px): off-canvas drawer with backdrop.
 */
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  isOpen = true; // Desktop: always true; mobile: toggles with backdrop

  navItems: NavItem[] = [
    { label: 'Dashboard', route: '/dashboard', icon: 'grid' },
    { label: 'Transactions', route: '/transactions', icon: 'table' },
    { label: 'Import', route: '/import', icon: 'upload' },
    { label: 'Accounts', route: '/accounts', icon: 'card' },
    { label: 'Analytics', route: '/analytics', icon: 'bars' },
    { label: 'Settings', route: '/settings', icon: 'gear' }
  ];

  closeSidebar() {
    this.isOpen = false;
  }
}
