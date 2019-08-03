# Siemens RDS Binding

The Siemens RDS binding provides the infrastructure for connecting openHAB to the Siemens Climtix IC cloud server and integrate connected Siemens RDS Smart thermostats onto the openHAB bus.

See the Siemens web-site for product details: https://new.siemens.com/global/en/products/buildings/hvac/room-thermostats/smart-thermostat.html

## Supported Things

The binding supports two types of Thing as follows..

| Thing Type           | Description
|----------------------|--------------------------------------------------------------------------------------------------------------------------
| Climatix IC Account  | User account on the Siemens Climatix IC cloud server (bridge) to connect with respective Smart Thermostat Things below..  
| RDS Smart Thermostat | Siemens RDS model Smart Thermostat devices 

## Discovery

You have to manually create a single (Bridge) Thing for the Climatix IC Account, and enter the required Configuration Parameters (see Thing Configuration for Climatix IC Account below). If the Configuration Parameters are all valid, then the Climatix IC Account Thing will automatically attempt to connect and sign on to the Siemens Climatix IC cloud server. If the sign on succeeds, the Thing will indicate its status as Online, otherwise it will show an error status. 

Note: You must create ONLY ONE Thing of the type Climatix IC Account; duplicate Climatix IC Account things risk causing communication errors with the cloud server.   

Once the Thing of the type Climatix IC Account has been created and successfully signed on to the cloud server, it will automatically interrogate the server to discover all the respective RDS Smart Thermostat Things associated with that account. After a short while, all discovered RDS Smart Thermostat Things will be displayed in the PaperUI Inbox. If in future you add new RDS Smart Thermostat devices to your Siemens account (e.g. via the Siemens App) then these new devices will also appear in the Inbox.    

Note: You must NOT manually create RDS Smart Thermostat Things; the Climatix IC Account can only connect to the cloud server for RDS Smart Thermostat devices that have been auto discovered via the process outlined above.       

## Thing Configuration for Climatix IC Account

The Climatix IC Account Thing connects to the Siemens Climatix IC cloud server (bridge) to communicate with any respective RDS Smart Thermostat Things associated with that account. It signs on to the cloud server using the supplied user's credentials, and it polls the server at regular intervals to read and write the data for each Smart Thermostat that is configured in that account. Before it can connect to the server, the following Configuration Parameters must be entered.   

| Configuration Parameter | Description
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| User E-mail Address     | The e-mail address of the user account on the cloud server; as entered in the Siemens App when first registering a thermostat. |
| User Password           | The password of the user account on the cloud server; as entered in the Siemens App.                                           |
| Polling Interval        | Time interval in seconds between polling requests to the cloud server; the Default (recommended) interval is set a 60 seconds. |
| Climatix IC API Key     | The key code needed to access the Siemens Climatix IC cloud server; you can request a key code from Siemens on their web-site. |

## Thing Configuration for RDS Smart Thermostat

The Climatix IC Account Thing connects to the Siemens Climatix IC cloud server (bridge) to communicate with any respective RDS Smart Thermostat Things associated with that account. Each such RDS Smart Thermostat Thing is identified by means of a unique Plant Id code. The Plant Id code is automatically discovered by the Climatix IC Account Thing.   

| Configuration Parameter | Description                                                                                                 | 
|-------------------------|-------------------------------------------------------------------------------------------------------------|
| Plant Id                | The unique code to identify a specific RDS Smart Thermostat Thing on the Siemens Climatix IC cloud server.  |

Note: Do NOT attempt to manually change or enter the Plant Id code; the Climatix IC Account Thing can only connect to the cloud server for RDS Smart Thermostat devices having a valid Plant Id code.         

## Channels for RDS Smart Thermostat

The RDS Smart Thermostat supports several channels as shown below. 

| Channel                  | Data Type | Description                                                                 |
|--------------------------|-----------|-----------------------------------------------------------------------------|
| Room Temperature         | Number    | Actual Room Temperature                                                     |
| Target Temperature       | Number    | Target temperature setting for the room                                     |
| Heating / Cooling        | String    | Status of whether the thermostat is Heating, Off, or Cooling                |
| Room Humidity	           | Number    | Actual Room Humidity                                                        |
| Room Air Quality         | String    | Actual Room Air Quality (Poor..Good)                                        |
| Outside Temperature      | Number    | Actual Outside temperature                                                  |
| Green Leaf               | String    | Green Leaf Mode / Energy saving level (Poor..Excellent)                     |
| Occupancy Mode Present   | Switch    | The Thermostat is in the Present Occupancy Mode (Off=Absent, On=Present)    |
| T'stat Program Auto Mode | Switch    | The Thermostat is in Automatic Mode (Off=Manual, On=Automatic)              |
| DHW Program Auto Mode    | Switch    | The Domestic Water Heating is in Automatic Mode (Off=Manual, On=Automatic)  |
| DHW Switch State         | Switch    | The On/Off state of the domestic water heating                              |
