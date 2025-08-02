# Pentair Binding

This is an openHAB binding for a Pentair Pool System.
It is based on combined efforts of many on the Internet in reverse-engineering the proprietary Pentair protocol (see References section).
The binding was developed and tested on a system with a Pentair EasyTouch controller, but will also operate with the Pentair IntelliTouch/SunTouch controllers.
Note that with the Pentair IntelliCenter controllers, the functionality will be limited since that utilizes a different protocol which has not yet been implemented.
Further, if there is interest to improve functionality or address issues with any of the Pentair controllers, please reach out to the binding author.

## Hardware Setup

> REQUISITE DISCLAIMER: CONNECTING 3RD PARTY DEVICES TO THE PENTAIR SYSTEM BUS COULD CAUSE SERIOUS DAMAGE TO THE SYSTEM SHOULD SOMETHING MALFUNCTION.  IT IS NOT ENDORSED BY PENTAIR AND COULD VOID WARRENTY. IF YOU DECIDE TO USE THIS BINDING TO INTERFACE TO A PENTAIR CONTROLLER, THE AUTHOR(S) CAN NOT BE HELD RESPONSIBLE.

This binding requires an adapter to interface to the Pentair system bus.
This bus/wire runs between the Pentair control system, indoor control panels, IntelliFlo pumps, etc.
It is a standard RS-485 bus running at 9600,8N1 so any RS-485 adapter should work and you should be able to buy one for under $30.
Pentair does not publish any information on the protocol so this binding was developed using the great reverse-engineering efforts of others made available on the internet.
I have cited several of those in the References section below.

### Connecting adapter to your system

A USB or serial RS-485 interface or IP based interface can be used to interface to the Pentair system bus.
The binding includes 2 different Bridges depending on which type of interface you use, serial_bridge or ip_bridge.

If your openHAB system is physically located far from your Pentair equipment or indoor control panel, you can use a Raspberry Pi or other computer to redirect USB/serial port traffic over the internet using a program called ser2sock (see Reference section).
An example setup would run the following command: "ser2sock -p 10000 -s /dev/ttyUSB1 -b 9600 -d".

Note: This is the setup utilized for the majority of my testing of this binding.

Once you have the interface connected to your system, it is best to test basic connectivity.
Note the protocol is a binary protocol (not ASCII text based) and in order to view the communication packets, one must use a program capable of a binary/HEX mode.
If connected properly, you will see a periodic traffic with packets staring with FF00FFA5.
This is the preamble for Pentair's communication packet.

After you see this traffic, you can proceed to configuring the Pentair binding in openHAB.

Note: Many adapters use A and B to represent Data+ and Data-. There is no reliable standard for determining which is Data+ and Data-. If you connect the system in reverse, you will still see serial data, however it will be corrupted. Look at your data coming from your device and look for a repeated "FFa5". If you don't see that preamble reliably, you may try switching your data lines."

#### USB/Serial interface

For a USB/Serial interface, you can use most terminal emulators.
For Linux, you can use minicom with the following options: `minicom -H -D /dev/ttyUSB1 -b 9600`

#### IP interface

For an IP based interface (or utilizing ser2sock) on a Linux system, you can use nc command with the following options: `nc localhost 10000 | xxd`

### Pentair Controller panel configuration

In order for the Pentair controller to receive commands from this binding, you may need to enable "Spa-side" remote on the controller itself.

## Supported Things

This binding supports the following thing types:

| ThingType UID   | Thing Type | Description                             |
| --------------- | :--------: | --------------------------------------- |
| ip_bridge       |   Bridge   | A TCP network RS-485 bridge device.     |
| serial_bridge   |   Bridge   | A USB or serial RS-485 device.          |
| controller      |   Thing    | Pentair EasyTouch, SunTouch, or IntelliTouch pool controller.      |
| intelliflo      |   Thing    | Pentair IntelliFlo variable speed pump. |
| intellichlor    |   Thing    | Pentair IntelliChlor chlorinator.       |
| intellichem     |   Thing    | Pentair IntelliChem.                     |

## Binding Configuration

