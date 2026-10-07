import { TestBed } from '@angular/core/testing';

import { JudgmentApiService } from './judgment-api';

describe('JudgmentApi', () => {
  let service: JudgmentApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(JudgmentApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
