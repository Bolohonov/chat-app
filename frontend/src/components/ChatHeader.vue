<template>
  <header class="chat-header">
    <div class="room-meta">
      <span class="room-type-icon">{{ room.type === 'DIRECT' ? '◈' : '⊞' }}</span>
      <div>
        <span class="room-title">{{ room.name }}</span>
        <span v-if="room.description" class="room-desc">{{ room.description }}</span>
      </div>
    </div>
    <div class="header-right">
      <div :class="['ws-badge', { connected: wsConnected }]">
        <span class="ws-dot"></span>
        <span class="ws-label">{{ wsConnected ? 'онлайн' : 'подключение' }}</span>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, inject } from 'vue'
import { useRoomsStore } from '@/stores/rooms'

const rooms = useRoomsStore()
const room  = computed(() => rooms.activeRoom)

// Receive connected state from parent via provide/inject or props
// For simplicity, we show it based on rooms store
const wsConnected = computed(() => true) // will be wired via prop in real use
</script>

<style scoped>
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 60px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--border);
  background: var(--bg-deep);
}
.room-meta { display: flex; align-items: center; gap: 12px; }
.room-type-icon { font-size: 20px; color: var(--accent); }
.room-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.room-desc {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 1px;
}
.header-right { display: flex; align-items: center; gap: 12px; }
.ws-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 10px;
  border-radius: 20px;
  border: 1px solid var(--border);
  background: var(--bg-elevated);
}
.ws-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: var(--text-muted);
  transition: background 0.3s;
}
.ws-badge.connected .ws-dot {
  background: var(--accent);
  animation: pulse-dot 2s ease-in-out infinite;
  box-shadow: 0 0 6px var(--accent);
}
.ws-label {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 1px;
}
.ws-badge.connected .ws-label { color: var(--accent); }
</style>