import type { Lead, LeadDraft, LeadPage } from './leads'
import type { Product, ProductDraft, ProductPage } from './products'

export interface Admin {
  username: string
}

interface CsrfToken {
  headerName: string
  token: string
}

export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, { ...options, credentials: 'same-origin' })

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    const errors = problem?.errors?.map((error: { message: string }) => error.message).join(' ')
    const fallback = response.status === 403
      ? 'A solicitação foi recusada. Atualize a página e tente novamente.'
      : response.status >= 500
        ? 'Não foi possível acessar o backend. Confira se o Spring está rodando na porta 8080.'
        : 'Não foi possível concluir a solicitação.'
    throw new ApiError(response.status, errors || problem?.detail || fallback)
  }

  return response.status === 204 ? undefined as T : await response.json()
}

async function write<T>(path: string, method: 'POST' | 'PUT' | 'DELETE', body?: URLSearchParams | LeadDraft | ProductDraft) {
  const csrf = await request<CsrfToken>('/auth/csrf', { cache: 'no-store' })
  const headers = new Headers({ [csrf.headerName]: csrf.token })
  if (body && !(body instanceof URLSearchParams)) {
    headers.set('Content-Type', 'application/json')
  }

  return request<T>(path, {
    method,
    headers,
    body: body instanceof URLSearchParams ? body : body && JSON.stringify(body),
  })
}

export const api = {
  session: () => request<Admin>('/auth/me'),
  login: (username: string, password: string) =>
    write<void>('/auth/login', 'POST', new URLSearchParams({ username, password })),
  logout: () => write<void>('/auth/logout', 'POST'),
  leads: (query: URLSearchParams) => request<LeadPage>(`/leads?${query}`),
  saveLead: (draft: LeadDraft, id?: number) =>
    id === undefined
      ? write<Lead>('/leads', 'POST', draft)
      : write<Lead>(`/leads/${id}`, 'PUT', draft),
  products: (page: number, size: number) => {
    const query = new URLSearchParams({ page: String(page), size: String(size) })
    return request<ProductPage>(`/products?${query}`)
  },
  createProduct: (draft: ProductDraft) => write<Product>('/products', 'POST', draft),
  removeProduct: (id: number) => write<void>(`/products/${id}`, 'DELETE'),
}

export function errorMessage(error: unknown) {
  return error instanceof ApiError
    ? error.message
    : 'Não foi possível conectar à API. Verifique se o backend está em execução.'
}
