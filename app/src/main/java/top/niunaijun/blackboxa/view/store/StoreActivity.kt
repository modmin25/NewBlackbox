package top.niunaijun.blackboxa.view.store

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.util.LanguageHelper
import top.niunaijun.blackboxa.util.ThemeHelper
import top.niunaijun.blackboxa.view.base.BaseActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StoreActivity : BaseActivity() {

    private lateinit var storeAdapter: StoreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        setContentView(R.layout.activity_store)

        initToolbar(findViewById<Toolbar>(R.id.toolbar), R.string.store_title, true)
        setupStoreList()
        setupSearch()
        setupCategoryFilter()
    }

    private fun setupStoreList() {
        val items = getStoreItems()
        storeAdapter = StoreAdapter(items.toMutableList()) { item ->
            top.niunaijun.blackboxa.util.toast(getString(R.string.store_installing, item.name))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.store_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = storeAdapter
    }

    private fun setupSearch() {
        val searchInput = findViewById<EditText>(R.id.search_input)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                storeAdapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryFilter() {
        val chipGroup = findViewById<ChipGroup>(R.id.category_chips)
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = findViewById<Chip>(checkedIds[0])
                val category = chip.text.toString()
                if (category == getString(R.string.store_category_all)) {
                    storeAdapter.filter.filter("")
                } else {
                    storeAdapter.filter.filter(category)
                }
            }
        }
    }

    private fun getStoreItems(): List<StoreItem> {
        return listOf(
            StoreItem("1", "MicroG Services", "Open-source replacement for Google Play Services", "org.microg.gms", "0.3.1", getString(R.string.store_category_tools)),
            StoreItem("2", "Shizuku", "API for apps to use system APIs directly", "moe.shizuku.privileged.api", "13.1.0", getString(R.string.store_category_tools)),
            StoreItem("3", "LSPosed", "Xposed framework for Android 8.1+", "org.lsposed.lsposed", "1.9.2", getString(R.string.store_category_tools)),
            StoreItem("4", "Termux", "Terminal emulator with packages", "com.termux", "0.118.0", getString(R.string.store_category_tools)),
            StoreItem("5", "Nova Launcher", "Fast, customizable launcher", "com.teslacoilsw.launcher", "8.0.3", getString(R.string.store_category_tools)),
            StoreItem("6", "Firefox Browser", "Fast, private web browser", "org.mozilla.firefox", "120.0", getString(R.string.store_category_social)),
            StoreItem("7", "VLC Player", "Multimedia player", "org.videolan.vlc", "3.5.1", getString(R.string.store_category_tools)),
            StoreItem("8", "Telegram", "Cloud-based messaging", "org.telegram.messenger", "10.2.8", getString(R.string.store_category_social)),
            StoreItem("9", "Genshin Impact", "Open-world action RPG", "com.miHoYo.GenshinImpact", "4.3.0", getString(R.string.store_category_games)),
            StoreItem("10", "PUBG Mobile", "Battle royale shooter", "com.tencent.ig", "2.9.0", getString(R.string.store_category_games)),
            StoreItem("11", "Minecraft", "Sandbox building game", "com.mojang.minecraftpe", "1.20.40", getString(R.string.store_category_games)),
            StoreItem("12", "F-Droid", "FOSS app repository", "org.fdroid.fdroid", "1.19.0", getString(R.string.store_category_tools))
        )
    }
}
