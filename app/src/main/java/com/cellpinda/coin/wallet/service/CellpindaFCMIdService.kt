package com.cellpinda.coin.wallet.service

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class CellpindaFCMIdService: FirebaseInstanceIdService() {

    private val TAG: String = this.javaClass.simpleName

    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        val refreshedToken = FirebaseInstanceId.getInstance().token
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken)
    }

    private fun sendRegistrationToServer(token: String?) {
        WalletLocalService.saveFcmToken(applicationContext, token!!)
        Log.d(TAG, "fcm token registered")
    }

}