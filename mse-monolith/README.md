# mse-monolith —— 单体示例（聚合基座 + 全部业务系统）

聚合**单体形态入口 `mono-starter`**（内含基座 service-system / service-platform / service-auth + 全部装配件 + 三方言驱动）
与**现有全部业务积木**（mse-modules：mp-service / oa-service / open-service / data-service / ai-service / fs-service / search-service / monitor-service）的单体启动器：
`mse.rpc.mode=local` 本地直调，默认仅依赖 MySQL + Redis（无 Nacos/Dubbo/Seata）。
**MQ 与单体/微服务形态无关**：`mq-starter` 已随包引入，需要 RocketMQ 时放开 `application.yaml` 的
`rocketmq` 段（或设 `MSE_ROCKETMQ_ADDR`）即可，无需换形态（见下方「启用 MQ」）。
**搜索引擎（search-service）另需 Elasticsearch**：`docker compose -f mse-framework/docker/docker-compose.yml up -d elasticsearch`；
ES 未启动不影响单体启动与其余功能，仅搜索索引/查询不可用（见下方「启用 Elasticsearch」）。

| 聚合的系统 | 引入的积木 jar | 说明 |
| --- | --- | --- |
| 基座（随 mono-starter） | service-system / service-platform / service-auth | RBAC、多租户、认证、系统管理 |
| 微信公众号 | mp-service | SPI 自装配；微信回调 `/mp/callback/**` 以签名自鉴权 |
| 协同办公 | oa-service | 已聚合 oa-form/oa-bpm，内置 Flowable 8（首次启动自动建 ACT_ 表）+ OaOrgService 基座桥接 |
| 开放平台 | open-service | 开放 API 以签名自鉴权；经契约本地直调基座数据 |
| 数据中台 | data-service | 元数据/集成/服务/质量；外部数据源运行期按注册信息建连接 |
| AI 智能体 | ai-service | 模型中心/对话/智能体（草稿-发布双版本、运行历史、并行编排）/工具中心/知识库 RAG/MCP/开放 API；默认未引模型 starter，按 pom 注释启用（如 spring-ai-starter-model-openai） |
| 文件存储 | fs-service | 本地/RustFS（S3 兼容）可切换；单体默认 local 磁盘，需要 RustFS 时补引 awssdk s3 依赖；业务经 api-fs 契约统一接入 |
| 搜索引擎 | search-service | Elasticsearch 9 全文检索（需 ES 中间件，未启动不影响其余功能）；源登记/增量数据经契约本地直调 |
| 监控中心 | monitor-service | 服务/中间件/操作系统监控 + 阈值告警（站内信/Webhook，通知设置在界面维护）；OS 指标采集单体所在主机 |

> 命名规范：名字含 `monolith` = 聚合单体；微服务启动器见 `mse-framework/mse-boot/boot-*`。

## 快速开始

```bash
# 1) 基座入仓（根聚合只含 mse-framework）
mvn install -DskipTests
# 2) 业务工程入仓（单体聚合依赖其发布件；已聚合全部，需先构建）
for p in mse-mp mse-oa mse-open mse-data mse-ai mse-fs mse-search mse-monitor; do mvn install -DskipTests -f mse-modules/$p/pom.xml; done
# 3) 构建单体（mse-sample 独立聚合：mse-demo + mse-monolith）
mvn install -DskipTests -f mse-sample/pom.xml

# 4) 初始化数据库：零手工——primary=platform（默认 mse_platform）首启自动建库，基座结构与种子随启动自动应用；
#    缺省共享库（默认 mse_tenant）由 SharedMainBootstrap 按 mse.tenant.shared-main.* 建库（MySQL；自 declare 注册内置池、不落登记表）
# 各业务积木的 jar 内 db/<方言>/ 版本化脚本（-- mse-target: master 业务表 + -- mse-target: base 基座接入）随第 5 步启动自动应用：
#   —— mse-target: master：不随启动直建——缺省共享库身份表由 SharedMainBootstrap eager 建、业务表随各服务首路由懒直建（0.1.0 去默认租户库）
#   —— mse-target: base：基座接入（子应用注册/页面菜单/权限点，幂等）经 starter-bridge 推送 mse-system 按账本应用，周期重推自愈
#   —— 独立库租户 mse_tenant2 示例不再随脚本预建：由平台端「租户数据源」界面初始化/懒初始化克隆+迁移自动就绪

# 5) 启动（8080，启动即自动建表 + 推送基座注册）
java -jar mse-sample/mse-monolith/target/mse-monolith-1.0.0-SNAPSHOT.jar
```

信创库切换：`--spring.profiles.active=dm` / `kingbase`（驱动已随包打入；业务 SQL 为 jar 内 db/<方言>/ 版本化脚本，自动应用，信创方言脚本暂未随业务 jar 提供）。

## 启用 MQ（可选）

