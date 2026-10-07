import { TestBed } from '@angular/core/testing';

import { CitizenApiService } from './citizen-api';

describe('CitizenApi', () => {
  let service: CitizenApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CitizenApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
