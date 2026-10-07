import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JudgmentForm } from './judgment-form';

describe('JudgmentForm', () => {
  let component: JudgmentForm;
  let fixture: ComponentFixture<JudgmentForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JudgmentForm],
    }).compileComponents();

    fixture = TestBed.createComponent(JudgmentForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
