# Sure Petcare Binding

This binding offers integration to the Sure Petcare API, supporting cloud-connected pet/cat flaps, feeders and waterstations.

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

This binding supports the following thing types

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

Channel names in **bold** are read/write, everything else is read-only

### Bridge Thing

| Channel     | Type   | Description                                                                       |
|-------------|--------|-----------------------------------------------------------------------------------|
| refresh     | Switch | Trigger switch to force a full cache update                                       |

### Household Thing

| Channel    | Type     | Description                                  |
|------------|----------|----------------------------------------------|
| id         | Number   | A unique id assigned by the Sure Petcare API |
| name       | Text     | The name of the household                    |
| timezoneId | Number   | The id of the household's timezone           |

### Hub Device Thing

| Channel         | Type     | Description                                                           |
|-----------------|----------|-----------------------------------------------------------------------|
| id              | Number   | A unique id assigned by the Sure Petcare API                          |
| name            | Text     | The name of the hub                                                   |
| product         | Text     | The type of product (1=hub)                                           |
| ledMode         | Text     | The mode of the hub's LED ears                                        |
| pairingMode     | Text     | The state of pairing                                                  |
| online          | Switch   | Indicator if the hub is connected to the internet                     |

### Flap Device Thing (Cat or Pet Flap)

| Channel            | Type                     | Description                                                |
|--------------------|--------------------------|------------------------------------------------------------|
| id                 | Number                   | A unique id assigned by the Sure Petcare API               |
| name               | Text                     | The name of the flap                                       |
| product            | Text                     | The type of product (3=pet flap, 6=cat flap)               |
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
| id                | Number                    | A unique id assigned by the Sure Petcare API                                                    |
| name              | Text                      | The name of the feeder                                                                          |
| product           | Text                      | The type of product                                                                             |
| online            | Switch                    | Indicator if the feeder is connected to the hub                                                 |
| lowBattery        | Switch                    | Indicator if the battery voltage is low                                                         |
| batteryLevel      | Number:Dimensionless      | The battery voltage percentage in %                                                             |
| batteryVoltage    | Number:ElectricPotential  | The absolute battery voltage measurement in V                                                   |
| deviceRSSI        | Number:Power              | The received device signal strength in dBm                                                      |
| hubRSSI           | Number:Power              | The received hub signal strength in dBm                                                         |
| bowls             | Text                      | The feeder bowls type (1 big bowl or 2 half bowls)                                              |
| bowlsFood         | Text                      | The feeder big bowl food type (wet food, dry food or both)                                      |
| bowlsTarget       | Number:Mass               | The feeder big bowl target weight in gram (even if user setting is oz, API stores this in gram) |
| bowlsFoodLeft     | Text                      | The feeder left half bowl food type (wet food, dry food or both)                                |
| bowlsTargetLeft   | Number:Mass               | The feeder left half bowl target weight in g                                                    |
| bowlsFoodRight    | Text                      | The feeder right half bowl food type (wet food, dry food or both)                               |
| bowlsTargetRight  | Number:Mass               | The feeder right half bowl target weight in g                                                   |
| bowlsCloseDelay   | Text                      | The feeder lid close delay (fast, normal, slow)                                                 |
| bowlsTrainingMode | Text                      | The feeder training mode (off, full open, almost full open, half closed, almost closed)         |

### Waterstation Device Thing

| Channel           | Type                      | Description                                            |
|-------------------|---------------------------|--------------------------------------------------------|
| id                | Number                    | A unique id assigned by the Sure Petcare API           |
| name              | Text                      | The name of the waterstation                           |
| product           | Text                      | The type of product                                    |
| online            | Switch                    | Indicator if the waterstation is connected to the hub  |
| lowBattery        | Switch                    | Indicator if the battery voltage is low                |
| batteryLevel      | Number:Dimensionless      | The battery voltage percentage in %                    |
| batteryVoltage    | Number:ElectricPotential  | The absolute battery voltage measurement in V          |
| deviceRSSI        | Number:Power              | The received device signal strength in dBm             |
| hubRSSI           | Number:Power              | The received hub signal strength in dBm                |

### Pet Thing

| Channel                | Type           | Description                                                                         |
|------------------------|----------------|-------------------------------------------------------------------------------------|
| id                     | Number         | A unique id assigned by the Sure Petcare API                                        |
| name                   | Text           | The name of the pet                                                                 |
| comment                | Text           | A user provided comment/description                                                 |
| gender                 | Text           | The pet's gender                                                                    |
| breed                  | Text           | The pet's breed                                                                     |
| species                | Text           | The pet's species                                                                   |
| photo                  | Image          | The image of the pet                                                                |
| tagIdentifier          | Text           | The unique identifier of the pet's micro chip or collar tag                         |
| location               | Text           | The current location of the pet (0=unknown, 1=inside, 2=outside)                    |
| locationChanged        | DateTime       | The time when the location was last changed                                         |
| locationTimeoffset     | String         | Time-Command to set the pet location with a time offset. (10, 30 or 60 minutes ago) |
| locationChangedThrough | Text           | The device name or username where the pet left/entered the house                    |
| weight                 | Number:Mass    | The pet's weight (in gram)                                                          |
| dateOfBirth            | DateTime       | The pet's date of birth                                                             |
| feederDevice           | Text           | The device from which the pet last ate                                              |
| feederLastChange       | Number:Mass    | The last eaten change in gram (big bowl)                                            |
| feederLastChangeLeft   | Number:Mass    | The last eaten change in gram (half bowl left)                                      |
| feederLastChangeRight  | Number:Mass    | The last eaten change in gram (half bowl right)                                     |
| feederLastFeeding      | DateTime       | The pet's last eaten date                                                           |
| waterDevice            | Text           | The device from which the pet last drunk                                            |
| waterLastChange        | Number:Volume  | The last drinking change in milliliter (ml)                                         |
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

