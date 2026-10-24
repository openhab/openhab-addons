# BMW CarData Binding

This binding integrates BMW and MINI vehicles via BMW CarData, the official service of the BMW Group that provides the telematic data of a vehicle to its owner.

The binding receives the data in two ways:

- **Streaming:** The CarData streaming service pushes the data of all vehicles of the account as soon as a vehicle reports it.
  This is the preferred way to keep the data up to date.
- **API requests:** The CarData API is read periodically and after stream outages for the current values of all channels.
  The API allows 50 requests per day for all vehicles of an account, so the binding uses it sparingly, see [API Requests](#api-requests).

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
   Request the access to the CarData API explicitly (German portal: "Zugang zur CarData API beantragen"); enabling streaming alone is not sufficient.
   Enable one subscription at a time and check after a minute that both are still active, as the portal may reset them.
1. In the streaming configuration of the portal, select the data that shall be streamed.
   Only the data selected there is pushed by the stream.

If the account reports `Device authorization request failed: invalid_request`, BMW does not accept the client ID: check the client ID and that both subscriptions are active.

## Discovery

After the account has been authorized, the vehicles mapped to the account as primary user are discovered.
A discovery scan costs one request of the daily API quota, so it only runs automatically after authorization while the account has no vehicle Things.
Discovered vehicles are labeled with their model, which costs one more request per vehicle once; a vehicle Thing created from the result reuses that data.
Further scans can be started manually from the Inbox.

## Thing Configuration

### `account` Bridge Configuration

| Name            | Type    | Description                                                                                             | Default | Required | Advanced |
|-----------------|---------|---------------------------------------------------------------------------------------------------------|---------|----------|----------|
| clientId        | text    | The client ID created in the CarData section of the portal                                              | N/A     | yes      | no       |
| streaming       | boolean | Receive the data pushed by the CarData streaming service                                                | true    | no       | no       |
| streamMode      | text    | `direct` connects to BMW, `bridge` receives the stream from the bmw-mqtt-bridge                         | direct  | no       | no       |
| mirrorToBroker  | boolean | Republish the direct stream to the local MQTT broker, see [Mirroring the Stream](#mirroring-the-stream) | false   | no       | yes      |
| brokerHost      | text    | Host of the local MQTT broker, required in bridge mode and for mirroring                                | N/A     | no       | yes      |
| brokerPort      | integer | Port of the local MQTT broker                                                                           | 1883    | no       | yes      |
| brokerSecure    | boolean | Connect to the local MQTT broker with TLS                                                               | false   | no       | yes      |
| brokerUsername  | text    | User name for the local MQTT broker                                                                     | N/A     | no       | yes      |
| brokerPassword  | text    | Password for the local MQTT broker                                                                      | N/A     | no       | yes      |
| topicPrefix     | text    | Prefix of the bridge topics (`LOCAL_PREFIX` of the bridge)                                              | bmw/    | no       | yes      |
| refreshInterval | integer | Hours between refreshes of all vehicle data via the API, `0` disables them                              | 6       | no       | no       |
| requestBudget   | integer | Maximum number of API requests in 24 hours (1 to 50)                                                    | 40      | no       | yes      |

The broker parameters are only used in bridge mode and for mirroring.

#### Stream Modes

BMW allows only one connection to the CarData stream per account.
A second connection with the same account, e.g. from another smart home system, MQTT Explorer or evcc, disconnects the first one, so both keep reconnecting.

- **Direct** (`streamMode=direct`): The binding connects to the BMW streaming service itself.
  This needs no additional software, but no other tool can use the stream of the account at the same time.
- **Bridge** (`streamMode=bridge`): The [bmw-mqtt-bridge](https://github.com/dj0abr/bmw-mqtt-bridge) holds the connection to BMW and republishes the unmodified stream messages to a local MQTT broker, on `<prefix>raw/<VIN>`, and its connection state on `<prefix>status`.
  The binding subscribes to these topics and does not connect to the BMW streaming service, but still uses the CarData API with its own authorization.
  Other tools, e.g. evcc, can read the same topics, so all of them share the single stream connection.

#### Mirroring the Stream

In direct mode, openHAB holds the only stream connection of the account.
With `mirrorToBroker=true`, the binding republishes the stream to the local MQTT broker configured by the broker parameters, so that other tools, e.g. evcc, can use it without running the bmw-mqtt-bridge.
The topics are those of the bmw-mqtt-bridge, so existing consumers of the bridge, and this binding in bridge mode, can read the mirror unchanged:

| Topic               | Payload                                                                                    | Retained |
|---------------------|--------------------------------------------------------------------------------------------|----------|
| `<prefix>raw/<VIN>` | Every stream message, unmodified as received from BMW                                      | yes      |
| `<prefix>status`    | State of the BMW stream connection: `{"connected":true,"timestamp":<seconds since epoch>}` | yes      |

The last will of the mirror sets `<prefix>status` to `{"connected":false}`, so consumers notice when openHAB stops.
The mirror reconnects to the local broker on its own; while the broker is not reachable, messages are dropped and the status description of the account shows this, but the stream and the channels are not affected.
Mirroring is ignored in bridge mode, as the bridge already publishes the stream.

While openHAB is down or restarting, consumers of the mirror receive no data.
If other tools need the stream independent of openHAB, run the bmw-mqtt-bridge and use bridge mode instead.

#### API Requests

BMW allows 50 requests to the CarData API per account in 24 hours, shared by all vehicles and all tools using the account.
The stream is not affected by this limit, so the binding uses the API only to complete the stream data:

- Two minutes after the authorization, and then every `refreshInterval` hours, the current values of all vehicles are read, one request per vehicle.
- When the stream was interrupted for five minutes or more, the values are read again after the reconnect, as the stream does not replay missed values.
  This catch-up runs at most every 30 minutes.
- When a read fails on BMW's side (e.g. HTTP 502) or times out, it is repeated once after 15 minutes for the affected vehicles.
  While the stream is connected, such a failure does not set the vehicle `OFFLINE`, as the stream keeps its values up to date.
- The static vehicle data shown as Thing properties is read once per vehicle, and the binding sets up its own telematic data container named `openHAB` on first use, which costs a few requests.

The binding counts its requests over a rolling 24 hours per client ID, also across restarts and when the account thing is recreated, and sends at most `requestBudget` requests.
When the budget is used up, or BMW reports its limit as reached, the binding pauses its requests and shows this in the status description of the account; the stream keeps working.
Keep a reserve below 50 if other tools use the same account.

#### Authorization

The binding is authorized with the OAuth device flow:

1. Add the `account` bridge with the client ID.
1. The bridge goes `OFFLINE (CONFIGURATION_PENDING)` and its status description shows a link and a user code.
1. Open the link, log in with your BMW or MINI account and enter the user code.
1. The bridge goes `ONLINE` within a few seconds.

The tokens are stored encrypted by openHAB's OAuth client service and renewed automatically.
If it expires or is revoked, the bridge shows a new link and user code.

### `vehicle` Thing Configuration

| Name            | Type    | Description                                                            | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------------|---------|----------|----------|
| vin             | text    | The 17 character vehicle identification number                         | N/A     | yes      | no       |
| dynamicChannels | boolean | Create channels for received data without predefined channel           | true    | no       | yes      |

## Channels

All channels are read-only.
A channel is only updated if the vehicle reports the corresponding data and, for streaming, if the data has been selected in the customer portal.

| Channel                              | Type                     | Description                                                                   | Advanced |
|--------------------------------------|--------------------------|-------------------------------------------------------------------------------|----------|
| status#door-lock                     | String                   | Lock state of the doors                                                       | no       |
| status#trunk                         | Contact                  | Whether the trunk is open                                                     | no       |
| status#hood                          | Contact                  | Whether the hood is open                                                      | no       |
| status#trunk-locked                  | Switch                   | Whether the trunk is locked                                                   | no       |
| status#door-front-driver             | Contact                  | Whether the front driver door is open                                         | no       |
| status#door-front-passenger          | Contact                  | Whether the front passenger door is open                                      | no       |
| status#door-rear-driver              | Contact                  | Whether the rear driver side door is open                                     | no       |
| status#door-rear-passenger           | Contact                  | Whether the rear passenger side door is open                                  | no       |
| status#window-front-driver           | Contact                  | Whether the front driver window is open, also if partly open                  | no       |
| status#window-front-driver-state     | String                   | Reported state of the front driver window (CLOSED, INTERMEDIATE, OPEN)        | yes      |
| status#window-front-passenger        | Contact                  | Whether the front passenger window is open, also if partly open               | no       |
| status#window-front-passenger-state  | String                   | Reported state of the front passenger window (CLOSED, INTERMEDIATE, OPEN)     | yes      |
| status#window-rear-driver            | Contact                  | Whether the rear driver side window is open, also if partly open              | no       |
| status#window-rear-driver-state      | String                   | Reported state of the rear driver side window (CLOSED, INTERMEDIATE, OPEN)    | yes      |
| status#window-rear-passenger         | Contact                  | Whether the rear passenger side window is open, also if partly open           | no       |
| status#window-rear-passenger-state   | String                   | Reported state of the rear passenger side window (CLOSED, INTERMEDIATE, OPEN) | yes      |
| status#rear-window                   | Contact                  | Whether the rear window is open                                               | no       |
| status#sunroof                       | Contact                  | Whether the sunroof is open or tilted                                         | no       |
| status#sunroof-state                 | String                   | Reported overall state of the sunroof                                         | yes      |
| status#moving                        | Switch                   | Whether the vehicle is moving                                                 | no       |
| status#ignition                      | Switch                   | Whether the ignition is on                                                    | yes      |
| status#engine                        | Switch                   | Whether the engine is running                                                 | no       |
| status#deep-sleep                    | Switch                   | Whether the deep sleep mode is active                                         | yes      |
| status#battery-voltage               | Number:ElectricPotential | Voltage of the 12V battery                                                    | yes      |
| status#battery-soc                   | Number:Dimensionless     | State of charge of the 12V battery                                            | yes      |
| status#last-update                   | DateTime                 | Time of the most recent data of the vehicle                                   | no       |
| range#mileage                        | Number:Length            | Total distance travelled                                                      | no       |
| range#total                          | Number:Length            | Total remaining range                                                         | no       |
| range#electric                       | Number:Length            | Remaining electric range                                                      | no       |
| range#electric-charging-forecast     | Number:Length            | Predicted electric range during charging                                      | yes      |
| range#fuel-level                     | Number:Dimensionless     | Fill level of the fuel tank                                                   | no       |
| range#remaining-fuel                 | Number:Volume            | Amount of fuel in the tank                                                    | no       |
| charge#soc                           | Number:Dimensionless     | State of charge of the high-voltage battery                                   | no       |
| charge#target-soc                    | Number:Dimensionless     | State of charge at which charging stops                                       | no       |
| charge#status                        | String                   | Charging state                                                                | no       |
| charge#plugged                       | Switch                   | Whether a charging cable is plugged in (see below)                            | no       |
| charge#power                         | Number:Power             | Current charging power                                                        | no       |
| charge#time-remaining                | Number:Time              | Estimated time until charging is completed                                    | no       |
| charge#ac-current                    | Number:ElectricCurrent   | Current AC charging current                                                   | yes      |
| charge#ac-voltage                    | Number:ElectricPotential | Current AC charging voltage                                                   | yes      |
| charge#ac-limit                      | Number:ElectricCurrent   | Selected limit of the AC charging current                                     | yes      |
| charge#phases                        | String                   | Number of phases used for charging                                            | yes      |
| charge#method                        | String                   | Charging method and plug type                                                 | yes      |
| charge#port                          | String                   | Whether a charging plug is connected to the charging port                     | yes      |
| charge#predicted-soc                 | Number:Dimensionless     | Predicted state of charge at the end of charging                              | yes      |
| charge#min-target-soc                | Number:Dimensionless     | Minimum state of charge requested for smart charging                          | yes      |
| charge#time-to-full                  | Number:Time              | Estimated time until the battery is fully charged                             | yes      |
| charge#mode                          | String                   | Current charging mode                                                         | yes      |
| charge#end-reason                    | String                   | Reason why the last charging ended                                            | yes      |
| charge#last-result                   | String                   | Result of the last charging                                                   | yes      |
| charge#cable-lock                    | String                   | Lock state of the charging cable                                              | yes      |
| charge#flap                          | Contact                  | Whether the charging flap is open                                             | yes      |
| battery#capacity                     | Number:Energy            | Size of the high-voltage battery                                              | yes      |
| battery#energy-content               | Number:Energy            | Maximum energy content of the high-voltage battery                            | yes      |
| battery#health                       | Number                   | Battery health as state of certified energy (SoCE)                            | no       |
| battery#energy-to-full               | Number:Energy            | Energy required to fully charge the battery                                   | no       |
| climate#comfort-state                | String                   | State of the cabin preconditioning                                            | no       |
| climate#progress                     | Number:Dimensionless     | Progress of the active preconditioning                                        | yes      |
| climate#remaining-running-time       | Number:Time              | Remaining running time of the preconditioning                                 | no       |
| climate#target-temperature           | Number:Temperature       | Target cabin temperature of the preconditioning                               | no       |
| climate#rear-defrost                 | Switch                   | Whether the rear window heating is active                                     | yes      |
| climate#mirror-heating               | Switch                   | Whether the exterior mirror heating is active                                 | yes      |
| climate#air-purification             | String                   | State of the cabin air purification                                           | yes      |
| climate#auxiliary-activity           | String                   | Activity of the auxiliary climate control                                     | no       |
| climate#auxiliary-remaining-time     | Number:Time              | Remaining running time of the auxiliary climate                               | no       |
| climate#auxiliary-error              | String                   | Reason why the auxiliary climate did not run                                  | yes      |
| navigation#gps                       | Location                 | Current location of the vehicle                                               | no       |
| navigation#heading                   | Number:Angle             | Compass heading of the vehicle                                                | yes      |
| navigation#altitude                  | Number:Length            | Altitude of the vehicle above sea level                                       | yes      |
| navigation#gps-fix                   | String                   | Quality of the GPS position                                                   | yes      |
| navigation#destination-distance      | Number:Length            | Distance to the active navigation destination                                 | no       |
| navigation#arrival-time              | String                   | Expected arrival time at the destination (hh:mm)                              | no       |
| trip#end-time                        | DateTime                 | Time when the last trip ended                                                 | no       |
| trip#end-mileage                     | Number:Length            | Total mileage at the end of the last trip                                     | yes      |
| trip#end-soc                         | Number:Dimensionless     | State of charge at the end of the last trip                                   | no       |
| trip#recuperation                    | Number                   | Energy recovered during the last trip in kWh/100km                            | no       |
| trip#comfort-energy                  | Number:Energy            | Energy consumed in COMFORT mode during the last trip                          | yes      |
| trip#electric-share                  | Number:Dimensionless     | Share of the distance driven electrically                                     | yes      |
| trip#eco-pro-share                   | Number:Dimensionless     | Share of the trip driven in ECO PRO mode                                      | yes      |
| trip#eco-pro-plus-share              | Number:Dimensionless     | Share of the trip driven in ECO PRO+ mode                                     | yes      |
| trip#acceleration-rating             | Number                   | Rating of the acceleration behaviour (0-5 stars)                              | yes      |
| trip#anticipation-rating             | Number                   | Rating of the anticipatory driving (0-5 stars)                                | yes      |
| tyres#pressure-front-left            | Number:Pressure          | Measured pressure of the front left tyre                                      | no       |
| tyres#pressure-front-right           | Number:Pressure          | Measured pressure of the front right tyre                                     | no       |
| tyres#pressure-rear-left             | Number:Pressure          | Measured pressure of the rear left tyre                                       | no       |
| tyres#pressure-rear-right            | Number:Pressure          | Measured pressure of the rear right tyre                                      | no       |
| tyres#target-front-left              | Number:Pressure          | Target pressure of the front left tyre                                        | yes      |
| tyres#target-front-right             | Number:Pressure          | Target pressure of the front right tyre                                       | yes      |
| tyres#target-rear-left               | Number:Pressure          | Target pressure of the rear left tyre                                         | yes      |
| tyres#target-rear-right              | Number:Pressure          | Target pressure of the rear right tyre                                        | yes      |
| tyres#temperature-front-left         | Number:Temperature       | Temperature of the front left tyre                                            | yes      |
| tyres#temperature-front-right        | Number:Temperature       | Temperature of the front right tyre                                           | yes      |
| tyres#temperature-rear-left          | Number:Temperature       | Temperature of the rear left tyre                                             | yes      |
| tyres#temperature-rear-right         | Number:Temperature       | Temperature of the rear right tyre                                            | yes      |
| security#alarm                       | Switch                   | Whether the anti-theft alarm is going off                                     | no       |
| security#arm-status                  | String                   | Arming state of the anti-theft alarm system                                   | no       |
| security#activation-time             | String                   | Time of the last alarm as reported by the vehicle                             | yes      |
| service#distance                     | Number:Length            | Remaining distance until the next service                                     | no       |
| service#inspection-date              | DateTime                 | Date of the next legal inspection                                             | no       |
| statistics#grid-energy               | Number:Energy            | Total energy charged from the grid                                            | yes      |
| statistics#fuel-consumed             | Number:Volume            | Total fuel consumed                                                           | yes      |
| statistics#avg-consumption           | Number                   | Average electric consumption in kWh/100km                                     | yes      |
| statistics#avg-speed                 | Number:Speed             | Average speed                                                                 | yes      |
| statistics#weekly-distance           | Number:Length            | Average distance driven per week                                              | yes      |
| statistics#weekly-distance-long-term | Number:Length            | Average distance per week over two months                                     | yes      |

The String channels show the values as reported by the vehicle; the door lock state is normalized, e.g. `SELECTIVE-LOCKED` becomes `SELECTIVELOCKED`.
Windows and the sunroof are `OPEN` also when they are only partly open or tilted; the advanced `-state` channels show the reported state.
Not every vehicle reports whether a charging cable is plugged in; until it does, `charge#plugged` follows the charging port status (`CONNECTED` or `DISCONNECTED`).
Values the vehicle reports as unavailable (e.g. `INVALID`, `-NA-` or `UNKNOWN`), and values that cannot be interpreted, set the channel to `UNDEF`.

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
    Thing vehicle i4 "BMW i4" [ vin="WBA00000000000000" ]
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
