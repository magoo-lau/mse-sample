# mse-sample 文档索引

| 文档 | 内容 |
|---|---|
| [../README.md](../README.md) | 示例区定位（monolith/demo/两模板）、跨仓构建顺序、单体快速开始 |
| [../mse-monolith/README.md](../mse-monolith/README.md) | 单体：聚合说明、零中间件起步、MQ/ES 可选启用 |
| [../mse-demo/README.md](../mse-demo/README.md) | demo 微服务：完整启动步骤（compose → Nacos 配置导入 → 启动） |
| [verification.md](verification.md) | 示例回归验证要点 |
| [../AGENTS.md](../AGENTS.md) | 示例区协作约定（边界/构建顺序/提交纪律） |
| [../docker/README.md](../docker/README.md) | 本地 compose（mysql/redis/nacos + monolith 应用容器） |

## 拆仓说明

自 ai-mse 单仓（源 commit `cc5410a3`）的 mse-sample/ 拆分独立。跨仓构建顺序（最后构建）：mse-framework → mse-modules → mse-mp/mse-oa/mse-mes → 本仓。历史设计快照（docs/feature、docs/test/35 用例全文）保留在 ai-mse 仓，本仓 docs 为最终态口径。

## 新业务工程创建路径

1. 照 mse-demo 抄（demo-api + demo-service + demo-boot + demo-ui 四件结构）；
2. 或用 mse-agent-plugin 仓 ZCode 插件 mse-dev（templates/micro|mono|ui 渲染脚手架）；
3. 业务工程落位：mse-modules 仓（多工程）或独立业务仓（如 mse-mes / mse-oa / mse-mp 模式）。
