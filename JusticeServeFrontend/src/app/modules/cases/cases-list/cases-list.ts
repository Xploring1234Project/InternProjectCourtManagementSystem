import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CaseResponse } from '../../../shared/models';
import { CaseApiService } from '../../../core/services/case/case-api';
import { CitizenApiService } from '../../../core/services/citizen/citizen-api';
import { AuthService } from '../../../core/services/auth/auth';

@Component({
  selector: 'app-cases-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './cases-list.html',
  styleUrls: ['./cases-list.css']
})
export class CasesList implements OnInit {
  allCases = signal<CaseResponse[]>([]);
  filteredCases = signal<CaseResponse[]>([]);
  isLoading = signal<boolean>(true);
  
  searchTerm = signal<string>('');
  selectedStatus = signal<string>('');
  assignmentFilter = signal<string>('');
  
  statusOptions = [
    'FILED', 'UNDER_REVIEW', 'ACTIVE', 
    'HEARING_SCHEDULED', 'JUDGMENT_PENDING', 
    'CLOSED', 'DISMISSED'
  ];

  constructor(
    private caseApiService: CaseApiService,
    private citizenApiService: CitizenApiService,
    private activatedRoute: ActivatedRoute,
    private authService: AuthService
  ) {}

  get isCitizen(): boolean { return this.authService.hasRole('CITIZEN'); }
  get isLawyer(): boolean { return this.authService.hasRole('LAWYER'); }
  get isJudge(): boolean { return this.authService.hasRole('JUDGE'); }
  get isClerk(): boolean { return this.authService.hasRole('CLERK'); }
  get isAdmin(): boolean { return this.authService.hasRole('ADMIN'); }

  ngOnInit(): void {
    this.activatedRoute.queryParamMap.subscribe(params => {
      this.selectedStatus.set(params.get('status') || '');
      this.applyFilters();
    });

    this.loadCasesData();
  }

  private loadCasesData(): void {
    const currentUserId = this.authService.currentUser?.userId;
    if (!currentUserId) {
      this.isLoading.set(false);
      return;
    }

    if (this.isCitizen) {
      this.loadCitizenSpecificCases(currentUserId);
    } else if (this.isLawyer) {
      this.loadLawyerSpecificCases(currentUserId);
    } else if (this.isJudge) {
      this.loadJudgeSpecificCases(currentUserId);
    } else {
      this.loadAllCases();
    }
  }

  private loadCitizenSpecificCases(userId: number): void {
    this.citizenApiService.getByUserId(userId).subscribe({
      next: (citizen) => {
        this.caseApiService.getByCitizenId(citizen.citizenId).subscribe({
          next: (cases) => this.handleDataSuccess(cases),
          error: () => this.isLoading.set(false)
        });
      },
      error: () => {
        // Fallback: search by name if profile isn't fully linked
        this.caseApiService.getAll().subscribe({
          next: (all) => {
            const userCases = all.filter(c => c.citizenName === this.authService.currentUser?.name);
            this.handleDataSuccess(userCases);
          },
          error: () => this.isLoading.set(false)
        });
      }
    });
  }

  private loadLawyerSpecificCases(userId: number): void {
    this.caseApiService.getByLawyerId(userId).subscribe({
      next: (cases) => this.handleDataSuccess(cases),
      error: () => {
        this.caseApiService.getAll().subscribe({
          next: (all) => {
            const assignedCases = all.filter(c => c.lawyerId === userId);
            this.handleDataSuccess(assignedCases);
          },
          error: () => this.isLoading.set(false)
        });
      }
    });
  }

  private loadJudgeSpecificCases(userId: number): void {
    this.caseApiService.getAll().subscribe({
      next: (all) => {
        const judgeCases = all.filter(c => c.judgeId === userId);
        this.handleDataSuccess(judgeCases);
      },
      error: () => this.isLoading.set(false)
    });
  }

  private loadAllCases(): void {
    this.caseApiService.getAll().subscribe({
      next: (cases) => this.handleDataSuccess(cases),
      error: () => this.isLoading.set(false)
    });
  }

  private handleDataSuccess(data: CaseResponse[]): void {
    this.allCases.set(data);
    this.applyFilters();
    this.isLoading.set(false);
  }

  applyFilters(): void {
    this.filteredCases.set(this.allCases().filter(caseItem => {
      const matchesStatus = !this.selectedStatus() || caseItem.status === this.selectedStatus();
      
      const matchesSearch = !this.searchTerm() ||
        caseItem.title?.toLowerCase().includes(this.searchTerm().toLowerCase()) ||
        caseItem.citizenName?.toLowerCase().includes(this.searchTerm().toLowerCase());
      
      const matchesAssignment = !this.assignmentFilter() ||
        (this.assignmentFilter() === 'assigned' ? !!caseItem.lawyerName : !caseItem.lawyerName);
      
      return matchesStatus && matchesSearch && matchesAssignment;
    }));
  }

  getStatusBadgeClass(status: string): string {
    const classMap: Record<string, string> = {
      FILED: 'bg-secondary',
      ACTIVE: 'bg-success',
      CLOSED: 'bg-dark',
      UNDER_REVIEW: 'bg-warning text-dark',
      HEARING_SCHEDULED: 'bg-info text-dark',
      JUDGMENT_PENDING: 'bg-danger',
      DISMISSED: 'bg-secondary'
    };
    return classMap[status] ?? 'bg-secondary';
  }
}