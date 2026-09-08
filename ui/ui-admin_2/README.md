# ui-admin_2 · 智慧养老管理系统前端

基于 **Vue 3 + Vite + Element Plus + Pinia** 的养老院管理后台前端（ui-admin 的美观重构版）。

> 与 `ui-admin` 功能保持完全一致（共用同一套 `src/api`、store、工具层），在其基础上重构了视觉与交互，
> 并新增首页看板、缺陷修复、必填校验、路由懒加载分包与响应式适配。

## 功能模块

| 模块 | 说明 |
| --- | --- |
| 首页看板 | 问候横幅、6 项业务统计（老人/员工/护理等级/护理计划/护理任务/体检套餐）、快捷入口 |
| 老人管理 | 老人档案 CRUD、批量删除、按标签搜索、分配标签、状态（入住/请假/退住等） |
| 标签管理 | 标签 CRUD |
| 护理项目管理 / 护理等级管理 | CRUD、启停用、排序、上传展示图 |
| 护理计划管理 | CRUD + 动态明细（护理项目、服务时间、执行周期/频次）、分配护理人员 |
| 护理任务管理 | 计划生成的任务执行回填：状态、实际完成时间、打卡照片 |
| 体检项目管理 / 体检套餐管理 | CRUD、分配体检项目（Transfer） |
| 用户 / 角色 / 权限管理 | 账号启停、分配角色；角色分配权限（树）；权限树维护（目录/菜单/按钮） |

## 技术栈

- Vue 3（`<script setup>`）+ Vue Router 5（路由懒加载）
- Element Plus 2（zh-CN 语言包、全量图标）+ 定制主题 `src/assets/theme.css`
- Pinia + pinia-plugin-persistedstate（token / 用户信息持久化）
- Axios（统一拦截：携带 token、401 跳登录、blob 下载直出）
- Vite 8 + Sass

## 快速开始

```bash
# 1. 安装依赖（仓库已含 node_modules，可跳过）
npm install

# 2. 启动开发服务器（默认端口 5174，避免与 ui-admin 冲突）
npm run dev

# 3. 生产构建
npm run build
```

访问：http://localhost:5174

> 接口代理：`/api` → `http://localhost:8081`（后端 Spring Boot 服务需先启动）。
> 登录账号由后端提供（如 `admin`），登录后菜单/按钮权限由后端按角色返回。

## 目录结构

```
src/
├── api/            # 各业务模块接口（与 ui-admin 共用，勿随意改动字段）
├── assets/         # base.css / main.css / theme.css(设计系统)
├── components/     # 通用组件（IconPicker 等）
├── router/         # 路由（业务页懒加载；登录后默认落地 /home）
├── store/          # token、userInfo（持久化）
├── utils/          # request.js、按钮权限指令
└── views/          # Home(首页看板) + 12 个业务页面
```

## 设计说明（theme.css）

- 主色：疗愈青绿 `#14b8a6`；辅色：深空蓝侧边栏渐变
- 全局覆盖 Element Plus 变量：圆角、边框、文字、填充
- 组件级美化：按钮光晕、卡片悬浮、表头/斑马、分页、弹窗、表单聚焦、滚动条
