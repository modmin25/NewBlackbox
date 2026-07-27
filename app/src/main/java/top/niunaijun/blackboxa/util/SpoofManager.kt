package top.niunaijun.blackboxa.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.util.UUID

object SpoofManager {

    private const val PREFS_NAME = "spoof_manager_prefs"
    private const val KEY_SPOOF_DATA = "spoof_data"

    data class SpoofConfig(
        val imei: String = "",
        val androidId: String = "",
        val macAddress: String = "",
        val serialNumber: String = "",
        val simSerial: String = "",
        val phoneNumber: String = "",
        val model: String = "",
        val manufacturer: String = "",
        val brand: String = "",
        val device: String = "",
        val product: String = "",
        val fingerprint: String = "",
        val hardware: String = "",
        val board: String = "",
        val bootloader: String = "",
        val display: String = "",
        val host: String = "",
        val type: String = "",
        val tags: String = "",
        val userName: String = "",
        val user: String = "",
        val enabled: Boolean = false
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getAllConfigs(context: Context): MutableMap<String, SpoofConfig> {
        val map = mutableMapOf<String, SpoofConfig>()
        val raw = getPrefs(context).getString(KEY_SPOOF_DATA, null) ?: return map
        try {
            val json = JSONObject(raw)
            for (key in json.keys()) {
                val obj = json.getJSONObject(key)
                map[key] = SpoofConfig(
                    imei = obj.optString("imei", ""),
                    androidId = obj.optString("androidId", ""),
                    macAddress = obj.optString("macAddress", ""),
                    serialNumber = obj.optString("serialNumber", ""),
                    simSerial = obj.optString("simSerial", ""),
                    phoneNumber = obj.optString("phoneNumber", ""),
                    model = obj.optString("model", ""),
                    manufacturer = obj.optString("manufacturer", ""),
                    brand = obj.optString("brand", ""),
                    device = obj.optString("device", ""),
                    product = obj.optString("product", ""),
                    fingerprint = obj.optString("fingerprint", ""),
                    hardware = obj.optString("hardware", ""),
                    board = obj.optString("board", ""),
                    bootloader = obj.optString("bootloader", ""),
                    display = obj.optString("display", ""),
                    host = obj.optString("host", ""),
                    type = obj.optString("type", ""),
                    tags = obj.optString("tags", ""),
                    userName = obj.optString("userName", ""),
                    user = obj.optString("user", ""),
                    enabled = obj.optBoolean("enabled", false)
                )
            }
        } catch (_: Exception) {}
        return map
    }

    private fun saveAllConfigs(context: Context, configs: Map<String, SpoofConfig>) {
        val json = JSONObject()
        for ((key, config) in configs) {
            val obj = JSONObject()
            obj.put("imei", config.imei)
            obj.put("androidId", config.androidId)
            obj.put("macAddress", config.macAddress)
            obj.put("serialNumber", config.serialNumber)
            obj.put("simSerial", config.simSerial)
            obj.put("phoneNumber", config.phoneNumber)
            obj.put("model", config.model)
            obj.put("manufacturer", config.manufacturer)
            obj.put("brand", config.brand)
            obj.put("device", config.device)
            obj.put("product", config.product)
            obj.put("fingerprint", config.fingerprint)
            obj.put("hardware", config.hardware)
            obj.put("board", config.board)
            obj.put("bootloader", config.bootloader)
            obj.put("display", config.display)
            obj.put("host", config.host)
            obj.put("type", config.type)
            obj.put("tags", config.tags)
            obj.put("userName", config.userName)
            obj.put("user", config.user)
            obj.put("enabled", config.enabled)
            json.put(key, obj)
        }
        getPrefs(context).edit().putString(KEY_SPOOF_DATA, json.toString()).apply()
    }

    fun getConfig(context: Context, packageName: String): SpoofConfig {
        return getAllConfigs(context)[packageName] ?: SpoofConfig()
    }

    fun saveConfig(context: Context, packageName: String, config: SpoofConfig) {
        val configs = getAllConfigs(context)
        configs[packageName] = config
        saveAllConfigs(context, configs)
    }

    fun removeConfig(context: Context, packageName: String) {
        val configs = getAllConfigs(context)
        configs.remove(packageName)
        saveAllConfigs(context, configs)
    }

    fun getAllPackages(context: Context): List<Pair<String, SpoofConfig>> {
        return getAllConfigs(context).toList()
    }

    fun getImei(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.imei.isNotEmpty()) config.imei
        else generateDeterministicImei(packageName)
    }

