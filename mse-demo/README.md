# mse-demo —— 演示中心示例工程（微服务业务工程参考实现）

位于示例区 `mse-sample/`，示范"基座积木拿来即用、业务功能积木式组装"的标准姿势。它本身也是一个可直接复制改造的**业务项目模板**。

```
mse-demo/
├── pom.xml                 # 独立 Maven 工程（仅消费框架区发布件，随 mse-sample 聚合构建）
├── demo-api/               # 自有契约积木：RemoteDemoOrderService + DTO（业务工程对外 RPC 契约示例）
├── demo-service/           # 业务积木：订单/库存/事件 + 混合租户路由 + RocketMQ + Seata（纯 jar，SPI 自装配）
├── demo-boot/              # 微服务启动器：micro-starter + demo-api/demo-service + mq-starter（8080，application.yaml 接 Nacos）
├── demo-ui/                # 前端积木：wujie 微前端子应用（3300，嵌入基座或独立运行）
├── docker/nacos/config/mse-demo.yaml   # Nacos 服务专属配置留档（Data ID: mse-demo.yaml）
└── demo-service/src/main/resources/db/ # jar 内版本化迁移脚本（当前仅随附 mysql 方言，信创方言脚本暂未随 jar 提供）：
                                        #   db/mysql/V20260922_01_demo__demo_init.sql（业务表，-- mse-target: master，目标=业务主库+租户独立库）
                                        #   db/mysql/V20260922_01_demo__demo_base.sql（基座接入，-- mse-target: base，目标=基座库）
```

## 设计原则

- **基座零改动**：框架区只含基础能力（RBAC / 多租户 / 认证 / 系统管理 + 技术组件 starter），演示功能全部在本工程，用不到的代码不需要从基座"分离"出去；
- **积木式组装**：
  - 后端——框架区发布件当积木搭：`mse-starter-parent`（统一父 pom）+ `starter-*`（技术装配件）+ `micro-starter`（微服务形态入口，starter-bridge 基座契约消费桥接随其内置：鉴权数据源/租户路由经契约消费 mse-system/mse-platform，不打包基座 HTTP 接口），自有业务 = `demo-api` + `demo-service` + `demo-boot`；
  - **SPI 接入**：demo-service 经 `META-INF/spring/...AutoConfiguration.imports` 自装配（Controller/Service/Mapper 全部就绪），启动器 `vip.mseapp.boot.demo.DemoBootApplication` 是纯 `@SpringBootApplication`；
  - 前端——复制子应用模板 `mse-sample/mse-ui-sub` 为本工程 `demo-ui`，在基座注册表 `mse-framework/mse-ui/src/micro/apps.ts` 登记一行即接入（wujie 微前端，共享登录态与主界面）。

## 1. 准备：安装基座发布件

```bash
# 仓库根目录（ai-mse）构建框架区，再单独构建示例区（mse-demo + mse-monolith，须在 mse-modules 各工程之后）
mvn install -DskipTests
mvn install -DskipTests -f mse-sample/pom.xml
```

## 2. 初始化数据库

基座表免手工执行：mse-system（或单体）启动时自动应用 jar 内基座版本化脚本（mse-target: base）——本节全部 SQL 零手工；本工程 SQL 为 **jar 内版本化脚本 + 全自动应用**：

- 业务表（`demo-service/src/main/resources/db/mysql/V20260922_01_demo__demo_init.sql`，头指令 `-- mse-target: master`）：demo-boot 启动时自动应用（`mse.tenant.schema.auto-migrate: true` 已开启，失败阻断启动；多实例经 Redis 锁按目标库互斥）；
- 基座接入（`db/mysql/V20260922_01_demo__demo_base.sql`，头指令 `-- mse-target: base`，子应用注册 + 页面菜单 + 权限点，幂等）：随 starter-bridge 就绪即推 mse-system 按分册账本（`mse_demo_schema_history`）应用，周期重推自愈；租户端页面菜单需在「套餐管理」勾选。

```bash
cd mse-framework/docker && docker compose up -d mysql redis
# 基座表免手工执行：mse-system（或单体）启动时自动应用 jar 内基座版本化脚本（mse-target: base）——本节全部 SQL 零手工
# 本工程业务表 + 基座接入：启动 demo-boot 即自动完成（见第 3 步），无需手工执行
# 独立库租户 mse_tenant2 的 demo_order / undo_log 也不再随脚本预建：由平台端「租户数据源」界面初始化/懒初始化克隆+迁移自动就绪
```

> 权限点 `demo:order:list/create` 已挂靠「订单演示」页面菜单：随菜单参与授权分组与套餐裁剪；
> 租户端需在「套餐管理」勾选菜单后按角色授权，平台端登录即可见。
> 信创库（达梦/金仓）：演示脚本为 jar 内 db/mysql/（信创方言脚本暂未入库，切换信创前需自行转译）。

