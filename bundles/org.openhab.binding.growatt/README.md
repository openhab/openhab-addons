# Growatt Binding

This binding supports the integration of Growatt solar inverters.

It depends on the independent [Grott](https://github.com/johanmeijer/grott#the-growatt-inverter-monitor) proxy server application.
This intercepts the logging data that the Growatt inverter data logger normally sends directly to the Growatt cloud server.
It sends the original (encoded) data onwards to the cloud server (so the cloud server will not notice anything different).
But it also sends a (decoded) copy to openHAB as well.

## Supported Things

The binding supports two types of things:

- `bridge`: The bridge is the interface to the Grott application; it receives the data from all inverters.
- `inverter`: The inverter thing contains channels which are updated with solor production and consumption data.

## Discovery

There is no automatic discovery of the bridge.
However if a bridge exists and it receives inverter data, then a matching inverter thing is created in the Inbox.

## Thing Configuration

The `bridge` thing allows configuration of the user credentials, which are only required if you want to send inverter commands via the Growatt cloud server:

| Name      | Type    | Description                                                                              | Advanced |Required |
|-----------|---------|------------------------------------------------------------------------------------------|----------|---------|
| userName  | text    | User name for the Growatt Shine app. Only needed if using [Rule Actions](#rule-actions)  | yes      | no      |
| password  | text    | Password for the Growatt Shine app. Only needed if using [Rule Actions](#rule-actions)   | yes      | no      |

The `inverter` thing requires configuration of its serial number resp. `deviceId`:

| Name      | Type    | Description                                                                              | Required |
|-----------|---------|------------------------------------------------------------------------------------------|----------|
| deviceId  | text    | Device serial number or id as configured in the Growatt cloud and the Grott application. | yes      |

## Channels

The `bridge` thing has no channels.

The `inverter` thing supports many possible channels relating to solar generation and consumption.
All channels are read-only.
Depending on the inverter model, and its configuration, not all of the channels will be present.
The list of all possible channels is as follows:

| Channel                       | Type                      | Description                                          | Advanced |
|-------------------------------|---------------------------|------------------------------------------------------|----------|
| system-status                 | Number:Dimensionless      | Inverter status code.                                |          |
| pv1-voltage                   | Number:ElectricPotential  | DC voltage from solar panel string #1.               | yes      |
| pv2-voltage                   | Number:ElectricPotential  | DC voltage from solar panel string #2.               | yes      |
| pv1-current                   | Number:ElectricCurrent    | DC current from solar panel string #1.               | yes      |
| pv2-current                   | Number:ElectricCurrent    | DC current from solar panel string #2.               | yes      |
| pv-power                      | Number:Power              | Total DC solar input power.                          |          |
| pv1-power                     | Number:Power              | DC power from solar panel string #1.                 | yes      |
| pv2-power                     | Number:Power              | DC power from solar panel string #2.                 | yes      |
| grid-frequency                | Number:Frequency          | Frequency of the grid.                               | yes      |
| grid-voltage-r                | Number:ElectricPotential  | Voltage of the grid (phase #R).                      |          |
| grid-voltage-s                | Number:ElectricPotential  | Voltage of the grid phase #S.                        | yes      |
| grid-voltage-t                | Number:ElectricPotential  | Voltage of the grid phase #T.                        | yes      |
| grid-voltage-rs               | Number:ElectricPotential  | Voltage of the grid phases #RS.                      | yes      |
| grid-voltage-st               | Number:ElectricPotential  | Voltage of the grid phases #ST.                      | yes      |
| grid-voltage-tr               | Number:ElectricPotential  | Voltage of the grid phases #TR.                      | yes      |
| inverter-current-r            | Number:ElectricCurrent    | AC current from inverter (phase #R).                 | yes      |
| inverter-current-s            | Number:ElectricCurrent    | AC current from inverter phase #S.                   | yes      |
| inverter-current-t            | Number:ElectricCurrent    | AC current from inverter phase #T.                   | yes      |
| inverter-power                | Number:Power              | Total AC output power from inverter.                 |          |
| inverter-power-r              | Number:Power              | AC power from inverter (phase #R).                   |          |
| inverter-power-s              | Number:Power              | AC power from inverter phase #S.                     | yes      |
| inverter-power-t              | Number:Power              | AC power from inverter phase #T.                     | yes      |
| inverter-va                   | Number:Power              | AC VA from inverter.                                 | yes      |
| export-power                  | Number:Power              | Power exported to grid.                              |          |
| export-power-r                | Number:Power              | Power exported to grid phase #R.                     | yes      |
| export-power-s                | Number:Power              | Power exported to grid phase #S.                     | yes      |
| export-power-t                | Number:Power              | Power exported to grid phase #T.                     | yes      |
| import-power                  | Number:Power              | Power imported from grid.                            |          |
| import-power-r                | Number:Power              | Power imported from grid phase #R.                   | yes      |
| import-power-s                | Number:Power              | Power imported from grid phase #S.                   | yes      |
| import-power-t                | Number:Power              | Power imported from grid phase #T.                   | yes      |
| load-power                    | Number:Power              | Power supplied to load.                              |          |
| load-power-r                  | Number:Power              | Power supplied to load phase #R.                     | yes      |
| load-power-s                  | Number:Power              | Power supplied to load phase #S.                     | yes      |
| load-power-t                  | Number:Power              | Power supplied to load phase #T.                     | yes      |
| charge-power                  | Number:Power              | Battery charge power.                                |          |
| charge-current                | Number:ElectricCurrent    | Battery charge current.                              | yes      |
| discharge-power               | Number:Power              | Battery discharge power.                             |          |
| discharge-va                  | Number:Power              | Battery discharge VA.                                | yes      |
| pv-energy-today               | Number:Energy             | DC energy collected by solar panels today.           |          |
| pv1-energy-today              | Number:Energy             | DC energy collected by solar panels string #1 today. | yes      |
| pv2-energy-today              | Number:Energy             | DC energy collected by solar panels string #2 today. | yes      |
| pv-energy-total               | Number:Energy             | Total DC energy collected by solar panels.           |          |
| pv1-energy-total              | Number:Energy             | Total DC energy collected by solar panels string #1. | yes      |
| pv2-energy-total              | Number:Energy             | Total DC energy collected by solar panels string #2. | yes      |
| inverter-energy-today         | Number:Energy             | AC energy produced by inverter today.                |          |
| inverter-energy-total         | Number:Energy             | Total AC energy produced by inverter.                |          |
| export-energy-today           | Number:Energy             | Energy exported today.                               |          |
| export-energy-total           | Number:Energy             | Total energy exported.                               |          |
| import-energy-today           | Number:Energy             | Energy imported today.                               |          |
| import-energy-total           | Number:Energy             | Total energy imported.                               |          |
| load-energy-today             | Number:Energy             | Energy supplied to load today.                       |          |
| load-energy-total             | Number:Energy             | Total energy supplied to load.                       |          |
| import-charge-energy-today    | Number:Energy             | Energy imported to charge battery today.             |          |
| import-charge-energy-total    | Number:Energy             | Total energy imported to charge battery.             |          |
| inverter-charge-energy-today  | Number:Energy             | Inverter energy to charge battery today.             |          |
| inverter-charge-energy-total  | Number:Energy             | Total inverter energy to charge battery.             |          |
| discharge-energy-today        | Number:Energy             | Energy consumed from battery.                        |          |
| discharge-energy-total        | Number:Energy             | Total energy consumed from battery.                  |          |
| total-work-time               | Number:Time               | Total work time of the system.                       | yes      |
| p-bus-voltage                 | Number:ElectricPotential  | P Bus voltage.                                       | yes      |
| n-bus-voltage                 | Number:ElectricPotential  | N Bus voltage.                                       | yes      |
| sp-bus-voltage                | Number:ElectricPotential  | SP Bus voltage.                                      | yes      |
| pv-temperature                | Number:Temperature        | Temperature of the solar panels (string #1).         | yes      |
| pv-ipm-temperature            | Number:Temperature        | Temperature of the IPM.                              | yes      |
| pv-boost-temperature          | Number:Temperature        | Boost temperature.                                   | yes      |
| temperature-4                 | Number:Temperature        | Temperature #4.                                      | yes      |
| pv2-temperature               | Number:Temperature        | Temperature of the solar panels (string #2).         | yes      |
| battery-type                  | Number:Dimensionless      | Type code of the battery.                            | yes      |
| battery-temperature           | Number:Temperature        | Battery temperature.                                 | yes      |
| battery-voltage               | Number:ElectricPotential  | Battery voltage.                                     | yes      |
| battery-display               | Number:Dimensionless      | Battery display code.                                | yes      |
| battery-soc                   | Number:Dimensionless      | Battery State of Charge percent.                     | yes      |
| system-fault-0                | Number:Dimensionless      | System fault code #0.                                | yes      |
| system-fault-1                | Number:Dimensionless      | System fault code #1.                                | yes      |
| system-fault-2                | Number:Dimensionless      | System fault code #2.                                | yes      |
| system-fault-3                | Number:Dimensionless      | System fault code #3.                                | yes      |
| system-fault-4                | Number:Dimensionless      | System fault code #4.                                | yes      |
| system-fault-5                | Number:Dimensionless      | System fault code #5.                                | yes      |
| system-fault-6                | Number:Dimensionless      | System fault code #6.                                | yes      |
| system-fault-7                | Number:Dimensionless      | System fault code #7.                                | yes      |
| system-work-mode              | Number:Dimensionless      | System work mode code.                               | yes      |
| sp-display-status             | Number:Dimensionless      | Solar panel display status code.                     | yes      |
| constant-power-ok             | Number:Dimensionless      | Constant power OK code.                              | yes      |
| load-percent                  | Number:Dimensionless      | Percent of full load.                                | yes      |
| rac                           | Number:Power              | Reactive 'power' (var).                              | yes      |
| erac-today                    | Number:Energy             | Reactive 'energy' today (kvarh).                     | yes      |
| erac-total                    | Number:Energy             | Total reactive 'energy' (kvarh).                     | yes      |

## Rule Actions

This binding includes rule actions, which allow you to setup programs for battery charging and discharging.
Each inverter thing has a separate actions instance, which can be retrieved as follows.

```php
val growattActions = getActions("growatt", "growatt:inverter:home:sph")
```

Where the first parameter must always be `growatt` and the second must be the full inverter thing UID.
Once the action instance has been retrieved, you can invoke the following method:

```php
growattActions.setupBatteryProgram(int programMode, @Nullable Integer powerLevel, @Nullable Integer stopSOC, @Nullable Boolean enableAcCharging, @Nullable String startTime, @Nullable String stopTime, @Nullable Boolean enableProgram) 
```

The meaning of the method parameters is as follows:

| Parameter                     | Description                                                                                                                                    |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| programMode                   | The program mode to set i.e. 'Load First' (0), 'Battery First' (1), 'Grid First' (2).                                                          |
| powerLevel<sup>2)</sup>       | The percentage rate of battery (dis-)charge e.g. 100 - in 'Battery First' mode => charge power, otherwise => discharge power.                  |
| stopSOC<sup>2)</sup>          | The battery SOC (state of charge) percentage when the program shall stop e.g. 20 - in 'Battery First' mode => max. SOC, otherwise => min. SOC. |
| enableAcCharging<sup>2)</sup> | Allow the battery to be charged from the AC mains supply e.g. true, false.                                                                     |
| startTime<sup>1,2)</sup>      | String representation of the local time when the program `time segment` shall start e.g. "00:15"                                               |
| stopTime<sup>1,2)</sup>       | String representation of the local time when the program `time segment` shall stop e.g. "06:45"                                                |
| enableProgram<sup>1,2)</sup>  | Enable / disable the program `time segment` e.g. true, false                                                                                   |

Notes:

-<sup>1)</sup> ***WARNING*** inverters have different program `time segment`'s for each `programMode`.
To prevent unexpected results do not overlap the `time segment`'s.

-<sup>2)</sup> Depending on inverter type and `programMode` certain parameters may accept 'null' values.
The 'mix', 'sph' and 'spa' types set the battery program in a single command, so all parameters - except `enableAcCharging` - <u>**must**</u> be ***non-*** 'null'.
By contrast 'tlx' types set the battery program in up to four partial commands, and you may pass 'null' parameters in order to omit a partial command.
The permission for passing 'null' parameters, and the effect of such 'null' parameters, is shown in detail in the table below:

