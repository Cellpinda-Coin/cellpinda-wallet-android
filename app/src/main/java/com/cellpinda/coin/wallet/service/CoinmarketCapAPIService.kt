package com.cellpinda.coin.wallet.service

import com.cellpinda.coin.wallet.data.res.CoinmarketCapTickerSingleRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Created by https://github.com/method76 on 2017-11-06.
 * https://api.coinmarketcap.com/v2/ticker/1027/?convert=KRW
 * => "KRW": { "price": 239361.9919916035 }
 */
interface CoinmarketCapAPIService {

    // 1027 => ETH
    @GET("ticker/{cryptoid}/?convert=KRW")
    fun ethKrwPrice(@Path("cryptoid") cryptoid: String): Call<CoinmarketCapTickerSingleRes>

}
