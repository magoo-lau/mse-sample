# 验证要点（示例区回归清单）

完整用例目录在 ai-mse 仓 docs/test/35-示例区-*.md（历史存档）；本仓维护最终态回归要点。

## 一、单体 mse-monolith

1. 跨仓全链构建：framework → modules → mp/oa/mes → 本仓 `sh build.sh` 全绿。
2. 起步三件套：`docker compose up -d mysql redis` → `java -jar mse-monolith/target/mse-monolith-1.0.0-SNAPSHOT.jar` → 首启自动建库建表（平台库/共享租户库/租户域副本）。
3. 登录：platform/123456 平台端、user/123456 租户端；`/system/profile/me?client=2` 移动端视图。
4. 业务积木即插即用：pom 增删 `xxx-service` jar 即完成聚合（SPI 零配置）。
5. MQ 可选：放开 yaml rocketmq 段 + framework 仓 compose 起 MQ 后事件链路通（未配时 noop 降级不影响启动）。
6. ES 可选：compose 起 ES 后搜索功能可用；未启动不影响其余功能。

## 二、微服务示例 mse-demo

1. compose `--profile micro` 起 mysql/redis/nacos；CONFIG_DIR 导入 mse-demo.yaml；demo-boot 启动（8080）。
2. demo 租户（租户2）下单落 `mse_tenant2.demo_order`（独立库路由）。
3. `POST /api/demo/order/place` 构造 `"fail":true`：Seata 开启时全局回滚（@GlobalTransactional 生效）。
4. 网关路由 `/demo/** → lb://mse-demo`；demo-ui 子应用经 `/sub-apps/demo/` 挂载可访问。

## 三、模板

1. mse-ui-sub / mse-ui-mobile-sub：复制改 name/端口/base 后 `npm ci && npm run build` 通过（mse-ui-shared 为 npm git 依赖，allow-git 已随模板 .npmrc 提供）。
2. mse-agent-plugin 仓 templates 渲染产物与本模板语义一致（那边为去业务化精炼版）。

## 四、工程底线

- `mvn install -DskipTests -f pom.xml` 通过；demo-ui 构建通过。
- 镜像：`docker build -f mse-monolith/Dockerfile .` 与 `docker build -f mse-demo/demo-boot/Dockerfile .` 成功（context=本仓根）；compose `--profile apps` 容器化单体可登录。
- 端口占用：单体 8080 与 Nacos v3 控制台宿主映射 18080 不冲突。
