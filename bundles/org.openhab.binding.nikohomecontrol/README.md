# Niko Home Control Binding

**Upgrade notice for Niko Home Control II and openHAB 2.5.3 or later**:
Starting with openHAB 2.5.3 the binding uses Niko Home Control hobby API token based authentication.
The Niko Home Control hobby API is available with Niko Home Control system version 2.5.1 or newer.
If currently using a profile and password based authentication with the binding (upgrading from an openHAB version before 2.5.3), you will need to start using hobby API token based authentication.
Make sure your Niko Home Control system is at version 2.5.1 or newer.
Request a hobby API token at [mynikohomecontrol](https://mynikohomecontrol.niko.eu).
In the bridge configuration, put the received token in the API Token parameter.
Delete the values for Bridge Port and Profile parameters.

The **Niko Home Control binding**  integrates with a [Niko Home Control](https://www.niko.eu/) system through a Niko Home Control IP-interface or Niko Home Control Connected Controller.

The binding supports both Niko Home Control I and Niko Home Control II.

For Niko Home Control I, the binding has been tested with a Niko Home Control IP-interface (550-00508).
This IP-interface provides access on the LAN.
The binding does not require a Niko Home Control Gateway (550-00580), but does work with it in the LAN.
It has also been confirmed to work with the Niko Home Control Connected Controller (550-00003) in a Niko Home Control I installation.

For Niko Home Control II, the binding requires the Niko Home Control Connected Controller (550-00003) or Niko Home Control Wireless Smart Hub (552-00001).
The installation only needs to be 'connected' (registered on the Niko Home Control website) when first connecting to validate the authentication, and will work strictly in the LAN thereafter.

For Niko Home Control I, the binding exposes all actions from the Niko Home Control System that can be triggered from the smartphone/tablet interface, as defined in the Niko Home Control I programming software.
For Niko Home Control II, the binding exposes all devices in the system.

Supported device types are switches, dimmers and rollershutters or blinds, thermostats, energy meters (Niko Home Control I only) and access control (Niko Home Control II only).
Niko Home Control alarm and notice messages are retrieved and made available in the binding.

## Supported Things

The Niko Home Control Controller is represented as a bridge in the binding.
Connected to a bridge, the Niko Home Control Binding supports all off actions, on/off actions (e.g. for lights or groups of lights), dimmers, rollershutters or blinds, thermostats, energy meters, access control devices (Niko Home Control II only) and alarm systems (Niko Home Control II only).

The following thing types are available in the binding:

| Thing Type          | NHC I | NHC II | Description                                                                       |
|---------------------|:-----:|:------:|-----------------------------------------------------------------------------------|
| pushButton          |   x   |   x    | maps directly to stateless actions in the system, such as all off actions         |
| onOff               |   x   |   x    | on/off type of action with on/off state, such as lights and sockets               |
| dimmer              |   x   |   x    | dimmable light action                                                             |
| blind               |   x   |   x    | rollershutter, venetian blind                                                     |
| thermostat          |   x   |   x    | thermostat                                                                        |
| energyMeterLive     |   x   |   x    | energy meter with live power monitoring and aggregation                           |
| energyMeter         |   x   |        | energy meter, aggregates readings with 10 min intervals                           |
| gasMeter            |   x   |        | gas meter, aggregates readings with 10 min intervals                              |
| waterMeter          |   x   |        | water meter, aggregates readings with 10 min intervals                            |
| access              |       |   x    | door with bell button and lock                                                    |
| accessRingAndComeIn |       |   x    | door with bell button, lock and ring and come in functionality                    |
| alarm               |       |   x    | alarm system                                                                      |

## Binding Configuration

The bridge representing the Niko Home Control IP-interface needs to be added first.
A bridge can be auto-discovered or created manually.
An auto-discovered bridge will have an IP-address parameter automatically filled with the current IP-address of the IP-interface.
This IP-address for the discovered bridge will automatically update when the IP-address of the IP-interface changes.

The IP-address and port can be set when manually creating the bridge.

If the IP-address is set on a manually created bridge, no attempt will be made to discover the correct IP-address.
You are responsible to force a fixed IP address on the Niko Home Control IP-interface through settings in your DHCP server.

For Niko Home Control I, the port is set to 8000 by default and should match the port used by the Niko Home Control I IP-interface or Niko Home Control I Connected Controller.
For Niko Home Control II, the port is set to the default Hobby API 8884 port.

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

A discovery scan will first discover Niko Home Control IP-interfaces or Niko Home Control Connected Controllers in the network as bridges.
Default parameters will be used.
Note that this may fail to find the Niko Home Control IP-interface when traffic to port 10000 on the openHAB server is blocked.

When a Niko Home Control bridge is added as a bridge, the system information will be read from the Niko Home Control Controller and will update the bridge properties.

Subsequently, all defined actions that can be triggered from a smartphone/tablet in the Niko Home Control I system, respectively all actions in the Niko Home Control II system,  will be discovered and put in the inbox.
It is possible to trigger a manual scan for things on the Niko Home Control bridge.
Note that Niko Home Control II will require the API token to be set on the bridge before the scan for actions can succeed.
The bridge will remain offline as long as these parameters are not set.

If the Niko Home Control system has locations configured, these will be copied to thing locations and grouped as such.
Locations can subsequently be changed through the thing location parameter.

## Bridge Configuration

There are two **bridge** types, `bridge` for Niko Home Control I and `bridge2` for Niko Home Control II.
The Thing configuration for the bridge has the following parameters:

| Parameter | NHC I | NHC II | Required | Description                                                                                            |
|-----------|:-----:|:------:|:--------:|--------------------------------------------------------------------------------------------------------|
| addr      |   x   |   x    |     x    | IP address of the Niko Home Control IP-interface, Connected Controller or Wireless Smart Hub           |
| port      |   x   |   x    |          | port used to connect, 8000 by default for NHC I, 8884 by default for NHC II                            |
| profile   |       |   x    |          | profile UUID being used, hobby by default                                                              |
| password  |       |   x    |     x    | API token retrieved from the Niko Home Control website, must be set for the bridge to go online        |
| refresh   |   x   |   x    |          | interval to restart the communication in minutes (300 by default), if 0 or omitted the connection will not restart at regular intervals |

For Niko Home Control I, an example manual configuration looks like:

```java
Bridge nikohomecontrol:bridge:controller [ addr="192.168.0.10", refresh=0 ]
```

For Niko Home Control II, it looks like:

```java
Bridge nikohomecontrol:bridge2:controller [ addr="192.168.0.10", password="<token>", refresh=0 ]
```

Advanced configuration note for Niko Home Control II:
It is possible to use authentication based on a touch panel profile, bypassing the Hobby API token authentication.
To make this work, you have to define a password protected touch profile in the Niko Home Control programming software.
Extract the embedded SQLite database from the configuration file.
Look for the profile you created in the `Profile` table (using a SQLite database browser tool) and copy the `CreationId` into the `profile` parameter for the bridge.
The `port` parameter on the bridge has to be set to 8883.
The `password` parameter should be set to the profile password.

## Thing Configuration

The Thing configurations for **Niko Home Control actions, thermostats, energy meters and access devices** have the following parameters:

| Parameter     | NHC I | NHC II | Required | Thing Types                      | Description                                                                       |
|---------------|:-----:|:------:|:--------:|----------------------------------|-----------------------------------------------------------------------------------|
| actionId      |   x   |   x    |     x    | pushButton, onOff, dimmer, blind | unique ID for the action in the controller                                        |
| step          |   x   |   x    |          | dimmer                           | step value for dimmer increase/decrease actions, 10 by default                    |
| invert        |   x   |   x    |          | blind, energyMeterLive, energyMeter, gasMeter, waterMeter | inverts rollershutter or blind direction. Inverts sign of meter reading. Default false |
| thermostatId  |   x   |   x    |     x    | thermostat                       | unique ID for the thermostat in the controller                                    |
| overruleTime  |   x   |   x    |          | thermostat                       | standard overrule duration in minutes when setting a new setpoint without providing an overrule duration, default value is 60 |
| meterId       |   x   |   x    |     x    | energyMeterLive, energyMeter, gasMeter, waterMeter | unique ID for the energy meter in the controller                |
| refresh       |   x   |   x    |          | energyMeterLive, energyMeter, gasMeter, waterMeter | refresh interval for meter reading in minutes, default 10 minutes. The value should not be lower than 5 minutes to avoid too many meter data retrieval calls |
| accessId      |       |   x    |     x    | access, accessRingAndComeIn      | unique ID for the access device in the controller                                 |
| alarmId       |       |   x    |     x    | alarm                            | unique ID for the alarm system in the controller                                  |

For Niko Home Control I, the `actionId`, `thermostatId` or `meterId` parameter are the unique IP Interface Object ID (`ipInterfaceObjectId`) as automatically assigned in the Niko Home Control Controller when programming the Niko Home Control system using the Niko Home Control I programming software.
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
The same applies applies for `thermostatId`, `meterId`, `accessId` and `alarmId`.

An example **action** textual configuration looks like:

```java
Thing nikohomecontrol:dimmer:mybridge:mydimmer [ actionId="1", step=5 ]
```

For **thermostats** it would be:

```java
Thing nikohomecontrol:thermostat:mybridge:mythermostat [ thermostatId="abcdef01-abcd-1234-ab98-012345abcdef", overruleTime=10 ]
```

For **energy meters**:

```java
Thing nikohomecontrol:energymeter:mybridge:mymeter [ meterId="3", refresh=30 ]
```

For **access devices**:

```java
Thing nikohomecontrol:accessRingAndComeIn:mybridge:myaccess [ accessId="abcdef01-dcba-1234-ab98-012345abcdef" ]
```

For **alarm systems**:

```java
Thing nikohomecontrol:alarm:mybridge:myalarm [ alarmId="abcdef01-dcba-1234-ab98-012345abcdef" ]
```

## Channels

| Channel Type ID | RW | Advanced | Item Type          | Thing Types | Description                                                                                         |
|-----------------|:--:|:--------:|--------------------|-------------|-----------------------------------------------------------------------------------------------------|
| button          | RW |          | Switch             | pushButton  | stateless action control, `autoupdate="false"` by default. Only accepts `ON`                        |
| switch          | RW |          | Switch             | onOff       | on/off switches with state control, such as light switches                                          |
| brightness      | RW |          | Dimmer             | dimmer      | control dimmer light intensity. OnOff, IncreaseDecrease and Percent command types are supported. Note that sending an `ON` command will switch the dimmer to the value stored when last turning the dimmer off, or 100% depending on the configuration in the Niko Home Control Controller. This can be changed with the Niko Home Control programming software |
| rollershutter   | RW |          | Rollershutter      | blind       | control rollershutter or blind. UpDown, StopMove and Percent command types are supported            |
| measured        | R  |          | Number:Temperature | thermostat  | current temperature. Because of API restrictions, NHC II will only report in 0.5°C increments       |
| heatingmode     | RW |          | String             | thermostat  | current thermostat mode. Allowed values are Day, Night, Eco, Off, Cool, Prog1, Prog2, Prog3. Setting `heatingmode` will reset the `setpoint` channel to the standard value for the mode in the controller |
| mode            | RW |    X     | Number             | thermostat  | current thermostat mode, same meaning as `heatingmode`, but numeric values (0=Day, 1=Night, 2=Eco, 3=Off, 4=Cool, 5=Prog1, 6=Prog2, 7=Prog3). Setting `mode` will reset the `setpoint` channel to the standard value for the mode in the controller. This channel is kept for binding backward compatibility |
| setpoint        | RW |          | Number:Temperature | thermostat  | current thermostat setpoint. Updating the `setpoint` will overrule the temperature setpoint defined by `heatingmode` or `mode` for `overruletime` duration. Because of API restrictions, NHC II will only report in 0.5°C increments |
| overruletime    | RW |          | Number             | thermostat  | used to set the total duration in minutes to apply the setpoint temperature set in the `setpoint` channel before the thermostat returns to the setting from its mode |
| heatingdemand   | R  |          | String             | thermostat  | indicating if the system is actively heating/cooling. This channel will have value Heating, Cooling or None. For NHC I this is set by the binding from the temperature difference between `setpoint` and `measured`. It therefore may incorrectly indicate cooling even when the system does not have active cooling capabilities |
| demand          | R  |    X     | Number             | thermostat  | indicating if the system is actively heating/cooling, same as `heatingdemand` but numeric values (-1=Cooling, 0=None, 1=Heating) |
| power           | R  |          | Number:Power       | energyMeterLive | instant power consumption/production (negative for production), refreshed every 2s. Linking this channel starts an intensive communication flow with the controller and should only be done when appropriate |
| energy          | R  |          | Number:Energy      | energyMeterLive, energyMeter | total energy meter reading                                                         |
| energyday       | R  |          | Number:Energy      | energyMeterLive, energyMeter | day energy meter reading                                                           |
| gas             | R  |          | Number:Volume      | gasMeter    | total gas meter reading                                                                             |
| gasday          | R  |          | Number:Volume      | gasMeter    | day gas meter reading                                                                               |
| water           | R  |          | Number:Volume      | waterMeter  | total water meter reading                                                                           |
| waterday        | R  |          | Number:Volume      | waterMeter  | day water meter reading                                                                             |
| measurementtime | R  |          | DateTimeType       | energyMeterLive, energyMeter, gasMeter, waterMeter | last meter reading time                                      |
| bellbutton      | RW |          | Switch             | access, accessRingAndComeIn | bell button connected to access device, including buttons on video phone devices linked to an access device. The bell can also be triggered by an `ON` command, `autoupdate="false"` by default |
| ringandcomein   | RW |          | Switch             | accessRingAndComeIn | provide state and turn automatic door unlocking at bell ring on/off                         |
| lock            | RW |          | Switch             | access, accessRingAndComeIn | provide doorlock state and unlock the door by sending an `OFF` command. `autoupdate="false"` by default |
| arm             | RW |          | Switch             | alarm       | arm/disarm alarm, will change state (on/off) immediately. Note some integrations (Homekit, Google Home, ...) may require String states for an alarm system (ARMED/DISARMED). This can be achieved using an extra item and a rule updated by/commanding an item linked to this channel |
| armed           | RW |          | Switch             | alarm       | state of the alarm system (on/off), will only turn on after pre-armed period when arming            |
| state           | R  |          | String             | alarm       | state of the alarm system (DISARMED, PREARMED, ARMED, PREALARM, ALARM, DETECTOR PROBLEM)            |
| alarm           |    |          |                    | bridge, alarm | trigger channel with alarm event message, can be used in rules                                    |
| notice          |    |          |                    | bridge      | trigger channel with notice event message, can be used in rules                                     |


## Limitations

The binding has been tested with a Niko Home Control I IP-interface (550-00508) and the Niko Home Control Connected Controller (550-00003) for Niko Home Control I and II, and the Niko Home Control Wireless Smart Hub for Niko Home Control II.

Not all action and device types supported in Niko Home Control I and II controllers are supported by the binding.
Refer to the list of things and their support for Niko Home Control I and II respectively.

## Example

.things:

```java
Bridge nikohomecontrol:bridge:nhc1 [ addr="192.168.0.70", port=8000, refresh=300 ] {
    pushButton 1 "AllOff" [ actionId="1" ]
    onOff 2 "LivingRoom" @ "Downstairs" [ actionId="2" ]
    dimmer 3 "TVRoom" [ actionId="3", step=5 ]
    blind 4 [ actionId="4" ]
    thermostat 5 [ thermostatId="0", overruleTime=10 ]
    energyMeterLive 6 [ meterId="1" ]
    waterMeter [ meterId="3" ]
}

Bridge nikohomecontrol:bridge2:nhc2 [ addr="192.168.0.70", port=8884, password="A.B.C", refresh=300 ] {
    pushButton 1 "AllOff" [ actionId="12345678-abcd-1234-ef01-aa12bb34ee89" ]
    onOff 2 "Office" @ "Downstairs" [ actionId="12345678-abcd-1234-ef01-aa12bb34cc56" ]
    dimmer 3 "DiningRoom" [ actionId="abcdef01-abcd-1234-ab98-abcdef012345", step=5 ]
    blind 4 [ actionId="abcdef01-abcd-1234-ab98-abcdefabcdef" ]
    thermostat 5 [ thermostatId="abcdef01-abcd-1234-ab98-012345abcdef", overruleTime=10 ]
    accessRingAndComeIn 6 [ accessId="abcdef01-abcd-1234-ab98-012345abcdef" ]
    alarm 7 [ alarmId="abcdef01-abcd-1234-ab98-543210abcdef" ]
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
Number:Power CurPower   "[%.0f W]"  {channel="nikohomecontrol:energyMeterLive:nhc1:6:power"} # Get current power consumption
Number:Energy MyMeter          "[%.0f kWh]"   {channel="nikohomecontrol:energyMeter:nhc1:7:energy         # Get energy meter reading
Number:Energy MyMeterDay       "[%.0f kWh]"   {channel="nikohomecontrol:energyMeter:nhc1:7:energyday      # Get energy meter day reading
Switch AlarmControl     {channel="nikohomecontrol:onOff:nhc2:7:arm"}               # Switch to arm/disarm alarm
Switch AlarmSwitch      {channel="nikohomecontrol:onOff:nhc2:7:armed"}             # Switch to arm/disarm alarm, on state delayed until after pre-arm phase
String AlarmState       {channel="nikohomecontrol:onOff:nhc2:7:state"}             # State of the alarm system
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
Text item=CurPower
Text item=MyMeter
Text item=MyMeterDay
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
