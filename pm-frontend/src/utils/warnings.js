export function getWarnings(stages) {
  const now = new Date()
  now.setHours(0, 0, 0, 0)

  const overdue = []
  const nearDeadline = []
  const pendingReview = []

  stages.forEach(s => {
    if (s.status === 'completed') return

    if (s.planEnd) {
      const endDate = new Date(s.planEnd)
      endDate.setHours(0, 0, 0, 0)
      const daysLeft = Math.ceil((endDate - now) / (1000 * 60 * 60 * 24))

      if (daysLeft < 0) {
        overdue.push({ ...s, daysOverdue: Math.abs(daysLeft) })
      } else if (daysLeft <= 2) {
        nearDeadline.push({ ...s, daysLeft })
      }
    }

    if (s.reviewStatus === 'pending' && s.submitTime) {
      const submitDate = new Date(s.submitTime)
      const hoursSinceSubmit = (now - submitDate) / (1000 * 60 * 60)
      if (hoursSinceSubmit > 48) {
        pendingReview.push({ ...s, hoursSinceSubmit: Math.round(hoursSinceSubmit) })
      }
    }
  })

  return { overdue, nearDeadline, pendingReview }
}
