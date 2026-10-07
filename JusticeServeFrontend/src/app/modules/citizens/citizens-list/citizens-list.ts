import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { CitizenResponse, CaseResponse } from '../../../shared/models';
import { CitizenApiService } from '../../../core/services/citizen/citizen-api';
import { CaseApiService } from '../../../core/services/case/case-api';
import { AuthService } from '../../../core/services/auth/auth';

@Component({
  selector: 'app-citizens-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, ReactiveFormsModule],
  templateUrl: './citizens-list.html',
  styleUrls: ['./citizens-list.css']
})
export class CitizensList implements OnInit {
  citizenList = signal<CitizenResponse[]>([]);
  isLoading = signal(true);
  searchForm: FormGroup;

  constructor(
    private citizenApi: CitizenApiService, 
    private caseApi: CaseApiService, 
    private authService: AuthService,
    private fb: FormBuilder
  ) {
    this.searchForm = this.fb.group({
      searchTerm: ['']
    });
  }

  get isCitizen(): boolean { 
    return this.authService.hasRole('CITIZEN'); 
  }

  get isLawyer(): boolean { 
    return this.authService.hasRole('LAWYER'); 
  }

  filteredCitizens = computed(() => {
    const list = this.citizenList();
    // Reactive Form values are not signals, so we access normally. 
    // If the template needs to react to typing, typically we'd use valueChanges 
    // to update a search signal, but per instructions we keep Reactive Forms logic untouched.
    const term = this.searchForm.get('searchTerm')?.value?.toLowerCase() || '';
    return list.filter(citizen =>
      !term || citizen.name?.toLowerCase().includes(term)
    );
  });

  ngOnInit(): void {
    this.loadData();
    // Trigger computed update when form changes if needed by UI
    this.searchForm.valueChanges.subscribe(() => {
      // Logic for filteredCitizens computed to re-evaluate is automatic if it depends on a signal.
      // Since searchTerm is in a Reactive Form, we force update by setting the same list.
      this.citizenList.set([...this.citizenList()]);
    });
  }

  private loadData(): void {
    if (this.isLawyer) {
      this.loadLawyerClients();
    } else {
      this.loadAllCitizens();
    }
  }

  private loadLawyerClients(): void {
    const currentUserId = this.authService.currentUser?.userId;
    if (!currentUserId) { 
      this.isLoading.set(false); 
      return; 
    }

    this.caseApi.getByLawyerId(currentUserId).subscribe({
      next: (cases: CaseResponse[]) => {
        // Filter out cases without a valid citizenId and extract unique citizen IDs
        const validCases = cases.filter(legalCase => legalCase.citizenId != null);
        const uniqueCitizenIds = new Set(validCases.map(legalCase => legalCase.citizenId));
        
        this.citizenApi.getAll().subscribe({
          next: (allCitizens: CitizenResponse[]) => {
            // Filter citizens whose citizenId is in the unique set
            const lawyerCitizens = allCitizens.filter(citizen => 
              uniqueCitizenIds.has(citizen.citizenId)
            );
            this.citizenList.set(lawyerCitizens);
            this.isLoading.set(false);
          },
          error: () => this.isLoading.set(false)
        });
      },
      error: () => this.isLoading.set(false)
    });
  }

  private loadAllCitizens(): void {
    this.citizenApi.getAll().subscribe({ 
      next: (data: CitizenResponse[]) => { 
        this.citizenList.set(data); 
        this.isLoading.set(false); 
      }, 
      error: () => this.isLoading.set(false) 
    });
  }
}