<script setup>
  import {onMounted, ref} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import {Plus, RefreshRight, Money} from '@element-plus/icons-vue'
  import stayApi from '@/api/stay.js'
  import roomApi from '@/api/room.js'
  import elderApi from '@/api/elder.js'
  import careLevelApi from '@/api/careLevel.js'

  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const query = ref({page: 1, limit: 10, elderName: '', activeOnly: null})
  const checkInRange = ref([])

  const loadData = async () => {
    loading.value = true
    try {
      const params = {...query.value}
      params.beginDate = checkInRange.value?.[0]
      params.endDate = checkInRange.value?.[1]
      const result = await stayApi.list(params)
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

  //办理入住
  const checkInVisible = ref(false)
  const checkInForm = ref({})
  const elderOptions = ref([])
  const freeBeds = ref([])
  const levelOptions = ref([])

  const prepareCheckIn = async () => {
    const [c, b, l] = await Promise.all([stayApi.candidates(), roomApi.freeBeds(), careLevelApi.list({page: 1, limit: 100})])
    elderOptions.value = c.data
    freeBeds.value = b.data
    levelOptions.value = l.data.records
    checkInForm.value = {
      elderId: null,
      bedId: null,
      careLevelId: null,
      checkInDate: new Date().toISOString().slice(0, 10),
      remark: ''
    }
    checkInVisible.value = true
  }

  const submitCheckIn = () => {
    if (!checkInForm.value.elderId) return ElMessage.warning('请选择老人')
    if (!checkInForm.value.bedId) return ElMessage.warning('请选择床位')
    stayApi.checkIn(checkInForm.value).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        checkInVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  //调床
  const transferVisible = ref(false)
  const transferStay = ref({})
  const transferForm = ref({bedId: null})
  const openTransfer = (row) => {
    transferStay.value = row
    transferForm.value = {bedId: null}
    roomApi.freeBeds().then(result => {
      freeBeds.value = result.data.filter(b => b.id !== row.bedId)
      transferVisible.value = true
    })
  }
  const submitTransfer = () => {
    if (!transferForm.value.bedId) return ElMessage.warning('请选择目标床位')
    stayApi.transfer(transferStay.value.id, transferForm.value.bedId).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        transferVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }

  //退住
  const checkOut = (row) => {
    ElMessageBox.confirm(`确定对「${row.elderName}」办理退住？系统将自动释放床位并结算费用。`, '退住确认', {
      confirmButtonText: '确认退住',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      stayApi.checkOut(row.id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }).catch(() => {})
  }

  //缴费
  const pay = (row) => {
    ElMessageBox.confirm(`确认「${row.elderName}」结算单 ¥${row.settleAmount} 已完成缴费？`, '缴费确认', {
      confirmButtonText: '确认缴费',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      stayApi.pay(row.id).then(result => {
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
        <div class="page-title">入住退住</div>
        <div class="header">
          <el-button type="primary" :icon="Plus" @click="prepareCheckIn">办理入住</el-button>
        </div>
      </div>
    </template>

    <el-form :inline="true">
      <el-form-item label="老人姓名">
        <el-input v-model="query.elderName" placeholder="请输入老人姓名" clearable style="width: 160px"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.activeOnly" placeholder="全部" clearable style="width: 120px">
          <el-option label="当前在住" :value="true"/>
        </el-select>
      </el-form-item>
      <el-form-item label="入住日期">
        <el-date-picker
            v-model="checkInRange"
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
      <el-table-column label="床位" width="130">
        <template #default="{row}">{{ row.roomCode }} {{ row.bedNo }}</template>
      </el-table-column>
      <el-table-column prop="careLevelName" label="护理等级" width="110">
        <template #default="{row}">
          <el-tag v-if="row.careLevelName" size="small" effect="plain">{{ row.careLevelName }}</el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="checkInDate" label="入住日期" width="120"/>
      <el-table-column label="退住日期" width="120">
        <template #default="{row}">
          <span v-if="row.checkOutDate">{{ row.checkOutDate }}</span>
          <el-tag v-else type="success" size="small">在住</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结算金额" width="110">
        <template #default="{row}">
          <span v-if="row.settleAmount != null" :style="{color: row.settleStatus === 0 ? '#f59e0b' : '#22c55e', fontWeight: 600}">
            ¥{{ row.settleAmount }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="缴费状态" width="100">
        <template #default="{row}">
          <el-tag v-if="row.settleStatus === 1" type="success" size="small">已结清</el-tag>
          <el-tag v-else-if="row.settleStatus === 0" type="warning" size="small">待缴费</el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" :show-overflow-tooltip="true"/>
      <el-table-column label="操作" width="210" align="center" fixed="right">
        <template #default="{row}">
          <template v-if="!row.checkOutDate">
            <el-button size="small" type="warning" @click="openTransfer(row)">调床</el-button>
            <el-button size="small" type="danger" @click="checkOut(row)">退住</el-button>
          </template>
          <template v-else>
            <el-button v-if="row.settleStatus === 0" size="small" type="success" :icon="Money" @click="pay(row)">缴费</el-button>
          </template>
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

  <!--办理入住-->
  <el-dialog v-model="checkInVisible" title="办理入住" width="520" :close-on-click-modal="false">
    <el-form :model="checkInForm" label-width="100">
      <el-form-item label="老人" required>
        <el-select v-model="checkInForm.elderId" placeholder="选择未在住的老人" filterable style="width: 100%">
          <el-option v-for="e in elderOptions" :key="e.id" :label="`${e.name}（${e.phone || '无手机号'}）`" :value="e.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="床位" required>
        <el-select v-model="checkInForm.bedId" placeholder="选择空闲床位" filterable style="width: 100%">
          <el-option v-for="b in freeBeds" :key="b.id"
                     :label="`${b.floor}楼${b.roomCode} · ${b.bedNo}（空闲）`" :value="b.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="护理等级">
        <el-select v-model="checkInForm.careLevelId" placeholder="用于退住结算" clearable style="width: 100%">
          <el-option v-for="l in levelOptions" :key="l.id" :label="`${l.name}（¥${l.price}/月）`" :value="l.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="入住日期">
        <el-date-picker v-model="checkInForm.checkInDate" type="date" value-format="YYYY-MM-DD" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="checkInForm.remark" type="textarea" :rows="2"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="checkInVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCheckIn">确认入住</el-button>
      </div>
    </template>
  </el-dialog>

  <!--调床-->
  <el-dialog v-model="transferVisible" :title="`调床 - ${transferStay.elderName || ''}`" width="460" :close-on-click-modal="false">
    <el-form label-width="90">
      <el-form-item label="目标床位" required>
        <el-select v-model="transferForm.bedId" placeholder="选择空闲床位" filterable style="width: 100%">
          <el-option v-for="b in freeBeds" :key="b.id"
                     :label="`${b.floor}楼${b.roomCode} · ${b.bedNo}`" :value="b.id"/>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer">确认调床</el-button>
      </div>
    </template>
  </el-dialog>
</template>
