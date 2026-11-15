package vip.mseapp.boot.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 演示中心启动器（微服务示例工程）。
 *
 * <p>启动类位于 vip.mseapp.boot.demo，与业务积木包树零重叠——默认扫描不触碰任何积木，
 * 一切组件经 Spring Boot SPI 自动装配：自有积木（demo-api/demo-service）引入 jar 即得
 * 全部能力（含 Mapper 注册）；基座能力不打包在本服务——鉴权数据源/独立库租户路由经
 * starter-bridge 以契约消费 mse-system/mse-tenant。
 * 各默认实现均带 @ConditionalOnMissingBean 回退，声明同契约 Bean 即可整体替换。
 *
 * <p>微服务形态（mse.rpc.mode=dubbo）：本地仅 application.yaml（spring.config.import），数据源/Redis/RocketMQ/Seata 等
 * 统一在 Nacos 配置中心管理（服务专属 Data ID 留档 docker/nacos/config/mse-demo.yaml），启动即注册。
 */
@SpringBootApplication
public class DemoBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoBootApplication.class, args);
    }
}
