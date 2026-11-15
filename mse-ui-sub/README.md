# mse-ui-sub —— 微前端子应用模板

基于 wujie（无界）的子应用模板：既可嵌入 `mse-ui` 基座运行（登录态由基座下发），也可独立启动开发调试。技术栈与基座一致（Vue3 + Vite + TS + Element Plus）。子应用**不自带侧边栏菜单**——应用地址在基座「子应用管理」登记、页面菜单在基座「菜单管理」以类型=子应用页面登记（所属子应用关联本应用标识），由基座菜单树渲染；子应用只负责内容页。

## 模板演示的接入要点

| 要点 | 位置 |
|---|---|
| wujie 生命周期（mount/unmount，独立运行直接挂载） | `src/main.ts` |
| 路由 base 适配（嵌入用基座下发的 urlPrefix，独立用 `/`） | `src/router/index.ts` |
| 登录态获取（props 下发 + `auth:change` 事件同步） | `src/stores/auth.ts` |
| 请求封装（token 注入、401 通过 bus 通知基座登出） | `src/api/request.ts` |
| dev server CORS（wujie 跨域加载子应用资源） | `vite.config.ts` |
| 避免 `100vh`（wujie 隐藏 iframe 中 vh 不可靠） | `src/style.css` |

## 独立运行

```bash
npm install && npm run dev   # http://localhost:3100
```

## 复制改造为新业务子应用

1. **复制目录**：`cp -r mse-ui-sub mse-ui-xxx`，改 `package.json` 的 `name`；
2. **改端口**：`vite.config.ts` 中 `server.port`（每个子应用唯一）；
3. **改路由前缀**：`src/router/index.ts` 中独立运行 fallback 前缀 `/sub/biz-demo` 改为 `/sub/xxx`（仅兜底用，实际以基座 props 为准）；
4. **改构建 basePath**：`package.json` 的 `build` 脚本 `--base=/sub-apps/biz-demo/` 改为实际部署路径；
5. **基座注册**（三选一，业务 SQL 优先）：随所在业务工程的 jar 内 `db/<方言>/V…__<biz>_base.sql` 版本化脚本（头指令 `-- mse-target: base`）幂等登记（应用地址 + 页面菜单 + 权限点，随 starter-bridge 自动推送 mse-system 应用，免手工执行，参照 mse-oa）；或平台端「平台管理 → 子应用管理」后台新增应用地址 + 「菜单管理」新增类型=子应用页面的菜单（所属子应用选本应用，路由地址填子应用内部路由）；本地零配置联调也可在基座 `mse-framework/mse-ui/src/micro/apps.ts` 兜底表临时追加：

```ts
{
  name: 'xxx',              // 路由前缀 /sub/xxx
  title: '业务菜单名',
  devEntry: '//localhost:31xx',
  prodEntry: '/sub-apps/xxx/',
  side: 'both'              // tenant 租户端 / platform 平台端 / both 双端（后台配置为 1/2/3）
}
// 页面菜单在基座「菜单管理」登记（类型=子应用页面）：
//   菜单名「页面一」 所属子应用 xxx 路由地址 /page-one
//   菜单名「详情页」 所属子应用 xxx 路由地址 /detail/:id（动态参数段用 :id，基座默认取 1）
```

6. **替换演示页面**：删除 `views/PageA.vue`、`views/PageB.vue`，业务页面自建。
7. **标准分页**：列表页分页统一用 `components/Pagination.vue`（`<Pagination :total="total" v-model:page="query.pageNum" v-model:size="query.pageSize" @change="load" />`，共 X 条 / 条每页 / 前后翻页 / 页码 / 前往跳页、右对齐，切条数自动回第 1 页），与各业务子应用及基座 `mse-ui` 同款；不要手写 `el-pagination`。

## 与基座的通信

- **props（基座 → 子应用，初始值）**：`window.$wujie.props.{ urlPrefix, token, tenantId, platform, nickname, permissions }`（permissions 用于按钮级 v-permission，参考 mse-sample/mse-demo/demo-ui/src/directives/permission.ts）
- **bus 事件（双向）**：
  - `auth:change`：基座登录/登出后广播，子应用 `stores/auth.ts` 已订阅；
  - `sub:navigate`：基座菜单点击/直达 URL 时下发目标路径（`{ urlPrefix, path }`），子应用 `App.vue` 已订阅并 `router.push`；
  - `sub:ready`：子应用挂载完成后上报，基座据此补发一次当前目标路由（覆盖直达/刷新场景）；
  - `sub:title`：路由切换后上报 `{urlPrefix, path, title}`（路由 `meta.title`），基座用于页签与面包屑的中文展示（深链/详情页无页面菜单可归并时的标题来源）；
  - `sub:auth-expired`：子应用收到 401 后上报，基座统一登出；
  - 自定义事件按需扩展：`window.$wujie.bus.$emit/$on('xxx:yyy', payload)`。
