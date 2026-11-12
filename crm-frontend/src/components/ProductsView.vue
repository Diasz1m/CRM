<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { api, ApiError, errorMessage } from '../api'
import type { Product, ProductPage } from '../products'

const emit = defineEmits<{ sessionExpired: [] }>()
const results = ref<ProductPage>()
const loading = ref(false)
const loadError = ref('')
const requestedPage = ref(0)
const notice = ref('')
const dialog = ref<HTMLDialogElement>()
const action = ref<'create' | 'remove'>('create')
const selected = ref<Product>()
const busy = ref(false)
const actionError = ref('')
const form = reactive({ name: '', description: '', price: '' })
const priceFormat = new Intl.NumberFormat('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const pageSize = 10

function showError(cause: unknown, target: typeof loadError) {
  if (cause instanceof ApiError && cause.status === 401) emit('sessionExpired')
  else target.value = errorMessage(cause)
}

async function load(page = 0) {
  if (loading.value) return
  loading.value = true
  requestedPage.value = page
  loadError.value = ''
  try {
    const data = await api.products(page, pageSize)
    const lastPage = Math.max(0, data.page.totalPages - 1)
    if (page > lastPage) {
      requestedPage.value = lastPage
      results.value = await api.products(lastPage, pageSize)
    } else {
      results.value = data
    }
  } catch (cause) {
    showError(cause, loadError)
  } finally {
    loading.value = false
  }
}

async function open(product?: Product) {
  selected.value = product
  action.value = product ? 'remove' : 'create'
  Object.assign(form, { name: '', description: '', price: '' })
  actionError.value = ''
  notice.value = ''
  await nextTick()
  dialog.value?.showModal()
}

function close() {
  if (!busy.value) dialog.value?.close()
}

async function submit() {
  if (busy.value) return
  actionError.value = ''
  const product = selected.value
  const price = form.price.replace(',', '.')
  if (!product && (!form.name || !/^\d{1,10}(\.\d{1,2})?$/.test(price))) {
    actionError.value = !form.name
      ? 'Informe o nome do produto.'
      : 'Informe um preço não negativo com até dez dígitos inteiros e duas casas decimais.'
    return
  }

  busy.value = true
  try {
    if (product) {
      await api.removeProduct(product.id)
      notice.value = 'Produto removido.'
    } else {
      await api.createProduct({ name: form.name, description: form.description || null, price: Number(price) })
      notice.value = 'Produto cadastrado.'
    }
    dialog.value?.close()
    await load(product ? results.value?.page.number : 0)
  } catch (cause) {
    showError(cause, actionError)
  } finally {
    busy.value = false
  }
}

onMounted(() => load())
</script>

<template>
  <header class="page-header">
    <div>
      <p class="eyebrow">Gestão comercial</p>
      <h1>Produtos</h1>
      <p class="muted">Cadastre produtos e acompanhe os preços do seu catálogo.</p>
    </div>
    <button class="button button-primary" type="button" :disabled="loading || busy" @click="open()">
      <span aria-hidden="true">+</span> Novo produto
    </button>
  </header>

  <p v-if="notice" class="notice" role="status">{{ notice }}</p>

  <section class="list-card product-list" aria-labelledby="products-title">
    <div class="list-header">
      <h2 id="products-title">Catálogo</h2>
      <span v-if="results && !loadError" class="count">
        {{ results.page.totalElements }} {{ results.page.totalElements === 1 ? 'produto' : 'produtos' }}
      </span>
    </div>
    <div v-if="loadError" class="empty">
      <p class="alert" role="alert">{{ loadError }}</p>
      <button class="button" type="button" @click="load(requestedPage)">Tentar novamente</button>
    </div>
    <p v-else-if="loading" class="empty" role="status">Carregando produtos…</p>
    <template v-else-if="results?.content.length">
      <div class="table-wrap" tabindex="0" aria-label="Lista de produtos">
        <table>
          <thead>
            <tr><th scope="col">Produto</th><th scope="col">Preço</th><th scope="col"><span class="sr-only">Ações</span></th></tr>
          </thead>
          <tbody>
            <tr v-for="product in results.content" :key="product.id">
              <td><strong>{{ product.name }}</strong><small v-if="product.description">{{ product.description }}</small></td>
              <td class="product-price">{{ priceFormat.format(product.price) }}</td>
              <td>
                <button class="button button-quiet button-danger" type="button" :disabled="busy" :aria-label="'Remover ' + product.name" @click="open(product)">
                  Remover
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <footer v-if="results.page.totalPages > 1" class="pagination">
        <span>Página {{ results.page.number + 1 }} de {{ results.page.totalPages }}</span>
        <div>
          <button class="button" type="button" :disabled="results.page.number === 0" @click="load(results.page.number - 1)">Anterior</button>
          <button class="button" type="button" :disabled="results.page.number + 1 >= results.page.totalPages" @click="load(results.page.number + 1)">Próxima</button>
        </div>
      </footer>
    </template>
    <div v-else class="empty">
      <strong>Nenhum produto cadastrado</strong>
      <p class="muted">Use “Novo produto” para adicionar o primeiro item ao catálogo.</p>
    </div>
  </section>

  <dialog ref="dialog" class="lead-dialog" aria-labelledby="product-dialog-title" @cancel.prevent="close">
    <form @submit.prevent="submit">
      <header class="dialog-header">
        <div>
          <h2 id="product-dialog-title">{{ action === 'create' ? 'Novo produto' : 'Remover produto' }}</h2>
          <p v-if="action === 'create'" class="muted">Informe o nome e o preço do produto.</p>
          <p v-else class="muted">O produto <strong>{{ selected?.name }}</strong> será excluído do catálogo.</p>
        </div>
      </header>
      <div v-if="action === 'create'" class="field-grid">
        <label class="full-width">Nome <input v-model.trim="form.name" maxlength="150" :disabled="busy" required autofocus></label>
        <label>Preço <input v-model.trim="form.price" inputmode="decimal" maxlength="13" placeholder="0,00" :disabled="busy" required></label>
        <label class="full-width">Descrição (opcional)
          <textarea v-model.trim="form.description" maxlength="1000" rows="3" :disabled="busy"></textarea>
        </label>
      </div>
      <p v-if="actionError" class="alert" role="alert">{{ actionError }}</p>
      <footer class="dialog-footer">
        <button class="button" type="button" :disabled="busy" :autofocus="action === 'remove'" @click="close">Cancelar</button>
        <button v-if="action === 'create'" class="button button-primary" :disabled="busy">{{ busy ? 'Salvando…' : 'Cadastrar produto' }}</button>
        <button v-else class="button button-destructive" :disabled="busy">{{ busy ? 'Removendo…' : 'Remover produto' }}</button>
      </footer>
    </form>
  </dialog>
</template>
