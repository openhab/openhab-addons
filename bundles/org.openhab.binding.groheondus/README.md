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
You can either use your username and password combination for logging in into your GROHE account, in which case both parameters, `username` as well as `password`, are required arguments and refer to the same login credentials you used during setting up your GROHE account or while logging into the app.
Alternatively you can use a so called `refresh token` to grant openHAB access to your account without having to share your credentials with the system.
For that you need to obtain such `refresh token` from the GROHE ONDUS Api (see more on that below) and paste this string into the respective input field on the account management page you can reach from `http://<your-openHAB-domain-and-port>/groheondus`.
On this site you can also delete a previously saved `refresh token`.
The GROHE ONDUS binding also refreshes this refresh token in order to ensure that you stay logged in.

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

## Obtaining a `refresh token`

Actually obtaining a `refresh token` from the GROHE ONDUS Api requires some manual steps.
In order to more deeply understand what is happening during the process, you can read more information about the OAuth2/OIDC (OpenID Connect) login flow by searching for these terms in your favorite search engine.
Here's a short step-by-step guide on how to obtain a refresh token:

1. Open a new tab in your Internet browser
2. Open the developer console of your browser (mostly possible by pressing F12)
3. Select the network tab of the developer console (which shows you the network request done by the browser)
4. Open the following URL: https://idp2-apigw.cloud.grohe.com/v3/iot/oidc/login
5. You will automatically being redirected to the GROHE ONDUS login page, login there
6. After logging in successfully, nothing should happen, except a failed request to a page starting with `token?`
7. Click on this request (the URL in the request overview should start with `ondus://idp2-apigw.cloud.grohe.com/v3/iot/oidc/token?` or something like that
8. Copy the whole request URL (which should contain a lot of stuff, like a `state` parameter and so on)
9. Open a new tab in your Internet browser and paste the URL into the address bar (do not hit ENTER or start the navigation to this page, yet)
10. Replace the `ondus://` part of the URL with `https://` and hit ENTER
11. The response of the page should be plain text with a so called `JSON object`. Somewhere in the text should be a `refresh_token` string, select the string after this `refresh_token` text, which is encapsulated with `"`.

E.g.: If the response of the page looks like this:

````
{
    "access_token": "the_access_token",
    "expires_in":3600,
    "refresh_expires_in":15552000,
    "refresh_token":"the_refresh_token",
    "token_type":"bearer",
    "id_token":"the_id_token",
    "not-before-policy":0,
    "session_state":"a-state",
    "scope":"",
    "tandc_accepted":true,
    "partialLogin":false
}
````

Then the `refresh_token` value you should copy would be: `the_refresh_token`.
This value is the `refresh token` you should save as described above.
