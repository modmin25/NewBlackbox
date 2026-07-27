package top.niunaijun.blackboxa.view.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.chip.ChipGroup
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.util.LanguageHelper
import top.niunaijun.blackboxa.util.ThemeHelper
import top.niunaijun.blackboxa.view.base.BaseActivity
import top.niunaijun.blackboxa.view.main.MainActivity
import com.google.android.material.button.MaterialButton

class OnboardingActivity : BaseActivity() {

    private var selectedTheme = ThemeHelper.THEME_COSMIC
    private var selectedLang = "system"

    companion object {
        private const val PREF_NAME = "onboarding_prefs"
        private const val KEY_COMPLETED = "onboarding_completed"

        fun isCompleted(context: Context): Boolean {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_COMPLETED, false)
        }

        fun start(context: Context) {
            val intent = Intent(context, OnboardingActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        setContentView(R.layout.activity_onboarding)

        setupThemeChips()
        setupLanguageSpinner()
        setupGetStartedButton()
    }

    private fun setupThemeChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.theme_chip_group)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                selectedTheme = when (checkedIds[0]) {
                    R.id.theme_cosmic -> ThemeHelper.THEME_COSMIC
                    R.id.theme_amoled -> ThemeHelper.THEME_AMOLED
                    R.id.theme_neon -> ThemeHelper.THEME_NEON
                    R.id.theme_forest -> ThemeHelper.THEME_FOREST
                    R.id.theme_ocean -> ThemeHelper.THEME_OCEAN
                    R.id.theme_sunset -> ThemeHelper.THEME_SUNSET
                    else -> ThemeHelper.THEME_COSMIC
                }
            }
        }
    }

    private fun setupLanguageSpinner() {
        val spinner = findViewById<Spinner>(R.id.language_spinner)
        val languageEntries = resources.getStringArray(R.array.language_entries)
        val languageValues = resources.getStringArray(R.array.language_values)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languageEntries
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val currentLang = LanguageHelper.getLanguage(this)
        val currentIndex = languageValues.indexOf(currentLang).coerceAtLeast(0)
        spinner.setSelection(currentIndex)

        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedLang = languageValues[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupGetStartedButton() {
        findViewById<MaterialButton>(R.id.btn_get_started).setOnClickListener {
            ThemeHelper.setTheme(this, selectedTheme)
            LanguageHelper.setLanguage(this, selectedLang)

            getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                .putBoolean(KEY_COMPLETED, true)
                .apply()

            Toast.makeText(this, R.string.onboarding_welcome, Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