## 3. 启动后端（demo-boot，微服务形态）

```bash
# 前置：Nacos 已启动（mse-framework/docker compose up -d nacos，基线自动导入）
cd mse-sample/mse-demo
CONFIG_DIR="$PWD/docker/nacos/config" sh ../../mse-framework/docker/nacos/init-configs.sh   # 首次：mse-demo.yaml 导入 Nacos
mvn install -DskipTests
java -jar demo-boot/target/demo-boot-1.0.0-SNAPSHOT.jar
# 8080，application.yaml（spring.config.import）接 Nacos 配置中心并注册（mse.rpc.mode=dubbo，网关 /demo/** 可达）
```

可选中间件（仅订单事件消费 / Seata 跨库回滚演示需要，登录与系统管理不依赖）：

```bash
cd mse-framework/docker && docker compose up -d rocketmq-namesrv rocketmq-broker seata
# Seata 默认关闭（上游 2.6 缺陷），开启：Nacos mse-demo.yaml 中 MSE_SEATA_ENABLED=true（或启动环境变量注入）
```

## 4. 启动前端

```bash
cd mse-framework/mse-ui && npm install && npm run dev            # 基座：http://localhost:3000
cd mse-sample/mse-demo/demo-ui && npm install && npm run dev     # 演示中心子应用：http://localhost:3300
```

登录租户端（导入可选示例租户 `mse-framework/docker/<方言>/demo/02_demo_tenant.sql` 后为 admin/tenant123，或自建租户的管理员账号），侧边栏「演示中心 → 订单演示」即为本子应用页面。

## 5. 演示内容与验证点

| 端点 / 页面 | 验证点 |
|---|---|
| `POST /demo/order/place` | 库存扣减（共享主库）+ 订单创建（`@TenantDatasource`：独立库租户路由专属库）；Seata 开启后 `fail=true` 验证跨库全局回滚 |
| `GET /demo/order/page` | 租户行级隔离（`tenant_id` 自动过滤）+ 数据权限 |
| `GET /demo/order/events` | RocketMQ 异步链路租户上下文恢复（消费端落 `demo_event`） |
| 独立库租户（平台端开通 `ds_key=tenant2` 的租户） | 订单物理落 `mse_tenant2.demo_order` |
| `RemoteDemoOrderService`（demo-api 契约） | 自有契约示例：`@RpcService` 实现 + `@RpcReference` 消费，dubbo 经 Nacos 注册中心远程调用（单体聚合形态下自动本地直调），消费方零改动 |

## 6. 容器化与 K8s（业务工程照抄姿势）

容器化资产随启动器模块存放，本工程已按底座姿势配好，可直接参考：

- **Dockerfile**：`demo-boot/Dockerfile`——从底座启动器（如 `mse-framework/mse-boot/boot-system/Dockerfile`）复制一份放到业务启动器目录，仅默认 `MODULE/PORT` 不同；构建 context 必须是仓库根目录：

  ```bash
  docker build -f mse-sample/mse-demo/demo-boot/Dockerfile -t mse/demo-boot:1.0.0 .
  ```

- **K8s 清单**：`demo-boot/k8s/demo.yaml`（Deployment + ClusterIP Service，探针 exec+curl 带 X-Actuator-Token 令牌头，MSE_ACTUATOR_TOKEN 环境变量与 mse-micro-common.yaml 同源）；共享环境变量复用 `mse-framework/mse-boot/k8s/configmap.yaml`（微服务必用 `MSE_NACOS_ADDR`，Nacos 配置内容里的 `MSE_*` 占位符经其生效）：

  ```bash
  kubectl apply -f mse-framework/mse-boot/k8s/configmap.yaml -f mse-sample/mse-demo/demo-boot/k8s/demo.yaml
  ```

## 7. 复制为自己的业务项目

1. 复制 `mse-sample/mse-demo/` 到 `mse-modules/<你的工程名>/`，全局替换 `demo` 为你的业务名（包名 `vip.mseapp.demo`、模块 `demo-*`、启动类 `vip.mseapp.boot.demo`、路由前缀 `/sub/demo`、端口、`prodEntry`）；
2. 删掉 `demo-service` 里的示例代码，换成自己的业务（Controller/Service/Mapper 写法照旧，基座能力直接用；保持 SPI 自动装配结构）；
3. 基座侧：`mse-framework/mse-ui/src/micro/apps.ts` 修改注册项（name/title/devEntry/prodEntry/menu）；
4. `demo-service` 内 `db/<方言>/` 版本化脚本换成自己的 DDL + 权限点（头指令 `-- mse-target: master|base`，命名 `V{yyyyMMdd}_{NN}_<biz>__<描述>.sql`，已应用脚本不再修改、变更新增版本文件；权限点挂菜单则需在平台端「菜单管理/权限点管理」登记）。
