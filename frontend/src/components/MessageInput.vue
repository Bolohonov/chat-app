<template>
  <div class="input-area">
    <div class="input-wrap">
      <textarea
          ref="inputEl"
          v-model="text"
          class="msg-input"
          :placeholder="`сообщение ${rooms.activeRoom?.name || ''}`"
          rows="1"
          @keydown.enter.exact.prevent="send"
          @keydown="onKeydown"
          @input="autoResize"
      ></textarea>
      <button
          class="send-btn"
          :class="{ active: text.trim().length > 0 }"
          @click="send"
          :disabled="!text.trim()"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"/>
        </svg>
      </button>
    </div>
    <div class="input-hint">
      <kbd>Enter</kbd> для отправки · <kbd>Shift+Enter</kbd> новая строка
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoomsStore } from '@/stores/rooms'

const emit  = defineEmits(['send', 'typing'])
const rooms = useRoomsStore()
const text  = ref('')
const inputEl = ref(null)

let typingTimeout = null

function send() {
  const content = text.value.trim()
  if (!content) return
  emit('send', content)
  text.value = ''
  if (inputEl.value) { inputEl.value.style.height = 'auto' }
}

function onKeydown(e) {
  if (e.key === 'Enter' && e.shiftKey) return
  // Fire typing event (debounced)
  clearTimeout(typingTimeout)
  emit('typing')
  typingTimeout = setTimeout(() => {}, 2000)
}

function autoResize() {
  const el = inputEl.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}
</script>

<style scoped>
.input-area {
  flex-shrink: 0;
  padding: 12px 20px 16px;
  border-top: 1px solid var(--border);
  background: var(--bg-deep);
}
.input-wrap {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 4px 4px 4px 16px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.input-wrap:focus-within {
  border-color: var(--border-accent);
  box-shadow: 0 0 0 3px var(--accent-subtle);
}
.msg-input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  resize: none;
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.5;
  padding: 8px 0;
  max-height: 160px;
  font-family: var(--font-sans);
}
.msg-input::placeholder { color: var(--text-ghost); }

.send-btn {
  width: 36px; height: 36px;
  border-radius: var(--radius-md);
  background: var(--bg-elevated);
  color: var(--text-muted);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
  margin-bottom: 2px;
}
.send-btn.active {
  background: var(--accent);
  color: var(--bg-void);
  animation: glow-pulse 2s ease-in-out infinite;
}
.send-btn:disabled { cursor: not-allowed; }

.input-hint {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-ghost);
  text-align: center;
  margin-top: 8px;
  letter-spacing: 0.5px;
}
kbd {
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 3px;
  padding: 1px 5px;
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--text-muted);
}
</style>