import {useUserInfoStore} from '@/store/userInfo.js'
//判断当前用户是否具有这个按钮权限 user:add
export default function hasBtnPermission(permission) {
    const userInfoStore = useUserInfoStore()
    //获取当前用户所有的按钮权限
    const btnList = userInfoStore.btnList || [];
    //['user:add','user:deleteById']
    //兼容：数据库未配置任何按钮权限(type=2)时不做拦截，避免按钮被全部禁用；
    //一旦角色配置了按钮权限，则按白名单精确控制
    if (btnList.length === 0) {
        return true
    }
    return btnList.indexOf(permission) !== -1
}
