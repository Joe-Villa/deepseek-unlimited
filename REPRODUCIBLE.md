# 可复现构建证明

## 结论

GitHub Releases 的 `deepseek-unlimited-0.11.3.apk` 由本仓库源码、固定版本的 Gradle/JDK 工具链和 `keystore/debug.keystore` 重新构建得到。

独立执行两次 `clean :app:assembleRelease`：

- 两次构建的 **APK payload（所有 ZIP 条目）完全一致**；
- 用同一 debug key、只启用 v2 签名做规范化重签后，两个 APK **字节级完全一致**；
- 发布 APK 的 SHA-256 为：

```text
0aaedd4e1ff4bf2e1347ae7fe82168abb768f850453afe2dd176420a16a7246d
```

- 规范化 payload SHA-256（按条目名排序，依次 hash UTF-8 名称、十进制长度、内容，无分隔符）：

```text
bff49a4e73b1e951e9092e99e457d03e784270dc0c139898662cddfb1f608036
```

- 证书 SHA-256：

```text
88e927c08e54abc623d15e7d8a3b455cb489f78271f4cc1f8fa97cd275b41d61
```

## 为什么需要“规范化重签”

AGP 的 `packageRelease`/签名任务会在 APK 尾部生成签名块；不同 clean build 的**签名块字节**可能不同，即使 ZIP 里的所有文件完全一致。为了得到字节级可比的发布包，本仓库：

1. 用 Gradle 从源码构建；
2. 用 `apksigner` 以同一个 debug key、只启用 v2 签名重新签名；
3. 这个重签过程是确定性的；两次 clean build 的最终 APK 完全一致。

v2 签名在 Android 7.0+ 有效，本项目 `minSdk 31`，不影响安装。

## 一键复现

```bash
./scripts/reproduce-release.sh /path/to/deepseek-unlimited-0.11.3.apk
```

APK 从 GitHub Releases 下载；仓库不存放安装包。省略路径时，脚本默认使用 `dist/deepseek-unlimited-0.11.3.apk`。

脚本会：

1. 校验 `SOURCE-MANIFEST.sha256`（所有源码/构建配置/keystore 的 SHA-256）；
2. `clean :app:assembleRelease`；
3. 用 `keystore/debug.keystore` 做 v2-only 重签；
4. 与你传入的发布 APK 做 `cmp`；
5. 验证签名证书。

## 工具链

- JDK 17：`/usr/lib/jvm/java-17-openjdk-amd64`
- Gradle：9.4.1（维护者本地 `vendor/`；仓库不提交；公共构建使用 wrapper）
- Android SDK / build-tools：37.0.0（维护者本地 `vendor/`；公共构建使用本机 SDK）
- 依赖：维护者本地使用 `vendor/.gradle` 离线缓存；公共构建联网解析固定版本
- minSdk 31 / targetSdk 37
- 维护者本地使用 `vendor/` 时没有网络请求参与构建；公共构建需要联网下载 Gradle wrapper 和依赖（版本固定）

## 源码完整性

`SOURCE-MANIFEST.sha256` 覆盖：

- `app/src/main/**`
- `app/src/test/**`
- `app/build.gradle.kts`、根构建脚本、`gradle/libs.versions.toml`、`gradle.properties`
- `localization/**`
- `app/proguard-rules.pro`
- `keystore/debug.keystore`
- `scripts/generate_localization.py`
- `scripts/reproduce-release.sh`

没有把任何未列入源码树的二进制/私有代码打进 APK；APK 的 payload 只由这些源码、资源和构建配置生成。

## 关于 0.10.0

0.10.0 的 APK 是增量构建产物，R8 输入里带有一次旧的 `OpenAIAPIImpl` 静态初始化残留；clean build 后该残留消失。0.11.3 是重新做 clean build + 规范化签名的版本，作为本次可复现证明的正式包。

## 限制

- 这是 Android debug key，不是生产签名；正式分发请换正式 keystore，并保留同样流程。
- 仓库不提交维护者本地的 `vendor/`（约 3 GB）。没有 `vendor/` 时，`reproduce-release.sh` 会回退到 `./gradlew` 和本机 Android SDK；要做字节级对比，仍需使用 Gradle 9.4.1、JDK 17、build-tools 37.0.0 和相同的依赖版本。
- 未在真机安装验证；本证明针对“源码 → APK”的构建一致性。


## 清单文件自身

`SOURCE-MANIFEST.sha256` 的 SHA-256：

```text
6e06e0b9e061ff3d184c25e3494b081a2f38b036d9fe2324511482d7f81055ac
```
