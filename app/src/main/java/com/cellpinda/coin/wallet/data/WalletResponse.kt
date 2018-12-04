package com.cellpinda.coin.wallet.data

/**
 * Created by method76 on 2017-11-06.
 */
data class WalletResponse (

    val txFee: Double,
    val balance: String,
    val address: String,
    val keywords: String,
    val fromAddress: String,
    val toAddress: String,
    val amount: Double

)
