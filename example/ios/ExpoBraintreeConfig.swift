import Braintree
import Foundation

public final class ExpoBraintreeConfig {

    private init() {}

    public static var paymentURLScheme: String {
        let bundleIdentifier = Bundle.main.bundleIdentifier ?? ""
        return bundleIdentifier + ".braintree"
    }

    public static func handleUrl(url: URL) -> Bool {
        // Updated for Braintree v7
        return BTAppContextSwitcher.sharedInstance.handleOpen(url)
    }
}
