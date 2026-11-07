# KM200 Binding

The KM200 Binding is communicating with a [Buderus Logamatic web KM200 / KM100 / KM50](https://www.buderus.de/de/produkte/catalogue/alle-produkte/7719_gateway-logamatic-web-km200-km100-km50).
It is possible to receive and send parameters like string or float values.

**Important**: If the communication is not working and you see errors in the log file like "illegal key size", then you have to change the [Java Cryptography Extension to the Unlimited Strength Jurisdiction](https://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html).

## Supported Things

This binding supports 12 different Thing types.

| Thing            | UI Only | Description                                               |
| ---------------- | :-----: | --------------------------------------------------------- |
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
| `systemStates`   |         | The system states.                                        |
| `switchProgram`  | X       | A switch program.                                         |

### KM 50/100/200

This Binding is tested on a KM200 but it should work on KM50 and KM100, too.

### Discovery

This binding discovers KM devices through mDNS in the local network.

## Thing Configuration

### kmdevice

The _kmdevice_ bridge requires the following configuration parameters:

| Parameter Label           | Parameter ID    | Description                                                                       | Required | Default | Example                                                          |
| ------------------------- | --------------- | --------------------------------------------------------------------------------- | -------- | ------- | ---------------------------------------------------------------- |
| IP address                | ip4Address      | The IP address of the KM device                                                   | true     |         | 192.168.1.10                                                     |
| Refresh Interval          | refreshInterval | The refresh interval in seconds which is used to poll the device.                 | true     | 30      | 30                                                               |
| Private Key               | privateKey      | Take a look to the internet. Maybe you will find a way for generation.            | true     |         | 0000FFFFEEEEDDDDCCCCBBBBAAAA999988887777666655554444333322221111 |
| Read Delay                | readDelay       | Delay between two read attempts in ms.                                            | true     | 100     | 100                                                              |
| Maximum Number Of Repeats | maxNbrRepeats   | Maximum number of repeats in case of a communication error (like HTTP 500 error). | true     | 10      | 10                                                               |

### Service Things

All Thing types except `switchProgram` accept the optional `rootService` configuration parameter.
Set it to the absolute path of a service group on your device, for example `/heatingCircuits/hc1`, `/dhwCircuits/dhw1` or `/system/sensors`.
The configured path takes precedence over the discovered or Thing-type `root` property.
Without `rootService`, the binding continues to use that property.
The Thing reports a configuration error if the path is empty, relative, unknown, or selects a service without child services.

| Parameter   | Description                                       | Required | Default                       |
| ----------- | ------------------------------------------------- | -------- | ----------------------------- |
| rootService | Absolute path of a service group on the KM device | false    | Discovered or Thing-type root |

For file-defined circuit Things, use the path of the individual circuit, such as `/heatingCircuits/hc1`.
The available paths depend on your heating system; discovered Things expose their path in the `root` property with `/` represented as `#`.
For a sensor Thing, use `/system/sensors` or the relevant sensor group, for example `/system/sensors/temperatures`.
Switch-program Things still require discovery because they also depend on device-specific switching properties.

### Channels

This binding creates the channels depending on the connected heating system fully automatically.
These channels depend on the connected heating system.
You can see the complete list of supported channels of the Thing in the UI.
There is no official documentation for the parameters available from Buderus, so the names of the channels is all that reflects their purpose.

## Full Examples

This example reads different values of items from a KMXXX Device.

`things/kmxxx.things`:

```java
Bridge km200:kmdevice:0815 "testKMDevice" @ "Room" [ privateKey="1234567890abcdef1234567890abcdef", readDelay=100, refreshInterval=30, maxNbrRepeats=10, ip4Address="192.168.1.111" ] {
    heatingCircuit 1 "TestHC1" [ rootService="/heatingCircuits/hc1" ]
    dhwCircuit 1 "TestDHW1" [ rootService="/dhwCircuits/dhw1" ]
    sensor 1 "TestSensors" [ rootService="/system/sensors/temperatures" ]
}
```

The same Things can be defined in `yaml/kmxxx.yaml` instead of the `.things` file:

```yaml
version: 1
things:
  km200:kmdevice:0815:
    isBridge: true
    label: testKMDevice
    location: Room
    config:
      privateKey: "1234567890abcdef1234567890abcdef"
      readDelay: 100
      refreshInterval: 30
      maxNbrRepeats: 10
      ip4Address: "192.168.1.111"
  km200:heatingCircuit:0815:1:
    bridge: km200:kmdevice:0815
    label: TestHC1
    config:
      rootService: /heatingCircuits/hc1
  km200:dhwCircuit:0815:1:
    bridge: km200:kmdevice:0815
    label: TestDHW1
    config:
      rootService: /dhwCircuits/dhw1
  km200:sensor:0815:1:
    bridge: km200:kmdevice:0815
    label: TestSensors
    config:
      rootService: /system/sensors/temperatures
```

`items/kmxxx.items`:

```java
Number  budWater  "Water temperature  [%.1f °C]"    {channel="km200:dhwCircuit:0815:1:actualTemp"}
Number  budOutdoor  "Outdoor temperature  [%.1f °C]"    {channel="km200:sensor:0815:1:outdoor_t1"}
```
