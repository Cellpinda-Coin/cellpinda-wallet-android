package com.cellpinda.coin.wallet.service

import com.cellpinda.coin.wallet.data.param.SmsVeriReq
import com.cellpinda.coin.wallet.data.res.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
interface WalletAPIService {

    @POST("app/")
    fun reqSms(@Query("c") c: String, @Query("phone") phone: String): Call<SmsReqRes>

    // SMS 인증 확인요청
    @POST("app/")
    fun confirmSms(@Query("c") c: String, @Query("phone") phone: String, @Query("num") num: String): Call<SmsReqRes>

    @POST("app/")
    fun veriSms(@Body params: SmsVeriReq): Call<SmsReqRes>

    @POST("app/")
    fun veriLogin(@Query("c") c: String, @Query("id") phone: String
                  , @Query("pw") pw: String, @Query("token") token: String): Call<LoginVeriRes>
    // /q/public/app/?c=j&phone=01048187321&name=홍길동&seq=인증index&recommend=추천자분 아이디
    @POST("app/")
    fun createWallet(@Query("c") c: String, @Query("phone") phone: String, @Query("name") name: String
                     , @Query("seq") seq: String, @Query("num") num: String
                     , @Query("recommend") recommend: String?): Call<CreateAccountRes>

    // 세션 1개월 마다 만료
    @POST("app/")
    fun mainData(@Query("c") c: String, @Query("uid") uid: String
                  , @Query("key") key: String): Call<LoginVeriRes>

    /**
     * 이더로 토큰 구매요청
     * app/?c=buy&uid=1111&key=651321safsdf&iamount=구매갯수
     */
    @POST("app/")
    fun buyTokenViaETH(@Query("c") c: String, @Query("uid") uid: String
                 , @Query("key") key: String, @Query("iamount") iamount: String): Call<CPDBuyRes>

    @POST("app/")
    fun tokenReg(@Query("addr") addr: String, @Query("q") q: String): Call<SmsReqRes>

    @POST("app/")
    fun recommendeeList(@Query("c") c: String, @Query("uid") uid: String
                        , @Query("key") key: String): Call<RecommendeeRes>

}
