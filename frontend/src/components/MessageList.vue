<template>
  <div class="messages-wrap" ref="scrollEl">
    <div v-if="rooms.loading" class="loading-state">
      <div class="msg-skeleton" v-for="i in 5" :key="i" :style="{ width: (50 + i * 7) + '%', alignSelf: i % 2 ? 'flex-end' : 'flex-start' }"></div>
    </div>

    <div v-else class="messages-inner">
      <div v-if="grouped.length === 0" class="no-messages">
        <span class="nm-icon">⊞</span>
        <span>Сообщений пока нет. Просто напиши привет!</span>
      </div>

      <template v-for="group in grouped" :key="group.date">
        <div class="date-divider">
          <span>{{ group.date }}</span>
        </div>

        <div
            v-for="(msg, idx) in group.messages"
            :key="msg.id"
            :class="['msg-row', { 'mine': msg.senderId === auth.userId, 'consecutive': isConsecutive(group.messages, idx) }]"
        >
          <div v-if="!isConsecutive(group.messages, idx) && msg.senderId !== auth.userId" class="msg-avatar">
            {{ (msg.senderName || msg.senderId).slice(0,2).toUpperCase() }}
          </div>
          <div v-else-if="msg.senderId !== auth.userId" class="msg-avatar-spacer"></div>

          <div class="msg-bubble-wrap">
            <div v-if="!isConsecutive(group.messages, idx)" class="msg-meta">
              <span class="msg-sender">{{ msg.senderId === auth.userId ? 'you' : msg.senderName }}</span>
              <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
            </div>
            <div class="msg-bubble">{{ msg.content }}</div>
          </div>
        </div>
      </template>

      <!-- Typing indicator -->
      <div v-if="typingUsers.size > 0" class="typing-row">
        <div class="typing-bubble">
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
          <span class="typing-dot"></span>
        </div>
        <span class="typing-label">{{ typingLabel }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRoomsStore } from '@/stores/rooms'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({ typingUsers: Set })
const rooms = useRoomsStore()
const auth  = useAuthStore()
const scrollEl = ref(null)

function formatTime(iso) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}
function formatDate(iso) {
  const d = new Date(iso)
  const today = new Date()
  if (d.toDateString() === today.toDateString()) return 'Today'
  const yesterday = new Date(today); yesterday.setDate(today.getDate()-1)
  if (d.toDateString() === yesterday.toDateString()) return 'Yesterday'
  return d.toLocaleDateString([], { weekday: 'long', month: 'short', day: 'numeric' })
}

const grouped = computed(() => {
  const groups = []
  let curDate = null, curGroup = null
  for (const msg of rooms.messages) {
    const d = formatDate(msg.createdAt)
    if (d !== curDate) {
      curDate = d
      curGroup = { date: d, messages: [] }
      groups.push(curGroup)
    }
    curGroup.messages.push(msg)
  }
  return groups
})

function isConsecutive(msgs, idx) {
  if (idx === 0) return false
  const prev = msgs[idx - 1], cur = msgs[idx]
  return prev.senderId === cur.senderId &&
      new Date(cur.createdAt) - new Date(prev.createdAt) < 120_000
}

const typingLabel = computed(() => {
  const users = [...(props.typingUsers || [])]
  if (users.length === 1) return `${users[0]} is typing...`
  if (users.length === 2) return `${users[0]} and ${users[1]} are typing...`
  return 'Several people are typing...'
})

// Auto-scroll to bottom on new messages
watch(() => rooms.messages.length, () => {
  nextTick(() => {
    if (scrollEl.value) scrollEl.value.scrollTop = scrollEl.value.scrollHeight
  })
})
watch(() => props.typingUsers?.size, () => {
  nextTick(() => { if (scrollEl.value) scrollEl.value.scrollTop = scrollEl.value.scrollHeight })
})
</script>

<style scoped>
.messages-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
}
.loading-state, .messages-inner {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-height: 100%;
  justify-content: flex-end;
}
.msg-skeleton {
  height: 44px;
  border-radius: var(--radius-lg);
  background: linear-gradient(90deg, var(--bg-elevated) 25%, var(--bg-hover) 50%, var(--bg-elevated) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
  margin-bottom: 8px;
}
.no-messages {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin: auto;
  color: var(--text-muted);
  font-size: 14px;
}
.nm-icon { font-size: 32px; color: var(--text-ghost); }

.date-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0 8px;
  color: var(--text-muted);
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 1px;
}
.date-divider::before, .date-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  animation: fade-up 0.2s ease;
  margin-bottom: 4px;
}
.msg-row.mine { flex-direction: row-reverse; }
.msg-row.consecutive { margin-bottom: 1px; }

.msg-avatar {
  width: 32px; height: 32px; flex-shrink: 0;
  border-radius: 9px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  color: var(--text-secondary);
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}
.msg-avatar-spacer { width: 32px; flex-shrink: 0; }

.msg-bubble-wrap { max-width: 68%; display: flex; flex-direction: column; }
.msg-row.mine .msg-bubble-wrap { align-items: flex-end; }

.msg-meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 3px;
  padding: 0 4px;
}
.msg-sender {
  font-family: var(--font-mono);
  font-size: 11px;
  font-weight: 600;
  color: var(--accent);
}
.msg-row.mine .msg-sender { color: var(--info); }
.msg-time {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--text-ghost);
}

.msg-bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  background: var(--bg-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border);
}
.msg-row.mine .msg-bubble {
  background: #0d2a1e;
  border-color: var(--border-accent);
  color: #c8f5e0;
}
.msg-row.consecutive .msg-bubble {
  border-radius: 14px;
}

/* Typing indicator */
.typing-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
  animation: fade-up 0.2s ease;
}
.typing-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 14px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 14px;
}
.typing-dot {
  width: 6px; height: 6px;
  background: var(--text-muted);
  border-radius: 50%;
  animation: typing-bounce 1.2s ease-in-out infinite;
}
.typing-dot:nth-child(2) { animation-delay: 0.15s; }
.typing-dot:nth-child(3) { animation-delay: 0.3s; }
.typing-label {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}
</style>