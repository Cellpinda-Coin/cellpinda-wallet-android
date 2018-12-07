package com.cellpinda.coin.wallet.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.util.Base64
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cellpinda.coin.wallet.ChangePwActivity
import com.cellpinda.coin.wallet.FirstActivity
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.abst.MethodApplication.Companion.app
import com.cellpinda.coin.wallet.common.constant.*
import com.cellpinda.coin.wallet.common.util.LocalStorage
import com.cellpinda.coin.wallet.common.util.MobileUtil
import com.cellpinda.coin.wallet.data.param.Account
import com.cellpinda.coin.wallet.data.res.LoginVeriRes
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService.getInstance
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback
import java.security.MessageDigest
import java.util.*

/**
 * Created by https://github.com/method76 on 2017-11-08.
 */
class WalletLocalService {

    companion object {

        var TAG: String = "WalletLocalService"

        fun isFirstRun(ctx: Context): Boolean {
            var addr: String = LocalStorage.get(ctx, KEY_MY_ADDR)
            var pw: String   = LocalStorage.get(ctx, KEY_USER_P)
            if (addr.isNullOrBlank() || pw.isNullOrBlank()) {
                LocalStorage.clear(ctx)
                return true
            }
            return false
        }

        fun goToFirstAccount(ctx: Activity) {
            val intent = Intent(ctx, FirstActivity::class.java)
            ctx.startActivity(intent)
            ctx.finish()
        }

        fun goToNewAccount(ctx: Activity) {
            val intent = Intent(ctx, ChangePwActivity::class.java)
            intent.putExtra(PARAM_NEW_ACC, true)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            ctx.startActivity(intent)
            ctx.finish()
        }

        fun goToMain(ctx: Activity) {
            val intent = Intent(ctx, MainActivity::class.java)
            ctx.startActivity(intent)
            ctx.finish()
        }

        fun finishApp(ctx: Activity) {
            app.locked = false
            LocalStorage.set(ctx, "lastActiveTime", 0L)
            ActivityCompat.finishAffinity(ctx)
        }

        fun hasAccount(ctx: Context): Boolean {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val a: String = pref.getString(KEY_UID,        "")
            val b: String = pref.getString(KEY_MY_ADDR,    "")
            val c: String = pref.getString(KEY_USER_P,     "")
            val d: String = pref.getString(KEY_USER_NAME,  "")
            return a.isNotEmpty() && b.isNotEmpty() && c.isNotEmpty() && d.isNotEmpty()
        }

        fun saveAccount(ctx: Context, e: LoginVeriRes, p: String, id: String) {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(KEY_ID,        id)
            editor.putString(KEY_UID,       "" + e.uid)
            editor.putString(KEY_MY_ADDR,   e.address)
            editor.putInt(KEY_LEVEL,        e.level)
            editor.putString(KEY_USER_P,    p)
            editor.putString(KEY_USER_NAME, e.name)
            editor.putString(KEY_KEY,       e.key)
            editor.apply()
        }

        fun saveFcmToken(ctx: Context, token: String) {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString("token", token)
            editor.apply()
        }

        fun loadFcmToken(ctx: Context): String {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            return pref.getString("token", "")
        }

        fun loadAccount(ctx: Context): Account? {
            if (!hasAccount(ctx)) {
                Log.e(TAG, "userinfo no account exists")
                return null
            }
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val a: String = pref.getString(KEY_ID,         "")
            val b: String = pref.getString(KEY_UID,        "")
            val c: String = pref.getString(KEY_MY_ADDR,    "")
            val d: String = pref.getString(KEY_USER_P,     "")
            val e: String = pref.getString(KEY_USER_NAME,  "")
            val f: String = pref.getString(KEY_KEY,        "")
            val g: Int    = pref.getInt(KEY_LEVEL,         0)
            val ret = Account(a, b, c, d, e, f, g)
//            Log.i(TAG, "userinfo loaded " + ret.toString())
            return ret
        }

        fun setEthKrwVal(ctx: Context, ethKrwVal: Int) {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putInt(KEY_ETH_KRW, ethKrwVal)
            editor.apply()
        }

        fun ethKrwVal(ctx: Context): Int {

            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            return pref.getInt(KEY_ETH_KRW, 0)
        }

        fun shareAddress(ctx: Context, addr: String) {
            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ctx.getString(R.string.txt_recv_addr))
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, addr)
            ctx.startActivity(Intent.createChooser(sharingIntent, ctx.getString(R.string.txt_export_addr)))
        }

        fun getMyAddress(ctx: Context): String {
            return LocalStorage.get(ctx, KEY_MY_ADDR)
        }

        fun getPw(ctx: Context): String {
            return LocalStorage.get(ctx, KEY_USER_P)
        }

        fun savePW(ctx: Context, pw: String) {
            LocalStorage.set(ctx, KEY_USER_P, pw)
        }

        fun hasPw(ctx: Context): Boolean {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val a: String = pref.getString(KEY_USER_P, "")
            return a.isNotEmpty()
        }

        fun logout(ctx: Activity) {
            app.locked = false
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.clear()
            editor.apply()
        }

        fun warn(ctx: Activity, resId: Int) {
            ctx.runOnUiThread {
                val diag = SweetAlertDialog(ctx, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(ctx.getString(resId))
                        .setConfirmText(ctx.getString(R.string.txt_btn_ok))
                        .showNeutralButton(false)
                        .showCancelButton(false)
                        .setConfirmClickListener { sDialog ->
                            sDialog.dismissWithAnimation()
                        }
                diag.show()
                Handler().postDelayed({
                    diag.dismissWithAnimation()
                }, 3500)
            }
        }

        fun alert(ctx: Activity, resId: Int) {
            ctx.runOnUiThread {
                val diag = SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(ctx.getString(resId))
                        .setConfirmText(ctx.getString(R.string.txt_btn_ok))
                        .showNeutralButton(false)
                        .showCancelButton(false)
                        .setConfirmClickListener { sDialog ->
                            sDialog.dismissWithAnimation()
                        }
                diag.show()
                Handler().postDelayed({
                    diag.dismissWithAnimation()
                }, 3500)
            }
        }

        fun warnAutoHide(ctx: Activity, resId: Int) {
            ctx.runOnUiThread {
                val diag = SweetAlertDialog(ctx, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(ctx.getString(resId))
                        .setConfirmText(ctx.getString(R.string.txt_btn_ok))
                        .showNeutralButton(false)
                        .showCancelButton(false)
                        .setConfirmClickListener { sDialog ->
                            sDialog.dismissWithAnimation()
                        }
                diag.show()
                Handler().postDelayed({
                    diag.dismissWithAnimation()
                }, 3500)
            }
        }

        fun successAutoHide(ctx: Activity, resId: Int) {
            ctx.runOnUiThread {
                val diag = SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                        .setTitleText(ctx.getString(resId))
                        .setConfirmText(ctx.getString(R.string.txt_btn_ok))
                        .showNeutralButton(false)
                        .showCancelButton(false)
                        .setConfirmClickListener { sDialog ->
                            sDialog.dismissWithAnimation()
                        }
                try {
                    if (diag!=null) {
                        diag.show()
                        Handler().postDelayed({
                            diag.dismissWithAnimation()
                        }, 3500)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, e.message)
                }
            }
        }

        fun alert(ctx: Activity, titleResId: Int, contentResId: Int) {
            val diag = SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(ctx.getString(titleResId))
                    .setContentText(ctx.getString(contentResId))
                    .setConfirmText(ctx.getString(R.string.txt_btn_ok))
                    .showNeutralButton(false)
                    .showCancelButton(false)
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                    }
            diag.show()
            Handler().postDelayed({
                diag.dismissWithAnimation()
            }, 3500)
        }

        fun registerToken(ctx: Activity, addr: String) {
            val t = WalletLocalService.loadFcmToken(ctx.applicationContext)
            if (t!="") {
                val db = FirebaseFirestore.getInstance()
                val myinfo = db.document("device-token/" + addr)
                // Create a new user with a first and last name
                val deviceInfo = HashMap<String, Any>()
                deviceInfo["token"] = t
                // Add a new document with a generated ID
                myinfo.set(deviceInfo)
                        .addOnSuccessListener {
                            Log.d(TAG, "firestore doc added with ID: ")
                        }.addOnFailureListener { e -> Log.w(TAG, "firestore error adding document", e) }
            }
        }

        fun lastSessionTime(ctx: Context): Long {
            return LocalStorage.getLong(ctx, "lastActiveTime")
        }

        fun setLastSessionTime(ctx: Context) {
            LocalStorage.set(ctx, "lastActiveTime", Calendar.getInstance().timeInMillis)
        }

        fun clearLastSessionTime(ctx: Context) {
            LocalStorage.set(ctx, "lastActiveTime", 0L)
        }

        fun sendRecommend(ctx: Context) {
            // 커스터마이즈된 템플릿과 템플릿을 채울 args들을 사용하여 보내는 코드
            val acc = loadAccount(ctx)
            val templateId = "13432"
            val templateArgs = HashMap<String, String>()
            templateArgs["url"] = "?apn=com.cellpinda.coin.wallet&link=https://wallet.cellpinda.com/i/" + acc!!.id
            // <?=$_SESSION[name]?>님이 추천합니다.
            templateArgs["recommend"] = acc!!.name + "님이 추천합니다."
            val serverCallbackArgs = HashMap<String, String>()
            serverCallbackArgs["user_id"] = "\${current_user_id}"
            serverCallbackArgs["product_id"] = "\${shared_product_id}"
            getInstance().sendScrap(ctx, "https://cellpinda.page.link", templateId,
                    templateArgs, serverCallbackArgs, object : ResponseCallback<KakaoLinkResponse>() {
                override fun onFailure(errorResult: ErrorResult) {
                    Log.e(TAG, errorResult.toString())
                }
                override fun onSuccess(result: KakaoLinkResponse) {
                    // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                }
            })
        }

        fun printHash(ctx: Context) {
            try {
                val info = ctx.getPackageManager().getPackageInfo(ctx.packageName, PackageManager.GET_SIGNATURES)
                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val hashKey = String(Base64.encode(md.digest(), 0))
                    Log.i("AppLog", "key:$hashKey=")
                }
            } catch (e: Exception) {
                Log.e("AppLog", "error:", e)
                e.printStackTrace()
            }

        }

        fun goBand(ctx: Context) {
            MobileUtil.openBrowser(ctx, "https://band.us/band/71232114")
        }

        fun goCafe(ctx: Context) {
            MobileUtil.openBrowser(ctx, "https://cafe.naver.com/cellpinda")
        }

        fun goKakakoOpen(ctx: Context) {
            MobileUtil.openBrowser(ctx, "http://coin.cellpinda.com/community/")
        }

    }

}