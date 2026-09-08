<script setup>
  import {onBeforeUnmount, onMounted, ref} from 'vue'
  import {useRouter} from 'vue-router'
  import statsApi from '@/api/stats.js'
  import * as echarts from 'echarts/core'
  import {BarChart, LineChart, PieChart} from 'echarts/charts'
  import {GridComponent, LegendComponent, TooltipComponent} from 'echarts/components'
  import {CanvasRenderer} from 'echarts/renderers'
  echarts.use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

  const router = useRouter()
  const data = ref(null)
  const refreshTime = ref('')
  let timer = null
  let instances = []
  const chartStatusEl = ref()
  const chartAgeEl = ref()
  const chartLevelEl = ref()
  const chartTrendEl = ref()

  const disposeAll = () => {
    instances.forEach(c => c && c.dispose())
    instances = []
  }
  const onResize = () => instances.forEach(c => c && c.resize())
  const make = (el) => {
    if (!el || !el.clientWidth) return null
    const c = echarts.init(el)
    instances.push(c)
    return c
  }
  const now = () => new Date().toLocaleString('zh-CN', {hour12: false})

  const load = async () => {
    try {
      const result = await statsApi.screen()
      if (result.code === 1) {
        data.value = result.data
        render()
        refreshTime.value = now()
      }
    } catch (e) {
      refreshTime.value = '加载失败，等待自动重试'
    }
  }

  const PALETTE = ['#22d3ee', '#34d399', '#a78bfa', '#fbbf24', '#f472b6', '#64748b']

  const render = () => {
    disposeAll()
    const d = data.value
    if (!d) return
    //donut
    const statusDonut = (raw, fallbackName) => {
      const list = (raw || []).filter(x => x.value > 0)
      return list.length ? list : [{name: '暂无数据', value: 1, itemStyle: {color: '#1e293b'}}]
    }
    const c1 = make(chartStatusEl.value)
    if (c1) {
      c1.setOption({
        tooltip: {trigger: 'item', formatter: '{b}：{c}（{d}%）'},
        legend: {bottom: 0, textStyle: {color: '#cbd5e1'}, icon: 'circle', itemWidth: 8, itemHeight: 8},
        color: PALETTE,
        series: [{
          type: 'pie', radius: ['42%', '66%'], center: ['50%', '44%'],
          itemStyle: {borderColor: '#0f172a', borderWidth: 3},
          label: {color: '#e2e8f0'},
          data: statusDonut(d.elderStatus)
        }]
      })
    }
    const c2 = make(chartAgeEl.value)
    if (c2) {
      const age = d.elderAge || []
      c2.setOption({
        tooltip: {trigger: 'axis'},
        grid: {left: 36, right: 16, top: 20, bottom: 26},
        xAxis: {type: 'category', data: age.map(a => a.name), axisLabel: {color: '#cbd5e1'}, axisLine: {lineStyle: {color: '#334155'}}},
        yAxis: {type: 'value', minInterval: 1, axisLabel: {color: '#cbd5e1'}, splitLine: {lineStyle: {color: 'rgba(148,163,184,.15)'}}},
        series: [{
          type: 'bar', data: age.map(a => a.value), barMaxWidth: 30,
          itemStyle: {color: '#22d3ee', borderRadius: [4, 4, 0, 0]}
        }]
      })
    }
    const c3 = make(chartLevelEl.value)
    if (c3) {
      const list = (d.careLevelPlan || []).filter(x => x.value > 0)
      c3.setOption({
        tooltip: {trigger: 'item', formatter: '{b}：{c}（{d}%）'},
        legend: {bottom: 0, textStyle: {color: '#cbd5e1'}, icon: 'circle', itemWidth: 8, itemHeight: 8},
        color: ['#a78bfa', '#fbbf24', '#34d399', '#f472b6', '#22d3ee'],
        series: [{
          type: 'pie', radius: ['42%', '66%'], center: ['50%', '44%'],
          itemStyle: {borderColor: '#0f172a', borderWidth: 3},
          data: list.length ? list : [{name: '暂无数据', value: 1, itemStyle: {color: '#1e293b'}}]
        }]
      })
    }
    const c4 = make(chartTrendEl.value)
    if (c4) {
      const t = d.taskTrend || {dates: [], total: [], done: [], rate: []}
      c4.setOption({
        tooltip: {trigger: 'axis'},
        legend: {data: ['任务数', '已完成', '完成率%'], textStyle: {color: '#cbd5e1'}},
        grid: {left: 40, right: 50, top: 34, bottom: 26},
        xAxis: {type: 'category', data: t.dates, axisLabel: {color: '#cbd5e1'}, axisLine: {lineStyle: {color: '#334155'}}},
        yAxis: [
          {type: 'value', minInterval: 1, axisLabel: {color: '#cbd5e1'}, splitLine: {lineStyle: {color: 'rgba(148,163,184,.15)'}}},
          {type: 'value', min: 0, max: 100, axisLabel: {color: '#cbd5e1', formatter: '{value}%'}, splitLine: {show: false}}
        ],
        series: [
          {name: '任务数', type: 'bar', barMaxWidth: 18, data: t.total, itemStyle: {color: '#475569', borderRadius: [3, 3, 0, 0]}},
          {name: '已完成', type: 'bar', barMaxWidth: 18, data: t.done, itemStyle: {color: '#34d399', borderRadius: [3, 3, 0, 0]}},
          {name: '完成率%', type: 'line', yAxisIndex: 1, smooth: true, data: t.rate, itemStyle: {color: '#fbbf24'}, lineStyle: {width: 3}}
        ]
      })
    }
  }

  onMounted(() => {
    load()
    timer = setInterval(load, 30000)
    window.addEventListener('resize', onResize)
  })
  onBeforeUnmount(() => {
    clearInterval(timer)
    window.removeEventListener('resize', onResize)
    disposeAll()
  })
