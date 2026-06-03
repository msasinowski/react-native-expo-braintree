import Braintree
func getPaymentMethodUsageByString(paymentMethodUsage: String?) -> BTVenmoPaymentMethodUsage {
    switch paymentMethodUsage {
    case "multiUse":
        return .multiUse
    case "singleUse":
        return .singleUse
    default:
        return .multiUse
    }
}
