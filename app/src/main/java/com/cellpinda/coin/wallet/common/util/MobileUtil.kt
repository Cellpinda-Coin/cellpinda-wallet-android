package com.cellpinda.coin.wallet.common.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.constant.IMG_NAME_QR
import com.cellpinda.coin.wallet.common.constant.REQ_CODE_NETWORK
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter


/**
 * Created by https://github.com/method76 on 2017-11-30.
 */
class MobileUtil {

    companion object {

        val TAG:String = "MobileUtil"

        fun checkPermissionsRequired(ctx: Activity, PERMISSIONS_REQUIRED: String,
                                     listener: PermissionCheckListener) {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                if (ContextCompat.checkSelfPermission(ctx, PERMISSIONS_REQUIRED)
                        == PackageManager.PERMISSION_GRANTED) {
                    listener.onPermissionIsOk()
                } else {
                    ActivityCompat.requestPermissions(ctx, arrayOf(PERMISSIONS_REQUIRED), REQ_CODE_NETWORK)
                    listener.onPermissionIsNotOk()
                }
            } else {
                listener.onPermissionIsOk()
            }
        }

        fun isOnline(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }

        fun showNetworkErrorMsg(ctx: Activity) {
            ctx.runOnUiThread {
                Toast.makeText(ctx, R.string.txt_msg_check_internet, Toast.LENGTH_SHORT).show()
            }
        }

        fun getMobileNumber(ctx: AppCompatActivity): String {
            val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                var no = tm.line1Number
                if (no==null) {
                    return ""
                } else if (no.startsWith("+821")) {
                    no = no.replace("+821", "01")
                } else if (no.startsWith("+8201")) {
                    no = no.replace("+8201", "01")
                }
                return no
            } catch (e: SecurityException) {
                Log.w(TAG, "could not get mobile no")
                return ""
            }
        }

        fun getRawMobileNumber(ctx: AppCompatActivity): String {
            val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                return tm.line1Number
            } catch (e: SecurityException) {
                Log.w(TAG, "could not get mobile no")
                return ""
            }
        }

        fun saveQRImage(ctx: Context, addr: String) {
            Thread {
                val writer = QRCodeWriter()
                try {
                    // generate a 150x150 QR code 196dp
                    val bitMatrix = writer.encode(addr, BarcodeFormat.QR_CODE, 784, 784)
                    val width = bitMatrix.width
                    val height = bitMatrix.height
                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    for (x in 0 until width) {
                        for (y in 0 until height) {
                            bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                        }
                    }
                    if (bmp != null) {
                        ImageSaver(ctx).setFileName(IMG_NAME_QR).setDirectoryName("images").setExternal(false).save(bmp)
                    }
                } catch(e: Exception) { e.printStackTrace() }
            }.start()
        }

        fun isFingerprintAuthAvailable(context: Context): Boolean {
            val ver = Build.VERSION.SDK_INT > 22
            val hard = FingerprintManagerCompat.from(context)
                    .isHardwareDetected
            val has  = FingerprintManagerCompat.from(context)
                    .hasEnrolledFingerprints()
            return ver && hard && has
        }

        fun getFingerprintManagerCompat(context: Context): FingerprintManagerCompat {
            return FingerprintManagerCompat.from(context)
        }

        fun openBrowser(ctx: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            ctx.startActivity(intent)
        }

    }

}