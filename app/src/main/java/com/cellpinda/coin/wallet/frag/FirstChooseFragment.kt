package com.cellpinda.coin.wallet.frag

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cellpinda.coin.wallet.FirstActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.util.MobileUtil
import kotlinx.android.synthetic.main.frag_choose.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class FirstChooseFragment : Fragment() {

    private val TAG: String = this.javaClass.simpleName
    private lateinit var ctx: FirstActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ctx = this.activity as FirstActivity
        return LayoutInflater.from(context)
                .inflate(R.layout.frag_choose, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_new_wallet.setOnClickListener {
            if (MobileUtil.isOnline(ctx)) {
                ctx.newWalletView()
            } else {
                MobileUtil.showNetworkErrorMsg(ctx)
            }
        }
        btn_restore_wallet.setOnClickListener {
            if (MobileUtil.isOnline(ctx)) {
                ctx.restoreWalletView()
            } else {
                MobileUtil.showNetworkErrorMsg(ctx)
            }
        }
    }
}