# BigAssFan Binding

The [BigAssFan](http://www.bigassfans.com/) binding is used to enable communication between openHAB and Big Ass Fans'  Haiku family of residential fans that implement the SenseME technology.

## Overview

Fans are discovered dynamically.
There is a single thing created for each fan connected to the network.
Each thing has channels that allow control of the fan and the optional LED light, as well as to monitor the status of the fan.
When the fan is controlled from the remote control, Wall Controller, or smartphone app, the openHAB items linked to the fan's channels will be updated to reflect the fan's status.


## Device Discovery

The BigAssFan binding discovers Haiku fans on the network, and creates an inbox entry for each discovered device.
Once added as a thing, the user can control the fan and optional LED light kit, similarly to how the fan is controlled using the remote, Wall Controller, or smartphone app.

Background discovery polls the network every few minutes for fans.
Background discovery is **enabled** by default.
To **disable** background discovery, add the following line to the *conf/services/runtime.cfg* file:

```
org.openhab.binding.bigassfan.discovery.BigAssFanDiscoveryService:backgroundDiscovery.enabled=false
```


## Thing Configuration

The fan's IP address, MAC address, and name is set at time of discovery.
However, in the event that any of this information changes, the fan's configuration must be updated.

#### Manual Thing Creation

Fans can be manually created in the *PaperUI* or *HABmin*, or by placing a *.things* file in the *conf/things* directory.
See example below.

## Channels

The following channels are supported for fans:

| Channel Name            | Item Type    | Description                                           |
|-------------------------|--------------|-------------------------------------------------------|
| fan-power               | Switch       | Power on/off the fan                                  |
| fan-speed               | Dimmer       | Adjust the speed of the fan                           |
| fan-direction           | String       | Indicates the direction in which the fan is turning   |
| fan-auto                | Switch       | Enable/disable fan auto mode                          |
| fan-whoosh              | Switch       | Enable/disable fan "whoosh" mode                      |
| fan-smartmode           | String       | Set Smartmode to HEATING, COOLING, or OFF             |
| fan-learn-minspeed      | Dimmer       | Set minimum fan speed for Smartmode COOLING           |
| fan-learn-maxspeed      | Dimmer       | Set maximum fan speed for Smartmode COOLING           |
| fan-wintermode          | Switch       | Enable/disable fan winter mode                        |
| fan-speed-min           | Dimmer       | Set minimum fan speed                                 |
| fan-speed-max           | Dimmer       | Set maximum fan speed                                 |
| light-power             | Switch       | Power on/off the fan                                  |
| light-level             | Dimmer       | Adjust the brightness of the light from               |
| light-auto              | Switch       | Enable/disable light auto mode                        |
| light-smarter           | String       | Enable/disable Smarter Lighting                       |
| light-level-min         | Dimmer       | Set minimum light level for Smarter Lighting          |
| light-level-max         | Dimmer       | Set maximum light level for Smarter Lighting          |
| light-present           | String       | Indicates is a light is installed in the fan          |
| motion                  | Switch       | Motion was detected                                   |
| time                    | DateTime     | Fan's date and time                                   |

The following channels are supported for wall controllers:

| Channel Name            | Item Type    | Description                                           |
|-------------------------|--------------|-------------------------------------------------------|
| motion                  | Switch       | Motion was detected                                   |
| time                    | DateTime     | Wall controllers date and time                        |


## Fan Items

The following item definitions would be used to control the fan.

```
Switch PorchFanPower                { channel="bigassfan:fan:20F85EDAA56A:fan-power" }
Dimmer PorchFanSpeed                { channel="bigassfan:fan:20F85EDAA56A:fan-speed" }
Switch PorchFanAuto                 { channel="bigassfan:fan:20F85EDAA56A:fan-auto" }
Switch PorchFanWhoosh               { channel="bigassfan:fan:20F85EDAA56A:fan-whoosh" }
String PorchFanSmartmode            { channel="bigassfan:fan:20F85EDAA56A:fan-smartmode" }
Dimmer PorchFanSpeedMin             { channel="bigassfan:fan:20F85EDAA56A:fan-learn-minspeed" }
Dimmer PorchFanSpeedMax             { channel="bigassfan:fan:20F85EDAA56A:fan-learn-maxspeed" }
```

The following item definitions would be used to control the light.

```
Switch PorchFanLightPower           { channel="bigassfan:fan:20F85EDAA56A:light-power" }
Dimmer PorchFanLightLevel           { channel="bigassfan:fan:20F85EDAA56A:light-level" }
Switch PorchFanLightAuto            { channel="bigassfan:fan:20F85EDAA56A:light-auto" }
Switch PorchFanLightSmarter         { channel="bigassfan:fan:20F85EDAA56A:light-smarter" }
Dimmer PorchFanLightLevelMin        { channel="bigassfan:fan:20F85EDAA56A:light-level-min" }
Dimmer PorchFanLightLevelMax        { channel="bigassfan:fan:20F85EDAA56A:light-level-max" }
String PorchFanLightPresent         { channel="bigassfan:fan:20F85EDAA56A:light-present" }
```

The following readonly items are provided by the fan.

```
Switch PorchFanMotionSensor         { channel="bigassfan:fan:20F85EDAA56A:motion" }
DateTime PorchFanTime               { channel="bigassfan:fan:20F85EDAA56A:time" }
```


## Wall Controller Items

```
Switch PorchControllerMotionSensor  { channel="bigassfan:controller:20F85ED87F01:motion" }
DateTime PorchControllerTime        { channel="bigassfan:controller:20F85ED87F01:time" }
```


### Sitemap

This is an example of how to set up your sitemap.

```
Frame label="Control My BigAssFan" {
    Switch item=PorchFanPower label="Fan Power [%s]"
    Slider item=PorchFanSpeed label="Fan Speed [%s %%]"
    Switch item=PorchFanLightPower label="Light Power [%s]"
    Slider item=PorchFanLightLevel label="Light Brightness [%s %%]"
}
```

### Manual Thing Creation

Place a file named *bigassfan.things* in the *conf/things* directory.
The file should contain lines formatted like this.

```
bigassfan:fan:20F85EDAA56A [ label="Porch Fan", ipAddress="192.168.12.62", macAddress="20:F8:5E:DA:A5:6A" ]
```

#### Unsupported Features

Standalone lights are not supported.
