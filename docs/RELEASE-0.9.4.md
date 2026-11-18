# DeepSeek Unlimited 0.9.4

> 中文名：DeepSeek 无限制版  
> 非官方客户端。与 DeepSeek 官方无关；身份说明只放在「关于与免责声明」里。  
> 发布包：`dist/deepseek-unlimited-0.9.4.apk`  
> 版本：`0.9.4`（versionCode 28）  
> 最低 Android：12（API 31）  
> 签名：发布包目前沿用 debug keystore，方便直接侧载；正式分发请换正式签名。  
> SHA-256：`8a52c268ae10437933e9c5c71dfe81e6408884d23e719dee5988068a6a887f84`

## 这次的重点：把「像官方」做进操作细节

### 1. 官方同款鲸鱼图标

- 使用 DeepSeek 官方鲸鱼 Logo 的轮廓路径（来源：官方 favicon 的鲸鱼 path），补上白色腹部与眼白，不再使用自绘近似图形。
- 启动页、自适应图标、首页、首次配置统一为同一套鲸鱼。
- 图标为深色背景上的蓝色鲸鱼 + 白色腹部/眼白，尽量贴近官方 App 的观感。

### 2. 主界面不再反复强调「非官方」

- 首页标题改为「DeepSeek」，不再在主界面挂「非官方」徽标。
- 首次配置页标题改为「DeepSeek」，去掉「非官方客户端」副标。
- 对话底部只保留「内容由 AI 生成，请仔细甄别」。
- 身份说明只放在 **设置 → 关于与免责声明** 里，包括第三方关系、数据、费用、GPL-3.0。

### 3. 官方结构的首页 + 历史抽屉

- 首页：菜单、DeepSeek 标题、新对话按钮、鲸鱼问候；不再把历史列表直接铺在首页。
- 左侧抽屉：搜索对话、今天/昨天/7 天内/更早分组、当前对话高亮。
- 抽屉底部：设置入口 + 本地对话数量，不再出现账号头像/昵称。
- 打开/关闭抽屉使用 Material 的侧滑与遮罩动画，支持返回键关闭。

### 4. 对话页操作对齐

- 去掉助手消息前的绿色 GPT Mobile 头像/平台按钮，改为官方式的纯文本回答。
- 顶部右侧：朗读回答（Android TTS）、新对话（+）。
- 消息操作：复制、赞、踩、分享、重新生成；赞/踩为本地 UI 状态，分享使用系统分享面板。
- 输入框：左侧「+」菜单（拍照 / 相册 / 文件）、语音输入（系统语音识别）、深度思考、联网搜索、发送/停止。
- 「深度思考」「联网搜索」现在是**可点击开关**，会影响下一次请求：
  - 深度思考关：请求 `thinking: {"type":"disabled"}`，不发推理强度；
  - 联网搜索关：不再声明 `deepseek_web_search` / `deepseek_read_url`。
- 对话底部增加「内容由 AI 生成，请仔细甄别」。

### 5. 思考与联网的细节

- 思考过程实时滚动；正文开始后自动折叠。
- 搜索状态、搜索词、来源可展开。
- 多轮回传 `reasoning_content` 与 `server_tool_calls`，尽量保持官方工具上下文。

## 已知限制

- 未在真机/模拟器上做端到端联网测试（本机无 KVM，也没有真实 API Key）；已通过编译、全量单元测试、Robolectric 截图测试。
- 语音输入依赖手机自带的语音识别服务；朗读依赖系统 TTS，音色与官方 App 不同。
- 赞/踩目前只做本地视觉反馈，没有服务端统计。
- 拍照/相册/文件走现有附件通道；DeepSeek 文本模型对图片/文件的实际支持以官方 API 为准。
- 对话页左上角已改为双线菜单；点击会回到首页并自动展开历史抽屉（不是在对话页上覆盖抽屉），与官方“抽屉盖在对话上”的层级还有一点差别。
- 发布包仍包含上游多平台/本地模型依赖，体积约 59MB；后续可继续裁剪。

## 构建

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export GRADLE_USER_HOME="$PWD/vendor/.gradle"
./vendor/gradle-9.4.1/bin/gradle :app:assembleRelease --offline --no-daemon
```

测试：

```bash
./vendor/gradle-9.4.1/bin/gradle :app:testDebugUnitTest \
  --offline --no-daemon -Pjava21Home=/usr/lib/jvm/java-21-openjdk-amd64
```

## 引用

- 鲸鱼 Logo 路径来源（官方 favicon 轮廓）：https://raw.githubusercontent.com/Nixz0824/dsh-usage-stats-plus/master/lib/client.js
- DeepSeek API 联网工具：https://api-docs.deepseek.com/zh-cn/guides/web_tools
- DeepSeek 思考模式：https://api-docs.deepseek.com/zh-cn/guides/thinking_mode
