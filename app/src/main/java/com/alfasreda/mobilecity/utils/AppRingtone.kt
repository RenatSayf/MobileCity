package com.alfasreda.mobilecity.utils

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager

object AppRingtone {

    private var ringtone: Ringtone? = null

    fun instance(context: Context): Ringtone? {
        val ringtoneUri =
            RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        return ringtone ?: run {
            ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
            ringtone
        }
    }


}