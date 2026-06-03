import dev.whyoleg.cryptography.random.CryptographyRandom

object RandomHelper {
    fun randomInt(min: Int, max: Int): Int {
        return (min..max).random()
    }

    fun secureRandomIV(bytes: Int): ByteArray {
        return CryptographyRandom.nextBytes(bytes)
    }
}