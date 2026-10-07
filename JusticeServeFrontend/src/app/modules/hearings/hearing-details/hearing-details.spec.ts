import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HearingDetails } from './hearing-details';

describe('HearingDetails', () => {
  let component: HearingDetails;
  let fixture: ComponentFixture<HearingDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HearingDetails],
    }).compileComponents();

    fixture = TestBed.createComponent(HearingDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
