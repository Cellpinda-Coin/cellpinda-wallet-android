package com.cellpinda.coin.wallet.common.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import android.util.Log
import com.cellpinda.coin.wallet.MainActivity
import com.cellpinda.coin.wallet.R
import com.google.zxing.integration.android.IntentIntegrator
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by https://github.com/method76 on 2017-11-09.
 */
class WalletUtil {

    companion object {

        val TAG: String = "WalletUtil"

        fun copyToClipboard(ctx: Context, text: String) {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("copied text", text)
            clipboard.setPrimaryClip(clip)
        }

        fun copyToClipboardWithLabel(ctx: Context, text: String, label: String) {
            val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
        }

        fun toCurrencyFormat(number: Double): String {
            return String.format("%,.4f", number)
        }
        fun toCurrencyFormat8(number: Double): String {
            val nf = NumberFormat.getInstance()
            return nf.format(number)
        }
        fun cpdToCurrencyFormat8(number: String): String {
            val nf = NumberFormat.getInstance()
            return nf.format(number.toDouble() / 100000000)
        }
        fun toCurrencyFormat8Int(number: Double): String {
            val nf = NumberFormat.getInstance()
            return nf.format(number.toInt())
        }
        fun toCurrencyFomatIntDecimal(number: Double): Array<String> {
            val num = String.format("%,.8f", number)
            val ret = arrayOf(num.substring(0, num.length-9),
                    num.substring(num.length-9, num.length))
            return ret
        }

        fun scanQR(ctx: MainActivity) {
            val it = IntentIntegrator(ctx)
            it.setBarcodeImageEnabled(true)
            it.setOrientationLocked(false);
            it.setBeepEnabled(false)
            it.setPrompt("Scan the QR code of destination address")
            it.initiateScan()
        }

        fun encSha512(plainText: String): String {
            try {
                val md: MessageDigest = MessageDigest.getInstance("SHA-512")
                val bytes: ByteArray = plainText.toByteArray(Charset.forName("UTF-8"))
                md.update(bytes)
                return Base64.encodeToString(md.digest(), Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e(TAG, "SHA512 encoding error, " + e.message)
                return ""
            }
        }

        fun unixToYyyymmdd(ts: Long): String {
            // convert seconds to milliseconds
            val date = java.util.Date(ts * 1000L)
            // the format of your date
            val sdf = java.text.SimpleDateFormat("M월d일 H시m분")
            // give a timezone reference for formatting (see comment at the bottom)
            sdf.timeZone = java.util.TimeZone.getTimeZone("GMT+9")
            return sdf.format(date)
        }

        fun unixToYyyymmddFormal(ts: Long): String {
            // convert seconds to milliseconds
            val date = java.util.Date(ts * 1000L)
            // the format of your date
            val sdf = java.text.SimpleDateFormat("MM월dd일 HH시mm분")
            // give a timezone reference for formatting (see comment at the bottom)
            sdf.timeZone = java.util.TimeZone.getTimeZone("GMT+9")
            return sdf.format(date)
        }

        fun toTimeDiffString(ctx: Context, thedaymilli: Long): String {

            val theday: Calendar    = Calendar.getInstance()
            theday.timeInMillis     = thedaymilli*1000L
            val now: Calendar       = Calendar.getInstance()
            val diffmilli: Long     = now.timeInMillis - theday.timeInMillis
            val monthYearDiff: Int  = (now.get(Calendar.YEAR) - theday.get(Calendar.YEAR)) * 12 + (now.get(Calendar.MONTH) - theday.get(Calendar.MONTH))
            val yearDiff: Int  = Math.floor(monthYearDiff/12.0).toInt()
            val monthDiff: Int = Math.floor(monthYearDiff.toDouble()%12).toInt()
            val dateDiff: Int  = TimeUnit.MILLISECONDS.toDays(diffmilli).toInt()
            val hourDiff: Int  = TimeUnit.MILLISECONDS.toHours(diffmilli-dateDiff*1000*60*60*24).toInt()
            val minDiff: Int   = TimeUnit.MILLISECONDS.toMinutes(diffmilli-dateDiff*1000*60*60*24 - hourDiff*1000*60).toInt()

            var ret = ""

            if (yearDiff>0) {
                ret = "" + yearDiff + " " + ctx.getString(R.string.txt_years)
            } else if (monthDiff>0) {
                ret = "" + monthDiff + " " + ctx.getString(R.string.txt_months)
            } else if (dateDiff>0) {
                ret = "" + dateDiff + " " + ctx.getString(R.string.txt_days)
            } else if (hourDiff>0) {
                ret = "" + hourDiff + " " + ctx.getString(R.string.txt_hours)
            } else if (minDiff>0) {
                ret = "" + minDiff + " " + ctx.getString(R.string.txt_minutes)
            } else {
                ret = ctx.getString(R.string.txt_just_now)
            }
            if (ret!=ctx.getString(R.string.txt_just_now)) {
                ret += " " + ctx.getString(R.string.txt_ago)
            }
            return ret
        }

    }

}