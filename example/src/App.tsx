import * as React from 'react';

import {
  ActivityIndicator,
  Button,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {
  BTVenmoPaymntMethodUsage,
  BoolValue,
  getDeviceDataFromDataCollector,
  requestBillingAgreement,
  requestOneTimePayment,
  requestVenmoNonce,
  tokenizeCardData,
  request3DSecurePaymentCheck,
} from 'react-native-expo-braintree';

// export const clientToken = 'sandbox_x62mvdjj_p8ngm2sczm8248vg';
export const clientToken =
  'eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpGVXpJMU5pSXNJbXRwWkNJNklqSXdNVGd3TkRJMk1UWXRjMkZ1WkdKdmVDSXNJbWx6Y3lJNkltaDBkSEJ6T2k4dllYQnBMbk5oYm1SaWIzZ3VZbkpoYVc1MGNtVmxaMkYwWlhkaGVTNWpiMjBpZlEuZXlKbGVIQWlPakUzTXpJM05USXpNRE1zSW1wMGFTSTZJams1TTJJME5EVmhMVGd6TkdJdE5HWXhPQzA0WTJGakxUZzJNMlZpTWpoak5qZ3daQ0lzSW5OMVlpSTZJbkE0Ym1kdE1uTmplbTA0TWpRNGRtY2lMQ0pwYzNNaU9pSm9kSFJ3Y3pvdkwyRndhUzV6WVc1a1ltOTRMbUp5WVdsdWRISmxaV2RoZEdWM1lYa3VZMjl0SWl3aWJXVnlZMmhoYm5RaU9uc2ljSFZpYkdsalgybGtJam9pY0RodVoyMHljMk42YlRneU5EaDJaeUlzSW5abGNtbG1lVjlqWVhKa1gySjVYMlJsWm1GMWJIUWlPblJ5ZFdWOUxDSnlhV2RvZEhNaU9sc2liV0Z1WVdkbFgzWmhkV3gwSWwwc0luTmpiM0JsSWpwYklrSnlZV2x1ZEhKbFpUcFdZWFZzZENKZExDSnZjSFJwYjI1eklqcDdmWDAuWnRuYmV3aV9OUXVoS1NlQ1ZPcUo1d2Yxd0RLWURWanltckRId2pYV0lWOXBQaTZfN254MklRcURTUkdKWU5jY20xRnJSblNHMTRQQ0dHZkVpblVNUFEiLCJjb25maWdVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvcDhuZ20yc2N6bTgyNDh2Zy9jbGllbnRfYXBpL3YxL2NvbmZpZ3VyYXRpb24iLCJncmFwaFFMIjp7InVybCI6Imh0dHBzOi8vcGF5bWVudHMuc2FuZGJveC5icmFpbnRyZWUtYXBpLmNvbS9ncmFwaHFsIiwiZGF0ZSI6IjIwMTgtMDUtMDgiLCJmZWF0dXJlcyI6WyJ0b2tlbml6ZV9jcmVkaXRfY2FyZHMiXX0sImNsaWVudEFwaVVybCI6Imh0dHBzOi8vYXBpLnNhbmRib3guYnJhaW50cmVlZ2F0ZXdheS5jb206NDQzL21lcmNoYW50cy9wOG5nbTJzY3ptODI0OHZnL2NsaWVudF9hcGkiLCJlbnZpcm9ubWVudCI6InNhbmRib3giLCJtZXJjaGFudElkIjoicDhuZ20yc2N6bTgyNDh2ZyIsImFzc2V0c1VybCI6Imh0dHBzOi8vYXNzZXRzLmJyYWludHJlZWdhdGV3YXkuY29tIiwiYXV0aFVybCI6Imh0dHBzOi8vYXV0aC52ZW5tby5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIiwidmVubW8iOiJvZmYiLCJjaGFsbGVuZ2VzIjpbXSwidGhyZWVEU2VjdXJlRW5hYmxlZCI6dHJ1ZSwiYW5hbHl0aWNzIjp7InVybCI6Imh0dHBzOi8vb3JpZ2luLWFuYWx5dGljcy1zYW5kLnNhbmRib3guYnJhaW50cmVlLWFwaS5jb20vcDhuZ20yc2N6bTgyNDh2ZyJ9LCJwYXlwYWxFbmFibGVkIjp0cnVlLCJwYXlwYWwiOnsiYmlsbGluZ0FncmVlbWVudHNFbmFibGVkIjp0cnVlLCJlbnZpcm9ubWVudE5vTmV0d29yayI6ZmFsc2UsInVudmV0dGVkTWVyY2hhbnQiOmZhbHNlLCJhbGxvd0h0dHAiOnRydWUsImRpc3BsYXlOYW1lIjoicyIsImNsaWVudElkIjoiQVd2R0d1MXV1Z3dTbzdqaG02dkJXN09Ga2twMFlJNFJKYU9JNllWNGRlRHdCQVJrNTdlMllkRlRTakRZUnppd0pMOUxzY0NUZGhfR1FlOEkiLCJiYXNlVXJsIjoiaHR0cHM6Ly9hc3NldHMuYnJhaW50cmVlZ2F0ZXdheS5jb20iLCJhc3NldHNVcmwiOiJodHRwczovL2NoZWNrb3V0LnBheXBhbC5jb20iLCJkaXJlY3RCYXNlVXJsIjpudWxsLCJlbnZpcm9ubWVudCI6Im9mZmxpbmUiLCJicmFpbnRyZWVDbGllbnRJZCI6Im1hc3RlcmNsaWVudDMiLCJtZXJjaGFudEFjY291bnRJZCI6InMiLCJjdXJyZW5jeUlzb0NvZGUiOiJFVVIifX0=';

export const merchantAppLink = 'https://braintree-example-app.web.app';

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
              merchantAppLink,
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
            const resultDeviceData =
              await getDeviceDataFromDataCollector(clientToken);
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
              merchantAppLink,
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
        title="Click Me To Request a Venmo nonce"
        onPress={async () => {
          try {
            setIsLoading(true);
            const nonce = await requestVenmoNonce({
              clientToken,
              vault: BoolValue.true,
              paymentMethodUsage: BTVenmoPaymntMethodUsage.multiUse,
              totalAmount: '5',
            });
            setIsLoading(false);
            setResult(JSON.stringify(nonce));
            console.log(JSON.stringify(nonce));
          } catch (ex) {
            console.log(ex);
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
              clientToken,
              // number: '4000000000001091',
              number: '5200000000001104',
              expirationMonth: '01',
              expirationYear: '2024',
              cvv: '123',
            });
            if ('nonce' in tokenizedCard) {
              const secureCheckResult = await request3DSecurePaymentCheck({
                // Only client Token will work Tokenized Key will do not work for 3DS
                // Take a look on the example/src/simple-braintree-server to generate clientToken
                clientToken,
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
