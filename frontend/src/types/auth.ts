export interface User {
  id: number
  username: string
  email: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface LoginResponse extends User {
  token: string
}

export interface AuthContextValue {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<void>
  register: (
    username: string,
    email: string,
    password: string
  ) => Promise<void>
  logout: () => void
}
