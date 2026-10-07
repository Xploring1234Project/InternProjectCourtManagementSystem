import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CitizenResponse, UserResponse } from '../../../shared/models';
import { CaseApiService } from '../../../core/services/case/case-api';
import { CitizenApiService } from '../../../core/services/citizen/citizen-api';
import { NotificationApiService } from '../../../core/services/notification/notification-api';
import { UserApiService } from '../../../core/services/user/user-api';
import { AuthService } from '../../../core/services/auth/auth';


@Component({
  selector: 'app-case-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './case-form.html',
  styleUrls: ['./case-form.css']
})
export class CaseForm implements OnInit {
  caseForm: FormGroup;
  
  citizensList = signal<CitizenResponse[]>([]);
  clerksList = signal<UserResponse[]>([]);
  allLawyersList = signal<UserResponse[]>([]);
  filteredLawyersList = signal<UserResponse[]>([]);

  selectedLawyerId = signal<number | null>(null);
  selectedLawyerName = signal<string>('');
  isLoadingLawyers = signal<boolean>(false);
  lawyerLoadErrorMessage = signal<string>('');

  isSubmitting = signal<boolean>(false);
  errorMessage = signal<string>('');
  successMessage = signal<string>('');

  constructor(
    private fb: FormBuilder,
    private caseApi: CaseApiService,
    private citizenApi: CitizenApiService,
    private userApi: UserApiService,
    private notificationApi: NotificationApiService,
    public authService: AuthService,
    private router: Router
  ) {
    // Initialize Reactive Form
    this.caseForm = this.fb.group({
      title: ['', [Validators.required]],
      description: [''],
      citizenId: [0, [Validators.required, Validators.min(1)]],
      lawyerId: [null],
      hasLawyerToggle: [false],
      lawyerSearchTerm: ['']
    });
  }

  get isCitizen(): boolean { 
    return this.authService.hasRole('CITIZEN'); 
  }

  ngOnInit(): void {
    this.loadInitialData();
  }

  private loadInitialData(): void {
    if (this.isCitizen) {
      this.citizenApi.getByUserId(this.authService.currentUser!.userId).subscribe({
        next: (citizen) => this.caseForm.patchValue({ citizenId: citizen.citizenId }),
        error: () => this.errorMessage.set('Please complete your citizen profile before filing a case.')
      });
    } else {
      this.citizenApi.getAll().subscribe({ 
        next: (citizens) => this.citizensList.set(citizens), 
        error: () => {} 
      });
    }

    this.userApi.getByRole('CLERK').subscribe({ 
      next: (clerks) => this.clerksList.set(clerks), 
      error: () => {} 
    });
  }

  onLawyerToggle(): void {
    const isEnabled = this.caseForm.get('hasLawyerToggle')?.value;
    if (isEnabled) {
      this.fetchLawyers();
    } else {
      this.clearSelectedLawyer();
    }
  }

  fetchLawyers(): void {
    this.isLoadingLawyers.set(true);
    this.lawyerLoadErrorMessage.set('');
    
    this.userApi.getByRole('LAWYER').subscribe({
      next: (lawyers) => {
        this.allLawyersList.set(lawyers);
        this.filteredLawyersList.set([...lawyers]);
        this.isLoadingLawyers.set(false);
      },
      error: (err) => {
        this.lawyerLoadErrorMessage.set(`Could not load lawyers (${err.status || 'network error'}). Check that the API gateway is running on port 9090.`);
        this.isLoadingLawyers.set(false);
      }
    });
  }

  filterLawyersList(): void {
    const searchTerm = this.caseForm.get('lawyerSearchTerm')?.value.toLowerCase().trim();
    if (!searchTerm) {
      this.filteredLawyersList.set([...this.allLawyersList()]);
      return;
    }

    this.filteredLawyersList.set(this.allLawyersList().filter(lawyer =>
      lawyer.name.toLowerCase().includes(searchTerm) || 
      lawyer.email.toLowerCase().includes(searchTerm)
    ));
  }

  clearLawyerSearch(): void {
    this.caseForm.patchValue({ lawyerSearchTerm: '' });
    this.filteredLawyersList.set([...this.allLawyersList()]);
  }

  selectLawyer(lawyer: UserResponse): void {
    this.selectedLawyerId.set(lawyer.userId);
    this.selectedLawyerName.set(lawyer.name);
    this.caseForm.patchValue({ lawyerId: lawyer.userId });
  }

  clearSelectedLawyer(): void {
    this.selectedLawyerId.set(null);
    this.selectedLawyerName.set('');
    this.caseForm.patchValue({ 
      lawyerId: null,
      lawyerSearchTerm: ''
    });
    this.filteredLawyersList.set([...this.allLawyersList()]);
  }

  onSubmit(): void {
    if (this.caseForm.invalid) {
      this.markFormGroupTouched(this.caseForm);
      if (this.caseForm.get('citizenId')?.value === 0) {
        this.errorMessage.set('Citizen profile not found. Please complete your profile first.');
      } else if (this.caseForm.get('title')?.invalid) {
        this.errorMessage.set('Case title is required.');
      }
      return;
    }

    const hasLawyerEnabled = this.caseForm.get('hasLawyerToggle')?.value;
    if (hasLawyerEnabled && !this.selectedLawyerId()) {
      this.errorMessage.set('You enabled "I have a lawyer" but did not select one.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    const formValues = this.caseForm.value;
    const payload: any = {
      citizenId: formValues.citizenId,
      title: formValues.title,
      description: formValues.description
    };
    
    if (formValues.lawyerId) {
      payload.lawyerId = formValues.lawyerId;
    }

    this.caseApi.fileNewCase(payload).subscribe({
      next: (newCase) => {
        this.handlePostSubmission(newCase);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to file case. Please try again.');
        this.isSubmitting.set(false);
      }
    });
  }

  private handlePostSubmission(createdCase: any): void {
    if (!this.selectedLawyerId()) {
      // Notify clerks to assign a lawyer
      this.clerksList().forEach(clerk => {
        this.notificationApi.sendNotification({
          userId: clerk.userId,
          entityId: createdCase.caseId,
          category: 'CASE',
          message: `New case filed: "${createdCase.title}" (Case #${createdCase.caseId}) by ${createdCase.citizenName}. Please assign a lawyer.`
        }).subscribe();
      });
      this.successMessage.set(`Case #${createdCase.caseId} filed! The clerk will assign a lawyer and notify you.`);
    } else {
      this.successMessage.set(`Case #${createdCase.caseId} filed! Lawyer ${this.selectedLawyerName()} has been notified.`);
    }

    this.isSubmitting.set(false);
    setTimeout(() => this.router.navigate(['/cases', createdCase.caseId]), 1800);
  }

  private markFormGroupTouched(formGroup: FormGroup) {
    Object.values(formGroup.controls).forEach(control => {
      control.markAsTouched();
      if ((control as any).controls) {
        this.markFormGroupTouched(control as FormGroup);
      }
    });
  }
}