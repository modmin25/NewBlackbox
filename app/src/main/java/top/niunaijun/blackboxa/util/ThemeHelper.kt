package top.niunaijun.blackboxa.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration

object ThemeHelper {

    const val THEME_COSMIC = "cosmic"
    const val THEME_AMOLED = "amoled"
    const val THEME_NEON = "neon"
    const val THEME_FOREST = "forest"
    const val THEME_OCEAN = "ocean"
    const val THEME_SUNSET = "sunset"
    const val THEME_DEFAULT = "cosmic"

    private const val PREF_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setTheme(context: Context, theme: String) {
        getPrefs(context).edit().putString(KEY_THEME, theme).apply()
    }

    fun getTheme(context: Context): String {
        return getPrefs(context).getString(KEY_THEME, THEME_DEFAULT) ?: THEME_DEFAULT
    }

    fun applyTheme(activity: Activity) {
        val theme = getTheme(activity)
        val themeResId = getThemeResId(theme)
        activity.setTheme(themeResId)
    }

    fun getThemeResId(theme: String): Int {
        return when (theme) {
            THEME_AMOLED -> top.niunaijun.blackboxa.R.style.Theme_Amoled
            THEME_NEON -> top.niunaijun.blackboxa.R.style.Theme_Neon
            THEME_FOREST -> top.niunaijun.blackboxa.R.style.Theme_Forest
            THEME_OCEAN -> top.niunaijun.blackboxa.R.style.Theme_Ocean
            THEME_SUNSET -> top.niunaijun.blackboxa.R.style.Theme_Sunset
            else -> top.niunaijun.blackboxa.R.style.Theme_BlackBox
        }
    }

    fun getThemeColors(context: Context): Map<String, Int> {
        val ta = context.obtainStyledAttributes(getThemeResId(getTheme(context)), intArrayOf(
            com.google.android.material.R.attr.colorPrimary,
            com.google.android.material.R.attr.colorSecondary
        ))
        val colors = mapOf(
            "primary" to ta.getColor(0, 0),
            "secondary" to ta.getColor(1, 0)
        )
        ta.recycle()
        return colors
    }
}
