package com.cellpinda.coin.wallet.data.param

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
data class RecaptchaReq (
    val secret: String,
    val response: String,
    val remoteip: String
)