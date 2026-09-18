import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import API, { errorMessage } from '../api/axiosConfig'
import useResource from '../hooks/useResource'
import { PageHeading, Field, ErrorNotice } from '../components/Ui'
import { today } from '../utils/format'
export default function ChoreForm() {
  const members = useResource('/groups/members'),
    navigate = useNavigate()
  const [busy, setBusy] = useState(false),
    [error, setError] = useState('')
  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    const values = Object.fromEntries(new FormData(event.currentTarget))
    values.assigneeId = values.assigneeId ? Number(values.assigneeId) : null
    values.status = 'TODO'
    try {
      await API.post('/chores', values)
      navigate('/chores')
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(false)
    }
  }
  return (
    <>
      <PageHeading
        title="Add a chore"
        description="Make the next task a little easier to remember."
      />
      <form className="panel form-panel" onSubmit={submit}>
        <ErrorNotice message={error || members.error} />
        <Field label="Chore name">
          <input name="choreName" required maxLength={120} />
        </Field>
        <Field label="Due date">
          <input name="dueDate" type="date" defaultValue={today()} required />
        </Field>
        <Field label="Assigned to">
          <select name="assigneeId">
            <option value="">Unassigned</option>
            {members.data?.map((member) => (
              <option key={member.userId} value={member.userId}>
                {member.name}
              </option>
            ))}
          </select>
        </Field>
        <Field label="Recurrence preference">
          <select name="recurrence">
            <option value="NONE">Once</option>
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly</option>
          </select>
        </Field>
        <p className="field-hint">
          Recurrence is recorded for planning. New occurrences are not created automatically yet.
        </p>
        <div className="form-actions">
          <button className="button" disabled={busy}>
            {busy ? 'Saving…' : 'Save chore'}
          </button>
          <Link to="/chores">Cancel</Link>
        </div>
      </form>
    </>
  )
}
