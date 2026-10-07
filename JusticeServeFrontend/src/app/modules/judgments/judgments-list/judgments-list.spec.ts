import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JudgmentsList } from './judgments-list';

describe('JudgmentsList', () => {
  let component: JudgmentsList;
  let fixture: ComponentFixture<JudgmentsList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JudgmentsList],
    }).compileComponents();

    fixture = TestBed.createComponent(JudgmentsList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
