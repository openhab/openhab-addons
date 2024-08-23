# Sure Petcare Binding

This binding offers integration to the Sure Petcare API, supporting cloud-connected cat flaps and feeders.

## Features

1. Read access to all attributes for households, devices (hubs, flaps) and pets through individual things/channels.
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

| Thing           | Thing Type | Discovery | Description                                                              |
|-----------------|------------|-----------|--------------------------------------------------------------------------|
| Bridge          | Bridge     | Manual    |  A single connection to the Sure Petcare API                             |
| Household       | Thing      | Automatic |  The Sure Petcare Household                                              |
| Hub Device      | Thing      | Automatic |  The hub device which connects the cat flaps and feeders to the internet |
| Flap Device     | Thing      | Automatic |  A cat or pet flap                                                       |
| Feeder Device   | Thing      | Automatic |  A pet feeder                                                            |
| Pet             | Thing      | Automatic |  A pet (dog or cat)                                                      |

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

| Channel               | Type     | Description                                                           |
|-----------------------|----------|-----------------------------------------------------------------------|
| id                    | Number   | A unique id assigned by the Sure Petcare API                          |
| name                  | Text     | The name of the flap                                                  |
| product               | Text     | The type of product (3=pet flap, 6=cat flap)                          |
| curfewEnabled1        | Switch   | Indicator if curfew #1 configuration is enabled                       |
| curfewLockTime1       | Text     | The curfew #1 locking time (HH:MM)                                    |
| curfewUnlockTime1     | Text     | The curfew #1 unlocking time (HH:MM)                                  |
| curfewEnabled2        | Switch   | Indicator if curfew #2 configuration is enabled                       |
| curfewLockTime2       | Text     | The curfew #2 locking time (HH:MM)                                    |
| curfewUnlockTime2     | Text     | The curfew #2 unlocking time (HH:MM)                                  |
| curfewEnabled3        | Switch   | Indicator if curfew #3 configuration is enabled                       |
| curfewLockTime3       | Text     | The curfew #3 locking time (HH:MM)                                    |
| curfewUnlockTime3     | Text     | The curfew #3 unlocking time (HH:MM)                                  |
| curfewEnabled4        | Switch   | Indicator if curfew #4 configuration is enabled                       |
| curfewLockTime4       | Text     | The curfew #4 locking time (HH:MM)                                    |
| curfewUnlockTime4     | Text     | The curfew #4 unlocking time (HH:MM)                                  |
| lockingMode           | Text     | The locking mode (e.g. in/out, in-only, out-only etc.)                |
| online                | Switch   | Indicator if the flap is connected to the hub                         |
| lowBattery            | Switch   | Indicator if the battery voltage is low                               |
| batteryLevel          | Number   | The battery voltage percentage                                        |
| batteryVoltage        | Number   | The absolute battery voltage measurement                              |
| deviceRSSI            | Number   | The received device signal strength in dB                             |
| hubRSSI               | Number   | The received hub signal strength in dB                                |

### Feeder Device Thing

| Channel           | Type        | Description                                                                                     |
|-------------------|-------------|-------------------------------------------------------------------------------------------------|
| id                | Number      | A unique id assigned by the Sure Petcare API                                                    |
| name              | Text        | The name of the feeder                                                                          |
| product           | Text        | The type of product                                                                             |
| online            | Switch      | Indicator if the feeder is connected to the hub                                                 |
| lowBattery        | Switch      | Indicator if the battery voltage is low                                                         |
| batteryLevel      | Number      | The battery voltage percentage                                                                  |
| batteryVoltage    | Number      | The absolute battery voltage measurement                                                        |
| deviceRSSI        | Number      | The received device signal strength in dB                                                       |
| hubRSSI           | Number      | The received hub signal strength in dB                                                          |
| bowls             | Text        | The feeder bowls type (1 big bowl or 2 half bowls)                                              |
| bowlsFood         | Text        | The feeder big bowl food type (wet food, dry food or both)                                      |
| bowlsTarget       | Number:Mass | The feeder big bowl target weight in gram (even if user setting is oz, API stores this in gram) |
| bowlsFoodLeft     | Text        | The feeder left half bowl food type (wet food, dry food or both)                                |
| bowlsTargetLeft   | Number:Mass | The feeder left half bowl target weight                                                         |
| bowlsFoodRight    | Text        | The feeder right half bowl food type (wet food, dry food or both)                               |
| bowlsTargetRight  | Number:Mass | The feeder right half bowl target weight                                                        |
| bowlsCloseDelay   | Text        | The feeder lid close delay (fast, normal, slow)                                                 |
| bowlsTrainingMode | Text        | The feeder training mode (off, full open, almost full open, half closed, almost closed)         |

