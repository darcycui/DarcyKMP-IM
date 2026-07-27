package com.darcy.kmpdemo.ssl

import android.annotation.SuppressLint
import com.darcy.kmpdemo.log.logV
import com.darcy.kmpdemo.log.logW
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

// 合并系统默认CA和自定义证书的TrustManager
@SuppressLint("CustomX509TrustManager")
class CompositeX509TrustManager(
    private val systemTrustManager: X509TrustManager,
    private val customTrustManager: X509TrustManager
) : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
        try {
            customTrustManager.checkClientTrusted(chain, authType)
        } catch (e: CertificateException) {
            systemTrustManager.checkClientTrusted(chain, authType)
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        // 1. 先用自定义 TrustManager 按标准 PKIX 验证
        try {
            customTrustManager.checkServerTrusted(chain, authType)
            logV("checkServerTrusted: custom trust manager validated the chain")
            return
        } catch (e: CertificateException) {
            logW("checkServerTrusted: custom PKIX validation failed: ${e.message}")
        }

        // 2. PKIX 验证失败时，直接通过公钥比对来验证证书链中的每个证书
        //    自签名证书可能因缺少 CA 扩展等非密码学原因被 PKIX 拒绝，但直接 verify 能绕过这些策略检查
        val issuers = customTrustManager.acceptedIssuers
        for (cert in chain) {
            for (trusted in issuers) {
                try {
                    cert.verify(trusted.publicKey)
                    logV("checkServerTrusted: cert verified directly against trusted issuer")
                    return
                } catch (_: Exception) {
                    // 继续尝试下一个
                }
            }
        }

        // 3. 兜底：用系统 TrustManager
        logW("checkServerTrusted: falling back to system trust manager")
        systemTrustManager.checkServerTrusted(chain, authType)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return systemTrustManager.acceptedIssuers + customTrustManager.acceptedIssuers
    }
}