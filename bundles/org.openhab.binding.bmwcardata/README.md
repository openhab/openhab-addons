# BMW CarData Binding

This binding integrates BMW and MINI vehicles via BMW CarData, the official service of the BMW Group that provides the telematic data of a vehicle to its owner.

The binding receives the data in two ways:

- **Streaming:** The CarData streaming service pushes the data of all vehicles of the account as soon as a vehicle reports it.
  This is the preferred way to keep the data up to date.
- **Polling:** The CarData API is polled periodically for the current values of all channels.
  The API allows 50 requests per day for all vehicles of an account, so polling is meant as a fallback with a long interval.

BMW CarData provides read-only access, the vehicles cannot be controlled with this binding.
BMW CarData is currently only available in some regions; check the CarData section of the MyBMW or MINI customer portal of your country.

## Supported Things

- `account`: Bridge representing the BMW CarData client of a customer account.
- `vehicle`: A vehicle mapped to the account, identified by its VIN.

## Prerequisites

Before the binding can be used, a CarData client has to be created in the customer portal:

1. Log in to the MyBMW or MINI customer portal and open the vehicle overview.
1. Open the BMW CarData section and create a client ID.
1. Subscribe the client to both **CarData API** and **CarData Streaming**.
1. In the streaming configuration of the portal, select the data that shall be streamed.
   Only the data selected there is pushed by the stream.

## Discovery

After the account has been authorized, the vehicles mapped to the account as primary user are discovered.
A discovery scan costs one request of the daily API quota, so it only runs automatically after authorization while the account has no vehicle Things.
Further scans can be started manually from the Inbox.

## Thing Configuration

### `account` Bridge Configuration

| Name      | Type    | Description                                                      | Default | Required | Advanced |
|-----------|---------|------------------------------------------------------------------|---------|----------|----------|
| clientId  | text    | The client ID created in the CarData section of the portal       | N/A     | yes      | no       |
| streaming | boolean | Receive the data pushed by the CarData streaming service         | true    | no       | no       |

#### Authorization

The binding is authorized with the OAuth device flow:

1. Add the `account` bridge with the client ID.
1. The bridge goes `OFFLINE (CONFIGURATION_PENDING)` and its status description shows a link and a user code.
1. Open the link, log in with your BMW or MINI account and enter the user code.
1. The bridge goes `ONLINE` within a few seconds.

The authorization is stored and renewed by the binding.
If it expires or is revoked, the bridge shows a new link and user code.

### `vehicle` Thing Configuration

| Name            | Type    | Description                                                            | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------------|---------|----------|----------|
| vin             | text    | The 17 character vehicle identification number                         | N/A     | yes      | no       |
| pollingInterval | integer | Interval in minutes to poll the CarData API, `0` disables polling      | 120     | no       | yes      |
| dynamicChannels | boolean | Create channels for received data without predefined channel           | true    | no       | yes      |

Each poll costs one API request per vehicle.
In addition, the static vehicle data shown as Thing properties is retrieved once, and the binding sets up its own telematic data container named `openHAB` on first use, which costs a few requests.
Make sure that the polls of all vehicles stay well below the daily quota of 50 requests.

## Channels

All channels are read-only.
A channel is only updated if the vehicle reports the corresponding data and, for streaming, if the data has been selected in the customer portal.

