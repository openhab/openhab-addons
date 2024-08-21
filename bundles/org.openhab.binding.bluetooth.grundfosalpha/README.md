# GrundfosAlpha Binding

This adds support for reading out the data of Grundfos Alpha Pumps with a [Grundfos Alpha Reader](https://product-selection.grundfos.com/products/alpha-reader)

The reverse engineering of the protocol was taken from [https://github.com/JsBergbau/AlphaDecoder](https://github.com/JsBergbau/AlphaDecoder).

## Supported Things

- `mi401`: The Grundfos MI401 ALPHA Reader

## Discovery

All readers are auto-detected as soon as Bluetooth is configured in openHAB and the MI401 device is powered on.

## Thing Configuration

### `mi401` Thing Configuration

| Name    | Type | Description                                   | Default | Required | Advanced |
|---------|------|-----------------------------------------------|---------|----------|----------|
| address | text | Bluetooth address in XX:XX:XX:XX:XX:XX format | N/A     | yes      | no       |

## Channels

| Channel          | Type                      | Read/Write | Description                        |
|------------------|---------------------------|------------|------------------------------------|
| rssi             | Number                    | R          | Received Signal Strength Indicator |
| flow-rate        | Number:VolumetricFlowRate | R          | The flow rate of the pump          |
| pump-head        | Number:Length             | R          | The water head above the pump      |
| pump-temperature | Number:Temperature        | R          | The temperature of the pump        |
| battery-level    | Number:Dimensionless      | R          | The battery level of the reader    |

## Full Example

grundfos_alpha.things (assuming you have a Bluetooth bridge with the ID `bluetooth:bluegiga:adapter1`:

```java
bluetooth:mi401:hci0:sensor1 "Grundfos Alpha Reader 1" (bluetooth:bluegiga:adapter1) [ address="12:34:56:78:9A:BC" ]
```

grundfos_alpha.items:

```java
Number RSSI "RSSI [%.1f dBm]" <QualityOfService> { channel="bluetooth:mi401:hci0:sensor1:rssi" }
Number:VolumetricFlowRate Flow_rate "Flowrate [%.1f %unit%]" <flow> { channel="bluetooth:mi401:hci0:sensor1:flow-rate" }
Number:Length Pump_Head "Pump head [%.1f %unit%]" <water> { channel="bluetooth:mi401:hci0:sensor1:pump-head" }
Number:Temperature Pump_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="bluetooth:mi401:hci0:sensor1:pump-temperature" }
Number:Dimensionless Battery_Level "Battery Level [%d %%]" <battery> { channel="bluetooth:mi401:hci0:sensor1:battery-level" }
```
