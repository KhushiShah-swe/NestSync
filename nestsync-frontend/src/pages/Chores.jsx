import { useState } from 'react'
import { Link } from 'react-router-dom'
import API, { errorMessage } from '../api/axiosConfig'
import useResource from '../hooks/useResource'
import { PageHeading, ResourceState, ErrorNotice } from '../components/Ui'
import { humanize } from '../utils/format'
export default function Chores() {
  const resource = useResource('/chores')
  const [error, setError] = useState(''),
    [busy, setBusy] = useState(null)
  async function update(chore, remove = false) {
    setBusy(chore.choreId)
    setError('')
    try {
      if (remove) await API.delete(`/chores/${chore.choreId}`)
      else
        await API.put(`/chores/${chore.choreId}`, {
          choreName: chore.choreName,
          dueDate: chore.dueDate,
          recurrence: chore.recurrence,
          status: chore.status === 'DONE' ? 'TODO' : 'DONE',
          assigneeId: chore.assignee?.userId || null,
        })
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
        title="A little teamwork"
        description="Give every chore a home, a person, and a due date."
        action={
          <Link className="button" to="/add-chore">
            + Add chore
          </Link>
        }
      />
      <ErrorNotice message={error} />
      <ResourceState resource={resource} empty={!resource.data?.length}>
        No chores yet. Share the next small task.
      </ResourceState>
      <div className="cards-grid">
        {resource.data?.map((chore) => (
          <article className="panel" key={chore.choreId}>
            <span className="badge">{humanize(chore.status)}</span>
            <h2>{chore.choreName}</h2>
            <p>{chore.assignee?.name || 'Unassigned'}</p>
            <p className="muted">
              Due {chore.dueDate} · {humanize(chore.recurrence)}
            </p>
            <div className="card-actions">
              <button
                className="button secondary"
                disabled={busy === chore.choreId}
                onClick={() => update(chore)}
              >
                {chore.status === 'DONE' ? 'Reopen' : 'Mark done'}
              </button>
              <button
                className="text-button danger"
                disabled={busy === chore.choreId}
                onClick={() => update(chore, true)}
              >
                Delete {chore.choreName}
              </button>
            </div>
          </article>
        ))}
      </div>
    </>
  )
}
