package top.niunaijun.blackboxa.view.store

data class StoreItem(
    val id: String,
    val name: String,
    val description: String,
    val packageName: String,
    val version: String,
    val category: String,
    val isInstalled: Boolean = false,
    val iconResId: Int = 0,
    val downloadUrl: String = ""
)
