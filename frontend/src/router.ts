import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuth, type UserRole } from '@/auth/auth'

import LoginView from '@/views/LoginView.vue'
import PatientPortalView from '@/views/PatientPortalView.vue'
import DoctorPortalView from '@/views/DoctorPortalView.vue'

type Meta = {
  requiresAuth?: boolean
  role?: UserRole
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: () => {
      const { state } = useAuth()
      if (state.role === 'doctor') return '/doctor'
      if (state.role === 'patient') return '/patient'
      return '/login'
    },
  },
  { path: '/login', name: 'login', component: LoginView },
  {
    path: '/patient',
    name: 'patient',
    component: PatientPortalView,
    meta: { requiresAuth: true, role: 'patient' } satisfies Meta,
  },
  {
    path: '/doctor',
    name: 'doctor',
    component: DoctorPortalView,
    meta: { requiresAuth: true, role: 'doctor' } satisfies Meta,
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const { isAuthenticated, state } = useAuth()
  const meta = (to.meta || {}) as Meta

  // If already logged in, keep them out of /login.
  if (to.path === '/login' && isAuthenticated.value) {
    return state.role === 'doctor' ? '/doctor' : '/patient'
  }

  if (meta.requiresAuth && !isAuthenticated.value) {
    return '/login'
  }

  if (meta.role && state.role && meta.role !== state.role) {
    return state.role === 'doctor' ? '/doctor' : '/patient'
  }

  return true
})
