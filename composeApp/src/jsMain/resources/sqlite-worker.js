/**
 * SQLite Web Worker for Room WebWorkerSQLiteDriver
 *
 * 此文件作为浏览器原生 ESM Module Worker 加载（type: "module"），不会被 webpack 打包。
 * 所有 import 使用相对路径，依赖文件由 webpack afterEmit hook 复制到同一输出目录。
 *
 * 实现 androidx.sqlite.driver.web.WebWorkerSQLiteDriver 的协议：
 *   open / prepare / step / close
 *
 * 改进：添加 WASM 初始化超时、错误上报、ping 命令
 */

import sqlite3InitModule from './sqlite3-worker1.mjs';

const WASM_INIT_TIMEOUT_MS = 20000;

let sqlite3 = null;
let initError = null;
let initComplete = false;
let databases = {};
let statements = {};
let nextDbId = 0;
let nextStmtId = 0;

/**
 * Shared init promise — ensures only ONE call to sqlite3InitModule ever happens.
 * If ensureInited() is called before the top-level init completes, it awaits this
 * same promise instead of starting a duplicate init that would race.
 */
let initPromise = null;

async function initSqlite() {
    const timeoutPromise = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('SQLite WASM init timed out after ' + WASM_INIT_TIMEOUT_MS + 'ms')), WASM_INIT_TIMEOUT_MS)
    );
    sqlite3 = await Promise.race([
        sqlite3InitModule({
            print: console.log,
            printErr: console.error,
        }),
        timeoutPromise
    ]);
    initComplete = true;
    console.log('[sqlite-worker] SQLite WASM initialized');
}

// Start WASM init immediately and store the promise so ensureInited() can await it
initPromise = initSqlite().then(() => {
    console.log('[sqlite-worker] WASM init resolved successfully');
}).catch(err => {
    initError = err;
    console.error('[sqlite-worker] SQLite WASM init failed:', err);
});

async function ensureInited() {
    if (initError) throw initError;
    if (!initComplete) {
        // Await the shared init promise instead of calling initSqlite() again.
        // This prevents a second concurrent call to sqlite3InitModule.
        await initPromise;
        // After the promise settles, re-check for errors
        if (initError) throw initError;
    }
}

function postSuccess(id, data) {
    self.postMessage({ id, data });
}

function postError(id, message) {
    self.postMessage({ id, error: String(message) });
}

function handlePing(id) {
    postSuccess(id, {
        ok: true,
        initComplete: initComplete,
        initError: initError ? initError.message : null
    });
}

function handleOpen(id, fileName) {
    try {
        if (!sqlite3) throw new Error('SQLite not initialized: ' + (initError ? initError.message : 'unknown'));
        const db = new sqlite3.oo1.OpfsDb(fileName, 'c');
        const dbId = ++nextDbId;
        databases[dbId] = db;
        postSuccess(id, { databaseId: dbId });
    } catch (e) {
        postError(id, e.message);
    }
}

function handlePrepare(id, databaseId, sql) {
    try {
        const db = databases[databaseId];
        if (!db) { postError(id, 'Database not found: ' + databaseId); return; }
        const stmt = db.prepare(sql);
        const colNames = [];
        try {
            const count = stmt.columnCount();
            for (let i = 0; i < count; i++) colNames.push(stmt.columnName(i));
        } catch (_) { /* not a SELECT */ }
        let paramCount = 0;
        try { paramCount = stmt.parameterCount(); } catch (_) {}
        const stmtId = ++nextStmtId;
        statements[stmtId] = stmt;
        postSuccess(id, { statementId: stmtId, parameterCount: paramCount, columnNames: colNames });
    } catch (e) {
        postError(id, e.message);
    }
}

function handleStep(id, statementId, bindings) {
    try {
        const stmt = statements[statementId];
        if (!stmt) { postError(id, 'Statement not found: ' + statementId); return; }
        if (bindings && bindings.length > 0) {
            for (let i = 0; i < bindings.length; i++)
                stmt.bind(i + 1, bindings[i] ?? null);
        }
        const rows = [];
        const columnTypes = [];
        let firstRow = true;
        while (stmt.step()) {
            const rowObj = stmt.get();
            const rowArray = Object.values(rowObj || {});
            rows.push(rowArray);
            if (firstRow && rowArray.length > 0) {
                for (let j = 0; j < rowArray.length; j++)
                    columnTypes.push(mapValueType(rowArray[j]));
                firstRow = false;
            }
        }
        stmt.reset();
        postSuccess(id, { rows, columnTypes });
    } catch (e) {
        try { statements[statementId]?.reset(); } catch (_) {}
        postError(id, e.message);
    }
}

function mapValueType(v) {
    if (v === null || v === undefined) return 0;
    if (typeof v === 'number') return Number.isInteger(v) ? 1 : 2;
    if (typeof v === 'string') return 3;
    if (v instanceof Uint8Array || v instanceof ArrayBuffer) return 4;
    return 3;
}

function handleClose(statementId, databaseId) {
    if (statementId != null) {
        try { statements[statementId]?.finalize(); } catch (e) { console.warn(e); }
        delete statements[statementId];
    }
    if (databaseId != null) {
        try { databases[databaseId]?.close(); } catch (e) { console.warn(e); }
        delete databases[databaseId];
    }
}

self.onmessage = async function (event) {
    const { id, data } = event.data;
    if (!data?.cmd) return;

    // ping 立即响应，无需等待 init
    if (data.cmd === 'ping') {
        handlePing(id);
        return;
    }

    // 其他命令等待 WASM init 完成
    try {
        await ensureInited();
    } catch (e) {
        postError(id, 'Init failed: ' + e.message);
        return;
    }

    switch (data.cmd) {
        case 'open':    handleOpen(id, data.fileName); break;
        case 'prepare': handlePrepare(id, data.databaseId, data.sql); break;
        case 'step':    handleStep(id, data.statementId, data.bindings); break;
        case 'close':   handleClose(data.statementId, data.databaseId); break;
        default:        postError(id, 'Unknown command: ' + data.cmd);
    }
};
