import { useState } from 'react'
import API, { errorMessage } from '../api/axiosConfig'
import useResource from '../hooks/useResource'
import { PageHeading, Field, ErrorNotice, ResourceState } from '../components/Ui'
export default function Grocery() {
  const resource = useResource('/groceries')
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
      await API.post('/groceries', {
        ...values,
        quantity: Number(values.quantity),
        purchased: false,
      })
      form.reset()
    })
  }
  return (
    <>
      <PageHeading
        title="The shared shopping list"
        description="For the things you need, before someone buys them twice."
      />
      <ErrorNotice message={error} />
      <form className="panel inline-form" onSubmit={add}>
        <Field label="Item name">
          <input
            name="itemName"
            required
            maxLength={120}
            placeholder="Oat milk, coffee, the essentials…"
          />
        </Field>
        <Field label="Quantity">
          <input type="number" name="quantity" min={1} max={999} defaultValue={1} required />
        </Field>
        <button className="button" disabled={busy}>
          Add item
        </button>
      </form>
      <ResourceState resource={resource} empty={!resource.data?.length}>
        Your list is clear. Add what your home needs.
      </ResourceState>
      <section className="panel">
        {resource.data?.map((item) => (
          <div className="list-row" key={item.groceryId}>
            <input
              className="checkbox"
              type="checkbox"
              aria-label={`Mark ${item.itemName} purchased`}
              checked={item.purchased}
              disabled={busy}
              onChange={() =>
                act(() =>
                  API.put(`/groceries/${item.groceryId}`, {
                    itemName: item.itemName,
                    quantity: item.quantity,
                    purchased: !item.purchased,
                  }),
                )
              }
            />
            <div className={item.purchased ? 'completed' : ''}>
              <strong>{item.itemName}</strong>
              <p>Quantity: {item.quantity}</p>
            </div>
            <button
              className="text-button danger"
              disabled={busy}
              onClick={() => act(() => API.delete(`/groceries/${item.groceryId}`))}
            >
              Delete {item.itemName}
            </button>
          </div>
        ))}
      </section>
    </>
  )
}
