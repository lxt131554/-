import { getMockData, setMockUser, getMockUser, nextReportId } from './data'

// Helper: parse request data into key-value pairs
// Handles URLSearchParams strings, FormData, and JSON strings
function parseRequestBody(data) {
  if (!data) return {}
  if (typeof data === 'string') {
    try {
      return JSON.parse(data)
    } catch {
      const params = new URLSearchParams(data)
      const result = {}
      for (const [key, value] of params.entries()) {
        result[key] = value
      }
      return result
    }
  }
  if (data instanceof FormData || (typeof data.get === 'function' && typeof data.entries === 'function')) {
    const result = {}
    for (const [key, value] of data.entries()) {
      result[key] = value
    }
    return result
  }
  return data
}

// Generate a mock axios response for the given config.
// Returns null if no mock matches, meaning the real adapter should handle it.
async function getMockResponse(config) {
  const mockData = getMockData()
  const url = config.url
  const method = config.method?.toLowerCase()
  const params = config.params || {}
  const body = parseRequestBody(config.data)

  // Simulate network delay
  await new Promise(r => setTimeout(r, 200 + Math.random() * 300))

  let result

  // ======================== Auth endpoints ========================
  if (url === '/auth/login' && method === 'post') {
    const username = body.username
    const user = setMockUser(username)
    if (user) {
      result = { code: 200, message: 'success', data: { id: user.id, username: user.username, realName: user.realName, role: user.role, dept: user.dept } }
    } else {
      result = { code: 401, message: '用户名或密码错误', data: null }
    }
  }
  else if (url === '/auth/current-user' && method === 'get') {
    const user = getMockUser()
    if (user) {
      result = { code: 200, message: 'success', data: user }
    } else {
      result = { code: 401, message: '未登录', data: null }
    }
  }
  else if (url === '/auth/logout' && method === 'post') {
    setMockUser(null)
    result = { code: 200, message: '已退出', data: null }
  }

  // ======================== Project endpoints ========================
  else if (url === '/projects' && method === 'get') {
    const page = parseInt(params.page) || 1
    const size = parseInt(params.size) || 10
    const keyword = params.keyword
    const status = params.status
    let list = [...mockData.projects]
    if (keyword) {
      list = list.filter(p => p.name.includes(keyword))
    }
    if (status) {
      list = list.filter(p => p.status === status)
    }
    result = { code: 200, message: 'success', data: { records: list, total: list.length, size, current: page, pages: 1 } }
  }
  else if (url?.match(/^\/projects\/\d+$/) && method === 'get') {
    const id = parseInt(url.split('/')[2])
    const project = mockData.projects.find(p => p.id === id)
    result = { code: 200, message: 'success', data: project || null }
  }
  else if (url === '/projects' && method === 'post') {
    const newProject = {
      id: Date.now(),
      name: body.name,
      description: body.description || '',
      status: 'active',
      createUserId: getMockUser()?.id,
      createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
      customerLevel: body.customerLevel || '',
      contacts: body.contacts || '',
      achievementType: body.achievementType || '',
      approvalRequirements: body.approvalRequirements || '',
      canUndertake: body.canUndertake || '',
      mainRisks: body.mainRisks || '',
      keyConstraints: body.keyConstraints || '',
      deliverableRequirements: body.deliverableRequirements || '',
      approvalPath: body.approvalPath || '',
      hrAllocation: body.hrAllocation || '',
      expectedOutputs: body.expectedOutputs || '',
      coreMaterials: body.coreMaterials || '',
      teamSetup: body.teamSetup || '',
      coreStrategy: body.coreStrategy || '',
      bidSituation: body.bidSituation || '',
      procurementInfo: body.procurementInfo || '',
      acquisitionResult: body.acquisitionResult || '',
      projectImportance: body.projectImportance || '',
      achievementDirection: body.achievementDirection || ''
    }
    mockData.projects.push(newProject)
    mockData.projectMembers[newProject.id] = []
    mockData.stages[newProject.id] = []
    result = { code: 200, message: 'success', data: newProject }
  }
  else if (url === '/projects/import/oa' && method === 'post') {
    result = {
      code: 200,
      message: 'success',
      data: {
        totalRows: 0,
        createdCount: 0,
        updatedCount: 0,
        skippedCount: 0,
        matchedManagerCount: 0,
        missingManagerCount: 0,
        missingManagers: [],
        items: []
      }
    }
  }
  else if (url?.match(/^\/projects\/\d+$/) && method === 'put') {
    const id = parseInt(url.split('/')[2])
    const project = mockData.projects.find(p => p.id === id)
    if (project) {
      if (body.name !== undefined) project.name = body.name
      if (body.description !== undefined) project.description = body.description
      if (body.status !== undefined) project.status = body.status
      if (body.customerLevel !== undefined) project.customerLevel = body.customerLevel
      if (body.contacts !== undefined) project.contacts = body.contacts
      if (body.achievementType !== undefined) project.achievementType = body.achievementType
      if (body.approvalRequirements !== undefined) project.approvalRequirements = body.approvalRequirements
      if (body.canUndertake !== undefined) project.canUndertake = body.canUndertake
      if (body.mainRisks !== undefined) project.mainRisks = body.mainRisks
      if (body.keyConstraints !== undefined) project.keyConstraints = body.keyConstraints
      if (body.deliverableRequirements !== undefined) project.deliverableRequirements = body.deliverableRequirements
      if (body.approvalPath !== undefined) project.approvalPath = body.approvalPath
      if (body.hrAllocation !== undefined) project.hrAllocation = body.hrAllocation
      if (body.expectedOutputs !== undefined) project.expectedOutputs = body.expectedOutputs
      if (body.coreMaterials !== undefined) project.coreMaterials = body.coreMaterials
      if (body.teamSetup !== undefined) project.teamSetup = body.teamSetup
      if (body.coreStrategy !== undefined) project.coreStrategy = body.coreStrategy
      if (body.bidSituation !== undefined) project.bidSituation = body.bidSituation
      if (body.procurementInfo !== undefined) project.procurementInfo = body.procurementInfo
      if (body.acquisitionResult !== undefined) project.acquisitionResult = body.acquisitionResult
      if (body.projectImportance !== undefined) project.projectImportance = body.projectImportance
      if (body.achievementDirection !== undefined) project.achievementDirection = body.achievementDirection
    }
    result = { code: 200, message: 'success', data: project || null }
  }
  else if (url?.match(/^\/projects\/\d+$/) && method === 'delete') {
    const id = parseInt(url.split('/')[2])
    const idx = mockData.projects.findIndex(p => p.id === id)
    if (idx !== -1) mockData.projects.splice(idx, 1)
    delete mockData.projectMembers[id]
    delete mockData.stages[id]
    result = { code: 200, message: 'success', data: null }
  }
  else if (url?.match(/^\/projects\/\d+\/members$/) && method === 'get') {
    const projectId = parseInt(url.split('/')[2])
    const allMembers = mockData.projectMembers[projectId] || []
    result = { code: 200, message: 'success', data: allMembers.filter(function(m) { return m.status !== 'pending' }) }
  }
  else if (url?.match(/^\/projects\/\d+\/members$/) && method === 'post') {
    const projectId = parseInt(url.split('/')[2])
    const members = mockData.projectMembers[projectId] || []
    const user = Object.values(mockData.users).find(u => u.id === parseInt(body.userId) || u.id === body.userId)
    const newMember = {
      id: Date.now(),
      userId: parseInt(body.userId) || body.userId,
      realName: user?.realName || '',
      dept: user?.dept || '',
      roleInProject: body.roleInProject,
      status: 'pending'
    }
    members.push(newMember)
    mockData.projectMembers[projectId] = members
    result = { code: 200, message: 'success', data: newMember }
  }
  else if (url?.match(/^\/projects\/\d+\/members\/\d+$/) && method === 'delete') {
    const parts = url.split('/')
    const projectId = parseInt(parts[2])
    const memberId = parseInt(parts[4])
    const members = mockData.projectMembers[projectId] || []
    const idx = members.findIndex(m => m.id === memberId)
    if (idx !== -1) members.splice(idx, 1)
    result = { code: 200, message: 'success', data: null }
  }
  else if (url?.match(/^\/projects\/\d+\/members\/\d+\/confirm$/) && method === 'put') {
    const parts = url.split('/')
    const projectId = parseInt(parts[2])
    const memberId = parseInt(parts[4])
    const members = mockData.projectMembers[projectId] || []
    const member = members.find(m => m.id === memberId)
    if (member) {
      const currentUser = getMockUser()
      if (member.userId !== currentUser?.id) {
        result = { code: 403, message: '无权操作', data: null }
      } else {
        member.status = 'confirmed'
        result = { code: 200, message: 'success', data: null }
      }
    } else {
      result = { code: 404, message: '成员不存在', data: null }
    }
  }
  else if (url === '/projects/members/pending' && method === 'get') {
    const currentUser = getMockUser()
    const pendingList = []
    Object.entries(mockData.projectMembers).forEach(function([projectId, members]) {
      members.forEach(function(m) {
        if (m.userId === currentUser?.id && m.status === 'pending') {
          const project = mockData.projects.find(function(p) { return p.id === parseInt(projectId) })
          pendingList.push({
            id: m.id,
            projectId: parseInt(projectId),
            projectName: project ? project.name : '',
            roleInProject: m.roleInProject
          })
        }
      })
    })
    result = { code: 200, message: 'success', data: pendingList }
  }

  // ======================== Stage endpoints ========================
  else if (url?.match(/^\/projects\/\d+\/stages$/) && method === 'get') {
    const projectId = parseInt(url.split('/')[2])
    result = { code: 200, message: 'success', data: mockData.stages[projectId] || [] }
  }
  else if (url?.match(/^\/projects\/\d+\/stages$/) && method === 'post') {
    const projectId = parseInt(url.split('/')[2])
    const stages = mockData.stages[projectId] || []
    const newStage = {
      id: Date.now(),
      projectId,
      stageName: body.stageName,
      description: body.description || '',
      planStart: body.planStart,
      planEnd: body.planEnd,
      actualStart: null,
      actualEnd: null,
      status: 'pending',
      assigneeId: body.assigneeId,
      assigneeName: '',
      sortOrder: body.sortOrder || 0,
      latestReport: null
    }
    stages.push(newStage)
    mockData.stages[projectId] = stages
    mockData.reports[newStage.id] = []
    result = { code: 200, message: 'success', data: newStage }
  }
  else if (url?.match(/^\/projects\/\d+\/stages\/\d+$/) && method === 'put') {
    const parts = url.split('/')
    const projectId = parseInt(parts[2])
    const stageId = parseInt(parts[4])
    const stageList = mockData.stages[projectId] || []
    const stage = stageList.find(s => s.id === stageId)
    if (stage) {
      if (body.stageName !== undefined) stage.stageName = body.stageName
      if (body.description !== undefined) stage.description = body.description
      if (body.planStart !== undefined) stage.planStart = body.planStart
      if (body.planEnd !== undefined) stage.planEnd = body.planEnd
      if (body.assigneeId !== undefined) stage.assigneeId = body.assigneeId
      if (body.status !== undefined) stage.status = body.status
    }
    result = { code: 200, message: 'success', data: stage || null }
  }
  else if (url?.match(/^\/projects\/\d+\/stages\/\d+$/) && method === 'delete') {
    const parts = url.split('/')
    const projectId = parseInt(parts[2])
    const stageId = parseInt(parts[4])
    const stageList = mockData.stages[projectId] || []
    const idx = stageList.findIndex(s => s.id === stageId)
    if (idx !== -1) stageList.splice(idx, 1)
    delete mockData.reports[stageId]
    result = { code: 200, message: 'success', data: null }
  }
  else if (url === '/stages/my-tasks' && method === 'get') {
    const user = getMockUser()
    const tasks = []
    Object.values(mockData.stages).forEach(stageList => {
      stageList.forEach(s => {
        if (s.assigneeId === user?.id) tasks.push(s)
      })
    })
    result = { code: 200, message: 'success', data: tasks }
  }

  // ======================== Stage detail endpoint ========================
  else if (url?.match(/^\/stages\/\d+\/detail$/) && method === 'get') {
    const stageId = parseInt(url.split('/')[2])
    // Find the stage across all projects
    let stageInfo = null
    Object.values(mockData.stages).forEach(stageList => {
      stageList.forEach(s => {
        if (s.id === stageId) stageInfo = { ...s }
      })
    })
    if (!stageInfo) {
      result = { code: 404, message: '阶段不存在', data: null }
    } else {
      // Populate project name
      const project = mockData.projects.find(p => p.id === stageInfo.projectId)
      stageInfo.projectName = project ? project.name : ''

      // Get reports for this stage
      const stageReports = (mockData.reports[stageId] || []).map(r => ({ ...r, attachmentData: undefined }))

      // Get deviations for this stage
      const stageDeviations = mockData.deviations.filter(d => d.stageId === stageId)

      result = {
        code: 200, message: 'success', data: {
          stage: stageInfo,
          reports: stageReports,
          deviations: stageDeviations,
          projectId: stageInfo.projectId,
          projectName: stageInfo.projectName
        }
      }
    }
  }

  // ======================== Report endpoints ========================
  else if (url?.match(/^\/stages\/\d+\/reports$/) && method === 'get') {
    const stageId = parseInt(url.split('/')[2])
    result = { code: 200, message: 'success', data: mockData.reports[stageId] || [] }
  }
  else if (url?.match(/^\/stages\/\d+\/reports$/) && method === 'post') {
    const stageId = parseInt(url.split('/')[2])
    const user = getMockUser()

    let stageInfo = null
    let projectName = ''
    Object.values(mockData.stages).forEach(stageList => {
      stageList.forEach(s => {
        if (s.id === stageId) {
          stageInfo = s
          const p = mockData.projects.find(pj => pj.id === s.projectId)
          projectName = p?.name || ''
        }
      })
    })

    const newReport = {
      id: nextReportId(),
      stageId,
      projectId: stageInfo?.projectId || 1,
      content: body.content || '',
      progressRate: parseInt(body.progressRate) || 0,
      problem: body.problem || '',
      qualityControl: body.qualityControl || '',
      resultSummary: body.resultSummary || '',
      coordinationNote: body.coordinationNote || '',
      deptReviewNote: body.deptReviewNote || '',
      actualStart: body.actualStart || null,
      actualEnd: body.actualEnd || null,
      reviewStatus: 'pending',
      reviewComment: null,
      submitUserId: user?.id,
      submitUserName: user?.realName || '',
      submitTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
      reviewUserId: null,
      reviewTime: null,
      createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
      attachmentName: body.file ? '附件_' + Date.now() + '.pdf' : null,
      stageName: stageInfo?.stageName || '',
      projectName
    }

    const reports = mockData.reports[stageId] || []
    reports.push(newReport)
    mockData.reports[stageId] = reports

    if (stageInfo) {
      stageInfo.status = 'submitted'
      stageInfo.latestReport = {
        id: newReport.id,
        content: newReport.content,
        progressRate: newReport.progressRate,
        problem: newReport.problem,
        reviewStatus: newReport.reviewStatus
      }
    }

    result = { code: 200, message: 'success', data: newReport }
  }
  else if (url === '/reports/pending' && method === 'get') {
    const pending = []
    Object.values(mockData.reports).forEach(reportList => {
      reportList.forEach(r => {
        if (r.reviewStatus === 'pending') pending.push(r)
      })
    })
    // Populate projectName and stageName
    pending.forEach(function(r) {
      if (!r.projectName) {
        var p = mockData.projects.find(function(x) { return x.id === r.projectId })
        if (p) r.projectName = p.name
      }
      if (!r.stageName && r.stageId) {
        var stages = mockData.stages[r.projectId] || []
        var s = stages.find(function(x) { return x.id === r.stageId })
        if (s) r.stageName = s.stageName
      }
    })
    var total = pending.length
    var page = parseInt(params.page) || 1
    var size = Math.min(parseInt(params.size) || 10, 100)
    var start = (page - 1) * size
    result = { code: 200, message: 'success', data: { records: pending.slice(start, start + size), total: total, size: size, current: page } }
  }
  else if (url?.match(/^\/reports\/\d+\/review$/) && method === 'post') {
    const reportId = parseInt(url.split('/')[2])
    Object.values(mockData.reports).forEach(reportList => {
      reportList.forEach(r => {
        if (r.id === reportId) {
          r.reviewStatus = body.reviewStatus
          r.reviewComment = body.reviewComment || null
        }
      })
    })
    result = { code: 200, message: 'success', data: null }
  }
  else if (url?.match(/^\/reports\/\d+\/attachment$/) && method === 'get') {
    // Return a tiny PDF placeholder
    const pdfPlaceholder = '%PDF-1.4 mock file'
    return {
      data: pdfPlaceholder,
      status: 200,
      statusText: 'OK',
      headers: { 'content-type': 'application/pdf', 'content-disposition': 'attachment; filename="demo.pdf"' },
      config,
      request: {}
    }
  }

  // ======================== User endpoints ========================
  else if (url === '/users' && method === 'get') {
    var allUsers = Object.values(mockData.users).map(u => ({...u}))
    var total = allUsers.length
    var page = parseInt(params.page) || 1
    var size = Math.min(parseInt(params.size) || 10, 100)
    var start = (page - 1) * size
    result = { code: 200, message: 'success', data: { records: allUsers.slice(start, start + size), total: total, size: size, current: page } }
  }
  else if (url === '/users' && method === 'post') {
    const newUser = {
      id: Date.now(),
      username: body.username,
      realName: body.realName,
      password: '',
      role: body.role || 'engineer',
      dept: body.dept || '',
      enabled: true
    }
    mockData.users[body.username] = newUser
    result = { code: 200, message: 'success', data: { ...newUser } }
  }
  else if (url?.match(/^\/users\/\d+$/) && method === 'put') {
    const id = parseInt(url.split('/')[2])
    let user = Object.values(mockData.users).find(u => u.id === id)
    if (user) {
      if (body.realName !== undefined) user.realName = body.realName
      if (body.role !== undefined) user.role = body.role
      if (body.dept !== undefined) user.dept = body.dept
      if (body.password !== undefined && body.password !== '') {
        // password change in mock is a no-op
      }
      result = { code: 200, message: 'success', data: { ...user } }
    } else {
      result = { code: 404, message: '用户不存在', data: null }
    }
  }
  else if (url?.match(/^\/users\/\d+\/toggle$/) && method === 'put') {
    const id = parseInt(url.split('/')[2])
    let user = Object.values(mockData.users).find(u => u.id === id)
    if (user) {
      user.enabled = !user.enabled
      result = { code: 200, message: 'success', data: null }
    } else {
      result = { code: 404, message: '用户不存在', data: null }
    }
  }

  // ======================== Dashboard endpoints ========================
  else if (url === '/dashboard' && method === 'get') {
    const user = getMockUser()
    let stats = {}
    const now = new Date()
    now.setHours(0, 0, 0, 0)

    // Helper: find projects for a user
    function getUserProjectIds(uid) {
      const ids = []
      Object.values(mockData.projectMembers).forEach(mlist => {
        mlist.forEach(m => {
          if (m.userId === uid && m.status === 'confirmed') ids.push(m.projectId)
        })
      })
      return [...new Set(ids)]
    }

    // Helper: build project cards
    function buildProjectCards(pids) {
      return pids.map(pid => {
        const p = mockData.projects.find(x => x.id === pid)
        if (!p) return null
        const stages = mockData.stages[pid] || []
        const activeStage = stages.find(s => s.status === 'in_progress' || s.status === 'submitted') || stages[stages.length - 1] || {}
        const deviations = mockData.deviations[pid] || []
        const hasDeviation = deviations.some(d => d.status === 'open')
        return {
          projectId: pid,
          projectName: p.name,
          status: p.status,
          currentStage: activeStage.stageName || '未开始',
          planEnd: activeStage.planEnd || null,
          hasDeviation
        }
      }).filter(Boolean)
    }

    if (user?.role === 'engineer') {
      const pidList = getUserProjectIds(user.id)
      let overdue = 0, nearDeadline = 0
      const pendingStagesList = []
      Object.values(mockData.stages).forEach(stageList => {
        stageList.forEach(s => {
          if (s.assigneeId === user?.id && s.status !== 'completed' && s.planEnd) {
            const endDate = new Date(s.planEnd)
            endDate.setHours(0, 0, 0, 0)
            const daysLeft = Math.ceil((endDate - now) / (1000 * 60 * 60 * 24))
            if (daysLeft < 0) overdue++
            else if (daysLeft <= 7) nearDeadline++
          }
          if (s.assigneeId === user?.id && (s.status === 'pending' || s.status === 'in_progress')) {
            const p = mockData.projects.find(x => x.id === s.projectId)
            pendingStagesList.push({
              stageId: s.id,
              projectName: p ? p.name : '',
              stageName: s.stageName,
              planEnd: s.planEnd,
              status: s.status
            })
          }
        })
      })
      stats = {
        todo: 2, returned: 1, overdue, nearDeadline,
        myProjects: buildProjectCards(pidList),
        pendingStages: pendingStagesList.slice(0, 5)
      }
    } else if (user?.role === 'manager') {
      const pidList = getUserProjectIds(user.id)
      let reviewOverdue = 0
      const pendingReviewItems = []
      Object.values(mockData.reports).forEach(reportList => {
        reportList.forEach(r => {
          if (r.reviewStatus === 'pending') {
            if (r.submitTime) {
              const submitDate = new Date(r.submitTime)
              const hoursSince = (now - submitDate) / (1000 * 60 * 60)
              if (hoursSince > 48) reviewOverdue++
            }
            // Only include if this report belongs to a project this manager manages
            if (pidList.includes(r.projectId)) {
              const p = mockData.projects.find(x => x.id === r.projectId)
              const stage = (mockData.stages[r.projectId] || []).find(x => x.id === r.stageId)
              pendingReviewItems.push({
                reportId: r.id,
                projectName: p ? p.name : '',
                stageName: stage ? stage.stageName : '',
                submitUserName: '张工',
                submitTime: r.submitTime
              })
            }
          }
        })
      })
      let nearDeadlineCount = 0
      pidList.forEach(pid => {
        (mockData.stages[pid] || []).forEach(s => {
          if (s.status !== 'completed' && s.planEnd) {
            const endDate = new Date(s.planEnd)
            endDate.setHours(0, 0, 0, 0)
            const daysLeft = Math.ceil((endDate - now) / (1000 * 60 * 60 * 24))
            if (daysLeft >= 0 && daysLeft <= 7) nearDeadlineCount++
          }
        })
      })
      stats = {
        myProjectCount: pidList.length,
        pendingReview: 1, pendingAchievement: 1, openDeviations: 2,
        pendingSupports: 2, pendingChanges: 1, reviewOverdue,
        nearDeadline: nearDeadlineCount,
        pendingReviewItems: pendingReviewItems.slice(0, 5),
        myProjects: buildProjectCards(pidList)
      }
    } else if (user?.role === 'leader') {
      stats = { openDeviations: 2, pendingSupports: 2, pendingReview: 1, pendingChanges: 1 }
    }
    result = { code: 200, message: 'success', data: stats }
  }

  // ======================== Notification endpoints ========================
  else if (url === '/notifications' && method === 'get') {
    var user = getMockUser()
    var list = []
    var now = new Date().toISOString().replace('T', ' ').slice(0, 19)

    if (user?.role === 'engineer') {
      // Returned reports (stages with status in_progress assigned to this user)
      Object.values(mockData.stages).forEach(function(stageList) {
        stageList.forEach(function(s) {
          if (s.assigneeId === user.id && s.status === 'in_progress') {
            list.push({
              type: 'returned',
              message: '「' + s.stageName + '」被退回，需重新填报',
              url: '/my-tasks/' + s.id + '/report',
              time: now
            })
          }
        })
      })
      // Overdue stages
      var today = new Date()
      today.setHours(0, 0, 0, 0)
      Object.values(mockData.stages).forEach(function(stageList) {
        stageList.forEach(function(s) {
          if (s.assigneeId === user.id && s.planEnd && s.status !== 'completed') {
            var endDate = new Date(s.planEnd)
            endDate.setHours(0, 0, 0, 0)
            if (endDate < today) {
              var days = Math.ceil((today - endDate) / (1000 * 60 * 60 * 24))
              list.push({
                type: 'overdue',
                message: '「' + s.stageName + '」已逾期 ' + days + ' 天',
                url: '/my-tasks/' + s.id + '/report',
                time: s.planEnd
              })
            }
          }
        })
      })
    }

    if (user?.role === 'manager') {
      // Pending reviews
      var pendingReview = []
      Object.values(mockData.reports).forEach(function(reportList) {
        reportList.forEach(function(r) {
          if (r.reviewStatus === 'pending') pendingReview.push(r)
        })
      })
      if (pendingReview.length > 0) {
        list.push({ type: 'review', message: pendingReview.length + ' 条阶段填报待审阅', url: '/pending-review', time: now })
      }
      // Pending achievements (reports with attachments)
      var pendingAchievement = pendingReview.filter(function(r) { return r.attachmentName }).length
      if (pendingAchievement > 0) {
        list.push({ type: 'achievement', message: pendingAchievement + ' 项成果待审核', url: '/pending-review', time: now })
      }
      // Open deviations
      var openDev = mockData.deviations.filter(function(d) { return d.status === 'open' }).length
      if (openDev > 0) {
        list.push({ type: 'deviation', message: openDev + ' 项偏差未关闭', url: '/deviations', time: now })
      }
      // Pending supports
      var pendingSup = mockData.supportItems.filter(function(s) { return s.status === 'pending' }).length
      if (pendingSup > 0) {
        list.push({ type: 'support', message: pendingSup + ' 项支持事项待处理', url: '/supports', time: now })
      }
    }

    if (user?.role === 'leader') {
      // Open deviations
      var openDevL = mockData.deviations.filter(function(d) { return d.status === 'open' }).length
      if (openDevL > 0) {
        list.push({ type: 'deviation', message: openDevL + ' 项偏差未关闭', url: '/deviations', time: now })
      }
      // Pending supports
      var pendingSupL = mockData.supportItems.filter(function(s) { return s.status === 'pending' }).length
      if (pendingSupL > 0) {
        list.push({ type: 'support', message: pendingSupL + ' 项支持事项待处理', url: '/supports', time: now })
      }
      // Pending changes
      var pendingChanges = 0
      Object.values(mockData.changes).forEach(function(changesList) {
        changesList.forEach(function(c) { if (c.status === 'pending') pendingChanges++ })
      })
      if (pendingChanges > 0) {
        list.push({ type: 'change', message: pendingChanges + ' 项变更待确认', url: '/deviations', time: now })
      }
    }

    result = { code: 200, message: 'success', data: list }
  }

  // ======================== Deviation endpoints ========================
  else if (url === '/deviations' && method === 'get') {
    let list = [...mockData.deviations]
    if (params.status) {
      list = list.filter(d => d.status === params.status)
    }
    // Populate projectName and stageName from mock data
    list.forEach(function(d) {
      if (!d.projectName) {
        var p = mockData.projects.find(function(x) { return x.id === d.projectId })
        if (p) d.projectName = p.name
      }
      if (!d.stageName && d.stageId) {
        var stages = mockData.stages[d.projectId] || []
        var s = stages.find(function(x) { return x.id === d.stageId })
        if (s) d.stageName = s.stageName
      }
    })
    var total = list.length
    var page = parseInt(params.page) || 1
    var size = Math.min(parseInt(params.size) || 10, 100)
    var start = (page - 1) * size
    result = { code: 200, message: 'success', data: { records: list.slice(start, start + size), total: total, size: size, current: page } }
  }
  else if (url === '/deviations' && method === 'post') {
    const user = getMockUser()
    const newDeviation = {
      id: Date.now(),
      projectId: body.projectId,
      stageId: body.stageId || null,
      reportId: body.reportId || null,
      type: body.type || 'manual',
      description: body.description,
      reason: body.reason || '',
      impact: body.impact || '',
      status: 'open',
      createUserId: user?.id,
      createUserName: user?.realName || '',
      createTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
      closeTime: null,
      projectName: '',
      stageName: ''
    }
    mockData.deviations.push(newDeviation)
    result = { code: 200, message: 'success', data: newDeviation }
  }
  else if (url?.match(/^\/deviations\/\d+\/close$/) && method === 'put') {
    const id = parseInt(url.split('/')[2])
    const deviation = mockData.deviations.find(d => d.id === id)
    if (deviation) {
      deviation.status = 'closed'
      deviation.closeTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    result = { code: 200, message: 'success', data: deviation || null }
  }

  // ======================== Support endpoints ========================
  else if (url === '/supports' && method === 'get') {
    let list = [...mockData.supportItems]
    if (params.status) {
      list = list.filter(s => s.status === params.status)
    }
    // Populate projectName from mock projects
    list.forEach(function(s) {
      if (!s.projectName) {
        var p = mockData.projects.find(function(x) { return x.id === s.projectId })
        if (p) s.projectName = p.name
      }
    })
    var total = list.length
    var page = parseInt(params.page) || 1
    var size = Math.min(parseInt(params.size) || 10, 100)
    var start = (page - 1) * size
    result = { code: 200, message: 'success', data: { records: list.slice(start, start + size), total: total, size: size, current: page } }
  }
  else if (url === '/supports' && method === 'post') {
    const user = getMockUser()
    const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
    const newSupport = {
      id: Date.now(),
      projectId: body.projectId,
      title: body.title,
      content: body.content,
      applicantId: user?.id,
      applicantName: user?.realName || '',
      handlerId: body.handlerId || null,
      handlerName: '',
      expectTime: body.expectTime || '',
      status: 'pending',
      reply: null,
      createTime: now,
      updateTime: now,
      projectName: ''
    }
    mockData.supportItems.push(newSupport)
    result = { code: 200, message: 'success', data: newSupport }
  }
  else if (url?.match(/^\/supports\/\d+\/resolve$/) && method === 'put') {
    const id = parseInt(url.split('/')[2])
    const supportItem = mockData.supportItems.find(s => s.id === id)
    if (supportItem) {
      supportItem.status = 'resolved'
      supportItem.reply = body.reply || ''
      supportItem.resolveNote = body.resolveNote || ''
      supportItem.updateTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    result = { code: 200, message: 'success', data: supportItem || null }
  }

  // ======================== Leader Dashboard endpoint ========================
  else if (url === '/leader-dashboard' && method === 'get') {
    var openDevList = mockData.deviations.filter(function(d) { return d.status === 'open' })
    var pendingSupList = mockData.supportItems.filter(function(s) { return s.status === 'pending' })
    var pendingChangeList = []
    Object.values(mockData.changes || {}).forEach(function(clist) {
      clist.forEach(function(c) { if (c.status === 'pending') pendingChangeList.push(c) })
    })
    // Enrich changes with project names
    pendingChangeList.forEach(function(c) {
      var p = mockData.projects.find(function(x) { return x.id === c.projectId })
      if (p) c.projectName = p.name
    })
    result = {
      code: 200, message: 'success', data: {
        activeProjects: mockData.projects.filter(function(p) { return p.status === 'active' }).length,
        completedProjects: mockData.projects.filter(function(p) { return p.status === 'completed' }).length,
        openDeviations: openDevList.length,
        openDeviationList: openDevList,
        pendingSupports: pendingSupList.length,
        pendingSupportList: pendingSupList,
        pendingChanges: pendingChangeList,
        pendingReports: [],
        projects: mockData.projects
      }
    }
  }

  // ======================== Statistics endpoint ========================
  else if (url === '/statistics' && method === 'get') {
    var allProjects = mockData.projects;
    var totalProjects = allProjects.length;
    var activeProjects = allProjects.filter(function(p) { return p.status === 'active'; }).length;
    var completedProjects = allProjects.filter(function(p) { return p.status === 'completed'; }).length;

    // Overdue stages
    var today = new Date();
    today.setHours(0, 0, 0, 0);
    var allStages = [];
    Object.values(mockData.stages).forEach(function(stageList) {
      stageList.forEach(function(s) { allStages.push(s); });
    });
    var overdueStages = allStages.filter(function(s) {
      if (!s.planEnd || s.status === 'completed') return false;
      var endDate = new Date(s.planEnd);
      endDate.setHours(0, 0, 0, 0);
      return endDate < today;
    }).length;

    // Open deviations
    var openDeviations = mockData.deviations.filter(function(d) { return d.status === 'open'; }).length;

    // Pending supports
    var pendingSupports = mockData.supportItems.filter(function(s) { return s.status === 'pending'; }).length;

    // By department
    var allUsers = Object.values(mockData.users);
    var userDeptMap = {};
    allUsers.forEach(function(u) { userDeptMap[u.id] = u.dept || '未知'; });

    var projectsByDept = {};
    allProjects.forEach(function(p) {
      var dept = userDeptMap[p.createUserId] || '未知';
      projectsByDept[dept] = (projectsByDept[dept] || 0) + 1;
    });

    var allExperiences = Object.values(mockData.experiences);
    var experiencesByDept = {};
    allExperiences.forEach(function(e) {
      var dept = userDeptMap[e.createUserId] || '未知';
      experiencesByDept[dept] = (experiencesByDept[dept] || 0) + 1;
    });

    var allDepts = {};
    Object.keys(projectsByDept).forEach(function(d) { allDepts[d] = true; });
    Object.keys(experiencesByDept).forEach(function(d) { allDepts[d] = true; });

    var deptStats = [];
    Object.keys(allDepts).forEach(function(dept) {
      deptStats.push({
        dept: dept,
        projects: projectsByDept[dept] || 0,
        experiences: experiencesByDept[dept] || 0
      });
    });

    // Monthly project creation
    var monthlyMap = {};
    allProjects.forEach(function(p) {
      if (p.createTime) {
        var month = p.createTime.substring(0, 7) + '-01';
        monthlyMap[month] = (monthlyMap[month] || 0) + 1;
      }
    });
    var monthlyKeys = Object.keys(monthlyMap).sort();
    var monthlyStats = monthlyKeys.map(function(k) {
      return { month: k, count: monthlyMap[k] };
    });

    result = {
      code: 200, message: 'success', data: {
        totalProjects: totalProjects,
        activeProjects: activeProjects,
        completedProjects: completedProjects,
        overdueStages: overdueStages,
        openDeviations: openDeviations,
        pendingSupports: pendingSupports,
        deptStats: deptStats,
        monthlyStats: monthlyStats
      }
    };
  }

  // ======================== Review endpoints ========================
  else if (url?.match(/^\/projects\/\d+\/review$/) && method === 'get') {
    const projectId = parseInt(url.split('/')[2])
    const review = Object.values(mockData.reviews).find(r => r.projectId === projectId)
    result = { code: 200, message: 'success', data: review || null }
  }
  else if (url?.match(/^\/projects\/\d+\/review$/) && method === 'post') {
    const projectId = parseInt(url.split('/')[2])
    const user = getMockUser()
    let review = Object.values(mockData.reviews).find(r => r.projectId === projectId)
    const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
    if (review) {
      if (body.overallDeviation !== undefined) review.overallDeviation = body.overallDeviation
      if (body.efficiencyRating !== undefined) review.efficiencyRating = body.efficiencyRating
      if (body.qualityRating !== undefined) review.qualityRating = body.qualityRating
      if (body.communicationNote !== undefined) review.communicationNote = body.communicationNote
      review.projectName = body.projectName || review.projectName
      review.createUserId = user?.id || review.createUserId
      review.createTime = now
    } else {
      const newId = Date.now()
      review = {
        id: newId,
        projectId,
        overallDeviation: body.overallDeviation || '',
        efficiencyRating: body.efficiencyRating || '',
        qualityRating: body.qualityRating || '',
        communicationNote: body.communicationNote || '',
        createUserId: user?.id,
        createTime: now,
        projectName: body.projectName || ''
      }
      mockData.reviews[newId] = review
    }
    result = { code: 200, message: 'success', data: review }
  }

  // ======================== Experience endpoints ========================
  else if (url?.match(/^\/projects\/\d+\/experience$/) && method === 'get') {
    const projectId = parseInt(url.split('/')[2])
    const experience = Object.values(mockData.experiences).find(e => e.projectId === projectId)
    result = { code: 200, message: 'success', data: experience || null }
  }
  else if (url?.match(/^\/projects\/\d+\/experience$/) && method === 'post') {
    const projectId = parseInt(url.split('/')[2])
    const user = getMockUser()
    let experience = Object.values(mockData.experiences).find(e => e.projectId === projectId)
    const now = new Date().toISOString().replace('T', ' ').slice(0, 19)
    if (experience) {
      if (body.reusableExperience !== undefined) experience.reusableExperience = body.reusableExperience
      if (body.shortcomings !== undefined) experience.shortcomings = body.shortcomings
      if (body.risks !== undefined) experience.risks = body.risks
      if (body.improvement !== undefined) experience.improvement = body.improvement
      experience.projectName = body.projectName || experience.projectName
      experience.createUserId = user?.id || experience.createUserId
      experience.createTime = now
    } else {
      const newId = Date.now()
      experience = {
        id: newId,
        projectId,
        reusableExperience: body.reusableExperience || '',
        shortcomings: body.shortcomings || '',
        risks: body.risks || '',
        improvement: body.improvement || '',
        createUserId: user?.id,
        createTime: now,
        projectName: body.projectName || ''
      }
      mockData.experiences[newId] = experience
    }
    result = { code: 200, message: 'success', data: experience }
  }
  else if (url === '/experiences' && method === 'get') {
    var allExp = Object.values(mockData.experiences)
    // Populate projectName
    allExp.forEach(function(e) {
      if (!e.projectName) {
        var p = mockData.projects.find(function(x) { return x.id === e.projectId })
        if (p) e.projectName = p.name
      }
    })
    var total = allExp.length
    var page = parseInt(params.page) || 1
    var size = Math.min(parseInt(params.size) || 10, 100)
    var start = (page - 1) * size
    result = { code: 200, message: 'success', data: { records: allExp.slice(start, start + size), total: total, size: size, current: page } }
  }

  // ======================== Change endpoints ========================
  else if (url === '/changes' && method === 'get') {
    const allChanges = []
    Object.entries(mockData.changes).forEach(([projectId, changesList]) => {
      const project = mockData.projects.find(p => p.id === parseInt(projectId))
      changesList.forEach(c => {
        allChanges.push({ ...c, projectName: project?.name || '' })
      })
    })
    result = { code: 200, message: 'success', data: allChanges }
  }
  else if (url?.match(/^\/projects\/\d+\/changes$/) && method === 'get') {
    const projectId = parseInt(url.split('/')[2])
    result = { code: 200, message: 'success', data: mockData.changes[projectId] || [] }
  }
  else if (url?.match(/^\/projects\/\d+\/changes$/) && method === 'post') {
    const projectId = parseInt(url.split('/')[2])
    const changes = mockData.changes[projectId] || []
    const newChange = {
      id: Date.now(),
      projectId,
      content: body.content || '',
      confirmTime: body.confirmTime || '',
      impact: body.impact || '',
      status: 'pending'
    }
    changes.push(newChange)
    mockData.changes[projectId] = changes
    result = { code: 200, message: 'success', data: newChange }
  }
  else if (url?.match(/^\/changes\/\d+\/confirm$/) && method === 'put') {
    const changeId = parseInt(url.split('/')[2])
    let found = null
    Object.values(mockData.changes).forEach(changesList => {
      const c = changesList.find(ch => ch.id === changeId)
      if (c) { c.status = 'confirmed'; found = c }
    })
    result = { code: 200, message: 'success', data: found }
  }

  // ======================== Approval endpoints ========================
  else if (url?.match(/^\/projects\/\d+\/approval$/) && method === 'get') {
    const projectId = parseInt(url.split('/')[2])
    result = { code: 200, message: 'success', data: mockData.approvals[projectId] || null }
  }
  else if (url?.match(/^\/projects\/\d+\/approval$/) && method === 'post') {
    const projectId = parseInt(url.split('/')[2])
    const approval = {
      projectId,
      reviewSituation: body.reviewSituation || '',
      failReason: body.failReason || '',
      confirmTime: body.confirmTime || ''
    }
    mockData.approvals[projectId] = approval
    result = { code: 200, message: 'success', data: approval }
  }

  if (result) {
    return {
      data: result,
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
      request: {}
    }
  }

  // No mock match — let the real adapter handle it
  return null
}

export function setupMock(axiosInstance) {
  const fallbackAdapter = axiosInstance.defaults.adapter

  axiosInstance.interceptors.request.use(config => {
    // Save the original adapter (it may be set per-request or as a default)
    const originalAdapter = config.adapter || fallbackAdapter

    // Override with our mock adapter
    // This prevents any real network request for mocked endpoints
    config.adapter = async (cfg) => {
      const mockResponse = await getMockResponse(cfg)
      if (mockResponse) {
        return mockResponse
      }
      // No mock for this endpoint — fall back to real HTTP request
      return originalAdapter(cfg)
    }

    return config
  })
}
