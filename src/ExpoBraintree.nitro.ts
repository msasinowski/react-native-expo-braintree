import type { HybridObject } from 'react-native-nitro-modules';

export interface ExpoBraintree extends HybridObject<{
  ios: 'swift';
  android: 'kotlin';
}> {
  requestBillingAgreement(
    options: Record<string, string>
  ): Promise<Record<string, string>>;
  requestOneTimePayment(
    options: Record<string, string>
  ): Promise<Record<string, string>>;
  tokenizeCardData(
    options: Record<string, string>
  ): Promise<Record<string, string>>;
  getDeviceDataFromDataCollector(
    options: Record<string, string>
  ): Promise<Record<string, string>>;
  requestVenmoNonce(
    options: Record<string, string>
  ): Promise<Record<string, string>>;
  request3DSecurePaymentCheck(
    options: Record<string, string>
  ): Promise<Record<string, string>>;
  requestGooglePayPayment(
    options: Record<string, string>
  ): Promise<Record<string, string>>;
}
