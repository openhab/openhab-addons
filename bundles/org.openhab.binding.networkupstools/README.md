# Network UPS Tools Binding

The primary goal of the [Network UPS Tools](https://networkupstools.org/) (NUT) project is to provide support for power devices, such as uninterruptible power supplies (UPS), Power Distribution Units and Solar Controllers.

Network UPS Tools (NUT) provides many control and monitoring features, with a uniform control and management interface.
More than 100 different manufacturers, and several thousands of models are compatible.

This binding lets you integrate NUT servers with openHAB.

## Supported Things

The binding can connect to multiple NUT instances.
The thing is an `ups` thing.
The thing supports a number of NUT features out-of-the-box and supports the option to configure additional channels to get other NUT variables.
The thing queries the NUT server for the status of the UPS approximate every 3 seconds and updates the status when a change happens.
When a change of the UPS status happens it will query the NUT server to update all linked channels.
Outside the status change updates, all linked channels are updated at the user configured refresh time.

Some NUT variables are static in nature and are not suited for a channel.
Some of these could change, like of firmware version.
Therefore these properties are updated with a 1 hour frequency.
The following NUT variables are read and added to the thing as properties:

| Property         | Description               |
| ---------------- | ------------------------- |
| ups.firmware     | UPS firmware              |
| ups.firmware.aux | Auxiliary device firmware |
| ups.id           | UPS system identifier     |
| ups.mfr          | UPS manufacturer          |
| ups.mfr.date     | UPS manufacturing date    |
| ups.model        | UPS model                 |
| ups.serial       | UPS serial number         |

## Discovery

Discovery is not supported.

## Thing Configuration

The thing configuration requires the name of the UPS device as configured on the NUT server.
If the NUT service isn't running locally the ip address or domain name (FDQN) of the server running NUT must be configured.
Optional, port, username and password might need to be configured if required.

| Parameter | Default   | Mandatory | Description                                     |
| --------- | --------- | --------- | ----------------------------------------------- |
| device    |           | Yes       | UPS device name, `ups` for example              |
| host      | localhost | Yes       | UPS server hostname                             |
| port      | 3493      | No        | UPS server port, 3493 for example               |
| username  |           | No        | UPS server username                             |
| password  |           | No        | UPS server password                             |
| refresh   | 60        | No        | Refresh interval for channel updates in seconds |

## Channels

The following channels are available:

| Channel Name        | Item Type                | Unit | Description                                                         | Advanced |
| ------------------- | ------------------------ | ---- | ------------------------------------------------------------------- | -------- |
| upsAlarm            | String                   |      | UPS alarms                                                          | no       |
| upsLoad             | Number:Dimensionless     | %    | Load on UPS (percent)                                               | yes      |
| upsPower            | Number:Power             | VA   | Current value of apparent power (Volt-Amps)                         | yes      |
| upsRealpower        | Number:Power             | W    | Current value of real power (Watts)                                 | no       |
| upsStatus           | String                   |      | Status of the UPS: OFF, OL,OB,LB,RB,OVER,TRIM,BOOST,CAL,BYPASS,NULL | no       |
| upsTemperature      | Number:Temperature       | °C   | UPS temperature (degrees C)                                         | yes      |
| upsTestResult       | String                   |      | Results of last self test (opaque string)                           | yes      |
| inputCurrent        | Number:ElectricCurrent   | A    | Input current (A)                                                   | yes      |
| inputCurrentStatus  | String                   |      | Status relative to the thresholds                                   | yes      |
| inputLoad           | Number:Dimensionless     | %    | Load on (ePDU) input (percent of full)                              | no       |
| inputRealpower      | Number:Power             | W    | Current sum value of all (ePDU) phases real power (W)               | yes      |
| inputQuality        | String                   |      | Input power quality (*** opaque)                                    | yes      |
| inputTransferReason | String                   |      | Reason for last transfer to battery (*** opaque)                    | yes      |
| inputVoltage        | Number:ElectricPotential | V    | Input voltage (V)                                                   | yes      |
| inputVoltageStatus  | String                   |      | Status relative to the thresholds                                   | yes      |
| outputCurrent       | Number:ElectricCurrent   | A    | Output current (A)                                                  | yes      |
| outputVoltage       | Number:ElectricPotential | V    | Output voltage (V)                                                  | yes      |
| batteryCharge       | Number:Dimensionless     | %    | Battery charge (percent)                                            | no       |
| batteryRuntime      | Number:Time              | s    | Battery runtime (seconds)                                           | no       |
| batteryVoltage      | Number:ElectricPotential | V    | Battery voltage (V)                                                 | yes      |

### Dynamic Channels

Because there is a lot of variation in UPS features, the binding supports dynamically adding channels for features, not supported out-of-the-box.
To get data from another NUT variable the channel needs to configured.
Channels can be created with as type: `Number`, `Number:<Quantity>`, `String` or `Switch`.

The following channel properties are needed:

| Property        | Description                    | Example                                       |
| --------------- | ------------------------------ | --------------------------------------------- |
| networkupstools | Links to NUT variable          | `networkupstools="input.voltage.low.warning"` |
| unit            | The unit of Quantity Type data | `unit="V"`                                    |

## Full Example

ups.things:

```java
Thing networkupstools:ups:ups1 [ device="ups", host="localhost", refresh=60 ]
```

ups-with-channels.things:

```java
Thing networkupstools:ups:ups2 [ device="ups", host="localhost", refresh=60 ] {
    Channels:
        String : testResult "Test Result" [networkupstools="ups.test.result"]
        Number:Frequency : upsOutFreq "Output Frequency" [networkupstools="output.frequency", unit="Hz"]
        Number:ElectricPotential : upsLowVoltage  "Low Voltage" [networkupstools="input.voltage.low.warning", unit="V"]
        Number:ElectricCurrent : upsLowCurrent "Low Current" [networkupstools="input.current.low.warning", unit="A"]
}
```

ups.items

```java
Number:Dimensionless ups_battery_charge "Battery Charge [%d %%]"  {channel="networkupstools:ups:ups1:batteryCharge"}
Number:ElectricCurrent ups_current "Input Current [%d mA]"{channel="networkupstools:ups:ups1:inputCurrent"}

String test_result "Test Result" {channel="networkupstools:ups:ups2:testResult"}
Number:Frequency ups_out_freq "Output Frequency" {channel="networkupstools:ups:ups2:upsOutFreq"}
Number:ElectricPotential ups_low_voltage  "Low Voltage [%.1f V]" {channel="networkupstools:ups:ups2:upsLowVoltage"}
Number:ElectricCurrent ups_low_current "Input Current [%d A]" {channel="networkupstools:ups:ups2:upsLowCurrent"}
```
