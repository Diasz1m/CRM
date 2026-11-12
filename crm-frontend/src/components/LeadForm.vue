<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { stages } from '../leads'
import type { Lead, LeadDraft } from '../leads'

const props = defineProps<{ lead: Lead | null; busy: boolean; error: string }>()
const emit = defineEmits<{ close: []; save: [draft: LeadDraft] }>()
const dialog = ref<HTMLDialogElement>()
const contactError = ref('')
const form = reactive({
  name: props.lead?.name ?? '',
  email: props.lead?.email ?? '',
  phone: props.lead?.phone ?? '',
  source: props.lead?.source ?? '',
  stage: props.lead?.stage ?? 'NEW',
  nextAction: props.lead?.nextAction ?? '',
  nextActionDate: props.lead?.nextActionDate ?? '',
})

onMounted(() => dialog.value?.showModal())

function close() {
  if (!props.busy) emit('close')
}

function save() {
  if (props.busy) return
  contactError.value = ''
  if (!form.email && !form.phone) {
    contactError.value = 'Informe o e-mail ou o telefone.'
    return
  }
  emit('save', {
    name: form.name,
    email: form.email || null,
    phone: form.phone || null,
    source: form.source || null,
    stage: form.stage,
    nextAction: form.nextAction || null,
    nextActionDate: form.nextActionDate || null,
  })
}
</script>

<template>
  <dialog ref="dialog" class="lead-dialog" aria-labelledby="form-title" @cancel.prevent="close">
    <form @submit.prevent="save">
      <header class="dialog-header">
        <div>
          <h2 id="form-title">{{ lead ? 'Editar lead' : 'Novo lead' }}</h2>
          <p class="muted">Preencha o nome e pelo menos um contato.</p>
        </div>
        <button class="button button-quiet close-button" type="button" aria-label="Fechar" :disabled="busy" @click="close">×</button>
      </header>
      <div class="field-grid">
        <label class="full-width">Nome <input v-model.trim="form.name" maxlength="150" required autofocus></label>
        <label>E-mail <input v-model.trim="form.email" type="email" maxlength="254"></label>
        <label>Telefone <input v-model.trim="form.phone" type="tel" maxlength="40"></label>
        <label>Origem <input v-model.trim="form.source" maxlength="100" placeholder="Ex.: Indicação"></label>
        <label>Etapa
          <select v-model="form.stage">
            <option v-for="(label, value) in stages" :key="value" :value="value">{{ label }}</option>
          </select>
        </label>
        <label>Próxima ação <input v-model.trim="form.nextAction" maxlength="255" placeholder="Ex.: Ligar"></label>
        <label>Data da próxima ação <input v-model="form.nextActionDate" type="date"></label>
      </div>
      <p v-if="contactError || error" class="alert" role="alert">{{ contactError || error }}</p>
      <footer class="dialog-footer">
        <button class="button" type="button" :disabled="busy" @click="close">Cancelar</button>
        <button class="button button-primary" :disabled="busy">{{ busy ? 'Salvando…' : 'Salvar lead' }}</button>
      </footer>
    </form>
  </dialog>
</template>
