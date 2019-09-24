# Sure Petcare Binding

This binding offers integration to the Sure Petcare API, supporting cloud-connected cat flaps and feeders.

### Restrictions / TODO

1. The Sure Petcare API is not publicly available and this binding has been based on observed interactions between their mobile phone app and the cloud API.
   If the Sure Petcare API changes, this binding might stop working.
2. The current version of the binding supports only cat/pet flaps. Feeders are not yet supported as I don't own one yet.
3. The binding is currently read-only. I.e. no changes to the Sure Petcare devices can be made through the binding.

### Credits

The binding code is based on a lot of work done by other developers:

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
| Pet             | Thing      | Automatic |  A pet (dog or cat)                                                      |


## Getting started /  Discovery

The binding consists of a Bridge (the API connection), and a number of Things, which relates to the individual hardware devices and pets.
SurePetCare things can be configured either through the online configuration utility via discovery, or manually through a 'surepetcare.things' configuration file.
The Bridge will not be autodiscovered and must be added manually. This can be done via bridge thing configuration file or via PaperUI. That is because the Sure PetCare API requires authentication credentials to communicate with the service.

After adding the Bridge, it will go ONLINE, and after a short while, the discovery process for household, devices and pets will start. When new hardware is discovered it will appear in the Inbox.

## Things and their channels

### Bridge Thing

| Channel           | Type     | Description                                                                                |
|-------------------|----------|--------------------------------------------------------------------------------------------|
| online            | Switch   | Parameter indicating if the bridge has a valid connection to the Sure Petcare API          |

### Household Thing

| Channel           | Type     | Description                                                                                |
|-------------------|----------|--------------------------------------------------------------------------------------------|
| id                | Number   | A unique id assigned by the Sure Petcare API                                               |
| name              | Text     | The name of the household                                                                  |
| timezone          | Text     | The household's timezone                                                                   |
| timezoneUTCOffset | Number   | The offset in secs between the household's timezone and UTC                                |

### Hub Device Thing

| Channel           | Type     | Description                                                                                |
|-------------------|----------|--------------------------------------------------------------------------------------------|
| id                | Number   | A unique id assigned by the Sure Petcare API                                               |
| name              | Text     | The name of the hub                                                                        |
| product           | Switch   | The type of product (i.e. Hub)                                                             |
| ledMode           | Number   | The numerical mode of the hub's LED ears                                                   |
| pairingMode       | Number   | The state of pairing                                                                       |
| hardwareVersion   | Text     | The hub's hardware version number                                                          |
| firmwareVersion   | Text     | The hub's firmware number                                                                  |
| online            | Switch   | Indicator if the hub is connected to the internet                                          |

### Flap Device Thing (Cat or Pet Flap)

| Channel           | Type     | Description                                                                                |
|-------------------|----------|--------------------------------------------------------------------------------------------|
| id                | Number   | A unique id assigned by the Sure Petcare API                                               |
| name              | Text     | The name of the flap                                                                       |
| product           | Text     | The type of product (i.e. Cat Flap or Dog Flap)                                            |
| curfewEnabled1    | Switch   | Indicator if this curfew configuration is enabled                                          |
| curfewLockTime1   | Text     | The curfew locking time (HH:MM)                                                            |
| curfewUnlockTime1 | Text     | The curfew unlocking time (HH:MM)                                                          |
| lockingMode       | Number   | A numeric indicator of the locking mode (e.g. in/out, in-only, out-only etc.)              |
| hardwareVersion   | Text     | The flap's hardware version number                                                         |
| firmwareVersion   | Text     | The flap's firmware number                                                                 |
| online            | Switch   | Indicator if the flap is connected to the hub                                              |
| lowBattery        | Switch   | Indicator if the battery voltage is low                                                    |
| batteryLevel      | Number   | The battery voltage percentage                                                             |
| batteryVoltage    | Number   | The absolute battery voltage measurement                                                   |
| deviceRSSI        | Number   | The received device signal strength in dB                                                  |
| hubRSSI           | Number   | The received hub signal strength in dB                                                     |

