
import Braintree

func getPaymentMethodUsageByString(paymentMethodUsage: String?) -> BTVenmoPaymentMethodUsage {
  switch localeCode {
  case "multiUse":
    return BTVenmoPaymentMethodUsage.multiUse
  case "singleUse":
    return BTVenmoPaymentMethodUsage.singleUse
  default:
    return BTPayPalLocaleCode.multiUse
  }
}
