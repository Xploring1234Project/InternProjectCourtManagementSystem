import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CaseResponse, CitizenResponse, DocumentResponse } from '../../../shared/models';
import { CitizenApiService } from '../../../core/services/citizen/citizen-api';
import { CaseApiService } from '../../../core/services/case/case-api';
import { AuthService } from '../../../core/services/auth/auth';

@Component({
  selector: 'app-citizen-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './citizen-details.html',
  styleUrls: ['./citizen-details.css']
})
export class CitizenDetails implements OnInit {
  citizenData = signal<CitizenResponse | undefined>(undefined);
  documentList = signal<DocumentResponse[]>([]);
  caseList = signal<CaseResponse[]>([]);
  isLoading = signal(true);
  
  documentTypes = ['PETITION', 'EVIDENCE', 'ORDER', 'ID_PROOF', 'LEGAL_DOC'];
  documentForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private citizenApi: CitizenApiService,
    private caseApi: CaseApiService,
    private authService: AuthService,
    private fb: FormBuilder
  ) { // check this form builder
    this.documentForm = this.fb.group({
      docType: ['', Validators.required],
      fileUri: ['', Validators.required]
    });
  }

  get isLawyer(): boolean { 
    return this.authService.hasRole('LAWYER'); 
  }

  get isClerk(): boolean {
    return this.authService.hasRole('CLERK');
  }

  filteredCases = computed(() => {
    const list = this.caseList();
    if (!this.isLawyer) return list;
    
    const currentUserId = this.authService.currentUser?.userId;
    return list.filter(assignedCase => (assignedCase as any).lawyerId === currentUserId);
  });

  ngOnInit(): void {
    const citizenId = +this.route.snapshot.params['id'];
    this.loadCitizenDetails(citizenId);
  }

  private loadCitizenDetails(id: number): void {
    this.citizenApi.getById(id).subscribe(data => { 
      this.citizenData.set(data); 
      this.isLoading.set(false); 
    });
    
    this.citizenApi.getDocuments(id).subscribe(docs => {
      this.documentList.set(docs);
    });
    
    this.caseApi.getByCitizenId(id).subscribe(cases => {
      this.caseList.set(cases);
    });
  }

  addDocument(): void {
    if (this.documentForm.invalid) return;
    const citizen = this.citizenData();
    if (!citizen) return;

    const payload = this.documentForm.value;
    this.citizenApi.addDocument(citizen.citizenId, payload as any).subscribe(newDoc => {
      this.documentList.update(list => [...list, newDoc]);
      this.documentForm.reset({ docType: '', fileUri: '' });
    });
  }

  verifyDocument(documentId: number, status: string): void {
    this.caseApi.verifyDocument(documentId, status).subscribe({
      next: (updatedDoc) => {
        this.documentList.update(list => 
          list.map(doc => doc.documentId === documentId ? updatedDoc : doc)
        );
      },
      error: (err) => {
        console.error('Verification failed', err);
      }
    });
  }

  getVerificationBadgeClass(status: string): string {
    switch (status) {
      case 'VERIFIED': return 'bg-success';
      case 'REJECTED': return 'bg-danger';
      default: return 'bg-warning text-dark';
    }
  }

  fileIcon(fileUri: string): string {
    const extension = (fileUri || '').split('.').pop()?.toLowerCase() || '';
    if (extension === 'pdf') return 'bi-file-earmark-pdf text-danger';
    if (['jpg', 'jpeg', 'png'].includes(extension)) return 'bi-file-earmark-image text-success';
    if (['doc', 'docx'].includes(extension)) return 'bi-file-earmark-word text-primary';
    return 'bi-file-earmark';
  }
}