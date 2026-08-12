import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ContractPayload, ContractStatus } from '../../models/contract';
import { ContractService } from '../../services/contract.service';

export function validDateRange(control: AbstractControl): ValidationErrors | null {
  const startDate = control.get('startDate')?.value;
  const endDate = control.get('endDate')?.value;
  return startDate && endDate && endDate < startDate ? { dateRange: true } : null;
}

@Component({
  selector: 'app-contract-form',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatCardModule,
    MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './contract-form.component.html',
  styleUrl: './contract-form.component.scss'
})
export class ContractFormComponent {
  private readonly service = inject(ContractService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  readonly contractId = this.route.snapshot.paramMap.get('id');
  readonly saving = signal(false);
  readonly statuses: ContractStatus[] = ['ACTIVE', 'SUSPENDED', 'CLOSED', 'CANCELLED'];
  readonly form = new FormGroup({
    contractNumber: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    processNumber: new FormControl<string | null>(null, Validators.maxLength(100)),
    object: new FormControl('', { nonNullable: true, validators: Validators.required }),
    companyName: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(255)] }),
    companyCnpj: new FormControl<string | null>(null, Validators.maxLength(18)),
    startDate: new FormControl('', { nonNullable: true, validators: Validators.required }),
    endDate: new FormControl('', { nonNullable: true, validators: Validators.required }),
    initialValue: new FormControl<number | null>(null, [Validators.required, Validators.min(0)]),
    status: new FormControl<ContractStatus>('ACTIVE', { nonNullable: true, validators: Validators.required }),
    notes: new FormControl<string | null>(null)
  }, { validators: validDateRange });

  constructor() {
    if (this.contractId) {
      this.service.getById(this.contractId).pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(contract => this.form.patchValue(contract));
    }
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    const payload = this.form.getRawValue() as ContractPayload;
    const request = this.contractId
      ? this.service.update(this.contractId, payload)
      : this.service.create(payload);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: contract => this.router.navigate(['/contracts', contract.id]),
      error: () => this.saving.set(false)
    });
  }
}
