# TA C.M.I. Binding

This binding makes use of the CAN over Ethernet feature of the C.M.I. from Technische Alternative.
Since I only have the new UVR16x2, it has only been tested with this controller.

The binding supports two ways to interact with the C.M.I. and all devices connected to the C.M.I. via the CAN bus.
These modes are:

Via a "Schema API Page"

- Read values from output nodes
- Change values for controllable nodes

CoE (CAN over Ethernet) Connection

- Receive data from analog CAN outputs defined in TAPPS2
- Receive data from digital CAN outputs defined in TAPPS2
- Send ON/OFF to digital CAN inputs defined in TAPPS2
- Send numeric values to analog CAN inputs defined in TAPPS2

JSON API

- Read selected data via the JSON API (inputs, outputs, logging).
  See [API documentation](https://wiki.ta.co.at/C.M.I._JSON-API) for details

Depending on what you want to achieve, either the "Schema API Page" or the CoE way might be better.
As rough guidance: Anything you want to provide to the TA equipment it has to work / operate with the CoE might be better.
If you plan things mainly for user interaction the "Schema API Page" might be better.

## Supported Bridge and Things

- TA C.M.I. **Schema API Connection** - Thing \
  This Thing reflects one of our 'schema API page' as defined in the prerequisites.
  This Thing doesn't need the bridge.
  Multiple of these pages on different C.M.I.'s could be defined within an openHAB instance.
- TA C.M.I. **CoE Bridge** \
  In order to get the CAN over Ethernet (CoE) environment working a `coe-bridge` has to be created.
  The bridge itself opens the UDP port 5441 for communication with the C.M.I. devices.
  The bridge could be used for multiple C.M.I. devices.
- TA C.M.I. **CoE Connection** - Thing \
  This Thing reflects a connection to a node behind a specific C.M.I.
  This node could be every CAN-capable device from TA which allows to define a CAN input.
- TA C.M.I. **JSON API Connection** - Thing \
  This is a Thing connection that regularly polls the C.M.I. using the JSON API.

## Discovery

Auto-discovery is not supported.
You have to define the Things manually.

## Schema API

### Setting up the "Schema API Page"

The "Schema API page" is a special schema page created via TA's _TA-Designer_ application available as download on their website.
This page just needs to exist and be deployed on the C.M.I. but it doesn't need to be linked by the other schema pages you are using to control your TA installation.

All objects from this special 'API' page are automatically mapped as channels of this Thing, so the labels of the objects on this page have to follow a specific schema.

When adding objects to this page, the schema for the Object's _Pre-Text_ field has to follow the schema `<channelName> <channel description>:`.

Maybe this screenshot shows it best:

![screenshot-channel-object-details](doc/images/channel-object-details.png)

The text from the _Pre-Text_ will be used to define the channel.
The first word _tempCollector_ (highlighted in the screenshot) will be used as channel name, so it has to be unique.
Everything else till the final _:_ will be used as channel description.
Be sure to have at least 2 words in the _Pre-Text_ as we need both - the channel name and a description.
The binding will log an error otherwise.
Also keep in mind: for the channel name we have to adhere to the openHAB channel name conventions - so just use letters and numbers without any special signs here.
The type of the channel will be automatically determined by the type of the object added.
Also don't forget the final colon - this is the separator between the label and the value.
Without the colon the parser couldn't build up a working channel for this value.

The first sample is a sensor reading, but also the 'operation mode' of a heating circuit could be added:

![screenshot-sample-with-heating-circuit](doc/images/sample-with-heating-circuit.png)

In this screenshot you also see the schema page id - the parenthesized number on the bottom page overview, in this sample 4.

### Connection Configuration

The _TA C.M.I. Schema API Connection_ has to be manually configured.

The Thing has the following configuration parameters:

| Parameter Label         | Parameter ID | Description                                                                                                   | Accepted values        |
|-------------------------|--------------|---------------------------------------------------------------------------------------------------------------|------------------------|
| C.M.I. IP Address       | host         | Host name or IP address of the C.M.I                                                                          | host name or ip        |
| Username                | username     | Username for authentication on the C.M.I.                                                                     | string with username   |
| Password                | password     | Password for authentication on the C.M.I.                                                                     | string with password   |
| API Schema ID           | schemaId     | ID of the schema API page                                                                                     | 1-256                  |
| Poll Interval           | pollInterval | Poll interval (in seconds) how often to poll the API Page                                                     | 1-300; default 10      |

This Thing doesn't need a bridge. Multiple of these things for different C.M.I.'s could be defined within an openHAB instance.

### TA C.M.I. schema API Channels

The channels provided by this Thing depend on the configuration of the "schema API page".
All the channels are dynamically created to match it.
Also when the API Page is updated, the channels are also updated during the next refresh.

The channels have a parameter allowing to configure their update behavior:

| Parameter Label | Parameter ID | Description                                                                                                                        | Accepted values                          |
|-----------------|--------------|------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------|
| Update Policy   | updatePolicy | Update policy for this channel. Default means "On-Fetch" for read-only channels and "On-Change" for channels that can be modified. | 0 (Default), 1 (On-Fetch), 2 (On-Change) |

The behavior in detail:

- `Default` (`0`): When the channel is 'read-only' the update policy defaults to _On-Fetch_. When the channel is linked to something that can be modified it defaults to _On-Change_.
- `On-Fetch` (`1`): This is the default for read-only values. This means the channel is updated every time the schema page is polled. Ideal for values you want to monitor and log into charts.
- `On-Change` (`2`): When channel values can be changed via openHAB it is better to only update the channel when the value changes. The binding will cache the previous value and only send an update when it changes from the previous known value. This is especially useful if you intend to link other things (like e.g. Zigbee or Shelly switches) to the TA via openHAB that can be controlled by different sources. This prevents unintended toggles or even toggle loops.

### Some additional hints and comments

You might already have noticed that some state information is in German.
As I have set the `Accept-Language` HTTP header to `en` for all requests and found no other way to set a language for the schema pages I assume it is a lack of internationalization in the C.M.I.
You could circumvent this by creating map files to map things properly to your language.

If you want to see the possible options of a multi-state field you could open the _schema API page_ with your web browser and click on the object.
A popup with an option field will be shown showing all possible options, like in this screenshot:

![screenshot-operation-mode-values](doc/images/operation-mode-values.png)

Please be also aware that there are fields having more 'state' values than options, e.g. a manual output override: It has 'Auto/On', 'Auto/Off', 'Manual/On', 'Manual/Off' as state, but only 'Auto', 'Manual/On' and 'Manual/Off' as updateable options.
You only set it to 'Auto' and the extension On/Off is added depending on the system's current state.

## CoE

### Configure CAN outputs in TAPPS2

You need to configure CAN outputs in your Functional data on the UVR16x2.
This can be done by using the TAPPS2 application from TA. Follow the user guide on how to do this.

### Configure your CMI for CoE

Now follow the user guide of the C.M.I. on how to setup CAN over Ethernet (CoE).
Here you will map your outputs that you configured in the previous step.
This can be accomplished via the GUI on the C.M.I. or via the coe.csv file.
As the target device you need to put the IP of your openHAB server.
Don't forget to reboot the C.M.I. after you uploaded the coe.csv file.

### TA C.M.I. CoE Connection

The _TA C.M.I. CoE Connection_ has to be manually configured.

This Thing reflects a connection to a node behind a specific C.M.I. This node could be every CAN-capable device from TA which allows to define a CAN input.

| Parameter Label         | Parameter ID    | Description                                                                                                   | Accepted values        |
|-------------------------|-----------------|---------------------------------------------------------------------------------------------------------------|------------------------|
| C.M.I. IP Address       | host            | Host name or IP address of the C.M.I                                                                          | host name or ip        |
| Node                    | node            | The CoE / CAN Node number openHAB should represent                                                            | 1-64                   |

The Thing has no channels by default - they have to be added manually matching the configured inputs / outputs for the related CAN Node. Digital and Analog channels are supported. Please read TA's documentation related to the CAN-protocol - multiple analog (4) and digital (16) channels are combined so please be aware of this design limitation.

### CoE Connection Channels

Some comments on the CoE Connection and channel configuration:
As you might already have taken notice when studying the TA's manual, there are always multiple CoE values updated within a single CoE message.
This is a design decision made by TA.
But this also means for CoE messages from openHAB to TA C.M.I. we have to send multiple values at once.
But due to openHAB's design there is no default restore of previous values out of the box.
So after openHAB startup the _output Thing channels_ are either initialized with its configured default value or flagged as 'unknown' until the first update on the channel happens.
You could either use some 'illegal' value as initial value and use _CoE Value Validation_ on the TA side to detect invalid values.
Another option would be to use only every 4th analog and 16th digital channel if you only need a few channels.
Additionally you could use [openHAB's persistence service](https://www.openhab.org/docs/configuration/persistence.html#restoring-item-states-on-restart) and its option to [restore the item states](https://www.openhab.org/docs/configuration/persistence.html#restoring-item-states-on-restart) during openHAB startup.
As this only restores the item states you have to write a rule issuing _postUpdates_ on the items with the item's current value so the channel for the binding is updated.

Supported channels for the CoE connection are:

| Channel         | Type        | Description                                                          |
|-----------------|-------------|----------------------------------------------------------------------|
| coe-digital-in  | Switch (RO) | Digital input channel for digital state data received from the node  |
| coe-digital-out | Switch      | Digital output channel for digital state data sent to the node       |
| coe-analog-in   | Number (RO) | Analog input channel for numeric values received from the node       |
| coe-analog-out  | Number      | Analog output channel for numeric values sent to the node            |

Each channel has its own set of configuration parameters.
Here is a list of possible parameters:

Channel's `coe-digital-in` and `coe-analog-in`:

| Parameter Label         | Parameter ID | Description                                                                                                   | Accepted values        |
|-------------------------|--------------|---------------------------------------------------------------------------------------------------------------|------------------------|
| Output                  | output       | C.M.I. Network Output                                                                                         | 1-64                   |

Channel `coe-digital-out`:

| Parameter Label         | Parameter ID | Description                                                                                                   | Accepted values         |
|-------------------------|--------------|---------------------------------------------------------------------------------------------------------------|-------------------------|
| Output                  | output       | C.M.I. Network Output                                                                                         | 1-64                    |
| Initial Value           | initialValue | Initial value to set after startup (optional, defaults to uninitialized)                                      | true (on) / false (off) |

Channel `coe-analog-out`:

| Parameter Label         | Parameter ID | Description                                                                                                   | Accepted values         |
|-------------------------|--------------|---------------------------------------------------------------------------------------------------------------|-------------------------|
| Output                  | output       | C.M.I. Network Output                                                                                         | 1-64                    |
| Measurement Type        | type         | Measurement type for this channel (see table below)                                                           | 0-21                    |
| Initial Value           | initialValue | Initial value to set after startup (optional, defaults to uninitialized)                                      | floating point numeric  |

The binding supports all 21 measure types that exist according to the TA documentation.
Unfortunately, the documentation is not consistent here, so most of the types are supported only by generic names.
The known measure types are:

| id     | type          | unit    | description                                  |
|--------|---------------|---------|----------------------------------------------|
| 1      | Temperature   | °C      | Temperature value, multiplied by 0.1         |
| 2      | Energy Flux   | W/m2    | e.g. for solar irradiation                   |
| 3      | Flow Rate     | l/h     | e.g. for flow meters such as TA FTS-xx       |
| 4      | Time          | s       |                                              |
| 5      | Time          | min     |                                              |
| 6      | Flow Rate     | l/min   | standardized l/min flow rate                 |
| 7      | Temperature   | K       | Kelvin, e.g. for Temperature differences     |
| 8      | Percentage    | %       |                                              |
| 9      | unknown       | :       | might be another dimensionless unit          |
| 10     | Power         | kW      |                                              |
| 11     | Energy        | kWh     |                                              |
| 12     | Energy        | MWh     |                                              |
| 13     | el. Voltage   | V       |                                              |
| 14     | el. Current   | mA      |                                              |
| 15     | Time          | hours   |                                              |
| 16     | dimensionless | [none]  | use for multiplexers, etc                    |
| 17..   | repeating     | again   | from 1, e.g 17==1, 18==2, ...                |

## Full Example (CoE/Schema API)

There is no common configuration as everything depends on the configuration of the TA devices.
So we can just provide some samples providing the basics so you can build the configuration matching your system.

Example of a _.thing_ file:

```java
Thing tacmi:cmiSchema:apiLab "CMIApiPage"@"lab" [ host="192.168.178.33", username="user", password="secret", schemaId=4 ]
Bridge tacmi:coe-bridge:coe-bridge "TA C.M.I. Bridge"
{

    Thing cmi cmiTest "Test-CMI"@"lab" [ host="192.168.178.33", node=54 ] {
    Channels:
        Type coe-digital-in : digitalInput1 "Digital input 1" [ output=1 ]
        Type coe-digital-out : digitalOutput1 "Digital output 1" [ output=1, initialValue=true]
        Type coe-analog-in : analogInput1 "Analog input 1" [ output=1 ]
        Type coe-analog-out : analogOutput1 "Analog output 1" [ output=1, type=1, initialValue=22 ]
    }

}
```

Sample _.items_-File:

```java
# APIPage-items
Number TACMI_Api_tempCollector "Collector temp [%.1f °C]" <temperature> {channel="tacmi:cmiSchema:apiLab:tempCollector"}
String TACMI_Api_hc1OperationMode "Heating Circuit 1 Operation Mode [%s]" {channel="tacmi:cmiSchema:apiLab:hc1OperationMode"}

# COE-items
Number TACMI_Analog_In_1     "TA input value 1 [%.1f]"  <temperature> {channel="tacmi:cmi:coe-bridge:cmiTest:analogInput1"}
Number TACMI_Analog_Out_1    "TA output value 1 [%.1f]" <temperature> {channel="tacmi:cmi:coe-bridge:cmiTest:analogOutput1"}
Switch TACMI_Digital_In_1    "TA input switch 1 [%s]"    {channel="tacmi:cmi:coe-bridge:cmiTest:digitalInput1"}
Switch TACMI_Digital_Out_1   "TA output switch 1 [%s]"   {channel="tacmi:cmi:coe-bridge:cmiTest:digitalOutput1"}
```

Sample _.sitemap_ snippet

```perl
sitemap heatingTA label="heatingTA"
{
    Text item=TACMI_Api_tempCollector
    Switch item=TACMI_Api_hc1OperationMode mappings=["Zeit/Auto"="Auto", "Normal"="Operating", "Abgesenkt"="lowered", "Standby/Frostschutz"="Standby"]

    Text item=TACMI_Analog_In_1
    Setpoint item=TACMI_Analog_Out_1 step=5 minValue=15 maxValue=45
    Switch item=TACMI_Digital_In_1
    Switch item=TACMI_Digital_Out_1
}
```

## JSON API

Before setting up the JSON API, it is worth reading the [API documentation](https://wiki.ta.co.at/C.M.I._JSON-API).
Once configured, the exposed items should show up as channels.

### Configuring the JSON API

The _TA C.M.I. JSON API Connection_ has to be manually configured.

| Parameter Label   | Parameter ID | Description                                                                                                                                                                                          | Accepted values               |
|-------------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------|
| C.M.I. IP Address | host         | Host name or IP address of the C.M.I.                                                                                                                                                                | Any String                    |
| User name         | username     | User name for authentication on the C.M.I.                                                                                                                                                           | Any String                    |
| Password          | password     | Password for authentication on the C.M.I.                                                                                                                                                            | Any String                    |
| Node Id           | nodeId       | The node ID of the device you want to monitor  (C.M.I. &rarr; CAN-Bus)                                                                                                                               | An Integer that is not 0      |
| API-Parameters    | params       | Parameters to query (I, O, Sg, La, Ld). See [API documentation](https://wiki.ta.co.at/C.M.I._JSON-API) for details: Inputs (I), Outputs (O), General (Sg), Logging Analog (La), Logging Digital (Ld) | Any String                    |
| Poll Interval     | pollInterval | Poll interval in seconds. The documentation suggests 60s, but less is possible.                                                                                                                      | An integer between 10 and 900 |
