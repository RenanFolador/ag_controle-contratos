import { FormControl, FormGroup } from '@angular/forms';
import { validDateRange } from './contract-form.component';

describe('ContractForm validation', () => {
  it('rejects an end date before the start date', () => {
    const form = new FormGroup({
      startDate: new FormControl('2026-10-10'),
      endDate: new FormControl('2026-10-09')
    }, { validators: validDateRange });

    expect(form.hasError('dateRange')).toBe(true);
    form.controls.endDate.setValue('2026-10-10');
    expect(form.hasError('dateRange')).toBe(false);
  });
});
