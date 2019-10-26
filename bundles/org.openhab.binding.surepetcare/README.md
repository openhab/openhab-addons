# Sure Petcare Binding

This binding offers integration to the Sure Petcare API, supporting cloud-connected cat flaps and feeders.

### Features

1. Read access to all attributes for households, devices (hubs, flaps) and pets through individual things/channels.
2. Manual setting of pet location.
3. Setting of LED Mode (hub), Locking Mode (flaps) and Curfews.

### Restrictions / TODO

1. The Sure Petcare API is not publicly available and this binding has been based on observed interactions between their mobile phone app and the cloud API.
   If the Sure Petcare API changes, this binding might stop working.
2. The current version of the binding supports only cat/pet flaps. Feeders are not yet supported as I don't own one yet.

### Credits

The binding code is based on a lot of work done by other developers:

- Holger Eisold (https://github.com/HerzScheisse) - Python use in OpenHAB and various PRs (https://github.com/HerzScheisse/SurePetcare-openHAB-JSR223-Rules)
- Alex Toft (https://github.com/alextoft) - PHP implementation (https://github.com/alextoft/sureflap)
- rcastberg (https://github.com/rcastberg) - Python implementation (https://github.com/rcastberg/sure_petcare)

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
SurePetCare things can be configured either through the online configuration utility via discovery, or manually through a 'surepetcare.things' configuration file.
The Bridge will not be autodiscovered and must be added manually. This can be done via bridge thing configuration file or via PaperUI. That is because the Sure PetCare API requires authentication credentials to communicate with the service.

After adding the Bridge, it will go ONLINE, and after a short while, the discovery process for household, devices and pets will start. When new hardware is discovered it will appear in the Inbox.

## Things and their channels

Channel names in **bold** are read/write, everything else is read-only

### Bridge Thing

| Channel     | Type   | Description                                                                       |
|-------------|--------|-----------------------------------------------------------------------------------|
| online      | Switch | Parameter indicating if the bridge has a valid connection to the Sure Petcare API |
| **refresh** | Switch | Trigger switch to force a full cache update                                       |


### Household Thing

| Channel    | Type     | Description                                  |
|------------|----------|----------------------------------------------|
| id         | Number   | A unique id assigned by the Sure Petcare API |
| name       | Text     | The name of the household                    |
| timezoneId | Number   | The id of the household's timezone           |
| createdAt  | DateTime | The date when the household was created      |
| updatedAt  | DateTime | The date when the household was last updated |
| userName1  | Text     | The name of the first household user         |
| userName2  | Text     | The name of the second household user        |
| userName3  | Text     | The name of the third household user         |
| userName4  | Text     | The name of the fourth household user        |
| userName5  | Text     | The name of the fifth household user         |

### Hub Device Thing

| Channel         | Type     | Description                                                           |
|-----------------|----------|-----------------------------------------------------------------------|
| id              | Number   | A unique id assigned by the Sure Petcare API                          |
| name            | Text     | The name of the hub                                                   |
| product         | Text     | The type of product (1=hub)                                           |
| **ledMode**     | Text     | The mode of the hub's LED ears                                        |
| pairingMode     | Text     | The state of pairing                                                  |
| hardwareVersion | Number   | The hub's hardware version number                                     |
| firmwareVersion | Number   | The hub's firmware number                                             |
| online          | Switch   | Indicator if the hub is connected to the internet                     |
| serialNumber    | Text     | The serial number of the device                                       |
| macAddress      | Text     | The mac address of the device                                         |
| createdAt       | DateTime | The date when the device was created (could be the manufactured date) |
| updatedAt       | DateTime | The date when the device was last updated (device settings changed)   |

### Flap Device Thing (Cat or Pet Flap)

| Channel               | Type     | Description                                                           |
|-----------------------|----------|-----------------------------------------------------------------------|
| id                    | Number   | A unique id assigned by the Sure Petcare API                          |
| name                  | Text     | The name of the flap                                                  |
| product               | Text     | The type of product (3=pet flap, 6=cat flap)                          |
| **curfewEnabled1**    | Switch   | Indicator if curfew #1 configuration is enabled                       |
| **curfewLockTime1**   | Text     | The curfew #1 locking time (HH:MM)                                    |
| **curfewUnlockTime1** | Text     | The curfew #1 unlocking time (HH:MM)                                  |
| **curfewEnabled2**    | Switch   | Indicator if curfew #2 configuration is enabled                       |
| **curfewLockTime2**   | Text     | The curfew #2 locking time (HH:MM)                                    |
| **curfewUnlockTime2** | Text     | The curfew #2 unlocking time (HH:MM)                                  |
| **curfewEnabled3**    | Switch   | Indicator if curfew #3 configuration is enabled                       |
| **curfewLockTime3**   | Text     | The curfew #3 locking time (HH:MM)                                    |
| **curfewUnlockTime3** | Text     | The curfew #3 unlocking time (HH:MM)                                  |
| **curfewEnabled4**    | Switch   | Indicator if curfew #4 configuration is enabled                       |
| **curfewLockTime4**   | Text     | The curfew #4 locking time (HH:MM)                                    |
| **curfewUnlockTime4** | Text     | The curfew #4 unlocking time (HH:MM)                                  |
| **lockingMode**       | Text     | The locking mode (e.g. in/out, in-only, out-only etc.)                |
| hardwareVersion       | Number   | The flap's hardware version number                                    |
| firmwareVersion       | Number   | The flap's firmware number                                            |
| online                | Switch   | Indicator if the flap is connected to the hub                         |
| lowBattery            | Switch   | Indicator if the battery voltage is low                               |
| batteryLevel          | Number   | The battery voltage percentage                                        |
| batteryVoltage        | Number   | The absolute battery voltage measurement                              |
| deviceRSSI            | Number   | The received device signal strength in dB                             |
| hubRSSI               | Number   | The received hub signal strength in dB                                |
| serialNumber          | Text     | The serial number of the device                                       |
| macAddress            | Text     | The mac address of the device                                         |
| createdAt             | DateTime | The date when the device was created (could be the manufactured date) |
| updatedAt             | DateTime | The date when the device was last updated (device settings changed)   |
| pairingAt             | Datetime | The date when the device was included in the hub device               |

### Feeder Device Thing

| Channel           | Type        | Description                                                                                     |
|-------------------|-------------|-------------------------------------------------------------------------------------------------|
| id                | Number      | A unique id assigned by the Sure Petcare API                                                    |
| name              | Text        | The name of the feeder                                                                          |
| product           | Text        | The type of product                                                                             |
| hardwareVersion   | Number      | The feeder's hardware version number                                                            |
| firmwareVersion   | Number      | The feeder's firmware number                                                                    |
| online            | Switch      | Indicator if the feeder is connected to the hub                                                 |
| lowBattery        | Switch      | Indicator if the battery voltage is low                                                         |
| batteryLevel      | Number      | The battery voltage percentage                                                                  |
| batteryVoltage    | Number      | The absolute battery voltage measurement                                                        |
| deviceRSSI        | Number      | The received device signal strength in dB                                                       |
| hubRSSI           | Number      | The received hub signal strength in dB                                                          |
| serialNumber      | Text        | The serial number of the device                                                                 |
| macAddress        | Text        | The mac address of the device                                                                   |
| createdAt         | DateTime    | The date when the device was created (could be the manufactured date)                           |
| updatedAt         | DateTime    | The date when the device was last updated (device settings changed)                             |
| pairingAt         | Datetime    | The date when the device was included in the hub device                                         |
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

| Channel                | Type        | Description                                                      |
|------------------------|-------------|------------------------------------------------------------------|
| id                     | Number      | A unique id assigned by the Sure Petcare API                     |
| name                   | Text        | The name of the pet                                              |
| comment                | Text        | A user provided comment/description                              |
| gender                 | Text        | The pet's gender                                                 |
| breed                  | Text        | The pet's breed                                                  |
| species                | Text        | The pet's species                                                |
| photo                  | Image       | The image of the pet                                             |
| tagIdentifier          | Text        | The unique identifier of the pet's micro chip or collar tag      |
| **location**           | Text        | The current location of the pet (0=unknown, 1=inside, 2=outside) |
| locationChanged        | DateTime    | The time when the location was last changed                      |
| locationChangedThrough | Text        | The device name or username where the pet left/entered the house |
| weight                 | Number      | The pet's weight                                                 |
| dateOfBirth            | DateTime    | The pet's date of birth                                          |
| feederDevice           | Text        | The device from which the pet last ate                           |
| feederLastChange       | Number:Mass | The last eaten change in gram (big bowl)                         |
| feederLastChangeLeft   | Number:Mass | The last eaten change in gram (half bowl left)                   |
| feederLastChangeRight  | Number:Mass | The last eaten change in gram (half bowl right)                  |
| feederLastFeeding      | DateTime    | The pet's last eaten date                                        |


## Manual configuration

### Things configuration

```
Bridge surepetcare:bridge:bridge1 "Demo API Bridge" @ "SurePetcare" [ username="<USERNAME>", password="<PASSWORD>", refreshIntervalTopology=36000, refreshIntervalStatus=300 ]
{
  Thing household     45237  "My Household" @ "SurePetcare"
  Thing hubDevice     439862 "My SurePetcare Hub" @ "SurePetcare Devices"
  Thing flapDevice    316524 "My Backdoor Cat Flap" @ "SurePetcare Devices"
  Thing feederDevice  123456 "My Pet Feeder" @ "SurePetcare Devices"
  Thing pet           60487  "My Cat" @ "SurePetcare Pets"
}
```

### Items configuration

```
/* *****************************************
 * Bridge
 * *****************************************/
Group      dgPet
Switch     UR_1a_Online             "Bridge Online [%s]"                                                (dgPet)  { channel="surepetcare:bridge:bridge1:online" }

/* *****************************************
 * Household
 * *****************************************/
Number     UR_1b_Id                 "Household Id [%d]"                                                 (dgPet)  { channel="surepetcare:household:bridge1:45237:id" }
String     UR_1b_Name               "Household Name [%s]"                                               (dgPet)  { channel="surepetcare:household:bridge1:45237:name" }
Number     UR_1b_TimezoneId         "Household Timezone Id [%d]"                                        (dgPet)  { channel="surepetcare:household:bridge1:45237:timezoneId" }

/* *****************************************
 * Hub
 * *****************************************/
Number     UR_1c_Id                 "Hub Id [%d]"                                                       (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:id" }
String     UR_1c_Name               "Hub Name [%s]"                                                     (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:name" }
String     UR_1c_Product            "Hub Product [%s]"                                                  (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:product" }
String     UR_1c_LEDMode            "Hub LED Mode [%s]"                                                 (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:ledMode" }
String     UR_1c_PairingMode        "Hub Pairing Mode [%s]"                                             (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:pairingMode" }
Number     UR_1c_HardwareVersion    "Hub Hardware Version [%s]"                                         (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:hardwareVersion" }
Number     UR_1c_FirmwareVersion    "Hub Firmware Version [%s]"                                         (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:firmwareVersion" }
Switch     UR_1c_Online             "Hub Online [%s]"                                                   (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:online" }
 
/* *****************************************
 * Cat Flap
 * *****************************************/
Number     UR_1d_Id                 "Cat Flap Id [%d]"                                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:id" }
String     UR_1d_Name               "Cat Flap Name [%s]"                                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:name" }
String     UR_1d_Product            "Cat Flap Product [%s]"                                             (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:product" }
Switch     UR_1d_CurfewEnabled1     "Cat Flap Curfew 1 Enabled [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewEnabled1" }
String     UR_1d_CurfewLockTime1    "Cat Flap Curfew 1 Lock Time [%s]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewLockTime1" }
String     UR_1d_CurfewUnlockTime1  "Cat Flap Curfew 1 Unlock Time [%s]"                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewUnlockTime1" }
Switch     UR_1d_CurfewEnabled2     "Cat Flap Curfew 2 Enabled [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewEnabled2" }
String     UR_1d_CurfewLockTime2    "Cat Flap Curfew 2 Lock Time [%s]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewLockTime2" }
String     UR_1d_CurfewUnlockTime2  "Cat Flap Curfew 2 Unlock Time [%s]"                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewUnlockTime2" }
Switch     UR_1d_CurfewEnabled3     "Cat Flap Curfew 3 Enabled [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewEnabled3" }
String     UR_1d_CurfewLockTime3    "Cat Flap Curfew 3 Lock Time [%s]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewLockTime3" }
String     UR_1d_CurfewUnlockTime3  "Cat Flap Curfew 3 Unlock Time [%s]"                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewUnlockTime3" }
Switch     UR_1d_CurfewEnabled4     "Cat Flap Curfew 4 Enabled [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewEnabled4" }
String     UR_1d_CurfewLockTime4    "Cat Flap Curfew 4 Lock Time [%s]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewLockTime4" }
String     UR_1d_CurfewUnlockTime5  "Cat Flap Curfew 4 Unlock Time [%s]"                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:curfewUnlockTime4" }
String     UR_1d_LockingMode        "Cat Flap Locking Mode [%s]"                                        (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:lockingMode" }
Number     UR_1d_HardwareVersion    "Cat Flap Hardware Version [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:hardwareVersion" }
Number     UR_1d_FirmwareVersion    "Cat Flap Firmware Version [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:firmwareVersion" }
Switch     UR_1d_LowBattery         "Cat Flap Low Battery [%s]"                                         (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:lowBattery" }
Number     UR_1d_BatteryLevel       "Cat Flap Battery Level [%.0f %%]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:batteryLevel" }
Number     UR_1d_BatteryVoltage     "Cat Flap Battery Voltage [%.1f V]"                                 (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:batteryVoltage" }
Switch     UR_1d_Online             "Cat Flap Online [%s]"                                              (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:online" }
Number     UR_1d_DeviceRSSI         "Cat Flap Device RSSI [%.2f dB]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:deviceRSSI" }
Number     UR_1d_HubRSSI            "Cat Flap Hub RSSI [%.2f dB]"                                       (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:hubRSSI" }

/* *****************************************
 * Pet
 * *****************************************/
Number     UR_1e_Id                 "Pet Id [%d]"                                                       (dgPet)  { channel="surepetcare:pet:bridge1:60487:id" }
String     UR_1e_Name               "Pet Name [%s]"                                                     (dgPet)  { channel="surepetcare:pet:bridge1:60487:name" }
String     UR_1e_Comment            "Pet Comment [%s]"                                                  (dgPet)  { channel="surepetcare:pet:bridge1:60487:comment" }
String     UR_1e_Gender             "Pet Gender [%s]"                                                   (dgPet)  { channel="surepetcare:pet:bridge1:60487:gender" }
String     UR_1e_Breed              "Pet Breed [%s]"                                                    (dgPet)  { channel="surepetcare:pet:bridge1:60487:breed" }
String     UR_1e_Species            "Pet Species [%s]"                                                  (dgPet)  { channel="surepetcare:pet:bridge1:60487:species" }
Image      UR_1e_PhotoURL           "Pet Photo"                                                         (dgPet)  { channel="surepetcare:pet:bridge1:60487:photo" }
String     UR_1e_TagIdentifier      "Pet Tag Identifier [%s]"                                           (dgPet)  { channel="surepetcare:pet:bridge1:60487:tagIdentifier" }
String     UR_1e_Location           "Pet Location [%s]"                                                 (dgPet)  { channel="surepetcare:pet:bridge1:60487:location" }
DateTime   UR_1e_LocationChanged    "Pet Location Last Updated [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]"   (dgPet)  { channel="surepetcare:pet:bridge1:60487:locationChanged" }
Number     UR_1e_Weight             "Pet Weight [%.1f kg]"                                              (dgPet)  { channel="surepetcare:pet:bridge1:60487:weight" }
DateTime   UR_1e_DateOfBirth        "Pet Date of Birth [%1$td.%1$tm.%1$tY]"                             (dgPet)  { channel="surepetcare:pet:bridge1:60487:dateOfBirth" }
```

### Sitemap Configuration

```

### Sitemap configuration

sitemap surepetcare label="Sure Petcare Sitemap"
TODO

```
 
## Troubleshooting

| Problem                                     | Solution                                                                            |
|---------------------------------------------|-------------------------------------------------------------------------------------|
| Bridge cannot connect to SurePetCare API    | Check if you can logon to the Sure Petcare app with the given username/password.    |

