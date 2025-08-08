# C-Bus Binding

This is the binding for the [Clipsal C-Bus System](https://www.clipsal.com/products/c-bus-control-and-management-system).
This binding allows you to view and control groups on C-Bus networks from openHAB.

## Configuration

This binding connects to C-Gate software which can be downloaded from the [Clipsal Downloads Site](https://updates.clipsal.com/ClipsalSoftwareDownload/mainsite/cis/technical/index.html).
There is information about setting up the C-Gate software in the [CBus Forums](https://www.cbusforums.com/forums/c-bus-toolkit-and-c-gate-software.4).
Make sure that the config/access.txt file allows a connection from computer running openHAB.

Whilst all versions of C-Gate should work, some need special attention:

- Versions before 2.11.2 lack a fix for handling Indicator Kill messages for trigger groups. Without that they will remain on the last value set and wont match what is shown on CBus devices.
- Versions from 3.4.0 or later have `event-millis` set to true by default, the binding cannot handle this. Adapt your cgate configuration file `C-GateConfig.txt` and set `event-millis=no`.

First the CGate Connection bridge needs to be configured with the ip address of the computer running the C-Gate software.
After this a Bridge is creaed for each network configured on the CBus Network. The CBus Project Name and the network Id for that network

## Supported Things

This binding support 6 different things types

| Thing       | Type   | Description                                         |
|-------------|--------|-----------------------------------------------------|
| cgate       | Bridge | This connects to a C-Bus CGate instance to          |
| network     | Bridge | This connects to a C-Bus Network via a CGate bridge |
| light       | Thing  | This is for C-Bus lighting groups                   |
| temperature | Thing  | This is for C-Bus temperature groups                |
| trigger     | Thing  | This is for C-Bus trigger groups                    |
| dali        | Thing  | This is for C-Bus DALI dimming groups               |

When a discovery scan is started in the UI, Things are discovered for the groups that are found on the CBus network.

## Channels

At startup the binding will scan the network for the values of all the groups and set those on the appropriate channels.
It is not possible to fetch the value of a Trigger Group so those values will only be updated when a trigger is set on the CBus network.

### Lights

Light things have 2 channels which show the current state of the group on the cbus network and can also set the state of the group:-

- **state** - On/Off state of the light
- **level** - The level of the channel between 0 and 100

### Temperature

Temperature things have 1 channel which shows the current value. This is read-only and will not set the value on the CBus Network.

- **temp** - Temperature value

### Trigger

Trigger things have 1 channel which shows the current trigger value on the cbus network and can be used to set a trigger value on the CBus Network.

- **value** - CBus Trigger value

### Dali

Dali things have 1 channel which shows the current value on the cbus network and can be used to set a value on the CBus Network.

- **level** - Value from the DALI node

## Example

### cbus.things

```java

/* Need a cgate bridge to connect to cgate and then 1 network bridge for each network on that system */
Bridge cbus:cgate:cgatenetwork "file - cgate" [ ipAddress="127.0.0.1"] {
  Bridge network cbusnetwork "file - network" [ id=254, project="OURHOME" ] {
    /* Things can be configured within each network bridge */
    Thing light light27 "light 27" [group=27]
  }
}

/* Things can be configured seperatly and associated with the network bridge */
Thing cbus:light:cgatenetwork:cbusnetwork:light31 "light 31" (cbus:network:cgatenetwork:cbusnetwork) [ group=31 ]
Thing cbus:trigger:cgatenetwork:cbusnetwork:trigger1 "trigger 1" (cbus:network:cgatenetwork:cbusnetwork) [ group=1 ]
Thing cbus:temperature:cgatenetwork:cbusnetwork:temp2 "temp 2" (cbus:network:cgatenetwork:cbusnetwork) [ group=2 ]
Thing cbus:dali:cgatenetwork:cbusnetwork:dali3 "dali 3 value" (cbus:network:cgatenetwork:cbusnetwork) [ group=3 ]
```

### cbus.items

```java
Dimmer light31Dimmer { channel="cbus:light:cgatenetwork:cbusnetwork:light31:level"}
Switch light31Switch { channel="cbus:light:cgatenetwork:cbusnetwork:light31:state"}
Number trigger1Value { channel="cbus:trigger:cgatenetwork:cbusnetwork:trigger1:value"}
Number temp2 { channel="cbus:temperature:cgatenetwork:cbusnetwork:temp2:temp"}
Dimmer dali3 { channel="cbus:dali:cgatenetwork:cbusnetwork:dali3:level"}
```

### cbusdemo.sitemap

```perl
sitemap cbusdemo label="CBus Binding Demo"
{
    Frame label="light" {
        Slider item=light31Dimmer label="dimmer"
        Switch item=light31Switch label="switch"
    }
    Frame label="trigger" {
         Switch item=trigger1Value label="trigger Value" mappings=[0="light 1", 1="light 2", 2="both lights", 3="off"]
    }
    Frame label="temperature" {
            Default item=temp2 label="Temperature" icon="temperature"
    }
    Frame label="dali" {
            Default item=dali3 label="Dali Level"
    }
}
```
