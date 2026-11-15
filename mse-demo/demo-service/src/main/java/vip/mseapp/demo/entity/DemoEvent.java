package vip.mseapp.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import vip.mseapp.mybatis.data.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 已消费的租户事件（共享主库）：证明异步链路租户上下文恢复正确。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("demo_event")
public class DemoEvent extends BaseEntity {

    private String eventType;

    private String payload;
}
