import { NativeModules, Platform } from 'react-native';
import {
  type RequestOneTimePaymentOptions,
  type RequestBillingAgreementOptions,
  type BTPayPalAccountNonceResult,
  type BTPayPalError,
  type BTPayPalGetDeviceDataResult,
  type BTCardTokenizationNonceResult,
  type TokenizeCardOptions,
} from './types';

const LINKING_ERROR =
  `The package 'react-native-paypal-reborn' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const PaypalReborn = NativeModules.PaypalReborn
  ? NativeModules.PaypalReborn
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export async function requestBillingAgreement(
  options: RequestBillingAgreementOptions
): Promise<BTPayPalAccountNonceResult | BTPayPalError> {
  try {
    const result: BTPayPalAccountNonceResult =
      PaypalReborn.requestBillingAgreement(options);
    return result;
  } catch (ex: unknown) {
    return ex as BTPayPalError;
  }
}

export async function requestOneTimePayment(
  options: RequestOneTimePaymentOptions
): Promise<BTPayPalAccountNonceResult | BTPayPalError> {
  try {
    const result: BTPayPalAccountNonceResult =
      await PaypalReborn.requestOneTimePayment(options);
    return result;
  } catch (ex: unknown) {
    return ex as BTPayPalError;
  }
}

export async function getDeviceDataFromDataCollector(
  clientToken: string
): Promise<BTPayPalGetDeviceDataResult | BTPayPalError> {
  try {
    const result: BTPayPalGetDeviceDataResult =
      await PaypalReborn.getDeviceDataFromDataCollector(clientToken);
    return result;
  } catch (ex: unknown) {
    return ex as BTPayPalError;
  }
}

export async function tokenizeCardData(
  options: TokenizeCardOptions
): Promise<BTCardTokenizationNonceResult | BTPayPalError> {
  try {
    const result: BTCardTokenizationNonceResult =
      await PaypalReborn.tokenizeCardData(options);
    return result;
  } catch (ex: unknown) {
    return ex as BTPayPalError;
  }
}

export * from './types';
