-- mse-target: master
-- =====================================================================
-- 演示中心初始化脚本（独立工程 mse-demo，MySQL）
-- 目标库：业务库（业务表所在库；master 默认库 mse_platform，首启自动建库；分库部署对业务库执行）
-- 内容：业务示例表（demo_order/demo_stock/demo_event）+ Seata undo_log
-- 配套：demo_base.sql（对基座库执行：子应用注册 + 页面菜单 + 权限点）
--
-- 前置：无（基座库随基座应用启动自动初始化；demo_base 随 jar 自动推送注册，无需手工执行）
-- 执行：自动——随服务启动应用主库（mse.tenant.schema.auto-migrate 默认开启）；租户独立库经「租户数据源初始化/懒初始化」克隆+迁移自动就绪
-- 版本化：V{yyyyMMdd}_{NN}_<biz>__<描述>.sql（biz 段路由分册账本 mse_<biz>_schema_history，同 biz 内字典序即时间序）；
--           已应用脚本不再修改（改动报 checksum 冲突），变更一律新增版本文件
--
-- 混合模式验证：租户1(demo)订单落主库 demo_order；独立库租户（ds_key=tenant2）的专属库由「租户数据源初始化/懒初始化」
-- =====================================================================

CREATE TABLE IF NOT EXISTS demo_order
(
    id          BIGINT        NOT NULL COMMENT '主键ID',
    tenant_id   BIGINT        NOT NULL DEFAULT 0 COMMENT '租户编号',
    order_no    VARCHAR(40)   NOT NULL COMMENT '订单号',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    amount      DECIMAL(12, 2) NULL COMMENT '订单金额',
    remark      VARCHAR(255) NULL COMMENT '备注',
    create_by   BIGINT        NULL COMMENT '创建者',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT        NULL COMMENT '更新者',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除（0 未删除 1 已删除）',
    PRIMARY KEY (id),
    KEY idx_tenant_order_no (tenant_id, order_no)
) ENGINE = InnoDB COMMENT ='示例订单（@TenantDatasource 混合路由）';

CREATE TABLE IF NOT EXISTS demo_stock
(
    id          BIGINT   NOT NULL COMMENT '主键ID',
    tenant_id   BIGINT   NOT NULL DEFAULT 0 COMMENT '租户编号',
    remain      INT      NOT NULL DEFAULT 0 COMMENT '剩余可下单数量',
    create_by   BIGINT   NULL COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT   NULL COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除（0 未删除 1 已删除）',
    PRIMARY KEY (id),
    KEY idx_tenant (tenant_id)
) ENGINE = InnoDB COMMENT ='示例库存（共享主库，跨库事务的另一端）';

CREATE TABLE IF NOT EXISTS demo_event
(
    id          BIGINT       NOT NULL COMMENT '主键ID',
    tenant_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    event_type  VARCHAR(60)  NOT NULL COMMENT '事件类型',
    payload     VARCHAR(1024) NULL COMMENT '事件负载（JSON）',
    create_by   BIGINT       NULL COMMENT '创建者',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by   BIGINT       NULL COMMENT '更新者',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0 未删除 1 已删除）',
    PRIMARY KEY (id),
    KEY idx_tenant (tenant_id)
) ENGINE = InnoDB COMMENT ='已消费租户事件（共享主库）';

