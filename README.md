# mse-sample —— 示例区

照着抄的参考实现，独立构建（在 mse-modules 各工程之后执行 `mvn install -DskipTests -f mse-sample/pom.xml`）。示例代码禁止上移到 `mse-framework/`。

| 工程 | 说明 |
| --- | --- |
| `mse-monolith/` | **单体示例**：聚合基座积木（service-system/tenant/auth）+ mse-modules 现有全部业务积木（含 search，需 Elasticsearch，未启动不影响其余功能），`mse.rpc.mode=local`，默认仅需 MySQL/Redis（MQ 可选：放开 yaml 的 rocketmq 段即启用，与形态无关）。业务积木全部 SPI 化——引入业务 jar（如 `oa-service`）即完成"选择业务聚合" |
| `mse-demo/` | **微服务业务工程示例**「演示中心」：demo-api（自有契约）/ demo-service（业务积木，SPI 自装配）/ demo-boot（微服务启动器：application.yaml（spring.config.import）接 Nacos，mse.rpc.mode=dubbo）/ demo-ui（wujie 子应用）（迁移脚本为 demo-service jar 内 db/<方言>/ 版本化脚本，目标由 -- mse-target: 头指令声明） |
| `mse-ui-sub/` | **独立前端（子应用）模板**：新子应用复制本目录改造 |

新建业务工程的完整姿势见根目录 [AGENTS.md](../AGENTS.md)；业务工程统一放 `mse-modules/`。
