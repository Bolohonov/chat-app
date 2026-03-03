<template>
  <div class="chat-shell">
    <Sidebar
        ref="sidebarRef"
        :class="['sidebar-panel', { 'mobile-hidden': isMobile && rooms.activeRoom && !showSidebar }]"
        @room-selected="onRoomSelected"
    />

    <div
        class="chat-main"
        :class="{ 'mobile-hidden': isMobile && !rooms.activeRoom && !showSidebar }"
    >
      <template v-if="rooms.activeRoom">
        <ChatHeader
            @add-user="openAddUserSearch"
            @back="goBack"
            @toggle-members="showMembers = !showMembers"
            :show-back="isMobile"
            :show-members="showMembers"
            :member-count="roomMembers.length"
        />
        <MessageList :typing-users="typingUsers" />
        <MessageInput @send="sendMessage" @typing="ws.sendTyping()" />
      </template>
      <EmptyState v-else />
    </div>

    <MembersPanel v-model="showMembers" :members="roomMembers" />

    <!-- Add user search modal -->
    <Teleport to="body">
      <div v-if="showUserSearch" class="modal-overlay" @click.self="showUserSearch = false">
        <div class="modal">
          <div class="modal-title">найти_участника <span class="accent">_</span></div>
          <input
              v-model="userSearchQuery"
              class="modal-input"
              placeholder="имя пользователя..."
              @input="onUserSearch"
          />
          <div v-if="userSearchLoading" class="modal-searching">поиск...</div>
          <div v-else-if="userSearchResults.length" class="modal-user-list">
            <button
                v-for="user in userSearchResults"
                :key="user.id"
                class="modal-user-item"
                @click="pickUser(user)"
            >
              <span class="modal-user-avatar">{{ (user.displayName || user.username).slice(0,2).toUpperCase() }}</span>
              <div class="modal-user-info">
                <span class="modal-user-name">{{ user.displayName }}</span>
                <span class="modal-user-handle">@{{ user.username }}</span>
              </div>
            </button>
          </div>
          <div v-else-if="userSearchQuery.length > 1" class="modal-empty">ничего не найдено</div>
          <div class="modal-actions">
            <button class="modal-cancel" @click="showUserSearch = false">отмена</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoomsStore } from '@/stores/rooms'
import { useAuthStore } from '@/stores/auth'
import { useWebSocket } from '@/composables/useWebSocket'
import api from '@/composables/api'
import Sidebar from '@/components/Sidebar.vue'
import ChatHeader from '@/components/ChatHeader.vue'
import MessageList from '@/components/MessageList.vue'
import MessageInput from '@/components/MessageInput.vue'
import EmptyState from '@/components/EmptyState.vue'
import MembersPanel from '@/components/MembersPanel.vue'

const rooms = useRoomsStore()
const auth  = useAuthStore()
const ws    = useWebSocket()
const { typingUsers, roomMembers } = ws

const sidebarRef  = ref(null)
const showSidebar = ref(false)
const showMembers = ref(false)
const windowWidth = ref(window.innerWidth)
const isMobile    = computed(() => windowWidth.value < 768)

const showUserSearch    = ref(false)
const userSearchQuery   = ref('')
const userSearchLoading = ref(false)
const userSearchResults = ref([])
let userSearchTimer = null

function onResize() { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

function onRoomSelected(room) {
  ws.connect(room.id)
  showSidebar.value = false
  showMembers.value = false
}

function goBack() {
  rooms.activeRoom = null
  showSidebar.value = false
  showMembers.value = false
}

function sendMessage(content) {
  rooms.pushMessage({
    id:         Date.now(),
    senderId:   auth.userId,
    senderName: auth.displayName || auth.username,
    content,
    createdAt:  new Date().toISOString(),
    roomId:     rooms.activeRoom?.id
  })
  rooms.updateRoomLastMessage(rooms.activeRoom?.id, content)
  ws.send(content)
}

function openAddUserSearch() {
  userSearchQuery.value = ''
  userSearchResults.value = []
  showUserSearch.value = true
}

function onUserSearch() {
  clearTimeout(userSearchTimer)
  if (userSearchQuery.value.trim().length < 2) { userSearchResults.value = []; return }
  userSearchLoading.value = true
  userSearchTimer = setTimeout(async () => {
    try {
      const { data } = await api.get('/api/search', { params: { q: userSearchQuery.value.trim() } })
      userSearchResults.value = data.users || []
    } catch {
      userSearchResults.value = []
    } finally {
      userSearchLoading.value = false
    }
  }, 300)
}

function pickUser(user) {
  showUserSearch.value = false
  sidebarRef.value?.openAddUserToRoom(user)
}
</script>

<style scoped>
.chat-shell {
  display: flex;
  height: 100dvh;
  background: var(--bg-void);
  overflow: hidden;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: var(--bg-deep);
}

@media (max-width: 767px) {
  .sidebar-panel {
    width: 100% !important;
    position: absolute;
    inset: 0;
    z-index: 10;
  }
  .chat-main {
    position: absolute;
    inset: 0;
    width: 100%;
  }
  .mobile-hidden {
    display: none !important;
  }
}

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.7); backdrop-filter: blur(6px); display: flex; align-items: center; justify-content: center; z-index: 100; padding: 16px; }
.modal { background: var(--bg-surface); border: 1px solid var(--border); border-radius: var(--radius-xl); padding: 28px 24px; width: 100%; max-width: 360px; box-shadow: var(--shadow-deep); }
.modal-title { font-family: var(--font-mono); font-size: 15px; font-weight: 700; color: var(--text-primary); margin-bottom: 16px; }
.modal-title .accent { color: var(--accent); }
.modal-input { width: 100%; background: var(--bg-deep); border: 1px solid var(--border); border-radius: var(--radius-sm); padding: 10px 14px; color: var(--text-primary); font-size: 14px; outline: none; margin-bottom: 10px; transition: border-color 0.2s; font-family: var(--font-sans); }
.modal-input:focus { border-color: var(--accent-dim); }
.modal-input::placeholder { color: var(--text-ghost); }
.modal-searching, .modal-empty { padding: 10px 0; font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); }
.modal-user-list { border: 1px solid var(--border); border-radius: var(--radius-sm); margin-bottom: 14px; max-height: 220px; overflow-y: auto; }
.modal-user-item { width: 100%; display: flex; align-items: center; gap: 10px; padding: 10px 12px; text-align: left; transition: background 0.15s; cursor: pointer; }
.modal-user-item:hover { background: var(--bg-elevated); }
.modal-user-avatar { width: 32px; height: 32px; border-radius: 8px; background: var(--accent-glow); border: 1px solid var(--border-accent); color: var(--accent); font-family: var(--font-mono); font-size: 11px; font-weight: 700; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.modal-user-info { display: flex; flex-direction: column; }
.modal-user-name { font-size: 13px; font-weight: 600; color: var(--text-primary); }
.modal-user-handle { font-family: var(--font-mono); font-size: 11px; color: var(--text-muted); }
.modal-actions { display: flex; gap: 8px; margin-top: 4px; }
.modal-cancel { flex: 1; padding: 10px; border: 1px solid var(--border); border-radius: var(--radius-sm); font-family: var(--font-mono); font-size: 12px; color: var(--text-muted); transition: all 0.2s; cursor: pointer; }
.modal-cancel:hover { border-color: var(--text-muted); color: var(--text-primary); }
</style>