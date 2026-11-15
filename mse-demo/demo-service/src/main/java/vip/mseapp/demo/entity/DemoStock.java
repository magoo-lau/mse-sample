package vip.mseapp.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import vip.mseapp.mybatis.data.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 示例库存（共享主库，租户隔离表）：每租户一行配额。
 * 与 {@link DemoOrder}（@TenantDatasource 独立库路由）构成跨数据源事务演示。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("demo_stock")
public class DemoStock extends BaseEntity {

    /** 剩余可下单数量 */
    private Integer remain;
}
