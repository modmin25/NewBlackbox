package top.niunaijun.blackboxa.view.apps

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.bean.AppInfo
import top.niunaijun.blackboxa.databinding.ItemAppBinding
import android.util.Log
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.Color

class AppsAdapter : RVHolderFactory() {

    companion object {
        private const val TAG = "AppsAdapter"
        private const val MAX_ICON_SIZE = 96
        private val DEFAULT_ICON_COLOR = Color.parseColor("#CCCCCC")
    }

    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return try {
            AppsVH(inflate(R.layout.item_app, parent))
        } catch (e: Exception) {
            Log.e(TAG, "Error creating ViewHolder: ${e.message}")
            FallbackAppsVH(inflate(R.layout.item_app, parent))
        }
    }

    class AppsVH(itemView: View) : RVHolder<AppInfo>(itemView) {
        val binding = ItemAppBinding.bind(itemView)
        private var currentIcon: Drawable? = null

        override fun setContent(item: AppInfo, isSelected: Boolean, payload: Any?) {
            try {
                setIconSafely(item.icon, item.packageName)

                binding.name.text = item.name ?: "Unknown App"

                if (item.isXpModule) {
                    binding.cornerLabel.visibility = View.VISIBLE
                } else {
                    binding.cornerLabel.visibility = View.INVISIBLE
                }

                if (item.versionName.isNotBlank()) {
                    binding.versionLabel.visibility = View.VISIBLE
                    binding.versionLabel.text = item.versionName
                } else {
                    binding.versionLabel.visibility = View.GONE
                }

                if (item.cloneCount > 1) {
                    binding.statusIndicator.visibility = View.VISIBLE
                } else {
                    binding.statusIndicator.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting content for ${item.packageName}: ${e.message}")
                setSafeDefaults()
            }
        }

        private fun setIconSafely(icon: Drawable?, packageName: String) {
            try {
                if (icon != null) {
                    binding.icon.setImageDrawable(optimizeIcon(icon))
                } else {
                    binding.icon.setImageDrawable(createDefaultIcon())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set icon for $packageName: ${e.message}")
                binding.icon.setImageDrawable(createDefaultIcon())
            }
        }

        private fun optimizeIcon(icon: Drawable): Drawable {
            return try {
                if (icon is BitmapDrawable) {
                    val bitmap = icon.bitmap
                    if (bitmap.width > MAX_ICON_SIZE || bitmap.height > MAX_ICON_SIZE) {
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, MAX_ICON_SIZE, MAX_ICON_SIZE, true)
                        BitmapDrawable(itemView.resources, scaledBitmap)
                    } else {
                        icon
                    }
                } else {
                    icon
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error optimizing icon: ${e.message}")
                icon
            }
        }

        private fun createDefaultIcon(): Drawable {
            return try {
                ColorDrawable(DEFAULT_ICON_COLOR)
            } catch (e: Exception) {
                ColorDrawable(Color.GRAY)
            }
        }

        private fun setSafeDefaults() {
            try {
                binding.icon.setImageDrawable(createDefaultIcon())
                binding.name.text = "Unknown App"
                binding.cornerLabel.visibility = View.INVISIBLE
                binding.versionLabel.visibility = View.GONE
                binding.statusIndicator.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error setting safe defaults: ${e.message}")
            }
        }
    }

    class FallbackAppsVH(itemView: View) : RVHolder<AppInfo>(itemView) {
        val binding = ItemAppBinding.bind(itemView)

        override fun setContent(item: AppInfo, isSelected: Boolean, payload: Any?) {
            try {
                binding.icon.setImageDrawable(ColorDrawable(DEFAULT_ICON_COLOR))
                binding.name.text = item.name ?: "Unknown App"
                binding.cornerLabel.visibility = View.INVISIBLE
                binding.versionLabel.visibility = View.GONE
                binding.statusIndicator.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error in fallback ViewHolder: ${e.message}")
            }
        }
    }
}
