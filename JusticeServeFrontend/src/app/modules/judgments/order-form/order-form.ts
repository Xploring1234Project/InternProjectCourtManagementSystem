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
  selector: 'app-order-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './order-form.html',
  styleUrls: ['./order-form.css']
})
export class OrderForm implements OnInit {
  orderForm!: FormGroup;
  cases = signal<CaseResponse[]>([]);
  judges = signal<UserResponse[]>([]);
  
  loading = signal(false); 
  error = signal(''); 
  success = signal(''); 

  constructor(
    private fb: FormBuilder,
    private orderApi: CourtOrderApiService, 
    private caseApi: CaseApiService, 
    private userApi: UserApiService, 
    public auth: AuthService, 
    private router: Router
  ) {}

  ngOnInit(): void {
    // Initialize the Reactive Form
    this.orderForm = this.fb.group({
      caseId: ['', Validators.required],
      judgeId: ['', Validators.required],
      description: ['', [Validators.required, Validators.minLength(5)]]
    });

    // Fetch Cases
    this.caseApi.getAll().subscribe(allCases => {
      if (this.auth.role === 'JUDGE') {
        // Filter cases mapped specifically to this judge
        this.cases.set(allCases.filter(caseItem => caseItem.judgeId === this.auth.currentUser?.userId));
        
        // Auto-fill and hide the judge select field logic if the user is a judge
        const currentJudgeId = this.auth.currentUser?.userId || '';
        this.orderForm.patchValue({ judgeId: currentJudgeId });
      } else {
        this.cases.set(allCases);
      }
    });

    // Fetch Judges if the user is not a judge (e.g., Clerk or Admin issuing the order)
    if (this.auth.role !== 'JUDGE') {
      this.userApi.getByRole('JUDGE').subscribe(judgesList => {
        this.judges.set(judgesList);
      });
    }
  }

  onSubmit(): void {
    if (this.orderForm.invalid) {
      this.orderForm.markAllAsTouched();
      return;
    }

    this.loading.set(true); 
    this.error.set('');

    const payload = {
      caseId: Number(this.orderForm.value.caseId),
      judgeId: Number(this.orderForm.value.judgeId),
      description: this.orderForm.value.description
    };
    
    this.orderApi.issueOrder(payload as any).subscribe({
      next: (orderResponse) => { 
        this.success.set(`Order #${orderResponse.orderId} issued successfully!`); 
        this.loading.set(false);
        setTimeout(() => this.router.navigate(['/judgments']), 1200); 
      },
      error: (err) => { 
        this.error.set(err.error?.message || 'Failed to issue the order. Please try again.'); 
        this.loading.set(false); 
      }
    });
  }
}