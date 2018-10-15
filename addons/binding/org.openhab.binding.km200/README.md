# KM200 Binding

The KM200 Binding is communicating with a [Buderus Logamatic web KM200 / KM100 / KM50](https://www.buderus.de/de/produkte/catalogue/alle-produkte/7719_gateway-logamatic-web-km200-km100-km50). It's an unofficial binding.
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

This binding supports 11 different things types

| Thing            |  PaperUI Only | Description                                                                          |
| ---------------- |  ------------ |------------------------------------------------------------------------------------- |
| `appliance`      |               | This thing is representing the appliance (The heater inside of this heating system). |
| `dhwCircuit`     |               | This thing is representing a hot water circuit.                                      |
| `gateway`        |               | This thing is representing the gateway. (The connected KM200/100/50 device).         |
| `heatingCircuit` |               | This thing is representing a heating circuit.                                        |
| `heatSource`     |               | This thing is representing the heat source.                                          |
| `holidayMode`    |               | This thing is representing the holiday modes configuration.                          |
| `sensor`         |               | This thing is representing the sensors.                                              |
| `solarCircuit`   |               | This thing is representing a solar circuit.                                          |
| `system`         |               | This thing is representing the system without sensors and appliance.                 |
| `notification`   |               | This thing is representing the notifications.                                        |
| `switchProgram`  |       X       | This thing is representing a switch program.                                         |

### Channels

This binding is creating the channels depending on the connected heating system full automatically. Every thing has a lot of channels. This channels are depending on the connected heating system. 
You can see the complete list of supported channels in the PaperUI after the creation of a thing. 
There is no official documentation for the parameters availible so the names of the channels are all what we have. 

### Items

In the items file it's possible to map the services. You can take the correct names from the channels in the PaperUI.
There is nothing specific in the sitemap configuration.

## Full Examples

Things can be configured via the Paper UI, or using a `things` file like here.

### Basic Example

This example reads different values of items from a KMXXX Device..

`things/kmxxx.things`:

```xtend
Bridge km200:kmdevice:0815 "testKMDevice" @ "Room" [ privateKey= "1234567890abcdef1234567890abcdef", maxnbrrepeats=10.0, readDelay=100, refreshInterval=30, maxNbrRepeats=10, ip4Address="192.168.1.111", refreshinterval=30.0, readdelay=100.0 ] {
	heatingCircuit 1 "TestHC1"
	sensor 1 "TestSensors"
}
```

`items/kmxxx.items`:

```xtend
Number  budWater  "Water temperature  [%.1f °C]"    {channel="km200:dhwCircuit:0815:1:actualTemp"}
Number  budOutdoor  "Outdoor temperature  [%.1f °C]"    {channel="km200:sensor:0815:1:outdoor_t1"}
```
