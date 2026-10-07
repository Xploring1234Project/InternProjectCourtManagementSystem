import { TestBed } from '@angular/core/testing';

import { AuditLogApiService } from './auditlog-api';

describe('AuditlogApi', () => {
  let service: AuditLogApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuditLogApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
