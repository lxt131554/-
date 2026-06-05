import pymysql
c = pymysql.connect(host='localhost',port=3306,user='root',password='root',database='pm_system',charset='utf8mb4')
cur = c.cursor()

cur.execute('DELETE FROM sys_deviation')
cur.execute('DELETE FROM sys_stage_report')
cur.execute('DELETE FROM sys_project_stage')

pid = 1
cur.execute('UPDATE sys_project SET status="active" WHERE id=%%s',(pid,))
cur.execute('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,status,assignee_id,sort_order) VALUES (%%s,"外业调查","实地勘测数据采集","2026-05-15","2026-06-15","2026-05-16","submitted",3,0),(%%s,"内业整理","数据整理图纸绘制","2026-06-16","2026-07-10",NULL,"pending",3,1),(%%s,"成果提交","编制报告提交审核","2026-07-11","2026-07-31",NULL,"pending",3,2)',(pid,pid,pid))
cur.execute('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time,attachment_name) VALUES (1,%%s,"已完成南部片区调查约15万亩设样地320个",65,"北部片区地形复杂延迟5天","每样地8个检尺点","完成调查表120份","与县林业局对接3次","2026-05-16","pending",3,"2026-05-28 16:30:00","调查报告初稿.pdf")',(pid,))
cur.execute('INSERT INTO sys_deviation (project_id,stage_id,type,description,reason,impact,status,create_user_id) VALUES (%%s,1,"auto","北部片区延迟5天","地形复杂不便","工期延后5天","open",2)',(pid,))

pid = 2
cur.execute('UPDATE sys_project SET status="completed" WHERE id=%%s',(pid,))
cur.execute('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%%s,"需求调研","沟通需求梳理功能","2026-03-01","2026-03-15","2026-03-02","2026-03-14","completed",3,0),(%%s,"系统开发","前后端开发联调","2026-03-16","2026-04-20","2026-03-18","2026-04-25","completed",3,1),(%%s,"测试上线","测试部署上线","2026-04-26","2026-05-10","2026-04-28","2026-05-12","completed",3,2)',(pid,pid,pid))
cur.execute('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,actual_start,review_status,submit_user_id,submit_time) VALUES (4,%%s,"完成需求调研梳理5大模块23项功能",100,"","每项需求确认签字","需求规格说明书1份","2026-03-02","passed",3,"2026-03-14 10:00:00"),(5,%%s,"完成V0.1-V0.6开发",100,"Mock与后端URL不一致","版本2轮审查","30+文件15页面7表","2026-03-18","passed",3,"2026-04-20 14:00:00"),(6,%%s,"全流程测试修复12个bug",100,"移动端效果不佳","测试覆盖全部场景","测试报告1份","2026-04-28","passed",3,"2026-05-10 16:00:00")',(pid,pid,pid))

pid = 3
cur.execute('UPDATE sys_project SET status="active" WHERE id=%%s',(pid,))
cur.execute('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%%s,"前期调研","攀枝花林业现状调研","2026-05-20","2026-06-10","2026-05-21","2026-06-08","completed",3,0),(%%s,"规划编制","编写产业规划文本","2026-06-11","2026-07-15","2026-06-12",NULL,"submitted",3,1),(%%s,"成果评审","专家评审修改报批","2026-07-16","2026-07-31",NULL,NULL,"pending",3,2)',(pid,pid,pid))
cur.execute('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time,attachment_name) VALUES (7,%%s,"走访3区县15家企业收集数据120份",100,"米易县交通不便","2人一组交叉验证","调研报告1份汇总表1套","与市局对接4次","2026-05-21","passed",3,"2026-06-08 11:00:00",NULL),(8,%%s,"完成规划初稿编制三大方向",70,"林下经济数据不足需补充","数据标注来源专家确认","规划文本初稿1份附件3份","与院技委汇报1次","2026-06-12","pending",3,"2026-06-28 09:00:00","攀枝花规划初稿.pdf")',(pid,pid))
cur.execute('INSERT INTO sys_deviation (project_id,stage_id,type,description,reason,impact,status,create_user_id) VALUES (%%s,8,"auto","林下经济数据不足延迟3天","对接人变更数据缺失","编制时间顺延3天","open",2)',(pid,))

pid = 4
cur.execute('UPDATE sys_project SET status="active" WHERE id=%%s',(pid,))
cur.execute('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,status,assignee_id,sort_order) VALUES (%%s,"外业调查","草原退化调查评估","2026-06-01","2026-07-01","2026-06-03","in_progress",3,0),(%%s,"方案编制","生态修复方案编制","2026-07-02","2026-07-25",NULL,"pending",3,1)',(pid,pid))
cur.execute('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time) VALUES (9,%%s,"红原县若尔盖县布设样方200个采集土壤80份",55,"高寒天气多变进度受影响","样方GPS精度小于5m","样方调查表200份土壤80份","与县草原站协调2次","2026-06-03","pending",3,"2026-06-20 15:00:00")',(pid,))

