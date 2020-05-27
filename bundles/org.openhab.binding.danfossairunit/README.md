# DanfossAirUnit Binding

This binding supports controlling and monitoring [Danfoss air units](https://www.danfoss.com/en/products/energy-recovery-devices/dhs/heat-recovery-ventilation/air-units/) via Ethernet connection.
 
## Supported Things

This binding has been tested/reported to work with the Danfoss Air w2 / a2 / a3 devices. 

## Discovery

Air units in the LAN are automatically discovered via broadcast and added to the Inbox. 

## Thing Configuration

These are the available configuration parameters:

- `host` Hostname/IP of the air unit (automatically set by discovery service)
- `refreshInterval` Time (in seconds) between monitoring requests to the air unit. Smaller values mean more network load, typically set between a few seconds and a minute. Defaults to 10 seconds. 
- `updateUnchangedValuesEveryMillis` Minimum time between state updates sent to the event bus for a particular channel when the state of the channel didn't change. This should avoid spamming the event bus with unnecessary updates. When set to 0, all channel state are updated every time the air unit requests are sent (see refresh interval). When set to a non zero value, unchanged values are only reported after the configured timespan has passed. Changed values are always sent to the event bus. Defaults to 60.000 (one minute), so updates are sent every minute or if the state of the channel changes.                  

## Channels

| channel | channel group | type   | readable only (RO) or writable (RW) | description                  |
|---|---|---|---|---|
| current_time | main | DateTime | RO | Current time reported by the air unit.  |
| mode | main | String | RW | Value to control the operation mode of the air unit. One of DEMAND, PROGRAM, MANUAL and OFF  |
| manual_fan_speed | main | Dimmer | RW | Value to control the fan speed when in MANUAL mode (10 steps) |
| supply_fan_speed | main | Number | RO | Current rotation of the fan supplying air to the rooms (in rpm) |
| extract_fan_speed | main | Number | RO | Current rotation of the fan extracting air from the rooms (in rpm) |
| supply_fan_step | main | Dimmer | RO | Current 10-step setting of the fan supplying air to the rooms |
| extract_fan_step | main | Dimmer | RO | Current 10-step setting of the fan extracting air from the rooms |
| boost | main | Switch | RW | Enables fan boost  |
| night_cooling | main | Switch | RW | Enables night cooling  |
| room_temp | temps | Number | RO | Temperature of the air in the room of the Air Dial  |
| room_temp_calculated | temps | Number | RO | Calculated Room Temperature  |
| outdoor_temp | temps | Number | RO | Temperature of the air outside  |
| humidity | humidity | Number | RO | Humidity  |
| bypass | recuperator | Switch | RW | Disables the heat exchange. Useful in summer when room temperature is above target and outside temperature is below target.  |
| supply_temp | recuperator | Number | RO | Temperature of air which is passed to the rooms  |
| extract_temp | recuperator | Number | RO | Temperature of the air as extracted from the rooms  |
| exhaust_temp | recuperator | Number | RO | Temperature of the air when pushed outside  |
| battery_life | service | Number | RO | Remaining Air Dial Battery Level (percentage) |
| filter_life | service | Number | RO | Remaining life of filter until exchange is necessary (percentage) |


## Full Example

### Things

Suppose your autodiscovered air unit is identified by the id "danfossairunit:airunit:-1062731769" (see section "Discovery").
The channel will then be identified by `<air unit id>:<channel group>#<channel>`

You can also manually configure your air unit in case you don't want to use autodiscovery
 (e. g. if you want to have a portable configuration):
Create a new file, e. g. `danfoss.things`, in your _things_ configuration folder: 
```
Thing danfossairunit:airunit:myairunit [host="192.168.0.7",
refreshInterval=5,
updateUnchangedValuesEveryMillis=30000]
```

### Items

```
Dimmer Lueftung_Drehzahl_Manuell "Drehzahl Lüftung %" (All,Lueftung) {channel = "danfossairunit:airunit:-1062731769:main#manual_fan_speed"}
Number Lueftung_Drehzahl_Supply "Drehzahl Lüftung Zuluft (rpm)" (All,Lueftung) {channel = "danfossairunit:airunit:-1062731769:main#supply_fan_speed"}
Number Lueftung_Drehzahl_Extract "Drehzahl Lüftung Abluft (rpm)" (All,Lueftung) {channel = "danfossairunit:airunit:-1062731769:main#extract_fan_speed"}
String Lueftung_Mode "Betriebsart Lüftung" (All,Lueftung) {channel = "danfossairunit:airunit:-1062731769:main#mode"}
Switch Lueftung_Boost "Stoßlüftung" (All,Lueftung) {channel = "danfossairunit:airunit:-1062731769:main#boost"}
Switch Lueftung_Bypass "Lüftung Bypass" (All,Lueftung) {channel = "danfossairunit:airunit:-1062731769:recuperator#bypass"}
```

### Sitemap

```
Slider item=Lueftung_Drehzahl_Manuell
Text item=Lueftung_Drehzahl_Supply
Text item=Lueftung_Drehzahl_Extract
Selection item=Lueftung_Mode mappings=[DEMAND="Bedarfslüftung", OFF="Aus", PROGRAM="Programm", MANUAL="manuell"]
Switch item=Lueftung_Boost
Switch item=Lueftung_Bypass
```