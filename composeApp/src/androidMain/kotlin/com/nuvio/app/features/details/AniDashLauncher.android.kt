package com.nuvio.app.features.details

import android.content.Intent
import android.net.Uri
import com.nuvio.app.features.settings.AppIconPlatform

actual object AniDashLauncher {
    actual fun launchAniDash(title: String, id: String) {
        val context = AppIconPlatform.appContext ?: return
        val pm = context.packageManager

        val candidatePackages = listOf(
            "com.anidash.anime",
            "com.anidash.anime.debug",
            "com.anidash.app",
        )

        for (pkg in candidatePackages) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.putExtra("anime_title", title)
                launchIntent.putExtra("anime_id", id)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return
            }

            // Direct launcher alias resolution
            val candidateAliases = listOf(
                "$pkg.LauncherDynamic",
                "$pkg.LauncherDark",
                "$pkg.LauncherLight",
                "$pkg.MainActivity",
            )
            for (alias in candidateAliases) {
                try {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        setClassName(pkg, alias)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("anime_title", title)
                        putExtra("anime_id", id)
                    }
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                        return
                    }
                } catch (_: Throwable) {}
            }
        }

        // Try launching via custom AniDash URI scheme
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("anidash://anime?title=${Uri.encode(title)}&id=${Uri.encode(id)}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (deepLinkIntent.resolveActivity(pm) != null) {
            context.startActivity(deepLinkIntent)
            return
        }

        // If not installed on the device, open AniDash download releases
        val storeIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/anshdeepofficial1/AniDash/releases")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(storeIntent)
    }
}
