import Braintree
import Foundation

func prepareBTPayPalVaultRequest(options: [String: String]) -> BTPayPalVaultRequest {
    let vaultRequest = BTPayPalVaultRequest(
        enablePayPalAppSwitch: true,
        offerCredit: getBoolValueByString(value: options["offerCredit"], defaultValue: false),
        billingAgreementDescription: options["billingAgreementDescription"],
        displayName: options["displayName"],
        isShippingAddressEditable: getBoolValueByString(value: options["isShippingAddressEditable"], defaultValue: false),
        isShippingAddressRequired: getBoolValueByString(value: options["isShippingAddressRequired"], defaultValue: false),
        landingPageType: .none,
        lineItems: nil,
        localeCode: getLocaleCodeValueByString(localeCode: options["localeCode"]),
        merchantAccountID: options["merchantAccountID"],
        recurringBillingDetails: nil,
        recurringBillingPlanType: .none, // v7 uses Type enum instead of ID string
        riskCorrelationID: nil,
        shippingAddressOverride: nil,
        shopperSessionID: nil,
        userAction: .none,
        userAuthenticationEmail: options["userAuthenticationEmail"],
        userPhoneNumber: nil
    )
    
    return vaultRequest
}