pid = 5
cur.execute('UPDATE sys_project SET status="active" WHERE id=%%s',(pid,))
cur.execute('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%%s,"数据收集","收集国土交通水利等数据","2026-05-10","2026-05-25","2026-05-11","2026-05-24","completed",3,0),(%%s,"方案设计","编制总体方案技术路线","2026-05-26","2026-06-15","2026-05-27",NULL,"submitted",3,1)',(pid,pid))
cur.execute('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time) VALUES (11,%%s,"完成基础数据收集含三调数据一张图",100,"部分乡镇数据更新不及时","数据来源标注多方验证","基础数据集1套","与县自规局对接1次","2026-05-11","passed",3,"2026-05-24 09:00:00"),(12,%%s,"完成方案框架确定遥感+地面结合路线",50,"遥感数据采购预算超预期","参考同类项目经验","方案框架说明1份","与院总工办讨论1次","2026-05-27","pending",3,"2026-06-10 14:00:00")',(pid,pid))

pid = 6
cur.execute('UPDATE sys_project SET status="active" WHERE id=%%s',(pid,))
cur.execute('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%%s,"外业作业","实地测量航拍采集","2026-04-01","2026-04-30","2026-04-03","2026-05-05","completed",3,0),(%%s,"内业处理","影像处理数据解算","2026-05-06","2026-05-31","2026-05-08",NULL,"in_progress",3,1)',(pid,pid))
cur.execute('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,coordination_note,actual_start,review_status,submit_user_id,submit_time) VALUES (13,%%s,"完成全部外业航拍50km²埋点120个",100,"4月中旬降雨延误5天","航拍重叠率大于60%%","航拍影像50km²控制点成果表","与测绘队协调2次","2026-04-03","passed",3,"2026-05-05 16:00:00"),(14,%%s,"完成影像拼接正射校正进行解算",60,"部分区域存在色差","影像分辨率优于0.2m","正射影像图1幅","","2026-05-08","pending",3,"2026-05-25 10:00:00")',(pid,pid))
cur.execute('INSERT INTO sys_deviation (project_id,stage_id,type,description,reason,impact,status,create_user_id) VALUES (%%s,13,"auto","降雨导致外业延期5天","4月连续降雨","工期延后5天","open",2)',(pid,))

pid = 7
cur.execute('UPDATE sys_project SET status="active" WHERE id=%%s',(pid,))
cur.execute('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%%s,"现场踏勘","了解地形植被分布","2026-06-01","2026-06-10","2026-06-02","2026-06-09","completed",3,0),(%%s,"调查实施","样地设置树种识别","2026-06-11","2026-07-10",NULL,NULL,"pending",3,1)',(pid,pid))
cur.execute('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,actual_start,review_status,submit_user_id,submit_time) VALUES (15,%%s,"完成项目区踏勘确定调查重点",100,"","踏勘路线覆盖80%%以上","踏勘报告1份","2026-06-02","passed",3,"2026-06-09 11:00:00")',(pid,))

pid = 8
cur.execute('UPDATE sys_project SET status="active" WHERE id=%%s',(pid,))
cur.execute('INSERT INTO sys_project_stage (project_id,stage_name,description,plan_start,plan_end,actual_start,actual_end,status,assignee_id,sort_order) VALUES (%%s,"前期准备","资料收集设备培训","2026-05-01","2026-05-15","2026-05-02","2026-05-14","completed",3,0),(%%s,"正式调查","全面外业调查","2026-05-16","2026-06-30","2026-05-18",NULL,"in_progress",3,1)',(pid,pid))
cur.execute('INSERT INTO sys_stage_report (stage_id,project_id,content,progress_rate,problem,quality_control,result_summary,actual_start,review_status,submit_user_id,submit_time) VALUES (17,%%s,"完成资料收集设备调试人员培训",100,"","培训覆盖率100%%","培训记录设备清单","2026-05-02","passed",3,"2026-05-14 09:00:00"),(18,%%s,"完成东部片区约8万亩开始西部",45,"西部地形复杂进度慢","数据当日录入复核","东部片区调查数据","2026-05-18","pending",3,"2026-06-10 14:00:00")',(pid,pid))
cur.execute('INSERT INTO sys_deviation (project_id,stage_id,type,description,reason,impact,status,create_user_id) VALUES (%%s,18,"auto","西部片区进度偏慢","地形复杂调查难","进度延迟3-5天","open",2)',(pid,))

c.commit()

cur.execute('SELECT p.id,p.name,p.status,(SELECT COUNT(*) FROM sys_project_stage WHERE project_id=p.id),(SELECT COUNT(*) FROM sys_stage_report WHERE project_id=p.id),(SELECT COUNT(*) FROM sys_deviation WHERE project_id=p.id) FROM sys_project p ORDER BY p.id')
for r in cur.fetchall():
    s = '进行中' if r[2]=='active' else '已完成'
    print(f'{r[0]}: {r[1]} [{s}] {r[3]}阶段 {r[4]}条填报 {r[5]}条偏差')

cur.execute('SELECT (SELECT COUNT(*) FROM sys_project_stage),(SELECT COUNT(*) FROM sys_stage_report),(SELECT COUNT(*) FROM sys_deviation)')
t = cur.fetchone()
print(f'\n总计: {t[0]}个阶段, {t[1]}条填报, {t[2]}条偏差')
c.close()
