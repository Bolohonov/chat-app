<template>
  <header class="chat-header">
    <div class="header-left">
      <button v-if="showBack" class="back-btn" @click="emit('back')" title="Назад">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M19 12H5M12 5l-7 7 7 7"/>
        </svg>
      </button>
      <div class="room-meta">
        <span class="room-type-icon">{{ room.type === 'DIRECT' ? '◈' : '⊞' }}</span>
        <div>
          <span class="room-title">{{ room.name }}</span>
          <span v-if="room.description" class="room-desc">{{ room.description }}</span>
        </div>
      </div>
    </div>
    <div class="header-right">
      <button class="add-user-btn" @click="emit('add-user')" title="Добавить участника">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M19 8v6M22 11h-6"/>
        </svg>
        <span class="btn-label">добавить</span>
      </button>

      <button
          :class="['members-btn', { active: showMembers }]"
          @click="emit('toggle-members')"
          title="Участники"
      >
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <path d="M23 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
        </svg>
        <span class="btn-label">{{ memberCount }}</span>
      </button>

      <div :class="['ws-badge', { connected: wsConnected }]">
        <span class="ws-dot"></span>
        <span class="ws-label">{{ wsConnected ? 'онлайн' : '...' }}</span>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoomsStore } from '@/stores/rooms'

const props = defineProps({
  showBack:    Boolean,
  showMembers: Boolean,
  memberCount: { type: Number, default: 0 }
})
const emit = defineEmits(['add-user', 'back', 'toggle-members'])
const rooms = useRoomsStore()
const room  = computed(() => rooms.activeRoom)
const wsConnected = computed(() => true)
</script>

<style scoped>
.chat-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 16px; height: 60px; flex-shrink: 0;
  border-bottom: 1px solid var(--border);
  background: var(--bg-deep); gap: 8px;
}
.header-left { display: flex; align-items: center; gap: 10px; min-width: 0; flex: 1; }
.back-btn { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; flex-shrink: 0; border-radius: var(--radius-sm); color: var(--text-muted); transition: all 0.2s; }
.back-btn:hover { background: var(--bg-elevated); color: var(--accent); }
.room-meta { display: flex; align-items: center; gap: 10px; min-width: 0; }
.room-type-icon { font-size: 18px; color: var(--accent); flex-shrink: 0; }
.room-title { display: block; font-size: 14px; font-weight: 600; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.room-desc { display: block; font-size: 11px; color: var(--text-muted); margin-top: 1px; }
.header-right { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }

.add-user-btn, .members-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 10px; border-radius: var(--radius-sm);
  border: 1px solid var(--border); background: var(--bg-elevated);
  color: var(--text-muted); font-family: var(--font-mono); font-size: 11px;
  transition: all 0.2s; cursor: pointer;
}
.add-user-btn:hover, .members-btn:hover { border-color: var(--accent-dim); color: var(--accent); background: var(--accent-subtle); }
.members-btn.active { border-color: var(--accent-dim); color: var(--accent); background: var(--accent-subtle); }

.ws-badge { display: flex; align-items: center; gap: 5px; padding: 5px 8px; border-radius: 20px; border: 1px solid var(--border); background: var(--bg-elevated); flex-shrink: 0; }
.ws-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--text-muted); transition: background 0.3s; }
.ws-badge.connected .ws-dot { background: var(--accent); animation: pulse-dot 2s ease-in-out infinite; box-shadow: 0 0 6px var(--accent); }
.ws-label { font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); letter-spacing: 0.5px; }
.ws-badge.connected .ws-label { color: var(--accent); }

@media (max-width: 767px) {
  .btn-label { display: none; }
  .add-user-btn, .members-btn { padding: 8px; }
  .chat-header { padding: 0 12px; }
}
</style>