    fun getAndroidId(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.androidId.isNotEmpty()) config.androidId
        else generateDeterministicAndroidId(packageName)
    }

    fun getMacAddress(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.macAddress.isNotEmpty()) config.macAddress
        else "ac:62:5a:82:65:c4"
    }

    fun getSerialNumber(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.serialNumber.isNotEmpty()) config.serialNumber
        else generateDeterministicSerial(packageName)
    }

    fun getSimSerial(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.simSerial.isNotEmpty()) config.simSerial
        else generateDeterministicSerial(packageName + "_sim")
    }

    fun getPhoneNumber(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.phoneNumber.isNotEmpty()) config.phoneNumber else ""
    }

    fun getModel(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.model.isNotEmpty()) config.model
        else android.os.Build.MODEL
    }

    fun getManufacturer(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.manufacturer.isNotEmpty()) config.manufacturer
        else android.os.Build.MANUFACTURER
    }

    fun getBrand(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.brand.isNotEmpty()) config.brand
        else android.os.Build.BRAND
    }

    fun getDevice(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.device.isNotEmpty()) config.device
        else android.os.Build.DEVICE
    }

    fun getProduct(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.product.isNotEmpty()) config.product
        else android.os.Build.PRODUCT
    }

    fun getFingerprint(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.fingerprint.isNotEmpty()) config.fingerprint
        else android.os.Build.FINGERPRINT
    }

    fun getHardware(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.hardware.isNotEmpty()) config.hardware
        else android.os.Build.HARDWARE
    }

    fun getBoard(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.board.isNotEmpty()) config.board
        else android.os.Build.BOARD
    }

    fun getBootloader(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.bootloader.isNotEmpty()) config.bootloader
        else android.os.Build.BOOTLOADER
    }

    fun getDisplay(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.display.isNotEmpty()) config.display
        else android.os.Build.DISPLAY
    }

    fun getHost(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.host.isNotEmpty()) config.host
        else android.os.Build.HOST
    }

    fun getType(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.type.isNotEmpty()) config.type
        else android.os.Build.TYPE
    }

    fun getTags(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.tags.isNotEmpty()) config.tags
        else android.os.Build.TAGS
    }

    fun getUserName(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.userName.isNotEmpty()) config.userName
        else android.os.Build.USER
    }

    fun getUser(context: Context, packageName: String): String {
        val config = getConfig(context, packageName)
        return if (config.enabled && config.user.isNotEmpty()) config.user
        else android.os.Build.USER
    }

    fun generateRandomImei(): String {
        val rand = java.util.Random()
        val prefix = longArrayOf(3528840, 3530000, 3540000, 3560000)
        var imei = prefix[rand.nextInt(prefix.size)].toString()
        while (imei.length < 14) {
            imei += rand.nextInt(10)
        }
        var sum = 0
        for (i in imei.indices) {
            var digit = imei[i].digitToInt()
            if (i % 2 == 1) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
        }
        val checkDigit = (10 - (sum % 10)) % 10
        return imei + checkDigit
    }

    fun generateRandomAndroidId(): String {
        return UUID.randomUUID().toString().replace("-", "").take(16).uppercase()
    }

    fun generateRandomMac(): String {
        val rand = java.util.Random()
        val bytes = ByteArray(6)
        rand.nextBytes(bytes)
        bytes[0] = (bytes[0].toInt() and 0xFE).toByte()
        return bytes.joinToString(":") { String.format("%02x", it) }
    }

    fun generateRandomSerial(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..16).map { chars.random() }.joinToString("")
    }

    private fun generateDeterministicImei(packageName: String): String {
        val hash = packageName.hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL
        var imei = (350000000000000L + (hash % 1000000000000L)).toString()
        while (imei.length < 14) {
            imei = "0$imei"
        }
        var sum = 0
        for (i in imei.indices) {
            var digit = imei[i].digitToInt()
            if (i % 2 == 1) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
        }
        val checkDigit = (10 - (sum % 10)) % 10
        return imei + checkDigit
    }

    private fun generateDeterministicAndroidId(packageName: String): String {
        val md5 = java.security.MessageDigest.getInstance("MD5")
        val digest = md5.digest(packageName.toByteArray())
        return digest.joinToString("") { String.format("%02x", it) }.take(16).uppercase()
    }

    private fun generateDeterministicSerial(packageName: String): String {
        val md5 = java.security.MessageDigest.getInstance("MD5")
        val digest = md5.digest(packageName.toByteArray())
        return digest.joinToString("") { String.format("%02x", it) }.take(16).uppercase()
    }
}
