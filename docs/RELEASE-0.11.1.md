# DeepSeek Unlimited 0.11.1

> 中文名：DeepSeek 无限制版  
> 非官方客户端。身份说明只在「关于与免责声明」里。  
> 发布包：`dist/deepseek-unlimited-0.11.1.apk`  
> 版本：`0.11.1`（versionCode 39）  
> 最低 Android：12（API 31）  
> SHA-256：`4a967e0f6c84d6b6cb4ee1f9b896dfef9be320442680f81104554fad104684fc`

## 图标

- 底部橙色通栏文字改为英文 **unlimited**。
- 以官方 `official-app-icon.jpg` 为底版，原图未改。
- 自适应图标前景为安全区内 288×288，避免放大裁切。

## 多语言

- `localization/`：`languages.json`、`strings.json`、`README.md`。
- `scripts/generate_localization.py` 生成 `res/values*/strings_deepseek.xml`。
- 代码中的中文硬编码已替换为 `R.string.ds_*`。
- 设置 → 语言：默认跟随系统，支持 21 个语言选项；选择后即时切换，支持 RTL。
- English / 简体中文完整；其他语言目前回退 English，可在 `localization/overrides/` 补翻译。

## 验证

- 图标、键盘收起、首页尺寸保持上一版。
- 0.11.1 使用 clean build 产物作为发布包；构建日志和 `SOURCE-MANIFEST.sha256` 可核对。
- 未真机安装验证。
