package com.cellpinda.coin.wallet.common.abst

import android.Manifest
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cellpinda.coin.wallet.BuildConfig
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.constant.REQ_CODE_NETWORK
import com.cellpinda.coin.wallet.common.util.ConnectivityInterceptor
import com.cellpinda.coin.wallet.common.util.MobileUtil
import com.cellpinda.coin.wallet.common.util.PermissionCheckListener
import com.cellpinda.coin.wallet.service.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * Created by https://github.com/method76 on 2017-10-30.
 */
abstract class MethodActivity : AppCompatActivity() {

    lateinit var app: MethodApplication
    val PERMISSIONS_18 = listOf (
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
    )
    val PERMISSIONS_28 = listOf (
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.USE_BIOMETRIC
    )
    private val TAG: String = "MethodActivity"
    private lateinit var retrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = MethodApplication.getInstance()
        val listener = object : PermissionCheckListener {
            override fun onPermissionIsOk() {
                Log.d(TAG, "onPermissionIsOk")
            }
            override fun onPermissionIsNotOk() {
//                showPermissionAggrement()
                Log.d(TAG, "onPermissionIsNotOk")
                ActivityCompat.requestPermissions(this@MethodActivity,
                        arrayOf(Manifest.permission.ACCESS_NETWORK_STATE),
                        REQ_CODE_NETWORK)
            }
        }
        if (savedInstanceState==null) {
            Log.d(TAG, "Checking Permissions")
            MobileUtil.checkPermissionsRequired(this,
                Manifest.permission.ACCESS_NETWORK_STATE, listener)
        }
    }

    fun showPermissionAggrement() {
        SweetAlertDialog(this@MethodActivity,
                SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.txt_msg_perm_agree))
                .setConfirmText(getString(R.string.txt_btn_ok))
                .setCancelText(getString(R.string.txt_btn_close))
                .showCancelButton(true)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }.show()
    }

    fun walletService(): WalletAPIService? {
        try {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(ConnectivityInterceptor(this))
                    .build()
            return Retrofit.Builder().baseUrl(BuildConfig.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client).build().create(WalletAPIService::class.java)
        } catch (e : Exception) {
            Log.e(TAG, "NoConnectivityException " + e.message)
            Toast.makeText(this, R.string.txt_msg_check_internet, Toast.LENGTH_LONG).show()
            return null
        }
    }

    fun recapService(): RecapchaAPIService? {
        try {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(ConnectivityInterceptor(this))
                    .build()
            return Retrofit.Builder().baseUrl(BuildConfig.RECAP_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client).build().create(RecapchaAPIService::class.java)
        } catch (e : Exception) {
            Log.e(TAG, "NoConnectivityException " + e.message)
            Toast.makeText(this, R.string.txt_msg_check_internet, Toast.LENGTH_LONG).show()
            return null
        }
    }

    fun tossService(): TossButtonAPIService? {
        try {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(ConnectivityInterceptor(this))
                    .build()
            return Retrofit.Builder().baseUrl(BuildConfig.TOSS_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client).build().create(TossButtonAPIService::class.java)
        } catch (e : Exception) {
            Log.e(TAG, "NoConnectivityException " + e.message)
            Toast.makeText(this, R.string.txt_msg_check_internet, Toast.LENGTH_LONG).show()
            return null
        }
    }

    /**
     * SocketTimeoutException
     */
    fun etherScanService(): EtherScanAPIService? {
        val cacheSize = (5 * 1024 * 1024).toLong()
        val myCache = Cache(this.cacheDir, cacheSize)
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        try {
            val client = OkHttpClient.Builder()
                .cache(myCache)
                .addInterceptor(interceptor)
                .addInterceptor(ConnectivityInterceptor(this))
                .addInterceptor { chain ->
                    val request = if (MobileUtil.isOnline(this)!!) {
                        chain.request().newBuilder().header("Cache-Control", "public, max-age=" + 60).build()
                    } else {
                        chain.request().newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build()
                    }
                    chain.proceed(request)
                }
                .build()
            return Retrofit.Builder()
                    .baseUrl(BuildConfig.ETHERSCAN_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client).build().create(EtherScanAPIService::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, R.string.txt_msg_check_internet, Toast.LENGTH_LONG).show()
            return null
        }
    }

    /**
     *
     */
    fun cmCapService(): CoinmarketCapAPIService? {
        try {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(ConnectivityInterceptor(this))
                    .addInterceptor { chain ->
                        val request = chain.request()
                        request.newBuilder().header("Cache-Control", "public, max-age=" + (60 * 60 * 3)).build()
                        chain.proceed(request)
                    }.build()
            return Retrofit.Builder().baseUrl(BuildConfig.CMCAP_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client).build().create(CoinmarketCapAPIService::class.java)
        } catch (e : Exception) {
            Log.e(TAG, "NoConnectivityException " + e.message)
            Toast.makeText(this, R.string.txt_msg_check_internet, Toast.LENGTH_LONG).show()
            return null
        }
    }

}