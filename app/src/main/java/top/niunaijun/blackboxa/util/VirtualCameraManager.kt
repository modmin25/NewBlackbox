package top.niunaijun.blackboxa.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

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
    private const val KEY_LOOP_VIDEO = "camera_loop_video"
    private const val KEY_FILTER = "camera_filter"
    private const val KEY_PREVIEW_WIDTH = "camera_preview_width"
    private const val KEY_PREVIEW_HEIGHT = "camera_preview_height"

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

    fun setLoopVideo(context: Context, loop: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_LOOP_VIDEO, loop).apply()
    }

    fun isLoopVideo(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_LOOP_VIDEO, true)
    }

    fun setFilter(context: Context, filter: Int) {
        getPrefs(context).edit().putInt(KEY_FILTER, filter).apply()
    }

    fun getFilter(context: Context): Int {
        return getPrefs(context).getInt(KEY_FILTER, 0)
    }

    fun setPreviewSize(context: Context, width: Int, height: Int) {
        getPrefs(context).edit()
            .putInt(KEY_PREVIEW_WIDTH, width)
            .putInt(KEY_PREVIEW_HEIGHT, height)
            .apply()
    }

    fun getPreviewWidth(context: Context): Int {
        return getPrefs(context).getInt(KEY_PREVIEW_WIDTH, 1280)
    }

    fun getPreviewHeight(context: Context): Int {
        return getPrefs(context).getInt(KEY_PREVIEW_HEIGHT, 720)
    }

    fun copyVideoToInternal(context: Context, sourceUri: Uri): String? {
        return try {
            val videoDir = File(context.filesDir, "virtual_camera")
            if (!videoDir.exists()) videoDir.mkdirs()
            val destFile = File(videoDir, "selected_video.mp4")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
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
