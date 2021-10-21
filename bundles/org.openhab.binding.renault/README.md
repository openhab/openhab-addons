# Renault Binding

This binding allow MyRenault App. users to get battery status and other data from their cars.

A binding that translates the [python based renault-api](https://renault-api.readthedocs.io/en/latest/) in an easy to use binding. 


## Supported Things

Works on my car (Renault Zoe 50) but I only have one car to test.


## Discovery

No discovery

## Binding Configuration

You require your MyRenault credential, locale and VIN for your MyRenault registered car.

## Thing Configuration

The thing has these configuration parameters:

| Parameter         | Description                            | Required |
|-------------------|----------------------------------------|----------|
| myRenaultUsername | MyRenault Username.                    | yes      |
| myRenaultPassword | MyRenault Password.                    | yes      |
| locale            | MyRenault Location (language_country). | yes      |
| vin               | Vehicle Identification Number.         | yes      |
| refreshInterval   | Interval the car is polled in minutes. | yes      |

## Channels

Currently available channels are 

| Channel ID   | Type     | Description                     | Read Only |
|--------------|----------|---------------------------------|-----------|
| batterylevel | Number   | State of the battery in %       |     X     |
| hvacstatus   | Switch   | HVAC status switch              |     X     |
| image        | String   | Image URL of MyRenault          |     X     |
| location     | Location | The GPS position of the vehicle |     X     |
| odometer     | Number   | Total distance travelled        |     X     |


