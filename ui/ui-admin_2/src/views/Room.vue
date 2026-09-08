<script setup>
  import {onMounted, ref} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  import {Plus, Delete, Edit, Key, Tools} from '@element-plus/icons-vue'
  import roomApi from '@/api/room.js'

  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const query = ref({page: 1, limit: 10, keyword: '', floor: null, status: null})

  const loadData = async () => {
    loading.value = true
    try {
      const result = await roomApi.list(query.value)
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

  //房间编辑
  const dialogVisible = ref(false)
  const roomForm = ref({})
  const openRoomDialog = (row) => {
    roomForm.value = row ? {...row} : {floor: 1, capacity: 2, price: 0, status: 1}
    dialogVisible.value = true
  }
  const submitRoom = () => {
    if (!roomForm.value.code) return ElMessage.warning('请输入房间号')
    const api = roomForm.value.id ? roomApi.update(roomForm.value.id, roomForm.value) : roomApi.add(roomForm.value)
    api.then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        dialogVisible.value = false
        loadData()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }
  const deleteRoom = (row) => {
    ElMessageBox.confirm(`确定删除房间「${row.code}」？`, '提示', {type: 'warning'}).then(() => {
      roomApi.deleteById(row.id).then(result => {
        result.code === 1 ? (ElMessage.success(result.msg), loadData()) : ElMessage.error(result.msg)
      })
    }).catch(() => {})
  }

  //床位管理
  const bedVisible = ref(false)
  const currentRoom = ref({})
  const beds = ref([])
  const newBedsText = ref('')
  const bedStatusMap = ['空闲', '占用', '维修']

  const openBedDialog = async (row) => {
    currentRoom.value = row
    bedVisible.value = true
    loadBeds()
  }
  const loadBeds = async () => {
    const result = await roomApi.bedsByRoom(currentRoom.value.id)
    if (result.code === 1) beds.value = result.data
  }
  const addBeds = () => {
    const nos = newBedsText.value.split(/[,，\s]+/).map(s => s.trim()).filter(Boolean)
    if (!nos.length) return ElMessage.warning('请输入床号')
    roomApi.addBeds(currentRoom.value.id, nos).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        newBedsText.value = ''
        loadBeds()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }
  const toggleBedStatus = (bed) => {
    if (bed.status === 1) return
    const target = bed.status === 2 ? 0 : 2
    roomApi.updateBedStatus(bed.id, target).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        loadBeds()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }
  const deleteBed = (bed) => {
    if (bed.status === 1) return ElMessage.warning('床位使用中不可删除')
    ElMessageBox.confirm(`确定删除床位「${bed.bedNo}」？`, '提示', {type: 'warning'}).then(() => {
      roomApi.deleteBed(bed.id).then(result => {
        result.code === 1 ? (ElMessage.success(result.msg), loadBeds(), loadData()) : ElMessage.error(result.msg)
      })
    }).catch(() => {})
  }

  onMounted(loadData)
</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="page-header">
        <div class="page-title">房间床位</div>
        <div class="header">
          <el-button type="primary" :icon="Plus" @click="openRoomDialog()">添加房间</el-button>
        </div>
      </div>
    </template>

    <el-form :inline="true">
      <el-form-item label="房间号/备注">
        <el-input v-model="query.keyword" placeholder="输入房间号检索" clearable style="width: 180px"/>
      </el-form-item>
      <el-form-item label="楼层">
        <el-input-number v-model="query.floor" :min="0" controls-position="right" placeholder="楼层" style="width: 110px"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
          <el-option label="正常" :value="1"/>
          <el-option label="停用" :value="0"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">搜索</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" border style="width: 100%">
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column prop="code" label="房间号" width="110"/>
      <el-table-column prop="floor" label="楼层" width="80"/>
      <el-table-column prop="capacity" label="床位容量" width="100"/>
      <el-table-column label="占用 / 总床" width="120">
        <template #default="{row}">
          <el-tag :type="row.bedTotal && row.occupied === row.bedTotal ? 'danger' : 'success'" size="small">
            {{ row.occupied ?? 0 }} / {{ row.bedTotal ?? 0 }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="床位月费" width="110">
        <template #default="{row}">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{row}">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" :show-overflow-tooltip="true"/>
      <el-table-column label="操作" width="230" align="center" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" :icon="Key" @click="openBedDialog(row)">床位</el-button>
          <el-button size="small" type="warning" :icon="Edit" plain @click="openRoomDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" :icon="Delete" plain :disabled="!!row.bedTotal" @click="deleteRoom(row)">删除</el-button>
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

  <!--房间编辑-->
  <el-dialog v-model="dialogVisible" :title="roomForm.id ? '编辑房间' : '添加房间'" width="480" :close-on-click-modal="false">
    <el-form :model="roomForm" label-width="110">
      <el-form-item label="楼层" required>
        <el-input-number v-model="roomForm.floor" :min="0" controls-position="right" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="房间号" required>
        <el-input v-model="roomForm.code" placeholder="如 101"/>
      </el-form-item>
      <el-form-item label="床位数">
        <el-input-number v-model="roomForm.capacity" :min="1" controls-position="right" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="床位月费(元)">
        <el-input-number v-model="roomForm.price" :min="0" :precision="2" controls-position="right" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="roomForm.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="roomForm.remark" type="textarea" :rows="2"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRoom">保存</el-button>
      </div>
    </template>
  </el-dialog>

  <!--床位管理-->
  <el-dialog v-model="bedVisible" :title="`床位管理 - ${currentRoom.code}`" width="720" :close-on-click-modal="false">
    <div class="bed-toolbar">
      <el-input v-model="newBedsText" placeholder="输入床号，多个用逗号分隔（如 101-3,101-4）" style="width: 320px"/>
      <el-button type="primary" :icon="Plus" @click="addBeds">添加床位</el-button>
      <span class="capacity-tip">容量 {{ currentRoom.capacity }} 床</span>
    </div>
    <el-table :data="beds" border>
      <el-table-column prop="bedNo" label="床号"/>
      <el-table-column label="状态" width="120">
        <template #default="{row}">
          <el-tag :type="row.status === 0 ? 'success' : row.status === 1 ? 'danger' : 'warning'">
            {{ bedStatusMap[row.status] ?? '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center">
        <template #default="{row}">
          <el-button
              v-if="row.status !== 1"
              size="small" type="warning" plain
              @click="toggleBedStatus(row)">{{ row.status === 2 ? '恢复空闲' : '标记维修' }}</el-button>
          <el-button size="small" type="danger" plain :disabled="row.status === 1" @click="deleteBed(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<style scoped>
  .bed-toolbar {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 14px;

    .capacity-tip {
      color: var(--el-text-color-secondary);
      font-size: 13px;
    }
  }
</style>
