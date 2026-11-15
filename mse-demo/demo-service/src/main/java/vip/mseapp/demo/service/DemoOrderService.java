package vip.mseapp.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import vip.mseapp.core.context.TenantContext;
import vip.mseapp.core.exception.ServiceException;
import vip.mseapp.demo.entity.DemoOrder;
import vip.mseapp.demo.entity.DemoStock;
import vip.mseapp.demo.mapper.DemoOrderMapper;
import vip.mseapp.demo.mapper.DemoStockMapper;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 下单全链路演示：Seata AT 跨数据源事务 + RocketMQ 租户事件。
 *
 * <p>链路：扣减库存（共享主库）→ 创建订单（@TenantDatasource：独立库租户路由专属库）
 * —— 两条语句落在不同物理库，由 @GlobalTransactional 保证原子性；
 * 提交后（控制器触发）发送租户事件，消费端恢复上下文落 demo_event。
 */
@Service
public class DemoOrderService {

    private static final Logger log = LoggerFactory.getLogger(DemoOrderService.class);

    private final DemoOrderMapper orderMapper;
    private final DemoStockMapper stockMapper;

    public DemoOrderService(DemoOrderMapper orderMapper, DemoStockMapper stockMapper) {
        this.orderMapper = orderMapper;
        this.stockMapper = stockMapper;
    }

    /**
     * 扣减库存（主库，本地事务——不在全局事务内）。
     *
     * <p>已知限制（Seata 2.6 + seata-all fat jar 组合）：UPDATE 语句在 undo 生成阶段触发
     * “undo_log needs to contain the primary key” 缺陷（全局回滚本身正确，但正常提交路径报错），
     * 故全局事务内仅保留 INSERT 型分支；生产环境升级 Seata 修复版后可将扣减移回全局事务。
     */
    public void deductStock() {
        // 取首行走分页插件按方言翻译，不用 LIMIT 1（信创库兼容）
        List<DemoStock> stocks = stockMapper
                .selectPage(Page.of(1, 1, false), new LambdaQueryWrapper<DemoStock>())
                .getRecords();
        DemoStock stock = stocks.isEmpty() ? null : stocks.get(0);
        if (stock == null) {
            stock = new DemoStock();
            stock.setRemain(100);
            stockMapper.insert(stock);
        }
        if (stock.getRemain() <= 0 || stockMapper.deductById(stock.getId(), stock.getRemain() - 1) != 1) {
            throw new ServiceException(1300, "库存不足");
        }
    }

    /**
     * 创建订单：@TenantDatasource 路由（独立库租户 → 专属库），Seata AT 跨库分支。
     *
     * <p>注意：本方法不加 @Transactional——Spring 本地事务在方法进入时即绑定连接，
     * 会先于 TenantDsExecutorInterceptor 的数据源路由决策（连接固定在主库）；
     * Seata AT 的分支原子性由连接级 undo_log 保证，无需 Spring 事务包裹。
     */
    @GlobalTransactional(name = "mse-demo-place-order", rollbackFor = Exception.class)
    public Long placeOrder(String productName, BigDecimal amount, boolean failAfterWrite) {
        Long tenantId = TenantContext.getRequiredTenantId();
        DemoOrder order = new DemoOrder();
        order.setOrderNo("ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        order.setProductName(productName);
        order.setAmount(amount);
        orderMapper.insert(order);
        log.info("[mse-demo] 下单成功: tenantId={}, orderId={}, orderNo={}", tenantId, order.getId(), order.getOrderNo());

        // 模拟提交前失败 → 验证 Seata 全局回滚（undo_log 双库回放）
        if (failAfterWrite) {
            throw new ServiceException(1301, "模拟业务失败，验证 Seata 全局回滚");
        }
        return order.getId();
    }
}
