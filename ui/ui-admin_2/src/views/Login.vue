<script setup>
  //定义数据模型
  import {ref} from "vue";
  import {User, Lock} from "@element-plus/icons-vue";
  import userApi from "@/api/user.js";
  import {ElMessage, ElMessageBox} from "element-plus";
  import {useRouter} from 'vue-router'
  const router = useRouter()
  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();

  //记住我：仅记住用户名，下次自动填充
  const remember = ref(!!localStorage.getItem('elder_remember_name'))
  const user = ref({
    name: localStorage.getItem('elder_remember_name') || '',
    password: ''
  })
  const loading = ref(false)
  const formRef = ref()

  const login = () => {
    if (!formRef.value) return
    formRef.value.validate((valid) => {
      if (!valid) return
      loading.value = true
      userApi.login(user.value).then(result => {
        if (result.code == 1) {
          if (remember.value) {
            localStorage.setItem('elder_remember_name', user.value.name)
          } else {
            localStorage.removeItem('elder_remember_name')
          }
          ElMessage.success(result.msg)
          tokenStore.setToken(result.data)
          router.push('/')
        } else {
          ElMessage.error(result.msg)
        }
      }).finally(() => {
        loading.value = false
      })
    })
  }

  const forgotPassword = () => {
    ElMessageBox.alert('请联系系统管理员重置您的账号密码。', '忘记密码', {
      confirmButtonText: '知道了',
      type: 'info'
    })
  }

  //表单校验模型
  const rules = ref({
    name: [
      {required: true, message: '请输入用户名', trigger: 'blur'},
      {min: 2, max: 16, message: '用户名的长度必须为2~16位', trigger: 'blur'}
    ],
    password: [
      {required: true, message: '请输入密码', trigger: 'blur'},
      {min: 3, max: 16, message: '密码长度必须为3~16位', trigger: 'blur'}
    ]
  })

</script>

<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="deco deco-1"></div>
    <div class="deco deco-2"></div>
    <div class="deco deco-3"></div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="login-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" width="30" height="30" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
            <path d="M12 5 9.04 7.96a2.17 2.17 0 0 0 0 3.08c.82.82 2.13.85 3 .07l2.07-1.9a2.82 2.82 0 0 1 3.79 0l2.96 2.66"/>
            <path d="m12 5-3 3.22"/>
          </svg>
        </div>
        <h1 class="brand-title">智慧养老管理系统</h1>
        <p class="brand-subtitle">用心呵护 · 让爱陪伴每一天</p>
      </div>

      <el-form class="login-form" ref="formRef" size="large" autocomplete="off" :model="user" :rules="rules"
               @keyup.enter="login">
        <el-form-item prop="name">
          <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="user.name" clearable autofocus></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input name="password" :prefix-icon="Lock" type="password" placeholder="请输入密码" show-password
                    v-model="user.password"></el-input>
        </el-form-item>
        <el-form-item class="flex">
          <div class="flex options-row">
            <el-checkbox v-model="remember">记住用户名</el-checkbox>
            <el-link type="primary" :underline="false" @click="forgotPassword">忘记密码？</el-link>
          </div>
        </el-form-item>
        <!-- 登录按钮 -->
        <el-form-item>
          <el-button class="login-button" type="primary" auto-insert-space :loading="loading" @click="login">登 录</el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">© 2026 智慧养老 · Elder Care Management System</div>
    </div>
  </div>
</template>

<style scoped>
  .login-page {
    position: relative;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    background:
      radial-gradient(1200px 600px at 15% -10%, rgba(14, 165, 233, 0.16), transparent 60%),
      radial-gradient(900px 500px at 110% 20%, rgba(20, 184, 166, 0.18), transparent 55%),
      linear-gradient(160deg, #f0fdfa 0%, #eff6ff 50%, #f8fafc 100%);
  }

  /* ---- 漂浮装饰圆 ---- */
  .deco {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.55;
    animation: float 9s ease-in-out infinite;
  }
  .deco-1 {
    width: 340px;
    height: 340px;
    background: #99f6e4;
    top: -80px;
    left: -60px;
  }
  .deco-2 {
    width: 300px;
    height: 300px;
    background: #bae6fd;
    bottom: -60px;
    right: -40px;
    animation-delay: -3s;
  }
  .deco-3 {
    width: 160px;
    height: 160px;
    background: #c4b5fd;
    top: 60%;
    left: 12%;
    animation-delay: -6s;
  }
  @keyframes float {
    0%, 100% { transform: translateY(0) scale(1); }
    50% { transform: translateY(-24px) scale(1.05); }
  }

  /* ---- 登录卡片 ---- */
  .login-card {
    position: relative;
    z-index: 1;
    width: 420px;
    padding: 48px 44px 32px;
    background: rgba(255, 255, 255, 0.88);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border: 1px solid rgba(255, 255, 255, 0.9);
    border-radius: 24px;
    box-shadow:
      0 8px 32px rgba(15, 23, 42, 0.08),
      0 24px 64px rgba(15, 23, 42, 0.12);
    animation: rise 0.6s ease both;
  }
  @keyframes rise {
    from { opacity: 0; transform: translateY(24px); }
    to { opacity: 1; transform: translateY(0); }
  }

  /* ---- 品牌区 ---- */
  .login-brand {
    text-align: center;
    margin-bottom: 32px;
  }
  .brand-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
    color: #fff;
    border-radius: 20px;
    background: linear-gradient(135deg, #14b8a6 0%, #0ea5e9 100%);
    box-shadow: 0 8px 20px rgba(20, 184, 166, 0.4);
  }
  .brand-title {
    font-size: 22px;
    font-weight: 700;
    color: #0f172a;
    letter-spacing: 1px;
  }
  .brand-subtitle {
    margin-top: 8px;
    font-size: 13px;
    color: #94a3b8;
    letter-spacing: 2px;
  }

  /* ---- 表单 ---- */
  .login-form :deep(.el-input__wrapper) {
    padding: 4px 14px;
    border-radius: 12px;
    background: #fff;
  }
  .options-row {
    display: flex;
    align-items: center;
    width: 100%;
    justify-content: space-between;
  }

  .login-button {
    width: 100%;
    height: 46px;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 6px;
    border: none;
    border-radius: 12px;
    background: linear-gradient(135deg, #14b8a6 0%, #0ea5e9 100%);
    background-size: 200% 100%;
    transition: all 0.3s ease;
  }
  .login-button:hover {
    background-position: 100% 0;
    box-shadow: 0 8px 24px rgba(20, 184, 166, 0.45);
    transform: translateY(-2px);
  }
  .login-button:active {
    transform: translateY(0);
  }

  /* ---- 底部 ---- */
  .login-footer {
    margin-top: 24px;
    text-align: center;
    font-size: 12px;
    color: #cbd5e1;
    letter-spacing: 0.5px;
  }

  @media (max-width: 480px) {
    .login-card {
      width: calc(100% - 32px);
      padding: 36px 24px 24px;
    }
  }
</style>
