/* eslint-disable react-native/no-inline-styles */
import * as React from 'react';
import {
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  SafeAreaView,
  TextInput,
  Platform,
} from 'react-native';
import {
  getDeviceDataFromDataCollector,
  requestBillingAgreement,
  requestOneTimePayment,
  tokenizeCardData,
  request3DSecurePaymentCheck,
  requestGooglePayPayment,
  requestApplePayPayment,
  requestVenmoNonce,
  GOOGLE_PAY_TOTAL_PRICE_STATUS,
  BTPayPalCheckoutIntent,
  BoolValue,
  BTVenmoPaymntMethodUsage,
  type ThreeDSecureCheckOptions,
} from 'react-native-expo-braintree';
import { LogView, type LogState } from './LogView';

const merchantAppLink = 'https://braintree-example-app.web.app';
const clientToken = 'sandbox_krwbdqfy_b3fgzq5txkj3j5s6';

const T3DS_SCENARIOS = [
  { label: '✅ 3DS Success (No Challenge)', number: '4000000000002701' },
  { label: '🔥 3DS Challenge Required', number: '4000000000002503' },
  { label: '❌ 3DS Failed (Frictionless)', number: '4000000000002925' },
];

export default function App() {
  // States for Logging
  const [log1, setLog1] = React.useState<LogState>({
    loading: false,
    result: null,
    error: null,
  });
  const [log2, setLog2] = React.useState<LogState>({
    loading: false,
    result: null,
    error: null,
  });
  const [log3, setLog3] = React.useState<LogState>({
    loading: false,
    result: null,
    error: null,
  });
  const [logGP, setLogGP] = React.useState<LogState>({
    loading: false,
    result: null,
    error: null,
  });
  const [logVenmo, setLogVenmo] = React.useState<LogState>({
    loading: false,
    result: null,
    error: null,
  });

  // Functional States
  const [intent, setIntent] = React.useState<BTPayPalCheckoutIntent>(
    BTPayPalCheckoutIntent.sale
  );
  const [dynamic3DSToken, setDynamic3DSToken] = React.useState('');

  const exec = async (
    setLog: React.Dispatch<React.SetStateAction<LogState>>,
    name: string,
    action: () => Promise<any>
  ) => {
    setLog({ loading: true, result: `Running ${name}...`, error: null });
    try {
      const result = await action();

      setLog({
        loading: false,
        result: JSON.stringify(result, null, 2),
        error: null,
      });
      console.log(JSON.stringify(result, null, 2));
    } catch (ex: any) {
      console.log('FULL ERROR OBJECT:', ex);
      console.log('ERROR MESSAGE:', ex.message);
      setLog({
        loading: false,
        result: null,
        error: JSON.stringify(ex),
      });
    }
  };

  const run3DSTest = async (cardNumber: string) => {
    if (!dynamic3DSToken) {
      setLog3({
        loading: false,
        result: null,
        error: 'Paste 3DS Token first!',
      });
      return;
    }
    await exec(setLog3, `3DS-${cardNumber.slice(-4)}`, async () => {
      const tokenized = await tokenizeCardData({
        clientToken: dynamic3DSToken.trim(),
        number: cardNumber,
        expirationMonth: '12',
        expirationYear: '2028',
        cvv: '123',
      });

      if (tokenized && 'nonce' in tokenized && tokenized.nonce) {
        const options: ThreeDSecureCheckOptions = {
          clientToken: dynamic3DSToken.trim(),
          amount: '10.00',
          nonce: tokenized.nonce,
          email: 'jill.doe@example.com',
          givenName: 'Jill',
          surName: 'Doe',
          streetAddress: '555 Smith St',
          city: 'Chicago',
          region: 'IL',
          postalCode: '60622',
          countryCodeAlpha2: 'US',
          phoneNumber: '5551234567',
        };

        return await request3DSecurePaymentCheck(options);
      }

      throw new Error(
        tokenized && 'message' in tokenized
          ? (tokenized as any).message
          : 'Tokenization failed - check logs'
      );
    });
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.header}>
        <Text style={styles.headerText}>Braintree Test Suite</Text>
      </View>

      <ScrollView contentContainerStyle={styles.container}>
        {/* SECTION 1: CORE & PAYPAL */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>1. Core & PayPal (Static)</Text>

          <Text style={styles.label}>Select PayPal Intent:</Text>
          <View style={styles.intentContainer}>
            {Object.values(BTPayPalCheckoutIntent).map((i, index) => (
              <TouchableOpacity
                key={index}
                style={[
                  styles.intentButton,
                  intent === i && styles.intentButtonActive,
                ]}
                onPress={() => setIntent(i)}
              >
                <Text
                  style={[
                    styles.intentButtonText,
                    intent === i && styles.intentButtonTextActive,
                  ]}
                >
                  {i.toUpperCase()}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <TouchableOpacity
            style={styles.button}
            onPress={() =>
              exec(setLog1, 'DeviceData', () =>
                getDeviceDataFromDataCollector(clientToken)
              )
            }
          >
            <Text style={styles.buttonText}>Get Device Data</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.button}
            onPress={() =>
              exec(setLog1, `PayPalOneTime (${intent})`, () =>
                requestOneTimePayment({
                  clientToken,
                  amount: '50.00',
                  merchantAppLink,
                  intent: intent,
                })
              )
            }
          >
            <Text style={styles.buttonText}>PayPal One Time ({intent})</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.button}
            onPress={() =>
              exec(setLog1, 'PayPalBilling', () =>
                requestBillingAgreement({
                  clientToken,
                  merchantAppLink,
                  billingAgreementDescription: 'Test Recurring Payment',
                  displayName: 'Custom Display Name',
                })
              )
            }
          >
            <Text style={styles.buttonText}>PayPal Billing Agreement</Text>
          </TouchableOpacity>
          <LogView
            state={log1}
            onClear={() =>
              setLog1({ loading: false, result: null, error: null })
            }
          />
        </View>

        {/* SECTION 2: CARD TOKENIZATION */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>2. Card Tokenization</Text>
          <TouchableOpacity
            style={styles.buttonTokenize}
            onPress={() =>
              exec(setLog2, 'TokenizeOnly', () =>
                tokenizeCardData({
                  clientToken,
                  number: '4111111111111111',
                  expirationMonth: '12',
                  expirationYear: '2030',
                  cvv: '123',
                })
              )
            }
          >
            <Text style={styles.buttonText}>Tokenize Card (No 3DS)</Text>
          </TouchableOpacity>
          <LogView
            state={log2}
            onClear={() =>
              setLog2({ loading: false, result: null, error: null })
            }
          />
        </View>

        {/* SECTION 3: 3D SECURE */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>3. 3D Secure (Dynamic Token)</Text>
          <TextInput
            style={styles.input}
            placeholder="Paste Fresh Client Token for 3DS..."
            value={dynamic3DSToken}
            onChangeText={setDynamic3DSToken}
            multiline
          />
          {T3DS_SCENARIOS.map((s, i) => (
            <TouchableOpacity
              key={i}
              style={[
                styles.button3DS,
                (!dynamic3DSToken || log3.loading) && styles.buttonDisabled,
              ]}
              onPress={() => run3DSTest(s.number)}
              disabled={!dynamic3DSToken || log3.loading}
            >
              <Text style={styles.buttonText}>{s.label}</Text>
            </TouchableOpacity>
          ))}
          <LogView
            state={log3}
            onClear={() =>
              setLog3({ loading: false, result: null, error: null })
            }
          />
        </View>

        {/* SECTION 4: VENMO */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>4. Venmo Payment</Text>
          <TouchableOpacity
            style={styles.buttonVenmo}
            onPress={() =>
              exec(setLogVenmo, 'VenmoOneTime', () =>
                requestVenmoNonce({
                  clientToken,
                  vault: BoolValue.false,
                  paymentMethodUsage: BTVenmoPaymntMethodUsage.singleUse,
                  merchantAppLink: `${merchantAppLink}/braintree-payments/`,
                })
              )
            }
          >
            <Text style={styles.buttonText}>Venmo One-Time</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[
              styles.buttonVenmo,
              { marginTop: 8, backgroundColor: '#3D95CE' },
            ]}
            onPress={() =>
              exec(setLogVenmo, 'VenmoVault', () =>
                requestVenmoNonce({
                  clientToken,
                  paymentMethodUsage: BTVenmoPaymntMethodUsage.multiUse,
                  vault: BoolValue.true,
                  merchantAppLink: `${merchantAppLink}/braintree-payments/`,
                })
              )
            }
          >
            <Text style={styles.buttonText}>Venmo Vault (Multi-Use)</Text>
          </TouchableOpacity>
          <LogView
            state={logVenmo}
            onClear={() =>
              setLogVenmo({ loading: false, result: null, error: null })
            }
          />
        </View>

        {/* SECTION 5: GOOGLE PAY */}
        {Platform.OS === 'android' && (
          <View style={[styles.section]}>
            <Text style={styles.sectionTitle}>5. Google Pay</Text>
            <TouchableOpacity
              style={styles.buttonGooglePay}
              onPress={() =>
                exec(setLogGP, 'GooglePay', () =>
                  requestGooglePayPayment({
                    clientToken,
                    totalPrice: '199.00',
                    currencyCode: 'USD',
                    totalPriceStatus: GOOGLE_PAY_TOTAL_PRICE_STATUS.FINAL,
                    billingAddressRequired: true,
                    shippingAddressRequired: true,
                    emailRequired: true,
                    allowPrepaidCards: false,
                  })
                )
              }
            >
              <Text style={styles.buttonText}>Launch Google Pay</Text>
            </TouchableOpacity>
            <LogView
              state={logGP}
              onClear={() =>
                setLogGP({ loading: false, result: null, error: null })
              }
            />
          </View>
        )}
        {/* SECTION 5: APPLE PAY (iOS Only) */}
        {Platform.OS === 'ios' && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>5. Apple Pay (iOS Only)</Text>
            <TouchableOpacity
              style={styles.buttonApplePay}
              onPress={() =>
                exec(setLogAP, 'ApplePay', () =>
                  requestApplePayPayment({
                    clientToken,
                    merchantId: 'merchant.com.expobraintreeexample',
                    amount: '49.99',
                    companyName: 'My Awesome Store',
                    countryCode: 'US',
                    currencyCode: 'USD',
                  })
                )
              }
            >
              <Text style={styles.buttonTextApple}> Pay</Text>
            </TouchableOpacity>
            <LogView
              state={logAP}
              onClear={() =>
                setLogAP({ loading: false, result: null, error: null })
              }
            />
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: '#fff' },
  header: { padding: 15, backgroundColor: '#0070ba', alignItems: 'center' },
  headerText: { color: 'white', fontSize: 16, fontWeight: 'bold' },
  container: { padding: 15 },
  section: {
    marginBottom: 30,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    paddingBottom: 15,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 12,
    color: '#333',
  },
  label: { fontSize: 12, color: '#666', marginBottom: 8 },
  intentContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 15,
  },
  intentButton: {
    flex: 1,
    paddingVertical: 8,
    borderWidth: 1,
    borderColor: '#0070ba',
    borderRadius: 4,
    marginHorizontal: 2,
    alignItems: 'center',
  },
  intentButtonActive: { backgroundColor: '#0070ba' },
  intentButtonText: { fontSize: 10, color: '#0070ba', fontWeight: 'bold' },
  intentButtonTextActive: { color: '#fff' },
  input: {
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 8,
    padding: 8,
    fontSize: 11,
    minHeight: 50,
    marginBottom: 10,
    backgroundColor: '#fafafa',
  },
  button: {
    padding: 12,
    backgroundColor: '#0070ba',
    borderRadius: 6,
    marginBottom: 6,
    alignItems: 'center',
  },
  buttonTokenize: {
    padding: 12,
    backgroundColor: '#673AB7',
    borderRadius: 6,
    marginBottom: 6,
    alignItems: 'center',
  },
  button3DS: {
    padding: 12,
    backgroundColor: '#444',
    borderRadius: 6,
    marginBottom: 6,
    alignItems: 'center',
  },
  buttonVenmo: {
    padding: 12,
    backgroundColor: '#3D95CE',
    borderRadius: 6,
    alignItems: 'center',
  },
  buttonDisabled: { backgroundColor: '#ccc' },
  buttonText: { color: 'white', fontWeight: 'bold', fontSize: 13 },
  buttonGooglePay: {
    padding: 12,
    backgroundColor: '#000',
    borderRadius: 6,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#555',
  },
  buttonApplePay: {
    padding: 12,
    backgroundColor: '#000',
    borderRadius: 8,
    marginBottom: 6,
    alignItems: 'center',
    justifyContent: 'center',
    height: 48,
  },
  buttonTextApple: {
    color: 'white',
    fontSize: 20,
    fontWeight: '600',
  },
});
