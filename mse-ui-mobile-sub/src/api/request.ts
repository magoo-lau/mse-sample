import { createRequest } from 'mse-ui-shared'
import { vantToast } from 'mse-ui-shared/mobile'
import { useAuthStore } from '../stores/auth'

/** 请求实例收敛至 mse-ui-shared 工厂（code===0 解包、401 嵌入模式上报主应用登出、skipErrorToast 单请求静默） */
const request = createRequest({
  getToken: () => useAuthStore().token,
  toast: vantToast,
  on401Local: () => vantToast.warning('会话已过期，请回到移动工作台重新登录')
})

export default request
