package org.bubbble.rclock.utils

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * @author Andrew
 * @date 2020/08/04 20:02
 */

inline fun <reified T: Any> T.logger(value: String) {
    Log.d("Andrew", "${this.javaClass.simpleName} -> $value")
}