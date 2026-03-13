require('dotenv').config();
const braintree = require('braintree');
const clipboardy = require('clipboardy'); // Import added

const gateway = new braintree.BraintreeGateway({
  environment: braintree.Environment.Sandbox,
  merchantId: process.env.MERCHANT_ID,
  publicKey: process.env.PUBLIC_KEY,
  privateKey: process.env.PRIVATE_KEY,
});

const getClientToken = () => {
  gateway.clientToken
    .generate()
    .then((response) => {
      const token = response?.clientToken;

      if (token) {
        // Copying to clipboard
        clipboardy.writeSync(token);

        console.log('✅ Client Token generated and copied to clipboard!');
        console.log('Token: ' + token);
      }
    })
    .catch((err) => {
      // Braintree errors often contain a 'message' or nested 'errors'
      console.error('❌ Error Details:', err.message || err);

      // If it's a validation error from Braintree:
      if (err.errors) {
        console.error(
          'Validation Errors:',
          JSON.stringify(err.errors.deepErrors(), null, 2)
        );
      }
    });
};

getClientToken();