There are no overall binding configurations that need to be set up as all configuration is done at the "Bridge/Thing" level.

## Bridge Configuration

A Bridge item must first be configured to gain access to the Pentair bus.
This can be done via the interactive setup pages in openHAB or manually through a .thing configuration file.
The following table shows the parameters for each Bridge.

| Thing         | Configuration Parameters                                     |
| ------------- | ------------------------------------------------------------ |
| ip_bridge     | address - IP address for the RS-485 adapter - Required.      |
|               | port - TCP port for the RS-485 adapter - Not Required - default = 10000. |
|               | id - ID to use when communicating on Pentair control bus - default = 34. |
| serial_bridge | serialPort - Serial port for the IT-100s bridge - Required.  |
|               | baud - Baud rate of the IT-100 bridge - Not Required - default = 9600. |
|               | pollPeriod - Period of time in minutes between the poll command being sent to the IT-100 bridge - Not Required - default=1. |
|               | id - ID to use when communicating on Pentair control bus - default = 34. |

```java
Bridge pentair:ip_bridge:1 [ address="192.168.1.202", port=10001 ] {
    controller main [ id=16 ]
    intelliflo pump1 [ id=96 ]
    intellichlor ic40
    intellichem chem
}
```

For a serial bridge you would use a configuration similar to this, again saved as 'pentair.things':

```java
Bridge pentair:serial_bridge:1 [ serialPort="/dev/ttyUSB0" ] {
    controller main [ id=16 ]
    intelliflo pump1 [ id=96 ]
    intellichlor ic40
    intellichem chem
}
```

## Things & Channels

### Thing: Controller

Represents and interfaces with a Pentair pool controller in the system.  This binding should work for both Intellitouch and EasyTouch systems.
Feature availability is dependent on the version of hardware and firmware versions of your specific controller.

#### Synchronize Time

This configuration setting will instruct the binding to automatically update the controller's clock every 24 hours with the value from the openHAB server.
This is useful to keep the pool system clock set correct and automatically adjust for daylight savings time.

| Channel Group                     | Channel           | Type               |     | Description  |
| :------------------------------:  | :-------:         | :----:             | :-: | :--------------: |
| pool, spa, aux[1-8], feature[1-8] | switch            | Switch             | RW  | Indicates the particulcar circuit or feature is on or off.  |
| "                                 | name              | String             | R   | Name of circuit |
| "                                 | feature           | String             | R   | Feature of ciruit |
| poolheat, spaheat                 | setpoint          | Number:Temperature | RW  | Temperature setpoint |
| "                                 | temperature       | Number:Temperature | R   | Current water temperature.  Note, the temperature is only valid while in either pool or spa mode. |
| "                                 | heatmode          | String             | R   | Heat mode configured.  Values: NONE, HEATER, SOLARPREFERRED, SOLAR |
| schedule[1-9]                     | schedule          | String             | RW  | Summary string of schedule.  |
| "                                 | type              | String             | RW  | Type of schedule.  Note, to actually write the program to the controller, this channel must be written to with the same value 2 times within 5s. Values: NONE, NORMAL, EGGTIMER, ONCE ONLY |
| "                                 | start             | Number:Time        | RW  | Time of day to start schedule expressed in minutes.  |
| "                                 | end               | Number:Time        | RW  | Time of day to end schedule expressed in minutes. In the case of EGG TIMER, this shoud be the duration. |
| "                                 | circuit           | Number             | RW  | Circuit/Feature the schedule will control. |
| "                                 | days              | String             | RW  | The days the schedule will run.  S=Sunday, M=Monday, T=Tuesday, W=Wednesday, R=Thursday, F=Friday, Y=Saturday |
| status                            | lightmode         | String             | RW  | Light mode. Values: OFF, ON, COLORSYNC, COLORSWIM, COLORSET, PARTY, ROMANCE, CARIBBEAN, AMERICAN, SUNSET, ROYAL, BLUE, GREEN, RED, WHITE, MAGENTA |
| "                                 | solartemperature  | Number:Temperature | R   | Solar temperature sensor reading. |
| "                                 | airtemperature    | Number:Temperature | R   | Air temperature sensor reading. |
| "                                 | servicemode       | Switch             | R   | Indicates whether controller is in service mode. |
| "                                 | solaron           | Switch             | R   | Indicates whether solar heat is on. |
| "                                 | heateron          | Switch             | R   | Indicates whether heater is on. |

