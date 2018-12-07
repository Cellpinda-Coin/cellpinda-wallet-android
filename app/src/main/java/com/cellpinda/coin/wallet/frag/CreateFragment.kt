package com.cellpinda.coin.wallet.frag

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cellpinda.coin.wallet.FirstActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.constant.SUCCESS_RES
import com.cellpinda.coin.wallet.common.util.MobileUtil
import com.cellpinda.coin.wallet.data.res.SmsReqRes
import com.cellpinda.coin.wallet.service.WalletLocalService
import kotlinx.android.synthetic.main.frag_create.*
import retrofit2.Call
import retrofit2.Response

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class CreateFragment : Fragment() {

    private val TAG: String = this.javaClass.simpleName
    private lateinit var ctx: FirstActivity
    private lateinit var mobileNo: String
    private var isOnline: Boolean = false
    private var certRequested: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?
                              , savedInstanceState: Bundle?): View? {
        ctx = this.activity as FirstActivity
        return LayoutInflater.from(context)
                .inflate(R.layout.frag_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isOnline = MobileUtil.isOnline(ctx)
        mobileNo = MobileUtil.getMobileNumber(ctx)
        mobile_no.setText(mobileNo)
        Log.d(TAG, "mobileNo" + mobileNo)
        if (ctx.recomId!=null) { tv_recom_id.text = ctx.recomId } else { tv_recom_desc.visibility = View.GONE }
        btn_cancel.setOnClickListener { ctx.chooseView() }
        btn_confirm.setOnClickListener {
            goCreate()
        }
        btn_sms_req.setOnClickListener { reqSms() }
        sms_cert_no.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var size: Int = if (s!=null) { s.length } else { 0 }
                if (size==6) {
                    goCreate()
                }
            }
        })
    }

    /**
     * 컨펌 버튼을 눌렀거나, SMS 6자리를 입력한 경우
     */
    private fun goCreate() {
        if (validateInput()) {
            if (!certRequested) {
                WalletLocalService.warnAutoHide(ctx, R.string.txt_msg_sms_cert)
            } else {
                // confirm : confirmSms
                Thread(Runnable {
                    try {
                        val req: Call<SmsReqRes> = ctx.walletService()!!.confirmSms(
                                "c", mobile_no.text.toString(), sms_cert_no.text.toString())
                        val call: Response<SmsReqRes> = req.execute()
                        Log.d(TAG, "res " + call.body().toString())
                        if (call.isSuccessful) {
                            val res: SmsReqRes? = call.body()!!
                            if (res!!.e == SUCCESS_RES) {
                                ctx.reqCreateAccount(mobile_no.text.toString(), user_name.text.toString()
                                        , sms_cert_no.text.toString(), res!!.seq.toString())
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }).start()
            }
        }
    }

    fun setSmsVeriCode(code: String) {
        sms_cert_no.setText(code)
//        btn_confirm.performClick()
    }

    fun validateInput(): Boolean {
        // user_name mobile_no sms_cert_no
        if (mobile_no.text.isNullOrBlank()) {
            return ctx.inputMobileNo()
        } else if (mobile_no.text.toString().length<10) {
            return ctx.mobileNoIsShort()
        } else if (user_name.text.isNullOrBlank()) {
            return ctx.inputUserName()
        } else if (user_name.text.toString().length < 2) {
            return ctx.userNameIsShort()
//        } else if (!certMatched) {
//            val diag: SweetAlertDialog = SweetAlertDialog(ctx, SweetAlertDialog.WARNING_TYPE)
//                    .setTitleText(getString(R.string.txt_msg_sms_wrong))
//                    .setConfirmText(getString(R.string.txt_btn_confirm))
//                    .showCancelButton(false)
//                    .setConfirmClickListener { sDialog ->
//                        sDialog.dismissWithAnimation()
//                    }
//            diag.show()
//            Handler().postDelayed({
//                diag.dismissWithAnimation()
//            }, 3000)
//            return false
        } else {
            return true
        }
    }

    private fun reqSms() {
        if (!validateInput()) { return  }
        ctx.showProgress3Sec()
        btn_sms_req.isEnabled = false
        if (Build.VERSION.SDK_INT > 20) {
            btn_sms_req.backgroundTintList = ctx.resources.getColorStateList(R.color.colorPrimaryDarkAlpha)
        }
        Thread(Runnable {
            try {
                val req: Call<SmsReqRes> = ctx.walletService()!!.reqSms("s", mobile_no.text.toString())
                val call: Response<SmsReqRes> = req.execute()
                Log.d(TAG, "res " + call.body().toString())
                if (call.isSuccessful) {
                    val res: SmsReqRes? = call.body()!!
                    if (res!!.e==SUCCESS_RES) {
                        // SMS 요청 성공
                        certRequested = true
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

}