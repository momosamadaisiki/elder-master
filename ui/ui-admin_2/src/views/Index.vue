<script setup>
  import {
    User,
    Crop,
    EditPen,
    SwitchButton,
    CaretBottom,
    Fold,
    Expand
  } from '@element-plus/icons-vue'
  import avatar from '@/assets/default.png'
  //条目被点击后,调用的函数
  import {useRouter, useRoute} from 'vue-router'
  const router = useRouter();
  const route = useRoute();
  import userApi from "@/api/user.js";
  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();
  import {useUserInfoStore} from '@/store/userInfo.js'
  import {ref, computed} from "vue";
  import {ElMessage} from "element-plus";
  const userInfoStore = useUserInfoStore();

  const dialogFormVisible = ref(false)
  const user = ref({})

  //侧边栏折叠
  const isCollapse = ref(false)
  const asideWidth = computed(() => isCollapse.value ? '64px' : '220px')

  //点击品牌区回到首页看板
  const goHome = () => {
    router.push('/home')
  }

  const handleCommand = (command) => {
    //判断指令
    if (command === 'logout') {
      //退出登录
      tokenStore.removeToken();
      router.push('/login')
    } else if (command === 'updateUserInfo') {
      dialogFormVisible.value = true
      //user.value = userInfoStore.user
      Object.assign(user.value, userInfoStore.user)
    } else if (command === 'resetPassword'){
      dialogResetPasswordDialog.value = true
      userPasswordDTO.value = {}
      //resetForm.value.resetFields()
    } else if (command === 'avatar') {
      //更换头像：与“基本资料”共用弹窗（内含头像上传）
      dialogFormVisible.value = true
      Object.assign(user.value, userInfoStore.user)
    } else {
      //路由
      router.push('/user/' + command)
    }
  }

  //获取用户信息
  const getUserInfo = () => {
    userApi.userInfo().then(result => {
      if (result.code == 1) {
        userInfoStore.setUserInfo(result.data.user)
        menuData.value = result.data.routerList
        userInfoStore.setBtnList(result.data.btnList)
        userInfoStore.setRoleCodes(result.data.roleCodes)
        //护工登录后直接进入护理任务页（首页统计等其它模块对护工不可见）
        if ((result.data.roleCodes || []).includes('hugong') && route.path !== '/care-task') {
          router.replace('/care-task')
        }
      }
    })
  }
  getUserInfo()

  //当前日期问候语
  const greeting = computed(() => {
    const hour = new Date().getHours()
    if (hour < 6) return '夜深了'
    if (hour < 12) return '早上好'
    if (hour < 14) return '中午好'
    if (hour < 18) return '下午好'
    return '晚上好'
  })
  const today = new Date().toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
  })

  //上传头像
  const handleAvatarSuccess = (result) => {
    user.value.avatar = result.data
  }

  const updateUserInfo = () => {
    userApi.update(user.value.id, user.value).then(result => {
      if (result.code == 1) {
        ElMessage.success(result.msg)
        dialogFormVisible.value = false
        getUserInfo()
      }
    })
  }

  //重置密码
  const userPasswordDTO = ref({
    'oldPassword': '',
    'newPassword': ''
  });
  const dialogResetPasswordDialog = ref(false)

  //自定义确认密码的校验函数
  const rePasswordValid = (rule, value, callback) => {
    if (value == null || value === '') {
      return callback(new Error('请再次确认密码'))
    }
    //响应式对象要：registerData.value才能拿到值
    if (userPasswordDTO.value.newPassword !== value) {
      return callback(new Error('两次输入密码不一致'))
    }

    callback()
  }

  const rules = ref({
    oldPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ],
    newPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ],
    reNewPassword: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {validator: rePasswordValid, trigger: 'blur' }
    ]
  })
  const resetForm = ref()
  const resetPassword = async (formEl) => {
    if (!formEl) return
    await formEl.validate((valid, fields) => {
      if (valid) {
        userApi.resetPassword(userPasswordDTO.value).then(result => {
          if (result.code === 1) {
            ElMessage.success(result.msg)
            dialogResetPasswordDialog.value = false
            tokenStore.removeToken();
            userInfoStore.removeUserInfo();
            // 跳转到登录
            router.push('/login')
          } else {
            ElMessage.error(result.msg)
          }
        })
      } else {
        ElMessage.error('表单验证失败');
      }
    })
  }

  // 菜单（登录后由后端权限接口动态返回）
  const menuData = ref([
    {name: '老人信息', icon: 'Notebook', path: "/elder"},
    {name: '标签管理', icon: 'TrendCharts', path: "/tag"},
    {name: '护理项目管理', icon: 'FirstAidKit', path: "/care-item"},
    {name: '护理等级管理', icon: 'Medal', path: "/care-level"},
    {name: '体检项目管理', icon: 'DataAnalysis', path: "/exam-item"},
    {name: '体检套餐管理', icon: 'Present', path: "/exam-package"},
    {
      name: '权限管理', icon: 'GobletFull', children: [
        {name: '管理员管理', icon: 'GobletSquareFull', path: "/user"},
        {name: '角色管理', icon: 'TrendCharts', path: "/role"},
        {name: '权限管理', icon: 'TrendCharts', path: "/permission"},
      ]
    }
  ]);

