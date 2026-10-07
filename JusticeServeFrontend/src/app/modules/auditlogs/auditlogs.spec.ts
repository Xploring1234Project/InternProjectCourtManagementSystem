import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Auditlogs } from './auditlogs';

describe('Auditlogs', () => {
  let component: Auditlogs;
  let fixture: ComponentFixture<Auditlogs>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Auditlogs],
    }).compileComponents();

    fixture = TestBed.createComponent(Auditlogs);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
