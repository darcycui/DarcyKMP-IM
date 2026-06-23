package com.darcy.kmpdemo.platform

import kotlinx.io.files.Path
import okio.Path.Companion.toPath

actual object FilePlatform {
    actual fun getCacheDir(): Path {
        return Path(DarcyFolder.DIR_CACHE)
    }

    actual fun getDocumentsDir(): Path {
        return Path(DarcyFolder.DIR_DOCUMENT)
    }

    actual fun getDownloadDir(): Path {
        return Path(DarcyFolder.DIR_DOWNLOAD)
    }

    actual suspend fun dealUriIfNeed(uriPath: Path): Path {
        return uriPath
    }
}