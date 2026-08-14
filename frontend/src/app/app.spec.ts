import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { provideRouter } from '@angular/router';
import { AuthService } from './core/auth/auth.service';

const authStub = {
  authenticated: true,
  userName: 'test-user',
  login: () => Promise.resolve(),
  logout: () => Promise.resolve(),
  hasAnyRole: () => true,
};

const viewerAuthStub = {
  authenticated: true,
  userName: 'viewer',
  login: () => Promise.resolve(),
  logout: () => Promise.resolve(),
  hasAnyRole: (...roles: string[]) => roles.includes('VIEWER'),
};

describe('App', () => {
  it('creates the application shell', async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), { provide: AuthService, useValue: authStub }],
    }).compileComponents();
    expect(TestBed.createComponent(App).componentInstance).toBeTruthy();
  });

  it('renders the main navigation', async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), { provide: AuthService, useValue: authStub }],
    }).compileComponents();
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Dashboard');
    expect(text).toContain('Novo contrato');
    expect(text).toContain('Próximos do vencimento');
    expect(text).toContain('Configurações');
  });

  it('hides Configurações from users without ADMIN', async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), { provide: AuthService, useValue: viewerAuthStub }],
    }).compileComponents();
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Configurações');
  });
});
