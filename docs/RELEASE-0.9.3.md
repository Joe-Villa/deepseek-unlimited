# DeepSeek Unlimited 0.9.3

> 中文名：DeepSeek 无限制版  
> 非官方客户端。不是 DeepSeek 官方产品。  
> 发布包：`dist/deepseek-unlimited-0.9.3.apk`  
> 版本：`0.9.3`（versionCode 27）  
> 最低 Android：12（API 31）  
> 签名：当前沿用 debug keystore，方便直接侧载；正式分发请换正式签名。  
> SHA-256：`9a957f76873a423e4f5c02c951663747f7f4ba148bcf48428b04485c9ff75c0f`

## 这个版本做了什么

### 1. 深色、极简的界面

- 全局深色画布（`#191A1D`）、卡片（`#232428`）、钴蓝（`#4D6BFE`）与圆角统一。
- 设置页只剩三组：
  - **连接**：API Key（查看 / 复制 / 更换）
  - **本机数据**：对话数量 / 清空所有对话
  - **关于**：版本 / 非官方说明 / GPL-3.0
- 移除了可见的账号、退出、语言、外观、字体、语音、检查更新、帮助反馈等入口。

### 2. 联网搜索使用 DeepSeek 官方服务端工具

请求中声明：

```json
"tools": [
  {"type": "deepseek_web_search"},
  {"type": "deepseek_read_url"}
]
```

- DeepSeek 配置固定走 Chat Completions（`/chat/completions`），而不是 Responses；服务端工具与 `reasoning_content` 的流式事件都在这条路径上。
- 搜索与网页抓取由 DeepSeek 服务端完成，不需要第二个搜索 Key。
- 模型自行判断是否搜索；支持普通联网搜索与深度思考搜索。
- 回答上方显示搜索次数、搜索词与来源；来源可展开、可点击。
- 搜索、抓取、深度思考均按 DeepSeek 官方用量计费，费用从用户自己的 API 余额扣除。

### 3. 思考过程实时滚动

- 修复了 `deepseek-flash` 返回 `delta.reasoning_content` 时客户端只认 `delta.reasoning` 的问题。
- 思考面板在生成阶段自动展开，内部滚动到最新一行；正文开始输出后自动折叠。
- Chat 列表在流式内容增长时显式滚动到底部锚点，长回答/长思考不会停在半截。
- 请求显式开启：

```json
"thinking": {"type": "enabled"},
"reasoning_effort": "high"
```

### 4. 多轮工具上下文

- 历史 assistant 消息回传 `reasoning_content` 与 `server_tool_calls`，让 DeepSeek 能还原上一轮的阅读链接与工具上下文。
- 服务端工具事件与最终调用快照保存在本机消息 timeline 中，不需要数据库结构迁移。

### 5. 身份与边界

- 首页、设置页、免责声明均标注“非官方”。
- 免责声明说明：不是官方 App、数据在本机、费用由官方收取、“无限制”仅指 App 侧无次数/修改/保留限制。
- 不复制官方备案号、ICP、版本号或服务协议。

## 安装

```bash
# 通过 adb
adb install -r dist/deepseek-unlimited-0.9.3.apk
```

或者把 APK 传到手机，允许“安装未知来源应用”后点击安装。

首次打开：粘贴 DeepSeek API Key → 开始使用。App 内不需要注册或登录；获取 Key 需要在 DeepSeek 开放平台完成。

## 已知限制

- 当前没有在真机/模拟器上跑完整流程（本机没有 KVM）。已通过：
  - `:app:compileDebugKotlin`
  - `:app:compileDebugUnitTestKotlin`
  - `DeepSeekWireFormatTest`（请求/响应格式）
  - `DeepSeekLookTest`（深色首页/聊天/思考/搜索组件截图）
- Release 沿用 debug 签名。
- 仍保留上游的多平台/本地模型依赖，APK 约 59MB；后续可继续裁剪。
- 联网搜索、深度思考、阅读链接均可能产生费用；请留意 DeepSeek 账户余额。
- 不同地区的 DeepSeek API 可用性与定价以官方为准。

## 构建记录

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export GRADLE_USER_HOME="$PWD/vendor/.gradle"
./vendor/gradle-9.4.1/bin/gradle :app:assembleDebug --offline --no-daemon
./vendor/gradle-9.4.1/bin/gradle :app:assembleRelease --offline --no-daemon
```

测试：

```bash
./vendor/gradle-9.4.1/bin/gradle :app:testDebugUnitTest \
  --tests '*DeepSeekWireFormatTest*' \
  --tests '*DeepSeekLookTest*' \
  --offline --no-daemon -Pjava21Home=/usr/lib/jvm/java-21-openjdk-amd64
```

## 引用

- DeepSeek API 全家桶公告：https://api-docs.deepseek.com/zh-cn/news/news261113/
- 联网工具指南：https://api-docs.deepseek.com/zh-cn/guides/web_tools
- 思考模式：https://api-docs.deepseek.com/zh-cn/guides/thinking_mode
- Chat Completions API：https://api-docs.deepseek.com/zh-cn/api/create-chat-completion
