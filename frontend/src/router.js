import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  { path: '/login',  component: () => import('@/views/LoginView.vue'), meta: { guest: true } },
  { path: '/',       component: () => import('@/views/ChatView.vue'),  meta: { auth: true } },
  { path: '/:path(.*)', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory('/chat/'),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.token) return '/login'
  if (to.meta.guest && auth.token)  return '/'
})

export default router