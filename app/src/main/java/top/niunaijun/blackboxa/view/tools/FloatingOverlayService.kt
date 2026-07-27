package top.niunaijun.blackboxa.view.tools

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import top.niunaijun.blackboxa.R

class FloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isExpanded = false

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            x = 0
        }

        overlayView = createOverlayView()
        windowManager?.addView(overlayView, params)
    }

    private fun createOverlayView(): View {
        val container = FrameLayout(this)

        val mainBtn = createCircleButton("\u2699", 48f)
        val lp = FrameLayout.LayoutParams(dpToPx(48), dpToPx(48))
        container.addView(mainBtn, lp)

        val expandedPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            setBackgroundColor(0xE6222222.toInt())
        }

        val actions = listOf(
            "\uD83D\uDCF1 Spoof ID" to "spoof_id",
            "\uD83D\uDCF7 Camera" to "camera",
            "\uD83D\uDCCD Location" to "location",
            "\u26A1 Restart" to "restart"
        )

        for ((label, action) in actions) {
            val btn = TextView(this).apply {
                text = label
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 14f
                setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
                setOnClickListener { handleAction(action) }
            }
            expandedPanel.addView(btn)
        }

        val panelLp = FrameLayout.LayoutParams(dpToPx(160), FrameLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.END
            marginEnd = dpToPx(56)
        }
        container.addView(expandedPanel, panelLp)

        var initialY = 0f
        var initialTouchY = 0f
        var moved = false

        mainBtn.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    initialTouchY = event.rawY
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - initialY
                    if (Math.abs(event.rawY - initialTouchY) > 10) {
                        moved = true
                    }
                    val params = overlayView?.layoutParams as? WindowManager.LayoutParams
                    params?.y = (params?.y ?: 0) - dy.toInt()
                    windowManager?.updateViewLayout(overlayView, params)
                    initialY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved) {
                        isExpanded = !isExpanded
                        expandedPanel.visibility = if (isExpanded) View.VISIBLE else View.GONE
                    }
                    true
                }
                else -> false
            }
        }

        return container
    }

    private fun handleAction(action: String) {
        isExpanded = false
        when (action) {
            "spoof_id" -> {
                sendBroadcast(Intent("top.niunaijun.blackboxa.OVERLAY_ACTION").putExtra("action", "spoof_id"))
            }
            "camera" -> {
                sendBroadcast(Intent("top.niunaijun.blackboxa.OVERLAY_ACTION").putExtra("action", "camera"))
            }
            "location" -> {
                sendBroadcast(Intent("top.niunaijun.blackboxa.OVERLAY_ACTION").putExtra("action", "location"))
            }
            "restart" -> {
                sendBroadcast(Intent("top.niunaijun.blackboxa.OVERLAY_ACTION").putExtra("action", "restart"))
            }
        }
    }

    private fun createCircleButton(text: String, sizeDp: Float): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 20f
            setTextColor(0xFFFFFFFF.toInt())
            val bg = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xFF6200EE.toInt())
                setStroke(dpToPx(2), 0xFFBB86FC.toInt())
            }
            background = bg
            gravity = Gravity.CENTER
            val size = dpToPx(sizeDp.toInt())
            minWidth = size
            minHeight = size
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {}
    }
}
