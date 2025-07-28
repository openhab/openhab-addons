# Bluetooth Grundfos Alpha Adapter

This binding adds support for reading out the data of Grundfos Alpha pumps with a [Grundfos Alpha Reader](https://product-selection.grundfos.com/products/alpha-reader) or [Alpha3 pump](https://product-selection.grundfos.com/products/alpha/alpha3) with built-in Bluetooth.

The reverse engineering of the Alpha Reader protocol was taken from [https://github.com/JsBergbau/AlphaDecoder](https://github.com/JsBergbau/AlphaDecoder).

## Supported Things

- `alpha3`: The Grundfos Alpha3 pump
- `mi401`: The Grundfos MI401 ALPHA Reader

## Discovery

All pumps and readers are auto-detected as soon as Bluetooth is configured in openHAB and the devices are powered on.

## Thing Configuration

### `alpha3` Thing Configuration

| Name            | Type    | Description                                             | Default | Required | Advanced |
|-----------------|---------|---------------------------------------------------------|---------|----------|----------|
| address         | text    | Bluetooth address in XX:XX:XX:XX:XX:XX format           | N/A     | yes      | no       |
| refreshInterval | integer | Number of seconds between fetching values from the pump | 30      | no       | yes      |

### Pairing

After creating the Thing, the binding will attempt to connect to the pump.
To start the pairing process, press the blue LED button on the pump.
When the LED stops blinking and stays lit, the connection has been established, and the Thing should appear online.

However, the pump may still not be bonded correctly, which could prevent the binding from reconnecting after a disconnection.
On Linux, you can take additional steps to fix this issue by manually pairing the pump:

```shell
bluetoothctl pair XX:XX:XX:XX:XX:XX
Attempting to pair with XX:XX:XX:XX:XX:XX
[CHG] Device XX:XX:XX:XX:XX:XX Bonded: yes
[CHG] Device XX:XX:XX:XX:XX:XX Paired: yes
Pairing successful
```

### `mi401` Thing Configuration

| Name    | Type | Description                                   | Default | Required | Advanced |
|---------|------|-----------------------------------------------|---------|----------|----------|
| address | text | Bluetooth address in XX:XX:XX:XX:XX:XX format | N/A     | yes      | no       |

## Channels

### `alpha3` Channels

| Channel          | Type                      | Read/Write | Description                        |
|------------------|---------------------------|------------|------------------------------------|
| rssi             | Number:Power              | R          | Received Signal Strength Indicator |
| flow-rate        | Number:VolumetricFlowRate | R          | The flow rate of the pump          |
| pump-head        | Number:Length             | R          | The water head above the pump      |
| voltage-ac       | Number:ElectricPotential  | R          | Current AC pump voltage            |
| power            | Number:Power              | R          | Current pump power consumption     |
| motor-speed      | Number:Frequency          | R          | Current rotation of the pump motor |

### `mi401` Channels

| Channel          | Type                      | Read/Write | Description                        |
|------------------|---------------------------|------------|------------------------------------|
| rssi             | Number:Power              | R          | Received Signal Strength Indicator |
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
