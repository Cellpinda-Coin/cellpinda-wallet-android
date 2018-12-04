package com.cellpinda.coin.wallet

import android.os.Bundle
import android.util.Log
import android.view.View
import com.cellpinda.coin.wallet.common.abst.MethodSessionActivity
import com.cellpinda.coin.wallet.data.res.Recommendee
import com.cellpinda.coin.wallet.helper.RecommendeeAdapter
import com.cellpinda.coin.wallet.service.RecommendeeCacheService
import com.cellpinda.coin.wallet.service.WalletLocalService
import kotlinx.android.synthetic.main.activity_contacts.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class ContactsActivity : MethodSessionActivity() {

    private val TAG: String = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        footer_layout.setOnClickListener {
            finish()
        }
        btn_recommend.setOnClickListener {
            WalletLocalService.sendRecommend(this)
        }
        Thread {
            val ret = RecommendeeCacheService.getRecommendees(this, true, false)
            runOnUiThread {
                hideProgress()
                if (ret!=null) {
                    setAdapter(ret!!)
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        someActivityOnTop = false
    }

    fun showProgress() {
        try {
            spinner.visibility = View.VISIBLE
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

    fun setAdapter(l: List<Recommendee>) {
        Log.i(TAG, "list " + l.toString())
        view1_listview.adapter = RecommendeeAdapter(this, 0, l)
        view1_listview.invalidate()
    }

}