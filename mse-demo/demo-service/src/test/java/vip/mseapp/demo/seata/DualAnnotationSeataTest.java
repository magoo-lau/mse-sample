package vip.mseapp.demo.seata;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import javax.sql.DataSource;

import org.apache.seata.core.context.RootContext;
import org.apache.seata.spring.annotation.AdapterSpringSeataInterceptor;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.apache.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.apache.seata.spring.boot.autoconfigure.SeataCoreAutoConfiguration;
import org.apache.seata.spring.boot.autoconfigure.loader.SeataPropertiesLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

/**
 * 回归测试：同一方法叠加 {@code @GlobalTransactional + @Transactional}（双注解）在
 * 真实 MySQL + Seata TC 下的代理结构与 AT 语义。
 * 前置：docker compose 中 mse-mysql(3306) 与 mse-seata(8091) 已启动，`mse` 库含 undo_log 表。
 *
 * <p>被验证的两个关键事实：
 * <ol>
 * <li><b>代理顺序正确</b>：双注解 bean 是单层代理，Seata 拦截器在最外层（index 0），
 *     Spring 事务拦截器在内层——即"全局事务先开、本地事务后开、先本地提交后全局提交"。
 *     这是 AT 分支正确注册的前提（分支注册发生在本地物理 commit 时）。</li>
 * <li><b>AT 语义完整</b>：内层 {@code @Transactional} 已物理提交的行，能被外层全局回滚
 *     通过 undo_log 回放消除——Spring 本地事务做不到这一点，只有分支正确加入全局事务才会发生。</li>
 * </ol>
 *
 * <p>注意：Seata 扫描器的 PROXYED_SET / NEED_ENHANCE_BEAN_NAME_SET 是 JVM 级静态集合（按 bean 名记录），
 * 因此本套件必须共享同一个 Spring 上下文（与真实应用单上下文一致）；若在同一 JVM 内创建多个上下文，
 * 后续上下文中的同名 bean 会被跳过增强，产生与真实行为不符的假象。
 *
 * <p>数据源说明：仓库真实链路用 dynamic-datasource 的 {@code seata: true} 逐源代理，
 * 本测试为聚焦注解语义直接以 {@code DataSourceProxy} 包装 Hikari——两者最终交给业务连接的
 * 都是同一个 {@code org.apache.seata.rm.datasource.DataSourceProxy}。
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// 真环境集成测试：依赖本机 127.0.0.1 的 MySQL(3306) + Seata TC(8091)，任何 CI/无中间件环境缺省不跑；
// 本地开启：先起 docker compose（mse-mysql + mse-seata），再 MSE_SEATA_IT=true mvn test（详见 docs/tech/ci.md §3）
@EnabledIfEnvironmentVariable(named = "MSE_SEATA_IT", matches = "true")
class DualAnnotationSeataTest {

    private static final String TABLE = "l2_dual_tx";

    private AnnotationConfigApplicationContext context;
    private JdbcTemplate jdbc;

    @BeforeAll
    void bootOnce() {
        StandardEnvironment env = new StandardEnvironment();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("seata.enabled", "true");
        props.put("seata.application-id", "l2-at-semantics");
        props.put("seata.tx-service-group", "mse-tx-group");
        props.put("seata.service.vgroup-mapping.mse-tx-group", "default");
        props.put("seata.service.grouplist.default", "127.0.0.1:8091");
        props.put("seata.config.type", "file");
        props.put("seata.config.file.name", "seata-l1.conf");
        props.put("seata.enable-auto-data-source-proxy", "false");
        env.getPropertySources().addFirst(new MapPropertySource("l2-props", props));

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.setEnvironment(env);
        ctx.register(L2Config.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                JdbcTemplateAutoConfiguration.class,
                TransactionAutoConfiguration.class,
                SeataCoreAutoConfiguration.class,
                SeataAutoConfiguration.class);
        // 与 SpringApplication 行为对齐：刷新前应用 Seata 的 ApplicationContextInitializer
        new SeataPropertiesLoader().initialize(ctx);
        ctx.refresh();
        context = ctx;

        jdbc = new JdbcTemplate(context.getBean(DataSource.class));
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + TABLE
                + " (id BIGINT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(64))");
        jdbc.update("DELETE FROM " + TABLE);
    }

    @AfterAll
    void shutdown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @Order(1)
    void dualAnnotatedBeanIsSingleProxyWithSeataOutsideTransaction() throws Exception {
        L2StackedService svc = context.getBean(L2StackedService.class);
        assertThat(AopUtils.isAopProxy(svc)).as("双注解 bean 应被代理").isTrue();

        Advised advised = (Advised) svc;
        var advisors = advised.getAdvisors();
        int seataIdx = -1;
        int txIdx = -1;
        for (int i = 0; i < advisors.length; i++) {
            Class<?> adviceClass = advisors[i].getAdvice().getClass();
            if (adviceClass == AdapterSpringSeataInterceptor.class) {
                seataIdx = i;
            } else if (adviceClass == org.springframework.transaction.interceptor.TransactionInterceptor.class) {
                txIdx = i;
            }
        }
        assertThat(seataIdx).as("应存在 Seata 拦截器").isGreaterThanOrEqualTo(0);
        assertThat(txIdx).as("应存在 Spring 事务拦截器").isGreaterThanOrEqualTo(0);
        assertThat(seataIdx).as("Seata 拦截器必须在外层（先于事务拦截器）").isLessThan(txIdx);
        assertThat(advised.getTargetSource().getTarget())
                .as("应为单层代理（Seata 2.6 会把 advisor 插入既有代理链，而非再包一层代理）")
                .isNotInstanceOf(Advised.class);
    }

    @Test
    @Order(2)
    void stackedMethodRunsInsideGlobalTransaction() {
        L2StackedService svc = context.getBean(L2StackedService.class);
        String xid = svc.writeAndOk("stacked-ok");
        assertThat(xid).as("双注解方法内应已绑定 XID（全局事务先于本地事务开启）").isNotBlank();
        await(() -> countOf("stacked-ok") == 1, "提交路径：行应存在");
    }

    @Test
    @Order(3)
    void stackedMethodRollsBackOnException() {
        L2StackedService svc = context.getBean(L2StackedService.class);
        assertThatThrownBy(() -> svc.writeAndFail("stacked-fail")).isInstanceOf(IllegalStateException.class);
        await(() -> countOf("stacked-fail") == 0, "回滚路径：行不应存在");
    }

    @Test
    @Order(4)
    void innerCommittedRowIsReplayedByGlobalRollback() {
        L2EntryService svc = context.getBean(L2EntryService.class);
        // 内层 @Transactional 已物理提交并注册 AT 分支，随后全局回滚 → undo_log 回放，行必须消失
        assertThatThrownBy(() -> svc.entryThenFail("entry-fail")).isInstanceOf(IllegalStateException.class);
        await(() -> countOf("entry-fail") == 0, "全局回滚应回放已提交的内层分支（AT 语义）");
    }

    @Test
    @Order(5)
    void innerCommittedRowStaysOnGlobalCommit() {
        L2EntryService svc = context.getBean(L2EntryService.class);
        svc.entryOk("entry-ok");
        await(() -> countOf("entry-ok") == 1, "全局提交：行应存在");
    }

    /**
     * 单体形态（seata.enabled=false，无 GlobalTransactionScanner）：双注解退化为纯 Spring 本地事务，
     * 且同 JVM 内后续上下文不会被 Seata 静态集合干扰（此上下文无扫描器，注解整体失效）。
     */
    @Test
    @Order(6)
    void seataDisabledDegeneratesToLocalTransaction() {
        StandardEnvironment env = new StandardEnvironment();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("seata.enabled", "false");
        props.put("spring.datasource.url", "jdbc:mysql://127.0.0.1:3306/mse?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
        props.put("spring.datasource.driver-class-name", "com.mysql.cj.jdbc.Driver");
        props.put("spring.datasource.username", "root");
        props.put("spring.datasource.password", "123456");
        env.getPropertySources().addFirst(new MapPropertySource("l2-props-off", props));

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.setEnvironment(env);
        ctx.register(L2Config.class,
                DataSourceAutoConfiguration.class,
                DataSourceTransactionManagerAutoConfiguration.class,
                JdbcTemplateAutoConfiguration.class,
                TransactionAutoConfiguration.class);
        new SeataPropertiesLoader().initialize(ctx);
        ctx.refresh();
        try {
            L2StackedService svc = ctx.getBean(L2StackedService.class);
            svc.writeAndOk("seata-off-ok");
            assertThat(countOf(ctx, "seata-off-ok")).as("Seata 关闭：本地事务提交生效").isEqualTo(1);
            assertThatThrownBy(() -> svc.writeAndFail("seata-off-fail")).isInstanceOf(IllegalStateException.class);
            await(() -> countOf(ctx, "seata-off-fail") == 0, "Seata 关闭：本地事务回滚生效");
        } finally {
            ctx.close();
        }
    }

    private int countOf(AnnotationConfigApplicationContext ctx, String name) {
        Integer n = new JdbcTemplate(ctx.getBean(DataSource.class))
                .queryForObject("SELECT COUNT(*) FROM " + TABLE + " WHERE name = ?", Integer.class, name);
        return n == null ? 0 : n;
    }

    private int countOf(String name) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + TABLE + " WHERE name = ?", Integer.class, name);
        return n == null ? 0 : n;
    }

    private static void await(BooleanSupplier condition, String message) {
        Duration timeout = Duration.ofSeconds(10);
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        fail(message + "（等待超时 " + timeout.getSeconds() + "s）");
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan("vip.mseapp.demo.seata")
    static class L2Config {

        /**
         * 直接产出 Seata DataSourceProxy（与 dynamic-datasource {@code seata: true} 逐源代理产出的是同一类实例），
         * 使本测试聚焦双注解与 AT 分支的交互语义。
         */
        @Bean
        DataSource dataSource() {
            com.zaxxer.hikari.HikariDataSource hikari = new com.zaxxer.hikari.HikariDataSource();
            hikari.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/mse?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
            hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikari.setUsername("root");
            hikari.setPassword("123456");
            hikari.setMaximumPoolSize(4);
            return new org.apache.seata.rm.datasource.DataSourceProxy(hikari);
        }
    }
}

