import * as React from 'react';

import { StyleSheet, View, Button } from 'react-native';
import { requestBillingAgreement } from 'react-native-paypal-reborn';

export default function App() {
  const [_result, setResult] = React.useState<any>();

  React.useEffect(() => {
    requestBillingAgreement({
      clientToken: 'some secret clientToken',
      description: 'some nice description',
    }).then(setResult);
  }, []);

  return (
    <View style={styles.container}>
      <Button
        title="Click Me to Open Paypal"
        onPress={async () => {
          const result = requestBillingAgreement({
            clientToken: 'some secret clientToken',
            description: 'some nice description',
          });
          console.log({ result });
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
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
