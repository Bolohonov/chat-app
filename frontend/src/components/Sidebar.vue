<template>
  <aside class="sidebar">
    <div class="profile-strip">
      <div class="avatar">{{ initials }}</div>
      <div class="profile-info">
        <span class="profile-name">{{ authS.displayName }}</span>
        <span class="profile-handle">@{{ authS.username }}</span>
      </div>
      <button class="logout-btn" @click="handleLogout" title="Sign out">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/>
        </svg>
      </button>
    </div>

    <div class="search-wrap">
      <svg class="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
      </svg>
      <input
          v-model="search"
          class="search-input"
          placeholder="поиск чатов и пользователей..."
          @input="onSearchInput"
          @focus="showDropdown = search.length > 0"
          @blur="hideDropdownDelayed"
      />
      <div v-if="showDropdown" class="search-dropdown">
        <div v-if="searchLoading" class="search-loading">поиск...</div>
        <template v-else>
          <template v-if="searchResults.rooms.length || searchResults.users.length">
            <div v-if="searchResults.rooms.length" class="search-section-label">чаты</div>
            <button v-for="room in searchResults.rooms" :key="'r-' + room.id" class="search-item" @mousedown.prevent="selectRoom(room)">
              <span class="search-item-icon">⊞</span>
              <span class="search-item-name">{{ room.name }}</span>
            </button>
            <div v-if="searchResults.users.length" class="search-section-label">пользователи</div>
            <button v-for="user in searchResults.users" :key="'u-' + user.id" class="search-item" @mousedown.prevent="openAddUserToRoom(user)">
              <span class="search-item-icon">◎</span>
              <div class="search-item-info">
                <span class="search-item-name">{{ user.displayName }}</span>
                <span class="search-item-sub">@{{ user.username }}</span>
              </div>
            </button>
          </template>
          <div v-else class="search-empty">ничего не найдено</div>
        </template>
      </div>
    </div>

    <div class="section-header">
      <span class="section-label">чаты</span>
      <button class="new-room-btn" @click="showNewRoom = true" title="Новая комната">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path d="M12 5v14M5 12h14"/>
        </svg>
      </button>
    </div>

    <div class="rooms-list" v-if="!loading">
      <div v-if="rooms.rooms.length === 0" class="empty-rooms"><span>чатов пока нет</span></div>
      <div
          v-for="room in rooms.rooms"
          :key="room.id"
          :class="['room-item', { active: rooms.activeRoom?.id === room.id }]"
          @click="selectRoom(room)"
      >
        <div class="room-icon">⊞</div>
        <div class="room-info">
          <span class="room-name">{{ room.name }}</span>
          <span v-if="room.lastMessage" class="room-preview">{{ room.lastMessage }}</span>
        </div>
        <button
            v-if="room.ownerId === authS.userId"
            class="room-delete-btn"
            @click.stop="confirmDelete(room)"
            title="Удалить комнату"
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6"/>
          </svg>
        </button>
        <div v-if="rooms.activeRoom?.id === room.id" class="active-indicator"></div>
      </div>
    </div>
    <div v-else class="loading-rooms">
      <div class="skeleton" v-for="i in 4" :key="i"></div>
    </div>

    <!-- New Room Modal -->
    <Teleport to="body">
      <div v-if="showNewRoom" class="modal-overlay" @click.self="showNewRoom = false">
        <div class="modal">
          <div class="modal-title">новый_чат <span class="accent">_</span></div>
          <input v-model="newRoom.name" class="modal-input" placeholder="название чата" maxlength="50" />
          <input v-model="newRoom.description" class="modal-input" placeholder="описание (не обязательно)" maxlength="200" />
          <div class="modal-actions">
            <button class="modal-cancel" @click="showNewRoom = false">отмена</button>
            <button class="modal-create" :disabled="!newRoom.name || createLoading" @click="createRoom">
              {{ createLoading ? '...' : 'создать_' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Add User to Room Modal -->
    <Teleport to="body">
      <div v-if="showAddUser" class="modal-overlay" @click.self="showAddUser = false">
        <div class="modal">
          <div class="modal-title">добавить_в_комнату <span class="accent">_</span></div>
          <div class="modal-user-info">
            <span class="modal-user-name">{{ pendingUser?.displayName }}</span>
            <span class="modal-user-handle">@{{ pendingUser?.username }}</span>
          </div>
          <div class="modal-subtitle">выберите комнату:</div>
          <div class="modal-room-list">
            <button
                v-for="room in rooms.rooms"
                :key="room.id"
                :class="['modal-room-item', { selected: selectedRoomForUser?.id === room.id }]"
                @click="selectedRoomForUser = room"
            >
              <span class="modal-room-icon">⊞</span>
              {{ room.name }}
            </button>
          </div>
          <div v-if="addUserError" class="modal-error">{{ addUserError }}</div>
          <div class="modal-actions">
            <button class="modal-cancel" @click="showAddUser = false">отмена</button>
            <button class="modal-create" :disabled="!selectedRoomForUser || addUserLoading" @click="confirmAddUser">
              {{ addUserLoading ? '...' : 'добавить_' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </aside>

  <!-- Delete Room Confirm Modal -->
  <Teleport to="body">
    <div v-if="roomToDelete" class="modal-overlay" @click.self="roomToDelete = null">
      <div class="modal">
        <div class="modal-title">удалить_комнату <span class="accent">_</span></div>
        <p class="modal-confirm-text">
          Удалить комнату <strong>{{ roomToDelete.name }}</strong>?
          Все сообщения будут потеряны.
        </p>
        <div class="modal-actions">
          <button class="modal-cancel" @click="roomToDelete = null">отмена</button>
          <button class="modal-delete" @click="doDeleteRoom">удалить_</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useRoomsStore } from '@/stores/rooms'
import api from '@/composables/api'

const emit  = defineEmits(['room-selected'])
const router = useRouter()
const authS = useAuthStore()
const rooms = useRoomsStore()

const search        = ref('')
const loading       = ref(true)
const showNewRoom   = ref(false)
const createLoading = ref(false)
const newRoom       = ref({ name: '', description: '' })

const showDropdown  = ref(false)
const searchLoading = ref(false)
const searchResults = ref({ rooms: [], users: [] })
let searchTimer = null

const showAddUser         = ref(false)
const pendingUser         = ref(null)
const selectedRoomForUser = ref(null)
const addUserLoading      = ref(false)
const addUserError        = ref('')

const initials = computed(() =>
    (authS.displayName || authS.username || '?').slice(0, 2).toUpperCase()
)

onMounted(async () => {
  try { await rooms.fetchRooms() } finally { loading.value = false }
})

function selectRoom(room) {
  rooms.selectRoom(room)
  emit('room-selected', room)
  search.value = ''
  showDropdown.value = false
}

async function createRoom() {
  if (!newRoom.value.name || createLoading.value) return
  createLoading.value = true
  try {
    const room = await rooms.createRoom({
      name: newRoom.value.name,
      description: newRoom.value.description,
      type: 'GROUP'
    })
    showNewRoom.value = false
    newRoom.value = { name: '', description: '' }
    selectRoom(room)
  } catch (e) {
    console.error('Failed to create room', e)
  } finally {
    createLoading.value = false
  }
}

function handleLogout() {
  authS.logout()
  router.push('/login')
}

function onSearchInput() {
  clearTimeout(searchTimer)
  if (search.value.trim().length < 1) {
    searchResults.value = { rooms: [], users: [] }
    showDropdown.value = false
    return
  }
  showDropdown.value = true
  searchLoading.value = true
  searchTimer = setTimeout(async () => {
    try {
      const { data } = await api.get('/api/search', { params: { q: search.value.trim() } })
      searchResults.value = data
    } catch {
      searchResults.value = { rooms: [], users: [] }
    } finally {
      searchLoading.value = false
    }
  }, 300)
}

function hideDropdownDelayed() {
  setTimeout(() => { showDropdown.value = false }, 150)
}

function openAddUserToRoom(user) {
  pendingUser.value = user
  selectedRoomForUser.value = null
  addUserError.value = ''
  showAddUser.value = true
  showDropdown.value = false
  search.value = ''
}

async function confirmAddUser() {
  if (!pendingUser.value || !selectedRoomForUser.value) return
  addUserLoading.value = true
  addUserError.value = ''
  try {
    await api.post(`/api/rooms/${selectedRoomForUser.value.id}/members/${pendingUser.value.id}`)
    showAddUser.value = false
  } catch (e) {
    addUserError.value = e?.response?.data?.message || 'ошибка при добавлении'
  } finally {
    addUserLoading.value = false
  }
}

// Delete room
const roomToDelete = ref(null)

function confirmDelete(room) {
  roomToDelete.value = room
}

async function doDeleteRoom() {
  if (!roomToDelete.value) return
  try {
    await rooms.deleteRoom(roomToDelete.value.id)
  } catch (e) {
    console.error('Failed to delete room', e)
  } finally {
    roomToDelete.value = null
  }
}

defineExpose({ openAddUserToRoom })
</script>

<style scoped>
.sidebar { width: 280px; flex-shrink: 0; background: var(--bg-surface); border-right: 1px solid var(--border); display: flex; flex-direction: column; overflow: hidden; }
.profile-strip { display: flex; align-items: center; gap: 10px; padding: 16px 16px 14px; border-bottom: 1px solid var(--border); }
.avatar { width: 36px; height: 36px; border-radius: 10px; background: var(--accent-glow); border: 1px solid var(--border-accent); color: var(--accent); font-family: var(--font-mono); font-size: 13px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.profile-info { flex: 1; min-width: 0; }
.profile-name { display: block; font-size: 13px; font-weight: 600; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.profile-handle { display: block; font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); }
.logout-btn { color: var(--text-muted); padding: 6px; border-radius: var(--radius-sm); transition: color 0.2s, background 0.2s; }
.logout-btn:hover { color: var(--danger); background: rgba(255,71,87,0.08); }
.search-wrap { position: relative; padding: 12px 14px; border-bottom: 1px solid var(--border); }
.search-icon { position: absolute; left: 26px; top: 22px; color: var(--text-muted); pointer-events: none; }
.search-input { width: 100%; background: var(--bg-deep); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 8px 10px 8px 32px; font-size: 13px; color: var(--text-primary); outline: none; font-family: var(--font-mono); transition: border-color 0.2s; }
.search-input:focus { border-color: var(--accent-dim); }
.search-input::placeholder { color: var(--text-ghost); }
.search-dropdown { position: absolute; left: 14px; right: 14px; top: calc(100% - 4px); background: var(--bg-surface); border: 1px solid var(--border-accent); border-radius: var(--radius-md); box-shadow: var(--shadow-deep); z-index: 200; max-height: 320px; overflow-y: auto; padding: 6px 0; }
.search-loading { padding: 14px 16px; font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); }
.search-section-label { padding: 6px 14px 2px; font-family: var(--font-mono); font-size: 10px; letter-spacing: 1.5px; color: var(--text-muted); text-transform: uppercase; }
.search-item { width: 100%; display: flex; align-items: center; gap: 10px; padding: 8px 14px; text-align: left; transition: background 0.15s; cursor: pointer; }
.search-item:hover { background: var(--bg-elevated); }
.search-item-icon { font-size: 14px; color: var(--text-muted); flex-shrink: 0; width: 18px; text-align: center; }
.search-item-info { display: flex; flex-direction: column; }
.search-item-name { font-size: 13px; color: var(--text-primary); font-weight: 500; }
.search-item-sub { font-size: 11px; color: var(--text-muted); font-family: var(--font-mono); }
.search-empty { padding: 14px; text-align: center; font-family: var(--font-mono); font-size: 12px; color: var(--text-ghost); }
.section-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px 8px; }
.section-label { font-family: var(--font-mono); font-size: 10px; letter-spacing: 2px; color: var(--text-muted); text-transform: uppercase; }
.new-room-btn { width: 22px; height: 22px; border-radius: 5px; display: flex; align-items: center; justify-content: center; color: var(--text-muted); transition: all 0.2s; }
.new-room-btn:hover { background: var(--accent-subtle); color: var(--accent); }
.rooms-list { flex: 1; overflow-y: auto; padding: 4px 8px 8px; }
.empty-rooms { padding: 24px; text-align: center; font-family: var(--font-mono); font-size: 12px; color: var(--text-ghost); }
.room-item { width: 100%; display: flex; align-items: center; gap: 10px; padding: 10px 10px; border-radius: var(--radius-md); text-align: left; cursor: pointer; transition: background 0.15s; position: relative; margin-bottom: 2px; }
.room-item:hover { background: var(--bg-elevated); }
.room-item.active { background: var(--accent-subtle); }
.room-icon { font-size: 16px; color: var(--text-muted); flex-shrink: 0; width: 20px; text-align: center; }
.room-item.active .room-icon { color: var(--accent); }
.room-info { flex: 1; min-width: 0; }
.room-name { display: block; font-size: 14px; font-weight: 500; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.room-item.active .room-name { color: var(--accent); }
.room-preview { display: block; font-size: 11px; color: var(--text-muted); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-top: 1px; }
.active-indicator { width: 3px; height: 20px; background: var(--accent); border-radius: 2px; position: absolute; right: 0; box-shadow: 0 0 8px var(--accent); }
.loading-rooms { flex: 1; padding: 8px; }
.skeleton { height: 52px; border-radius: var(--radius-md); margin-bottom: 4px; background: linear-gradient(90deg, var(--bg-elevated) 25%, var(--bg-hover) 50%, var(--bg-elevated) 75%); background-size: 200% 100%; animation: shimmer 1.4s infinite; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.7); backdrop-filter: blur(6px); display: flex; align-items: center; justify-content: center; z-index: 100; }
.modal { background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-xl); padding: 32px 28px; width: 360px; box-shadow: var(--shadow-deep); }
.modal-title { font-family: var(--font-mono); font-size: 16px; font-weight: 700; color: var(--text-primary); margin-bottom: 20px; }
.modal-title .accent { color: var(--accent); }
.modal-input { width: 100%; background: var(--bg-deep); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 10px 14px; color: var(--text-primary); font-size: 14px; outline: none; margin-bottom: 10px; transition: border-color 0.2s; font-family: var(--font-sans); }
.modal-input:focus { border-color: var(--accent-dim); }
.modal-input::placeholder { color: var(--text-ghost); }
.modal-user-info { background: var(--bg-deep); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 10px 14px; margin-bottom: 14px; display: flex; align-items: center; gap: 10px; }
.modal-user-name { font-weight: 600; color: var(--text-primary); font-size: 14px; }
.modal-user-handle { font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); }
.modal-subtitle { font-family: var(--font-mono); font-size: 11px; letter-spacing: 1px; color: var(--text-muted); text-transform: uppercase; margin-bottom: 8px; }
.modal-room-list { max-height: 200px; overflow-y: auto; border: 1px solid var(--border); border-radius: var(--radius-sm); margin-bottom: 16px; }
.modal-room-item { width: 100%; display: flex; align-items: center; gap: 8px; padding: 10px 12px; text-align: left; font-size: 13px; color: var(--text-primary); transition: background 0.15s; cursor: pointer; }
.modal-room-item:hover { background: var(--bg-elevated); }
.modal-room-item.selected { background: var(--accent-subtle); color: var(--accent); }
.modal-room-icon { font-size: 14px; color: var(--text-muted); }
.modal-room-item.selected .modal-room-icon { color: var(--accent); }
.modal-error { font-size: 12px; color: var(--danger); font-family: var(--font-mono); margin-bottom: 10px; }
.modal-actions { display: flex; gap: 8px; }
.modal-cancel { flex: 1; padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); transition: all 0.2s; }
.modal-cancel:hover { border-color: var(--text-muted); color: var(--text-primary); }
.modal-create { flex: 1; padding: 10px; background: var(--accent); color: var(--bg-void); border-radius: var(--radius-sm); font-family: var(--font-mono); font-size: 12px; font-weight: 700; transition: all 0.2s; }
.modal-create:hover:not(:disabled) { background: #1affaa; }
.modal-create:disabled { opacity: 0.4; cursor: not-allowed; }
.modal-confirm-text { font-size: 14px; color: var(--text-secondary); margin-bottom: 20px; line-height: 1.5; }
.modal-confirm-text strong { color: var(--text-primary); }
.modal-delete { flex: 1; padding: 10px; background: var(--danger); color: #fff; border-radius: var(--radius-sm); font-family: var(--font-mono); font-size: 12px; font-weight: 700; transition: all 0.2s; cursor: pointer; border: none; }
.modal-delete:hover { background: #ff6b7a; }
.room-delete-btn { opacity: 0; width: 24px; height: 24px; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: var(--text-muted); transition: all 0.15s; flex-shrink: 0; margin-right: 4px; }
.room-item:hover .room-delete-btn { opacity: 1; }
.room-delete-btn:hover { background: rgba(255,71,87,0.15); color: var(--danger); }

/* Mobile */
@media (max-width: 767px) {
  .sidebar { width: 100%; }
  .room-item { padding: 13px 10px; }
  .search-input { font-size: 16px !important; }
  .modal-input { font-size: 16px !important; }
}
</style>
