# Siemens RDS Binding

The Siemens RDS binding provides the infrastructure for connecting openHAB to the Siemens Climatix IC cloud server and integrate connected Siemens RDS Smart thermostats onto the openHAB bus.

See the Siemens web-site for product details: https://new.siemens.com/global/en/products/buildings/hvac/room-thermostats/smart-thermostat.html

## Supported Things

The binding supports two types of Thing as follows..

| Thing Type           | Description                                                                                                              |
|----------------------|--------------------------------------------------------------------------------------------------------------------------|
| Climatix IC Account  | User account on the Siemens Climatix IC cloud server (bridge) to connect with respective Smart Thermostat Things below.. |
| RDS Smart Thermostat | Siemens RDS model Smart Thermostat devices                                                                               |

## Discovery

You have to manually create a single (Bridge) Thing for the Climatix IC Account, and enter the required Configuration Parameters (see Thing Configuration for Climatix IC Account below).
If the Configuration Parameters are all valid, then the Climatix IC Account Thing will automatically attempt to connect and sign on to the Siemens Climatix IC cloud server.
If the sign on succeeds, the Thing will indicate its status as Online, otherwise it will show an error status. 

Once the Thing of the type Climatix IC Account has been created and successfully signed on to the cloud server, it will automatically interrogate the server to discover all the respective RDS Smart Thermostat Things associated with that account.
After a short while, all discovered RDS Smart Thermostat Things will be displayed in the PaperUI Inbox.
If in future you add new RDS Smart Thermostat devices to your Siemens account (e.g. via the Siemens App) then these new devices will also appear in the Inbox.    

## Thing Configuration for "Climatix IC Account"

The Climatix IC Account connects to the Siemens Climatix IC cloud server (bridge) to communicate with any respective RDS Smart Thermostats associated with that account.
It signs on to the cloud server using the supplied user's credentials, and it polls the server at regular intervals to read and write the data for each Smart Thermostat that is configured in that account.
Before it can connect to the server, the following Configuration Parameters must be entered.   

| Configuration Parameter | Description
|-------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| userEmail               | The e-mail address of the user account on the cloud server; as entered in the Siemens App when first registering a thermostat.                                      |
| userPassword            | The password of the user account on the cloud server; as entered in the Siemens App.                                                                                |
| pollingInterval         | Time interval in seconds between polling requests to the cloud server; the value must be between 8..60 seconds; the Default value (recommended) is 60 seconds.      |
| apiKey                  | The key code needed to access the application program interface on the Siemens Climatix IC cloud server; you can request a key code from Siemens on their web-site. |

Note: You must create ONLY ONE Thing of the type Climatix IC Account; duplicate Climatix IC Account things risk causing communication errors with the cloud server.   

## Thing Configuration for "RDS Smart Thermostat"

Each RDS Smart Thermostat Thing is identified in the Climatix IC Account by means of a unique Plant Id code.
The PaperUI automatic discovery process determines the Plant Id codes of all connected thermostats automatically.   

| Configuration Parameter | Description                                                                                                 | 
|-------------------------|-------------------------------------------------------------------------------------------------------------|
| plantId                 | The unique code to identify a specific RDS Smart Thermostat Thing on the Siemens Climatix IC cloud server.  |

## Channels for RDS Smart Thermostat

The RDS Smart Thermostat supports several channels as shown below. 

| Channel                  | Data Type          | Description                                                                 |
|--------------------------|--------------------|-----------------------------------------------------------------------------|
| roomTemperature          | Number:Temperature | Actual Room Temperature                                                     |
| targetTemperature        | Number:Temperature | Target temperature setting for the room                                     |
| thermostatOutputState    | String             | The output state of the thermostat (Heating, Off, Cooling)                  |
| roomHumidity	           | Number             | Actual Room Humidity                                                        |
| roomAirQuality           | String             | Actual Room Air Quality (Poor..Good)                                        |
| outsideTemperature       | Number:Temperature | Actual Outside temperature                                                  |
| energySavingsLevel       | String             | Energy saving level (Green Leaf score) (Poor..Excellent)                    |
| occupancyModePresent     | Switch             | The Thermostat is in the Present Occupancy Mode (Off=Absent, On=Present)    |
| thermostatAutoMode       | Switch             | The Thermostat is in Automatic Mode (Off=Manual, On=Automatic)              |
| hotWaterAutoMode         | Switch             | The Domestic Water Heating is in Automatic Mode (Off=Manual, On=Automatic)  |
| hotWaterOutputState      | Switch             | The On/Off state of the domestic water heating                              |