| Channel                              | Type                     | Description                                          | Advanced |
|--------------------------------------|--------------------------|------------------------------------------------------|----------|
| status#door-lock                     | String                   | Lock state of the doors                              | no       |
| status#doors                         | String                   | Overall state of the doors                           | no       |
| status#trunk                         | Contact                  | Whether the trunk is open                            | no       |
| status#hood                          | Contact                  | Whether the hood is open                             | no       |
| status#trunk-locked                  | Switch                   | Whether the trunk is locked                          | no       |
| status#door-front-driver             | Contact                  | Whether the front driver door is open                | no       |
| status#door-front-passenger          | Contact                  | Whether the front passenger door is open             | no       |
| status#door-rear-driver              | Contact                  | Whether the rear driver side door is open            | no       |
| status#door-rear-passenger           | Contact                  | Whether the rear passenger side door is open         | no       |
| status#window-front-driver           | String                   | State of the front driver window                     | no       |
| status#window-front-passenger        | String                   | State of the front passenger window                  | no       |
| status#window-rear-driver            | String                   | State of the rear driver side window                 | no       |
| status#window-rear-passenger         | String                   | State of the rear passenger side window              | no       |
| status#rear-window                   | Contact                  | Whether the rear window is open                      | no       |
| status#sunroof                       | String                   | Overall state of the sunroof                         | no       |
| status#moving                        | Switch                   | Whether the vehicle is moving                        | no       |
| status#ignition                      | Switch                   | Whether the ignition is on                           | yes      |
| status#engine                        | Switch                   | Whether the engine is running                        | no       |
| status#battery-voltage               | Number:ElectricPotential | Voltage of the 12V battery                           | yes      |
| status#battery-soc                   | Number:Dimensionless     | State of charge of the 12V battery                   | yes      |
| status#last-update                   | DateTime                 | Time of the most recent data of the vehicle          | no       |
| range#mileage                        | Number:Length            | Total distance travelled                             | no       |
| range#total                          | Number:Length            | Total remaining range                                | no       |
| range#electric                       | Number:Length            | Remaining electric range                             | no       |
| range#fuel-level                     | Number:Dimensionless     | Fill level of the fuel tank                          | no       |
| range#remaining-fuel                 | Number:Volume            | Amount of fuel in the tank                           | no       |
| charge#soc                           | Number:Dimensionless     | State of charge of the high-voltage battery          | no       |
| charge#target-soc                    | Number:Dimensionless     | State of charge at which charging stops              | no       |
| charge#status                        | String                   | Charging state                                       | no       |
| charge#plugged                       | Switch                   | Whether a charging cable is plugged in               | no       |
| charge#power                         | Number:Power             | Current charging power                               | no       |
| charge#time-remaining                | Number:Time              | Estimated time until charging is completed           | no       |
| charge#ac-current                    | Number:ElectricCurrent   | Current AC charging current                          | yes      |
| charge#ac-voltage                    | Number:ElectricPotential | Current AC charging voltage                          | yes      |
| charge#ac-limit                      | Number:ElectricCurrent   | Selected limit of the AC charging current            | yes      |
| charge#phases                        | String                   | Number of phases used for charging                   | yes      |
| charge#method                        | String                   | Charging method and plug type                        | yes      |
| charge#dc-port                       | String                   | Whether a DC charging plug is connected              | yes      |
| charge#predicted-soc                 | Number:Dimensionless     | Predicted state of charge at the end of charging     | yes      |
| charge#min-target-soc                | Number:Dimensionless     | Minimum state of charge requested for smart charging | yes      |
| charge#time-to-full                  | Number:Time              | Estimated time until the battery is fully charged    | yes      |
| charge#mode                          | String                   | Current charging mode                                | yes      |
| charge#end-reason                    | String                   | Reason why the last charging ended                   | yes      |
| charge#last-result                   | String                   | Result of the last charging                          | yes      |
| charge#cable-lock                    | String                   | Lock state of the charging cable                     | yes      |
| charge#flap                          | Contact                  | Whether the charging flap is open                    | yes      |
| battery#capacity                     | Number:Energy            | Size of the high-voltage battery                     | yes      |
| battery#energy-content               | Number:Energy            | Maximum energy content of the high-voltage battery   | yes      |
| battery#health                       | Number                   | Battery health as state of certified energy (SoCE)   | no       |
| battery#energy-to-full               | Number:Energy            | Energy required to fully charge the battery          | no       |
| climate#comfort-state                | String                   | State of the cabin preconditioning                   | no       |
| climate#progress                     | Number:Dimensionless     | Progress of the active preconditioning               | yes      |
| climate#remaining-running-time       | Number:Time              | Remaining running time of the preconditioning        | no       |
| climate#target-temperature           | Number:Temperature       | Target cabin temperature of the preconditioning      | no       |
| climate#rear-defrost                 | Switch                   | Whether the rear window heating is active            | yes      |
| climate#mirror-heating               | Switch                   | Whether the exterior mirror heating is active        | yes      |
| climate#air-purification             | String                   | State of the cabin air purification                  | yes      |
| climate#auxiliary-activity           | String                   | Activity of the auxiliary climate control            | no       |
| climate#auxiliary-remaining-time     | Number:Time              | Remaining running time of the auxiliary climate      | no       |
| climate#auxiliary-error              | String                   | Reason why the auxiliary climate did not run         | yes      |
| navigation#gps                       | Location                 | Current location of the vehicle                      | no       |
| navigation#heading                   | Number:Angle             | Compass heading of the vehicle                       | yes      |
| navigation#altitude                  | Number:Length            | Altitude of the vehicle above sea level              | yes      |
| navigation#gps-fix                   | String                   | Quality of the GPS position                          | yes      |
| navigation#destination-distance      | Number:Length            | Distance to the active navigation destination        | no       |
| navigation#arrival-time              | String                   | Expected arrival time at the destination (hh:mm)     | no       |
| trip#end-time                        | DateTime                 | Time when the last trip ended                        | no       |
| trip#end-mileage                     | Number:Length            | Total mileage at the end of the last trip            | yes      |
| trip#end-soc                         | Number:Dimensionless     | State of charge at the end of the last trip          | no       |
| trip#recuperation                    | Number                   | Energy recovered during the last trip in kWh/100km   | no       |
| trip#comfort-energy                  | Number:Energy            | Energy consumed in COMFORT mode during the last trip | yes      |
| trip#electric-share                  | Number:Dimensionless     | Share of the distance driven electrically            | yes      |
| trip#eco-pro-share                   | Number:Dimensionless     | Share of the trip driven in ECO PRO mode             | yes      |
| trip#eco-pro-plus-share              | Number:Dimensionless     | Share of the trip driven in ECO PRO+ mode            | yes      |
| trip#acceleration-rating             | Number                   | Rating of the acceleration behaviour (0-5 stars)     | yes      |
| trip#anticipation-rating             | Number                   | Rating of the anticipatory driving (0-5 stars)       | yes      |
| tyres#pressure-front-left            | Number:Pressure          | Measured pressure of the front left tyre             | no       |
| tyres#pressure-front-right           | Number:Pressure          | Measured pressure of the front right tyre            | no       |
| tyres#pressure-rear-left             | Number:Pressure          | Measured pressure of the rear left tyre              | no       |
| tyres#pressure-rear-right            | Number:Pressure          | Measured pressure of the rear right tyre             | no       |
| tyres#target-front-left              | Number:Pressure          | Target pressure of the front left tyre               | yes      |
| tyres#target-front-right             | Number:Pressure          | Target pressure of the front right tyre              | yes      |
| tyres#target-rear-left               | Number:Pressure          | Target pressure of the rear left tyre                | yes      |
| tyres#target-rear-right              | Number:Pressure          | Target pressure of the rear right tyre               | yes      |
| tyres#temperature-front-left         | Number:Temperature       | Temperature of the front left tyre                   | yes      |
| tyres#temperature-front-right        | Number:Temperature       | Temperature of the front right tyre                  | yes      |
| tyres#temperature-rear-left          | Number:Temperature       | Temperature of the rear left tyre                    | yes      |
| tyres#temperature-rear-right         | Number:Temperature       | Temperature of the rear right tyre                   | yes      |
| security#alarm                       | Switch                   | Whether the anti-theft alarm is going off            | no       |
| security#arm-status                  | String                   | Arming state of the anti-theft alarm system          | no       |
| security#activation-time             | String                   | Time of the last alarm as reported by the vehicle    | yes      |
| service#distance                     | Number:Length            | Remaining distance until the next service            | no       |
| service#inspection-date              | DateTime                 | Date of the next legal inspection                    | no       |
| statistics#grid-energy               | Number:Energy            | Total energy charged from the grid                   | yes      |
| statistics#fuel-consumed             | Number:Volume            | Total fuel consumed                                  | yes      |
| statistics#avg-consumption           | Number                   | Average electric consumption in kWh/100km            | yes      |
| statistics#avg-speed                 | Number:Speed             | Average speed                                        | yes      |
| statistics#weekly-distance           | Number:Length            | Average distance driven per week                     | yes      |
| statistics#weekly-distance-long-term | Number:Length            | Average distance per week over two months            | yes      |

