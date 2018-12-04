package com.cellpinda.coin.wallet.common.util

import android.content.Context
import com.cellpinda.coin.wallet.common.exception.NoConnectivityException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Created by https://github.com/method76 on 2017-11-30.
 */
class ConnectivityInterceptor(private val mContext: Context) : Interceptor {

    @Throws(NoConnectivityException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!MobileUtil.isOnline(mContext)) {
            throw NoConnectivityException()
        }
        val builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }

}