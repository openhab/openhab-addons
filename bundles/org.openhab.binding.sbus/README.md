# OpenHAB SBUS Binding

This binding integrates SBUS devices with OpenHAB, allowing control and monitoring of SBUS-compatible devices over UDP.

## Supported Things

* SBUS Bridge (Thing Type: `bridge-udp`)
* RGB/RGBW Controllers
* Temperature Sensors
* Switch Controllers

## Installation

Install this binding through the OpenHAB console:

```
bundle:install org.openhab.binding.sbus
```

## Configuration

### Bridge Configuration

The SBUS Bridge requires the following configuration parameters:

* `host` - IP address of the SBUS device
* `port` - UDP port number (default: 5000)

Example:

```
Bridge sbus:udp:mybridge [ host="192.168.1.255", port=5000 ]
```

Please note the broadcast address. This is how Sbus devices communicate with each other.

### Thing Configuration

#### RGBW Controller

```
Thing sbus:rgbw:mybridge:light1 [ address=1 ]
```

Supported channels:

* `red` - Red component (0-100%)
* `green` - Green component (0-100%)
* `blue` - Blue component (0-100%)
* `white` - White component (0-100%)

#### Temperature Sensor

```
Thing temperature temp1 [ id=62, refresh=30 ] {
    Channels:
        Type temperature-channel : temperature [ channelNumber=1 ]
}
```

Supported channels:

* `temperature` - Current temperature reading

#### Switch Controller

```
Thing switch switch1 [ id=75, refresh=30 ] {
    Channels:
        Type switch-channel : first_switch  [ channelNumber=1 ]
        Type dimmer-channel : second_switch [ channelNumber=2 ]
        Type paired-channel : third_switch [ channelNumber=3 ]
}
```

Supported channels:

* `switch` - ON/OFF state
* `dimmer` - ON/OFF state with timer transition
* `paired` - ON/OFF state for two paired channels. This feature is used for curtains and other devices that require two actuator channels.

## Example Usage

items/sbus.items:

```
Color Light_RGB "RGB Light" { channel="sbus:rgbw:mybridge:light1:color" }
Number:Temperature Temp_Sensor "Temperature [%.1f °C]" { channel="sbus:temperature:mybridge:temp1:temperature" }
Switch Light_Switch "Switch" { channel="sbus:switch:mybridge:switch1:switch" }
```

sitemap/sbus.sitemap:

```
sitemap sbus label="SBUS Demo"
{
    Frame label="SBUS Controls" {
        Colorpicker item=Light_RGB
        Text item=Temp_Sensor
        Switch item=Light_Switch
    }
}
```

## Special Case: RGBW Controller with Power Control

Here's how to configure an RGBW controller with both color and power control:

```
// RGBW controller for color
Thing rgbw colorctrl [ id=72, refresh=30 ] {
    Channels:
        Type color-channel : color [ channelNumber=1 ]   // HSB color picker, RGBW values stored at channel 1
}

// Switch for power control
Thing switch powerctrl [ id=72, refresh=30 ] {
    Channels:
        Type switch-channel : power [ channelNumber=5, timer=-1 ]  // On/Off control for the RGBW output. Disable the timer functionality. The device doesn't support it.
}

// Light Group
Group   gLight      "RGBW Light"    <light>     ["Lighting"]

// Color Control
Color   rgbwColor    "Color"        <colorwheel> (gLight)   ["Control", "Light"]    { channel="sbus:rgbw:mybridge:colorctrl:color" }

// Power Control
Switch  rgbwPower    "Power"        <switch>     (gLight)   ["Switch", "Light"]     { channel="sbus:switch:mybridge:powerctrl:power" }
