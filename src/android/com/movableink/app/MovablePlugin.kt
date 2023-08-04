package android.com.movableink.app
import android.content.Intent
import android.util.Log
import com.movableink.app.CATEGORY_VIEWED
import com.movableink.app.IDENTIFY_USER
import com.movableink.app.LOG_EVENT
import com.movableink.app.ORDER_COMPLETED
import com.movableink.app.PRODUCT_ADDED
import com.movableink.app.PRODUCT_SEARCHED
import com.movableink.app.PRODUCT_VIEWED
import com.movableink.app.RETRIEVE_LAST_RESOLVED_URL
import com.movableink.app.SET_MIU
import com.movableink.inked.MIClient
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaInterface
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.apache.cordova.PluginResult
import org.json.JSONArray
import org.json.JSONException

const val TAG = "MovablePlugin"

class MovablePlugin : CordovaPlugin() {

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
    handleIntent(intent)
  }

  private fun resolveURL(url: String): String? {
    var resolvedLink: String? = null
    MIClient.resolveUrlAsync(url) { urlString ->
      // callBack
      resolvedLink = urlString
      Log.d(TAG, "Resolved URL:$resolvedLink ")
//            val result = PluginResult(PluginResult.Status.OK, resolvedLink)
//            callbackContext.sendPluginResult(result)
    }
    return resolvedLink
  }

  override fun execute(
    action: String,
    args: JSONArray,
    callbackContext: CallbackContext,
  ): Boolean {
    when (action) {
      SET_MIU -> {
        return setMiu(args)
      }
      ORDER_COMPLETED -> {
        return orderCompleted(args, callbackContext)
      }
      PRODUCT_VIEWED -> {
        return productViewed(args, callbackContext)
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
  private fun JSONArray.readProperties(): Map<String, Any?> {
    val map = HashMap<String, Any?>()
    try {
      for (i in 0 until length()) {
        val key = getString(i)
        val value = get(i + 1)
        map[key] = value
      }
    } catch (e: JSONException) {
      e.printStackTrace()
    }

    return map
  }
}
