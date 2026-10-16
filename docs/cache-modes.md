# 缓存模式

默认使用本地内存缓存，无需启动 Redis；数据库仍需正常配置。

在 `ruoyi-admin/src/main/resources/application.yml` 添加以下配置，或使用同名启动参数：

```yaml
ruoyi:
  cache:
    type: local
```

已有 `ruoyi:` 节点时，将 `cache:` 合并到该节点，不要重复创建。

- `local`：本地内存模式，支持会话、验证码、参数、字典、防重复提交和限流。
- `redis`：启用原 Redis 实现，继续使用原有 Redis 连接配置。

也可通过启动参数切换：

```text
java -jar ruoyi-admin.jar --ruoyi.cache.type=local
java -jar ruoyi-admin.jar --ruoyi.cache.type=redis
```

修改后重启生效。缓存不会在两种模式间迁移，Redis 连接失败时不会自动降级。

本地模式仅适合单实例。重启后会话、验证码、密码错误次数和限流计数均会清空；多实例之间不共享缓存。缓存监控页面展示当前模式及键数量，缓存列表支持查询与清理。

当前业务未使用的 `setCacheSet` / `getCacheSet` 仍仅支持 Redis 模式，本地模式调用会明确报错。
