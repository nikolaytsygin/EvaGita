import client from './client'
import axios from 'axios'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  User,
} from '../types/auth'

export class LoginApiError extends Error {
  readonly status?: number
  readonly code?: string

  constructor(status?: number, code?: string) {
    super(code ?? 'LOGIN_FAILED')
    this.name = 'LoginApiError'
    this.status = status
    this.code = code
  }
}

export const login = async (
  request: LoginRequest
): Promise<LoginResponse> => {
  try {
    const response = await client.post<LoginResponse>('/auth/login', request)
    return response.data
  } catch (error) {
    if (axios.isAxiosError<{ code?: string }>(error)) {
      throw new LoginApiError(error.response?.status, error.response?.data?.code)
    }
    throw new LoginApiError()
  }
}

export const register = async (
  request: RegisterRequest
): Promise<User> => {
  const response = await client.post<User>(
    '/users',
    request
  )

  return response.data
}
