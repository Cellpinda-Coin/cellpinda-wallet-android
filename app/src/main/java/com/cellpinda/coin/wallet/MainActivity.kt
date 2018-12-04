package com.cellpinda.coin.wallet

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cellpinda.coin.wallet.common.abst.MethodApplication
import com.cellpinda.coin.wallet.common.abst.MethodSessionActivity
import com.cellpinda.coin.wallet.common.constant.*
import com.cellpinda.coin.wallet.common.util.MobileUtil
import com.cellpinda.coin.wallet.common.util.WalletUtil
import com.cellpinda.coin.wallet.data.param.Account
import com.cellpinda.coin.wallet.data.param.ButtonLinkGenReq
import com.cellpinda.coin.wallet.data.res.ButtonLinkGenRes
import com.cellpinda.coin.wallet.data.res.CPDBuyRes
import com.cellpinda.coin.wallet.data.res.SalesMainData
import com.cellpinda.coin.wallet.frag.NewsFragment
import com.cellpinda.coin.wallet.frag.ReceiveFragment
import com.cellpinda.coin.wallet.frag.SalesFragment
import com.cellpinda.coin.wallet.frag.TxsFragment
import com.cellpinda.coin.wallet.service.WalletLocalService
import com.cellpinda.coin.wallet.view.BuyViaEthDialog
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_toolbar.*
import retrofit2.Call

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class MainActivity : MethodSessionActivity() {

    private val TAG: String = this.javaClass.simpleName
    lateinit var myAddr: String
    private var backCount:Int = 0
    private lateinit var scheme: String
    private lateinit var link: String
    private lateinit var toggle: ActionBarDrawerToggle
    private var purpose: String? = null
    private var acc: Account? = null
    private var isFabOpen: Boolean = false
    private lateinit var fab_open: Animation
    private lateinit var fab_close: Animation
    var salesData: SalesMainData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.itemIconTintList = null

        /*
        iv_qr.setOnClickListener {
            someActivityOnTop = true
            WalletUtil.scanQR(this)
        }
        */
        WalletLocalService.printHash(this)
        acc = WalletLocalService.loadAccount(this)
        if (acc==null) {
            // 계정 정보가 없음 => Toast 처리하고 종료
            Toast.makeText(this, R.string.txt_msg_no_acc_finish, Toast.LENGTH_LONG).show()
            WalletLocalService.finishApp(this)
            return
        }

        myAddr = acc!!.address
        if (myAddr.isNotEmpty()) {
            // FCM 토큰 등록
            WalletLocalService.registerToken(this, myAddr)
        } else {
            WalletLocalService.finishApp(this)
            return
        }

        purpose = intent.getStringExtra("intent")
        if (!purpose.isNullOrEmpty()) {
            WalletLocalService.setLastSessionTime(this)
            // 추가적으로 주소하고 이더 싯가 GET
            if (purpose=="create" || purpose=="restore") {
                // 새 지갑 생성한 경우
                MobileUtil.saveQRImage(this, myAddr)
                WalletLocalService.successAutoHide(this, R.string.txt_msg_sms_success)
            }
        }

        setSupportActionBar(toolbar)
        setupDrawerToggle()
//        getButtonLink()
        right_drawer_contents.menu.findItem(R.id.drawer_item_5).title = (getString(R.string.txt_drawer_menu_5) + " "
                    + this.packageManager.getPackageInfo(this.packageName, 0).versionName)
        right_drawer_contents.setNavigationItemSelectedListener {
            //Check to see which item was being clicked and perform appropriate action
            when (it.itemId ) {
                R.id.drawer_item_1 -> {
                    // 비번변경
                    someActivityOnTop = true
                    startActivity(Intent(this, ChangePwActivity::class.java))
                    drawer_layout.closeDrawers()
                }
                R.id.drawer_item_2 -> {
                    // 내 주소록
//                    startActivity(Intent(this, ChangePwActivity::class.java))
//                    showFunctionNotReady()
                    someActivityOnTop = true
                    val first = Intent(this, ContactsActivity::class.java)
                    startActivity(first)
                }
                R.id.drawer_item_3 -> {
                    // 코인홈/기업홈
                    val diag: SweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(getString(R.string.txt_msg_go_where))
                            .setConfirmText(getString(R.string.txt_go_corp_home))
                            .setNeutralText(getString(R.string.txt_go_coin_home))
                            .setCancelText(getString(R.string.txt_btn_do_cancel))
                            .showCancelButton(true)
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                                someActivityOnTop = true
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.CORP_HOME_URL))
                                startActivity(browserIntent)
                            }.setNeutralClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                                someActivityOnTop = true
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.COIN_HOME_URL))
                                startActivity(browserIntent)
                            }.setCancelClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            }
                    diag.show()
                    drawer_layout.closeDrawers()
                }
                R.id.drawer_item_4 -> {
                    // 4. 메시지알림: +토글버튼
                    showFunctionNotReady()
                }
                R.id.drawer_item_6 -> {
                    // 5. 단톡방 연결 => https://open.kakao.com/o/gmUbb34
                    MobileUtil.openBrowser(this, "http://coin.cellpinda.com/community/")
                }
                // 5. 앱버전 => no action
                R.id.drawer_item_7 -> {
                    // 6. 로그아웃
                    val diag: SweetAlertDialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(getString(R.string.txt_msg_confirm_logout))
                            .setConfirmText(getString(R.string.txt_btn_confirm))
                            .setCancelText(getString(R.string.txt_btn_do_cancel))
                            .showCancelButton(true)
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                                WalletLocalService.logout(this)
                                val first = Intent(this, FirstActivity::class.java)
                                startActivity(first)
                                WalletLocalService.finishApp(this)
                                overridePendingTransition(R.anim.left_to_right, R.anim.to_right_out)
                            }
                    diag.show()
                }
            }
            true
        }
        footer_layout.setOnClickListener {
            drawer_layout.closeDrawers()
        }
        fab_open = AnimationUtils.loadAnimation(applicationContext,  R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(applicationContext, R.anim.fab_close);
        fab.setOnClickListener { anim() }
        fab1.setOnClickListener {
            anim()
            WalletLocalService.goCafe(this)
        }
        fab2.setOnClickListener {
            anim()
            WalletLocalService.goBand(this)
        }
        fab3.setOnClickListener {
            anim()
            WalletLocalService.goKakakoOpen(this)
        }

        if (intent.extras==null) {
            Log.i(WalletLocalService.TAG, "required params are empty")
            Toast.makeText(this, "required params are empty", Toast.LENGTH_LONG).show()
            WalletLocalService.finishApp(this)
            return
        }

        val txProfile = right_drawer_contents.getHeaderView(0)
                .findViewById<TextView>(R.id.tx_profile)
        val level = if (acc!!.level>0) { " " + getString(R.string.txt_cellin_level, acc!!.level) } else { "" }
        txProfile.text = acc!!.name + level

        if (purpose=="txs") {
            // 거래내역 변동 알림 푸시 받은 경우
            navigation.selectedItemId = R.id.menu_3
        } else {
            navigation.selectedItemId = R.id.menu_1
        }
    }

    private fun anim() {
        if (isFabOpen) {
            fab1.startAnimation(fab_close)
            fab2.startAnimation(fab_close)
            fab3.startAnimation(fab_close)
            fab1.isClickable = false
            fab2.isClickable = false
            fab3.isClickable = false
            isFabOpen = false
        } else {
            fab1.startAnimation(fab_open)
            fab2.startAnimation(fab_open)
            fab3.startAnimation(fab_open)
            fab1.isClickable = true
            fab2.isClickable = true
            fab3.isClickable = true
            isFabOpen = true
        }
    }

    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        toggle = ActionBarDrawerToggle(this, drawer_layout, null, R.string.txt_drawer_open, R.string.txt_drawer_close)
        toggle.isDrawerIndicatorEnabled = false
        btn_drawer_control.setOnClickListener { drawer_layout.openDrawer(GravityCompat.END) }
        drawer_layout.setScrimColor(resources.getColor(android.R.color.transparent))
        drawer_layout.addDrawerListener(toggle)
        return toggle
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState()
    }

    /**
     * QR 스캔 결과 처리
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode==IntentIntegrator.REQUEST_CODE && resultCode==Activity.RESULT_OK) {
            try {
                val result = IntentIntegrator.parseActivityResult(requestCode,
                        resultCode, data)
                if (result != null && result.contents != null) {
                    // QR 스캔 결과
//                    edt_to_addr.setText(result.contents)
                    return
                }
            } catch(e: Exception) {
                Log.e(TAG, "error in getting QR result " + e.message)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * 카메라 권한요청 결과
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_CODE_CAMERA -> {
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한 허가 했을때 해주어야 할 동작
                    WalletUtil.scanQR(this)
                } else {
                    // 권한을 거부했을때 해주어야 할 동작
                    finish()
                }
            }
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
            backCount = 0
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MethodApplication.app.locked = false
        WalletLocalService.clearLastSessionTime(this)
    }

    /**
     *
     */
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.menu_1 -> {
                // 프리세일
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, SalesFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_2 -> {
                // 입금/충전
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ReceiveFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_3 -> {
                // 입출금 내역
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, TxsFragment()).commit()
                // Todo: 1분에 한번 업데이트
                return@OnNavigationItemSelectedListener true
            }
            R.id.menu_4 -> {
                // 보상/추천
                showFunctionNotReady()
            }
            R.id.menu_5 -> {
                // 뉴스
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, NewsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun showFunctionNotReady() {
        Toast.makeText(this, R.string.txt_msg_function_not_ready, Toast.LENGTH_SHORT).show()
    }

    fun showBuyCpd() {
        if (salesData==null) { return }
        if (salesData!!.ethbal < ETH_MIN_LIMIT) {
//        if (false) {
            // 0.1ETH 이상 구매하실 수 있습니다
            WalletLocalService.warn(this, R.string.txt_msg_min_amt)
        } else {
            val ethmodAmt = salesData!!.ethbal - ETH_FEE_LIMIT
            // 결제금액 *ETH, 예상구매수량 *CPD
            val cpdAmt = WalletUtil.toCurrencyFormat8Int(ethmodAmt * salesData!!.ethval / 5)
//            val cpdAmt = "200"
            val contentText = getString(R.string.txt_msg_payment_desc, "" + ethmodAmt, "" + cpdAmt)
            // ※ 지불금액 *ETH, 예상구매수량 *CPD
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.txt_msg_all_amt_avail))
                    .setConfirmText(getString(R.string.txt_full_amount))
                    .setNeutralText(getString(R.string.txt_partial_amount))
                    .setCancelText(getString(R.string.txt_btn_do_cancel))
                    .setContentText(contentText)
                    .showNeutralButton(true)
                    .showCancelButton(true)
                    .setConfirmClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                        showLastBuyConfirm(ethmodAmt.toString(), cpdAmt)
                    }.setNeutralClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                        showPartialBuy()
                    }.setCancelClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                    }.show()
        }
    }

    fun showLastBuyConfirm(ethamt: String, cpdamt: String) {
        SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.txt_msg_eth_to_cpd_confirm, ethamt, cpdamt))
                .setConfirmText(getString(R.string.txt_req_buy))
                .showCancelButton(true)
                .setCancelText(getString(R.string.txt_btn_do_cancel))
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    buyTokenViaETH(cpdamt)
                }.setCancelClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }.show()
    }

    fun showPartialBuy() {
        BuyViaEthDialog(this).show()
    }

    fun buyTokenViaETH(cpdamt: String) {
        // c, uid, key, amt
        showProgress2Sec()
        val restService: Call<CPDBuyRes> = walletService()!!.buyTokenViaETH("buy", acc!!.uid, acc!!.key, cpdamt)
        Thread {
            try {
                val call1: retrofit2.Response<CPDBuyRes> = restService.execute()
                var res: CPDBuyRes? = null
                if (call1.isSuccessful) {
                    res = call1.body()
                    if (res!!.e == SUCCESS_RES) {
                        // 구매요청을 하였습니다. 구매완료까지는 10분 안팎의 시간이 필요하므로, 잠시 후 거래내역을 확인해주세요.
                        Log.i(TAG, "CPD buy request succcess " + res.orderId)
                        runOnUiThread {
                            WalletLocalService.alert(this, R.string.txt_msg_buy_req_success_title
                                        , R.string.txt_msg_buy_req_success_content)
                        }
                    } else if (res!!.e == SESS_EXPIRE_RES) { }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 서버와의 통신에 실패하였
            }
        }.start()
    }

    fun showProgress() {
        try {
            spinner.visibility = View.VISIBLE
        } catch(e: Exception) {
            Log.w(TAG, "" + e.message)
        }
    }

    fun showProgress2Sec() {
        try {
            spinner.visibility = View.VISIBLE
            Handler().postDelayed({
                if (spinner!=null) {
                    spinner.visibility = View.GONE
                }
            }, 2000)
        } catch(e: Exception) {
            Log.w(TAG, "" + e.message)
        }
    }

    fun hideProgress() {
        try {
            spinner.visibility = View.GONE
        } catch(e: Exception) {
            Log.w(TAG, "" + e.message)
        }
    }

    fun getButtonLink() {
        val param = ButtonLinkGenReq("0f197caf326847699749b6d287fa6c09"
            , "KB국민", "08370104009690", 100, "김성준XZ")
        val restService: Call<ButtonLinkGenRes> = tossService()!!.getButtonLink(param)
        Thread {
            val call: retrofit2.Response<ButtonLinkGenRes> = restService.execute()
            if (call.isSuccessful) {
                val res: ButtonLinkGenRes? = call.body()
                if (res!!.resultType=="SUCCESS") {
                    this.scheme = res.success.scheme
                    this.link = res.success.link
                    Log.i(TAG, "" + scheme + " " + link)
                }
            }
        }.start()
    }

    fun callToss() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme))
        startActivity(intent)
    }

}
