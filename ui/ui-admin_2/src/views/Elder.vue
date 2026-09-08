<script setup>
  import elderApi from '@/api/elder.js'
  import tagApi from '@/api/tag.js'
  import elderProfileApi from '@/api/elderProfile.js'
  import {ref} from 'vue'
  import {ElMessage, ElMessageBox} from 'element-plus'
  //图片上传
  import {Delete, Plus} from '@element-plus/icons-vue'
  import defaultAvatar from '@/assets/default.png'
  import {useTokenStore} from '@/store/token.js'
  const tokenStore = useTokenStore();

  //表格数据
  const list = ref([])
  const total = ref(0)
  //分页信息和搜索条件
  const elderQuery = ref({
    name: '',
    phone: '',
    tagIds: [],
    page: 1,
    limit: 10
  })

  //时间范围
  const createTimeRange = ref([])
  const loadData = () => {
    elderQuery.value.beginCreateTime = createTimeRange.value?.[0]
    elderQuery.value.endCreateTime = createTimeRange.value?.[1]
    //tagIds数组转逗号分隔字符串，后端List<Long>自动解析
    const params = {...elderQuery.value, tagIds: elderQuery.value.tagIds.join(',')}
    elderApi.list(params).then(result => {
      list.value = result.data.records
      total.value = result.data.total
    })
  }

  loadData()

  //搜索下拉用的所有标签
  const tagOptions = ref([])
  const loadTagOptions = () => {
    tagApi.list({page: 1, limit: 1000}).then(result => {
      tagOptions.value = result.data.records
    })
  }
  loadTagOptions()

  // ==================== 老人详情（家属/收费项目/月度账单） ====================
  const drawerVisible = ref(false)
  const detailElder = ref({})
  const activeTab = ref('family')
  const familyList = ref([])
  const chargeList = ref([])
  const bill = ref(null)
  const billMonth = ref('')
  const chargeMonthFilter = ref('')

  const curMonth = () => {
    const d = new Date()
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  }

  const openDetail = (row) => {
    detailElder.value = row
    activeTab.value = 'family'
    billMonth.value = curMonth()
    drawerVisible.value = true
    loadFamily()
    loadCharges()
    loadBill()
  }

  // === 家属 ===
  const loadFamily = async () => {
    const result = await elderProfileApi.familyList(detailElder.value.id)
    if (result.code === 1) familyList.value = result.data
  }
  const familyDialogVisible = ref(false)
  const familyForm = ref({})
  const relationOptions = ['儿子', '女儿', '配偶', '家属', '其他']
  const openFamilyEdit = (row) => {
    familyForm.value = row ? {...row, password: ''} : {relation: '家属', password: ''}
    familyDialogVisible.value = true
  }
  const saveFamily = () => {
    if (!familyForm.value.name) return ElMessage.warning('请输入家属姓名')
    const data = {...familyForm.value, elderId: detailElder.value.id}
    const api = data.id ? elderProfileApi.familyUpdate(data.id, data) : elderProfileApi.familyAdd(data)
    api.then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        familyDialogVisible.value = false
        loadFamily()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }
  const deleteFamily = (f) => {
    ElMessageBox.confirm(`确定删除家属「${f.name}」？`, '提示', {type: 'warning'}).then(() => {
      elderProfileApi.familyDelete(f.id).then(result => {
        result.code === 1 ? (ElMessage.success(result.msg), loadFamily()) : ElMessage.error(result.msg)
      })
    }).catch(() => {})
  }

  // === 收费项目 ===
  const chargeDialogVisible = ref(false)
  const chargeForm = ref({})
  const loadCharges = async () => {
    const result = await elderProfileApi.chargeList(detailElder.value.id, chargeMonthFilter.value || undefined)
    if (result.code === 1) chargeList.value = result.data
  }
  const openChargeAdd = () => {
    chargeForm.value = {itemName: '', amount: null, chargeType: 0, chargeMonth: curMonth(), remark: ''}
    chargeDialogVisible.value = true
  }
  const saveCharge = () => {
    if (!chargeForm.value.itemName) return ElMessage.warning('请输入收费项目名称')
    if (!chargeForm.value.amount || Number(chargeForm.value.amount) <= 0) return ElMessage.warning('金额必须大于0')
    elderProfileApi.chargeAdd({...chargeForm.value, elderId: detailElder.value.id}).then(result => {
      if (result.code === 1) {
        ElMessage.success(result.msg)
        chargeDialogVisible.value = false
        loadCharges()
        loadBill()
      } else {
        ElMessage.error(result.msg)
      }
    })
  }
  const deleteCharge = (c) => {
    ElMessageBox.confirm(`确定删除收费记录「${c.itemName}」？`, '提示', {type: 'warning'}).then(() => {
      elderProfileApi.chargeDelete(c.id).then(result => {
        result.code === 1 ? (ElMessage.success(result.msg), loadCharges(), loadBill()) : ElMessage.error(result.msg)
      })
    }).catch(() => {})
  }

  // === 月度账单 ===
  const loadBill = async () => {
    if (!billMonth.value) billMonth.value = curMonth()
    const result = await elderProfileApi.bill(detailElder.value.id, billMonth.value)
    if (result.code === 1) bill.value = result.data
  }


  const onSearch = () => {
    elderQuery.value.page = 1
    loadData()
  }

  //状态（0：禁用，1：启用，2：请假，3：退住中，4：入住中，5：已退住）
  const statusOptions = ref([
    {value: 0, label: '禁用'},
    {value: 1, label: '启用'},
    {value: 2, label: '请假'},
    {value: 3, label: '退住中'},
    {value: 4, label: '入住中'},
    {value: 5, label: '已退住'}
  ])

  const statusText = (status) => {
    const statusObj = statusOptions.value.find(item => item.value === status)
    return statusObj ? statusObj.label : '未知'
  }

  const statusTagType = (status) => {
    const map = {0: 'danger', 1: 'success', 2: 'warning', 3: 'info', 4: 'primary', 5: 'info'}
    return map[status] || 'info'
  }

  //根据id删除
  const deleteById = (id) => {
    ElMessageBox.confirm(
        '您确认要删除么?',
        '警告',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          lockScroll: false //防止抖动
        }
    ).then(() => {
      elderApi.deleteById(id).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }

  let ids = []
  const handleSelectionChange = (rows) => {
    //console.log('多选', rows)
    ids = rows.map(row => row.id)
  }

  const deleteAll = () => {
    ElMessageBox.confirm(
        '您确认要删除么?',
        '警告',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          lockScroll: false //防止抖动
        }
    ).then(() => {
      elderApi.deleteAll(ids).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    })
  }


  //添加、编辑
  const dialogFormVisible = ref(false)
  const elder = ref({})
  const title = ref()

  const showAddDialog = () => {
    dialogFormVisible.value = true
    title.value = '添加'
    elder.value = {status: 1}
  }

  const showUpdateDialog = (id) => {
    dialogFormVisible.value = true
    title.value = '编辑'
    elder.value = {}
    elderApi.selectById(id).then(result => {
      elder.value = result.data
      //编辑时不回显已有密码（留空表示不修改）
      elder.value.password = ''
    })
  }

  const addOrUpdate = () => {
    if (elder.value.id) {//编辑
      const payload = {...elder.value}
      //密码留空表示不修改
      if (!payload.password) {
        delete payload.password
      }
      elderApi.update(elder.value.id, payload).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    } else {//添加
      if (!elder.value.name) {
        ElMessage.warning('请输入名字')
        return
      }
      if (!elder.value.password) {
        ElMessage.warning('请输入初始密码')
        return
      }
      elderApi.add(elder.value).then(result => {
        if (result.code === 1) {
          ElMessage.success(result.msg)
          dialogFormVisible.value = false
          loadData()
        } else {
          ElMessage.error(result.msg)
        }
      })
    }
  }

  //上传头像
  const handleAvatarSuccess = (result) => {
    elder.value.avatar = result.data
  }

  //分配标签
  const dialogTagVisible = ref(false)
  const tagList = ref([])
  const assignedTagIdList = ref([])
  const showAssignedTagDialog = (row) => {
    elder.value = row;
    elderApi.selectAssignedTag(row.id).then((result) => {
      tagList.value = result.data.tagList;
      assignedTagIdList.value = result.data.assignedTagIdList;
      dialogTagVisible.value = true;
    });
  }

  const assignTag = () => {
    const dataDTO = {
      elderId: elder.value.id,
      assignedTagIdList: assignedTagIdList.value
    };
    const tagIds = assignedTagIdList.value.join(',');
    // /elders/assignTag?elderId=1&tagIds=1,2,3
    elderApi.assignTag(elder.value.id, tagIds).then((result) => {
      if (result.code === 1) {
        ElMessage.success(result.msg);
        dialogTagVisible.value = false;
        loadData(); //刷新列表，让标签列同步更新
      } else {
        ElMessage.error(result.msg);
      }
    });
  }




