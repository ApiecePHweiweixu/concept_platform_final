<template>
  <div class="page-wrap project-form">
    <div class="page-header">
      <div>
        <h2 class="page-title">{{ isView ? '项目详情' : (isEdit ? '编辑项目' : '科技成果信息申报') }}</h2>
        <p class="page-subtitle">{{ isView ? '查看项目详细信息' : (isEdit ? '修改项目信息' : '提交科技成果信息与附件，进入审核流程') }}</p>
      </div>
      <el-button plain @click="$router.back()">返回</el-button>
    </div>

    <el-card class="surface-card" shadow="never">
      <el-form :model="form" :rules="isView ? {} : rules" ref="formRef" label-width="140px" class="tech-form">
        <el-form-item label="成果名称" prop="projectName">
          <el-input v-model="form.projectName" :disabled="isView" placeholder="请输入成果名称" />
        </el-form-item>

        <el-form-item label="成果领域" prop="techDomain">
          <el-checkbox-group v-model="form.techDomainArray" :disabled="isView">
            <el-checkbox label="人工智能">人工智能（机器学习 / 自然语言处理 / 计算机视觉等）</el-checkbox>
            <el-checkbox label="大数据与云计算">大数据与云计算（数据治理 / 分布式计算等）</el-checkbox>
            <el-checkbox label="信息安全">信息安全（密码技术 / 数据脱敏 / 漏洞检测等）</el-checkbox>
            <el-checkbox label="物联网与嵌入式技术">物联网与嵌入式技术</el-checkbox>
            <el-checkbox label="数字人文技术">数字人文技术（古籍数字化 / 文化遗产 AI 保护等）</el-checkbox>
            <el-checkbox label="其他">其他（需注明）</el-checkbox>
          </el-checkbox-group>
          <el-input 
            v-if="form.techDomainArray.includes('其他')" 
            v-model="form.techDomainOther" 
            placeholder="请注明其他领域" 
            style="margin-top: 10px;"
            :disabled="isView"
          />
        </el-form-item>

        <el-form-item label="成果介绍" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="8" 
            :disabled="isView"
            placeholder='包括成果背景、痛点问题、技术解决方案、竞争优势分析、获奖情况、创新点等方面，可重点说明：&#10;1. 解决的行业痛点（如"政务数据跨部门共享中的隐私泄露问题"）；&#10;2. 核心技术路径（如"基于联邦学习的横向数据协同训练算法"）；&#10;3. 技术成熟度（如"已完成算法验证 / 原型系统开发"）' 
          />
        </el-form-item>

        <el-form-item label="应用场景及案例" prop="applicationScenario">
          <el-input 
            v-model="form.applicationScenario" 
            type="textarea" 
            :rows="4" 
            :disabled="isView"
            placeholder="请说明技术过往实际应用场景（含项目、工程）" 
          />
        </el-form-item>

        <el-form-item label="合作需求" prop="cooperationNeed">
          <el-checkbox-group v-model="form.cooperationNeedArray" :disabled="isView">
            <el-checkbox label="技术授权">技术授权（对外输出算法 / 软件著作权）</el-checkbox>
            <el-checkbox label="联合研发">联合研发（与企业合作开发定制化产品）</el-checkbox>
            <el-checkbox label="技术咨询">技术咨询（为行业提供解决方案指导）</el-checkbox>
            <el-checkbox label="人才联合培养">人才联合培养（与企业共建实习基地）</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="知识产权情况" prop="intellectualProperty">
          <el-input 
            v-model="form.intellectualProperty" 
            type="textarea" 
            :rows="4" 
            :disabled="isView"
            placeholder='需列出核心知识产权，注明"申请中"或"已授权"：&#10;- 专利：发明 / 实用新型专利名称、专利号；&#10;- 软著：软件著作权名称、登记号；&#10;- 核心论文：EI/SCI 收录论文标题、期刊（如有）' 
          />
        </el-form-item>

        <el-form-item label="附件材料清单" prop="attachmentUrl">
          <el-upload
            v-if="!isView"
            class="upload-demo"
            action="http://localhost:8080/api/common/upload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :limit="5"
            :file-list="fileList"
            multiple
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip muted">
                支持上传多个文件，包括：技术展示材料（2-4张清晰图片）、补充材料（专利证书、软著登记证、成果获奖证书等）、技术白皮书（可选）。文件格式：jpg/png/pdf/doc，单个文件不超过 5MB
              </div>
            </template>
          </el-upload>
          <div v-else-if="form.attachmentUrl">
            <a :href="form.attachmentUrl" target="_blank" style="color: #409eff;">{{ form.attachmentUrl }}</a>
          </div>
          <span v-else class="muted">无附件</span>
        </el-form-item>

        <el-form-item label="其他补充" prop="otherSupplement">
          <el-input 
            v-model="form.otherSupplement" 
            type="textarea" 
            :rows="3" 
            :disabled="isView"
            placeholder='可填写上述未覆盖的重要信息（无则填"无"）' 
          />
        </el-form-item>

        <el-form-item v-if="!isView">
          <el-button type="primary" @click="handleSubmit" :loading="loading">{{ isEdit ? '保存修改' : '立即申报' }}</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { addProject, getProjectById, updateProject } from '@/api/project'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const fileList = ref([])
const isEdit = ref(false)
const isView = ref(false)
const projectId = ref(null)
const originalStatus = ref(null)

