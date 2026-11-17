# mse-sample —— 示例区（照着抄的参考实现）

自 ai-mse 单仓（源 commit `cc5410a3`）拆分独立。含两个参考工程与两个前端模板，**照抄样板、不进框架区**：

| 工程 | 说明 |
| --- | --- |
| `mse-monolith/` | **单体示例**：聚合基座积木（service-system/platform/auth）+ 全部业务积木（mp/oa/open/data/ai/fs/search/mes/im/demo-service），`mse.rpc.mode=local`，默认仅需 MySQL/Redis（MQ 可选：放开 yaml 的 rocketmq 段即启用，与形态无关；ES 可选）。业务积木全部 SPI 化——引入业务 jar（如 `oa-service`）即完成"选择业务聚合" |
| `mse-demo/` | **微服务业务工程示例**「演示中心」：demo-api（自有契约）/ demo-service（业务积木，SPI 自装配）/ demo-boot（微服务启动器：application.yaml 经 spring.config.import 接 Nacos，mse.rpc.mode=dubbo）/ demo-ui（wujie 子应用）；迁移脚本为 demo-service jar 内 db/方言/ 版本化脚本，目标由 `-- mse-target:` 头指令声明 |
| `mse-ui-sub/` | **独立前端（子应用）模板**：新子应用复制本目录改造（mse-ui-shared 经 npm git 依赖引入：独立仓 github:magoo-lau/mse-ui-shared） |
| `mse-ui-mobile-sub/` | **移动子应用模板**（Vant 4，base=/m-apps/&lt;name&gt;/） |

新建业务工程的完整姿势见本仓 [AGENTS.md](AGENTS.md)；业务工程分别放 **mse-modules 仓**（7 工程）与 **mse-mes / mse-oa / mse-mp 各仓**（ZCode 插件 mse-dev 可脚手架生成，见 mse-agent-plugin 仓）。

## 跨仓构建顺序（本仓最后）

```
1. mse-framework 仓   sh build.sh     # 框架区（mse-starter-parent / mse-dependencies 入库）
2. mse-modules 仓     sh build.sh     # ai/data/fs/im/monitor/open/search
3. mse-mp / mse-oa / mse-mes 各仓     sh build.sh
4. 本仓               sh build.sh     # mse-monolith 聚合全部 biz 发布件，必须最后
```

## 单体快速开始

```sh
docker compose -f docker/docker-compose.yml up -d mysql redis   # 仅需 MySQL + Redis
sh build.sh
java -jar mse-monolith/target/mse-monolith-1.0.0-SNAPSHOT.jar   # 8080，首启自动建库建表
# 或容器化：sh build.sh && docker compose -f docker/docker-compose.yml --profile apps up -d --build
```

登录：platform / 123456（平台端）、user / 123456（租户端）。详见 [mse-monolith/README.md](mse-monolith/README.md) 与 [docs/README.md](docs/README.md)。

## 仓库关系

- 框架底座、基础设施 compose、Nacos 基线配置单一真源：**mse-framework 仓**（含全部框架技术文档）
- 业务积木真源：**mse-modules 仓**（ai/data/fs/im/monitor/open/search）与 **mse-mes / mse-oa / mse-mp 仓**
- 本仓只放示例与模板；docker 编排见 [docker/README.md](docker/README.md)