<details>
<summary>New Items with Points (semantic model)</summary>
Choose "Create Equipment from Thing" -> select Thing -> Channels click on Expert Mode

```java
// Equipment representing thing:
// surepetcare:bridge:api
// (SurePetcare API Konto)
Group     gSurePetcare           "SurePetcare API Konto"    (gLivingRoom)     ["WebService"]
Switch    SurePet_API_Refresh    "Aktualisieren"            (gSurePetcare)    ["Point"]         {channel="surepetcare:bridge:api:refresh"}

// Equipment representing thing:
// surepetcare:household:api:CHANGE_ME
// (SurePetcare Haushalt EddyMurphy)
Group     gSurePetcareHousehold            "SurePetcare Haushalt EddyMurphy"    (gSurePetcare)             ["Equipment"]
Number    SurePet_HouseholdId_1            "Haushalt ID"                        (gSurePetcareHousehold)    ["Point"]        {channel="surepetcare:household:api:CHANGE_ME:id"}
String    SurePet_HouseholdName_1          "Haushalt Name"                      (gSurePetcareHousehold)    ["Point"]        {channel="surepetcare:household:api:CHANGE_ME:name"}
Number    SurePet_HouseholdTimezoneId_1    "Haushalt Zeitzone ID"               (gSurePetcareHousehold)    ["Point"]        {channel="surepetcare:household:api:CHANGE_ME:timezoneId"}

Group    gSurePetcareDevices    "SurePetcare Geräte"    (gSurePetcareHousehold)    ["Equipment"]
// Equipment representing thing:
// surepetcare:hubDevice:api:CHANGE_ME
// (SurePetcare Hub Home)
Group     gSurePetcareHub             "SurePetcare Hub Home"    (gSurePetcareDevices)    ["Equipment"]
Number    SurePet_HubId_1             "Hub ID"                  (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:id"}
String    SurePet_HubName_1           "Hub Name"                (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:name"}
String    SurePet_HubProduct_1        "Hub Produkt Typ"         (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:product"}
String    SurePet_HubLedMode_1        "Hub Led Modus"           (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:ledMode"}
String    SurePet_HubPairingMode_1    "Hub Paarungs Modus"      (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:pairingMode"}
Switch    SurePet_HubOnline_1         "Hub Online Status"       (gSurePetcareHub)        ["Point"]        {channel="surepetcare:hubDevice:api:CHANGE_ME:online"}

// Equipment representing thing:
// surepetcare:flapDevice:api:CHANGE_ME
// (SurePetcare Klappe Bad)
Group                       gSurePetcareFlap                   "SurePetcare Klappe Bad"                                      (gSurePetcareDevices)    ["Equipment"]
Number                      SurePet_FlapId_1                   "Flap ID"                                                     (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:id"}
String                      SurePet_FlapName_1                 "Flap Name"                                                   (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:name"}
String                      SurePet_FlapProduct_1              "Flap Produkt Typ"                                            (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:product"}
Switch                      SurePet_FlapCurfewEnabled1_1       "Flap Ausgangssperre 1 Aktiv"                                 (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewEnabled1"}
String                      SurePet_FlapCurfewLockTime1_1      "Flap Ausgangssperre 1 Sperrzeit"                             (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewLockTime1"}
String                      SurePet_FlapCurfewUnlockTime1_1    "Flap Ausgangssperre 1 Entsperrzeit"                          (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewUnlockTime1"}
Switch                      SurePet_FlapCurfewEnabled2_1       "Flap Ausgangssperre 2 Aktiv"                                 (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewEnabled2"}
String                      SurePet_FlapCurfewLockTime2_1      "Flap Ausgangssperre 2 Sperrzeit"                             (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewLockTime2"}
String                      SurePet_FlapCurfewUnlockTime2_1    "Flap Ausgangssperre 2 Entsperrzeit"                          (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewUnlockTime2"}
Switch                      SurePet_FlapCurfewEnabled3_1       "Flap Ausgangssperre 3 Aktiv"                                 (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewEnabled3"}
String                      SurePet_FlapCurfewLockTime3_1      "Flap Ausgangssperre 3 Sperrzeit"                             (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewLockTime3"}
String                      SurePet_FlapCurfewUnlockTime3_1    "Flap Ausgangssperre 3 Entsperrzeit"                          (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewUnlockTime3"}
Switch                      SurePet_FlapCurfewEnabled4_1       "Flap Ausgangssperre 4 Aktiv"                                 (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewEnabled4"}
String                      SurePet_FlapCurfewLockTime4_1      "Flap Ausgangssperre 4 Sperrzeit"                             (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewLockTime4"}
String                      SurePet_FlapCurfewUnlockTime4_1    "Flap Ausgangssperre 4 Entsperrzeit"                          (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:curfewUnlockTime4"}
String                      SurePet_FlapLockingMode_1          "Flap Sperrmodus"                       <lock>                (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:lockingMode"}
Switch                      SurePet_FlapLowBattery_1           "Flap Niedriger Batteriestatus"         <LowBattery>          (gSurePetcareFlap)       ["Energy", "LowBattery"]     {channel="surepetcare:flapDevice:api:CHANGE_ME:lowBattery"}
Number:Dimensionless        SurePet_FlapBatteryLevel_1         "Flap Batterieladung"                   <Battery>             (gSurePetcareFlap)       ["Measurement", "Energy"]    {channel="surepetcare:flapDevice:api:CHANGE_ME:batteryLevel", stateDescription="%.0f %%", unit="%"}
Number:ElectricPotential    SurePet_FlapBatteryVoltage_1       "Flap Batterie Spannung"                <energy>              (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:batteryVoltage", stateDescription="%.1f V", unit="V"}
Switch                      SurePet_FlapOnline_1               "Flap Online Status"                                          (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:online"}
Number:Power                SurePet_FlapDeviceRSSI_1           "Flap Signalstärke (Gerät)"             <qualityofservice>    (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:deviceRSSI", stateDescription="%.2f dBm", unit="dBm"}
Number:Power                SurePet_FlapHubRSSI_1              "Flap Signalstärke (Hub)"               <qualityofservice>    (gSurePetcareFlap)       ["Point"]                    {channel="surepetcare:flapDevice:api:CHANGE_ME:hubRSSI", stateDescription="%.2f dBm", unit="dBm"}

// Equipment representing thing:
// surepetcare:feederDevice:api:CHANGE_ME
// (SurePetcare Futterautomat Luna)
Group                       gSurePetcareDevice1                  "SurePetcare Futterautomat Luna"                                              (gSurePetcareDevices)    ["Equipment"]
Number                      SurePet_FeederId_1                   "Futterautomat ID"                                                            (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:id"}
String                      SurePet_FeederName_1                 "Futterautomat Name"                                                          (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:name"}
String                      SurePet_FeederProduct_1              "Futterautomat Produkt Typ"                                                   (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:product"}
Switch                      SurePet_FeederLowBattery_1           "Futterautomat Niedriger Batteriestatus"                <LowBattery>          (gSurePetcareDevice1)    ["Energy", "LowBattery"]     {channel="surepetcare:feederDevice:api:CHANGE_ME:lowBattery"}
Number:Dimensionless        SurePet_FeederBatteryLevel_1         "Futterautomat Batterieladung"                          <Battery>             (gSurePetcareDevice1)    ["Measurement", "Energy"]    {channel="surepetcare:feederDevice:api:CHANGE_ME:batteryLevel", stateDescription="%.0f %%", unit="%"}
Number:ElectricPotential    SurePet_FeederBatteryVoltage_1       "Futterautomat Batterie Spannung"                       <energy>              (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:batteryVoltage", stateDescription="%.1f V", unit="V"}
Switch                      SurePet_FeederOnline_1               "Futterautomat Online Status"                                                 (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:online"}
Number:Power                SurePet_FeederDeviceRSSI_1           "Futterautomat Signalstärke (Gerät)"                    <qualityofservice>    (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:deviceRSSI", stateDescription="%.2f dBm", unit="dBm"}
Number:Power                SurePet_FeederHubRSSI_1              "Futterautomat Signalstärke (Hub)"                      <qualityofservice>    (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:hubRSSI", stateDescription="%.2f dBm", unit="dBm"}
String                      SurePet_FeederBowlsFood_1            "Futterautomat Napf Futter Typ (großer Napf)"                                 (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFood"}
Number:Mass                 SurePet_FeederBowlsTarget_1          "Futterautomat Napf Gewicht (großer Napf)"                                    (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTarget", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowlsFoodLeft_1        "Futterautomat Napf Futter Typ links (halbe Näpfe)"                           (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFoodLeft"}
Number:Mass                 SurePet_FeederBowlsTargetLeft_1      "Futterautomat Napf Gewicht links (halbe Näpfe)"                              (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTargetLeft", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowlsFoodRight_1       "Futterautomat Napf Futter Typ rechts (halbe Näpfe)"                          (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFoodRight"}
Number:Mass                 SurePet_FeederBowlsTargetRight_1     "Futterautomat Napf Gewicht rechts (halbe Näpfe)"                             (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTargetRight", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowls_1                "Futterautomat Napf Typ"                                                      (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowls"}
String                      SurePet_FeederBowlsCloseDelay_1      "Futterautomat Deckel Schließverzögerung"                                     (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsCloseDelay"}
String                      SurePet_FeederBowlsTrainingMode_1    "Futterautomat Futterautomat Training Modus"                                  (gSurePetcareDevice1)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTrainingMode"}

// Equipment representing thing:
// surepetcare:feederDevice:api:CHANGE_ME
// (SurePetcare Futterautomat Rudi)
Group                       gSurePetcareDevice2                  "SurePetcare Futterautomat Rudi"                                              (gSurePetcareDevices)    ["Equipment"]
Number                      SurePet_FeederId_2                   "Futterautomat ID"                                                            (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:id"}
String                      SurePet_FeederName_2                 "Futterautomat Name"                                                          (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:name"}
String                      SurePet_FeederProduct_2              "Futterautomat Produkt Typ"                                                   (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:product"}
Switch                      SurePet_FeederLowBattery_2           "Futterautomat Niedriger Batteriestatus"                <LowBattery>          (gSurePetcareDevice2)    ["Energy", "LowBattery"]     {channel="surepetcare:feederDevice:api:CHANGE_ME:lowBattery"}
Number:Dimensionless        SurePet_FeederBatteryLevel_2         "Futterautomat Batterieladung"                          <Battery>             (gSurePetcareDevice2)    ["Measurement", "Energy"]    {channel="surepetcare:feederDevice:api:CHANGE_ME:batteryLevel", stateDescription="%.0f %%", unit="%"}
Number:ElectricPotential    SurePet_FeederBatteryVoltage_2       "Futterautomat Batterie Spannung"                       <energy>              (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:batteryVoltage", stateDescription="%.1f V", unit="V"}
Switch                      SurePet_FeederOnline_2               "Futterautomat Online Status"                                                 (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:online"}
Number:Power                SurePet_FeederDeviceRSSI_2           "Futterautomat Signalstärke (Gerät)"                    <qualityofservice>    (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:deviceRSSI", stateDescription="%.2f dBm", unit="dBm"}
Number:Power                SurePet_FeederHubRSSI_2              "Futterautomat Signalstärke (Hub)"                      <qualityofservice>    (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:hubRSSI", stateDescription="%.2f dBm", unit="dBm"}
String                      SurePet_FeederBowlsFood_2            "Futterautomat Napf Futter Typ (großer Napf)"                                 (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFood"}
Number:Mass                 SurePet_FeederBowlsTarget_2          "Futterautomat Napf Gewicht (großer Napf)"                                    (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTarget", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowlsFoodLeft_2        "Futterautomat Napf Futter Typ links (halbe Näpfe)"                           (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFoodLeft"}
Number:Mass                 SurePet_FeederBowlsTargetLeft_2      "Futterautomat Napf Gewicht links (halbe Näpfe)"                              (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTargetLeft", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowlsFoodRight_2       "Futterautomat Napf Futter Typ rechts (halbe Näpfe)"                          (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsFoodRight"}
Number:Mass                 SurePet_FeederBowlsTargetRight_2     "Futterautomat Napf Gewicht rechts (halbe Näpfe)"                             (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTargetRight", stateDescription="%.0f g", unit="g"}
String                      SurePet_FeederBowls_2                "Futterautomat Napf Typ"                                                      (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowls"}
String                      SurePet_FeederBowlsCloseDelay_2      "Futterautomat Deckel Schließverzögerung"                                     (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsCloseDelay"}
String                      SurePet_FeederBowlsTrainingMode_2    "Futterautomat Futterautomat Training Modus"                                  (gSurePetcareDevice2)    ["Point"]                    {channel="surepetcare:feederDevice:api:CHANGE_ME:bowlsTrainingMode"}

// Equipment representing thing:
// surepetcare:waterDevice:api:CHANGE_ME
// (SurePetcare - Poseidon)
Group                       gSurePetcareDevice3                "Wasserstation Poseidon"                                          (gSurePetcareDevices)    ["Equipment"]
Number                      SurePet_Poseidon_ID                "Wasserstation ID"                                                (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:id"}
String                      SurePet_Poseidon_Name              "Wasserstation Name"                                              (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:name"}
String                      SurePet_Poseidon_Product           "Wasserstation Produkt Typ"                                       (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:product"}
Switch                      SurePet_Poseidon_LowBattery        "Wasserstation Niedriger Batteriestatus"    <LowBattery>          (gSurePetcareDevice3)    ["Energy", "LowBattery"]     {channel="surepetcare:waterDevice:api:CHANGE_ME:lowBattery"}
Number:Dimensionless        SurePet_Poseidon_BatteryLevel      "Wasserstation Batterieladung"              <Battery>             (gSurePetcareDevice3)    ["Measurement", "Energy"]    {channel="surepetcare:waterDevice:api:CHANGE_ME:batteryLevel", stateDescription="%.0f %%", unit="%"}
Number:ElectricPotential    SurePet_Poseidon_BatteryVolatge    "Wasserstation Batterie Spannung"           <energy>              (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:batteryVoltage", stateDescription="%.1f V", unit="V"}
Switch                      SurePet_Poseidon_Online            "Wasserstation Online Status"                                     (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:online"}
Number:Power                SurePet_Poseidon_DeviceRSSI        "Wasserstation Signalstärke (Gerät)"        <qualityofservice>    (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:deviceRSSI", stateDescription="%.2f dBm", unit="dBm"}
Number:Power                SurePet_Poseidon_HubRSSI           "Wasserstation Signalstärke (Hub)"          <qualityofservice>    (gSurePetcareDevice3)    ["Point"]                    {channel="surepetcare:waterDevice:api:CHANGE_ME:hubRSSI", stateDescription="%.2f dBm", unit="dBm"}


Group    gSurePetcarePets    "SurePetcare Haustiere"    (gSurePetcareHousehold)    ["Equipment"]
// Equipment representing thing:
// surepetcare:pet:api:CHANGE_ME
// (SurePetcare Haustier Luna)
Group            gSurePetcarePet1                    "SurePetcare Haustier Luna"                                     (gSurePetcarePets)    ["Equipment"]
Number           SurePet_Id_1                        "Tier ID"                                                       (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:id"}
String           SurePet_Name_1                      "Tier Name"                                                     (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:name"}
String           SurePet_Comment_1                   "Tier Kommentar"                                                (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:comment"}
String           SurePet_Gender_1                    "Tier Geschlecht"                                               (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:gender"}
String           SurePet_Breed_1                     "Tier Rasse"                                                    (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:breed"}
String           SurePet_Species_1                   "Tier Tierart"                                                  (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:species"}
Image            SurePet_Photo_1                     "Tier Foto"                                                     (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:photo"}
String           SurePet_TagIdentifier_1             "Tier Mikrochip"                                                (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:tagIdentifier"}
String           SurePet_Location_1                  "Wo ist Luna"                                     <motion>      (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:location"}
DateTime         SurePet_LocationChanged_1           "Tier Standort Zeit"                                            (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationChanged", stateDescription="%1$ta. %1$tH:%1$tM"}
String           SurePet_LocationTimeoffset_1        "Tier Standortwechsel Zeitversatz"                              (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationTimeoffset"}
String           SurePet_LocationChangedThrough_1    "Tier Standort geändert durch"                                  (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationChangedThrough"}
DateTime         SurePet_DateOfBirth_1               "Tier Geburtstag"                                 <calendar>    (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:dateOfBirth", stateDescription="%1$td.%1$tm.%1$tY"}
Number:Mass      SurePet_Weight_1                    "Tier Gewicht"                                                  (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:weight", stateDescription="%.1f kg", unit="g"}
String           SurePet_FeederDevice_1              "Tier Futterautomat Name"                                       (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederDevice"}
DateTime         SurePet_FeederLastFeeding_1         "Tier Letzte Futteraufnahme"                      <time>        (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastFeeding", stateDescription="%1$ta. %1$tH:%1$tM"}
Number:Mass      SurePet_FeederLastChange_1          "Tier Letzte Futteraufnahme Änderung"                           (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChange", stateDescription="%.1f g", unit="g"}
Number:Mass      SurePet_FeederLastChangeLeft_1      "Tier Letzte Futteraufnahme Änderung (links)"                   (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChangeLeft", stateDescription="%.1f g", unit="g"}
Number:Mass      SurePet_FeederLastChangeRight_1     "Tier Letzte Futteraufnahme Änderung (rechts)"                  (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChangeRight", stateDescription="%.1f g", unit="g"}
String           SurePet_WaterDevice_1               "Tier Wasserstation Name"                                       (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterDevice"}
DateTime         SurePet_WaterLastDrinking_1         "Tier Letzte Wasseraufnahme"                      <time>        (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterLastDrinking", stateDescription="%1$ta. %1$tH:%1$tM"}
Number:Volume    SurePet_WaterLastChange_1           "Tier Letzte Wasseraufnahme Änderung"                           (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterLastChange", stateDescription="%.1f ml", unit="ml"}

// Equipment representing thing:
// surepetcare:pet:api:CHANGE_ME
// (SurePetcare Haustier Rudi)
Group            gSurePetcarePet2                    "SurePetcare Haustier Rudi"                                     (gSurePetcarePets)    ["Equipment"]
Number           SurePet_Id_2                        "Tier ID"                                                       (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:id"}
String           SurePet_Name_2                      "Tier Name"                                                     (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:name"}
String           SurePet_Comment_2                   "Tier Kommentar"                                                (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:comment"}
String           SurePet_Gender_2                    "Tier Geschlecht"                                               (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:gender"}
String           SurePet_Breed_2                     "Tier Rasse"                                                    (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:breed"}
String           SurePet_Species_2                   "Tier Tierart"                                                  (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:species"}
Image            SurePet_Photo_2                     "Tier Foto"                                                     (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:photo"}
String           SurePet_TagIdentifier_2             "Tier Mikrochip"                                                (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:tagIdentifier"}
String           SurePet_Location_2                  "Wo ist Rudi"                                     <motion>      (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:location"}
DateTime         SurePet_LocationChanged_2           "Tier Standort Zeit"                                            (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationChanged", stateDescription="%1$ta. %1$tH:%1$tM"}
String           SurePet_LocationTimeoffset_2        "Tier Standortwechsel Zeitversatz"                              (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationTimeoffset"}
String           SurePet_LocationChangedThrough_2    "Tier Standort geändert durch"                                  (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:locationChangedThrough"}
DateTime         SurePet_DateOfBirth_2               "Tier Geburtstag"                                 <calendar>    (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:dateOfBirth", stateDescription="%1$td.%1$tm.%1$tY"}
Number:Mass      SurePet_Weight_2                    "Tier Gewicht"                                                  (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:weight", stateDescription="%.1f kg", unit="g"}
String           SurePet_FeederDevice_2              "Tier Futterautomat Name"                                       (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederDevice"}
DateTime         SurePet_FeederLastFeeding_2         "Tier Letzte Futteraufnahme"                      <time>        (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastFeeding", stateDescription="%1$ta. %1$tH:%1$tM"}
Number:Mass      SurePet_FeederLastChange_2          "Tier Letzte Futteraufnahme Änderung"                           (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChange", stateDescription="%.1f g", unit="g"}
Number:Mass      SurePet_FeederLastChangeLeft_2      "Tier Letzte Futteraufnahme Änderung (links)"                   (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChangeLeft", stateDescription="%.1f g", unit="g"}
Number:Mass      SurePet_FeederLastChangeRight_2     "Tier Letzte Futteraufnahme Änderung (rechts)"                  (gSurePetcarePet2)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:feederLastChangeRight", stateDescription="%.1f g", unit="g"}
String           SurePet_WaterDevice_2               "Tier Wasserstation Name"                                       (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterDevice"}
DateTime         SurePet_WaterLastDrinking_2         "Tier Letzte Wasseraufnahme"                      <time>        (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterLastDrinking", stateDescription="%1$ta. %1$tH:%1$tM"}
Number:Volume    SurePet_WaterLastChange_2           "Tier Letzte Wasseraufnahme Änderung"                           (gSurePetcarePet1)    ["Point"]        {channel="surepetcare:pet:api:CHANGE_ME:waterLastChange", stateDescription="%.1f ml", unit="ml"}
```