const form = reactive({
  projectName: '',
  techDomainArray: [],
  techDomainOther: '',
  description: '',
  applicationScenario: '',
  cooperationNeedArray: [],
  intellectualProperty: '',
  attachmentUrl: '',
  otherSupplement: ''
})

const validateTechDomain = (rule, value, callback) => {
  if (form.techDomainArray.length === 0) {
    callback(new Error('请至少选择一个成果领域'))
  } else {
    callback()
  }
}

const validateCooperationNeed = (rule, value, callback) => {
  if (form.cooperationNeedArray.length === 0) {
    callback(new Error('请至少选择一个合作需求'))
  } else {
    callback()
  }
}

const rules = {
  projectName: [{ required: true, message: '请输入成果名称', trigger: 'blur' }],
  techDomain: [{ validator: validateTechDomain, trigger: 'change' }],
  description: [{ required: true, message: '请输入成果介绍', trigger: 'blur' }],
  applicationScenario: [{ required: true, message: '请输入应用场景及案例', trigger: 'blur' }],
  cooperationNeed: [{ validator: validateCooperationNeed, trigger: 'change' }],
  intellectualProperty: [{ required: true, message: '请输入知识产权情况', trigger: 'blur' }],
  attachmentUrl: [{ required: true, message: '请上传附件材料', trigger: 'change' }]
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.attachmentUrl = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
    fileList.value = []
  }
}

const handleUploadError = (error) => {
  console.error(error)
  ElMessage.error('上传失败，请检查网络或后端接口')
}

// 解析techDomain字符串为数组（逗号分隔）
const parseTechDomain = (techDomainStr) => {
  if (!techDomainStr) return []
  // 直接按逗号分割
  if (typeof techDomainStr === 'string') {
    if (techDomainStr.includes(',')) {
      return techDomainStr.split(',').map(s => s.trim()).filter(s => s)
    }
    return [techDomainStr.trim()].filter(s => s)
  }
  return []
}

// 加载项目数据
const loadProject = async (id) => {
  loading.value = true
  try {
    const res = await getProjectById(id)
    const project = res || {}
    
    // 填充表单数据
    form.projectName = project.projectName || ''
    form.description = project.description || ''
    form.applicationScenario = project.applicationScenario || ''
    form.intellectualProperty = project.intellectualProperty || ''
    form.attachmentUrl = project.attachmentUrl || ''
    form.otherSupplement = project.otherSupplement || ''
    
    // 解析技术领域
    const techDomains = parseTechDomain(project.techDomain)
    form.techDomainArray = techDomains.filter(d => !d.startsWith('其他：'))
    const otherDomain = techDomains.find(d => d.startsWith('其他：'))
    if (otherDomain) {
      form.techDomainArray.push('其他')
      form.techDomainOther = otherDomain.replace('其他：', '')
    }
    
    // 解析合作需求
    form.cooperationNeedArray = parseTechDomain(project.cooperationNeed)
    
    // 设置文件列表（如果有附件）
    if (project.attachmentUrl) {
      fileList.value = [{
        name: '附件',
        url: project.attachmentUrl
      }]
    }
    
    // 保存原始状态
    originalStatus.value = project.status
  } catch (error) {
    console.error('加载项目数据失败', error)
    ElMessage.error('加载项目数据失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value || isView.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const userStr = localStorage.getItem('user')
        let user = {}
        if (userStr) {
          try {
            user = JSON.parse(userStr)
          } catch (e) {
            console.error('JSON Parse Error', e)
          }
        }

        // 处理成果领域（多选转逗号分隔字符串）
        let techDomainStr = form.techDomainArray.join(',')
        if (form.techDomainArray.includes('其他') && form.techDomainOther) {
          techDomainStr = [...form.techDomainArray.filter(d => d !== '其他'), `其他：${form.techDomainOther}`].join(',')
        }
        
        // 处理合作需求（多选转逗号分隔字符串）
        const cooperationNeedStr = form.cooperationNeedArray.join(',')

        const submitData = {
          projectName: form.projectName,
          techDomain: techDomainStr,
          description: form.description,
          applicationScenario: form.applicationScenario,
          cooperationNeed: cooperationNeedStr,
          intellectualProperty: form.intellectualProperty,
          attachmentUrl: form.attachmentUrl,
          otherSupplement: form.otherSupplement || '无'
        }

        const userId = user.userId || user.user_id || user.id
        submitData.applicantId = userId || 1
        if (!userId) {
          ElMessage.warning('当前未读取到用户ID，使用测试ID: 1')
        }

        if (isEdit.value && projectId.value) {
          // 更新项目
          submitData.projectId = projectId.value
          // 保持原有状态
          if (originalStatus.value !== null && originalStatus.value !== undefined) {
            submitData.status = originalStatus.value
          }
          await updateProject(submitData)
          ElMessage.success('项目修改成功')
        } else {
          // 新建项目
          submitData.status = 1
          await addProject(submitData)
          ElMessage.success('项目申报成功')
        }
        router.push('/')
      } catch (error) {
        console.error(error)
        ElMessage.error('操作失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const resetForm = () => {
  if (!formRef.value || isView.value) return
  formRef.value.resetFields()
  fileList.value = []
  form.techDomainArray = []
  form.techDomainOther = ''
  form.cooperationNeedArray = []
}

// 初始化
onMounted(async () => {
  const id = route.query.id
  const view = route.query.view
  
  if (view === 'true') {
    isView.value = true
  }
  
  if (id) {
    projectId.value = parseInt(id)
    isEdit.value = true
    await loadProject(projectId.value)
  }
})
</script>

<style scoped>
.project-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 900px;
}
</style>
