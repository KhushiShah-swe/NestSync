import { Link } from 'react-router-dom'
import useAuth from '../auth/useAuth'
import useResource from '../hooks/useResource'
import { PageHeading, ErrorNotice } from '../components/Ui'
import { money, expenseTotal, humanize } from '../utils/format'
export default function Dashboard() {
  const { user } = useAuth()
  const expenses = useResource('/expenses'),
    chores = useResource('/chores'),
    balances = useResource('/balances')
  const loading = expenses.loading || chores.loading || balances.loading
  const error = expenses.error || chores.error || balances.error
  const records = expenses.data || [],
    tasks = chores.data || []
  const mine = (balances.data || []).find((balance) => balance.userId === user.userId)
  return (
    <>
      <PageHeading
        eyebrow="HOME / OVERVIEW"
        title={`A little more in sync, ${user.name.split(' ')[0]}.`}
        description="A clear view of the little things that make a home."
        action={
          <Link className="button" to="/add-expense">
            + Add expense
          </Link>
        }
      />
      <div className="welcome-banner">
        <div>
          <p className="eyebrow">ROOM TOGETHER. LIVE BETTER.</p>
          <h2>
            Less admin.
            <br />
            More feeling at home.
          </h2>
          <p>Everyone on the same page, one small task at a time.</p>
        </div>
        <div className="house-art" aria-hidden="true">
          <div className="house-roof" />
          <div className="house-body">
            <span />
            <span />
            <i />
          </div>
          <div className="house-ground" />
        </div>
      </div>
      <ErrorNotice message={error} />
      {loading ? (
        <p role="status">Loading your overview…</p>
      ) : (
        !error && (
          <>
            <div className="stats-grid">
              <article className="stat-card">
                <span className="stat-icon">↗</span>
                <p>Total household expenses</p>
                <strong>{money(expenseTotal(records))}</strong>
                <small>{records.length} recorded expenses · all time</small>
              </article>
              <article className="stat-card">
                <span className="stat-icon lilac">✓</span>
                <p>Chores to do</p>
                <strong>{tasks.filter((chore) => chore.status !== 'DONE').length}</strong>
                <small>A little teamwork goes a long way</small>
              </article>
              <article className="stat-card">
                <span className="stat-icon peach">⇄</span>
                <p>Your net balance</p>
                <strong>{money(mine?.netAmount || 0)}</strong>
                <small>
                  {Number(mine?.netAmount || 0) > 0
                    ? 'You are owed this amount'
                    : Number(mine?.netAmount || 0) < 0
                      ? 'You owe this amount'
                      : 'You’re all balanced'}
                </small>
              </article>
            </div>
            <div className="dashboard-columns">
              <section className="panel">
                <div className="section-heading">
                  <h2>Recent expenses</h2>
                  <Link to="/expenses">View all ↗</Link>
                </div>
                {records.length ? (
                  records.slice(0, 4).map((expense) => (
                    <div className="list-row" key={expense.expenseId}>
                      <span className="row-symbol">↗</span>
                      <div>
                        <strong>{expense.title}</strong>
                        <p>
                          {expense.category} · {expense.paidBy.name}
                        </p>
                      </div>
                      <b>{money(expense.amount)}</b>
                    </div>
                  ))
                ) : (
                  <p className="empty-state">
                    Your first shared expense starts here.
                    <br />
                    <Link to="/add-expense">Add an expense</Link>
                  </p>
                )}
              </section>
              <section className="panel">
                <div className="section-heading">
                  <h2>On the to-do list</h2>
                  <Link to="/chores">View all ↗</Link>
                </div>
                {tasks
                  .filter((chore) => chore.status !== 'DONE')
                  .slice(0, 4)
                  .map((chore) => (
                    <div className="list-row" key={chore.choreId}>
                      <span className="row-symbol lilac">✓</span>
                      <div>
                        <strong>{chore.choreName}</strong>
                        <p>
                          {chore.assignee?.name || 'Unassigned'} · {chore.dueDate}
                        </p>
                      </div>
                      <span className="badge">{humanize(chore.status)}</span>
                    </div>
                  ))}
                {!tasks.some((chore) => chore.status !== 'DONE') && (
                  <p className="empty-state">Nothing on the list. Enjoy your space.</p>
                )}
              </section>
            </div>
          </>
        )
      )}
      <p className="page-footer">Your home, perfectly in sync.</p>
    </>
  )
}
