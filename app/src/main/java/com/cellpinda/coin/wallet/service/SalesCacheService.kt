package com.cellpinda.coin.wallet.service

import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.widget.Toast
import com.cellpinda.coin.wallet.BuildConfig
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.constant.CMCAP_ID_ETH
import com.cellpinda.coin.wallet.common.constant.SESS_EXPIRE_RES
import com.cellpinda.coin.wallet.common.constant.SUCCESS_RES
import com.cellpinda.coin.wallet.common.constant.SYM_KRW
import com.cellpinda.coin.wallet.common.util.BalanceUtils
import com.cellpinda.coin.wallet.data.param.Account
import com.cellpinda.coin.wallet.data.res.CoinmarketCapTickerSingleRes
import com.cellpinda.coin.wallet.data.res.LoginVeriRes
import com.cellpinda.coin.wallet.data.res.SalesMainData
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import retrofit2.Call
import rx.Single
import rx.schedulers.Schedulers
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class SalesCacheService {

    companion object {

        private val TAG: String = this.javaClass.simpleName

        var ethbal: Double = 0.0
        var ethval: Double = 0.0
        var tokenbal: Double = 0.0
        var bonus: Double = 0.0
        var point: Double = 0.0
        var ret: SalesMainData? = null
        var lastCallTimeSales: Long = 0
        val TIME_DIFF = 1000 * 30

        /**
         * SalesMainData =>
         * ethbal, ethval, tokenbal, bonus, point
         */
        fun getMainData(ctx: MainActivity, addr: String, showProgress: Boolean, forceRefresh: Boolean): SalesMainData? {
            val currTime = Calendar.getInstance().timeInMillis
            if (forceRefresh || ret==null || lastCallTimeSales==0L || (currTime - lastCallTimeSales) > TIME_DIFF) {
                if (showProgress) {
                    ctx.runOnUiThread {
                        ctx.showProgress()
                    }
                }
                val latch = CountDownLatch(4)
                getEthBalance(ctx, addr, latch)
                getEthValue(ctx, latch)
                getBonusPoint(ctx, latch)
                getTokenBalance(ctx, addr, BuildConfig.CONTRACT_ADDR, latch)
                latch.await()
                ret = SalesMainData(ethbal, ethval, tokenbal, bonus, point)
                lastCallTimeSales = Calendar.getInstance().timeInMillis
                ctx.runOnUiThread {
                    try {
                        ctx.findViewById<SwipeRefreshLayout>(R.id.refresh_layout).isRefreshing = false
                        ctx.hideProgress()
                    } catch(e: Exception) {
                        Log.w(TAG, "" + e.message)
                    }
                }
            }
            return ret
        }

        /**
         *
         */
        private fun getEthBalance(ctx: MainActivity, addr: String, latch: CountDownLatch) {
            Thread {
                // ETH 잔고
                Log.i(InfuraApiService.TAG, "getEthBalance")
                Single.fromCallable {
                    InfuraApiService.getWeb3j(ctx).ethGetBalance(addr
                            , DefaultBlockParameterName.LATEST).send().balance
                }.subscribeOn(Schedulers.io()).subscribe { ethBal ->
                    ethbal = BalanceUtils.weiToEthDouble(ethBal, 5)
                    latch.countDown()
                }
            }.start()
        }

        /**
         *
         */
        private fun getEthValue(ctx: MainActivity, latch: CountDownLatch) {
            Thread {
                val restService: Call<CoinmarketCapTickerSingleRes> = ctx.cmCapService()!!.ethKrwPrice(CMCAP_ID_ETH.toString())
                val call: retrofit2.Response<CoinmarketCapTickerSingleRes> = restService.execute()
                if (call.isSuccessful) {
                    val res: CoinmarketCapTickerSingleRes? = call.body()
                    if (res!!.metadata.error.isNullOrBlank()) {
                        if (res.data.quotes.containsKey(SYM_KRW)) {
                            ethval = res.data.quotes[SYM_KRW]!!.price
                        } else {
                            ethval = 0.0
                        }
                    }
                }
                latch.countDown()
            }.start()
        }

        /**
         *
         */
        private fun getTokenBalance(ctx: MainActivity, addr: String, contractAddr: String, latch: CountDownLatch) {
            try {
                val func = InfuraApiService.balanceOf(addr)
                val resVal = InfuraApiService.callSmartContractFunction(func, contractAddr, addr, ctx)
                val res = FunctionReturnDecoder.decode(resVal, func.outputParameters)
                if (res.size == 1) {
                    tokenbal = BigDecimal((res[0] as Uint256).value).divide(BigDecimal.TEN.pow(8)).toDouble()
                }
            } catch(e: IOException) {
                e.printStackTrace()
                Toast.makeText(ctx, R.string.txt_msg_check_internet, Toast.LENGTH_LONG).show()
//                WalletLocalService.finishApp(ctx)
            }
            latch.countDown()
        }

        /**
         *
         */
        private fun getBonusPoint(ctx: MainActivity, latch: CountDownLatch) {
            val acc: Account? = WalletLocalService.loadAccount(ctx)
            acc ?: return
            val restService: Call<LoginVeriRes> = ctx.walletService()!!.mainData("r", acc.uid, acc.key)
            Thread {
                val call1: retrofit2.Response<LoginVeriRes> = restService.execute()
                var res: LoginVeriRes? = null
                if (call1.isSuccessful) {
                    res = call1.body()
                    if (res!!.e == SUCCESS_RES) {
                        bonus = res.bonus?:0.0
                        point = res.point?:0.0
                    } else if (res!!.e == SESS_EXPIRE_RES) {
                        WalletLocalService.alert(ctx, R.string.txt_msg_sess_expired)
                        ctx.runOnUiThread {
                            Handler().postDelayed({
                                WalletLocalService.logout(ctx)
                            }, 3500)
                        }
                    }
                }
                latch.countDown()
            }.start()
        }
    }
}