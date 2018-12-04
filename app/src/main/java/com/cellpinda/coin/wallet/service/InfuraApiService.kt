package com.cellpinda.coin.wallet.service

import android.content.Context
import com.cellpinda.coin.wallet.BuildConfig
import com.cellpinda.coin.wallet.common.util.ConnectivityInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService

/**
 * Created by https://github.com/method76 on 2017-11-08.
 */
class InfuraApiService {

    companion object {

        var TAG: String = "InfuraApiService"

        fun balanceOf(addr: String): org.web3j.abi.datatypes.Function {
            return org.web3j.abi.datatypes.Function(
                    "balanceOf",
                    listOf(Address(addr)), //
                    listOf<TypeReference<*>>(object : TypeReference<Uint256>() {
                    }))
        }

        @Throws(Exception::class)
        fun callSmartContractFunction(function: Function, contractAddress: String, addr: String, ctx: Context): String {
            val encodedFunction = FunctionEncoder.encode(function)
            val response = getWeb3j(ctx).ethCall(
                    Transaction.createEthCallTransaction(addr, contractAddress, encodedFunction),
                    DefaultBlockParameterName.LATEST)
                    .sendAsync().get()
            return response.getValue()
        }

        fun getWeb3j(ctx: Context): Web3j {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val httpClient = OkHttpClient.Builder().addInterceptor(interceptor)
                    .addInterceptor(ConnectivityInterceptor(ctx)).build()
            return Web3jFactory.build(HttpService(BuildConfig.INFURA_URL, httpClient, false))
        }

    }

}