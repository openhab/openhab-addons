# Renault Binding

This binding allows MyRenault App. users to get battery status and other data from their cars.
They can also heat their cars by turning ON the HVAC status and toggle the car's charging mode.

The binding translates the [python based renault-api](https://renault-api.readthedocs.io/en/latest/) in an easy to use openHAB java binding.

## Supported Things

Supports MyRenault (and MyDacia) registered cars with an active Connected-Services account.

This binding can only retrieve information that is available in the MyRenault App.

## Discovery

No discovery

## Thing Configuration

You require your MyRenault credential, locale and VIN for your MyRenault registered car.

| Parameter         | Description                                                                | Default                          |
|-------------------|----------------------------------------------------------------------------|----------------------------------|
| accountType       | Account Type. (MYDACIA,MYRENAULT)                                          | MYRENAULT                        |
| myRenaultUsername | MyRenault Username.                                                        |                                  |
| myRenaultPassword | MyRenault Password.                                                        |                                  |
| locale            | MyRenault Location (language_country).                                     |                                  |
| vin               | Vehicle Identification Number.                                             |                                  |
| refreshInterval   | Interval the car is polled in minutes.                                     |                               10 |
| updateDelay       | How long to wait for commands to reach car and update to server in seconds.|                               30 |
| kamereonApiKey    | Kamereon API Key.                                                          | VAX7XYKGfa92yMvXculCkEFyfZbuM7Ss |

## Channels

| Channel ID             | Type               | Description                                     | Read Only |
|------------------------|--------------------|-------------------------------------------------|-----------|
| batteryavailableEnergy | Number:Energy      | Battery Energy Available                        | Yes       |
| batterylevel           | Number             | State of the battery in %                       | Yes       |
| batterystatusupdated   | DateTime           | Timestamp of the last battery status update     | Yes       |
| chargingmode           | String             | Charging mode. always_charging or schedule_mode | No        |
| pause                  | Switch             | Pause the charge.                               | No        |
| chargingstatus         | String             | Charging status                                 | Yes       |
| chargingremainingtime  | Number:Time        | Charging time remaining                         | Yes       |
| plugstatus             | String             | Status of charging plug                         | Yes       |
| estimatedrange         | Number:Length      | Estimated range of the car                      | Yes       |
| odometer               | Number:Length      | Total distance travelled                        | Yes       |
| hvacstatus             | String             | HVAC status HVAC Status (ON, OFF, PENDING)      | No        |
| hvactargettemperature  | Number:Temperature | HVAC target temperature (19 to 21)              | No        |
| externaltemperature    | Number:Temperature | Temperature outside of the car                  | Yes       |
| image                  | String             | Image URL of MyRenault                          | Yes       |
| location               | Location           | The GPS position of the vehicle                 | Yes       |
| locationupdated        | DateTime           | Timestamp of the last location update           | Yes       |
| locked                 | Switch             | Locked status of the car                        | Yes       |

## Limitations

Some channels may not work depending on your car and MyRenault account.

The "externaltemperature" only works on a few cars.

The "hvactargettemperature" is used by the hvacstatus ON command for pre-conditioning the car.
This seams to only allow values 19, 20 and 21 or else the pre-conditioning command will not work.

The 'pause' and 'chargingmode' may not work on some cars.
As an example, 'chargingmode' does not work on Dacia Spring cars.

The Kamereon API Key changes periodically, which causes a communication error.
To fix this error update the API Key in the bindings configuration.
The new key value can hopefully be found in the renault-api project: [KAMEREON_APIKEY value](https://github.com/hacf-fr/renault-api/blob/main/src/renault_api/const.py) or in the openHAB forums.

## Example

renaultcar.sitemap:

```perl
sitemap renaultcar label="Renault Car" {
    Frame {
        Image item=RenaultCar_ImageURL
        Default icon="batterylevel" item=RenaultCar_BatteryLevel
        Default item=RenaultCar_BatteryEnergyAvailable
        Default item=RenaultCar_BatteryStatusUpdated
        Default icon="poweroutlet" item=RenaultCar_PlugStatus
        Default icon="switch" item=RenaultCar_ChargingStatus
        Selection icon="switch" item=RenaultCar_ChargingMode mappings=[SCHEDULE_MODE="Schedule mode",ALWAYS_CHARGING="Instant charge"]
        Default icon="switch" item=RenaultCar_Pause
        Default item=RenaultCar_ChargingTimeRemaining
        Default icon="pressure" item=RenaultCar_EstimatedRange
        Default icon="pressure" item=RenaultCar_Odometer
        Selection icon="switch" item=RenaultCar_HVACStatus mappings=[ON="ON"]
        Setpoint icon="temperature" item=RenaultCar_HVACTargetTemperature maxValue=21 minValue=19 step=1
        Default icon="lock" item=RenaultCar_Locked
        Default item=RenaultCar_LocationUpdate
        Default icon="zoom" item=RenaultCar_Location
    }
}
```

If you want to limit the charge of the car battery to less than 100%, this can be done as follows.

- Set up an active dummy charge schedule in the MyRenault App.

- Create a Dimmer item "RenaultCar_ChargeLimit" and set it to 80% for example.

- Add the ChargeRenaultCarLimit rule using the code below.

The rule will change the RenaultCar_ChargingMode to schedule_mode when the limit is reached.
This stops charging after the battery level goes over the charge limit.

ChargeRenaultCarLimit Code

```javascript
configuration: {}
triggers:
  - id: "1"
    configuration:
      itemName: RenaultCar_BatteryLevel
    type: core.ItemStateUpdateTrigger
  - id: "2"
    configuration:
      itemName: RenaultCar_ChargeLimit
    type: core.ItemStateUpdateTrigger
  - id: "3"
    configuration:
      itemName: RenaultCar_PlugStatus
    type: core.ItemStateUpdateTrigger
conditions: []
actions:
  - inputs: {}
    id: "4"
    configuration:
      type: application/vnd.openhab.dsl.rule
      script: >
        if ( RenaultCar_PlugStatus.state.toString == 'PLUGGED' ) {
          if ( RenaultCar_BatteryLevel.state as Number >= RenaultCar_ChargeLimit.state as Number ) {
            if (RenaultCar_ChargingMode.state.toString != 'SCHEDULE_MODE' ) {
              RenaultCar_ChargingMode.sendCommand("SCHEDULE_MODE")
            }
          } else {
            if (RenaultCar_ChargingMode.state.toString != 'ALWAYS_CHARGING' ) {
              RenaultCar_ChargingMode.sendCommand("ALWAYS_CHARGING")
            }
          }
        } else {
          if (RenaultCar_ChargingMode.state.toString != 'ALWAYS_CHARGING' ) {
            RenaultCar_ChargingMode.sendCommand("ALWAYS_CHARGING")
          }
        }
    type: script.ScriptAction

```
