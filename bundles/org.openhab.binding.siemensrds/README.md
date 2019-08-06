# Siemens RDS Binding

The Siemens RDS binding provides the infrastructure for connecting openHAB to the Siemens Climtix IC cloud server and integrate connected Siemens RDS Smart thermostats onto the openHAB bus.

See the Siemens web-site for product details: https://new.siemens.com/global/en/products/buildings/hvac/room-thermostats/smart-thermostat.html

## Supported Things

The binding supports two types of Thing as follows..

| Thing Type           | Description                                                                                                              |
|----------------------|--------------------------------------------------------------------------------------------------------------------------|
| Climatix IC Account  | User account on the Siemens Climatix IC cloud server (bridge) to connect with respective Smart Thermostat Things below.. |
| RDS Smart Thermostat | Siemens RDS model Smart Thermostat devices                                                                               |

## Discovery

You have to manually create a single (Bridge) Thing for the Climatix IC Account, and enter the required Configuration Parameters (see Thing Configuration for Climatix IC Account below). If the Configuration Parameters are all valid, then the Climatix IC Account Thing will automatically attempt to connect and sign on to the Siemens Climatix IC cloud server. If the sign on succeeds, the Thing will indicate its status as Online, otherwise it will show an error status. 

Note: You must create ONLY ONE Thing of the type Climatix IC Account; duplicate Climatix IC Account things risk causing communication errors with the cloud server.   

Once the Thing of the type Climatix IC Account has been created and successfully signed on to the cloud server, it will automatically interrogate the server to discover all the respective RDS Smart Thermostat Things associated with that account. After a short while, all discovered RDS Smart Thermostat Things will be displayed in the PaperUI Inbox. If in future you add new RDS Smart Thermostat devices to your Siemens account (e.g. via the Siemens App) then these new devices will also appear in the Inbox.    

Note: You must NOT manually create RDS Smart Thermostat Things; the Climatix IC Account can only connect to the cloud server for RDS Smart Thermostat devices that have been auto discovered via the process outlined above.       

## Thing Configuration for "Climatix IC Account"

The Climatix IC Account Thing connects to the Siemens Climatix IC cloud server (bridge) to communicate with any respective RDS Smart Thermostat Things associated with that account. It signs on to the cloud server using the supplied user's credentials, and it polls the server at regular intervals to read and write the data for each Smart Thermostat that is configured in that account. Before it can connect to the server, the following Configuration Parameters must be entered.   

| Configuration Parameter | Description
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| userEmail               | The e-mail address of the user account on the cloud server; as entered in the Siemens App when first registering a thermostat.                                      |
| userPassword            | The password of the user account on the cloud server; as entered in the Siemens App.                                                                                |
| pollingInterval         | Time interval in seconds between polling requests to the cloud server; the Default (recommended) interval is set a 60 seconds.                                      |
| apiKey                  | The key code needed to access the application program interface on the Siemens Climatix IC cloud server; you can request a key code from Siemens on their web-site. |

## Thing Configuration for "RDS Smart Thermostat"

The Climatix IC Account Thing connects to the Siemens Climatix IC cloud server (bridge) to communicate with any respective RDS Smart Thermostat Things associated with that account. Each such RDS Smart Thermostat Thing is identified by means of a unique Plant Id code. The Plant Id code is automatically discovered by the Climatix IC Account Thing.   

| Configuration Parameter | Description                                                                                                 | 
|-------------------------|-------------------------------------------------------------------------------------------------------------|
| plantId                 | The unique code to identify a specific RDS Smart Thermostat Thing on the Siemens Climatix IC cloud server.  |

Note: Do NOT attempt to manually change or enter the Plant Id code; the Climatix IC Account Thing can only connect to the cloud server for RDS Smart Thermostat devices having a valid Plant Id code.         

## Channels for RDS Smart Thermostat

The RDS Smart Thermostat supports several channels as shown below. 

| Channel                  | Data Type | Description                                                                 |
|--------------------------|-----------|-----------------------------------------------------------------------------|
| roomTemperature          | Number    | Actual Room Temperature                                                     |
| targetTemperature        | Number    | Target temperature setting for the room                                     |
| thermostatOutputState    | String    | Status of whether the thermostat is Heating, Cooling, or Neither            |
| roomHumidity	           | Number    | Actual Room Humidity                                                        |
| roomAirQuality           | String    | Actual Room Air Quality (Poor..Good)                                        |
| outsideTemperature       | Number    | Actual Outside temperature                                                  |
| greenLeafScore           | String    | Green Leaf Mode / Energy saving level (Poor..Excellent)                     |
| occupancyModePresent     | Switch    | The Thermostat is in the Present Occupancy Mode (Off=Absent, On=Present)    |
| thermostatAutoMode       | Switch    | The Thermostat is in Automatic Mode (Off=Manual, On=Automatic)              |
| hotWaterAutoMode         | Switch    | The Domestic Water Heating is in Automatic Mode (Off=Manual, On=Automatic)  |
| hotWaterSwitchState      | Switch    | The On/Off state of the domestic water heating                              |

