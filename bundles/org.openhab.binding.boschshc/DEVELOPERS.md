# For Developers

## Certificates

For a secured connection to the Bosch SHC a self singed key + certificate will be created.
This cert will be stored with the public Bosch SHC certificates in a Java Key store (jks)  

The public certificates files are from https://github.com/BoschSmartHome/bosch-shc-api-docs/tree/master/best_practice.
File copies stored in ``src/main/resource``.

All three certificates and the key will be used for the HTTPS connection between
this openHAB binding and the Bosch SHC.

During pairing the openHAB binding will exchange the self singed certificate with SHC.    