# PlugwiseHA Binding

The Plugwise Home Automation binding adds support to openHAB for the [Plugwise Home Automation ecosystem](https://www.plugwise.com/en_US/adam_zone_control).
This system is built around a gateway from Plugwise called the 'Adam' which incorporates a Zigbee controller to manage thermostatic radiator valves, room thermostats, floor heating pumps, et cetera.

Users can manage and control this system either via a web app or a mobile phone app developed by Plugwise.
The (web) app allows users to define heating zone's (e.g. rooms) and add radiator valves to those rooms to manage and control their heating irrespective of other rooms.

Using the Plugwise Home Automation binding you can incorporate the management of these devices and heating zones into openHAB.
The binding uses the same RESTfull API that both the mobile phone app and the web app use.

The binding requires users to have a working Plugwise Home Automation setup consisting of at least 1 gateway device (the 'Adam') and preferably 1 radiator valve as a bare minimum.
The 'Adam' (from hereon called the gateway) needs to be accessible from the openHAB instance via a TCP/IP connection.

## Supported Things

| Device Type                                              | Description                                                                                                        | Thing Type           |
|----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|----------------------|
| -                                                        | A Plugwise heating zone configured with at least 1 of the devices below                                            | zone                 |
| [Adam](https://www.plugwise.com/en_US/products/adam-ha)  | The Plugwise Home Automation Bridge is needed to connect to the Adam boiler gateway                                | gateway              |
| [Tom](https://www.plugwise.com/en_US/products/tom)       | A Plugwise Home Automation radiator valve                                                                          | appliance_valve      |
| [Floor](https://www.plugwise.com/en_US/products/floor)   | A Plugwise Home Automation radiator valve specifically used for floor heating                                      | appliance_valve      |
| [Circle](https://www.plugwise.com/en_US/products/circle) | A power outlet plug that provides energy measurement and switching control of appliances (e.g. floor heating pump) | appliance_pump       |
| [Lisa](https://www.plugwise.com/en_US/products/lisa)     | A room thermostat (also supports the 'Anna' room thermostat)                                                       | appliance_thermostat |
| [Boiler]                                                 | A central boiler used for heating and/or domestic hot water                                                        | appliance_boiler     |

## Discovery

After setting up the Plugwise Home Automation bridge you can start a manual scan to find all devices registered on the gateway.
You can also manually add things by entering the corresponding device id as a configuration parameter.
The device IDs can be found be enabling TRACE logging in the Karaf console.

## Thing Configuration

You must define a Plugwise Home Automation gateway (Bridge) before defining zones or appliances (Things) for this binding to work.

### Plugwise Home Automation gateway (Bridge)

| Parameter | Description                                                             | Config   | Default |
|-----------|-------------------------------------------------------------------------|----------|---------|
| host      | The IP address or hostname of the Adam HA gateway                       | Required | 'adam'  |
| username  | The username for the Adam HA gateway                                    | Optional | 'smile' |
| smileID   | The 8 letter code on the sticker on the back of the Adam boiler gateway | Required | -       |
| refresh   | The refresh interval in seconds                                         | Optional | 15      |

### Plugwise Home Automation zone (`zone`)

| Parameter | Description               | Config   | Default |
| --------- | ------------------------- | -------- | ------- |
| id        | The unique ID of the zone | Required | -       |

### Plugwise Home Automation appliance (`appliance_valve`)

| Parameter            | Description                                                                                                        | Config   | Default |
|----------------------|--------------------------------------------------------------------------------------------------------------------|----------|---------|
| id                   | The unique ID of the radiator valve appliance                                                                      | Required | -       |
| lowBatteryPercentage | Battery charge remaining at which to trigger battery low warning. (_Only applicable for battery operated devices_) | Optional | 15      |

### Plugwise Home Automation appliance (`appliance_thermostat`)

| Parameter            | Description                                                                                                        | Config   | Default |
|----------------------|--------------------------------------------------------------------------------------------------------------------|----------|---------|
| id                   | The unique ID of the room thermostat appliance                                                                     | Required | -       |
| lowBatteryPercentage | Battery charge remaining at which to trigger battery low warning. (_Only applicable for battery operated devices_) | Optional | 15      |

### Plugwise Home Automation appliance (`appliance_pump`)

| Parameter | Description                         | Config   | Default |
| --------- | ----------------------------------- | -------- | ------- |
| id        | The unique ID of the pump appliance | Required | -       |

### Plugwise Home Automation boiler (`appliance_boiler`)

| Parameter | Description                 | Config   | Default |
|-----------|-----------------------------|----------|---------|
| id        | The unique ID of the boiler | Required | -       |

## Channels

| channel               | type                 | Read-only? | description                                                                                                                                                                                          |
|-----------------------|----------------------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| temperature           | Number:Temperature   | Yes        | The temperature of an appliance that supports the thermostat functionality                                                                                                                           |
| setpointTemperature   | Number:Temperature   | No         | The setpoint temperature (read/write) of an appliance that supports the thermostat functionality                                                                                                     |
| power                 | Switch               | No         | Toggle an appliance ON/OFF that supports the relay functionality                                                                                                                                     |
| lock                  | Switch               | No         | Toggle an appliance lock ON/OFF that supports the relay functionality.(_When the lock is ON the gateway will not automatically control the corresponding relay switch depending on thermostat mode_) |
| powerUsage            | Number:Power         | Yes        | The current power usage in Watts of an appliance that supports this                                                                                                                                  |
| batteryLevel          | Number               | Yes        | The current battery level of an appliance that is battery operated                                                                                                                                   |
| batteryLevelLow       | Switch               | Yes        | Switches ON when the battery level of an appliance that is battery operated drops below a certain threshold                                                                                          |
| chState               | Switch               | Yes        | The current central heating state of the boiler                                                                                                                                                      |
| dhwState              | Switch               | Yes        | The current domestic hot water state of the boiler                                                                                                                                                   |
| waterPressure         | Number:Pressure      | Yes        | The current water pressure of the boiler                                                                                                                                                             |
| presetScene           | String               | No         | The current active scene for the zone                                                                                                                                                                |
| regulationControl     | String               | No         | Toggle current regulation control (Active, Passive, Off) for the zone                                                                                                                                |
| coolingAllowed        | Switch               | No         | Toggle the cooling allowed of a zone ON/OFF                                                                                                                                                          |
| valvePosition         | Number:Dimensionless | Yes        | The current position of the valve                                                                                                                                                                    |
| preHeat               | Switch               | No         | Toggle the pre heating of a zone ON/OFF                                                                                                                                                              |
| coolingState          | Switch               | Yes        | The current cooling state of the boiler                                                                                                                                                              |
| intendedBoilerTemp    | Number:Temperature   | Yes        | The intended boiler temperature                                                                                                                                                                      |
| flameState            | Switch               | Yes        | The flame state of the boiler                                                                                                                                                                        |
| intendedHeatingState  | Switch               | Yes        | The intended heating state of the boiler                                                                                                                                                             |
| modulationLevel       | Number:Dimensionless | Yes        | The current modulation level of the boiler                                                                                                                                                           |
| otAppFaultCode        | Number               | Yes        | The Opentherm application fault code of the boiler                                                                                                                                                   |
| dhwTemperature        | Number:Temperature   | Yes        | The current central heating state of the boiler                                                                                                                                                      |
| otOEMFaultCode        | Number               | Yes        | The Opentherm OEM fault code of the boiler                                                                                                                                                           |
| boilerTemperature     | Number:Temperature   | Yes        | The current temperature of the boiler                                                                                                                                                                |
| dhwSetpoint           | Number:Temperature   | Yes        | The domestic hot water setpoint                                                                                                                                                                      |
| maxBoilerTemperature  | Number:Temperature   | Yes        | The maximum temperature of the boiler                                                                                                                                                                |
| dhwComfortMode        | Switch               | Yes        | The domestic hot water confortmode                                                                                                                                                                   |
| burnerStartsFailed    | Number               | Yes        | Total count of failed burner starts                                                                                                                                                                  |
| burnerStarts          | Number               | Yes        | Total count of burner starts                                                                                                                                                                         |
| burnerIgnitionsFailed | Number               | Yes        | Total count of failed burner ignitions                                                                                                                                                               |
| burnerOpTime          | Number:Time          | Yes        | Total operation time                                                                                                                                                                                 |
| burnerDHWOpTime       | Number:Time          | Yes        | Total operation time for domestic hot water                                                                                                                                                          |

## Full Example

### plugwiseha.things

```java
Bridge plugwiseha:gateway:home "Plugwise Home Automation Gateway" [ smileId="abcdefgh" ] {
    Thing zone living_room_zone "Living room" [ id="$device_id" ]
    Thing appliance_valve living_room_radiator "Living room radiator valve" [ id="$device_id" ]
    Thing appliance_thermostat living_room_thermostat "Living room thermostat" [ id="$device_id" ]
    Thing appliance_pump living_room_pump "Floor heating pump" [ id="$device_id" ]
    Thing appliance_boiler main_boiler "Main boiler" [ id="$device_id" ]
}
```

Replace `$device_id` accordingly.

### plugwiseha.items

```java
Number:Temperature living_room_zone_temperature "Zone temperature" {channel="plugwiseha:zone:home:living_room_zone:temperature"}
Number:Temperature living_room_zone_temperature_setpoint "Zone temperature setpoint" {channel="plugwiseha:zone:home:living_room_zone:setpointTemperature"}
String living_room_zone_preset_scene "Zone preset scene" {channel="plugwiseha:zone:home:living_room_zone:presetScene"}
Switch living_room_zone_preheat "Zone preheat enabled" {channel="plugwiseha:zone:home:living_room_zone:preHeat"}
String living_room_zone_cooling "Zone cooling enabled" {channel="plugwiseha:zone:home:living_room_zone:coolingAllowed"}
String living_room_zone_regulation_control "Zone regulation control" {channel="plugwiseha:zone:home:living_room_zone:regulationControl"}

Number:Temperature living_room_radiator_temperature "Radiator valve temperature" {channel="plugwiseha:appliance_valve:home:living_room_radiator:temperature"}
Number:Temperature living_room_radiator_temperature_setpoint "Radiator valve temperature setpoint" {channel="plugwiseha:appliance_valve:home:living_room_radiator:setpointTemperature"}
Number living_room_radiator_valve_position "Radiator valve position" {channel="plugwiseha:appliance_valve:home:living_room_radiator:valvePosition"}

Number:Temperature living_room_thermostat_temperature "Room thermostat temperature" {channel="plugwiseha:appliance_valve:home:living_room_thermostat:temperature"}
Number:Temperature living_room_thermostat_temperature_setpoint "Room thermostat temperature setpoint" {channel="plugwiseha:appliance_valve:home:living_room_thermostat:setpointTemperature"}
Number:Temperature living_room_thermostat_temperature_offset "Room thermostat temperature offset" {channel="plugwiseha:appliance_valve:home:living_room_thermostat:offsetTemperature"}

Switch living_room_pump_power "Floor heating pump power" {channel="plugwiseha:appliance_pump:home:living_room_pump:power"}
Switch living_room_pump_lock "Floor heating pump lock [MAP:(plugwiseha.map):%s]" {channel="plugwiseha:appliance_pump:home:living_room_pump:lock"}
Number:Power living_room_pump_power_usage "Floor heating pump power [%0.2fW]" {channel="plugwiseha:appliance_pump:home:living_room_pump:powerUsage"}

Number:Pressure main_boiler_waterpressure "Waterpressure" { channel="plugwiseha:appliance_boiler:home:main_boiler:waterPressure"}
Switch  main_boiler_chState "Heating active" { channel="plugwiseha:appliance_boiler:home:main_boiler:chState"}
Switch  main_boiler_dhwState "Domestic hot water active" { channel="plugwiseha:appliance_boiler:home:main_boiler:dhwState"}

Switch main_boiler_coolingState "Cooling state" { channel="plugwiseha:appliance_boiler:home:main_boiler:coolingState"}
Number:Temperature main_boiler_intendedBoilerTemp "Intended boiler temperature" {channel="plugwiseha:appliance_boiler:home:main_boiler:intendedBoilerTemp"}
Switch main_boiler_flameState "Flame state" { channel="plugwiseha:appliance_boiler:home:main_boiler:flameState"}
Switch main_boiler_intendedHeatingState "Intended heating state" { channel="plugwiseha:appliance_boiler:home:main_boiler:intendedHeatingState"}
Number main_boiler_modulationLevel "Modulation level" {channel="plugwiseha:appliance_boiler:home:living_room_radiator:modulationLevel"}
Number main_boiler_otAppFaultCode "Opentherm app. faultcode" {channel="plugwiseha:appliance_boiler:home:living_room_radiator:otAppFaultCode"}
Number:Temperature main_boiler_dhwTemperature "DHW temperature" {channel="plugwiseha:appliance_boiler:home:main_boiler:dhwTemperature"}
Number main_boiler_otOEMFaultCode "Opentherm OEM faultcode" {channel="plugwiseha:appliance_boiler:home:main_boiler:otOEMFaultCode"}
Number:Temperature main_boiler_boilerTemperature "Boiler temperature" {channel="plugwiseha:appliance_boiler:home:main_boiler:boilerTemperature"}
Number:Temperature main_boiler_dhwSetpoint "DHW setpoint" {channel="plugwiseha:appliance_boiler:home:main_boiler:dhwSetpoint"}
Number:Temperature main_boiler_maxBoilerTemperature "Max. boiler temperature" {channel="plugwiseha:appliance_boiler:home:main_boiler:maxBoilerTemperature"}
Switch main_boiler_dhwComfortMode "DHW comfort mode" { channel="plugwiseha:appliance_boiler:home:main_boiler:dhwComfortMode"}
Number:Temperature main_boiler_returnTemperature "Boiler return temperature" {channel="plugwiseha:appliance_boiler:home:main_boiler:returnWaterTemperature"}
```

### plugwiseha.map

```text
ON=Locked
OFF=Unlocked
```

### plugwiseha.sitemap

```perl
sitemap plugwiseha label="PlugwiseHA Binding"
{
    Frame {
        Text item=living_room_zone_temperature
        Setpoint item=living_room_zone_temperature_setpoint label="Living room [%.1f °C]" minValue=5.0 maxValue=25 step=0.5
        Text item=living_room_zone_presetScene
        Switch item=living_room_zone_preheat
        Text item=living_room_zone_regulation_control
        Switch item=living_room_zone_cooling

        Text item=living_room_radiator_temperature
        Setpoint item=living_room_radiator_temperature_setpoint label="Living room [%.1f °C]" minValue=5.0 maxValue=25 step=0.5
        Text item=living_room_radiator_valve_position

        Text item=living_room_thermostat_temperature
        Setpoint item=living_room_thermostat_temperature_setpoint label="Living room [%.1f °C]" minValue=5.0 maxValue=25 step=0.5
        Setpoint item=living_room_thermostat_temperature_offset label="Living room offset [%.1f °C]" minValue=-5.0 maxValue=5 step=0.5

        Number item=living_room_pump_power_usage
        Switch item=living_room_pump_power
        Switch item=living_room_pump_lock

        Number item=main_boiler_waterpressure
        Switch item=main_boiler_chState
        Switch item=main_boiler_dhwState

        Switch item=main_boiler_coolingState
        Number item=main_boiler_intendedBoilerTemp
        Switch item=main_boiler_flameState
        Switch item=main_boiler_intendedHeatingState
        Number item=main_boiler_modulationLevel
        Number item=main_boiler_otAppFaultCode
        Number item=main_boiler_dhwTemperature
        Number item=main_boiler_otOEMFaultCode
        Number item=main_boiler_boilerTemperature
        Number item=main_boiler_dhwSetpoint
        Number item=main_boiler_maxBoilerTemperature
        Switch item=main_boiler_dhwComfortMode
        Number item=main_boiler_returnTemperature
    }
}
```
