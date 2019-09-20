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
Bridge surepetcare:bridge:MyBridge [ username="<USERNAME>", password="<PASSWORD>", refresh_interval_topology=36000, refresh_interval_location=300 ]
{
	Thing hub MyHub
	Thing household MyHousehold
	Thing flapDevice MyCatFlap
	Thing pet MyCat
}
```

### Items configuration

```
/* *****************************************
 * Bridge
 * *****************************************/
Switch bridge_Online 		"Sure Petcare Bridge Online [%s]"		<switch>	{channel="surepetcare:bridge:MyBridge:online"}

/* *****************************************
 * Household
 * *****************************************/
TODO

/* *****************************************
 * Hub
 * *****************************************/
TODO
 
/* *****************************************
 * Cat Flap
 * *****************************************/
TODO

/* *****************************************
 * Pet
 * *****************************************/
TODO

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

