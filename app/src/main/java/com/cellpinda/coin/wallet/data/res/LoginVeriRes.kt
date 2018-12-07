package com.cellpinda.coin.wallet.data.res

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
data class LoginVeriRes (
    val e: Int,
    val m: String,
    val uid: Int,
    val key: String, // session key
    val address: String,
    val level: Int,
    val name: String,
    val cpd: Double?,
    val bonus: Double?,
    val point: Double?
)
