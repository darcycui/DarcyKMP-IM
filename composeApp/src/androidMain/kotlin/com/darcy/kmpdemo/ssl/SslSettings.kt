package com.darcy.kmpdemo.ssl

import com.darcy.kmpdemo.log.logE
import com.darcy.kmpdemo.log.logV
import java.io.ByteArrayInputStream
import java.security.KeyStore
import java.security.cert.Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object SslSettings {
    // resources中内置证书路径
    const val KEYSTORE_PATH_SERVER = "files/ssl/test2ServerSelf.p12"
    const val KEYSTORE_PATH_IP = "files/ssl/test2IPSelf241.p12"

    /**
     * 初始化证书 certBytes
     */
    var certBytesList: List<ByteArray>? = null

    fun initCertBytes(bytesList: List<ByteArray>) {
        certBytesList = bytesList
    }

    /**
     * 从 PKCS12 字节数组提取证书
     * 先尝试 Android 标准方式，失败后用 Bouncy Castle 的 KeyStore SPI 直接加载
     */
    private fun extractCertificatesFromP12(p12Bytes: ByteArray, password: CharArray): List<Certificate> {
        // 尝试1: Android 标准 KeyStore 方式
        try {
            return loadCertsFromKeyStore(p12Bytes, password, null)
        } catch (e: Exception) {
            logV("extract: Android PKCS12 加载失败: ${e.message}")
        }

        // 尝试2: Bouncy Castle KeyStore SPI 直接加载（解决 Android BC 不支持某些 MAC 算法的问题）
        return try {
            val bcProvider = org.bouncycastle.jce.provider.BouncyCastleProvider()
            loadCertsFromKeyStore(p12Bytes, password, bcProvider)
        } catch (e: Exception) {
            logE("extract: BC 加载也失败: ${e.message}")
            throw SSLException("无法加载 PKCS12 证书: ${e.message}", e)
        }
    }

    /** 使用指定的 Provider 加载 PKCS12 并提取证书（provider=null 表示使用默认） */
    private fun loadCertsFromKeyStore(
        p12Bytes: ByteArray,
        password: CharArray,
        provider: java.security.Provider?
    ): List<Certificate> {
        ByteArrayInputStream(p12Bytes).use { certStream ->
            val tempKeyStore = if (provider != null) {
                KeyStore.getInstance("PKCS12", provider)
            } else {
                KeyStore.getInstance("PKCS12")
            }.apply {
                load(certStream, password)
            }
            val certs = mutableListOf<Certificate>()
            tempKeyStore.aliases().toList().forEach { alias ->
                when {
                    tempKeyStore.isCertificateEntry(alias) -> {
                        certs.add(tempKeyStore.getCertificate(alias))
                    }
                    tempKeyStore.isKeyEntry(alias) -> {
                        val chain = tempKeyStore.getCertificateChain(alias)
                        chain?.forEach { certs.add(it) }
                    }
                }
            }
            if (certs.isEmpty()) {
                throw SSLException("PKCS12 中未找到证书")
            }
            logV("extract: BC库加载证书成功 获取证书数量: ${certs.size}")
            return certs
        }
    }

    /**
     * 获取 KeyStore
     */
    fun getKeyStore(): KeyStore {
        val certList = certBytesList ?: throw IllegalStateException("Certificates not initialized")
        val keyStorePassword = "1234".toCharArray()
        val keyStore = KeyStore.getInstance("PKCS12").apply {
            load(null, null)
        }

        certList.forEachIndexed { index, bytes ->
            val certs = extractCertificatesFromP12(bytes, keyStorePassword)
            certs.forEachIndexed { chainIndex, cert ->
                keyStore.setCertificateEntry("cert_${index}_$chainIndex", cert)
            }
        }
        return keyStore
    }

    /**
     * 获取 TrustManagerFactory
     */
    fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory =
            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    /**
     * 获取 SSLContext
     */
    fun getSslContext(): SSLContext? {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, getTrustManagerFactory()?.trustManagers, null)
        return sslContext
    }

    /**
     * 获取 Cert TrustManager
     */
    fun getTrustManager(): X509TrustManager? {
        // 1. 获取系统默认TrustManager
        val systemTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        systemTrustManagerFactory.init(null as KeyStore?) // 加载系统默认CA
        val systemTrustManager = systemTrustManagerFactory.trustManagers.first { it is X509TrustManager } as X509TrustManager

        // 2. 获取自定义证书TrustManager
        val customTrustManager =
            getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as? X509TrustManager

        // 验证证书链非空
        if (customTrustManager == null || customTrustManager.acceptedIssuers?.isEmpty() == true) {
            throw SSLException("No trusted certificates found in keystore")
        }

        // 3. 合并两者
        return CompositeX509TrustManager(systemTrustManager, customTrustManager)
    }
}
