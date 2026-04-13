//
//  getBoolValueByString.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//
import Braintree
import Foundation

func getBoolValueByString(value: String?, defaultValue: Bool) -> Bool {
  switch value {
  case "true":
    return true
  case "false":
    return false
  default:
    return defaultValue
  }
}

func getLocaleCodeValueByString(localeCode: String?) -> BTPayPalLocaleCode {
    guard let code = localeCode?.lowercased().replacingOccurrences(of: "-", with: "_") else { 
        return .en_US 
    }
    
    switch code {
    case "da_dk": return .da_DK
    case "de_de": return .de_DE
    case "en_au": return .en_AU
    case "en_gb": return .en_GB
    case "en_us": return .en_US
    case "es_es": return .es_ES
    case "es_xc": return .es_XC
    case "fr_ca": return .fr_CA
    case "fr_fr": return .fr_FR
    case "fr_xc": return .fr_XC
    case "it_it": return .it_IT
    case "ja_jp": return .ja_JP
    case "ko_kr": return .ko_KR
    case "nl_nl": return .nl_NL
    case "no_no": return .no_NO
    case "pl_pl": return .pl_PL
    case "pt_br": return .pt_BR
    case "pt_pt": return .pt_PT
    case "ru_ru": return .ru_RU
    case "sv_se": return .sv_SE
    case "th_th": return .th_TH
    case "tr_tr": return .tr_TR
    case "zh_cn": return .zh_CN
    case "zh_hk": return .zh_HK
    case "zh_tw": return .zh_TW
    case "zh_xc": return .zh_XC
    default: return .en_US
    }
}