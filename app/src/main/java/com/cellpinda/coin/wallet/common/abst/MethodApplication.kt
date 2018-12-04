package com.cellpinda.coin.wallet.common.abst

import android.app.Application
import com.google.android.gms.analytics.Tracker
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Created by https://github.com/method76 on 2017-11-13.
 */
class MethodApplication : Application() {

//    var locked: Boolean = false
//    private lateinit var sAnalytics: GoogleAnalytics
    private lateinit var sTracker: Tracker
    var locked = true

    override fun onCreate() {
        super.onCreate()
//        sAnalytics = GoogleAnalytics.getInstance(this)
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
    }

    /**
     * Gets the default [Tracker] for this [Application].
     * @return tracker
     */
//    @Synchronized
//    fun getDefaultTracker(): Tracker {
//        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
//        if (sTracker == null) {
//            sTracker = sAnalytics.newTracker(R.xml.global_tracker)
//        }
//        return sTracker
//    }

    companion object {
        val app: MethodApplication = MethodApplication()
        fun getInstance(): MethodApplication {
            return app
        }

    }
}