### Pet Thing

| Channel                | Type        | Description                                                                         |
|------------------------|-------------|-------------------------------------------------------------------------------------|
| id                     | Number      | A unique id assigned by the Sure Petcare API                                        |
| name                   | Text        | The name of the pet                                                                 |
| comment                | Text        | A user provided comment/description                                                 |
| gender                 | Text        | The pet's gender                                                                    |
| breed                  | Text        | The pet's breed                                                                     |
| species                | Text        | The pet's species                                                                   |
| photo                  | Image       | The image of the pet                                                                |
| tagIdentifier          | Text        | The unique identifier of the pet's micro chip or collar tag                         |
| location               | Text        | The current location of the pet (0=unknown, 1=inside, 2=outside)                    |
| locationChanged        | DateTime    | The time when the location was last changed                                         |
| locationTimeoffset     | String      | Time-Command to set the pet location with a time offset. (10, 30 or 60 minutes ago) |
| locationChangedThrough | Text        | The device name or username where the pet left/entered the house                    |
| weight                 | Number:Mass | The pet's weight (in kilogram)                                                      |
| dateOfBirth            | DateTime    | The pet's date of birth                                                             |
| feederDevice           | Text        | The device from which the pet last ate                                              |
| feederLastChange       | Number:Mass | The last eaten change in gram (big bowl)                                            |
| feederLastChangeLeft   | Number:Mass | The last eaten change in gram (half bowl left)                                      |
| feederLastChangeRight  | Number:Mass | The last eaten change in gram (half bowl right)                                     |
| feederLastFeeding      | DateTime    | The pet's last eaten date                                                           |

## Manual configuration

### Things configuration

```java
Bridge surepetcare:bridge:bridge1 "Demo API Bridge" @ "SurePetcare" [ username="<USERNAME>", password="<PASSWORD>", refreshIntervalTopology=36000, refreshIntervalStatus=300 ]
{
  Thing household     12345  "My Household" @ "SurePetcare"
  Thing hubDevice     123456 "My SurePetcare Hub" @ "SurePetcare Devices"
  Thing flapDevice    123456 "My Backdoor Cat Flap" @ "SurePetcare Devices"
  Thing feederDevice  123456 "My Pet Feeder" @ "SurePetcare Devices"
  Thing pet           12345  "My Cat" @ "SurePetcare Pets"
}
```

### Items configuration

