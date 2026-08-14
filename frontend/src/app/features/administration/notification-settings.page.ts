import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { NotificationDeadline } from './notification-deadline';
import { NotificationDeadlineService } from './notification-deadline.service';

@Component({
  selector: 'app-notification-settings-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSlideToggleModule,
    MatTableModule,
    RouterLink,
  ],
  templateUrl: './notification-settings.page.html',
  styleUrl: './notification-settings.page.scss',
})
export class NotificationSettingsPage {
  private readonly service = inject(NotificationDeadlineService);
  private readonly destroyRef = inject(DestroyRef);

  readonly deadlines = signal<NotificationDeadline[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly savingId = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly daysBefore = new FormControl<number | null>(null, [
    Validators.required,
    Validators.min(1),
  ]);
  readonly enabled = new FormControl(true, { nonNullable: true });
  readonly displayedColumns = ['daysBefore', 'enabled', 'actions'];

  constructor() {
    this.load();
  }

  trackByDeadlineId(_: number, deadline: NotificationDeadline): string {
    return deadline.id;
  }

  add(event?: Event): void {
    event?.preventDefault();
    if (this.saving() || this.daysBefore.invalid || this.daysBefore.value === null) {
      this.daysBefore.markAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.saving.set(true);
    this.service
      .create(this.daysBefore.value, this.enabled.value)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (created) => {
          this.saving.set(false);
          this.daysBefore.reset();
          this.enabled.setValue(true);
          this.deadlines.update((items) =>
            [...items, created].sort((a, b) => b.daysBefore - a.daysBefore),
          );
        },
        error: () => {
          this.saving.set(false);
          this.errorMessage.set('Não foi possível adicionar o prazo. Tente novamente.');
        },
      });
  }

  toggle(deadline: NotificationDeadline, enabled: boolean): void {
    if (this.savingId() !== null) {
      return;
    }

    this.errorMessage.set(null);
    this.savingId.set(deadline.id);
    this.service
      .setEnabled(deadline.id, enabled)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (updated) => {
          this.savingId.set(null);
          this.deadlines.update((items) =>
            items.map((item) =>
              item.id === updated.id ? { ...item, enabled: updated.enabled } : item,
            ),
          );
        },
        error: () => {
          this.savingId.set(null);
          this.errorMessage.set('Não foi possível atualizar o prazo. Tente novamente.');
        },
      });
  }

  remove(deadline: NotificationDeadline): void {
    if (
      this.savingId() !== null ||
      !confirm(`Remover o prazo de ${deadline.daysBefore} dias?`)
    ) {
      return;
    }

    this.errorMessage.set(null);
    this.savingId.set(deadline.id);
    this.service
      .remove(deadline.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.savingId.set(null);
          this.deadlines.update((items) => items.filter((item) => item.id !== deadline.id));
        },
        error: () => {
          this.savingId.set(null);
          this.errorMessage.set('Não foi possível remover o prazo. Tente novamente.');
        },
      });
  }

  reload(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.service
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (items) => {
          this.deadlines.set([...items].sort((a, b) => b.daysBefore - a.daysBefore));
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.errorMessage.set('Não foi possível carregar as configurações de notificações.');
        },
      });
  }
}
