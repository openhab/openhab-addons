# Ruuvi Gateway MQTT Binding

This binding allows integration of Ruuvi Tags via MQTT data, as collected by the [Ruuvi Gateway](https://ruuvi.com/gateway/).
Ruuvi Gateway listens for Bluetooth advertisements and publishes that data over MQTT.
Ruuvi Cloud subscription is not required; the integration is local.

Compared to the Ruuvi Tag Bluetooth binding, this binding benefits from the Ruuvi Gateway’s stronger and more reliable antenna, rather than the usually weaker antenna integrated into a computer motherboard.
An obvious downside compared to the Bluetooth binding is the requirement of a Ruuvi Gateway device.

Both RuuviTag and RuuviTag Pro are supported.

## Set up the Gateway

Before using this binding, the Ruuvi Gateway needs to be configured to publish the sensor data via MQTT.

For further instructions, refer to the relevant section in the [Ruuvi Gateway documentation](https://ruuvi.com/gateway-config/).
For the most convenient usage of this binding, ensure that the "Use 'ruuvi' as the prefix" MQTT setting is enabled on the Ruuvi Gateway.

## Discovery

First install the MQTT binding and set up a `broker` Thing, and make sure it is ONLINE, as this binding uses the MQTT binding to talk to your broker.

This binding discovers the Ruuvi Tags via the MQTT bridge; the discovered Things should appear in your Thing Inbox.

## Thing Configuration

There is only one Thing type supported by this binding, `ruuvitag_beacon`.
No manual configuration is needed, and the discovery function can be used instead.

For users who prefer manual configuration, here are the configurable parameters:

| Parameter | Description                               | Required | Default |
|-----------|-------------------------------------------|----------|---------|
| `topic`   | MQTT topic containing the gateway payload | Y        | (N/A)   |

## Channels

| Channel ID                | Item Type                | Description                                                              |
|---------------------------|--------------------------|--------------------------------------------------------------------------|
| temperature               | Number:Temperature       | The measured temperature                                                 |
| humidity                  | Number:Dimensionless     | The measured humidity                                                    |
| pressure                  | Number:Pressure          | The measured air pressure                                                |
| batteryVoltage            | Number:ElectricPotential | The measured battery voltage                                             |
| accelerationx             | Number:Acceleration      | The measured acceleration of X                                           |
| accelerationy             | Number:Acceleration      | The measured acceleration of Y                                           |
| accelerationz             | Number:Acceleration      | The measured acceleration of Z                                           |
| txPower                   | Number:Power             | TX power                                                                 |
| dataFormat                | Number                   | Data format version                                                      |
| measurementSequenceNumber | Number:Dimensionless     | Measurement sequence number                                              |
| movementCounter           | Number:Dimensionless     | Movement counter                                                         |
| rssi                      | Number:Power             | Received signal strength indicator (between the Gateway and the sensor)  |
| ts                        | DateTime                 | Timestamp when the message from the Bluetooth sensor was received by the Gateway |
| gwts                      | DateTime                 | Timestamp when the message from the Bluetooth sensor was relayed by the Gateway  |
| gwmac                     | String                   | MAC address of the Ruuvi Gateway                                         |

Note: Not all channels are always updated.
Available fields depend on [Ruuvi Data Format](https://github.com/ruuvi/ruuvi-sensor-protocols).
At the time of writing (2022-09), most Ruuvi Tags use Ruuvi Data Format 5 out of the box.

Some measurements might not make sense.
For example, the Ruuvi Tag Pro 2‑in‑1 does not have a humidity measurement and thus the humidity data advertised by that sensor is meaningless.

## Example

Please note that Thing and Item configuration can be done fully in the UI.
For those who prefer textual configuration, we share this example here.

To use these examples for textual configuration, you must already have configured an MQTT `broker` Thing and know its unique ID.
This UID will be used in the things file and will replace the text `myBroker`.
The first line in the things file will create a `broker` Thing and this can be removed if you have already setup a broker in another file or via the UI already.

### *.things

```java
Bridge mqtt:broker:myBroker [ host="localhost", secure=false, password="*******", qos=1, username="user"]
mqtt:ruuvitag_beacon:myTag1  "RuuviTag Sensor Beacon 9ABC" (mqtt:broker:myBroker) [ topic="ruuvi/mygw/DE:AD:BE:EF:AA:01" ]
```

### *.items

```java
Number:Temperature      temperature "Room Temperature [%.1f %unit%]" { channel="mqtt:ruuvitag_beacon:myTag1:temperature" }
Number:Dimensionless    humidity    "Humidity [%.0f %unit%]"         { channel="mqtt:ruuvitag_beacon:myTag1:humidity" }
Number:Pressure         pressure    "Air Pressure [%.0f %unit%]"     { channel="mqtt:ruuvitag_beacon:myTag1:pressure" }

// Examples of converting units
Number:Acceleration      acceleration_ms "Acceleration z [%.2f m/s²]" { channel="mqtt:ruuvitag_beacon:myTag1:accelerationz" }
Number:Acceleration      acceleration_g  "Acceleration z (g-force) [%.2f gₙ]" { channel="mqtt:ruuvitag_beacon:myTag1:accelerationz" }
```
