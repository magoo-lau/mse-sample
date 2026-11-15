package vip.mseapp.demo.api;

import vip.mseapp.demo.api.dto.DemoOrderDTO;

/**
 * 示例订单服务契约（demo-service 实现）。
 *
 * <p>业务工程自有契约示例：其他服务（或外部 Java 系统）依赖 demo-api 后经
 * {@code @RpcReference} 消费——单体 local 模式本地直调，微服务 Dubbo 模式远程调用，
 * 消费方代码零改动。租户上下文经 RPC 隐式透传，契约方法无需携带 tenantId。
 */
public interface RemoteDemoOrderService {

    /** 按订单号查询（跨租户唯一）；不存在返回 null */
    DemoOrderDTO getOrderByOrderNo(String orderNo);
}
