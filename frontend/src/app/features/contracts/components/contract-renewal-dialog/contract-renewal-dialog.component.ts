import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Contract, ContractRenewalPayload } from '../../models/contract';

@Component({ selector: 'app-contract-renewal-dialog', imports: [DatePipe, ReactiveFormsModule,
  MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule],
  templateUrl: './contract-renewal-dialog.component.html', styleUrl: './contract-renewal-dialog.component.scss' })
export class ContractRenewalDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ContractRenewalDialogComponent>);
  protected readonly contract = inject<Contract>(MAT_DIALOG_DATA);
  protected readonly form = this.fb.nonNullable.group({
    newEndDate: ['', [Validators.required, (control: AbstractControl<string>) => control.value && control.value <= this.contract.endDate ? { notAfterCurrent: true } : null]],
    reference: ['', [Validators.required, Validators.maxLength(255)]],
    reason: ['', [Validators.required, Validators.maxLength(500)]],
    notes: ['', Validators.maxLength(2000)],
  });
  cancel(): void { this.dialogRef.close(); }
  renew(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    this.dialogRef.close({ ...value, notes: value.notes.trim() || null } satisfies ContractRenewalPayload);
  }
}
