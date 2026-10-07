import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuditResponse, ComplianceRecordResponse, UserResponse } from '../../shared/models';
import { ComplianceAuditService } from '../../core/services/compliance/compliance-api';
import { UserApiService } from '../../core/services/user/user-api';
import { AuthService } from '../../core/services/auth/auth';

@Component({
  selector: 'app-compliance',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './compliance.html',
  styleUrls: ['./compliance.css']
})
export class Compliance implements OnInit {
  tab = signal('compliance');
  
  records = signal<ComplianceRecordResponse[]>([]);
  audits = signal<AuditResponse[]>([]);
  
  loading = signal(true);
  auditsLoading = signal(true);
  showRecordForm = signal(false);
  showAuditForm = signal(false);
  toast = signal<{ msg: string; type: 'success' | 'error' } | null>(null);

  complianceForm: FormGroup;
  auditForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private complianceApi: ComplianceAuditService, 
    private auth: AuthService
  ) {
    this.complianceForm = this.fb.group({
      entityId: ['', [Validators.required, Validators.min(1)]],
      type: ['', Validators.required],
      result: ['', Validators.required],
      notes: ['']
    });

    this.auditForm = this.fb.group({
      officerId: ['', Validators.required],
      scope: ['', Validators.required],
      findings: ['']
    });
  }

  get isAuditor(): boolean {
    return this.auth.hasRole('AUDITOR');
  }

  get isCompliance(): boolean {
    return this.auth.hasRole('COMPLIANCE');
  }

  ngOnInit(): void {
    this.complianceApi.getAllComplianceRecords().subscribe({ 
      next: (recordsData) => { 
        this.records.set(recordsData); 
        this.loading.set(false); 
      }, 
      error: () => this.loading.set(false) 
    });

    this.complianceApi.getAllAudits().subscribe({ 
      next: (auditsData) => { 
        this.audits.set(auditsData); 
        this.auditsLoading.set(false); 
      }, 
      error: () => this.auditsLoading.set(false) 
    });

  }

  addRecord(): void {
    if (this.isAuditor) {
      this.showToast('Auditors cannot add compliance records', 'error');
      return;
    }

    if (this.complianceForm.invalid) {
      this.complianceForm.markAllAsTouched();
      this.showToast('Please fill in required fields correctly', 'error');
      return;
    }

    const payload = this.complianceForm.value;
    console.log('Creating compliance record:', payload);

    this.complianceApi.createComplianceRecord(payload).subscribe({
      next: (newRecord) => {
        console.log('Record created successfully:', newRecord);
        //It will always add (append) the new record to the end of the list
        //spread operator
        this.records.update(r => [...r, newRecord]);
        this.showRecordForm.set(false);
        this.complianceForm.reset({ entityId: '', type: '', result: '', notes: '' });
        this.showToast('Record added successfully', 'success');
      },
      error: (err) => {
        console.error('Error creating record:', err);
        this.showToast('Failed to add record: ' + (err.error?.message || err.message), 'error');
      }
    });
  }

  addAudit(): void {
    if (this.isCompliance) {
      this.showToast('Compliance officers cannot add audit records', 'error');
      return;
    }

    if (this.auditForm.invalid) {
      this.auditForm.markAllAsTouched();
      this.showToast('Please select an Officer and enter a Scope', 'error');
      return;
    }

    const payload = this.auditForm.value;
    console.log('Creating audit:', payload);

    this.complianceApi.initiateAudit(payload).subscribe({
      next: (newAudit) => {
        console.log('Audit created successfully:', newAudit);
        //It will always add (append) the new record to the end of the list
        //spread operator
        this.audits.update(a => [...a, newAudit]);
        this.showAuditForm.set(false);
        this.auditForm.reset({ officerId: '', scope: '', findings: '' });
        this.showToast('Audit added successfully', 'success');
      },
      error: (err) => {
        console.error('Error creating audit:', err);
        this.showToast('Failed to add audit: ' + (err.error?.message || err.message), 'error');
      }
    });
  }

  updateAuditStatus(id: number, status: string): void {
    console.log('Updating audit status:', id, status);
    this.complianceApi.updateAuditStatus(id, status).subscribe({
      next: (updatedAudit) => {
        console.log('Audit status updated:', updatedAudit);
        this.audits.update(a => a.map(auditItem => auditItem.auditId === id ? updatedAudit : auditItem));
        this.showToast('Status updated successfully', 'success');
      },
      error: (err) => {
        console.error('Error updating audit status:', err);
        this.showToast('Failed to update status', 'error');
      }
    });
  }

  showToast(msg: string, type: 'success' | 'error'): void {
    this.toast.set({ msg, type });
    setTimeout(() => this.toast.set(null), 4000);
  }

  resultBadge(status: string): string {
    return status === 'COMPLIANT' ? 'bg-success' : status === 'NON_COMPLIANT' ? 'bg-danger' : 'bg-warning text-dark';
  }

  auditBadge(status: string): string {
    return status === 'CLOSED' ? 'bg-success' : status === 'REVIEW' ? 'bg-warning text-dark' : 'bg-primary';
  }
}