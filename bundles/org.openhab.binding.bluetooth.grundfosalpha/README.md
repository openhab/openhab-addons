# Bluetooth Grundfos Alpha Adapter

This binding supports reading data from Grundfos Alpha pumps with a [Grundfos Alpha Reader](https://product-selection.grundfos.com/products/alpha-reader) or [Alpha3 pump](https://product-selection.grundfos.com/products/alpha/alpha3) with built-in Bluetooth.

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

| Channel          | Type                      | Read/Write | Description                                        | Unit |
|------------------|---------------------------|------------|----------------------------------------------------|------|
| rssi             | Number:Power              | R          | Received Signal Strength Indicator                 | dBm  |
| flow-rate        | Number:VolumetricFlowRate | R          | The flow rate of the pump                          | m³/h |
| pump-head        | Number:Length             | R          | The water head above the pump                      | m    |
| voltage-ac       | Number:ElectricPotential  | R          | Current AC pump voltage                            | V    |
| motor-current    | Number:ElectricCurrent    | R          | Current drawn by the pump motor                    | A    |
| motor-speed      | Number:Frequency          | R          | Current rotation of the pump motor                 | rpm  |
| power            | Number:Power              | R          | Current pump power consumption                     | W    |
| energy           | Number:Energy             | R          | Accumulated electrical energy consumed by the pump | kWh  |
| operating-time   | Number:Time               | R          | Accumulated running time of the pump               | s    |
| start-count      | Number                    | R          | Accumulated number of pump starts                  |      |

### `mi401` Channels

| Channel          | Type                      | Read/Write | Description                        | Unit |
|------------------|---------------------------|------------|------------------------------------|------|
| rssi             | Number:Power              | R          | Received Signal Strength Indicator | dBm  |
| flow-rate        | Number:VolumetricFlowRate | R          | The flow rate of the pump          | m³/h |
| pump-head        | Number:Length             | R          | The water head above the pump      | m    |
| pump-temperature | Number:Temperature        | R          | The temperature of the pump        | °C   |
| battery-level    | Number:Dimensionless      | R          | The battery level of the reader    | %    |

## Full Example

### `mi401` Example

grundfos_alpha.things (assuming you have a Bluetooth bridge with the ID `bluetooth:bluegiga:adapter1`):

```java
bluetooth:mi401:hci0:sensor1 "Grundfos Alpha Reader 1" (bluetooth:bluegiga:adapter1) [ address="12:34:56:78:9A:BC" ]
```

grundfos_alpha.items:

```java
Number RSSI "RSSI [%.1f dBm]" <qualityofservice> { channel="bluetooth:mi401:hci0:sensor1:rssi" }
Number:VolumetricFlowRate Flow_rate "Flowrate [%.1f %unit%]" <flow> { channel="bluetooth:mi401:hci0:sensor1:flow-rate" }
Number:Length Pump_Head "Pump Head [%.1f %unit%]" <water> { channel="bluetooth:mi401:hci0:sensor1:pump-head" }
Number:Temperature Pump_Temperature "Temperature [%.1f %unit%]" <temperature> { channel="bluetooth:mi401:hci0:sensor1:pump-temperature" }
Number:Dimensionless Battery_Level "Battery Level [%d %%]" <battery> { channel="bluetooth:mi401:hci0:sensor1:battery-level" }
```

### `alpha3` Example

grundfos_alpha.things:

```java
Bridge bluetooth:bluez:hci0 "Bluetooth BlueZ Adapter" [ address="12:34:56:78:9A:BC" ] {
    alpha3 alpha3 "Grundfos Alpha3" [ address="12:34:56:78:9A:DE" ]
}
```

grundfos_alpha.items:

```java
Number:Power Alpha3_RSSI "RSSI" <qualityofservice> { channel="bluetooth:alpha3:hci0:alpha3:rssi", unit="dBm" }
Number:VolumetricFlowRate Alpha3_FlowRate "Flowrate" <flow> { channel="bluetooth:alpha3:hci0:alpha3:flow-rate", unit="m³/h" }
Number:Length Alpha3_Head "Pump Head" <water> { channel="bluetooth:alpha3:hci0:alpha3:pump-head", unit="m" }
Number:ElectricPotential Alpha3_Voltage "Voltage" <energy> ["Measurement", "Voltage"] { channel="bluetooth:alpha3:hci0:alpha3:voltage-ac", unit="V" }
Number:ElectricCurrent Alpha3_Current "Current" <energy> ["Measurement", "Current"] { channel="bluetooth:alpha3:hci0:alpha3:motor-current", unit="A" }
Number:Power Alpha3_Power "Power" <energy> ["Measurement", "Power"] { channel="bluetooth:alpha3:hci0:alpha3:power", unit="W" }
Number:Energy Alpha3_Energy "Energy" <energy> ["Measurement", "Energy"] { channel="bluetooth:alpha3:hci0:alpha3:energy", unit="kWh" }
Number:Frequency Alpha3_MotorSpeed "Pump speed" <fan> { channel="bluetooth:alpha3:hci0:alpha3:motor-speed", unit="rpm" }
Number:Time Alpha3_OperatingTime "Operating time" <time> { channel="bluetooth:alpha3:hci0:alpha3:operating-time", unit="d" }
Number Alpha3_StartCount "Start count" { channel="bluetooth:alpha3:hci0:alpha3:start-count" }
```
