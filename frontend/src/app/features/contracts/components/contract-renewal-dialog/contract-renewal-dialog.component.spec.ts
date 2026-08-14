import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Contract } from '../../models/contract';
import { ContractRenewalDialogComponent } from './contract-renewal-dialog.component';

describe('ContractRenewalDialogComponent', () => {
  let fixture: ComponentFixture<ContractRenewalDialogComponent>;
  const dialogRef = { close: vi.fn() };
  const contract: Contract = {
    id: 'contract-id', contractNumber: '025/2026', processNumber: null,
    object: 'Objeto', companyName: 'Empresa', companyCnpj: null,
    startDate: '2026-01-01', endDate: '2026-10-10', initialValue: 100,
    status: 'ACTIVE', notes: null, createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z', createdBy: 'admin', updatedBy: 'admin',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ContractRenewalDialogComponent, NoopAnimationsModule],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: contract },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();
    dialogRef.close.mockClear();
    fixture = TestBed.createComponent(ContractRenewalDialogComponent);
    fixture.detectChanges();
  });

  it('shows the contract and current expiration', () => {
    expect(fixture.nativeElement.textContent).toContain('025/2026');
    expect(fixture.nativeElement.textContent).toContain('10/10/2026');
  });

  it('requires a date after the current expiration', () => {
    const component = fixture.componentInstance as unknown as { form: { controls: { newEndDate: { setValue(value: string): void; hasError(error: string): boolean } } } };
    component.form.controls.newEndDate.setValue('2026-10-10');
    expect(component.form.controls.newEndDate.hasError('notAfterCurrent')).toBe(true);
  });

  it('returns a complete renewal payload', () => {
    const component = fixture.componentInstance as unknown as { form: { patchValue(value: object): void }; renew(): void };
    component.form.patchValue({ newEndDate: '2027-10-10', reference: '1º Termo Aditivo',
      reason: 'Prorrogação', notes: 'Mais 12 meses' });
    component.renew();
    expect(dialogRef.close).toHaveBeenCalledWith({ newEndDate: '2027-10-10',
      reference: '1º Termo Aditivo', reason: 'Prorrogação', notes: 'Mais 12 meses' });
  });
});
