package com.nuvio.app.features.details

import android.content.Intent
import android.net.Uri
import com.nuvio.app.features.settings.AppIconPlatform

actual object AniDashLauncher {
    actual fun launchAniDash(title: String, id: String) {
        val context = AppIconPlatform.appContext ?: return
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage("com.anidash.app")
        if (launchIntent != null) {
            launchIntent.putExtra("anime_title", title)
            launchIntent.putExtra("anime_id", id)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/anshdeepofficial1")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(storeIntent)
        }
    }
}
