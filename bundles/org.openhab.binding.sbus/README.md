# Sbus Binding

This binding integrates Sbus devices with openHAB, allowing control and monitoring of Sbus-compatible devices over UDP.
Sbus is a protocol used for home automation devices that communicate over UDP networks.
The binding supports various device types including RGB/RGBW controllers, temperature sensors, and switch controllers.

## Supported Things

- `udp` - Sbus Bridge for UDP communication
- `rgbw` - RGB/RGBW Controllers for color and brightness control
- `temperature` - Temperature Sensors for monitoring environmental conditions
- `switch` - Switch Controllers for basic on/off and dimming control
- `contact` - Contact Sensors for monitoring open/closed states

## Discovery

Sbus devices communicate via UDP broadcast, but manual configuration is required to set up the devices in openHAB.
Auto-discovery is not supported at this moment.

## Binding Configuration

The binding itself does not require any special configuration.

_note_ If openHAB is deployed in a Docker container, you must set the `network_mode` to host. Without this setting, messages on the host network will not reach the Docker container's internal networks.

## Thing Configuration

### Bridge Configuration

The Sbus Bridge has the following configuration parameters:

| Name    | Type    | Description                                          | Default | Required | Advanced |
|:--------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| host    | text    | IP address of the Sbus device (typically broadcast)  | N/A     | yes      | no        |
| port    | integer | UDP port number                                      | 6000    | no       | no        |

### RGBW Controller, Contact, Switch, Temperature Configuration

| Name    | Type    | Description                                          | Default | Required | Advanced |
|:--------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| subnetId| integer | Subnet ID the RGBW controller is part of             | N/A     | yes      | no        |
| id      | integer | Device ID of the RGBW controller                     | N/A     | yes      | no        |
| refresh | integer | Refresh interval in seconds                          | 30      | no       | yes       |

## Channels

### RGBW Controller Channels

| Channel | Type   | Read/Write | Description                                                |
|:--------|:-------|:----------:|:-----------------------------------------------------------|
| color   | Color  | RW         | HSB color picker that controls RGBW components (0-100%). Can be configured to disable the white channel. |
| switch  | Switch | RW         | On/Off control for the RGBW output with optional timer     |

The color channel of RGBW controllers supports these additional parameters:

| Parameter   | Type    | Description                                          | Default | Required | Advanced |
|:------------|:--------|:-----------------------------------------------------|:-------:|:--------:|:---------:|
| channelNumber | integer | The physical channel number on the Sbus device     | N/A     | yes      | no        |
| enableWhite | boolean | Controls the white component support for RGB palette | true    | no       | yes       |

### Temperature Sensor Channels

| Channel     | Type                | Read/Write | Description                    |
|:------------|:--------------------|:----------:|:-------------------------------|
| temperature | Number:Temperature  | R          | Current temperature reading. Can be configured to use Celsius (default) or Fahrenheit units    |

### Switch Controller Channels

| Channel | Type           | Read/Write | Description                                               |
|:--------|:---------------|:----------:|:----------------------------------------------------------|
| switch  | Switch         | RW         | Basic ON/OFF state control                                |
| dimmer  | Dimmer         | RW         | ON/OFF state with timer transition                        |
| paired  | Rollershutter  | RW         | UP/DOWN/STOP control for two paired channels (e.g., rollershutters)|

### Contact Sensor Channels

| Channel | Type    | Read/Write | Description                                               |
|:--------|:--------|:----------:|:----------------------------------------------------------|
| contact | Contact | R          | Contact state (OPEN/CLOSED)                               |

## Full Example

### Thing Configuration

```java
Bridge sbus:udp:mybridge [ host="192.168.1.255", port=5000 ] {
    Thing rgbw colorctrl [ id=72, refresh=30 ] {
        Channels:
            Type color-channel : color [ channelNumber=1, enableWhite=true ]   // Full RGBW control with white component
            Type switch-channel : power [ channelNumber=1 ]  // On/Off control for the RGBW output
    }
    
    Thing rgbw rgbonly [ id=73, refresh=30 ] {
        Channels:
            Type color-channel : color [ channelNumber=1, enableWhite=false ]  // RGB only, no white component
            Type switch-channel : power [ channelNumber=1 ]
    }
    
    Thing temperature temp1 [ id=62, refresh=30 ] {
        Channels:
            Type temperature-channel : temperature [ channelNumber=1 ]
    }
    
    Thing switch switch1 [ id=75, refresh=30 ] {
        Channels:
            Type switch-channel : first_switch  [ channelNumber=1 ]
            Type dimmer-channel : second_switch [ channelNumber=2 ]
            Type paired-channel : third_switch [ channelNumber=3, pairedChannelNumber=4 ]
    }
    
    Thing contact contact1 [ id=80, refresh=30 ] {
        Channels:
            Type contact-channel : contact [ channelNumber=1 ]
    }
}
```

### Item Configuration

```java
// Temperature Sensor
Number:Temperature Temp_Sensor "Temperature [%.1f °C]" { channel="sbus:temperature:mybridge:temp1:temperature" }

// Basic Switch
Switch Light_Switch "Switch" { channel="sbus:switch:mybridge:switch1:switch" }

// Paired Channel (e.g., for rollershutters)
Rollershutter Rollershutter_Switch "Rollershutter [%s]" { channel="sbus:switch:mybridge:switch1:third_switch" }

// RGBW Controller with Power Control
Group   gLight      "RGBW Light"    <light>     ["Lighting"]
Color   rgbwColor    "Color"        <colorwheel> (gLight)   ["Control", "Light"]    { channel="sbus:rgbw:mybridge:colorctrl:color" }
Switch  rgbwPower    "Power"        <switch>     (gLight)   ["Switch", "Light"]     { channel="sbus:rgbw:mybridge:colorctrl:power" }

// Contact Sensor
Contact Door_Contact "Door [%s]" <door> { channel="sbus:contact:mybridge:contact1:contact" }
```

### Sitemap Configuration

```perl
sitemap sbus label="Sbus Demo"
{
    Frame label="Sbus Controls" {
        Colorpicker item=Light_RGB
        Text item=Temp_Sensor
        Switch item=Light_Switch
        Rollershutter item=Rollershutter_Switch
        Text item=Door_Contact
    }
}

## Usage Notes

### RGB vs. RGBW Mode

The `enableWhite` parameter for color channels controls whether the white component is used:

- Set to `true` (default): Full RGBW control with white component
- Set to `false`: RGB-only control with no white component

This is useful for:
- Pure RGB fixtures without a white channel
- Creating saturated colors without white dilution
- Specialized color effects where white would wash out the intended color

### Color Control and On/Off Functionality

The Color item type in openHAB inherently supports both color selection and on/off functionality:
- The color picker controls hue and saturation
- The brightness component (0-100%) functions as the on/off control
  - When brightness is 0%, the light is OFF
  - When brightness is >0%, the light is ON

This is why a Color item shows both a color picker and an on/off button in the UI without requiring a separate Switch item.
