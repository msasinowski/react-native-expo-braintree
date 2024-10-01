import * as React from 'react';

import {
  StyleSheet,
  View,
  Button,
  ActivityIndicator,
  Text,
} from 'react-native';
import {
  getDeviceDataFromDataCollector,
  requestBillingAgreement,
  requestOneTimePayment,
  tokenizeCardData,
  request3DSecurePaymentCheck,
} from 'react-native-expo-braintree';

export const clientToken = 'sandbox_9dbg82cq_dcpspy2brwdjr3qn';

export default function App() {
  const [isLoading, setIsLoading] = React.useState(false);
  const [result, setResult] = React.useState('');

  return (
    <View style={styles.container}>
      <Button
        title="Click Me to request Billing Agreement"
        onPress={async () => {
          try {
            setIsLoading(true);
            const localResult = await requestBillingAgreement({
              clientToken,
            });
            setIsLoading(false);
            setResult(JSON.stringify(localResult));
            console.log(JSON.stringify(localResult));
          } catch (ex) {
            console.log(JSON.stringify(ex));
          } finally {
            setIsLoading(false);
          }
        }}
      />
      <Button
        title="Click Me To Get Device Data"
        onPress={async () => {
          try {
            setIsLoading(true);
            const resultDeviceData = await getDeviceDataFromDataCollector(
              'sandbox_9dbg82cq_dcpspy2brwdjr3qn'
            );
            setIsLoading(false);
            setResult(JSON.stringify(resultDeviceData));
            console.log(JSON.stringify(resultDeviceData));
          } catch (ex) {
            console.log(JSON.stringify(ex));
          } finally {
            setIsLoading(false);
          }
        }}
      />

      <Button
        title="Click Me To request One time Payment"
        onPress={async () => {
          try {
            setIsLoading(true);
            const resultDeviceData = await requestOneTimePayment({
              clientToken,
              amount: '5',
            });
            setIsLoading(false);
            setResult(JSON.stringify(resultDeviceData));
            console.log(JSON.stringify(resultDeviceData));
          } catch (ex) {
            console.log(JSON.stringify(ex));
          } finally {
            setIsLoading(false);
          }
        }}
      />

      <Button
        title="Click Me To Tokenize Card"
        onPress={async () => {
          try {
            setIsLoading(true);
            const tokenizedCard = await tokenizeCardData({
              clientToken,
              number: '1111222233334444',
              expirationMonth: '11',
              expirationYear: '24',
              cvv: '123',
              postalCode: '',
            });
            setIsLoading(false);
            setResult(JSON.stringify(tokenizedCard));
            console.log(JSON.stringify(tokenizedCard));
          } catch (ex) {
            console.log(JSON.stringify(ex));
          } finally {
            setIsLoading(false);
          }
        }}
      />

      <Button
        title="Click Me To Tokenize Card and Run 3DS Check"
        onPress={async () => {
          try {
            setIsLoading(true);
            const tokenizedCard = await tokenizeCardData({
              // Only client Token will work Tokenized Key will do not work for 3DS
              // Take a look on the example/src/simple-braintree-server to generate clientToken
              clientToken: '',
              number: '4000000000001000',
              expirationMonth: '01',
              expirationYear: '2024',
              cvv: '123',
            });

            if ('nonce' in tokenizedCard) {
              console.log(tokenizedCard?.nonce);
              const secureCheckResult = await request3DSecurePaymentCheck({
                // Only client Token will work Tokenized Key will do not work for 3DS
                // Take a look on the example/src/simple-braintree-server to generate clientToken
                clientToken: '',
                amount: '10',
                nonce: tokenizedCard?.nonce,
              });
              setIsLoading(false);
              setResult(JSON.stringify(secureCheckResult));
              console.log(JSON.stringify(secureCheckResult));
            }
          } catch (ex) {
            console.log(JSON.stringify(ex));
          } finally {
            setIsLoading(false);
          }
        }}
      />
      {isLoading && <ActivityIndicator />}
      <Text>{result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    rowGap: 20,
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
