# GROHE ONDUS Binding

The GROHE ONDUS Binding provides access to data collected by a GROHE ONDUS appliance, such as an [GROHE Sense Guard](https://www.grohe.de/de_de/smarthome/grohe-sense-guard/).
The binding uses the REST API interface (the same as used by the Android App) to retrieve the collected data.

## Supported Things

This binding should support all appliances from GROHE, however, only the GROHE Sense Guard is tested with it.

| Thing type               | Name                     |
|--------------------------|--------------------------|
| account                  | GROHE ONDUS Account      |
| senseguard               | GROHE SENSE Guard device |
| sense                    | GROHE SENSE device       |

## Discovery

The binding requires you to create at least one Account thing as a bridge manually.
The discovery process will look through all locations and rooms of your configured GROHE account and adds each found appliance as a new thing automatically to the inbox.

## Binding Configuration

This binding does not require any configuration outside of things.

## Thing Configuration

There's only one thing and one bridge that needs to be configured together to get this binding to work, see the full example section for a self-explaining example.

### Account Bridge

The `groheondus:account` bridge is used to configure the API interface for a specific account, which is used to access the collected and saved data of your GROHE account.
Both parameters, `username` as well as `password`, are required arguments and refer to the same login credentials you used during setting up your GROHE account or while logging into the app.

### Appliance

The `groheondus:sense` and `groheondus:senseguard` things are used to retrieve information of a specific appliance from GROHE.
This appliance needs to be connected with your GROHE ONDUS account as configured in the corresponding Account Bridge.
The appliance needs to be configured with the unique appliance ID (with the `applianceId` configuration) as well as the `roomId`
and the `locationId`. Once the account bridge is configured, the appliances in your account will be discovered as Appliance things.

| Configuration            | Default value            | Description                                           |
|--------------------------|--------------------------|-------------------------------------------------------|
| applianceId              | ''                       | Unique ID of the appliance in the GROHE ONDUS account |
| roomId                   | ''                       | ID of the room the appliance is in                    |
| locationId               | ''                       | ID of the location (building) the appliance is in     |
| pollingInterval          | Retrieved from API,      | Interval in seconds to get new data from the API      |
|                          | usually 900              | The `sense` thing uses 900 by default               |

#### Channels

##### senseguard

| Channel                  | Type                     | Description                                           |
|--------------------------|--------------------------|-------------------------------------------------------|
| name                     | String                   | The name of the appliance                             |
| pressure                 | Number:Pressure          | The pressure of your water supply                     |
| temperature_guard        | Number:Temperature       | The ambient temperature of the appliance              |
| valve_open               | Switch                   | Valve switch                                          |
| waterconsumption         | Number                   | The amount of water used in a specific timeframe      |

##### sense

| Channel                  | Type                     | Description                                           |
|--------------------------|--------------------------|-------------------------------------------------------|
| name                     | String                   | The name of the appliance                             |
| humidity                 | Number:Dimensionless     | The humidity measured by the appliance                |
| temperature              | Number:Temperature       | The ambient temperature of the appliance              |
| battery                  | Number                   | The battery level of the appliance                    |

## Full Example

Things file:

````
Bridge groheondus:account:account1 [ username="user@example.com", password="YourStrongPasswordHere!" ] {
    groheondus:senseguard:550e8400-e29b-11d4-a716-446655440000 [ applianceId="550e8400-e29b-11d4-a716-446655440000", roomId=456, locationId=123 ] {
        Channels:
            Type number : waterconsumption [
               timeframe=3
            ]
    }
    groheondus:sense:550e8400-e29b-11d4-a716-446655440000 [ applianceId="444e8400-e29b-11d4-a716-446655440000", roomId=456, locationId=123 ]
}
````

Items file:

````
String Name_Sense_Guard "Appliance Name" {channel="groheondus:senseguard:groheondus:appliance:550e8400-e29b-11d4-a716-446655440000:name"}
Number:Pressure Pressure_Sense_Guard "Pressure [%.1f %unit%]" {channel="groheondus:senseguard:groheondus:appliance:550e8400-e29b-11d4-a716-446655440000:pressure"}
Number:Temperature Temperature_Sense_Guard "Temperature [%.1f %unit%]" {channel="groheondus:senseguard:groheondus:appliance:550e8400-e29b-11d4-a716-446655440000:temperature_guard"}

String Name_Sense "Temperature [%.1f %unit%]" {channel="groheondus:sense:groheondus:appliance:444e8400-e29b-11d4-a716-446655440000:name"}
Number:Temperature Temperature_Sense "Temperature [%.1f %unit%]" {channel="groheondus:sense:groheondus:appliance:444e8400-e29b-11d4-a716-446655440000:temperature"}
Number Humidity_Sense "Humidity [%.1f %unit%]" {channel="groheondus:sense:groheondus:appliance:444e8400-e29b-11d4-a716-446655440000:humidity"}
````

