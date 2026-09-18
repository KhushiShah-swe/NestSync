import { expenseTotal, humanize, money, today } from './format'
describe('display helpers', () => {
  it('sums cents without decimal drift', () =>
    expect(expenseTotal([{ amount: '0.10' }, { amount: '0.20' }])).toBe(0.3))
  it('handles no expenses', () => expect(expenseTotal([])).toBe(0))
  it('formats USD and status names', () => {
    expect(money(10)).toBe('$10.00')
    expect(humanize('IN_PROGRESS')).toBe('in progress')
    expect(humanize(null)).toBe('')
  })
  it('uses the local calendar date', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date(2026, 0, 2, 23, 30))
    expect(today()).toBe('2026-01-02')
    vi.useRealTimers()
  })
})
