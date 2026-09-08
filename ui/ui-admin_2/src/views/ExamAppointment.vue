<script setup>
  import {computed, onMounted, ref} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import {Plus, Close} from '@element-plus/icons-vue'
  import examAppointmentApi from '@/api/examAppointment.js'

  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const query = ref({page: 1, limit: 10, elderName: '', status: null})
  const dateRange = ref([])

  const statusMap = [
    {text: '待体检', type: 'warning'},
    {text: '体检中', type: 'primary'},
    {text: '已完成', type: 'success'},
    {text: '已取消', type: 'info'},
    {text: '已过期', type: 'info'}
  ]
  const statusText = s => statusMap[s]?.text || '未知'
  const statusType = s => statusMap[s]?.type || 'info'

  const loadData = async () => {
    loading.value = true
    try {
      const params = {...query.value}
      params.beginDate = dateRange.value?.[0]
      params.endDate = dateRange.value?.[1]
      const result = await examAppointmentApi.list(params)
      if (result.code === 1) {
        list.value = result.data.records
        total.value = result.data.total
      } else {
        ElMessage.error(result.msg)
      }
    } finally {
      loading.value = false
    }
  }
  const onSearch = () => {
    query.value.page = 1
    loadData()
  }

  //详情抽屉
  const drawerVisible = ref(false)
  const detail = ref(null)
  const mode = ref('edit') // edit 录入 | view 查看
  const saving = ref(false)
  const abnormalText = (a) => a === 1 ? '异常' : (a === 0 ? '正常' : '未判定')

  //前端按参考范围自动判定预览
  const judge = (r) => {
    if (r.resultType === 1 && r.resultValue != null && r.referenceMin != null && r.referenceMax != null) {
      if (Number(r.resultValue) < Number(r.referenceMin) || Number(r.resultValue) > Number(r.referenceMax)) return '1'
      return '0'
    }
    return null
  }
  const judgePreview = (r) => {
    const j = judge(r)
    return j === null ? '' : (j === '1' ? '（按参考范围判定：异常）' : '（按参考范围判定：正常）')
  }

  const openEdit = async (row) => {
    const result = await examAppointmentApi.detail(row.id)
    if (result.code === 1) {
      detail.value = result.data
      detail.value.items.forEach(r => { r._judge = 'auto' })
      mode.value = 'edit'
      drawerVisible.value = true
    }
  }
  const openView = async (row) => {
    const result = await examAppointmentApi.detail(row.id)
    if (result.code === 1) {
      detail.value = result.data
      mode.value = 'view'
      drawerVisible.value = true
    }
  }

  const itemStatusText = (s) => ['待检查', '正常', '异常', '未完成'][s] || '未知'
  const itemStatusType = (s) => ({1: 'success', 2: 'danger', 3: 'info'}[s] || 'info')

  const submitResults = () => {
    const payload = detail.value.items.map(r => {
      const abnormal = r._judge === 'auto' ? null : Number(r._judge)
      return {
        id: r.id,
        resultValue: r.resultValue ?? null,
        resultText: r.resultText ?? null,
        abnormal,
        remark: r.remark ?? null
      }
    })
    if (!payload.some(p => p.resultValue != null || p.resultText)) {
      return ElMessage.warning('请至少录入一个项目结果')
    }
    saving.value = true
    examAppointmentApi.saveItems(detail.value.id, payload).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        drawerVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    }).finally(() => { saving.value = false })
  }

  const cancelAppointment = (row) => {
    ElMessageBox.confirm('确定取消该体检预约？', '提示', {type: 'warning'}).then(() => {
      examAppointmentApi.cancel(row.id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }).catch(() => {})
  }

  onMounted(loadData)
</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="page-header">
        <div class="page-title">体检登记</div>
      </div>
    </template>

    <el-form :inline="true">
      <el-form-item label="老人姓名">
        <el-input v-model="query.elderName" placeholder="请输入老人姓名" clearable style="width: 160px"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="(s, i) in statusMap" :key="i" :label="s.text" :value="i"/>
        </el-select>
      </el-form-item>
      <el-form-item label="预约日期">
        <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border style="width: 100%" show-overflow-tooltip>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column prop="elderName" label="老人" width="110"/>
      <el-table-column prop="packageName" label="套餐" min-width="150"/>
      <el-table-column label="预约时间" width="180">
        <template #default="{row}">{{ row.appointmentDate }} {{ row.appointmentTime }}</template>
      </el-table-column>
      <el-table-column label="价格" width="90">
        <template #default="{row}">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column label="项目进度" width="130">
        <template #default="{row}">
          <span>{{ row.doneItemCount ?? 0 }}/{{ row.examItemCount ?? 0 }} 已出结果</span>
          <el-tag v-if="row.abnormalCount > 0" type="danger" size="small" style="margin-left: 6px">异常{{ row.abnormalCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="210" align="center" fixed="right">
        <template #default="{row}">
          <el-button v-if="row.status === 0 || row.status === 1" size="small" type="primary" @click="openEdit(row)">录入结果</el-button>
          <el-button v-if="row.status === 2" size="small" type="success" plain @click="openView(row)">查看</el-button>
          <el-button v-if="row.status === 0" size="small" type="danger" plain @click="cancelAppointment(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.limit"
        :page-sizes="[10, 20, 30, 40]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @change="loadData"
        style="margin-top: 20px"
    />
  </el-card>

  <!--结果录入/查看抽屉-->
  <el-drawer v-model="drawerVisible" size="56%" :title="detail ? `${detail.elderName} · ${detail.packageName}` : ''">
    <template v-if="detail">
      <div class="detail-head">
        <span>预约时间：{{ detail.appointmentDate }} {{ detail.appointmentTime }}</span>
        <el-tag :type="statusType(detail.status)">{{ statusText(detail.status) }}</el-tag>
      </div>

      <div v-for="r in detail.items" :key="r.id" class="item-card">
        <div class="item-title">
          <span class="item-name">{{ r.itemName }}</span>
          <template v-if="mode === 'view'">
            <el-tag :type="itemStatusType(r.status)" size="small">{{ itemStatusText(r.status) }}</el-tag>
            <el-tag v-if="r.abnormal === 1" type="danger" size="small">异常</el-tag>
          </template>
        </div>

        <!-- 查看模式：只读结果 -->
        <template v-if="mode === 'view'">
          <div v-if="r.resultType === 1 && r.resultValue != null" class="result-line">
            数值：<b>{{ r.resultValue }}</b> {{ r.resultUnit || r.referenceUnit }}
            <span v-if="r.referenceMin != null || r.referenceMax != null" class="ref">
              （参考 {{ r.referenceMin }} ~ {{ r.referenceMax }} {{ r.referenceUnit }}）
            </span>
          </div>
          <div v-else-if="r.resultText" class="result-line">结果：{{ r.resultText }}</div>
          <div v-else class="result-line muted">未填写结果</div>
        </template>

        <!-- 录入模式 -->
        <template v-else>
          <div v-if="r.resultType === 1" class="edit-row">
            <el-input-number v-model="r.resultValue" :precision="2" :controls="false" placeholder="输入数值" style="width: 180px"/>
            <span class="ref">{{ r.referenceUnit || '' }}
              <template v-if="r.referenceMin != null || r.referenceMax != null">参考 {{ r.referenceMin }} ~ {{ r.referenceMax }}</template>
            </span>
            <span class="auto-hint">{{ judgePreview(r) }}</span>
          </div>
          <div v-else class="edit-row">
            <el-input v-model="r.resultText" type="textarea" :rows="2" placeholder="输入检查结果描述"/>
          </div>
          <div class="edit-row judge-row">
            <span class="judge-label">判定：</span>
            <el-radio-group v-model="r._judge">
              <el-radio value="auto">按范围自动</el-radio>
              <el-radio value="0">正常</el-radio>
              <el-radio value="1">异常</el-radio>
            </el-radio-group>
          </div>
        </template>
      </div>
    </template>
    <template #footer v-if="mode === 'edit' && detail">
      <div class="drawer-footer">
        <el-button :loading="saving" type="primary" @click="submitResults">保存并完成体检</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped>
  .detail-head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .item-card {
    padding: 12px;
    margin-bottom: 10px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 10px;
    background: #fbfdfe;

    .item-title {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;

      .item-name { font-weight: 600; font-size: 14px; }
    }

    .result-line { font-size: 13px; color: #1e293b; margin-top: 2px;
      .ref { color: #94a3b8; }
    }
    .result-line.muted { color: #94a3b8; }

    .edit-row {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;

      .ref, .auto-hint { font-size: 12px; color: #94a3b8; }
    }
    .judge-row { margin-top: 8px; }
    .judge-label { font-size: 13px; color: var(--el-text-color-secondary); }
  }
</style>