```java
/* *****************************************
 * Bridge
 * *****************************************/
Group   dgPet                                               
Switch  UR_1a_Online    "Bridge Online [%s]"        (dgPet) {channel="surepetcare:bridge:bridge1:online"}
Switch  UR_1a_Refresh   "Bridge Data Refresh [%s]"  (dgPet) {channel="surepetcare:bridge:bridge1:refresh"}

/* *****************************************
 * Household
 * *****************************************/
Number      UR_1b_Id            "Household Id [%d]"                 (dgPet) {channel="surepetcare:household:bridge1:12345:id"}
String      UR_1b_Name          "Household Name [%s]"               (dgPet) {channel="surepetcare:household:bridge1:12345:name"}
Number      UR_1b_TimezoneId    "Household Timezone Id [%d]"        (dgPet) {channel="surepetcare:household:bridge1:12345:timezoneId"}

/* *****************************************
 * Hub
 * *****************************************/
Number      UR_1c_Id                "Hub Id [%d]"                                   (dgPet) {channel="surepetcare:hubDevice:bridge1:123456:id"}
String      UR_1c_Name              "Hub Name [%s]"                                 (dgPet) {channel="surepetcare:hubDevice:bridge1:123456:name"}
String      UR_1c_Product           "Hub Product [%s]"                              (dgPet) {channel="surepetcare:hubDevice:bridge1:123456:product"}
String      UR_1c_LEDMode           "Hub LED Mode [%s]"                             (dgPet) {channel="surepetcare:hubDevice:bridge1:123456:ledMode"}
String      UR_1c_PairingMode       "Hub Pairing Mode [%s]"                         (dgPet) {channel="surepetcare:hubDevice:bridge1:123456:pairingMode"}
Switch      UR_1c_Online            "Hub Online [%s]"                               (dgPet) {channel="surepetcare:hubDevice:bridge1:123456:online"}
 
/* *****************************************
 * Cat/Pet Flap
 * *****************************************/
Number      UR_1d_Id                "Flap Id [%d]"                              (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:id"}
String      UR_1d_Name              "Flap Name [%s]"                            (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:name"}
String      UR_1d_Product           "Flap Product [%s]"                         (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:product"}
Switch      UR_1d_CurfewEnabled1    "Flap Curfew 1 Enabled [%s]"                (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewEnabled1"}
String      UR_1d_CurfewLockTime1   "Flap Curfew 1 Lock Time [%s]"              (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewLockTime1"}
String      UR_1d_CurfewUnlockTime1 "Flap Curfew 1 Unlock Time [%s]"            (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewUnlockTime1"}
Switch      UR_1d_CurfewEnabled2    "Flap Curfew 2 Enabled [%s]"                (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewEnabled2"}
String      UR_1d_CurfewLockTime2   "Flap Curfew 2 Lock Time [%s]"              (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewLockTime2"}
String      UR_1d_CurfewUnlockTime2 "Flap Curfew 2 Unlock Time [%s]"            (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewUnlockTime2"}
Switch      UR_1d_CurfewEnabled3    "Flap Curfew 3 Enabled [%s]"                (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewEnabled3"}
String      UR_1d_CurfewLockTime3   "Flap Curfew 3 Lock Time [%s]"              (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewLockTime3"}
String      UR_1d_CurfewUnlockTime3 "Flap Curfew 3 Unlock Time [%s]"            (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewUnlockTime3"}
Switch      UR_1d_CurfewEnabled4    "Flap Curfew 4 Enabled [%s]"                (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewEnabled4"}
String      UR_1d_CurfewLockTime4   "Flap Curfew 4 Lock Time [%s]"              (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewLockTime4"}
String      UR_1d_CurfewUnlockTime5 "Flap Curfew 4 Unlock Time [%s]"            (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:curfewUnlockTime4"}
String      UR_1d_LockingMode       "Flap Locking Mode [%s]"                    (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:lockingMode"}
Switch      UR_1d_LowBattery        "Flap Low Battery [%s]"                     (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:lowBattery"}
Number      UR_1d_BatteryLevel      "Flap Battery Level [%.0f %%]"              (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:batteryLevel"}
Number      UR_1d_BatteryVoltage    "Flap Battery Voltage [%.1f V]"             (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:batteryVoltage"}
Switch      UR_1d_Online            "Flap Online [%s]"                          (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:online"}
Number      UR_1d_DeviceRSSI        "Flap Device RSSI [%.2f dB]"                (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:deviceRSSI"}
Number      UR_1d_HubRSSI           "Flap Hub RSSI [%.2f dB]"                   (dgPet) {channel="surepetcare:flapDevice:bridge1:123456:hubRSSI"}

/* *****************************************
 * Pet
 * *****************************************/
Number      UR_1e_Id                "Pet Id [%d]"                           (dgPet) {channel="surepetcare:pet:bridge1:12345:id"}
String      UR_1e_Name              "Pet Name [%s]"                         (dgPet) {channel="surepetcare:pet:bridge1:12345:name"}
String      UR_1e_Comment           "Pet Comment [%s]"                      (dgPet) {channel="surepetcare:pet:bridge1:12345:comment"}
String      UR_1e_Gender            "Pet Gender [%s]"                       (dgPet) {channel="surepetcare:pet:bridge1:12345:gender"}
String      UR_1e_Breed             "Pet Breed [%s]"                        (dgPet) {channel="surepetcare:pet:bridge1:12345:breed"}
String      UR_1e_Species           "Pet Species [%s]"                      (dgPet) {channel="surepetcare:pet:bridge1:12345:species"}
Image       UR_1e_Photo             "Pet Photo"                             (dgPet) {channel="surepetcare:pet:bridge1:12345:photo"}
String      UR_1e_TagIdentifier     "Pet Tag Identifier [%s]"               (dgPet) {channel="surepetcare:pet:bridge1:12345:tagIdentifier"}
String      UR_1e_Location          "Pet Location [%s]"                     (dgPet) {channel="surepetcare:pet:bridge1:12345:location"}
String      UR_1e_LocationTimeoffset"Pet Switch Location [%s]"              (gCats) {channel="surepetcare:pet:bridge1:20584:locationTimeoffset"}
DateTime    UR_1e_LocationChanged   "Pet Loc. Updated [%1$ta. %1$tH:%1$tM]" (dgPet) {channel="surepetcare:pet:bridge1:12345:locationChanged"}
String      UR_1e_LocationThrough   "Pet Entered / Left through [%s]"       (dgPet) {channel="surepetcare:pet:bridge1:12345:locationChangedThrough"}
Number:Mass UR_1e_Weight            "Pet Weight [%.1f %unit%]"              (dgPet) {channel="surepetcare:pet:bridge1:12345:weight"}
DateTime    UR_1e_DateOfBirth       "Pet Date of Birth [%1$td.%1$tm.%1$tY]" (dgPet) {channel="surepetcare:pet:bridge1:12345:dateOfBirth"}
// Pet Feeder Data
String      UR_1e_Device            "Device Name [%s]"                      (dgPet) {channel="surepetcare:pet:bridge1:12345:feederDevice"}
Number:Mass UR_1e_Change            "Change: [%.2f %unit%]"                 (dgPet) {channel="surepetcare:pet:bridge1:12345:feederLastChange"}
Number:Mass UR_1e_ChangeLeft        "Change: L [%.2f %unit%]"               (dgPet) {channel="surepetcare:pet:bridge1:12345:feederLastChangeLeft"}
Number:Mass UR_1e_ChangeRight       "Change: R [%.2f %unit%]"               (dgPet) {channel="surepetcare:pet:bridge1:12345:feederLastChangeRight"}
DateTime    UR_1e_FeedAt            "Last Feeding [%1$ta. %1$tH:%1$tM]"     (dgPet) {channel="surepetcare:pet:bridge1:12345:feederLastFeeding"}

/* *****************************************
 * Pet Feeder
 * *****************************************/
Number      UR_1f_Id                    "Feeder ID [%s]"                            (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:id"}
String      UR_1f_Name                  "Feeder Name [%s]"                          (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:name"}
String      UR_1f_Product               "Feeder Product [%s]"                       (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:product"}
Switch      UR_1f_LowBattery            "Feeder Low Battery [%s]"                   (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:lowBattery"}
Number      UR_1f_BatteryLevel          "Feeder Battery Level [%.0f %%]"            (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:batteryLevel"}
Number      UR_1f_BatteryVoltage        "Feeder Battery Voltage [%.2f V]"           (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:batteryVoltage"}
String      UR_1f_BowlsType             "Feeder Bowls Type [%s]"                    (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowls"}
String      UR_1f_BowlsFoodtype         "Feeder Food Type [%s]"                     (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowlsFood"}
Number:Mass UR_1f_BowlsTarget           "Feeder Target [%.0f %unit%]"               (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowlsTarget"}
String      UR_1f_BowlsFoodtypeLeft     "Feeder Food Type L [%s]"                   (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowlsFoodLeft"}
Number:Mass UR_1f_BowlsTargetLeft       "Feeder Target L [%.0f %unit%]"             (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowlsTargetLeft"}
String      UR_1f_BowlsFoodtypeRight    "Feeder Food Type R [%s]"                   (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowlsFoodRight"}
Number:Mass UR_1f_BowlsTargetRight      "Feeder Target R [%.0f %unit%]"             (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowlsTargetRight"}
String      UR_1f_BowlsLidCloseDelay    "Feeder Close Delay [%s]"                   (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowlsCloseDelay"}
String      UR_1f_BowlsTrainingMode     "Feeder Training Mode [%s]"                 (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:bowlsTrainingMode"}
Switch      UR_1f_Online                "Feeder Status [%s]"                        (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:online"}
Number      UR_1f_DeviceRSSI            "Feeder Device Signal [%.2f dB]"            (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:deviceRSSI"}
Number      UR_1f_HubRSSI               "Feeder Hub Signal [%.2f dB]"               (dgPet) {channel="surepetcare:feederDevice:bridge1:123456:hubRSSI"}
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
