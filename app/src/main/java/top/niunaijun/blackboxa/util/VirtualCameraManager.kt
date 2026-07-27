package top.niunaijun.blackboxa.util

import android.content.Context
import android.content.SharedPreferences

object VirtualCameraManager {

    const val MODE_OFF = 0
    const val MODE_DISABLE = 1
    const val MODE_LOCAL = 2
    const val MODE_NETWORK = 3

    private const val PREFS_NAME = "virtual_camera_prefs"
    private const val KEY_CAMERA_MODE = "camera_mode"
    private const val KEY_VIDEO_PATH = "camera_video_path"
    private const val KEY_VIDEO_URI = "camera_video_uri"
    private const val KEY_NETWORK_URL = "camera_network_url"
    private const val KEY_AUDIO_ENABLED = "camera_audio_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setCameraMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_CAMERA_MODE, mode).apply()
    }

    fun getCameraMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_CAMERA_MODE, MODE_OFF)
    }

    fun setVideoPath(context: Context, path: String) {
        getPrefs(context).edit().putString(KEY_VIDEO_PATH, path).apply()
    }

    fun getVideoPath(context: Context): String? {
        return getPrefs(context).getString(KEY_VIDEO_PATH, null)
    }

    fun setVideoUri(context: Context, uri: String) {
        getPrefs(context).edit().putString(KEY_VIDEO_URI, uri).apply()
    }

    fun getVideoUri(context: Context): String? {
        return getPrefs(context).getString(KEY_VIDEO_URI, null)
    }

    fun setNetworkUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_NETWORK_URL, url).apply()
    }

    fun getNetworkUrl(context: Context): String? {
        return getPrefs(context).getString(KEY_NETWORK_URL, null)
    }

    fun setAudioEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUDIO_ENABLED, enabled).apply()
    }

    fun isAudioEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUDIO_ENABLED, false)
    }

    fun getModeName(mode: Int): String {
        return when (mode) {
            MODE_DISABLE -> "Disable"
            MODE_LOCAL -> "Local"
            MODE_NETWORK -> "Network"
            else -> "Off"
        }
    }

    fun isCameraIntercepted(context: Context): Boolean {
        return getCameraMode(context) != MODE_OFF
    }
}
