<template>
  <div class="auth-shell">
    <!-- Background grid -->
    <div class="grid-bg"></div>

    <div class="auth-card">
      <!-- Logo -->
      <div class="logo">
        <span class="logo-bracket">[</span>
        <span class="logo-text">RELAY</span>
        <span class="logo-bracket">]</span>
      </div>
      <p class="tagline">secure · real-time · minimal</p>

      <!-- Tab switcher -->
      <div class="tabs">
        <button :class="['tab', { active: mode === 'login' }]" @click="mode = 'login'">sign_in</button>
        <button :class="['tab', { active: mode === 'register' }]" @click="mode = 'register'">sign_up</button>
      </div>

      <!-- Form -->
      <form class="form" @submit.prevent="submit">
        <div v-if="mode === 'register'" class="field" style="animation-delay:0s">
          <label>отображаемое_имя</label>
          <input v-model="form.displayName" placeholder="Как другие будут вас видеть" autocomplete="off"/>
        </div>
        <div class="field" :style="{ animationDelay: mode === 'register' ? '0.05s' : '0s' }">
          <label>имя</label>
          <input v-model="form.username" placeholder="Ваше имя" autocomplete="username"/>
        </div>
        <div v-if="mode === 'register'" class="field" style="animation-delay:0.1s">
          <label>email</label>
          <input v-model="form.email" type="email" placeholder="you@domain.com"/>
        </div>
        <div class="field" :style="{ animationDelay: mode === 'register' ? '0.15s' : '0.05s' }">
          <label>пароль</label>
          <input v-model="form.password" type="password" placeholder="••••••••" autocomplete="current-password"/>
        </div>

        <div v-if="error" class="error">⚠ {{ error }}</div>

        <button type="submit" class="btn-submit" :disabled="loading">
          <span v-if="!loading">{{ mode === 'login' ? 'аутентификация_' : 'создать_аккаунт_' }}</span>
          <span v-else class="loading-dots">
            <span></span><span></span><span></span>
          </span>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth   = useAuthStore()
const mode   = ref('login')
const loading = ref(false)
const error  = ref('')

const form = reactive({ username: '', password: '', email: '', displayName: '' })

watch(mode, () => { error.value = ''; Object.assign(form, { username:'', password:'', email:'', displayName:'' }) })

async function submit() {
  error.value = ''

  if (!form.username || !form.password) { error.value = 'Заполните все обязательные поля'; return }
  if (form.username.length < 3 || form.username.length > 30) { error.value = 'Имя пользователя — от 3 до 30 символов'; return }
  if (form.password.length < 6) { error.value = 'Пароль — минимум 6 символов'; return }

  if (mode.value === 'register') {
    if (!form.email) { error.value = 'Email обязателен'; return }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) { error.value = 'Некорректный email'; return }
  }

  loading.value = true
  try {
    if (mode.value === 'login') {
      await auth.login({ username: form.username, password: form.password })
    } else {
      await auth.register({ username: form.username, password: form.password, email: form.email, displayName: form.displayName || form.username })
    }
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || e.response?.data || 'Ошибка аутентификации'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-void);
  position: relative;
  overflow: hidden;
}

.grid-bg {
  position: absolute;
  inset: 0;
  background-image:
      linear-gradient(rgba(0,255,135,0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0,255,135,0.03) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 70% 70% at 50% 50%, black, transparent);
}

.auth-card {
  position: relative;
  width: 420px;
  padding: 48px 40px;
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-deep), 0 0 60px rgba(0,255,135,0.05);
  animation: fade-up 0.5s ease both;
}

.logo {
  font-family: var(--font-mono);
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  letter-spacing: 6px;
  margin-bottom: 6px;
}
.logo-bracket { color: var(--accent); }
.logo-text { color: var(--text-primary); }

.tagline {
  text-align: center;
  font-size: 11px;
  letter-spacing: 3px;
  color: var(--text-muted);
  text-transform: uppercase;
  margin-bottom: 32px;
  font-family: var(--font-mono);
}

.tabs {
  display: flex;
  gap: 4px;
  background: var(--bg-deep);
  border-radius: var(--radius-md);
  padding: 4px;
  margin-bottom: 28px;
}
.tab {
  flex: 1;
  padding: 9px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-muted);
  transition: all 0.2s;
  letter-spacing: 0.5px;
}
.tab.active {
  background: var(--bg-elevated);
  color: var(--accent);
}

.form { display: flex; flex-direction: column; gap: 16px; }

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  animation: fade-up 0.35s ease both;
}
.field label {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-muted);
  letter-spacing: 1px;
}
.field input {
  background: var(--bg-deep);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 11px 14px;
  color: var(--text-primary);
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s;
  outline: none;
}
.field input:focus {
  border-color: var(--accent-dim);
  box-shadow: 0 0 0 3px var(--accent-subtle);
}
.field input::placeholder { color: var(--text-ghost); }

.error {
  font-size: 13px;
  color: var(--danger);
  background: rgba(255,71,87,0.08);
  border: 1px solid rgba(255,71,87,0.2);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-family: var(--font-mono);
}

.btn-submit {
  margin-top: 4px;
  padding: 13px;
  background: var(--accent);
  color: var(--bg-void);
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  transition: all 0.2s;
  animation: glow-pulse 3s ease-in-out infinite;
}
.btn-submit:hover:not(:disabled) {
  background: #1affaa;
  transform: translateY(-1px);
}
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; animation: none; }

.loading-dots { display: flex; align-items: center; justify-content: center; gap: 5px; height: 18px; }
.loading-dots span {
  width: 6px; height: 6px;
  background: var(--bg-void);
  border-radius: 50%;
  animation: typing-bounce 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
</style>