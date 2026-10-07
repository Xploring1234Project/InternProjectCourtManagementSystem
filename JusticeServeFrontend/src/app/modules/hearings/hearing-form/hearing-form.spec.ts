import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HearingForm } from './hearing-form';

describe('HearingForm', () => {
  let component: HearingForm;
  let fixture: ComponentFixture<HearingForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HearingForm],
    }).compileComponents();

    fixture = TestBed.createComponent(HearingForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
