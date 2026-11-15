package vip.mseapp.demo.rpc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import vip.mseapp.core.annotation.RpcService;
import vip.mseapp.demo.api.RemoteDemoOrderService;
import vip.mseapp.demo.api.dto.DemoOrderDTO;
import vip.mseapp.demo.entity.DemoOrder;
import vip.mseapp.demo.mapper.DemoOrderMapper;

import java.util.List;

/**
 * 示例订单契约实现：单体 local 模式本地直调，微服务 Dubbo 模式自动导出（消费方零改动）。
 */
@RpcService(RemoteDemoOrderService.class)
public class RemoteDemoOrderServiceImpl implements RemoteDemoOrderService {

    private final DemoOrderMapper orderMapper;

    public RemoteDemoOrderServiceImpl(DemoOrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public DemoOrderDTO getOrderByOrderNo(String orderNo) {
        List<DemoOrder> records = orderMapper.selectList(
                new LambdaQueryWrapper<DemoOrder>().eq(DemoOrder::getOrderNo, orderNo));
        return records.isEmpty() ? null : toDto(records.get(0));
    }

    private static DemoOrderDTO toDto(DemoOrder order) {
        DemoOrderDTO dto = new DemoOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setProductName(order.getProductName());
        dto.setAmount(order.getAmount());
        dto.setRemark(order.getRemark());
        dto.setTenantId(order.getTenantId());
        return dto;
    }
}
