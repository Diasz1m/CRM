<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api, ApiError, errorMessage } from '../api'
import { stages } from '../leads'
import type { Lead, LeadDraft, LeadPage, LeadStage } from '../leads'
import LeadForm from './LeadForm.vue'

const emit = defineEmits<{ sessionExpired: [] }>()
const results = ref<LeadPage>()
const search = ref('')
const stage = ref<LeadStage | ''>('')
const appliedSearch = ref('')
const appliedStage = ref<LeadStage | ''>('')
const loading = ref(false)
const loadError = ref('')
const selected = ref<Lead | null>()
const saving = ref(false)
const saveError = ref('')
const notice = ref('')

const dateFormat = new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' })

async function load(page = 0) {
  if (loading.value) return
  loading.value = true
  loadError.value = ''
  const query = new URLSearchParams({ page: String(page), size: '10' })
  if (appliedSearch.value) query.set('q', appliedSearch.value)
  if (appliedStage.value) query.set('stage', appliedStage.value)

  try {
    const data = await api.leads(query)
    if (data.page.totalPages > 0 && page >= data.page.totalPages) {
      query.set('page', String(data.page.totalPages - 1))
      results.value = await api.leads(query)
    } else {
      results.value = data
    }
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) emit('sessionExpired')
    else loadError.value = errorMessage(error)
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  appliedSearch.value = search.value
  appliedStage.value = stage.value
  void load()
}

function openForm(lead: Lead | null) {
  saveError.value = ''
  notice.value = ''
  selected.value = lead
}

async function save(draft: LeadDraft) {
  if (saving.value) return
  saving.value = true
  saveError.value = ''
  const id = selected.value?.id
  try {
    await api.saveLead(draft, id)
    selected.value = undefined
    notice.value = id === undefined ? 'Lead cadastrado.' : 'Lead atualizado.'
    await load(id === undefined ? 0 : results.value?.page.number)
  } catch (error) {
    if (error instanceof ApiError && error.status === 401) emit('sessionExpired')
    else saveError.value = errorMessage(error)
  } finally {
    saving.value = false
  }
}

onMounted(() => load())
</script>

<template>
  <header class="page-header">
    <div>
      <p class="eyebrow">Gestão comercial</p>
      <h1>Leads</h1>
      <p class="muted">Acompanhe contatos, etapas e próximas ações.</p>
    </div>
    <button class="button button-primary" type="button" :disabled="loading" @click="openForm(null)">
      <span aria-hidden="true">+</span> Novo lead
    </button>
  </header>

  <p v-if="notice" class="notice" role="status">{{ notice }}</p>

  <section class="list-card" aria-labelledby="list-title">
    <div class="list-header">
      <h2 id="list-title">Lista de leads</h2>
      <span v-if="results" class="count">{{ results.page.totalElements }} {{ results.page.totalElements === 1 ? 'lead' : 'leads' }}</span>
    </div>
    <form class="filters" @submit.prevent="applyFilters">
      <label>Buscar por nome<input v-model.trim="search" type="search" placeholder="Ex.: Ana Silva"></label>
      <label>Etapa
        <select v-model="stage">
          <option value="">Todas as etapas</option>
          <option v-for="(label, value) in stages" :key="value" :value="value">{{ label }}</option>
        </select>
      </label>
      <button class="button" :disabled="loading">Filtrar</button>
    </form>

    <div v-if="loadError" class="empty">
      <p class="alert" role="alert">{{ loadError }}</p>
      <button class="button" type="button" @click="load(results?.page.number)">Tentar novamente</button>
    </div>
    <p v-else-if="loading" class="empty" role="status">Carregando leads…</p>
    <template v-else-if="results?.content.length">
      <div class="table-wrap" tabindex="0" aria-label="Resultados dos leads">
        <table>
          <thead><tr><th scope="col">Nome</th><th scope="col">Contato</th><th scope="col">Etapa</th><th scope="col">Próxima ação</th><th scope="col"><span class="sr-only">Ações</span></th></tr></thead>
          <tbody>
            <tr v-for="lead in results.content" :key="lead.id">
              <td><strong>{{ lead.name }}</strong><small v-if="lead.source">{{ lead.source }}</small></td>
              <td>{{ lead.email || lead.phone }}<small v-if="lead.email && lead.phone">{{ lead.phone }}</small></td>
              <td><span class="stage" :data-stage="lead.stage">{{ stages[lead.stage] }}</span></td>
              <td>{{ lead.nextAction || '—' }}<small v-if="lead.nextActionDate">{{ dateFormat.format(new Date(lead.nextActionDate)) }}</small></td>
              <td><button class="button button-quiet" type="button" :aria-label="'Editar ' + lead.name" @click="openForm(lead)">Editar</button></td>
            </tr>
          </tbody>
        </table>
      </div>
      <footer v-if="results.page.totalPages > 1" class="pagination">
        <span>Página {{ results.page.number + 1 }} de {{ results.page.totalPages }}</span>
        <div>
          <button class="button" :disabled="results.page.number === 0" @click="load(results.page.number - 1)">Anterior</button>
          <button class="button" :disabled="results.page.number + 1 >= results.page.totalPages" @click="load(results.page.number + 1)">Próxima</button>
        </div>
      </footer>
    </template>
    <div v-else class="empty">
      <strong>Nenhum lead encontrado</strong>
      <p class="muted">{{ appliedSearch || appliedStage ? 'Tente outros filtros.' : 'Use “Novo lead” para cadastrar o primeiro contato.' }}</p>
    </div>
  </section>

  <LeadForm v-if="selected !== undefined" :lead="selected" :busy="saving" :error="saveError" @close="selected = undefined" @save="save" />
</template>