## Full Example

### `demo.things` File

```
Bridge siemensrds:climatixic:mybridgename "Climatix IC Account" [ userEmail="email@example.com", userPassword="secret", apiKey="32-character-code-provided-by-siemens", pollingInterval=60 ]
}
```

To manually configure an RDS Smart Thermostat Thing requires knowledge of the "Plant Id" which is a unique code used to identify a specific thermostat device in the Siemens Climatix IC cloud server account.
The PaperUI automatic Discovery service (see above) discovers the "Plant Id" codes during the discovery process.

```
Bridge siemensrds:climatixic:mybridgename "Climatix IC Account" [ userEmail="email@example.com", userPassword="secret", apiKey="32-character-code-provided-by-siemens", pollingInterval=60 ] {
    Thing siemensrds:rds:mybridgename:mydownstairs "Downstairs Thermostat" @ "Hall" [ plantId="Pd0123456-789a-bcde-0123456789abcdef0" ]
    Thing siemensrds:rds:mybridgename:myupstairs "Upstairs Thermostat" @ "Landing" [ plantId="Pd0123456-789a-bcde-f0123456789abcdef" ]
}
```

### `demo.items` File

```
Number:Temperature Upstairs_RoomTemperature "Room Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:roomTemperature" }
Number:Temperature Upstairs_TargetTemperature "Target Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:targetTemperature" }
String Upstairs_ThermostatOutputState "Thermostat Output State" { channel="siemensrds:rds:mybridgename:myupstairs:thermostatOutputState" }
Number Upstairs_RoomHumidity "Room Humidity"	{ channel="siemensrds:rds:mybridgename:myupstairs:roomHumidity" }
String Upstairs_RoomAirQuality "Room Air Quality" { channel="siemensrds:rds:mybridgename:myupstairs:roomAirQuality" }
Number:Temperature Upstairs_OutsideTemperature "Outside Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:outsideTemperature" }
String Upstairs_EnergySavingsLevel "Energy Savings Level" { channel="siemensrds:rds:mybridgename:myupstairs:energySavingsLevel" }
Switch Upstairs_OccupancModePresent "Occupancy Mode Present" { channel="siemensrds:rds:mybridgename:myupstairs:occupancyModePresent" }
Switch Upstairs_ThermostatAutoMode "Thermostat Auto Mode" { channel="siemensrds:rds:mybridgename:myupstairs:thermostatAutoMode" }
Switch Upstairs_HotWaterAutoMode "Hotwater Auto Mode" { channel="siemensrds:rds:mybridgename:myupstairs:hotWaterAutoMode" }
Switch Upstairs_HotWaterOutputState "Hotwater Output State" { channel="siemensrds:rds:mybridgename:myupstairs:hotWaterOutputState" }
```

### `demo.sitemap` File

```
sitemap siemensrds label="Siemens RDS"
{
	Frame label="Heating" {
		Text      item=Upstairs_RoomTemperature
		Setpoint  item=Upstairs_TargetTemperature minValue=15 maxValue=30 step=1
		Switch	  item=Upstairs_ThermostatAutoMode
		Switch    item=Upstairs_OccupancyModePresent
		Text      item=Upstairs_ThermostatOutputState
	}

	Frame label="Environment" {
		Text      item=Upstairs_RoomHumidity
		Text      item=Upstairs_OutsideTemperature
		Text      item=Upstairs_RoomAirQuality
		Text      item=Upstairs_EnergySavingsLevel
	}

	Frame label="Hot Water" {
		Switch item=Upstairs_HotwaterAutoMode
		Switch item=Upstairs_HotwaterOutputState
	}
}
```
