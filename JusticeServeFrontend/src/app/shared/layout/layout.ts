import { Component, OnInit, OnDestroy, signal, computed } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth/auth';
import { NotificationApiService } from '../../core/services/notification/notification-api';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './layout.html',
  styleUrl: './layout.css'
})
export class Layout implements OnInit, OnDestroy {
  unreadCount = signal<number>(0);
  currentTime = signal<Date>(new Date());
  
  private timerSub?: Subscription;
  private notifSub?: Subscription;

  constructor(
    private auth: AuthService, 
    private notifApi: NotificationApiService
  ) {}

  // --- User Info Getters ---
  userName = computed(() => this.auth.currentUser?.name ?? 'User');
  userRole = computed(() => this.auth.currentUser?.role ?? '');
  
  initials = computed(() => {
    const name = this.userName();
    if (!name || name === 'User') return '??';
    return name
      .split(' ')
      .map(w => w[0])
      .join('')
      .substring(0, 2)
      .toUpperCase(); 
  });

  // --- Role Check Getters (Using AuthService.hasRole) ---
  isAdmin = computed(() => this.auth.hasRole('ADMIN'));
  isJudge = computed(() => this.auth.hasRole('JUDGE'));
  isClerk = computed(() => this.auth.hasRole('CLERK'));
  isLawyer = computed(() => this.auth.hasRole('LAWYER'));
  isCitizen = computed(() => this.auth.hasRole('CITIZEN'));
  isCompliance = computed(() => this.auth.hasRole('COMPLIANCE'));
  isAuditor = computed(() => this.auth.hasRole('AUDITOR'));

  ngOnInit(): void {
    // 1. Update the clock every second
    this.timerSub = interval(1000).subscribe(() => {
      this.currentTime.set(new Date());
    });

    // 2. Setup Notification Polling
    const userId = this.auth.currentUser?.userId;
    if (userId) {
      // Initial fetch
      this.fetchNotificationCount(userId);

      // Poll every 30 seconds
      this.notifSub = interval(30000)
        .pipe(
          switchMap(() => this.notifApi.getUnreadCount(userId)))
        .subscribe({
          next: (count) => this.unreadCount.set(count),
          error: (err) => console.error('Failed to poll notifications', err)
        });
    }
  }

  fetchNotificationCount(userId: number | string): void {
    this.notifApi.getUnreadCount(userId).subscribe({
      next: (count) => this.unreadCount.set(count),
      error: () => this.unreadCount.set(0)
    });
  }

  logout(): void {
    this.auth.logout();
  }

  ngOnDestroy(): void {
    // Clean up subscriptions to prevent memory leaks
    this.timerSub?.unsubscribe();
    this.notifSub?.unsubscribe();
  }
}