MQ 是异步解耦能力，与单体/微服务形态正交——单体不是"没有 MQ"，只是默认不配。`mq-starter` 已随包：
未配置 `rocketmq.name-server` 时事件发送降级 noop（warn 后丢弃），其余功能不受影响；需要时两步启用：

```bash
docker compose -f mse-framework/docker/docker-compose.yml up -d rocketmq-namesrv rocketmq-broker
# 然后放开 mse-monolith application.yaml 的 rocketmq 段（或设环境变量 MSE_ROCKETMQ_ADDR=localhost:9876）重启
```

消费端示例见 mse-demo 的 `OrderEventListener`（租户上下文经 `TenantEvent` 恢复），单体下行为与微服务一致。

## 启用 Elasticsearch（搜索引擎 search-service 依赖）

search-service 聚合后即随单体自装配（源管理/任务中心/热词统计/搜索测试接口全部就绪），
索引与查询需要 Elasticsearch 9.x 中间件（镜像已预装 smartcn 中文分析插件，单节点免鉴权）：

```bash
docker compose -f mse-framework/docker/docker-compose.yml up -d elasticsearch
# 连接地址经 application.yaml 的 spring.elasticsearch.uris（或环境变量 MSE_ES_HOST）调整，默认 localhost:9200
```

ES 未启动不影响单体启动与其余功能，仅搜索索引/查询不可用；验收路径 = 管理端「搜索引擎」登记源
→ 搜索测试推文档 → 顶栏搜索/搜索测试查询（源登记与增量数据经契约本地直调，无需业务改造）。

## 如何增删聚合的业务积木

所有业务积木（mse-modules 各工程的 `xxx-service`，及 mse-demo 的 `demo-service`）均经 **Spring Boot SPI 自动装配**：
pom 里增删对应依赖即可，Controller/Service/Mapper 自动就绪，**无需任何 scanBasePackages / @MapperScan / 装配配置**：

```xml
<dependency>
    <groupId>vip.mseapp</groupId>
    <artifactId>mp-service</artifactId>   <!-- 示例：去掉即不聚合公众号 -->
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

注意：

- **不聚合的业务无需任何处理**：对应业务表可以不建（脚本只在聚合启动时应用），其 yaml 配置键（如 `mse.mp.*`）冗余无害
  （全部有代码默认值，见各工程 `*Properties`）；
- 各业务积木的初始化 SQL 为 jar 内版本化脚本（`xxx-service/src/main/resources/db/<方言>/` 的版本化脚本，
  mse-target 头区分业务表/基座接入），聚合启动时自动应用（master 随 auto-migrate 建业务表、
  base 经桥接推送登记基座数据），无需手工执行；脚本幂等，重复应用无害：权限点已挂靠页面菜单，
  历史安装（menu_id=NULL）重跑会自动补挂；
- **聚合 mse-oa** 必须引入 `oa-service`（而非 oa-form/oa-bpm 散件）——它已内置
  `OaOrgService` 基座桥接默认实现（单体下本地直调同容器实现）；对接其他组织中台时
  声明自己的 `OaOrgService` Bean 即可整体替换，oa-bpm 零改动；
- **聚合 mse-demo 的 demo-service**（可选）：会连带引入 RocketMQ 与 Seata 依赖，需保持
  `seata.enabled=false` 使 `@GlobalTransactional` 被忽略（`@Transactional` 不受影响）；
  MQ 按「启用 MQ」一节放开配置即可——按需自行评估；
- **聚合 mse-search** 需运行 Elasticsearch（见「启用 Elasticsearch」一节）；不聚合时其 yaml
  配置键（`mse.search.*`、`spring.elasticsearch.*`）冗余无害，ES 客户端也不会被装配；
- **聚合 mse-monitor**（监控中心）：拉模式零侵入，预置目标仅 MySQL/Redis 默认启用，其余
  （微服务/Nacos/ES/RocketMQ）在「监控目标」页按部署实况启停；OS 指标 = OSHI 采集单体所在
  主机；告警通知的收件人/Webhook 在「告警中心 → 通知设置」界面维护（存 mon_setting，无 yaml）；
  调度为内存态，单实例部署即可；参数默认值即开箱可跑（`mse.monitor.*` 见其 MonitorProperties）；
- 业务积木的默认控制器可用 `mse.service.<name>.controller.enabled=false` 整体关闭后由自有控制器替代；
- **租户端可见性**：子应用页面菜单需在「套餐管理」勾选后，租户侧才可见/可授权（页面级裁剪）；
  平台端子应用（如开放平台）登录即可见。

镜像构建用本工程自带的 `Dockerfile`（`docker build -f mse-sample/mse-monolith/Dockerfile -t mse/mse-monolith:1.0.0 .`，context 为仓库根）；K8s 清单见本工程 `k8s/monolith.yaml`（探针 exec+curl 带 X-Actuator-Token（MSE_ACTUATOR_TOKEN，单体 application.yaml 的 mse.actuator.auth.token 同源））。
