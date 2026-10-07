import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CitizenApiService } from '../../../core/services/citizen/citizen-api';
import { AuthService } from '../../../core/services/auth/auth';

@Component({
  selector: 'app-citizen-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './citizen-form.html',
  styleUrls: ['./citizen-form.css']
})
export class CitizenForm implements OnInit {
  citizenForm!: FormGroup;
  isSubmitting = signal(false);
  errorMessage = signal('');
  successMessage = signal('');
  genderOptions = ['Male', 'Female', 'Other'];

  constructor(
    private fb: FormBuilder,
    private citizenService: CitizenApiService,
    private authService: AuthService,
    private router: Router
  ) {
    this.initForm();
  }

  private initForm(): void {
    this.citizenForm = this.fb.group({
      userId: [0],
      fullName: ['', [Validators.required,Validators.minLength(3), Validators.pattern(/^[a-zA-Z ]+$/)]],
      dateOfBirth: ['',[Validators.required]],
      gender: ['',[Validators.required]],
      address: ['',[Validators.required]],
      contactInfo: ['',[Validators.required]],
    });
  }

  get currentUserName(): string { 
    return this.authService.currentUser?.name ?? ''; 
  }

  get currentUserEmail(): string { 
    return this.authService.currentUser?.email ?? ''; 
  }

  ngOnInit(): void {
    const user = this.authService.currentUser;
    if (user) {
      this.citizenForm.patchValue({
        userId: user.userId ?? 0,
        fullName: user.name ?? ''
      });
    }
  }

  onFormSubmit(): void {
    if (this.citizenForm.invalid) {
      this.citizenForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    
    const payload = {
      userId: this.citizenForm.value.userId,
      name: this.citizenForm.value.fullName,
      dob: this.citizenForm.value.dateOfBirth,
      gender: this.citizenForm.value.gender,
      address: this.citizenForm.value.address,
      contactInfo: this.citizenForm.value.contactInfo
    };

    this.citizenService.create(payload as any).subscribe({
      next: (citizen) => {
        this.successMessage.set('Profile registered successfully! Redirecting...');
        setTimeout(() => this.router.navigate(['/citizens', citizen.citizenId]), 1200);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to register profile');
        this.isSubmitting.set(false);
      }
    });
  }
}