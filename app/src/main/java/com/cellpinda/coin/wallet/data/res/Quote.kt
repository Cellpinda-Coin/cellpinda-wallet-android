package com.cellpinda.coin.wallet.data.res

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
data class Quote (
    val price: Double,
    val volume_24h: Double,
    val market_cap: Double,
    val percent_change_1h: Float,
    val percent_change_24h: Float,
    val percent_change_7d: Float
)