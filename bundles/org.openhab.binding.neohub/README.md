# NeoHub Binding

The NeoHub (bridge) binding allows you to connect openHab via TCP/IP to Heatmiser's NeoHub and integrate your NeoStat smart thermostats and NeoPlug smart plugs onto the bus.

## Supported Things

The binding supports two types of Thing as follows..

| Thing Type | Description                                                                               |
|------------|-------------------------------------------------------------------------------------------|
| NeoHub     | The Heatmiser NeoHub bridge which is used to communicate with NeoStat and NeoPlug devices |
| NeoStat    | Heatmiser Neostat Smart Thermostat                                                        |
| NeoPlug    | Heatmiser NeoPlug Smart Plug                                                              |

## Discovery

You have to manually create a single (Bridge) Thing for the NeoHub, and enter the required Configuration Parameters (see Thing Configuration for NeoHub below). If the Configuration Parameters are all valid, then the NeoHub Thing will automatically attempt to connect and sign on to the hub. If the sign on succeeds, the Thing will indicate its status as Online, otherwise it will show an error status. 

Note: You must create ONLY ONE Thing of the type NeoHub; duplicate NeoHub things risk causing communication errors.   

Once the NeoHub Thing has been created and it has successfully signed on, it will automatically interrogate the server to discover all the respective NeoStat and NeoPlug Things that are connected to the hub. After a short while, all discovered Things will be displayed in the PaperUI Inbox. If in future you add new devices to your hub then these new devices will also appear in the Inbox.    

## Thing Configuration for "NeoHub"

The NeoHub Thing connects to the hub (bridge) to communicate with any respective connected NeoStat and NeoPlug Things. It signs on to the hub using the supplied connection parameters, and it polls the hub at regular intervals to read and write the data for each NeoXxx device. Before it can connect to the hub, the following Configuration Parameters must be entered.   

| Configuration Parameter | Description                                                                       |
|-------------------------|-----------------------------------------------------------------------------------|
| hostName                | Host name (IP address) of the NeoHub (example 192.168.1.123)                      |
| portNumber              | Port number of the NeoHub (Default Value: 4242)                                   |
| pollingInterval         | Time (seconds) between polling requests to the NeoHub (Default Value: 60 seconds) |

## Thing Configuration for "NeoStat" and "NeoPlug"

The NeoHub Thing connects to the hub (bridge) to communicate with any NeoStat or NeoPlug devices that are connected to it. Each such NeoStat or NeoPlug device is identified by means of a unique Device Name in the hub. The Device Name is automatically discovered by the NeoHub Thing, and it is also visible (and changeable) via the Heatmiser App.
    
| Configuration Parameter | Description                                                                   |
|-------------------------|-------------------------------------------------------------------------------|
| deviceNameInHub         | Device Name that identifies the NeoXxx device in the NeoHub and Heatmiser App |

Warning: take care when editing the Device Name in openHAB; the NeoHub Thing can only get data from the hub, for NeoPlugs and NeoStats that have a valid Device Name.         

## Channels for "NeoStat"

The following Channels, and their associated channel types are shown below.

| Channel               | Data Type | Description                                                                 |
|-----------------------|-----------|-----------------------------------------------------------------------------|
| roomTemperature       | Number    | Actual room temperature                                                     |
| targetTemperature     | Number    | Target temperature setting for the room                                     |
| floorTemperature      | Number    | Actual floor temperature                                                    |
| outputState           | String    | Status of whether the thermostat is calling for Heat, or Off                |
| occupancyModePresent  | Switch    | The Thermostat is in the Present Occupancy Mode (Off=Absent, On=Present)    |

## Channels for "NeoPlug"

The following Channels, and their associated channel types are shown below.

| Channel              | Data Type | Description                                              |
|----------------------|-----------|----------------------------------------------------------|
| outputState          | Switch    | The state of the Plug switch, Off or On                  |
| autoMode             | Switch    | The Plug is in Automatic Mode (Off=Manual, On=Automatic) |


## Full Example

### `demo.things` File

As a general rule it is **recommended** to create the NeoHub, NeoStat and NeoPlug Things using the PaperUI; however if you wish to do so, you *MAY* create them by means of a `.things` file as follows..

```
Bridge neohub:neohub:myhubname "Heatmiser NeoHub" [ hostName="192.168.1.123", portNumber=4242, pollingInterval=60 ]

Thing neohub:neoplug:myhubname:mydownstairs "Downstairs Plug" @ "Hall" [ deviceNameInHub="Hall Plug" ]

Thing neohub:neostat:myhubname:myupstairs "Upstairs Thermostat" @ "Landing"  [ deviceNameInHub="Landing Thermostat" ]
```

### `demo.items` File

```
Number:Temperature Upstairs_RoomTemp "Room Temperature" { channel="neohub:neostat:myhubname:myupstairs:roomTemperature" }
Number:Temperature Upstairs_TargetTemp "Target Temperature" { channel="neohub:neostat:myhubname:myupstairs:targetTemperature" }
Number:Temperature Upstairs_FloorTemp "Floor Temperature" { channel="neohub:neostat:myhubname:myupstairs:floorTemperature" }
String Upstairs_ThermostatOutputState "Heating State" { channel="neohub:neostat:myhubname:myupstairs:outputState" }
Switch Upstairs_OccModePresent "Occupancy Mode Present" { channel="neohub:neostat:myhubname:myupstairs:myupstairs:occupancyModePresent" }

Switch Upstairs_PlugAuto "Plug Auto Mode" { channel="neohub:neoplug:myhubname:mydownstairs:autoMode" }
Switch Upstairs_PlugState "Plug Switch State" { channel="neohub:neoplug:myhubname:mydownstairs:outputState" }
```

### `demo.sitemap` File

```
sitemap neohub label="Heatmiser NeoHub"
{
	Frame label="Heating" {
		Text      item=Upstairs_RoomTemp icon="temperature" 
		Text      item=Upstairs_FloorTemp icon="temperature" 
		Setpoint  item=Upstairs_TargetTemp icon="temperature" minValue=15 maxValue=30 step=1
		Text      item=Upstairs_ThermostatOutputState icon="fire"
		Switch    item=Upstairs_OccModePresent icon="presence"
	}

	Frame label="Plug" {
		Switch item=Upstairs_PlugAuto
		Switch item=Upstairs_PlugState 	
	}
}
```

