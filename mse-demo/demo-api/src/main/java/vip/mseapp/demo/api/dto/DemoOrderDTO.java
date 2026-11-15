package vip.mseapp.demo.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 示例订单信息（业务 RPC 契约）。
 */
public class DemoOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private String productName;
    private BigDecimal amount;
    private String remark;
    /** 租户编号（订单归属租户；0 表示平台级） */
    private Long tenantId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
