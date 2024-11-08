
import Braintree

func getPaymentMethodUsageByString(paymentMethodUsage: String?) -> BTVenmoPaymentMethodUsage {
  switch paymentMethodUsage {
  case "multiUse":
    return BTVenmoPaymentMethodUsage.multiUse
  case "singleUse":
    return BTVenmoPaymentMethodUsage.singleUse
  default:
    return BTVenmoPaymentMethodUsage.multiUse
  }
}
