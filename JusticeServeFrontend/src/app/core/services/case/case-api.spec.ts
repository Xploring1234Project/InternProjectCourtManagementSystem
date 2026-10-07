import { TestBed } from '@angular/core/testing';

import { CaseApiService } from './case-api';

describe('CaseApi', () => {
  let service: CaseApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CaseApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
