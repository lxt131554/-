<template>
  <div class="page-container">
    <div class="page-header"><h2>用户管理</h2></div>
    <div class="card-box">
      <div class="page-toolbar">
        <div></div>
        <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon> 新增用户</el-button>
      </div>
      <el-table v-if="users.length" :data="users" border stripe v-loading="loading">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="role" label="角色" min-width="100">
          <template #default="{row}">
            <el-tag size="small">{{ roleMap[row.role] || row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dept" label="部门" min-width="140" />
        <el-table-column prop="enabled" label="状态" min-width="100" align="center">
          <template #default="{row}">
            <el-tag :type="row.enabled?'success':'danger'" size="small">{{ row.enabled?'启用':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right" align="center">
          <template #default="{row}">
            <div class="table-actions">
              <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button :type="row.enabled?'warning':'success'" size="small" @click="toggleUser(row)">
                {{ row.enabled?'禁用':'启用' }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!loading" description="暂无用户数据" />
    </div>

    <el-dialog v-model="showDialog" :title="isEdit?'编辑用户':'新增用户'" width="500px" :close-on-click-modal="false" append-to-body align-center :lock-scroll="true">
      <el-form :model="form" label-width="120px">
        <el-form-item label="用户名" required><el-input v-model="form.username" :disabled="isEdit" /></el-form-item>
        <el-form-item label="姓名" required><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="密码" :required="!isEdit">
          <el-input v-model="form.password" type="password" :placeholder="isEdit?'留空则不修改':''" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role" style="width:100%">
            <el-option label="系统管理员" value="admin" />
            <el-option label="项目负责人" value="manager" />
            <el-option label="工程师" value="engineer" />
            <el-option label="院领导" value="leader" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门"><el-input v-model="form.dept" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog=false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../api/index'
import { ElMessage } from 'element-plus'
import { confirmDanger, showActionError } from '../utils/actionGuards'

const users = ref([])
const loading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const saving = ref(false)
const form = reactive({ username:'', realName:'', password:'', role:'manager', dept:'' })
const roleMap = { admin:'管理员', manager:'项目负责人', engineer:'工程师', leader:'院领导' }

async function loadUsers() {
  loading.value = true
  try { const res = await request.get('/users'); users.value = res.data || [] }
  catch (error) { showActionError(error, '用户列表加载失败') }
  finally { loading.value = false }
}

function openCreate() {
  isEdit.value = false; editId.value = null
  Object.assign(form, { username:'', realName:'', password:'', role:'manager', dept:'' })
  showDialog.value = true
}

function openEdit(row) {
  isEdit.value = true; editId.value = row.id
  Object.assign(form, { username: row.username, realName: row.realName, password:'', role: row.role, dept: row.dept || '' })
  showDialog.value = true
}

async function handleSave() {
  if (!form.username || !form.realName || (!isEdit.value && !form.password)) {
    ElMessage.warning('请填写必填项'); return
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await request.put(`/users/${editId.value}`, { ...form })
      ElMessage.success('用户已更新')
    } else {
      await request.post('/users', { ...form })
      ElMessage.success('用户已创建')
    }
    showDialog.value = false; loadUsers()
  } catch (error) {
    showActionError(error, '用户保存失败')
  } finally { saving.value = false }
}

async function toggleUser(row) {
  try {
    await confirmDanger(`确定${row.enabled ? '禁用' : '启用'}用户“${row.realName || row.username}”？`)
    await request.put(`/users/${row.id}/toggle`)
    ElMessage.success('状态已更新')
    loadUsers()
  } catch (error) {
    showActionError(error, '用户状态更新失败')
  }
}

onMounted(loadUsers)
</script>
