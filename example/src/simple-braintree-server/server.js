require('dotenv').config();

const braintree = require('braintree');

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
      console.log('Client Token ' + response?.clientToken);
    })
    .catch((err) => {
      console.log('Error ' + JSON.stringify(err));
    });
};

getClientToken();
