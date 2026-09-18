import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App'
import API from './api/axiosConfig'
vi.mock('./api/axiosConfig', async (original) => ({
  ...(await original()),
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}))
const member = { userId: 1, name: 'Alex', email: 'alex@example.com' }
const expense = {
  expenseId: 1,
  title: 'Internet',
  amount: 60,
  category: 'Utilities',
  date: '2026-09-18',
  splitType: 'EQUAL',
  paidBy: member,
  shares: { 1: 30, 2: 30 },
  notes: 'Monthly bill',
}
const chore = {
  choreId: 1,
  choreName: 'Clean kitchen',
  dueDate: '2026-09-20',
  recurrence: 'WEEKLY',
  status: 'TODO',
  assignee: member,
}
const grocery = { groceryId: 1, itemName: 'Oat milk', quantity: 2, purchased: false }
const issue = {
  issueId: 1,
  title: 'Leaky tap',
  description: 'Kitchen faucet drips.',
  status: 'OPEN',
}
let data
beforeEach(() => {
  vi.clearAllMocks()
  sessionStorage.clear()
  window.history.replaceState({}, '', '/')
  data = {
    '/expenses': [expense],
    '/expenses/1': expense,
    '/chores': [chore],
    '/balances': [
      { userId: 1, name: 'Alex', netAmount: 30 },
      { userId: 2, name: 'Sam', netAmount: -30 },
    ],
    '/groceries': [grocery],
    '/maintenance': [issue],
    '/groups/members': [member],
    '/groups/me': { groupId: 1, groupName: 'Alex’s home', inviteCode: 'example-invite-code' },
  }
  API.get.mockImplementation(async (path) => ({ data: structuredClone(data[path]) }))
  API.post.mockResolvedValue({ data: { token: 'signed-token', user: member } })
  API.put.mockResolvedValue({ data: {} })
  API.delete.mockResolvedValue({})
})
function open(path = '/', authenticated = true) {
  window.history.replaceState({}, '', path)
  if (authenticated) {
    sessionStorage.setItem('nestsync.token', 'signed-token')
    sessionStorage.setItem('nestsync.user', JSON.stringify(member))
  }
  render(<App />)
  return userEvent.setup()
}
it('protects private routes', async () => {
  open('/expenses', false)
  expect(await screen.findByRole('heading', { name: 'Welcome home.' })).toBeVisible()
})
it('logs in and stores the session', async () => {
  const user = open('/login', false)
  await user.type(screen.getByLabelText('Email address'), member.email)
  await user.type(screen.getByLabelText('Password'), 'password123')
  await user.click(screen.getByRole('button', { name: 'Sign in' }))
  expect(await screen.findByRole('heading', { name: /A little more in sync/ })).toBeVisible()
  expect(sessionStorage.getItem('nestsync.token')).toBe('signed-token')
})
it('shows login errors without entering the private app', async () => {
  API.post.mockRejectedValueOnce({
    response: { data: { detail: 'Email or password is incorrect.' } },
  })
  const user = open('/login', false)
  await user.type(screen.getByLabelText('Email address'), member.email)
  await user.type(screen.getByLabelText('Password'), 'wrong')
  await user.click(screen.getByRole('button', { name: 'Sign in' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('Email or password is incorrect.')
  expect(sessionStorage.getItem('nestsync.token')).toBeNull()
})
it('registers with all required fields', async () => {
  const user = open('/register', false)
  await user.type(screen.getByLabelText('Your name'), 'Alex')
  await user.type(screen.getByLabelText('Email address'), member.email)
  await user.type(screen.getByLabelText('Password'), 'password123')
  await user.click(screen.getByRole('button', { name: 'Create account' }))
  await waitFor(() =>
    expect(API.post).toHaveBeenCalledWith('/auth/register', {
      name: 'Alex',
      email: member.email,
      password: 'password123',
    }),
  )
})
it('redirects signed-in users away from login', async () => {
  open('/login')
  expect(await screen.findByRole('heading', { name: /A little more in sync/ })).toBeVisible()
})
it('handles corrupt browser state', async () => {
  sessionStorage.setItem('nestsync.token', 'token')
  sessionStorage.setItem('nestsync.user', 'broken')
  open('/login', false)
  expect(await screen.findByRole('heading', { name: 'Welcome home.' })).toBeVisible()
})
it('shows real dashboard totals', async () => {
  open()
  expect(await screen.findByText('$60.00', { selector: 'strong' })).toBeVisible()
  expect(screen.getByText('1 recorded expenses · all time')).toBeVisible()
  expect(screen.getByText('You are owed this amount')).toBeVisible()
})
it('shows empty dashboard states', async () => {
  data['/expenses'] = []
  data['/chores'] = []
  data['/balances'] = []
  open()
  expect(await screen.findByText('You’re all balanced')).toBeVisible()
  expect(screen.getByText('Nothing on the list. Enjoy your space.')).toBeVisible()
})
it('reports dashboard API failures', async () => {
  API.get.mockRejectedValue(new Error('Offline'))
  open()
  expect(await screen.findByRole('alert')).toHaveTextContent('Unable to connect')
})
it('signs out and removes session data', async () => {
  const user = open()
  await user.click(screen.getByRole('button', { name: 'Sign out' }))
  expect(await screen.findByRole('heading', { name: 'Welcome home.' })).toBeVisible()
  expect(sessionStorage.getItem('nestsync.token')).toBeNull()
})
it('lists expenses and deletes a selected record', async () => {
  const user = open('/expenses')
  expect(await screen.findByRole('heading', { name: 'Internet' })).toBeVisible()
  await user.click(screen.getByRole('button', { name: 'Delete Internet' }))
  await waitFor(() => expect(API.delete).toHaveBeenCalledWith('/expenses/1'))
})
it('surfaces failed expense deletions', async () => {
  API.delete.mockRejectedValue(new Error('Offline'))
  const user = open('/expenses')
  await user.click(await screen.findByRole('button', { name: 'Delete Internet' }))
  expect(await screen.findByRole('alert')).toBeVisible()
})
it('adds an expense with exact decimal input', async () => {
  const user = open('/add-expense')
  await user.type(screen.getByLabelText('Expense title'), 'Coffee')
  await user.type(screen.getByLabelText('Amount (USD)'), '25.50')
  await user.click(screen.getByRole('button', { name: 'Save expense' }))
  await waitFor(() =>
    expect(API.post).toHaveBeenCalledWith(
      '/expenses',
      expect.objectContaining({ title: 'Coffee', amount: '25.5', splitType: 'EQUAL' }),
    ),
  )
  expect(await screen.findByRole('heading', { name: 'Shared expenses' })).toBeVisible()
})
it('edits a fetched expense', async () => {
  const user = open('/expenses/1/edit')
  const amount = await screen.findByLabelText('Amount (USD)')
  await user.clear(amount)
  await user.type(amount, '90')
  await user.click(screen.getByRole('button', { name: 'Save changes' }))
  await waitFor(() =>
    expect(API.put).toHaveBeenCalledWith('/expenses/1', expect.objectContaining({ amount: '90' })),
  )
})
it('keeps a failed expense form visible for correction', async () => {
  API.post.mockRejectedValue({ response: { data: { errors: { amount: 'invalid' } } } })
  const user = open('/add-expense')
  await user.type(screen.getByLabelText('Expense title'), 'Bill')
  await user.type(screen.getByLabelText('Amount (USD)'), '10')
  await user.click(screen.getByRole('button', { name: 'Save expense' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('amount: invalid')
})
it('marks chores done and allows deletion', async () => {
  const user = open('/chores')
  await user.click(await screen.findByRole('button', { name: 'Mark done' }))
  await waitFor(() =>
    expect(API.put).toHaveBeenCalledWith(
      '/chores/1',
      expect.objectContaining({ status: 'DONE', assigneeId: 1 }),
    ),
  )
  await user.click(screen.getByRole('button', { name: 'Delete Clean kitchen' }))
  await waitFor(() => expect(API.delete).toHaveBeenCalledWith('/chores/1'))
})
it('reopens completed chores', async () => {
  data['/chores'] = [{ ...chore, status: 'DONE', assignee: null }]
  const user = open('/chores')
  await user.click(await screen.findByRole('button', { name: 'Reopen' }))
  await waitFor(() =>
    expect(API.put).toHaveBeenCalledWith(
      '/chores/1',
      expect.objectContaining({ status: 'TODO', assigneeId: null }),
    ),
  )
})
it('adds an assigned chore', async () => {
  const user = open('/add-chore')
  await user.type(screen.getByLabelText('Chore name'), 'Water plants')
  await screen.findByRole('option', { name: 'Alex' })
  await user.selectOptions(screen.getByLabelText('Assigned to'), '1')
  await user.click(screen.getByRole('button', { name: 'Save chore' }))
  await waitFor(() =>
    expect(API.post).toHaveBeenCalledWith(
      '/chores',
      expect.objectContaining({ choreName: 'Water plants', assigneeId: 1, status: 'TODO' }),
    ),
  )
})
it('explains balance direction', async () => {
  open('/balances')
  expect(await screen.findByText('To receive')).toBeVisible()
  expect(screen.getByText('To contribute')).toBeVisible()
})
it('adds, purchases, and removes groceries', async () => {
  const user = open('/grocery')
  await user.type(screen.getByLabelText('Item name'), 'Bread')
  await user.click(screen.getByRole('button', { name: 'Add item' }))
  await waitFor(() =>
    expect(API.post).toHaveBeenCalledWith('/groceries', {
      itemName: 'Bread',
      quantity: 1,
      purchased: false,
    }),
  )
  await user.click(await screen.findByRole('checkbox'))
  await waitFor(() =>
    expect(API.put).toHaveBeenCalledWith(
      '/groceries/1',
      expect.objectContaining({ purchased: true }),
    ),
  )
  await user.click(screen.getByRole('button', { name: 'Delete Oat milk' }))
  await waitFor(() => expect(API.delete).toHaveBeenCalledWith('/groceries/1'))
})
it('reports grocery errors without losing the form', async () => {
  API.post.mockRejectedValue(new Error('Offline'))
  const user = open('/grocery')
  await user.type(screen.getByLabelText('Item name'), 'Bread')
  await user.click(screen.getByRole('button', { name: 'Add item' }))
  expect(await screen.findByRole('alert')).toBeVisible()
  expect(screen.getByLabelText('Item name')).toHaveValue('Bread')
})
it('reports, resolves, and deletes maintenance issues', async () => {
  const user = open('/maintenance')
  await user.type(screen.getByLabelText('Issue title'), 'Light')
  await user.type(screen.getByLabelText('Description'), 'Bulb needs replacing')
  await user.click(screen.getByRole('button', { name: 'Report issue' }))
  await waitFor(() =>
    expect(API.post).toHaveBeenCalledWith(
      '/maintenance',
      expect.objectContaining({ title: 'Light', status: 'OPEN' }),
    ),
  )
  await user.click(await screen.findByRole('button', { name: 'Mark resolved' }))
  await waitFor(() =>
    expect(API.put).toHaveBeenCalledWith(
      '/maintenance/1',
      expect.objectContaining({ status: 'RESOLVED' }),
    ),
  )
  await user.click(screen.getByRole('button', { name: 'Delete Leaky tap' }))
  await waitFor(() => expect(API.delete).toHaveBeenCalledWith('/maintenance/1'))
})
it('shows membership and joins another home', async () => {
  const user = open('/profile')
  expect(await screen.findByText('example-invite-code')).toBeVisible()
  await user.type(screen.getByLabelText('Household invite code'), 'new-code')
  await user.click(screen.getByRole('button', { name: 'Join household' }))
  expect(await screen.findByRole('status')).toHaveTextContent('You joined your new home.')
  expect(API.post).toHaveBeenCalledWith('/groups/join', { inviteCode: 'new-code' })
})
it('explains a rejected household change', async () => {
  API.post.mockRejectedValue({ response: { data: { detail: 'Your home contains records.' } } })
  const user = open('/profile')
  await user.type(screen.getByLabelText('Household invite code'), 'new-code')
  await user.click(screen.getByRole('button', { name: 'Join household' }))
  expect(await screen.findByRole('alert')).toHaveTextContent('Your home contains records.')
})
it('lets a user retry a failed list request', async () => {
  API.get.mockRejectedValueOnce(new Error('Offline'))
  const user = open('/expenses')
  await user.click(await screen.findByRole('button', { name: 'Try again' }))
  expect(await screen.findByRole('heading', { name: 'Internet' })).toBeVisible()
})
it('redirects unknown routes safely', async () => {
  open('/missing')
  expect(await screen.findByRole('heading', { name: /A little more in sync/ })).toBeVisible()
})
