import {defineStore} from 'pinia'

//登录身份与家属信息（登录身份：elder 老人 / family 家属）
export const useFamilyStore = defineStore('family', {
    state() {
        return {
            loginType: 'elder',
            family: {}
        }
    },
    actions: {
        setLoginType(type) {
            this.loginType = type || 'elder'
        },
        setFamily(data) {
            this.family = data || {}
            this.loginType = 'family'
        },
        clearFamily() {
            this.family = {}
            this.loginType = 'elder'
        }
    },
    persist: {
        enabled: true
    }
})
