package com.cellpinda.coin.wallet.data.param

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
data class ButtonLinkGenReq (
    val apiKey: String,
    val bankName: String,
    val bankAccountNo: String,
    val amount : Long,
    val message : String
)