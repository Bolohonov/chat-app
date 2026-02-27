import { ref, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRoomsStore } from '@/stores/rooms'

export function useWebSocket() {
  const auth = useAuthStore()
  const roomsStore = useRoomsStore()

  let ws = null
  const connected   = ref(false)
  const typingUsers = ref(new Set())
  let typingTimer   = null
  let currentRoomId = null

  function connect(roomId) {
    disconnect()
    currentRoomId = roomId

    const proto = location.protocol === 'https:' ? 'wss' : 'ws'
    const url = `${proto}://${location.host}/ws/${roomId}?token=${auth.token}`
    ws = new WebSocket(url)

    ws.onopen = () => { connected.value = true }

    ws.onmessage = (e) => {
      const lines = e.data.trim().split('\n')
      for (const line of lines) {
        try {
          const msg = JSON.parse(line)
          handleMessage(msg)
        } catch {}
      }
    }

    ws.onclose = () => {
      connected.value = false
      // Auto-reconnect after 3s
      setTimeout(() => { if (currentRoomId === roomId) connect(roomId) }, 3000)
    }

    ws.onerror = () => ws.close()
  }

  function handleMessage(msg) {
    if (msg.type === 'typing') {
      if (msg.userId !== auth.userId) {
        typingUsers.value.add(msg.userId)
        clearTimeout(typingTimer)
        typingTimer = setTimeout(() => typingUsers.value.clear(), 3000)
      }
      return
    }

    if (msg.type === 'message') {
      roomsStore.pushMessage({
        id:          msg.id || Date.now(),
        senderId:    msg.userId,
        senderName:  msg.senderName || msg.userId,
        content:     msg.content,
        createdAt:   msg.createdAt || new Date().toISOString(),
        roomId:      msg.roomId
      })
      roomsStore.updateRoomLastMessage(msg.roomId, msg.content)
    }
  }

  function send(content) {
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'message', content }))
    }
  }

  function sendTyping() {
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ type: 'typing', content: '' }))
    }
  }

  function disconnect() {
    currentRoomId = null
    if (ws) { ws.onclose = null; ws.close(); ws = null }
    connected.value = false
    typingUsers.value.clear()
  }

  onUnmounted(disconnect)

  return { connected, typingUsers, connect, send, sendTyping, disconnect }
}