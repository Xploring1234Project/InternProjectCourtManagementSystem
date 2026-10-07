import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth/auth';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  loginForm!: FormGroup;
  loading = signal<boolean>(false);
  error = signal<string>('');

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)])
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set('');

    // Calling the AuthService login method which handles the AuthResponse and Token storage
    this.authService.login(this.loginForm.value).subscribe({
      next: (res) => {
        this.loading.set(false);
        console.log('Login successful, Token stored:', res.token);
        
        // Navigate to the home/dashboard page after successful login
        this.router.navigate(['/dashboard']); 
      },
      error: (err) => {
        this.loading.set(false);
        // Handling backend error messages (e.g., Bad Credentials)
        this.error.set(err.error?.message || 'Invalid email or password');
      }
    });
  }
}