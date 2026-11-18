# DeepSeek Unlimited 0.9.8

> 中文名：DeepSeek 无限制版  
> 非官方客户端。身份说明只在「关于与免责声明」里。  
> 发布包：`dist/deepseek-unlimited-0.9.8.apk`  
> 版本：`0.9.8`（versionCode 32）  
> 最低 Android：12（API 31）  
> SHA-256：`1a977a1a936a304bfcb75ac8bacf13e54f8f773ee78387e564782e15fa2853a6`

## 这版做了什么

### 1. 超长历史：先压缩，压不下就滑动窗口

- 保留原有摘要压缩（TEXT_SUMMARY）/ 原生 OpenAI、Anthropic compaction。
- 新增 `SLIDING_WINDOW` 兜底：
  - 压缩调用失败；
  - 摘要压缩后仍超出模型窗口；
  - 甚至没有可压缩的 prefix；
  - → 保留最近能放下的若干轮，丢弃更早的轮次，并写入 checkpoint。
- 滑动窗口会持久化 checkpoint，下一次请求直接从这里继续，不会每轮重新加载几百万 token。
- 被编辑过的旧轮次会使 checkpoint 指纹失效，下一轮会重新从完整历史生成，不会错误复用旧摘要/旧窗口。

### 2. 5M token 并行压测

新增测试 `three parallel five million token histories fall back to sliding windows`：

- 3 个测试并行执行；
- 每个测试构造 **1000 轮 × 用户+助手**，历史估算 ≥ **5,000,000 token**；
- 压缩器故意不可用，验证自动切到滑动窗口；
- 断言窗口已丢弃旧轮次（coveredTurnCount > 0）、保留最近轮次、窗口估算 ≤ 128k 上下文上限。

测试结果：通过。

### 3. 编辑逻辑测试

新增 `editing one turn only changes context from that turn onward`：

- 修改中间某一轮后，旧 checkpoint 不被复用；
- 编辑后的内容进入新上下文；
- 编辑点之后被删除的轮次不会出现在新上下文；
- 配合已有 `editing a covered prefix invalidates the checkpoint` 测试。

测试结果：通过。

### 4. Fork 对话

- 对话页右上角新增 Fork 按钮（生成中禁用）。
- 点击后复制当前对话的**全部历史**：
  - 用户消息、助手回答、思考、时间线/来源、附件、修订、工具事件、平台模型设置；
  - 新对话使用新的 chat id、message id、run id；
  - 标题保持一致；只有唯一标识/时间戳变化。
- 自动跳到新对话。

## 测试

- `CompactionCoordinatorTest`：新增 3 路 5M token 并行压测、编辑逻辑测试；全量通过。
- `ChatArchiveManagerTest`：官方 ZIP 导入/导出结构往返测试通过。
- 全量单元测试通过。
- 未真机跑；本机无 KVM、无真实 Key。
- debug 签名，约 59MB。
