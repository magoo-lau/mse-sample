package vip.mseapp.boot.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 单体启动器：聚合全部 service 模块，本地直调（mse.rpc.mode=local）。
 *
 * <p>基座 service 模块经 Spring Boot SPI（AutoConfiguration.imports）自装配：
 * 引入 jar 即得到全部接口与默认实现，启动类无需 scanBasePackages 覆盖基座包名，
 * 也无需 @MapperScan。各默认实现均带 @ConditionalOnMissingBean 回退，
 * 声明同契约 Bean 即可整体替换（如自定义 SysUserService）。
 *
 * <p>默认仅需 MySQL + Redis 即可运行，无 Nacos/Dubbo/Seata 依赖——
 * 开发与轻量部署零中间件成本。MQ 与形态正交：application.yaml 放开 rocketmq 段即启用
 * （mq-starter 已随包），未配置时租户事件发送降级 noop，其余功能不受影响。
 */
@SpringBootApplication
public class MonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonolithApplication.class, args);
    }
}
