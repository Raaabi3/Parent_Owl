package com.example.parent_owl_app

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.DisplayMetrics
import android.view.View
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayOutputStream
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy.OnPixelCopyFinishedListener


class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.example.parent_owl/native"
    private val REQUEST_CODE_ENABLE_ADMIN = 1

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "takeScreenshot" -> takeScreenshot(result)
                "lockScreen" -> lockScreen(result)
                else -> result.notImplemented()
            }
        }
    }

    // Method to take a screenshot and return the image data
    
private fun takeScreenshot(result: MethodChannel.Result) {
    try {
        val rootView = window.decorView.rootView
        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)

        // Use PixelCopy to capture the screen
        PixelCopy.request(
            window,
            bitmap,
            { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    // Convert bitmap to byte array
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    result.success(byteArray)
                } else {
                    result.error("ERROR", "Failed to take screenshot", "PixelCopy failed")
                }
            },
            Handler(Looper.getMainLooper())
        )
    } catch (e: Exception) {
        result.error("ERROR", "Failed to take screenshot", e.toString())
    }
}

    // Method to lock the screen
    private fun lockScreen(result: MethodChannel.Result) {
        if (isDeviceAdminEnabled()) {
            lockDevice()
            result.success(true)
        } else {
            requestDeviceAdminPermission()
            result.success(false)
        }
    }

    private fun isDeviceAdminEnabled(): Boolean {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    private fun lockDevice() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)
        if (devicePolicyManager.isAdminActive(adminComponent)) {
            devicePolicyManager.lockNow()
        }
    }

    private fun requestDeviceAdminPermission() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this@MainActivity, DeviceAdminReceiver::class.java))
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable device admin to lock the screen.")
        }
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == Activity.RESULT_OK) {
                // Device admin enabled, lock the screen
                lockDevice()
            } else {
                // Device admin not enabled
                println("Device admin permission denied")
            }
        }
    }
}