#### Working with schedules

This binding allows both reading and writing of schedules and supports up to 9 schedules.
Programming of a schedule can be accomplished either by using the discrete channels linked to items (i.e. type, start, end, circuit, days) or you can concatenate those and use the `schedule` channel saved as a comma delimited string.
To prevent erroneous writes to the schedules though, one must write to the `type` channel the same value twice within 5 sec.

### Thing: IntelliChlor

Represents an Intellichlor module connected in your system.  Currently, the values here are readonly.

| Channel              | Type                       |     | Description |
| :------------------: | :----:                     | :-: | :---------- |
| saltOutput           | Number:Dimensionless       | R   | Current salt output %. |
| salinity             | Number:Dimensionless       | R   | Salinity (ppm). |
| ok                   | Switch                     | R   | System is operating normally. |
| lowFlow              | Switch                     | R   | Water flow rate is low. |
| lowSalt              | Switch                     | R   | Low salt level. |
| veryLowSalt          | Switch                     | R   | Very low salt level. |
| highCurrent          | Switch                     | R   | High current level. |
| cleanCell            | Switch                     | R   | Clean cell. |
| lowVoltage           | Switch                     | R   | Low voltage. |
| lowWaterTemp         | Switch                     | R   | Water temperature is too low for chlorine generation. |
| commError            | Switch                     | R   | Communication error. |

### Thing: IntelliFlo

Represents and interfaces to an Intelliflo pump.
When a controller is active in the system all pump values are read only since the pump can only have one master at a time.
If no controller is present or the controller is in service mode, the pump can be controlled directly from OpenHab.

