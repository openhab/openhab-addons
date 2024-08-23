# Ecobee Binding

[Ecobee Inc.](https://www.ecobee.com/)  of Toronto, Canada, sells a range of Wi-Fi
enabled thermostats, principally in the Americas.
This binding communicates with the
[Ecobee API](https://www.ecobee.com/home/developer/api/introduction/index.shtml) over a secure,
RESTful API to Ecobee's servers.
Monitoring ambient temperature and humidity, changing HVAC mode, changing heat or cool setpoints,
changing the backlight intensity, and even sending textual messages to one or a group of thermostats,
can be accomplished through this binding.

## Supported Things

The following thing types are supported:

| Thing          |  ID             |  Description |
|----------------|-----------------|--------------|
| Account        | account         | Represents an Ecobee account. Manages all communication with the Ecobee API. |
| Thermostat     | thermostat      | Represents a physical Ecobee thermostat associated with the Ecobee Account. |
| Remote Sensor  | sensor          | Represents an Ecobee remote sensor that is associated with an Ecobee Thermostat. Also represents the internal sensor of an Ecobee Thermostat. |

## Discovery

Once an Account has been set up, and the API key has been authorized, Thermostats and Remote
Sensors will be discovered automatically.
First, the thermostats will be added to the inbox.
Then, once a Thermostat thing has been created, the Remote Sensors associated with that
thermostat will be added to the inbox.
The binding will detect the capabilities (e.g. temperature, humidity, occupancy) supported by the
sensor, and then dynamically create channels for those capabilities.

## Getting Started

### Getting an API Key

Before you can start using the Ecobee binding, you need to get an API key.
The following steps describe what you need to do.

1. First you need to register your thermostat(s) with Ecobee.
To complete this step, follow the directions in your Ecobee set up documentation.

1. Once the thermostats are registered, the next step is to sign up as a developer.
Do this from the Ecobee [developer portal](https://www.ecobee.com/developers/).

1. Now that you are set up as an Ecobee developer, login to your Ecobee [web portal](https://www.ecobee.com/).
Once logged in, select the **Developer** option from the menu.
If you don't see the **Developer** option on the menu, then something went wrong with the previous step.

1. Finally, create a new application.

Give the application a name and fill in the application summary.
Select the **Ecobee PIN** Authorization Method, then press **Create**.
You now should see the **API key** for the application you just created.

### Authorization

After you have installed the binding, you can create the Account thing using the API key you just created.
Once the Account thing is created, the binding will try to get information about your thermostats from the Ecobee web service.
When this happens, the binding will determine that it has not yet been authorized by the Ecobee web service.

At this point the binding will retrieve a multi-character PIN code from the Ecobee web service.
The binding will mark the Account thing OFFLINE with a detailed status message that contains the
PIN code needed to complete the authorization.
The PIN code will be valid for several minutes.
The status message will look something like this.

```text
Enter PIN 'JULO-RVLA' in MyApps. PIN expires in 257 minutes.
```

To complete the authorization, the PIN code must be entered into the Ecobee **My Apps** settings in your account at ecobee.com.
This will authorize the binding to work with the thermostats associated with your Ecobee account.
Once authorization is complete, the binding will retrieve information about the available thermostats,
and add those thermostats to the inbox.

## Ecobee Authorization Changes Effective 1 December 2020

Effective 1 Dec 2020, Ecobee implemented changes to their authorization process.
Ecobee recommends that users who authorized with Ecobee prior to 1 Dec 2020 should reauthorize their application as the new process affords greater security.
While the binding will continue to work using the old authorization method, it's recommended that you reauthorize the binding using the following process.

- You may need to update openHAB to get the latest version of the binding

- In the MyApps section of your Ecobee Portal, remove the application using the **Remove App** function.

- Wait up to one hour for the binding to do a token refresh with the Ecobee servers.

- At this point the Ecobee Account thing should be OFFLINE with a CONFIGURATION_PENDING status.

- In the MyApps section of your Ecobee Portal, re-add the binding using the **Add Application** function.
Use the PIN code that is displayed in the Ecobee Account thing status, or in the log file.

- Confirm that the binding is again communicating with the Ecobee servers.
You can do this by verifying that your items are updating, or by putting the binding in DEBUG mode and monitoring the log file.

- Post any issues on the forum.

## Thing Configuration

### Ecobee Account

The following configuration parameters are available on the Ecobee Account:

| Parameter               | Type       |Required/Optional | Description |
|-------------------------|------------|------------------|-------------|
| apiKey                  | String     | Required         | This is the Ecobee API key, which is needed to authorize the binding with the Ecobee servers. |
| refreshIntervalNormal   | Integer    | Required         | Specifies the interval in seconds with which the Ecobee data will be updated under normal operation. |
| refreshIntervalQuick    | Integer    | Required         | Specifies the interval in seconds with which the Ecobee data will be updated after sending an update or executing a function. |
| apiTimeout              | Integer    | Required         | Time in seconds to allow an API request against the Ecobee servers to complete. |
| discoveryEnabled        | Switch     | Required         | Specifies whether the binding should auto-discover thermostats and remote sensors. |

### Ecobee Thermostat

The following configuration parameters are available on the Ecobee Thermostat:

| Parameter               | Required/Optional | Description |
|-------------------------|-------------------|-------------|
| thermostatId            | Required          | This is the ID that is assigned to a thermostat by Ecobee. This parameter is used for all communication with Ecobee involving this thermostat. |

### Ecobee Remote Sensor

The following configuration parameters are available on the Ecobee Remote Sensor:

| Parameter               | Required/Optional | Description |
|-------------------------|-------------------|-------------|
| sensorId                | Required          | This is the ID that is assigned to a remote and/or internal sensor by Ecobee. |

## Channels

### Thermostat Channels

The following channels are available on the Ecobee Thermostat.

| Group | Channel | Type | ReadWrite | Description |
|-------|---------|------|-----------|--------------|
| info | identifier | String |     | Identifier |
| info | name | String | yes | Name |
| info | thermostatRev | String |     | Thermostat Rev |
| info | isRegistered | Switch |     | Is Registered |
| info | modelNumber | String |     | Model Number |
| info | brand | String |     | Brand |
| info | features | String |     | Features |
| info | lastModified | DateTime |     | Last Modified |
| info | thermostatTime | DateTime |     | Thermostat Time |
||||||
| equipmentStatus | equipmentStatus | String |     | Equipment Status |
||||||
| program | currentClimateRef | String |     | Current Climate Ref |
||||||
| runtime | runtimeRev | String |     | Runtime Rev |
| runtime | connected | Switch |     | Connected |
| runtime | firstConnected | DateTime |     | First Connected |
| runtime | connectDateTime | DateTime |     | Connected Date Time |
| runtime | disconnectDateTime | DateTime |     | Disconnected Date Time |
| runtime | lastModified | DateTime |     | Last Modified |
| runtime | lastStatusModified | DateTime |     | Last Status Modified |
| runtime | runtimeDate | String |     | Runtime Date |
| runtime | runtimeInterval | Number |     | Runtime Interval |
| runtime | actualTemperature | Number:Temperature |     | Actual Temperature |
| runtime | actualHumidity | Number:Dimensionless |     | Actual Humidity |
| runtime | rawTemperature | Number:Temperature |     | Raw Temperature |
| runtime | showIconMode | Number |     | Show Icon Mode |
| runtime | desiredHeat | Number:Temperature |     | Desired Heat |
| runtime | desiredCool | Number:Temperature |     | Desired Cool |
| runtime | desiredHumidity | Number:Dimensionless |     | Desired Humidity |
| runtime | desiredDehumidity | Number:Dimensionless |     | Desired Dehumidity |
| runtime | desiredFanMode | String |     | Desired Fan Mode |
| runtime | desiredHeatRangeLow | Number:Temperature |     | Desired Heat Range Low |
| runtime | desiredHeatRangeHigh | Number:Temperature |     | Desired Heat Range High |
| runtime | desiredCoolRangeLow | Number:Temperature |     | Desired Cool Range Low |
| runtime | desiredCoolRangeHigh | Number:Temperature |     | Desired Cool Range High |
| runtime | actualAQAccuracy | Number |     | Actual Air Quality Accuracy |
| runtime | actualAQScore | Number |     | Actual Air Quality Score |
| runtime | actualCO2 | Number:Dimensionless |     | Actual CO2 |
| runtime | actualVOC | Number:Dimensionless |     | Actual VOC |
||||||
| settings | hvacMode | String | yes | HVAC Mode |
| settings | lastServiceDate | String | yes | Last Service Date |
| settings | serviceRemindMe | Switch | yes | Service Remind Me |
| settings | monthsBetweenService | Number | yes | Months Between Service |
| settings | remindMeDate | String | yes | Remind Me Date |
| settings | vent | String | yes | Vent |
| settings | ventilatorMinOnTime | Number | yes | Ventilator Min On Time |
| settings | serviceRemindTechnician | Switch | yes | Service Remind Technician |
| settings | eiLocation | String | yes | EI Location |
| settings | coldTempAlert | Number:Temperature | yes | Cold Temp Alert |
| settings | coldTempAlertEnabled | Switch | yes | Cold Temp Alert Enabled |
| settings | hotTempAlert | Number:Temperature | yes | Hot Temp Alert |
| settings | hotTempAlertEnabled | Switch | yes | Hot Temp Alert Enabled |
| settings | coolStages | Number |     | Cool Stages |
| settings | heatStages | Number |     | Heat Stages |
| settings | maxSetBack | Number | yes | Max Set Back |
| settings | maxSetForward | Number | yes | Max Set Forward |
| settings | quickSaveSetBack | Number | yes | Quick Save Set Back |
| settings | quickSaveSetForward | Number | yes | Quick Save Set Forward |
| settings | hasHeatPump | Switch |     | Has Heat Pump |
| settings | hasForcedAir | Switch |     | Has Forced Air |
| settings | hasBoiler | Switch |     | Has Boiler |
| settings | hasHumidifier | Switch |     | Has Humidifier |
| settings | hasElectric | Switch |     | Has Electric |
| settings | hasDehumidifier | Switch |     | Has Dehumidifier |
| settings | hasErv | Switch |     | Has ERV |
| settings | hasHrv | Switch |     | Has HRV |
| settings | condensationAvoid | Switch | yes | Condensation Avoid |
| settings | useCelsius | Switch | yes | Use Celsius |
| settings | useTimeFormat12 | Switch | yes | Use Time Format 12 |
| settings | locale | String | yes | Locale |
| settings | humidity | String | yes | Humidity |
| settings | humidifierMode | String | yes | Humidifier Mode |
| settings | backlightOnIntensity | Number | yes | Backlight On Intensity |
| settings | backlightSleepIntensity | Number | yes | Backlight Sleep Intensity |
| settings | backlightOffTime | Number | yes | Backlight Off Time |
| settings | soundTickVolume | Number | yes | Sound Tick Volume |
| settings | soundAlertVolume | Number | yes | Sound Alert Volume |
| settings | compressorProtectionMinTime | Number | yes | Compressor Protection Min Time |
| settings | compressorProtectionMinTemp | Number:Temperature | yes | Compressor Protection Min Temp |
| settings | stage1HeatingDifferentialTemp | Number | yes | Stage 1 Heating Differential Temp |
| settings | stage1CoolingDifferentialTemp | Number | yes | Stage 1 Cooling Differential Temp |
| settings | stage1HeatingDissipationTime | Number | yes | Stage 1 Heating Dissipation Time |
| settings | stage1CoolingDissipationTime | Number | yes | Stage 1 Cooling Dissipation Time |
| settings | heatPumpReversalOnCool | Switch | yes | Heat Pump Reversal On Cool |
| settings | fanControlRequired | Switch | yes | Fan Control Required |
| settings | fanMinOnTime | Number | yes | Fan Min On Time |
| settings | heatCoolMinDelta | Number | yes | Heat Cool Min Delta |
| settings | tempCorrection | Number | yes | Temp Correction |
| settings | holdAction | String | yes | Hold Action |
| settings | heatPumpGroundWater | Switch |     | Heat Pump Ground Water |
| settings | dehumidifierMode | String | yes | Dehumidifier Mode |
| settings | dehumidifierLevel | Number | yes | Dehumidifier Level |
| settings | dehumidifyWithAC | Switch | yes | Dehumidify With AC |
| settings | dehumidifyOvercoolOffset | Number | yes | Dehumidify Overcool Effect |
| settings | autoHeatCoolFeatureEnabled | Switch | yes | Auto Heat Cool Feature Enabled |
| settings | wifiOfflineAlert | Switch | yes | WiFi Offline Alert |
| settings | heatMinTemp | Number:Temperature |     | Heat Min Temp |
| settings | heatMaxTemp | Number:Temperature |     | Heat Max Temp |
| settings | coolMinTemp | Number:Temperature |     | Cool Min Temp |
| settings | coolMaxTemp | Number:Temperature |     | Cool Max Temp |
| settings | heatRangeHigh | Number:Temperature | yes | Heat Range High |
| settings | heatRangeLow | Number:Temperature | yes | Heat Range Low |
| settings | coolRangeHigh | Number:Temperature | yes | Cool Range High |
| settings | coolRangeLow | Number:Temperature | yes | Cool Range Low |
| settings | userAccessCode | String |     | User Access Code |
| settings | userAccessSetting | Number |     | User Access Settings |
| settings | auxRuntimeAlert | Number | yes | Aux Runtime Alert |
| settings | auxOutdoorTempAlert | Number:Temperature | yes | Aux Outdoor Temp Alert |
| settings | auxMaxOutdoorTemp | Number:Temperature | yes | Aux Max Outdoor Temp |
| settings | auxRuntimeAlertNotify | Switch | yes | Aux Runtime Alert Notify |
| settings | auxOutdoorTempAlertNotify | Switch | yes | Aux Outdoor Temp Alert Notify |
| settings | auxRuntimeAlertNotifyTechnician | Switch | yes | Aux Runtime Alert Notify Technician |
| settings | auxOutdoorTempAlertNotifyTechnician | Switch | yes | Aux Outdoor Temp Alert Notify Technician |
| settings | disablePreHeating | Switch | yes | Disable Pre Heating |
| settings | disablePreCooling | Switch | yes | Disable Pre Cooling |
| settings | installerCodeRequired | Switch | yes | Installer Code Required |
| settings | drAccept | String | yes | DR Accept |
| settings | isRentalProperty | Switch | yes | Is Rental Property |
| settings | useZoneController | Switch | yes | Use Zone Controller |
| settings | randomStartDelayCool | Number | yes | Random Start Delay Cool |
| settings | randomStartDelayHeat | Number | yes | Random Start Delay Heat |
| settings | humidityHighAlert | Number:Dimensionless | yes | Humidity High Alert |
| settings | humidityLowAlert | Number:Dimensionless | yes | Humidity Low Alert |
| settings | disableHeatPumpAlerts | Switch | yes | Disable Heat Pump Alerts |
| settings | disableAlertsOnIdt | Switch | yes | Disable Alerts On IDT |
| settings | humidityAlertNotify | Switch | yes | Humidity Alert Notify |
| settings | humidityAlertNotifyTechnician | Switch | yes | Humidity Alert Notify Technician |
| settings | tempAlertNotify | Switch | yes | Temp Alert Notify |
| settings | tempAlertNotifyTechnician | Switch | yes | Temp Alert Notify Technician |
| settings | monthlyElectricityBillLimit | Number | yes | Monthly Electricity Bill Limit |
| settings | enableElectricityBillAlert | Switch | yes | Enable Electricity Bill Alert |
| settings | enableProjectedElectricityBillAlert | Switch | yes | Enable Projected Electricity Bill Alert |
| settings | electricityBillingDayOfMonth | Number | yes | Electricity Billing Day Of Month |
| settings | electricityBillCycleMonths | Number | yes | Electricity Bill Cycle Months |
| settings | electricityBillStartMonth | Number | yes | Electricity Bill Start Month |
| settings | ventilatorMinOnTimeHome | Number | yes | Ventilator Min On Time Home |
| settings | ventilatorMinOnTimeAway | Number | yes | Ventilator Min On Time Away |
| settings | backlightOffDuringSleep | Switch | yes | Backlight Off During Sleep |
| settings | autoAway | Switch | yes | Auto Away |
| settings | smartCirculation | Switch | yes | Smart Circulation |
| settings | followMeComfort | Switch | yes | Follow Me Comfort |
| settings | ventilatorType | String |     | Ventilator Type |
| settings | isVentilatorTimerOn | Switch | yes | Is Ventilator Timer On |
| settings | ventilatorOffDateTime | String |     | Ventilator Off Date Time |
| settings | hasUVFilter | Switch | yes | Has UV Filter |
| settings | coolingLockout | Switch | yes | Cooling Lockout |
| settings | ventilatorFreeCooling | Switch | yes | Ventilator Free Cooling |
| settings | dehumidifyWhenHeating | Switch | yes | Dehumidify When Heating |
| settings | ventilatorDehumidify | Switch | yes | Ventilator Dehumidify |
| settings | groupRef | String | yes | Group Ref |
| settings | groupName | String | yes | Group Name |
| settings | groupSetting | Number | yes | Group Setting |
||||||
| alerts | acknowledgeRef | String |     | Acknowledge Ref |
| alerts | date | String |     | Date |
| alerts | time | String |     | Time |
| alerts | severity | String |     | Severity |
| alerts | text | String |     | Text |
| alerts | number | Number |     | Number |
| alerts | type | String |     | Type |
| alerts | isOperatorAlert | Switch |     | Is Operator Alert |
| alerts | reminder | String |     | Reminder |
| alerts | showIdt | Switch |     | Show IDT |
| alerts | showWeb | Switch |     | Show Web |
| alerts | sendEmail | Switch |     | Send Email |
| alerts | acknowledgement | String |     | Acknowledgement |
| alerts | remindMeLater | Switch |     | Remind Me Later |
| alerts | thermostatIdentifier | String |     | Thermostat Identifier |
| alerts | notificationType | String |     | Notification Type |
||||||
| events | name | String |     | Event Name |
| events | type | String |     | Event Type |
| events | running | Switch |     | Event is Running |
| events | startDate | String |     | Event Start Date |
| events | startTime | String |     | Event Start Time |
| events | endDate | String |     | Event End Date |
| events | endTime | String |     | Event End Time |
| events | isOccupied | Switch |     | Is Occupied |
| events | isCoolOff | Switch |     | Is Cool Off |
| events | isHeatOff | Switch |     | Is Heat Off |
| events | coolHoldTemp | Number:Temperature |     | Cool Hold Temp |
| events | heatHoldTemp | Number:Temperature |     | Heat Hold Temp |
| events | fan | String |     | Fan |
| events | vent | String |     | Vent |
| events | ventilatorMinOnTime | Number |     | Ventilator Min On Time |
| events | isOptional | Switch |     | Is Optional |
| events | isTemperatureRelative | Switch |     | Is Temperature Relative |
| events | coolRelativeTemp | Number |     | Cool Relative Temp |
| events | heatRelativeTemp | Number |     | Heat Relative Temp |
| events | isTemperatureAbsolute | Switch |     | Is Temperature Absolute |
| events | dutyCyclePercentage | Number |     | Duty Cycle Percentage |
| events | fanMinOnTime | Number |     | Fan Min On Time |
| events | occupiedSensorActive | Switch |     | Occupied Sensor Active |
| events | unoccupiedSensorActive | Switch |     | Unoccupied Sensor Active |
| events | drRampUpTemp | Number |     | DR Ramp Up Temp |
| events | drRampUpTime | Number |     | DR Ramp Up Time |
| events | linkRef | String |     | Link Ref |
| events | holdClimateRef | String |     | Hold Climate Ref |
||||||
| weather | timestamp | DateTime |     | Timestamp |
| weather | weatherStation | String |     | Weather Station |
| weather | weatherSymbol | Number |     | Symbol |
| weather | weatherSymbolText | String |     | Symbol Text |
||||||
| forecast0..9 | dateTime | DateTime |     | Date Time |
| forecast0..9 | condition | String |     | Condition |
| forecast0..9 | temperature | Number:Temperature |     | Temperature |
| forecast0..9 | pressure | Number:Pressure |     | Pressure |
| forecast0..9 | relativeHumidity | Number:Dimensionless |     | Relative Humidity |
| forecast0..9 | dewpoint | Number:Temperature |     | Dewpoint |
| forecast0..9 | visibility | Number |     | Visibility |
| forecast0..9 | windSpeed | Number:Speed |     | Wind Speed |
| forecast0..9 | windGust | Number:Speed |     | Wind Gust |
| forecast0..9 | windDirection | String |     | Wind Direction |
| forecast0..9 | windBearing | Number:Angle |     | Wind Bearing |
| forecast0..9 | pop | Number:Dimensionless |     | Probability of Precipitation |
| forecast0..9 | tempHigh | Number:Temperature |     | High Temperature |
| forecast0..9 | tempLow | Number:Temperature |     | Low Temperature |
| forecast0..9 | sky | Number |     | Sky |
| forecast0..9 | skyText | String |     | Sky Text |
||||||
| location | timeZoneOffsetMinutes | Number |     | Time Zone Offset Minutes |
| location | timeZone | String | yes | Time Zone |
| location | isDaylightSaving | Switch |     | Is Daylight Saving |
| location | streetAddress | String | yes | Street Address |
| location | city | String | yes | City |
| location | provinceState | String | yes | Province/State |
| location | country | String | yes | Country |
| location | postalCode | String | yes | Postal Code |
| location | phoneNumber | String | yes | Phone Number |
| location | mapCoordinates | Location | yes | Thermostat Location |
||||||
| houseDetails | style | String | yes | Style |
| houseDetails | size | Number | yes | Size |
| houseDetails | numberOfFloors | Number | yes | Number of Floors |
| houseDetails | numberOfRooms | Number | yes | Number of Rooms |
| houseDetails | numberOfOccupants | Number | yes | Number of Occupants |
| houseDetails | age | Number | yes | Age |
| houseDetails | windowEfficiency | Number | yes | Window Efficiency |
||||||
| management | administrativeContact | String |     | Administrative Contact |
| management | billingContact | String |     | Billing Contact |
| management | name | String |     | Name |
| management | phone | String |     | Phone |
| management | email | String |     | Email |
| management | web | String |     | Web |
| management | showAlertIdt | Switch |     | Show Alert Idt |
| management | showAlertWeb | Switch |     | Show Alert Web |
||||||
| technician | contractorRef | String |     | Contractor Ref |
| technician | name | String |     | Name |
| technician | phone | String |     | Phone |
| technician | streetAddress | String |     | Street Address |
| technician | city | String |     | City |
| technician | provinceState | String | yes | Province/State |
| technician | country | String |     | Country |
| technician | postalCode | String |     | Postal Code |
| technician | email | String |     | Email |
| technician | web | String |     | Web |
||||||
| version | thermostatFirmwareVersion | String |     | Firmware Version |

### Remote Sensor Channels

The following channels are available on the Ecobee Remote Sensor.

| Channel      | Type     | ReadWrite   | Description  |
|--------------|----------|-------------|--------------|
| id           | String   |             | Sensor ID assigned by thermostat  |
| name         | String   |             | Name given to the remote sensor by the user  |
| type         | String   |             | The type of sensor  |
| code         | String   |             | The unique 4-digit alphanumeric sensor code  |
| inUse        | Switch   |             | Indicates whether the remote sensor is currently in use by a comfort setting  |

Some or all of the following Remote Sensor channels will be added dynamically depending on the capabilities of the sensor.

| Channel            | Type                  | ReadWrite | Description  |
|--------------------|-----------------------|-----------|--------------|
| temperature        | Number:Temperature    |           | Temperature reported by the sensor  |
| humidity           | Number:Dimensionless  |           | Humidity reported by the sensor  |
| occupancy          | Switch                |           | Occupancy status reported by the sensor  |
| adc                | String                |           | ADC reported by the sensor  |
| airPressure        | String                |           | Air Pressure reported by the sensor  |
| airQuality         | String                |           | Air Quality reported by the sensor (clean-poor)  |
| airQualityAccuracy | String                |           | Air Quality Accuracy reported by the sensor  |
| co2                | String                |           | CO2 reported by the sensor  |
| co2PPM             | String                |           | CO2 level reported by the sensor (low-high)  |
| dryContact         | String                |           | Dry contact status reported by the sensor  |
| vocPPM             | String                |           | Volatile organic compounds (VOC) reported by the sensor (low-high)  |

## Thing Actions

### Acknowledge

The acknowledge function allows an alert to be acknowledged by specifying the alert's acknowledgement ref.

#### acknowledge - acknowledge an alert

```java
boolean acknowledge(String ackRef, String ackType, Boolean remindMeLater)
```

```text
Parameters:
ackRef - The acknowledge ref of alert.
ackType - The type of acknowledgement. Valid values: accept, decline, defer, unacknowledged.
remindMeLater - Whether to remind at a later date, if this is a defer acknowledgement.

Returns - true if the operation was successful, false otherwise
```

### Control Plug

Control the on/off state of a plug by setting a hold on the plug.
Creates a hold for the on or off state of the plug for the specified duration.
Note that an event is created regardless of whether the program is in the same state as the requested state.

#### controlPlug - Control the on/off state of a plug

```java
boolean controlPlug(String plugName, String plugState, Date startDateTime, Date endDateTime, String holdType, Number holdHours)
```

```text
Parameters:
plugName - Name of plug to be controlled.
plugState - State to which plug should be set (on, off, resume).
startDateTime - Start time for which the plug state should be applied.
endDateTime - End time for which the plug state should be applied.
holdType - Type of hold that should be applied (dateTime, nextTransityion, indefinite, holdHours).
holdHours - Number of hours for which the plug state should be applied.

Returns - true if the operation was successful, false otherwise
```

### Create Vacation

If the start/end date/times are not provided for the vacation event,
the vacation event will begin immediately and last 14 days.
If both the coolHoldTemp and heatHoldTemp parameters provided to this function have the same value,
and the Thermostat is in auto mode, then the two values will be adjusted during processing to be
separated by the value stored in thermostat.settings.heatCoolMinDelta.

#### createVacation - Create a vacation event on the thermostat

```java
boolean createVacation(String name, QuantityType<Temperature> coolHoldTemp, QuantityType<Temperature> heatHoldTemp, Date startDateTime, Date endDateTime, String fan, Number fanMinOnTime)
```

```text
Parameters:
name - The vacation event name. It must be unique.
coolHoldTemp - The temperature to set the cool vacation hold at.
heatHoldTemp - The temperature to set the heat vacation hold at.
startDateTime - The start date/time.
endDateTime - The end date/time.
fan - The fan mode during the vacation. Values: auto, on Default: auto
fanMinOnTime - The minimum number of minutes to run the fan each hour. Range: 0-60, Default: 0

Returns - true if the operation was successful, false otherwise
```

### Delete Vacation

The delete vacation function deletes a vacation event from a thermostat.
This is the only way to cancel a vacation event.
This method is able to remove vacation events not yet started and scheduled in the future.

#### deleteVacation - delete a vacation event from a thermostat

```java
boolean deleteVacation(String name)
```

```text
Parameters:
name - Name of vacation to be deleted.

Returns - true if the operation was successful, false otherwise
```

### Reset Preferences

The reset preferences function sets all of the user configurable settings back
to the factory default values.
This function call will not only reset the top level thermostat settings such
as hvacMode, lastServiceDate and vent, but also all of the user configurable
fields of the thermostat.settings and thermostat.program objects.
Note that this does not reset all values.
For example, the installer settings and wifi details remain untouched.

```java
boolean resetPreferences()
```

```text
Returns - true if the operation was successful, false otherwise
```

### Resume program

The resume program function removes the currently running event providing the event
is not a mandatory demand response event.
If resumeAll parameter is not set, top active event is removed from the stack and
the thermostat resumes its program, or enters the next event in the stack if one exists.
If resumeAll parameter set to true, the function resumes all events and returns the thermostat to its program.

#### resumeProgram - Remove the currently running event

```java
boolean resumeProgram(Boolean resumeAll)
```

```text
Parameters:
resumeAll - Indicates if the thermostat should be resumed to next event (false) or to its program (true).

Returns - true if the operation was successful, false otherwise
```

### Send Message

The send message function allows an alert message to be sent to the thermostat.
The message properties are same as those of the Alert Object.

#### - sendMessage - Send a message to a thermostat

```java
boolean sendMessage(String text)
```

```text
Parameters:
text - Text of message to be sent to the thermostat.

Returns - true if the operation was successful, false otherwise
```

### Set Hold

The set hold function sets the thermostat into a hold with the specified temperature.
Creates a hold for the specified duration.
Note that an event is created regardless of whether the program is in the same state as the requested state.

There is also support for creating a hold by passing a holdClimateRef request parameter/value pair to this function.
When an existing and valid Climate.climateRef value is passed to this function, the coolHoldTemp,
heatHoldTemp and fan mode from that Climate are used in the creation of the hold event.
The values from that Climate will take precedence over any coolHoldTemp, heatHoldTemp and fan mode
parameters passed into this function separately.

To resume from a hold and return to the program, use the `resumeProgram` function.

#### setHold - Set an indefinite hold using the supplied coolHoldTemp and heatHoldTemp

```java
boolean setHold(QuantityType<Temperature> coolHoldTemp, QuantityType<Temperature> heatHoldTemp)
```

```text
Parameters:
coolHoldTemp - The temperature to set the cool hold at.
heatHoldTemp - The temperature to set the heat hold at.

Returns - true if the operation was successful, false otherwise
```

##### setHold - Set a hold using the supplied cool and heat temperatures that lasts for the specified number of hours

```java
boolean setHold(QuantityType<Temperature> coolHoldTemp, QuantityType<Temperature> heatHoldTemp, Number holdHours)
```

```text
Parameters:
coolHoldTemp - The temperature to set the cool hold at.
heatHoldTemp - The temperature to set the heat hold at.
holdHours - Duration of hold.

Returns - true if the operation was successful, false otherwise
```

##### setHold - Set an indefinite hold using the supplied climate ref

```java
boolean setHold(String climateRef)
```

```text
Parameters:
climateRef - Climate to be applied to thermostat (e.g. home, away).

Returns - true if the operation was successful, false otherwise
```

##### setHold - Set a hold using the supplied climate ref that lasts for the specified number of hours

```java
boolean setHold(String climateRef, Number holdHours)
```

```text
Parameters:
climateRef - Climate to be applied to thermostat (e.g. home, away).
holdHours - Duration of hold.

Returns - true if the operation was successful, false otherwise
```

##### setHold - Quantity Type

```java
boolean setHold(QuantityType<Temperature> coolHoldTemp, QuantityType<Temperature> heatHoldTemp, String holdClimateRef, Date startDateTime, Date endDateTime, String holdType, Number holdHours)
```

```text
Parameters:
coolHoldTemp - The temperature to set the cool hold at.
heatHoldTemp - The temperature to set the heat hold at.
holdClimateRef - Climate to be applied to thermostat (e.g. home, away).
startDateTime - The start date/time of the hold.
endDateTime - The end date/time of the hold.
holdType - The hold duration type. Valid values: dateTime, nextTransition, indefinite, holdHours.
holdHours - Duration of hold.

Returns - true if the operation was successful, false otherwise
```

##### setHold - Map

```java
boolean setHold(Map<String,Object> params, String holdType, Number holdHours, Date startDateTime, Date endDateTime)

Parameters:
params - The map of hold parameters.
holdType - The hold duration type. Valid values: dateTime, nextTransition, indefinite, holdHours.
holdHours - Duration of hold.
startDateTime - The start date/time of the hold.
endDateTime - The end date/time of the hold.

Returns - true if the operation was successful, false otherwise
```

### Set Occupied

The set occupied function may only be used by EMS thermostats.
The function switches a thermostat from occupied mode to unoccupied, or vice versa.
If used on a Smart thermostat, the function will throw an error.
Switch occupancy events are treated as Holds.
There may only be one Switch Occupancy at one time, and the new event will replace any previous event.

Note that an occupancy event is created regardless what the program on the thermostat is set to.
For example, if the program is currently unoccupied and you set occupied=false, an occupancy event
will be created using the heat/cool settings of the unoccupied program climate.
If your intent is to go back to the program and remove the occupancy event, use resumeProgram instead.

#### setOccupied -

```java
boolean setOccupied(Boolean occupied, Date startDateTime, Date endDateTime, String holdType, Number holdHours)
```

```text
Parameters:
occupied - The climate to use for the temperature, occupied (true) or unoccupied (false).
holdType - The hold duration type. Valid values: dateTime, nextTransition, indefinite, holdHours.
holdHours - Duration of hold.
startDateTime - The start date/time.
endDateTime - The end date/time.

Returns - true if the operation was successful, false otherwise
```

### Update Sensor

The update sensor function allows the caller to update the name of an ecobee3 remote sensor.

Each ecobee3 remote sensor "enclosure" contains two distinct sensors types temperature and occupancy.
Only one of the sensors is required in the request.
Both of the sensors' names will be updated to ensure consistency as they are part of the
same remote sensor enclosure.
This also reflects accurately what happens on the Thermostat itself.

#### updateSensor - Update the name of a sensor

```java
boolean updateSensor(String name, String deviceId, String sensorId)
```

```text
Parameters:
name - The updated name to give the sensor. Has a max length of 32, but shorter is recommended.
deviceId - The deviceId for the sensor, typically this indicates the enclosure and corresponds to the ThermostatRemoteSensor.id field. For example: rs:100
sensorId - The idendifier for the sensor within the enclosure. Corresponds to the RemoteSensorCapability.id. For example: 1

Returns - true if the operation was successful, false otherwise
```

### Get Alerts

#### getAlerts - Get the list of alerts

```java
String getAlerts()
```

```text
Returns - A JSON string representing the array of alerts for the thermostat, or null if there are no alerts.
```

### Get Events

#### getEvents - Get the list of events

```java
String getEvents()
```

```text
Returns - A JSON string representing the array of events for the thermostat, or null if there are no events.
```

### Get Climates

#### getClimates - Get the list of climates configured on this thermostat

```java
String getClimates()
```

```text
Returns - A JSON string representing the array of climates for the thermostat.
```

## Full Example

### Things

```java
Bridge ecobee:account:account "Ecobee Account" [ apiKey="kjafhd4YTiucye48yn498n94c8ufn49", refreshIntervalNormal=30, refreshIntervalQuick=5, apiTimeout=20, discoveryEnabled=false ] {
    Bridge thermostat 32122305166 "Ecobee First Floor Thermostat" [ thermostatId="32122305166" ] {
        Thing sensor ei-0 "Ecobee Sensor Thermostat" [ sensorId="ei:0" ] {
            Channels:
                Type sensorTemperature : temperature [ ]
                Type sensorHumidity : humidity [ ]
                Type sensorOccupancy : occupancy [ ]
        }
        Thing sensor rs-101 "Ecobee Sensor Room 1" [ sensorId="rs:101" ] {
            Channels:
                Type sensorTemperature : temperature [ ]
                Type sensorOccupancy : occupancy [ ]
        }
        Thing sensor rs-100 "Ecobee Sensor Room 2" [ sensorId="rs:100" ] {
            Channels:
                Type sensorTemperature : temperature [ ]
                Type sensorOccupancy : occupancy [ ]
        }
    }
    Bridge thermostat 385421394655 "Ecobee Upstairs Thermostat" [ thermostatId="385421394655" ] {
    }
}
```

### Items

```java
Group gInfo "Information"
Group gRuntime "Runtime"
Group gEquipmentStatus "Equipment Status"
Group gSettings "Settings"
Group gProgram "Program"
Group gAlert "First Alert in Alert List"
Group gEvent "Currently Running Event"
Group gWeather "Weather Forecast"
Group gVersion "Version"
Group gLocation "Location"
Group gHouseDetails "House Details"
Group gManagement "Management"
Group gTechnician "Technician"
Group gThermostatSensor "Thermostat Sensor"
Group gRoom1Sensor "Room 1 Sensor"
Group gRoom2Sensor "Room 2 Sensor"

// Info group
String Info_Identifier "Thermostat Identifier [%s]" <text> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#identifier" }
String Info_Name "Thermostat Name [%s]" <text> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#name" }
String Info_ThermostatRev "Thermostat Rev [%s]" <text> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#thermostatRev" }
Switch Info_IsRegistered "Is Registered [%s]" <switch> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#isRegistered" }
String Info_ModelNumber "Model Number [%s]" <text> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#modelNumber" }
String Info_Brand "Brand [%s]" <text> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#brand" }
String Info_Features "Features [%s]" <text> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#features" }
DateTime Info_LastModified "Last Modified [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#lastModified" }
DateTime Info_ThermostatTime "Thermostat Time [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gInfo) { channel="ecobee:thermostat:account:729318833078:info#thermostatTime" }

// Equipment Status group
String EquipmentStatus_EquipmentStatus "Equipment Status [%s]" <text> (gEquipmentStatus) { channel="ecobee:thermostat:account:729318833078:equipmentStatus#equipmentStatus" }

// Runtime group
String Runtime_RuntimeRev "Runtime Rev [%s]" <text> (gRuntime) ["tag"] { channel="ecobee:thermostat:account:729318833078:runtime#runtimeRev" }
Switch Runtime_Connected "Connected [%s]" <switch> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#connected" }
DateTime Runtime_FirstConnected "First Connected [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#firstConnected" }
DateTime Runtime_ConnectDateTime "Connect Date Time [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#connectDateTime" }
DateTime Runtime_DisconnectDateTime "Disconnect Date Time [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#disconnectDateTime" }
DateTime Runtime_LastModified "Last Modified [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#lastModified" }
DateTime Runtime_LastStatusModified "Last Status Modified [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#lastStatusModified" }
String Runtime_RuntimeDate "Runtime Date [%s]" <text> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#runtimeDate" }
Number Runtime_RuntimeInterval "Runtime Interval [%.0f]" <none> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#runtimeInterval" }
Number:Temperature Runtime_ActualTemperature "Actual Temperature [%.1f %unit%]" <temperature> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#actualTemperature" }
Number:Dimensionless Runtime_ActualHumidity "Actual Humidity [%.0f %unit%]" <humidity> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#actualHumidity" }
Number:Temperature Runtime_RawTemperature "Raw Temperature [%.1f %unit%]" <temperature> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#rawTemperature" }
Number Runtime_ShowIconMode "Show Icon Mode [%.0f]" <none> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#showIconMode" }
Number:Temperature Runtime_DesiredHeat "Desired Heat [%.1f %unit%]" <temperature> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredHeat" }
Number:Temperature Runtime_DesiredCool "Desired Cool [%.1f %unit%]" <temperature> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredCool" }
Number:Dimensionless Runtime_DesiredHumidity "Desired Humidity [%.0f %unit%]" <humidity> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredHumidity" }
Number:Dimensionless Runtime_DesiredDehumidity "Desired Dehumidity [%.0f %unit%]" <humidity> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredDehumidity" }
String Runtime_DesiredFanMode "Desired Fan Mode [%s]" <text> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredFanMode" }
Number:Temperature Runtime_DesiredHeatRangeLow "Desired Heat Range Low [%.1f %unit%]" <temperature> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredHeatRangeLow" }
Number:Temperature Runtime_DesiredHeatRangeHigh "Desired Heat Range High [%.1f %unit%]" <temperature> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredHeatRangeHigh" }
Number:Temperature Runtime_DesiredCoolRangeLow "Desired Cool Range Low [%.1f %unit%]" <temperature> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredCoolRangeLow" }
Number:Temperature Runtime_DesiredCoolRangeHigh "Desired Cool Range High [%.1f %unit%]" <temperature> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#desiredCoolRangeHigh" }
Number Runtime_ActualAQAccuracy "Actual Air Quality Accuracy [%d]" <none> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#actualAQAccuracy" }
Number Runtime_ActualAQScore "Actual Air Quality Score [%d]" <none> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#actualAQScore" }
Number:Dimensionless Runtime_ActualCO2 "Actual CO2 [%d %unit%]" <carbondioxide> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#actualCO2" }
Number:Dimensionless Runtime_ActualVOC "Actual VOC [%d %unit%]" <none> (gRuntime) { channel="ecobee:thermostat:account:729318833078:runtime#actualVOC" }

// Settings group
String Settings_HvacMode "HVAC Mode [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hvacMode" }
String Settings_LastServiceDate "Last Service Date [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#lastServiceDate" }
Switch Settings_ServiceRemindMe "Service Remind Me [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#serviceRemindMe" }
Number Settings_MonthsBetweenService "Months Between Service [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#monthsBetweenService" }
String Settings_RemindMeDate "Remind Me Date [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#remindMeDate" }
String Settings_Vent "Vent [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#vent" }
Number Settings_VentilatorMinOnTime "Ventilator Min On Time [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#ventilatorMinOnTime" }
Switch Settings_ServiceRemindTechnician "Service Remind Technician [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#serviceRemindTechnician" }
String Settings_Location "EI Location [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#eiLocation" }
Number:Temperature Settings_ColdTempAlert "Cold Temp Alert [%.0f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#coldTempAlert" }
Switch Settings_ColdTempAlertEnabled "Cold Temp Alert Enabled [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#coldTempAlertEnabled" }
Number:Temperature Settings_HotTempAlert "Hot Temp Alert [%.0f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hotTempAlert" }
Switch Settings_HotTempAlertEnabled "Hot Temp Alert Enabled [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hotTempAlertEnabled" }
Number Settings_CoolStages "Cool Stages [%s]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#coolStages" }
Number Settings_HeatStages "Heat Stages [%s]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#heatStages" }
Number Settings_MaxSetBack "Max Set Back [%s]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#maxSetBack" }
Number Settings_MaxSetForward "Max Set Forward [%s]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#maxSetForward" }
Number Settings_QuickSaveSetBack "Quick Save Set Back [%s]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#quickSaveSetBack" }
Number Settings_QuickSaveSetForward "Quick Save Set Forward [%s]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#quickSaveSetForward" }
Switch Settings_HasHeatPump "Has Heat Pump [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasHeatPump" }
Switch Settings_HasForcedAir "Has Forced Air [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasForcedAir" }
Switch Settings_HasBoiler "Has Boiler [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasBoiler" }
Switch Settings_HasHumidifier "Has Humidifier [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasHumidifier" }
Switch Settings_HasERV "Has ERV [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasErv" }
Switch Settings_HasHRV "Has HRV [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasHrv" }
Switch Settings_CondensationAvoid "Condensation Avoid [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#condensationAvoid" }
Switch Settings_UseCelsius "Use Celsius [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#useCelsius" }
Switch Settings_UseTime_Format12 "Use Time Format 12 [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#useTimeFormat12" }
String Settings_Locale "Locale [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#locale" }
String Settings_Humidity "Humidity [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#humidity" }
String Settings_HumidifierMode "Humidifier Mode [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#humidifierMode" }
Number Settings_BacklightOnIntensity "Backlight On Intensity [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#backlightOnIntensity" }
Number Settings_BacklightSleepIntensity "Backlight Sleep Intensity [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#backlightSleepIntensity" }
Number Settings_BacklightOffTime "Backlight Off Time [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#backlightOffTime" }
Number Settings_SoundTickVolume "Sound Tick Volume [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#soundTickVolume" }
Number Settings_SoundAlertVolume "Sound Alert Volume [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#soundAlertVolume" }
Number Settings_CompressorProtectionMinTime "Compressor Protection Min Time [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#compressorProtectionMinTime" }
Number:Temperature Settings_CompressorProtectionMinTemp "Compressor Protection Min Temp [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#compressorProtectionMinTemp" }
Number Settings_Stage1HeatingDifferentialTemp "Stage 1 Heating Differential Temp [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#stage1HeatingDifferentialTemp" }
Number Settings_Stage1CoolingDifferentialTemp "Stage 1 Cooling Differential Temp [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#stage1CoolingDifferentialTemp" }
Number Settings_Stage1HeatingDissipationTime "Stage 1 Heating Dissipation Time [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#stage1HeatingDissipationTime" }
Number Settings_Stage1CoolingDissipationTime "Stage 1 Cooling Dissipation Time [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#stage1CoolingDissipationTime" }
Switch Settings_HeatPumpReversalOnCool "Heat Pump Reversal On Cool [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#heatPumpReversalOnCool" }
Switch Settings_FanControlRequired "Fan Control Required [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#fanControlRequired" }
Number Settings_FanMinOnTime "Fan Min On Time [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#fanMinOnTime" }
Number Settings_HeatCoolMinDelta "Heat Cool Min Delta [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#heatCoolMinDelta" }
Number Settings_TempCorrection "Temp Correction [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#tempCorrection" }
String Settings_HoldAction "Hold Action [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#holdAction" }
Switch Settings_HeatPumpGroundWater "Heat Pump Ground Water [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#heatPumpGroundWater" }
Switch Settings_HasElectric "Has Electric [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasElectric" }
Switch Settings_HasDehumidifier "Has Dehumidifier [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasDehumidifier" }
String Settings_DehumidifierMode "Dehumidifier Mode [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#dehumidifierMode" }
Number Settings_DehumidifierLevel "Dehumidifier Level [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#dehumidifierLevel" }
Switch Settings_DehumidifyWithAC "Dehumidify With AC [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#dehumidifyWithAC" }
Number Settings_DehumidifyOvercoolEffect "Dehumidify Overcool Effect [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#dehumidifyOvercoolOffset" }
Switch Settings_AutoHeatCoolFeatureEnabled "Auto Heat Cool Feature Enabled [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#autoHeatCoolFeatureEnabled" }
Switch Settings_WiFiOfflineAlert "WiFi Offline Alert [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#wifiOfflineAlert" }
Number:Temperature Settings_HeatMinTemp "Heat Min Temp [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#heatMinTemp" }
Number:Temperature Settings_HeatMaxTemp "Heat Max Temp [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#heatMaxTemp" }
Number:Temperature Settings_CoolMinTemp "Cool Min Temp [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#coolMinTemp" }
Number:Temperature Settings_CoolMaxTemp "Cool Max Temp [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#coolMaxTemp" }
Number:Temperature Settings_HeatRangeHigh "Heat Range High [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#heatRangeHigh" }
Number:Temperature Settings_HeatRangeLow "Heat Range Low [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#heatRangeLow" }
Number:Temperature Settings_CoolRangeHigh "Cool Range High [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#coolRangeHigh" }
Number:Temperature Settings_CoolRangeLow "Cool Range Low [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#coolRangeLow" }
String Settings_UserAccessCode "User Access Code [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#userAccessCode" }
Number Settings_UserAccessSettings "User Access Settings [%s]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#userAccessSetting" }
Number Settings_AuxRuntimeAlert "Aux Runtime Alert [%s]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#auxRuntimeAlert" }
Number:Temperature Settings_AuxOutdoorTempAlert "Aux Outdoor Temp Alert [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#auxOutdoorTempAlert" }
Number:Temperature Settings_AuxMaxOutdoorTemp "Aux Max Outdoor Temp [%.1f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#auxMaxOutdoorTemp" }
Switch Settings_AuxRuntimeAlertNotify "Aux Runtime Alert Notify [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#auxRuntimeAlertNotify" }
Switch Settings_AuxOutdoorTempAlertNotify "Aux Outdoor Temp Alert Notify [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#auxOutdoorTempAlertNotify" }
Switch Settings_AuxRuntimeAlertNotifyTechnician "Aux Runtime Alert Notifi Technician [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#auxRuntimeAlertNotifyTechnician" }
Switch Settings_AuxOutdoorTempAlertNotifyTechnician "Aux Outdoor Temp Alert Notify Technician [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#auxOutdoorTempAlertNotifyTechnician" }
Switch Settings_DisablePreHeating "Disable Pre Heating [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#disablePreHeating" }
Switch Settings_DisablePreCooling "Disable Pre Cooling [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#disablePreCooling" }
Switch Settings_InstallerCodeRequired "Installer Code Required [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#installerCodeRequired" }
String Settings_DRAccept "DR Accept [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#drAccept" }
Switch Settings_IsRentalProperty "Is Rental Property [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#isRentalProperty" }
Switch Settings_UseZoneController "Use Zone Controller [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#useZoneController" }
Number Settings_RandomStartDelayCool "Random Start Delay Cool [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#randomStartDelayCool" }
Number Settings_RandomStartDelayHeat "Random Start Delay Heat [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#randomStartDelayHeat" }
Number:Dimensionless Settings_HumidityHighAlert "Humidity High Alert [%.0f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#humidityHighAlert" }
Number:Dimensionless Settings_HumidityLowAlert "Humidity Low Alert [%.0f %unit%]" <temperature> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#humidityLowAlert" }
Switch Settings_DisableHeatPumpAlerts "Disable Heat Pump Alerts [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#disableHeatPumpAlerts" }
Switch Settings_DisableAlertsOnIDT "Disable Alerts On IDT [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#disableAlertsOnIdt" }
Switch Settings_HumidityAlertNotify "Humidity Alert Notify [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#humidityAlertNotify" }
Switch Settings_HumidityAlertNotifyTechnicial "Humidity Alert Notify Technician [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#humidityAlertNotify" }
Switch Settings_TempAlertNotify "Temp Alert Notify [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#tempAlertNotify" }
Switch Settings_TempAlertNotifyTechnician "Temp Alert Notify Technician [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#tempAlertNotify" }
Number Settings_MonthlyElectricityBillLimit "Monthly Electricity Bill Limit [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#monthlyElectricityBillLimit" }
Switch Settings_EnableElectricityBillAlert "Enable Electricity Bill Alert [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#enableElectricityBillAlert" }
Switch Settings_EnableProjectedElectricityBillAlert "Enable Projected Electricity Bill Alert [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#enableProjectedElectricityBillAlert" }
Number Settings_ElectricityBillingDayOfMonth "Electricity Billing Day Of Month [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#electricityBillingDayOfMonth" }
Number Settings_ElectricityBillCycleMonths "Electricity Bill Cycle Months [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#electricityBillCycleMonths" }
Number Settings_ElectricityBillStartMonth "Electricity Bill Start Month [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#electricityBillStartMonth" }
Number Settings_VentilatorMinOnTimeHome "Ventilator Min On Time Home [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#ventilatorMinOnTimeHome" }
Number Settings_VentilatorMinOnTimeAway "Ventilator Min On Time Away [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#ventilatorMinOnTimeAway" }
Switch Settings_BacklightOffDuringSleep "Backlight Off During Sleep [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#backlightOffDuringSleep" }
Switch Settings_AutoAway "Auto Away [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#autoAway" }
Switch Settings_SmartCirculation "Smart Circulation [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#smartCirculation" }
Switch Settings_FollowMeComfort "Follow Me Comfort [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#followMeComfort" }
String Settings_VentilatorType "Ventilator Type [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#ventilatorType" }
Switch Settings_IsVentilatorTimerOn "Is Ventilator Timer On [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#isVentilatorTimerOn" }
String Settings_VentilatorOffDateTime "Ventilator Off Date Time [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#ventilatorOffDateTime" }
Switch Settings_HasUVFilter "Has UV Filter [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#hasUVFilter" }
Switch Settings_CoolingLockout "Cooling Lockout [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#coolingLockout" }
Switch Settings_VentilatorFreeCooling "Ventilator Free Cooling [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#ventilatorFreeCooling" }
Switch Settings_DehumidifyWhenHeating "Dehumidify When Heating [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#dehumidifyWhenHeating" }
Switch Settings_VentilatorDehumidify "Ventilator Dehumidify [%s]" <switch> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#ventilatorDehumidify" }
String Settings_GroupRef "Group Ref [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#groupRef" }
String Settings_GroupName "Group Name [%s]" <text> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#groupName" }
Number Settings_GroupSetting "Group Setting [%.0f]" <none> (gSettings) { channel="ecobee:thermostat:account:729318833078:settings#groupSetting" }

// Alert group
String Alert_Achnowledge_Ref "Alert Achnowledge Ref [%s]" <text> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#acknowledgeRef" }
String Alert_Date "Date [%s]" <date> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#date" }
String Alert_Time "Time [%s]" <time> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#time" }
String Alert_Severity "Severity [%s]" <text> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#severity" }
String Alert_Text "Alert Text [%s]" <text> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#text" }
Number Alert_AlertNumber "Alert Number [%.0f]" (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#number" }
String Alert_AlertType "Alert Type [%s]" <text> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#type" }
Switch Alert_IsOperatorAlert "Is Operator Alert [%s]" <switch> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#isOperatorAlert" }
String Alert_Reminder "Reminder [%s]" <text> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#reminder" }
Switch Alert_ShowIdt "Show IDT [%s]" <switch> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#showIdt" }
Switch Alert_ShowWeb "Show Web [%s]" <switch> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#showWeb" }
Switch Alert_SendEmail "Send Email [%s]" <switch> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#sendEmail" }
String Alert_Acknowledgement "Acknowledgement [%s]" <text> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#acknowledgement" }
Switch Alert_RemindMeLater "Remind Me Later [%s]" <switch> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#remindMeLater" }
String Alert_ThermostatIdentifier "Thermostat Identifier [%s]" <text> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#thermostatIdentifier" }
String Alert_NotificationType "Notification Type [%s]" <text> (gAlert) { channel="ecobee:thermostat:account:729318833078:alerts#notificationType" }

// Program group
String Program_CurrentClimateRef "Current Climate Ref [%s]" <text> (gProgram) { channel="ecobee:thermostat:account:729318833078:program#currentClimateRef" }

// Events group
String Event_Name "Name [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#name" }
String Event_Type "Type [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#type" }
Switch Event_Running "Running [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#running" }
String Event_StartDate "Start Date [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#startDate" }
String Event_StartTime "Start Time [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#startTime" }
String Event_EndDate "End Date [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#endDate" }
String Event_EndTime "End Time [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#endTime" }
Switch Event_IsOccupied "Running [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#isOccupied" }
Switch Event_IsCoolOff "Is Cool Off [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#isCoolOff" }
Switch Event_HeatOff "Is Heat Off [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#isHeatOff" }
Number:Temperature Event_CoolHoldTemp "Cool Hold Temp [%.1f %unit%]" (gEvent) { channel="ecobee:thermostat:account:729318833078:events#coolHoldTemp" }
Number:Temperature Event_HeatHoldTemp "Heat Hold Temp [%.1f %unit%]" (gEvent) { channel="ecobee:thermostat:account:729318833078:events#heatHoldTemp" }
String Event_Fan "Fan [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#fan" }
String Event_Vent "Vent [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#vent" }
Number Event_VentilatorMinOnTime "Ventilator Min On Time [%.0f]" <none> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#ventilatorMinOnTime" }
Switch Event_IsOptional "Is Optional [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#isOptional" }
Switch Event_IsTemperatureRelative "Is Temperature Relative [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#isTemperatureRelative" }
Number Event_CoolRelativeTemp "Cool Relative Temp [%.0f]" <none> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#coolRelativeTemp" }
Number Event_HeatRelativeTemp "Heat Relative Temp [%.0f]" <none> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#heatRelativeTemp" }
Switch Event_IsTemperatureAbsolute "Is Temperature Absolute [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#isTemperatureAbsolute" }
Number Event_DutyCyclePercentage "Duty Cycle Percentage [%.0f]" <none> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#dutyCyclePercentage" }
Number Event_FanMinOnTime "Fan Min On Time [%.0f]" <none> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#fanMinOnTime" }
Switch Event_OccupiedSensorActive "Occupied Sensor Active [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#occupiedSensorActive" }
Switch Event_UnoccupiedSensorActive "Unoccupied Sensor Active [%s]" <switch> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#unoccupiedSensorActive" }
Number Event_DrRampUpTemp "DR Ramp Up Temp [%.0f]" <none> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#drRampUpTemp" }
Number Event_DrRampUpTime "DR Ramp Up Time [%.0f]" <none> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#drRampUpTime" }
String Event_LinkRef "Link Ref [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#linkRef" }
String Event_HoldClimateRef "Hold Climate Ref [%s]" <text> (gEvent) { channel="ecobee:thermostat:account:729318833078:events#holdClimateRef" }

// Version group
String Version_ThermostatFirmwareVersion "Thermostat Firmware Version [%s]" <text> (gVersion) { channel="ecobee:thermostat:account:729318833078:version#thermostatFirmwareVersion" }

// Location group
Number Location_TimeZoneOffsetMinutes "Time Zone Offset Minutes [%.0f]" <none> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#timeZoneOffsetMinutes" }
String Location_TimeZone "Time Zone [%s]" <text> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#timeZone" }
Switch Location_IsDaylightSaving "Is Daylight Saving [%s]" <switch> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#isDaylightSaving" }
String Location_StreetAddress "Street Address [%s]" <text> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#streetAddress" }
String Location_City "City [%s]" <text> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#city" }
String Location_ProvinceState "Province/State [%s]" <text> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#provinceState" }
String Location_Country "Country [%s]" <text> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#country" }
String Location_PostalCode "Postal Code [%s]" <text> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#postalCode" }
String Location_PhoneNumber "Phone Number [%s]" <text> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#phoneNumber" }
Location Location_MapCoordinates "Lat/Lon [%s]" <none> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#mapCoordinates" }
Switch Location_ShowAlertIdt "Show Alert Idt [%s]" <switch> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#showAlertIdt" }
Switch Location_ShowAlertWeb "Show Alert Web [%s]" <switch> (gLocation) { channel="ecobee:thermostat:account:729318833078:location#showAlertWeb" }

// House Details group
String HouseDetails_Style "Style [%s]" <text> (gHouseDetails) { channel="ecobee:thermostat:account:729318833078:houseDetails#style" }
Number HouseDetails_Size "Size [%.0f]" <none> (gHouseDetails) { channel="ecobee:thermostat:account:729318833078:houseDetails#size" }
Number HouseDetails_NumberOfFloors "Number of Floors [%.0f]" <none> (gHouseDetails) { channel="ecobee:thermostat:account:729318833078:houseDetails#numberOfFloors" }
Number HouseDetails_NumberOfRooms "Number of Rooms [%.0f]" <none> (gHouseDetails) { channel="ecobee:thermostat:account:729318833078:houseDetails#numberOfRooms" }
Number HouseDetails_NumberOfOccupants "Number of Occupants [%.0f]" <none> (gHouseDetails) { channel="ecobee:thermostat:account:729318833078:houseDetails#numberOfOccupants" }
Number HouseDetails_Age "Age [%.0f]" <none> (gHouseDetails) { channel="ecobee:thermostat:account:729318833078:houseDetails#age" }
Number HouseDetails_WindowEfficiency "Window Efficiency [%.0f]" <none> (gHouseDetails) { channel="ecobee:thermostat:account:729318833078:houseDetails#windowEfficiency" }

// Management group
String Management_AdministrativeContact "Administrative Contact [%s]" <text> (gManagement) { channel="ecobee:thermostat:account:729318833078:management#administrativeContact" }
String Management_BillingContact "Billing Contact [%s]" <text> (gManagement) { channel="ecobee:thermostat:account:729318833078:management#billingContact" }
String Management_Name "Name [%s]" <text> (gManagement) { channel="ecobee:thermostat:account:729318833078:management#Name" }
String Management_Phone "Phone [%s]" <text> (gManagement) { channel="ecobee:thermostat:account:729318833078:management#phone" }
String Management_Email "Email [%s]" <text> (gManagement) { channel="ecobee:thermostat:account:729318833078:management#email" }
String Management_Web "Web [%s]" <text> (gManagement) { channel="ecobee:thermostat:account:729318833078:management#web" }

// Technician group
String Technician_ContractorRef "Contractor Ref [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#contractorRef" }
String Technician_Name "Name [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#name" }
String Technician_Phone "Phone [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#phone" }
String Technician_StreetAddress "Street Address [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#streetAddress" }
String Technician_City "City [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#city" }
String Technician_ProvinceState "Province/State [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#provinceState" }
String Technician_Country "Country [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#country" }
String Technician_PostalCode "Postal Code [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#postalCode" }
String Technician_Email "Email [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#email" }
String Technician_Web "Web [%s]" <text> (gTechnician) { channel="ecobee:thermostat:account:729318833078:technician#web" }

// Weather group
DateTime Weather_Timestamp "Timestamp [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gWeather) { channel="ecobee:thermostat:account:729318833078:weather#timestamp" }
String Weather_WeatherStation "Weather Station [%s]" <text> (gWeather) { channel="ecobee:thermostat:account:729318833078:weather#weatherStation" }

// Forecast0 group
Number Forecast0_WeatherSymbol "Symbol [%.0f]" <none> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#weatherSymbol" }
String Forecast0_WeatherSymbolText "Symbol Text [%s]" <text> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#weatherSymbolText" }
DateTime Forecast0_DateTime "Date/Time [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#dateTime" }
String Forecast0_Condition "Condition [%s]" <text> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#condition" }
Number:Temperature Forecast0_Temperature "Temperature [%.1f %unit%]" <temperature> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#temperature" }
Number:Pressure Forecast0_Pressure "Pressure [%.1f %unit%]" <pressure> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#pressure" }
Number:Dimensionless Forecast0_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#relativeHumidity" }
Number:Temperature Forecast0_Dewpoint "Dewpoint [%.1f %unit%]" <temperature> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#dewpoint" }
Number:Length Forecast0_Visibility "Visibility [%.1f mi]" <none> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#visibility" }
Number:Speed Forecast0_WindSpeed "Wind Speed [%.1f %unit%]" <wind> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#windSpeed" }
Number:Speed Forecast0_WindGust "Wind Gust [%.1f %unit%]" <wind> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#windGust" }
String Forecast0_WindDirection "Wind Direction [%s]" <wind> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#windDirection" }
Number:Angle Forecast0_WindBearing "Wind Bearing [%.0f %unit%]" <wind> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#windBearing" }
Number:Dimensionless Forecast0_ProbabilityOfPrecipitation "Probability of Precipitation [%.0f %unit%]" <none> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#pop" }
Number:Temperature Forecast0_TemperatureHigh "Temperature High [%.1f %unit%]" <temperature> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#tempHigh" }
Number:Temperature Forecast0_TemperatureLow "Temperature Low [%.1f %unit%]" <temperature> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#tempLow" }
Number Forecast0_Sky "Sky [%.0f]" <none> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#sky" }
String Forecast0_SkyText "Sky Text [%s]" <text> (gForecast0) { channel="ecobee:thermostat:account:729318833078:forecast0#skyText" }

// Forecast1 group
Number Forecast1_WeatherSymbol "Symbol [%.0f]" <none> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#weatherSymbol" }
String Forecast1_WeatherSymbolText "Symbol Text [%s]" <text> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#weatherSymbolText" }
DateTime Forecast1_DateTime "Date/Time [%1$tA, %1$tm/%1$td/%1$tY %1$tl:%1$tM %1$tp]" <time> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#dateTime" }
String Forecast1_Condition "Condition [%s]" <text> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#condition" }
Number:Temperature Forecast1_Temperature "Temperature [%.1f %unit%]" <temperature> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#temperature" }
Number:Pressure Forecast1_Pressure "Pressure [%.1f %unit%]" <pressure> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#pressure" }
Number:Dimensionless Forecast1_RelativeHumidity "Relative Humidity [%.0f %unit%]" <humidity> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#relativeHumidity" }
Number:Temperature Forecast1_Dewpoint "Dewpoint [%.1f %unit%]" <temperature> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#dewpoint" }
Number:Length Forecast1_Visibility "Visibility [%.1f mi]" <none> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#visibility" }
Number:Speed Forecast1_WindSpeed "Wind Speed [%.1f %unit%]" <wind> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#windSpeed" }
Number:Speed Forecast1_WindGust "Wind Gust [%.1f %unit%]" <wind> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#windGust" }
String Forecast1_WindDirection "Wind Direction [%s]" <wind> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#windDirection" }
Number:Angle Forecast1_WindBearing "Wind Bearing [%.0f %unit%]" <wind> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#windBearing" }
Number:Dimensionless Forecast1_ProbabilityOfPrecipitation "Probability of Precipitation [%.0f %unit%]" <none> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#pop" }
Number:Temperature Forecast1_TemperatureHigh "Temperature High [%.1f %unit%]" <temperature> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#tempHigh" }
Number:Temperature Forecast1_TemperatureLow "Temperature Low [%.1f %unit%]" <temperature> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#tempLow" }
Number Forecast1_Sky "Sky [%.0f]" <none> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#sky" }
String Forecast1_SkyText "Sky Text [%s]" <text> (gForecast1) { channel="ecobee:thermostat:account:729318833078:forecast1#skyText" }

// Remote sensor S0
String S0_Id "Sensor Id [%s]" <text> (gThermostatSensor) { channel="ecobee:sensor:account:729318833078:ei-0:id" }
String S0_Name "Sensor Name [%s]" <text> (gThermostatSensor) { channel="ecobee:sensor:account:729318833078:ei-0:name" }
String S0_Type "Sensor Type [%s]" <text> (gThermostatSensor) { channel="ecobee:sensor:account:729318833078:ei-0:type" }
String S0_Code "Sensor Code [%s]" <text> (gThermostatSensor) { channel="ecobee:sensor:account:729318833078:ei-0:code" }
Switch S0_InUse "Sensor In Use [%s]" <switch> (gThermostatSensor) { channel="ecobee:sensor:account:729318833078:ei-0:inUse" }
Number:Temperature S0_Temperature "Temperature [%.1f %unit%]" <none> (gThermostatSensor) { channel="ecobee:sensor:account:729318833078:ei-0:temperature" }
Number:Dimensionless S0_Humidity "Humidity [%.0f %unit%]" <none> (gThermostatSensor) { channel="ecobee:sensor:account:729318833078:ei-0:humidity" }
Switch S0_Occupancy "Occupancy [%s]" <none> (gThermostatSensor) { channel="ecobee:sensor:account:729318833078:ei-0:occupancy" }

// Remote sensor S1
String S1_Id "Sensor Id [%s]" <text> (gRoom1Sensor) { channel="ecobee:sensor:account:729318833078:rs-101:id" }
String S1_Name "Sensor Name [%s]" <text> (gRoom1Sensor) { channel="ecobee:sensor:account:729318833078:rs-101:name" }
String S1_Type "Sensor Type [%s]" <text> (gRoom1Sensor) { channel="ecobee:sensor:account:729318833078:rs-101:type" }
String S1_Code "Sensor Code [%s]" <text> (gRoom1Sensor) { channel="ecobee:sensor:account:729318833078:rs-101:code" }
Switch S1_InUse "Sensor In Use [%s]" <switch> (gRoom1Sensor) { channel="ecobee:sensor:account:729318833078:rs-101:inUse" }
Number:Temperature S1_Temperature "Temperature [%.1f %unit%]" <none> (gRoom1Sensor) { channel="ecobee:sensor:account:729318833078:rs-101:temperature" }
Switch S1_Occupancy "Occupancy [%s]" <none> (gRoom1Sensor) { channel="ecobee:sensor:account:729318833078:rs-101:occupancy" }

// Remote sensor S2
String S2_Id "Sensor Id [%s]" <text> (gRoom2Sensor) { channel="ecobee:sensor:account:729318833078:rs-100:id" }
String S2_Name "Sensor Name [%s]" <text> (gRoom2Sensor) { channel="ecobee:sensor:account:729318833078:rs-100:name" }
String S2_Type "Sensor Type [%s]" <text> (gRoom2Sensor) { channel="ecobee:sensor:account:729318833078:rs-100:type" }
String S2_Code "Sensor Code [%s]" <text> (gRoom2Sensor) { channel="ecobee:sensor:account:729318833078:rs-100:code" }
Switch S2_InUse "Sensor In Use [%s]" <switch> (gRoom2Sensor) { channel="ecobee:sensor:account:729318833078:rs-100:inUse" }
Number:Temperature S2_Temperature "Temperature [%.1f %unit%]" <none> (gRoom2Sensor) { channel="ecobee:sensor:account:729318833078:rs-100:temperature" }
Switch S2_Occupancy "Occupancy [%s]" <none> (gRoom2Sensor) { channel="ecobee:sensor:account:729318833078:rs-100:occupancy" }
```

### Sitemap

```perl
Frame label="Ecobee Thermostat" {
    Group item=gInfo
    Group item=gEquipmentStatus
    Group item=gRuntime
    Group item=gSettings
    Group item=gProgram
    Group item=gAlert
    Group item=gEvent
    Group item=gWeather
    Group item=gLocation
    Group item=gHouseDetails
    Group item=gVersion
    Group item=gManagement
    Group item=gTechnician
    Group item=gThermostatSensor
    Group item=gRoom1Sensor
    Group item=gRoom2Sensor
}
```

### Rules

Some of the example rules below depend on the following _proxy_ items.

```java
Switch SetTemperatureHold "Set Temperature Hold [%s]"
Number:Temperature UserCool "User Selected Heat [%.1f %unit%]"
Number:Temperature UserHeat "User Selected Cool [%.1f %unit%]"
String UserClimateRef "User Climate Ref [%s]"
String SendMessage "Send a Message [%s]"
String AcknowledgeAlert "Acknowledge An Alert [%s]"
Switch GetAlerts "Get All Alerts [%s]"
Switch GetEvents "Get All Events [%s]"
Switch GetClimates "Get All Climates [%s]"
```

```java
rule "Set Temperature Hold"
when
    Item SetTemperatureHold received command
then
    if (UserCool.state instanceof QuantityType &&  UserHeat.state instanceof QuantityType) {
        switch Settings_HvacMode.state.toString {
            case "cool",
            case "heat",
            case "auto" :  {
                val ecobeeActions = getActions("ecobee","ecobee:thermostat:account:103778713388")
                ecobeeActions.setHold(UserCool.state as QuantityType, UserHeat.state as QuantityType)
            }
            case "off"  :  {
                logWarn("ecobee", "HVAC mode is off, temperature hold ignored")
                return
            }
            case "default" : {
                logWarn("ecobee", "HVAC mode '{}' is unknown, temperature hold ignored", Settings_HvacMode.state)
            }
        }
    }
end


rule "Set Climate Ref Hold"
when
    Item UserClimateRef received command
then
    val ecobeeActions = getActions("ecobee","ecobee:thermostat:account:103778713388")
    if (receivedCommand.toString.equals("resume")) {
        ecobeeActions.resumeProgram(true)
    } else {
        ecobeeActions.setHold(receivedCommand.toString)
    }
end


rule "Send Message"
when
    Item SendMessage received command
then
    val ecobeeActions = getActions("ecobee","ecobee:thermostat:account:103778713388")
    var Boolean isSuccess
    isSuccess = ecobeeActions.sendMessage(receivedCommand.toString)
    logInfo("ecobee", "Action 'sendMessage' returned '{}'", isSuccess)
end


rule "Acknowledge Alert"
when
    Item AcknowledgeAlert received command
then
    val ecobeeActions = getActions("ecobee","ecobee:thermostat:account:103778713388")
    var Boolean isSuccess
    isSuccess = ecobeeActions.acknowledge(receivedCommand.toString, "accept", null)
    logInfo("ecobee", "Action 'acknowledge' returned '{}'", isSuccess)
end


rule "Get Alerts"
when
    Item GetAlerts received command
then
    val ecobeeActions = getActions("ecobee","ecobee:thermostat:account:103778713388")
    var String alerts = ecobeeActions.getAlerts()
    if (alerts !== null) {
        val int numElements = Integer.parseInt(transform("JSONPATH", "$.length()", alerts))
        logInfo("ecobee", "Alerts: There are {} alerts in array", numElements)
        var String ref
        var String text
        for (var int i=0; i < numElements; i++) {
            ref = transform("JSONPATH", "$.[" + i + "].acknowledgeRef", alerts)
            text = transform("JSONPATH", "$.[" + i + "].text", alerts)
            logInfo("test", "Alerts: Alert '{}' with acknowledgeRef '{}'", text, ref)
        }
    } else {
        logInfo("ecobee", "Alerts: No alerts!!!")
    }
end


rule "Get Events"
when
    Item GetEvents received command
then
    val ecobeeActions = getActions("ecobee","ecobee:thermostat:account:103778713388")
    var String events = ecobeeActions.getEvents()
    if (events !== null) {
        val int numElements = Integer.parseInt(transform("JSONPATH", "$.length()", events))
        logInfo("ecobee", "Events: There are {} events in array", numElements)
    } else {
        logInfo("ecobee", "Events: No events!!!")
    }
end


rule "Get Climates"
when
    Item GetClimates received command
then
    val ecobeeActions = getActions("ecobee","ecobee:thermostat:account:103778713388")
    var String climates = ecobeeActions.getClimates()
    if (climates !== null) {
        val int numElements = Integer.parseInt(transform("JSONPATH", "$.length()", climates))
        logInfo("ecobee", "Climates: There are {} climates in array", numElements)
        var String climateRef
        for (var int i=0; i < numElements; i++) {
            climateRef = transform("JSONPATH", "$.[" + i + "].climateRef", climates)
            logInfo("test", "Climates: Climate Ref: {}", climateRef)
        }
    } else {
        logInfo("ecobee", "Climates: No climates!!!")
    }
end

```

## Acnowledgements

Thanks to John Cocula for his openHAB version 1 implementation of the Ecobee binding.
It was a valuable starting point for my work on the openHAB version 2 binding.
