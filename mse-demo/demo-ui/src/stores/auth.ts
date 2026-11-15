import { createAuthStore } from 'mse-ui-shared'

/** 主应用下发登录态（工厂语义见 mse-ui-shared src/auth/store.ts），导出名保持 useAuthStore 不动调用方 */
export const useAuthStore = createAuthStore()
