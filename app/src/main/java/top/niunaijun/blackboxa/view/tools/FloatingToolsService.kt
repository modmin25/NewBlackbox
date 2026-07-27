package top.niunaijun.blackboxa.view.tools

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.app.App

class FloatingToolsService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var isExpanded = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createFloatingButton()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {}
        }
    }

    private fun createFloatingButton() {
        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.view_floating_tools, null)

        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        params.gravity = Gravity.TOP or Gravity.END
        params.x = 16
        params.y = 200

        windowManager?.addView(floatingView, params)

        setupDragBehavior(floatingView!!, params)
        setupToolButtons()
        toggleTools(false)
    }

    private fun setupDragBehavior(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val dx = Math.abs(event.rawX - initialTouchX)
                    val dy = Math.abs(event.rawY - initialTouchY)
                    if (dx < 10 && dy < 10) {
                        toggleTools(!isExpanded)
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX - (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun toggleTools(expand: Boolean) {
        isExpanded = expand
        floatingView?.let { view ->
            val toolsContainer = view.findViewById<LinearLayout>(R.id.btn_app_cloner)?.parent as? LinearLayout
            val title = view.findViewById<android.widget.TextView>(R.id.tools_title)

            if (expand) {
                title?.visibility = View.VISIBLE
                toolsContainer?.visibility = View.VISIBLE
                view.alpha = 0f
                view.animate().alpha(1f).setDuration(200).start()
            } else {
                title?.visibility = View.GONE
                toolsContainer?.visibility = View.GONE
            }
        }
    }

    private fun setupToolButtons() {
        floatingView?.let { view ->
            view.findViewById<View>(R.id.btn_app_cloner)?.setOnClickListener {
                Toast.makeText(App.getContext(), R.string.tool_app_cloner, Toast.LENGTH_SHORT).show()
                toggleTools(false)
            }
            view.findViewById<View>(R.id.btn_virtual_camera)?.setOnClickListener {
                try {
                    val intent = Intent(App.getContext(), top.niunaijun.blackboxa.view.camera.VirtualCameraActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    App.getContext().startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(App.getContext(), R.string.virtual_camera_title, Toast.LENGTH_SHORT).show()
                }
                toggleTools(false)
            }
            view.findViewById<View>(R.id.btn_file_manager)?.setOnClickListener {
                Toast.makeText(App.getContext(), R.string.tool_file_manager, Toast.LENGTH_SHORT).show()
                toggleTools(false)
            }
            view.findViewById<View>(R.id.btn_memory_booster)?.setOnClickListener {
                Toast.makeText(App.getContext(), R.string.memory_booster_started, Toast.LENGTH_SHORT).show()
                Runtime.getRuntime().gc()
                toggleTools(false)
            }
            view.findViewById<View>(R.id.btn_privacy_guard)?.setOnClickListener {
                Toast.makeText(App.getContext(), R.string.privacy_guard_active, Toast.LENGTH_SHORT).show()
                toggleTools(false)
            }
        }
    }
}
