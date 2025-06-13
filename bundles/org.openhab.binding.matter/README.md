# Matter Binding

The Matter Binding for openHAB allows seamless integration with Matter-compatible devices.

It currently supports version 1.4.1 of the Matter specification and earlier.

## Supported functionality

This binding supports two different types of Matter functionality which operate independently of each other.

- [Matter Client](#matter-client)
  - This allows openHAB to discover and control other Matter devices like lights, thermostats, window coverings, locks, etc...

- [Matter Bridge](#matter-bridge)
  - This allows openHAB to expose items as Matter devices to other Matter clients.
  This allows local control of openHAB devices from other ecosystems like Apple Home, Amazon, and Google Home.

For more information on the Matter specification, see the [Matter Ecosystem Overview](#matter-ecosystem-overview) section at the end of this document.

## Matter.JS Runtime

This binding uses the excellent [matter.js](https://github.com/project-chip/matter.js) implementation of the the Matter 1.4.1 protocol.

As such, this binding requires NodesJS 18+ and will attempt to download and cache an appropriate version when started if a version is not already installed on the system.
Alpine Linux users (typically docker) and those on older Linux distributions will need to install this manually as the official NodeJS versions are not compatible.

## Matter and IPv6

Matter **requires** IPv6 to be enabled and be routable between openHAB and the Matter device.
This means IPv6 needs to be enabled on the host openHAB is running, and the network must be able route IPv6 unicast and multicast messages.
Docker, VLANs, subnets and other configurations can prohibit Matter from working if not configured correctly.


# Matter Client

This describes the Matter controller functionality for discovering and controlling Matter devices.

## Supported Things

The Matter Binding supports the following types of things:

- `controller`: The main controller that interfaces with Matter devices.
It requires the configuration parameter `nodeId` which sets the local Matter node ID for this controller (must be unique in the fabric).
**This must be added manually.**
- `node`: Represents an individual Node within the Matter network.
The only configuration parameter is `nodeId`.
A standard Node will map Matter endpoints to openHAB channel groups.
**This will be discovered automatically** when a pairing code is used to scan for a device and should not be added manually.
- `endpoint`: Represents an standalone endpoint as a child of a `node` thing. Only Endpoints exposed by Matter bridges will be added as `endpoint` things, otherwise Matter Endpoints are mapped on a `node` thing as channel groups. An `endpoint` thing **will be discovered automatically** when a node is added that has multiple bridged endpoints and should not be added manually.

## Discovery

Matter controllers must be added manually.
Nodes (devices) will be discovered when a `pairCode` is used to search for a device to add.
Bridged endpoints will be added to the inbox once the parent Node is added as a thing.

### Device Pairing: General

The pairing action can be found in the settings of the "Controller" thing under the "Actions" -> "Pair Matter Device" 

<img src="doc/pairing.png" alt="Matter Pairing" width="600"/>

This action will give feedback on the pairing process, if successful a device will be added to the Inbox.

See [Device Pairing: Code Types](#device-pairing-code-types) for more information on pairing codes and code formats.

The same codes can also be used in the openHAB Thing discovery UI, although feedback is limited and only a single controller is supported.  

<img src="doc/thing-discovery.png" alt="Thing Discovery" width="600"/>

### Device Pairing: Code Types

In order to pair (commission in matter terminology) a device, you must have an 11 digit manual pairing code (eg 123-4567-8901 or 12345678901) or a QR Code (eg MT:ABCDEF1234567890123).
If the device has not been paired before, use the code provided by the manufacturer and **ensure the device is in pairing mode**, refer to your devices instructions for pairing for more information.
You can include dashes or omit them in a manual pairing code.

If the device is paired with another Matter ecosystem (Apple, Google, Amazon, etc..) you must use that ecosystem to generate a new pairing code and search for devices.  
The pairing code and device will only be available for commissioning for a limited time.
Refer to the ecosystem that generated the code for the exact duration (typically 5-15 minutes). In this case, openHAB still talks directly to the device and is not associated with that existing ecosystem.

If the device seems to be found in the logs, but can not be added, its possible the device has been already paired.
Hard resetting the device may help this case.
See your device documentation for how to hard reset the device.

### Device Pairing: Thread Devices

Thread devices require a Thread Border Router and a bluetooth enabled device to facilitate the thread joining process (typically a mobile device).
Until there is a supported thread border router integration in openHAB and the openHAB mobile apps, it's strongly recommended to pair the device to a commercial router with thread support first (Apple TV 4k, Google Nest Hub 2, Amazon Gen 4 Echo, etc... ), then generate a matter pairing code using that ecosystem and add the device normally.
This will still allow openHAB to have direct access to the device using only the embedded thread border router and does not interact with the underlying providers home automation stack.

Support for using a OpenThread Border Router has been verified to work and will be coming soon to openHAB, but in some cases requires strong expertise in IPv6 routing as well as support in our mobile clients. 

### Enabling IPv6 Thread Connectivity on Linux Hosts

It is important to make sure that Route Announcements (RA) and Route Information Options (RIO) are enabled on your host so that Thread boarder routers can announce routes to the Thread network.
This is done by setting the following sysctl options:

1. `net.ipv6.conf.wlan0.accept_ra` should be at least `1` if ip forwarding is not enabled, and `2` otherwise.
1. `net.ipv6.conf.wlan0.accept_ra_rt_info_max_plen` should not be smaller than `64`.

the `accept_ra` is defaulted to `1` for most distributions.

There may be other network daemons which will override this option (for example, dhcpcd on Raspberry Pi will override accept_ra to 0).

You can check the accept_ra value with:

```shell
$ sudo sysctl -n net.ipv6.conf.wlan0.accept_ra
0
```

And set the value to 1 (or 2 in case IP forwarding is enabled) with:

```shell
$ sudo sysctl -w net.ipv6.conf.wlan0.accept_ra=1
Net.ipv6.conf.wlan0.accept_ra = 1
```

The accept_ra_rt_info_max_plen option on most Linux distributions is default to 0, set it to 64 with:

```shell
$ sudo sysctl -w net.ipv6.conf.wlan0.accept_ra_rt_info_max_plen=64
net.ipv6.conf.wlan0.accept_ra_rt_info_max_plen = 64
```

To make these changes permanent, add the following lines to `/etc/sysctl.conf`:

```ini
net.ipv6.conf.eth0.accept_ra=1
net.ipv6.conf.eth0.accept_ra_rt_info_max_plen=64
```

Raspberry Pi users may need to add the following lines to `/etc/dhcpcd.conf` to prevent dhcpcd from overriding the accept_ra value:

```ini
noipv6
noipv6rs
```

***NOTE:  Please ensure you use the right interface name for your network interface.*** The above examples use `wlan0` and `eth0` as examples.
You can find the correct interface name by running `ip a` and looking for the interface that has an IPv6 address assigned to it.

## Thing Configuration

### Controller Thing Configuration

The controller thing must be created manually before devices can be discovered.

| Name   | Type   | Description                            | Default | Required | Advanced |
|--------|--------|----------------------------------------|---------|----------|----------|
| nodeId | number | The matter node ID for this controller | 0       | yes      | no       |

Note: The controller nodeId must not be changed after a controller is created.  

### Node Thing Configuration

Nodes are discovered automatically (see [Discovery](#Discovery) for more information) and should not be added manually.

| Name       | Type   | Description                        | Default | Required | Advanced |
|------------|--------|------------------------------------|---------|----------|----------|
| nodeId     | text   | The node ID of the endpoint        | N/A     | yes      | no       |

### Endpoint Thing Configuration

 Endpoints are discovered automatically once their parent Node has been added (see [Discovery](#Discovery) for more information) and should not be added manually.

| Name       | Type   | Description                        | Default | Required | Advanced |
|------------|--------|------------------------------------|---------|----------|----------|
| endpointId | number | The endpoint ID within the node    | N/A     | yes      | no       |

## Thing Actions

### Node Thing Actions

| Name                                            | Description                                                                                                                                                                                                                                                               |
|-------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Decommission Matter node from fabric            | This will remove the device from the Matter fabric. If the device is online and reachable this will attempt to remove the credentials from the device first before removing it from the network. Once a device is removed, this Thing will go offline and can be removed. |
| Generate a new pairing code for a Matter device | Generates a new manual and QR pairing code to be used to pair the Matter device with an external Matter controller                                                                                                                                                        |
| List Connected Matter Fabrics                   | This will list all the Matter fabrics this node belongs to                                                                                                                                                                                                                |
| Remove Connected Matter Fabric                  | This removes a connected Matter fabric from a device. Use the 'List connected Matter fabrics' action to retrieve the fabric index number                                                                                                                                  |


For nodes that contain a Thread Border Router Management Cluster, the following additional actions will be present

| Name                                         | Description                                                                                                                                                                                       |
|----------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Thread: Load external operational dataset    | Updates the local operational dataset configuration from a hex or JSON string for the node. Use the 'Push local operational dataset' action to push the dataset back to the device after loading. |
| Thread: Load operational dataset from device | Updates the local operational dataset configuration from the device.                                                                                                                                                      |
| Thread: Operational Dataset Generator        | Generates a new operational dataset and optionally saves it locally.                                                                                                                              |
| Thread: Push local operational dataset       | Pushes the local operational dataset configuration to the device.                                                                                                                                 |

A Thread operational data set is a hex encoded string which contains a Thread border router's configuration.
Using the same operational data set across multiple Thread border routers allows those routers to form a single network where Thread devices can roam from router to router.
Some Thread border routers allow a "pending" operational dataset to be configured, this allows routers to coordinate the configuration change with current Thread devices without requiring those devices to be reconfigured (live migration).

## Channels

### Controller Channels

Controllers have no channels.

### Node and Bridge Endpoint Channels

Channels are dynamically added based on the endpoint type and matter cluster supported.
Each endpoint is represented as a channel group.
Possible channels include:

## Endpoint Channels

| Channel ID                                                  | Type                     | Label                        | Description                                                                                                                                                                                                                                                          | Category         | ReadOnly | Pattern     |
|-------------------------------------------------------------|--------------------------|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|----------|-------------|
| battery-voltage                                             | Number:ElectricPotential | Battery Voltage              | The current battery voltage                                                                                                                                                                                                                                          | Energy           | true     | %.1f %unit% |
| battery-alarm                                               | String                   | Battery Alarm                | The battery alarm state                                                                                                                                                                                                                                              | Energy           | true     |             |
| powersource-batpercentremaining                             | Number:Dimensionless     | Battery Percent Remaining    | Indicates the estimated percentage of battery charge remaining until the battery will no longer be able to provide power to the Node                                                                                                                                 | Energy           | true     | %d %%       |
| powersource-batchargelevel                                  | Number                   | Battery Charge Level         | Indicates a coarse ranking of the charge level of the battery, used to indicate when intervention is required                                                                                                                                                        | Energy           | true     |             |
| booleanstate-statevalue                                     | Switch                   | Boolean State                | Indicates a boolean state value                                                                                                                                                                                                                                      | Status           | true     |             |
| colorcontrol-color                                          | Color                    | Color                        | The color channel allows to control the color of a light. It is also possible to dim values and switch the light on and off.                                                                                                                                         | ColorLight       |          |             |
| colorcontrol-temperature                                    | Dimmer                   | Color Temperature            | Sets the color temperature of the light                                                                                                                                                                                                                              | ColorLight       |          |             |
| colorcontrol-temperature-abs                                | Number:Temperature       | Color Temperature            | Sets the color temperature of the light in mirek                                                                                                                                                                                                                     | ColorLight       |          | %.0f %unit% |
| doorlock-lockstate                                          | Switch                   | Door Lock State              | Locks and unlocks the door and maintains the lock state                                                                                                                                                                                                              | Door             |          |             |
| fancontrol-fanmode                                          | Number                   | Fan Mode                     | Set the fan mode                                                                                                                                                                                                                                                     | HVAC             |          |             |
| onoffcontrol-onoff                                          | Switch                   | Switch                       | Switches the power on and off                                                                                                                                                                                                                                        | Light            |          |             |
| levelcontrol-level                                          | Dimmer                   | Dimmer                       | Sets the level of the light                                                                                                                                                                                                                                          | Light            |          |             |
| modeselect-mode                                             | Number                   | Mode Select                  | Selection of 1 or more states                                                                                                                                                                                                                                        |                  |          | %d          |
| switch-switch                                               | Number                   | Switch                       | Indication of a switch or remote being activated                                                                                                                                                                                                                     |                  | true     | %d          |
| switch-switchlatched                                        | Trigger                  | Switched Latched Trigger     | This trigger shall indicate the new value of the CurrentPosition attribute as a JSON object, i.e. after the move.                                                                                                                                                    |                  |          |             |
| switch-initialpress                                         | Trigger                  | Initial Press Trigger        | This trigger shall indicate the new value of the CurrentPosition attribute as a JSON object, i.e. while pressed.                                                                                                                                                     |                  |          |             |
| switch-longpress                                            | Trigger                  | Long Press Trigger           | This trigger shall indicate the new value of the CurrentPosition attribute as a JSON object, i.e. while pressed.                                                                                                                                                     |                  |          |             |
| switch-shortrelease                                         | Trigger                  | Short Release Trigger        | This trigger shall indicate the previous value of the CurrentPosition attribute as a JSON object, i.e. just prior to release.                                                                                                                                        |                  |          |             |
| switch-longrelease                                          | Trigger                  | Long Release Trigger         | This trigger shall indicate the previous value of the CurrentPosition attribute as a JSON object, i.e. just prior to release.                                                                                                                                        |                  |          |             |
| switch-multipressongoing                                    | Trigger                  | Multi-Press Ongoing Trigger  | This trigger shall indicate 2 numeric fields as a JSON object. The first is the new value of the CurrentPosition attribute, i.e. while pressed. The second is the multi press code with a value of N when the Nth press of a multi-press sequence has been detected. |                  |          |             |
| switch-multipresscomplete                                   | Trigger                  | Multi-Press Complete Trigger | This trigger shall indicate 2 numeric fields as a JSON object. The first is the new value of the CurrentPosition attribute, i.e. while pressed. The second is how many times the momentary switch has been pressed in a multi-press sequence.                        |                  |          |             |
| thermostat-localtemperature                                 | Number:Temperature       | Local Temperature            | Indicates the local temperature provided by the thermostat                                                                                                                                                                                                           | HVAC             | true     | %.1f %unit% |
| thermostat-outdoortemperature                               | Number:Temperature       | Outdoor Temperature          | Indicates the outdoor temperature provided by the thermostat                                                                                                                                                                                                         | HVAC             | true     | %.1f %unit% |
| thermostat-occupiedheating                                  | Number:Temperature       | Occupied Heating Setpoint    | Set the heating temperature when the room is occupied                                                                                                                                                                                                                | HVAC             |          | %.1f %unit% |
| thermostat-occupiedcooling                                  | Number:Temperature       | Occupied Cooling Setpoint    | Set the cooling temperature when the room is occupied                                                                                                                                                                                                                | HVAC             |          | %.1f %unit% |
| thermostat-unoccupiedheating                                | Number:Temperature       | Unoccupied Heating Setpoint  | Set the heating temperature when the room is unoccupied                                                                                                                                                                                                              | HVAC             |          | %.1f %unit% |
| thermostat-unoccupiedcooling                                | Number:Temperature       | Unoccupied Cooling Setpoint  | Set the cooling temperature when the room is unoccupied                                                                                                                                                                                                              | HVAC             |          | %.1f %unit% |
| thermostat-systemmode                                       | Number                   | System Mode                  | Set the system mode of the thermostat                                                                                                                                                                                                                                | HVAC             |          |             |
| thermostat-runningmode                                      | Number                   | Running Mode                 | The running mode of the thermostat                                                                                                                                                                                                                                   | HVAC             | true     |             |
| windowcovering-lift                                         | Rollershutter            | Window Covering Lift         | Sets the window covering level - supporting open/close and up/down type commands                                                                                                                                                                                     | Blinds           |          | %.0f %%     |
| fancontrol-percent                                          | Dimmer                   | Fan Control Percent          | The current fan speed percentage level                                                                                                                                                                                                                               | HVAC             | true     | %.0f %%     |
| fancontrol-mode                                             | Number                   | Fan Control Mode             | The current mode of the fan                                                                                                                                                                                                                                          | HVAC             |          |             |
| temperaturemeasurement-measuredvalue                        | Number:Temperature       | Temperature                  | The measured temperature                                                                                                                                                                                                                                             | Temperature      | true     | %.1f %unit% |
| occupancysensing-occupied                                   | Switch                   | Occupancy                    | Indicates if an occupancy sensor is triggered                                                                                                                                                                                                                        | Presence         | true     |             |
| relativehumiditymeasurement-measuredvalue                   | Number:Dimensionless     | Humidity                     | The measured humidity                                                                                                                                                                                                                                                | Humidity         | true     | %.0f %%     |
| illuminancemeasurement-measuredvalue                        | Number:Illuminance       | Illuminance                  | The measured illuminance in Lux                                                                                                                                                                                                                                      | Illuminance      | true     | %d %unit%   |
| wifinetworkdiagnostics-rssi                                 | Number:Power             | Signal                       | Wi-Fi signal strength indicator.                                                                                                                                                                                                                                     | QualityOfService | true     | %d %unit%   |
| electricalpowermeasurement-activepower                      | Number:Power             | Active Power                 | The active power measurement in watts                                                                                                                                                                                                                                | Energy           | true     | %.1f %unit% |
| electricalpowermeasurement-activecurrent                    | Number:ElectricCurrent   | Active Current               | The active current measurement in amperes                                                                                                                                                                                                                            | Energy           | true     | %.1f %unit% |
| electricalpowermeasurement-voltage                          | Number:ElectricPotential | Voltage                      | The voltage measurement in volts                                                                                                                                                                                                                                     | Energy           | true     | %.2f %unit% |
| electricalenergymeasurement-energymeasurmement-energy       | Number:Energy            | Energy                       | The measured energy                                                                                                                                                                                                                                                  | Energy           | true     | %.1f %unit% |
| electricalenergymeasurement-cumulativeenergyimported-energy | Number:Energy            | Cumulative Energy Imported   | The cumulative energy imported measurement                                                                                                                                                                                                                           | Energy           | true     | %.1f %unit% |
| electricalenergymeasurement-cumulativeenergyexported-energy | Number:Energy            | Cumulative Energy Exported   | The cumulative energy exported measurement                                                                                                                                                                                                                           | Energy           | true     | %.1f %unit% |
| electricalenergymeasurement-periodicenergyimported-energy   | Number:Energy            | Periodic Energy Imported     | The periodic energy imported measurement                                                                                                                                                                                                                             | Energy           | true     | %.1f %unit% |
| electricalenergymeasurement-periodicenergyexported-energy   | Number:Energy            | Periodic Energy Exported     | The periodic energy exported measurement                                                                                                                                                                                                                             | Energy           | true     | %.1f %unit% |
| threadnetworkdiagnostics-channel                            | Number                   | Channel                      | The Thread network channel                                                                                                                                                                                                                                           | Network          | true     | %d          |
| threadnetworkdiagnostics-routingrole                        | Number                   | Routing Role                 | The Thread routing role (0=Unspecified, 1=Unassigned, 2=Sleepy End Device, 3=End Device, 4=Reed, 5=Router, 6=Leader)                                                                                                                                                 | Network          | true     | %d          |
| threadnetworkdiagnostics-networkname                        | String                   | Network Name                 | The Thread network name                                                                                                                                                                                                                                              | Network          | true     |             |
| threadnetworkdiagnostics-panid                              | Number                   | PAN ID                       | The Thread network PAN ID                                                                                                                                                                                                                                            | Network          | true     | %d          |
| threadnetworkdiagnostics-extendedpanid                      | Number                   | Extended PAN ID              | The Thread network extended PAN ID                                                                                                                                                                                                                                   | Network          | true     | %d          |
| threadnetworkdiagnostics-rloc16                             | Number                   | RLOC16                       | The Thread network RLOC16 address                                                                                                                                                                                                                                    | Network          | true     | %d          |
| threadborderroutermanagement-borderroutername               | String                   | Border Router Name           | The name of the Thread border router                                                                                                                                                                                                                                 | Network          | true     |             |
| threadborderroutermanagement-borderagentid                  | String                   | Border Agent ID              | The unique identifier of the Thread border agent                                                                                                                                                                                                                     | Network          | true     |             |
| threadborderroutermanagement-threadversion                  | Number                   | Thread Version               | The version of Thread protocol being used                                                                                                                                                                                                                            | Network          | true     | %d          |
| threadborderroutermanagement-interfaceenabled               | Switch                   | Interface Enabled            | Whether the Thread border router interface is enabled                                                                                                                                                                                                                | Network          |          |             |
| threadborderroutermanagement-activedatasettimestamp         | Number                   | Active Dataset Timestamp     | Timestamp of the active Thread network dataset                                                                                                                                                                                                                       | Network          | true     | %d          |
| threadborderroutermanagement-activedataset                  | String                   | Active Dataset               | The active Thread network dataset configuration                                                                                                                                                                                                                      | Network          |          |             |
| threadborderroutermanagement-pendingdatasettimestamp        | Number                   | Pending Dataset Timestamp    | Timestamp of the pending Thread network dataset (only available if PAN change feature is supported)                                                                                                                                                                  | Network          | true     | %d          |
| threadborderroutermanagement-pendingdataset                 | String                   | Pending Dataset              | The pending Thread network dataset configuration (only available if PAN change feature is supported)                                                                                                                                                                 | Network          |          |             |

## Full Example

### Thing Configuration

```java
Thing configuration example for the Matter controller:
Thing matter:controller:main [ nodeId="1" ]

Thing configuration example for a Matter node:
Thing matter:node:main:12345678901234567890 [ nodeId="12345678901234567890"]

Thing configuration example for a Matter bridge endpoint:
Thing matter:endpoint:main:12345678901234567890:2 [ endpointId=2]
```

### Item Configuration

```java
Dimmer MyDimmer "My Endpoint Dimmer" { channel="matter:node:main:12345678901234567890:1#levelcontrol-level" }
Dimmer MyBridgedDimmer "My Bridged Dimmer" { channel="matter:endpoint:main:12345678901234567890:2#levelcontrol-level" }

```

### Sitemap Configuration

```perl
Optional Sitemap configuration:
sitemap home label="Home"
{
    Frame label="Matter Devices"
    {
        Dimmer item=MyEndpointDimmer
    }
}
```

# Matter Bridge

openHAB can also expose Items and Item groups as Matter devices to 3rd party Matter clients like Google Home, Apple Home and Amazon Alexa. This allows local control for those ecosystems and can be used instead of cloud based integrations for features like voice assistants.

## Configuration

The openHAB matter bridge uses Metadata tags with the key "matter", similar to the Alexa, Google Assistant and Apple Homekit integrations.
Matter Metadata tag values generally follow the Matter "Device Type" and "Cluster" specification as much as possible.
Items and item groups are initially tagged with a Matter "Device Type", which are Matter designations for common device types like lights, thermostats, locks, window coverings, etc...
For single items, like a light switch or dimmer, simply tagging the item with the Matter device type is enough.
For more complicated devices, like thermostats, A group item is tagged with the device type, and its child members are tagged with the cluster attribute(s) that it will be associated with.
Multiple attributes use a comma delimited format like `attribute1, attribute2, ... attributeN`.
For devices like fans that support groups with multiple items, but you are only using one item to control (like On/Off or Speed), you can tag the regular item with both the device type and the cluster attribute(s) separated by a comma.

Pairing codes and other options can be found in the MainUI under "Settings -> Add-on Settings -> Matter Binding"

### Device Types

| Type                | Item Type                             | Tag               | Option                                                                          |
|---------------------|---------------------------------------|-------------------|---------------------------------------------------------------------------------|
| OnOff Light         | Switch, Dimmer                        | OnOffLight        |                                                                                 |
| Dimmable Light      | Dimmer                                | DimmableLight     |                                                                                 |
| Color Light         | Color                                 | ColorLight        |                                                                                 |
| On/Off Plug In Unit | Switch, Dimmer                        | OnOffPlugInUnit   |                                                                                 |
| Thermostat          | Group                                 | Thermostat        |                                                                                 |
| Window Covering     | Rollershutter, Dimmer, String, Switch | WindowCovering    | String types: [OPEN="OPEN", CLOSED="CLOSED"], Switch types: [invert=true/false] |
| Temperature Sensor  | Number                                | TemperatureSensor |                                                                                 |
| Humidity Sensor     | Number                                | HumiditySensor    |                                                                                 |
| Occupancy Sensor    | Switch, Contact                       | OccupancySensor   |                                                                                 |
| Contact Sensor      | Switch, Contact                       | ContactSensor     |                                                                                 |
| Door Lock           | Switch                                | DoorLock          |                                                                                 |
| Fan                 | Group, Switch, String, Dimmer         | Fan               |                                                                                 |

### Global Options

* Endpoint Labels
  *  By default, the Item label is used as the Matter label but can be overridden by adding a `label` key as a metadata option, either by itself or part of other options required for a device.
  * Example: `[label="My Custom Label"]`
* Fixed Labels
  * Matter has a concept of "Fixed Labels" which allows devices to expose arbitrary label names and values which can be used by clients for tasks like grouping devices in rooms.
  * Example: `[fixedLabels="room=Office, floor=1"]` 

### Thermostat group member tags

| Type                | Item Type              | Tag                                | Options                                                                                  |
|---------------------|------------------------|------------------------------------|------------------------------------------------------------------------------------------|
| Current Temperature | Number                 | thermostat.localTemperature        |                                                                                          |
| Outdoor Temperature | Number                 | thermostat.outdoorTemperature      |                                                                                          |
| Heating Setpoint    | Number                 | thermostat.occupiedHeatingSetpoint |                                                                                          |
| Cooling Setpoint    | Number                 | thermostat.occupiedCoolingSetpoint |                                                                                          |
| System Mode         | Number, String, Switch | thermostat.systemMode              | [OFF=0,AUTO=1,ON=1,COOL=3,HEAT=4,EMERGENCY_HEAT=5,PRECOOLING=6,FAN_ONLY=7,DRY=8,SLEEP=9] |
| Running Mode        | Number, String         | thermostat.runningMode             |                                                                                          |

For `systemMode` the `ON` option should map to the system mode custom value that would be appropriate if a 'ON' command was issued, defaults to the `AUTO` mapping.

The following attributes can be set in the options of any thermostat member or on the Group item to set temperature options.

| Setting                              | Description                                                                                     | Value (in 0.01°C) |
|--------------------------------------|-------------------------------------------------------------------------------------------------|-------------------|
| `thermostat-minHeatSetpointLimit`    | The minimum allowable heat setpoint limit.                                                      | 0                 |
| `thermostat-maxHeatSetpointLimit`    | The maximum allowable heat setpoint limit.                                                      | 3500              |
| `thermostat-absMinHeatSetpointLimit` | The absolute minimum heat setpoint limit that cannot be exceeded by the `minHeatSetpointLimit`. | 0                 |
| `thermostat-absMaxHeatSetpointLimit` | The absolute maximum heat setpoint limit that cannot be exceeded by the `maxHeatSetpointLimit`. | 3500              |
| `thermostat-minCoolSetpointLimit`    | The minimum allowable cool setpoint limit.                                                      | 0                 |
| `thermostat-maxCoolSetpointLimit`    | The maximum allowable cool setpoint limit.                                                      | 3500              |
| `thermostat-absMinCoolSetpointLimit` | The absolute minimum cool setpoint limit that cannot be exceeded by the `minCoolSetpointLimit`. | 0                 |
| `thermostat-absMaxCoolSetpointLimit` | The absolute maximum cool setpoint limit that cannot be exceeded by the `maxCoolSetpointLimit`. | 3500              |
| `thermostat-minSetpointDeadBand`     | The minimum deadband (temperature gap) between heating and cooling setpoints.                   | 0                 |

### Fan group member tags

| Type           | Item Type              | Tag                       | Options                                                 |
|----------------|------------------------|---------------------------|---------------------------------------------------------|
| Fan Mode       | Number, String, Switch | fanControl.fanMode        | [OFF=0, LOW=1, MEDIUM=2, HIGH=3, ON=4, AUTO=5, SMART=6] |
| Fan Percentage | Dimmer                 | fanControl.percentSetting |                                                         |
| Fan OnOff      | Switch                 | onOff.onOff               |                                                         |

The following attributes can be set on the Fan Mode item or the Group item to set fan options.

| Setting                      | Description                                                                                              | Value |
|------------------------------|----------------------------------------------------------------------------------------------------------|-------|
| `fanControl-fanModeSequence` | The sequence of fan modes to cycle through.  See [Fan Mode Sequence Options](#fan-mode-sequence-options) | 5     |

#### Fan Mode Sequence Options

| Value | Description       |
|-------|-------------------|
| 0     | OffLowMedHigh     |
| 1     | OffLowHigh        |
| 2     | OffLowMedHighAuto |
| 3     | OffLowHighAuto    |
| 4     | OffHighAuto       |
| 5     | OffHigh           |

### Example

```java
Dimmer                TestDimmer               "Test Dimmer [%d%%]"                                                      {matter="DimmableLight" [label="My Custom Dimmer", fixedLabels="room=Bedroom 1, floor=2, direction=up, customLabel=Custom Value"]}

Group                 TestHVAC                 "Thermostat"                             ["HVAC"]                         {matter="Thermostat" [thermostat-minHeatSetpointLimit=0, thermostat-maxHeatSetpointLimit=3500]}
Number:Temperature    TestHVAC_Temperature     "Temperature [%d °F]"      (TestHVAC)    ["Measurement","Temperature"]    {matter="thermostat.localTemperature"}
Number:Temperature    TestHVAC_HeatSetpoint    "Heat Setpoint [%d °F]"    (TestHVAC)    ["Setpoint", "Temperature"]      {matter="thermostat.occupiedHeatingSetpoint"}
Number:Temperature    TestHVAC_CoolSetpoint    "Cool Setpoint [%d °F]"    (TestHVAC)    ["Setpoint", "Temperature"]      {matter="thermostat.occupiedCoolingSetpoint"}
Number                TestHVAC_Mode            "Mode [%s]"                (TestHVAC)    ["Control" ]                     {matter="thermostat.systemMode" [OFF=0, HEAT=1, COOL=2, AUTO=3]}

Switch                TestDoorLock             "Door Lock"                                                               {matter="DoorLock"}
Rollershutter         TestShade                "Window Shade"                                                            {matter="WindowCovering"}
Number:Temperature    TestTemperatureSensor    "Temperature Sensor"                                                      {matter="TemperatureSensor"}
Number                TestHumiditySensor       "Humidity Sensor"                                                         {matter="HumiditySensor"}
Switch                TestOccupancySensor      "Occupancy Sensor"                                                        {matter="OccupancySensor"}

### Fan with group item control
Group                 TestFan                  "Test Fan"                                                                {matter="Fan" [fanControl-fanModeSequence=3]}
Dimmer                TestFanSpeed             "Speed"                    (TestFan)                                      {matter="fanControl.percentSetting"}
Switch                TestFanOnOff             "On/Off"                   (TestFan)                                      {matter="fanControl.fanMode"}
Number                TestFanMode              "Mode"                     (TestFan)                                      {matter="fanControl.fanMode" [OFF=0, LOW=1, MEDIUM=2, HIGH=3, ON=4, AUTO=5, SMART=6]}

### Fan with single item control , so no group item is needed
Switch                TestFanSingleItem         "On/Off"                                                                 {matter="Fan, fanControl.fanMode"}
```

### Bridge FAQ

* Alexa: When pairing, after a minute Alexa reports "Something went wrong" 
  * Alexa can take 3-4 seconds per device to process which can take longer then the Alexa UI is willing to wait.
  Eventually the pairing will complete, which for a large number of devices may be a few minutes.
* Alexa: Suddenly stops working and says it could not connect to a device or device not responding.
  * Check the Settings page in the Main UI to confirm the bridge is running
  * Ensure the openHAB item has the proper matter tag, or that the item is being loaded at all (check item file errors)
  * Rarely, you may need to reboot the Alexa device.
  If you have multiple devices and not sure which is the primary matter connection, you may need to reboot all of them.

# Matter Ecosystem Overview

Matter is an open-source connectivity standard for smart home devices, allowing seamless communication between a wide range of devices, controllers, and ecosystems.

Below is a high-level overview of the Matter ecosystem as well as common terminology used in the Matter standard.

## Matter Devices

### Nodes and Endpoints

In the Matter ecosystem, a **node** represents a single device that joins a Matter network and will have a locally routable IPv6 address.
A **node** can have multiple **endpoints**, which are logical representations of specific features or functionalities of the device.
For example, a smart thermostat (node) may have an endpoint for general thermostat control (heating, cooling, current temperature, operating state, etc....) and another endpoint for humidity sensing.
Many devices will only have a single endpoint.
[Matter Bridges](#bridges) will expose multiple endpoints for each device they are bridging, and the bridge itself will be a node.

**Example:**

- A Thermostat node with an endpoint for general temperature control and another endpoint for a remote temperature or humidity sensor.

### Controllers

A **controller** manages the interaction between Matter devices and other parts of the network.
Controllers can send commands, receive updates, and facilitate device communication.
They also handle the commissioning process when new devices are added to the network.

**Example:**

- openHAB or another smart home hub or a smartphone app that manages your smart light bulbs, door locks, and sensors (Google Home, Apple Home, Amazon Alexa, etc...)

### Bridges

A **bridge** is a special type of node that connects non-Matter devices to a Matter network, effectively translating between protocols.
Bridges allow legacy devices to be controlled via the Matter standard.

openHAB fully supports connecting to Matter bridges. 
In addition, openHAB has support for running its own Matter bridge service, exposing openHAB items as Matter endpoints to 3rd party systems.
See [Matter Bridge](#Matter-Bridge) for information on running a Bridge server.

**Example:**

- A bridge that connects Zigbee or Z-Wave devices, making them accessible within a Matter ecosystem. The Ikea Dirigera and Philips Hue Bridge both act as matter bridges and are supported in openHAB.

### Thread Border Routers

A **Thread Border Router** is a device that allows devices connected via Thread (a low-power wireless protocol) to communicate with devices on other networks, such as Wi-Fi or Ethernet. 
It facilitates IPv6-based communication between Thread networks and the local IP network.

**Example:**

- An OpenThread Boarder Router (open source) as well as recent versions of Apple TVs, Amazon Echos and Google Nest Hubs all have embedded thread boarder routers.

## IPv6 and Network Connectivity

Matter devices operate over an IPv6 network, and obtaining an IPv6 address is required for communication.
Devices can connect to the network via different interfaces:

### Ethernet

Ethernet-connected Matter devices receive an IPv6 address through standard DHCPv6 or stateless address auto-configuration (SLAAC).

### Wi-Fi

Wi-Fi-enabled Matter devices also receive an IPv6 address using DHCPv6 or SLAAC.
They rely on the existing Wi-Fi infrastructure for communication within the Matter ecosystem.

### Thread

Thread-based Matter devices connect to the network via a **Thread Border Router**.
They receive an IPv6 address from the Thread router.

## IPv6 Requirements

For Matter devices to function correctly, **IPv6 must be enabled** and supported in both the local network (router) and the Matter controllers.
Without IPv6, devices won't be able to communicate properly within the Matter ecosystem.
Ensure that your router has IPv6 enabled and that any Matter controllers (like smart hubs, apps or openHAB) are configured to support IPv6 as well.

**Note that environments like Docker require special configurations to enable IPv6**

## Matter Commissioning and Pairing Codes

Commissioning a Matter device involves securely adding it to the network using a **pairing code**.
This process ensures that only authorized devices can join the network.

### Pairing Code from the Device

When commissioning a new Matter device, it typically has a printed QR code or numeric pairing code that you scan or enter during setup. This pairing code allows the controller to establish a secure connection to the device and add it to the network.
Once a device pairing code is in use, it typically can not be used again to pair other controllers.

### Additional Pairing Code from a Controller

If a device has already been commissioned and you want to add it to another Matter controller, the existing controller can generate an additional pairing code.
This is useful when sharing access to a device across multiple hubs or apps.
Apple Home, Google Home, Amazon Alexa and openHAB all support generating pairing codes for existing paired devices.

### Example:

- When setting up a smart lock, you may scan a QR code directly from the lock, or use the 11 digit pairing code printed on it to pair it with openHAB. If you later want to control the lock from another app or hub, you would retrieve a new pairing code directly from openHAB.
