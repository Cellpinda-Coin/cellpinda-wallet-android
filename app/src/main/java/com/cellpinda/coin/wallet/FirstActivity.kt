package com.cellpinda.coin.wallet

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsMessage
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cellpinda.coin.wallet.common.abst.MethodActivity
import com.cellpinda.coin.wallet.common.constant.*
import com.cellpinda.coin.wallet.common.util.ManagePermissions
import com.cellpinda.coin.wallet.common.util.MobileUtil
import com.cellpinda.coin.wallet.data.res.CreateAccountRes
import com.cellpinda.coin.wallet.data.res.LoginVeriRes
import com.cellpinda.coin.wallet.frag.CreateFragment
import com.cellpinda.coin.wallet.frag.FirstChooseFragment
import com.cellpinda.coin.wallet.frag.RestoreFragment
import com.cellpinda.coin.wallet.service.WalletLocalService
import com.cellpinda.coin.wallet.service.WalletLocalService.Companion.loadAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.safetynet.SafetyNet
import kotlinx.android.synthetic.main.activity_first.*
import retrofit2.Call

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class FirstActivity : MethodActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private val TAG: String = this.javaClass.simpleName
    private val REQ_CODE_PERMS      = 10000
    private val REQ_CODE_CREATE_PIN = 10001
    private val ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"
    private lateinit var createFrag: CreateFragment
    private lateinit var restoreFrag: RestoreFragment
    private lateinit var managePermissions: ManagePermissions
    private lateinit var smsReceiver: BroadcastReceiver
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var isOnline: Boolean = false
    private var backCount:Int = 0
    private val POSITION_CREATE = 1
    private val POSITION_RESTORE = 2
    private var fragPos:Int = 0
    private var recaptchaCalled = false
    private var user: LoginVeriRes? = null
    private var m: String? = null
    var recomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            managePermissions = ManagePermissions(this,
                    super.PERMISSIONS_28, REQ_CODE_PERMS)
        } else {
            managePermissions = ManagePermissions(this,
                    super.PERMISSIONS_18, REQ_CODE_PERMS)
        }
        initUi()

        if (WalletLocalService.hasAccount(this)) {
            Log.i(TAG, "has account")
            // 어카운트 삭제 필요할 듯
            Handler().postDelayed({
                mainViewWithExistingAccount()
            }, 3500)
            return
        } else {
            // if there is no 처음 계정설정 화면
            Log.i(TAG, "no account")
            Handler().postDelayed({
                // 지갑복구 혹은 새지갑 생성 화면
                moveLogoToTop()
            }, 2000)
        }
    }

    private fun initUi() {
        iv_bg_lab.visibility = View.INVISIBLE
        isOnline = MobileUtil.isOnline(this)
        if (!isOnline) {
            MobileUtil.showNetworkErrorMsg(this)
        } else {
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addApi(SafetyNet.API)
                    .addConnectionCallbacks(this@FirstActivity)
                    .addOnConnectionFailedListener(this@FirstActivity)
                    .build()
            mGoogleApiClient.connect()
        }
    }

    fun showProgress3Sec() {
        try {
            spinner.visibility = View.VISIBLE
            Handler().postDelayed({
                if (spinner!=null) {
                    spinner.visibility = View.GONE
                }
            }, 3500)
        } catch(e: Exception) {
            Log.w(TAG, "" + e.message)
        }
    }

    private fun moveLogoToTop() {
        Log.i(TAG, "moveLogoToTop")
        val logoMoving = AnimationUtils.loadAnimation(this, R.anim.logo_move_top)
        val show = AnimationUtils.loadAnimation(this, R.anim.alpha_show)
        val hide = AnimationUtils.loadAnimation(this, R.anim.alpha_hide)
        logoMoving.fillAfter = true
        show.fillAfter       = true
        hide.fillAfter       = true
        iv_bg_lab.visibility = View.VISIBLE
        ll_logo_group.startAnimation(logoMoving)
        iv_bg_lab.startAnimation(show)
//        iv_bg_splash.startAnimation(hide)
        Handler().postDelayed({
            if (intent!=null && intent.data!=null) {
                // 추천인가입: 추천인 ID
                val uri = intent.data
//                val scheme = uri.host + " " + uri.path + " " + uri.query
                if (uri != null) {
                    Log.d(TAG, "uri " + uri.toString())
                    recomId = uri.path.split("/")[2]
                    Log.d(TAG, "recom " + recomId)
                    newWalletView()
                } else {
                    chooseView()
                }
            } else {
                chooseView()
            }
        }, 750)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            managePermissions.checkPermissions()
        }

    }

    fun callRecaptcha(mobileno: String, passcode: String) {
        SafetyNet.getClient(this).verifyWithRecaptcha("6LdSAXkUAAAAAKlRVsajZOklm4KCNh4wO335If-r")
                .addOnSuccessListener { response ->
                    if (!response.tokenResult.isEmpty()) {
                        recaptchaCalled = true
                         reqLogin(mobileno, passcode, response.tokenResult)
                    } else {
                        Log.d(TAG, "Recap has NO token result!")
                    }
                }.addOnFailureListener { e ->
                        if (e is ApiException) {
                            val apiException: ApiException = e
                            Log.e(TAG, "Recap Error message: " +
                                    CommonStatusCodes.getStatusCodeString(apiException.statusCode))
                        } else {
                            Log.e(TAG, "Recap Unknown type of error: " + e.message)
                        }
                    }
    }

    fun inputMobileNo(): Boolean {
        val diag: SweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.txt_msg_mobile_no_empty))
                .setConfirmText(getString(R.string.txt_btn_confirm))
                .showCancelButton(false)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }
        diag.show()
        Handler().postDelayed({
            diag.dismissWithAnimation()
        }, 3000)
        return false
    }

    fun inputPasscode(): Boolean {
        val diag: SweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.txt_msg_passcode_null_or_short))
                .setConfirmText(getString(R.string.txt_btn_confirm))
                .showCancelButton(false)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }
        diag.show()
        Handler().postDelayed({
            diag.dismissWithAnimation()
        }, 3000)
        return false
    }

    fun mobileNoIsShort(): Boolean {
        val diag: SweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.txt_msg_mobile_no_short))
                .setConfirmText(getString(R.string.txt_btn_confirm))
                .showCancelButton(false)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }
        diag.show()
        Handler().postDelayed({
            diag.dismissWithAnimation()
        }, 3000)
        return false
    }

    fun inputUserName(): Boolean {
        val diag: SweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.txt_msg_input_name))
                .setConfirmText(getString(R.string.txt_btn_confirm))
                .showCancelButton(false)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }
        diag.show()
        Handler().postDelayed({
            diag.dismissWithAnimation()
        }, 3000)
        return false
    }

    fun userNameIsShort(): Boolean {
        val diag: SweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.txt_msg_name_len))
                .setConfirmText(getString(R.string.txt_btn_confirm))
                .showCancelButton(false)
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }
        diag.show()
        Handler().postDelayed({
            diag.dismissWithAnimation()
        }, 3000)
        return false
    }

    /**
      * https://wallet.cellpinda.com/q/public/app/?c=j&phone=01048187321&name=홍길동&seq=인증index&recommend=추천자분 아이디
      * 잠금 PIN 세팅하고 메인 페이지로?
      */
    fun reqCreateAccount(mobileno: String, name: String, num: String, seq: String) {
        val restService: Call<CreateAccountRes> = walletService()!!.createWallet("j"
                , mobileno, name, seq, num, recomId)
        user = null
        m    = null
        val call: retrofit2.Response<CreateAccountRes> = restService.execute()
        if (call.isSuccessful) {
            val res: CreateAccountRes? = call.body()
            if (res!!.i==SUCCESS_RES) { // Todo: i => e
                // 메시지: 계정이 생성되었습니다. 로그인 해주세요
                WalletLocalService.successAutoHide(this, R.string.txt_msg_create_success)
                restoreWalletView()
            } else {
                // 미인증 가입요청입니다
                failAuthentification()
            }
        }
    }

    /**
     * 로그인 인증과 후처리
     */
    private fun reqLogin(mobileno: String, passcode: String, tokenResult: String) {
        val restService: Call<LoginVeriRes> = walletService()!!.veriLogin("l", mobileno
                , passcode, tokenResult)
        Thread {
            user = null
            m    = null
            val call1: retrofit2.Response<LoginVeriRes> = restService.execute()
            if (call1.isSuccessful) {
                val res: LoginVeriRes? = call1.body()
                if (res!!.e==0) {
                    user = res
                    m = mobileno
                    mainViewViaLogin(res, mobileno)
                } else {
                    failAuthentification()
                }
            }
        }.start()
    }

    fun failAuthentification() {
        WalletLocalService.warn(this, R.string.txt_msg_no_auth)
    }

    fun newWalletView() {
        fragPos = 1
        createFrag = CreateFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, createFrag).commit()
    }

    fun restoreWalletView() {
        fragPos = 2
        restoreFrag = RestoreFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RestoreFragment()).commit()
    }

    fun chooseView() {
        fragPos = 0
        try {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, FirstChooseFragment()).commit()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun mainViewWithExistingAccount() {
        val main = Intent(this, MainActivity::class.java)
        val e = loadAccount(this)
        if (e!=null) {
            main.putExtra(KEY_UID,       e.uid)
            main.putExtra(KEY_MY_ADDR,   e.address)
            main.putExtra(KEY_USER_P,    e.p)
            main.putExtra(KEY_USER_NAME, e.name)
            main.putExtra(KEY_KEY,       e.key)
            startActivity(main)
            finish()
        } else {
            // Todo: 메시지 처리
        }
    }

    /**
     * 로그인 인증이 성공하면
     *  => 핀번호 세팅
     */
    private fun mainViewViaLogin(e: LoginVeriRes, m: String) {
        val intent = Intent(this, ChangePwActivity::class.java)
        intent.putExtra(PARAM_NEW_ACC, true)
        startActivityForResult(intent, REQ_CODE_CREATE_PIN)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQ_CODE_PERMS -> {
                val isPermissionsGranted = managePermissions
                        .processPermissionsResult(requestCode,permissions,grantResults)
                if(!isPermissionsGranted){
                    toast(R.string.txt_msg_perm_denied)
                    WalletLocalService.finishApp(this)
                }
                return
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnected(p0: Bundle?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onConnectionSuspended(p0: Int) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun Context.toast(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun Context.toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(ACTION_SMS_RECEIVED)
        // SMS 리시버 초기화
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            smsReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val bundle: Bundle = intent.extras
                    if (!bundle.isEmpty) {
                        // SMS 수신된 내용이 있으면
                        val sms = bundle.get("pdus") as Array<ByteArray>
                        (0 until sms.size).forEach { i ->
                            val format = bundle.getString("format")
                            val smsMessage = SmsMessage.createFromPdu(sms[i], format)
                            val smsBody = smsMessage.messageBody.toString()
                            val address = smsMessage.originatingAddress
                            Log.i(TAG, "smsBody " + smsBody + " address " + address)
                            try {
                                var veriNo = ""
                                if (smsBody.contains("Cellpinda Wallet") && smsBody.contains("인증번호[")) {
                                    // if has "Cellpinda Wallet" && "인증번호[ "
                                    val str = smsBody.split("인증번호[ ")[1]
                                    veriNo = str.substring(0, smsBody.indexOf("]"))
                                    if (veriNo.length==6) {
                                        createFrag.setSmsVeriCode(veriNo)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                }
            }
            this.registerReceiver(smsReceiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            unregisterReceiver(smsReceiver)
        }
    }

    override fun onBackPressed() {
        if (fragPos>0) {
            chooseView()
        } else {
            if (backCount==0) {
                Toast.makeText(this, R.string.txt_msg_back_again, Toast.LENGTH_SHORT).show()
                backCount = 1
                Handler().postDelayed({
                    backCount = 0
                }, 1500)
            } else if (backCount==1) {
                backCount = 0
                super.onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode== REQ_CODE_CREATE_PIN && resultCode== Activity.RESULT_OK) {
            if (user==null || m==null) {
                super.onActivityResult(requestCode, resultCode, data)
            } else {
                // PIN 번호 설정 후 리턴 시
                val pin = data!!.getStringExtra("pin")
                Log.i(TAG, "pin " + pin)
//                val p = m!!.substring(m!!.length - 4, m!!.length)
                WalletLocalService.saveAccount(this, user!!, pin, m!!)
                val main = Intent(this, MainActivity::class.java)
                main.putExtra("intent", "restore")
                main.putExtra(KEY_UID,       user!!.uid)
                main.putExtra(KEY_MY_ADDR,   user!!.address)
                main.putExtra(KEY_USER_P,    pin)
                main.putExtra(KEY_USER_NAME, user!!.name)
                startActivity(main)
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Calls Google's site verify API
     */
//    fun siteVerify(mobileno: String, passcode: String, tokenResult: String) {
//        val param = RecaptchaReq(mobileno, passcode, tokenResult)
//        val restService: Call<RecaptchaRes> = recapService()!!.siteverify(param)
//        val call1: retrofit2.Response<RecaptchaRes> = restService.execute()
//        if (call1.isSuccessful) {
//            val res: RecaptchaRes? = call1.body()
//            if (res!!.success) {
//                Thread {
//                    reqLogin()
//                }.start()
//                mainViewWithExistingAccount()
//            }
//        }

}