### Pet Thing

| Channel           | Type     | Description                                                                                |
|-------------------|----------|--------------------------------------------------------------------------------------------|
| id                | Number   | A unique id assigned by the Sure Petcare API                                               |
| name              | Text     | The name of the pet                                                                        |
| comment           | Text     | A user provided comment/description                                                        |
| gender            | Text     | The pet's gender                                                                           |
| breed             | Text     | The pet's breed                                                                            |
| species           | Text     | The pet's species (cat or dog)                                                             |
| photoURL          | Text     | The URL of the pet's photo                                                                 |
| tagIdentifier     | Text     | The unique identifier of the pet's micro chip or collar tag                                |
| location          | Text     | The current location of the pet (inside or outside)                                        |
| locationChanged   | DateTime | The time when the location was last changed                                                |


## Manual configuration

### Things configuration

```
Bridge surepetcare:bridge:bridge1 [ username="<USERNAME>", password="<PASSWORD>", refresh_interval_topology=36000, refresh_interval_location=300 ]
{
  Thing household  41121  "My Household"
  Thing hubDevice  752464 "My SurePetcare Hub"
  Thing flapDevice 123166 "My Backdoor Cat Flap"
  Thing pet        72317  "My Cat"
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
Number     UR_1b_Id                 "Household Id [%d]"                                                 (dgPet)  { channel="surepetcare:household:bridge1:41121:id" }
String     UR_1b_Name               "Household Name [%s]"                                               (dgPet)  { channel="surepetcare:household:bridge1:41121:name" }
String     UR_1b_Timezone           "Household Timezone [%s]"                                           (dgPet)  { channel="surepetcare:household:bridge1:41121:timezone" }
Number     UR_1b_UTCOffset          "Household Timezone UTC Office"                                     (dgPet)  { channel="surepetcare:household:bridge1:41121:timezoneUTCOffset" }


/* *****************************************
 * Hub
 * *****************************************/
Number     UR_1c_Id                 "Hub Id [%d]"                                                       (dgPet)  { channel="surepetcare:hubDevice:bridge1:752464:id" }
String     UR_1c_Name               "Hub Name [%s]"                                                     (dgPet)  { channel="surepetcare:hubDevice:bridge1:752464:name" }
String     UR_1c_Product            "Hub Product [%s]"                                                  (dgPet)  { channel="surepetcare:hubDevice:bridge1:752464:product" }
Number     UR_1c_LEDMode            "Hub LED Mode [%d]"                                                 (dgPet)  { channel="surepetcare:hubDevice:bridge1:752464:ledMode" }
Number     UR_1c_PairingMode        "Hub Pairing Mode [%d]"                                             (dgPet)  { channel="surepetcare:hubDevice:bridge1:752464:pairingMode" }
String     UR_1c_HardwareVersion    "Hub Hardware Version [%s]"                                         (dgPet)  { channel="surepetcare:hubDevice:bridge1:752464:hardwareVersion" }
String     UR_1c_FirmwareVersion    "Hub Firmware Version [%s]"                                         (dgPet)  { channel="surepetcare:hubDevice:bridge1:752464:firmwareVersion" }
Switch     UR_1c_Online             "Hub Online [%s]"                                                   (dgPet)  { channel="surepetcare:hubDevice:bridge1:752464:online" }

 
/* *****************************************
 * Cat Flap
 * *****************************************/
Number     UR_1d_Id                 "Cat Flap Id [%d]"                                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:id" }
String     UR_1d_Name               "Cat Flap Name [%s]"                                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:name" }
String     UR_1d_Product            "Cat Flap Product [%s]"                                             (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:product" }
Switch     UR_1d_CurfewEnabled1     "Cat Flap Curfew 1 Enabled [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewEnabled1" }
String     UR_1d_CurfewLockTime1    "Cat Flap Curfew 1 Lock Time [%s]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewLockTime1" }
String     UR_1d_CurfewUnlockTime1  "Cat Flap Curfew 1 Unlock Time [%s]"                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewUnlockTime1" }
Switch     UR_1d_CurfewEnabled2     "Cat Flap Curfew 2 Enabled [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewEnabled2" }
String     UR_1d_CurfewLockTime2    "Cat Flap Curfew 2 Lock Time [%s]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewLockTime2" }
String     UR_1d_CurfewUnlockTime2  "Cat Flap Curfew 2 Unlock Time [%s]"                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewUnlockTime2" }
Switch     UR_1d_CurfewEnabled3     "Cat Flap Curfew 3 Enabled [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewEnabled3" }
String     UR_1d_CurfewLockTime3    "Cat Flap Curfew 3 Lock Time [%s]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewLockTime3" }
String     UR_1d_CurfewUnlockTime3  "Cat Flap Curfew 3 Unlock Time [%s]"                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewUnlockTime3" }
Switch     UR_1d_CurfewEnabled4     "Cat Flap Curfew 4 Enabled [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewEnabled4" }
String     UR_1d_CurfewLockTime4    "Cat Flap Curfew 4 Lock Time [%s]"                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewLockTime4" }
String     UR_1d_CurfewUnlockTime5  "Cat Flap Curfew 4 Unlock Time [%s]"                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:curfewUnlockTime4" }
Number     UR_1d_LEDMode            "Cat Flap LED Mode [%d]"                                            (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:ledMode" }
Number     UR_1d_LockingMode        "Cat Flap Locking Mode [%d]"                                        (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:lockingMode" }
String     UR_1d_HardwareVersion    "Cat Flap Hardware Version [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:hardwareVersion" }
String     UR_1d_FirmwareVersion    "Cat Flap Firmware Version [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:firmwareVersion" }
Switch     UR_1d_LowBattery         "Cat Flap Low Battery [%s]"                                         (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:lowBattery" }
Number     UR_1d_BatteryLevel       "Cat Flap Battery Level [%f]"                                       (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:batteryLevel" }
Number     UR_1d_BatteryVoltage     "Cat Flap Battery Voltage [%f]"                                     (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:batteryVoltage" }
Switch     UR_1d_Online             "Cat Flap Online [%s]"                                              (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:online" }
Number     UR_1d_DeviceRSSI         "Cat Flap Device RSSI [%f]"                                         (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:deviceRSSI" }
Number     UR_1d_HubRSSI            "Cat Flap Hub RSSI [%f]"                                            (dgPet)  { channel="surepetcare:flapDevice:bridge1:123166:hubRSSI" }


/* *****************************************
 * Pet
 * *****************************************/
Number     UR_1e_Id                 "Pet Id [%d]"                                                       (dgPet)  { channel="surepetcare:pet:bridge1:70347:id" }
String     UR_1e_Name               "Pet Name [%s]"                                                     (dgPet)  { channel="surepetcare:pet:bridge1:72317:name" }
String     UR_1e_Comment            "Pet Comment [%s]"                                                  (dgPet)  { channel="surepetcare:pet:bridge1:72317:comment" }
String     UR_1e_Gender             "Pet Gender [%s]"                                                   (dgPet)  { channel="surepetcare:pet:bridge1:72317:gender" }
String     UR_1e_Breed              "Pet Breed [%s]"                                                    (dgPet)  { channel="surepetcare:pet:bridge1:72317:breed" }
String     UR_1e_Species            "Pet Species [%s]"                                                  (dgPet)  { channel="surepetcare:pet:bridge1:72317:species" }
String     UR_1e_PhotoURL           "Pet Photo URL [%s]"                                                (dgPet)  { channel="surepetcare:pet:bridge1:72317:photoURL" }
String     UR_1e_TagIdentifier      "Pet Tag Identifier [%s]"                                           (dgPet)  { channel="surepetcare:pet:bridge1:72317:tagIdentifier" }
String     UR_1e_Location           "Pet Location [%s]"                                                 (dgPet)  { channel="surepetcare:pet:bridge1:72317:location" }
DateTime   UR_1e_LocationChanged    "Pet Location Last Updated [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]"   (dgPet)  { channel="surepetcare:pet:bridge1:72317:locationChanged" }
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

