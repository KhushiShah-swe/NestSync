import axios from 'axios'

const API = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL || '/api', timeout: 15000 })
API.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('nestsync.token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config?.url?.startsWith('/auth/')) {
      sessionStorage.removeItem('nestsync.token')
      sessionStorage.removeItem('nestsync.user')
      window.dispatchEvent(new Event('nestsync:signout'))
    }
    return Promise.reject(error)
  },
)
export function errorMessage(error) {
  const fields = error.response?.data?.errors
  return fields
    ? Object.entries(fields)
        .map(([name, message]) => `${name}: ${message}`)
        .join(' · ')
    : error.response?.data?.detail || 'Unable to connect. Please try again.'
}
export default API
