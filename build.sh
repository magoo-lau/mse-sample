#!/bin/sh
# =============================================================================
# 示例区构建：mvn install -f pom.xml（聚合 mse-monolith + mse-demo，产物入本地仓库）
#
# 前置（跨仓构建顺序——本仓最后构建）：
#   1) mse-framework 仓  sh build.sh        # 框架区
#   2) mse-modules 仓    sh build.sh        # ai/data/fs/im/monitor/open/search
#   3) mse-mp / mse-oa / mse-mes 各仓 sh build.sh
# mse-monolith 聚合全部 biz 发布件（mp/oa/open/data/ai/fs/search/mes/im/demo-service），
# 必须最后构建；单体运行仅需 MySQL + Redis（数据库零手工，首启自动建库建表）。
#
# 用法：sh build.sh / MSE_RUN_ALL_TESTS=1 sh build.sh / MAVEN_ARGS=...
# =============================================================================
set -eu
cd "$(dirname "$0")"

MVN="mvn -B -ntp ${MAVEN_ARGS:-}"
if [ "${MSE_RUN_ALL_TESTS:-0}" = "1" ]; then
  $MVN install -f pom.xml
else
  $MVN install -f pom.xml -DskipTests
fi
echo "示例区构建完成"
