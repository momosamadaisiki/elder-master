import axios from 'axios'

// const baseURL = 'http://localhost:8080'
const baseURL = '/api/admin'
//这个request和axios具有相同的功能
const request = axios.create({baseURL})

import {useTokenStore} from '@/store/token.js'
import {ElMessage} from "element-plus";
import router from '@/router'

//添加请求拦截器
request.interceptors.request.use(
    (config) => {
        //添加token
        const tokenStore = useTokenStore();
        //判断有没有token
        if (tokenStore.token) {
            config.headers.Authorization = tokenStore.token
        }
        return config;
    },
    (error) => {
        //请求错误的回调
        Promise.reject(error)
    }
)


//添加响应的拦截器
request.interceptors.response.use(
    response => {
        //blob 响应(如 excel 导出)需要读取响应头里的文件名,返回完整 response
        if (response.config.responseType === 'blob') {
            return response;
        }
        //返回result
        return response.data
    },
    error => {
        if (error.response) {
            const data = error.response.data
            const status = error.response.status
            //护工权限性 403：属于“受限接口的正常跳过”，静默处理，避免刷屏
            if (status === 403 && data && data.msg && data.msg.indexOf('护工') >= 0) {
                //仅静默，不弹提示
            } else if (status === 401) {
                ElMessage.error('请先登录')
                router.push('/login')
            } else if (data && data.msg) {
                ElMessage.error(data.msg)
            } else {
                ElMessage.error('服务异常')
            }
        } else {
            ElMessage.error('服务异常')
        }

        return Promise.reject(error);//异步的状态转化成失败的状态
    }
)

export default request