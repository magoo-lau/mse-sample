package vip.mseapp.demo.autoconfigure;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import vip.mseapp.demo.api.RemoteDemoOrderService;
import vip.mseapp.demo.mapper.DemoOrderMapper;
import vip.mseapp.demo.rpc.RemoteDemoOrderServiceImpl;
import vip.mseapp.mybatis.config.MseMybatisAutoConfiguration;
import vip.mseapp.core.annotation.TenantServiceModule;

/**
 * 演示中心业务服务自动装配（Spring Boot SPI）。
 *
 * <p>接入方仅需引入 demo-service 依赖：Service / 消息监听 / Mapper 均由此注册，
 * 无需 component-scan 业务包名，也无需 @MapperScan。控制器由
 * {@link DemoControllerAutoConfiguration} 装配，两者均经
 * {@code META-INF/spring/...AutoConfiguration.imports} 登记。
 *
 * <p>RPC 契约实现采用 {@code @Bean + @ConditionalOnMissingBean} 显式注册：
 * 接入方声明自己的同契约 Bean 即可整体替换默认实现。
 */
@AutoConfiguration(before = MseMybatisAutoConfiguration.class)
@MapperScan(basePackages = "vip.mseapp.demo.mapper", annotationClass = Mapper.class)
@ComponentScan(basePackages = {"vip.mseapp.demo.service", "vip.mseapp.demo.mq"})
@TenantServiceModule("mse-demo")
public class DemoServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RemoteDemoOrderService.class)
    public RemoteDemoOrderService remoteDemoOrderServiceImpl(DemoOrderMapper orderMapper) {
        return new RemoteDemoOrderServiceImpl(orderMapper);
    }
}
