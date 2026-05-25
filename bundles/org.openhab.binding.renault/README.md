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

There is a single thing `car`.
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
| gigyaApiKey       | Gigya API Key. Configure to override the hard coded value for your region. | Hard coded region value          |

## Channels

| Channel ID             | Type               | Description                                         | Read Only |
|------------------------|--------------------|-----------------------------------------------------|-----------|
| batteryavailableenergy | Number:Energy      | Battery Energy Available                            | Yes       |
| batterylevel           | Number             | State of the battery in %                           | Yes       |
| batterystatusupdated   | DateTime           | Timestamp of the last battery status update         | Yes       |
| chargingmode           | String             | Charging mode. `ALWAYS_CHARGING` or `SCHEDULE_MODE` | No        |
| pause                  | Switch             | Pause the charge                                    | No        |
| chargingstatus         | String             | Charging status                                     | Yes       |
| chargingremainingtime  | Number:Time        | Charging time remaining                             | Yes       |
| plugstatus             | String             | Status of charging plug                             | Yes       |
| estimatedrange         | Number:Length      | Estimated range of the car                          | Yes       |
| odometer               | Number:Length      | Total distance travelled                            | Yes       |
| hvacstatus             | String             | HVAC status HVAC Status (ON, OFF, PENDING)          | No        |
| hvactargettemperature  | Number:Temperature | HVAC target temperature (19 to 21)                  | No        |
| externaltemperature    | Number:Temperature | Temperature outside of the car                      | Yes       |
| image                  | String             | Image URL of MyRenault                              | Yes       |
| location               | Location           | The GPS position of the vehicle                     | Yes       |
| locationupdated        | DateTime           | Timestamp of the last location update               | Yes       |
| locked                 | Switch             | Locked status of the car                            | Yes       |

## Limitations

Some channels may not work depending on your car and MyRenault account.

The "externaltemperature" only works on a few cars.

The "hvactargettemperature" is used by the hvacstatus ON command for pre-conditioning the car.
This seams to only allow values 19, 20 and 21 or else the pre-conditioning command will not work.

The 'pause' and 'chargingmode' may not work on some cars.
As an example, 'chargingmode' does not work on Dacia Spring cars.

The `odometer` may not work on some cars.

The Kamereon API Key changes periodically, which causes a communication error.
To fix this error update the API Key in the bindings configuration.
The new key value can hopefully be found in the renault-api project: [KAMEREON_APIKEY value](https://github.com/hacf-fr/renault-api/blob/main/src/renault_api/const.py) or in the openHAB forums.

## Example

renaultcar.things:

```java
renault:car:renault "Renault" [
    accountType="MYRENAULT",
    locale="<your locale>",
    myRenaultUsername="<my renault username>",
    myRenaultPassword="<my renault password>",
    kamereonApiKey="YjkKtHmGfaceeuExUDKGxrLZGGvtVS0J",
    vin="<your vin>",
    gigyaApiKey="",
    updateDelay=60,
    refreshInterval=10
  ]
```

renaultcar.items:

```java
Number:Energy   RenaultCar_BatteryEnergyAvailable    "Battery Energy Available [%.1f %unit%]" <batterylevel>  { channel="renault:car:renault:batteryavailableenergy", unit="kWh" }
Number          RenaultCar_BatteryLevel              "Battery Level [%d %%]"                  <batterylevel>  { channel="renault:car:renault:batterylevel" }
DateTime        RenaultCar_BatteryStatusUpdated      "Battery Status Updated [%1$tH:%1$tM]"  <time>          { channel="renault:car:renault:batterystatusupdated" }
String          RenaultCar_ChargingMode              "Charging Mode [%s]"                     <switch>        { channel="renault:car:renault:chargingmode" }
String          RenaultCar_Pause                     "Pause/Resume Charge [%s]"               <switch>        { channel="renault:car:renault:pause" }
String          RenaultCar_ChargingStatus            "Charging Status [%s]"                   <switch>        { channel="renault:car:renault:chargingstatus" }
Number:Time     RenaultCar_ChargingTimeRemaining     "Charging Time Remaining [%d %unit%]"    <time>          { channel="renault:car:renault:chargingremainingtime", unit="min" }
String          RenaultCar_PlugStatus                "Plug Status [%s]"                       <poweroutlet>   { channel="renault:car:renault:plugstatus" }

Number:Length   RenaultCar_EstimatedRange            "Estimated Range [%d %unit%]"                            { channel="renault:car:renault:estimatedrange", unit="km" }
Number:Length   RenaultCar_Odometer                  "Odometer [%d %unit%]"                                   { channel="renault:car:renault:odometer", unit="km" }

String          RenaultCar_HVACStatus                "HVAC Status [%s]"                       <heating>       { channel="renault:car:renault:hvacstatus" }
Number:Temperature  RenaultCar_HVACTargetTemperature "HVAC Target Temperature [%.0f %unit%]"  <temperature>   { channel="renault:car:renault:hvactargettemperature" }
Number:Temperature  RenaultCar_ExternalTemperature   "External Temperature [%.1f %unit%]"     <temperature>   { channel="renault:car:renault:externaltemperature" }

String          RenaultCar_ImageURL                  "Car Image URL [%s]"                     <image>         { channel="renault:car:renault:image" }
Location        RenaultCar_Location                  "Location Car"                           <zoom>          { channel="renault:car:renault:location" }
DateTime        RenaultCar_LocationUpdate            "Location Updated [%1$tH:%1$tM]"         <time>          { channel="renault:car:renault:locationupdated" }
Switch          RenaultCar_Locked                    "Locked [%s]"                            <lock>          { channel="renault:car:renault:locked" }

// ------------------------------------------------------------
// Additional – Charge limit via Dimmer-item
// Use together with the ChargeRenaultCarLimit rule
// (see binding-documentation for rule code)
// ------------------------------------------------------------

// Maximum load percentage (e.g. 80 is 80%)
Dimmer          RenaultCar_ChargeLimit              "Charge Limit [%d %%]"                   <batterylevel>
```

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
