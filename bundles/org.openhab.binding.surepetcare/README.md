# Sure Petcare Binding

This binding integrates with the Sure Petcare API, supporting cloud‑connected pet/cat flaps, feeders, and waterstations.

## Features

1. Read access to all attributes for households, devices (hubs, flaps, feeder, waterstations) and pets through individual things/channels.
1. Manual setting of pet location.
1. Setting of LED Mode (hub), Locking Mode (flaps) and Curfews.

### Restrictions / TODO

1. The Sure Petcare API is not publicly available and this binding has been based on observed interactions between their mobile phone app and the cloud API.
   If the Sure Petcare API changes, this binding might stop working.

### Credits

The binding code is based on a lot of work done by other developers:

- Holger Eisold (<https://github.com/HerzScheisse>) - Python use in openHAB and various PRs (<https://github.com/HerzScheisse/SurePetcare-openHAB-JSR223-Rules>)
- Alex Toft (<https://github.com/alextoft>) - PHP implementation (<https://github.com/alextoft/sureflap>)
- rcastberg (<https://github.com/rcastberg>) - Python implementation (<https://github.com/rcastberg/sure_petcare>)

## Supported Things

This binding supports the following Thing types

| Thing                 | Thing Type | Discovery | Description                                                              |
|-----------------------|------------|-----------|--------------------------------------------------------------------------|
| Bridge                | Bridge     | Manual    |  A single connection to the Sure Petcare API                             |
| Household             | Thing      | Automatic |  The Sure Petcare Household                                              |
| Hub Device            | Thing      | Automatic |  The hub device which connects the cat flaps and feeders to the internet |
| Flap Device           | Thing      | Automatic |  A cat or pet flap                                                       |
| Feeder Device         | Thing      | Automatic |  A pet feeder                                                            |
| Waterstation Device   | Thing      | Automatic |  A pet drinking station                                                  |
| Pet                   | Thing      | Automatic |  A pet (dog or cat)                                                      |

## Getting started /  Discovery

The binding consists of a Bridge (the API connection), and a number of Things, which relates to the individual hardware devices and pets.
Sure Petcare things can be configured either through the online configuration utility via discovery, or manually through a 'surepetcare.things' configuration file.
The Bridge is not automatically discovered and must be added manually.
That is because the Sure Petcare API requires authentication credentials to communicate with the service.

After adding the Bridge, it will go ONLINE, and after a short while, the discovery process for household, devices and pets will start.
When new hardware is discovered it will appear in the Inbox.

## Things and their channels

Channel names in **bold** are read/write; everything else is read‑only.

### Bridge Thing

| Channel     | Type   | Description                                                                       |
|-------------|--------|-----------------------------------------------------------------------------------|
| refresh     | Switch | Trigger switch to force a full cache update                                       |

### Household Thing

| Channel    | Type     | Description                                  |
|------------|----------|----------------------------------------------|
| id         | Number   | A unique ID assigned by the Sure Petcare API |
| name       | String   | The name of the household                    |
| timezoneId | Number   | The ID of the household's timezone           |

### Hub Device Thing

| Channel         | Type     | Description                                                           |
|-----------------|----------|-----------------------------------------------------------------------|
| id              | Number   | A unique ID assigned by the Sure Petcare API                          |
| name            | String   | The name of the hub                                                   |
| product         | String   | The type of product (1=hub)                                           |
| ledMode         | String   | The mode of the hub's LED ears                                        |
| pairingMode     | String   | The state of pairing                                                  |
| online          | Switch   | Indicator if the hub is connected to the internet                     |

### Flap Device Thing (Cat or Pet Flap)

| Channel            | Type                     | Description                                                |
|--------------------|--------------------------|------------------------------------------------------------|
| id                 | Number                   | A unique ID assigned by the Sure Petcare API               |
| name               | String                   | The name of the flap                                       |
| product            | String                   | The type of product (3=pet flap, 6=cat flap)               |
| curfewEnabled1     | Switch                   | Indicator if curfew #1 configuration is enabled            |
| curfewLockTime1    | Text                     | The curfew #1 locking time (HH:MM)                         |
| curfewUnlockTime1  | Text                     | The curfew #1 unlocking time (HH:MM)                       |
| curfewEnabled2     | Switch                   | Indicator if curfew #2 configuration is enabled            |
| curfewLockTime2    | Text                     | The curfew #2 locking time (HH:MM)                         |
| curfewUnlockTime2  | Text                     | The curfew #2 unlocking time (HH:MM)                       |
| curfewEnabled3     | Switch                   | Indicator if curfew #3 configuration is enabled            |
| curfewLockTime3    | Text                     | The curfew #3 locking time (HH:MM)                         |
| curfewUnlockTime3  | Text                     | The curfew #3 unlocking time (HH:MM)                       |
| curfewEnabled4     | Switch                   | Indicator if curfew #4 configuration is enabled            |
| curfewLockTime4    | Text                     | The curfew #4 locking time (HH:MM)                         |
| curfewUnlockTime4  | Text                     | The curfew #4 unlocking time (HH:MM)                       |
| lockingMode        | Text                     | The locking mode (e.g. in/out, in-only, out-only etc.)     |
| online             | Switch                   | Indicator if the flap is connected to the hub              |
| lowBattery         | Switch                   | Indicator if the battery voltage is low                    |
| batteryLevel       | Number:Dimensionless     | The battery voltage percentage in %                        |
| batteryVoltage     | Number:ElectricPotential | The absolute battery voltage measurement in V              |
| deviceRSSI         | Number:Power             | The received device signal strength in dBm                 |
| hubRSSI            | Number:Power             | The received hub signal strength in dBm                    |

### Feeder Device Thing

| Channel           | Type                      | Description                                                                                     |
|-------------------|---------------------------|-------------------------------------------------------------------------------------------------|
| id                | Number                    | A unique ID assigned by the Sure Petcare API                                                    |
| name              | String                    | The name of the feeder                                                                          |
| product           | String                    | The type of product                                                                             |
| online            | Switch                    | Indicator if the feeder is connected to the hub                                                 |
| lowBattery        | Switch                    | Indicator if the battery voltage is low                                                         |
| batteryLevel      | Number:Dimensionless      | The battery voltage percentage in %                                                             |
| batteryVoltage    | Number:ElectricPotential  | The absolute battery voltage measurement in V                                                   |
| deviceRSSI        | Number:Power              | The received device signal strength in dBm                                                      |
| hubRSSI           | Number:Power              | The received hub signal strength in dBm                                                         |
| bowls             | String                    | The feeder bowls type (1 big bowl or 2 half bowls)                                              |
| bowlsFood         | String                    | The feeder big bowl food type (wet food, dry food or both)                                      |
| bowlsTarget       | Number:Mass               | The feeder big bowl target weight in gram (even if user setting is oz, API stores this in gram) |
| bowlsFoodLeft     | String                    | The feeder left half bowl food type (wet food, dry food or both)                                |
| bowlsTargetLeft   | Number:Mass               | The feeder left half bowl target weight in g                                                    |
| bowlsFoodRight    | String                    | The feeder right half bowl food type (wet food, dry food or both)                               |
| bowlsTargetRight  | Number:Mass               | The feeder right half bowl target weight in g                                                   |
| bowlsCloseDelay   | String                    | The feeder lid close delay (fast, normal, slow)                                                 |
| bowlsTrainingMode | String                    | The feeder training mode (off, full open, almost full open, half closed, almost closed)         |

### Waterstation Device Thing

| Channel           | Type                      | Description                                            |
|-------------------|---------------------------|--------------------------------------------------------|
| id                | Number                    | A unique ID assigned by the Sure Petcare API           |
| name              | String                    | The name of the waterstation                           |
| product           | String                    | The type of product                                    |
| online            | Switch                    | Indicator if the waterstation is connected to the hub  |
| lowBattery        | Switch                    | Indicator if the battery voltage is low                |
| batteryLevel      | Number:Dimensionless      | The battery voltage percentage in %                    |
| batteryVoltage    | Number:ElectricPotential  | The absolute battery voltage measurement in V          |
| deviceRSSI        | Number:Power              | The received device signal strength in dBm             |
| hubRSSI           | Number:Power              | The received hub signal strength in dBm                |

### Pet Thing

| Channel                | Type           | Description                                                                         |
|------------------------|----------------|-------------------------------------------------------------------------------------|
| id                     | Number         | A unique ID assigned by the Sure Petcare API                                        |
| name                   | String         | The name of the pet                                                                 |
| comment                | String         | A user-provided comment/description                                                 |
| gender                 | String         | The pet's gender                                                                    |
| breed                  | String         | The pet's breed                                                                     |
| species                | String         | The pet's species                                                                   |
| photo                  | Image          | The image of the pet                                                                |
| tagIdentifier          | String         | The unique identifier of the pet's microchip or collar tag                          |
| location               | String         | The current location of the pet (0=unknown, 1=inside, 2=outside)                    |
| locationChanged        | DateTime       | The time when the location was last changed                                         |
| locationTimeoffset     | String         | Time-Command to set the pet location with a time offset. (10, 30 or 60 minutes ago) |
| locationChangedThrough | Text           | The device name or username where the pet left/entered the house                    |
| weight                 | Number:Mass    | The pet's weight (in gram)                                                          |
| dateOfBirth            | DateTime       | The pet's date of birth                                                             |
| feederDevice           | String         | The device from which the pet last ate                                              |
| feederLastChange       | Number:Mass    | The last eaten change in gram (big bowl)                                            |
| feederLastChangeLeft   | Number:Mass    | The last eaten change in gram (half bowl left)                                      |
| feederLastChangeRight  | Number:Mass    | The last eaten change in gram (half bowl right)                                     |
| feederLastFeeding      | DateTime       | The pet's last eaten date                                                           |
| waterDevice            | String         | The device from which the pet last drank                                            |
| waterLastChange        | Number:Volume  | The last change in drinking volume in milliliters (mL)                              |
| waterLastDrinking      | DateTime       | The pet's last drinking date                                                        |

## Manual configuration

### Things configuration

```java
Bridge surepetcare:bridge:bridge1 "Demo API Bridge" @ "SurePetcare" [ username="<USERNAME>", password="<PASSWORD>", refreshIntervalTopology=36000, refreshIntervalStatus=300 ]
{
  Thing household     12345  "My Household" @ "SurePetcare"
  Thing hubDevice     123456 "My SurePetcare Hub" @ "SurePetcare Devices"
  Thing flapDevice    123456 "My Backdoor Cat Flap" @ "SurePetcare Devices"
  Thing feederDevice  123456 "My Pet Feeder" @ "SurePetcare Devices"
  Thing waterDevice   123456 "My Pet Waterstation" @ "SurePetcare Devices"
  Thing pet           12345  "My Cat" @ "SurePetcare Pets"
}
```

### Items configuration

Choose "Create Equipment from Thing" -> select Thing -> Channels click on Expert Mode

```java
// Equipment representing thing:
// surepetcare:bridge:api
// (SurePetcare API Bridge)
Group     gSurePetcare           "SurePetcare API Bridge"   (gLivingRoom)     ["WebService"]
Switch    SurePet_API_Refresh    "Bridge Data Refresh"      (gSurePetcare)    ["Point"]         {channel="surepetcare:bridge:api:refresh"}

// Equipment representing thing:
// surepetcare:household:api:CHANGE_ME
// (SurePetcare Household)
Group     gSurePetcareHousehold            "SurePetcare Household"          (gSurePetcare)             ["Equipment"]
Number    SurePet_HouseholdId_1            "Household Id"                   (gSurePetcareHousehold)    ["Point"]        {channel="surepetcare:household:api:CHANGE_ME:id"}
String    SurePet_HouseholdName_1          "Household Name"                 (gSurePetcareHousehold)    ["Point"]        {channel="surepetcare:household:api:CHANGE_ME:name"}
Number    SurePet_HouseholdTimezoneId_1    "Household Timezone Id"          (gSurePetcareHousehold)    ["Point"]        {channel="surepetcare:household:api:CHANGE_ME:timezoneId"}

Group    gSurePetcareDevices    "SurePetcare Devices"    (gSurePetcareHousehold)    ["Equipment"]
// Equipment representing thing:
// surepetcare:hubDevice:api:CHANGE_ME
// (SurePetcare Hub)
Group     gSurePetcareHub             "SurePetcare Hub"         (gSurePetcareDevices)    ["Equipment"]
Number    SurePet_HubId_1             "Hub Id"                  (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:id"}
String    SurePet_HubName_1           "Hub Name"                (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:name"}
String    SurePet_HubProduct_1        "Hub Product"             (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:product"}
String    SurePet_HubLedMode_1        "Hub LED Mode"            (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:ledMode"}
String    SurePet_HubPairingMode_1    "Hub Pairing Mode"        (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:pairingMode"}
Switch    SurePet_HubOnline_1         "Hub Online"              (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:online"}

// Equipment representing thing:
// surepetcare:flapDevice:api:CHANGE_ME
// (SurePetcare Flap)
Group                       gSurePetcareFlap                   "SurePetcare Flap"                                            (gSurePetcareDevices)    ["Equipment"]
Number                      SurePet_FlapId_1                   "Flap Id"                                                     (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:id"}
String                      SurePet_FlapName_1                 "Flap Name"                                                   (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:name"}
String                      SurePet_FlapProduct_1              "Flap Product"                                                (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:product"}
Switch                      SurePet_FlapCurfewEnabled1_1       "Flap Curfew 1 Enabled"                                       (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewEnabled1"}
String                      SurePet_FlapCurfewLockTime1_1      "Flap Curfew 1 Lock Time"                                     (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewLockTime1"}
String                      SurePet_FlapCurfewUnlockTime1_1    "Flap Curfew 1 Unlock Time"                                   (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewUnlockTime1"}
Switch                      SurePet_FlapCurfewEnabled2_1       "Flap Curfew 2 Enabled"                                       (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewEnabled2"}
String                      SurePet_FlapCurfewLockTime2_1      "Flap Curfew 2 Lock Time"                                     (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewLockTime2"}
String                      SurePet_FlapCurfewUnlockTime2_1    "Flap Curfew 2 Unlock Time"                                   (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewUnlockTime2"}
Switch                      SurePet_FlapCurfewEnabled3_1       "Flap Curfew 3 Enabled"                                       (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewEnabled3"}
String                      SurePet_FlapCurfewLockTime3_1      "Flap Curfew 3 Lock Time"                                     (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewLockTime3"}
String                      SurePet_FlapCurfewUnlockTime3_1    "Flap Curfew 3 Unlock Time"                                   (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewUnlockTime3"}
Switch                      SurePet_FlapCurfewEnabled4_1       "Flap Curfew 4 Enabled"                                       (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewEnabled4"}
String                      SurePet_FlapCurfewLockTime4_1      "Flap Curfew 4 Lock Time"                                     (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewLockTime4"}
String                      SurePet_FlapCurfewUnlockTime4_1    "Flap Curfew 4 Unlock Time"                                   (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewUnlockTime4"}
String                      SurePet_FlapLockingMode_1          "Flap Locking Mode"                     <lock>                (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:lockingMode"}
Switch                      SurePet_FlapLowBattery_1           "Flap Low Battery"                      <LowBattery>          (gSurePetcareFlap)       ["Energy", "LowBattery"]     {channel="surepetcare:flapDevice:api:CHANGE_ME:lowBattery"}
Number:Dimensionless        SurePet_FlapBatteryLevel_1         "Flap Battery Level"                    <Battery>             (gSurePetcareFlap)       ["Measurement", "Energy"]    {channel="surepetcare:flapDevice:api:CHANGE_ME:batteryLevel", stateDescription="%.0f %%", unit="%"}
Number:ElectricPotential    SurePet_FlapBatteryVoltage_1       "Flap Battery Voltage"                  <energy>              (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:batteryVoltage", stateDescription="%.1f V", unit="V"}
Switch                      SurePet_FlapOnline_1               "Flap Online"                                                 (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:online"}
Number:Power                SurePet_FlapDeviceRSSI_1           "Flap Device RSSI"                      <qualityofservice>    (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:deviceRSSI", stateDescription="%.2f dBm", unit="dBm"}
Number:Power                SurePet_FlapHubRSSI_1              "Flap Hub RSSI"                         <qualityofservice>    (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:hubRSSI", stateDescription="%.2f dBm", unit="dBm"}

// Equipment representing thing:
// surepetcare:feederDevice:api:CHANGE_ME
// (SurePetcare Feeder 1)
Group                       gSurePetcareDevice1                  "SurePetcare Feeder 1"                                                 (gSurePetcareDevices)    ["Equipment"]
Number                      SurePet_FeederId_1                   "Feeder ID"                                                            (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:id"}
String                      SurePet_FeederName_1                 "Feeder Name"                                                          (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:name"}
String                      SurePet_FeederProduct_1              "Feeder Product"                                                       (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:product"}
Switch                      SurePet_FeederLowBattery_1           "Feeder Low Battery"                             <LowBattery>          (gSurePetcareDevice1)    ["Energy", "LowBattery"]     {channel="surepetcare:feederDevice:api:CHANGE_ME:lowBattery"}
Number:Dimensionless        SurePet_FeederBatteryLevel_1         "Feeder Battery Level"                           <Battery>             (gSurePetcareDevice1)    ["Measurement", "Energy"]    {channel="surepetcare:feederDevice:api:CHANGE_ME:batteryLevel", stateDescription="%.0f %%", unit="%"}
Number:ElectricPotential    SurePet_FeederBatteryVoltage_1       "Feeder Battery Voltage"                         <energy>              (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:batteryVoltage", stateDescription="%.1f V", unit="V"}
Switch                      SurePet_FeederOnline_1               "Feeder Status"                                                        (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:online"}
Number:Power                SurePet_FeederDeviceRSSI_1           "Feeder Device Signal"                           <qualityofservice>    (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:deviceRSSI", stateDescription="%.2f dBm", unit="dBm"}
Number:Power                SurePet_FeederHubRSSI_1              "Feeder Hub Signal"                              <qualityofservice>    (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:hubRSSI", stateDescription="%.2f dBm", unit="dBm"}
String                      SurePet_FeederBowlsFood_1            "Feeder Food Type"                                                     (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFood"}
Number:Mass                 SurePet_FeederBowlsTarget_1          "Feeder Target"                                                        (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTarget", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowlsFoodLeft_1        "Feeder Food Type L"                                                   (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFoodLeft"}
Number:Mass                 SurePet_FeederBowlsTargetLeft_1      "Feeder Target L"                                                      (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTargetLeft", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowlsFoodRight_1       "Feeder Food Type R"                                                   (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFoodRight"}
Number:Mass                 SurePet_FeederBowlsTargetRight_1     "Feeder Target R"                                                      (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTargetRight", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowls_1                "Feeder Bowls Type"                                                    (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowls"}
String                      SurePet_FeederBowlsCloseDelay_1      "Feeder Close Delay"                                                   (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsCloseDelay"}
String                      SurePet_FeederBowlsTrainingMode_1    "Feeder Training Mode"                                                 (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTrainingMode"}


// Equipment representing thing:
// surepetcare:feederDevice:api:CHANGE_ME
// (SurePetcare Feeder 2)
Group                       gSurePetcareDevice2                  "SurePetcare Feeder 2"                                                 (gSurePetcareDevices)    ["Equipment"]
Number                      SurePet_FeederId_2                   "Feeder ID"                                                            (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:id"}
String                      SurePet_FeederName_2                 "Feeder Name"                                                          (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:name"}
String                      SurePet_FeederProduct_2              "Feeder Product"                                                       (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:product"}
Switch                      SurePet_FeederLowBattery_2           "Feeder Low Battery"                             <LowBattery>          (gSurePetcareDevice2)    ["Energy", "LowBattery"]     {channel="surepetcare:feederDevice:api:CHANGE_ME:lowBattery"}
Number:Dimensionless        SurePet_FeederBatteryLevel_2         "Feeder Battery Level"                           <Battery>             (gSurePetcareDevice2)    ["Measurement", "Energy"]    {channel="surepetcare:feederDevice:api:CHANGE_ME:batteryLevel", stateDescription="%.0f %%", unit="%"}
Number:ElectricPotential    SurePet_FeederBatteryVoltage_2       "Feeder Battery Voltage"                         <energy>              (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:batteryVoltage", stateDescription="%.1f V", unit="V"}
Switch                      SurePet_FeederOnline_2               "Feeder Status"                                                        (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:online"}
Number:Power                SurePet_FeederDeviceRSSI_2           "Feeder Device Signal"                           <qualityofservice>    (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:deviceRSSI", stateDescription="%.2f dBm", unit="dBm"}
Number:Power                SurePet_FeederHubRSSI_2              "Feeder Hub Signal"                              <qualityofservice>    (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:hubRSSI", stateDescription="%.2f dBm", unit="dBm"}
String                      SurePet_FeederBowlsFood_2            "Feeder Food Type"                                                     (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFood"}
Number:Mass                 SurePet_FeederBowlsTarget_2          "Feeder Target"                                                        (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTarget", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowlsFoodLeft_2        "Feeder Food Type L"                                                   (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFoodLeft"}
Number:Mass                 SurePet_FeederBowlsTargetLeft_2      "Feeder Target L"                                                      (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTargetLeft", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowlsFoodRight_2       "Feeder Food Type R"                                                   (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFoodRight"}
Number:Mass                 SurePet_FeederBowlsTargetRight_2     "Feeder Target R"                                                      (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTargetRight", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowls_2                "Feeder Bowls Type"                                                    (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowls"}
String                      SurePet_FeederBowlsCloseDelay_2      "Feeder Close Delay"                                                   (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsCloseDelay"}
String                      SurePet_FeederBowlsTrainingMode_2    "Feeder Training Mode"                                                 (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTrainingMode"}

// Equipment representing thing:
// surepetcare:waterDevice:api:CHANGE_ME
// (SurePetcare Waterstation)
Group                       gSurePetcareDevice3                "SurePetcare Waterstation"                                     (gSurePetcareDevices)    ["Equipment"]
Number                      SurePet_Poseidon_ID                "Waterstation ID"                                              (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:id"}
String                      SurePet_Poseidon_Name              "Waterstation Name"                                            (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:name"}
String                      SurePet_Poseidon_Product           "Waterstation Product"                                         (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:product"}
Switch                      SurePet_Poseidon_LowBattery        "Waterstation Low Battery"               <LowBattery>          (gSurePetcareDevice3)    ["Energy", "LowBattery"]     {channel="surepetcare:waterDevice:api:CHANGE_ME:lowBattery"}
Number:Dimensionless        SurePet_Poseidon_BatteryLevel      "Waterstation Battery Level"             <Battery>             (gSurePetcareDevice3)    ["Measurement", "Energy"]    {channel="surepetcare:waterDevice:api:CHANGE_ME:batteryLevel", stateDescription="%.0f %%", unit="%"}
Number:ElectricPotential    SurePet_Poseidon_BatteryVolatge    "Waterstation Battery Voltage"           <energy>              (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:batteryVoltage", stateDescription="%.1f V", unit="V"}
Switch                      SurePet_Poseidon_Online            "Waterstation Status"                                          (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:online"}
Number:Power                SurePet_Poseidon_DeviceRSSI        "Waterstation Device Signal"             <qualityofservice>    (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:deviceRSSI", stateDescription="%.2f dBm", unit="dBm"}
Number:Power                SurePet_Poseidon_HubRSSI           "Waterstation Hub Signal"                <qualityofservice>    (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:hubRSSI", stateDescription="%.2f dBm", unit="dBm"}


Group    gSurePetcarePets    "SurePetcare Pets"    (gSurePetcareHousehold)    ["Equipment"]
// Equipment representing thing:
// surepetcare:pet:api:CHANGE_ME
// (SurePetcare Pet 1)
Group            gSurePetcarePet1                    "SurePetcare Pet 1"                                             (gSurePetcarePets)    ["Equipment"]
Number           SurePet_Id_1                        "Pet Id"                                                        (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:id"}
String           SurePet_Name_1                      "Pet Name"                                                      (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:name"}
String           SurePet_Comment_1                   "Pet Comment"                                                   (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:comment"}
String           SurePet_Gender_1                    "Pet Gender"                                                    (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:gender"}
String           SurePet_Breed_1                     "Pet Breed"                                                     (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:breed"}
String           SurePet_Species_1                   "Pet Species"                                                   (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:species"}
Image            SurePet_Photo_1                     "Pet Photo"                                                     (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:photo"}
String           SurePet_TagIdentifier_1             "Pet Tag Identifier"                                            (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:tagIdentifier"}
String           SurePet_Location_1                  "Pet Location"                            <motion>              (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:location"}
DateTime         SurePet_LocationChanged_1           "Pet Loc. Updated"                                              (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationChanged", stateDescription="%1$ta. %1$tH:%1$tM"}
String           SurePet_LocationTimeoffset_1        "Pet Switch Location"                                           (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationTimeoffset"}
String           SurePet_LocationChangedThrough_1    "Pet Entered / Left through"                                    (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationChangedThrough"}
DateTime         SurePet_DateOfBirth_1               "Pet Date of Birth"                       <calendar>            (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:dateOfBirth", stateDescription="%1$td.%1$tm.%1$tY"}
Number:Mass      SurePet_Weight_1                    "Pet Weight"                                                    (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:weight", stateDescription="%.1f kg", unit="g"}
String           SurePet_FeederDevice_1              "Device Name"                                                   (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederDevice"}
DateTime         SurePet_FeederLastFeeding_1         "Last Feeding"                            <time>                (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastFeeding", stateDescription="%1$ta. %1$tH:%1$tM"}
Number:Mass      SurePet_FeederLastChange_1          "Change:"                                                       (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChange", stateDescription="%.1f g", unit="g"}
Number:Mass      SurePet_FeederLastChangeLeft_1      "Change: L"                                                     (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChangeLeft", stateDescription="%.1f g", unit="g"}
Number:Mass      SurePet_FeederLastChangeRight_1     "Change: R"                                                     (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChangeRight", stateDescription="%.1f g", unit="g"}
String           SurePet_WaterDevice_1               "Device Name"                                                   (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterDevice"}
DateTime         SurePet_WaterLastDrinking_1         "Last Drinking"                           <time>                (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterLastDrinking", stateDescription="%1$ta. %1$tH:%1$tM"}
Number:Volume    SurePet_WaterLastChange_1           "Change:"                                                       (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterLastChange", stateDescription="%.1f ml", unit="ml"}

// Equipment representing thing:
// surepetcare:pet:api:CHANGE_ME
// (SurePetcare Pet 2)
Group            gSurePetcarePet2                    "SurePetcare Pet 2"                                             (gSurePetcarePets)    ["Equipment"]
Number           SurePet_Id_2                        "Pet Id"                                                        (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:id"}
String           SurePet_Name_2                      "Pet Name"                                                      (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:name"}
String           SurePet_Comment_2                   "Pet Comment"                                                   (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:comment"}
String           SurePet_Gender_2                    "Pet Gender"                                                    (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:gender"}
String           SurePet_Breed_2                     "Pet Breed"                                                     (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:breed"}
String           SurePet_Species_2                   "Pet Species"                                                   (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:species"}
Image            SurePet_Photo_2                     "Pet Photo"                                                     (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:photo"}
String           SurePet_TagIdentifier_2             "Pet Tag Identifier"                                            (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:tagIdentifier"}
String           SurePet_Location_2                  "Pet Location"                            <motion>              (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:location"}
DateTime         SurePet_LocationChanged_2           "Pet Loc. Updated"                                              (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationChanged", stateDescription="%1$ta. %1$tH:%1$tM"}
String           SurePet_LocationTimeoffset_2        "Pet Switch Location"                                           (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationTimeoffset"}
String           SurePet_LocationChangedThrough_2    "Pet Entered / Left through"                                    (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationChangedThrough"}
DateTime         SurePet_DateOfBirth_2               "Pet Date of Birth"                       <calendar>            (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:dateOfBirth", stateDescription="%1$td.%1$tm.%1$tY"}
Number:Mass      SurePet_Weight_2                    "Pet Weight"                                                    (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:weight", stateDescription="%.1f kg", unit="g"}
String           SurePet_FeederDevice_2              "Device Name"                                                   (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederDevice"}
DateTime         SurePet_FeederLastFeeding_2         "Last Feeding"                            <time>                (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastFeeding", stateDescription="%1$ta. %1$tH:%1$tM"}
Number:Mass      SurePet_FeederLastChange_2          "Change:"                                                       (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChange", stateDescription="%.1f g", unit="g"}
Number:Mass      SurePet_FeederLastChangeLeft_2      "Change: L"                                                     (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChangeLeft", stateDescription="%.1f g", unit="g"}
Number:Mass      SurePet_FeederLastChangeRight_2     "Change: R"                                                     (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChangeRight", stateDescription="%.1f g", unit="g"}
String           SurePet_WaterDevice_2               "Device Name"                                                   (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterDevice"}
DateTime         SurePet_WaterLastDrinking_2         "Last Drinking"                           <time>                (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterLastDrinking", stateDescription="%1$ta. %1$tH:%1$tM"}
Number:Volume    SurePet_WaterLastChange_2           "Change:"                                                       (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterLastChange", stateDescription="%.1f ml", unit="ml"}
```

### Sitemap Configuration

```perl
sitemap surepetcare label="My home automation" {
  Frame label="Bridge" {
    Text item=UR_1a_Online valuecolor=[ON="green", OFF="red"]
    Switch item=UR_1a_Refresh
  }

  Frame label="Single Pet/Cats items" {
    Text item=UR_1e_Location valuecolor=[1="green", 2="red"]
    // to see also the item state, just remove the brackets from the label
    Switch item=UR_1e_Location label="Set Pet Location []" mappings=[1="Inside", 2="Outside"]
    // Selection item=UR_1e_Location label="Set Pet Location []" mappings=[1="Im Haus", 2="Draußen"]
    Text item=UR_1e_LocationChanged
    Switch item=UR_1e_LocationTimeoffset label="Set Loc with time offset []" mappings=[10="-10min", 30="-30min", 60="-1h"]
    Text item=UR_1e_LocationThrough
    Text item=UR_1e_Id icon="text"
    Text item=UR_1e_Name
    Text item=UR_1e_Comment
    Text item=UR_1e_Gender
    Text item=UR_1e_Breed
    Text item=UR_1e_Species
    Text item=UR_1e_MicroChip
    Text item=UR_1e_Weight icon="text"
    Text item=UR_1e_DateOfBirth
    Text item=UR_1e_FeedDevice
    /*Text item=UR_1e_FeedChange icon="text"*/ // if you have one big bowl in your feeder use this line and comment the following 2 out
    Text item=UR_1e_FeedChangeLeft icon="text"
    Text item=UR_1e_FeedChangeRight icon="text"
    Text item=UR_1e_FeedAt
    Image item=UR_1e_Photo
  }

  Frame label="Hub Device" {
    Text item=UR_1c_HubOnline valuecolor=[ON="green", OFF="red"]
    Text item=UR_1c_HubId icon="text"
    Text item=UR_1c_HubName
    Text item=UR_1c_HubProduct
    Switch item=UR_1c_HubLedMode mappings=[0="Off", 1="Bright", 4="Dimmed"]
    Text item=UR_1c_HubPairingMode
  }

  Frame label="Flap Device" {
    Text item=UR_1d_FlapOnline valuecolor=[ON="green", OFF="red"]
    Text item=UR_1d_FlapId icon="text"
    Text item=UR_1d_FlapName
    Text item=UR_1d_FlapProduct
    Switch item=UR_1d_FlapCurfewEnabled1
    Text item=UR_1d_FlapCurfewLocktime1
    Text item=UR_1d_FlapCurfewUnlocktime1
    Switch item=UR_1d_FlapCurfewEnabled2
    Text item=UR_1d_FlapCurfewLocktime2
    Text item=UR_1d_FlapCurfewUnlocktime2
    Switch item=UR_1d_FlapCurfewEnabled3
    Text item=UR_1d_FlapCurfewLocktime3
    Text item=UR_1d_FlapCurfewUnlocktime3
    Switch item=UR_1d_FlapCurfewEnabled4
    Text item=UR_1d_FlapCurfewLocktime4
    Text item=UR_1d_FlapCurfewUnlocktime4
    Text item=UR_1d_FlapLockingMode
    Text item=UR_1d_FlapLowBattery valuecolor=[OFF="green", ON="red"]
    Text item=UR_1d_FlapBatteryLevel icon="battery"
    Text item=UR_1d_FlapBatteryVoltage icon="text"
    Text item=UR_1d_FlapDeviceRSSI icon="network"
    Text item=UR_1d_FlapHubRSSI icon="network"
  }

  Frame label="Feeder Device" {
    Text item=UR_1f_FeederOnline valuecolor=[ON="green", OFF="red"]
    Text item=UR_1f_FeederId icon="text"
    Text item=UR_1f_FeederName
    Text item=UR_1f_FeederProduct
    Text item=UR_1f_FeederLowBattery valuecolor=[OFF="green", ON="red"]
    Text item=UR_1f_FeederBatteryLevel icon="battery"
    Text item=UR_1f_FeederBatteryVoltage icon="text"
    Text item=UR_1f_FeederBowlsType
    /*Text item=UR_1f_FeederBowlsFoodtype
    Text item=UR_1f_FeederBowlsTarget icon="text"*/
    Text item=UR_1f_FeederBowlsFoodtypeLeft
    Text item=UR_1f_FeederBowlsTargetLeft icon="text"
    Text item=UR_1f_FeederBowlsFoodtypeRight
    Text item=UR_1f_FeederBowlsTargetRight icon="text"
    Text item=UR_1f_FeederBowlsLidCloseDelay
    Text item=UR_1f_FeederBowlsTrainingMode
    Text item=UR_1f_FeederDeviceRSSI icon="network"
    Text item=UR_1f_FeederHubRSSI icon="network"
  }
}

```

### Using Group Items

You can also set pet locations with a group item.
Please note: the location for each pet gets updated only if the current location is not already the location you want to set.
This can be very useful if you have a lot of pets that often enter the home by any window/door.
Your .items file should contain this:

```java
Group:String:OR(1,2)  gLocation      "Cats inside [%d]"
String                UR_1e_Location "Pet Location [%s]"  (dgPet, gLocation)  {channel="surepetcare:pet:bridge1:12345:location"}
```

And your .sitemap file could look like this:

```perl
Frame label="Group Pet/Cats items" {
  Selection item=gLocation label="Set ALL cats to:" mappings=[1="Inside", 2="Outside"] icon="text"
  Switch item=gLocation label="Set ALL cats to: []" mappings=[1="Inside", 2="Outside"]
  Group item=gLocation
}
```

## Troubleshooting

| Problem                                   | Solution                                                                          |
|-------------------------------------------|-----------------------------------------------------------------------------------|
| Bridge cannot connect to Sure Petcare API | Check if you can log in to the Sure Petcare app with the given username/password. |
