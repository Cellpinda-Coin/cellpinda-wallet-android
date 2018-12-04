package com.cellpinda.coin.wallet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.andrognito.pinlockview.PinLockListener
import com.cellpinda.coin.wallet.common.abst.MethodActivity
import com.cellpinda.coin.wallet.common.constant.PARAM_NEW_ACC
import com.cellpinda.coin.wallet.service.WalletLocalService
import kotlinx.android.synthetic.main.activity_changepw.*
import kotlinx.android.synthetic.main.view_confirm_pw.*
import kotlinx.android.synthetic.main.view_curr_pw.*
import kotlinx.android.synthetic.main.view_new_pw.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class ChangePwActivity : MethodActivity(), PinLockListener {

    private val TAG: String = this.javaClass.simpleName
    var isNew: Boolean = false
    var newpw: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changepw)
        pin_lock_view1.attachIndicatorDots(indicator_dots1)
        pin_lock_view2.attachIndicatorDots(indicator_dots2)
        pin_lock_view3.attachIndicatorDots(indicator_dots3)
        pin_lock_view1.setPinLockListener(this)
        pin_lock_view2.setPinLockListener(this)
        pin_lock_view3.setPinLockListener(this)

        isNew = intent.getBooleanExtra(PARAM_NEW_ACC, false)
        if (isNew) {
            // 신규 생성이면 기존 PW 치지 않도록
            view_flip.displayedChild = 1
        } else {
            view_flip.displayedChild = 0
        }
    }

    override fun onEmpty() {
        Log.d(TAG, "onEmpty")
    }

    override fun onComplete(pin: String?) {
        // Todo: ecryption compare
        if (view_flip.displayedChild==0) {
            val pw: String = WalletLocalService.getPw(this)
            if (pw==""+pin) {
                view_flip.showNext()
            } else {
                pin_lock_view1.resetPinLockView()
                Toast.makeText(this, R.string.txt_msg_pw_not_match, Toast.LENGTH_SHORT).show()
            }
        } else if (view_flip.displayedChild==1) {
            newpw = pin
            view_flip.showNext()
        } else if (view_flip.displayedChild==2) {
            if (newpw==pin) {
                if (!pin.isNullOrEmpty()) {
                    if (!isNew) {
                        WalletLocalService.savePW(this, pin!!)
                    }
                    // 메인화면
                    val result = Intent()
                    result.putExtra("pin", pin)
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            } else {
                pin_lock_view3.resetPinLockView()
                Toast.makeText(this, R.string.txt_msg_confirm_pw_not_match, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPinChange(pinLength: Int, intermediatePin: String?) {
//        Log.d(TAG, "onPinChange " + intermediatePin)
    }

}