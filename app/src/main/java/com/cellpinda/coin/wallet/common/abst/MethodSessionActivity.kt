package com.cellpinda.coin.wallet.common.abst

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.cellpinda.coin.wallet.LockActivity
import com.cellpinda.coin.wallet.common.constant.REQ_CODE_LOCK
import com.cellpinda.coin.wallet.common.util.LocalStorage
import com.cellpinda.coin.wallet.service.WalletLocalService
import java.util.*

/**
 * Created by https://github.com/method76 on 2017-10-30.
 */
abstract class MethodSessionActivity : MethodActivity() {

    private val TAG: String = this.javaClass.simpleName
    private val sessionTime = 1000*60*1 // 1min?
    var someActivityOnTop: Boolean = false

    override fun onResume() {
        val lastActiveTime = WalletLocalService.lastSessionTime(this)
        val timeDiff = Calendar.getInstance().timeInMillis - lastActiveTime
//        Log.i(TAG, "timeDiff " + timeDiff)
        if (WalletLocalService.hasAccount(this) && !someActivityOnTop
                && (lastActiveTime == 0L || timeDiff > sessionTime)) {
            // 잠금화면 팝업
            app.locked = true
            someActivityOnTop = false
            startActivityForResult(Intent(this, LockActivity::class.java)
                    , REQ_CODE_LOCK)
            overridePendingTransition(0, 0)
        } else {
            app.locked = false
        }
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause locked")
        super.onPause()
        if (!app.locked) {
            WalletLocalService.setLastSessionTime(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_LOCK) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "lock canceled")
                finish()
            } else if (resultCode == Activity.RESULT_OK) {
                // 잠금화면 해제 시
                app.locked = false
                LocalStorage.set(this, "lastActiveTime", Calendar.getInstance().timeInMillis)
                Log.d(TAG, "unlocked")
            }
        }
    }

}