# DeepSeek Unlimited 0.11.3

> 中文名：DeepSeek 无限制版  
> 非官方客户端。身份说明只在「关于与免责声明」里。  
> 发布包：GitHub Releases asset `deepseek-unlimited-0.11.3.apk`  
> 版本：`0.11.3`（versionCode 41）  
> 最低 Android：12（API 31）  
> SHA-256：`0aaedd4e1ff4bf2e1347ae7fe82168abb768f850453afe2dd176420a16a7246d`

## 修复：切换语言闪退

原因：`LocalizedContent` 之前把 `LocalContext` 替换成了 `createConfigurationContext()` 返回的非 Activity `ContextImpl`。语言切换触发重组后，设置页的 `AlertDialog`、Activity 作用域 owner 等拿到的不再是 Activity，于是闪退，语言也没有真正切换。

修复：

- `LocalizedContent` 不再覆盖 `LocalContext`，保留原始 `ComponentActivity`。
- 仍然覆盖 `LocalResources`、`LocalConfiguration` 和 `LocalLayoutDirection`；Compose 的 `stringResource()` 读取 `LocalResources`，所以界面文字正常切换。
- 新增 Robolectric 回归测试 `LocalizedContentTest`：
  - 切换前 `LocalContext.current` 是 Activity；
  - 切换后仍然是 Activity；
  - 英文 `Language` 能切换成中文 `语言`。

测试通过；未真机安装验证。

## 不变

- 0.11.2 的数据策略不变：没有账号系统，没有后端，关闭系统备份/换机迁移。
- 图标、多语言列表、其他功能不变。
