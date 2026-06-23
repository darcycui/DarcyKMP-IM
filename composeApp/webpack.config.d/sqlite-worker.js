/**
 * Webpack 配置扩展 — SQLite Web Worker (浏览器原生 ESM Module Worker)
 *
 * 不通过 webpack 打包 Worker（避免 ESM/npm 解析和 document 引用问题），
 * 改为将 @sqlite.org/sqlite-wasm 的 dist 文件和 sqlite-worker.js 注入 webpack compilation assets，
 * Worker 使用浏览器原生 ESM import 加载依赖。
 *
 * 使用 compiler.hooks.emit 直接修改 compilation.assets，
 * 这样 webpack-dev-server（memfs）和 production build（磁盘）都能正确提供文件。
 */
;(function(config) {
    var path = require('path');
    var fs = require('fs');

    // ── 路径定义 ──────────────────────────────────────────────────
    // __dirname = build/js/packages/KMPDarcyDemo-composeApp/（webpack 输出目录）
    var outputDir = __dirname;
    var jsBuildDir = path.resolve(__dirname, '..', '..');              // build/js/
    var npmDistDir = path.join(jsBuildDir, 'node_modules', '@sqlite.org', 'sqlite-wasm', 'dist');
    var workerSrc = path.resolve(
        __dirname, '..', '..', '..', '..',
        'composeApp', 'src', 'jsMain', 'resources', 'sqlite-worker.js'
    );

    // 需要从 npm dist/ 注入 compilation assets 的文件
    var distFiles = [
        'sqlite3-worker1.mjs',
        'sqlite3.wasm',
        'sqlite3-opfs-async-proxy.js',
        'index.mjs',
    ];

    // ── 禁止 Webpack 拦截 new Worker() ────────────────────────────
    // 默认 Webpack 5 会处理 new Worker() 调用并创建单独的 chunk，
    // 但我们的 sqlite-worker.js 使用浏览器原生 ESM Module Worker，
    // 依赖文件通过 emit hook 直接注入 compilation.assets，不需要 webpack 打包。
    // 关闭 webpack 的 worker 解析，让 new Worker() 直接透传到浏览器。
    config.module = config.module || {};
    config.module.parser = config.module.parser || {};
    config.module.parser.javascript = config.module.parser.javascript || {};
    config.module.parser.javascript.worker = false;

    // ── 注入 compilation assets（emit hook，memfs + 磁盘）───────
    config.plugins.push({
        apply: function(compiler) {
            compiler.hooks.emit.tap('SqliteWorkerAssets', function(compilation) {
                // 注入 @sqlite.org/sqlite-wasm dist 文件
                distFiles.forEach(function(file) {
                    var src = path.join(npmDistDir, file);
                    try {
                        if (fs.existsSync(src)) {
                            var content = fs.readFileSync(src);
                            compilation.assets[file] = {
                                source: function() { return content; },
                                size: function() { return content.length; }
                            };
                        }
                    } catch (e) {
                        console.warn('[sqlite-worker] Asset inject failed:', file, e.message);
                    }
                });

                // 注入 worker 脚本本身
                try {
                    if (fs.existsSync(workerSrc)) {
                        var workerContent = fs.readFileSync(workerSrc);
                        compilation.assets['sqlite-worker.js'] = {
                            source: function() { return workerContent; },
                            size: function() { return workerContent.length; }
                        };
                    }
                } catch (e) {
                    console.warn('[sqlite-worker] Worker inject failed:', e.message);
                }

                console.log('[sqlite-worker] Injected SQLite dist files + worker into compilation assets');
            });
        }
    });

    // ── 检查 npm 包是否已安装 ────────────────────────────────────
    if (fs.existsSync(path.join(npmDistDir, 'sqlite3-worker1.mjs'))) {
        console.log('[sqlite-worker] npm package found, will inject as assets');
    } else {
        console.warn(
            '[sqlite-worker] @sqlite.org/sqlite-wasm not found.\n' +
            '  Run `./gradlew :composeApp:jsBrowserRun` to trigger npm install.'
        );
    }
})(config);
