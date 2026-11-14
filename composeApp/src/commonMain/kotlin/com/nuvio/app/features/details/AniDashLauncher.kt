package com.nuvio.app.features.details

expect object AniDashLauncher {
    fun launchAniDash(title: String, id: String, season: Int? = null, episode: Int? = null)
}
