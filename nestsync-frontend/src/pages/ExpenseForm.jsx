import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import API, { errorMessage } from '../api/axiosConfig'
import useResource from '../hooks/useResource'
import { PageHeading, Field, ErrorNotice, ResourceState } from '../components/Ui'
import { today } from '../utils/format'
function Form({ initial, id }) {
  const navigate = useNavigate()
  const [busy, setBusy] = useState(false),
    [error, setError] = useState('')
  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    const values = { ...Object.fromEntries(new FormData(event.currentTarget)), splitType: 'EQUAL' }
    try {
      if (id) await API.put(`/expenses/${id}`, values)
      else await API.post('/expenses', values)
      navigate('/expenses')
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(false)
    }
  }
  return (
    <form className="panel form-panel" onSubmit={submit}>
      <ErrorNotice message={error} />
      <Field label="Expense title">
        <input
          name="title"
          defaultValue={initial?.title}
          required
          maxLength={120}
          placeholder="e.g. Weekly groceries"
        />
      </Field>
      <div className="form-row">
        <Field label="Amount (USD)">
          <input
            name="amount"
            type="number"
            min="0.01"
            max="9999999999.99"
            step="0.01"
            defaultValue={initial?.amount}
            required
          />
        </Field>
        <Field label="Date">
          <input name="date" type="date" required defaultValue={initial?.date || today()} />
        </Field>
      </div>
      <Field label="Category">
        <select name="category" defaultValue={initial?.category || 'Groceries'}>
          {['Groceries', 'Rent', 'Utilities', 'Household', 'Other'].map((category) => (
            <option key={category}>{category}</option>
          ))}
        </select>
      </Field>
      <Field label="Notes (optional)">
        <textarea name="notes" maxLength={1000} defaultValue={initial?.notes} />
      </Field>
      <p className="notice">
        {id
          ? 'Changes keep the original roommates and payer.'
          : 'You will be recorded as the payer. This expense is split equally among the current members of your home.'}
      </p>
      <div className="form-actions">
        <button className="button" disabled={busy}>
          {busy ? 'Saving…' : id ? 'Save changes' : 'Save expense'}
        </button>
        <Link to="/expenses">Cancel</Link>
      </div>
    </form>
  )
}
function EditForm({ id }) {
  const resource = useResource(`/expenses/${id}`)
  return (
    <>
      <ResourceState resource={resource} />
      {resource.data && <Form id={id} initial={resource.data} />}
    </>
  )
}
export default function ExpenseForm() {
  const { id } = useParams()
  return (
    <>
      <PageHeading
        title={id ? 'Edit expense' : 'Add an expense'}
        description="A shared bill, without the back-and-forth."
      />
      {id ? <EditForm id={id} /> : <Form />}
    </>
  )
}
