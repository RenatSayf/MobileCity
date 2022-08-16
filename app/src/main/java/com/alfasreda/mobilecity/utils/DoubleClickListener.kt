package com.alfasreda.mobilecity.utils

import android.os.SystemClock
import android.view.View


abstract class DoubleClickListener(
    private var doubleClickQualificationSpanInMillis: Long = DEFAULT_QUALIFICATION_SPAN,
    private var timestampLastClick: Long = 0
) : View.OnClickListener {

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - timestampLastClick < doubleClickQualificationSpanInMillis) {
            onDoubleClick()
        }
        timestampLastClick = SystemClock.elapsedRealtime()
    }

    abstract fun onDoubleClick()

    companion object {
        // The time in which the second tap should be done in order to qualify as
        // a double click
        private const val DEFAULT_QUALIFICATION_SPAN: Long = 500
    }
}