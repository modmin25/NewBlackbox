package top.niunaijun.blackboxa.view.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import top.niunaijun.blackboxa.util.InjectionUtil
import top.niunaijun.blackboxa.util.ThemeHelper
import top.niunaijun.blackboxa.view.list.ListViewModel
import top.niunaijun.blackboxa.view.onboarding.OnboardingActivity

class WelcomeActivity : AppCompatActivity() {

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        jump()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        previewInstalledAppList()
        jump()
    }

    private fun jump() {
        if (OnboardingActivity.isCompleted(this)) {
            MainActivity.start(this)
        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
        finish()
    }

    private fun previewInstalledAppList(){
        val viewModel = ViewModelProvider(this,InjectionUtil.getListFactory()).get(ListViewModel::class.java)
        viewModel.previewInstalledList()
    }
}
