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
import com.movableink.app.RESOLVE_URL
import com.movableink.app.RETRIEVE_LAST_RESOLVED_URL
import com.movableink.app.SET_MIU
import com.movableink.app.START
import com.movableink.inked.MIClient

import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import org.json.JSONException

class MovablePlugin: CordovaPlugin {
  var mDeepLinkListener: CallbackContext?

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    cordova.getActivity().setIntent(intent)
    handleIntent(intent)
  }
  
  fun initialize(cordova: CordovaInterface?, webView: CordovaWebView?) {
    MIClient.start()
    
    handleIntent(cordova.getActivity().getIntent());
  }
  
  private fun handleIntent(intent: Intent?) {
    if (intent == null) return

    val action = intent.getAction()
    val launchURI = intent.getData()

    if (!Intent.ACTION_VIEW.equals(action) || launchURI == null) return

    val result = PluginResult(PluginResult.Status.OK, resolvedURL)
    
    MIClient.resolveUrlAsync(launchURI) { resolvedURL ->
      val result = PluginResult(PluginResult.Status.OK, resolvedURL)
      result.setKeepCallacK(true)
      
      if (mDeepLinkListener == null) return

      mDeepLinkListener.sendPluginResult(result)
    }
  }

  override fun execute(action: String, args: JSONArray, callback: CallbackContext): Boolean {
    when (action) {
      SET_MIU -> {
        return setMiu(args)
      }
      RESOLVE_URL -> {
        return resolveUrl(args, callback)
      }
      ORDER_COMPLETED -> {
        return orderCompleted(args)
      }
      PRODUCT_VIEWED -> {
        return productViewed(args)
      }
      CATEGORY_VIEWED -> {
        return categoryViewed(args)
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
        return lastResolvedUrl(args, callback)
      }
      LOG_EVENT -> {
        return logEvent(args)
      }
      START -> {
        return start(callback)
      }
      else -> {
        callback.error("Invalid action")
      }
    }
  }
  
  private fun start(callback: CallbackContext): Boolean {
    mDeepLinkListener = callback
    return true
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
  
  private fun identifyUser(): Boolean {
    MIClient.identifyUser()
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
    
    MIClient.logEvent(eventName,eventProperties)
    return true
  }
  
  private fun productSearched(parameters: JSONArray): Boolean {
    val properties = parameters.readProperties()
    MIClient.productSearched(properties)
    return true
  }
  
  private fun productViewed(parameters: JSONArray): Boolean {
    val properties = parameters.readProperties()
    MIClient.productViewed(properties)
    return true
  }
  
  private fun productAdded(parameters: JSONArray): Boolean {
    val properties = parameters.readProperties()
    MIClient.productAdded(properties)
    return true
  }
  
  private fun categoryViewed(parameters: JSONArray): Boolean {
    val properties = parameters.readProperties()
    MIClient.categoryViewed(properties)
    return true
  }
  
  private fun orderCompleted(parameters: JSONArray): Boolean {
    val properties = parameters.readProperties()
    MIClient.orderCompleted(properties)
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
  
  private fun lastResolvedUrl(parameters: JSONArray, callbackContext: CallbackContext): Boolean {
    val url = MIClient.retrieveStoredDeepLink()
    val result = PluginResult(PluginResult.Status.OK, url)
    callbackContext.sendPluginResult(result)
    return true
  }
  
  private fun resolveUrl(parameters: JSONArray, callbackContext: CallbackContext): Boolean {
    val rawURL: String? = try {
      parameters.getString(0)
    } catch (e: JSONException) {
      e.printStackTrace()
      return true
    }
    
    rawURL?.let {
      MIClient.resolveUrlAsync(it) { resolvedURL ->
        val result = PluginResult(PluginResult.Status.OK, resolvedURL)
        callbackContext.sendPluginResult(result)
      }
    }
    
    return true
  }
}
