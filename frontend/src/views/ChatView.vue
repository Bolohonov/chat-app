<template>
  <div class="chat-shell">
    <Sidebar @room-selected="onRoomSelected" />
    <div class="chat-main">
      <template v-if="rooms.activeRoom">
        <ChatHeader />
        <MessageList :typing-users="typingUsers" />
        <MessageInput @send="sendMessage" @typing="ws.sendTyping()" />
      </template>
      <EmptyState v-else />
    </div>
  </div>
</template>

<script setup>
import { watch } from 'vue'
import { useRoomsStore } from '@/stores/rooms'
import { useWebSocket } from '@/composables/useWebSocket'
import Sidebar from '@/components/Sidebar.vue'
import ChatHeader from '@/components/ChatHeader.vue'
import MessageList from '@/components/MessageList.vue'
import MessageInput from '@/components/MessageInput.vue'
import EmptyState from '@/components/EmptyState.vue'

const rooms = useRoomsStore()
const ws    = useWebSocket()
const { typingUsers } = ws

function onRoomSelected(room) {
  ws.connect(room.id)
}

function sendMessage(content) {
  ws.send(content)
}
</script>

<style scoped>
.chat-shell {
  display: flex;
  height: 100vh;
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
</style>