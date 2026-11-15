import { createPermissionDirective } from 'mse-ui-shared'
import { useAuthStore } from '../stores/auth'

/** 按钮级权限指令（核心实现收敛于 mse-ui-shared）：v-permission="'权限码'"（数组 = 任一命中），无权限移除元素 */
export const permission = createPermissionDirective((code) => useAuthStore().hasPermission(code))
