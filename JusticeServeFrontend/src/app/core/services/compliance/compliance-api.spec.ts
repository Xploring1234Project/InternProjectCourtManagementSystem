import { TestBed } from '@angular/core/testing';

import { ComplianceAuditService } from './compliance-api';

describe('ComplianceApi', () => {
  let service: ComplianceAuditService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ComplianceAuditService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
