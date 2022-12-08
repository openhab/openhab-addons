# For Developers

## Build

To only build the Bosch Smart Home binding code execute

```shell
mvn -pl :org.openhab.binding.boschshc install
```

## Execute

After compiling a new ``org.openhab.binding.boschshc.jar``
copy it into the ``addons`` folder of your openHAB test instance.

For the first time the jar is loaded automatically as a bundle.

It should also be reloaded automatically when the jar changed.

To reload the bundle manually you need to execute in the openhab console:

```shell
bundle:update "openHAB Add-ons :: Bundles :: Bosch Smart Home Binding"
```

or get the ID and update the bundle using the ID:

```shell
bundle:list
```

-> Get ID for "openHAB Add-ons :: Bundles :: Bosch Smart Home Binding"

```shell
bundle:update <ID>
```

## Debugging

To get debug output and traces of the Bosch Smart Home binding code
add the following lines into ``userdata/etc/log4j2.xml`` Loggers XML section.

```xml
<!-- Bosch SHC for debugging -->
<Logger level="TRACE" name="org.openhab.binding.boschshc"/>
```

or use the openhab console to change the log level

```shell
log:set TRACE org.openhab.binding.boschshc
```

## Pairing and  Certificates

We need secured and paired connection from the openHAB binding instance to the Bosch Smart Home Controller (SHC).  

Read more about the pairing process in [register a new client to the bosch smart home controller](https://github.com/BoschSmartHome/bosch-shc-api-docs/tree/master/postman#register-a-new-client-to-the-bosch-smart-home-controller)

A precondition for the secured connection to the Bosch SHC is a self singed key + certificate.
The key + certificate will be created and stored with the public Bosch SHC certificates in a Java Key store (jks).  

The public certificates files are from <https://github.com/BoschSmartHome/bosch-shc-api-docs/tree/master/best_practice>.
File copies stored in ``src/main/resource``.

All three certificates and the key will be used for the HTTPS connection between
this openHAB binding and the Bosch SHC.

During pairing the openHAB binding will exchange the self singed certificate with SHC.
