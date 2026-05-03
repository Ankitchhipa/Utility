package com.itl.commonres

import android.view.View

/**
 * **Sets a click listener on a View that prevents rapid consecutive clicks.**
 *
 * @param onLegitClick The callback function to be invoked on a legitimate click.
 * @author Ankit Chhipa
 */

fun View.setSafeClickListener(onLegitClick: (View) -> Unit) {
    var lastClickedTime: Long = 0
    this.setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickedTime < 1000) {
            return@setOnClickListener
        }
        lastClickedTime = currentTime
        onLegitClick(this)
    }
}