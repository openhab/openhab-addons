<<<<<<< .mine
# <bindingName> NibeUplink Binding

The NibeUplink binding is used to get "live data" from from Nibe heat pumps without plugging any custom devices into your heat pump. This avoids the risk of losing your warranty. Instead data is retrieved from Nibe Uplink. This binding should in general be compatible with heat pump models that support Nibe Uplink.
Currently only read access is supported. Ability to modify heat pump configuration might be added in a future version.

## Supported Things

This binding provides only one thing type: The Nibe heat pump. Create one Nibe heat pump thing per physical heat pump installation available in your home(s).

## Discovery

Auto-Discovery is not supported, as credentials are necessary to login into NibeUplink.

## Thing Configuration

The syntax for a heat pump thing is:

```
nibeuplink:<MODEL>:<NAME>
```

- **nibeuplink** the binding id, fixed
- **model** the heatpump model
- **name** the name of the heatpump (choose any name)

Following models are currently supported:

| Model             | Description                                           |
|-------------------|-------------------------------------------------------|
| vvm320            | all channels                                          |
| vvm320-sensors    | only sensors                                          |
| vvm320-settings   | only settings                                         |
| vvm320-special    | a special subset of channels as shown in the Web UI   |
|                   |                                                       |
|                   |                                                       |
|                   |                                                       |
|                   |                                                       |

There are a few settings this thing:

- **user** (required)  
username used to login on NibeUplink

- **password** (required)  
password used to login on NibeUplink

- **nibeId** (required)  
Id of your heatpump in NibeUplink (can be found in the URL after successful login: https://www.nibeuplink.com/System/**<nibeId>>**/Status/Overview)

- **pollingInterval**  
interval (seconds) in which values are retrieved from NibeUplink. Setting less than 60 seconds does not make any sense as the heat pump only provides periodic updates to NibeUplink. (default = 60). 

### Examples

- minimum configuration

```
nibeuplink:vvm320:mynibe [ user="...", password="...", nibeId="..."]
```

- with pollingInterval

```
nibeuplink:vvm320:mynibe[ user="...", password="...", nibeId="...", pollingInterval=... ]
```

- multiple heat pumps

```
nibeuplink:vvm320:home1 [ user="...", password="...", nibeId="..."]
nibeuplink:vvm320:home2  [ user="...", password="...", nibeId="..."]
```

## Channels

Please find Channel Lists [HERE](channels)

[VVM320](channels/vvm320/channels.md)
[VVM320 Special](channels/vvm320/special.md)

## Full Example

### Thing

```
nibeuplink:vvm320:mynibe     [ user="nibe@my-domain.de", password="geheim123", nibeId="4711", pollingInterval=300 ]
```

### Items

```
Number      Nibe_40013_BT7        "Brauchwasser oben [%.2f °C]"            {channel="nibeuplink:vvm320:mynibe:40013"}
Number      Nibe_40014_BT6        "Brauchwasserbereitung [%.2f °C]"        {channel="nibeuplink:vvm320:mynibe:40014"}
```
=======
# <bindingName> Binding

_Give some details about what this binding is meant for - a protocol, system, specific device._

_If possible, provide some resources like pictures, a YouTube video, etc. to give an impression of what can be done with this binding. You can place such resources into a `doc` folder next to this README.md._

## Supported Things

_Please describe the different supported things / devices within this section._
_Which different types are supported, which models were tested etc.?_
_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Discovery

_Describe the available auto-discovery features here. Mention for what it works and what needs to be kept in mind when using it._

## Binding Configuration

_If your binding requires or supports general configuration settings, please create a folder ```cfg``` and place the configuration file ```<bindingId>.cfg``` inside it. In this section, you should link to this file and provide some information about the options. The file could e.g. look like:_

```
# Configuration for the Philips Hue Binding
#
# Default secret key for the pairing of the Philips Hue Bridge.
# It has to be between 10-40 (alphanumeric) characters 
# This may be changed by the user for security reasons.
secret=EclipseSmartHome
```

_Note that it is planned to generate some part of this based on the information that is available within ```ESH-INF/binding``` of your binding._

_If your binding does not offer any generic configurations, you can remove this section completely._

## Thing Configuration

_Describe what is needed to manually configure a thing, either through the (Paper) UI or via a thing-file. This should be mainly about its mandatory and optional configuration parameters. A short example entry for a thing file can help!_

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```ESH-INF/thing``` of your binding._

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
>>>>>>> .r378
