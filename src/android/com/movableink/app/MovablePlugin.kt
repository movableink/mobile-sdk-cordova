
package com.movableink.app

import android.content.Intent
import android.util.Log
import com.movableink.app.*
import com.movableink.inked.MIClient
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

const val TAG = "MovablePlugin"

class MovablePlugin : CordovaPlugin() {
  private var deepLinkListener: CallbackContext? = null

  override fun initialize(cordova: CordovaInterface?, webView: CordovaWebView?) {
    super.initialize(cordova, webView)
    handleIntent(cordova?.activity?.intent)
    MIClient.start()
  }

  private fun handleIntent(intent: Intent?) {
    intent?.let {
      if (it.action == Intent.ACTION_VIEW) {
        val url = it.data.toString()
        resolveURL(url)
      }
    }
  }
  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    cordova.activity.intent = intent
    handleIntent(intent)
  }

  private fun resolveURL(url: String) {
    MIClient.resolveUrlAsync(url) { urlString ->
      urlString?.let {
        val result = PluginResult(PluginResult.Status.OK, it)
        result.keepCallback = true
        deepLinkListener?.sendPluginResult(result)
      }
    }
  }

  override fun execute(
    action: String,
    args: JSONArray,
    callbackContext: CallbackContext,
  ): Boolean {
    when (action) {
      START -> {
        return start(callbackContext)
      }
      SET_MIU -> {
        return setMiu(args)
      }
      ORDER_COMPLETED -> {
        return orderCompleted(args, callbackContext)
      }
      PRODUCT_VIEWED -> {
        return productViewed(args, callbackContext)
      }
      PRODUCT_REMOVED -> {
        return productRemoved(args, callbackContext)
      }
      CATEGORY_VIEWED -> {
        return categoryViewed(args, callbackContext)
      }
      PRODUCT_SEARCHED -> {
        return productSearched(args)
      }
      PRODUCT_ADDED -> {
        return productAdded(args)
      }
      IDENTIFY_USER -> {
        return identifyUser()
      }
      RETRIEVE_LAST_RESOLVED_URL -> {
        return lastResolvedUrl(callbackContext)
      }
      LOG_EVENT -> {
        return logEvent(args)
      }
      CHECK_PASTEBOARD_ON_INSTALL -> {
        return checkPasteboardOnInstall(callbackContext)
      }
      SHOW_IN_APP_MESSAGE -> {
        return showInAppMessage(args, callbackContext)
      }
    }
    return false
  }

  private fun setMiu(parameters: JSONArray): Boolean {
    val miu: String? = try {
      parameters.getString(0)
    } catch (e: JSONException) {
      e.printStackTrace()
      return true
    }

    if (miu.isNullOrEmpty()) {
      return true
    }
    
    MIClient.setMIU(miu)
    return true
  }

  private fun orderCompleted(parameters: JSONArray, callbackContext: CallbackContext): Boolean {
    val properties = parameters.readProperties()
    MIClient.orderCompleted(properties)
    val pluginResult = PluginResult(PluginResult.Status.OK)
    pluginResult.keepCallback = false
    callbackContext.sendPluginResult(pluginResult)
    return true
  }

  private fun productViewed(parameters: JSONArray, callbackContext: CallbackContext): Boolean {
    val properties = parameters.readProperties()
    MIClient.productViewed(properties)
    val pluginResult = PluginResult(PluginResult.Status.OK)
    pluginResult.keepCallback = false
    callbackContext.sendPluginResult(pluginResult)
    return true
  }

  private fun productRemoved(parameters: JSONArray, callbackContext: CallbackContext): Boolean {
    val properties = parameters.readProperties()
    MIClient.productRemoved(properties)
    val pluginResult = PluginResult(PluginResult.Status.OK)
    pluginResult.keepCallback = false
    callbackContext.sendPluginResult(pluginResult)
    return true
  }

  private fun categoryViewed(parameters: JSONArray, callbackContext: CallbackContext): Boolean {
    val properties = parameters.readProperties()
    MIClient.categoryViewed(properties)
    val pluginResult = PluginResult(PluginResult.Status.OK)
    pluginResult.keepCallback = false
    callbackContext.sendPluginResult(pluginResult)
    return true
  }

  private fun productSearched(parameters: JSONArray): Boolean {
    val properties = parameters.readProperties()
    MIClient.productSearched(properties)
    return true
  }

  private fun productAdded(parameters: JSONArray): Boolean {
    val properties = parameters.readProperties()
    MIClient.productAdded(properties)
    return true
  }

  private fun identifyUser(): Boolean {
    MIClient.identifyUser()
    return true
  }

  private fun lastResolvedUrl(callbackContext: CallbackContext): Boolean {
    val url = MIClient.retrieveStoredDeepLink()
    val result = PluginResult(PluginResult.Status.OK, url)
    result.keepCallback = false

    callbackContext.sendPluginResult(result)
    return true
  }

  private fun checkPasteboardOnInstall(callbackContext: CallbackContext): Boolean {
    val url = MIClient.checkPasteboardOnInstall()
    val result = PluginResult(PluginResult.Status.OK, url)
    result.keepCallback = false

    callbackContext.sendPluginResult(result)
    return true
  }

  private fun showInAppMessage(parameters: JSONArray, callbackContext: CallbackContext): Boolean  {
    val url: String? = try {
      parameters.getString(0)
    } catch (e: JSONException) {
      e.printStackTrace()
      return true
    }

    // TODO: implement

    return true
  }

  private fun logEvent(parameters: JSONArray): Boolean {
    val eventName: String? = try {
      parameters.getString(0)
    } catch (e: JSONException) {
      e.printStackTrace()
      return true
    }

    val eventProperties = parameters.readProperties()
    if (eventName.isNullOrEmpty()) {
      return true
    }

    MIClient.logEvent(eventName, eventProperties)
    return true
  }

  private fun start(callback: CallbackContext): Boolean {
    deepLinkListener = callback
    return true
  }

  private fun JSONArray.readProperties(): Map<String, Any?> {
    val map = HashMap<String, Any?>()
    try {
      val jsonString = this.getString(0)
      val jsonObject = JSONObject(jsonString)
      return jsonObject.toMap()
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return map
  }

  @Throws(JSONException::class)
  fun JSONObject.toMap(): Map<String, Any?> {
    val map: MutableMap<String, Any> = HashMap()
    val keys: Iterator<String> = keys()
    while (keys.hasNext()) {
      val key = keys.next()
      var value: Any = get(key)
      if (value is JSONArray) {
        value = toList(value as JSONArray)
      } else if (value is JSONObject) {
        value = (value as JSONObject).toMap()
      }
      map[key] = value
    }
    return map
  }

  @Throws(JSONException::class)
  fun toList(array: JSONArray): List<Any?> {
    val list: MutableList<Any> = ArrayList()
    for (i in 0 until array.length()) {
      var value: Any = array.get(i)
      if (value is JSONArray) {
        value = toList(value)
      } else if (value is JSONObject) {
        value = value.toMap()
      }
      list.add(value)
    }
    return list
  }
}
