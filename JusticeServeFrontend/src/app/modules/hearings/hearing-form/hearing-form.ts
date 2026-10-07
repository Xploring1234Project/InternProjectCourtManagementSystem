import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { CaseResponse, UserResponse } from '../../../shared/models';
import { HearingApiService } from '../../../core/services/hearing/hearing-api';
import { CaseApiService } from '../../../core/services/case/case-api';
import { UserApiService } from '../../../core/services/user/user-api';
import { CitizenApiService } from '../../../core/services/citizen/citizen-api';
import { NotificationApiService } from '../../../core/services/notification/notification-api';
import { AuthService } from '../../../core/services/auth/auth';

@Component({
  selector: 'app-hearing-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './hearing-form.html',
  styleUrls: ['./hearing-form.css']
})
export class HearingForm implements OnInit {
  hearingForm: FormGroup;
  cases = signal<CaseResponse[]>([]);
  judges = signal<UserResponse[]>([]);
  selectedCase = signal<CaseResponse | null>(null);
  loading = signal(false);
  error = signal('');
  success = signal('');

  constructor(
    private fb: FormBuilder,
    private hearingApi: HearingApiService,
    private caseApi: CaseApiService,
    private userApi: UserApiService,
    private router: Router,
    private route: ActivatedRoute,
    public auth: AuthService
  ) {
    this.hearingForm = this.fb.group({
      caseId: ['', Validators.required],
      judgeId: ['', Validators.required],
      date: ['', Validators.required],
      time: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    // Load cases based on user role
    if (this.auth.hasRole('JUDGE')) {
      // For judges, show only cases assigned to them
      const judgeId = this.auth.currentUser?.userId;
      if (judgeId) {
        this.caseApi.getByJudgeId(judgeId).subscribe({
          next: casesData => {
            this.cases.set(casesData.filter(caseItem => caseItem.status !== 'CLOSED' && caseItem.status !== 'DISMISSED'));
          },
          error: () => {}
        });

        this.userApi.getById(judgeId).subscribe({
          next: judgeData => this.judges.set([judgeData]),
          error: () => {}
        });
      }

    } else {
      // For other users, show all cases
      this.caseApi.getAll().subscribe({
        next: casesData => {
          this.cases.set(casesData.filter(caseItem => caseItem.status !== 'CLOSED' && caseItem.status !== 'DISMISSED'));
        },
        error: () => {}
      });

      this.userApi.getByRole('JUDGE').subscribe({
        next: judgesData => this.judges.set(judgesData),
        error: () => {}
      });
    }



    // Pre-fill from query params (e.g. from case detail page)
    const caseIdParam = this.route.snapshot.queryParams['caseId'];
    if (caseIdParam) {
      this.hearingForm.patchValue({ caseId: +caseIdParam });
      this.caseApi.getById(+caseIdParam).subscribe({
        next: caseItem => this.selectedCase.set(caseItem),
        error: () => {}
      });
    }

    // Pre-fill judge if current user is a judge
    if (this.auth.hasRole('JUDGE')) {
      this.hearingForm.patchValue({ judgeId: this.auth.currentUser!.userId });
    }
  }

  onCaseChange(): void {
    const selectedCaseId = +this.hearingForm.get('caseId')?.value;
    const matchedCase = this.cases().find(caseItem => caseItem.caseId === selectedCaseId);
    this.selectedCase.set(matchedCase || null);
  }

  onSubmit(): void {
    if (this.hearingForm.invalid) {
      this.hearingForm.markAllAsTouched();
      this.error.set('Please fill all required fields.');
      return;
    }
    
    this.loading.set(true);
    this.error.set('');

    const formValues = this.hearingForm.value;

    // Build payload with ALL required fields that the backend expects
    const payload: any = {
      caseId: formValues.caseId,
      judgeId: formValues.judgeId,
      date: formValues.date,
      time: formValues.time,
      caseTitle: this.selectedCase()?.title || `Case #${formValues.caseId}`,
      // citizenUserId comes from the citizen who owns the case
      // The hearing-service requires this for notifications
      citizenUserId: this.selectedCase()?.citizenId || null,
    };

    if (this.selectedCase()?.lawyerId) {
      payload.lawyerUserId = this.selectedCase()!.lawyerId;
    }

    this.hearingApi.scheduleHearing(payload).subscribe({
      next: scheduledHearing => {
        this.success.set(`Hearing #${scheduledHearing.hearingId} scheduled for ${scheduledHearing.date} at ${scheduledHearing.time}!`);
        this.loading.set(false);
        setTimeout(() => this.router.navigate(['/hearings']), 1500);
      },
      error: err => {
        this.error.set(err.error?.message || 'Failed to schedule hearing. Check all fields.');
        this.loading.set(false);
      }
    });
  }
}