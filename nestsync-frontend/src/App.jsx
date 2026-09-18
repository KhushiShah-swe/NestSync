import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import useAuth from './auth/useAuth'
import Layout from './components/Layout'
import Auth from './pages/Auth'
import Dashboard from './pages/Dashboard'
import Expenses from './pages/Expenses'
import ExpenseForm from './pages/ExpenseForm'
import Chores from './pages/Chores'
import ChoreForm from './pages/ChoreForm'
import Balances from './pages/Balances'
import Grocery from './pages/Grocery'
import Maintenance from './pages/Maintenance'
import Profile from './pages/Profile'
function ProtectedLayout() {
  return useAuth().user ? <Layout /> : <Navigate to="/login" replace />
}
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Auth />} />
          <Route path="/register" element={<Auth register />} />
          <Route element={<ProtectedLayout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/expenses" element={<Expenses />} />
            <Route path="/add-expense" element={<ExpenseForm />} />
            <Route path="/expenses/:id/edit" element={<ExpenseForm />} />
            <Route path="/chores" element={<Chores />} />
            <Route path="/add-chore" element={<ChoreForm />} />
            <Route path="/balances" element={<Balances />} />
            <Route path="/grocery" element={<Grocery />} />
            <Route path="/maintenance" element={<Maintenance />} />
            <Route path="/profile" element={<Profile />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
