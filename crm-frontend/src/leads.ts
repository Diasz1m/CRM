export const stages = {
  NEW: 'Novo',
  CONTACTED: 'Em contato',
  PROPOSAL_SENT: 'Proposta enviada',
  WON: 'Ganho',
  LOST: 'Perdido',
} as const

export type LeadStage = keyof typeof stages

export interface LeadDraft {
  name: string
  email: string | null
  phone: string | null
  source: string | null
  stage: LeadStage
  nextAction: string | null
  nextActionDate: string | null
}

export interface Lead extends LeadDraft {
  id: number
  createdAt: string
}

export interface LeadPage {
  content: Lead[]
  page: { number: number; size: number; totalElements: number; totalPages: number }
}