| Channel              | Type                       |     | Description |
| :------------------: | :----:                     | :-: | :---------- |
| run                  | Switch                     | RW  | Indicates whether the pump is running. |
| rpm                  | Number                     | RW  | Pump RPM |
| gpm                  | Number:VolumetricFlowRate  | R   | Pump GPM (only valid for VF pumps) |
| power                | Number:Power               | R   | Pump power (Watt) |
| status1              | Number                     | R   | Pump status1. (not reversed engineered) |
| status2              | Number                     | R   | Pump status2. (not reversed engineered) |
| runProgram           | Number                     | RW  | Run program (0 to stop, # to run) |

### Thing: IntelliChem

Represents and interfaces to an IntelliChem unit.
This is for monitoring of values only and IntelliChem cannot be directly controlled through this binding.
Note: This has limited testing since I don't own an IntelliChem

| Channel                       | Type                   |     | Description |
| :------------------:          | :----:                 | :-: | :---------- |
| phReading                     | Number                 | R   | Current PH reading. |
| orpReading                    | Number                 | R   | Current Oxidation Reduction Potential (ORP) reading. |
| phSetPoint                    | Number                 | R   | Current PH set point. |
| orpSetPoint                   | Number                 | R   | Oxidation Reduction Potential (ORP) set point. |
| tank1Level                    | Number                 | R   | Tank 1 level (1-7). |
| tank2Level                    | Number                 | R   | Tank 2 level (1-7). |
| calciumHardness               | Number:Dimensionless   | R   | Calcium hardness PPM (mg/L). |
| cyaReading                    | Number                 | R   | Cyanuric acid reading. |
| alkalinity                    | Number                 | R   | Alkalinity reading. |
| phDoserType                   | String                 | R   | The doser type for PH (None, CO2, Acid). |
| orpDOserType                  | String                 | R   | The doser type for ORP (None, ORP). |
| phDoserStatus                 | Switch                 | R   | Whether the chemical is currently dosing. |
| orpDoserStatus                | Switch                 | R   | Whether the chemical is currently dosing. |
| phDoseTime                    | Number:Time            | R   | The time a particular chemical has been dosing. |
| orpdoseTime                   | Number:Time            | R   | The time a particular chemical has been dosing. |
| lsi                           | Number                 | R   | Langelier Saturation Index. |
| saltLevel                     | Number:Dimensionless   | R   | Current salt content reading of the water (PPM). |
| temperature                   | Number:Temperature     | R   | Current temperature. |
| alarmWaterFlow                | Switch                 | R   | Water flow alarm (on = no water flow). |
| alarmPh                       | Switch                 | R   | PH alarm reported. |
| alarmOrp                      | Switch                 | R   | ORP alarm reported. |
| alarmPhTank                   | Switch                 | R   | PH tank alarm reported. |
| alarmOrpTank                  | Switch                 | R   | ORP tank alarm reported. |
| alarmProbeFault               | Switch                 | R   | Probe fault alarm reported. |
| warningPhLockout              | Switch                 | R   | Unit is in PH Lockout. |
| warningPhDailyLimitReached    | Switch                 | R   | Daily limit of PH dosing has been reached. |
| warningOrpDailLimitReached    | Switch                 | R   | Daily limit of ORP dosing has been reached. |
| warningInvalidSetup           | Switch                 | R   | Invalid setup for the unit. |
| warningChlorinatorCommError   | Switch                 | R   | Error in communicating with the Chlorinator. |

## Example setup

### pentair.items

```java
Group gPool             (All)

Number:Temperature  Pool_Temp   "Pool Temperature"              <temperature>   (gPool)     { channel = "pentair:controller:1:main:poolheat#temperature" }
Number:Temperature  Spa_Temp    "Spa Temperature "              <temperature>   (gPool)     { channel = "pentair:controller:1:main:spaheat#temperature" }
Number:Temperature  Air_Temp    "Air Temperature"               <temperature>   (gPool)     { channel = "pentair:controller:1:main:status#airtemperature" }
Number:Temperature  Solar_Temp  "Solar Temperature"             <temperature>   (gPool)     { channel = "pentair:controller:1:main:status#solartemperature" }

String PoolHeatMode            "Pool Heat Mode [%s]"                            (gPool)     { channel="pentair:controller:1:main:poolheat#heatmode" }
String SpaHeatMode             "Spa Heat Mode [%s]"                             (gPool)     { channel="pentair:controller:1:main:spaheat#heatmode" }
Number:Temperature PoolSetPoint "Pool Set Point"                                (gPool)     { channel="pentair:controller:1:main:poolheat#setpoint" }
Number:Temperature SpaSetPoint  "Spa Set Point"                                 (gPool)     { channel="pentair:controller:1:main:spaheat#setpoint" }

String PoolLightMode           "Light Mode"                                     (gPool)     { channel="pentair:controller:1:main:status#lightmode" }

Number PoolHeatEnable           "Pool Heat Enable [%d]"                         (gPool)     { channel="pentair:controller:1:main:poolheatenable" }
Number SpaHeatEnable            "Spa Heat Enable [%d]"                          (gPool)     { channel="pentair:controller:1:main:spaheatenable" }

Switch Mode_Pool                "Pool Mode"                                     (gPool)     { channel = "pentair:controller:1:main:pool#switch" }
Switch Mode_Spa                 "Spa"                                           (gPool)     { channel = "pentair:controller:1:main:spa#switch" }
Switch Mode_PoolLight           "Pool Light"                                    (gPool)     { channel = "pentair:controller:1:main:aux1#switch" }
Switch Mode_SpaLight            "Spa Light"                                     (gPool)     { channel = "pentair:controller:1:main:aux2#switch" }
Switch Mode_Jets                "Jets"                                          (gPool)     { channel = "pentair:controller:1:main:aux3#switch" }
Switch Mode_Boost               "Boost Mode"                                    (gPool)     { channel = "pentair:controller:1:main:aux4#switch" }
Switch Mode_Aux5                "Aux5 Mode"                                     (gPool)     { channel = "pentair:controller:1:main:aux5#switch" }
Switch Mode_Aux6                "Aux6 Mode"                                     (gPool)     { channel = "pentair:controller:1:main:aux6#switch" }
Switch Mode_Aux7                "Aux7 Mode"                                     (gPool)     { channel = "pentair:controller:1:main:aux7#switch" }

String ModeName_Pool            "Pool Name [%s]"                                (gPool)     { channel = "pentair:controller:1:main:pool#name" }
String ModeFunc_Pool            "Pool Func [%s]"                                (gPool)     { channel = "pentair:controller:1:main:pool#function" }

String ModeName_Spa            "Spa Name [%s]"                                (gPool)     { channel = "pentair:controller:1:main:spa#name" }
String ModeFunc_Spa            "Spa Func [%s]"                                (gPool)     { channel = "pentair:controller:1:main:spa#function" }

Switch Delay                    "Heater Delay"                                  (gPool)     { channel = "pentair:controller:1:main:status#heaterdelay" }

Number Salt_Output              "Salt Output [%d%%]"                            (gPool)     { channel = "pentair:intellichlor:1:ic40:salt_output" }
Number Salinity                 "Salinity [%d ppm]"                             (gPool)     { channel = "pentair:intellichlor:1:ic40:salinity" }

Switch Pump_Run                 "Pump run"                                      (gPool)     { channel = "pentair:intelliflo:1:pump1:run" }
Number Pump_RPM                 "Pump RPM [%d]"                                 (gPool)     { channel = "pentair:intelliflo:1:pump1:rpm" }
Number Pump_GPM                 "Pump GPM [%d]"                                 (gPool)     { channel = "pentair:intelliflo:1:pump1:gpm" }
Number Pump_Power               "Pump Power [%d W]"                             (gPool)     { channel = "pentair:intelliflo:1:pump1:power" }
Number Pump_Error               "Pump Error [%d]"                               (gPool)     { channel = "pentair:intelliflo:1:pump1:error" }
Number Pump_Status1             "Pump Status 1 [%d]"                            (gPool)     { channel = "pentair:intelliflo:1:pump1:status1" }
Number Pump_Status2             "Pump Status 2 [%d]"                            (gPool)     { channel = "pentair:intelliflo:1:pump1:status2" }

Number Schedule1_Start          "Schedule 1 start"                              (gPool)     { channel = "pentair:controller:1:main:schedule1#start" }
Number Schedule1_End            "Schedule 1 end"                                (gPool)     { channel = "pentair:controller:1:main:schedule1#end" }
Number Schedule1_Type           "Schedule 1 type"                               (gPool)     { channel = "pentair:controller:1:main:schedule1#type" }
String Schedule1_String         "Schedule 1 string"                             (gPool)     { channel = "pentair:controller:1:main:schedule1#schedule" }
Number Schedule1_Circuit        "Schedule 1 circuit"                            (gPool)     { channel = "pentair:controller:1:main:schedule1#circuit" }
String Schedule1_Days           "Schedule 1 days"                               (gPool)     { channel = "pentair:controller:1:main:schedule1#days" }
```

### sitemap

```perl
sitemap pool label="Pool stuff" {
  Frame label="Pool" {
    Switch item=Mode_Pool
    Switch item=Mode_PoolLight
    Text item=Pool_Temp valuecolor=[>82="red",>77="orange",<=77="blue"]
    Setpoint item=PoolSetPoint minValue=85 maxValue=103 step=1.0
    Default item=PoolLightMode
    Group item=gPool label="Advanced"
  }
  Frame label="Spa" {
    Switch item=Mode_Spa
    Switch item=Mode_SpaLight
    Switch item=Mode_Jets
    Text item=Spa_Temp valuecolor=[>82="red",>77="orange",<=77="blue"]
    Setpoint item=SpaSetPoint minValue=85 maxValue=103 step=1.0
  }
}
```

## References

Setting up RS485 and basic protocol - <https://www.sdyoung.com/home/decoding-the-pentair-easytouch-rs-485-protocol/>
ser2sock GitHub - <https://github.com/nutechsoftware/ser2sock>
nodejs-poolController - <https://github.com/tagyoureit/nodejs-poolController>
