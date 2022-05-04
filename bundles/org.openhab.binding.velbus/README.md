# Velbus Binding

The Velbus binding integrates with a [Velbus](https://www.velbus.eu/) system through a Velbus configuration module (VMBRSUSB, VMB1USB or VMB1RS) or a network connection (TCP/IP).

The binding has been tested with a USB configuration module for universal mounting (VMB1USB).
For optimal stability, the preferred configuration module is the VMBRSUSB module.

The binding exposes basic actions from the Velbus System that can be triggered from the smartphone/tablet interface, as defined by the [Velbus Protocol info sheets](https://github.com/velbus).

Supported item types are switches, dimmers and rollershutters.
Pushbutton, temperature sensors and input module states are retrieved and made available in the binding.

## Supported Things

A Velbus configuration module (e.g. VMBRSUSB) or a network server (e.g. [VelServer](https://github.com/StefCoene/velserver/wiki/TCP-server-for-Velbus)) is required as a "bridge" for accessing any other Velbus devices.

The supported Velbus devices are:

```
vmb1bl, vmb1bls, vmb1dm, vmb1led, vmb1ry, vmb1ryno, vmb1rynos, vmb1rys, vmb1ts, vmb2bl, vmb2ble, vmb2pbn, vmb4an, vmb4dc, vmb4ry, vmb4ryld, vmb4ryno, vmb6in, vmb6pbn, vmb7in, vmb8ir, vmb8pb, vmb8pbu, vmbdme, vmbdmi, vmbdmir, vmbel1, vmbel2, vmbel4, vmbelo, vmbelpir, vmbgp1, vmbgp2, vmbgp4, vmbgp4pir, vmbgpo, vmbgpod, vmbmeteo, vmbpirc, vmbpirm, vmbpiro, vmbvp1
```

The type of a specific device can be found in the configuration section for things in the UI. 
It is part of the unique thing id which could look like:

```
velbus:vmb4ryld:0424e5d2:01:CH1
```

The thing type is the second string behind the first colon and in this example it is **vmb4ryld**.

## Discovery

The Velbus bridge cannot be discovered automatically. 
It has to be added manually by defining the serial port of the Velbus Configuration module for the Velbus Serial Bridge or by defining the IP Address and port for the Velbus Network Bridge.

Once the bridge has been added as a thing, a manual scan can be launched to discover all other supported Velbus devices on the bus. 
These devices will be available in the inbox.
The discovery scan will also retrieve the channel names of the Velbus devices.

## Thing Configuration

The Velbus bridge needs to be added first.

For the Velbus Serial Bridge it is necessary to specify the serial port device used for communication.
On Linux systems, this will usually be either `/dev/ttyS0`, `/dev/ttyUSB0` or `/dev/ttyACM0` (or a higher  number than `0` if multiple devices are present).
On Windows it will be `COM1`, `COM2`, etc.

In the things file, this might look e.g. like

```
Bridge velbus:bridge:1 [ port="COM1" ]
```

For the Velbus Network Bridge it is necessary to specify the IP Address or hostname and the port of the Velbus network server.
This will usually be either the loopback address `127.0.0.1`, and port number. 
Or the specific IP of the machine `10.0.0.110` , and port number.

In the things file, this might look like

```
Bridge velbus:networkbridge:1 "Velbus Network Bridge - Loopback" @ "Control" [ address="127.0.0.1", port=6000 ]
```

Optionally, both the serial bridge and the network bridge can also update the realtime clock, date and daylight savings status of the Velbus modules. 
This is achieved by setting the Time Update Interval (in minutes) on the bridge, e.g.:

```
Bridge velbus:bridge:1 [ port="COM1", timeUpdateInterval="360" ]
```

The default time update interval is every 360 minutes. 
Setting the interval to 0 minutes or leaving it empty disables the update of the realtime clock, date and daylight savings status of the Velbus modules.

In case of a connection error, the bridges can also try to reconnect automatically. 
You can specify at which interval the bridge should try to reconnect by setting the Reconnection Interval (in seconds), e.g.:

```
Bridge velbus:bridge:1 [ port="COM1", reconnectionInterval="15" ]
```

The default reconnection interval is 15 seconds.


For the other Velbus devices, the thing configuration has the following syntax:

```
Thing velbus:<thing type>:<bridgeId>:<thingId> "Label" @ "Location" [CH1="Kitchen Light", CH2="Living Light"]
```

or nested in the bridge configuration:

```
<thing type> <thingId> "Label" @ "Location" [CH1="Kitchen Light", CH2="Living Light"]
```

The following thing types are valid for configuration:

```
vmb1bl, vmb1bls, vmb1dm, vmb1led, vmb1ry, vmb1ryno, vmb1rynos, vmb1rys, vmb1ts, vmb2bl, vmb2ble, vmb2pbn, vmb4an, vmb4dc, vmb4ry, vmb4ryld, vmb4ryno, vmb6in, vmb6pbn, vmb7in, vmb8ir, vmb8pb, vmb8pbu, vmbdme, vmbdmi, vmbdmir, vmbel1, vmbel2, vmbel4, vmbelo, vmbelpir, vmbgp1, vmbgp2, vmbgp4, vmbgp4pir, vmbgpo, vmbgpod, vmbmeteo, vmbpirc, vmbpirm, vmbpiro, vmbvp1
```

`thingId` is the hexadecimal Velbus address of the thing.

`"Label"` is an optional label for the thing.

`@ "Location"` is optional, and represents the location of the thing.

`[CHx="..."]` is optional, and represents the name of channel x, e.g. CH1 specifies the name of channel 1.

For thing types with builtin sensors (e.g. temperature), the interval at which the sensors should be checked can be set by specifying the Refresh Interval, e.g.:

```
Thing velbus:vmbelo:<bridgeId>:<thingId> [refresh="300"]
```

The default refresh interval for the sensors is 300 seconds. 
Setting the refresh interval to 0 or leaving it empty will prevent the thing from periodically refreshing the sensor values.

The following thing types support a sensor refresh interval:

```
vmb1ts, vmb4an, vmbel1, vmbel2, vmbel4, vmbelo, vmbelpir, vmbgp1, vmbgp2, vmbgp4, vmbgp4pir, vmbgpo, vmbgpod, vmbmeteo, vmbpirc, vmbpirm, vmbpiro
```

The `vmb7in` thing type also supports a refresh interval. For this thing type, the refresh interval is the interval at which the counter values should be refreshed.

For dimmers the speed (in seconds) at which the modules should dim from 0% to 100% can be set by specifying the Dimspeed, e.g.:

```
Thing velbus:vmb4dc:<bridgeId>:<thingId> [dimspeed="5"]
```

The following thing types support setting the dimspeed:

```
vmb1dm, vmb1led, vmb4dc, vmbdme, vmbdmi, vmbdmir
```

## Channels

For thing types `vmb1bl` and `vmb1bls` the supported channel is `CH1`. 
UpDown, StopMove and Percent command types are supported.

For thing types `vmb1dm`, `vmb1led`, `vmbdme`, `vmbdmi` and `vmbdmir` the supported channel is `CH1`.
OnOff and Percent command types are supported.
Sending an ON command will switch the dimmer to the value stored when last turning the dimmer off.

For thing type `vmb1ry` the supported channel is `CH1`.
OnOff command types are supported.

For thing type `vmb4ry` 4 channels are available `CH1` ... `CH4`.
OnOff command types are supported.

For thing types `vmb1ryno`, `vmb1rynos`, `vmb4ryld` and `vmb4ryno` 5 channels are available `CH1` ... `CH5`.
OnOff command types are supported.

For thing types `vmb1rys` 6 channels are available `CH1` ... `CH6`.
OnOff command types are supported on channels `CH1` ... `CH5`.
Pressed and Long_Pressed command types are supported on channel `CH6`.
1 trigger channel on `CH6t`.

The module `vmb1ts` has a number of channels to set the module's thermostat (`thermostat:currentTemperatureSetpoint`, `thermostat:heatingModeComfortTemperatureSetpoint`, `thermostat:heatingModeDayTemperatureSetpoint`, `thermostat:heatingModeNightTemperatureSetpoint`, `thermostat:heatingModeAntiFrostTemperatureSetpoint`, `thermostat:coolingModeComfortTemperatureSetpoint`, `thermostat:coolingModeDayTemperatureSetpoint`, `thermostat:coolingModeNightTemperatureSetpoint`, `thermostat:coolingModeSafeTemperatureSetpoint`, `operatingMode` and `thermostat:mode`) and thermostat trigger channels: `thermostat:heater`, `thermostat:boost`, `thermostat:pump`, `thermostat:cooler`, `thermostat:alarm1`, `thermostat:alarm2`, `thermostat:alarm3`, `thermostat:alarm4`.

For thing types `vmb2bl` and `vmb2ble` the supported channels are `CH1` and `CH2`. UpDown, StopMove and Percent command types are supported.

For thing type `vmb6in` 6 channels are available `CH1` ... `CH6`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH6`.
6 trigger channels on channels `input#CH1` ... `input#CH6`.

For thing type `vmb7in` 8 channels are available `CH1` ... `CH8`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH8`.
8 trigger channels on channels `input#CH1` ... `input#CH8`.

For thing types `vmb2pbn`, `vmb6pbn`, `vmb7in`, `vmb8ir`, `vmb8pb`, `vmb8pbu`, `vmbrfr8s` and `vmbvp1`  8 channels are available `CH1` ... `CH8`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH8`.
8 trigger channels on channels `input:CH1` ... `input:CH8`.

Thing types `vmb2pbn`, `vmb6pbn`, `vmb7in`, `vmb8pb`, `vmb8pbu`, `vmbrfr8s` and `vmbvp1` also have and 2 channels to steer the button LED feedback (`feedback:CH1` and `feedback:CH2`).
Additionally, the modules `vmb2pbn`, `vmb6pbn`, `vmb7in`, `vmb8pbu`, `vmbrfr8s` and `vmbvp1` have a number of channels to set the module's alarms: `clockAlarm:clockAlarm1Enabled`, `clockAlarm:clockAlarm1Type`, `clockAlarm:clockAlarm1WakeupHour`, `clockAlarm:clockAlarm1WakeupMinute`, `clockAlarm:clockAlarm1BedtimeHour`, `clockAlarm:clockAlarm1BedtimeMinute`, `clockAlarm:clockAlarm2Enabled`, `clockAlarm:clockAlarm2Type`, `clockAlarm:clockAlarm2WakeupHour`, `clockAlarm:clockAlarm2WakeupMinute`, `clockAlarm:clockAlarm2BedtimeHour` and `clockAlarm:clockAlarm2BedtimeMinute`.

For thing type`vmb4an` 8 trigger channels are avaiable `input:CH1` ... `input:CH8`. 
These channels will be triggered by the module's alarms.
Four pairs of channels are available to retrieve the module's analog inputs. 
Each pair has a channel to retrieve the raw analog value (`analogInput:CH9Raw` ... `analogInput:CH12Raw`) and a channel to retrieve the textual analog value (`analogInput:CH9` ... `analogInput:CH12`).
Four channels are available to set the module's analog outputs `analogOutput:CH13` ... `analogOutput:CH16`. 

For thing type `vmb4dc` 4 channels are available `CH1` ... `CH4`.
OnOff and Percent command types are supported.
Sending an ON command will switch the dimmer to the value stored when last turning the dimmer off.

For thing type `vmb4ry` 4 channels are available `CH1` ... `CH4`.
OnOff command types are supported.

Thing types `vmbel1`, `vmbel2`, `vmbel4`, `vmbelpir`, `vmbgp1`, `vmbgp2`, `vmbgp4`, `vmbgp4pir` and `vmbpiro` have 8 trigger channels `input:CH1` ... `input:CH8` and one temperature channel `input:CH9`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` and `button#CH2` for the thing type `vmbelpir`.  
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH8` for the thing types `vmbel1`, `vmbel2`, `vmbel4`, `vmbgp1`, `vmbgp2`, `vmbgp4`, `vmbgp4pir` and `vmbpiro`.
The thing types `vmbel1` and `vmbgp1` have one channel to steer the button LED feedback `feedback:CH1`.
The thing types `vmbel2` and `vmbgp2` have two channels to steer the button LED feedback `feedback:CH1` and `feedback:CH2`.
The thing types `vmbel4`, `vmbgp4` and `vmbgp4pir` have four channels to steer the button LED feedback `feedback:CH1` ... `feedback:CH4`.
The thing type `vmbpiro` has a channel `input:LIGHT` indicating the illuminance.
Thing types `vmbel1`, `vmbel2`, `vmbel4`, `vmbelpir`, `vmbgp1`, `vmbgp2`, `vmbgp4` and `vmbgp4pir` have a number of channels to set the module's alarms: `clockAlarm:clockAlarm1Enabled`, `clockAlarm:clockAlarm1Type`, `clockAlarm:clockAlarm1WakeupHour`, `clockAlarm:clockAlarm1WakeupMinute`, `clockAlarm:clockAlarm1BedtimeHour`, `clockAlarm:clockAlarm1BedtimeMinute`, `clockAlarm:clockAlarm2Enabled`, `clockAlarm:clockAlarm2Type`, `clockAlarm:clockAlarm2WakeupHour`, `clockAlarm:clockAlarm2WakeupMinute`, `clockAlarm:clockAlarm2BedtimeHour` and `clockAlarm:clockAlarm2BedtimeMinute`.
Thing types `vmbel1`, `vmbel2`, `vmbel4`, `vmbelpir`, `vmbgp1`, `vmbgp2`, `vmbgp4` and `vmbgp4pir` also have a number of channels to set the module's thermostat (`thermostat:currentTemperatureSetpoint`, `thermostat:heatingModeComfortTemperatureSetpoint`, `thermostat:heatingModeDayTemperatureSetpoint`, `thermostat:heatingModeNightTemperatureSetpoint`, `thermostat:heatingModeAntiFrostTemperatureSetpoint`, `thermostat:coolingModeComfortTemperatureSetpoint`, `thermostat:coolingModeDayTemperatureSetpoint`, `thermostat:coolingModeNightTemperatureSetpoint`, `thermostat:coolingModeSafeTemperatureSetpoint`, `operatingMode` and `thermostat:mode`) and thermostat trigger channels: `thermostat:heater`, `thermostat:boost`, `thermostat:pump`, `thermostat:cooler`, `thermostat:alarm1`, `thermostat:alarm2`, `thermostat:alarm3`, `thermostat:alarm4`.

Thing types `vmbelo`, `vmbgpo` and `vmbgpod` have 32 trigger channels `input:CH1` ... `input:CH32` and one temperature channel `input:CH33`.
Pressed and Long_Pressed command types are supported on channels `button#CH1` ... `button#CH32`.
They have have 32 channels to steer the button LED feedback `feedback:CH1` ... `feedback:CH32`.
They have a number of channels to set the module's alarms: `clockAlarm:clockAlarm1Enabled`, `clockAlarm:clockAlarm1Type`, `clockAlarm:clockAlarm1WakeupHour`, `clockAlarm:clockAlarm1WakeupMinute`, `clockAlarm:clockAlarm1BedtimeHour`, `clockAlarm:clockAlarm1BedtimeMinute`, `clockAlarm:clockAlarm2Enabled`, `clockAlarm:clockAlarm2Type`, `clockAlarm:clockAlarm2WakeupHour`, `clockAlarm:clockAlarm2WakeupMinute`, `clockAlarm:clockAlarm2BedtimeHour` and `clockAlarm:clockAlarm2BedtimeMinute`.
They have a number of channels to set the module's thermostat thermostat (`thermostat:currentTemperatureSetpoint`, `thermostat:heatingModeComfortTemperatureSetpoint`, `thermostat:heatingModeDayTemperatureSetpoint`, `thermostat:heatingModeNightTemperatureSetpoint`, `thermostat:heatingModeAntiFrostTemperatureSetpoint`, `thermostat:coolingModeComfortTemperatureSetpoint`, `thermostat:coolingModeDayTemperatureSetpoint`, `thermostat:coolingModeNightTemperatureSetpoint`, `thermostat:coolingModeSafeTemperatureSetpoint`, `operatingMode` and `thermostat:mode`) and thermostat trigger channels: `thermostat:heater`, `thermostat:boost`, `thermostat:pump`, `thermostat:cooler`, `thermostat:alarm1`, `thermostat:alarm2`, `thermostat:alarm3`, `thermostat:alarm4`.
They also have two channels to control the module's display `oledDisplay:MEMO` and `oledDisplay:SCREENSAVER`.

Thing type `vmbmeteo`has 8 trigger channels (`input:CH1` ... `input:CH8`). These channels will be triggered by the module's alarms.
It has a number of channels to set the module's alarms: `clockAlarm:clockAlarm1Enabled`, `clockAlarm:clockAlarm1Type`, `clockAlarm:clockAlarm1WakeupHour`, `clockAlarm:clockAlarm1WakeupMinute`, `clockAlarm:clockAlarm1BedtimeHour`, `clockAlarm:clockAlarm1BedtimeMinute`, `clockAlarm:clockAlarm2Enabled`, `clockAlarm:clockAlarm2Type`, `clockAlarm:clockAlarm2WakeupHour`, `clockAlarm:clockAlarm2WakeupMinute`, `clockAlarm:clockAlarm2BedtimeHour` and `clockAlarm:clockAlarm2BedtimeMinute`.
It also has a number of channels to read out the weather station's sensors: `weatherStation:temperature`, `weatherStation:rainfall`, `weatherStation:illuminance` and `weatherStation:windspeed`.

Thing types `vmbpirc` and `vmbpirm` have 7 trigger channels `input:CH1` ... `input:CH7`.
Additionally, these modules have a number of channels to set the module's alarms: `clockAlarm:clockAlarm1Enabled`, `clockAlarm:clockAlarm1Type`, `clockAlarm:clockAlarm1WakeupHour`, `clockAlarm:clockAlarm1WakeupMinute`, `clockAlarm:clockAlarm1BedtimeHour`, `clockAlarm:clockAlarm1BedtimeMinute`, `clockAlarm:clockAlarm2Enabled`, `clockAlarm:clockAlarm2Type`, `clockAlarm:clockAlarm2WakeupHour`, `clockAlarm:clockAlarm2WakeupMinute`, `clockAlarm:clockAlarm2BedtimeHour` and `clockAlarm:clockAlarm2BedtimeMinute`.

The trigger channels can be used as a trigger to rules. The event message can be `PRESSED`, `RELEASED`or `LONG_PRESSED`.

To remove the state of the Item in the Sitemap for a `button` channel.
Go to the Items list, select the Item, add a State Description Metadata, and set the Pattern value to a blank space.

## Full Example

.things:

```
Bridge velbus:bridge:1 [ port="COM1"] {
    vmb2ble     01
    vmb2pbn     02
    vmb6pbn     03
    vmb8pbu     04
    vmb7in      05
    vmb4ryld    06
    vmb4dc      07
    vmbgp1      08
    vmbgp2      09
    vmbgp4      0A
    vmbgp4pir   0B
    vmbgpo      0C
    vmbgpod     0D
    vmbpiro     0E
}
```

.items:

```
Switch LivingRoom           {channel="velbus:vmb4ryld:1:06:CH1"}                # Switch for onOff type action
Switch KitchenButton        {velbus:vmb2pbn:1:05:button#CH1}                    # Switch for Pressed and Long_Pressed type actions
Dimmer TVRoom               {channel="velbus:vmb4dc:1:07:CH2"}                  # Changing brightness dimmer type action
Rollershutter Kitchen       {channel="velbus:vmb2ble:1:01"}                     # Controlling rollershutter or blind type action

Number Temperature_LivingRoom   "Temperature [%.1f °C]"     <temperature> channel="velbus:vmbgp1:1:08:CH09"}  
Number Temperature_Corridor   "Temperature [%.1f °C]"     <temperature> channel="velbus:vmbgpo:1:0C:CH33"}  
Number Temperature_Outside   "Temperature [%.1f °C]"     <temperature> channel="velbus:vmbpiro:1:0E:CH09"}  
```

.sitemap:

```
Switch item=LivingRoom
Slider item=TVRoom
Switch item=TVRoom          # allows switching dimmer item off or on
Rollershutter item=Kitchen

Switch item=KitchenButton # Press and Long_Pressed message are available
# or
Switch item=KitchenButton mappings=[PRESSED="Push"] # only the Pressed message is send on the bus
# or
Switch item=KitchenButton mappings=[LONG_PRESSED="Push"] # only the Long_Pressed message is send on the bus
```

Example trigger rule:

```
rule "example trigger rule"
when
    Channel 'velbus:vmb7in:1:05:CH5' triggered PRESSED
then
    var message = receivedEvent.getEvent()
    logInfo("velbusTriggerExample", "Message: {}", message)
    ...
end
```
