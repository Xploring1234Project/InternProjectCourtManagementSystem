import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HearingsList } from './hearings-list';

describe('HearingsList', () => {
  let component: HearingsList;
  let fixture: ComponentFixture<HearingsList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HearingsList],
    }).compileComponents();

    fixture = TestBed.createComponent(HearingsList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
