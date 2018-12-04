package com.cellpinda.coin.wallet.data

/**
 * Created by method76 on 2017-11-06.
 */
data class Transaction (

    val value: Double,
    val fromAddress: String,
    val toAddress: String,
    val amount: Double,
    val txhash: String,
    val time: Long,
    val state: String

)