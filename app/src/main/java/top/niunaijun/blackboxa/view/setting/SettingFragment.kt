package top.niunaijun.blackboxa.view.setting

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.app.AppManager
import top.niunaijun.blackboxa.util.LanguageHelper
import top.niunaijun.blackboxa.util.ThemeHelper
import top.niunaijun.blackboxa.util.toast
import top.niunaijun.blackboxa.view.gms.GmsManagerActivity

class SettingFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)

        initLanguage()
        initTheme()
        initGms()

        invalidHideState {
            val rootHidePreference: Preference = (findPreference("root_hide")!!)
            val hideRoot = AppManager.mBlackBoxLoader.hideRoot()
            rootHidePreference.setDefaultValue(hideRoot)
            rootHidePreference
        }

        invalidHideState {
            val daemonPreference: Preference = (findPreference("daemon_enable")!!)
            val mDaemonEnable = AppManager.mBlackBoxLoader.daemonEnable()
            daemonPreference.setDefaultValue(mDaemonEnable)
            daemonPreference
        }

        invalidHideState {
            val vpnPreference: Preference = (findPreference("use_vpn_network")!!)
            val mUseVpnNetwork = AppManager.mBlackBoxLoader.useVpnNetwork()
            vpnPreference.setDefaultValue(mUseVpnNetwork)
            vpnPreference
        }

        invalidHideState {
            val disableFlagSecurePreference: Preference = (findPreference("disable_flag_secure")!!)
            val mDisableFlagSecure = AppManager.mBlackBoxLoader.disableFlagSecure()
            disableFlagSecurePreference.setDefaultValue(mDisableFlagSecure)
            disableFlagSecurePreference
        }

        initSendLogs()
    }

    private fun initLanguage() {
        val languagePreference: ListPreference? = findPreference("app_language")
        languagePreference?.let {
            it.value = LanguageHelper.getLanguage(requireContext())
            it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            it.setOnPreferenceChangeListener { _, newValue ->
                val lang = newValue as String
                LanguageHelper.setLanguage(requireContext(), lang)
                toast(R.string.restart_module)
                true
            }
        }
    }

    private fun initTheme() {
        val themePreference: ListPreference? = findPreference("app_theme")
        themePreference?.let {
            it.value = ThemeHelper.getTheme(requireContext())
            it.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
            it.setOnPreferenceChangeListener { _, newValue ->
                val theme = newValue as String
                ThemeHelper.setTheme(requireContext(), theme)
                ThemeHelper.applyTheme(requireActivity())
                toast(R.string.theme_restart)
                true
            }
        }
    }

    private fun initGms() {
        val gmsManagerPreference: Preference = (findPreference("gms_manager")!!)

        if (BlackBoxCore.get().isSupportGms) {
            gmsManagerPreference.setOnPreferenceClickListener {
                GmsManagerActivity.start(requireContext())
                true
            }
        } else {
            gmsManagerPreference.summary = getString(R.string.no_gms)
            gmsManagerPreference.isEnabled = false
        }
    }

    private fun invalidHideState(block: () -> Preference) {
        val pref = block()
        pref.setOnPreferenceChangeListener { preference, newValue ->
            val tmpHide = (newValue == true)
            when (preference.key) {
                "root_hide" -> {
                    AppManager.mBlackBoxLoader.invalidHideRoot(tmpHide)
                }
                "daemon_enable" -> {
                    AppManager.mBlackBoxLoader.invalidDaemonEnable(tmpHide)
                }
                "use_vpn_network" -> {
                    AppManager.mBlackBoxLoader.invalidUseVpnNetwork(tmpHide)
                }
                "disable_flag_secure" -> {
                    AppManager.mBlackBoxLoader.invalidDisableFlagSecure(tmpHide)
                }
            }
            toast(R.string.restart_module)
            return@setOnPreferenceChangeListener true
        }
    }

    private fun initSendLogs() {
        val sendLogsPreference: Preference? = findPreference("send_logs")
        sendLogsPreference?.setOnPreferenceClickListener {
            it.isEnabled = false
            BlackBoxCore.get()
                .sendLogs(
                    "Manual Log Upload from Settings",
                    true,
                    object : BlackBoxCore.LogSendListener {
                        override fun onSuccess() {
                            activity?.runOnUiThread { sendLogsPreference.isEnabled = true }
                        }

                        override fun onFailure(error: String?) {
                            activity?.runOnUiThread { sendLogsPreference.isEnabled = true }
                        }
                    }
                )
            toast("Sending logs... (Check notifications for status)")
            true
        }
    }
}