## Full Example

### `demo.things` File

As a general rule it is **recommended** to create the Thing for Climatix IC Account using the PaperUI; however if you wish to do so, you *MAY* create the account thing by means of a `.things` file as follows..

```
Bridge siemensrds:climatixic:mybridgename "Climatix IC Account" [ userEmail="email@example.com", userPassword="secret", apiKey="32-character-code-provided-by-siemens", pollingInterval=60 ]
}
```

Note: it is **strongly** recommended **NOT** to create RDS Smart Thermostat Things through manual configuration files. The reason is that to configure an RDS Smart Thermostat Thing requires advance knowledge of the "Plant Id" which is a unique code used to identify a specific thermostat device in the Siemens Climatix IC cloud server account. The PaperUI automatic Discovery service (see above) discovers the unique "Plant Id" codes during the discovery process. But if you wanted to create an RDS Smart Thermostat by means of a manual configuration file you would not know its Plant Id. *Nevertheless, for completeness sake, the following shows an example of a manual configuration file, assuming that you had determined the Plant Ids by previously using the auto discovery service*..        

```
Bridge siemensrds:climatixic:mybridgename "Climatix IC Account" [ userEmail="email@example.com", userPassword="secret", apiKey="32-character-code-provided-by-siemens", pollingInterval=60 ] {
    Thing siemensrds:rds:mybridgename:mydownstairs "Downstairs Thermostat" @ "Hall" [ plantId="Pd0123456-789a-bcde-0123456789abcdef0" ]
    Thing siemensrds:rds:mybridgename:myupstairs "Upstairs Thermostat" @ "Landing" [ plantId="Pd0123456-789a-bcde-f0123456789abcdef" ]
}
```

### `demo.items` File

```
Number:Temperature Upstairs_RoomTemp "Room Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:roomTemperature" }
Number:Temperature Upstairs_TargetTemp "Target Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:targetTemperature" }
String Upstairs_HeatingOrCooling "Heating or Cooling" { channel="siemensrds:rds:mybridgename:myupstairs:thermostatOutputState" }
Number Upstairs_Humidity "Room Humidity"	{ channel="siemensrds:rds:mybridgename:myupstairs:roomHumidity" }
String Upstairs_AirQuality "Room Air Quality" { channel="siemensrds:rds:mybridgename:myupstairs:roomAirQuality" }
Number:Temperature Upstairs_OutsideTemp "Outside Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:outsideTemperature" }
String Upstairs_GreenLeafMode "Green Leaf Score" { channel="siemensrds:rds:mybridgename:myupstairs:greenLeafScore" }
Switch Upstairs_OccModePresent "Occupancy Mode Present" { channel="siemensrds:rds:mybridgename:myupstairs:occupancyModePresent" }
Switch Upstairs_ThermostatAuto "Thermostat Auto Mode" { channel="siemensrds:rds:mybridgename:myupstairs:thermostatAutoMode" }
Switch Upstairs_HotWaterAuto "Hotwater Auto Mode" { channel="siemensrds:rds:mybridgename:myupstairs:hotWaterAutoMode" }
Switch Upstairs_HotWaterState "Hotwater Switch State" { channel="siemensrds:rds:mybridgename:myupstairs:hotWaterSwitchState" }
```

### `demo.sitemap` File

```
sitemap siemensrds label="Siemens RDS"
{
	Frame label="Heating" {
		Text      item=Upstairs_RoomTemp icon="temperature" 
		Setpoint  item=Upstairs_TargetTemp icon="temperature" minValue=15 maxValue=30 step=1
		Text      item=Upstairs_HeatingOrCooling icon="fire"
		Switch    item=Upstairs_OccupancyPresent	icon="presence"
		Switch    item=Upstairs_ThermostatAuto 	
	}

	Frame label="Environment" {
		Text      item=Upstairs_Humidity icon="temperature"
		Text      item=Upstairs_OutsideTemp icon="humidity" 
		Text      item=Upstairs_AirQuality icon="qualityofservice"  
		Text      item=Upstairs_GreenLeafMode icon="qualityofservice"
	}

	Frame label="Hot Water" {
		Switch item=Upstairs_HotWaterAuto
		Switch item=Upstairs_HotWaterState 	
	}
}
```
