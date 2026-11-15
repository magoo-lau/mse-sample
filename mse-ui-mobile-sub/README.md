# mse-ui-mobile-sub —— 移动子应用模板（Vant 4 版）

`mse-modules` 业务工程的 `<biz>-mobile-ui` 以本模板复制改造后接入**移动基站 mse-ui-mobile**（wujie 契约与 PC 模板完全同名）。dev :3210，构建 `--base=/m-apps/biz-demo/`。

内置：wujie 挂载契约（`__WUJIE_MOUNT/__WUJIE_UNMOUNT`）、auth store（props + `auth:change`）、主题桥（Vant 版 `--van-*` + `van-theme-dark`）、request（401 经 `sub:auth-expired` 交基站登出）、`v-permission` 权限指令。

接入步骤与授权口径：[docs/tech/frontend/mse-ui-mobile-sub.md](../../docs/tech/frontend/mse-ui-mobile-sub.md)
