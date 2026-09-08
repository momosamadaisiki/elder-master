<script setup>
  import {computed, markRaw, nextTick, onBeforeUnmount, onMounted, ref} from 'vue'
  import {useRouter} from 'vue-router'
  import {useUserInfoStore} from '@/store/userInfo.js'
  import elderApi from '@/api/elder.js'
  import userApi from '@/api/user.js'
  import careLevelApi from '@/api/careLevel.js'
  import carePlanApi from '@/api/carePlan.js'
  import careTaskApi from '@/api/careTask.js'
  import examPackageApi from '@/api/examPackage.js'
  import statsApi from '@/api/stats.js'
  import * as echarts from 'echarts/core'
  import {BarChart, LineChart, PieChart} from 'echarts/charts'
  import {GridComponent, LegendComponent, TooltipComponent} from 'echarts/components'
  import {CanvasRenderer} from 'echarts/renderers'
  echarts.use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])
  import {
    Notebook,
    User,
    Medal,
    Calendar,
    TrendCharts,
    Present,
    FirstAidKit,
    ArrowRight
  } from '@element-plus/icons-vue'

  const router = useRouter()
  const userInfoStore = useUserInfoStore()

  const loading = ref(true)

  //统计卡片：调用各模块的列表接口（limit=1），取 total 作为总数
  const statCards = ref([
    {label: '在册老人', key: 'elder', icon: markRaw(Notebook), color: '#14b8a6', value: 0, api: () => elderApi.list({page: 1, limit: 1})},
    {label: '员工账号', key: 'user', icon: markRaw(User), color: '#0ea5e9', value: 0, api: () => userApi.list({page: 1, limit: 1})},
    {label: '护理等级', key: 'careLevel', icon: markRaw(Medal), color: '#6366f1', value: 0, api: () => careLevelApi.list({page: 1, limit: 1})},
    {label: '护理计划', key: 'carePlan', icon: markRaw(Calendar), color: '#8b5cf6', value: 0, api: () => carePlanApi.list({page: 1, limit: 1})},
    {label: '护理任务', key: 'careTask', icon: markRaw(TrendCharts), color: '#f59e0b', value: 0, api: () => careTaskApi.list({page: 1, limit: 1})},
    {label: '体检套餐', key: 'examPackage', icon: markRaw(Present), color: '#ec4899', value: 0, api: () => examPackageApi.list({page: 1, limit: 1})}
  ])

  const loadStats = async () => {
    loading.value = true
    const results = await Promise.allSettled(statCards.value.map(card => card.api()))
    results.forEach((result, index) => {
      if (result.status === 'fulfilled') {
        const data = result.value?.data
        statCards.value[index].value = data?.total ?? 0
      } else {
        statCards.value[index].value = '-'
      }
    })
    loading.value = false
  }
  loadStats()

  //快捷入口
  const quickLinks = [
    {name: '老人信息', desc: '登记与维护老人档案', icon: Notebook, path: '/elder'},
    {name: '护理计划', desc: '制定个性化护理计划', icon: Calendar, path: '/care-plan'},
    {name: '护理任务', desc: '跟踪护理执行与打卡', icon: TrendCharts, path: '/care-task'},
    {name: '体检套餐', desc: '维护体检项目与套餐', icon: Present, path: '/exam-package'},
    {name: '用户管理', desc: '账号、角色与权限', icon: User, path: '/user'},
    {name: '标签管理', desc: '老人特征标签维护', icon: FirstAidKit, path: '/tag'}
  ]

  const userName = computed(() => userInfoStore.user?.name || '管理员')

  const greeting = computed(() => {
    const hour = new Date().getHours()
    if (hour < 6) return '夜深了，请注意休息'
    if (hour < 12) return '早上好，元气满满的一天'
    if (hour < 14) return '中午好，稍作休息一下吧'
    if (hour < 18) return '下午好，工作辛苦了'
    return '晚上好，今天辛苦了'
  })

  const today = computed(() => {
    const date = new Date()
    const weekdays = ['日', '一', '二', '三', '四', '五', '六']
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日 星期${weekdays[date.getDay()]}`
  })

  const goTo = (path) => {
    router.push(path)
  }

  //统计卡片点击 → 对应管理页
  const statLinkMap = {
    elder: '/elder',
    user: '/user',
    careLevel: '/care-level',
    carePlan: '/care-plan',
    careTask: '/care-task',
    examPackage: '/exam-package'
  }
  const goToStat = (key) => {
    const path = statLinkMap[key]
    if (path) goTo(path)
  }

  // ==================== 数据统计图表 ====================
  const chartLoading = ref(false)
  const chartData = ref(null)
  const chartStatusEl = ref()
  const chartAgeEl = ref()
  const chartLevelEl = ref()
  const chartTrendEl = ref()

  let chartInstances = []
  const CHART_COLORS = ['#14b8a6', '#0ea5e9', '#6366f1', '#f59e0b', '#ec4899', '#64748b']

  const disposeCharts = () => {
    chartInstances.forEach(c => {
      if (c) c.dispose()
    })
    chartInstances = []
  }
  const makeChart = (el) => {
    if (!el || !el.clientWidth || !el.clientHeight) return null
    const chart = echarts.init(el)
    chartInstances.push(chart)
    return chart
  }
  const onWinResize = () => {
    chartInstances.forEach(c => c.resize())
  }

  //环形图（状态/等级分布）
  const donutOption = (data) => ({
    tooltip: {trigger: 'item', formatter: '{b}：{c}（{d}%）'},
    legend: {bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: {color: '#475569'}},
    color: CHART_COLORS,
    series: [{
      type: 'pie',
      radius: ['38%', '62%'],
      center: ['50%', '42%'],
      avoidLabelOverlap: true,
      itemStyle: {borderRadius: 6, borderColor: '#fff', borderWidth: 2},
      label: {show: false},
      emphasis: {label: {show: true, fontWeight: 600, formatter: '{b}\n{c}'}},
      data
    }]
  })

  //柱状图（年龄分布）
  const barOption = (categories, values) => ({
    tooltip: {trigger: 'axis', axisPointer: {type: 'shadow'}},
    grid: {left: 40, right: 20, top: 20, bottom: 30},
    xAxis: {type: 'category', data: categories, axisTick: {show: false}},
    yAxis: {type: 'value', minInterval: 1, splitLine: {lineStyle: {type: 'dashed'}}},
    series: [{
      type: 'bar',
      data: values,
      barMaxWidth: 34,
      itemStyle: {borderRadius: [6, 6, 0, 0], color: '#14b8a6'}
    }]
  })

  //任务趋势（柱 + 完成率折线）
  const trendOption = (trend) => ({
    tooltip: {trigger: 'axis'},
    legend: {data: ['执行总数', '已完成', '完成率'], icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: {color: '#475569'}},
    grid: {left: 40, right: 52, top: 40, bottom: 30},
    xAxis: {type: 'category', data: trend.dates, axisTick: {show: false}},
    yAxis: [
      {type: 'value', name: '任务数', minInterval: 1, splitLine: {lineStyle: {type: 'dashed'}}},
      {type: 'value', name: '完成率', min: 0, max: 100, axisLabel: {formatter: '{value}%'}, splitLine: {show: false}}
    ],
    series: [
      {
        name: '执行总数', type: 'bar', barMaxWidth: 18, data: trend.total,
        itemStyle: {color: '#cbd5e1', borderRadius: [4, 4, 0, 0]}
      },
      {
        name: '已完成', type: 'bar', barMaxWidth: 18, data: trend.done,
        itemStyle: {color: '#14b8a6', borderRadius: [4, 4, 0, 0]}
      },
      {
        name: '完成率', type: 'line', yAxisIndex: 1, smooth: true, symbol: 'circle',
        data: trend.rate, itemStyle: {color: '#f59e0b'},
        lineStyle: {width: 3}, label: {show: true, formatter: '{c}%', fontSize: 11}
      }
    ]
  })

  const renderCharts = async () => {
    disposeCharts()
    await nextTick()
    const ov = chartData.value
    if (!ov) return

    const statusData = (ov.elderStatus || []).filter(s => s.value > 0)
    const statusChart = makeChart(chartStatusEl.value)
    if (statusChart) {
      statusChart.setOption(donutOption(statusData.length ? statusData : [{name: '暂无数据', value: 1, itemStyle: {color: '#e2e8f0'}}]))
    }

    const age = ov.elderAge || []
    const ageChart = makeChart(chartAgeEl.value)
    if (ageChart) {
      ageChart.setOption(barOption(age.map(a => a.name), age.map(a => a.value)))
    }

    const levelData = (ov.careLevelPlan || []).filter(l => l.value > 0)
    const levelChart = makeChart(chartLevelEl.value)
    if (levelChart) {
      levelChart.setOption(donutOption(levelData.length ? levelData : [{name: '暂无数据', value: 1, itemStyle: {color: '#e2e8f0'}}]))
    }

    const trend = ov.taskTrend
    const trendChart = makeChart(chartTrendEl.value)
    if (trendChart && trend) {
      trendChart.setOption(trendOption(trend))
    }
  }

  const loadCharts = async () => {
    chartLoading.value = true
    try {
      const result = await statsApi.overview()
      if (result.code === 1) {
        chartData.value = result.data
        await renderCharts()
      }
    } catch (e) {
      //统计接口失败不影响其余页面展示
      chartData.value = null
    } finally {
      chartLoading.value = false
    }
  }

  onMounted(() => {
    loadCharts()
    window.addEventListener('resize', onWinResize)
  })
  onBeforeUnmount(() => {
    window.removeEventListener('resize', onWinResize)
    disposeCharts()
  })

</script>

<template>
  <div class="home" v-loading="loading">
    <!-- 顶部问候横幅 -->
    <section class="hero">
      <div class="hero-text">
        <div class="hero-greeting">{{ greeting }}，{{ userName }}</div>
        <div class="hero-date">{{ today }}</div>
        <p class="hero-desc">欢迎使用智慧养老管理系统，愿每一位老人都能被温柔以待。</p>
        <el-button class="screen-btn" size="small" round plain @click="goTo('/screen')">
          <el-icon style="margin-right: 4px"><TrendCharts/></el-icon>数据大屏
        </el-button>
      </div>
      <div class="hero-icon">
        <svg viewBox="0 0 24 24" width="72" height="72" fill="none" stroke="currentColor" stroke-width="1.6"
             stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"/>
          <path d="M12 5 9.04 7.96a2.17 2.17 0 0 0 0 3.08c.82.82 2.13.85 3 .07l2.07-1.9a2.82 2.82 0 0 1 3.79 0l2.96 2.66"/>
          <path d="m12 5-3 3.22"/>
        </svg>
      </div>
    </section>

    <!-- 数据统计 -->
    <section class="stat-grid">
      <div
          v-for="card in statCards"
          :key="card.key"
          class="stat-card"
          @click="goToStat(card.key)"
      >
        <div class="stat-icon" :style="{ background: `linear-gradient(135deg, ${card.color}22 0%, ${card.color}11 100%)`, color: card.color }">
          <el-icon :size="26"><component :is="card.icon"/></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-panel">
      <div class="quick-title">
        <span>快捷入口</span>
      </div>
      <div class="quick-grid">
        <div
            v-for="link in quickLinks"
            :key="link.path"
            class="quick-card"
            @click="goTo(link.path)"
        >
          <el-icon class="quick-icon"><component :is="link.icon"/></el-icon>
          <div class="quick-text">
            <div class="quick-name">{{ link.name }}</div>
            <div class="quick-desc">{{ link.desc }}</div>
          </div>
          <el-icon class="quick-arrow"><ArrowRight/></el-icon>
        </div>
      </div>
    </section>

    <!-- 数据可视化 -->
    <section class="charts-panel">
      <div class="quick-title">
        <span>数据可视化</span>
      </div>
      <div v-loading="chartLoading" class="charts-grid">
        <div class="chart-card">
          <div class="chart-card-title">老人状态分布</div>
          <div ref="chartStatusEl" class="chart-box"></div>
        </div>
        <div class="chart-card">
          <div class="chart-card-title">老人年龄分布</div>
          <div ref="chartAgeEl" class="chart-box"></div>
        </div>
        <div class="chart-card">
          <div class="chart-card-title">护理计划等级分布</div>
          <div ref="chartLevelEl" class="chart-box"></div>
        </div>
        <div class="chart-card chart-card-wide">
          <div class="chart-card-title">护理任务完成趋势（近 7 天）</div>
          <div ref="chartTrendEl" class="chart-box"></div>
        </div>
      </div>
    </section>
  </div>
</template>

<style lang="scss" scoped>
  .home {
    display: flex;
    flex-direction: column;
    gap: 20px;
    min-height: 100%;
  }

  /* ============ 问候横幅 ============ */
  .hero {
    position: relative;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30px 36px;
    border-radius: 16px;
    color: #fff;
    background:
      radial-gradient(560px 260px at 88% -30%, rgba(255, 255, 255, 0.22), transparent 60%),
      linear-gradient(120deg, #0f766e 0%, #0e7490 55%, #155e75 100%);

    .hero-text {
      position: relative;
      z-index: 1;
    }

    .hero-greeting {
      font-size: 24px;
      font-weight: 700;
      letter-spacing: 1px;
    }

    .hero-date {
      margin-top: 10px;
      font-size: 14px;
      opacity: 0.85;
      letter-spacing: 1px;
    }

    .hero-desc {
      margin-top: 16px;
      font-size: 13px;
      opacity: 0.75;
      letter-spacing: 0.5px;
    }

    .hero-icon {
      position: relative;
      z-index: 1;
      flex-shrink: 0;
      opacity: 0.9;
      color: rgba(255, 255, 255, 0.9);
      filter: drop-shadow(0 4px 12px rgba(255, 255, 255, 0.18));
    }
  }

  /* ============ 数据统计 ============ */
  .stat-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }

  .stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 22px;
    background: #fff;
    border: 1px solid var(--el-border-color-light);
    border-radius: 14px;
    box-shadow: var(--app-card-shadow);
    cursor: pointer;
    transition: all 0.25s ease;

    &:hover {
      transform: translateY(-3px);
      box-shadow: var(--app-card-shadow-hover);
    }

    .stat-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 52px;
      height: 52px;
      flex-shrink: 0;
      border-radius: 14px;
    }

    .stat-value {
      font-size: 26px;
      font-weight: 700;
      color: #0f172a;
      line-height: 1.2;
      font-variant-numeric: tabular-nums;
    }

    .stat-label {
      margin-top: 4px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  /* ============ 快捷入口 ============ */
  .quick-panel {
    padding: 24px;
    background: #fff;
    border: 1px solid var(--el-border-color-light);
    border-radius: 14px;
    box-shadow: var(--app-card-shadow);
  }

  .quick-title {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 18px;
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;

    &::before {
      content: '';
      width: 4px;
      height: 16px;
      border-radius: 2px;
      background: linear-gradient(180deg, #14b8a6 0%, #0ea5e9 100%);
    }
  }

  .quick-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 14px;
  }

  .quick-card {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 18px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 12px;
    background: #fbfdfe;
    cursor: pointer;
    transition: all 0.22s ease;

    &:hover {
      border-color: rgba(20, 184, 166, 0.5);
      background: #f0fdfa;
      transform: translateY(-2px);

      .quick-arrow {
        opacity: 1;
        transform: translateX(2px);
      }
    }

    .quick-icon {
      flex-shrink: 0;
      font-size: 22px;
      color: var(--el-color-primary);
      background: var(--el-color-primary-light-9);
      border-radius: 10px;
      padding: 8px;
    }

    .quick-text {
      flex: 1;
      min-width: 0;
    }

    .quick-name {
      font-size: 15px;
      font-weight: 600;
      color: #1e293b;
    }

    .quick-desc {
      margin-top: 3px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .quick-arrow {
      color: var(--el-color-primary);
      opacity: 0;
      transition: all 0.22s ease;
    }
  }

  /* ============ 数据可视化 ============ */
  .charts-panel {
    padding: 24px;
    background: #fff;
    border: 1px solid var(--el-border-color-light);
    border-radius: 14px;
    box-shadow: var(--app-card-shadow);
  }

  .charts-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 16px;
  }

  .chart-card {
    min-width: 0;
    padding: 14px;
    background: #fbfdfe;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 12px;
  }

  .chart-card-wide {
    grid-column: 1 / -1;
  }

  .chart-card-title {
    margin-bottom: 10px;
    padding-left: 8px;
    border-left: 3px solid #14b8a6;
    font-size: 14px;
    font-weight: 600;
    color: #334155;
    line-height: 1;
  }

  .chart-box {
    width: 100%;
    height: 300px;
  }

  /* 首页大屏入口 */
  .screen-btn {
    margin-top: 16px;
    color: #fff;
    border-color: rgba(255, 255, 255, 0.65);
    background: rgba(255, 255, 255, 0.14);

    &:hover {
      color: #fff;
      border-color: #fff;
      background: rgba(255, 255, 255, 0.24);
    }
  }

  @media (max-width: 900px) {
    .charts-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
