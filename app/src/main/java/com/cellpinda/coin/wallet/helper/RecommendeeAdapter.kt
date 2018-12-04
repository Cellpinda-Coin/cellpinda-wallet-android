package com.cellpinda.coin.wallet.helper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.util.WalletUtil
import com.cellpinda.coin.wallet.data.res.Recommendee

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class RecommendeeAdapter(context: Context, resource: Int, objects: List<Recommendee>) :
        ArrayAdapter<Recommendee>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_recommend, null)
        }
        val name = view!!.findViewById(R.id.btn_recomendee_nm) as TextView
        val amt = view!!.findViewById(R.id.txt_amount) as TextView
        var ago = view!!.findViewById(R.id.txt_ago) as TextView
        name.text = getItem(position).name
        amt.text = WalletUtil.toCurrencyFormat8Int(getItem(position).CPD.toDouble()) + " CPD"
        ago.text = getItem(position).inputtime.substring(3, getItem(position).inputtime.length-3)
        return view
    }

}