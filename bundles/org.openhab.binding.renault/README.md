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

Currently all available channels are read only:

| Channel ID   | Type          | Description                     |
|--------------|---------------|---------------------------------|
| batterylevel | Number        | State of the battery in %       |
| hvacstatus   | Switch        | HVAC status switch              |
| image        | String        | Image URL of MyRenault          |
| location     | Location      | The GPS position of the vehicle |
| odometer     | Number:Length | Total distance travelled        |


