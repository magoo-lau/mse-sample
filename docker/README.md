# docker/ —— 示例区本地编排

基础设施与共享 Nacos 基线配置的单一真源在 mse-framework 仓 `docker/`；本目录只编排骨架最简集。

## 单体（mse-monolith，推荐起步）

```sh
sh build.sh                                  # 前置：framework/modules/mp/oa/mes 各仓已 build.sh
docker compose up -d mysql redis             # 仅需 MySQL + Redis
java -jar mse-monolith/target/mse-monolith-1.0.0-SNAPSHOT.jar   # 8080，首启自动建库建表
# 或直接容器化：docker compose --profile apps up -d --build
```

## 示例微服务（mse-demo/demo-boot）

```sh
docker compose --profile micro up -d         # mysql + redis + nacos
# 导入 demo 专属配置（mse-demo.yaml）——init-configs.sh 真源在 framework 仓：
CONFIG_DIR="$(pwd)/../mse-demo/docker/nacos/config" \
  sh <framework仓>/docker/nacos/init-configs.sh
sh build.sh && java -jar mse-demo/demo-boot/target/demo-boot-1.0.0-SNAPSHOT.jar   # 8080
```

微服务全套（基座四启动器 + 网关）与 Nacos 基线管理见 mse-framework 仓 `docker/README.md`。

## 账号

platform / 123456（平台端）；user / 123456（租户端，示例租户）。
