# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

基于 Kotlin Multiplatform (KMP) + Compose Multiplatform 的端到端加密聊天应用。共享 UI 和业务逻辑位于 `commonMain`，通过 Compose Multiplatform 在所有目标平台渲染。

**目标平台：** Android、Desktop (JVM)、Web (JS)、iOS
**iOS 应用**（`iosApp` 模块）是外部 Xcode 项目，不在此仓库中；iOS 框架由 Gradle 构建供 Xcode 消费。

## 关键技术栈

| 组件 | 版本 |
|---|---|
| Kotlin | 2.4.0 |
| Compose Multiplatform | 1.11.1 |
| AGP | 8.12.0 |
| Gradle | 8.14.3（腾讯镜像） |
| KSP | 2.3.9 |
| Room | 3.0.0-alpha06 |
| Ktor | 3.3.3 |
| Krossbow (STOMP) | 9.3.0 |

## 构建与运行命令

```bash
# Android（安装到设备/模拟器）
./gradlew :composeApp:installDebug

# Desktop
./gradlew :composeApp:run

# Web (JS) — 开发模式，支持热更新
./gradlew :composeApp:jsBrowserRun -t --quiet

# iOS — 先构建框架，再用 Xcode 运行 iosApp 项目
./gradlew :composeApp:linkDebugFrameworkIosArm64

# 全部测试（commonTest + 各平台测试）
./gradlew :composeApp:check

# 仅 JVM 测试
./gradlew :composeApp:jvmTest

# 仅 JS 测试（需要 Karma + webpack 配置，参见 karma.config.d/ 和 webpack.config.d/）
./gradlew :composeApp:jsBrowserTest
```

测试框架为 `kotlin.test`，测试代码位于 `commonTest/kotlin/`。

## 架构总览

### 分层结构（自下而上）

```
platform/          ← expect/actual 平台抽象（File、Crypto、SSL、Time、Picker、KeyValue 等）
storage/           ← 持久化层（Room3 数据库 + multiplatform-settings 键值存储 + 文件 + 内存）
network/           ← 网络层（HTTP: Ktor 插件管线 + WebSocket: Krossbow STOMP）
crypto/            ← 加密层（文件加密、消息加密、传输加密、HMAC、X3DH 密钥协商）
bean/              ← 数据类（HTTP 请求/响应、WebSocket STOMP 帧、UI 状态）
repository/        ← 数据仓库
ui/base/           ← MVI 框架基础设施
ui/screen/         ← 页面（按平台形态分 phone/desktop/learn）
ui/components/     ← 可复用 Compose 组件（原子设计：atom, molecule, structure）
ui/theme/colors/   ← 主题与颜色
x3dh/              ← X3DH 端到端加密密钥协商协议实现
utils/             ← 工具类
```

### MVI 架构（`ui/base/`）

核心抽象：`IIntent` → `IReducer` → `IState` → `IEvent`

- **`BaseViewModel<S : IState>`** — 所有 ViewModel 的基类。通过 `_uiState: MutableStateFlow<S>` 管理状态，通过 `_event: MutableSharedFlow<IEvent>` 发送一次性事件。`dispatch(intent)` 方法将所有 reducer 依次对当前状态执行 reduce 得到新状态。
- **`IState`** — UI 页面持续性的状态（文本、列表、加载状态等）
- **`IIntent`** — 用户/系统触发的意图
- **`IReducer<S>`** — 纯函数 `(intent, state) -> state`
- **`IEvent`** — 一次性 UI 事件（导航、Toast 等）

Reducer 组合模式（`ui/base/combined/`）：`ScreenStateFetchPagingTipsCombinedReducer` 将多个专项 reducer 合并为一个，每个专项 reducer 只关心 IState 的一个字段：
- **Fetch**（`impl/fetch/`）：数据加载状态（Loading/Success/Error）
- **Paging**（`impl/paging/`）：分页数据管理
- **ScreenStatus**（`impl/screenstatus/`）：页面级别状态
- **Tips**（`impl/tips/`）：提示信息

典型页面目录结构（以 `phone/privatechat/` 为例）：
```
intent/     ← IIntent 定义
state/      ← IState 定义
event/      ← IEvent 定义
reducer/    ← IReducer 实现
repository/ ← 数据仓库
usecase/    ← IUseCase 实现
```

### 平台抽象（expect/actual 机制）

`commonMain/platform/` 中定义的 expect 声明：

| expect | 用途 |
|---|---|
| `getPlatform()` / `isPhonePlatform()` / `isJvmPlatform()` / `isJsPlatform()` | 平台类型判断 |
| `FilePlatform` | 文件操作（缓存目录、文档目录、下载目录、URI 处理） |
| `KeyValueStorage` | 键值存储（基于 multiplatform-settings） |
| `KotlinCryptoPlatform` | 加密提供者（optimal / BouncyCastle） |
| `ImagePicker` | 图片选择器 |
| `TimePlatform` | 当前时间戳 |
| `encryptString()` / `decryptString()` | 字符串加解密 |
| `sslCertsConfig()` / `configureEngineTLS()` | SSL 证书配置 |
| `ShowUploadImage()` | 上传图片展示 |
| `createPlatformAntilog()` | 日志实现 |
| `getIMDatabaseBuilder()` | Room 数据库构建器（各平台 SQLite 驱动不同） |
| `DarcyIMDatabaseConstructor` | Room 实例化 |

