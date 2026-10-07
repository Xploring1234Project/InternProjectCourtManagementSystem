import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CaseResponse, HearingResponse, ProceedingResponse } from '../../../shared/models';
import { HearingApiService } from '../../../core/services/hearing/hearing-api';
import { CaseApiService } from '../../../core/services/case/case-api';
import { AuthService } from '../../../core/services/auth/auth';

@Component({
  selector: 'app-hearing-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './hearing-details.html',
  styleUrls: ['./hearing-details.css']
})
export class HearingDetails implements OnInit {
  hearing = signal<HearingResponse | null>(null);
  caseData = signal<CaseResponse | null>(null);
  proceedings = signal<ProceedingResponse[]>([]);
  
  loading = signal(true);
  statusMsg = signal('');
  showForm = signal(false);
  
  statusForm: FormGroup;
  proceedingForm: FormGroup;

  statuses = ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'ADJOURNED', 'CANCELLED'];

  constructor(
    private route: ActivatedRoute,
    private hearingApi: HearingApiService,
    private caseApi: CaseApiService,
    private auth: AuthService,
    private fb: FormBuilder
  ) {
    this.statusForm = this.fb.group({
      statusControl: ['', Validators.required]
    });

    this.proceedingForm = this.fb.group({
      notes: ['', Validators.required],
      status: ['', Validators.required]
    });
  }

  get isLawyer(): boolean { 
    return this.auth.hasRole('LAWYER'); 
  }

  get isCaseClosed(): boolean {
    return this.caseData()?.status === 'CLOSED' || this.caseData()?.status === 'DISMISSED';
  }

  ngOnInit(): void {
    const hearingId = +this.route.snapshot.params['id'];
    
    this.hearingApi.getHearingById(hearingId).subscribe(hearingDetails => { 
      this.hearing.set(hearingDetails); 
      this.statusForm.patchValue({ statusControl: hearingDetails.status });
      
      // Fetch case details
      this.caseApi.getById(hearingDetails.caseId).subscribe(caseDetails => {
        this.caseData.set(caseDetails);
      });
      
      this.loading.set(false); 
    });

    this.hearingApi.getProceedings(hearingId).subscribe(proceedingList => {
      this.proceedings.set(proceedingList);
    });
  }

  updateStatus(): void {
    if (this.statusForm.invalid) return;

    const updatedStatus = this.statusForm.get('statusControl')?.value;

    this.hearingApi.updateStatus(this.hearing()!.hearingId, updatedStatus).subscribe(updatedHearing => {
      this.hearing.set(updatedHearing);
      this.statusMsg.set('Updated!');
      setTimeout(() => this.statusMsg.set(''), 2000);
    });
  }

  addProceeding(): void {
    if (this.proceedingForm.invalid) {
      this.proceedingForm.markAllAsTouched();
      return;
    }

    const proceedingPayload = {
      hearingId: this.hearing()!.hearingId,
      ...this.proceedingForm.value
    };

    this.hearingApi.addProceeding(this.hearing()!.hearingId, proceedingPayload).subscribe(newProceeding => {
      this.proceedings.update(p => [...p, newProceeding]);
      this.showForm.set(false);
      this.proceedingForm.reset({ status: 'IN_PROGRESS' });
    });
  }

  badge(statusText: string): string {
    const statusMap: Record<string, string> = { 
      SCHEDULED: 'bg-primary', 
      IN_PROGRESS: 'bg-info text-dark', 
      COMPLETED: 'bg-success', 
      ADJOURNED: 'bg-warning text-dark', 
      CANCELLED: 'bg-danger' 
    };
    return statusMap[statusText] ?? 'bg-secondary';
  }

  caseStatusBadge(statusText: string): string {
    const caseStatusMap: Record<string, string> = { 
      FILED: 'bg-secondary', 
      ACTIVE: 'bg-success', 
      CLOSED: 'bg-dark', 
      UNDER_REVIEW: 'bg-warning text-dark', 
      HEARING_SCHEDULED: 'bg-info text-dark', 
      JUDGMENT_PENDING: 'bg-danger', 
      DISMISSED: 'bg-secondary' 
    };
    return caseStatusMap[statusText] ?? 'bg-secondary';
  }
}