# KM200 Binding

The KM200 Binding is communicating with a [Buderus Logamatic web KM200 / KM100 / KM50](https://www.buderus.de/de/produkte/catalogue/alle-produkte/7719_gateway-logamatic-web-km200-km100-km50).
It is possible to receive and send parameters like string or float values.

**Important**: If the communication is not working and you see in the logfile errors like "illegal key size" then you have to change the [Java Cryptography Extension to the Unlimited Strength Jurisdiction](https://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html).

## Supported Things

This binding supports 11 different things types

| Thing            | UI Only | Description                                               |
|------------------|:-------:|-----------------------------------------------------------|
| `appliance`      |         | The appliance (The heater inside of this heating system). |
| `dhwCircuit`     |         | A hot water circuit.                                      |
| `gateway`        |         | The gateway. (The connected KM200/100/50 device).         |
| `heatingCircuit` |         | A heating circuit.                                        |
| `heatSource`     |         | The heat source.                                          |
| `holidayMode`    |         | The holiday modes configuration.                          |
| `sensor`         |         | The sensors.                                              |
| `solarCircuit`   |         | A solar circuit.                                          |
| `system`         |         | The system without sensors and appliance.                 |
| `notification`   |         | The notifications.                                        |
| `switchProgram`  |    X    | A switch program.                                         |

### KM 50/100/200

This Binding is tested on a KM200 but it should work on KM50 and KM100, too.

### Discovery

This binding discovers KM devices through mDNS in the local network.

## Thing Configuration

### kmdevice

The _kmdevice_ bridge requires the following configuration parameters:

| Parameter Label           | Parameter ID    | Description                                                                       | Required | Default              | Example                                                          |
|---------------------------|-----------------|-----------------------------------------------------------------------------------|----------|----------------------|------------------------------------------------------------------|
| IP address                | ip4_address     | The IP address of the KMXXX device                                                | true     |                      | 192.168.1.10                                                     |
| Refresh Interval          | refreshInterval | The refresh interval in seconds which is used to poll the device.                 | true     |    30                | 30                                                               |
| Private Key               | privKey         | Take a look to the internet. Maybe you will find a way for generation.            | true     |                      | 0000FFFFEEEEDDDDCCCCBBBBAAAA999988887777666655554444333322221111 |
| Read Delay                | readDelay       | Delay between two read attempts in ms.                                            | true     |    100               | 100                                                              |
| Maximum Number Of Repeats | maxNbrRepeats   | Maximum number of repeats in case of a communication error (like HTTP 500 error). | true     |    10                | 10                                                               |

### Channels

This binding creates the channels depending on the connected heating system fully automatically.
These channels depend on the connected heating system.
You can see the complete list of supported channels of the thing in the UI.
There is no official documentation for the parameters available from Buderus, so the names of the channels is all that reflects their purpose.

## Full Examples

This example reads different values of items from a KMXXX Device.

`things/kmxxx.things`:

```java
Bridge km200:kmdevice:0815 "testKMDevice" @ "Room" [ privateKey= "1234567890abcdef1234567890abcdef", maxnbrrepeats=10.0, readDelay=100, refreshInterval=30, maxNbrRepeats=10, ip4Address="192.168.1.111", refreshinterval=30.0, readdelay=100.0 ] {
 heatingCircuit 1 "TestHC1"
 sensor 1 "TestSensors"
}
```

`items/kmxxx.items`:

```java
Number  budWater  "Water temperature  [%.1f °C]"    {channel="km200:dhwCircuit:0815:1:actualTemp"}
Number  budOutdoor  "Outdoor temperature  [%.1f °C]"    {channel="km200:sensor:0815:1:outdoor_t1"}
```
