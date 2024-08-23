# Siemens RDS Binding

The Siemens RDS binding provides the infrastructure for connecting openHAB to the Siemens Climatix IC cloud server and integrate connected [Siemens RDS Smart thermostats](https://new.siemens.com/global/en/products/buildings/hvac/room-thermostats/smart-thermostat.html) onto the openHAB bus.

![Siemens RDS](doc/rds110-family.jpg)

## Supported Things

The binding supports two types of Thing as follows..

| Thing Type           | Description                                                                                                              |
| -------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| Climatix IC Account  | User account on the Siemens Climatix IC cloud server (bridge) to connect with respective Smart Thermostat Things below.. |
| RDS Smart Thermostat | Siemens RDS model Smart Thermostat devices                                                                               |

## Discovery

You have to manually create a single (Bridge) Thing for the Climatix IC Account, and enter the required Configuration Parameters (see Thing Configuration for Climatix IC Account below).
If the Configuration Parameters are all valid, then the Climatix IC Account Thing will automatically attempt to connect and sign on to the Siemens Climatix IC cloud server.
If the sign on succeeds, the Thing will indicate its status as Online, otherwise it will show an error status.

Once the Thing of the type Climatix IC Account has been created and successfully signed on to the cloud server, it will automatically interrogate the server to discover all the respective RDS Smart Thermostat Things associated with that account.
After a short while, all discovered RDS Smart Thermostat Things will be displayed in the Inbox.
If in future you add new RDS Smart Thermostat devices to your Siemens account (e.g. via the Siemens App) then these new devices will also appear in the Inbox.

## Thing Configuration for "Climatix IC Account"

The Climatix IC Account connects to the Siemens Climatix IC cloud server (bridge) to communicate with any respective RDS Smart Thermostats associated with that account.
It signs on to the cloud server using the supplied user's credentials, and it polls the server at regular intervals to read and write the data for each Smart Thermostat that is configured in that account.
Before it can connect to the server, the following Configuration Parameters must be entered.

| Configuration Parameter | Description                                                                                                                                                                      |
| ----------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| userEmail               | The e-mail address of the user account on the cloud server; as entered in the Siemens App when first registering a thermostat.                                                   |
| userPassword            | The password of the user account on the cloud server; as entered in the Siemens App.                                                                                             |
| pollingInterval         | Time interval in seconds between polling requests to the cloud server; the value must be between 8..60 seconds; the Default value (recommended) is 60 seconds.                   |
| apiKey                  | The key code needed to access the application program interface on the Siemens Climatix IC cloud server; you can request a key code from Siemens customer support <sup>1)</sup>. |

