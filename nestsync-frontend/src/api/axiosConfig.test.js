import API, { errorMessage } from './axiosConfig'
describe('API session boundary', () => {
  it('adds the session bearer token', async () => {
    sessionStorage.setItem('nestsync.token', 'signed-token')
    API.defaults.adapter = async (config) => ({
      data: config.headers.Authorization,
      status: 200,
      headers: {},
      config,
    })
    expect((await API.get('/expenses')).data).toBe('Bearer signed-token')
  })
  it('allows unauthenticated auth requests', async () => {
    API.defaults.adapter = async (config) => ({
      data: config.headers.Authorization,
      status: 200,
      headers: {},
      config,
    })
    expect((await API.post('/auth/login', {})).data).toBeUndefined()
  })
  it('clears stale sessions on protected 401 responses', async () => {
    sessionStorage.setItem('nestsync.token', 'expired')
    const listener = vi.fn()
    window.addEventListener('nestsync:signout', listener)
    API.defaults.adapter = async (config) => {
      throw { config, response: { status: 401 } }
    }
    await expect(API.get('/expenses')).rejects.toMatchObject({ response: { status: 401 } })
    expect(sessionStorage.getItem('nestsync.token')).toBeNull()
    expect(listener).toHaveBeenCalledOnce()
    window.removeEventListener('nestsync:signout', listener)
  })
  it('does not dispatch a logout for invalid login credentials', async () => {
    const listener = vi.fn()
    window.addEventListener('nestsync:signout', listener)
    API.defaults.adapter = async (config) => {
      throw { config, response: { status: 401 } }
    }
    await expect(API.post('/auth/login', {})).rejects.toBeDefined()
    expect(listener).not.toHaveBeenCalled()
    window.removeEventListener('nestsync:signout', listener)
  })
  it('renders field errors, API errors, and connection failures', () => {
    expect(errorMessage({ response: { data: { errors: { amount: 'must be positive' } } } })).toBe(
      'amount: must be positive',
    )
    expect(errorMessage({ response: { data: { detail: 'Not found' } } })).toBe('Not found')
    expect(errorMessage({})).toMatch('Unable to connect')
  })
})
