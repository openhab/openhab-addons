# myStrom Binding

This extension adds support for the myStrom devices.
As of today only the Smart Plug, Bulb and the Motionsensor are implemented.

## Supported Things

This bundle adds the following thing types:

| Thing                 | ThingTypeID | Description                                        |
| ----------------------| ----------- | -------------------------------------------------- |
| myStrom Smart Plug    | mystromplug | A myStrom smart plug                               |
| myStrom Bulb          | mystrombulb | A myStrom bulb                                     |
| myStrom Motion Sensor | mystrompir  | A myStrom PIR motion sensor                        |

According to the myStrom API documentation all request specific to the myStrom Bulb are also work on the LED strip.

## Discovery

This extension does not support autodiscovery. The things need to be added manually.

## Thing Configuration

The following parameters are valid for all thing types:

| Parameter | Type    | Required | Default if omitted | Description                                                                |
| --------- | ------- | -------- | ------------------ | -------------------------------------------------------------------------- |
| hostname  | string  | yes      | localhost          | The IP address or hostname of the myStrom smart plug                       |
| refresh   | integer | no       | 10                 | Poll interval in seconds. Increase this if you encounter connection errors |
| apiToken  | string  | no       |                    | Specifies the API Token, if required.                                      |

## Properties

In addition to the configuration a myStrom thing has the following properties.
The properties are updated during initialize.
Disabling/enabling the thing can be used to update the properties.

| Property-Name | Description                                                           |
| ------------- | --------------------------------------------------------------------- |
| version       | Current firmware version                                              |
| type          | The type of the device (i.e. bulb = 102)                              |
| ssid          | SSID of the currently connected network                               |
| ip            | Current ip address                                                    |
| mask          | Mask of the current network                                           |
| gateway       | Gateway of the current network                                        |
| dns           | DNS of the current network                                            |
| static        | Whether or not the ip address is static                               |
| connected     | Whether or not the device is connected to the internet                |
| mac           | The mac address of the bridge in upper case letters without delimiter |

## Channels

| Channel ID       | Item Type            | Read only | Description                                                           | Thing types supporting this channel |
| ---------------- | -------------------- | --------- | --------------------------------------------------------------------- |-------------------------------------|
| switch           | Switch               | false     | Turn the device on or off                                             | mystromplug, mystrombulb            |
| power            | Number:Power         | true      | The currently delivered power                                         | mystromplug, mystrombulb            |
| temperature      | Number:Temperature   | true      | The temperature at the plug                                           | mystromplug, mystrompir             |
| color            | Color                | false     | The color we set the bulb to (mode 'hsv')                             | mystrombulb                         |
| colorTemperature | Dimmer               | false     | The color temperature of the bulb in mode 'mono' (percentage)         | mystrombulb                         |
| brightness       | Dimmer               | false     | The brightness of the bulb in mode 'mono'                             | mystrombulb                         |
| ramp             | Number:Time          | false     | Transition time from the light’s current state to the new state. [ms] | mystrombulb                         |
| mode             | String               | false     | The color mode we want the Bulb to set to (rgb, hsv or mono)          | mystrombulb                         |
| light            | Dimmer               | true      | The brightness of the Room.                                           | mystrompir                          |
| motion           | Switch               | true      | Motionstatus of the sensor                                            | mystrompir                          |

## Full Example

### Thing Configuration

```java
Thing mystrom:mystromplug:d6217a31 "Plug" [hostname="hostname|ip"]
```

### Item Configuration

```java
Switch PlugSwitch  "Plug"                       {channel="mystrom:mystromplug:d6217a31:switch"}
Number:Temperature PlugTemperature "Temperature: [%.1f °C]"     {channel="mystrom:mystromplug:d6217a31:temperature"}  
Number:Power PlugPower "Power: [%.1f W]"                        {channel="mystrom:mystromplug:d6217a31:power"} 

```

### Sitemap Configuration

```perl
Frame label="myStrom Plug" { 
    Switch item=PlugSwitch
    Text item=PlugTemperature
    Text item=PlugPower
}
```
