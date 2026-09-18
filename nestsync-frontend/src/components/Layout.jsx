import { NavLink, Outlet } from 'react-router-dom'
import useAuth from '../auth/useAuth'
const links = [
  ['/', 'Overview', '◈'],
  ['/expenses', 'Expenses', '↗'],
  ['/chores', 'Chores', '✓'],
  ['/balances', 'Balances', '⇄'],
  ['/grocery', 'Groceries', '▦'],
  ['/maintenance', 'Maintenance', '⌂'],
  ['/profile', 'Your household', '◎'],
]
export default function Layout() {
  const { user, signOut } = useAuth()
  return (
    <div className="app-shell">
      <a className="skip-link" href="#main">
        Skip to content
      </a>
      <aside className="sidebar">
        <NavLink to="/" className="brand">
          <span className="brand-mark">n.</span> NestSync
        </NavLink>
        <p className="eyebrow sidebar-label">A LITTLE MORE IN SYNC</p>
        <nav aria-label="Main navigation">
          {links.map(([to, label, icon]) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              <span aria-hidden="true">{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-note">
          <span className="status-dot" /> One home. A shared rhythm.
          <p>
            Less keeping track.
            <br />
            More living together.
          </p>
        </div>
        <div className="user-panel">
          <span className="avatar">{user.name.slice(0, 1).toUpperCase()}</span>
          <div>
            <strong>{user.name}</strong>
            <button className="text-button" onClick={signOut}>
              Sign out
            </button>
          </div>
        </div>
      </aside>
      <main id="main" className="main-content">
        <Outlet />
      </main>
    </div>
  )
}
