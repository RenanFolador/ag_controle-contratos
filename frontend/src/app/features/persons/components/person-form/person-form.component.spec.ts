import { formatCpf, formatPhone } from '../../utils/person-format';

describe('person form masks', () => {
  it('formats CPF progressively as xxx.xxx.xxx-xx', () => {
    expect(formatCpf('12345678901')).toBe('123.456.789-01');
    expect(formatCpf('123.456.789-01')).toBe('123.456.789-01');
  });

  it('formats phone progressively as (xx) xxxx-xxxx', () => {
    expect(formatPhone('11')).toBe('(11)');
    expect(formatPhone('1198765432')).toBe('(11) 9876-5432');
    expect(formatPhone('(11) 9876-5432')).toBe('(11) 9876-5432');
  });

  it('removes non-digits and limits the fields to their mask length', () => {
    expect(formatCpf('abc123456789012')).toBe('123.456.789-01');
    expect(formatPhone('abc11987654321')).toBe('(11) 9876-5432');
  });
});
