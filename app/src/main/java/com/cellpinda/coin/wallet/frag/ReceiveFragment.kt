package com.cellpinda.coin.wallet.frag

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.constant.IMG_NAME_QR
import com.cellpinda.coin.wallet.common.util.ImageSaver
import com.cellpinda.coin.wallet.common.util.MobileUtil
import com.cellpinda.coin.wallet.common.util.WalletUtil
import com.cellpinda.coin.wallet.service.WalletLocalService
import kotlinx.android.synthetic.main.frag_recv.*

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class ReceiveFragment : Fragment() {

    private val TAG: String = this.javaClass.simpleName
    private lateinit var ctx: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ctx = this.activity as MainActivity
        return LayoutInflater.from(context)
                .inflate(R.layout.frag_recv, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ctx.hideProgress()
        btn_copy.setOnClickListener {
            val text: String = txt_addr_recv.text as String
            WalletUtil.copyToClipboard(ctx, text)
            Toast.makeText(ctx, R.string.txt_msg_copied, Toast.LENGTH_SHORT).show()
        }
        btn_share.setOnClickListener { WalletLocalService.shareAddress(ctx, ctx.myAddr) }
        iv_qr_my_wallet.setOnClickListener {
            val text: String = txt_addr_recv.text as String
            WalletUtil.copyToClipboard(ctx, text)
            Toast.makeText(ctx, R.string.txt_msg_copied, Toast.LENGTH_SHORT).show()
        }
        btn_myaddr_etherscan.setOnClickListener {
            MobileUtil.openBrowser(ctx,"https://etherscan.io/address/" + ctx.myAddr)
        }
        showQRCode()
    }

    private fun showQRCode() {
        txt_addr_recv.text = ctx.myAddr
        val bmp = ImageSaver(ctx).setFileName(IMG_NAME_QR).setDirectoryName("images").setExternal(false).load()
        Glide.with(ctx).load(bmp).into(iv_qr_my_wallet)
    }

}