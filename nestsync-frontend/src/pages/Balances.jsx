import useResource from '../hooks/useResource'
import { PageHeading, ResourceState } from '../components/Ui'
import { money } from '../utils/format'
export default function Balances() {
  const resource = useResource('/balances')
  return (
    <>
      <PageHeading
        title="All squared up?"
        description="Net balances calculated from your household’s recorded expenses."
      />
      <p className="notice">
        Positive means this person is owed money. Negative means they owe money. Payments and
        settlements are not recorded yet.
      </p>
      <ResourceState resource={resource} />
      <div className="cards-grid">
        {resource.data?.map((member) => (
          <article className="panel" key={member.userId}>
            <span className="avatar">{member.name[0]}</span>
            <h2>{member.name}</h2>
            <p className="amount">{money(member.netAmount)}</p>
            <p className="muted">
              {Number(member.netAmount) > 0
                ? 'To receive'
                : Number(member.netAmount) < 0
                  ? 'To contribute'
                  : 'Balanced'}
            </p>
          </article>
        ))}
      </div>
    </>
  )
}
