import Braintree
import Foundation
func prepare3DSecureData(options: [String: String]) -> BTThreeDSecureRequest {
    let postalAddress = BTThreeDSecurePostalAddress(
        givenName: options["givenName"],
        surname: options["surName"],
        streetAddress: options["streetAddress"],
        extendedAddress: options["extendedAddress"],
        line3: nil,
        locality: options["city"],
        region: options["region"],
        postalCode: options["postalCode"],
        countryCodeAlpha2: options["countryCodeAlpha2"],
        phoneNumber: options["phoneNumber"]
    )
    let additionalInfo = BTThreeDSecureAdditionalInformation(
        accountAgeIndicator: nil,
        accountChangeDate: nil,
        accountChangeIndicator: nil,
        accountCreateDate: nil,
        accountID: nil,
        accountPurchases: nil,
        accountPwdChangeDate: nil,
        accountPwdChangeIndicator: nil,
        addCardAttempts: nil,
        addressMatch: nil,
        authenticationIndicator: nil,
        deliveryEmail: nil,
        deliveryTimeframe: nil,
        fraudActivity: nil,
        giftCardAmount: nil,
        giftCardCount: nil,
        giftCardCurrencyCode: nil,
        installment: nil,
        ipAddress: nil,
        orderDescription: nil,
        paymentAccountAge: nil,
        paymentAccountIndicator: nil,
        preorderDate: nil,
        preorderIndicator: nil,
        productCode: nil,
        purchaseDate: nil,
        recurringEnd: nil,
        recurringFrequency: nil,
        reorderIndicator: nil,
        sdkMaxTimeout: nil,
        shippingAddress: postalAddress,
        shippingAddressUsageDate: nil,
        shippingAddressUsageIndicator: nil,
        shippingMethodIndicator: nil,
        shippingNameIndicator: nil,
        taxAmount: nil,
        transactionCountDay: nil,
        transactionCountYear: nil,
        userAgent: nil,
        workPhoneNumber: nil
    )
    let threeDSecureRequest = BTThreeDSecureRequest(
        amount: options["amount"] ?? "0.00",
        nonce: options["nonce"] ?? "",
        additionalInformation: additionalInfo,
        billingAddress: postalAddress,
        email: options["email"]
    )
    return threeDSecureRequest
}
func prepare3DSecureNonceResult(tokenizedCard: BTCardNonce) -> NSDictionary {
    let result = NSMutableDictionary()
    result["nonce"] = tokenizedCard.nonce
    let info = tokenizedCard.threeDSecureInfo
    let infoDict = NSMutableDictionary()
    infoDict["liabilityShifted"] = info.liabilityShifted
    infoDict["liabilityShiftPossible"] = info.liabilityShiftPossible
    infoDict["wasVerified"] = info.wasVerified
    infoDict["status"] = info.status
    result["threeDSecureInfo"] = infoDict
    return result
}