</script>

<template>
  <el-card class="">
    <template #header>
      <div class="page-header">
        <div class="page-title">老人信息</div>
        <div class="header">
          <el-button type="primary" :icon="Plus" @click="showAddDialog">添加</el-button>
          <el-button type="danger" :icon="Delete" @click="deleteAll">批量删除</el-button>
        </div>
      </div>
    </template>
    <el-form :inline="true">
      <el-form-item label="名字">
        <el-input v-model="elderQuery.name" placeholder="请输入名字" clearable style="width: 200px"/>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="elderQuery.phone" placeholder="请输入手机号" clearable style="width: 200px"/>
      </el-form-item>
      <el-form-item label="标签">
        <el-select
            v-model="elderQuery.tagIds"
            multiple
            clearable
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="4"
            placeholder="请选择标签"
            style="width: 260px"
        >
          <el-option v-for="tag in tagOptions" :key="tag.id" :label="tag.name" :value="tag.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
            v-model="createTimeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">搜索</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="list" border style="width: 100%" show-overflow-tooltip ref="multipleTableRef" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column fixed prop="id" label="ID"/>
      <el-table-column prop="name" label="名字"/>
      <el-table-column prop="phone" label="电话"/>
      <el-table-column prop="idCardNo" label="身份证号" :show-overflow-tooltip="true"/>
      <el-table-column prop="avatar" label="头像" width="72">
        <template #default="{row}">
          <el-avatar :size="36" :src="row.avatar || defaultAvatar"/>
        </template>
      </el-table-column>
      <el-table-column prop="birthday" label="出生日期" width="100"/>
      <el-table-column prop="address" label="家庭住址" :show-overflow-tooltip="true"/>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tagNames" label="标签" width="200" :show-overflow-tooltip="false">
        <template #default="{row}">
          <el-tag
              v-for="tagName in row.tagNames"
              :key="tagName"
              size="small"
              effect="plain"
              style="margin: 0 4px 4px 0"
          >{{ tagName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" :show-overflow-tooltip="true"/>
      <el-table-column prop="createTime" label="创建时间" width="200"/>
      <el-table-column align="center" width="270px" fixed="right" label="操作">
        <template #default="{ row }">
          <el-button size="small" type="primary" plain @click="openDetail(row)">详情</el-button>
          <el-button size="small" type="primary" @click="showUpdateDialog(row.id)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteById(row.id)">删除</el-button>
          <el-button type="success" size="small" @click="showAssignedTagDialog(row)">标签</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
        v-model:current-page="elderQuery.page"
        v-model:page-size="elderQuery.limit"
        :page-sizes="[10, 20, 30, 40]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
    />
  </el-card>


  <!--添加、编辑弹出框-->
  <el-dialog v-model="dialogFormVisible" :title="title" width="500" :lock-scroll="false" :close-on-click-modal="false">
    <el-form :model="elder">
      <el-form-item label="名字" :label-width="80">
        <el-input v-model="elder.name" autocomplete="off" />
      </el-form-item>
      <el-form-item label="密码" :label-width="80">
        <el-input
            v-model="elder.password"
            type="password"
            show-password
            autocomplete="new-password"
            :placeholder="elder.id ? '留空则不修改' : '请输入初始密码'"
        />
      </el-form-item>
      <el-form-item label="手机号" :label-width="80">
        <el-input v-model="elder.phone" autocomplete="off" />
      </el-form-item>
      <el-form-item label="身份证号" :label-width="80">
        <el-input v-model="elder.idCardNo" autocomplete="off" />
      </el-form-item>
      <el-form-item label="出生日期" :label-width="80">
        <el-date-picker
            v-model="elder.birthday"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择出生日期"
            style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="家庭住址" :label-width="80">
        <el-input v-model="elder.address" autocomplete="off" />
      </el-form-item>
      <el-form-item label="状态" :label-width="80">
        <el-select v-model="elder.status" placeholder="请选择状态">
          <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注" :label-width="80">
        <el-input v-model="elder.remark" type="textarea" autocomplete="off" />
      </el-form-item>
      <el-form-item label="照片" :label-width="80">
        <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :headers="{Authorization: tokenStore.token}"
        >
          <img v-if="elder.avatar" :src="elder.avatar" class="avatar"/>
          <el-icon v-else class="avatar-uploader-icon">
            <Plus/>
          </el-icon>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="addOrUpdate">
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 标签分配dialog-->
  <el-dialog title="分配标签" v-model="dialogTagVisible" width="40%">
    <el-form ref="form" :model="elder" label-width="80px">
      <el-form-item label="名字">
        <el-input v-model="elder.name" disabled></el-input>
      </el-form-item>
      <el-form-item label="标签列表">
        <el-checkbox-group v-model="assignedTagIdList">
          <el-checkbox v-for="tag in tagList" :key="tag.id" :label="tag.id">{{tag.name}}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogTagVisible = false">取消</el-button>
        <el-button type="primary" @click="assignTag">保存</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 老人信息详情（家属/收费项目/月度账单） -->
  <el-drawer v-model="drawerVisible" :title="`${detailElder.name || '老人'} · 老人信息`" size="52%">
    <div class="detail-summary">
      <span class="ds-status">
        <el-tag :type="statusTagType(detailElder.status)">{{ statusText(detailElder.status) }}</el-tag>
      </span>
      <span v-if="detailElder.phone">📞 {{ detailElder.phone }}</span>
      <span v-if="detailElder.address">📍 {{ detailElder.address }}</span>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 家属信息 -->
      <el-tab-pane label="家属信息" name="family">
        <div class="pane-toolbar">
          <el-button type="primary" size="small" :icon="Plus" @click="openFamilyEdit()">添加家属</el-button>
        </div>
        <el-table :data="familyList" border>
          <el-table-column prop="name" label="姓名" width="110"/>
          <el-table-column prop="relation" label="关系" width="90"/>
          <el-table-column prop="phone" label="联系电话" width="130"/>
          <el-table-column prop="remark" label="备注" :show-overflow-tooltip="true"/>
          <el-table-column label="操作" width="150" align="center">
            <template #default="{row}">
              <el-button size="small" type="primary" plain @click="openFamilyEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="deleteFamily(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 收费项目 -->
      <el-tab-pane label="收费项目" name="charge">
        <div class="pane-toolbar">
          <el-date-picker
              v-model="chargeMonthFilter"
              type="month"
              value-format="YYYY-MM"
              placeholder="按月份筛选"
              clearable
              style="width: 150px"
              @change="loadCharges"
          />
          <el-button type="primary" size="small" :icon="Plus" @click="openChargeAdd">添加收费项</el-button>
        </div>
        <el-table :data="chargeList" border>
          <el-table-column prop="itemName" label="收费项目"/>
          <el-table-column label="金额" width="110">
            <template #default="{row}">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column label="类型" width="90">
            <template #default="{row}">
              <el-tag :type="row.chargeType === 1 ? 'warning' : 'info'" size="small">
                {{ row.chargeType === 1 ? '月度' : '一次性' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="chargeMonth" label="月份" width="100"/>
          <el-table-column prop="remark" label="备注" :show-overflow-tooltip="true"/>
          <el-table-column label="操作" width="90" align="center">
            <template #default="{row}">
              <el-button size="small" type="danger" plain @click="deleteCharge(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 月度账单 -->
      <el-tab-pane label="月度账单" name="bill">
        <div class="pane-toolbar">
          <el-date-picker v-model="billMonth" type="month" value-format="YYYY-MM" style="width: 150px"/>
          <el-button type="primary" size="small" @click="loadBill">查询账单</el-button>
        </div>
        <template v-if="bill">
          <div class="bill-summary">
            <div class="bill-item">
              <div class="bi-label">护理等级月费</div>
              <div class="bi-value">¥{{ bill.nursingAmount ?? 0 }}</div>
              <div class="bi-sub">{{ bill.nursingLevelName }}</div>
            </div>
            <div class="bill-item">
              <div class="bi-label">床位月费</div>
              <div class="bi-value">¥{{ bill.bedAmount ?? 0 }}</div>
              <div class="bi-sub">{{ bill.bedInfo }}</div>
            </div>
            <div class="bill-item">
              <div class="bi-label">收费项小计</div>
              <div class="bi-value">¥{{ bill.extraAmount ?? 0 }}</div>
              <div class="bi-sub">{{ (bill.items || []).length }} 笔</div>
            </div>
            <div class="bill-item bill-total">
              <div class="bi-label">本月合计</div>
              <div class="bi-value">¥{{ bill.totalAmount ?? 0 }}</div>
              <div class="bi-sub">{{ bill.month }}（{{ bill.elderName }}）</div>
            </div>
          </div>
          <el-table :data="bill.items || []" border style="margin-top: 12px">
            <el-table-column prop="itemName" label="收费项目"/>
            <el-table-column label="金额" width="120">
              <template #default="{row}">¥{{ row.amount }}</template>
            </el-table-column>
            <el-table-column label="类型" width="90">
              <template #default="{row}">
                <el-tag :type="row.chargeType === 1 ? 'warning' : 'info'" size="small">
                  {{ row.chargeType === 1 ? '月度' : '一次性' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="chargeMonth" label="起始月份" width="100"/>
            <el-table-column prop="remark" label="备注"/>
          </el-table>
          <div class="bill-tip">提示：月度收费项按起始月份起每月计入；基础月费来自入住模块的在住等级/床位配置。</div>
        </template>
      </el-tab-pane>
    </el-tabs>
  </el-drawer>

  <!-- 家属编辑弹窗 -->
  <el-dialog v-model="familyDialogVisible" :title="familyForm.id ? '编辑家属' : '添加家属'" width="460"
             :close-on-click-modal="false">
    <el-form :model="familyForm" label-width="90">
      <el-form-item label="姓名" required>
        <el-input v-model="familyForm.name" placeholder="请输入家属姓名"/>
      </el-form-item>
      <el-form-item label="关系">
        <el-select v-model="familyForm.relation" style="width: 100%">
          <el-option v-for="r in relationOptions" :key="r" :label="r" :value="r"/>
        </el-select>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="familyForm.phone" placeholder="请输入电话" maxlength="20"/>
      </el-form-item>
      <el-form-item label="登录密码">
        <el-input
            v-model="familyForm.password"
            type="password"
            show-password
            :placeholder="familyForm.id ? '留空则不修改' : '留空默认 123456'"
        />
        <div class="family-pwd-tip">家属用「家属姓名 + 此密码」在老人端登录</div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="familyForm.remark" type="textarea" :rows="2"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="familyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveFamily">保存</el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 添加收费项弹窗 -->
  <el-dialog v-model="chargeDialogVisible" title="添加收费项" width="460" :close-on-click-modal="false">
    <el-form :model="chargeForm" label-width="100">
      <el-form-item label="收费项目" required>
        <el-input v-model="chargeForm.itemName" placeholder="如：营养餐费 / 专项护理费"/>
      </el-form-item>
      <el-form-item label="金额(元)" required>
        <el-input-number v-model="chargeForm.amount" :min="0.01" :precision="2" controls-position="right" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="类型">
        <el-radio-group v-model="chargeForm.chargeType">
          <el-radio :value="0">一次性</el-radio>
          <el-radio :value="1">月度(每月重复)</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="月份">
        <el-date-picker v-model="chargeForm.chargeMonth" type="month" value-format="YYYY-MM" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="chargeForm.remark" type="textarea" :rows="2"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="chargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCharge">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
  .avatar-uploader .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }

  .detail-summary {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 16px;
    margin-bottom: 12px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .pane-toolbar {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
  }

  .bill-summary {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;

    .bill-item {
      padding: 14px;
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 10px;
      background: #f8fafc;

      .bi-label {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }

      .bi-value {
        margin-top: 6px;
        font-size: 22px;
        font-weight: 700;
        color: #0f172a;
      }

      .bi-sub {
        margin-top: 4px;
        font-size: 12px;
        color: #94a3b8;
      }
    }

    .bill-total {
      background: linear-gradient(135deg, #14b8a6 0%, #0ea5e9 100%);
      border: none;

      .bi-label, .bi-sub { color: rgba(255, 255, 255, 0.85); }
      .bi-value { color: #fff; }
    }
  }

  .bill-tip {
    margin-top: 12px;
    font-size: 12px;
    color: #94a3b8;
  }

  .family-pwd-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    line-height: 1.4;
  }

  @media (max-width: 900px) {
    .bill-summary {
      grid-template-columns: repeat(2, 1fr);
    }
  }
</style>

<style>
  .avatar-uploader .el-upload {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
  }

  .avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
  }

  .el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 178px;
    height: 178px;
    text-align: center;
  }
</style>
