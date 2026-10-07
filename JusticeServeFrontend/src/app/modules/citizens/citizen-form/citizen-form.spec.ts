import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CitizenForm } from './citizen-form';

describe('CitizenForm', () => {
  let component: CitizenForm;
  let fixture: ComponentFixture<CitizenForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CitizenForm],
    }).compileComponents();

    fixture = TestBed.createComponent(CitizenForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
