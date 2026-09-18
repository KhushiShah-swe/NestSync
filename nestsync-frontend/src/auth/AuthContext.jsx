import { createContext, useEffect, useState } from 'react'
import API from '../api/axiosConfig'
// The context is kept separate from useAuth so Fast Refresh can track this provider.
export const AuthContext = createContext(null)
function savedUser() {
  try {
    return sessionStorage.getItem('nestsync.token')
      ? JSON.parse(sessionStorage.getItem('nestsync.user'))
      : null
  } catch {
    return null
  }
}
export function AuthProvider({ children }) {
  const [user, setUser] = useState(savedUser)
  useEffect(() => {
    const reset = () => setUser(null)
    window.addEventListener('nestsync:signout', reset)
    return () => window.removeEventListener('nestsync:signout', reset)
  }, [])
  async function authenticate(mode, values) {
    const { data } = await API.post(`/auth/${mode}`, values)
    sessionStorage.setItem('nestsync.token', data.token)
    sessionStorage.setItem('nestsync.user', JSON.stringify(data.user))
    setUser(data.user)
  }
  function signOut() {
    sessionStorage.removeItem('nestsync.token')
    sessionStorage.removeItem('nestsync.user')
    setUser(null)
  }
  return (
    <AuthContext.Provider value={{ user, authenticate, signOut }}>{children}</AuthContext.Provider>
  )
}