| Parameter                          | Permission for.. / effect of.. passing a 'null' parameter                                                                                     |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| programMode                        | Shall <u>**not**</u> be 'null' under any circumstance!                                                                                        |
| powerLevel                         | May be 'null' on 'tlx' inverters whereby the prior `programMode` / `powerLevel` continues to apply.                                           |
| stopSOC                            | May be 'null' on 'tlx' inverters whereby the prior `programMode` / `stopSOC` continues to apply.                                              |
| enableAcCharging                   | If 'null' the prior `enableAcCharging` (if any) continues to apply. Shall <u>**not**</u> be 'null' on 'mix' inverter 'Battery First' program. |
| startTime, stopTime, enableProgram | May be 'null' on 'tlx' inverters whereby the prior `programMode` / `time segment` continues to apply - note all 'null' resp. non-'null'.      |

### Example program to charge battery during night-time low tariff time window

The following is an example program to charge the battery during a night-time low tariff period, and depending on the forecast solar energy for the coming day.

```php
// solar power constants
val Integer programMode = 1 // 0 = Load First, 1 = Battery First, 2 = Grid First
val Integer powerLevel = 23 // percent
val batteryFull = 6500.0 // Wh
val batteryMin = 500.0 // Wh
val daylightConsumption = 10000.0 // Wh
val offPeakConsumption = 2000.0 // Wh
val maximumSOC = 100.0 // percent
val minimumSOC = 10.0 // percent
val maxChargingPower = 4000.0 // W
val offPeakEndMinute = 420 // 07:00
val offPeakStartMinute = 20 // 00:20

..

rule "Setup Solar Battery Charging Program"
when
    Time cron "0 10 0 ? * * *"
then
    val growattActions = getActions("growatt", "growatt:inverter:home:ABCD1234") // thing UID
    if (growattActions === null) {
        logWarn("Rules", "growattActions is null")
        return
    }

    // variable algorithm parameters
    var Integer startMinute = offPeakStartMinute
    var Boolean enableProgram = true
    var Boolean enableAcCharging = true

    // calculate required stop SOC based on weather forecast
    val Double solarForecast = (Solar_Forecast_Energy_Today_Full.state as QuantityType<Energy>).toUnit("Wh").doubleValue()
    var Double targetSOC = (100.0 * (batteryMin + daylightConsumption - solarForecast)) / batteryFull
    if (targetSOC > maximumSOC) {
        targetSOC = maximumSOC
    }

    // calculate notional SOC at end of off peak period
    var Double morningSOC = ((Battery_SOC_Level.state as QuantityType<Dimensionless>).toUnit("one").doubleValue() - (offPeakConsumption / batteryFull)) * 100.0
    if (morningSOC < minimumSOC) {
        morningSOC = minimumSOC
    }

    // calculate charging start time (if any)
    if (targetSOC > morningSOC) {
        startMinute = (offPeakEndMinute - (60.0 * (targetSOC - morningSOC) * batteryFull / (powerLevel * maxChargingPower))).intValue()
        if (startMinute < offPeakStartMinute) {
            startMinute = offPeakStartMinute
        }
    } else {
        enableProgram = false
        enableAcCharging = false
        targetSOC = minimumSOC
    }

    // convert times to strings
    val String startTime = String.format("%02d:%02d", startMinute / 60, startMinute % 60);
    val String stopTime = String.format("%02d:%02d", offPeakEndMinute / 60, offPeakEndMinute % 60);

    // convert to integer percent
    val Integer stopSOC = targetSOC.intValue()

    logInfo("Rules", "Setup Charging Program(morningSOC=" + morningSOC + "%, solarForecast=" + solarForecast + "Wh, programMode=" + programMode + ", powerLevel=" + powerLevel +
        "%, stopSOC=" + stopSOC + "%, enableCharging=" + enableAcCharging + ", startTime=" + startTime + ", stopTime=" + stopTime + ", enableProgram=" + enableProgram +")")
    growattActions.setupBatteryProgram(programMode, powerLevel, stopSOC, enableAcCharging, startTime, stopTime, enableProgram)
end
```

