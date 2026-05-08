package com.darcy.kmpdemo.utils

import com.darcy.kmpdemo.log.logE
import kotlinx.io.files.Path
import org.kotlincrypto.hash.md.MD5

object Md5Helper {
    suspend fun calculateMD5Sync(filePath: String, toLowerCase: Boolean = true): String {
        return runCatching {
            val path: Path = Path(filePath)
            val digest = MD5()
            FileHelper.readFileBuffered(path) { bufferedData ->
                // 流式读取文件 计算MD5
                if (bufferedData.isNotEmpty()) {
                    digest.update(bufferedData)
                }
            }
            HexHelper.bytesToHexStr(digest.digest()).apply {
                return if (toLowerCase) this.lowercase() else this
            }
        }.onFailure {
            logE("Md5Helper calculateMD5Sync error: ${it.message}")
            it.printStackTrace()
        }.getOrElse { "default" }
    }
}

/* 记录原有代码 expect/actual jvm实现
*     actual fun calculateMD5Sync(filePath: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val file = File(filePath)

        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int

            do {
                bytesRead = fis.read(buffer)
                if (bytesRead > 0) {
                    digest.update(buffer, 0, bytesRead)
                }
            } while (bytesRead != -1)
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }
*/