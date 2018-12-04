package com.cellpinda.coin.wallet.common.util

import android.util.Base64
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Created by https://github.com/method76 on 2017-10-30.
 */
class AES256Cipher {

    @Volatile private var INSTANCE: AES256Cipher? = null
    val secretKey = "12345678901234567890123456789012" // 32bit
    var IV        = ""                                 // 16bit

    fun getInstance(): AES256Cipher? {
        if (INSTANCE == null) {
            synchronized(AES256Cipher::class.java) {
                if (INSTANCE == null)
                    INSTANCE = AES256Cipher()
            }
        }
        return INSTANCE
    }

    private fun AES256Cipher(): AES256Cipher {
        IV = secretKey.substring(0, 16)
        return this
    }

    @Throws(java.io.UnsupportedEncodingException::class, NoSuchAlgorithmException::class,
            NoSuchPaddingException::class, InvalidKeyException::class,
            InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class,
            BadPaddingException::class)
    fun AES_Encode(str: String): String {
        val keyData = secretKey.toByteArray()
        val secureKey = SecretKeySpec(keyData, "AES")
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.ENCRYPT_MODE, secureKey, IvParameterSpec(IV.toByteArray()))
        val encrypted = c.doFinal(str.toByteArray(charset("UTF-8")))
        return String(Base64.encode(encrypted, encrypted.size))
    }

    @Throws(java.io.UnsupportedEncodingException::class, NoSuchAlgorithmException::class, NoSuchPaddingException::class, InvalidKeyException::class, InvalidAlgorithmParameterException::class, IllegalBlockSizeException::class, BadPaddingException::class)
    fun AES_Decode(str: String): String {
        val keyData = secretKey.toByteArray()
        val secureKey = SecretKeySpec(keyData, "AES")
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        c.init(Cipher.DECRYPT_MODE, secureKey, IvParameterSpec(IV.toByteArray(charset("UTF-8"))))
        val byteStr = Base64.decode(str.toByteArray(), str.toByteArray().size)
        return String(c.doFinal(byteStr), Charset.forName("UTF-8"))
    }

}
