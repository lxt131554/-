import { ElMessage, ElMessageBox } from 'element-plus'

export async function confirmDanger(message, title = '操作确认') {
  await ElMessageBox.confirm(message, title, {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  })
}

export function showActionError(error, fallback = '操作失败，请稍后重试') {
  if (error === 'cancel' || error === 'close') return
  const data = error?.response?.data
  const message = data?.message || error?.message || fallback
  const displayMessage = data?.requestId ? `${message} [requestId: ${data.requestId}]` : message
  ElMessage.error(displayMessage)
}
