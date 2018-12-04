package com.cellpinda.coin.wallet.service

import com.cellpinda.coin.wallet.ContactsActivity
import com.cellpinda.coin.wallet.data.param.Account
import com.cellpinda.coin.wallet.data.res.Recommendee
import com.cellpinda.coin.wallet.data.res.RecommendeeRes
import retrofit2.Call
import java.util.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class RecommendeeCacheService {

    companion object {

        private val TAG: String = this.javaClass.simpleName
        var lastCallTimeSales: Long = 0
        val TIME_DIFF = 1000 * 30
        var recommendees: List<Recommendee>? = null

        /**
         * SalesMainData =>
         * ethbal, ethval, tokenbal, bonus, point
         */
        fun getRecommendees(ctx: ContactsActivity, showProgress: Boolean, forceRefresh: Boolean): List<Recommendee>? {
            val acc: Account? = WalletLocalService.loadAccount(ctx)
            acc ?: return null
            val currTime = Calendar.getInstance().timeInMillis
            if (forceRefresh || RecommendeeCacheService.lastCallTimeSales ==0L
                    || (currTime - RecommendeeCacheService.lastCallTimeSales) > RecommendeeCacheService.TIME_DIFF) {
//                Log.d(TAG, "getRecommendees")
                if (showProgress) {
                    ctx.runOnUiThread {
                        ctx.showProgress()
                    }
                }
                val restService: Call<RecommendeeRes> = ctx.walletService()!!.recommendeeList("recommend", acc.uid, acc.key)
                val call: retrofit2.Response<RecommendeeRes> = restService.execute()
                var res: RecommendeeRes? = null
                if (call.isSuccessful) {
                    res = call.body()
                    if (res!!.e=="0") {
                        lastCallTimeSales = Calendar.getInstance().timeInMillis
                        recommendees = res!!.result
                        return recommendees
                    } else {
                        return null
                    }
                } else {
                    return null
                }
            } else {
                return recommendees
            }
        }
    }
}