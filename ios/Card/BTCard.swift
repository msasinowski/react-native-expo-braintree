import Braintree
import Foundation
func prepareCardData(options: [String: String]) -> BTCard {
    let card = BTCard(
        number: options["number"] ?? "",
        expirationMonth: options["expirationMonth"] ?? "",
        expirationYear: options["expirationYear"] ?? "",
        cvv: options["cvv"] ?? ""
    )
    return card
}
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
