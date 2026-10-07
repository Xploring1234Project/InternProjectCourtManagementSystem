import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CaseResponse, CitizenResponse, DocumentResponse } from '../../../shared/models';
import { AuthService } from '../../../core/services/auth/auth';
import { CitizenApiService } from '../../../core/services/citizen/citizen-api';
import { CaseApiService } from '../../../core/services/case/case-api';

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './my-profile.html',
  styleUrls: ['./my-profile.css']
})
export class MyProfile implements OnInit {
  citizenData = signal<CitizenResponse | null>(null);
  documentList = signal<DocumentResponse[]>([]);
  caseList = signal<CaseResponse[]>([]);
  isLoading = signal(true);
  isSaving = signal(false);
  errorMessage = signal('');

  showDocForm = signal(false);
  selectedDocType = signal('');
  fileUriPath = signal('');
  docTypeOptions = ['PETITION', 'EVIDENCE', 'ORDER', 'IDPROOF', 'LEGALDOC'];

  isEditing = signal(false);
  profileForm = { 
    userId: 0, 
    name: '', 
    dob: '', 
    gender: '', 
    address: '', 
    contactInfo: '' 
  };

  constructor(
    public authService: AuthService,
    private citizenApiService: CitizenApiService,
    private caseApiService: CaseApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const currentUserId = this.authService.currentUser?.userId;
    if (!currentUserId) {
      this.isLoading.set(false);
      return;
    }
    
    this.profileForm.userId = currentUserId;
    this.profileForm.name = this.authService.currentUser?.name ?? '';

    this.citizenApiService.getByUserId(currentUserId).subscribe({
      next: (response) => {
        this.citizenData.set(response);
        this.isLoading.set(false);
        this.populateFormData(response);
        this.loadRelatedData(response.citizenId);
      },
      error: () => {
        this.citizenData.set(null);
        this.isLoading.set(false);
      }
    });
  }

  populateFormData(citizen: CitizenResponse): void {
    this.profileForm.name = citizen.name;
    this.profileForm.dob = citizen.dob;
    this.profileForm.gender = citizen.gender;
    this.profileForm.address = citizen.address;
    this.profileForm.contactInfo = citizen.contactInfo;
  }

  startEdit(): void {
    this.isEditing.set(true);
    const data = this.citizenData();
    if (data) this.populateFormData(data);
  }

  cancelEdit(): void {
    this.isEditing.set(false);
    const data = this.citizenData();
    if (data) this.populateFormData(data);
  }

  updateProfile(): void {
    const data = this.citizenData();
    if (!data) return;
    this.isSaving.set(true);
    this.errorMessage.set('');

    this.citizenApiService.update(data.citizenId, this.profileForm as any).subscribe({
      next: (updatedCitizen) => {
        this.citizenData.set(updatedCitizen);
        this.isEditing.set(false);
        this.isSaving.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to update profile');
        this.isSaving.set(false);
      }
    });
  }

  loadRelatedData(citizenId: number): void {
    this.citizenApiService.getDocuments(citizenId).subscribe(docs => this.documentList.set(docs));
    this.caseApiService.getByCitizenId(citizenId).subscribe(cases => this.caseList.set(cases));
  }

  registerProfile(): void {
    this.isSaving.set(true);
    this.errorMessage.set('');

    this.citizenApiService.create(this.profileForm as any).subscribe({
      next: (newCitizen) => {
        this.citizenData.set(newCitizen);
        this.isSaving.set(false);
        this.loadRelatedData(newCitizen.citizenId);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to save profile');
        this.isSaving.set(false);
      }
    });
  }

  addDocument(): void {
    const type = this.selectedDocType();
    const uri = this.fileUriPath();
    const data = this.citizenData();
    if (!type || !uri || !data) return;

    this.citizenApiService.addDocument(data.citizenId, {
      docType: type,
      fileUri: uri
    } as any).subscribe(newDoc => {
      this.documentList.update(list => [...list, newDoc]);
      this.showDocForm.set(false);
      this.selectedDocType.set('');
      this.fileUriPath.set('');
    });
  }
}