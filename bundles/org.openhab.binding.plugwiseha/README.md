# PlugwiseHA Binding

The Plugwise Home Automation binding adds support to openHAB for the [Plugwise Home Automation ecosystem](https://www.plugwise.com/en_US/adam_zone_control). This system is built around a gateway from Plugwise called the 'Adam' which incorporates a ZigBee controller to manage thermostatic radiator valves, room thermostats, floor heating pumps, et cetera.

Users can manage and control this system either via a web app or a mobile phone app developed by Plugwise. The (web) app allows users to define heating zone's (e.g. rooms) and add radiator valves to those rooms to manage and control their heating irrespective of other rooms.

Using the Plugwise Home Automation binding you can incorporate the management of these devices and heating zones into openHAB. The binding uses the same RESTfull API that both the mobile phone app and the web app use.

The binding requires users to have a working Plugwise Home Automation setup consisting of at least 1 gateway device (the 'Adam') and preferably 1 radiator valve as a bare minimum. The 'Adam' (from hereon called the gateway) needs to be accessible from the openHAB instance via a TCP/IP connection.

## Supported Things

| Device Type                                              | Description                                                                                                        | Thing Type      |
| -------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ | --------------- |
| -                                                        | A Plugwise heating zone configured with at least 1 of the devices below                                            | zone            |
| [Adam](https://www.plugwise.com/en_US/products/adam-ha)  | The Plugwise Home Automation Bridge is needed to connect to the Adam boiler gateway                                | gateway         |
| [Tom](https://www.plugwise.com/en_US/products/tom)       | A Plugwise Home Automation radiator valve                                                                          | appliance_valve |
| [Floor](https://www.plugwise.com/en_US/products/floor)   | A Plugwise Home Automation radiator valve specificaly used for floor heating                                       | appliance_valve |
| [Circle](https://www.plugwise.com/en_US/products/circle) | A power outlet plug that provides energy measurement and switching control of appliances (e.g. floor heating pump) | appliance_pump  |
| [Lisa](https://www.plugwise.com/en_US/products/lisa)     | A room thermostat (also supports the 'Anna' room thermostat)                                                       | appliance_thermostat |
| [Boiler]                                                 | A central boiler used for heating and/or domestic hot water                                                        | appliance_boiler |



## Discovery

After setting up the Plugwise Home Automation bridge you can start a manual scan to find all devices registered on the gateway. You can also manually add things by entering the corresponding device id as a configuration parameter. The device id's can be found be enabling TRACE logging in the Karaf console.

## Thing Configuration

You must define a Plugwise Home Automation gateway (Bridge) before defining zones or appliances (Things) for this binding to work.

#### Plugwise Home Automation gateway (Bridge):

| Parameter | Description                                                             | Config   | Default |
| --------- | ----------------------------------------------------------------------- | -------- | ------- |
| host      | The IP address or hostname of the Adam HA gateway                       | Required | 'adam'  |
| username  | The username for the Adam HA gateway                                    | Optional | 'smile' |
| smileID   | The 8 letter code on the sticker on the back of the Adam boiler gateway | Required | -       |
| refresh   | The refresh interval in seconds                                         | Optional | 15      |

#### Plugwise Home Automation zone (`zone`):

| Parameter | Description               | Config   | Default |
| --------- | ------------------------- | -------- | ------- |
| id        | The unique ID of the zone | Required | -       |

#### Plugwise Home Automation appliance (`appliance_valve`):

| Parameter            | Description                                                                                                        | Config   | Default |
| -------------------- | ------------------------------------------------------------------------------------------------------------------ | -------- | ------- |
| id                   | The unique ID of the radiator valve appliance                                                                      | Required | -       |
| lowBatteryPercentage | Battery charge remaining at which to trigger battery low warning. (*Only applicable for battery operated devices*) | Optional | 15      |

#### Plugwise Home Automation appliance (`appliance_thermostat`):

| Parameter            | Description                                                                                                        | Config   | Default |
| -------------------- | ------------------------------------------------------------------------------------------------------------------ | -------- | ------- |
| id                   | The unique ID of the room thermostat appliance                                                                     | Required | -       |
| lowBatteryPercentage | Battery charge remaining at which to trigger battery low warning. (*Only applicable for battery operated devices*) | Optional | 15      |


#### Plugwise Home Automation appliance (`appliance_pump`):

| Parameter | Description                         | Config   | Default |
| --------- | ----------------------------------- | -------- | ------- |
| id        | The unique ID of the pump appliance | Required | -       |

#### Plugwise Home Automation zone (`zone`):

| Parameter | Description                 | Config   | Default |
| --------- | --------------------------- | -------- | ------- |
| id        | The unique ID of the boiler | Required | -       |

## Channels

| channel | type   | Read-only?  | description                 |
| ------- | ------ | ------------ | --------------- |
| temperature | Number:Temperature | Yes | This channel is used to read the temperature of an appliance that supports the thermostat functionality |
| setpointTemperature | Number:Temperature | No | This channel is used to read or write the setpoint temperature of an appliance that supports the thermostat functionality |
| power | Switch | No | This channel is used to toggle an appliance ON/OFF that supports the relay functionality |
| lock | Switch | No | This channel is used to toggle an appliance lock ON/OFF that supports the relay functionality.(*When the lock is ON the gateway will not automatically control the corresponding relay switch depending on thermostat mode*) |
| powerUsage | Number | Yes | This channel is used to read the current power usage in Watts of an appliance that supports this |
| batteryLevel | Number | Yes | This channel is used to read the current battery level of an appliance that is battery operated |
| batteryLevelLow | Switch | Yes | This channel will switch ON when the battery level of an appliance that is battery operated drops below a certain threshold |
| chState | Switch | Yes | This channel is used to read the current central heating state of the boiler |
| dhwState | Switch | Yes | This channel is used to read the current domestic hot water state of the boiler |
| waterPressure | Number | Yes | This channel is used to read the current water pressure of the boiler |

## Full Example

**things/plugwiseha.things**

```
Bridge plugwiseha:gateway:home "Plugwise Home Automation Gateway" [ smileId="abcdefgh" ] {
	Thing zone living_room_zone "Living room" [ id="$device_id" ]
    Thing appliance_valve living_room_radiator "Living room radiator valve" [ id="$device_id" ]
	Thing appliance_thermostat living_room_thermostat "Living room thermostat" [ id="$device_id" ]
    Thing appliance_pump living_room_pump "Floor heating pump" [ id="$device_id" ]
	Thing appliance_boiler main_boiler "Main boiler" [ id="$device_id" ]
}
```

Replace `$device_id` accordingly.

**items/plugwiseha.items**

```
Number living_room_zone_temperature "Zone temperature" {channel="plugwiseha:zone:home:living_room_zone:temperature"}
Number living_room_zone_temperature_setpoint "Zone temperature setpoint" {channel="plugwiseha:zone:home:living_room_zone:setpointTemperature"}

Number living_room_radiator_temperature "Radiator valve temperature" {channel="plugwiseha:appliance_valve:home:living_room_radiator:temperature"}
Number living_room_radiator_temperature_setpoint "Radiator valve temperature setpoint" {channel="plugwiseha:appliance_valve:home:living_room_radiator:setpointTemperature"}

Number living_room_thermostat_temperature "Room thermostat temperature" {channel="plugwiseha:appliance_valve:home:living_room_thermostat:temperature"}
Number living_room_thermostat_temperature_setpoint "Room thermostat temperature setpoint" {channel="plugwiseha:appliance_valve:home:living_room_thermostat:setpointTemperature"}

Switch living_room_pump_power "Floor heating pump power" {channel="plugwiseha:appliance_pump:home:living_room_pump:power"}
Switch living_room_pump_lock "Floor heating pump lock [MAP:(plugwiseha.map):%s]" {channel="plugwiseha:appliance_pump:home:living_room_pump:lock"}
Number living_room_pump_power_usage "Floor heating pump power [%0.2fW]" {channel="plugwiseha:appliance_pump:home:living_room_pump:powerUsage"}

Number	main_boiler_waterpressure "Waterpressure" { channel="plugwiseha:appliance_boiler:home:main_boiler:waterPressure"}
Switch	main_boiler_chState "Heating active" { channel="plugwiseha:appliance_boiler:home:main_boiler:chActive"}
Switch	main_boiler_dhwState "Domestic hot water active" { channel="plugwiseha:appliance_boiler:home:main_boiler:dhwActive"}
```

**transform/plugwiseha.map**

```
ON=Locked
OFF=Unlocked
```

**sitemaps/plugwiseha.sitemap**

```
sitemap plugwiseha label="PlugwiseHA Binding"
{
	Frame {
        Text item=living_room_zone_temperature
        Setpoint item=living_room_zone_temperature_setpoint label="Living room [%.1f °C]" minValue=5.0 maxValue=25 step=0.5

		Text item=living_room_radiator_temperature
        Setpoint item=living_room_radiator_temperature_setpoint label="Living room [%.1f °C]" minValue=5.0 maxValue=25 step=0.5

		Text item=living_room_thermostat_temperature
        Setpoint item=living_room_thermostat_temperature_setpoint label="Living room [%.1f °C]" minValue=5.0 maxValue=25 step=0.5

		Number item=living_room_pump_power_usage
		Switch item=living_room_pump_power
		Switch item=living_room_pump_lock

		Number item=main_boiler_waterpressure
		Switch item=main_boiler_chState
		Switch item=main_boiler_dhwState
	}
}
```