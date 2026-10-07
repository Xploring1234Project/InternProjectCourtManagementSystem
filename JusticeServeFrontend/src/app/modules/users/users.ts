import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserResponse } from '../../shared/models';
import { UserApiService } from '../../core/services/user/user-api';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.html',
  styleUrls: ['./users.css']
})
export class Users implements OnInit {

  users = signal<UserResponse[]>([]);
  loading = signal(true);

  searchText = signal('');
  selectedRoleFilter = signal('');
  selectedStatusFilter = signal('');

  savingRoleForUserId = signal<number | null>(null);

  toastMessage = signal<{ msg: string; type: 'success' | 'error' } | null>(null);

  roleSelections = signal<Record<number, string>>({});

  readonly roles = [
    'CITIZEN', 'LAWYER', 'JUDGE', 'CLERK',
    'ADMIN', 'COMPLIANCE', 'AUDITOR'
  ];

  constructor(private userApi: UserApiService) {}

  ngOnInit(): void {
    this.userApi.getAll().subscribe({
      next: usersResponse => {
        this.users.set(usersResponse);
        
        const selections: Record<number, string> = {};
        usersResponse.forEach(user => {
          selections[user.userId] = user.role;
        });
        this.roleSelections.set(selections);
        
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  filteredUsers = computed(() => {
    const search = this.searchText().toLowerCase();
    const role = this.selectedRoleFilter();
    const status = this.selectedStatusFilter();

    return this.users().filter(user =>
      (!role || user.role === role) &&
      (!status || user.status === status) &&
      (!search ||
        user.name.toLowerCase().includes(search) ||
        user.email.toLowerCase().includes(search))
    );
  });

  updateRoleSelection(userId: number, newRole: string): void {
    this.roleSelections.update(selections => ({
      ...selections,
      [userId]: newRole
    }));
  }

  changeUserRole(user: UserResponse): void {
    const newRole = this.roleSelections()[user.userId];
    if (newRole === user.role) return;

    const confirmed = confirm(
      `Change ${user.name}'s role from ${user.role} to ${newRole}?\n\nUser must log out and log back in.`
    );

    if (!confirmed) {
      this.updateRoleSelection(user.userId, user.role);
      return;
    }

    this.savingRoleForUserId.set(user.userId);

    this.userApi.updateRole(user.userId, newRole).subscribe({
      next: updatedUser => {
        this.users.update(currentUsers => currentUsers.map(existingUser =>
          existingUser.userId === updatedUser.userId ? updatedUser : existingUser
        ));
        this.updateRoleSelection(updatedUser.userId, updatedUser.role);
        this.savingRoleForUserId.set(null);
        this.showToast(`${updatedUser.name}'s role updated to ${updatedUser.role}.`, 'success');
      },
      error: error => {
        this.savingRoleForUserId.set(null);
        this.updateRoleSelection(user.userId, user.role);
        this.showToast(error.error?.message || 'Role update failed.', 'error');
      }
    });
  }

  toggleUserStatus(user: UserResponse): void {
    const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const confirmed = confirm(`${newStatus === 'ACTIVE' ? 'Activate' : 'Deactivate'} ${user.name}?`);
    if (!confirmed) return;

    this.userApi.updateStatus(user.userId, newStatus).subscribe(updatedUser => {
      this.users.update(currentUsers => currentUsers.map(existingUser =>
        existingUser.userId === updatedUser.userId ? updatedUser : existingUser
      ));
      this.showToast(`${updatedUser.name} is now ${updatedUser.status}.`, 'success');
    });
  }

  deleteUser(userId: number): void {
    const user = this.users().find(u => u.userId === userId);
    if (!user) return;

    const confirmed = confirm(`Permanently delete ${user.name}?`);
    if (!confirmed) return;

    this.userApi.delete(userId).subscribe(() => {
      this.users.update(currentUsers => currentUsers.filter(u => u.userId !== userId));
      this.showToast('User deleted successfully.', 'success');
    });
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.toastMessage.set({ msg: message, type });
    setTimeout(() => this.toastMessage.set(null), 4000);
  }

  getPortalName(role: string): string {
    const portalMap: Record<string, string> = {
      ADMIN: 'Admin Panel',
      JUDGE: 'Judge Console',
      LAWYER: 'Lawyer Dashboard',
      CLERK: 'Clerk Panel',
      CITIZEN: 'Citizen Portal',
      COMPLIANCE: 'Compliance Console',
      AUDITOR: 'Auditor Dashboard'
    };
    return portalMap[role] ?? role;
  }
}