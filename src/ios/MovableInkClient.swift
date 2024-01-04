import Foundation
import MovableInk

@objc(MovableInkClient)
class MovableInkClient: CDVPlugin {
  enum Errors: Error {
    case invalidArguments
  }
  
  private var deeplinkListener: String?
  
  override func pluginInitialize() {
    super.pluginInitialize()
    
    MIClient.start { [weak self] result in
      guard let self, let deeplinkListener = self.deeplinkListener else { return }
      
      switch result {
      case let .success(url):
        let pluginResult = CDVPluginResult(
          status: CDVCommandStatus_OK,
          messageAs: url
        )
        
        pluginResult?.setKeepCallbackAs(true)
        self.commandDelegate?.send(pluginResult, callbackId: deeplinkListener)
        
      default:
        break
      }
    }
  }
  
  @objc(start:)
  func start(command: CDVInvokedUrlCommand) {
    deeplinkListener = command.callbackId
  }

  @objc(retrieveStoredDeeplink:)
  func retrieveStoredDeeplink(command: CDVInvokedUrlCommand) {
    let pluginResult = CDVPluginResult(
      status: CDVCommandStatus_OK,
      messageAs: MIClient.storedDeeplink ?? ""
    )
    
    self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
  }
  
  @objc(resolveUrl:)
  func resolveUrl(command: CDVInvokedUrlCommand) {
    let callbackId = command.callbackId
    
    guard let urlString = command.argument(at: 0) as? String, let url = URL(string: urlString) else {
      let pluginResult = CDVPluginResult(
        status: CDVCommandStatus_ERROR
      )
      
      self.commandDelegate?.send(pluginResult, callbackId: callbackId)
      
      return
    }
                                                        
    Task {
      do {
        let resolved = try await MIClient.resolve(url: url)
        let pluginResult = CDVPluginResult(
          status: CDVCommandStatus_OK,
          messageAs: resolved.absoluteString
        )
        
        self.commandDelegate?.send(pluginResult, callbackId: callbackId)
      } catch {
        let pluginResult = CDVPluginResult(
          status: CDVCommandStatus_ERROR
        )
        
        self.commandDelegate?.send(pluginResult, callbackId: callbackId)
      }
    }
  }

  @objc(checkPasteboardOnInstall:)
  public func checkPasteboardOnInstall(command: CDVInvokedUrlCommand) {
    let callbackId = command.callbackId

    Task {
      let value = await MIClient.checkPasteboardOnInstall()
      let pluginResult = CDVPluginResult(
        status: CDVCommandStatus_OK,
        messageAs: value?.absoluteString
      )
      self.commandDelegate.send(pluginResult, callbackId: callbackId)
    }
  }
  
  private func guardProperties(command: CDVInvokedUrlCommand) throws -> [String: Any] {
    guard let properties = command.argument(at: 0) as? [String: Any] else {
      let pluginResult = CDVPluginResult(
        status: CDVCommandStatus_ERROR
      )
      
      self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
      
      throw Errors.invalidArguments
    }
    
    return properties
  }
  
  @objc(productSearched:)
  public func productSearched(command: CDVInvokedUrlCommand) {
    guard let properties = try? guardProperties(command: command) else {
      return
    }
    
    MIClient.productSearched(properties)
  }
  
  @objc(productViewed:)
  public func productViewed(command: CDVInvokedUrlCommand) {
    guard let properties = try? guardProperties(command: command) else {
      return
    }
    
    MIClient.productViewed(properties)
  }
  
  @objc(productAdded:)
  public func productAdded(command: CDVInvokedUrlCommand) {
    guard let properties = try? guardProperties(command: command) else {
      return
    }
    
    MIClient.productAdded(properties)
  }

  @objc(productRemoved:)
  public func productRemoved(command: CDVInvokedUrlCommand) {
    guard let properties = try? guardProperties(command: command) else {
      return
    }
    
    MIClient.productRemoved(properties)
  }
  
  @objc(orderCompleted:)
  public func orderCompleted(command: CDVInvokedUrlCommand) {
    guard let properties = try? guardProperties(command: command) else {
      return
    }
    
    MIClient.orderCompleted(properties)
  }
  
  @objc(categoryViewed:)
  public func categoryViewed(command: CDVInvokedUrlCommand) {
    guard let properties = try? guardProperties(command: command) else {
      return
    }
    
    MIClient.categoryViewed(properties)
  }
  
  @objc(logEvent:)
  public func logEvent(command: CDVInvokedUrlCommand) {
    guard let name = command.argument(at: 0) as? String,
          let properties = command.argument(at: 1) as? [String: Any]
    else {
      let pluginResult = CDVPluginResult(
        status: CDVCommandStatus_ERROR
      )
      
      self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
      
      return
    }
    
    MIClient.logEvent(name: name, properties: properties)
  }
  
  @objc(identifyUser:)
  public func identifyUser(command: CDVInvokedUrlCommand) {
    MIClient.identifyUser()
  }
  
  @objc(setMIU:)
  public func setMIU(command: CDVInvokedUrlCommand) {
    guard let value = command.argument(at: 0) as? String
    else {
      let pluginResult = CDVPluginResult(
        status: CDVCommandStatus_ERROR
      )
      
      self.commandDelegate?.send(pluginResult, callbackId: command.callbackId)
      
      return
    }
    
    MIClient.setMIU(value)
  }
}
