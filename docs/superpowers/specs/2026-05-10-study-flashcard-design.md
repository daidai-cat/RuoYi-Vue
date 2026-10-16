# 学习 / 抽记卡 设计说明

## 1. 结论

- 本功能采用 `方案 B`：按当前 `RuoYi-Vue` 技术栈重写页面和接口，复用旧版抽记卡功能规则。
- 首版范围包含：`学习 / 抽记卡` 菜单、个人私有卡组、四象限看板、练习弹窗、轻量 CRUD、`CSV` 导入、`JSON` 批量注入。
- 首版不包含：多人共享、自动象限流转、复杂统计报表、spaced repetition 算法。

## 2. 背景

`nims-modernize/migration-packages/four-quadrant-flashcard` 中已经提供了一套完整的抽记卡迁移包，包含前端页面、后端接口和 SQL。

但旧迁移包依赖的技术栈与当前系统不一致：

- 旧前端：`Vue 3 + script setup + viy-ui`
- 当前前端：`Vue 2 + Element UI + RuoYi-Vue`
- 旧后端：`Spring Boot + JPA`
- 当前后端：`RuoYi Controller + Service + Mapper + Domain`

因此本次不采用直接拷贝旧实现的方式，而是保留功能规则、按当前系统风格重写。

## 3. 设计目标

- 在当前系统中新建一级菜单 `学习`，其下新增子菜单 `抽记卡`
- 保留旧版抽记卡的主要业务能力
- 使用当前系统统一的权限、提示、接口、页面布局和数据访问方式
- 确保卡组和卡片仅当前登录人可见、可练习

## 4. 范围

### 4.1 首版包含

- 菜单：`学习 / 抽记卡`
- 卡组管理
- 卡片管理
- 四象限看板
- 拖拽调整象限
- 练习弹窗
- 保存最近答案
- 评分
- 手动移动象限
- `CSV` 导入
- `JSON` 批量导入

### 4.2 首版不包含

- 多人共享卡组
- 公共卡组市场
- 自动根据评分调整象限
- 复习计划统计看板
- 复杂学习算法
- 单独的多页面工作流

## 5. 功能规则

### 5.1 菜单结构

- 新建一级菜单：`学习`
- 在 `学习` 下新增子菜单：`抽记卡`
- 页面路由建议：`/study/flashcard`

### 5.2 数据归属

- 卡组仅当前登录人可见
- 卡片仅当前登录人可见
- 练习仅针对当前登录人的卡组和卡片
- 后端按登录态自动做数据隔离
- 前端不允许传入可覆盖 `userId` 的字段

### 5.3 象限规则

四象限沿用旧版语义：

- `Q1`：重要 / 困难
- `Q2`：重要 / 熟悉
- `Q3`：次要 / 困难
- `Q4`：次要 / 熟悉

首版评分规则：

- 评分会被记录
- 评分时会累计复习次数
- 评分不会自动修改象限
- 象限主要通过拖拽或手动移动来调整

### 5.4 练习规则

- 可选择练习象限范围
- 支持 `顺序` 和 `随机` 两种练习顺序
- 支持翻卡
- 支持输入当前答案
- 支持保存最近答案
- 支持上一张 / 下一张
- 支持评分
- 支持手动移动象限
- 支持答错重试

答错重试规则：

- 延续旧逻辑
- 当前卡片被追加到本轮练习队尾
- 不直接修改象限

## 6. 页面设计

页面采用当前 `RuoYi-Vue` 单页风格，不拆成多路由详情页。

### 6.1 页面入口

建议新增页面文件：

- `ruoyi-ui/src/views/study/flashcard/index.vue`

### 6.2 页面结构

页面从上到下分为四个区域：

#### 6.2.1 卡组区

包含：

- 卡组下拉选择
- 新建卡组按钮
- 卡组描述展示或编辑入口
- 刷新按钮

#### 6.2.2 导入区

包含两个导入块：

- `CSV` 导入
- `JSON` 批量注入

两者都仅作用于当前选中的卡组。

#### 6.2.3 看板区

主体为四列看板：

- `Q1 重要 / 困难`
- `Q2 重要 / 熟悉`
- `Q3 次要 / 困难`
- `Q4 次要 / 熟悉`

每张卡片展示：

- 正面内容摘要
- 复习次数
- 当前评分
- 最近答案摘要

每张卡片支持：

- 拖拽到其他象限
- 查看
- 编辑
- 删除

