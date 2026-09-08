<script setup>
  import {onMounted, ref} from 'vue'
  import {showToast} from 'vant'
  import {useRoute, useRouter} from 'vue-router'
  import appointmentApi from '@/api/appointment.js'

  const route = useRoute()
  const router = useRouter()
  const report = ref(null)
  const loading = ref(true)

  const itemStatusText = (s) => ['待检查', '正常', '异常', '未完成'][s] || '未知'

  onMounted(() => {
    appointmentApi.report(route.params.id).then(result => {
      if (result.code === 1) {
        report.value = result.data
      } else {
        showToast(result.msg || '报告加载失败')
      }
    }).catch(() => {
      showToast('报告加载失败')
    }).finally(() => { loading.value = false })
  })
</script>

<template>
  <div class="report-page">
    <van-nav-bar title="体检报告" left-arrow fixed placeholder @click-left="router.back()"/>

    <van-loading v-if="loading" class="page-loading" size="24" vertical>加载中...</van-loading>

    <template v-else-if="report">
      <!--报告头-->
      <div class="report-head">
        <div class="head-title">体检报告</div>
        <div class="head-name">{{ report.packageName }}</div>
        <div class="head-sub">
          体检人：{{ report.elderName }} · {{ report.appointmentDate }} {{ report.appointmentTime }}
        </div>
      </div>

      <!--项目结果列表-->
      <div class="items">
        <div v-for="(item, i) in report.items" :key="item.id" class="item">
          <div class="item-row">
            <span class="item-name">{{ i + 1 }}. {{ item.itemName }}</span>
            <span class="item-status" :class="'st' + (item.status || 0)">{{ itemStatusText(item.status) }}</span>
          </div>

          <div class="item-result">
            <template v-if="item.resultType === 1 && item.resultValue != null">
              数值：<b>{{ item.resultValue }}</b> {{ item.resultUnit || item.referenceUnit }}
            </template>
            <template v-else-if="item.resultText">
              结果：{{ item.resultText }}
            </template>
            <template v-else>
              <span class="muted">未填写结果</span>
            </template>
            <span v-if="item.referenceMin != null || item.referenceMax != null" class="ref">
              （参考 {{ item.referenceMin }} ~ {{ item.referenceMax }} {{ item.referenceUnit }}）
            </span>
          </div>

          <div v-if="item.abnormal === 1" class="item-abnormal">⚠ 该项提示异常，请遵医嘱复查或咨询医生</div>
        </div>
      </div>

      <div class="report-tip">本报告结果由机构录入，仅供健康参考，不作为诊断依据；如有疑问请联系社区医护人员。</div>
    </template>

    <van-empty v-else description="报告不存在或已删除"/>
  </div>
</template>

<style scoped lang="scss">
  .report-page { padding-bottom: 24px; }

  .page-loading { padding: 60px 0; }

  .report-head {
    margin: 12px 16px 0;
    padding: 18px 16px;
    color: #fff;
    border-radius: 12px;
    background: linear-gradient(120deg, #07c160, #4acd80);

    .head-title { font-size: 12px; opacity: 0.85; letter-spacing: 2px; }
    .head-name { margin-top: 6px; font-size: 18px; font-weight: bold; }
    .head-sub { margin-top: 8px; font-size: 12px; opacity: 0.9; }
  }

  .items {
    margin: 12px 16px 0;

    .item {
      padding: 12px 14px;
      margin-bottom: 10px;
      background: #fff;
      border-radius: 12px;

      .item-row {
        display: flex;
        justify-content: space-between;
        align-items: center;

        .item-name { font-weight: 600; font-size: 14px; color: #323233; }
        .item-status {
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 10px;
          background: #f2f3f5;
          color: #969799;

          &.st1 { background: #e8f8f0; color: #07c160; }
          &.st2 { background: #ffece5; color: #ee0a24; }
        }
      }

      .item-result {
        margin-top: 8px;
        font-size: 13px;
        color: #323233;

        .ref { color: #969799; }
        .muted { color: #b6b7b9; }
      }

      .item-abnormal {
        margin-top: 8px;
        font-size: 12px;
        color: #ee0a24;
        background: #fff3ef;
        border-radius: 8px;
        padding: 6px 8px;
      }
    }
  }

  .report-tip {
    margin: 4px 16px 0;
    font-size: 11px;
    color: #b6b7b9;
    text-align: center;
  }
</style>
