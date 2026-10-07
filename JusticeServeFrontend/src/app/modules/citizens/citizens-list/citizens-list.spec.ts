import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CitizensList } from './citizens-list';

describe('CitizensList', () => {
  let component: CitizensList;
  let fixture: ComponentFixture<CitizensList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CitizensList],
    }).compileComponents();

    fixture = TestBed.createComponent(CitizensList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
