import { TestBed } from '@angular/core/testing';

import { CourtOrderApiService } from './courtorder-api';

describe('CourtorderApi', () => {
  let service: CourtOrderApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CourtOrderApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
