// 创建一个路由器，并暴露出去
// 第一步：引入createRouter
import {createRouter, createWebHistory} from 'vue-router'

// 布局与登录页为首屏必需，静态引入
import Index from '@/views/Index.vue'
import Login from '@/views/Login.vue'

// 业务页面按需懒加载（每个页面独立分包，利于首屏与缓存）
const Home = () => import('@/views/Home.vue')
const Screen = () => import('@/views/Screen.vue')
const Visit = () => import('@/views/Visit.vue')
const Room = () => import('@/views/Room.vue')
const Stay = () => import('@/views/Stay.vue')
const ExamAppointment = () => import('@/views/ExamAppointment.vue')
const User = () => import('@/views/User.vue')
const Elder = () => import('@/views/Elder.vue')
const Tag = () => import('@/views/Tag.vue')
const Role = () => import('@/views/Role.vue')
const Permission = () => import('@/views/Permission.vue')
const CareItem = () => import('@/views/CareItem.vue')
const CareLevel = () => import('@/views/CareLevel.vue')
const CarePlan = () => import('@/views/CarePlan.vue')
const CareTask = () => import('@/views/CareTask.vue')
const ExamItem = () => import('@/views/ExamItem.vue')
const ExamPackage = () => import('@/views/ExamPackage.vue')

//创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {path: '/login', component: Login},
        //全屏数据大屏（独立于后台布局）
        {path: '/screen', component: Screen},
        {
            path: '/', component: Index, children: [
                {path: '/home', component: Home},
                {path: '', redirect: '/home'},
                {path: '/visit', component: Visit},
                {path: '/room', component: Room},
                {path: '/stay', component: Stay},
                {path: '/user', component: User},
                {path: '/elder', component: Elder},
                {path: '/tag', component: Tag},
                {path: '/care-item', component: CareItem},
                {path: '/care-level', component: CareLevel},
                {path: '/care-plan', component: CarePlan},
                {path: '/care-task', component: CareTask},
                {path: '/exam-item', component: ExamItem},
                {path: '/exam-package', component: ExamPackage},
                {path: '/exam-appointment', component: ExamAppointment},
                {path: '/role', component: Role},
                {path: '/permission', component: Permission}
            ]
        }
    ]
})

//路由守卫
//全局前置守卫
import {useTokenStore} from '@/store/token.js'
let whiteList = ['/login']; // 白名单
router.beforeEach((to, from) => {
    const tokenStore = useTokenStore()
    const token = tokenStore.token;
    if (!whiteList.includes(to.path) && !token) {
        return '/login'
    }
    if (token && (to.path === '/' || to.path === '')) {
        //登录后默认进入首页看板
        return '/home'
    }
    return true
})

// 暴露出去router
export default router