### Example program to charge battery prior to an extra high tariff window in the day

The following is an example program to charge the battery in preparation to avoid importing energy during a coming extra high tariff time window.

```php
// solar power constants
var pauseProgramLastSetupDate

..

rule "Setup Solar Power Pause Program"
when
    Time cron "59 0 8-22 ? * * *" or
    Item Power_Pause_Program_Start changed
then
    val programSetupDate = now.toLocalDate()
    if (programSetupDate.equals(pauseProgramLastSetupDate)) {
        logInfo("Rules", "Power Pause program already setup for " + programSetupDate)
        return
    }

    val pauseStartState = Power_Pause_Program_Start.state
    if (pauseStartState == NULL || pauseStartState == UNDEF) {
        logWarn("Rules", "Power_Pause_Program_Start state is null or undefined")
        return
    }

    var pauseStartDateTime = (pauseStartState as DateTimeType).getZonedDateTime()
    if (pauseStartDateTime.getHour() < 8) {
        logWarn("Rules", "Power Pause program shall not start before 08:00h => " + pauseStartDateTime)
        return 
    }

    val programDuration = Duration.between(now, pauseStartDateTime)
    if (programDuration.isNegative() || programDuration.toDays() > 0) {
        logInfo("Rules", "Power Pause program date is not today => " + pauseStartDateTime)
        return
    }
    if (programDuration.toHours() < 1) {
        logWarn("Rules", "Power Pause program lead time is too short => " + pauseStartDateTime)
        return
    }

    // setup program to start late and end early in case inverter clock not in synch
    val delta = 600
    pauseStartDateTime = pauseStartDateTime.minusSeconds(delta)
    val chargeStartDateTime = pauseStartDateTime.minusSeconds(programDuration.toSeconds()).plusSeconds(2 * delta)
    if (chargeStartDateTime.isBefore(now)) {
        logWarn("Rules", "Power Pause program start time is in the past")
        return
    }

    val formatter = DateTimeFormatter.ofPattern("HH:mm");
    val String stopTime = pauseStartDateTime.format(formatter)
    val String startTime = chargeStartDateTime.format(formatter)

    val socState = Battery_SOC_Level.state
    if (socState == NULL || socState == UNDEF) {
        logWarn("Rules", "Battery_SOC_Level is null or undefined")
        return
    }
    val currentSOC = (socState as Number)

    var targetPowerLevel = ((maximumSOC - currentSOC) * batteryFull) / (maxChargingPower * (programDurationSeconds / 3600.0))
    if (targetPowerLevel < 23.0) {
        targetPowerLevel = 23.0
    } else if (targetPowerLevel > 100.0) {
        targetPowerLevel = 100.0
    }
    val Integer powerLevel = targetPowerLevel.intValue()

    val Integer programMode = 1 // 0 = Load First, 1 = Battery First, 2 = Grid First
    val Boolean enableAcCharging = true
    val Boolean enableProgram = true
    val Integer stopSOC = maximumSOC.intValue()

    val growattActions = getActions("growatt", "growatt:inverter:home:ABCD1234") // thing UID
    if (growattActions === null) {
        logWarn("Rules", "growattActions is null")
        return
    }

    pauseProgramLastSetupDate = programSetupDate
    logInfo("Rules", "Setup Solar Power Pause Program(programMode=" + programMode + ", powerLevel=" + powerLevel + "%, stopSOC=" + stopSOC + "%, enableCharging=" +
        enableAcCharging + ", startTime=" + startTime + ", stopTime=" + stopTime + ", enableProgram=" + enableProgram +")")
    growattActions.setupBatteryProgram(programMode, powerLevel, stopSOC, enableAcCharging, startTime, stopTime, enableProgram)
end
```

