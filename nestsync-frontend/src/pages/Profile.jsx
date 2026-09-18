import { useState } from 'react'
import API, { errorMessage } from '../api/axiosConfig'
import useResource from '../hooks/useResource'
import useAuth from '../auth/useAuth'
import { PageHeading, Field, ErrorNotice, ResourceState } from '../components/Ui'
export default function Profile() {
  const { user } = useAuth(),
    home = useResource('/groups/me'),
    members = useResource('/groups/members')
  const [error, setError] = useState(''),
    [busy, setBusy] = useState(false),
    [message, setMessage] = useState('')
  async function join(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setMessage('')
    try {
      await API.post('/groups/join', Object.fromEntries(new FormData(event.currentTarget)))
      home.reload()
      members.reload()
      setMessage('You joined your new home.')
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(false)
    }
  }
  return (
    <>
      <PageHeading
        title="Your household"
        description="Good company makes a place feel like home."
      />
      <ErrorNotice message={error} />
      {message && (
        <p role="status" className="notice">
          {message}
        </p>
      )}
      <ResourceState resource={home} />
      <div className="dashboard-columns">
        <section className="panel">
          <h2>{home.data?.groupName || 'Your home'}</h2>
          <p>
            {user.name} · {user.email}
          </p>
          <p className="muted">Share this invite code privately with your roommates.</p>
          <code className="invite-code">{home.data?.inviteCode}</code>
          <h3>Roommates</h3>
          <ResourceState resource={members} />
          {members.data?.map((member) => (
            <div className="list-row" key={member.userId}>
              <span className="avatar">{member.name[0]}</span>
              <div>
                <strong>{member.name}</strong>
                <p>{member.email}</p>
              </div>
            </div>
          ))}
        </section>
        <form className="panel" onSubmit={join}>
          <h2>Joining an existing home?</h2>
          <p className="muted">
            Ask a roommate for their invite code. Join before adding records or inviting others;
            moving an active household is not supported yet.
          </p>
          <Field label="Household invite code">
            <input name="inviteCode" required maxLength={36} />
          </Field>
          <button className="button" disabled={busy}>
            {busy ? 'Joining…' : 'Join household'}
          </button>
        </form>
      </div>
    </>
  )
}
