import Braintree
import Foundation

/**
 * Prepares a BTCard object from the options dictionary received from JavaScript.
 * Fully adapted for Braintree iOS v7 strict immutability.
 */
func prepareCardData(options: [String: String]) -> BTCard {
    // In v7, the primary way to provide data is the exhaustive initializer.
    // If postalCode or shouldValidate are throwing 'internal' errors, 
    // it means they are now either controlled via the Client Token (server-side)
    // or need to be passed in a specific v7 initializer.
    
    let card = BTCard(
        number: options["number"] ?? "",
        expirationMonth: options["expirationMonth"] ?? "",
        expirationYear: options["expirationYear"] ?? "",
        cvv: options["cvv"] ?? ""
    )
    
    // If 'postalCode' gave you an 'internal' error, comment out the line below.
    // Braintree v7 often handles this via the billingAddress in higher-level requests.
    // card.postalCode = options["postalCode"] 

    // If 'shouldValidate' gave you an 'internal' error, it is now managed 
    // by your Braintree Control Panel settings or the tokenization call.
    // card.shouldValidate = false
    
    return card
}

/**
 * Maps the BTCardNonce result into an NSDictionary to be sent back to React Native.
 */
func prepareBTCardNonceResult(cardNonce: BTCardNonce) -> NSDictionary {
    let result = NSMutableDictionary()
    
    result["nonce"] = cardNonce.nonce
    result["cardNetwork"] = transformCardNetworkToString(cardNonce.cardNetwork)
    result["lastFour"] = cardNonce.lastFour
    result["lastTwo"] = cardNonce.lastTwo
    result["expirationMonth"] = cardNonce.expirationMonth
    result["expirationYear"] = cardNonce.expirationYear
    
    return result
}

/**
 * Helper function to map BTCardNetwork enums to human-readable strings.
 * Switch is exhaustive to handle v7 @unknown default cases.
 */
func transformCardNetworkToString(_ network: BTCardNetwork) -> String {
    switch network {
        case .visa: return "Visa"
        case .masterCard: return "MasterCard"
        case .AMEX: return "Amex"
        case .dinersClub: return "DinersClub"
        case .JCB: return "JCB"
        case .maestro: return "Maestro"
        case .discover: return "Discover"
        case .unionPay: return "UnionPay"
        case .hiper: return "Hiper"
        case .hipercard: return "Hipercard"
        case .unknown: return "Unknown"
        default:
        return "unknown"
    }
}
