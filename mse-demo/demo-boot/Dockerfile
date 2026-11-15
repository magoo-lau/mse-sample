# syntax=docker/dockerfile:1
# =============================================================================
# ai-mse demo-boot 启动器镜像（mse-demo 示例工程）—— 业务工程 Dockerfile 的参考实现：
# 从底座启动器（mse-framework/mse-boot/boot-*）复制一份放到业务启动器目录、改默认
# MODULE/PORT 即可，模板体保持一致（改运行层处理需同步各份 Dockerfile）
#
# 构建（context 必须是仓库根目录，源码自包含，无需宿主机预装 Maven/JDK）：
#   docker build -f mse-sample/mse-demo/demo-boot/Dockerfile -t mse/demo-boot:1.0.0 .
#
# 本地已 mvn package 想快速出镜像：mse-framework/mse-boot/Dockerfile.prebuilt（所有启动器通用）
# =============================================================================

ARG MAVEN_IMAGE=maven:3.9-eclipse-temurin-21
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre-jammy

# ---- 阶段 1：Maven 构建 ----
FROM ${MAVEN_IMAGE} AS builder
ARG MODULE=mse-sample/mse-demo/demo-boot
# 国内网络慢时可自行挂载加速配置，如：--build-arg MAVEN_ARGS="-s /path/settings.xml"
ARG MAVEN_ARGS=""
WORKDIR /build

COPY . .
# .m2 走 BuildKit 缓存挂载：源码变动不触发依赖重新下载
# -am 连带构建该启动器依赖的 service/common/api 模块
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests ${MAVEN_ARGS} package -pl ${MODULE} -am

# Spring Boot tools jarmode 分层解包：依赖层与应用层分离，业务代码变动时命中缓存只换最上层
# 统一重命名为 app.jar，解包产物为 app.jar（瘦启动器）+ lib/（依赖），固定文件名便于启动
RUN cp ${MODULE}/target/*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --destination /extracted

# ---- 阶段 2：运行时 ----
FROM ${RUNTIME_IMAGE}
ARG PORT=8080

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

# 非 root 运行（k8s runAsNonRoot / OpenShift 安全基线）
RUN groupadd -r -g 1001 app && useradd -r -u 1001 -g app -s /usr/sbin/nologin app

WORKDIR /app
COPY --from=builder --chown=app:app /extracted/dependencies/ ./
COPY --from=builder --chown=app:app /extracted/spring-boot-loader/ ./
COPY --from=builder --chown=app:app /extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=app:app /extracted/application/ ./
USER app
EXPOSE ${PORT}

# exec java 使 JVM 为 PID 1 直接接收 SIGTERM：k8s 滚动更新 / docker stop 时优雅停机
# 内存按容器 limit 自适应（MaxRAMPercentage=75），k8s 里可用 JAVA_OPTS 覆盖
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
