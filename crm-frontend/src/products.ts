export interface ProductDraft {
  name: string
  description: string | null
  price: number
}

export interface Product extends ProductDraft {
  id: number
  createdAt: string
}

export interface ProductPage {
  content: Product[]
  page: { number: number; size: number; totalElements: number; totalPages: number }
}