</script>

<template>
  <!-- element-plus中的容器 -->
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside :width="asideWidth" class="layout-aside">
      <div class="aside-brand" v-show="!isCollapse" @click="goHome">
        <div class="brand-logo">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
            <path d="M12 5 9.04 7.96a2.17 2.17 0 0 0 0 3.08c.82.82 2.13.85 3 .07l2.07-1.9a2.82 2.82 0 0 1 3.79 0l2.96 2.66"/>
            <path d="m12 5-3 3.22"/>
          </svg>
        </div>
        <div class="brand-text">
          <span class="brand-name">智慧养老</span>
          <span class="brand-desc">管理系统</span>
        </div>
      </div>
      <div class="aside-brand collapsed" v-show="isCollapse" @click="goHome">
        <div class="brand-logo">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
            <path d="M12 5 9.04 7.96a2.17 2.17 0 0 0 0 3.08c.82.82 2.13.85 3 .07l2.07-1.9a2.82 2.82 0 0 1 3.79 0l2.96 2.66"/>
            <path d="m12 5-3 3.22"/>
          </svg>
        </div>
      </div>

      <!-- element-plus的菜单标签 -->
      <el-scrollbar class="menu-scrollbar">
        <el-menu class="layout-menu" :collapse="isCollapse" :collapse-transition="false" router
                 :default-active="route.path">
          <!-- 动态生成菜单 -->
          <template v-for="(menu, index) in menuData" :index="index.toString()">
            <el-sub-menu v-if="menu.children?.length>0" :index="menu.name">
              <template #title>
                <el-icon class="menu-icon">
                  <component :is="menu.icon"></component>
                </el-icon>
                <span>{{ menu.name }}</span>
              </template>
              <el-menu-item v-for="(child, ind) in menu.children" :index="child.path">
                <el-icon class="menu-icon"><component :is="child.icon"></component></el-icon>
                <span>{{ child.name }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.path">
              <el-icon class="menu-icon"><component :is="menu.icon"></component></el-icon>
              <template #title>
                <span>{{ menu.name }}</span>
              </template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container class="layout-main-container">
      <!-- 头部区域 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <component :is="isCollapse ? Expand : Fold"/>
          </el-icon>
          <div class="welcome">
            <span class="welcome-text">{{ greeting }}，{{ userInfoStore.user.name || '管理员' }}</span>
            <span class="welcome-date">{{ today }}</span>
          </div>
        </div>

        <!-- 下拉菜单 -->
        <!-- command: 条目被点击后会触发,在事件函数上可以声明一个参数,接收条目对应的指令 -->
        <el-dropdown placement="bottom-end" @command="handleCommand">
          <span class="el-dropdown__box">
            <el-avatar :size="38" :src="userInfoStore.user.avatar?userInfoStore.user.avatar:avatar"/>
            <div class="user-info">
              <span class="user-name">{{ userInfoStore.user.name || '用户' }}</span>
              <span class="user-role">管理员</span>
            </div>
            <el-icon class="caret">
              <CaretBottom/>
            </el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="updateUserInfo" :icon="User">基本资料</el-dropdown-item>
              <el-dropdown-item command="avatar" :icon="Crop">更换头像</el-dropdown-item>
              <el-dropdown-item command="resetPassword" :icon="EditPen">重置密码</el-dropdown-item>
              <el-dropdown-item command="logout" :icon="SwitchButton" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 中间区域 -->
      <el-main class="layout-main">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="dialogFormVisible" title="修改个人信息" width="500" :lock-scroll="false">
    <el-form :model="user">
      <el-form-item label="名字" :label-width="60">
        <el-input v-model="user.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="邮箱" :label-width="60">
        <el-input v-model="user.email" autocomplete="off" />
      </el-form-item>
      <el-form-item label="手机号" :label-width="60">
        <el-input v-model="user.phone" autocomplete="off" />
      </el-form-item>
      <el-form-item label="头像" :label-width="60">
        <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="{Authorization: tokenStore.token}">
          <img v-if="user.avatar" :src="user.avatar" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="updateUserInfo">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog  v-model="dialogResetPasswordDialog" title="重置密码" width="500" :lock-scroll="false">
    <el-form ref="resetForm" :rules="rules" :model="userPasswordDTO">
      <el-form-item prop="oldPassword" label="原密码" :label-width="100">
        <el-input v-model="userPasswordDTO.oldPassword" autocomplete="off"/>
      </el-form-item>
      <el-form-item prop="newPassword" label="新密码" :label-width="100">
        <el-input v-model="userPasswordDTO.newPassword" autocomplete="off"/>
      </el-form-item>
      <el-form-item prop="reNewPassword" label="重复新密码" :label-width="100">
        <el-input v-model="userPasswordDTO.reNewPassword" autocomplete="off"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogResetPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="resetPassword(resetForm)">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
  .layout-container {
    height: 100vh;
  }

  /* ===================== 侧边栏 ===================== */
  .layout-aside {
    background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
    display: flex;
    flex-direction: column;
    transition: width 0.3s ease;
    overflow: hidden;
  }

  .aside-brand {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    height: 72px;
    padding: 0 18px;
    flex-shrink: 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .brand-logo {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      flex-shrink: 0;
      color: #fff;
      border-radius: 12px;
      background: linear-gradient(135deg, #14b8a6 0%, #0ea5e9 100%);
      box-shadow: 0 4px 12px rgba(20, 184, 166, 0.35);
    }

    .brand-text {
      display: flex;
      flex-direction: column;
      line-height: 1.3;
      white-space: nowrap;

      .brand-name {
        font-size: 17px;
        font-weight: 700;
        color: #fff;
        letter-spacing: 2px;
      }
      .brand-desc {
        font-size: 12px;
        color: rgba(148, 163, 184, 0.9);
        letter-spacing: 4px;
      }
    }

    &.collapsed {
      justify-content: center;
      padding: 0;
    }
  }

  .menu-scrollbar {
    flex: 1;

    :deep(.el-scrollbar__view) {
      padding: 8px;
    }
  }

  /* 菜单样式 */
  .layout-menu {
    border-right: none;
    background: transparent;

    --el-menu-bg-color: transparent;
    --el-menu-text-color: #94a3b8;
    --el-menu-hover-bg-color: rgba(255, 255, 255, 0.06);
    --el-menu-active-color: #2dd4bf;
    --el-menu-item-height: 46px;
    --el-menu-sub-item-height: 44px;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      position: relative;
      border-radius: 10px;
      margin: 3px 0;
      transition: all 0.2s ease;

      &:hover {
        color: #e2e8f0;
      }
    }

    :deep(.el-menu-item.is-active) {
      background: linear-gradient(135deg, rgba(20, 184, 166, 0.22) 0%, rgba(14, 165, 233, 0.16) 100%);
      color: #2dd4bf;
      font-weight: 600;
      box-shadow: inset 0 0 0 1px rgba(45, 212, 191, 0.25);

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 20px;
        border-radius: 2px;
        background: #2dd4bf;
      }
    }

    :deep(.el-sub-menu .el-menu) {
      background: rgba(0, 0, 0, 0.18);
      border-radius: 10px;
      margin: 2px 0 6px;
    }

    .menu-icon {
      font-size: 17px;
      margin-right: 4px;
    }
  }

  /* ===================== 头部 ===================== */
  .layout-header {
    height: 72px;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border-bottom: 1px solid #eef2f7;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    z-index: 10;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 18px;

    .collapse-btn {
      font-size: 20px;
      color: #475569;
      cursor: pointer;
      padding: 6px;
      border-radius: 8px;
      transition: all 0.2s ease;

      &:hover {
        background: #f1f5f9;
        color: #14b8a6;
      }
    }

    .welcome {
      display: flex;
      flex-direction: column;
      line-height: 1.4;

      .welcome-text {
        font-size: 15px;
        font-weight: 600;
        color: #1e293b;
      }
      .welcome-date {
        font-size: 12px;
        color: #94a3b8;
      }
    }
  }

  .el-dropdown__box {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 6px 12px;
    border-radius: 12px;
    cursor: pointer;
    transition: background 0.2s ease;

    &:hover {
      background: #f1f5f9;
    }

    .user-info {
      display: flex;
      flex-direction: column;
      line-height: 1.3;

      .user-name {
        font-size: 14px;
        font-weight: 600;
        color: #1e293b;
      }
      .user-role {
        font-size: 12px;
        color: #94a3b8;
      }
    }

    .caret {
      color: #94a3b8;
      font-size: 12px;
    }

    &:active,
    &:focus {
      outline: none;
    }
  }

  /* ===================== 主内容区 ===================== */
  .layout-main-container {
    min-width: 0; // 防止内容撑开布局
  }

  .layout-main {
    background: #f1f5f9;
    padding: 20px;
    overflow-y: auto;
  }

  /* 窄屏：隐藏次要信息，保持头部清爽 */
  @media (max-width: 900px) {
    .welcome-date {
      display: none;
    }

    .user-info {
      display: none;
    }
  }

  /* 路由切换：直接切换（懒加载页面不做 out-in 过渡，避免首进偶发空白） */

  /* ===================== 头像上传 ===================== */
  .avatar-uploader .avatar {
    width: 178px;
    height: 178px;
    display: block;
    border-radius: 12px;
  }

  .avatar-uploader .el-upload {
    border: 1px dashed var(--el-border-color);
    border-radius: 12px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
  }

  .avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
  }

  .el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
  }
</style>
