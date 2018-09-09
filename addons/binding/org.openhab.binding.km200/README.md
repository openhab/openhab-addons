# KM200 Binding

The KM200 Binding is communicating with a [Buderus Logamatic web KM200 / KM100 / KM50](https://www.buderus.de/de/produkte/catalogue/alle-produkte/7719_gateway-logamatic-web-km200-km100-km50).
It is possible to receive and send parameters like string or float values.

**Important**: If the communication is not working and you see in the logfile errors like "illegal key size" then you have to change the [Java Cryptography Extension to the Unlimited Strength Jurisdiction](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html). 

## Supported Things

### KM 50/100/200

This Binding is tested on a KM200 but it should work on KM50 and KM100, too.

### Discovery and services

This binding is discovering the kmdevice with a MDNS search method in the local network. After adding of the discovered device to the Things the configuration of the device has to be completed (In the settings of this Thing). 

## Binding configuration

The binding doesn't have any configuration parameter.

## Thing Configuration

### kmdevice

The *kmdevice* bridge thing requires the following configuration parameters:

| Parameter Label         | Parameter ID    | Description                                                                 | Required | Default              | Example                                                          |
|-------------------------|-----------------|-----------------------------------------------------------------------------|----------|----------------------|------------------------------------------------------------------|
| IP address              | ip4_address     | The IP address of the KMXXX device                                          | true     |                      | 192.168.1.10                                                     |
| Refresh Interval        | RefreshInterval | The refresh interval in seconds which is used to poll the device.           | false    |    30                | 30                                                               |
|-------------------------|----Option 1-----|-----------------------------------------------------------------------------|----------|----------------------|------------------------------------------------------------------|
| Private Key             | PrivKey         | Take a look to the internet. Maybe you will find a way for generation.      | true     |                      | 0000FFFFEEEEDDDDCCCCBBBBAAAA999988887777666655554444333322221111 |
|-------------------------|----Option 2-----|-----------------------------------------------------------------------------|----OR----|----------------------|------------------------------------------------------------------|
| MD5 Salt                | MD5Salt         | The MD5-Salt (Take a look to the internet, maybe you will find it).         | true     |                      | 111122223333444455556666777788889999aaaabbbbccccddddeeeeffff0000 |
| Gateway Password        | GatewayPassword | The gateway password.                                                       | true     |                      | AAAABBBBCCCCDDDD                                                 |
| Private Password        | PrivatePassword | The private password.                                                       | true     |                      | MYPASSWORD                                                       |

### Other Things

The other things are available after the bridge device connected successfully and they don't need any configuration parameters.
This binding is creating things depending on the connected heating system full automatically.
There is no way for a manual configuration. (because they are always a bit different). 
Take a look to the inbox for the valid Things.

### Channels

This binding is creating the channels depending on the connected heating system full automatically. 
Take a look at the Things in the PaperUI for valid channels.

### Items

In the items file it's possible to map the services. You can take the correct names from the channels in the PaperUI.
Example:

```
String  budFirmware  "Firmware version [%s]"        {channel="km200:gateway:123456789:gateway:versionFirmware"}
Number  budWater  "Water temperature  [%.1f °C]"    {channel="km200:dhwCircuit:123456789:dhw1:actualTemp"}
```

There is nothing specific in the sitemap configuration.
