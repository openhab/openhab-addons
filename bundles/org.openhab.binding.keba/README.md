# Keba Binding

This binding integrates the [Keba KeContact EV Charging Stations](https://www.keba.com).

## Supported Things

The Keba KeContact P20 and P30 stations which are providing the UDP interface (P20 LSA+ socket, P30 c-series and x-series or BMW wallbox) are supported by this binding, the thing type id is `kecontact`.

## Thing Configuration

The Keba KeContact P20/30 requires the IP address as the configuration parameter `ipAddress`.
Optionally, a refresh interval (in seconds) can be defined as parameter `refreshInterval` that defines the polling of values from the charging station.

## Channels

All devices support the following channels:

| Channel ID              | Item Type                | Read-only | Description                                                             |
| ----------------------- | ------------------------ | --------- | ----------------------------------------------------------------------- |
| state                   | Number                   | yes       | current operational state of the wallbox                                |
| enabledsystem           | Switch                   | yes       | activation state of the wallbox (System)                                |
| enableduser             | Switch                   | no        | activation state of the wallbox (User)                                  |
| maxpresetcurrent        | Number:ElectricCurrent   | no        | maximum current the charging station should deliver to the EV in A      |
| maxpresetcurrentrange   | Number:Dimensionless     | no        | maximum current the charging station should deliver to the EV in %      |
| power                   | Number:Power             | yes       | active power delivered by the charging station                          |
| wallbox                 | Switch                   | yes       | plug state of wallbox                                                   |
| vehicle                 | Switch                   | yes       | plug state of vehicle                                                   |
| locked                  | Switch                   | yes       | lock state of plug at vehicle                                           |
| I1/2/3                  | Number:ElectricCurrent   | yes       | current for the given phase                                             |
| U1/2/3                  | Number:ElectricPotential | yes       | voltage for the given phase                                             |
| output                  | Switch                   | no        | state of the X1 relais                                                  |
| input                   | Switch                   | yes       | state of the X2 contact                                                 |
| display                 | String                   | no        | display text on wallbox                                                 |
| error1                  | String                   | yes       | error code state 1, if in error (see the KeContact FAQ)                 |
| error2                  | String                   | yes       | error code state 2, if in error (see the KeContact FAQ)                 |
| maxsystemcurrent        | Number:ElectricCurrent   | yes       | maximum current the wallbox can deliver                                 |
| failsafecurrent         | Number:ElectricCurrent   | yes       | maximum current the wallbox can deliver, if network is lost             |
| uptime                  | Number:Time              | yes       | system uptime since the last reset of the wallbox                       |
| sessionconsumption      | Number:Energy            | yes       | energy delivered in current session                                     |
| totalconsumption        | Number:Energy            | yes       | total energy delivered since the last reset of the wallbox              |
| authreq                 | Switch                   | yes       | authentication required                                                 |
| authon                  | Switch                   | yes       | authentication enabled                                                  |
| sessionrfidtag          | String                   | yes       | RFID tag used for the last charging session                             |
| sessionrfidclass        | String                   | yes       | RFID tag class used for the last charging session                       |
| sessionid               | Number                   | yes       | session ID of the last charging session                                 |
| setenergylimit          | Number:Energy            | no        | set an energy limit for an already running or the next charging session |
| authenticate            | String                   | no        | authenticate and start a session using RFID tag+RFID class              |
| maxpilotcurrent         | Number:ElectricCurrent   | yes       | current offered to the vehicle via control pilot signalization          |
| maxpilotcurrentdutycyle | Number:Dimensionless     | yes       | duty cycle of the control pilot signal                                  |

## Rule Actions

Certain Keba models support setting the text on the built-in display.
The text can be set via a rule action `setDisplay`. It comes in two variants:

```java
rule "Set Display Text"
when
  System reached start level 100
then
   val keContactActions = getActions("keba", "keba:kecontact:1")
   // Default duration
   keContactActions.setDisplay("TEXT$1")
   // Explicit duration set
   keContactActions.setDisplay("TEXT$2", 5, 10)
end
```

| Parameter                | Description                                                                                                                                                                      |
| ------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| text                     | Text shown on the display. Maximum 23 ASCII characters can be used. `~` == Σ, `$` == blank, `,` == comma                                                                         |
| durationMin _(optional)_ | Defines the duration in seconds how long the text will displayed before another display command will be processed (internal MID metering relevant information may overrule this) |
| durationMax _(optional)_ | Defines the duration in seconds how long the text will displayed if no additional display command follows.                                                                       |

## Example

demo.Things:

```java
Thing keba:kecontact:1 [ipAddress="192.168.0.64", refreshInterval=30]
```

demo.items:

```java
Number:Dimensionless      KebaCurrentRange      "Maximum supply current [%.1f %%]"        {channel="keba:kecontact:1:maxpresetcurrentrange"}
Number:ElectricCurrent    KebaCurrent           "Maximum supply current [%.3f A]"         {channel="keba:kecontact:1:maxpresetcurrent"}
Number:ElectricCurrent    KebaSystemCurrent     "Maximum system supply current [%.3f A]"  {channel="keba:kecontact:1:maxsystemcurrent"}
Number:ElectricCurrent    KebaFailSafeCurrent   "Failsafe supply current [%.3f A]"        {channel="keba:kecontact:1:failsafecurrent"}
Number                    KebaState             "Operating State [%s]"                    {channel="keba:kecontact:1:state"}
Switch                    KebaEnabledSystem     "Enabled (System)"                        {channel="keba:kecontact:1:enabledsystem"}
Switch                    KebaEnabledUser       "Enabled (User)"                          {channel="keba:kecontact:1:enableduser"}
Switch                    KebaWallboxPlugged    "Plugged into wallbox"                    {channel="keba:kecontact:1:wallbox"}
Switch                    KebaVehiclePlugged    "Plugged into vehicle"                    {channel="keba:kecontact:1:vehicle"}
Switch                    KebaPlugLocked        "Plug locked"                             {channel="keba:kecontact:1:locked"}
DateTime                  KebaUptime            "Uptime [%s s]"                           {channel="keba:kecontact:1:uptime"}
Number:ElectricCurrent    KebaI1                                                          {channel="keba:kecontact:1:I1"}
Number:ElectricCurrent    KebaI2                                                          {channel="keba:kecontact:1:I2"}
Number:ElectricCurrent    KebaI3                                                          {channel="keba:kecontact:1:I3"}
Number:ElectricPotential  KebaU1                                                          {channel="keba:kecontact:1:U1"}
Number:ElectricPotential  KebaU2                                                          {channel="keba:kecontact:1:U2"}
Number:ElectricPotential  KebaU3                                                          {channel="keba:kecontact:1:U3"}
Number:Power              KebaPower             "Energy during current session [%.1f W]"  {channel="keba:kecontact:1:power"}
Number:Energy             KebaSessionEnergy                                               {channel="keba:kecontact:1:sessionconsumption"}
Number:Energy             KebaTotalEnergy       "Energy during all sessions [%.1f Wh]"    {channel="keba:kecontact:1:totalconsumption"}
Switch                    KebaInputSwitch                                                 {channel="keba:kecontact:1:input"}
Switch                    KebaOutputSwitch                                                {channel="keba:kecontact:1:output"}
Number:Energy             KebaSetEnergyLimit    "Set charge energy limit [%.1f Wh]"       {channel="keba:kecontact:1:setenergylimit"}
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
 Text label="Charging Station" {
  Text  item=KebaState
  Text  item=KebaUptime
  Switch  item=KebaEnabledSystem
  Switch  item=KebaEnabledUser
  Switch  item=KebaWallboxPlugged
  Switch  item=KebaVehiclePlugged
  Switch  item=KebaPlugLocked
  Slider  item=KebaCurrentRange
  Text  item=KebaCurrent
  Text  item=KebaSystemCurrent
  Text  item=KebaFailSafeCurrent
  Text  item=KebaSessionEnergy
  Text  item=KebaTotalEnergy
  Switch  item=KebaSetEnergyLimit
 }
}
```

## Troubleshooting

### Enable Verbose Logging

Enable `DEBUG` or `TRACE` (even more verbose) logging for the logger named:

```text
org.openhab.binding.keba
```

If everything is working fine, you see the cyclic reception of `report 1`, `2` & `3` from the station. The frequency is according to the `refreshInterval` configuration.

### UDP Ports used

```text
Send port = UDP 7090
```

The Keba station is the server

```text
Receive port = UDP 7090
```

This binding is providing the server

UDP port 7090 needs to be available/free on the openHAB server.

In order to enable the UDP port 7090 on the Keba station with full functionality, `DIP switch 1.3` must be `ON`.
With `DIP switch 1.3 OFF` only ident-data can be read (`i` and `report 1`) but not the other reports as well as the commands needed for the write access.
After setting the DIP switch, you need to `power OFF` and `ON` the station. SW-reset via WebGUI seems not to be sufficient in order to apply the new configuration.

The right configuration can be validated as follows:

- WebGUI DSW Settings:
  - `DIP 1.3 | ON | UDP interface (SmartHome)`
- UDP response of `report 1`:
  - `DIP-Sw1` `0x20` Bit is set (enable at least `DEBUG` log-level for the binding)

### Supported stations

- KeContact P20 charging station with network connection (LSA+ socket)
  - Product code: `KC-P20-xxxxxx2x-xxx` or `KC-P20-xxxxxx3x-xxx`
  - Firmware version: 2.5 or higher
- KeContact P30 charging station (c- or x-series) or BMW wallbox
  - Firmware version 3.05 or higher
