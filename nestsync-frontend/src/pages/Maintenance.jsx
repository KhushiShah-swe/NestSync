import { useState } from 'react'
import API, { errorMessage } from '../api/axiosConfig'
import useResource from '../hooks/useResource'
import { PageHeading, Field, ErrorNotice, ResourceState } from '../components/Ui'
import { humanize } from '../utils/format'
export default function Maintenance() {
  const resource = useResource('/maintenance')
  const [error, setError] = useState(''),
    [busy, setBusy] = useState(false)
  async function act(operation) {
    setBusy(true)
    setError('')
    try {
      await operation()
      resource.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(false)
    }
  }
  async function add(event) {
    event.preventDefault()
    const form = event.currentTarget,
      values = Object.fromEntries(new FormData(form))
    await act(async () => {
      await API.post('/maintenance', { ...values, status: 'OPEN' })
      form.reset()
    })
  }
  return (
    <>
      <PageHeading
        title="Keep your home happy"
        description="Track what needs a fix, from first report to resolved."
      />
      <ErrorNotice message={error} />
      <form className="panel form-panel" onSubmit={add}>
        <Field label="Issue title">
          <input name="title" required maxLength={120} />
        </Field>
        <Field label="Description">
          <textarea name="description" required maxLength={2000} />
        </Field>
        <button className="button" disabled={busy}>
          Report issue
        </button>
      </form>
      <ResourceState resource={resource} empty={!resource.data?.length}>
        No maintenance issues. Everything has its place.
      </ResourceState>
      <div className="cards-grid">
        {resource.data?.map((issue) => (
          <article className="panel" key={issue.issueId}>
            <span className="badge">{humanize(issue.status)}</span>
            <h2>{issue.title}</h2>
            <p>{issue.description}</p>
            <div className="card-actions">
              <button
                className="button secondary"
                disabled={busy}
                onClick={() =>
                  act(() =>
                    API.put(`/maintenance/${issue.issueId}`, {
                      title: issue.title,
                      description: issue.description,
                      status: issue.status === 'RESOLVED' ? 'OPEN' : 'RESOLVED',
                    }),
                  )
                }
              >
                {issue.status === 'RESOLVED' ? 'Reopen' : 'Mark resolved'}
              </button>
              <button
                className="text-button danger"
                disabled={busy}
                onClick={() => act(() => API.delete(`/maintenance/${issue.issueId}`))}
              >
                Delete {issue.title}
              </button>
            </div>
          </article>
        ))}
      </div>
    </>
  )
}
