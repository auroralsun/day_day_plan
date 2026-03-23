# Day Day Plan 产品规划

## 1. 产品目标

- 打造一个适合个人日常使用的安卓本地离线 App。
- 核心闭环：`规划今天 → 执行今天 → 查看完成结果`。
- 第一版强调快速录入、低学习成本、当天可用。

## 2. 第一版定位

- 技术：`原生 Android / Kotlin / Compose`
- 产品范围：`MVP`
- 核心模式：`日程 + 待办混合`
- 存储策略：`本地离线优先`
- 明确不做：登录、同步、提醒、复盘、统计、模板、AI 生成

## 3. V1 核心功能

### 3.1 今日总览

- 显示今天日期
- 显示进行中的日程
- 显示剩余待办数
- 显示已完成项数量
- 提供新增日程 / 新增待办快捷入口

### 3.2 日程

- 新增日程：标题、开始时间、结束时间、备注
- 编辑/删除日程
- 标记完成 / 取消完成
- 按时间顺序展示

### 3.3 待办

- 新增待办：标题、优先级、是否预计今天完成、备注
- 编辑/删除待办
- 标记完成 / 取消完成
- 手动调整排序

### 3.4 已完成

- 聚合展示今天已完成的日程与待办
- 支持恢复为未完成

## 4. 技术设计

- UI：Jetpack Compose + Material 3
- 导航：Navigation Compose
- 数据：Room
- 轻量偏好：DataStore
- 架构：`ui / domain / data`

## 5. 当前已实现状态

- [x] 项目骨架
- [x] 本地数据库与仓储
- [x] 四个主页面
- [x] 新增/编辑弹层
- [x] 本地状态持久化
- [ ] 提醒能力
- [ ] 历史计划视图
- [ ] 数据统计

## 6. 后续记录方式

- 所有新增需求、交互决策、版本范围都继续记录在这个文件。
- 每次细化一个点，就追加“决策内容 + 原因 + 是否进入 V1”。

## 7. Visual Decisions Log

- Decision: upgrade V1 from default Material layout to a more product-like visual style.
- Why: the core workflow already exists, so visual hierarchy now matters for daily habit formation.
- Included in V1: yes.
- Implemented details:
  - gradient hero summary on the overview screen
  - branded light/dark color system and rounded shape system
  - richer schedule/todo cards with stronger hierarchy
  - polished empty states and editor dialogs
  - softer app shell with elevated bottom navigation and branded FAB
