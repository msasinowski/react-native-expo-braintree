//
//  PaypalRebornConfig.swift
//  PaypalRebornExample
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree
import Foundation

@objc public class PaypalRebornConfig: NSObject {

  @objc(configure)
  public static func configure() {
    BTAppContextSwitcher.sharedInstance.returnURLScheme = self.getPaymentUrlScheme()
  }

  @objc(getPaymentUrlScheme)
  public static func getPaymentUrlScheme() -> String {
    let bundleIdentifier = Bundle.main.bundleIdentifier ?? ""
    return bundleIdentifier + ".braintree"
  }

  @objc(handleUrl:)
  public static func handleUrl(url: URL) -> Bool {
    return BTAppContextSwitcher.sharedInstance.handleOpen(url)
  }
}
