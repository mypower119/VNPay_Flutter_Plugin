package com.hlsolutions.flutter_hl_vnpay.flutter_hl_vnpay

import android.content.Intent
import androidx.annotation.NonNull
import com.vnpay.authentication.VNP_AuthenticationActivity
import com.vnpay.authentication.VNP_SdkCompletedCallback

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** FlutterHlVnpayPlugin */
class FlutterHlVnpayPlugin : FlutterPlugin, ActivityAware, PluginRegistry.ActivityResultListener, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private var activityBinding: ActivityPluginBinding? = null
    private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.flutterPluginBinding = flutterPluginBinding
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_hl_vnpay")
        channel.setMethodCallHandler(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activityBinding = binding
        binding.addActivityResultListener(this)
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        activityBinding?.removeActivityResultListener(this)
        activityBinding = null
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        this.flutterPluginBinding = binding
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "show") {
            this.handleShow(call)
            result.success(null)
        } else {
            result.notImplemented()
        }
    }

    private fun handleShow(@NonNull call: MethodCall) {
        val params = call.arguments as HashMap<*, *>
        val paymentUrl = params["paymentUrl"] as String
        val scheme = params["scheme"] as String
        val tmnCode = params["tmn_code"] as String
        val intent = Intent(flutterPluginBinding!!.applicationContext, VNP_AuthenticationActivity::class.java).apply {
            putExtra("url", paymentUrl)
            putExtra("scheme", scheme)
            putExtra("tmn_code", tmnCode)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        VNP_AuthenticationActivity.setSdkCompletedCallback(object: VNP_SdkCompletedCallback {
            override fun sdkAction(s: String) {
                //action == AppBackAction
                //Ng?????i d??ng nh???n back t??? sdk ????? quay l???i

                //action == CallMobileBankingApp
                //Ng?????i d??ng nh???n ch???n thanh to??n qua app thanh to??n (Mobile Banking, V??...)
                //l??c n??y app t??ch h???p s??? c???n l??u l???i c??i PNR, khi n??o ng?????i d??ng m??? l???i app t??ch h???p th?? s??? g???i ki???m tra tr???ng th??i thanh to??n c???a PNR ???? xem ???? thanh to??n hay ch??a.

                //action == WebBackAction
                //Ng?????i d??ng nh???n back t??? trang thanh to??n th??nh c??ng khi thanh to??n qua th??? khi g???i ?????n http://sdk.merchantbackapp

                //action == FaildBackAction
                //giao d???ch thanh to??n b??? failed

                //action == SuccessBackAction
                //thanh to??n th??nh c??ng tr??n webview
                val map: HashMap<String, Int> = HashMap<String, Int>() //define empty hashmap
                val resultCode: Int = 0;
                if ("AppBackAction" == s) {
                    map.put("resultCode", -1)
                } else if ("CallMobileBankingApp" == s) {
                    map.put("resultCode", 10)
                } else if ("WebBackAction" == s) {
                    map.put("resultCode", 99)
                } else if ("FaildBackAction" == s) {
                    map.put("resultCode", 98)
                } else if ("SuccessBackAction" == s) {
                    map.put("resultCode", 97)
                }
                channel.invokeMethod("PaymentBack", map)
            }
        })

//        activityBinding?.activity?.startActivityForResult(intent, 99)
        activityBinding?.activity?.startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
      if (requestCode == 99) {
          val map:HashMap<String, Int> = HashMap<String, Int>() //define empty hashmap
          map.put("resultCode", resultCode)
          channel.invokeMethod("PaymentBack", resultCode)
      }
      return false
    }
}
