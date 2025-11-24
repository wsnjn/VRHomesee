import { reactive } from 'vue'

export const userState = reactive({
    user: null,

    setUser(userData) {
        this.user = userData
        localStorage.setItem('user', JSON.stringify(userData))
    },

    clearUser() {
        this.user = null
        localStorage.removeItem('user')
    },

    loadFromStorage() {
        const stored = localStorage.getItem('user')
        if (stored) {
            try {
                this.user = JSON.parse(stored)
            } catch (e) {
                console.error('Failed to parse user data', e)
                this.clearUser()
            }
        }
    },

    get avatarUrl() {
        if (!this.user || !this.user.avatar) {
            return '/src/assets/image/default-avatar.png'
        }
        return `/src/assets/image/${this.user.avatar}`
    }
})
