# KMPDarcyDemo — 智能体指南

中文回答，英文 Git 提交（Conventional Commits）。

## 技术栈

| 组件 | 版本 |
|---|---|
| Kotlin | 2.4.0 |
| Compose Multiplatform | 1.11.1 |
| AGP | 8.12.0 |
| Gradle | 8.14.5（腾讯镜像 `mirrors.cloud.tencent.com`） |
| KSP | 2.3.9 |
| Room | 3.0.0-rc01 |

## 运行命令

```bash
./gradlew :composeApp:installDebug          # Android
./gradlew :composeApp:run                   # Desktop
./gradlew :composeApp:jsBrowserRun -t --quiet  # Web (JS)
./gradlew :composeApp:linkDebugFrameworkIosArm64  # iOS 框架
./gradlew :composeApp:check                 # 全部测试
./gradlew :composeApp:jvmTest               # 仅 JVM 测试
./gradlew :composeApp:jsBrowserTest         # JS 测试（需 Karma + webpack）
```

## 入口点

| 平台 | 源集 | 文件 |
|---|---|---|
| Android | `androidMain/` | `MainActivity.kt` — `ComponentActivity` + `setContent { App() }` |
| Desktop | `desktopMain/` | `main.kt` — `fun main() = application { Window { App() } }` |
| Web (JS) | `webMain/` | `main.kt` — `ComposeViewport { App() }` |
| Wasm | `wasmJsMain/` | 无独立入口，共享 webMain 启动逻辑 |
| iOS | `iosMain/` | `MainViewController.kt` — `ComposeUIViewController { App() }` |

iOS 框架产物：`DarcyKMP`，静态链接，由外部 Xcode 项目消费。

## 源集结构（8 个源集）

`webMain/` 和 `wasmJsMain/` 是独立源集，提供 `actual` 实现和启动入口。
`jsMain/` 仅含 platform actual 声明（`platform/*.js.kt`），无入口点。
`commonMain/composeResources/files/ssl/` 内置 SSL 证书，`App.kt` 中 `LaunchedEffect` 加载。

## 架构

分层（自下而上）：`platform/` → `storage/` → `network/` → `crypto/` → `bean/` → `repository/` → `ui/`

**MVI** 框架位于 `ui/base/`：
- `IIntent` → `IReducer` → `IState` → `IEvent`
- `BaseViewModel` 编排，Reducer 可组合（Fetch/Paging/ScreenStatus/Tips → `*CombinedReducer`）

**expect/actual 平台抽象**（`commonMain/platform/`）：所有 expect/actual 类必须使用 `Platform` 后缀（`FilePlatform`、`TimePlatform`、`KtorEnginePlatform` 等）。新增 actual 声明时需确认 `-Xexpect-actual-classes` 编译器标志已启用（已在 `androidTarget` 和顶层 `compilerOptions` 两处设置）。

**Room3 KSP** 手动按平台添加（`add("kspAndroid", ...)`），未使用标准 KSP DSL。全部平台（含 `kspJs`、`kspWasmJs`）均已配置。

**JS WebWorker**：`sqliteWasmWorker` 模块提供 SQLite OPFS 持久化 Worker 脚本，`webMain` 依赖此模块。

## 注意事项

- **moko-resources** (`dev.icerock.moko:resources`) 生成 `SharedRes` 类（internal）。Gradle 插件自动生成 `karma.config.d/moko-resources-generated.js` 和 `webpack.config.d/moko-resources-generated.js` — 勿手动修改。
- **mavenLocal()** 在 Google / Maven Central 之前启用。
- **无 CI**（无 `.github/workflows/`）。
- **测试框架**：`kotlin.test`（无 JUnit Platform），测试位于 `commonTest/kotlin/`。
- `gradle.properties` 指定 JDK 21 路径（`org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`）。
