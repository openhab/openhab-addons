# Keba Binding

This binding integrates the [Keba KeContact EV Charging Stations](http://www.keba.com).

## Supported Things

The Keba KeContact P20 and P30 stations are supported by this binding, the thing type id is `kecontact`.


## Thing Configuration

The Keba KeContact P20/30 requires the ip address as the configuration parameter `ipAddress`. Optionally, a refresh interval (in seconds) can be defined as parameter `refreshInterval` that defines the polling of values from the charging station.


## Channels

All devices support the following channels:

| Channel ID         | Item Type | Read-only | Description                                                            |
|--------------------|-----------|-----------|------------------------------------------------------------------------|
| state              | Number    | yes       | current operational state of the wallbox                               |
| enabled            | Switch    | no        | activation state of the wallbox                                        |
| maxpresetcurrent   | Number    | no        | maximum current the charging station should deliver to the EV          |
| power              | Number    | yes       | active power delivered by the charging station                         |
| wallbox            | Switch    | yes       | plug state of wallbox                                                  |
| vehicle            | Switch    | yes       | plug state of vehicle                                                  |
| locked             | Switch    | yes       | lock state of plug at vehicle                                          |
| I1/2/3             | Number    | yes       | current for the given phase                                            |
| U1/2/3             | Number    | yes       | voltage for the given phase                                            |
| output             | Switch    | no        | state of the X1 relais                                                 |
| input              | Switch    | yes       | state of the X2 contact                                                |
| display            | String    | yes       | display text on wallbox                                                |
| error1             | String    | yes       | error code state 1, if in error (see the KeContact FAQ)                |
| error2             | String    | yes       | error code state 2, if in error (see the KeContact FAQ)                |
| maxsystemcurrent   | Number    | yes       | maximum current the wallbox can deliver                                |
| failsafecurrent    | Number    | yes       | maximum current the wallbox can deliver, if network is lost            |
| uptime             | DateTime  | yes       | system uptime since the last reset of the wallbox                      |
| sessionconsumption | Number    | yes       | energy delivered in current session                                    |
| totalconsumption   | Number    | yes       | total energy delivered since the last reset of the wallbox             |
| authreq            | Switch    | yes       | authentication required                                                |
| authon             | Switch    | yes       | authentication enabled                                                 |
| sessionrfidtag     | String    | yes       | RFID tag used for the last charging session                            |
| sessionrfidclass   | String    | yes       | RFID tag class used for the last charging session                      |
| sessionid          | Number    | yes       | session ID of the last charging session                                |


## Example

demo.Things:

```
Thing keba:kecontact:1 [ipAddress="192.168.0.64", refreshInterval=30]
```

demo.items:

```
Dimmer KebaCurrentRange  {channel="keba:kecontact:1:maxpresetcurrentrange"} 
Number KebaCurrent  {channel="keba:kecontact:1:maxpresetcurrent"}
Number KebaSystemCurrent  {channel="keba:kecontact:1:maxsystemcurrent"} 
Number KebaFailSafeCurrent  {channel="keba:kecontact:1:failsafecurrent"} 
String KebaState  {channel="keba:kecontact:1:state"}
Switch KebaSwitch  {channel="keba:kecontact:1:enabled"}
Switch KebaWallboxPlugged  {channel="keba:kecontact:1:wallbox"}
Switch KebaVehiclePlugged  {channel="keba:kecontact:1:vehicle"}
Switch KebaPlugLocked  {channel="keba:kecontact:1:locked"}
DateTime KebaUptime "Uptime [%1$tY Y, %1$tm M, %1$td D,  %1$tT]"  {channel="keba:kecontact:1:uptime"}
Number KebaI1  {channel="keba:kecontact:1:I1"}
Number KebaI2  {channel="keba:kecontact:1:I2"}
Number KebaI3  {channel="keba:kecontact:1:I3"}
Number KebaU1  {channel="keba:kecontact:1:U1"}
Number KebaU2  {channel="keba:kecontact:1:U2"}
Number KebaU3  {channel="keba:kecontact:1:U3"}
Number KebaPower  {channel="keba:kecontact:1:power"}
Number KebaSessionEnergy  {channel="keba:kecontact:1:sessionconsumption"}
Number KebaTotalEnergy  {channel="keba:kecontact:1:totalconsumption"}
Switch KebaInputSwitch  {channel="keba:kecontact:1:input"}
Switch KebaOutputSwitch  {channel="keba:kecontact:1:output"}
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
