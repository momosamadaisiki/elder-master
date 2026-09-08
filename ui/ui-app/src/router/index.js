// 创建一个路由器，并暴露出去
// 第一步：引入createRouter
import {createRouter, createWebHistory} from 'vue-router'
// 引入一个一个可能要呈现组件
import Login from '@/views/Login.vue'
import Layout from '@/views/Layout.vue'
import Home from '@/views/Home.vue'
import ExamPackage from '@/views/ExamPackage.vue'
import PackageDetail from '@/views/PackageDetail.vue'
import AppointmentForm from '@/views/AppointmentForm.vue'
import MyAppointment from '@/views/MyAppointment.vue'
import Profile from '@/views/Profile.vue'
import AiAssistant from '@/views/AiAssistant.vue'
import Family from '@/views/Family.vue'
import Report from '@/views/Report.vue'

//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {path: '/login', component: Login},
        {path: '/family', component: Family},
        {
            path: '/', component: Layout, children: [
                {path: '', redirect: '/home'},
                {path: '/home', component: Home},
                {path: '/ai', component: AiAssistant},
                {path: '/package', component: ExamPackage},
                {path: '/appointment', component: MyAppointment},
                {path: '/profile', component: Profile}
            ]
        },
        //独立页面（不在底部Tabbar内）
        {path: '/package/:id', component: PackageDetail},
        {path: '/appointment-form/:packageId', component: AppointmentForm},
        {path: '/report/:id', component: Report}
    ]
})

//路由守卫
//全局前置守卫
import {useTokenStore} from '@/store/token.js'
import {useFamilyStore} from '@/store/family.js'
let whiteList = ['/login']; // 白名单
router.beforeEach((to) => {
    const tokenStore = useTokenStore()
    const familyStore = useFamilyStore()
    const token = tokenStore.token;
    if (!whiteList.includes(to.path) && !token) {
        return '/login'
    }
    //家属账号只能在 /family 家属页，老人账号不能进家属页
    const isFamily = familyStore.loginType === 'family'
    if (token && isFamily && to.path !== '/family' && to.path !== '/login') {
        return '/family'
    }
    if (token && !isFamily && to.path === '/family') {
        return '/home'
    }
})

// 暴露出去router
export default router
