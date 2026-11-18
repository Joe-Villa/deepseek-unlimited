# DeepSeek Unlimited 0.11.2

> 中文名：DeepSeek 无限制版  
> 非官方客户端。身份说明只在「关于与免责声明」里。  
> 发布包：`dist/deepseek-unlimited-0.11.2.apk`  
> 版本：`0.11.2`（versionCode 40）  
> 最低 Android：12（API 31）  
> SHA-256：`32d565f93c9088b97895a935ac00d5e069ae5e5920779ee6ca338134cd9a97bf`

## 数据与备份

- `android:allowBackup="false"`：关闭 Android 系统备份，也关闭换机迁移。
- 移除自定义 backup agent、备份规则和启动时的恢复逻辑；App 不再参与系统备份/恢复。
- 换机只能手动：
  1. 旧设备：设置 → 本机数据 → 导出所有对话；
  2. 新设备：安装后 设置 → 本机数据 → 导入对话 ZIP。
- 导出的 ZIP 不含 API Key；新设备需要重新粘贴 Key。
- 对话、消息、设置和 API Key 只保存在本机；没有账号服务器，也没有我们运营的云端数据库。

## 名称

- 项目名：**DeepSeek Unlimited**；中文名：**DeepSeek 无限制版**。
- 应用名（launcher label）：
  - 英文/兜底：`DeepSeek Unlimited`；
  - 简体中文、繁體中文：`DeepSeek 无限制版`。
- 图标、设置页与「关于」保持原有非官方说明。

## 验证

- clean build + v2-only 重签后，重建 APK 与发布包字节级一致。
- `SOURCE-MANIFEST.sha256` 覆盖 483 个源码、构建配置和 keystore 文件。
- 未真机安装验证。