#### 6.2.4 练习入口区

可放在工具栏右侧或页面上方，包含：

- 开始练习按钮
- 象限选择
- 顺序模式选择

### 6.3 弹窗设计

#### 6.3.1 卡组弹窗

用于新增和编辑卡组：

- 卡组名称
- 卡组描述

#### 6.3.2 卡片弹窗

用于查看和编辑单卡：

- 正面内容
- 背面内容
- 当前象限
- 复习次数
- 评分
- 最近答案

#### 6.3.3 练习弹窗

保留旧版的主要交互：

- 翻卡
- 输入答案
- 保存答案
- 上一张
- 下一张
- 认识 / 不认识类反馈
- 评分按钮
- 手动改象限

### 6.4 UI 约束

- 不复刻旧版 `viy-ui` 外观
- 使用当前系统 `Element UI` 风格
- 使用当前系统常用的工具栏、表单、对话框和按钮样式
- 提示统一使用当前系统能力

## 7. 数据设计

新增两张业务表：

- `study_flashcard_book`
- `study_flashcard_item`

两张表都沿用 RuoYi 常见审计字段。

### 7.1 卡组表 `study_flashcard_book`

建议字段：

- `book_id`
- `user_id`
- `book_name`
- `book_description`
- `status`
- `remark`
- `create_by`
- `create_time`
- `update_by`
- `update_time`

建议约束：

- 同一 `user_id` 下 `book_name` 唯一

### 7.2 卡片表 `study_flashcard_item`

建议字段：

- `item_id`
- `book_id`
- `user_id`
- `front_content`
- `back_content`
- `quadrant_type`
- `review_count`
- `difficulty_score`
- `last_user_answer`
- `status`
- `remark`
- `create_by`
- `create_time`
- `update_by`
- `update_time`

建议约束：

- `quadrant_type` 仅允许 `1 ~ 4`

### 7.3 索引建议

- `study_flashcard_book(user_id, create_time)`
- `study_flashcard_item(user_id, book_id)`
- `study_flashcard_item(user_id, book_id, quadrant_type)`

## 8. 后端设计

### 8.1 分层方式

按当前 RuoYi 常规实现：

- `controller`
- `service`
- `mapper`
- `domain`

不引入 JPA。

### 8.2 包与文件建议

建议新增以下后端结构：

- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/study/StudyFlashcardBookController.java`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/study/StudyFlashcardItemController.java`
- `ruoyi-system/src/main/java/com/ruoyi/study/domain/StudyFlashcardBook.java`
- `ruoyi-system/src/main/java/com/ruoyi/study/domain/StudyFlashcardItem.java`
- `ruoyi-system/src/main/java/com/ruoyi/study/service/IStudyFlashcardBookService.java`
- `ruoyi-system/src/main/java/com/ruoyi/study/service/IStudyFlashcardItemService.java`
- `ruoyi-system/src/main/java/com/ruoyi/study/service/impl/StudyFlashcardBookServiceImpl.java`
- `ruoyi-system/src/main/java/com/ruoyi/study/service/impl/StudyFlashcardItemServiceImpl.java`
- `ruoyi-system/src/main/java/com/ruoyi/study/mapper/StudyFlashcardBookMapper.java`
- `ruoyi-system/src/main/java/com/ruoyi/study/mapper/StudyFlashcardItemMapper.java`
- `ruoyi-system/src/main/resources/mapper/study/StudyFlashcardBookMapper.xml`
- `ruoyi-system/src/main/resources/mapper/study/StudyFlashcardItemMapper.xml`

### 8.3 接口风格

不沿用旧版 `/flashcard/**.json` 风格，改为当前 RuoYi 风格 REST 接口。

返回统一使用：

- `AjaxResult`

### 8.4 卡组接口

- `GET /study/flashcard/book/list`
  - 获取当前用户全部卡组
- `GET /study/flashcard/book/{bookId}`
  - 获取卡组详情
- `POST /study/flashcard/book`
  - 新增卡组
- `PUT /study/flashcard/book`
  - 修改卡组
- `DELETE /study/flashcard/book/{bookIds}`
  - 删除卡组

卡组删除规则：

- 首版采用级联删除所属卡片

### 8.5 卡片接口

- `GET /study/flashcard/item/list`
  - 按 `bookId` 获取当前卡组卡片
- `GET /study/flashcard/item/{itemId}`
  - 获取单卡详情
