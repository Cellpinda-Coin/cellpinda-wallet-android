package com.cellpinda.coin.wallet.common.util

import android.content.Context
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.cellpinda.coin.wallet.BuildConfig
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.data.res.EtherScanTx
import java.math.BigInteger

/**
 * Created by https://github.com/method76 on 2018-11-27.
 */
class TxsAdapter internal constructor(private val ctx: Context, private val list: List<EtherScanTx>, private val myaddr: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_IN  = 0
    private val TYPE_OUT = 1

    abstract class TxHolder(v: CardView) : RecyclerView.ViewHolder(v) {
        abstract var addr: TextView
        var table: TableLayout = v.findViewById(R.id.item_table)
        val title: TextView = v.findViewById(R.id.txt_btn_title)
        val amount: TextView = v.findViewById(R.id.txt_amt)
        val hash: TextView = v.findViewById(R.id.txt_tx_hash)
        val ago: TextView = v.findViewById(R.id.txt_ago)
        val address_row: TableRow = v.findViewById(R.id.address_row)
    }

    class InViewHolder(v: CardView) : TxHolder(v) {
        override var addr: TextView = v.findViewById(R.id.txt_from)
    }

    class OutViewHolder(v: CardView) : TxHolder(v) {
        override var addr: TextView = v.findViewById(R.id.txt_to)
    }

    override fun getItemViewType(position: Int): Int {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return if (list[position].from==myaddr) { TYPE_OUT } else { TYPE_IN }
    }

    /**
     * LAYOUT 정의
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_IN  -> return InViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_in, parent, false) as CardView)
            TYPE_OUT -> return OutViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_out, parent, false) as CardView)
            else -> return OutViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.view_in, parent, false) as CardView)
        }
    }

    /**
     * 행에 보여질 컴포넌트 정의
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        val lHolder = holder as TxHolder
        if (item.from==myaddr) {
            // 출금
            (lHolder as OutViewHolder).addr.text = item.to
            if (item.to== BuildConfig.MASTER_ADDR) {
                // 토큰 구매요청
                lHolder.title.text = ctx.getString(R.string.txt_token_purchase)
                lHolder.address_row.visibility = View.GONE
            }
        } else {
            // 입금
            (lHolder as InViewHolder).addr.text = item.from
            if (item.from== BuildConfig.MASTER_ADDR) {
                // 토큰 지급
                lHolder.title.text = ctx.getString(R.string.txt_token_payment)
                lHolder.address_row.visibility = View.GONE
            }
        }
        if (item.contractAddress.isNotEmpty()) {
            lHolder.amount.text = (WalletUtil.cpdToCurrencyFormat8(item.value) + " CPD")
            lHolder.table.setBackgroundResource(R.drawable.bg_right_bottom_symbol_item)
        } else {
            lHolder.amount.text = (WalletUtil.toCurrencyFormat8(
                    BalanceUtils.weiToEth(BigInteger(item.value)).toDouble()) + " ETH")
            lHolder.table.setBackgroundResource(R.drawable.bg_right_bottom_eth_item)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            lHolder.hash.text = Html.fromHtml("<a href=\"https://etherscan.io/tx/" + item.hash + "\" target=\"_blank\">" + item.hash + "</a>"
                    , Html.FROM_HTML_MODE_COMPACT)
        } else {
            lHolder.hash.text = Html.fromHtml("<a href=\"https://etherscan.io/tx/" + item.hash + "\" target=\"_blank\">" + item.hash + "</a>")
        }
        holder.hash.linksClickable = true
        holder.hash.movementMethod = LinkMovementMethod.getInstance()
        holder.ago.text   = WalletUtil.unixToYyyymmdd(item.timeStamp)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}