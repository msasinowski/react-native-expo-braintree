import { NativeModules, Platform } from 'react-native';
import type {
  BTCardTokenizationNonceResult,
  BTPayPalAccountNonceResult,
  BTPayPalError,
  BTPayPalGetDeviceDataResult,
  BTVenmoError,
  BTVenmoNonceResult,
  RequestBillingAgreementOptions,
  RequestOneTimePaymentOptions,
  RequestVenmoNonceOptions,
  TokenizeCardOptions,
  BTCardTokenization3DSNonceResult,
  ThreeDSecureCheckOptions,
  BTThreeDError,
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
  clientToken: string,
  hasUserLocationConsent?: boolean,
  riskCorrelationId?: string
): Promise<BTPayPalGetDeviceDataResult | BTPayPalError> {
  try {
    const result: BTPayPalGetDeviceDataResult =
      await ExpoBraintree.getDeviceDataFromDataCollector({
        clientToken,
        hasUserLocationConsent,
        riskCorrelationId,
      });
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
      await ExpoBraintree.tokenizeCardData(options);
    return result;
  } catch (ex: unknown) {
    return ex as BTPayPalError;
  }
}

export async function requestVenmoNonce(
  options: RequestVenmoNonceOptions
): Promise<BTVenmoNonceResult | BTVenmoError> {
  try {
    const result: BTVenmoNonceResult =
      await ExpoBraintree.requestVenmoNonce(options);
    return result;
  } catch (ex: unknown) {
    return ex as BTVenmoError;
  }
}

export async function request3DSecurePaymentCheck(
  options: ThreeDSecureCheckOptions
): Promise<BTCardTokenization3DSNonceResult | BTThreeDError> {
  try {
    const result: BTCardTokenization3DSNonceResult =
      await ExpoBraintree.request3DSecurePaymentCheck(options);
    return result;
  } catch (ex: unknown) {
    return ex as BTThreeDError;
  }
}

export * from './types';