- `POST /study/flashcard/item`
  - 新增单卡
- `PUT /study/flashcard/item`
  - 修改单卡
- `DELETE /study/flashcard/item/{itemIds}`
  - 删除卡片
- `PUT /study/flashcard/item/move`
  - 调整象限
- `PUT /study/flashcard/item/score`
  - 记录评分并累计复习次数
- `PUT /study/flashcard/item/answer`
  - 保存最近输入答案
- `POST /study/flashcard/item/import/json`
  - JSON 批量导入
- `POST /study/flashcard/item/import/csv`
  - CSV 导入

### 8.6 安全规则

- 所有查询必须附带当前登录用户条件
- 所有写操作必须校验卡组归属
- 所有写操作必须校验卡片归属
- `itemId` 与 `bookId` 不得跨用户串改

## 9. 导入设计

### 9.1 JSON 导入

接收数组结构。

字段兼容旧版常见命名：

- 正面字段：`frontContent` / `front` / `question`
- 背面字段：`backContent` / `back` / `answer`

批量导入时：

- 目标卡组来自当前选中卡组
- 目标象限来自导入区域选择值

### 9.2 CSV 导入

首版按两列解析：

- 第一列：正面内容
- 第二列：背面内容

兼容策略：

- 允许存在表头
- 尝试识别常见表头名
- 对于空行或缺字段行给出失败原因

### 9.3 导入结果返回

导入接口建议返回：

- 总数
- 成功数
- 失败数
- 失败详情前若干条

前端据此展示导入结果提示。

## 10. 异常处理

- 未选择卡组时，新增卡片、导入、开始练习不可执行
- 卡组为空时，看板显示空态
- 卡片为空时，练习入口提示无可练习内容
- `CSV` 或 `JSON` 格式错误时给出明确提示
- 批量导入支持部分成功、部分失败
- 删除卡组前必须二次确认
- 删除卡片前必须二次确认
- 操作失败时不强制整页刷新
- 练习中保存类失败时不自动切题，避免输入丢失

## 11. 权限与菜单

建议新增权限前缀：

- `study:flashcard:list`
- `study:flashcard:query`
- `study:flashcard:add`
- `study:flashcard:edit`
- `study:flashcard:remove`
- `study:flashcard:import`
- `study:flashcard:practice`

建议新增菜单：

- 一级菜单：`学习`
- 二级菜单：`抽记卡`

## 12. 前端文件建议

建议新增以下前端文件：

- `ruoyi-ui/src/views/study/flashcard/index.vue`
- `ruoyi-ui/src/api/study/flashcard.js`

如果后续页面体量较大，可再拆分：

- `ruoyi-ui/src/views/study/flashcard/components/BookDialog.vue`
- `ruoyi-ui/src/views/study/flashcard/components/ItemDialog.vue`
- `ruoyi-ui/src/views/study/flashcard/components/PracticeDialog.vue`

首版可先集中在一个页面文件中，后续再按复杂度拆分。

## 13. 实施顺序

建议顺序：

1. 新增 SQL 表结构
2. 实现后端 Domain / Mapper / Service / Controller
3. 实现前端 API 封装
4. 实现前端页面
5. 配置菜单与权限
6. 联调与回归

## 14. 验收标准

满足以下条件即视为首版功能闭环：

- 能新建卡组
- 能编辑卡组
- 能删除卡组
- 能手工新增卡片
- 能查看、编辑、删除卡片
- 能按四象限展示卡片
- 能拖拽调整象限
- 能开始练习
- 能保存最近答案
- 能记录评分并累计复习次数
- 能在练习中手动移动象限
- 能导入 `CSV`
- 能导入 `JSON`
- 不同登录用户之间数据隔离正确

## 15. 方案选择说明

本次明确选择 `方案 B`，原因如下：

- 更符合当前项目现有技术栈
- 更符合当前页面和接口习惯
- 更容易接入现有权限体系
- 更容易接入现有菜单体系
- 更方便后续维护和二次扩展

## 16. 迁移来源说明

本设计基于以下迁移包功能规则抽取：

- `nims-modernize/migration-packages/four-quadrant-flashcard/README.md`
- `nims-modernize/migration-packages/four-quadrant-flashcard/frontend/NIMS-ui/src/views/Flashcard/FourQuadrantFlashcard.vue`

本设计不直接复用其技术实现，仅复用业务能力和交互规则。
