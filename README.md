智慧养老管理系统 
---
第 0 部分:从零到落地总流程(先懂骨架)
1. 技术选型:Java 21 / Spring Boot 3.4 / MyBatis-Plus 3.5.5 / MySQL 8 / JWT(java-jwt)/ EasyExcel / 阿里云 OSS / Spring AI-百炼;前端 Vue3 + Vite + Element Plus(后台)、Vant(H5)、Pinia、ECharts。
2. 搭工程:pom 依赖 → application.yml(端口8081/数据源/MyBatis-Plus 逻辑删除/百炼 key)→ 启动类 ElderApplication(@MapperScan)。
3. 定公共约定(全项目复用):
  - 统一返回 Result{code,msg,data}(1成功/0失败);
  - 全局异常:ServiceException(业务可读错误)+ @RestControllerAdvice GlobalExceptionHandler;
  - 鉴权:JWT 签发/解析 + LoginInterceptor(登录门卫)+ RoleAccessInterceptor(护工门卫);
  - CRUD 套路:entity(表)→ Mapper(BaseMapper)→ ServiceImpl(Page+LambdaWrapper)→ Controller(Result) → 前端 api/views;
  - 数据规范:逻辑删除 deleted、时间自动填充、状态 tinyint+注释、金额 decimal、快照防历史被改。
4. 逐模块叠加(顺序即依赖关系):登录/RBAC → 老人档案(先做列表CRUD)→ 标签 → 护理(等级/项目→计划头明细→任务)→ 体检(项目/套餐→预约/结果)→ 老人端 H5 → 后台可视化 → 入住床位/退住结算 → 探视登记 → 老人信息增强(家属/收费/月账单)→ 家属登录 → 体检登记/报告 → AI 助手 → (部署:jar+Nginx+MySQL)。

---
第 1 部分:工程与公共类(每个类干什么)
1.1 配置类(config/)
暂时无法在飞书文档外展示此内容
1.2 拦截器(interceptor/)
暂时无法在飞书文档外展示此内容
1.3 工具类(util/)
暂时无法在飞书文档外展示此内容
1.4 异常与监听(exception/ listener/)
暂时无法在飞书文档外展示此内容
1.5 配置(application.yml 关键点)
端口8081、MySQL(elder 库)、MyBatis-Plus(logic-delete-field=deleted)、StdOut 日志、spring.ai.dashscope(百炼 key+model)。

---
第 2 部分:pojo 数据载体(每个子包职责)
暂时无法在飞书文档外展示此内容

---
第 3 部分:每个模块怎么实现(表→类→接口→前端)
模块1:登录认证与 RBAC 权限
- 表:user/role/user_role/permission(目录0/菜单1/按钮2,自关联树)/role_permission
- 后端:
  - UserController:POST /admin/users/login(BCrypt 校验+明文自动升级)、GET /admin/users/userInfo(返回 user+routerList+btnList+roleCodes)、PUT /resetPassword、用户 CRUD/分配角色/导入导出;
  - AppElderController:POST /app/elders/login(老人端,type=elder)、GET /app/elders/elderInfo;
  - PermissionServiceImpl.selectPermissionByUserId:三表 JOIN(XML PermissionMapper.xml)→ type2→btnList、type0/1→buildTree 递归菜单树;
  - UserServiceImpl:listRoleCodes/hasRoleCode/assignRole(先删后插)/listByRoleCode(护工下拉)。
