import { defineStore } from 'pinia'
import { doLogin, logout, getCurrentUser } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    isLoggedIn: false
  }),

  actions: {
    async login(username, password) {
      await doLogin(username, password)
      await this.fetchUser()
    },

    async fetchUser() {
      const res = await getCurrentUser()
      this.user = res.data
      this.isLoggedIn = true
    },

    async logout() {
      try { await logout() } catch {}
      this.user = null
      this.isLoggedIn = false
    }
  }
})
