import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuditLogResponse } from '../../shared/models';
import { AuditLogApiService } from '../../core/services/auditlog/auditlog-api';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe],
  templateUrl: './auditlogs.html',
  styleUrls: ['./auditlogs.css']
})
export class AuditLogs implements OnInit {
  logs = signal<AuditLogResponse[]>([]);
  filteredLogs = signal<AuditLogResponse[]>([]);
  loading = signal<boolean>(true);
  lastUpdated = signal<Date>(new Date());

  filterForm!: FormGroup;

  uniqueActions = signal<string[]>([]);
  uniqueResources = signal<string[]>([]);

  // Derived state converted to a computed signal
  uniqueUsers = computed(() => new Set(this.logs().map(logEntry => logEntry.userId)).size);

  constructor(
    private auditApi: AuditLogApiService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initFilterForm();
    this.loadLogs();
  }

  initFilterForm(): void {
    this.filterForm = this.fb.group({
      actionFilter: [''],
      resourceFilter: [''],
      userIdFilter: ['']
    });

    // Automatically apply filters whenever any form value changes
    this.filterForm.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  loadLogs(): void {
    this.loading.set(true);
    this.auditApi.getSystemAuditTrail().subscribe({
      next: (data) => {
        this.logs.set(data.sort((a, b) => 
          new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
        ));
        this.updateFilterDropdowns();
        this.applyFilters();
        this.loading.set(false);
        this.lastUpdated.set(new Date());
      },
      error: (err) => {
        console.error('Error loading audit logs:', err);
        this.loading.set(false);
      }
    });
  }

  updateFilterDropdowns(): void {
    this.uniqueActions.set([...new Set(this.logs().map(logEntry => logEntry.action))].sort());
    this.uniqueResources.set([...new Set(this.logs().map(logEntry => logEntry.resource))].sort());
  }

  applyFilters(): void {
    const formValues = this.filterForm.value;
    const filterAction = formValues.actionFilter;
    const filterResource = formValues.resourceFilter;
    const filterUserId = formValues.userIdFilter;

    const parsedUserId = filterUserId == null || filterUserId === '' ? null : Number(filterUserId);

    const filtered = this.logs().filter(logEntry => {
      const matchAction = !filterAction || logEntry.action === filterAction;
      const matchResource = !filterResource || logEntry.resource === filterResource;
      const matchUserId = parsedUserId == null || logEntry.userId === parsedUserId;
      
      return matchAction && matchResource && matchUserId;
    });

    this.filteredLogs.set(filtered);
  }

  getActionClass(action: string): string {
    const actionLower = action.toLowerCase();
    if (actionLower.includes('create') || actionLower.includes('post')) return 'create';
    if (actionLower.includes('update') || actionLower.includes('put') || actionLower.includes('patch')) return 'update';
    if (actionLower.includes('delete')) return 'delete';
    if (actionLower.includes('login') || actionLower.includes('auth')) return 'login';
    return 'read';
  }

  getResourceIcon(resource: string): string {
    const resourceLower = resource.toLowerCase();
    if (resourceLower.includes('user')) return 'bi-person';
    if (resourceLower.includes('case')) return 'bi-folder';
    if (resourceLower.includes('hearing')) return 'bi-calendar';
    if (resourceLower.includes('judgment')) return 'bi-hammer';
    if (resourceLower.includes('document')) return 'bi-file-text';
    if (resourceLower.includes('audit')) return 'bi-shield-check';
    return 'bi-document';
  }

  refreshLogs(): void {
    this.loadLogs();
  }


}