import {
  createContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import { login as loginRequest, register as registerRequest } from '../api/auth'
import type { AuthContextValue, User } from '../types/auth'

export const AuthContext = createContext<AuthContextValue | undefined>(
  undefined
)

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(() => {
    const storedUser = localStorage.getItem('evagita_user')

    if (!storedUser) {
      return null
    }

    try {
      return JSON.parse(storedUser) as User
    } catch {
      localStorage.removeItem('evagita_user')
      return null
    }
  })

  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem('evagita_token')
  )

  useEffect(() => {
    if (user) {
      localStorage.setItem('evagita_user', JSON.stringify(user))
    } else {
      localStorage.removeItem('evagita_user')
    }
  }, [user])

  useEffect(() => {
    if (token) {
      localStorage.setItem('evagita_token', token)
    } else {
      localStorage.removeItem('evagita_token')
    }
  }, [token])

  const login = async (email: string, password: string) => {
    const response = await loginRequest({
      email,
      password,
    })

    setUser({
      id: response.id,
      username: response.username,
      email: response.email,
    })

    setToken(response.token)
  }

  const register = async (
    username: string,
    email: string,
    password: string
  ) => {
    await registerRequest({
      username,
      email,
      password,
    })
  }

  const logout = () => {
    setUser(null)
    setToken(null)
  }

  const value: AuthContextValue = {
    user,
    token,
    isAuthenticated: Boolean(token && user),
    login,
    register,
    logout,
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
