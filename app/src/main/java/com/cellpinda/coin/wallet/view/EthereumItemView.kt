package com.cellpinda.coin.wallet.view

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.cellpinda.coin.wallet.BuildConfig
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.util.BalanceUtils
import com.cellpinda.coin.wallet.common.util.WalletUtil
import com.cellpinda.coin.wallet.data.res.EtherScanTx
import java.math.BigInteger

/**
 * Created by https://github.com/method76 on 2017-11-12.
 */
class EthereumItemView : FrameLayout {

    lateinit var v: View
    lateinit var ctx: Context
    lateinit var myaddr: String

    constructor(context: Context) : super(context)
    constructor(context: Context, data: EtherScanTx, myaddr: String) : super(context) {
        this.ctx = context
        this.myaddr = myaddr
        initView(context, data)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun initView(ctx: Context, data: EtherScanTx) {

        val li: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (data.from==myaddr) {
            // 출금
            v = li.inflate(R.layout.view_out, this, false)
            val to: TextView = v.findViewById(R.id.txt_to)
            val title: TextView = v.findViewById(R.id.txt_btn_title)
            to.text = data.to
            if (data.to==BuildConfig.MASTER_ADDR) {
                // 토큰 구매요청
                // txt_token_purchase
                title.text = ctx.getString(R.string.txt_token_purchase)
                val address_row: TableRow = v.findViewById(R.id.address_row)
                address_row.visibility = View.GONE
            }
        } else {
            // 입금
            v = li.inflate(R.layout.view_in, this, false)
            val from: TextView = v.findViewById(R.id.txt_from)
            val title: TextView = v.findViewById(R.id.txt_btn_title)
            from.text = data.from
            if (data.from==BuildConfig.MASTER_ADDR) {
                // 토큰 지급
                // txt_token_purchase
                title.text = ctx.getString(R.string.txt_token_payment)
                val address_row: TableRow = v.findViewById(R.id.address_row)
                address_row.visibility = View.GONE
            }
        }
        val value: TextView = v.findViewById(R.id.txt_amt)
        val table: TableLayout = v.findViewById(R.id.item_table)
        if (data.contractAddress.isNotEmpty()) {
            value.text = (WalletUtil.cpdToCurrencyFormat8(data.value) + " CPD")
            table.setBackgroundResource(R.drawable.bg_right_bottom_symbol_item)
        } else {
            value.text = (WalletUtil.toCurrencyFormat8(
                    BalanceUtils.weiToEth(BigInteger(data.value)).toDouble()) + " ETH")
            table.setBackgroundResource(R.drawable.bg_right_bottom_eth_item)
        }
        val hash: TextView  = v.findViewById(R.id.txt_tx_hash)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            hash.text = Html.fromHtml("<a href=\"https://etherscan.io/tx/" + data.hash + "\" target=\"_blank\">" + data.hash + "</a>"
                    , Html.FROM_HTML_MODE_COMPACT)
        } else {
            hash.text = Html.fromHtml("<a href=\"https://etherscan.io/tx/" + data.hash + "\" target=\"_blank\">" + data.hash + "</a>")
        }
        hash.linksClickable = true
        hash.movementMethod = LinkMovementMethod.getInstance()
        val ago: TextView   = v.findViewById(R.id.txt_ago)
        ago.text   = WalletUtil.unixToYyyymmdd(data.timeStamp)
        addView(v)
    }
}