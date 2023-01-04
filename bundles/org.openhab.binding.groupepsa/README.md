# Groupe PSA Binding

Binding to retrieve information via the Groupe PSA Web API for cars from Opel, Peugeot, Citroen, DS and Vauxhall.

## Supported Things

´bridge´ - Groupe PSA Web Api Bridge: The Thing to auto discover your cars.

´vehicle´ - Groupe PSA Car: The actual car Thing.

## Discovery

Use the "Groupe PSA Web Api bridge" to auto discover your cars.
You need to select the brand for the bridge binding and only cars for the brand will be auto discovered.
If you need to add for multiple brands or multiple different users, add multiple bridges.

## Bridge Configuration

You need to select a brand and enter the user name and password.
The Polling Interval (in minutes) determines how often the API will be polled for new cars.
The Client ID and Client Secret should not need to be updated.
(However you can register your own app via <https://developer.groupe-psa.com/inc/> and use this client information if you wish.)

### parameters

| Property        | Default | Required | Description                                                                                                                                             |
| --------------- | ------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| vendor          | None    | Yes      | The brand of the car (PEUGEOT, CITROEN, DS, OPEL or VAUXHALL).                                                                                          |
| userName        | None    | Yes      | The user name for the mypeugot/mycitroen/myds/myopel/myvauxhall website or app.                                                                         |
| password        | None    | Yes      | The password for the given user.                                                                                                                        |
| pollingInterval | 60      | No       | The Polling Interval (in minutes) determines how often the available vehicles are queried.                                                              |
| clientId        |         | Yes      | The Client ID for API access: can normally left at the default value. (see: <https://developer.groupe-psa.io/webapi/b2c/quickstart/connect/#article>)     |
| clientSecret    |         | Yes      | The Client Secret for API access: can normally left at the default value. (see: <https://developer.groupe-psa.io/webapi/b2c/quickstart/connect/#article>) |

## Vehicle Configuration

Normally the vehicles will be autodiscovered.
The Polling Interval and Online Timeout can be adjusted.

### parameters

| Property        | Default | Required | Description                                                                                       |                                                                                            |
|-----------------|---------|----------|---------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|
| id              | None    | Yes      | Vehicle API ID.                                                                                   | The ID is the vehicle API ID (not equal to the VIN), which is autodiscoverd by the bridge. |
| pollingInterval | 5       | No       | The Polling Interval (in minutes) determines how often the car is polled for updated information. |                                                                                            |
| onlineInterval  | 15      | No       | The Online Timeout (in minutes) determines when the car is deemed to be offline.                  |                                                                                            |

## Channels

| Channel Type ID         | Item Type                 | Description                                      |
| ----------------------- | ------------------------- | ------------------------------------------------ |
| current                 | Number:ElectricCurrent    | Electrical current                               |
| voltage                 | Number:ElectricPotential  | Voltage                                          |
| temperature             | Number:Temperature        | Temperature                                      |
| daytime                 | Contact                   | Enabled if it is daytime                         |
| doorLock                | String                    | Door lock state                                  |
| doorOpen                | Contact                   | Door is open                                     |
| ignition                | String                    | Ignition state                                   |
| moving                  | Contact                   | Vehicle is moving                                |
| acceleration            | Number:Acceleration       | Current acceleration                             |
| speed                   | Number:Speed              | Current speed                                    |
| mileage                 | Number:Length             | Total travelled distance                         |
| position                | Location                  | Last known position                              |
| heading                 | Number:Angle              | Direction of travel                              |
| type                    | String                    | Position acquisition type                        |
| signal                  | Number:Dimensionless      | Strength of the position localization signal     |
| lastUpdated             | DateTime                  | Last time the results were updated on the server |
| privacy                 | String                    | Privacy status                                   |
| belt                    | String                    | Seat belt status                                 |
| emergency               | String                    | Emergency call status                            |
| service                 | String                    | Service Type                                     |
| preconditioning         | String                    | Air conditioning status                          |
| preconditioningFailure  | String                    | Air conditioning failure cause                   |
| level                   | Number:Dimensionless      | Fuel level                                       |
| autonomy                | Number:Length             | Remaining distance                               |
| consumption             | Number:VolumetricFlowRate | Fuel consumption                                 |
| residual                | Number:Energy             | Remaining battery charge                         |
| capacity                | Number:Energy             | Battery capacity                                 |
| healthCapacity          | Number:Dimensionless      | Health of the battery capacity                   |
| healthResistance        | Number:Dimensionless      | Health of the battery resistance                 |
| chargingStatus          | String                    | Battery charging status                          |
| chargingMode            | String                    | Battery charging mode                            |
| chargingPlugged         | Contact                   | Vehicle plugged in to charger                    |
| chargingRate            | Number:Speed              | Battery Charging Rate                            |
| chargingRemainingTime   | Number:Time               | Time remaining till charged                      |
| chargingNextDelayedTime | Number:Time               | Time till the next charging starts               |

Further documentation can be found at: <https://developer.groupe-psa.io/webapi/b2c/api-reference/specification/#article>

## Full Example

### Things file

```java
Bridge groupepsa:bridge:opel "Auto Interface" [
    pollingInterval=60,
    userName="anonymous@anonymous.email",
    password="password",
    vendor="OPEL"
] {
    Things:
        vehicle zafira "Auto" @ "Outdoors"
        [
            id="<web api id here>",
            pollingInterval=5,
            onlineInterval=1440
        ]

}
```

### Items file

```java
Group Auto

Number:ElectricCurrent Auto_Aux_Current "Auxillliary Battery Current [%.1f %unit%]" (Auto) ["Measurement","Current"] {channel="groupepsa:vehicle:opel:zafira:battery#current"}
Number Auto_Aux_Level "Auxillliary Battery Level [%.1f %unit%]" (Auto) ["Measurement","Level"] {channel="groupepsa:vehicle:opel:zafira:battery#voltage"}

Number:Temperature Auto_Outside_Temperature "Outside Temperature [%.1f %unit%]" (Auto) ["Measurement","Temperature"] {channel="groupepsa:vehicle:opel:zafira:environment#temperature"}

Number:Length Auto_Mileage "Mileage [%.1f %unit%]" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:motion#mileage"}
Number:Speed Auto_Speed "Speed [%.1f %unit%]" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:motion#speed"}
Number:Acceleration Auto_Acceleration "Acceleration [%.1f %unit%]" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:motion#acceleration"}

Location Auto_Location "Locatie" (Auto) ["Point"] {channel="groupepsa:vehicle:opel:zafira:position#position"}

Number:Angle Auto_Heading "Richting" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:position#heading"}
String Auto_Location_Type "Locatie Type" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:position#type"}
Number Auto_Signal_Strength "Signaal Sterkte [%.1f %unit%]" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:position#signal"}

DateTime Auto_Last_Update "Laatst bijgewerkt [%1$tA, %1$te %1$tb %1$tY %1$tR]" (Auto) ["Measurement", "Timestamp"] {channel="groupepsa:vehicle:opel:zafira:various#lastUpdated"}
String Auto_Privacy_Status "Privacy Status" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:various#privacyStatus"}
String Auto_Belt_Status "Belt Status" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:various#beltStatus"}
String Auto_Emergency_Call "Emergency Call" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:various#emergencyCall"}
String Auto_Service "Service" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:various#service"}
String Auto_Air_Conditioning "Air Conditioning" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:various#preconditioning"}
String Auto_Air_Conditioning_Failure "Air Conditioning Failure" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:various#preconditioningFailure"}

Number:Length Auto_Autonomy "Autonomy [%.1f %unit%]" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:electric#autonomy"}
Number Auto_Level "Battery Level [%.1f %unit%]" (Auto) ["Measurement", "Level"] {channel="groupepsa:vehicle:opel:zafira:electric#level"}
Number:Energy Auto_Residual "Battery Residual [%.1f %unit%]" (Auto) ["Measurement", "Level"] {channel="groupepsa:vehicle:opel:zafira:electric#residual"}

Number:Energy Auto_Capacity "Battery Capacity [%.1f %unit%]" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:electric#capacity"}
Number Auto_Health_Capacity "Battery Health Capacity [%.1f %unit%]" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:electric#batteryHealthCapacity"}
Number Auto_Health_Resistance "Battery Health Resistance [%.1f %unit%]" (Auto) ["Measurement"] {channel="groupepsa:vehicle:opel:zafira:electric#batteryHealthResistance"}

String Auto_Charging_Status "Charging Status" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:electric#chargingStatus"}
String Auto_Charging_Mode "Charging Mode" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:electric#chargingMode"}
Contact Auto_Plugged_In "Plugged In" (Auto) ["Status"] {channel="groupepsa:vehicle:opel:zafira:electric#chargingPlugged"}
Number:Speed Auto_Charging_Rate "Charging Rate [%.1f %unit%]" (Auto) ["Measurement"]  {channel="groupepsa:vehicle:opel:zafira:electric#chargingRate"}

Number:Time Auto_Charging_Time_Remaining "Charging Time Remaining [%.1f %unit%]" (Auto) ["Measurement", "Duration"] {channel="groupepsa:vehicle:opel:zafira:electric#chargingRemainingTime"}
Number:Time Auto_Charging_Time_Till_Next "Charging Time Till Next Charging [%.1f %unit%]" (Auto) ["Measurement", "Duration"] {channel="groupepsa:vehicle:opel:zafira:electric#chargingNextDelayedTime"}
```
