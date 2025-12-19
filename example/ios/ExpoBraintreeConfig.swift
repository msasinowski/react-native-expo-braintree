//
//  ExpoBraintreeConfig.swift
//

import Braintree
import Foundation

public final class ExpoBraintreeConfig {

    private init() {}

    public static var paymentURLScheme: String {
        let bundleIdentifier = Bundle.main.bundleIdentifier ?? ""
        return bundleIdentifier + ".braintree"
    }

    public static func handleUrl(url: URL) -> Bool {
        return BTAppContextSwitcher.sharedInstance.handleOpen(url)
    }
}
