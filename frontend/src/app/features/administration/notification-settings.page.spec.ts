import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { NotificationSettingsPage } from './notification-settings.page';
import { NotificationDeadlineService } from './notification-deadline.service';

describe('NotificationSettingsPage', () => {
  let fixture: ComponentFixture<NotificationSettingsPage>;
  const service = {
    list: vi.fn().mockReturnValue(
      of([
        { id: 'deadline-60', daysBefore: 60, enabled: true, createdAt: '', updatedAt: '' },
        { id: 'deadline-15', daysBefore: 15, enabled: false, createdAt: '', updatedAt: '' },
      ]),
    ),
    create: vi.fn(),
    setEnabled: vi.fn(),
    remove: vi.fn(),
  };

  beforeEach(async () => {
    service.list.mockClear();
    await TestBed.configureTestingModule({
      imports: [NotificationSettingsPage],
      providers: [{ provide: NotificationDeadlineService, useValue: service }, provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(NotificationSettingsPage);
    fixture.detectChanges();
  });

  it('loads and presents configured notification periods', () => {
    const text = fixture.nativeElement.textContent;
    expect(service.list).toHaveBeenCalled();
    expect(text).toContain('Configurações de notificações');
    expect(text).toContain('60 dias');
    expect(text).toContain('15 dias');
  });

  it('updates only the enabled state and preserves the current row order', () => {
    const component = fixture.componentInstance;
    const second = component.deadlines()[1];
    service.setEnabled.mockReturnValue(
      of({ ...second, enabled: true }),
    );

    component.toggle(second, true);

    expect(component.deadlines().map((item) => item.id)).toEqual([
      'deadline-60',
      'deadline-15',
    ]);
    expect(component.deadlines()[1].enabled).toBe(true);
  });

  it('prevents the native form navigation when adding a deadline', () => {
    const component = fixture.componentInstance;
    const preventDefault = vi.fn();
    const created = {
      id: 'deadline-30',
      daysBefore: 30,
      enabled: true,
      createdAt: '',
      updatedAt: '',
    };
    service.create.mockReturnValue(of(created));
    component.daysBefore.setValue(30);

    component.add({ preventDefault } as unknown as Event);

    expect(preventDefault).toHaveBeenCalled();
    expect(component.deadlines().map((item) => item.daysBefore)).toEqual([60, 30, 15]);
  });
});
