# DeepSeek Unlimited 0.11.0

> 中文名：DeepSeek 无限制版  
> 非官方客户端。身份说明只在「关于与免责声明」里。  
> 发布包：`dist/deepseek-unlimited-0.11.0.apk`  
> 版本：`0.11.0`（versionCode 38）  
> 最低 Android：12（API 31）  
> SHA-256：`086876db31fd3c92660601952e400d8981f2a64cbdba106904df8ce3ef3bdad1`

## 图标

- 底部橙色通栏里的「无限制版」改为英文 **unlimited**。
- 仍以官方 `official-app-icon.jpg` 为底版，原图未修改。
- 自适应图标使用安全区内的 288×288 前景，避免放大裁切。

## 多语言（Localization）

- 新增 `localization/` 目录：
  - `languages.json`：语言列表与 Android qualifier；
  - `strings.json`：所有 key + English/简体中文；
  - `README.md`：维护方式。
- 新增 `scripts/generate_localization.py`：从 JSON 生成 `res/values*/strings_deepseek.xml`。
- 代码里的中文硬编码全部替换为 `R.string.ds_*` key。
- 设置 → 语言新增语言选择器：
  - 默认「跟随系统」；
  - 支持 English、简体中文、繁體中文、日本語、한국어、Français、Deutsch、Español、Português、Русский、العربية、हिन्दी、Indonesia、Italiano、ไทย、Tiếng Việt、Türkçe、Nederlands、Polski、Українська、فارسی。
- 选择后立即通过 `LocalizedContent` 切换 Compose 资源，不需要重启 Activity；RTL 语言同时切布局方向。
- 其他语言当前对未翻译 key 回退 English；简体中文完整。

## 说明

- 只改图标与 localization；其他功能不变。
- 未真机安装验证；编译通过。
- 可复现构建脚本同步更新。