每个平台源集（`androidMain/`、`desktopMain/`、`iosMain/`、`jsMain/`）提供各自的 `actual` 实现。

### 网络层

**HTTP（Ktor 客户端）：**
- `HttpManager` — 统一入口，封装 GET/POST/PUT/DELETE
- `KtorInstance` — Ktor HttpClient 引擎创建（各平台使用不同引擎：Android=OkHttp, Desktop=CIO, JS=Js, iOS=Darwin）
- 自定义 Ktor 插件：`EncryptRequestBodyPlugin`（请求体加密）、`DecryptResponseBodyPlugin`（响应体解密）、`CustomHeaderPlugin`
- `TokenManager` — Token 管理（存储在 KeyValueStorage 中）
- JSON 序列化通过 `kotlinx.serialization` 实现

**WebSocket（Krossbow STOMP）：**
- `WebSocketManager` — WebSocket 生命周期管理
- `KrossbowWebsocketClientImpl` — STOMP 协议实现，包含 `CryptoSessionWrapImpl`（加密会话包装）
- `StompFrameHelper` — STOMP 帧解析
- `HeartbeatHelper` — 心跳管理
- 备用实现：`KtorWebSocketClientImpl`（基于 Ktor WebSocket）

### 存储层

- **Room3 数据库**（`DarcyIMDatabase`）：跨平台 SQLite，包含 10 个表（User、Conversation、Friendship、PrivateMessage、IdentityKey、SignedPreKey、OneTimePreKey、SessionRecord、MessageReadStatus、SkippedMessageKey）。JS 端使用 OPFS（Origin Private File System）配合 `sqlite-web` 驱动实现持久化。
- **multiplatform-settings**：通过 `KeyValueStorage` expect 提供键值存储
- **内存存储**：`IMGlobalStorage`（全局状态）、`TransportGlobalStorage`（传输层状态）

### 加密架构（`crypto/`）

- **KMP加密库 cryptography-kotlin** 跨平台加密库，支持 Android、iOS、JVM、JS、WasmJS平台
- `transport/TransportCipherChaCha20` — 传输层加密（ChaCha20-Poly1305）
- `transport/TransportCipherGCM` — 传输层加密（AES-256-GCM，JS平台兼容）
- `message/MessageCipher` + `MessageHelper` — 消息体加密
- `file/FileCipher` — 文件加密
- `hmac/HMAC1` — HMAC 签名
- `JsonCryptoHelper` — JSON payload 加解密
- `repository/DHExchangeRepository` — DH 密钥交换
- `x3dh/` — X3DH 端到端加密密钥协商（chain、exchange、sign、repository、usecase）

### 入口点

| 平台 | 入口文件 | 说明 |
|---|---|---|
| Android | `androidMain/.../MainActivity.kt` | ComponentActivity + setContent { App() } |
| Desktop | `desktopMain/.../main.kt` | `fun main() = application { Window { App() } }` |
| Web (JS) | `webMain/.../main.kt` | `fun main() { ComposeViewport { AppTheme { App() } } }` |
| iOS | `iosMain/.../MainViewController.kt` | `fun MainViewController() = ComposeUIViewController { App() }` |

iOS 构建产物框架名：`DarcyKMP`，静态链接。

### 导航

使用 `org.jetbrains.androidx.navigation:navigation-compose`（Compose Multiplatform 的 Navigation3 实现），在 `ui/screen/phone/navigation/` 中定义：
- `PhoneRoute` — 路由常量
- `AppNavigationNavHost` — 导航图
- `BottomBarNavigation` — 底部导航栏
- `NavControllerExts` — 导航扩展函数

### 代码版本管理
- 使用 git 进行版本管理
- 提交信息用英文描述

### 回答规范
- 必须使用中文作为主要回答语言

## 重要注意事项

- **Room3 KSP 配置**是手动按平台添加的，而非使用标准 KSP DSL：
  ```
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspDesktop", libs.androidx.room.compiler)
  add("kspCommonMainMetadata", libs.androidx.room.compiler)
  add("kspJs", libs.androidx.room.compiler)
  ```
  wasm 目标已注释掉。

- **`-Xexpect-actual-classes`** 编译器标志在 `androidTarget` 和顶层 `sourceSets` 两处都设置了，消除 expect/actual class 的警告。

- **moko-resources**：生成 `SharedRes` 类（internal 可见性）。JS 的 webpack/karma 配置文件（`karma.config.d/moko-resources-generated.js`、`webpack.config.d/moko-resources-generated.js`）解决资源打包问题。

- **SSL 证书**作为 compose 资源打包在 `commonMain/composeResources/files/ssl/` 中，在 `App.kt` 的 `LaunchedEffect` 中读取并通过 `sslCertsConfig()` 配置到各平台。

- **JS 平台特殊处理**：
  - 入口点在 `webMain/`（使用 `ComposeViewport`），而非 `jsMain/`
  - `jsMain/` 提供平台 actual 实现（`platform/*.js.kt`、`storage/database/DarcyIMDatabase.js.kt`）
  - 日志使用自定义 `JsAntilog` 替代 Napier 默认实现
  - Webpack 和 Karma 配置目录中的 `.js` 文件由 moko-resources Gradle 插件自动生成，勿手动修改

- **无 CI/CD**：无 `.github/workflows/`。

- **版本依赖对应关系**：参考 README.md 中的 KMP/AGP/Gradle/Android Studio 版本兼容表。
