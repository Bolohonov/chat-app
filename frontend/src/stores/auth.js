import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/composables/api'

export const useAuthStore = defineStore('auth', () => {
  const token    = ref(localStorage.getItem('token') || '')
  const userId   = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const displayName = ref(localStorage.getItem('displayName') || '')

  const isLoggedIn = computed(() => !!token.value)

  function _persist(data) {
    token.value       = data.token
    userId.value      = data.userId
    username.value    = data.username
    displayName.value = data.displayName
    localStorage.setItem('token',       data.token)
    localStorage.setItem('userId',      data.userId)
    localStorage.setItem('username',    data.username)
    localStorage.setItem('displayName', data.displayName)
  }

  async function login(creds) {
    const { data } = await api.post('/api/auth/login', creds)
    _persist(data)
  }

  async function register(payload) {
    const { data } = await api.post('/api/auth/register', payload)
    _persist(data)
  }

  function logout() {
    token.value = userId.value = username.value = displayName.value = ''
    localStorage.clear()
  }

  return { token, userId, username, displayName, isLoggedIn, login, register, logout }
})