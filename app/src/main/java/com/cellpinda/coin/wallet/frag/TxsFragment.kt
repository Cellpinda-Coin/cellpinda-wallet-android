package com.cellpinda.coin.wallet.frag

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.util.TxsAdapter
import com.cellpinda.coin.wallet.service.TxsCacheService
import kotlinx.android.synthetic.main.frag_txs.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class TxsFragment : Fragment() {

    private val TAG: String = this.javaClass.simpleName
    private lateinit var ctx: MainActivity
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ctx = this.activity as MainActivity
        return LayoutInflater.from(context)
                .inflate(R.layout.frag_txs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx.hideProgress()
        refresh_layout.setOnRefreshListener { refreshData(false, true) }
        viewManager = LinearLayoutManager(ctx)
        refreshData(true, false)
    }

    private fun refreshData(showProgress: Boolean, forceRefresh: Boolean) {
        if (showProgress) { ctx.showProgress() }
        Thread {
            val data = TxsCacheService.getTxs(ctx, forceRefresh)
            if (data==null || data.isEmpty()) {
                ctx.runOnUiThread {
                    // 거래내역 없는 경우
                    val li: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val v: ViewGroup = li.inflate(R.layout.view_empty, null) as ViewGroup
                    recycle_frame.addView(v)
                    recycle_frame.invalidate()
                    ctx.hideProgress()
                }
            } else {
                ctx.runOnUiThread {
                    viewAdapter = TxsAdapter(ctx, data, ctx.myAddr)
                    if (recycler_view != null) {
                        recycler_view.apply {
                            // use this setting to improve performance if you know that changes
                            // in content do not change the layout size of the RecyclerView
                            setHasFixedSize(true)
                            // use a linear layout manager
                            layoutManager = viewManager
                            // specify an viewAdapter (see also next example)
                            adapter = viewAdapter

                        }
                        ctx.hideProgress()
                    }
                }
            }
        }.start()
    }

}