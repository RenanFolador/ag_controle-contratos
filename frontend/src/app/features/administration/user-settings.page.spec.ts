import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { UserSettingsPage } from './user-settings.page';
import { UserAdminService } from './user.service';

describe('UserSettingsPage', () => {
  let fixture: ComponentFixture<UserSettingsPage>;
  const service = {
    list: vi.fn().mockReturnValue(
      of({
        content: [
          {
            id: 'user-1',
            username: 'maria',
            firstName: 'Maria',
            lastName: 'Silva',
            email: 'maria@example.com',
            enabled: true,
            roles: ['VIEWER'],
          },
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
        first: true,
        last: true,
      }),
    ),
    updateRoles: vi.fn().mockReturnValue(
      of({
        id: 'user-1',
        username: 'maria',
        firstName: 'Maria',
        lastName: 'Silva',
        email: 'maria@example.com',
        enabled: true,
        roles: ['ADMIN'],
      }),
    ),
  };

  beforeEach(async () => {
    service.list.mockClear();
    service.updateRoles.mockClear();
    await TestBed.configureTestingModule({
      imports: [UserSettingsPage],
      providers: [{ provide: UserAdminService, useValue: service }, provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(UserSettingsPage);
    fixture.detectChanges();
  });

  it('renders users and available role options', () => {
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Maria Silva');
    expect(text).toContain('Usuários e permissões');
    expect(fixture.componentInstance.roleOptions).toHaveLength(4);
  });

  it('saves the selected roles', () => {
    vi.stubGlobal('confirm', vi.fn(() => true));
    const user = fixture.componentInstance.users()[0];
    fixture.componentInstance.rolesControl(user).setValue(['ADMIN']);

    fixture.componentInstance.save(user);

    expect(service.updateRoles).toHaveBeenCalledWith('user-1', ['ADMIN']);
    vi.unstubAllGlobals();
  });
});
