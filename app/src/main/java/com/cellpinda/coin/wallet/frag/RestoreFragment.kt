package com.cellpinda.coin.wallet.frag

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cellpinda.coin.wallet.FirstActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.util.MobileUtil
import kotlinx.android.synthetic.main.frag_restore.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class RestoreFragment : Fragment() {

    private val TAG: String = this.javaClass.simpleName
    private lateinit var ctx: FirstActivity
    private lateinit var mobileNo: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ctx = this.activity as FirstActivity
        return LayoutInflater.from(context)
                .inflate(R.layout.frag_restore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mobileNo = MobileUtil.getMobileNumber(ctx)
        mobile_no.setText(mobileNo)
        btn_cancel.setOnClickListener { ctx.chooseView() }
        btn_confirm.setOnClickListener {
            if (validateInput()) {
                ctx.callRecaptcha(mobile_no.text.toString(), passcode.text.toString())
            }
        }
    }

    fun validateInput(): Boolean {
        // user_name mobile_no sms_cert_no
        if (mobile_no.text.isNullOrBlank()) {
            return ctx.inputMobileNo()
        } else if (mobile_no.text.toString().length<10) {
            return ctx.mobileNoIsShort()
        } else if (passcode.text.isNullOrBlank() || passcode.text.length!=4) {
            return ctx.inputPasscode()
        } else {
            return true
        }
    }

}