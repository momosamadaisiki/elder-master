<script setup>
  import {ref, watch} from "vue";
  import elderApi from "@/api/elder.js";
  import familyApi from "@/api/family.js";
  import {showToast} from "vant";
  import {useRouter} from 'vue-router'
  const router = useRouter()
  import {useTokenStore} from '@/store/token.js'
  import {useElderInfoStore} from '@/store/elderInfo.js'
  import {useFamilyStore} from '@/store/family.js'
  const tokenStore = useTokenStore();
  const elderInfoStore = useElderInfoStore();
  const familyStore = useFamilyStore();

  //登录身份：elder 老人 / family 家属
  const loginType = ref(familyStore.loginType || 'elder')
  watch(loginType, t => familyStore.setLoginType(t))

  const form = ref({
    name: '',
    password: ''
  })

  const loading = ref(false)

  const doLogin = () => {
    if (!form.value.name || !form.value.password) {
      showToast('请输入用户名和密码')
      return
    }
    loading.value = true

    if (loginType.value === 'family') {
      familyApi.login(form.value).then(result => {
        if (result.code == 1) {
          tokenStore.setToken(result.data)
          familyStore.setLoginType('family')
          showToast('登录成功')
          router.push('/family')
        } else {
          showToast(result.msg)
        }
      }).finally(() => { loading.value = false })
      return
    }

    elderApi.login(form.value).then(result => {
      if (result.code == 1) {
        tokenStore.setToken(result.data)
        familyStore.setLoginType('elder')
        //拉取当前登录老人的信息
        return elderApi.elderInfo().then(info => {
          if (info.code == 1) {
            elderInfoStore.setElderInfo(info.data)
          }
          showToast('登录成功')
          router.push('/')
        })
      } else {
        showToast(result.msg)
      }
    }).finally(() => {
      loading.value = false
    })
  }

</script>

<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="logo-circle">
        <span class="logo-icon">❤</span>
      </div>
      <h1 class="app-title">智慧养老社区</h1>
      <p class="app-subtitle">贴心守护 · 安享晚年</p>
    </div>

    <div class="login-form">
      <!-- 登录身份切换 -->
      <div class="type-tabs">
        <div class="type-tab" :class="{active: loginType === 'elder'}" @click="loginType = 'elder'">老人登录</div>
        <div class="type-tab" :class="{active: loginType === 'family'}" @click="loginType = 'family'">家属登录</div>
      </div>

      <van-cell-group inset>
        <van-field
            v-model="form.name"
            :label="loginType === 'family' ? '家属姓名' : '用户名'"
            :placeholder="loginType === 'family' ? '请输入家属姓名' : '请输入老人姓名'"
            left-icon="manager"
            clearable
        />
        <van-field
            v-model="form.password"
            type="password"
            label="密码"
            placeholder="请输入密码"
            left-icon="lock"
        />
      </van-cell-group>
      <div class="login-btn">
        <van-button type="primary" block round :loading="loading" loading-text="登录中..." @click="doLogin">
          登 录
        </van-button>
      </div>
      <p v-if="loginType === 'family'" class="login-tip">家属登录后可查看绑定老人的档案与健康信息（只读）</p>
      <p v-else class="login-tip">账号为老人姓名，密码请联系社区管理员</p>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .login-page {
    min-height: 100vh;
    background-color: #fff;
    display: flex;
    flex-direction: column;
  }

  .login-bg {
    padding: 80px 0 50px;
    text-align: center;
    background: linear-gradient(180deg, #1989fa 0%, #e8f3ff 100%);

    .logo-circle {
      width: 72px;
      height: 72px;
      margin: 0 auto 16px;
      border-radius: 50%;
      background-color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 12px rgba(25, 137, 250, 0.3);

      .logo-icon {
        font-size: 36px;
        color: #1989fa;
      }
    }

    .app-title {
      margin: 0;
      font-size: 24px;
      color: #fff;
      letter-spacing: 2px;
    }

    .app-subtitle {
      margin: 8px 0 0;
      font-size: 13px;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .login-form {
    flex: 1;
    padding: 0 16px 0;

    .type-tabs {
      display: flex;
      margin: 26px 16px 0;

      .type-tab {
        flex: 1;
        padding: 10px 0;
        text-align: center;
        font-size: 15px;
        color: #969799;
        border-bottom: 2px solid transparent;

        &.active {
          color: #1989fa;
          font-weight: bold;
          border-bottom-color: #1989fa;
        }
      }
    }

    .login-btn {
      margin: 32px 16px 0;
    }

    .login-tip {
      margin-top: 20px;
      text-align: center;
      font-size: 12px;
      color: #969799;
    }
  }
</style>