## Full Example

### Example `.things` file

```java
Bridge growatt:bridge:home "Growattt Bridge" [userName="USERNAME", password="PASSWORD"] {
    Thing inverter sph "Growatt SPH Inverter" [deviceId="INVERTERTID"]
}
```

### Example `.items` file

```java
Number:ElectricPotential Solar_String1_Voltage "Solar String #1 PV Voltage" {channel="growatt:inverter:home:sph:pv1-voltage"}
Number:ElectricCurrent Solar_String1_Current "Solar String #1 PV Current" {channel="growatt:inverter:home:sph:pv1-current"}
Number:Power Solar_String1_Power "Solar String #1 PV Power" {channel="growatt:inverter:home:sph:pv1-power"}
Number:Energy Solar_Output_Energy "Solar Output Energy Total" {channel="growatt:inverter:home:sph:pv-energy-total"}
```

Example using a transform profile to invert an item value:

```java
// charge item with positive value
Number:Power Charge_Power "Charge Power [%.0f W]" <energy> {channel="growatt:inverter:home:sph:charge-power"}

// discarge item with negative value
Number:Power Discharge_Power "Discharge Power [%.0f W]" <energy> {channel="growatt:inverter:home:sph:discharge-power" [ profile="transform:JS", toItemScript="| Quantity(input).multiply(-1).toString();" ] }
```

