# elder-master
智慧养老管理平台
技术栈：Spring Boot， Redis ，EasyExcel ， OSS ，Vue3 ，Spring AI
基于SpringBoot+Vue3面向养老机构的前后端分离系统：覆盖老人档案、周期护理任务、体检预约、入住结算与老人端AI 健康助手,由管理端，老人与家属移动端三端协同
1. 不建统计表,基于@Select聚合SQL实时统计，CASE WHEN做年龄分桶SUM算完成率、日期窗口做近 7 天趋势,Service 层补齐固定桶与缺失日期供ECharts直接渲染
2. 基于JWT+拦截器+自定义注解+AOP实现管理员，老人，家属三端的多身份 RBAC
3. 基于Spring AI接入阿里云百炼大模型实现老人端 AI 健康助手，设计Redis 会话与长期两级记忆,并基于 SSE 流式输出降低AI回复的首字延迟,用户等待感下降约 80%
