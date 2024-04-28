//
//  getLocaleCodeValueByString.swift
//  react-native-paypal-reborn
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

import Braintree

func getLocaleCodeValueByString(localeCode: String?) -> BTPayPalLocaleCode {
  switch localeCode {
  case "da_DK":
    return BTPayPalLocaleCode.da_DK
  case "de_DE":
    return BTPayPalLocaleCode.de_DE
  case "en_AU":
    return BTPayPalLocaleCode.en_AU
  case "en_GB":
    return BTPayPalLocaleCode.en_GB
  case "en_US":
    return BTPayPalLocaleCode.en_US
  case "es_ES":
    return BTPayPalLocaleCode.es_ES
  case "es_XC":
    return BTPayPalLocaleCode.es_XC
  case "fr_CA":
    return BTPayPalLocaleCode.fr_CA
  case "fr_FR":
    return BTPayPalLocaleCode.fr_FR
  case "fr_XC":
    return BTPayPalLocaleCode.fr_XC
  case "id_ID":
    return BTPayPalLocaleCode.id_ID
  case "it_IT":
    return BTPayPalLocaleCode.it_IT
  case "ja_JP":
    return BTPayPalLocaleCode.ja_JP
  case "ko_KR":
    return BTPayPalLocaleCode.ko_KR
  case "nl_NL":
    return BTPayPalLocaleCode.nl_NL
  case "no_NO":
    return BTPayPalLocaleCode.no_NO
  case "pl_PL":
    return BTPayPalLocaleCode.pl_PL
  case "pt_BR":
    return BTPayPalLocaleCode.pt_BR
  case "ru_RU":
    return BTPayPalLocaleCode.ru_RU
  case "sv_SE":
    return BTPayPalLocaleCode.sv_SE
  case "th_TH":
    return BTPayPalLocaleCode.th_TH
  case "tr_TR":
    return BTPayPalLocaleCode.tr_TR
  case "zh_CN":
    return BTPayPalLocaleCode.zh_CN
  case "zh_TW":
    return BTPayPalLocaleCode.zh_TW
  case "zh_XC":
    return BTPayPalLocaleCode.zh_XC
  default:
    return BTPayPalLocaleCode.en_US
  }
}
