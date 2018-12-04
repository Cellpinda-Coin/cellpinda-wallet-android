package com.cellpinda.coin.wallet.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.cellpinda.coin.wallet.common.constant.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by https://github.com/method76 on 2017-11-06.
 */
class CellpindaFCMService: FirebaseMessagingService() {

    private val TAG: String = this.javaClass.simpleName

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
//        Log.d(TAG, "From: " + remoteMessage!!.from!!)
        if (remoteMessage!!.data.isNotEmpty()) {
//            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            val msg = parseMessage(remoteMessage.data)
            sendNotification(msg[0], msg[1])
        }
    }

//    INTENT_PURETX           = 'S'
//    INTENT_BUY_TOKEN        = 'T'
//    INTENT_BONUS_TOKEN      = 'B'
//    INTENT_MANUAL_PAY       = 'M'
//    INTENT_POINT_EXCHANGE   = 'P'
    // title: P, body: P|0.005|101
    private fun parseMessage(msg: Map<String, String>): Array<String?> {
        val ret = arrayOfNulls<String?>(2)
        msg["title"]?:return ret
        msg["body"]?:return ret

        val msgType: Char = msg["title"]!!.toCharArray()[0]
        if (!AVAILABLE_TYPE.contains(msgType)) { return ret }

        val body: List<String> = msg["body"]!!.split("|")
        if (body.size<3) { return ret }
        ret[0] = when (msgType) {
            INTENT_BUY_TOKEN -> getString(R.string.txt_title_notification_t)
            INTENT_PURETX -> getString(R.string.txt_title_notification_s)
            // ? 코인 입금/출금 중입니다 / ? 코인 입금/출금이 완료되었습니다
            INTENT_BONUS_TOKEN -> getString(R.string.txt_title_notification_b)
            // ? 셀핀다코인 지급 처리 중 입니다 / ? 셀핀다코인 지급이 완료되었습니다
            INTENT_MANUAL_PAY -> getString(R.string.txt_title_notification_m)
            // ? 셀핀다코인 지급 처리 중 입니다 / ? 셀핀다코인 지급이 완료되었습니다
            INTENT_POINT_EXCHANGE -> getString(R.string.txt_title_notification_p)
            // ? 셀핀다코인을 ? 포인트로 환전 진행 중 입니다
            else -> getString(R.string.txt_title_notification_s)
        }

        // msgs = 상태|수신량|송신량|암호화폐심볼
        ret[1] = when (body[0].toCharArray()[0]) {
            STAT_DEPOSITING  -> getString(R.string.txt_buy_stat_depo, body[2])
            STAT_WITHDRAWING -> getString(R.string.txt_buy_stat_with, body[2])
            STAT_PROGRESS    -> getString(R.string.txt_buy_stat_with, body[2])
            STAT_COMPLETED   -> getString(R.string.txt_buy_stat_comp, body[2])
            STAT_FAILED      -> getString(R.string.txt_buy_stat_fail, body[2])
            else -> ""
        }
        return ret
    }

    /**
     * D/W/C/F
     */
    private fun sendNotification(title: String?, messageBody: String?) {

        title?:return
        messageBody?:return

        // 계정없으면 노티 안함: Elvis Operator
        WalletLocalService.loadAccount(this)?:return

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("intent", "txs")
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT)
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        val notificationBuilder =
                NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.default_notification_channel_name)
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())

    }


}