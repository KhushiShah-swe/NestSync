import { useState } from 'react'
import { Link } from 'react-router-dom'
import API, { errorMessage } from '../api/axiosConfig'
import useResource from '../hooks/useResource'
import { PageHeading, ResourceState, ErrorNotice } from '../components/Ui'
import { money } from '../utils/format'
export default function Expenses() {
  const resource = useResource('/expenses')
  const [error, setError] = useState(''),
    [busy, setBusy] = useState(null)
  async function remove(id) {
    setBusy(id)
    setError('')
    try {
      await API.delete(`/expenses/${id}`)
      resource.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(null)
    }
  }
  return (
    <>
      <PageHeading
        title="Shared expenses"
        description="Every bill in one place. Equal shares, down to the cent."
        action={
          <Link className="button" to="/add-expense">
            + Add expense
          </Link>
        }
      />
      <ErrorNotice message={error} />
      <ResourceState resource={resource} empty={!resource.data?.length}>
        No expenses yet. Add your first shared bill.
      </ResourceState>
      <div className="cards-grid">
        {resource.data?.map((expense) => (
          <article className="panel" key={expense.expenseId}>
            <span className="badge">{expense.category}</span>
            <h2>{expense.title}</h2>
            <p className="amount">{money(expense.amount)}</p>
            <p className="muted">
              Paid by {expense.paidBy.name} · {expense.date}
            </p>
            <p className="muted">
              Split equally between {Object.keys(expense.shares).length} roommates.
            </p>
            {expense.notes && <p>{expense.notes}</p>}
            <div className="card-actions">
              <Link to={`/expenses/${expense.expenseId}/edit`}>Edit expense</Link>
              <button
                className="text-button danger"
                disabled={busy === expense.expenseId}
                onClick={() => remove(expense.expenseId)}
              >
                Delete {expense.title}
              </button>
            </div>
          </article>
        ))}
      </div>
    </>
  )
}
