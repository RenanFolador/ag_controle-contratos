import { remainingDays } from './dashboard.component';

describe('Dashboard remaining days', () => {
  it('calculates calendar days without depending on the current time', () => {
    expect(remainingDays('2026-08-22', new Date(2026, 7, 12, 23, 30))).toBe(10);
    expect(remainingDays('2026-08-11', new Date(2026, 7, 12, 1, 0))).toBe(-1);
  });
});
