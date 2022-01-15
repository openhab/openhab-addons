# Renault Binding

This binding allow MyRenault App. users to get battery status and other data from their cars.

A binding that translates the [python based renault-api](https://renault-api.readthedocs.io/en/latest/) in an easy to use binding. 


## Supported Things

Supports MyRenault registered cars with an active Connected-Services account.

This binding can only retrieve information that is available in the the MyRenault App.


## Discovery

No discovery

## Thing Configuration

You require your MyRenault credential, locale and VIN for your MyRenault registered car.

| Parameter         | Description                            | Required |
|-------------------|----------------------------------------|----------|
| myRenaultUsername | MyRenault Username.                    | yes      |
| myRenaultPassword | MyRenault Password.                    | yes      |
| locale            | MyRenault Location (language_country). | yes      |
| vin               | Vehicle Identification Number.         | yes      |
| refreshInterval   | Interval the car is polled in minutes. | no       |

## Channels

Some channels may not work depending on your car and MyRenault account.

| Channel ID             | Type               | Description                        | Read Only |
|------------------------|--------------------|------------------------------------|-----------|
| batteryavailableEnergy | Number:Energy      | Battery Energy Available           | Yes       |
| batterycapacity        | Number:Energy      | Battery Capacity                   | Yes       |
| batterylevel           | Number             | State of the battery in %          | Yes       |
| batterytemperature     | Number:Temperature | Battery Temperature                | Yes       |
| plugstatus             | String             | Status of charging plug            | Yes       |
| chargingstatus         | String             | Charging status                    | Yes       |
| estimatedrange         | Number:Length      | Estimated range of the car         | Yes       |
| odometer               | Number:Length      | Total distance travelled           | Yes       |
| hvacstatus             | Switch             | HVAC status switch                 | No        |
| hvactargettemperature  | Number:Temperature | HVAC thermostat target temperature | No        |
| externaltemperature    | Number:Temperature | Temperature outside of the car     | Yes       |
| image                  | String             | Image URL of MyRenault             | Yes       |
| location               | Location           | The GPS position of the vehicle    | Yes       |

