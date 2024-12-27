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
Bridge sbus:bridge-udp:mybridge [ host="192.168.1.100", port=5000 ]
```

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
Thing sbus:temperature:mybridge:temp1 [ address=2 ]
```

Supported channels:
* `temperature` - Current temperature reading

#### Switch Controller

```
Thing sbus:switch:mybridge:switch1 [ address=3 ]
```

Supported channels:
* `switch` - ON/OFF state

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
