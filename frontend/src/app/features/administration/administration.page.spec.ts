import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AdministrationPage } from './administration.page';

describe('AdministrationPage', () => {
  let fixture: ComponentFixture<AdministrationPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdministrationPage],
      providers: [provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(AdministrationPage);
    fixture.detectChanges();
  });

  it('presents the three administrative configuration cards', () => {
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('Configurações');
    expect(text).toContain('Usuários e permissões');
    expect(text).toContain('Notificações');
    expect(text).toContain('Sistema');
  });

  it('links users, notifications and system to their dedicated pages', () => {
    const links = Array.from(fixture.nativeElement.querySelectorAll('a')) as HTMLAnchorElement[];
    expect(links.map((link) => link.getAttribute('href'))).toEqual([
      '/administration/users',
      '/administration/notifications',
      '/administration/system',
    ]);
  });
});
