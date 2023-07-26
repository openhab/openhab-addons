# Ruuvi Gateway MQTT Binding

This binding allows integration of Ruuvi Tags via MQTT data, as collected by [Ruuvi Gateway](https://ruuvi.com/gateway/).
Ruuvi gateway is listening for Bluetooth advertisements and publishing that data over MQTT.
Ruuvi Cloud Subscription is not needed at all as the integration is local.

Compared to Ruuvi Tag Bluetooth binding, this binding has the benefit of relying on strong and reliable antenna of Ruuvi Gateway, as opposed to e.g. usually much weaker antenna integrated onto computer motherboard.
Obvious downside compared to the bluetooth binding is the requirement of having Ruuvi Gateway device.

Both RuuviTag and RuuviTag Pro are supported. 

## Setup the Gateway

Before using this binding, Ruuvi Gateway needs to configured to publish the sensor data via MQTT. 

For further instructions, refer to relevant section in [Ruuvi Gateway documentation](https://ruuvi.com/gateway-config/).
For most convenient usage of this binding, please ensure that "Use 'ruuvi' on the prefix' MQTT setting is enabled on Ruuvi Gateway.

## Discovery

First install the MQTT binding and setup a `broker` thing and make sure it is ONLINE, as this binding uses the MQTT binding to talk to your broker and hence that binding must be setup first.

This binding discovers the Ruuvi Tags via the MQTT bridge; the discovered things should appear in your thing Inbox.

## Thing Configuration


There is only thing type supported by this binding, `ruuvitag_beacon`.
No manual configuration is needed, and discovery function can be used instead.

For users that prefer manual configuration, we list here the configurable parameters.

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
| rssi                      | Number                   | Received signal (between the Gateway and the sensor) strength indicator  |
| ts                        | DateTime                 | Timestamp when the message from Bluetooth-sensor was received by Gateway |
| gwts                      | DateTime                 | Timestamp when the message from Bluetooth-sensor was relayed by Gateway  |
| gwmac                     | String                   | MAC-address of Ruuvi Gateway                                             |

Note: not all channels are always updated.
Available fields depend on [Ruuvi Data Format](https://github.com/ruuvi/ruuvi-sensor-protocols).
At the time of writing (2022-09), most Ruuvi Tags use Ruuvi Data Format 5 out of box.

Some measurements might not make any sense.
For example, Ruuvi Tag Pro 2in1 does not have a humidity measurement and thus, the humidity data advertised by the sensor is garbage.

## Example

Please note that Thing and Item configuration can be done fully in the UI.
For those who prefer textual configuration, we share this example here.

To use these examples for textual configuration, you must already have a configured a MQTT `broker` thing and know its unique ID.
This UID will be used in the things file and will replace the text `myBroker`.
The first line in the things file will create a `broker` thing and this can be removed if you have already setup a broker in another file or via the UI already.

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
