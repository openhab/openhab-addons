# NeoHub Binding

This is a binding for integrating Heatmiser room and underfloor heating control products.
The NeoHub (bridge) binding allows you to connect openHab via TCP/IP to Heatmiser's NeoHub and integrate your NeoStat smart thermostats and NeoPlug smart plugs onto the bus.

See the manufacturers web site for more details: https://www.heatmiser.com

## Supported Things

The binding supports three types of Thing as follows..

| Thing Type           | Description                                                                               |
|----------------------|-------------------------------------------------------------------------------------------|
| NeoHub               | The Heatmiser NeoHub bridge which is used to communicate with NeoStat and NeoPlug devices |
| NeoStat              | Heatmiser Neostat Smart Thermostat                                                        |
| NeoPlug              | Heatmiser NeoPlug Smart Plug                                                              |
| NeoContact           | Heatmiser (wireless) door or window contact                                               |
| NeoTemperatureSensor | Heatmiser (wireless) temperature sensor                                                   |

## Discovery

You have to manually create a single (Bridge) Thing for the NeoHub, and enter the required Configuration Parameters (see Thing Configuration for NeoHub below).
If the Configuration Parameters are all valid, then the NeoHub Thing will automatically attempt to connect and sign on to the hub.
If the sign on succeeds, the Thing will indicate its status as Online, otherwise it will show an error status. 

Once the NeoHub Thing has been created and it has successfully signed on, it will automatically interrogate the server to discover all the respective NeoXxx Things that are connected to the hub.

## Thing Configuration for "NeoHub"

The NeoHub Thing connects to the hub (bridge) to communicate with any respective connected NeoXxx Things.
It signs on to the hub using the supplied connection parameters, and it polls the hub at regular intervals to read and write the data for each NeoXxx device.
Before it can connect to the hub, the following Configuration Parameters must be entered.   

| Configuration Parameter | Description                                                                               |
|-------------------------|-------------------------------------------------------------------------------------------|
| hostName                | Host name (IP address) of the NeoHub (example 192.168.1.123)                              |
| portNumber              | Port number of the NeoHub (Default=4242)                                                  |
| pollingInterval         | Time (seconds) between polling requests to the NeoHub (Minimum=4; Maximum=60; Default=60) |

## Thing Configuration for "NeoStat" and "NeoPlug"

The NeoHub Thing connects to the hub (bridge) to communicate with any NeoStat or NeoPlug devices that are connected to it.
Each such NeoStat or NeoPlug device is identified by means of a unique Device Name in the hub.
The Device Name is automatically discovered by the NeoHub Thing, and it is also visible (and changeable) via the Heatmiser App.
    
| Configuration Parameter | Description                                                                   |
|-------------------------|-------------------------------------------------------------------------------|
| deviceNameInHub         | Device Name that identifies the NeoXxx device in the NeoHub and Heatmiser App |

## Channels for "NeoStat"

The following Channels, and their associated channel types are shown below.

| Channel               | Data Type          | Description                                                                 |
|-----------------------|--------------------|-----------------------------------------------------------------------------|
| roomTemperature       | Number:Temperature | Actual room temperature                                                     |
| targetTemperature     | Number:Temperature | Target temperature setting for the room                                     |
| floorTemperature      | Number:Temperature | Actual floor temperature                                                    |
| thermostatOutputState | String             | Status of whether the thermostat is Off, or calling for Heat                |
| occupancyModePresent  | Switch             | The Thermostat is in the Present Occupancy Mode (Off=Absent, On=Present)    |
| batteryLowAlarm       | Switch             | The battery is low (only applies to battery thermostats) (Off=Ok, On=Alarm) |

## Channels for "NeoPlug"

The following Channels, and their associated channel types are shown below.

| Channel              | Data Type | Description                                              |
|----------------------|-----------|----------------------------------------------------------|
| plugOutputState      | Switch    | The output state of the Plug switch (Off, On)            |
| plugAutoMode         | Switch    | The Plug is in Automatic Mode (Off=Manual, On=Automatic) |

