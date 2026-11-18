# DeepSeek Unlimited 0.10.1

> 中文名：DeepSeek 无限制版  
> 非官方客户端。身份说明只在「关于与免责声明」里。  
> 发布包：`dist/deepseek-unlimited-0.10.1.apk`  
> 版本：`0.10.1`（versionCode 35）  
> 最低 Android：12（API 31）  
> SHA-256：`9bba2dc467fb49542ab7d129d62d24dcc3425fca33a19372d098aba95f7ee9f0`

## 这版是什么

- 图标与 0.10.0 相同：官方白底蓝鲸 + 底部橙色渐变通栏 + 四个白字「无限制版」。
- 这版的主要目的是**可复现构建证明**：发布 APK 可以从源码 clean build、再用仓库内 debug keystore 规范化重签得到。
- 0.10.0 是增量构建产物，R8 输入里保留了旧的 `OpenAIAPIImpl` 静态初始化残留；0.10.1 是 clean build 后的正式包。

## 复现

```bash
./scripts/reproduce-release.sh
```

脚本会：

1. 校验 `SOURCE-MANIFEST.sha256`；
2. `clean :app:assembleRelease --offline`；
3. 用 `keystore/debug.keystore` 做 v2-only 重签；
4. 与 `dist/deepseek-unlimited-0.10.1.apk` 做 `cmp`。

本机实测：

```text
build/verify/deepseek-reproduced.apk
9bba2dc467fb49542ab7d129d62d24dcc3425fca33a19372d098aba95f7ee9f0

dist/deepseek-unlimited-0.10.1.apk
9bba2dc467fb49542ab7d129d62d24dcc3425fca33a19372d098aba95f7ee9f0

OK: rebuilt APK is byte-identical to the published APK.
```

详见：

- [REPRODUCIBLE.md](../REPRODUCIBLE.md)
- [SOURCE-MANIFEST.sha256](../SOURCE-MANIFEST.sha256)

## 说明

- 使用仓库内标准 Android debug key（证书 SHA-256 `88e927c0...b41d61`），不是生产签名。
- 构建为离线构建，依赖 `vendor/.gradle`。
- 未真机安装验证。
