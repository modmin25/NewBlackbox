package top.niunaijun.blackboxa.view.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.databinding.ActivityVirtualCameraBinding
import top.niunaijun.blackboxa.util.ThemeHelper
import top.niunaijun.blackboxa.util.VirtualCameraManager

class VirtualCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVirtualCameraBinding

    private val pickVideoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = VirtualCameraManager.copyVideoToInternal(this, it)
            if (localPath != null) {
                VirtualCameraManager.setVideoUri(this, it.toString())
                VirtualCameraManager.setVideoPath(this, localPath)
                binding.tvVideoPath.text = it.lastPathSegment ?: it.toString()
            } else {
                Toast.makeText(this, "Failed to copy video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityVirtualCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupFilterSpinner()
        loadCurrentSettings()
        setupListeners()
    }

    private fun setupFilterSpinner() {
        val filters = arrayOf(
            getString(R.string.filter_none),
            getString(R.string.filter_grayscale),
            getString(R.string.filter_sepia),
            getString(R.string.filter_invert),
            getString(R.string.filter_brightness),
            getString(R.string.filter_contrast),
            getString(R.string.filter_blur)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
        binding.spinnerFilter.adapter = adapter
    }

    private fun loadCurrentSettings() {
        val mode = VirtualCameraManager.getCameraMode(this)
        when (mode) {
            VirtualCameraManager.MODE_OFF -> binding.rbOff.isChecked = true
            VirtualCameraManager.MODE_DISABLE -> binding.rbDisable.isChecked = true
            VirtualCameraManager.MODE_LOCAL -> binding.rbLocal.isChecked = true
            VirtualCameraManager.MODE_NETWORK -> binding.rbNetwork.isChecked = true
        }
        updateVisibility(mode)

        VirtualCameraManager.getVideoUri(this)?.let { uri ->
            binding.tvVideoPath.text = Uri.parse(uri).lastPathSegment ?: uri
        }

        VirtualCameraManager.getNetworkUrl(this)?.let { url ->
            binding.etUrl.setText(url)
        }

        binding.switchAudio.isChecked = VirtualCameraManager.isAudioEnabled(this)
        binding.switchLoop.isChecked = VirtualCameraManager.isLoopVideo(this)
        binding.spinnerFilter.setSelection(VirtualCameraManager.getFilter(this))
        updateStatusText(mode)
    }

    private fun setupListeners() {
        binding.rgMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rbOff -> VirtualCameraManager.MODE_OFF
                R.id.rbDisable -> VirtualCameraManager.MODE_DISABLE
                R.id.rbLocal -> VirtualCameraManager.MODE_LOCAL
                R.id.rbNetwork -> VirtualCameraManager.MODE_NETWORK
                else -> VirtualCameraManager.MODE_OFF
            }
            updateVisibility(mode)
        }

        binding.btnPickVideo.setOnClickListener {
            pickVideoLauncher.launch("video/*")
        }

        binding.btnSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun updateVisibility(mode: Int) {
        val showLocal = mode == VirtualCameraManager.MODE_LOCAL
        val showNetwork = mode == VirtualCameraManager.MODE_NETWORK
        val showExtra = mode != VirtualCameraManager.MODE_OFF

        binding.cardLocal.visibility = if (showLocal) android.view.View.VISIBLE else android.view.View.GONE
        binding.cardNetwork.visibility = if (showNetwork) android.view.View.VISIBLE else android.view.View.GONE
        binding.cardAudio.visibility = if (showExtra) android.view.View.VISIBLE else android.view.View.GONE
        binding.cardLoop.visibility = if (showExtra) android.view.View.VISIBLE else android.view.View.GONE
        binding.cardFilter.visibility = if (showExtra) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun saveSettings() {
        val mode = when (binding.rgMode.checkedRadioButtonId) {
            R.id.rbOff -> VirtualCameraManager.MODE_OFF
            R.id.rbDisable -> VirtualCameraManager.MODE_DISABLE
            R.id.rbLocal -> VirtualCameraManager.MODE_LOCAL
            R.id.rbNetwork -> VirtualCameraManager.MODE_NETWORK
            else -> VirtualCameraManager.MODE_OFF
        }

        if (mode == VirtualCameraManager.MODE_LOCAL && VirtualCameraManager.getVideoUri(this) == null) {
            Toast.makeText(this, R.string.virtual_camera_select_video_first, Toast.LENGTH_SHORT).show()
            return
        }

        if (mode == VirtualCameraManager.MODE_NETWORK) {
            val url = binding.etUrl.text?.toString()?.trim() ?: ""
            if (url.isEmpty()) {
                Toast.makeText(this, R.string.virtual_camera_enter_url, Toast.LENGTH_SHORT).show()
                return
            }
            VirtualCameraManager.setNetworkUrl(this, url)
        }

        VirtualCameraManager.setCameraMode(this, mode)
        VirtualCameraManager.setAudioEnabled(this, binding.switchAudio.isChecked)
        VirtualCameraManager.setLoopVideo(this, binding.switchLoop.isChecked)
        VirtualCameraManager.setFilter(this, binding.spinnerFilter.selectedItemPosition)

        updateStatusText(mode)
        Toast.makeText(this, R.string.virtual_camera_saved, Toast.LENGTH_SHORT).show()
    }

    private fun updateStatusText(mode: Int) {
        val statusText = when (mode) {
            VirtualCameraManager.MODE_OFF -> getString(R.string.virtual_camera_status_off)
            VirtualCameraManager.MODE_DISABLE -> getString(R.string.virtual_camera_status_disabled)
            VirtualCameraManager.MODE_LOCAL -> getString(R.string.virtual_camera_status_local)
            VirtualCameraManager.MODE_NETWORK -> getString(R.string.virtual_camera_status_network)
            else -> getString(R.string.virtual_camera_status_off)
        }
        binding.btnStatus.text = statusText
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
