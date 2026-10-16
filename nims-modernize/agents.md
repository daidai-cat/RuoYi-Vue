## 回答原则

- 先给结论，1-3 条短 bullet

- 必要信息为主，避免废话

- 每句提供新信息，不重复

- 默认不举例，除非影响理解



---



## Skill 使用规范

- 用户点名 skill → 必须使用

- 明显匹配 skill → 必须使用

- 多个 skill → 最少必要集合并说明顺序

- **子agent调度规则**：

  - 主agent可自动分配子任务给子agent

  - 子agent完成 → 结果回传主agent → 主agent整合 + 完成性判断

  - 子agent调用 skill 遵循 Skill 使用规范



已安装：

- claudeception

- superpower

- homunculus

- skill-from-masters



---



## 容错机制（新增）

- 子agent执行步数/Token 超过阈值 → 自动中止并回传错误

- 主agent收到异常 → 回退安全状态 + 日志提示

- 防止死循环、无限递归、Token 溢出



---



## 代码规范

- 必须添加注释（英文 + 中文）

```javascript

// 获取 Dealer Map 信息

// Get dealer map information

```



---



## Git 提交规范

- <type>(scope): description

- 类型：feat / fix / refactor / perf / docs / chore / test / ci / build / style

- Breaking Change：

  - feat!: xxx

  - BREAKING CHANGE: xxx



---



## 安全规则

- 不泄露隐私

- 不执行危险操作（未确认）

- 删除文件优先用 trash

- 不确定 → 先询问



---



## 项目注意事项

- 不需写 junit / vitest

- 不需执行编译 / lint

- 注意中文编码

- 文件引用：

  [app/cli.py:101](./app/cli.py#L101)



---



## Agent 协作策略（新增）

- 大任务可分解 → 创建子agent处理子任务

- 子agent完成 → 回传结果 → 主agent整合和完成性判断

- 完整任务完成后 → 主agent调用 qwnotify skill 发送通知

- 子agent执行优先使用必要 skill，避免重复调用



### 流程示意

``` 

Main Agent receives task

        │

        ├─> 可分解? → Yes → Spawn SubAgents

        │           ├─ SubAgent1: 处理子任务 A

        │           ├─ SubAgent2: 处理子任务 B

        │           └─ SubAgentN: 处理子任务 N

        │

        └─> 子agent完成 → 结果回传主agent

                 │

                 └─ 主agent整合 / 完成性判断

                        │

                        └─ 任务完成 → qwnotify 通知

```



---



## Identity 文件指向

identity_file: "./IDENTITY.md"