</details>

<details>
<summary>Old Items file</summary>

```java
/* *****************************************
 * Bridge
 * *****************************************/
Group   dgPet
Switch  UR_1a_Online       "Bridge Online [%s]"          (dgPet)  {channel="surepetcare:bridge:bridge1:online"}
Switch  UR_1a_Refresh      "Bridge Data Refresh [%s]"    (dgPet)  {channel="surepetcare:bridge:bridge1:refresh"}

/* *****************************************
 * Household
 * *****************************************/
Number  UR_1b_Id           "Household Id [%d]"           (dgPet)  {channel="surepetcare:household:bridge1:12345:id"}
String  UR_1b_Name         "Household Name [%s]"         (dgPet)  {channel="surepetcare:household:bridge1:12345:name"}
Number  UR_1b_TimezoneId   "Household Timezone Id [%d]"  (dgPet)  {channel="surepetcare:household:bridge1:12345:timezoneId"}

/* *****************************************
 * Hub
 * *****************************************/
Number  UR_1c_Id           "Hub Id [%d]"                 (dgPet)  {channel="surepetcare:hubDevice:bridge1:123456:id"}
String  UR_1c_Name         "Hub Name [%s]"               (dgPet)  {channel="surepetcare:hubDevice:bridge1:123456:name"}
String  UR_1c_Product      "Hub Product [%s]"            (dgPet)  {channel="surepetcare:hubDevice:bridge1:123456:product"}
String  UR_1c_LEDMode      "Hub LED Mode [%s]"           (dgPet)  {channel="surepetcare:hubDevice:bridge1:123456:ledMode"}
String  UR_1c_PairingMode  "Hub Pairing Mode [%s]"       (dgPet)  {channel="surepetcare:hubDevice:bridge1:123456:pairingMode"}
Switch  UR_1c_Online       "Hub Online [%s]"             (dgPet)  {channel="surepetcare:hubDevice:bridge1:123456:online"}

/* *****************************************
 * Cat/Pet Flap
 * *****************************************/
Number                    UR_1d_Id                 "Flap Id [%d]"                    (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:id"}
String                    UR_1d_Name               "Flap Name [%s]"                  (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:name"}
String                    UR_1d_Product            "Flap Product [%s]"               (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:product"}
Switch                    UR_1d_CurfewEnabled1     "Flap Curfew 1 Enabled [%s]"      (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewEnabled1"}
String                    UR_1d_CurfewLockTime1    "Flap Curfew 1 Lock Time [%s]"    (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewLockTime1"}
String                    UR_1d_CurfewUnlockTime1  "Flap Curfew 1 Unlock Time [%s]"  (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewUnlockTime1"}
Switch                    UR_1d_CurfewEnabled2     "Flap Curfew 2 Enabled [%s]"      (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewEnabled2"}
String                    UR_1d_CurfewLockTime2    "Flap Curfew 2 Lock Time [%s]"    (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewLockTime2"}
String                    UR_1d_CurfewUnlockTime2  "Flap Curfew 2 Unlock Time [%s]"  (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewUnlockTime2"}
Switch                    UR_1d_CurfewEnabled3     "Flap Curfew 3 Enabled [%s]"      (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewEnabled3"}
String                    UR_1d_CurfewLockTime3    "Flap Curfew 3 Lock Time [%s]"    (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewLockTime3"}
String                    UR_1d_CurfewUnlockTime3  "Flap Curfew 3 Unlock Time [%s]"  (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewUnlockTime3"}
Switch                    UR_1d_CurfewEnabled4     "Flap Curfew 4 Enabled [%s]"      (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewEnabled4"}
String                    UR_1d_CurfewLockTime4    "Flap Curfew 4 Lock Time [%s]"    (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewLockTime4"}
String                    UR_1d_CurfewUnlockTime5  "Flap Curfew 4 Unlock Time [%s]"  (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:curfewUnlockTime4"}
String                    UR_1d_LockingMode        "Flap Locking Mode [%s]"          (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:lockingMode"}
Switch                    UR_1d_LowBattery         "Flap Low Battery [%s]"           (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:lowBattery"}
Number:Dimensionless      UR_1d_BatteryLevel       "Flap Battery Level [%.0f %%]"    (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:batteryLevel"}
Number:ElectricPotential  UR_1d_BatteryVoltage     "Flap Battery Voltage [%.1f V]"   (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:batteryVoltage"}
Switch                    UR_1d_Online             "Flap Online [%s]"                (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:online"}
Number:Power              UR_1d_DeviceRSSI         "Flap Device RSSI [%.2f dBm]"     (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:deviceRSSI"}
Number:Power              UR_1d_HubRSSI            "Flap Hub RSSI [%.2f dBm]"        (dgPet)  {channel="surepetcare:flapDevice:bridge1:123456:hubRSSI"}

/* *****************************************
 * Pet
 * *****************************************/
Number         UR_1e_Id                  "Pet Id [%d]"                            (dgPet)  {channel="surepetcare:pet:bridge1:12345:id"}
String         UR_1e_Name                "Pet Name [%s]"                          (dgPet)  {channel="surepetcare:pet:bridge1:12345:name"}
String         UR_1e_Comment             "Pet Comment [%s]"                       (dgPet)  {channel="surepetcare:pet:bridge1:12345:comment"}
String         UR_1e_Gender              "Pet Gender [%s]"                        (dgPet)  {channel="surepetcare:pet:bridge1:12345:gender"}
String         UR_1e_Breed               "Pet Breed [%s]"                         (dgPet)  {channel="surepetcare:pet:bridge1:12345:breed"}
String         UR_1e_Species             "Pet Species [%s]"                       (dgPet)  {channel="surepetcare:pet:bridge1:12345:species"}
Image          UR_1e_Photo               "Pet Photo"                              (dgPet)  {channel="surepetcare:pet:bridge1:12345:photo"}
String         UR_1e_TagIdentifier       "Pet Tag Identifier [%s]"                (dgPet)  {channel="surepetcare:pet:bridge1:12345:tagIdentifier"}
String         UR_1e_Location            "Pet Location [%s]"                      (dgPet)  {channel="surepetcare:pet:bridge1:12345:location"}
String         UR_1e_LocationTimeoffset  "Pet Switch Location [%s]"               (gCats)  {channel="surepetcare:pet:bridge1:20584:locationTimeoffset"}
DateTime       UR_1e_LocationChanged     "Pet Loc. Updated [%1$ta. %1$tH:%1$tM]"  (dgPet)  {channel="surepetcare:pet:bridge1:12345:locationChanged"}
String         UR_1e_LocationThrough     "Pet Entered / Left through [%s]"        (dgPet)  {channel="surepetcare:pet:bridge1:12345:locationChangedThrough"}
Number:Mass    UR_1e_Weight              "Pet Weight [%.1f %unit%]"               (dgPet)  {channel="surepetcare:pet:bridge1:12345:weight"}
DateTime       UR_1e_DateOfBirth         "Pet Date of Birth [%1$td.%1$tm.%1$tY]"  (dgPet)  {channel="surepetcare:pet:bridge1:12345:dateOfBirth"}
// Pet Feeder Data
String         UR_1e_Device              "Device Name [%s]"                       (dgPet)  {channel="surepetcare:pet:bridge1:12345:feederDevice"}
Number:Mass    UR_1e_Change              "Change: [%.2f %unit%]"                  (dgPet)  {channel="surepetcare:pet:bridge1:12345:feederLastChange"}
Number:Mass    UR_1e_ChangeLeft          "Change: L [%.2f %unit%]"                (dgPet)  {channel="surepetcare:pet:bridge1:12345:feederLastChangeLeft"}
Number:Mass    UR_1e_ChangeRight         "Change: R [%.2f %unit%]"                (dgPet)  {channel="surepetcare:pet:bridge1:12345:feederLastChangeRight"}
DateTime       UR_1e_FeedAt              "Last Feeding [%1$ta. %1$tH:%1$tM]"      (dgPet)  {channel="surepetcare:pet:bridge1:12345:feederLastFeeding"}
// Pet Water Data
String         UR_1e_WaterDevice         "Device Name [%s]"                       (dgPet)  {channel="surepetcare:pet:bridge1:12345:waterDevice"}
Number:Volume  UR_1e_WaterChange         "Change: [%.2f %unit%]"                  (dgPet)  {channel="surepetcare:pet:bridge1:12345:waterLastChange"}
DateTime       UR_1e_WaterDrunkAt        "Last Drinking [%1$ta. %1$tH:%1$tM]"     (dgPet)  {channel="surepetcare:pet:bridge1:12345:waterLastDrinking"}

/* *****************************************
 * Pet Feeder
 * *****************************************/
Number                    UR_1f_Id                  "Feeder ID [%s]"                         (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:id"}
String                    UR_1f_Name                "Feeder Name [%s]"                       (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:name"}
String                    UR_1f_Product             "Feeder Product [%s]"                    (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:product"}
Switch                    UR_1f_LowBattery          "Feeder Low Battery [%s]"                (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:lowBattery"}
Number:Dimensionless      UR_1f_BatteryLevel        "Feeder Battery Level [%.0f %%]"         (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:batteryLevel"}
Number:ElectricPotential  UR_1f_BatteryVoltage      "Feeder Battery Voltage [%.2f V]"        (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:batteryVoltage"}
String                    UR_1f_BowlsType           "Feeder Bowls Type [%s]"                 (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowls"}
String                    UR_1f_BowlsFoodtype       "Feeder Food Type [%s]"                  (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowlsFood"}
Number:Mass               UR_1f_BowlsTarget         "Feeder Target [%.0f %unit%]"            (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowlsTarget"}
String                    UR_1f_BowlsFoodtypeLeft   "Feeder Food Type L [%s]"                (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowlsFoodLeft"}
Number:Mass               UR_1f_BowlsTargetLeft     "Feeder Target L [%.0f %unit%]"          (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowlsTargetLeft"}
String                    UR_1f_BowlsFoodtypeRight  "Feeder Food Type R [%s]"                (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowlsFoodRight"}
Number:Mass               UR_1f_BowlsTargetRight    "Feeder Target R [%.0f %unit%]"          (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowlsTargetRight"}
String                    UR_1f_BowlsLidCloseDelay  "Feeder Close Delay [%s]"                (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowlsCloseDelay"}
String                    UR_1f_BowlsTrainingMode   "Feeder Training Mode [%s]"              (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:bowlsTrainingMode"}
Switch                    UR_1f_Online              "Feeder Status [%s]"                     (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:online"}
Number:Power              UR_1f_DeviceRSSI          "Feeder Device Signal [%.2f dBm]"        (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:deviceRSSI"}
Number:Power              UR_1f_HubRSSI             "Feeder Hub Signal [%.2f dBm]"           (dgPet)  {channel="surepetcare:feederDevice:bridge1:123456:hubRSSI"}

/* *****************************************
 * Pet Waterstation
 * *****************************************/
Number                    UR_1g_Id                  "Waterstation ID [%s]"                   (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:id"}
String                    UR_1g_Name                "Waterstation Name [%s]"                 (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:name"}
String                    UR_1g_Product             "Waterstation Product [%s]"              (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:product"}
Switch                    UR_1g_LowBattery          "Waterstation Low Battery [%s]"          (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:lowBattery"}
Number:Dimensionless      UR_1g_BatteryLevel        "Waterstation Battery Level [%.0f %%]"   (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:batteryLevel"}
Number:ElectricPotential  UR_1g_BatteryVoltage      "Waterstation Battery Voltage [%.2f V]"  (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:batteryVoltage"}
Switch                    UR_1g_Online              "Waterstation Status [%s]"               (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:online"}
Number:Power              UR_1g_DeviceRSSI          "Waterstation Device Signal [%.2f dBm]"  (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:deviceRSSI"}
Number:Power              UR_1g_HubRSSI             "Waterstation Hub Signal [%.2f dBm]"     (dgPet)  {channel="surepetcare:waterDevice:bridge1:123456:hubRSSI"}
```

</details>

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
Please Note: the location for each pet gets updated only if the current location is not already the location you want to set.
This can be very useful if you have alot of pets that often enter the home by any window/door.
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

| Problem                                     | Solution                                                                            |
|---------------------------------------------|-------------------------------------------------------------------------------------|
| Bridge cannot connect to Sure Petcare API    | Check if you can logon to the Sure Petcare app with the given username/password.    |
