package com.cellpinda.coin.wallet.common.util

import android.content.Context
import com.cellpinda.coin.wallet.common.constant.KEY_PREF

/**
 * Created by https://github.com/method76 on 2017-11-07.
 */
class LocalStorage {

    companion object {

        val TAG: String = LocalStorage::class.java.simpleName

        // 값 불러오기
        fun get(ctx: Context, key: String): String {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            return pref.getString(key, "")
        }

        fun getLong(ctx: Context, key: String): Long {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            return pref.getLong(key, 0)
        }

        // 값 저장하기
        fun set(ctx: Context, key: String, value: String) {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(key, value)
            editor.apply()
        }

        fun set(ctx: Context, key: String, value: Long) {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putLong(key, value)
            editor.apply()
        }

        // 값(Key Data) 삭제하기
        fun remove(ctx: Context, key: String) {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.remove(key)
            editor.apply()
        }

        // 값(ALL Data) 삭제하기
        fun clear(ctx: Context) {
            val pref = ctx.applicationContext.getSharedPreferences(KEY_PREF,
                    Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.clear()
            editor.apply()
        }

    }

}