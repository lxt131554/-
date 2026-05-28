<template>
  <div class="page-container">
    <div class="page-header">
      <h2>工作台</h2>
      <p style="color:var(--pm-text-secondary);margin-top:4px;font-size:16px">欢迎，{{ auth.user?.realName }}</p>
    </div>

    <!-- Engineer stats -->
    <div v-if="auth.user?.role=='engineer'" class="stat-grid">
      <div class="stat-card stat-card--blue" @click="$router.push('/my-tasks')" style="animation-delay:0ms">
        <div class="stat-inner">
          <div class="stat-num">{{ stats.todo ?? '--' }}</div>
          <div class="stat-label">待填报</div>
        </div>
      </div>
      <div class="stat-card stat-card--amber" style="animation-delay:60ms">
        <div class="stat-inner">
          <div class="stat-num">{{ stats.returned ?? '--' }}</div>
          <div class="stat-label">被退回待补充</div>
        </div>
      </div>
    </div>

    <!-- Manager stats -->
    <div v-if="auth.user?.role=='manager'" class="stat-grid">
      <div class="stat-card stat-card--blue" @click="$router.push('/pending-review')" style="animation-delay:0ms">
        <div class="stat-inner">
          <div class="stat-num">{{ stats.pendingReview ?? '--' }}</div>
          <div class="stat-label">待审阅填报</div>
        </div>
      </div>
      <div class="stat-card stat-card--green" @click="$router.push('/pending-review')" style="animation-delay:60ms">
        <div class="stat-inner">
          <div class="stat-num">{{ stats.pendingAchievement ?? '--' }}</div>
          <div class="stat-label">待审核成果</div>
        </div>
      </div>
      <div class="stat-card stat-card--amber" @click="$router.push('/deviations')" style="animation-delay:120ms">
        <div class="stat-inner">
          <div class="stat-num">{{ stats.openDeviations ?? '--' }}</div>
          <div class="stat-label">未关闭偏差</div>
        </div>
      </div>
      <div class="stat-card stat-card--red" @click="$router.push('/supports')" style="animation-delay:180ms">
        <div class="stat-inner">
          <div class="stat-num">{{ stats.pendingSupports ?? '--' }}</div>
          <div class="stat-label">待处理支持</div>
        </div>
      </div>
    </div>

    <!-- Leader stats -->
    <div v-if="auth.user?.role=='leader'" class="stat-grid">
      <div class="stat-card stat-card--amber" @click="$router.push('/deviations')" style="animation-delay:0ms">
        <div class="stat-inner">
          <div class="stat-num">{{ stats.openDeviations ?? '--' }}</div>
          <div class="stat-label">未关闭偏差</div>
        </div>
      </div>
      <div class="stat-card stat-card--red" @click="$router.push('/supports')" style="animation-delay:60ms">
        <div class="stat-inner">
          <div class="stat-num">{{ stats.pendingSupports ?? '--' }}</div>
          <div class="stat-label">待处理支持事项</div>
        </div>
      </div>
    </div>

    <!-- Quick actions row -->
    <div class="quick-actions">
      <el-button class="quick-action-btn" @click="$router.push('/projects')">
        <el-icon><Folder /></el-icon>
        <span>项目列表</span>
      </el-button>
      <el-button v-if="auth.user?.role=='engineer'" class="quick-action-btn" @click="$router.push('/my-tasks')">
        <el-icon><Edit /></el-icon>
        <span>我的待填报</span>
      </el-button>
      <el-button v-if="auth.user?.role=='manager'" class="quick-action-btn" @click="$router.push('/pending-review')">
        <el-icon><View /></el-icon>
        <span>待审阅</span>
      </el-button>
    </div>
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
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: var(--pm-space-xl);
}

/* Double-bezel stat card */
.stat-card {
  cursor: pointer;
  padding: 4px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(0,0,0,0.02), rgba(0,0,0,0.04));
  transition: transform var(--pm-duration-fast) var(--pm-ease),
              box-shadow var(--pm-duration-fast) var(--pm-ease);
  animation: cardEnter 0.5s var(--pm-ease) both;
  position: relative;
  overflow: hidden;
}

.stat-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  z-index: 2;
}

.stat-card:hover {
  transform: translateY(-1px);
  box-shadow: var(--pm-shadow);
}

.stat-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 28px 24px 24px;
  background: var(--pm-surface);
  border-radius: 10px;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.8);
  height: 100%;
  position: relative;
}

/* Semantic color bars */
.stat-card--blue::after  { background: var(--pm-accent); }
.stat-card--amber::after { background: #F59E0B; }
.stat-card--green::after { background: var(--pm-green-text); }
.stat-card--red::after   { background: var(--pm-red-text); }

/* Stat number colors */
.stat-card--blue .stat-num  { color: var(--pm-accent); }
.stat-card--amber .stat-num { color: var(--pm-amber-text); }
.stat-card--green .stat-num { color: var(--pm-green-text); }
.stat-card--red .stat-num   { color: var(--pm-red-text); }

.stat-num {
  font-size: 60px;
  font-weight: 700;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--pm-text-secondary);
  margin-top: 10px;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

/* Quick actions */
.quick-actions {
  display: flex;
  gap: 16px;
  margin-top: var(--pm-space-lg);
}

.quick-action-btn {
  flex: 1;
  height: 56px;
  border: 1px solid var(--pm-border);
  background: var(--pm-surface);
  color: var(--pm-text);
  font-size: 16px;
  font-weight: 500;
  border-radius: var(--pm-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: var(--pm-shadow);
  transition: all var(--pm-duration-fast) var(--pm-ease);
}

.quick-action-btn:hover {
  border-color: var(--pm-primary);
  color: var(--pm-primary);
  background: var(--pm-primary-light);
  box-shadow: var(--pm-shadow);
  transform: translateY(-1px);
}
</style>
