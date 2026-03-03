import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/composables/api'

export const useRoomsStore = defineStore('rooms', () => {
  const rooms      = ref([])
  const activeRoom = ref(null)
  const messages   = ref([])
  const loading    = ref(false)

  async function fetchRooms() {
    const { data } = await api.get('/api/rooms')
    rooms.value = data
  }

  async function createRoom(payload) {
    const { data } = await api.post('/api/rooms', payload)
    rooms.value.unshift(data)
    return data
  }

  async function deleteRoom(roomId) {
    await api.delete(`/api/rooms/${roomId}`)
    rooms.value = rooms.value.filter(r => r.id !== roomId)
    if (activeRoom.value?.id === roomId) {
      activeRoom.value = null
      messages.value = []
    }
  }

  async function selectRoom(room) {
    activeRoom.value = room
    messages.value = []
    loading.value = true
    try {
      const { data } = await api.get(`/api/rooms/${room.id}/messages?page=0&size=80`)
      messages.value = [...data.content].reverse()
    } finally {
      loading.value = false
    }
  }

  function pushMessage(msg) {
    messages.value.push(msg)
  }

  function updateRoomLastMessage(roomId, content) {
    const r = rooms.value.find(r => r.id === roomId)
    if (r) r.lastMessage = content
  }

  return { rooms, activeRoom, messages, loading,
    fetchRooms, createRoom, deleteRoom, selectRoom, pushMessage, updateRoomLastMessage }
})
