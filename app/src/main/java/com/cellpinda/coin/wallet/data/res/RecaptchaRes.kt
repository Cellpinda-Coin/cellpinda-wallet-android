package com.cellpinda.coin.wallet.data.res

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
data class RecaptchaRes (
    val success: Boolean,
    val challenge_ts: Long,
    val apk_package_name: String
//    val error-codes: Int
)