import { TestBed } from '@angular/core/testing';

import { HearingApiService } from './hearing-api';

describe('HearingApi', () => {
  let service: HearingApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HearingApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});