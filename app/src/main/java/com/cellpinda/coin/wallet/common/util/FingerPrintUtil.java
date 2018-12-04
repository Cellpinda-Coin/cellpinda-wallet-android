package com.cellpinda.coin.wallet.common.util;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * 지문 인식 Util
 * @author 나비이쁜이
 * @since 2018.03.07
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerPrintUtil {

    /**
     * Static 변수
     **************************************************************************************************************************************/
    private static final String KeyName = "yourKeyPassword";
    private static final String AndroidKeyStore = "AndroidKeyStore";

    /**
     * 생성자 변수
     **************************************************************************************************************************************/
    private Context mContext;
    private FingerprintManager fingerprintManager;
    private FingerprintManager.CryptoObject cryptoObject;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;

    /**
     * 생성자
     **************************************************************************************************************************************/
    public FingerPrintUtil(Context mContext) {
        this.mContext = mContext;
        setKeyStore();
    }

    /**
     * FingerPrintUtil callback 초기화
     **************************************************************************************************************************************/
    public void initalize(FingerprintManager.AuthenticationCallback callback) {
        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, callback, null);
    }

    /**
     * 하드웨어 지원 가능 여부
     **************************************************************************************************************************************/
    public boolean isFingerHardWare() {
        return fingerprintManager.isHardwareDetected();
    }

    /**
     * 하드웨어 현재 지문 여부
     **************************************************************************************************************************************/
    public boolean isFingerPassCode() {
        return fingerprintManager.hasEnrolledFingerprints();
    }

    /**
     * KeyStore Setting
     **************************************************************************************************************************************/
    private void setKeyStore() {
        // FingerPrint Manager Setting
        fingerprintManager = (FingerprintManager) mContext.getSystemService
                (Context.FINGERPRINT_SERVICE);
        cryptoObject = new FingerprintManager.CryptoObject(cipher);

        // keyStore
        try {
            keyStore = KeyStore.getInstance(AndroidKeyStore);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // keyGenerator
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES
                    , AndroidKeyStore);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // keyGenerator initalize
        try {
            keyStore.load(null);

            keyGenerator.init(new KeyGenParameterSpec.Builder(KeyName
                    , KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            keyGenerator.generateKey();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // cipher initalize
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                    + "/" + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // cipher set Key
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KeyName, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (KeyPermanentlyInvalidatedException e) {
            e.printStackTrace();
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

}
