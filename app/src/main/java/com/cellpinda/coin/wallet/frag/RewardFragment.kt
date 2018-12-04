package com.cellpinda.coin.wallet.frag

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class RewardFragment : Fragment() {

    private val TAG: String = this.javaClass.simpleName
    private lateinit var ctx: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ctx = this.activity as MainActivity
        return LayoutInflater.from(context)
                .inflate(R.layout.frag_reward, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}