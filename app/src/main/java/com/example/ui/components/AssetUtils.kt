package com.example.ui.components

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.R

object AssetUtils {
    @DrawableRes
    fun getDrawableResByName(context: Context, name: String?): Int {
        if (name.isNullOrBlank()) return R.drawable.img_editorial_architecture
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (resId != 0) resId else R.drawable.img_editorial_architecture
    }
}
