# DeepSeek Unlimited 0.9.7

> 中文名：DeepSeek 无限制版  
> 非官方客户端。身份说明只在「关于与免责声明」里。  
> 发布包：`dist/deepseek-unlimited-0.9.7.apk`  
> 版本：`0.9.7`（versionCode 31）  
> 最低 Android：12（API 31）  
> 签名：目前沿用 debug keystore，方便侧载；正式分发请换正式签名。  
> SHA-256：`e7cf1765c1176c930e77d3237902b96e9b38fc9dc0139ef7c84d1f258c084650`

## 新增：DeepSeek 官方导出 ZIP 导入 / 本机一键导出

### 1. 导入官方 ZIP

入口：设置 → 本机数据 → 导入对话 ZIP。

支持官方 `deepseek_data-*.zip`（`export_format_version: 3`）：

- 读取 `manifest.json`、`chat_sessions/*.json`。
- 按 `current_message_id` + `parent_id` 还原**当前分支**，不会把分叉里的旧回复混进来。
- 解析官方 fragment：
  - `REQUEST` → 用户消息；
  - `THINK` → 思考内容；
  - `RESPONSE` / `TEMPLATE_RESPONSE` → 回答；
  - `CONTENT_RISK` → 被过滤时的提示文本；
  - `TOOL_SEARCH` / `TOOL_OPEN` / `READ_LINK` → 来源胶囊；
  - `FILE` → 本地附件。
- 自动提取 `chat_sessions/*_files/` 里的图片/文件到 App 私有目录，并关联到用户消息。
- 按「标题 + 创建时间」去重；重复导入同一份 ZIP 时跳过已有对话，不覆盖本机数据。
- 保留官方标题、创建时间、更新时间；不覆盖本机已有对话。

### 2. 一键导出

入口：设置 → 本机数据 → 导出所有对话。

- 生成与官方相同结构的 ZIP：
  - `manifest.json`（`export_format_version: 3`）
  - `user.json`（本地占位，不含 API Key）
  - `chat_sessions/<uuid>.json`
  - `chat_sessions/<uuid>_files/...`（本地图片/文件）
- 每条消息按官方字段导出：`id`、`parent_id`、`role`、`status`、`thinking_enabled`、`search_enabled`、`accumulated_token_usage`、`inserted_at`、`fragments`。
- 思考导出为 `THINK`；回答导出为 `RESPONSE`；来源导出为 `TOOL_SEARCH`；用户附件导出为 `FILE` + 实际文件条目。
- 使用 SAF 选择保存位置，不写公共存储、不需要存储权限。

### 3. UI

- 设置页「本机数据」新增「导入对话 ZIP」「导出所有对话」两行。
- 导入/导出时显示进度弹窗（当前/总数）；完成后显示对话、消息、附件、跳过、失败数量。
- 导入完成自动刷新对话数量。

## 说明

- 导出文件不含 API Key、账号信息；`user.json` 是本地占位。
- 导入不覆盖本机已有对话；重复对话跳过。
- 官方 ZIP 里的分叉消息只导入当前分支。
- 附件会复制到 App 私有目录，删除 App 数据会一并删除。
- 未真机跑；编译、单元测试、ZIP 往返测试通过。
- debug 签名，约 59MB。
