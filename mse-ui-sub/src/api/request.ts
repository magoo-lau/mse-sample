import { createRequest } from 'mse-ui-shared'
import { elementPlusToast } from 'mse-ui-shared/pc'
import { useAuthStore } from '../stores/auth'

/** 请求实例收敛至 mse-ui-shared 工厂（code===0 解包、401 嵌入模式上报主应用登出、skipErrorToast 单请求静默） */
const request = createRequest({ getToken: () => useAuthStore().token, toast: elementPlusToast })

export default request