<sup>1)</sup> The Siemens Climatix IC cloud server exists primarily for supporting Original Equipment Manufacturing (OEM) customers who use the Climatix range of HVACR control products.
However the Climatix IC cloud server is also used for supporting private customers using the RDS range of residential smart thermostats.
But Siemens customer support people are often unaware of the latter fact, so when you ask them for the API key for the RDS smart thermostat range, their first reaction might often be to say you are talking nonsense!
Do not accept that answer!
You need to insist that you are requesting the Climatix IC cloud server API key _**for the RDS smart thermostat range**_ – it is a <u>different</u> key than those for OEM commercial customers.
You can also get the API key by observing the traffic between your RDS App and the server, as explained [below](#observing-the-api-key).

Note: You must create ONLY ONE Thing of the type Climatix IC Account; duplicate Climatix IC Account Things risk causing communication errors with the cloud server.

## Thing Configuration for "RDS Smart Thermostat"

Each RDS Smart Thermostat Thing is identified in the Climatix IC Account by means of a unique Plant Id code.
The automatic discovery determines the Plant Id codes of all connected thermostats automatically.

| Configuration Parameter | Description                                                                                                |
| ----------------------- | ---------------------------------------------------------------------------------------------------------- |
| plantId                 | The unique code to identify a specific RDS Smart Thermostat Thing on the Siemens Climatix IC cloud server. |

## Channels for RDS Smart Thermostat

The RDS Smart Thermostat supports several channels as shown below.

| Channel               | Data Type            | Description                                                                |
| --------------------- | -------------------- | -------------------------------------------------------------------------- |
| roomTemperature       | Number:Temperature   | Actual Room Temperature                                                    |
| targetTemperature     | Number:Temperature   | Target temperature setting for the room                                    |
| thermostatOutputState | String               | The output state of the thermostat (Heating, Off, Cooling)                 |
| roomHumidity          | Number:Dimensionless | Actual Room Humidity                                                       |
| roomAirQuality        | String               | Actual Room Air Quality (Poor..Good)                                       |
| outsideTemperature    | Number:Temperature   | Actual Outside temperature                                                 |
| energySavingsLevel    | String               | Energy saving level (Green Leaf score) (Poor..Excellent)                   |
| occupancyModePresent  | Switch               | The Thermostat is in the Present Occupancy Mode (Off=Absent, On=Present)   |
| thermostatAutoMode    | Switch               | The Thermostat is in Automatic Mode (Off=Manual, On=Automatic)             |
| hotWaterAutoMode      | Switch               | The Domestic Water Heating is in Automatic Mode (Off=Manual, On=Automatic) |
| hotWaterOutputState   | Switch               | The On/Off state of the domestic water heating                             |

## Observing the API Key

You can find your API key by observing the traffic between the RDS App on a phone/tablet and the remote ClimatixIC server.
The traffic is encrypted using SSL so regular network analyzers like [WireShark](https://www.wireshark.org/) will not work.
But you can use an interposing SSL proxy server like [Charles Proxy](https://www.charlesproxy.com/).
The general technique of using Charles Proxy to observe SSL App/server traffic is explained in this [video](https://m.youtube.com/watch?v=r7aV39-CKg4).
And specifically for this case you can examine the SSL traffic to 'api.climatixic.com', and search for the 'Ocp-Apim-Subscription-Key’ header.

## Full Example

### `demo.things` File

```java
Bridge siemensrds:climatixic:mybridgename "Climatix IC Account" [ userEmail="email@example.com", userPassword="secret", apiKey="32-character-code-provided-by-siemens", pollingInterval=60 ]
}
```

To manually configure an RDS Smart Thermostat Thing requires knowledge of the "Plant Id" which is a unique code used to identify a specific thermostat device in the Siemens Climatix IC cloud server account.

```java
Bridge siemensrds:climatixic:mybridgename "Climatix IC Account" [ userEmail="email@example.com", userPassword="secret", apiKey="32-character-code-provided-by-siemens", pollingInterval=60 ] {
    Thing rds mydownstairs "Downstairs Thermostat" @ "Hall" [ plantId="Pd0123456-789a-bcde-0123456789abcdef0" ]
    Thing rds myupstairs "Upstairs Thermostat" @ "Landing" [ plantId="Pd0123456-789a-bcde-f0123456789abcdef" ]
}
```

### `demo.items` File

```java
Number:Temperature Upstairs_RoomTemperature "Room Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:roomTemperature" }
Number:Temperature Upstairs_TargetTemperature "Target Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:targetTemperature" }
String Upstairs_ThermostatOutputState "Thermostat Output State" { channel="siemensrds:rds:mybridgename:myupstairs:thermostatOutputState" }
Number:Dimensionless Upstairs_RoomHumidity "Room Humidity" { channel="siemensrds:rds:mybridgename:myupstairs:roomHumidity" }
String Upstairs_RoomAirQuality "Room Air Quality" { channel="siemensrds:rds:mybridgename:myupstairs:roomAirQuality" }
Number:Temperature Upstairs_OutsideTemperature "Outside Temperature" { channel="siemensrds:rds:mybridgename:myupstairs:outsideTemperature" }
String Upstairs_EnergySavingsLevel "Energy Savings Level" { channel="siemensrds:rds:mybridgename:myupstairs:energySavingsLevel" }
Switch Upstairs_OccupancModePresent "Occupancy Mode Present" { channel="siemensrds:rds:mybridgename:myupstairs:occupancyModePresent" }
Switch Upstairs_ThermostatAutoMode "Thermostat Auto Mode" { channel="siemensrds:rds:mybridgename:myupstairs:thermostatAutoMode" }
Switch Upstairs_HotWaterAutoMode "Hotwater Auto Mode" { channel="siemensrds:rds:mybridgename:myupstairs:hotWaterAutoMode" }
Switch Upstairs_HotWaterOutputState "Hotwater Output State" { channel="siemensrds:rds:mybridgename:myupstairs:hotWaterOutputState" }
```

### `demo.sitemap` File

```perl
sitemap siemensrds label="Siemens RDS"
{
Frame label="Heating" {
    Text     item=Upstairs_RoomTemperature
    Setpoint item=Upstairs_TargetTemperature minValue=5 maxValue=30 step=0.5
    Switch   item=Upstairs_ThermostatAutoMode
    Switch   item=Upstairs_OccupancyModePresent
    Text     item=Upstairs_ThermostatOutputState
  }

Frame label="Environment" {
    Text item=Upstairs_RoomHumidity
    Text item=Upstairs_OutsideTemperature
    Text item=Upstairs_RoomAirQuality
    Text item=Upstairs_EnergySavingsLevel
  }

Frame label="Hot Water" {
    Switch item=Upstairs_HotwaterAutoMode
    Switch item=Upstairs_HotwaterOutputState
  }
}
```
