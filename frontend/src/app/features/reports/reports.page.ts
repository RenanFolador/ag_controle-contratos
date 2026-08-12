import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Person } from '../persons/models/person';
import { PersonService } from '../persons/services/person.service';
import { ReportService, ReportType } from './report.service';

@Component({
  selector: 'app-reports-page',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './reports.page.html',
  styleUrl: './reports.page.scss',
})
export class ReportsPage {
  private readonly reports = inject(ReportService);
  private readonly personsService = inject(PersonService);
  private readonly destroyRef = inject(DestroyRef);
  readonly exporting = signal(false);
  readonly persons = signal<Person[]>([]);
  readonly types: { value: ReportType; label: string }[] = [
    { value: 'ACTIVE_CONTRACTS', label: 'Contratos ativos' },
    { value: 'EXPIRED_CONTRACTS', label: 'Contratos vencidos' },
    { value: 'EXPIRING_CONTRACTS', label: 'Próximos do vencimento' },
    { value: 'CONTRACTS_BY_RESPONSIBLE', label: 'Contratos por responsável' },
    { value: 'SENT_NOTIFICATIONS', label: 'Notificações enviadas' },
    { value: 'FAILED_NOTIFICATIONS', label: 'Notificações com falha' },
  ];
  readonly statuses = ['ACTIVE', 'CLOSED', 'CANCELLED', 'SUSPENDED'];
  readonly form = new FormGroup({
    type: new FormControl<ReportType>('ACTIVE_CONTRACTS', {
      nonNullable: true,
      validators: Validators.required,
    }),
    format: new FormControl<'CSV'>('CSV', { nonNullable: true }),
    year: new FormControl<number | null>(null),
    status: new FormControl<string | null>(null),
    startDate: new FormControl<string | null>(null),
    endDate: new FormControl<string | null>(null),
    personId: new FormControl<string | null>(null),
    company: new FormControl<string | null>(null),
  });

  constructor() {
    this.personsService
      .list()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((persons) => this.persons.set(persons));
  }

  export(): void {
    if (this.exporting() || this.form.invalid) return;
    const value = this.form.getRawValue();
    if (value.startDate && value.endDate && value.endDate < value.startDate) {
      this.form.controls.endDate.setErrors({ period: true });
      return;
    }
    this.exporting.set(true);
    this.reports
      .export(value)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.reports.download(response);
          this.exporting.set(false);
        },
        error: () => this.exporting.set(false),
      });
  }
}
