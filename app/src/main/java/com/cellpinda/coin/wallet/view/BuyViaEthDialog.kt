package com.cellpinda.coin.wallet.view

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.constant.ETH_FEE_LIMIT
import com.cellpinda.coin.wallet.common.constant.ETH_MIN_LIMIT
import com.cellpinda.coin.wallet.common.util.WalletUtil
import kotlinx.android.synthetic.main.activity_cpd_partial_buy.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class BuyViaEthDialog(context: Context) : Dialog(context) {

    private var ctx = (context as MainActivity)
    private var estimate = "0"

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.activity_cpd_partial_buy)
        cancel_button.setOnClickListener {
            this.dismiss()
        }
        confirm_button.setOnClickListener {
            // validation
            if (pay_amt.text.isNotEmpty() && pay_amt.text.toString().toDouble() >= ETH_MIN_LIMIT) {
                tv_warn_less_min.visibility = View.GONE
                ctx.showLastBuyConfirm(pay_amt.text.toString(), estimate)
                this.dismiss()
            } else {
                tv_warn_less_min.visibility = View.VISIBLE
            }
        }
        val ethRaw        = ctx.salesData!!.ethbal
        val ethVal        = ctx.salesData!!.ethval
        val ethFeeRemoved = if (ctx.salesData!!.ethbal>(ETH_MIN_LIMIT+ETH_FEE_LIMIT)) {
            ctx.salesData!!.ethbal - ETH_FEE_LIMIT
        } else if (ctx.salesData!!.ethbal>ETH_MIN_LIMIT) {
            ctx.salesData!!.ethbal
        } else {
            ETH_MIN_LIMIT
        }

        tv_eth_price_cmc.text = ctx.getString(R.string.txt_eth_price_cmc
                , WalletUtil.toCurrencyFormat8Int(ethVal))
        tv_pay_amt_desc.text = ctx.getString(R.string.txt_expt_pay_amt, "0")
        tv_max.text = ctx.getString(R.string.txt_tv_max_amt, ethFeeRemoved.toString())
        pay_amt.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 텍스트 변경 시 아래 수량 변경 표시
                estimate = "0"
                if (pay_amt.text.isNotEmpty()) {
                    // 예상지급 토큰 수
                    estimate = WalletUtil.toCurrencyFormat8Int(
                            pay_amt.text.toString().toDouble() * ctx.salesData!!.ethval / 5.0)
                }
                tv_pay_amt_desc.text = ctx.getString(R.string.txt_expt_pay_amt, estimate)
            }
            // 입력이 끝났을 때
            override fun afterTextChanged(edt: Editable) {
                if (pay_amt.text.isNotEmpty()) {
                    if (pay_amt.text.toString().toDouble() < ETH_MIN_LIMIT) {
                        tv_warn_less_min.visibility = View.VISIBLE
                    } else {
                        tv_warn_less_min.visibility = View.GONE
                    }
                }
            }
            // 입력하기 전에
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }
        })
        btn_10.setOnClickListener {
            val payAmt: Double = if (ethRaw*0.1<ETH_MIN_LIMIT) { ETH_MIN_LIMIT } else { ethRaw * 0.1 }
            pay_amt.setText(payAmt.toString())
        }
        btn_25.setOnClickListener {
            val payAmt: Double = if (ethRaw*0.25<ETH_MIN_LIMIT) { ETH_MIN_LIMIT } else { ethRaw * 0.25 }
            pay_amt.setText(payAmt.toString())
        }
        btn_50.setOnClickListener {
            val payAmt: Double = if (ethRaw*0.5<ETH_MIN_LIMIT) { ETH_MIN_LIMIT } else { ethRaw * 0.5 }
            pay_amt.setText(payAmt.toString())
        }
        btn_75.setOnClickListener {
            val payAmt: Double = if (ethRaw*0.75<ETH_MIN_LIMIT) { ETH_MIN_LIMIT } else { ethRaw * 0.75 }
            pay_amt.setText(payAmt.toString())
        }
        btn_90.setOnClickListener {
            val payAmt: Double = if (ethRaw*0.9<ETH_MIN_LIMIT) { ETH_MIN_LIMIT } else { ethRaw * 0.9 }
            pay_amt.setText(payAmt.toString())
        }
    }
}