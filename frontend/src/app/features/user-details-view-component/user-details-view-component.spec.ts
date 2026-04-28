import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserDetailsViewComponent } from './user-details-view-component';

describe('UserDetailsViewComponent', () => {
  let component: UserDetailsViewComponent;
  let fixture: ComponentFixture<UserDetailsViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserDetailsViewComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(UserDetailsViewComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