## Channels for "NeoContact"

The following Channels, and their associated channel types are shown below.

| Channel           | Data Type | Description                                              |
|-------------------|-----------|----------------------------------------------------------|
| contactState      | Switch    | The state of the contact (Off, On) (Read-Only)           |
| batteryLowAlarm   | Switch    | The battery is low (Off=Ok, On=Alarm)                    |

## Channels for "NeoTemperatureSensor"

The following Channels, and their associated channel types are shown below.

| Channel           | Data Type          | Description                                     |
|-------------------|--------------------|-------------------------------------------------|
| sensorTemperature | Number:Temperature | Actual measured temperature                     |
| batteryLowAlarm   | Switch             | The battery is low (Off=Ok, On=Alarm)           |

## Full Example

### `demo.things` File

```
Bridge neohub:neohub:myhubname "Heatmiser NeoHub" [ hostName="192.168.1.123", portNumber=4242, pollingInterval=60 ] {
    Thing neoplug mydownstairs "Downstairs Plug" @ "Hall" [ deviceNameInHub="Hall Plug" ]
    Thing neostat myupstairs "Upstairs Thermostat" @ "Landing" [ deviceNameInHub="Landing Thermostat" ]
    Thing neocontact mycontact "Window Contact" @ "Bedroom" [ deviceNameInHub="Bedroom Window Contact" ]
    Thing neotemperaturesensor mysensor "Kitchen Temperature" @ "Kitchen" [ deviceNameInHub="Kitchen Temperature Sensor" ]
}
```

### `demo.items` File

```
Number:Temperature Upstairs_RoomTemperature "Room Temperature" { channel="neohub:neostat:myhubname:myupstairs:roomTemperature" }
Number:Temperature Upstairs_TargetTemperature "Target Temperature" { channel="neohub:neostat:myhubname:myupstairs:targetTemperature" }
Number:Temperature Upstairs_FloorTemperature "Floor Temperature" { channel="neohub:neostat:myhubname:myupstairs:floorTemperature" }
String Upstairs_ThermostatOutputState "Heating State" { channel="neohub:neostat:myhubname:myupstairs:thermostatOutputState" }
Switch Upstairs_OccupancyModePresent "Occupancy Mode Present" { channel="neohub:neostat:myhubname:myupstairs:myupstairs:occupancyModePresent" }

Switch Downstairs_PlugAutoMode "Plug Auto Mode" { channel="neohub:neoplug:myhubname:mydownstairs:plugAutoMode" }
Switch Downstairs_PlugOutputState "Plug Output State" { channel="neohub:neoplug:myhubname:mydownstairs:plugOutputState" }

Switch Window_Contact_State "Window Contact State" { channel="neohub:neocontact:myhubname:mycontact:contactState" }
Switch Window_Contact_Battery_Low "Window Contact Battery Low" { channel="neohub:neocontact:myhubname:mycontact:batteryLowAlarm" }

Number:Temperature Kitchen_Temperature "Kitchen Temperature" { channel="neohub:neotemperaturesensor:myhubname:mysensor:sensorTemperature" }
```

### `demo.sitemap` File

```
sitemap neohub label="Heatmiser NeoHub"
{
	Frame label="Thermostat" {
		Text      item=Upstairs_RoomTemperature 
		Setpoint  item=Upstairs_TargetTemperature minValue=15 maxValue=30 step=1
		Text      item=Upstairs_ThermostatOutputState
		Switch    item=Upstairs_OccupancyModePresent
		Text      item=Upstairs_FloorTemperature 
	}

	Frame label="Plug" {
		Switch item=Downstairs_PlugOutputState 	
		Switch item=Downstairs_PlugAutoMode
	}

	Frame label="Contact" {
		Switch item=Window_Contact_State
		Switch item=Window_Contact_Battery_Low
	}

	Frame label="Sensor" {
		Text item=Kitchen_Temperature
	}
}
```
