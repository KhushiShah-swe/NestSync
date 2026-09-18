export function PageHeading({ eyebrow = 'YOUR SHARED SPACE', title, description, action }) {
  return (
    <header className="page-heading">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        {description && <p className="muted">{description}</p>}
      </div>
      {action}
    </header>
  )
}
export function ErrorNotice({ message }) {
  return message ? (
    <p role="alert" className="notice error">
      {message}
    </p>
  ) : null
}
export function ResourceState({ resource, empty, children }) {
  if (resource.loading)
    return (
      <p role="status" className="empty-state">
        Loading your home…
      </p>
    )
  if (resource.error)
    return (
      <div>
        <ErrorNotice message={resource.error} />
        <button className="button secondary" onClick={resource.reload}>
          Try again
        </button>
      </div>
    )
  if (empty) return <div className="empty-state">{children}</div>
  return null
}
export function Field({ label, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  )
}
