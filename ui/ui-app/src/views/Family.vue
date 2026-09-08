<script setup>
  import {computed, onMounted, ref} from 'vue'
  import {showConfirmDialog, showToast} from 'vant'
  import {useRouter} from 'vue-router'
  import familyApi from '@/api/family.js'
  import {useTokenStore} from '@/store/token.js'
  import {useFamilyStore} from '@/store/family.js'

  const router = useRouter()
  const tokenStore = useTokenStore()
  const familyStore = useFamilyStore()

  const loading = ref(false)
  const elder = ref(null)

  const loadElder = async () => {
    loading.value = true
    try {
      const result = await familyApi.me()
      if (result.code === 1) {
        elder.value = result.data
        familyStore.setFamily(result.data)
      } else {
        showToast(result.msg || '加载失败')
      }
    } catch (e) {
      showToast('加载失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
  onMounted(loadElder)

  const statusText = (s) => ['禁用', '启用', '请假', '退住中', '入住中', '已退住'][s] || '未知'
  const statusColor = (s) => {
    const map = {4: '#07c160', 2: '#ff976a', 5: '#969799', 1: '#1989fa'}
    return map[s] || '#969799'
  }

  const greetingName = computed(() => familyStore.family?.name || '')

  const logout = () => {
    showConfirmDialog({title: '退出登录', message: '确定退出当前家属账号？'}).then(() => {
      tokenStore.removeToken()
      familyStore.clearFamily()
      router.replace('/login')
    }).catch(() => {})
  }
</script>

<template>
  <div class="family-page">
    <!--顶部-->
    <div class="f-header">
      <div class="hello">你好，{{ greetingName || '家属' }}</div>
      <div class="sub">正在查看老人档案与健康信息（只读）</div>
      <van-button size="small" round plain class="logout-btn" @click="logout">退出</van-button>
    </div>

    <div class="f-body">
      <div v-if="loading" class="loading-wrap"><van-loading size="22" color="#1989fa">加载中…</van-loading></div>
      <!--老人档案卡-->
      <div v-if="elder" class="card">
        <div class="card-title">老人档案</div>
        <div class="elder-row">
          <div class="elder-name">{{ elder.name }}</div>
          <span class="status-dot" :style="{background: statusColor(elder.status)}"></span>
          <span class="status-text" :style="{color: statusColor(elder.status)}">{{ statusText(elder.status) }}</span>
        </div>
        <div class="info-grid">
          <div class="info-item"><span class="label">出生日期</span><span class="value">{{ elder.birthday || '—' }}</span></div>
          <div class="info-item"><span class="label">住址</span><span class="value">{{ elder.address || '—' }}</span></div>
          <div class="info-item full"><span class="label">特征标签</span><span class="value">{{ elder.tagNames || '无' }}</span></div>
          <div class="info-item full"><span class="label">备注</span><span class="value">{{ elder.remark || '—' }}</span></div>
        </div>
      </div>

      <!--最近护理记录-->
      <div v-if="elder && elder.careTasks && elder.careTasks.length" class="card">
        <div class="card-title">最近护理记录</div>
        <div class="task-list">
          <div v-for="(t, i) in elder.careTasks" :key="i" class="task-item">
            <div class="task-main">
              <span class="task-date">{{ t.date }}</span>
              <span class="task-item-name">{{ t.item }}</span>
              <van-tag :type="t.status === '已完成' ? 'success' : 'warning'" plain size="small">{{ t.status }}</van-tag>
            </div>
            <div v-if="t.result" class="task-result">结果：{{ t.result }}</div>
            <div v-if="t.remark" class="task-remark">{{ t.remark }}</div>
          </div>
        </div>
      </div>
      <van-empty v-else-if="elder" description="暂无护理记录"/>

      <div class="readonly-tip">提示：家属端仅可查看，如需预约体检/探视请联系老人或管理员。</div>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .family-page {
    min-height: 100vh;
    background: #f5f6f8;
  }

  .f-header {
    position: relative;
    padding: 24px 16px 30px;
    color: #fff;
    background: linear-gradient(120deg, #1989fa 0%, #4facfe 100%);

    .hello { font-size: 20px; font-weight: bold; }
    .sub { margin-top: 6px; font-size: 12px; opacity: 0.9; }

    .logout-btn {
      position: absolute;
      top: 22px;
      right: 14px;
      color: #fff;
      border-color: rgba(255, 255, 255, 0.7);
      background: rgba(255, 255, 255, 0.12);
    }
  }

  .f-body {
    padding: 14px 12px 20px;
  }

  .loading-wrap {
    padding: 50px 0;
    display: flex;
    justify-content: center;
  }

  .card {
    padding: 14px;
    margin-bottom: 12px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);

    .card-title {
      font-size: 15px;
      font-weight: bold;
      color: #323233;
      margin-bottom: 12px;
      padding-left: 8px;
      border-left: 3px solid #1989fa;
    }
  }

  .elder-row {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 10px;

    .elder-name { font-size: 20px; font-weight: bold; margin-right: 4px; }
    .status-dot { width: 8px; height: 8px; border-radius: 50%; }
    .status-text { font-size: 12px; }
  }

  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px 14px;

    .full { grid-column: 1 / -1; }
    .info-item {
      .label { color: #969799; font-size: 12px; margin-right: 6px; }
      .value { color: #323233; font-size: 13px; word-break: break-all; }
    }
  }

  .task-list {
    .task-item {
      padding: 10px 0;
      border-bottom: 1px solid #f2f3f5;

      &:last-child { border-bottom: none; }

      .task-main { display: flex; align-items: center; gap: 8px; }
      .task-date { font-size: 12px; color: #969799; }
      .task-item-name { font-size: 14px; font-weight: 600; color: #323233; flex: 1; }
      .task-result { margin-top: 4px; font-size: 13px; color: #576b95; }
      .task-remark { margin-top: 2px; font-size: 12px; color: #969799; }
    }
  }

  .readonly-tip {
    margin-top: 6px;
    font-size: 12px;
    color: #b6b7b9;
    text-align: center;
  }
</style>
