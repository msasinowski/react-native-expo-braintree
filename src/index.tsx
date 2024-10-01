import { NativeModules, Platform } from 'react-native';
import {
  type RequestOneTimePaymentOptions,
  type RequestBillingAgreementOptions,
  type BTPayPalAccountNonceResult,
  type BTPayPalError,
  type BTPayPalGetDeviceDataResult,
  type BTCardTokenizationNonceResult,
  type TokenizeCardOptions,
  type ThreeDSecureCheckOptions,
  type BTCardTokenization3DSNonceResult,
} from './types';

const LINKING_ERROR =
  `The package 'expo-braintree' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const ExpoBraintree = NativeModules.ExpoBraintree
  ? NativeModules.ExpoBraintree
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
      ExpoBraintree.requestBillingAgreement(options);
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
      await ExpoBraintree.requestOneTimePayment(options);
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
      await ExpoBraintree.getDeviceDataFromDataCollector(clientToken);
    return result;
  } catch (ex: unknown) {
    return ex as BTPayPalError;
  }
}

export async function tokenizeCardData(
  options: TokenizeCardOptions
): Promise<BTCardTokenization3DSNonceResult | BTPayPalError> {
  try {
    const result: BTCardTokenization3DSNonceResult =
      await ExpoBraintree.tokenizeCardData(options);
    return result;
  } catch (ex: unknown) {
    return ex as BTPayPalError;
  }
}

export async function request3DSecurePaymentCheck(
  options: ThreeDSecureCheckOptions
): Promise<any | BTPayPalError> {
  try {
    const result: BTCardTokenizationNonceResult =
      await ExpoBraintree.request3DSecurePaymentCheck(options);
    return result;
  } catch (ex: unknown) {
    return ex as BTPayPalError;
  }
}

export * from './types';
