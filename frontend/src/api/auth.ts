import client from './client'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  User,
} from '../types/auth'

export const login = async (
  request: LoginRequest
): Promise<LoginResponse> => {
  const response = await client.post<LoginResponse>(
    '/auth/login',
    request
  )

  return response.data
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
