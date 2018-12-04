package com.cellpinda.coin.wallet.data.res

import com.google.gson.internal.LinkedTreeMap

data class CoinmarketCapTickerRes (
    val data: LinkedTreeMap<String, CoinmarketCapTicker>,
    val metadata: CoinmarketCapMeta
)