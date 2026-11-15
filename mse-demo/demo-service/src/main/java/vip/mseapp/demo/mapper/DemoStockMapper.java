package vip.mseapp.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import vip.mseapp.demo.entity.DemoStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DemoStockMapper extends BaseMapper<DemoStock> {

    /**
     * 按主键回填库存值。
     *
     * <p>纯主键 WHERE + 跳过租户插件改写：MP 追加的 tenant_id/deleted 条件会令
     * Seata AT 无法从 UPDATE 语句解析主键（undo_log needs to contain the primary key）；
     * 租户安全由调用方“租户上下文内先查行、再按主键扣减”保证。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Update("UPDATE demo_stock SET remain = #{remain} WHERE id = #{id}")
    int deductById(@Param("id") Long id, @Param("remain") Integer remain);
}
