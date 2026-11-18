# 开发说明

给想自己编译、或者想改点东西的人。只是想聊天的话，看 [README](README.md) 就够了。

## 这个 App 是怎么来的

它是开源项目 [GPT Mobile](https://github.com/Taewan-P/gpt_mobile) 的裁剪版。

**保留**：聊天、流式输出、本地数据库、消息编辑与重新生成、长对话自动压缩、Key 本地加密。

**去掉**：多平台配置向导、首次启动的引导、以及所有"先配置再聊天"的步骤。

**新增**：一个只要求贴 Key 的首屏，以及一份预置好的 DeepSeek 配置。

上游是 GPL-3.0，所以本项目同样是 GPL-3.0。

## 0.9.4 改了什么（对齐官方操作细节）

- 官方鲸鱼 Logo（轮廓 path + 白色腹部/眼白），统一用于启动页/桌面/首页/配置页。
- 主界面移除「非官方」徽标；身份说明只放在设置 → 关于与免责声明。
- 首页改为「菜单 + DeepSeek 标题 + 新对话 + 左侧历史抽屉」；抽屉含搜索与时间分组。
- 对话页移除绿色 GPT Mobile 头像/平台按钮；顶部增加朗读与新对话；消息操作改为复制/赞/踩/分享/重新生成。
- 输入框增加 + 菜单（拍照/相册/文件）、语音输入；深度思考/联网搜索改为可点击开关，并真实影响请求。
- 版本 `0.9.4` / `versionCode 28`。

## 0.9.3 改了什么（DeepSeek 专业版）

- 强制深色主题，设置页收拢为一屏三组：API Key / 本机数据 / 非官方说明。
- 接入 DeepSeek 官方服务端工具：`deepseek_web_search`、`deepseek_read_url`；不需要额外的搜索 Key。
- 修复 `reasoning_content` 的解析；思考过程实时显示并在面板内滚动，正文开始后自动折叠。
- 显示搜索次数、搜索词与来源链接；来源由服务端返回，不伪造。
- 首次配置预置 `reasoning = true`，模型保持 `deepseek-flash`。
- 桌面/启动图标换成鲸鱼；启动页改为深色。
- 版本号 `0.9.3` / `versionCode 27`。

## 改了什么

| 文件 | 改动 |
| --- | --- |
| `app/src/main/kotlin/dev/chungjungsoo/gptmobile/presentation/ui/setup/DeepSeekSetupScreen.kt` | 新增：首屏只有一个输入框 |
| `app/src/main/kotlin/dev/chungjungsoo/gptmobile/presentation/ui/setup/DeepSeekSetupViewModel.kt` | 新增：校验后写入本地加密存储，并创建 DeepSeek 平台记录 |
| `app/src/main/kotlin/dev/chungjungsoo/gptmobile/presentation/common/Route.kt` | 新增路由 `deepseek_setup` |
| `app/src/main/kotlin/dev/chungjungsoo/gptmobile/presentation/common/NavigationGraph.kt` | 注册首屏 |
| `app/src/main/kotlin/dev/chungjungsoo/gptmobile/presentation/ui/main/MainActivity.kt` | 首次启动直接进入首屏 |
| `app/src/main/kotlin/dev/chungjungsoo/gptmobile/presentation/ui/setting/AboutScreen.kt` | 去掉指向上游商店的链接，改为项目说明 |
| `presentation/theme/Theme.kt` | 全局换成 DeepSeek 配色与圆角 |
| `presentation/ui/chat/ChatBubble.kt` | 用户气泡 14dp 圆角，回复操作图标改小改灰 |
| `presentation/ui/chat/ChatScreen.kt` | 输入框白底描边，发送键改为钴蓝圆形上箭头 |
| `presentation/ui/home/HomeScreen.kt` | 顶部 DeepSeek 标题、整行「开启新对话」、去掉悬浮按钮、列表扁平化 |
| `presentation/ui/setup/DeepSeekSetupScreen.kt` | 拆出无状态 `DeepSeekSetupContent`，加 ∞ 标记，便于截图测试 |
| `app/build.gradle.kts` | `applicationId` 改为 `com.deepseek.unlimited` |
| `app/src/main/res/values*/strings.xml` | 应用名：英文/兜底为「DeepSeek Unlimited」，中文为「DeepSeek 无限制版」 |
| `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` | 自适应图标（深色底 + 鲸鱼） |
| `data/dto/openai/request/ChatCompletionRequest.kt` | 支持 `thinking`、`title`、服务端 `tools`（`List<JsonObject>`） |
| `data/dto/openai/request/ChatMessage.kt` | 支持 `reasoning_content` 与 `server_tool_calls` 回传 |
| `data/dto/openai/response/ChatCompletionChunk.kt` | 解析 `reasoning_content`、`server_tool_event`、`server_tool_calls`、`annotations` |
| `data/agent/provider/ProviderAdapters.kt` | DeepSeek 请求启用思考 + 联网工具，流式解析思考/搜索事件 |
| `data/agent/provider/ProviderEventAssemblers.kt` | 把服务端搜索事件转成 `ProviderEvent.ServerTool` |
| `data/database/entity/MessageV2.kt` | 从 timeline 中取回 `server_tool_calls` 供下一轮回传 |
| `presentation/ui/chat/ThinkingBlock.kt` | 新增 `StreamingThinkingPanel`，思考内容自动滚动 |
| `presentation/ui/chat/ServerToolBlock.kt` | 搜索状态、搜索词、来源列表 |
| `presentation/ui/chat/ChatScreen.kt` | 输入框收起附件入口，显示「深度思考 / 联网搜索」状态；顶栏精简 |
| `presentation/ui/setting/SettingScreen.kt` | 重写为极简设置（Key / 数据 / 关于） |
| `presentation/ui/setting/AboutScreen.kt` | 非官方说明与 GPL 免责声明 |
| `presentation/ui/home/HomeScreen.kt` | 鲸鱼问候 + 新对话 + 最近对话 |
| `presentation/theme/Theme.kt` / `MainActivity.kt` | 强制深色 DeepSeek 配色 |
| `res/drawable/ic_whale.xml` / `ic_launcher_foreground.xml` / splash | 鲸鱼图标与深色启动页 |

## 预置参数

- API 地址：`https://api.deepseek.com/v1`（OpenAI 兼容协议）
- 模型：`deepseek-flash`
- 流式输出：开

## 构建

需要 JDK 17、Android SDK 37（`compileSdk = 37`、`build-tools 37`）。

> 本机环境里 Gradle 用户目录可能不可写。可以直接使用仓库自带的 Gradle 与可写缓存：
>
> ```bash
> export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
> export GRADLE_USER_HOME="$PWD/vendor/.gradle"
> ./vendor/gradle-9.4.1/bin/gradle :app:assembleDebug --offline --no-daemon
> ./vendor/gradle-9.4.1/bin/gradle :app:assembleRelease --offline --no-daemon
> ```

```bash
export JAVA_HOME=/path/to/jdk17
export ANDROID_HOME=/path/to/android-sdk
export GRADLE_USER_HOME=/path/to/writable/gradle-home

./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/app-debug.apk`（当前约 134MB）。Release 产物在 `app/build/outputs/apk/release/app-release.apk`（约 59MB）。`dist/` 中会放一份可安装的 0.9.3 包。

如果 `sdkmanager` 连不上，可以直接从 `https://dl.google.com/android/repository/` 下 `platform-37.0_r02.zip`，解压到 `platforms/android-37`，再从 `build-tools_r37_linux.zip` 解压到 `build-tools/37.0.0`。

## 发布版

```bash
./gradlew :app:assembleRelease
```

R8 全量压缩很慢（这个体量的 App 可能半小时以上），产物在 `app/build/outputs/apk/release/`。

目前 release 用 debug 签名，方便直接安装；正式分发请换成自己的 keystore。

## 已经补上的两个坑

1. **首屏 Key 校验**：贴完点按钮会先请求 `GET /v1/models`。401/403 直接提示「这把钥匙好像不对」；断网等其它情况放行。
2. **长对话的上下文容量**：自定义端点解析不出模型上下文长度时，上游逻辑会拒绝发送并弹「请输入上下文长度」。现在首屏保存 Key 时会同时写入 1,000,000 tokens 的容量，长对话可以直接续着聊。

## 界面：对齐 DeepSeek

配色与圆角集中在 `presentation/theme/Theme.kt`：

| 令牌 | 值 |
| --- | --- |
| 主色 cobalt | `#4D6BFE` |
| 用户气泡浅蓝 | `#EEF2FF` |
| 画布 | `#FFFFFF` |
| 次级表面 | `#F7F8FA` |
| 正文 | `#15171B` |
| 次要文字 | `#777B84` |
| 分割线 | `#E7E9ED` |
| 圆角 | 控件 14 / 卡片 18 / 弹层 28 |

具体改动：

- 全局配色替换掉上游的 Material 默认绿；动态取色默认关闭，保证品牌色稳定。
- 用户气泡 14dp 圆角、浅蓝底；助手回复不加框，就是正文。
- 输入框白底 + 1dp 描边 + 18dp 圆角，发送键是钴蓝圆形 + 白色上箭头。
- 首页顶部是「DeepSeek」标题，下面一整行「开启新对话」按钮，去掉悬浮按钮。
- 会话列表是纯文字行，去掉图标和平台副标题。
- 首屏加了 ∞ 标记。

## 截图（不需要设备）

`app/src/test/kotlin/dev/chungjungsoo/gptmobile/DeepSeekLookTest.kt` 用 Roborazzi + Robolectric 在 JVM 里渲染界面：

```bash
./gradlew :app:testDebugUnitTest --tests "*DeepSeekLookTest*" -Pjava21Home=/path/to/jre21
```

产物在 `app/build/screenshots/`。需要 JRE 21，因为某个依赖是 Java 21 字节码。

## 待办

- [ ] 真机 / 模拟器跑通完整流程（本机没有 KVM，模拟器起不来）
- [ ] 设置页收拢（隐藏多平台、本地模型等上游功能）
- [ ] APK 体积优化（去掉本地模型用的原生库，可省约 50 MB）
- [ ] 正式签名与 GitHub Release 流程
