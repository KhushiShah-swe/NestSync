import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import useAuth from '../auth/useAuth'
import { errorMessage } from '../api/axiosConfig'
import { ErrorNotice, Field } from '../components/Ui'
export default function Auth({ register = false }) {
  const { user, authenticate } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  if (user) return <Navigate to="/" replace />
  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    const values = Object.fromEntries(new FormData(event.currentTarget))
    try {
      await authenticate(register ? 'register' : 'login', values)
      navigate('/')
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setBusy(false)
    }
  }
  return (
    <div className="auth-shell">
      <section className="auth-story">
        <div className="brand">
          <span className="brand-mark">n.</span>NestSync
        </div>
        <div>
          <p className="eyebrow">SHARED LIVING, SIMPLIFIED</p>
          <h1>
            Your home,
            <br />
            perfectly
            <br />
            <em>in sync.</em>
          </h1>
          <p>
            Split the bills. Share the chores.
            <br />
            Make room for the good stuff.
          </p>
        </div>
        <span className="auth-footer">A calmer home starts with a little clarity.</span>
      </section>
      <section className="auth-form-wrap">
        <form className="auth-form" onSubmit={submit}>
          <p className="eyebrow">MAKE YOURSELF AT HOME</p>
          <h2>{register ? 'Start your shared space.' : 'Welcome home.'}</h2>
          <p className="muted">
            {register
              ? 'Create an account, then invite your roommates.'
              : 'Sign in to see what’s happening at home.'}
          </p>
          <ErrorNotice message={error} />
          {register && (
            <Field label="Your name">
              <input name="name" autoComplete="name" required maxLength={80} />
            </Field>
          )}
          <Field label="Email address">
            <input type="email" name="email" autoComplete="email" required maxLength={254} />
          </Field>
          <Field label="Password">
            <input
              type="password"
              name="password"
              autoComplete={register ? 'new-password' : 'current-password'}
              required
              minLength={register ? 8 : undefined}
              maxLength={64}
            />
          </Field>
          {register && <p className="field-hint">Use at least 8 characters.</p>}
          <button className="button full" disabled={busy}>
            {busy ? 'Please wait…' : register ? 'Create account' : 'Sign in'}
          </button>
          <p className="auth-switch">
            {register ? 'Already have an account?' : 'New to NestSync?'}{' '}
            <Link to={register ? '/login' : '/register'}>
              {register ? 'Sign in' : 'Create an account'}
            </Link>
          </p>
        </form>
      </section>
    </div>
  )
}
