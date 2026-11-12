<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api, ApiError, errorMessage } from './api'
import type { Admin } from './api'
import LeadsView from './components/LeadsView.vue'
import ProductsView from './components/ProductsView.vue'

const admin = ref<Admin | null>(null)
const checkingSession = ref(true)
const working = ref(false)
const error = ref('')
const username = ref('admin')
const password = ref('')
const section = ref<'leads' | 'products'>('leads')

onMounted(async () => {
  try {
    admin.value = await api.session()
  } catch (cause) {
    if (!(cause instanceof ApiError && cause.status === 401)) error.value = errorMessage(cause)
  } finally {
    checkingSession.value = false
  }
})

async function login() {
  if (working.value) return
  working.value = true
  error.value = ''
  try {
    await api.login(username.value.trim(), password.value)
    admin.value = await api.session()
    password.value = ''
    section.value = 'leads'
  } catch (cause) {
    error.value = cause instanceof ApiError && cause.status === 401
      ? 'Usuário ou senha inválidos.'
      : errorMessage(cause)
  } finally {
    working.value = false
  }
}

function expireSession() {
  admin.value = null
  error.value = 'Sua sessão expirou. Entre novamente para continuar.'
}

async function logout() {
  working.value = true
  error.value = ''
  try {
    await api.logout()
    admin.value = null
  } catch (cause) {
    if (cause instanceof ApiError && cause.status === 401) expireSession()
    else error.value = errorMessage(cause)
  } finally {
    working.value = false
  }
}
</script>

<template>
  <div v-if="admin" class="workspace">
    <aside class="sidebar">
      <div class="brand"><span class="brand-mark" aria-hidden="true">C</span> CRM</div>
      <p class="eyebrow">Área comercial</p>
      <nav class="workspace-nav" aria-label="Área comercial">
        <button class="nav-item" type="button" :aria-current="section === 'leads' ? 'page' : undefined" @click="section = 'leads'">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
            <rect x="3" y="5" width="18" height="14" rx="2" />
            <circle cx="9" cy="10" r="2" />
            <path d="M6 16a3 3 0 0 1 6 0m3-6h3m-3 4h3" />
          </svg>
          Leads
        </button>
        <button class="nav-item" type="button" :aria-current="section === 'products' ? 'page' : undefined" @click="section = 'products'">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true">
            <path d="m3 7 9-4 9 4v10l-9 4-9-4V7Zm0 0 9 4 9-4m-9 4v10" />
          </svg>
          Produtos
        </button>
      </nav>
      <div class="account">
        <span class="avatar" aria-hidden="true">{{ admin.username.charAt(0).toUpperCase() }}</span>
        <span><strong>{{ admin.username }}</strong><small>Administrador</small></span>
        <button class="button button-quiet" type="button" :disabled="working" @click="logout">Sair</button>
      </div>
    </aside>
    <main class="main-content">
      <p v-if="error" class="alert" role="alert">{{ error }}</p>
      <LeadsView v-if="section === 'leads'" @session-expired="expireSession" />
      <ProductsView v-else @session-expired="expireSession" />
    </main>
  </div>

  <main v-else class="login-layout">
    <section class="login-card" aria-labelledby="login-title">
      <div class="brand"><span class="brand-mark" aria-hidden="true">C</span> CRM</div>
      <p class="eyebrow">Área comercial</p>
      <h1 id="login-title">Entre na sua conta</h1>
      <p class="muted">Acesse o CRM para gerenciar seus leads e produtos.</p>
      <p v-if="checkingSession" role="status">Verificando sessão…</p>
      <form v-else class="login-form" @submit.prevent="login">
        <label>Usuário<input v-model="username" autocomplete="username" required></label>
        <label>Senha<input v-model="password" type="password" autocomplete="current-password" required></label>
        <p v-if="error" class="alert" role="alert">{{ error }}</p>
        <button class="button button-primary" :disabled="working">{{ working ? 'Entrando…' : 'Entrar' }}</button>
      </form>
    </section>
  </main>
</template>