## Grott Application Installation and Setup

You can install the Grott application either on the same computer as openHAB or on another.
The following assumes you will be running it on the same computer.
The Grott application acts as a proxy server between your Growatt inverter and the Growatt cloud server.
It intercepts data packets between the inverter and the cloud server, and it sends a copy of the intercepted data also to openHAB.

**NOTE**: make sure that the Grott application is **FULLY OPERATIONAL** for your inverter **BEFORE** you create any things in openHAB!
Otherwise the binding might create a wrong (or even empty) list of channels for the inverter thing.
(Yet if you do make that mistake you can rectify it by deleting and recreating the thing).

You should configure the Grott application via its `grott.ini` file.
Configure Grott to match your inverter according to the [instructions](https://github.com/johanmeijer/grott#the-growatt-inverter-monitor).

### Install Python

If Python is not already installed on you computer, then install it first.
And install the following additional necessary python packages:

```bash
sudo pip3 install paho-mqtt
sudo pip3 install requests
```

### Install Grott

First install the Grott application and the Grott application extension files in a Grott specific home folder.
Note that Grott requires the `grottext.py` application extension in addition to the standard application files.
The installation is as follows:

- Create a 'home' sub-folder for Grott e.g. `/home/<username>/grott/`.
- Copy `grott.py`, `grottconf.py`, `grottdata.py`, `grottproxy.py`, `grottsniffer.py`, `grottserver.py` to the home folder.
- Copy `grottext.py` application extension to the home folder.
- Copy `grott.ini` configuration file to the home folder.
- Modify `grott.ini` to run in proxy mode; not in compatibility mode; show your inverter type; not run MQTT; not run PVOutput; enable the `grottext` extension; and set the openHAB `/growatt` servlet url.

A suggested Grott configuration for openHAB is as follows:

```php
[Generic]
mode = proxy
compat = False
invtype = sph // your inverter type

[MQTT]
nomqtt = True // disable mqtt

[PVOutput]
pvoutput = False // disable pvoutput

[extension] // enable the 'grottext' extension
extension = True
extname = grottext
extvar = {"url": "http://127.0.0.1:8080/growatt"} // or ip address of openHAB (if remote)
```

### Start Grott as a Service

Finally you should set your computer to starts the Grott application automatically as a service when your computer starts.
For Windows see wiki: https://github.com/johanmeijer/grott/wiki/Grott-as-a-service-(Windows)
For Linux see wiki: https://github.com/johanmeijer/grott/wiki/Grott-as-a-service-(Linux)
The service configuration for Linux is summarised below:

- Copy the `grott.service` file to the `/etc/systemd/system/` folder
- Modify `grott.service` to enter your user name; the Grott settings; the path to Python; and the path to the Grott application:

```php
[Service]
SyslogIdentifier=grott
User=<username>  // your username
WorkingDirectory=/home/<username>/grott/ // your home grott folder
ExecStart=-/usr/bin/python3 -u /home/<username>/grott/grott.py -v // ditto
```

And finally enable the Grott service:

```bash
sudo systemctl enable grott
```

### Route Growatt Inverter Logging via Grott Proxy

Normally the Growatt inverter sends its logging data directly to port `5279` on the Growatt server at `server.growatt.com` (ip=47.91.67.66) on the cloud.
Grott is a proxy server that interposes itself beween the inverter and the cloud server.
i.e. it receives the inverter logging data and forwards it unchanged to the cloud server.

**WARNING**: make sure that Grott is running on a computer with a **STATIC IP ADDRESS** (and note this safely)!
Otherwise if the computer changes its ip address dynamically, it can no longer intercept the inverter data.
This means **YOU WILL NO LONGER BE ABLE TO RESET THE INVERTER** to its original settings!

You need to use the Growatt App to tell the inverter to send its logging data to the Grott proxy instead of to the cloud.
See wiki: https://github.com/johanmeijer/grott/wiki/Rerouting-Growatt-Wifi-TCPIP-data-via-your-Grott-Server for more information.
