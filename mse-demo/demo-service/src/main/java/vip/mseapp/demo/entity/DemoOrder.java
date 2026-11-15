package vip.mseapp.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import vip.mseapp.core.annotation.TenantDatasource;
import vip.mseapp.mybatis.data.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 示例订单（业务表）。
 *
 * <p>标注 {@link TenantDatasource}：租户配置了独立库（sys_tenant.ds_key 非空）时，
 * 本表读写自动路由到租户专属数据源；否则留在共享主库。
 * 表结构在两种库中一致，均含 tenant_id 列。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TenantDatasource
@TableName("demo_order")
public class DemoOrder extends BaseEntity {

    /** 订单号 */
    private String orderNo;

    /** 商品名 */
    private String productName;

    /** 金额 */
    private BigDecimal amount;

    /** 备注 */
    private String remark;
}
