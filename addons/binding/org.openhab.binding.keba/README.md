# Keba Binding

This binding integrates the [Keba KeContact EV Charging Stations](http://www.keba.com).

## Supported Things

Currently the Keba KeContact P20 stations are supported by this binding.

## Binding Configuration

The binding uses the default UDP port number to connect to the Keba Charging Station.
So, no special configuration of the binding itself is required.

## Thing Configuration

The Keba KeContact P20 requires the ip address as a configuration value in order for the binding to know where to access it.
Optionally, a refresh interval (in seconds) can be defined that steers the polling of the actual state of the charging station.
In the thing file, this looks e.g. like

```
Thing keba:kecontactp20:1 [ ipAddress="192.168.0.64", refreshInterval=30 ]
```

the refreshInterval can optionally be used in combination with ```autoupdate="false"``` in the .items file so that the Items are updated with the latest actual value sent by the charging station.

## Channels

All devices support the following channels (non exhaustive):

| Channel Type ID  | Item Type | Description                                                                                       |   |   |
|------------------|-----------|---------------------------------------------------------------------------------------------------|---|---|
| state            | Number    | This channel indicates the current operational state of the wallbox                               |   |   |
| maxpresetcurrent | Number    | This channel supports adjusting the maximim current the charging station should deliver to the EV |   |   |
| power            | Number    | This channel indicates the active power delivered by the charging station                         |   |   |
| I1/2/3           | Number    | This channel indicates the current for the given phase                                            |   |   |
| U1/2/3           | Number    | This channel indicates the voltage for the given phase                                            |   |   |

## Example

demo.Things:

```
Thing keba:kecontactp20:1 [ipAddress="192.168.0.64", refreshInterval=30]
```

demo.items:

```
Dimmer KebaCurrentRange  {channel="keba:kecontactp20:1:maxpresetcurrentrange", autoupdate="false"}
Number KebaCurrent  {channel="keba:kecontactp20:1:maxpresetcurrent", autoupdate="false"}
Number KebaSystemCurrent  {channel="keba:kecontactp20:1:maxsystemcurrent"}
Number KebaFailSafeCurrent  {channel="keba:kecontactp20:1:failsafecurrent"}
String KebaState  {channel="keba:kecontactp20:1:state"}
Switch KebaSwitch  {channel="keba:kecontactp20:1:enabled", autoupdate="false"}
Switch KebaWallboxPlugged  {channel="keba:kecontactp20:1:wallbox"}
Switch KebaVehiclePlugged  {channel="keba:kecontactp20:1:vehicle"}
Switch KebaPlugLocked  {channel="keba:kecontactp20:1:locked"}
DateTime KebaUptime "Uptime [%1$tY Y, %1$tm M, %1$td D,  %1$tT]"  {channel="keba:kecontactp20:1:uptime"}
Number KebaI1  {channel="keba:kecontactp20:1:I1"}
Number KebaI2  {channel="keba:kecontactp20:1:I2"}
Number KebaI3  {channel="keba:kecontactp20:1:I3"}
Number KebaU1  {channel="keba:kecontactp20:1:U1"}
Number KebaU2  {channel="keba:kecontactp20:1:U2"}
Number KebaU3  {channel="keba:kecontactp20:1:U3"}
Number KebaPower  {channel="keba:kecontactp20:1:power"}
Number KebaSessionEnergy  {channel="keba:kecontactp20:1:sessionconsumptio"}
Number KebaTotalEnergy  {channel="keba:kecontactp20:1:totalconsumption"}
Switch KebaInputSwitch  {channel="keba:kecontactp20:1:input"}
Switch KebaOutputSwitch  {channel="keba:kecontactp20:1:output"}
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
			Text label="Charging Station" {
				Text item=KebaState label="Operating State [%s]"
				Text item=KebaUptime
				Switch item=KebaSwitch label="Enabled" mappings=[ON=ON, OFF=OFF ]
				Switch item=KebaWallboxPlugged label="Plugged into wallbox" mappings=[ON=ON, OFF=OFF ]
				Switch item=KebaVehiclePlugged label="Plugged into vehicle" mappings=[ON=ON, OFF=OFF ]
				Switch item=KebaPlugLocked label="Plug locked" mappings=[ON=ON, OFF=OFF ]
				Slider item=KebaCurrentRange switchSupport label="Maximum supply current [%.1f %%]"
				Text item=KebaCurrent label="Maximum supply current [%.0f mA]"
				Text item=KebaSystemCurrent label="Maximum system supply current [%.0f mA]"
				Text item=KebaFailSafeCurrent label="Failsafe supply current [%.0f mA]"
				Text item=KebaSessionEnergy label="Energy during current session [%.0f Wh]"
				Text item=KebaTotalEnergy label="Energy during all sessions [%.0f Wh]"
			}
}
```
