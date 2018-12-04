package com.cellpinda.coin.wallet.service

import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.data.res.EtherScanTx
import com.cellpinda.coin.wallet.data.res.EtherScanTxsRes
import retrofit2.Call
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class TxsCacheService {

    companion object {

        private val TAG: String = this.javaClass.simpleName
        var ret: List<EtherScanTx>? = null
        var lastCallTimeSales: Long = 0
        val TIME_DIFF = 1000 * 30

        /**
         * SalesMainData =>
         * ethbal, ethval, tokenbal, bonus, point
         */
        fun getTxs(ctx: MainActivity, forceRefresh: Boolean): List<EtherScanTx>? {
            val currTime = Calendar.getInstance().timeInMillis
            if (forceRefresh || ret==null || lastCallTimeSales ==0L || (currTime - lastCallTimeSales) > TIME_DIFF) {
                Log.d(TAG, "getTxs")
                val latch = CountDownLatch(2)
                var temp = ArrayList<EtherScanTx>()
                getEthTxs(ctx, latch, temp)
                getTokenTxs(ctx, latch, temp)
                latch.await()
                ret = temp.sortedWith(compareByDescending<EtherScanTx> { it.timeStamp }
                        .thenByDescending { it.contractAddress })
                lastCallTimeSales = Calendar.getInstance().timeInMillis
                ctx.runOnUiThread {
                    if (forceRefresh) {
                        try {
                            ctx.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing = false
                        } catch(e: Exception) {
                            Log.w(TAG, "" + e.message)
                        }
                    }
                }
            }
            return ret
        }

        /**
         *
         */
        private fun getEthTxs(ctx: MainActivity, latch: CountDownLatch, ret: ArrayList<EtherScanTx>) {
            Thread {
                val restService: Call<EtherScanTxsRes> = ctx.etherScanService()!!.ethTxs(ctx.myAddr)
                val call: retrofit2.Response<EtherScanTxsRes> = restService.execute()
                if (call.isSuccessful) {
                    val res: EtherScanTxsRes? = call.body()
                    if (res!=null && res.status == "1") {
                        ret.addAll(res.result)
                    }
                }
                latch.countDown()
            }.start()
        }

        /**
         *
         */
        private fun getTokenTxs(ctx: MainActivity, latch: CountDownLatch, ret: ArrayList<EtherScanTx>) {
            Thread {
                val restService: Call<EtherScanTxsRes> = ctx.etherScanService()!!.tokenTxs(ctx.myAddr)
                val call: retrofit2.Response<EtherScanTxsRes> = restService.execute()
                if (call.isSuccessful) {
                    val res: EtherScanTxsRes? = call.body()
                    if (res!!.status == "1") {
                        ret.addAll(res.result)
                    }
                }
                latch.countDown()
            }.start()
        }
    }
}