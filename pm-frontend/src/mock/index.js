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
    let list = [...mockData.projects]
    if (keyword) {
      list = list.filter(p => p.name.includes(keyword))
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
      createTime: new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    mockData.projects.push(newProject)
    mockData.projectMembers[newProject.id] = []
    mockData.stages[newProject.id] = []
    result = { code: 200, message: 'success', data: newProject }
  }
  else if (url?.match(/^\/projects\/\d+$/) && method === 'put') {
    const id = parseInt(url.split('/')[2])
    const project = mockData.projects.find(p => p.id === id)
    if (project) {
      if (body.name !== undefined) project.name = body.name
      if (body.description !== undefined) project.description = body.description
      if (body.status !== undefined) project.status = body.status
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
    result = { code: 200, message: 'success', data: mockData.projectMembers[projectId] || [] }
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
      roleInProject: body.roleInProject
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
    result = { code: 200, message: 'success', data: pending }
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
    result = { code: 200, message: 'success', data: Object.values(mockData.users) }
  }

  // ======================== Dashboard endpoints ========================
  else if (url === '/api/dashboard' && method === 'get') {
    const user = getMockUser()
    let stats = {}
    if (user?.role === 'engineer') {
      stats = { todo: 2, returned: 1 }
    } else if (user?.role === 'manager') {
      stats = { pendingReview: 1, pendingAchievement: 1, openDeviations: 2, pendingSupports: 2 }
    } else if (user?.role === 'leader') {
      stats = { openDeviations: 2, pendingSupports: 2 }
    }
    result = { code: 200, message: 'success', data: stats }
  }

  // ======================== Deviation endpoints ========================
  else if (url === '/api/deviations' && method === 'get') {
    let list = [...mockData.deviations]
    if (params.status) {
      list = list.filter(d => d.status === params.status)
    }
    result = { code: 200, message: 'success', data: list }
  }
  else if (url === '/api/deviations' && method === 'post') {
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
  else if (url?.match(/^\/api\/deviations\/\d+\/close$/) && method === 'put') {
    const id = parseInt(url.split('/')[3])
    const deviation = mockData.deviations.find(d => d.id === id)
    if (deviation) {
      deviation.status = 'closed'
      deviation.closeTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    result = { code: 200, message: 'success', data: deviation || null }
  }

  // ======================== Support endpoints ========================
  else if (url === '/api/supports' && method === 'get') {
    let list = [...mockData.supportItems]
    if (params.status) {
      list = list.filter(s => s.status === params.status)
    }
    result = { code: 200, message: 'success', data: list }
  }
  else if (url === '/api/supports' && method === 'post') {
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
  else if (url?.match(/^\/api\/supports\/\d+\/resolve$/) && method === 'put') {
    const id = parseInt(url.split('/')[3])
    const supportItem = mockData.supportItems.find(s => s.id === id)
    if (supportItem) {
      supportItem.status = 'resolved'
      supportItem.reply = body.reply || ''
      supportItem.updateTime = new Date().toISOString().replace('T', ' ').slice(0, 19)
    }
    result = { code: 200, message: 'success', data: supportItem || null }
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
