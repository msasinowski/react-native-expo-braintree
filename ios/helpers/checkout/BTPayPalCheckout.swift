import Braintree
import Foundation

/**
 * Prepares a BTPayPalCheckoutRequest with the strict v7 initializer order.
 */
func prepareBTPayPalCheckoutRequest(options: [String: String]) -> BTPayPalCheckoutRequest {
    let intent = getBTPayPalCheckoutIntentByString(intent: options["intent"])
    let userAction = getBTPayPalRequestUserActionByString(userAction: options["userAction"])
    
    let checkoutRequest = BTPayPalCheckoutRequest(
        amount: options["amount"] ?? "0.00",
        intent: intent,
        userAction: userAction,
        offerPayLater: getBoolValueByString(value: options["offerPayLater"], defaultValue: false),
        amountBreakdown: nil,
        billingAgreementDescription: options["billingAgreementDescription"], // Must precede currencyCode
        contactInformation: nil,
        contactPreference: .none,
        currencyCode: options["currencyCode"],
        displayName: options["displayName"],
        enablePayPalAppSwitch: true,
        isShippingAddressEditable: getBoolValueByString(value: options["isShippingAddressEditable"], defaultValue: false),
        isShippingAddressRequired: getBoolValueByString(value: options["isShippingAddressRequired"], defaultValue: false),
        landingPageType: .none,
        lineItems: nil,
        localeCode: getLocaleCodeValueByString(localeCode: options["localeCode"]),
        merchantAccountID: options["merchantAccountID"],
        recurringBillingDetails: nil,
        recurringBillingPlanType: nil,
        requestBillingAgreement: getBoolValueByString(value: options["requestBillingAgreement"], defaultValue: false),
        riskCorrelationID: nil,
        shippingAddressOverride: nil,
        shippingCallbackURL: nil,
        shopperSessionID: nil,
        userAuthenticationEmail: options["userAuthenticationEmail"],
        userPhoneNumber: nil,
        offerCredit: getBoolValueByString(value: options["offerCredit"], defaultValue: false)
    )

    return checkoutRequest
}

func getBTPayPalCheckoutIntentByString(intent: String?) -> BTPayPalRequestIntent {
    switch intent {
    case "sale": return .sale
    case "order": return .order
    default: return .authorize
    }
}

func getBTPayPalRequestUserActionByString(userAction: String?) -> BTPayPalRequestUserAction {
    return (userAction == "commit") ? .payNow : .none
}