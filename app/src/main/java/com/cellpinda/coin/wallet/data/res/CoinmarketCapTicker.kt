package com.cellpinda.coin.wallet.data.res

import com.google.gson.internal.LinkedTreeMap

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
data class CoinmarketCapTicker (
    val id: Int,
    val name: String,
    val symbol: String,
    val website_slug: String,
    val rank: Int,
    val circulating_supply: Double,
    val total_supply: Double,
    val max_supply: Double,
    val quotes: LinkedTreeMap<String, Quote>
)