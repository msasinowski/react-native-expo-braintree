import * as React from 'react';

import { StyleSheet, View, Button } from 'react-native';
import {
  getDeviceDataFromDataCollector,
  requestBillingAgreement,
  requestOneTimePayment,
  tokenizeCardData,
} from 'react-native-paypal-reborn';

export const clientToken = 'sandbox_9dbg82cq_dcpspy2brwdjr3qn';

export default function App() {
  return (
    <View style={styles.container}>
      <Button
        title="Click Me to request Billing Agreement"
        onPress={async () => {
          try {
            const result = await requestBillingAgreement({
              clientToken,
            });
            console.log({ result });
          } catch (ex) {
            console.log({ ex });
          }
        }}
      />
      <Button
        title="Click Me To Get Device Data"
        onPress={async () => {
          try {
            const resultDeviceData = await getDeviceDataFromDataCollector(
              'sandbox_9dbg82cq_dcpspy2brwdjr3qn'
            );
            console.log({ resultDeviceData });
          } catch (ex) {
            console.log({ ex });
          }
        }}
      />

      <Button
        title="Click Me To request One time Payment"
        onPress={async () => {
          try {
            const resultDeviceData = await requestOneTimePayment({
              clientToken,
              amount: '5',
            });
            console.log({ resultDeviceData });
          } catch (ex) {
            console.log({ ex });
          }
        }}
      />

      <Button
        title="Click Me To Tokenize Card"
        onPress={async () => {
          try {
            const tokenizedCard = await tokenizeCardData({
              clientToken,
              number: '1111222233334444',
              expirationMonth: '11',
              expirationYear: '24',
              cvv: '123',
              postalCode: '',
            });
            console.log({ tokenizedCard });
          } catch (ex) {
            console.log({ ex });
          }
        }}
      />
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