- 前端:后台 Index.vue 按 routerList 渲染菜单、utils/btnPermission.js 控按钮、路由守卫;ui-app 登录页。
- 实现要点:一层前端可见性 + 一层拦截器(护工白名单);方法级权限(AOP+@RequiresPermission)是下一步建议。
模块2:老人信息(档案+标签+家属+收费+月账单)
- 表:elder、tag、elder_tag、elder_family、elder_charge
- 后端:ElderController(/admin/elders CRUD/assignTag/selectAssignedTag,密码BCrypt,新增默认123456、编辑留空不改)→ ElderServiceImpl.list(Page+条件+批量回填 tagNames 成 ElderVO);ElderFamilyController、ElderChargeController(含 /elder-charges/bill)→ ElderProfileServiceImpl(家属唯一名+密码管理、月度账单=在住等级月费+床位月费+收费项现算)。
- 前端:api/elder.js+api/elderProfile.js → views/Elder.vue(列表+「详情」抽屉:家属/收费/月度账单 三 Tab)。
- 家属登录:AppFamilyController /app/family/login|/me(type=family,只读视图)。
模块3:护理管理(等级/项目/计划/任务)
- 表:care_level、care_item、care_plan、care_plan_item、care_task
- 后端:等级/项目字典 CRUD 控制器;CarePlanController → CarePlanServiceImpl:add/update(@Transactional 头+明细+generateTaskList 生成任务,快照项目名;只重建待执行任务)、removeById(级联删明细+任务)、list(批量回填老人/护理员/等级名→CarePlanVO);CareTaskController(护工强制只看自己的任务、越权/删除被拦)+ CareTaskServiceImpl.list→CareTaskVO。
- 前端:CarePlan.vue(动态明细行)、CareTask.vue(打卡回填/上传;护工只读他人+隐藏删除)、CareItem/CareLevel.vue。
- 护工权限:LoginInterceptor 之后 RoleAccessInterceptor;护工登录自动跳护理任务页。
模块4:体检管理(项目/套餐/预约/登记/报告)
- 表:exam_item/exam_package/exam_package_item/exam_appointment/exam_appointment_item
- 后端:
  - 后台:ExamItemController、ExamPackageController(套餐↔项目 Transfer);
  - 老人端:AppAppointmentController add/list/cancel;
  - ExamAppointmentServiceImpl.add:校验套餐/时段 → 存预约头(价格快照,status0)→ 按套餐项写明细快照(status0);
  - 体检登记(新增):ExamAppointmentAdminController /admin/exam-appointments → adminPage/getAdminDetail/saveResults/adminCancel;saveResults 数值型按参考范围自动判正常/异常(可手动覆盖 abnormal)、未填=未完成 → 预约置已完成;
  - 报告:report(id,elderId)(本人校验)→ GET /app/appointment/{id}/report。
- 前端:后台 ExamAppointment.vue(录入结果抽屉+查看+取消);ui-app MyAppointment.vue 已完成→Report.vue /report/:id。
- 注意:老人端"今日健康数据/公告"仍是 mock;预约/报告为真实接口。
模块5:入住/床位与费用结算
- 表:room、bed、elder_stay、fee_settlement
- 后端:
  - RoomController/BedController:房间 CRUD(占用统计)、床位增删/空闲/维修;
  - StayController /admin/stays:check-in(入住:占床+老人状态4+插入住单)、transfer(调床)、check-out(释放床+状态5+自动生成 fee_settlement=等级月费+床位月费÷30×天数,快照)、pay(缴费0→1);
  - StayServiceImpl.pageStays 批量回填老人/床位/等级/结算。
- 前端:Room.vue(床位管理弹窗)、Stay.vue(入住/调床/退住/缴费)。
模块6:探视/访客登记
- 表:visit_record(状态机 0待审批→1通过/在访→3离院完成;2拒绝/4取消/5过期)
- 后端:VisitController /admin/visits + VisitServiceImpl(approve/reject/arrive/leave/expire/delete,每步 requireStatus 校验;列表批量回填老人名→VisitVO;现场登记自动到访)。
- 前端:Visit.vue(状态化按钮)。
- 联动:大屏"今日探视"计数。
模块7:统计/数据大屏
- 后端:StatsMapper(@Select 聚合:CASE 分桶/GROUP BY/近7天/COUNT)、StatsServiceImpl(getOverview/getScreen:补零、补日期、算完成率)、StatsController /admin/stats/overview|/screen。
- 前端:Home.vue(6卡4图)、Screen.vue(全屏,30s 轮询,resize/卸载释放)。
- 实现要点:一次接口喂多图;纯读聚合不写表。
模块8:AI 健康助手(康养小智)
- 配置:resources/ai/kangyang-xiaozhi-system.md(人设)。
- 后端:AppAiController /app/ai/chat → AiChatServiceImpl:只读组装该老人档案/标签/护理记录/体检摘要 → 拼 system → JDK HttpClient 调百炼 OpenAI 兼容协议 → 返回;输入过滤(角色白名单/长度)、错误兜底。
- 前端:ui-app AiAssistant.vue(消息流+快捷提问)。
模块9:老人端 H5 + 家属端
- ui-app 页面:Login(老人/家属切换)、Home、体检套餐/预约、我的预约+报告、AI 助手、Family(家属只读老人档案)。
- 基础设施:utils/request(baseURL=/api/app+token/401)、store/token、store/elderInfo、store/family、router(守卫按登录身份分流)。
