import Braintree

/**
 * Maps a string value to the BTVenmoPaymentMethodUsage enum.
 * Updated for v7 naming conventions (lowercase enum cases).
 */
func getPaymentMethodUsageByString(paymentMethodUsage: String?) -> BTVenmoPaymentMethodUsage {
    switch paymentMethodUsage {
    case "multiUse":
        return .multiUse
    case "singleUse":
        return .singleUse
    default:
        return .multiUse // Default for Vault/Billing Agreement
    }
}