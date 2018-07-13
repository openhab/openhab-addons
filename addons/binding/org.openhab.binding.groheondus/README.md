# GROHE ONDUS Binding

The GROHE ONDUS Binding provides access to data collected by a GROHE ONDUS appliance, such as an (GROHE Sense Guard)[https://www.grohe.de/de_de/smarthome/grohe-sense-guard/].
The binding uses the REST API interface (the same as used by the Android App) to retrieve the collected data.

## Supported Things

This binding should support all appliances from GROHE, however, only the GROHE Sense Guard is tested with it.

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


## Full Example

Things file:

````
Bridge groheondus:account:account1 [ username="user@example.com", password="YourStrongPasswordHere!" ] {
    groheondus:appliance:550e8400-e29b-11d4-a716-446655440000 [ applianceId="550e8400-e29b-11d4-a716-446655440000", roomId=456, locationId=123 ]
}
````

Items file:

````
String Name "Appliance Name" {channel="groheondus:appliance:groheondus:appliance:550e8400-e29b-11d4-a716-446655440000:name"}
Number:Pressure Pressure "Pressure [%.1f %unit%]" {channel="groheondus:appliance:groheondus:appliance:550e8400-e29b-11d4-a716-446655440000:pressure"}
Number:Temperature Temperature "Temperature [%.1f %unit%]" {channel="groheondus:appliance:groheondus:appliance:550e8400-e29b-11d4-a716-446655440000:temperature"}
````

