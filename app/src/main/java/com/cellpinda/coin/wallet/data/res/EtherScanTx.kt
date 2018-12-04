package com.cellpinda.coin.wallet.data.res

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
data class EtherScanTx (
    val timeStamp: Long,
    val hash: String,
    val from: String,
    val to: String,
    val value: String,
    val isError: String,
    val txreceipt_status: String,
    val contractAddress: String
//    val blockNumber: Int,
//    val nonce: String,
//    val blockHash: String,
//    val transactionIndex: String,
//    val gas: String,
//    val gasPrice: String,
//    val cumulativeGasUsed: String,
//    val gasUsed: String,
//    val input: String,
//    val confirmations: String
)