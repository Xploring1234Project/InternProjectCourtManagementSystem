import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CitizenDetails } from './citizen-details';

describe('CitizenDetails', () => {
  let component: CitizenDetails;
  let fixture: ComponentFixture<CitizenDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CitizenDetails],
    }).compileComponents();

    fixture = TestBed.createComponent(CitizenDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
