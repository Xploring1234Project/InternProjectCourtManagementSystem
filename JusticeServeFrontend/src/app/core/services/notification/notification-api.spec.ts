import { TestBed } from '@angular/core/testing';

import { NotificationApiService } from './notification-api';

describe('NotificationApi', () => {
  let service: NotificationApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
