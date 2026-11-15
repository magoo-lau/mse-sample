package vip.mseapp.demo.mq;

import vip.mseapp.core.context.TenantContext;
import vip.mseapp.mq.TenantEvent;
import vip.mseapp.demo.entity.DemoEvent;
import vip.mseapp.demo.mapper.DemoEventMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 订单事件消费者：从消息体恢复租户上下文后处理（落 demo_event 表），
 * 验证异步链路的租户 SQL 隔离与租户归属正确性。
 *
 * <p>仅在配置了 rocketmq.name-server 时注册消费容器——未启用 MQ 的环境
 * （未配置 rocketmq 的单体/微服务均可）跳过装配，否则 rocketmq-spring 初始化容器会抛
 * "Property 'nameServer' is required"。
 */
@Component
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
@RocketMQMessageListener(topic = "mse-demo-order-event", consumerGroup = "mse-demo-order-event-group")
public class OrderEventListener implements RocketMQListener<TenantEvent> {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final DemoEventMapper eventMapper;

    public OrderEventListener(DemoEventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    @Override
    public void onMessage(TenantEvent event) {
        log.info("[mse-demo] 收到租户事件: tenantId={}, type={}", event.tenantId(), event.eventType());
        TenantContext.runAs(event.tenantId(), () -> {
            DemoEvent record = new DemoEvent();
            record.setEventType(event.eventType());
            record.setPayload(event.payload());
            eventMapper.insert(record);
        });
    }
}