The String channels show the values as reported by the vehicle.
Values the vehicle reports as unavailable (`INVALID`, `-NA-` or `UNKNOWN`) set the channel to `UNDEF`.

### Dynamic Channels

BMW CarData provides several hundred data points, of which only the most common ones have a predefined channel.
If `dynamicChannels` is enabled, the binding adds a channel for every other data point it receives, typically data selected for the stream in the customer portal.

- The channels are created in the channel group `other`.
- The channel ID is the CarData identifier without the `vehicle.` prefix in lower-case-hyphen notation, e.g. `vehicle.cabin.sunroof.tiltStatus` becomes `other#cabin-sunroof-tilt-status`.
- The item type is derived from the first received value: `Switch` for booleans, `Number` (with unit, e.g. `Number:Length`, if the data has one) for numbers and `String` otherwise.
- The channel is created when the first value is received, so it does not exist before the vehicle reported the data.

For Things defined in `.things` files, dynamic channels are not stored and are created again when the data is received after a restart.

## Full Example

### `bmwcardata.things` Example

```java
Bridge bmwcardata:account:myaccount "BMW CarData" [ clientId="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" ] {
    Thing vehicle i4 "BMW i4" [ vin="WBA00000000000000", pollingInterval=180 ]
}
```

### `bmwcardata.items` Example

```java
Number:Length        i4_Mileage   "Mileage [%d %unit%]"           { channel="bmwcardata:vehicle:myaccount:i4:range#mileage" }
Number:Length        i4_Range     "Range [%d %unit%]"             { channel="bmwcardata:vehicle:myaccount:i4:range#electric" }
Number:Dimensionless i4_Soc       "State of Charge [%d %%]"       { channel="bmwcardata:vehicle:myaccount:i4:charge#soc" }
String               i4_Charging  "Charging [%s]"                 { channel="bmwcardata:vehicle:myaccount:i4:charge#status" }
String               i4_DoorLock  "Doors [%s]"                    { channel="bmwcardata:vehicle:myaccount:i4:status#door-lock" }
Location             i4_Location  "Location"                      { channel="bmwcardata:vehicle:myaccount:i4:navigation#gps" }
DateTime             i4_Updated   "Last Update [%1$tF %1$tR]"     { channel="bmwcardata:vehicle:myaccount:i4:status#last-update" }
```
