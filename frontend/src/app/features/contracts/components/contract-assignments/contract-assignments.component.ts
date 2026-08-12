import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { Person } from '../../../persons/models/person';
import { PersonService } from '../../../persons/services/person.service';
import { ContractAssignment, ContractAssignmentPayload, ContractRole } from '../../models/contract-assignment';
import { ContractAssignmentService } from '../../services/contract-assignment.service';
import { validDateRange } from '../contract-form/contract-form.component';
import { PermissionService } from '../../../../core/auth/permission.service';

@Component({ selector: 'app-contract-assignments', imports: [DatePipe, ReactiveFormsModule,
  MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule,
  MatSelectModule, MatTableModule], templateUrl: './contract-assignments.component.html',
  styleUrl: './contract-assignments.component.scss' })
export class ContractAssignmentsComponent {
  readonly contractId = input.required<string>();
  private readonly service = inject(ContractAssignmentService);
  readonly permissions = inject(PermissionService);
  private readonly personService = inject(PersonService);
  private readonly destroyRef = inject(DestroyRef);
  readonly assignments = signal<ContractAssignment[]>([]);
  readonly persons = signal<Person[]>([]);
  readonly editingId = signal<string | null>(null);
  readonly formVisible = signal(false);
  readonly displayedColumns = ['person', 'role', 'startDate', 'endDate', 'status', 'actions'];
  readonly roles: { value: ContractRole; label: string }[] = [
    { value: 'MANAGER', label: 'Gestor' },
    { value: 'PRIMARY_INSPECTOR', label: 'Fiscal titular' },
    { value: 'SUBSTITUTE_INSPECTOR', label: 'Fiscal substituto' }
  ];
  readonly form = new FormGroup({
    personId: new FormControl('', { nonNullable: true, validators: Validators.required }),
    role: new FormControl<ContractRole>('MANAGER', { nonNullable: true, validators: Validators.required }),
    startDate: new FormControl('', { nonNullable: true, validators: Validators.required }),
    endDate: new FormControl<string | null>(null)
  }, { validators: validDateRange });

  constructor() {
    this.personService.list().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(persons => this.persons.set(persons.filter(person => person.active)));
  }

  ngOnInit(): void { this.load(); }

  add(): void {
    this.editingId.set(null);
    this.form.reset({ personId: '', role: 'MANAGER', startDate: '', endDate: null });
    this.formVisible.set(true);
  }

  edit(assignment: ContractAssignment): void {
    this.editingId.set(assignment.id);
    this.form.reset({ personId: assignment.person.id, role: assignment.role,
      startDate: assignment.startDate, endDate: assignment.endDate });
    this.formVisible.set(true);
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const payload = this.form.getRawValue() as ContractAssignmentPayload;
    const id = this.editingId();
    const request = id ? this.service.update(this.contractId(), id, payload)
      : this.service.create(this.contractId(), payload);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.formVisible.set(false); this.load();
    });
  }

  end(assignment: ContractAssignment): void {
    const defaultDate = new Date().toISOString().slice(0, 10);
    const endDate = prompt('Data final do vínculo (AAAA-MM-DD):', defaultDate);
    if (!endDate || !/^\d{4}-\d{2}-\d{2}$/.test(endDate)) return;
    this.service.end(this.contractId(), assignment.id, endDate).pipe(
      takeUntilDestroyed(this.destroyRef)).subscribe(() => this.load());
  }

  roleLabel(role: ContractRole): string { return this.roles.find(item => item.value === role)?.label ?? role; }
  private load(): void { this.service.list(this.contractId()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(items => this.assignments.set(items)); }
}
