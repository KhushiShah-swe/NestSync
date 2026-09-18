export const money = (value) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value)
export const humanize = (value) =>
  String(value || '')
    .toLowerCase()
    .replaceAll('_', ' ')
export function today() {
  const date = new Date()
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
export function expenseTotal(expenses) {
  // Match the backend's cent precision instead of accumulating binary floating point values.
  return (
    expenses.reduce((cents, expense) => cents + Math.round(Number(expense.amount) * 100), 0) / 100
  )
}
