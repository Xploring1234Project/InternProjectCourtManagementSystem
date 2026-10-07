import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CaseResponse, UserResponse } from '../../../shared/models';
import { JudgmentApiService } from '../../../core/services/judgment/judgment-api';
import { CaseApiService } from '../../../core/services/case/case-api';
import { UserApiService } from '../../../core/services/user/user-api';
import { AuthService } from '../../../core/services/auth/auth';
import { CourtOrderApiService } from '../../../core/services/courtorder/courtorder-api';

@Component({
  selector: 'app-judgment-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './judgment-form.html',
  styleUrls: ['./judgment-form.css']
})
export class JudgmentForm implements OnInit {
  judgmentForm: FormGroup;
  cases = signal<CaseResponse[]>([]);
  judges = signal<UserResponse[]>([]);
  loading = signal(false);
  error = signal('');
  success = signal('');

  constructor(
    private judgmentApi: JudgmentApiService,
    private orderApi: CourtOrderApiService,
    private caseApi: CaseApiService,
    private userApi: UserApiService,
    public auth: AuthService,
    private router: Router,
    private fb: FormBuilder
  ) {
    // Initialize the reactive form with validators
    this.judgmentForm = this.fb.group({
      caseId: [0, [Validators.required, Validators.min(1)]],
      judgeId: [0, [Validators.required, Validators.min(1)]],
      summary: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  ngOnInit(): void {
    this.caseApi.getAll().subscribe(fetchedCases => {
      if (this.auth.role === 'JUDGE') {
        this.cases.set(fetchedCases.filter(courtCase => courtCase.judgeId === this.auth.currentUser?.userId));
        // Automatically set the judgeId if the logged-in user is a judge
        this.judgmentForm.patchValue({ judgeId: this.auth.currentUser?.userId || 0 });
      } else {
        this.cases.set(fetchedCases);
      }
    });

    if (this.auth.role !== 'JUDGE') {
      this.userApi.getByRole('JUDGE').subscribe(fetchedJudges => {
        this.judges.set(fetchedJudges);
      });
    }
  }

  onSubmit(): void {
    // Check form validity before proceeding
    if (this.judgmentForm.invalid) {
      this.judgmentForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');
    
    this.judgmentApi.recordJudgment(this.judgmentForm.value).subscribe({
      next: judgmentResponse => {
        this.success.set(`Judgment #${judgmentResponse.judgmentId} recorded as DRAFT`);
        setTimeout(() => this.router.navigate(['/judgments']), 1200);
      },
      error: err => {
        this.error.set(err.error?.message || 'Failed to record judgment.');
        this.loading.set(false);
      }
    });
  }
}