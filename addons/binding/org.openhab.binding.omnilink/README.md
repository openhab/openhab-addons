# HAI/Leviton Omnilink Binding

This binding integrates the [Omni and Lumina](http://www.leviton.com/en/products/security-automation/automation-av-controllers/omni-security-systems) line of home automation systems. At Its core the Omni is a hardware board that provides security and access features.  It connects to many other devices through serial ports or wired contacts and exposes them through a single TCP based API.

## Supported Things

The Omni/Lumina controller acts as a "bridge" for accessing other connected devices.


| Omni type           | Hardware Type                                    | Things |
|---------------------|--------------------------------------------------|--------
| Controller          | Omni (Pro II, IIe, LTe), Lumina                  | omni, lumina
| Lights              | Builtin, UPB,X-10, PLC, ALC                      | unit, upb, x10, plc, alc
| Thermostats         | Omnistat, Omnistat2                              | thermostat
| Zones               | Built-in/Hardwire, GE Wireless                   | zone
| Audio Zones/Sources | HAI Hi-Fi, Russound, NuVo, Xantech, Speakercraft | audio_zone, audio_source
| Consoles            | HAI Omni Console, HAI Lumina Console             | console
| Areas               | Builtin                                          | area
| Buttons             | Builtin                                          | button
| Flags               | Builtin                                          | flag


## Discovery

### Controller

Omni and Lumina controllers must be manually added using the IP and port of the controller as well as the 2 encryption keys required for network access.

### Devices

Once a connection can be established to a controller, all connected devices will be automatically discovered and added to the inbox.

## Thing Configuration

A Omni or Lumina controller requires the IP address, optional port (defaults to 4369), and 2 encryption keys.  The hexadecimal pairs in the encryption keys are typically delimited using a colon`:`, but dashes `-`, spaces ` ` or no delimiter may be used.

In the thing file, this looks e.g. like

```
Bridge omnilink:controller:1 [ ipAddress="192.168.1.10", key1="00:11:22:33:0A:0B:0C:0D", key2="00:11:22:33:1A:1B:1C:1D" ]
```

The devices are identified by device number that the Omni bridge assigns to them, for manual configuration this looks like:

```
zone 14 [ number="14" ]
```

## Channels

### Controller

### Units

### Thermostats

### Zones

### Areas

### Buttons

### Flags

### Audio Zones

### Audio Sources

## Full Example

### demo.things


```
Bridge omnilink:controller:home [ ipAddress="192.168.1.10", key1="00:11:22:33:0A:0B:0C:0D", key2="00:11:22:33:1A:1B:1C:1D" ] {
  area 1 [ number="1"]
  zone 1 [ number="1" ]  
}
```

### demo.items
```
Group:Contact:OR(OPEN, CLOSED) Zones "All Zones [%s]"
Group:Switch:OR(ON, OFF) Alarms "All Alarms [%s]"

Number    AlarmMode                 "Alarm [MAP(area-modes.map):%s]"      {channel="omnilink:area:home:1:mode"}
Number    ConsoleBeep                                                     {channel="omnilink:controller:home:beep"}
Contact   ZoneFrontDoor             "Front Door [%s]"       (Zones)       {channel="omnilink:zone:home:1:contact"}
String    ZoneFrontDoorBypass                                             {channel="omnilink:zone:home:1:bypass"}
String    ZoneFrontDoorRestore                                            {channel="omnilink:zone:home:1:restore"}


Contact   ZoneGarageDoor            "Garage Door [%s]"        (Zones)     {channel="omnilink:zone:home:2:contact"}
Contact   ZoneExtGarageDoor         "Ext Garage Door [%s]"    (Zones)     {channel="omnilink:zone:home:4:contact"}
Contact   ZoneKitchenDoor           "Kitchen Door [%s]"       (Zones)     {channel="omnilink:zone:home:5:contact"}
Contact   ZoneMotion                "Motion [%s]"             (Zones)     {channel="omnilink:zone:home:6:contact"}
Contact   ZoneGreatRoom             "Great Room [%s]"         (Zones)     {channel="omnilink:zone:home:7:contact"}
Contact   ZoneDinningRoom           "Dinning Room [%s]"       (Zones)     {channel="omnilink:zone:home:8:contact"}
Contact   ZoneKitchenOffice         "Kitchen/Office [%s]"     (Zones)     {channel="omnilink:zone:home:9:contact"}
Contact   ZoneLivingRoom            "Living Room [%s]"        (Zones)     {channel="omnilink:zone:home:10:contact"}
Contact   ZoneMaster                "Master [%s]"             (Zones)     {channel="omnilink:zone:home:11:contact"}
Contact   ZoneBed2                  "Bed 2 [%s]"              (Zones)     {channel="omnilink:zone:home:12:contact"}
Contact   ZoneBed3                  "Bed 3 [%s]"              (Zones)     {channel="omnilink:zone:home:13:contact"}
Contact   ZoneBed4                  "Bed 4 [%s]"              (Zones)     {channel="omnilink:zone:home:14:contact"}

Switch    AlarmBurglary             "Burglary Alarm [%s]"     (Alarms)    {channel="omnilink:area:home:1:alarm_burglary"}
Switch    AlarmFire                 "Fire Alarm [%s]"         (Alarms)    {channel="omnilink:area:home:1:alarm_fire"}
Switch    alarm_gas                 "Gas Alarm [%s]"          (Alarms)    {channel="omnilink:area:home:1:alarm_gas"}
Switch    AlarmAuxiliary            "Auxiliary Alarm [%s]"    (Alarms)    {channel="omnilink:area:home:1:alarm_auxiliary"}
Switch    AlarmFreeze               "Freeze Alarm [%s]"       (Alarms)    {channel="omnilink:area:home:1:alarm_freeze"}
Switch    AlarmWater                "Water Alarm [%s]"        (Alarms)    {channel="omnilink:area:home:1:alarm_water"}
Switch    AlarmDuress               "Duress Alarm [%s]"       (Alarms)    {channel="omnilink:area:home:1:alarm_duress"}
Switch    AlarmTemperature          "Temperature Alarm [%s]"  (Alarms)    {channel="omnilink:area:home:1:alarm_temperature"}

Number    AlarmModeDisarm                                                 {channel="omnilink:area:home:1:disarm"}
Number    AlarmModeDay                                                    {channel="omnilink:area:home:1:day"}
Number    AlarmModeNight                                                  {channel="omnilink:area:home:1:night"}
Number    AlarmModeAway                                                   {channel="omnilink:area:home:1:away"}
Number    AlarmModeVacation                                               {channel="omnilink:area:home:1:vacation"}
Number    AlarmModeDayInstant                                             {channel="omnilink:area:home:1:day_instant"}
Number    AlarmModeNightDelayed                                           {channel="omnilink:area:home:1:night_delayed"}
```
