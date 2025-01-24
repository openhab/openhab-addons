# Sbus Binding

This binding integrates Sbus devices with openHAB, allowing control and monitoring of Sbus-compatible devices over UDP.
Sbus is a protocol used for home automation devices that communicate over UDP networks.
The binding supports various device types including RGB/RGBW controllers, temperature sensors, and switch controllers.

## Supported Things

- `udp` - Sbus Bridge for UDP communication
- `rgbw` - RGB/RGBW Controllers for color and brightness control
- `temperature` - Temperature Sensors for monitoring environmental conditions
- `switch` - Switch Controllers for basic on/off and dimming control

## Discovery

Sbus devices communicate via UDP broadcast, but manual configuration is required to set up the devices in openHAB.
Auto-discovery is not supported at this moment.

## Binding Configuration

The binding itself does not require any special configuration.

## Thing Configuration

### Bridge Configuration

The Sbus Bridge has the following configuration parameters:

| Name    | Type    | Description                                          | Default | Required | Advanced |
|---------|---------|------------------------------------------------------|---------|----------|-----------|
| host    | text    | IP address of the Sbus device (typically broadcast)  | N/A     | yes      | no        |
| port    | integer | UDP port number                                      | 6000    | no       | no        |

### RGBW Controller Configuration

| Name    | Type    | Description                                          | Default | Required | Advanced |
|---------|---------|------------------------------------------------------|---------|----------|-----------|
| subnetId| integer | Subnet ID the RGBW controller is part of             | N/A     | yes      | no        |
| id      | integer | Device ID of the RGBW controller                     | N/A     | yes      | no        |
| refresh | integer | Refresh interval in seconds                          | 30      | no       | yes       |

### Temperature Sensor Configuration

| Name    | Type    | Description                                          | Default | Required | Advanced |
|---------|---------|------------------------------------------------------|---------|----------|-----------|
| subnetId| integer | Subnet ID the temperature sensor is part of          | N/A     | yes      | no        |
| id      | integer | Device ID of the temperature sensor                  | N/A     | yes      | no        |
| refresh | integer | Refresh interval in seconds                          | 30      | no       | yes       |

### Switch Controller Configuration

| Name    | Type    | Description                                          | Default | Required | Advanced |
|---------|---------|------------------------------------------------------|---------|----------|-----------|
| subnetId| integer | Subnet ID the switch controller is part of           | N/A     | yes      | no        |
| id      | integer | Device ID of the switch controller                   | N/A     | yes      | no        |
| refresh | integer | Refresh interval in seconds                          | 30      | no       | yes       |

## Channels

### RGBW Controller Channels

| Channel | Type   | Read/Write | Description                                                |
|---------|--------|------------|------------------------------------------------------------|
| color   | Color  | RW         | HSB color picker that controls RGBW components (0-100%)    |
| switch  | Switch | RW         | On/Off control for the RGBW output with optional timer     |

### Temperature Sensor Channels

| Channel     | Type                | Read/Write | Description                    |
|-------------|---------------------|------------|--------------------------------|
| temperature | Number:Temperature  | R          | Current temperature reading. Can be configured to use Celsius (default) or Fahrenheit units    |

### Switch Controller Channels

| Channel | Type    | Read/Write | Description                                               |
|---------|---------|------------|-----------------------------------------------------------|
| switch  | Switch  | RW         | Basic ON/OFF state control                                |
| dimmer  | Dimmer  | RW         | ON/OFF state with timer transition                        |
| paired  | Contact | RW         | OPEN/CLOSED state for two paired channels (e.g., curtains)|

## Full Example

### Thing Configuration

```java
Bridge sbus:udp:mybridge [ host="192.168.1.255", port=5000 ] {
    Thing rgbw colorctrl [ id=72, refresh=30 ] {
        Channels:
            Type color-channel : color [ channelNumber=1 ]   // HSB color picker, RGBW values stored at channel 1
            Type switch-channel : power [ channelNumber=1 ]  // On/Off control for the RGBW output For complex scenes, one Sbus color controller can keep up to 40 color states. The switch channelNumber has to fall into this range.
    }
    
    Thing temperature temp1 [ id=62, refresh=30 ] {
        Channels:
            Type temperature-channel : temperature [ channelNumber=1 ]
    }
    
    Thing switch switch1 [ id=75, refresh=30 ] {
        Channels:
            Type switch-channel : first_switch  [ channelNumber=1 ]
            Type dimmer-channel : second_switch [ channelNumber=2 ]
            Type paired-channel : third_switch [ channelNumber=3 ]
    }
}
```

### Item Configuration

```java
// Temperature Sensor
Number:Temperature Temp_Sensor "Temperature [%.1f °C]" { channel="sbus:temperature:mybridge:temp1:temperature" }

// Basic Switch
Switch Light_Switch "Switch" { channel="sbus:switch:mybridge:switch1:switch" }

// Paired Channel (e.g., for curtains)
Contact Curtain_Switch "Curtain [%s]" { channel="sbus:switch:mybridge:switch1:third_switch" }

// RGBW Controller with Power Control
Group   gLight      "RGBW Light"    <light>     ["Lighting"]
Color   rgbwColor    "Color"        <colorwheel> (gLight)   ["Control", "Light"]    { channel="sbus:rgbw:mybridge:colorctrl:color" }
Switch  rgbwPower    "Power"        <switch>     (gLight)   ["Switch", "Light"]     { channel="sbus:rgbw:mybridge:colorctrl:power" }
```

### Sitemap Configuration

```perl
sitemap sbus label="Sbus Demo"
{
    Frame label="Sbus Controls" {
        Colorpicker item=Light_RGB
        Text item=Temp_Sensor
        Switch item=Light_Switch
        Text item=Curtain_Switch
    }
}
