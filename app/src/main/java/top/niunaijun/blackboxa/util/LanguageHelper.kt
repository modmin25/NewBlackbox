package top.niunaijun.blackboxa.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LanguageHelper {

    private const val PREF_NAME = "app_language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setLanguage(context: Context, languageCode: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, languageCode).apply()
        updateResources(context, languageCode)
    }

    fun getLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, "system") ?: "system"
    }

    fun applyLanguage(activity: Activity) {
        val language = getLanguage(activity)
        updateResources(activity, language)
    }

    private fun updateResources(context: Context, languageCode: String) {
        val locale = if (languageCode == "system") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                Locale.getDefault()
            }
        } else {
            parseLocale(languageCode)
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        }

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    private fun parseLocale(code: String): Locale {
        return when (code) {
            "zh-rCN" -> Locale("zh", "CN")
            "zh-rTW" -> Locale("zh", "TW")
            else -> Locale(code)
        }
    }

    fun applyLanguageBeforeAttach(base: Context): Context {
        val language = getPrefs(base).getString(KEY_LANGUAGE, "system") ?: "system"
        if (language == "system") return base

        val locale = parseLocale(language)
        Locale.setDefault(locale)

        val config = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            base.createConfigurationContext(config)
        } else {
            base.resources.updateConfiguration(config, base.resources.displayMetrics)
            base
        }
    }
}
