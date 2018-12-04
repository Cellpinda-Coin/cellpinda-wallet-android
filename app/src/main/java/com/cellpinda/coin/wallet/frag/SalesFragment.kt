package com.cellpinda.coin.wallet.frag

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.util.WalletUtil
import com.cellpinda.coin.wallet.service.SalesCacheService
import kotlinx.android.synthetic.main.frag_sale.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class SalesFragment : Fragment() {

    private val TAG: String = this.javaClass.simpleName
    private lateinit var ctx: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ctx = this.activity as MainActivity
        return LayoutInflater.from(context)
                .inflate(R.layout.frag_sale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx.hideProgress()
        refresh_layout.setOnRefreshListener {
            refreshData(false, true)
        }
        btn_fill.setOnClickListener { ctx.callToss() }
        btn_buy_product.setOnClickListener { ctx.showFunctionNotReady() }
        btn_buy_cpd.setOnClickListener {
            ctx.showBuyCpd()
        }
        refreshData(true, false)
    }

    /**
     * params
     *      => ctx, addr: String, showProgress: Boolean, forceRefresh: Boolean
     */
    private fun refreshData(showProgress: Boolean, forceRefresh: Boolean) {
        Thread {
            // Throwing new exception 'length=245; index=1278' with unexpected pending exception: java.lang.ArrayIndexOutOfBoundsException: length=245; index=1278
            val data = SalesCacheService.getMainData(ctx, ctx.myAddr, showProgress, forceRefresh)
            if (data!=null) {
                ctx.salesData = data
                ctx.runOnUiThread {
                    try {
                        txt_eth_amt.text             = data.ethbal.toString()
                        txt_point_int.text           = WalletUtil.toCurrencyFormat8Int(data.point)
                        txt_token_amt_int.text       = WalletUtil.toCurrencyFormat8Int(data.tokenbal)
                        txt_token_bonus_int.text     = WalletUtil.toCurrencyFormat8Int(data.bonus)
                        txt_total_token_amt_int.text = WalletUtil.toCurrencyFormat8Int(data.tokenbal + data.bonus)
                        // 이더 원화가치
                        val totalKrw = data.tokenbal * 5 + data.ethval * data.ethbal + data.point
                        txt_krw_val.text = "￦" + WalletUtil.toCurrencyFormat8Int(totalKrw)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }.start()
    }

}