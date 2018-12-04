package com.cellpinda.coin.wallet.service

import com.cellpinda.coin.wallet.data.param.ButtonLinkGenReq
import com.cellpinda.coin.wallet.data.res.ButtonLinkGenRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
interface TossButtonAPIService {

    @POST("linkgen-api/link")
    fun getButtonLink(@Body param: ButtonLinkGenReq): Call<ButtonLinkGenRes>

}