</script>

<template>
  <div class="screen">
    <!--顶部标题栏-->
    <header class="screen-header">
      <div class="title-badge">数据指挥中心</div>
      <h1>智慧养老 · 数据大屏</h1>
      <div class="header-right">
        <span class="refresh-time">更新：{{ refreshTime }}</span>
        <el-button type="primary" plain size="small" @click="router.push('/home')">返回系统</el-button>
      </div>
    </header>

    <!--核心指标条-->
    <section class="kpi-row">
      <div class="kpi">
        <div class="kpi-label">在住老人</div>
        <div class="kpi-value">{{ data?.elderStatus?.find(s => s.key === 4)?.value ?? '—' }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-label">今日护理任务</div>
        <div class="kpi-value"><span class="accent">{{ data?.todayTask?.done ?? '—' }}</span>/{{ data?.todayTask?.total ?? '—' }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-label">今日完成率</div>
        <div class="kpi-value accent">{{ data?.todayTask?.rate ?? '—' }}<small>%</small></div>
      </div>
      <div class="kpi">
        <div class="kpi-label">7天内待体检预约</div>
        <div class="kpi-value">{{ data?.appointmentNext7 ?? '—' }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-label">今日探视预约</div>
        <div class="kpi-value"><span class="accent">{{ data?.visitToday?.arrived ?? '—' }}</span>/{{ data?.visitToday?.total ?? '—' }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-label">近7天体检异常</div>
        <div class="kpi-value warn">{{ data?.abnormalRecent7 ?? '—' }}</div>
      </div>
    </section>

    <!--图表区-->
    <section class="chart-area">
      <div class="panel">
        <div class="panel-title">老人状态分布</div>
        <div ref="chartStatusEl" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">老人年龄分布</div>
        <div ref="chartAgeEl" class="chart"></div>
      </div>
      <div class="panel panel-wide">
        <div class="panel-title">护理任务完成趋势（近 7 天）</div>
        <div ref="chartTrendEl" class="chart"></div>
      </div>
      <div class="panel">
        <div class="panel-title">护理计划等级分布</div>
        <div ref="chartLevelEl" class="chart"></div>
      </div>
    </section>
  </div>
</template>

<style scoped>
  .screen {
    min-height: 100vh;
    padding: 18px 24px 24px;
    color: #e2e8f0;
    background:
      radial-gradient(900px 400px at 10% -10%, rgba(34, 211, 238, 0.12), transparent 55%),
      radial-gradient(800px 400px at 105% 0%, rgba(52, 211, 153, 0.1), transparent 55%),
      #0b1120;
    box-sizing: border-box;
  }

  .screen-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .title-badge {
      padding: 4px 12px;
      border: 1px solid rgba(34, 211, 238, 0.5);
      border-radius: 999px;
      color: #22d3ee;
      font-size: 12px;
      letter-spacing: 2px;
    }

    h1 {
      flex: 1;
      text-align: center;
      margin: 0;
      font-size: 26px;
      letter-spacing: 6px;
      background: linear-gradient(90deg, #22d3ee, #34d399, #a78bfa);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .refresh-time {
      font-size: 12px;
      color: #94a3b8;
    }
  }

  .kpi-row {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 12px;
    margin-top: 20px;
  }

  .kpi {
    padding: 14px 12px;
    text-align: center;
    border: 1px solid rgba(148, 163, 184, 0.2);
    border-radius: 12px;
    background: rgba(30, 41, 59, 0.5);

    .kpi-label {
      font-size: 12px;
      color: #94a3b8;
    }

    .kpi-value {
      margin-top: 8px;
      font-size: 26px;
      font-weight: 700;
      color: #e2e8f0;

      &.accent { color: #34d399; }
      &.warn { color: #fbbf24; }
      small { font-size: 14px; }
    }
  }

  .chart-area {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
    margin-top: 14px;
  }

  .panel {
    padding: 14px;
    border: 1px solid rgba(148, 163, 184, 0.18);
    border-radius: 12px;
    background: rgba(30, 41, 59, 0.45);

    .panel-title {
      font-size: 14px;
      font-weight: 600;
      color: #cbd5e1;
      padding-left: 10px;
      border-left: 3px solid #22d3ee;
      margin-bottom: 8px;
    }

    .chart {
      width: 100%;
      height: 260px;
    }
  }

  .panel-wide {
    grid-row: span 2;
  }

  @media (max-width: 1100px) {
    .kpi-row {
      grid-template-columns: repeat(3, 1fr);
    }
    .chart-area {
      grid-template-columns: 1fr;
    }
    .panel-wide {
      grid-row: auto;
    }
  }
</style>
