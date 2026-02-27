<template>
  <aside class="sidebar">
    <!-- User profile strip -->
    <div class="profile-strip">
      <div class="avatar">{{ initials }}</div>
      <div class="profile-info">
        <span class="profile-name">{{ auth.displayName }}</span>
        <span class="profile-handle">@{{ auth.username }}</span>
      </div>
      <button class="logout-btn" @click="handleLogout" title="Sign out">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/>
        </svg>
      </button>
    </div>

    <!-- Search -->
    <div class="search-wrap">
      <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
      </svg>
      <input v-model="search" class="search-input" placeholder="search rooms..." />
    </div>

    <!-- Rooms header -->
    <div class="section-header">
      <span class="section-label">rooms</span>
      <button class="new-room-btn" @click="showNewRoom = true" title="New room">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M12 5v14M5 12h14"/>
        </svg>
      </button>
    </div>

    <!-- Room list -->
    <div class="rooms-list" v-if="!loading">
      <div v-if="filteredRooms.length === 0" class="empty-rooms">
        <span>no rooms yet</span>
      </div>
      <button
          v-for="room in filteredRooms"
          :key="room.id"
          :class="['room-item', { active: rooms.activeRoom?.id === room.id }]"
          @click="selectRoom(room)"
      >
        <div class="room-icon">
          {{ room.type === 'DIRECT' ? '◈' : '⊞' }}
        </div>
        <div class="room-info">
          <span class="room-name">{{ room.name }}</span>
          <span v-if="room.lastMessage" class="room-preview">{{ room.lastMessage }}</span>
        </div>
        <div v-if="rooms.activeRoom?.id === room.id" class="active-indicator"></div>
      </button>
    </div>
    <div v-else class="loading-rooms">
      <div class="skeleton" v-for="i in 4" :key="i"></div>
    </div>

    <!-- New Room Modal -->
    <Teleport to="body">
      <div v-if="showNewRoom" class="modal-overlay" @click.self="showNewRoom = false">
        <div class="modal">
          <div class="modal-title">new_room <span class="accent">_</span></div>
          <input v-model="newRoom.name" class="modal-input" placeholder="room name" maxlength="50" />
          <input v-model="newRoom.description" class="modal-input" placeholder="description (optional)" maxlength="200" />
          <div class="modal-type-row">
            <button
                v-for="t in ['GROUP','DIRECT']"
                :key="t"
                :class="['type-btn', { active: newRoom.type === t }]"
                @click="newRoom.type = t"
            >{{ t.toLowerCase() }}</button>
          </div>
          <div class="modal-actions">
            <button class="modal-cancel" @click="showNewRoom = false">cancel</button>
            <button class="modal-create" :disabled="!newRoom.name" @click="createRoom">create_</button>
          </div>
        </div>
      </div>
    </Teleport>
  </aside>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useRoomsStore } from '@/stores/rooms'

const emit  = defineEmits(['room-selected'])
const auth  = useRouter, router = useRouter()
const authS = useAuthStore()
const rooms = useRoomsStore()

const search      = ref('')
const loading     = ref(true)
const showNewRoom = ref(false)
const newRoom     = ref({ name: '', description: '', type: 'GROUP' })

const initials = computed(() =>
    (authS.displayName || authS.username || '?').slice(0,2).toUpperCase()
)

const filteredRooms = computed(() =>
    rooms.rooms.filter(r => r.name.toLowerCase().includes(search.value.toLowerCase()))
)

onMounted(async () => {
  try { await rooms.fetchRooms() } finally { loading.value = false }
})

function selectRoom(room) {
  rooms.selectRoom(room)
  emit('room-selected', room)
}

async function createRoom() {
  if (!newRoom.value.name) return
  const room = await rooms.createRoom({
    name: newRoom.value.name,
    description: newRoom.value.description,
    type: newRoom.value.type
  })
  showNewRoom.value = false
  newRoom.value = { name: '', description: '', type: 'GROUP' }
  selectRoom(room)
}

function handleLogout() {
  authS.logout()
  router.push('/login')
}
</script>

