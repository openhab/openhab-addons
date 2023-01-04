# Niko Home Control Binding

**Upgrade notice for Niko Home Control II and openHAB 2.5.3**:
Starting with openHAB 2.5.3 the binding uses Niko Home Control hobby API token based authentication.
The Niko Home Control hobby API is available with Niko Home Control system version 2.5.1 or newer.
If currently using a profile and password based authentication with the binding (upgrading from an openHAB version before 2.5.3), you will need to start using hobby API token based authentication.
Make sure your Niko Home Control system is at version 2.5.1 or newer.
Request a hobby API token at [mynikohomecontrol](https://mynikohomecontrol.niko.eu).
In the bridge configuration, put the received token in the API Token parameter.
Delete the values for Bridge Port and Profile parameters.

The Niko Home Control binding integrates with a [Niko Home Control](https://www.niko.eu/) system through a Niko Home Control IP-interface or Niko Home Control Connected Controller.

The binding supports both Niko Home Control I and Niko Home Control II.

For Niko Home Control I, the binding has been tested with a Niko Home Control IP-interface (550-00508).
This IP-interface provides access on the LAN.
The binding does not require a Niko Home Control Gateway (550-00580), but does work with it in the LAN.
It has also been confirmed to work with the Niko Home Control Connected Controller (550-00003) in a Niko Home Control I installation.

For Niko Home Control II, the binding requires the Niko Home Control Connected Controller (550-00003) or Niko Home Control Wireless Smart Hub (552-00001).
The installation only needs to be 'connected' (registered on the Niko Home Control website) when first connecting to validate the authentication, and will work strictly in the LAN thereafter.

For Niko Home Control I, the binding exposes all actions from the Niko Home Control System that can be triggered from the smartphone/tablet interface, as defined in the Niko Home Control I programming software.
For Niko Home Control II, the binding exposes all devices in the system.

Supported device types are switches, dimmers and rollershutters or blinds, thermostats and energy meters (Niko Home Control II only).
Niko Home Control alarm and notice messages are retrieved and made available in the binding.

## Supported Things

The Niko Home Control Controller is represented as a bridge in the binding.
Connected to a bridge, the Niko Home Control Binding supports alloff actions, on/off actions (e.g. for lights or groups of lights), dimmers, rollershutters or blinds, thermostats and energy meters (only Niko Home Control II).

## Binding Configuration

The bridge representing the Niko Home Control IP-interface needs to be added first.
A bridge can be auto-discovered or created manually.
An auto-discovered bridge will have an IP-address parameter automatically filled with the current IP-address of the IP-interface.
This IP-address for the discovered bridge will automatically update when the IP-address of the IP-interface changes.

The IP-address and port can be set when manually creating the bridge.

If the IP-address is set on a manually created bridge, no attempt will be made to discover the correct IP-address.
You are responsible to force a fixed IP address on the Niko Home Control IP-interface through settings in your DHCP server.

For Niko Home Control I, the port is set to 8000 by default and should match the port used by the Niko Home Control I IP-interface or Niko Home Control I Connected Controller.
For Niko Home Control II, the port is set to 8884 by default and should match the secure MQTT port used by the Niko Home Control II Connected Controller.

For Niko Home Control I, no further bridge configuration is required when using auto-discovery.

The Niko Home Control II bridge has an extra required parameter for the API token.
The API token can be retrieved from your Niko Home Control profile page on the Niko Home Control website.
For that, you will need to add the Hobby API as a connected service on your profile.
Note that the API token is only valid for one year after creation.
The token expiry date is visible in the bridge properties.
Entries will also be written in the log when the token is about to expire (starting 14 days in advance).

An optional refresh interval will be used to restart the bridge at regular intervals (every 300 minutes by default).
Restarting the bridge at regular times improves the connection stability and avoids loss of connection.
It can be turned off completely by setting the parameter to 0.

## Discovery

A discovery scan will first discover the Niko Home Control IP-interface or Niko Home Control Connected Controller in the network as a bridge.
Default parameters will be used.
Note that this may fail to find the correct Niko Home Control IP-interface when there are multiple IP-interfaces in the network, or when traffic to port 10000 on the openHAB server is blocked.

When the Niko Home Control bridge is added as a thing, the system information will be read from the Niko Home Control Controller and will update the bridge properties.

Subsequently, all defined actions that can be triggered from a smartphone/tablet in the Niko Home Control I system, respectively all actions in the Niko Home Control II system,  will be discovered and put in the inbox.
It is possible to trigger a manual scan for things on the Niko Home Control bridge.
Note that Niko Home Control II will require the token to be set on the bridge before the scan for actions can succeed.
The bridge will remain offline as long as these parameters are not set.

If the Niko Home Control system has locations configured, these will be copied to thing locations and grouped as such.
Locations can subsequently be changed through the thing location parameter.

## Thing Configuration

If you wish to use textual config instead of automatic discovery, you can add thing definitions to a things file.

The Thing configuration for the **bridge** uses the following syntax:

For Niko Home Control I:

```java
Bridge nikohomecontrol:bridge:<bridgeId> [ addr="<IP-address of IP-interface>", port=<listening port>,
                                           refresh=<Refresh interval> ]
```

`bridgeId` can have any value.

`addr` is the fixed Niko Home Control IP-interface or Connected Controller address and is required.
`port` will be the port used to connect and is 8000 by default.
`refresh` is the interval to restart the communication in minutes (300 by default), if 0 or omitted the connection will not restart at regular intervals.

For Niko Home Control II:

```java
Bridge nikohomecontrol:bridge2:<bridgeId> [ addr="<IP-address of IP-interface>", port=<listening port>, profile="<profile>",
                                           password="<token>", refresh=<Refresh interval> ]
```

`bridgeId` can have any value.

`addr` is the fixed Niko Home Connected Controller address and is required.
`port` will be the port used to connect and is 8884 by default.
`profile` is the profile UUID being used, hobby by default.
`password` is the API token retrieved from the Niko Home Control website, cannot be empty.
`refresh` is the interval to restart the communication in minutes (300 by default), if 0 or omitted the connection will not restart at regular intervals.

Advanced configuration note for Niko Home Control II:
It is possible to use authentication based on a touch panel profile, bypassing the hobby API token authentication.
To make this work, you have to define a password protected touch profile in the Niko Home Control programming software.
Extract the embedded SQLite database from the configuration file.
Look for the profile you created in the `Profile` table (using a SQLite database browser tool) and copy the `CreationId` into the profile parameter for the bridge.
The port parameter on the bridge has to be set to 8883.
The API token parameter should be set to the profile password.

The Thing configuration for **Niko Home Control actions** has the following syntax:

```java
Thing nikohomecontrol:<thing type>:<bridgeId>:<thingId> "Label" @ "Location"
                        [ actionId="<Niko Home Control action ID>",
                          step=<dimmer increase/decrease step value> ]
```

or nested in the bridge configuration:

```java
<thing type> <thingId> "Label" @ "Location" [ actionId="<Niko Home Control action ID>",
                         step=<dimmer increase/decrease step value> ]
```

The following action thing types are valid for the configuration:

```text
pushButton, onOff, dimmer, blind
```

`pushButton` types are used to map directly to stateless actions in the Niko Home Control system, such as All Off actions.
Discovery will identify All Off actions and map them to `pushButton` things.

`thingId` can have any value, but will be set to the same value as the actionId parameter if discovery is used.

`"Label"` is an optional label for the thing.

`@ "Location"` is optional, and represents the location of the Thing. Auto-discovery would have assigned a value automatically.

For Niko Home Control I, the `actionId` parameter is the unique IP Interface Object ID (`ipInterfaceObjectId`) as automatically assigned in the Niko Home Control Controller when programming the Niko Home Control system using the Niko Home Control I programming software.
It is not directly visible in the Niko Home Control programming or user software, but will be detected and automatically set by openHAB discovery.
For textual configuration, you can manually retrieve it from the content of the .nhcp configuration file created by the programming software.
Open the file with an unzip tool to read its content.

For Niko Home Control II, the `actionId` parameter is a unique ID for the action in the controller.
It can only be auto-discovered.
If you want to define the action through textual configuration, the easiest way is to first do discovery on the bridge to get the correct `actionId` to use in the textual configuration.
Discover and add the thing you want to add.
Note down the `actionId` parameter from the thing, remove it before adding it again through textual configuration, with the same `actionId` parameter.
Alternatively the `actionId` can be retrieved from the configuration file.
The file contains a SQLLite database.
The database contains a table `Action` with column `FifthplayId` corresponding to the required `actionId` parameter.

The `step` parameter is only available for dimmers.
It sets a step value for dimmer increase/decrease actions.
The parameter is optional and set to 10 by default.

The Thing configuration for **Niko Home Control thermostats** has the following syntax:

```java
Thing nikohomecontrol:thermostat:<bridgeId>:<thingId> "Label" @ "Location"
                        [ thermostatId="<Niko Home Control thermostat ID>",
                          overruleTime=<default duration for overrule temperature in minutes> ]
```

or nested in the bridge configuration:

```java
thermostat <thingId> "Label" @ "Location" [ thermostatId="<Niko Home Control thermostat ID>" ]
```

`thingId` can have any value, but will be set to the same value as the thermostatId parameter if discovery is used.

`"Label"` is an optional label for the Thing.

`@ "Location"` is optional, and represents the location of the thing.
Auto-discovery would have assigned a value automatically.

The `thermostatId` parameter is the unique IP Interface Object ID as automatically assigned in the Niko Home Control I Controller when programming the Niko Home Control system using the Niko Home Control programming software.
It is not directly visible in the Niko Home Control programming or user software, but will be detected and automatically set by openHAB discovery.
For textual configuration, it can be retrieved from the .nhcp configuration file.

For Niko Home Control II, the `thermostatId` parameter is a unique ID for the thermostat in the controller.
It can only be auto-discovered.
If you want to define the thermostat through textual configuration, you may first need to do discovery on the bridge to get the correct `thermostatId` to use in the textual configuration.

The `overruleTime` parameter is used to set the standard overrule duration in minutes when you set a new setpoint without providing an overrule duration.
The default value is 60 minutes.

The Thing configuration for **Niko Home Control energy meters** has the following syntax:

```java
Thing nikohomecontrol:energymeter:<bridgeId>:<thingId> "Label" @ "Location"
                        [ energyMeterId="<Niko Home Control energy meter ID>" ]
```

or nested in the bridge configuration:

```java
energymeter <thingId> "Label" @ "Location" [ energyMeterId="<Niko Home Control energy meter ID>" ]
```

`thingId` can have any value, but will be set to the same value as the thermostatId parameter if discovery is used.

`"Label"` is an optional label for the Thing.

`@ "Location"` is optional, and represents the location of the thing. Auto-discovery would have assigned a value automatically.

Energy meters can only be configured for Niko Home Control II.
The `energyMeterId` parameter is a unique ID for the energy meter in the controller.
It can only be auto-discovered.
If you want to define the energy meter through textual configuration, you may first need to do discovery on the bridge to get the correct `energyMeterId` to use in the textual configuration.

## Channels

For thing type `pushButton` the supported channel is `button`.
OnOff command types are supported.
For this channel, `autoupdate="false"` is set by default.
This will stop the linked item state from updating.

For thing type `onOff` the supported channel is `switch`.
OnOff command types are supported.

For thing type `dimmer` the supported channel is `brightness`.
OnOff, IncreaseDecrease and Percent command types are supported.
Note that sending an ON command will switch the dimmer to the value stored when last turning the dimmer off, or 100% depending on the configuration in the Niko Home Control Controller.
This can be changed with the Niko Home Control programming software.

For thing type `blind` the supported channel is `rollershutter`.
UpDown, StopMove and Percent command types are supported.

For thing type `thermostat` the supported channels are `measured`, `heatingmode`, `mode`, `setpoint`, `overruletime`, `heatingdemand` and `demand`.
`measured` gives the current temperature in QuantityType<Temperature>, allowing for different temperature units.
This channel is read only.
`heatingmode` can be set and shows the current thermostat mode.
Allowed values are Day, Night, Eco, Off, Cool, Prog1, Prog2, Prog3.
As an alternative to `heatingmode` and for backward compatibility, the advanced channel `mode` is provided.
This channel has the same meaning, but with numeric values (0=Day, 1=Night, 2=Eco, 3=Off, 4=Cool, 5=Prog1, 6=Prog2, 7=Prog3) instead of string values.
If `heatingmode` or `mode` is set, the `setpoint` temperature will return to the standard value for the mode as defined in Niko Home Control.
`setpoint` shows the current thermostat setpoint value in QuantityType<Temperature>.
When updating `setpoint`, it will overrule the temperature setpoint defined by the thermostat mode for `overruletime` duration.
`overruletime` is used to set the total duration to apply the setpoint temperature set in the setpoint channel before the thermostat returns to the setting from its mode.
`heatingdemand` is a string indicating if the system is actively heating/cooling.
This channel will have value Heating, Cooling or None.
As an alternative to `heatingdemand`, the advanced channel `demand` is provided.
The value will be 1 for heating, -1 for cooling and 0 if not heating or cooling.
`heatingdemand` and `demand` are read only channels.
Note that cooling in NHC I is set by the binding, and will incorrectly show cooling demand when the system does not have cooling capabilities.
In NHC II, `measured` and `setpoint` temperatures will always report in 0.5°C increments due to a Niko Home Control II API restriction.

For thing type `energymeter` the only supported channel is `power`.
This channel is read only and give a current power consumption/production reading (positive for consumption) every 2 seconds.

The bridge has two trigger channels `alarm` and `notice`.
It can be used as a trigger to rules.
The event message is the alarm or notice text coming from Niko Home Control.

## Limitations

The binding has been tested with a Niko Home Control I IP-interface (550-00508) and the Niko Home Control Connected Controller (550-00003) for Niko Home Control I and II, and the Niko Home Control Wireless Smart Hub for Niko Home Control II.

The action events implemented are limited to onOff, dimmer, allOff, scenes, PIR and rollershutter or blinds.
Other actions have not been implemented.
It is not possible to tilt the slates of venetian blinds.

Beyond action, thermostat and electricity usage events, the Niko Home Control communication also supports gas and water usage data.
Niko Home Control II also supports 3rd party devices.
All of this has not been implemented.
Electricity power consumption/production has only been implemented for Niko Home Control II.

## Example

.things:

```java
Bridge nikohomecontrol:bridge:nhc1 [ addr="192.168.0.70", port=8000, refresh=300 ] {
    pushButton 1 "AllOff" [ actionId="1" ]
    onOff 2 "LivingRoom" @ "Downstairs" [ actionId="2" ]
    dimmer 3 "TVRoom" [ actionId="3", step=5 ]
    blind 4 [ actionId="4" ]
    thermostat 5 [ thermostatId="0", overruleTime=10 ]
}

Bridge nikohomecontrol:bridge2:nhc2 [ addr="192.168.0.70", port=8884, password="A.B.C", refresh=300 ] {
    pushButton 1 "AllOff" [ actionId="12345678-abcd-1234-ef01-aa12bb34ee89" ]
    onOff 2 "Office" @ "Downstairs" [ actionId="12345678-abcd-1234-ef01-aa12bb34cc56" ]
    dimmer 3 "DiningRoom" [ actionId="abcdef01-abcd-1234-ab98-abcdef012345", step=5 ]
    blind 4 [ actionId="abcdef01-abcd-1234-ab98-abcdefabcdef" ]
    thermostat 5 [ thermostatId="abcdef01-abcd-1234-ab98-012345abcdef", overruleTime=10 ]
    energymeter 6 [ energyMeterId="abcdef01-abcd-1234-cd56-ffee34567890" ]
}

Bridge nikohomecontrol:bridge:nhc3 [ addr="192.168.0.110" ] {
    onOff 11 @ "Upstairs" [ actionId="11" ]
    dimmer 12 [ actionId="12", step=5 ]
    blind 13 [ actionId="13" ]
    thermostat 14 [ thermostatId="10" ]
}
```

.items:

```java
Switch AllOff           {channel="nikohomecontrol:onOff:nhc1:1:button"}           # Pushbutton for All Off action
Switch LivingRoom       {channel="nikohomecontrol:onOff:nhc1:2:switch"}           # Switch for onOff type action
Dimmer TVRoom           {channel="nikohomecontrol:dimmer:nhc1:3:brightness"}      # Changing brightness dimmer type action
Rollershutter Kitchen   {channel="nikohomecontrol:blind:nhc1:4:rollershutter"}    # Controlling rollershutter or blind type action
Number:Temperature CurTemperature   "[%.1f °F]"  {channel="nikohomecontrol:thermostat:nhc1:5:measured"} # Getting measured temperature from thermostat in °F, read only
String ThermostatMode   {channel="nikohomecontrol:thermostat:nhc1:5:heatingmode"}        # Get and set thermostat mode
Number:Temperature SetTemperature   "[%.1f °C]"  {channel="nikohomecontrol:thermostat:nhc1:5:setpoint"} # Get and set target temperature in °C
Number OverruleDuration {channel="nikohomecontrol:thermostat:nhc1:5:overruletime"}       # Get and set the overrule time
String ThermostatDemand   {channel="nikohomecontrol:thermostat:nhc1:5:heatingdemand"}      # Get the current heating/cooling demand
Number:Power CurPower   "[%.0f W]"  {channel="nikohomecontrol:energyMeter:nhc2:6:power"} # Get current power consumption
```

.sitemap:

```perl
Switch item=AllOff
Switch item=LivingRoom
Slider item=TVRoom
Switch item=TVRoom          # allows switching dimmer item off or on (with controller defined behavior)
Rollershutter item=Kitchen
Text item=CurTemperature
Selection item=ThermostatMode mappings=[Day="day", Night="night", Eco="eco", Off="off", Prog1="Away"]
Setpoint item=SetTemperature minValue=0 maxValue=30
Slider item=OverruleDuration minValue=0 maxValue=120
Text item=Power
```

Example trigger rule:

```java
rule "example trigger rule"
when
    Channel 'nikohomecontrol:bridge:nhc1:alarm' triggered or
    Channel 'nikohomecontrol:bridge2:nhc2:notice' triggered
then
    var message = receivedEvent.getEvent()
    logInfo("nhcTriggerExample", "Message: {}", message)
    ...
end
```
