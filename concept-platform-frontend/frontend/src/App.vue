<script setup>
import { RouterView } from 'vue-router'
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getIncubationReminders, getApplicantResourceList } from '@/api/incubation'

const reminderVisible = ref(false)
const reminderData = ref({
  agreementReminders: [],
  midtermReminders: [],
  finalReminders: [],
  resourceReminders: []
})
const loading = ref(false)

const loadReminders = async () => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return
  try {
    const user = JSON.parse(userStr)
    if (user.role !== 'APPLICANT') return
    const applicantId = user.userId || user.id || user.user_id
    loading.value = true
    const [remindersRes, resourceRes] = await Promise.all([
      getIncubationReminders({ applicantId }),
      getApplicantResourceList({ applicantId })
    ])
    const resourceDone = Array.isArray(resourceRes)
      ? resourceRes.filter(r => r.status === 2)
      : []
    if (remindersRes && (remindersRes.agreementReminders || remindersRes.midtermReminders || remindersRes.finalReminders || resourceDone.length > 0)) {
      reminderData.value = {
        agreementReminders: remindersRes.agreementReminders || [],
        midtermReminders: remindersRes.midtermReminders || [],
        finalReminders: remindersRes.finalReminders || [],
        resourceReminders: resourceDone || []
      }
      const totalCount = reminderData.value.agreementReminders.length + 
                        reminderData.value.midtermReminders.length + 
                        reminderData.value.finalReminders.length +
                        reminderData.value.resourceReminders.length
      if (totalCount > 0) {
        reminderVisible.value = true
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 登录后的每次刷新都会检查一次提醒；若不提交材料，将持续弹出（用户可手动关闭）
  loadReminders()
})
</script>

<template>
  <div class="app-shell">
    <div class="app-bg">
      <div class="bg-orbit bg-orbit-1"></div>
      <div class="bg-orbit bg-orbit-2"></div>
      <div class="bg-orbit bg-orbit-3"></div>

      <div class="bg-geo geo-circle"></div>
      <div class="bg-geo geo-pentagon"></div>
      <div class="bg-geo geo-hexagon"></div>

      <div class="bg-lines lines-horizontal"></div>
      <div class="bg-lines lines-diagonal"></div>
    </div>
    <RouterView />

    <!-- 孵化提醒弹窗：包含协议签署、中期汇报、最终提交三类提醒 -->
    <el-dialog
      v-model="reminderVisible"
      title="孵化提醒"
      width="680px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-alert
        v-if="loading"
        title="正在加载提醒..."
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 12px;"
      />
      <div v-else>
        <!-- 待签署协议提醒 -->
        <div v-if="reminderData.agreementReminders.length > 0" style="margin-bottom: 16px;">
          <h4 style="margin: 0 0 8px 0; color: #e6a23c;">待签署协议（{{ reminderData.agreementReminders.length }}）</h4>
          <el-table :data="reminderData.agreementReminders" size="small" border style="width: 100%; margin-bottom: 8px;">
            <el-table-column prop="projectName" label="项目名称" min-width="150" />
            <el-table-column prop="createdAt" label="入孵时间" width="160">
              <template #default="scope">
                {{ scope.row.createdAt ? scope.row.createdAt.replace('T', ' ').substring(0, 16) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <p class="muted small">请尽快在线签署《孵化服务协议》并填写《入孵信息表》</p>
        </div>

        <!-- 中期汇报提醒 -->
        <div v-if="reminderData.midtermReminders.length > 0" style="margin-bottom: 16px;">
          <h4 style="margin: 0 0 8px 0; color: #409eff;">中期汇报提醒（{{ reminderData.midtermReminders.length }}）</h4>
          <el-table :data="reminderData.midtermReminders" size="small" border style="width: 100%; margin-bottom: 8px;">
            <el-table-column prop="projectName" label="项目名称" min-width="150" />
            <el-table-column prop="milestoneName" label="里程碑" width="120" />
            <el-table-column prop="deadline" label="截止日期" width="160">
              <template #default="scope">
                {{ scope.row.deadline ? scope.row.deadline.replace('T', ' ').substring(0, 16) : '待定' }}
              </template>
            </el-table-column>
          </el-table>
          <p class="muted small">请提交中期进展报告，专家将进行中期评审与资源调整</p>
        </div>

        <!-- 最终提交提醒 -->
        <div v-if="reminderData.finalReminders.length > 0" style="margin-bottom: 16px;">
          <h4 style="margin: 0 0 8px 0; color: #67c23a;">最终提交提醒（{{ reminderData.finalReminders.length }}）</h4>
          <el-table :data="reminderData.finalReminders" size="small" border style="width: 100%; margin-bottom: 8px;">
            <el-table-column prop="projectName" label="项目名称" min-width="150" />
            <el-table-column prop="milestoneName" label="里程碑" width="120" />
            <el-table-column prop="deadline" label="截止日期" width="160">
              <template #default="scope">
                {{ scope.row.deadline ? scope.row.deadline.replace('T', ' ').substring(0, 16) : '待定' }}
              </template>
            </el-table-column>
          </el-table>
          <p class="muted small">请提交最终成果材料，完成孵化立项，专家将进行结项/毕业评审</p>
        </div>

        <!-- 资源分配结果提醒 -->
        <div v-if="reminderData.resourceReminders.length > 0" style="margin-bottom: 16px;">
          <h4 style="margin: 0 0 8px 0; color: #f56c6c;">资源分配结果（{{ reminderData.resourceReminders.length }}）</h4>
          <el-table :data="reminderData.resourceReminders" size="small" border style="width: 100%; margin-bottom: 8px;">
            <el-table-column prop="projectName" label="项目名称" min-width="150" />
            <el-table-column prop="type" label="资源类型" width="120">
              <template #default="scope">
                <span>
                  <!-- 简单类型映射，避免重新引入工具函数 -->
                  {{ scope.row.type === 'FUNDING' ? '拨款支持' :
                     scope.row.type === 'SERVER' ? '服务器/算力' :
                     scope.row.type === 'SUPPORT' ? '技术支持' :
                     scope.row.type === 'INTERNSHIP' ? '企业实习' :
                     scope.row.type === 'MENTOR' ? '辅导/陪跑' :
                     scope.row.type === 'FUND' ? '融资路演机会' :
                     scope.row.type === 'MARKET' ? '市场渠道对接' :
                     scope.row.type === 'IP' ? '知识产权辅导' :
                     scope.row.type === 'TECH' ? '技术咨询' : scope.row.type }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column prop="createdAt" label="处理时间" width="180">
              <template #default="scope">
                {{ scope.row.createdAt ? scope.row.createdAt.replace('T', ' ').substring(0, 16) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <p class="muted small">以上资源申请已由导师/管理员处理，状态为“已完成”，请留意处理说明及后续对接。</p>
        </div>
      </div>
      <p class="muted small" style="margin-top: 12px;">
        提示：在您完成材料提交之前，每次进入平台首页都会弹出此提醒。您也可以暂时关闭弹窗，稍后再处理。
      </p>
      <template #footer>
        <el-button @click="reminderVisible = false">稍后再说</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style>
.app-shell {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.app-bg {
  position: fixed;
  inset: 0;
  background:
    radial-gradient(1200px circle at 10% 0%, rgba(0, 191, 255, 0.22), transparent 40%),
    radial-gradient(900px circle at 90% 100%, rgba(147, 112, 219, 0.24), transparent 45%),
    radial-gradient(1100px circle at 50% 80%, rgba(57, 255, 20, 0.12), transparent 48%),
    linear-gradient(180deg, #0a1929 0%, #020617 55%, #050816 100%);
  pointer-events: none;
  z-index: 0;
}

.bg-orbit {
  position: absolute;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.45);
  box-shadow: 0 0 40px rgba(15, 23, 42, 0.2);
  backdrop-filter: blur(20px);
  opacity: 0.7;
}

.bg-orbit-1 {
  width: 520px;
  height: 520px;
  top: -160px;
  left: -80px;
  background: radial-gradient(circle at 0 0, rgba(129, 140, 248, 0.35), transparent 55%);
  animation: float-orbit-1 28s linear infinite;
}

.bg-orbit-2 {
  width: 420px;
  height: 420px;
  top: 40%;
  right: -140px;
  background: radial-gradient(circle at 100% 0, rgba(56, 189, 248, 0.26), transparent 55%);
  animation: float-orbit-2 32s linear infinite;
}

.bg-orbit-3 {
  width: 560px;
  height: 560px;
  bottom: -260px;
  left: 30%;
  background: radial-gradient(circle at 50% 100%, rgba(59, 130, 246, 0.28), transparent 55%);
  animation: float-orbit-3 36s linear infinite;
}

.bg-geo {
  position: absolute;
  width: 160px;
  height: 160px;
  background: conic-gradient(from 120deg, rgba(59, 130, 246, 0.7), rgba(56, 189, 248, 0.4), rgba(129, 140, 248, 0.7), rgba(59, 130, 246, 0.7));
  mix-blend-mode: screen;
  opacity: 0.9;
  filter: blur(0.5px);
}

.geo-circle {
  top: 14%;
  right: 16%;
  border-radius: 50%;
  animation: geo-drift-1 26s ease-in-out infinite;
}

.geo-pentagon {
  bottom: 18%;
  left: 10%;
  clip-path: polygon(50% 0%, 100% 38%, 82% 100%, 18% 100%, 0% 38%);
  animation: geo-drift-2 30s ease-in-out infinite;
}

.geo-hexagon {
  top: 55%;
  left: 55%;
  clip-path: polygon(25% 3%, 75% 3%, 100% 50%, 75% 97%, 25% 97%, 0% 50%);
  animation: geo-drift-3 34s ease-in-out infinite;
}

.bg-lines {
  position: absolute;
  inset: 0;
  opacity: 0.42;
  background-size: 50px 50px;
  mask-image: radial-gradient(circle at 50% 50%, black 0%, transparent 70%);
}

.lines-horizontal {
  background-image:
    linear-gradient(
      to right,
      rgba(72, 209, 204, 0.4) 1px,
      transparent 1px
    ),
    linear-gradient(
      to bottom,
      rgba(72, 209, 204, 0.4) 1px,
      transparent 1px
    );
  animation: lines-pan-x 40s linear infinite;
}

.lines-diagonal {
  background-image:
    repeating-linear-gradient(
      135deg,
      rgba(0, 255, 255, 0.22) 0,
      rgba(0, 255, 255, 0.22) 1px,
      transparent 1px,
      transparent 18px
    );
  mix-blend-mode: soft-light;
  animation: lines-pan-y 46s linear infinite;
}

.app-shell > :not(.app-bg) {
  position: relative;
  z-index: 1;
}

@keyframes float-orbit-1 {
  0% {
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
  50% {
    transform: translate3d(20px, 40px, 0) rotate(5deg);
  }
  100% {
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
}

@keyframes float-orbit-2 {
  0% {
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
  50% {
    transform: translate3d(-30px, -30px, 0) rotate(-6deg);
  }
  100% {
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
}

@keyframes float-orbit-3 {
  0% {
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
  50% {
    transform: translate3d(10px, -40px, 0) rotate(8deg);
  }
  100% {
    transform: translate3d(0, 0, 0) rotate(0deg);
  }
}

@keyframes geo-drift-1 {
  0% {
    transform: translate3d(0, 0, 0) rotate(0deg) scale(1);
  }
  50% {
    transform: translate3d(-20px, 18px, 0) rotate(35deg) scale(1.05);
  }
  100% {
    transform: translate3d(0, 0, 0) rotate(0deg) scale(1);
  }
}

@keyframes geo-drift-2 {
  0% {
    transform: translate3d(0, 0, 0) rotate(-8deg) scale(0.95);
  }
  50% {
    transform: translate3d(18px, -16px, 0) rotate(10deg) scale(1.06);
  }
  100% {
    transform: translate3d(0, 0, 0) rotate(-8deg) scale(0.95);
  }
}

@keyframes geo-drift-3 {
  0% {
    transform: translate3d(0, 0, 0) rotate(4deg) scale(0.96);
  }
  50% {
    transform: translate3d(-24px, 22px, 0) rotate(-6deg) scale(1.04);
  }
  100% {
    transform: translate3d(0, 0, 0) rotate(4deg) scale(0.96);
  }
}

@keyframes lines-pan-x {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(-120px, 0, 0);
  }
}

@keyframes lines-pan-y {
  0% {
    transform: translate3d(0, 0, 0);
  }
  100% {
    transform: translate3d(0, -180px, 0);
  }
}
</style>
