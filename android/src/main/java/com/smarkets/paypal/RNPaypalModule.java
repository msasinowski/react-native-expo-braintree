
package com.smarkets.paypal;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.PayPalClient;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PayPalVaultRequest;
import com.braintreepayments.api.PayPalCheckoutRequest;
import com.braintreepayments.api.PayPalPaymentIntent;
import com.braintreepayments.api.PostalAddress;
import com.braintreepayments.api.PayPalListener;
import com.braintreepayments.api.UserCanceledException;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.UiThreadUtil;

public class RNPaypalModule extends ReactContextBaseJavaModule implements PayPalListener {

    private static final String TAG = "RNPaypal";
    private Promise promise;
    private final ActivityEventListener activityEventListener = new BaseActivityEventListener() {
        @Override
        public void onNewIntent(Intent intent) {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity != null) {
                currentActivity.setIntent(intent);
            }
        }
    };

    public RNPaypalModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(activityEventListener);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethod
    public void requestOneTimePayment(
            String token,
            ReadableMap options,
            Promise promise) {
        this.promise = promise;
        FragmentActivity activity = (FragmentActivity) getCurrentActivity();

        if (activity == null) {
            promise.reject("creation_error", "Something went wrong");
            return;
        }

        BraintreeClient braintreeClient = new BraintreeClient(activity, token);

        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PayPalClient payPalClient = new PayPalClient(activity, braintreeClient);
                payPalClient.setListener(RNPaypalModule.this);
                PayPalCheckoutRequest request = new PayPalCheckoutRequest(options.getString("amount"));
                if (options.hasKey("currency"))
                    request.setCurrencyCode(options.getString("billingAgreementDescription"));
                if (options.hasKey("localeCode"))
                    request.setLocaleCode(options.getString("localeCode"));
                if (options.hasKey("displayName"))
                    request.setDisplayName(options.getString("displayName"));
                if (options.hasKey("shippingAddressRequired"))
                    request.setShippingAddressRequired(options.getBoolean("shippingAddressRequired"));
                if (options.hasKey("intent")) {
                    String intent = options.getString("intent");
                    switch (intent) {
                        case PayPalPaymentIntent.SALE:
                            request.setIntent(PayPalPaymentIntent.SALE);
                            break;
                        case PayPalPaymentIntent.ORDER:
                            request.setIntent(PayPalPaymentIntent.ORDER);
                    }
                } else {
                    request.setIntent(PayPalPaymentIntent.AUTHORIZE);
                }

                payPalClient.tokenizePayPalAccount(activity, request);
            }
        });
    }

    protected WritableMap postalAddressToMap(PostalAddress address) {
        WritableMap result = Arguments.createMap();
        result.putString("recipientName", address.getRecipientName());
        result.putString("streetAddress", address.getStreetAddress());
        result.putString("extendedAddress", address.getExtendedAddress());
        result.putString("locality", address.getLocality());
        result.putString("countryCodeAlpha2", address.getCountryCodeAlpha2());
        result.putString("postalCode", address.getPostalCode());
        result.putString("region", address.getRegion());
        return result;
    }

    @ReactMethod
    public void requestBillingAgreement(
            String token,
            ReadableMap options,
            Promise promise
    ) {
        this.promise = promise;
        FragmentActivity activity = (FragmentActivity) getCurrentActivity();

        if (activity == null) {
            promise.reject("creation_error", "Something went wrong");
            return;
        }

        BraintreeClient braintreeClient = new BraintreeClient(activity, token);

        UiThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PayPalClient payPalClient = new PayPalClient(activity, braintreeClient);
                payPalClient.setListener(RNPaypalModule.this);
                PayPalVaultRequest request = new PayPalVaultRequest();
                if (options.hasKey("billingAgreementDescription"))
                    request.setBillingAgreementDescription(options.getString("billingAgreementDescription"));
                if (options.hasKey("localeCode"))
                    request.setLocaleCode(options.getString("localeCode"));
                if (options.hasKey("displayName"))
                    request.setDisplayName(options.getString("displayName"));
                payPalClient.tokenizePayPalAccount(activity, request);
            }
        });
    }

    @Override
    public void onPayPalSuccess(@NonNull PayPalAccountNonce payPalAccountNonce) {
        WritableMap result = Arguments.createMap();
        result.putString("nonce", payPalAccountNonce.getString());
        result.putString("payerId", payPalAccountNonce.getPayerId());
        result.putString("email", payPalAccountNonce.getEmail());
        result.putString("firstName", payPalAccountNonce.getFirstName());
        result.putString("lastName", payPalAccountNonce.getLastName());
        result.putString("phone", payPalAccountNonce.getPhone());
        result.putMap("billingAddress", postalAddressToMap(payPalAccountNonce.getBillingAddress()));
        result.putMap("shippingAddress", postalAddressToMap(payPalAccountNonce.getShippingAddress()));
        promise.resolve(result);

    }

    @Override
    public void onPayPalFailure(@NonNull Exception error) {
        if (error instanceof UserCanceledException) {
            // user canceled
            promise.reject("user_cancellation", error);

        } else {
            // handle error
            promise.reject("request_one_time_payment_error", error);
        }
    }
}
