//
//  BraintreeConfig.swift
//

import Braintree
import Foundation

public final class BraintreeConfig {

    private init() {}

    public static func configure() {
      BTAppContextSwitcher.sharedInstance.returnURLScheme = self.paymentURLScheme
    }

    public static var paymentURLScheme: String {
        let bundleIdentifier = Bundle.main.bundleIdentifier ?? ""
        return bundleIdentifier + ".braintree"
    }

    public static func handleUrl(url: URL) -> Bool {
        return BTAppContextSwitcher.sharedInstance.handleOpen(url)
    }
}
