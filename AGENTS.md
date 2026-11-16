# AGENTS.md —— mse-sample 示例仓协作约定

本文件面向所有在本仓库工作的 AI 助手与开发者。本仓库自 ai-mse 单仓（源 commit `cc5410a3`）拆分独立。

## 一、定位

示例区 = **照着抄的参考实现**，不进框架区、不进业务仓：

- `mse-monolith/`：单体聚合示例（mono-starter + 基座三积木 + 全部 biz 积木 jar；`mse.rpc.mode=local`；仅依赖 MySQL+Redis，MQ 经 mq-starter 可选）
- `mse-demo/`：微服务业务工程标准样板（demo-api + demo-service + demo-boot + demo-ui）——**新业务工程照此创建**（ZCode 插件 mse-dev 模板即其去业务化版本，见 mse-agent-plugin 仓）
- `mse-ui-sub/`、`mse-ui-mobile-sub/`：独立前端模板（file:../../mse-framework/mse-ui-shared，渲染/复制后改 name、端口、base）

## 二、跨仓构建顺序（红线）

本仓**最后构建**：mse-framework → mse-modules → mse-mp/mse-oa/mse-mes → 本仓（mse-monolith 聚合全部 biz 发布件，版本 1.0.0-SNAPSHOT 逐一显式声明）。任一上游仓变更后本仓需重新 `sh build.sh`。

## 三、示例代码边界

- 示例功能（demo 表、演示接口、模板）只进本仓；**禁止**把示例代码加回 mse-framework 或业务仓。
- 模板（mse-ui-sub 等）改动需与 mse-agent-plugin 仓 `templates/ui` 渲染模板保持同源语义（那边是去业务化精炼版）。
- 单体 `application.yaml` 是本地全量配置样板（数据源/Redis/ES/Milvus/信创 profile 全在），改配置项时同步文档。

## 四、构建、docker 与验证

- `sh build.sh`（前置五仓已 install）；`docker compose -f docker/docker-compose.yml up -d mysql redis`。
- 镜像：`mse-monolith/Dockerfile`、`mse-demo/demo-boot/Dockerfile`（context=本仓根）；compose `--profile apps` 容器化单体、`--profile micro` 起 Nacos 供 demo 微服务。
- 验证底线：`mvn install -DskipTests -f pom.xml` 通过；demo-ui `npm run build` 通过；demo 租户下业务行为回归见 docs/verification.md（租户2 下单落 `mse_tenant2.demo_order`、`/demo/order/place` "fail":true 全局回滚等）。

## 五、提交纪律

示例区改动独立 commit；涉及演示基座能力的，评估是否应上移框架仓（修底座优先于在示例绕过）。
