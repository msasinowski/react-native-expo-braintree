//
//  getBoolValueByString.swift
//  expo-braintree
//
//  Created by Maciej Sasinowski on 28/04/2024.
//

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
