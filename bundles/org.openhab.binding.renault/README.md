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

| Parameter         | Description                                                                | Required |
|-------------------|----------------------------------------------------------------------------|----------|
| myRenaultUsername | MyRenault Username.                                                        | yes      |
| myRenaultPassword | MyRenault Password.                                                        | yes      |
| locale            | MyRenault Location (language_country).                                     | yes      |
| vin               | Vehicle Identification Number.                                             | yes      |
| refreshInterval   | Interval the car is polled in minutes.                                     | no       |
| updateDelay       | How long to wait for commands to reach car and update to server in seconds.| no       |

## Channels

Some channels may not work depending on your car and MyRenault account. The "Advanced" channels are especially likely to cause confusion and problems.

| Channel ID             | Type               | Description                                     | Read Only |
|------------------------|--------------------|-------------------------------------------------|-----------|
| batteryavailableEnergy | Number:Energy      | Battery Energy Available                        | Yes       |
| batterylevel           | Number             | State of the battery in %                       | Yes       |
| chargingmode           | String             | Charging mode. always_charging or schedule_mode | No        |
| chargingstatus         | String             | Charging status                                 | Yes       |
| chargingremainingtime  | Number             | Charging time remaining                         | Yes       |
| plugstatus             | String             | Status of charging plug                         | Yes       |
| estimatedrange         | Number:Length      | Estimated range of the car                      | Yes       |
| odometer               | Number:Length      | Total distance travelled                        | Yes       |
| hvacstatus             | String             | HVAC status HVAC Status (ON | OFF | PENDING)    | No        |
| hvactargettemperature  | Number:Temperature | HVAC target temperature (19 to 21)              | No        |
| externaltemperature    | Number:Temperature | Temperature outside of the car                  | Yes       |
| image                  | String             | Image URL of MyRenault                          | Yes       |
| location               | Location           | The GPS position of the vehicle                 | Yes       |
| locationupdated        | DateTime           | Timestamp of the last location update           | Yes       |
