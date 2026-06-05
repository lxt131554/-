import pymysql
conn = pymysql.connect(host='localhost',port=3306,user='root',password='root',database='pm_system',charset='utf8mb4')
c = conn.cursor()

c.execute('DELETE FROM sys_deviation')
c.execute('DELETE FROM sys_stage_report')
c.execute('DELETE FROM sys_project_stage')

def q(sql, *args):
    c.execute(sql, args)

# Project 1 - active, 3 stages, 1 report, 1 deviation
pid=1
q('UPDATE sys_project SET status=%s WHERE id=%s','active',pid)
q('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,status,assignee_id,sort_order) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,'外业调查','实地勘测数据采集样地调查','2026-05-15','2026-06-15','2026-05-16','submitted',3,0,
  pid,'内业整理','数据整理图纸绘制统计分析','2026-06-16','2026-07-10',None,'pending',3,1,
  pid,'成果提交','编制报告制作图件提交审核','2026-07-11','2026-07-31',None,'pending',3,2)
q('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time,attachment_name) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  1,pid,'已完成南部片区调查约15万亩设样地320个树种马尾松杉木柏木',65,'北部片区地形复杂预计延迟5天','每样地8个检尺点数据当日复核','完成调查表120份树种分布图2幅','与县林业局对接3次院内部协调会2次','2026-05-16','pending',3,'2026-05-28 16:30:00','调查报告初稿.pdf')
q('INSERT INTO sys_deviation (project_id,stage_id,type,description,reason,impact,status,create_user_id) VALUES (%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,1,'auto','北部片区地形复杂延迟5天','地形复杂交通不便','工期延后5天','open',2)

# Project 2 - completed, 3 stages, 3 reports
pid=2
q('UPDATE sys_project SET status=%s WHERE id=%s','completed',pid)
q('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,'需求调研','沟通需求梳理功能清单','2026-03-01','2026-03-15','2026-03-02','2026-03-14','completed',3,0,
  pid,'系统开发','前后端开发数据库联调','2026-03-16','2026-04-20','2026-03-18','2026-04-25','completed',3,1,
  pid,'测试上线','功能测试部署上线','2026-04-26','2026-05-10','2026-04-28','2026-05-12','completed',3,2)
q('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,actual_start,review_status,submit_user_id,submit_time) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  4,pid,'完成需求调研梳理5大模块23项功能形成需求文档',100,'','每项需求与业务部门确认签字','需求规格说明书1份','2026-03-02','passed',3,'2026-03-14 10:00:00',
  5,pid,'完成V0.1至V0.6版本开发含前后端15页面7张数据表',100,'Mock模式与真后端URL不一致已修复','每版本经2轮审查','30+后端文件15前端页面','2026-03-18','passed',3,'2026-04-20 14:00:00',
  6,pid,'全流程测试修复12个bug已部署Netlify演示版',100,'移动端显示效果需优化','测试覆盖全部业务场景','测试报告1份','2026-04-28','passed',3,'2026-05-10 16:00:00')

# Project 3 - active, 3 stages, 2 reports, 1 deviation
pid=3
q('UPDATE sys_project SET status=%s WHERE id=%s','active',pid)
q('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,'前期调研','攀枝花市域林业产业现状调研','2026-05-20','2026-06-10','2026-05-21','2026-06-08','completed',3,0,
  pid,'规划编制','编写产业发展规划文本及说明','2026-06-11','2026-07-15','2026-06-12',None,'submitted',3,1,
  pid,'成果评审','组织专家评审修改完善报批','2026-07-16','2026-07-31',None,None,'pending',3,2)
q('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time,attachment_name) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  7,pid,'完成仁和区米易县盐边县3区县调研走访企业15家收集数据120份',100,'米易县部分乡镇交通不便','每区县2人一组交叉验证数据','调研报告1份数据汇总表1套','与市林业局对接4次','2026-05-21','passed',3,'2026-06-08 11:00:00',None,
  8,pid,'完成规划初稿编制围绕特色经济林林下经济森林康养三大方向',70,'林下经济方向数据支撑不足需补充调研','引用数据标注来源经专家咨询确认','规划文本初稿1份附件3份','与院技术委员会汇报1次','2026-06-12','pending',3,'2026-06-28 09:00:00','攀枝花林业产业规划初稿.pdf')
q('INSERT INTO sys_deviation (project_id,stage_id,type,description,reason,impact,status,create_user_id) VALUES (%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,8,'auto','林下经济数据不足导致编制延迟3天','项目对接人变更部分数据缺失','编制时间顺延3天','open',2)

# Project 4 - active, 2 stages, 1 report
pid=4
q('UPDATE sys_project SET status=%s WHERE id=%s','active',pid)
q('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,status,assignee_id,sort_order) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,'外业调查','草原退化现状调查鼠荒地治理评估','2026-06-01','2026-07-01','2026-06-03','in_progress',3,0,
  pid,'方案编制','退化草原生态修复方案编制','2026-07-02','2026-07-25',None,'pending',3,1)
q('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  9,pid,'完成红原县若尔盖县草原调查布设样方200个采集土壤样本80份',55,'高寒地区天气多变调查进度受降雨影响','样方GPS定位精度小于5m','样方调查表200份土壤样本80份','与县草原站协调2次','2026-06-03','pending',3,'2026-06-20 15:00:00')

# Project 5 - active, 2 stages, 2 reports
pid=5
q('UPDATE sys_project SET status=%s WHERE id=%s','active',pid)
q('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,'数据收集','收集自然资源交通水利等基础数据','2026-05-10','2026-05-25','2026-05-11','2026-05-24','completed',3,0,
  pid,'方案设计','编制项目总体方案及技术路线','2026-05-26','2026-06-15','2026-05-27',None,'submitted',3,1)
q('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  11,pid,'完成基础数据收集含国土三调数据林地一张图交通水利分布图',100,'部分乡镇数据更新不及时','数据来源标注清晰多方交叉验证','基础数据集1套','与县自然资源局对接1次','2026-05-11','passed',3,'2026-05-24 09:00:00',
  12,pid,'完成方案框架设计确定技术路线采用遥感加地面调查结合方式',50,'遥感数据采购预算超出预期','技术路线参考同类项目经验','方案框架及技术路线说明1份','与院总工办技术讨论1次','2026-05-27','pending',3,'2026-06-10 14:00:00')

# Project 6 - active, 2 stages, 2 reports, 1 deviation
pid=6
q('UPDATE sys_project SET status=%s WHERE id=%s','active',pid)
q('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,'外业作业','实地测量标志点埋设航拍数据采集','2026-04-01','2026-04-30','2026-04-03','2026-05-05','completed',3,0,
  pid,'内业处理','影像处理数据解算成果编制','2026-05-06','2026-05-31','2026-05-08',None,'in_progress',3,1)
q('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  13,pid,'完成全部外业作业航拍面积50km2标志点埋设120个',100,'4月中旬连续降雨延误5天','航拍重叠率大于百分之六十标志点复核率百分之百','航拍影像50km2控制点成果表','与测绘大队协调2次','2026-04-03','passed',3,'2026-05-05 16:00:00',
  14,pid,'完成影像拼接和正射校正正在进行数据解算',60,'部分航拍区域影像拼接存在色差','影像分辨率优于0.2m','正射影像图1幅','','2026-05-08','pending',3,'2026-05-25 10:00:00')
q('INSERT INTO sys_deviation (project_id,stage_id,type,description,reason,impact,status,create_user_id) VALUES (%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,13,'auto','连续降雨导致外业延期5天','4月中旬连续降雨无法作业','整体工期延后5天','open',2)

# Project 7 - active, 2 stages, 1 report
pid=7
q('UPDATE sys_project SET status=%s WHERE id=%s','active',pid)
q('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,'现场踏勘','项目区现场踏勘了解地形地貌植被分布','2026-06-01','2026-06-10','2026-06-02','2026-06-09','completed',3,0,
  pid,'调查实施','样地设置树种识别蓄积量调查','2026-06-11','2026-07-10',None,None,'pending',3,1)
q('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,actual_start,review_status,submit_user_id,submit_time) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  15,pid,'完成项目区踏勘了解地形变化植被分布现状确定调查重点区域',100,'','踏勘路线覆盖项目区百分之八十以上','踏勘报告1份','2026-06-02','passed',3,'2026-06-09 11:00:00')

# Project 8 - active, 2 stages, 2 reports, 1 deviation
pid=8
q('UPDATE sys_project SET status=%s WHERE id=%s','active',pid)
q('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,'前期准备','资料收集设备调试人员培训','2026-05-01','2026-05-15','2026-05-02','2026-05-14','completed',3,0,
  pid,'正式调查','全面开展外业调查工作','2026-05-16','2026-06-30','2026-05-18',None,'in_progress',3,1)
q('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,actual_start,review_status,submit_user_id,submit_time) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s),(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)',
  17,pid,'完成资料收集设备调试和人员培训准备就绪',100,'','培训覆盖率100%','培训记录及设备清单','2026-05-02','passed',3,'2026-05-14 09:00:00',
  18,pid,'完成东部片区调查约8万亩开始西部片区',45,'西部片区地形复杂调查进度偏慢','每样地数据当日录入当日复核','东部片区调查数据','2026-05-18','pending',3,'2026-06-10 14:00:00')
q('INSERT INTO sys_deviation (project_id,stage_id,type,description,reason,impact,status,create_user_id) VALUES (%s,%s,%s,%s,%s,%s,%s,%s)',
  pid,18,'auto','西部片区进度偏慢','地形复杂调查难度大','整体进度或将延迟3-5天','open',2)

conn.commit()

c.execute('SELECT COUNT(*) FROM sys_project_stage')
n1 = c.fetchone()[0]
c.execute('SELECT COUNT(*) FROM sys_stage_report')
n2 = c.fetchone()[0]
c.execute('SELECT COUNT(*) FROM sys_deviation')
n3 = c.fetchone()[0]

print(f'总计: {n1}个阶段, {n2}条填报, {n3}条偏差')
c.execute('SELECT p.id,p.name,p.status,(SELECT COUNT(*) FROM sys_project_stage WHERE project_id=p.id),(SELECT COUNT(*) FROM sys_stage_report WHERE project_id=p.id),(SELECT COUNT(*) FROM sys_deviation WHERE project_id=p.id) FROM sys_project p ORDER BY p.id')
for r in c.fetchall():
    s='进行中' if r[2]=='active' else '已完成'
    print(f'  {r[0]}. {r[1]} [{s}] {r[3]}阶段 {r[4]}填报 {r[5]}偏差')
conn.close()
