<template>
  <div class="page-container">
    <div class="page-header">
      <h2>工作台</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px">欢迎，{{ auth.user?.realName }}</p>
    </div>

    <!-- Engineer stats -->
    <div v-if="auth.user?.role=='engineer'" class="stat-grid">
      <el-card class="stat-card" shadow="hover" @click="$router.push('/my-tasks')">
        <div class="stat-num" style="color:#409eff">{{ stats.todo ?? '--' }}</div>
        <div class="stat-label">待填报</div>
      </el-card>
      <el-card class="stat-card" shadow="hover">
        <div class="stat-num" style="color:#e6a23c">{{ stats.returned ?? '--' }}</div>
        <div class="stat-label">被退回待补充</div>
      </el-card>
    </div>

    <!-- Manager stats -->
    <div v-if="auth.user?.role=='manager'" class="stat-grid">
      <el-card class="stat-card" shadow="hover" @click="$router.push('/pending-review')">
        <div class="stat-num" style="color:#409eff">{{ stats.pendingReview ?? '--' }}</div>
        <div class="stat-label">待审阅填报</div>
      </el-card>
      <el-card class="stat-card" shadow="hover" @click="$router.push('/pending-review')">
        <div class="stat-num" style="color:#67c23a">{{ stats.pendingAchievement ?? '--' }}</div>
        <div class="stat-label">待审核成果</div>
      </el-card>
      <el-card class="stat-card" shadow="hover" @click="$router.push('/deviations')">
        <div class="stat-num" style="color:#e6a23c">{{ stats.openDeviations ?? '--' }}</div>
        <div class="stat-label">未关闭偏差</div>
      </el-card>
      <el-card class="stat-card" shadow="hover" @click="$router.push('/supports')">
        <div class="stat-num" style="color:#f56c6c">{{ stats.pendingSupports ?? '--' }}</div>
        <div class="stat-label">待处理支持</div>
      </el-card>
    </div>

    <!-- Leader stats -->
    <div v-if="auth.user?.role=='leader'" class="stat-grid">
      <el-card class="stat-card" shadow="hover" @click="$router.push('/deviations')">
        <div class="stat-num" style="color:#e6a23c">{{ stats.openDeviations ?? '--' }}</div>
        <div class="stat-label">未关闭偏差</div>
      </el-card>
      <el-card class="stat-card" shadow="hover" @click="$router.push('/supports')">
        <div class="stat-num" style="color:#f56c6c">{{ stats.pendingSupports ?? '--' }}</div>
        <div class="stat-label">待处理支持事项</div>
      </el-card>
    </div>

    <!-- Quick actions row -->
    <el-row :gutter="16" style="margin-top:24px">
      <el-col :span="8">
        <el-button size="large" style="width:100%;height:64px" @click="$router.push('/projects')">
          <el-icon style="margin-right:6px"><Folder /></el-icon> 项目列表
        </el-button>
      </el-col>
      <el-col :span="8" v-if="auth.user?.role=='engineer'">
        <el-button size="large" style="width:100%;height:64px" @click="$router.push('/my-tasks')">
          <el-icon style="margin-right:6px"><Edit /></el-icon> 我的待填报
        </el-button>
      </el-col>
      <el-col :span="8" v-if="auth.user?.role=='manager'">
        <el-button size="large" style="width:100%;height:64px" @click="$router.push('/pending-review')">
          <el-icon style="margin-right:6px"><View /></el-icon> 待审阅
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import request from '../api/index'

const auth = useAuthStore()
const stats = ref({})

async function loadStats() {
  try {
    const res = await request.get('/dashboard')
    stats.value = res.data
  } catch {}
}

onMounted(loadStats)
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 8px;
}
.stat-card {
  cursor: pointer;
  text-align: center;
  padding: 20px 0;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border-radius: 8px;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}
.stat-num {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 13px;
  color: var(--pm-text-secondary);
  margin-top: 6px;
  letter-spacing: 0.5px;
}
</style>
