package vip.mseapp.demo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import vip.mseapp.core.constant.MseConstants;
import vip.mseapp.core.result.PageResult;
import vip.mseapp.core.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import vip.mseapp.mq.TenantEventSender;
import vip.mseapp.demo.entity.DemoEvent;
import vip.mseapp.demo.entity.DemoOrder;
import vip.mseapp.demo.entity.DemoStock;
import vip.mseapp.demo.mapper.DemoEventMapper;
import vip.mseapp.demo.mapper.DemoOrderMapper;
import vip.mseapp.demo.mapper.DemoStockMapper;
import vip.mseapp.demo.service.DemoOrderService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 示例订单（租户端）。
 *
 * <p>验证点：
 * <ul>
 *   <li>/demo/order/page|POST：混合租户路由（共享库/独立库）；</li>
 *   <li>/demo/order/place：Seata AT 跨数据源全局事务（主库库存 + 租户库订单）；</li>
 *   <li>/demo/order/events：RocketMQ 异步链路租户上下文恢复。</li>
 * </ul>
 */
@Validated
@RestController
@RequestMapping("/demo/order")
public class DemoOrderController {

    private final DemoOrderMapper orderMapper;
    private final DemoStockMapper stockMapper;
    private final DemoEventMapper eventMapper;
    private final DemoOrderService orderService;
    private final TenantEventSender eventSender;

    public DemoOrderController(DemoOrderMapper orderMapper, DemoStockMapper stockMapper,
                               DemoEventMapper eventMapper, DemoOrderService orderService,
                               TenantEventSender eventSender) {
        this.orderMapper = orderMapper;
        this.stockMapper = stockMapper;
        this.eventMapper = eventMapper;
        this.orderService = orderService;
        this.eventSender = eventSender;
    }

    @SaCheckPermission(value = "demo:order:create", type = MseConstants.LOGIN_TYPE_TENANT)
    @PostMapping("/place")
    public Result<Long> place(@RequestBody @Validated PlaceRequest request) {
        orderService.deductStock();
        Long orderId = orderService.placeOrder(request.productName(), request.amount(), request.fail());
        // 事务提交后发送租户事件（生产环境如需“事务+消息”原子性应使用 RocketMQ 事务消息）
        eventSender.send("mse-demo-order-event", "ORDER_PLACED",
                "{\"orderId\":" + orderId + ",\"productName\":\"" + request.productName() + "\"}");
        return Result.ok(orderId);
    }

    @SaCheckPermission(value = "demo:order:list", type = MseConstants.LOGIN_TYPE_TENANT)
    @GetMapping("/stock")
    public Result<DemoStock> stock() {
        // 取首行走分页插件按方言翻译（不用 LIMIT 1，达梦/金仓等信创库不保证语法兼容）
        List<DemoStock> records = stockMapper
                .selectPage(Page.of(1, 1, false), new LambdaQueryWrapper<DemoStock>())
                .getRecords();
        return Result.ok(records.isEmpty() ? null : records.get(0));
    }

    @SaCheckPermission(value = "demo:order:list", type = MseConstants.LOGIN_TYPE_TENANT)
    @GetMapping("/events")
    public Result<List<DemoEvent>> events() {
        return Result.ok(eventMapper.selectPage(Page.of(1, 20, false),
                new LambdaQueryWrapper<DemoEvent>().orderByDesc(DemoEvent::getId)).getRecords());
    }

    @SaCheckPermission(value = "demo:order:create", type = MseConstants.LOGIN_TYPE_TENANT)
    @PostMapping
    public Result<Long> create(@RequestBody @Validated OrderCreateRequest request) {
        DemoOrder order = new DemoOrder();
        order.setOrderNo("ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        order.setProductName(request.productName());
        order.setAmount(request.amount());
        order.setRemark(request.remark());
        orderMapper.insert(order);
        return Result.ok(order.getId());
    }

    @SaCheckPermission(value = "demo:order:list", type = MseConstants.LOGIN_TYPE_TENANT)
    @GetMapping("/page")
    public Result<PageResult<DemoOrder>> page(@RequestParam(defaultValue = "1") long pageNum,
                                              @RequestParam(defaultValue = "10") long pageSize) {
        Page<DemoOrder> page = orderMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<DemoOrder>().orderByDesc(DemoOrder::getId));
        return Result.ok(PageResult.of(page.getTotal(), pageNum, pageSize, page.getRecords()));
    }

    @SaCheckPermission(value = "demo:order:list", type = MseConstants.LOGIN_TYPE_TENANT)
    @GetMapping("/{id}")
    public Result<DemoOrder> get(@PathVariable Long id) {
        return Result.ok(orderMapper.selectById(id));
    }

    public record OrderCreateRequest(
            @NotBlank(message = "商品名不能为空") String productName,
            BigDecimal amount,
            String remark) {
    }

    public record PlaceRequest(
            @NotBlank(message = "商品名不能为空") String productName,
            BigDecimal amount,
            /** true 时写入后抛错，验证 Seata 跨库全局回滚 */
            boolean fail) {
    }
}
