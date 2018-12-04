package com.cellpinda.coin.wallet.service

import com.cellpinda.coin.wallet.data.param.RecaptchaReq
import com.cellpinda.coin.wallet.data.res.RecaptchaRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET


/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
interface RecapchaAPIService {

    @GET("/siteverify")
    fun siteverify(@Body params: RecaptchaReq): Call<RecaptchaRes>

}
