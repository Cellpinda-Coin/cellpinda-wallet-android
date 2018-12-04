package com.cellpinda.coin.wallet.service

import com.cellpinda.coin.wallet.data.res.EtherScanTxsRes
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by https://github.com/method76 on 2017-11-06.
 * http://api.etherscan.io/api
 */
interface EtherScanAPIService {

    // &address=0x1641c455868064aa0fdc03f916abdb14688da6eb
    @GET("/api?module=account&action=tokentx&startblock=6329086&endblock=99999999&sort=desc&page=1&offset=100&apikey=XWTHUF21CG4EQRA826SEPED9TRCT5TEWNW")
    fun tokenTxs(@Query("address") address: String): Call<EtherScanTxsRes>

    // &address=0x1641c455868064aa0fdc03f916abdb14688da6eb
    @GET("/api?module=account&action=txlist&startblock=6329086&endblock=99999999&sort=desc&page=1&offset=100&apikey=XWTHUF21CG4EQRA826SEPED9TRCT5TEWNW")
    fun ethTxs(@Query("address") address: String): Call<EtherScanTxsRes>

//    @Headers("Accept: application/json")

}