@Service
class L2StackedService {

    @Autowired
    JdbcTemplate jdbc;

    /** 双注解同方法：返回方法内绑定的 XID（验证全局事务先于本地事务开启）。 */
    @GlobalTransactional(name = "l2-stacked-ok", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public String writeAndOk(String name) {
        String xid = RootContext.getXID();
        jdbc.update("INSERT INTO l2_dual_tx (name) VALUES (?)", name);
        return xid;
    }

    @GlobalTransactional(name = "l2-stacked-fail", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public void writeAndFail(String name) {
        jdbc.update("INSERT INTO l2_dual_tx (name) VALUES (?)", name);
        throw new IllegalStateException("boom-after-insert");
    }
}

@Service
class L2InnerWriter {

    @Autowired
    JdbcTemplate jdbc;

    @Transactional(rollbackFor = Exception.class)
    public void insertRow(String name) {
        jdbc.update("INSERT INTO l2_dual_tx (name) VALUES (?)", name);
    }
}

@Service
class L2EntryService {

    @Autowired
    L2InnerWriter innerWriter;

    @GlobalTransactional(name = "l2-entry-fail", rollbackFor = Exception.class)
    public void entryThenFail(String name) {
        innerWriter.insertRow(name);
        throw new IllegalStateException("boom-after-inner-commit");
    }

    @GlobalTransactional(name = "l2-entry-ok", rollbackFor = Exception.class)
    public void entryOk(String name) {
        innerWriter.insertRow(name);
    }
}
