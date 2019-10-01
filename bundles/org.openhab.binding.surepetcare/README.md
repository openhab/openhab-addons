# Sure Petcare Binding

This binding offers integration to the Sure Petcare API, supporting cloud-connected cat flaps and feeders.

### Restrictions / TODO

1. The Sure Petcare API is not publicly available and this binding has been based on observed interactions between their mobile phone app and the cloud API.
   If the Sure Petcare API changes, this binding might stop working.
2. The current version of the binding supports only cat/pet flaps. Feeders are not yet supported as I don't own one yet.
3. The binding has limited support for writable channels. At the moment, only the manual setting of the pet location is supported.

### Credits

The binding code is based on a lot of work done by other developers:

- HoLLe (https://github.com/HerzScheisse) - Python use in OpenHAB and various PRs (https://github.com/HerzScheisse/SurePetcare-openHAB-JSR223-Rules)
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
| timezoneId        | Number   | The id of the household's timezone                                                         |

### Hub Device Thing

| Channel           | Type     | Description                                                                                |
|-------------------|----------|--------------------------------------------------------------------------------------------|
| id                | Number   | A unique id assigned by the Sure Petcare API                                               |
| name              | Text     | The name of the hub                                                                        |
| productId         | Number   | The type of product (1=hub)                                                                |
| ledModeId         | Number   | The numerical mode of the hub's LED ears                                                   |
| pairingModeId     | Number   | The state of pairing                                                                       |
| hardwareVersion   | Text     | The hub's hardware version number                                                          |
| firmwareVersion   | Text     | The hub's firmware number                                                                  |
| online            | Switch   | Indicator if the hub is connected to the internet                                          |

### Flap Device Thing (Cat or Pet Flap)

| Channel           | Type     | Description                                                                                |
|-------------------|----------|--------------------------------------------------------------------------------------------|
| id                | Number   | A unique id assigned by the Sure Petcare API                                               |
| name              | Text     | The name of the flap                                                                       |
| productId         | Number   | The type of product (3=pet flap, 6=cat flap)                                               |
| curfewEnabled1    | Switch   | Indicator if this curfew configuration is enabled                                          |
| curfewLockTime1   | Text     | The curfew locking time (HH:MM)                                                            |
| curfewUnlockTime1 | Text     | The curfew unlocking time (HH:MM)                                                          |
| lockingModeId     | Number   | A numeric indicator of the locking mode (e.g. in/out, in-only, out-only etc.)              |
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
| genderId          | Number   | The pet's gender id (0=female, 1=male)                                                     |
| breedId           | Number   | The pet's breed id (see related mapping table)                                             |
| speciesId         | Number   | The pet's species id (0=unknown, 1=cat, 2=dog)                                             |
| photoURL          | Text     | The URL of the pet's photo                                                                 |
| tagIdentifier     | Text     | The unique identifier of the pet's micro chip or collar tag                                |
| locationId        | Number   | The current location id of the pet (0=unknown, 1=inside, 2=outside)                        |
| locationChanged   | DateTime | The time when the location was last changed                                                |


## Manual configuration

### Things configuration

```
Bridge surepetcare:bridge:bridge1 "Demo API Bridge" [ username="<USERNAME>", password="<PASSWORD>", refresh_interval_topology=36000, refresh_interval_location=300 ]
{
  Thing household  45237  "My Household"
  Thing hubDevice  439862 "My SurePetcare Hub"
  Thing flapDevice 316524 "My Backdoor Cat Flap"
  Thing pet        60487  "My Cat"
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
String     UR_1c_Product            "Hub Product [%s]"                                                  (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:productId" [profile="transform:MAP", function="surepetcare_product_en.map"] }
Number     UR_1c_ProductId          "Hub Product Id [%d]"                                               (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:productId" }
String     UR_1c_LEDMode            "Hub LED Mode [%s]"                                                 (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:ledModeId" [profile="transform:MAP", function="surepetcare_ledmode_en.map"] }
Number     UR_1c_LEDModeId          "Hub LED Mode Id [%d]"                                              (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:ledModeId" }
String     UR_1c_PairingMode        "Hub Pairing Mode [%s]"                                             (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:pairingModeId" [profile="transform:MAP", function="surepetcare_pairingmode_en.map"] }
Number     UR_1c_PairingModeId      "Hub PairingMode Mode Id [%d]"                                      (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:pairingModeId" }
String     UR_1c_HardwareVersion    "Hub Hardware Version [%s]"                                         (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:hardwareVersion" }
String     UR_1c_FirmwareVersion    "Hub Firmware Version [%s]"                                         (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:firmwareVersion" }
Switch     UR_1c_Online             "Hub Online [%s]"                                                   (dgPet)  { channel="surepetcare:hubDevice:bridge1:439862:online" }

 
/* *****************************************
 * Cat Flap
 * *****************************************/
Number     UR_1d_Id                 "Cat Flap Id [%d]"                                                  (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:id" }
String     UR_1d_Name               "Cat Flap Name [%s]"                                                (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:name" }
String     UR_1d_Product            "Cat Flap Product [%s]"                                             (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:productId" [profile="transform:MAP", function="surepetcare_product_en.map"] }
Number     UR_1d_ProductId          "Cat Flap Product Id [%d]"                                          (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:productId" }
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
String     UR_1d_LockingMode        "Cat Flap Locking Mode [%s]"                                        (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:lockingModeId" [profile="transform:MAP", function="surepetcare_lockingmode_en.map"] }
Number     UR_1d_LockingModeId      "Cat Flap Locking Mode Id [%d]"                                     (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:lockingModeId" }
String     UR_1d_HardwareVersion    "Cat Flap Hardware Version [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:hardwareVersion" }
String     UR_1d_FirmwareVersion    "Cat Flap Firmware Version [%s]"                                    (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:firmwareVersion" }
Switch     UR_1d_LowBattery         "Cat Flap Low Battery [%s]"                                         (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:lowBattery" }
Number     UR_1d_BatteryLevel       "Cat Flap Battery Level [%f]"                                       (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:batteryLevel" }
Number     UR_1d_BatteryVoltage     "Cat Flap Battery Voltage [%f]"                                     (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:batteryVoltage" }
Switch     UR_1d_Online             "Cat Flap Online [%s]"                                              (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:online" }
Number     UR_1d_DeviceRSSI         "Cat Flap Device RSSI [%f]"                                         (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:deviceRSSI" }
Number     UR_1d_HubRSSI            "Cat Flap Hub RSSI [%f]"                                            (dgPet)  { channel="surepetcare:flapDevice:bridge1:316524:hubRSSI" }


/* *****************************************
 * Pet
 * *****************************************/
Number     UR_1e_Id                 "Pet Id [%d]"                                                       (dgPet)  { channel="surepetcare:pet:bridge1:60487:id" }
String     UR_1e_Name               "Pet Name [%s]"                                                     (dgPet)  { channel="surepetcare:pet:bridge1:60487:name" }
String     UR_1e_Comment            "Pet Comment [%s]"                                                  (dgPet)  { channel="surepetcare:pet:bridge1:60487:comment" }
String     UR_1e_Gender             "Pet Gender [%s]"                                                   (dgPet)  { channel="surepetcare:pet:bridge1:60487:genderId" [profile="transform:MAP", function="surepetcare_gender_en.map"] }
Number     UR_1e_GenderId           "Pet Gender Id [%d]"                                                (dgPet)  { channel="surepetcare:pet:bridge1:60487:genderId" }
String     UR_1e_Breed              "Pet Breed [%s]"                                                    (dgPet)  { channel="surepetcare:pet:bridge1:60487:breedId" [profile="transform:MAP", function="surepetcare_breed_en.map"] }
Number     UR_1e_BreedId            "Pet Breed Id [%d]"                                                 (dgPet)  { channel="surepetcare:pet:bridge1:60487:breedId" }
String     UR_1e_Species            "Pet Species [%s]"                                                  (dgPet)  { channel="surepetcare:pet:bridge1:60487:speciesId" [profile="transform:MAP", function="surepetcare_species_en.map"] }
Number     UR_1e_SpeciesId          "Pet Species Id [%d]"                                               (dgPet)  { channel="surepetcare:pet:bridge1:60487:speciesId" }
String     UR_1e_PhotoURL           "Pet Photo URL [%s]"                                                (dgPet)  { channel="surepetcare:pet:bridge1:60487:photoURL" }
String     UR_1e_TagIdentifier      "Pet Tag Identifier [%s]"                                           (dgPet)  { channel="surepetcare:pet:bridge1:60487:tagIdentifier" }
String     UR_1e_Location           "Pet Location [%s]"                                                 (dgPet)  { channel="surepetcare:pet:bridge1:60487:locationId" [profile="transform:MAP", function="surepetcare_location_en.map"] }
Number     UR_1e_LocationId         "Pet Location Id [%d]"                                              (dgPet)  { channel="surepetcare:pet:bridge1:60487:locationId" }
DateTime   UR_1e_LocationChanged    "Pet Location Last Updated [%1$td.%1$tm.%1$tY %1$tH:%1$tM:%1$tS]"   (dgPet)  { channel="surepetcare:pet:bridge1:60487:locationChanged" }
```

### Sitemap Configuration

```

### Sitemap configuration

sitemap surepetcare label="Sure Petcare Sitemap"
TODO

```

### Transform Maps

Several of the channels are of "code"-type, i.e. they map back to a human readable value such as Locking Mode, Pet Location etc. 
To allow language independent values in either items or sitemap, the MAP transformation bundle should be installed and a number of maps. The maps are currently available in the code base (src/main/resources/transform) and in the bundle jar.
 
## Troubleshooting

| Problem                                     | Solution                                                                            |
|---------------------------------------------|-------------------------------------------------------------------------------------|
| Bridge cannot connect to SurePetCare API    | Check if you can logon to the Sure Petcare app with the given username/password.    |

