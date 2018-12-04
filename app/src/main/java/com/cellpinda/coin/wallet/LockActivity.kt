package com.cellpinda.coin.wallet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.security.keystore.KeyProperties
import android.support.annotation.RequiresApi
import android.util.Log
import android.widget.Toast
import com.andrognito.pinlockview.PinLockListener
import com.cellpinda.coin.wallet.common.abst.MethodActivity
import com.cellpinda.coin.wallet.common.util.FingerprintHandler
import com.cellpinda.coin.wallet.service.WalletLocalService
import kotlinx.android.synthetic.main.activity_lock.*
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class LockActivity : MethodActivity(), PinLockListener {

    private val TAG: String = this.javaClass.simpleName
    private val KEY_NAME = "example_key"
    private var backCount:Int = 0
    private var cipher: Cipher? = null
    private var fingerprintManager: FingerprintManager? = null
    private var keyStore: KeyStore? = null
    private var keyGenerator: KeyGenerator? = null
    private var cryptoObject: FingerprintManager.CryptoObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)
        pin_lock_view.setPinLockListener(this)
        pin_lock_view.attachIndicatorDots(findViewById(R.id.indicator_dots))

        fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE)
                as FingerprintManager
        if (fingerprintManager!!.hasEnrolledFingerprints()) {
            Log.i(TAG, "finger enrolled")
            if (generateKey()) {
                if (cipherInit()) {
                    cipher?.let {
                        cryptoObject = FingerprintManager.CryptoObject(it)
                    }
                    val helper = FingerprintHandler(this)
                    if (fingerprintManager != null && cryptoObject != null) {
                        helper.startAuth(fingerprintManager!!, cryptoObject!!)
                    }
                }
            }
        }
    }

    private fun generateKey(): Boolean {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get KeyGenerator instance" + e.message)
            return false
        }
    }

    override fun onEmpty() {
        Log.d(TAG, "onEmpty")
    }

    override fun onComplete(pin: String?) {
        // Check Correct
        val pw: String = WalletLocalService.getPw(this)
        if (pw == "" + pin) {
            setResult(Activity.RESULT_OK, Intent())
            finish()
        } else {
            pin_lock_view.resetPinLockView()
            Toast.makeText(this, R.string.txt_msg_pw_not_match_again,
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {
//        Log.d(TAG, "onPinChange " + intermediatePin)
    }

    private fun cipherInit(): Boolean {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            keyStore?.load(null)
            val key = keyStore?.getKey(KEY_NAME, null) as SecretKey
            cipher?.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun onBackPressed() {
        if (backCount==0) {
            Toast.makeText(this, R.string.txt_msg_back_again, Toast.LENGTH_SHORT).show()
            backCount = 1
            Handler().postDelayed({
                backCount = 0
            }, 1500)
        } else if (backCount==1) {
            https@ //wallet.cellpinda.com/q/public/app/?c=r&uid=10&key=smqiqd97tij1lopm1ms0v8hpc1 (145m
            backCount = 0
            WalletLocalService.finishApp(this)
        }
    }

}