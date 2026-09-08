<script setup>
  import {ref, onMounted} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import {Plus, Delete, CircleCheck, CircleClose, Timer, Position} from '@element-plus/icons-vue'
  import visitApi from '@/api/visit.js'
  import elderApi from '@/api/elder.js'

  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const query = ref({
    visitorName: '',
    elderId: null,
    status: null,
    page: 1,
    limit: 10
  })
  const visitDateRange = ref([])

  const loadData = async () => {
    loading.value = true
    try {
      const params = {...query.value}
      params.beginVisitDate = visitDateRange.value?.[0]
      params.endVisitDate = visitDateRange.value?.[1]
      const result = await visitApi.list(params)
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

  //老人下拉选项
  const elderOptions = ref([])
  const loadElders = () => {
    elderApi.list({page: 1, limit: 1000}).then(result => {
      elderOptions.value = result.data.records
    })
  }
  onMounted(() => {
    loadData()
    loadElders()
  })

  //状态文案与标签
  const statusMap = [
    {text: '待审批', type: 'warning'},
    {text: '已通过/在访', type: 'success'},
    {text: '已拒绝', type: 'danger'},
    {text: '已完成', type: 'info'},
    {text: '已取消', type: 'info'},
    {text: '已过期', type: 'info'}
  ]
  const statusText = (s) => statusMap[s]?.text || '未知'
  const statusType = (s) => statusMap[s]?.type || 'info'

  //新增弹窗
  const dialogVisible = ref(false)
  const form = ref({})
  const relationOptions = ['家属', '朋友', '维修', '其他']
  const todayStr = () => new Date().toISOString().slice(0, 10)

  const openAdd = (applyWay) => {
    form.value = {
      applyWay: applyWay, //0线上申请 1现场登记
      visitorName: '',
      visitorPhone: '',
      relation: '家属',
      idCardNo: '',
      visitDate: todayStr(),
      startTime: '',
      endTime: '',
      purpose: '',
      remark: ''
    }
    dialogVisible.value = true
  }

  const submitAdd = () => {
    if (!form.value.elderId) {
      ElMessage.warning('请选择被访老人')
      return
    }
    if (!form.value.visitorName) {
      ElMessage.warning('请输入访客姓名')
      return
    }
    if (!form.value.visitDate) {
      ElMessage.warning('请选择探视日期')
      return
    }
    visitApi.add(form.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  const confirmAction = (title, fn, successMsg) => {
    ElMessageBox.confirm(title, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
      lockScroll: false
    }).then(() => {
      fn().then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg || successMsg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  const approve = (row) => confirmAction(`确定通过「${row.visitorName}」的探视申请？`, () => visitApi.approve(row.id))
  const reject = (row) => {
    ElMessageBox.prompt('请输入拒绝原因（选填）', `拒绝「${row.visitorName}」的申请`, {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      inputPlaceholder: '如：探视时段冲突',
      inputValidator: () => true,
      lockScroll: false
    }).then(({value}) => {
      visitApi.reject(row.id, value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }).catch(() => {})
  }
  const arrive = (row) => confirmAction(`确认「${row.visitorName}」已到达探视？`, () => visitApi.arrive(row.id))
  const leave = (row) => confirmAction(`确认「${row.visitorName}」已离院？`, () => visitApi.leave(row.id))
  const expire = (row) => confirmAction(`确定将「${row.visitorName}」的记录置为过期？`, () => visitApi.expire(row.id))
  const del = (row) => confirmAction(`确定删除「${row.visitorName}」的这条记录？`, () => visitApi.deleteById(row.id))

</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="page-header">
        <div class="page-title">探视访客登记</div>
        <div class="header">
          <el-button type="primary" :icon="Plus" @click="openAdd(0)">新增申请</el-button>
          <el-button type="success" :icon="Timer" @click="openAdd(1)">现场登记</el-button>
        </div>
      </div>
    </template>

    <el-form :inline="true">
      <el-form-item label="老人">
        <el-select v-model="query.elderId" placeholder="请选择老人" clearable filterable style="width: 170px">
          <el-option v-for="e in elderOptions" :key="e.id" :label="e.name" :value="e.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="访客姓名">
        <el-input v-model="query.visitorName" placeholder="请输入访客姓名" clearable style="width: 160px"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option v-for="(s, i) in statusMap" :key="i" :label="s.text" :value="i"/>
        </el-select>
      </el-form-item>
      <el-form-item label="探视日期">
        <el-date-picker
            v-model="visitDateRange"
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
      <el-table-column prop="elderName" label="被访老人" width="110"/>
      <el-table-column prop="visitorName" label="访客姓名" width="110"/>
      <el-table-column prop="relation" label="关系" width="80">
        <template #default="{row}">
          <el-tag size="small" effect="plain">{{ row.relation }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="visitorPhone" label="手机号" width="130"/>
      <el-table-column prop="visitDate" label="探视日期" width="120"/>
      <el-table-column label="时段" width="120">
        <template #default="{row}">{{ row.startTime || '--' }} ~ {{ row.endTime || '--' }}</template>
      </el-table-column>
      <el-table-column prop="purpose" label="事由" :show-overflow-tooltip="true"/>
      <el-table-column label="方式" width="90">
        <template #default="{row}">
          <el-tag :type="row.applyWay === 1 ? 'success' : 'info'" size="small">{{ row.applyWay === 1 ? '现场' : '线上' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{row}">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="到访/离院" width="200">
        <template #default="{row}">
          <div v-if="row.arriveTime">到：{{ row.arriveTime }}</div>
          <div v-if="row.leaveTime">离：{{ row.leaveTime }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注/审批意见" min-width="120" :show-overflow-tooltip="true"/>
      <el-table-column label="操作" width="230" align="center" fixed="right">
        <template #default="{row}">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" :icon="CircleCheck" @click="approve(row)">通过</el-button>
            <el-button size="small" type="danger" :icon="CircleClose" @click="reject(row)">拒绝</el-button>
            <el-button size="small" @click="expire(row)">过期</el-button>
          </template>
          <template v-else-if="row.status === 1">
            <el-button v-if="!row.arriveTime" size="small" type="primary" :icon="Position" @click="arrive(row)">到达登记</el-button>
            <el-button v-if="row.arriveTime" size="small" type="warning" :icon="Timer" @click="leave(row)">离院登记</el-button>
          </template>
          <el-button
              v-if="row.status === 0 || row.status === 2 || row.status === 4 || row.status === 5"
              size="small" type="danger" :icon="Delete" plain @click="del(row)">删除</el-button>
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

  <!--新增/现场登记弹窗-->
  <el-dialog v-model="dialogVisible" :title="form.applyWay === 1 ? '现场登记' : '新增探视申请'" width="560" :lock-scroll="false"
             :close-on-click-modal="false">
    <el-form :model="form" label-width="100">
      <el-form-item label="被访老人" required>
        <el-select v-model="form.elderId" placeholder="请选择老人" filterable style="width: 100%">
          <el-option v-for="e in elderOptions" :key="e.id" :label="e.name" :value="e.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="访客姓名" required>
        <el-input v-model="form.visitorName" placeholder="请输入访客姓名"/>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.visitorPhone" placeholder="请输入手机号" maxlength="20"/>
      </el-form-item>
      <el-form-item label="关系">
        <el-select v-model="form.relation" style="width: 100%">
          <el-option v-for="r in relationOptions" :key="r" :label="r" :value="r"/>
        </el-select>
      </el-form-item>
      <el-form-item label="身份证号">
        <el-input v-model="form.idCardNo" placeholder="选填" maxlength="18"/>
      </el-form-item>
      <el-form-item label="探视日期" required>
        <el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" placeholder="请选择日期" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="预约时段">
        <el-time-picker
            v-model="form.startTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="开始时间"
            style="width: 45%"
        />
        <span style="margin: 0 8px">~</span>
        <el-time-picker
            v-model="form.endTime"
            format="HH:mm"
            value-format="HH:mm"
            placeholder="结束时间"
            style="width: 45%"
        />
      </el-form-item>
      <el-form-item label="探视事由">
        <el-input v-model="form.purpose" type="textarea" :rows="2" placeholder="选填"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="选填"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>
