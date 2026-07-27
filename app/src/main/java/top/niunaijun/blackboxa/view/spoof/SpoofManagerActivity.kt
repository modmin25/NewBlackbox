package top.niunaijun.blackboxa.view.spoof

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import top.niunaijun.blackboxa.R
import top.niunaijun.blackboxa.databinding.ActivitySpoofManagerBinding
import top.niunaijun.blackboxa.util.SpoofManager
import top.niunaijun.blackboxa.util.ThemeHelper

class SpoofManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpoofManagerBinding
    private var packageName: String = ""

    data class DevicePreset(
        val name: String,
        val model: String,
        val manufacturer: String,
        val brand: String,
        val device: String,
        val product: String,
        val fingerprint: String,
        val hardware: String,
        val board: String,
        val bootloader: String
    )

    private val presets = listOf(
        DevicePreset("Samsung Galaxy S24 Ultra", "SM-S928B", "samsung", "samsung", "e3s", "e3s", "samsung/e3sxxx/e3s:14/UP1A.231005.007/S928BXXS2AXE5:user/release-keys", "qcom", "kalama", "S928BXXS2AXE5"),
        DevicePreset("Pixel 8 Pro", "Pixel 8 Pro", "Google", "google", "husky", "husky", "google/husky/husky:14/AP1A.240505.005/11579264:user/release-keys", "husky", "husky", "husky"),
        DevicePreset("OnePlus 12", "CPH2583", "OnePlus", "OnePlus", "houji", "houji", "OnePlus/CPH2583/CPH2583:14/UP1A.231005.007/1721542902:user/release-keys", "qcom", "kona", "unknown"),
        DevicePreset("Xiaomi 14", "23127PN0CC", "Xiaomi", "Xiaomi", "shennong", "shennong", "Xiaomi/shennong/shennong:14/UP1A.231005.007/V816.0.10.0.UNCCNXM:user/release-keys", "qcom", "taro", "unknown"),
        DevicePreset("Huawei P60 Pro", "MNA-LX9", "HUAWEI", "HUAWEI", "ALN-AL10", "ALN-AL10", "HUAWEI/ALN-AL10/ALN-AL10:12/HUAWEIMNA-LX9/12.0.0.180C605:user/release-keys", "qcom", "kunlun", "unknown"),
        DevicePreset("OPPO Find X7", "PHZ110", "OPPO", "OPPO", "PHZ110", "PHZ110", "OPPO/PHZ110/PHZ110:14/UP1A.231005.007/1709748900:user/release-keys", "qcom", "kunlun", "unknown")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySpoofManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        packageName = intent.getStringExtra("package_name") ?: ""
        if (packageName.isEmpty()) {
            Toast.makeText(this, R.string.spoof_no_package, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.spoof_manager_title)

        setupPresetSpinner()
        loadCurrentConfig()
        setupListeners()
    }

    private fun setupPresetSpinner() {
        val presetNames = presets.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, presetNames)
        binding.spinnerPresets.adapter = adapter
    }

    private fun loadCurrentConfig() {
        val config = SpoofManager.getConfig(this, packageName)
        binding.switchEnable.isChecked = config.enabled
        binding.etImei.setText(config.imei)
        binding.etAndroidId.setText(config.androidId)
        binding.etMac.setText(config.macAddress)
        binding.etSerial.setText(config.serialNumber)
        binding.etSimSerial.setText(config.simSerial)
        binding.etPhoneNumber.setText(config.phoneNumber)
        binding.etModel.setText(config.model)
        binding.etManufacturer.setText(config.manufacturer)
        binding.etBrand.setText(config.brand)
        binding.etDevice.setText(config.device)
        binding.etFingerprint.setText(config.fingerprint)
        binding.etHardware.setText(config.hardware)

        updateFieldStates(config.enabled)
    }

    private fun setupListeners() {
        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            updateFieldStates(isChecked)
        }

        binding.btnGenerateImei.setOnClickListener {
            binding.etImei.setText(SpoofManager.generateRandomImei())
        }

        binding.btnGenerateAndroidId.setOnClickListener {
            binding.etAndroidId.setText(SpoofManager.generateRandomAndroidId())
        }

        binding.btnGenerateMac.setOnClickListener {
            binding.etMac.setText(SpoofManager.generateRandomMac())
        }

        binding.btnApplyPreset.setOnClickListener {
            val pos = binding.spinnerPresets.selectedItemPosition
            if (pos in presets.indices) {
                applyPreset(presets[pos])
            }
        }

        binding.btnSave.setOnClickListener {
            saveConfig()
        }

        binding.btnReset.setOnClickListener {
            SpoofManager.removeConfig(this, packageName)
            loadCurrentConfig()
            Toast.makeText(this, R.string.spoof_reset_done, Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyPreset(preset: DevicePreset) {
        binding.etModel.setText(preset.model)
        binding.etManufacturer.setText(preset.manufacturer)
        binding.etBrand.setText(preset.brand)
        binding.etDevice.setText(preset.device)
        binding.etFingerprint.setText(preset.fingerprint)
        binding.etHardware.setText(preset.hardware)
        binding.switchEnable.isChecked = true
        updateFieldStates(true)
        Toast.makeText(this, getString(R.string.spoof_preset_applied, preset.name), Toast.LENGTH_SHORT).show()
    }

    private fun updateFieldStates(enabled: Boolean) {
        val alpha = if (enabled) 1.0f else 0.5f
        binding.cardIdentity.alpha = alpha
        binding.cardDevice.alpha = alpha
        binding.cardPresets.alpha = alpha
        binding.etImei.isEnabled = enabled
        binding.etAndroidId.isEnabled = enabled
        binding.etMac.isEnabled = enabled
        binding.etSerial.isEnabled = enabled
        binding.etSimSerial.isEnabled = enabled
        binding.etPhoneNumber.isEnabled = enabled
        binding.etModel.isEnabled = enabled
        binding.etManufacturer.isEnabled = enabled
        binding.etBrand.isEnabled = enabled
        binding.etDevice.isEnabled = enabled
        binding.etFingerprint.isEnabled = enabled
        binding.etHardware.isEnabled = enabled
    }

    private fun saveConfig() {
        val config = SpoofManager.SpoofConfig(
            imei = binding.etImei.text?.toString()?.trim() ?: "",
            androidId = binding.etAndroidId.text?.toString()?.trim() ?: "",
            macAddress = binding.etMac.text?.toString()?.trim() ?: "",
            serialNumber = binding.etSerial.text?.toString()?.trim() ?: "",
            simSerial = binding.etSimSerial.text?.toString()?.trim() ?: "",
            phoneNumber = binding.etPhoneNumber.text?.toString()?.trim() ?: "",
            model = binding.etModel.text?.toString()?.trim() ?: "",
            manufacturer = binding.etManufacturer.text?.toString()?.trim() ?: "",
            brand = binding.etBrand.text?.toString()?.trim() ?: "",
            device = binding.etDevice.text?.toString()?.trim() ?: "",
            product = "",
            fingerprint = binding.etFingerprint.text?.toString()?.trim() ?: "",
            hardware = binding.etHardware.text?.toString()?.trim() ?: "",
            board = "",
            bootloader = "",
            display = "",
            host = "",
            type = "",
            tags = "",
            userName = "",
            user = "",
            enabled = binding.switchEnable.isChecked
        )
        SpoofManager.saveConfig(this, packageName, config)
        Toast.makeText(this, R.string.spoof_saved, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun start(context: Context, packageName: String) {
            val intent = Intent(context, SpoofManagerActivity::class.java)
            intent.putExtra("package_name", packageName)
            context.startActivity(intent)
        }
    }
}