<style scoped>
.sidebar {
  width: 280px;
  flex-shrink: 0;
  background: var(--bg-surface);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.profile-strip {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 16px 14px;
  border-bottom: 1px solid var(--border);
}
.avatar {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: var(--accent-glow);
  border: 1px solid var(--border-accent);
  color: var(--accent);
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.profile-info { flex: 1; min-width: 0; }
.profile-name {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.profile-handle {
  display: block;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
}
.logout-btn {
  color: var(--text-muted);
  padding: 6px;
  border-radius: var(--radius-sm);
  transition: color 0.2s, background 0.2s;
}
.logout-btn:hover { color: var(--danger); background: rgba(255,71,87,0.08); }

.search-wrap {
  position: relative;
  padding: 12px 14px;
  border-bottom: 1px solid var(--border);
}
.search-icon { position: absolute; left: 26px; top: 50%; transform: translateY(-50%); color: var(--text-muted); pointer-events: none; }
.search-input {
  width: 100%;
  background: var(--bg-deep);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 8px 10px 8px 32px;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
  font-family: var(--font-mono);
  transition: border-color 0.2s;
}
.search-input:focus { border-color: var(--accent-dim); }
.search-input::placeholder { color: var(--text-ghost); }

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px 8px;
}
.section-label {
  font-family: var(--font-mono);
  font-size: 10px;
  letter-spacing: 2px;
  color: var(--text-muted);
  text-transform: uppercase;
}
.new-room-btn {
  width: 22px; height: 22px;
  border-radius: 5px;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-muted);
  transition: all 0.2s;
}
.new-room-btn:hover { background: var(--accent-subtle); color: var(--accent); }

.rooms-list { flex: 1; overflow-y: auto; padding: 4px 8px 8px; }
.empty-rooms {
  padding: 24px;
  text-align: center;
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-ghost);
}

.room-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 10px;
  border-radius: var(--radius-md);
  text-align: left;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
  margin-bottom: 2px;
}
.room-item:hover { background: var(--bg-elevated); }
.room-item.active { background: var(--accent-subtle); }
.room-icon { font-size: 16px; color: var(--text-muted); flex-shrink: 0; width: 20px; text-align: center; }
.room-item.active .room-icon { color: var(--accent); }
.room-info { flex: 1; min-width: 0; }
.room-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.room-item.active .room-name { color: var(--accent); }
.room-preview {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 1px;
}
.active-indicator {
  width: 3px; height: 20px;
  background: var(--accent);
  border-radius: 2px;
  position: absolute;
  right: 0;
  box-shadow: 0 0 8px var(--accent);
}

.loading-rooms { flex: 1; padding: 8px; }
.skeleton {
  height: 52px;
  border-radius: var(--radius-md);
  margin-bottom: 4px;
  background: linear-gradient(90deg, var(--bg-elevated) 25%, var(--bg-hover) 50%, var(--bg-elevated) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

/* Modal */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.7);
  backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center;
  z-index: 100;
  animation: fade-up 0.2s ease;
}
.modal {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  padding: 32px 28px;
  width: 340px;
  box-shadow: var(--shadow-deep);
}
.modal-title {
  font-family: var(--font-mono);
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
}
.modal-title .accent { color: var(--accent); animation: pulse-dot 1s infinite; display: inline-block; }
.modal-input {
  width: 100%;
  background: var(--bg-deep);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
  margin-bottom: 10px;
  transition: border-color 0.2s;
  font-family: var(--font-sans);
}
.modal-input:focus { border-color: var(--accent-dim); }
.modal-input::placeholder { color: var(--text-ghost); }
.modal-type-row { display: flex; gap: 8px; margin-bottom: 20px; }
.type-btn {
  flex: 1; padding: 8px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
  transition: all 0.2s;
}
.type-btn.active { border-color: var(--accent-dim); background: var(--accent-subtle); color: var(--accent); }
.modal-actions { display: flex; gap: 8px; }
.modal-cancel {
  flex: 1; padding: 10px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
  transition: all 0.2s;
}
.modal-cancel:hover { border-color: var(--text-muted); color: var(--text-primary); }
.modal-create {
  flex: 1; padding: 10px;
  background: var(--accent);
  color: var(--bg-void);
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 12px;
  font-weight: 700;
  transition: all 0.2s;
}
.modal-create:hover:not(:disabled) { background: #1affaa; }
.modal-create:disabled { opacity: 0.4; cursor: not-allowed; }
</style>