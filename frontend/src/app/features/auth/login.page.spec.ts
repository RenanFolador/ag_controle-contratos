import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { LoginPage } from './login.page';
import { AuthService } from '../../core/auth/auth.service';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  const auth = { login: vi.fn().mockResolvedValue(undefined) };

  beforeEach(async () => {
    auth.login.mockClear();
    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [
        { provide: AuthService, useValue: auth },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: () => null } } },
        },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(LoginPage);
    fixture.detectChanges();
  });

  it('renders the application access page without starting login automatically', () => {
    expect(fixture.nativeElement.textContent).toContain('Acesso ao sistema');
    expect(fixture.nativeElement.textContent).toContain('Entrar com Keycloak');
    expect(auth.login).not.toHaveBeenCalled();
  });

  it('starts Keycloak login only after clicking the button', () => {
    fixture.nativeElement.querySelector('button').click();

    expect(auth.login).toHaveBeenCalledWith(`${window.location.origin}/dashboard`);
  });
});
