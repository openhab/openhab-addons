# Adorne Binding

The Adorne Binding integrates [Adorne Wi-Fi ready devices](https://www.legrand.us/adorne/products/wireless-whole-house-lighting-controls.aspx) (switches, dimmers, outlets) from [Legrand](https://legrand.com/).

Legrand attempted to provide a public API based on Samsung's ARTIK Cloud and the initial version of this binding was based on that API.
However, Samsung shut down ARTIK Cloud shortly after the release and Legrand has not offered a public API replacement since.
That leaves direct interaction with the Adorne Hub as the only control option.
Consequently the openHAB server and the Adorne Hub must be located on the same network.

The Adorne Hub supports a REST API, but unfortunately there is no documentation or official support from Legrand.
This binding's implementation of the REST API is motivated by the great work of [sbozarth](https://github.com/sbozarth/homebridge-lc7001) who figured out the API details.

## Supported Things

| Thing Type | Description                                                                                                                                                           |
|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| hub        | The Adorne [Hub LC7001](https://www.legrand.us/adorne/products/wireless-whole-house-lighting-controls/lc7001.aspx) serves as the bridge to control all Adorne devices |
| switch     | All Adorne switches and outlets                                                                                                                                       |
| dimmer     | All Adorne dimmers                                                                                                                                                    |

## Discovery

Auto-discovery is supported as long as the hub can be discovered using the default host and port.
If the hub requires custom host and/or port configuration manual setup is required.

Background discovery is not supported.

## Thing Configuration

### Hub

The hub offers two optional configuration parameters:

| Parameter | Description                                                                                                                                                                                                                                                           |
|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| host      | The URL to reach the hub. The hub makes itself known through mDNS as `LCM1.local` and the host parameter defaults to this value. As long as the openHAB server and the hub are on the same broadcast domain for mDNS the host parameter doesn't need to be specified. |
| port      | The port the hub communicates on. By default the hub answers on port 2112 and the port parameter defaults to this value. As long as the hub configuration hasn't been changed the port parameter doesn't need to be specified.                                        |

### Devices

All devices share one required paramenter:

| Parameter | Description                                                                    |
|-----------|--------------------------------------------------------------------------------|
| zoneId    | The zone ID that is assigned by the hub to each device as a unique identifier. |

Legrand does not provide an easy way to look up a zone ID for a device.
However, zone IDs are simply assigned sequentially starting with 0 in the order devices are added to the hub.
So the first device will have zone ID 0, the next 1 and so on.

## Channels

| Channel Type ID | Item Type | Commands | Description             | Thing Types Supporting This Channel |
|-----------------|-----------|----------|-------------------------|-------------------------------------|
| power           | Switch    | ON, OFF  | Turn device on and off  | switch, dimmer                      |
| brightness      | Dimmer    | 1-100    | Set device's brightness | dimmer                              |

Note that the brightness channel is limited to values from 1 to 100.
All other commands are ignored.
That means in particular that a dimmer can't be turned off by sending 0 to the brightness channel.
Also, if a dimmer is turned off (via the power channel) and the brightness is updated the dimmer will remain off.
Once the dimmer is turned on it will turn on with the updated brightness setting.
Consequently when a dimmer is turned on it always returns to the most recent brightness setting.
In other words power and brightness states are controlled independently.
This matches how power and brightness are managed on the physical dimmer itself.

To avoid confusion for the user any UI must ensure that only values from 1 to 100 are passed to the brightness channel.
A default slider allows a 0 value and should not be used since there will be no response when the user selects 0.
Common UI choices are Sliders or Setpoints with a minimum value of 1 and a maximum value of 100 (min/max values in Sliders are only supported as of openHAB 2.5).

## Example

This is a simple example that uses an Adorne switch and two dimmers.
Remember that the host and port parameter are not needed in most cases.
As discussed above care is taken that the brightness channel only allows values from 1 to 100 by specifying a min and max value in the sitemap for the dimmers.
For this example to run on an openHAB version older than 2.5 Bedroom 1's Slider must be removed in the sitemap since older versions don't support the min/max setting.

## `demo.things` Example

```java
Bridge adorne:hub:home "Adorne Hub" [host="192.160.1.111", port=2113] {
 switch bathroom "Bathroom" [zoneId=0]
 dimmer bedroom1 "Bedroom1" [zoneId=1]
 dimmer bedroom2 "Bedroom2" [zoneId=2]
}
```

## `demo.items` Example

```java
Switch LightBathroom {channel="adorne:switch:home:bathroom:power"}
Switch LightBedroomSwitch1 {channel="adorne:dimmer:home:bedroom1:power"}
Dimmer LightBedroomDimmer1 {channel="adorne:dimmer:home:bedroom1:brightness"}
Switch LightBedroomSwitch2 {channel="adorne:dimmer:home:bedroom2:power"}
Dimmer LightBedroomDimmer2 {channel="adorne:dimmer:home:bedroom2:brightness"}
```

## `demo.sitemap` Example

```perl
sitemap demo label="Adorne Binding Demo"
{
 Frame label="Adorne Switch" {
  Switch item=LightBathroom label="Bathroom" mappings=["ON"="On", "OFF"="Off"] icon="light-on"
 }
 Frame label="Adorne Dimmer using Slider" {
  Switch item=LightBedroomSwitch1 label="Bedroom 1" mappings=["ON"="On", "OFF"="Off"] icon="light-on"
  Slider item=LightBedroomDimmer1 label="Bedroom 1" icon="light-on" minValue=1 maxValue=100 step=1
 }
 Frame label="Adorne Dimmer using Setpoint" {
  Switch item=LightBedroomSwitch2 label="Bedroom 2" mappings=["ON"="On", "OFF"="Off"] icon="light-on"
  Setpoint item=LightBedroomDimmer2 label="Bedroom 2" icon="light-on" minValue=1 maxValue=100 step=5
 }
}
```
