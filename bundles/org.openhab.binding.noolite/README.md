# NooLite Binding

This binding is for [Noolite USB adapters](https://translate.google.com/translate?hl=ru&sl=ru&tl=en&u=http%3A%2F%2Fwww.noo.com.by%2Fadapter-noolite-pc.html), MTRF-64-USB adapter

## Supported Things

There is one supported thing type - 
It has the `devices` id.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description            |
|-----------|------------------------|
| port      | channel address        |
| type      | type of noolite device |

## Type of Noolite channels
| Value | Description  |
|-------|--------------|
| 0     | Noolite TX   |
| 1     | Noolite RX   |
| 2     | Noolite F TX |
| 3     | Noolite F RX |
| 4     | Service      |


## Channels

The Noolite information that is retrieved from channels:

| Channel ID  | Item Type | Description                               |
|-------------|-----------|-------------------------------------------|
| switch      | Switch    | Port set as input signal for switch using |
| temperature | Number    | Temperature from DHT sensor               |
| humidity    | Number    | Humidity from DHT sensor                  |
| battery     | String    | Battery state                             |
| sensortype  | String    | Sensor type                               |
| bindchannel | Number    | Bind number                               |

## Full Example

noolite.things:

```java
Bridge noolite:bridgeMTRF64:usb [serial="/dev/ttyUSB1"]{
  Thing devices firstFloorTemp [port=1, type ="1"]
  Thing devices firstFloorLamp [port=1, type ="0"]
}
```

noolite.items:

```java
Number    First_Floor_Temp         "Temperature on First Floor"  { channel="noolite:devices:usb:firstFloorTemp:temperature" }
Number    First_Floor_Hum          "Humidity on First Floor"  { channel="noolite:devices:usb:firstFloorLamp:humidity" }
String    First_Floor_Batt         "Battery state %s"  { channel="noolite:devices:usb:firstFloorLamp:battery" }
Number    First_Floor_Bind         "Bind number"  { channel="noolite:devices:usb:firstFloorLamp:bindchannel" }
String    First_Floor_SensorType   "Sensor type %s"  { channel="noolite:devices:usb:firstFloorLamp:sensortype" }
Switch    FFSwitch                 "Lamp on First Floor"  { channel="noolite:devices:usb:firstFloorTemp:switch" }
```
