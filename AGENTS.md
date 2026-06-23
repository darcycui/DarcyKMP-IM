# KMPDarcyDemo — 智能体指南

## 项目简介

基于 Kotlin Multiplatform (KMP) + Compose Multiplatform 的聊天应用，支持端到端加密。
目标平台：**Android**、**Desktop (JVM)**、**Web (JS)**、**iOS**。
共享 UI 代码位于 `commonMain` — Compose Multiplatform 在所有目标平台渲染。

## 关键技术栈

| 组件 | 版本 |
|---|---|
| Kotlin | 2.3.0 |
| Compose Multiplatform | 1.10.0 |
| AGP | 8.12.0 |
| Gradle | 8.14.3 (腾讯镜像) |
| KSP | 2.3.7 |
| Room | 3.0.0-alpha06 |

当前 KMP 项目使用 Google Room3 实现跨平台数据库，支持全平台。

## 源集与入口点

| 目标平台 | 源集 | 入口点 |
|---|---|---|
| Android | `androidMain/` | `MainActivity.kt` : `ComponentActivity` + `setContent { App() }` |
| Desktop | `desktopMain/` | `main.kt` : `fun main() = application { Window { App() } }` |
| Web (JS) | `jsMain/` | `webMain/main.kt` (目录存在但未在构建中配置源集 — 死代码？) |
| iOS | `iosMain/` | `MainViewController.kt` : `fun MainViewController() = ComposeUIViewController { App() }` |

iOS 框架名（构建产物）：`DarcyKMP`，静态链接。

## 架构

**MVI**（Model-View-Intent）模式，位于 `ui/base/`：
- `IIntent` → `IReducer` → `IState` → `IEvent`
- `BaseViewModel` 编排整个周期
- Fetch / Paging / ScreenStatus / Tips 等 reducer 可通过 `*CombinedReducer` 组合

平台抽象：`expect`/`actual` 机制，位于 `platform/`（File、Crypto、KeyValue、Picker、SSL、Screen、Time）。
每个平台（android、ios、desktop、js）提供各自的 `actual` 实现。

## 运行命令

```bash
# Android（在设备/模拟器上运行）
./gradlew :composeApp:installDebug

# Desktop
./gradlew :composeApp:run

# Web (JS)
./gradlew :composeApp:jsBrowserRun -t --quiet

# iOS — 需要 Xcode 项目；框架由外部消费
```

## 构建与测试

```bash
# 全部检查（commonTest + 各平台测试）
./gradlew :composeApp:check

# 仅 JVM 测试
./gradlew :composeApp:jvmTest

# JS 测试需要 Karma + 自定义 webpack 配置（参见 karma.config.d/）
./gradlew :composeApp:jsBrowserTest
```

测试框架：`kotlin.test`（无 JUnit Platform）。测试位于 `commonTest/kotlin/`。

## 注意事项

- **Room3 KSP** 手动按平台配置，而非使用标准 KSP DSL：
  ```
  add("kspAndroid", ...)
  add("kspIosArm64", ...)
  add("kspDesktop", ...)
  add("kspCommonMainMetadata", ...)
  ```
  JS wasm 目标已注释掉。

- **moko-resources** 生成 `SharedRes` 类（internal 可见性）。JS 的 webpack/karma 配置文件（`karma.config.d/` 和 `webpack.config.d/`）用于解决资源打包问题。

- **`-Xexpect-actual-classes`** 编译器标志全局设置（消除 expect/actual 类警告）。新增 actual 声明时需添加此标志。

- **Gradle 发行版** 使用腾讯镜像（`gradle-8.14.3-all.zip`）。本地 `.m2` 仓库（`mavenLocal()`）在 Google/Maven Central 仓库之前启用。

- **iOS 应用**（`iosApp` 模块）是外部生成的 Xcode 项目 — 不在此仓库中。iOS 框架由 Gradle 构建，Xcode 项目消费。

- **无 CI**（无 `.github/workflows/`）。

- **SSL 证书** 作为资源打包（`files/ssl/test2IPSelf241.p12`、`files/ssl/test2ServerSelf.p12`），在 `App.kt` 中加载。

- **`expect`/`actual` 命名约定**：所有通过 `expect`/`actual` 机制实现的类/文件，必须使用 `Platform` 后缀（例如 `TimePlatform`、`KtorEnginePlatform`）。非 `expect`/`actual` 的文件不应使用此后缀。

- **`kotlin.incremental.js=false`** 在 `gradle.properties` 中设置 — JS 增量编译已禁用。
