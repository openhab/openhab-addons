# RFXCOM Binding

This binding integrates large number of sensors and actuators from several different manufactures through [RFXCOM transceivers](http://www.rfxcom.com).

RFXCOM transceivers support RF 433 Mhz protocols like:

*   HomeEasy
*   Cresta
*   X10
*   La Crosse
*   OWL
*   CoCo (KlikAanKlikUit),
*   PT2262
*   Oregon
*   etc.

See the RFXtrx User Guide from [RFXCOM](http://www.rfxcom.com) for the complete list of supported sensors and devices as well as firmware update announcements.

## Supported RFXCOM Types

This binding supports the RFXtrx433E and RFXtrx315 transceivers and the RFXrec433 receiver as bridges for accessing different sensors and actuators.

## Discovery

The transceivers/receivers may be automatically discovered by the JD2XX library and put in the Inbox or may be configured manually.

After the bridge is configured and the transceiver receives a message from any sensor or actuator, the device is put in the Inbox.
Because RFXCOM communication is a one way protocol, receiver actuators can't be discovered automatically.

### Note: Apple OS X

Apple provides built-in FTDI drivers for OS X, which need to be disabled to get JD2XX work properly.

FTDI driver disabling can be done by the following command

```
sudo kextunload -b com.apple.driver.AppleUSBFTDI
```

FTDI driver can be enabled by the following command

```
sudo kextload -b com.apple.driver.AppleUSBFTDI
```

### Note: Linux

Linux has built-in FTDI drivers, which need to be disabled for JD2XX to take over

FTDI drivers can be disabled by blacklisting the ftdi\_sio module in your modprobe config (/etc/modprobe.d/).
However this will require ALL FTDI devices to then be accessed via something like JD2XX.
If you have, or may acquire, other USB serial devices you will probably prefer to configure your RFXCOM manually.

If you configure the RFXCOM manually note that the serial port that is assigned to it may change if you have more than one USB serial device.
On systems using udev (practically all modern Linux systems) you can add a rule to /etc/udev/rules.d/ such as:

```
SUBSYSTEM=="tty", ATTRS{product}=="RFXtrx433", ATTRS{serial}=="A12LPLW", SYMLINK+="rfxtrx0"
```

and then you will be able to use /dev/rfxtrx0 as the serial device regardless of what /dev/ttyUSB<n> device has been assigned.
(N.B. you can get the product and serial strings to use from the output of dmesg, lsusb or by looking in /sys/)

### Manual Configuration

If you have any problems with JD2XX, or you don't want to disable FTDI driver under OS X or Linux, you can also configure RFXCOM transceivers/receivers manually.

To do that, manually add the generic RFXCOM device named `RFXCOM USB Transceiver`, with the description "This is universal RFXCOM transceiver bridge for manual configuration purposes".
You will need to specify at least the serial port which has been assigned to the RFXCOM (see notes above).
To configure the serial port within openHAB see the [general documentation about serial port configuration](/docs/administration/serial.html).

Alternatively you can add the RFXCOM using a thing file such as:

```
Bridge rfxcom:bridge:usb0 [ serialPort="/dev/<device>" ] {
    _thing definitions_...
}
```

#### RFXCOM over TCP/IP

You can also use an RFXCOM device over TCP/IP.
To start a TCP server for an RFXCOM device, you can use socat:

```
socat tcp-listen:10001,fork,reuseaddr file:/dev/ttyUSB0,raw
```

A TCP bridge, for use with socat on a remote host, can be configured manually, or by adding an "RFXCOM USB Transceiver over TCP/IP" device or in a thing file like this:

```
Bridge rfxcom:tcpbridge:sunflower [ host="sunflower", port=10001 ] {
    Thing lighting2 100001_1 [deviceId="100001.1", subType="AC"]
}
```

## Bridge Configuration

| Applies to                        | Parameter Label                 | Parameter ID           | Description                                                              | Required | Default |
|-----------------------------------|---------------------------------|------------------------|--------------------------------------------------------------------------|----------|---------|
| all                               | Transceiver type                | transceiverType        | Type of the transceiver                                                  | false    |         |
| all                               | Disable discovery               | disableDiscovery       | Prevent unknown devices from being added to the inbox                    | true     | false   |
| all                               | Skip transceiver configuration  | ignoreConfig           | Do not send config. command, other config will be ignored                | true     | true    |
| all                               | RFXCOM transceiver mode         | setMode                | Config. command as hexadec. (28 chars). If set, other config is ignored. | false    |         |
| all                               | Transmit Power                  | transmitPower          | Transmit power in dBm, between -18dBm and +10dBm.                        | false    | -18     |
| all except RFXtrx315              | Enable AEBlyss                  | enableAEBlyss          | Enable receiving of protocol AEBlyss                                     | false    |         |
| all except RFXtrx315              | Enable AC                       | enableAC               | Enable receiving of protocol AC                                          | false    |         |
| all except RFXtrx315              | Enable AD / LightwaveRF         | enableADLightwaveRF    | Enable receiving of protocol AD / LightwaveRF                            | false    |         |
| all except RFXtrx315              | Enable ARC                      | enableARC              | Enable receiving of protocol ARC                                         | false    |         |
| all except RFXtrx315              | Enable ATI                      | enableATI              | Enable receiving of protocol ATI / cartelectronic                        | false    |         |
| all except RFXtrx315              | Enable Blinds T0                | enableBlindsT0         | Enable receiving of protocol Blinds T0                                   | false    |         |
| all except RFXtrx315              | Enable Blinds T1                | enableBlindsT1T2T3T4   | Enable receiving of protocol Blinds T1                                   | false    |         |
| all except RFXtrx315              | Enable Byron SX                 | enableByronSX          | Enable receiving of protocol Byron SX                                    | false    |         |
| all except RFXtrx315              | Enable FineOffset / Viking      | enableFineOffsetViking | Enable receiving of protocol FineOffset / Viking                         | false    |         |
| all except RFXtrx315              | Enable FS20/Legrand CAD         | enableFS20             | Enable receiving of protocol FS20/Legrand CAD                            | false    |         |
| all except RFXtrx315              | Enable Hideki / UPM             | enableHidekiUPM        | Enable receiving of protocol Hideki / UPM                                | false    |         |
| all except RFXtrx315              | Enable HomeConfort              | enableHomeConfort      | Enable receiving of protocol HomeConfort                                 | false    |         |
| all except RFXtrx315              | Enable HomeEasy EU              | enableHomeEasyEU       | Enable receiving of protocol HomeEasy EU                                 | false    |         |
| all except RFXtrx315              | Enable Imagintronix / Opus      | enableImagintronixOpus | Enable receiving of protocol Imagintronix / Opus                         | false    |         |
| all except RFXtrx315              | Enable KEELOQ                   | enableKEELOQ           | Enable receiving of protocol KEELOQ                                      | false    |         |
| all except RFXtrx315              | Enable La Crosse                | enableLaCrosse         | Enable receiving of protocol La Crosse                                   | false    |         |
| all except RFXtrx315              | Enable Lighting4                | enableLighting4        | Enable receiving of protocol Lighting4                                   | false    |         |
| all except RFXtrx315              | Enable Meiantech                | enableMeiantech        | Enable receiving of protocol Meiantech                                   | false    |         |
| all except RFXtrx315              | Enable Mertik                   | enableMertik           | Enable receiving of protocol Mertik                                      | false    |         |
| all except RFXtrx315              | Enable Oregon Scientific        | enableOregonScientific | Enable receiving of protocol Oregon Scientific                           | false    |         |
| all except RFXtrx315              | Enable ProGuard                 | enableProGuard         | Enable receiving of protocol ProGuard                                    | false    |         |
| all except RFXtrx315              | Enable RSL                      | enableRSL              | Enable receiving of protocol RSL                                         | false    |         |
| all except RFXtrx315              | Enable Rubicson                 | enableRubicson         | Enable receiving of protocol Rubicson / Lacrosse / Banggood              | false    |         |
| all                               | Enable Visonic                  | enableVisonic          | Enable receiving of protocol Visonic                                     | false    |         |
| all                               | Enable Undecoded                | enableUndecoded        | Enable receiving of protocol Undecoded                                   | false    |         |
| all                               | Enable X10                      | enableX10              | Enable receiving of protocol X10                                         | false    |         |
| bridge                            | Serial port                     | serialPort             | Serial port for manual configuration                                     | true     |         |
| RFXtrx315 / RFXrec433 / RFXtrx433 | Serial number                   | bridgeId               | Serial number of the RFXCOM (FTDI) device                                | true     |         |
| tcpbridge                         | Host                            | host                   | Hostname / ip address of device                                          | true     |         |
| tcpbridge                         | Port                            | port                   | Port of device                                                           | true     |         |

## Thing Configuration

Configuration parameters are listed alongside each thing type. Most devices only require a deviceId and
a subType, but some things require additional configuration. The deviceId is used both when receiving and
transmitting messages, the subType is mainly used when sending messages, but it can vary between device
types.

## Channels

This binding currently supports following channel types:

| Channel Type ID | Item Type     | Description                                                                        |
|-----------------|---------------|------------------------------------------------------------------------------------|
| chimesound      | Number        | Id of the chime sound                                                              |
| command         | Switch        | Command channel.                                                                   |
| commandId       | Number        | Id of the command (between 0 and 255).                                             |
| commandString   | String        | Id of the command.                                                                 |
| contact         | Contact       | Contact channel.                                                                   |
| datetime        | DateTime      | DateTime channel.                                                                  |
| dimminglevel    | Dimmer        | Dimming level channel.                                                             |
| fanspeedstring  | String        | Set the speed of the device, values could be device specific                       |
| fanspeedcontrol | Rollershutter | Set the speed of the device, values could be device specific                       |
| fanlight        | Switch        | Enable light of Fan                                                                |
| forecast        | String        | Weather forecast from device: NO\_INFO\_AVAILABLE/SUNNY/PARTLY\_CLOUDY/CLOUDY/RAIN |
| tempcontrol     | Rollershutter | Global control for temperature also setting ON, OFF, UP, DOWN                      |
| humidity        | Number        | Relative humidity level in percentages.                                            |
| humiditystatus  | String        | Current humidity status: NORMAL/COMFORT/DRY/WET                                    |
| instantamp      | Number        | Instant current in Amperes.                                                        |
| instantpower    | Number        | Instant power consumption in Watts.                                                |
| mood            | Number        | Mood channel.                                                                      |
| motion          | Switch        | Motion detection sensor state.                                                     |
| pressure        | Number        | Barometric value in hPa.                                                           |
| pulses          | String        | Space separated decimal pulse lengths for a raw message in usec.                   |
| rainrate        | Number        | Rain fall rate in millimeters per hour.                                            |
| raintotal       | Number        | Total rain in millimeters.                                                         |
| rawmessage      | String        | Hexadecimal representation of the raw RFXCOM msg incl. header and payload          |
| rawpayload      | String        | Hexadecimal representation of the payload of RFXCOM messages                       |
| setpoint        | Number        | Requested temperature.                                                             |
| shutter         | Rollershutter | Shutter/blind channel.                                                             |
| status          | String        | Status channel.                                                                    |
| temperature     | Number        | Current temperature in degree Celsius.                                             |
| totalusage      | Number        | Used energy in Watt hours.                                                         |
| totalamphour    | Number        | Used "energy" in ampere-hours.                                                     |
| uv              | Number        | Current UV level.                                                                  |
| venetianBlind   | Dimmer        | Open/close and adjust angle of venetian blind                                      |
| voltage         | Number        | Voltage                                                                            |
| winddirection   | Number        | Wind direction in degrees.                                                         |
| windspeed       | Number        | Wind speed in meters per second.                                                   |


The binding uses the following system channels:

| Channel Type ID        | Item Type | Description                                                                                                                                                                                                           |
|------------------------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| system.signal-strength | Number    | Represents signal strength of a device as a Number with values 0, 1, 2, 3 or 4; 0 being worst strength and 4 being best strength.                                                                                     |
| system.battery-level   | Number    | Represents the battery level as a percentage (0-100%). Bindings for things supporting battery level in a different format (eg 4 levels) should convert to a percentage to provide a consistent battery level reading. |
| system.low-battery     | Switch    | Represents a low battery warning with possible values on/off.                                                                                                                                                         |

## Full Example

*.thing
```
Bridge rfxcom:bridge:usb0 [ serialPort="/dev/<device>" ] {
    Thing lighting2 100001_1 [ deviceId="100001.1", subType="AC" ]
}
```

*.items
```
Switch Switch {channel="rfxcom:lighting2:usb0:100001_1:command"}
```

## Supported Things

This binding currently supports the following things / message types:

*   [bbqtemperature - RFXCOM BBQ Temperature Sensor](#bbqtemperature---rfxcom-bbq-temperature-sensor)
*   [blinds1 - RFXCOM Blinds1 Actuator](#blinds1---rfxcom-blinds1-actuator)
*   [chime - RFXCOM Chime](#chime---rfxcom-chime)
*   [currentenergy - RFXCOM CurrentEnergy Actuator](#currentenergy---rfxcom-currentenergy-actuator)
*   [curtain1 - RFXCOM Curtain1 Actuator](#curtain1---rfxcom-curtain1-actuator)
*   [datetime - RFXCOM Date/time sensor](#datetime---rfxcom-datetime-sensor)
*   [energy - RFXCOM Energy Sensor](#energy---rfxcom-energy-sensor)
*   [fan - RFXCOM Fan Actuator](#fan---rfxcom-fan-actuator)
*   [humidity - RFXCOM Humidity Sensor](#humidity---rfxcom-humidity-sensor)
*   [lighting1 - RFXCOM Lighting1 Actuator](#lighting1---rfxcom-lighting1-actuator)
*   [lighting2 - RFXCOM Lighting2 Actuator](#lighting2---rfxcom-lighting2-actuator)
*   [lighting4 - RFXCOM Lighting4 Actuator](#lighting4---rfxcom-lighting4-actuator)
*   [lighting5 - RFXCOM Lighting5 Actuator](#lighting5---rfxcom-lighting5-actuator)
*   [lighting6 - RFXCOM Lighting6 Actuator](#lighting6---rfxcom-lighting6-actuator)
*   [rain - RFXCOM Rain Sensor](#rain---rfxcom-rain-sensor)
*   [raw - RFXCOM Raw Messages](#raw---rfxcom-raw-messages)
*   [rfxsensor - RFXCOM rfxsensor](#rfxsensor)
*   [rfy - RFXCOM Rfy Actuator](#rfy---rfxcom-rfy-actuator)
*   [security1 - RFXCOM Security1 Sensor](#security1---rfxcom-security1-sensor)
*   [temperaturehumiditybarometric - RFXCOM Temperature-Humidity-Barometric Sensor](#temperaturehumiditybarometric---rfxcom-temperature-humidity-barometric-sensor)
*   [temperaturehumidity - RFXCOM Temperature-Humidity Sensor](#temperaturehumidity---rfxcom-temperature-humidity-sensor)
*   [temperaturerain - RFXCOM Temperature-Rain Sensor](#temperaturerain---rfxcom-temperature-rain-sensor)
*   [temperature - RFXCOM Temperature Sensor](#temperature---rfxcom-temperature-sensor)
*   [thermostat1 - RFXCOM Thermostat1 Sensor](#thermostat1---rfxcom-thermostat1-sensor)
*   [thermostat3 - RFXCOM Thermostat3 Sensor](#thermostat3---rfxcom-thermostat3-sensor)
*   [undecoded - RFXCOM Undecoded RF Messages](#undecoded---rfxcom-undecoded-rf-messages)
*   [uv - RFXCOM UV/Temperature Sensor](#uv---rfxcom-uvtemperature-sensor)
*   [wind - RFXCOM Wind Sensor](#wind---rfxcom-wind-sensor)

### bbqtemperature - RFXCOM BBQ Temperature Sensor

A BBQ Temperature device

#### Channels

| Name            | Channel Type                        | Item Type | Remarks  |
|-----------------|-------------------------------------|-----------|----------|
| foodTemperature | [temperature](#channels)            | Number    |          |
| bbqTemperature  | [temperature](#channels)            | Number    |          |
| signalLevel     | [system.signal-strength](#channels) | Number    |          |
| batteryLevel    | [system.battery-level](#channels)   | Number    |          |
| lowBattery      | [system.low-battery](#channels)     | Switch    |          |

#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 56923


### blinds1 - RFXCOM Blinds1 Actuator

A Blinds1 device. Not all blinds support all commands.

#### Channels

| name         | Channel Type                        | Item type     | Remarks |
|--------------|-------------------------------------|---------------|---------|
| command      | [command](#channels)                | Switch        |         |
| shutter      | [shutter](#channels)                | Rollershutter |         |
| signalLevel  | [system.signal-strength](#channels) | Number        |         |
| batteryLevel | [system.battery-level](#channels)   | Number        |         |
| lowBattery   | [system.low-battery](#channels)     | Switch        |         |


#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id + unit code, separated by dot. Example 23455.1

*   subType - Sub Type
    *   Specifies device sub type.

        *   T0 - RollerTrol, Hasta new
        *   T1 - Hasta old
        *   T2 - A-OK RF01
        *   T3 - A-OK AC114/AC123
        *   T4 - Raex YR1326
        *   T5 - Media Mount (warning - directions reversed)
        *   T6 - DC106/Rohrmotor24-RMF/Yooda/Dooya/ESMO/Brel/Quitidom
        *   T7 - Forest
        *   T8 - Chamberlain CS4330CN
        *   T9 - Sunpery/BTX
        *   T10 - Dolat DLM-1, Topstar
        *   T11 - ASP
        *   T12 - Confexx CNF24-2435
        *   T13 - Screenline
        *   T14 - Hualite
        *   T15 - Motostar
        *   T16 - Zemismart
        *   T17 - Gaposa
        *   T18 - Cherubini
        *   T19 - Louvolite One Touch Vogue motor
        *   T20 - OZRoll

### chime - RFXCOM Chime

A Chime device

#### Channels

| Name        | Channel Type                        | Item Type | Remarks                                 |
|-------------|-------------------------------------|-----------|-----------------------------------------|
| chimeSound  | [chimesound](#channels)             | Number    | not all devices support multiple sounds |
| signalLevel | [system.signal-strength](#channels) | Number    |                                         |


#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 2983

*   subType - Sub Type
    *   Specifies device sub type.

        * BYRONSX - Byron SX
        * BYRONMP001 - Byron MP001
        * SELECTPLUS - SelectPlus
        * SELECTPLUS3 - SelectPlus3
        * ENVIVO - Envivo
        * ALFAWISE_DBELL - Alfawise, dBell


### current - RFXCOM Current Sensor

A Current sensing device.

#### Channels

| Name         | Channel Type                        | Item Type | Remarks          |
|--------------|-------------------------------------|-----------|------------------|
| channel1Amps | [instantamp](#channels)             | Number    |                  |
| channel2Amps | [instantamp](#channels)             | Number    |                  |
| channel3Amps | [instantamp](#channels)             | Number    |                  |
| signalLevel  | [system.signal-strength](#channels) | Number    |                  |
| batteryLevel | [system.battery-level](#channels)   | Number    |                  |
| lowBattery   | [system.low-battery](#channels)     | Switch    |                  |

#### Configuration Options

 * deviceId - Device Id
    * Sensor Id. Example 5693

 * subType - Sub Type
    * Specifies device sub type.

        * ELEC1 - OWL - CM113

### currentenergy - RFXCOM CurrentEnergy Actuator

A CurrentEnergy device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks          |
|--------------|-------------------------------------|-----------|------------------|
| channel1Amps | [instantamp](#channels)             | Number    |                  |
| channel2Amps | [instantamp](#channels)             | Number    |                  |
| channel3Amps | [instantamp](#channels)             | Number    |                  |
| totalUsage   | [totalusage](#channels)             | Number    |                  |
| signalLevel  | [system.signal-strength](#channels) | Number    |                  |
| batteryLevel | [system.battery-level](#channels)   | Number    |                  |
| lowBattery   | [system.low-battery](#channels)     | Switch    |                  |

#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 47104

*   subType - Sub Type
    *   Specifies device sub type.

        *   ELEC4 - OWL - CM180i


### curtain1 - RFXCOM Curtain1 Actuator

A Curtain1 device

#### Channels

| Name         | Channel Type                        | Item Type     | Remarks          |
|--------------|-------------------------------------|---------------|------------------|
| command      | [command](#channels)                | Switch        |                  |
| shutter      | [shutter](#channels)                | Rollershutter |                  |
| signalLevel  | [system.signal-strength](#channels) | Number        |                  |
| batteryLevel | [system.battery-level](#channels)   | Number        |                  |
| lowBattery   | [system.low-battery](#channels)     | Switch        |                  |

#### Configuration Options

*   deviceId - Device Id
    *   House code + unit code, separated by dot. Example A.1

*   subType - Sub Type
    *   Specifies device sub type.

        *   HARRISON - Harrison Curtain


### datetime - RFXCOM Date/time sensor

A DateTime device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks          |
|--------------|-------------------------------------|-----------|------------------|
| dateTime     | [datetime](#channels)               | DateTime  |                  |
| signalLevel  | [system.signal-strength](#channels) | Number    |                  |
| batteryLevel | [system.battery-level](#channels)   | Number    |                  |
| lowBattery   | [system.low-battery](#channels)     | Switch    |                  |

#### Configuration Options

*   deviceId - Device Id
    *   Device id, example 47360

*   subType - Sub Type
    *   Specifies device sub type.

        *   RTGR328N - Oregon RTGR328N

### fan - RFXCOM Fan Actuator

A group of fan devices

#### fan - Standard Fan

A Fan device


##### Channels

| Name         | Channel Type                        | Item Type | Remarks                      |
|--------------|-------------------------------------|-----------|------------------------------|
| command      | [command](#channels)                | Switch    |                              |
| fanSpeed     | [fanspeedstring](#channels)         | String    | Options: HI, MED, LOW, OFF   |
| fanLight     | [fanlight](#channels)               | Switch    |                              |
| signalLevel  | [system.signal-strength](#channels) | Number    |                              |


##### Configuration Options

*   deviceId - Device Id
    *   Device id, example 47360    
*   subType - Sub Type
    *   Specifies device sub type.
        *   LUCCI_AIR_FAN - Lucci Air fan
        *   CASAFAN - Casafan
        *   WESTINGHOUSE_7226640 - Westinghouse 7226640

##### Example

Sitemap:

```java
Switch item=FanSwitch label="Fan"
Switch item=FanLightSwitch label="Light" mappings=[ON="On"]
Switch item=FanSpeedSwitch label="Speed" mappings=[LOW=Low, MED=Medium, HI=High]
```

#### fan_falmec - Falmec fan

A Falmec Fan device

##### Channels

| Name         | Channel Type                        | Item Type | Remarks                      |
|--------------|-------------------------------------|-----------|------------------------------|
| command      | [command](#channels)                | Switch    |                              |
| fanSpeed     | [fanspeed](#channels)               | Number    | Options: 1,2,3,4,5,6         |
| fanLight     | [fanlight](#channels)               | Switch    |                              |
| signalLevel  | [system.signal-strength](#channels) | Number    |                              |


##### Configuration Options

*   deviceId - Device Id
    *   Device id, example 47360    
*   subType - Sub Type
    *   Specifies device sub type.
        *   FALMEC - Falmec

#### fan_lucci_dc - Lucci Air DC fan

A Lucci Air DC fan device

##### Channels

| Name            | Channel Type                        | Item Type     | Remarks                                                      |
|-----------------|-------------------------------------|---------------|--------------------------------------------------------------|
| commandString   | [commandString](#channels)          | String        | Options: POWER, UP, DOWN, LIGHT, REVERSE, NATURAL_FLOW, PAIR |
| fanSpeedControl | [fanspeedcontrol](#channels)        | RollerShutter | Options: UP / DOWN                                           |
| fanSpeed        | [fanspeed](#channels)               | Number        | Options: 1,2,3,4,5,6                                         |
| fanLight        | [fanlight](#channels)               | Switch        |                                                              |
| signalLevel     | [system.signal-strength](#channels) | Number        |                                                              |

##### Configuration Options

*   deviceId - Device Id
    *   Device id, example 47360    
*   subType - Sub Type
    *   Specifies device sub type.
        *   LUCCI_AIR_DC - Lucci Air DC

#### fan_lucci_dc_ii - Lucci Air DC II fan

A Lucci Air DC II fan device

##### Channels

| Name         | Channel Type                        | Item Type | Remarks                              |
|--------------|-------------------------------------|-----------|--------------------------------------|
| command      | [command](#channels)                | Switch    |                                      |
| commandString| [commandString](#channels)          | String    | Options: POWER_OFF, LIGHT, REVERSE   |
| fanSpeed     | [fanspeed](#channels)               | Number    | Options: 1,2,3,4,5,6                 |
| fanLight     | [fanlight](#channels)               | Switch    |                                      |
| signalLevel  | [system.signal-strength](#channels) | Number    |                                      |

##### Configuration Options

*   deviceId - Device Id
    *   Device id, example 47360    
*   subType - Sub Type
    *   Specifies device sub type.
        *   LUCCI_AIR_DC_II - Lucci Air DC II
        
#### fan_novy - Novy extractor fan

A Novy extractor fan.

##### Channels

| Name            | Channel Type                        | Item Type     | Remarks                  |
|-----------------|-------------------------------------|---------------|--------------------------|
| command         | [command](#channels)                | Switch        |                          |
| commandString   | [commandString](#channels)          | String        | Options: POWER, UP, DOWN, LIGHT, LEARN, RESET_FILTER |
| fanSpeedControl | [fanspeedcontrol](#channels)        | RollerShutter | Options: UP / DOWN       |
| fanLight        | [fanlight](#channels)               | Switch        |                          |
| signalLevel     | [system.signal-strength](#channels) | Number        |                          |

##### Configuration Options

*   deviceId - Device Id
    *   Device id, example 47360
*   subType - Sub Type
    *   Specifies device sub type.
        *   NOVY - Novy extractor fan

### energy - RFXCOM Energy Sensor

An Energy device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks   |
|--------------|-------------------------------------|-----------|-----------|
| instantPower | [instantpower](#channels)           | Number    |           |
| totalUsage   | [totalusage](#channels)             | Number    |           |
| instantAmp   | [instantamp](#channels)             | Number    |           |
| totalAmpHour | [totalamphour](#channels)           | Number    |           |
| signalLevel  | [system.signal-strength](#channels) | Number    |           |
| batteryLevel | [system.battery-level](#channels)   | Number    |           |
| lowBattery   | [system.low-battery](#channels)     | Switch    |           |

#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 5693

*   subType - Sub Type
    *   Specifies device sub type.

        *   ELEC2 - CM119/160
        *   ELEC3 - CM180


### humidity - RFXCOM Humidity Sensor

A Humidity device

#### Channels

| Name           | Channel Type                        | Item Type | Remarks  |
|----------------|-------------------------------------|-----------|----------|
| humidity       | [humidity](#channels)               | Number    |          |
| humidityStatus | [humiditystatus](#channels)         | String    |          |
| signalLevel    | [system.signal-strength](#channels) | Number    |          |
| batteryLevel   | [system.battery-level](#channels)   | Number    |          |
| lowBattery     | [system.low-battery](#channels)     | Switch    |          |

#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 5693

*   subType - Sub Type
    *   Specifies device sub type.

        *   HUM1 - LaCrosse TX3
        *   HUM2 - LaCrosse WS2300
        *   HUM3 - Inovalley S80 plant humidity sensor


### lighting1 - RFXCOM Lighting1 Actuator

A Lighting1 device

#### Channels

| Name              | Channel Type                        | Item Type | Remarks  |
|-------------------|-------------------------------------|-----------|----------|
| command           | [command](#channels)                | Switch    |          |
| commandString\*\* | [commandString](#channels)          | String    |          |
| contact           | [contact](#channels)                | Contact   |          |
| signalLevel       | [system.signal-strength](#channels) | Number    |          |

\*\* `commandString` supports:

* OFF
* ON
* DIM
* BRIGHT
* GROUP_OFF
* GROUP_ON
* CHIME

#### Configuration Options

*   deviceId - Device Id
    *   Device Id. House code + unit code, separated by dot. Example A.1

*   subType - Sub Type
    *   Specifies device sub type.

        * X10 - X10 lighting
        * ARC - ARC
        * AB400D - ELRO AB400D (Flamingo)
        * WAVEMAN - Waveman
        * EMW200 - Chacon EMW200
        * IMPULS - IMPULS
        * RISINGSUN - RisingSun
        * PHILIPS - Philips SBC
        * ENERGENIE - Energenie ENER010
        * ENERGENIE\_5 - Energenie 5-gang
        * COCO - COCO GDR2-2000R
        * HQ\_COCO20 - HQ COCO-20
        * OASE_INSCENIO_FM_N - Oase Inscenio FM Master


### lighting2 - RFXCOM Lighting2 Actuator

A Lighting2 device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks |
|--------------|-------------------------------------|-----------|---------|
| command      | [command](#channels)                | Switch    |         |
| contact      | [command](#channels)                | Contact   |         |
| dimmingLevel | [dimminglevel](#channels)           | Dimmer    |         |
| signalLevel  | [system.signal-strength](#channels) | Number    |         |

#### Configuration Options

*   deviceId - Device Id
    *   Remote/switch/unit Id + unit code, separated by dot. Example 8773718.10

*   subType - Sub Type
    *   Specifies device sub type.

        *   AC - AC
        *   HOME\_EASY\_EU - HomeEasy EU
        *   ANSLUT - ANSLUT
        *   KAMBROOK - Kambrook RF3672


### lighting4 - RFXCOM Lighting4 Actuator

A Lighting4 device. The specification for the PT2262 protocol includes 3 bytes for data. By
convention, the first 20 bits of this is used for deviceId, and the last 4 bits is used for
command, which gives us a total of 16 commands per device.

Depending on your device, you may have only one command, one pair of commands (on/off), or
any other multiple, for example, a set of 4 sockets with an on/off pair for each and an
additional pair for "all".

Different device manufactures using this protocol will use different schemes for their
commands, so to configure a thing using the lighting4 protocol, you must specify at least
one commandId in the thing configuration. If a device has multiple sets of commands, you
can configure multiple things with the same device id, but different commandIds.

Some devices will expect a specific pulse length. If required, that can also be specified
as a thing configuration parameter.

Previously, openHAB would attempt to guess at the meaning of a commandId if it was not
specified in the thing configuration based on devices seen in the wild. Due to the varying
nature of devices, this behaviour is deprecated and will be removed in a future openHAB
version. Until then, commands 1, 3, 5-13 and 15 are considered ON and 0, 2, 4 and 14 are
considered OFF when the `onCommandId` or `offCommandId` for a device is not specified.

#### Channels

| Name        | Channel Type                        | Item Type | Remarks  |
|-------------|-------------------------------------|-----------|----------|
| command     | [command](#channels)                | Switch    |          |
| commandId   | [commandId](#channels)              | Number    |          |
| signalLevel | [system.signal-strength](#channels) | Number    |          |


#### Configuration Options

*   deviceId - Device Id
    *   Device Id. Example 3456

*   subType - Sub Type
    *   Specifies device sub type.

        *   PT2262 - PT2262

*   pulse - Pulse length
    *   Pulse length of the device

*   onCommandId - On command
    *   Specifies command that represents ON for this device.

*   offCommandId - Off command
    *   Specifies command that represents OFF for this device.

*   openCommandId - Open command
    *   Specifies command that represents OPEN for this device.
    
*   closedCommandId - Closed command
    *   Specifies command that represents CLOSED for this device.

#### Discovering commandId values

There are a number of ways to detect the commandId values for your device.

- You can turn on DEBUG messages for the rfxcom binding by adding the line
  `<Logger level="DEBUG" name="org.openhab.binding.rfxcom"/>`
  to your `log4j2.xml`. You will then be able to see the commandId in the log
  file when you trigger the device.
  
- You can link a Number Item to the commandId channel. The item will be updated with the
  detected commandId when you trigger the device.
  
- You can use RFXmngr to look at the data from the device. Use the last letter/number
  of the hexadecimal "Code", and convert it from hexadecimal to decimal.

#### Examples

For a USB attached RFXCOM on Windows the configuration could look like this (note the `pulse` is optional):

```
Bridge rfxcom:bridge:238adf67 [ serialPort="COM4" ] {
    Thing lighting4 17745a  [deviceId="17745",  subType="PT2262", onCommandId=7, offCommandId=4]
    Thing lighting4 17745b  [deviceId="17745",  subType="PT2262", onCommandId=10, offCommandId=2]
    Thing lighting4 motion [deviceId="286169", subType="PT2262", onCommandId=9, pulse=392]
}
```

Your items file could look like this:

```
Number SocketCommandId            {channel="rfxcom:lighting4:238adf67:17745a:commandId"}
Switch SocketA                    {channel="rfxcom:lighting4:238adf67:17745a:command"}
Switch SocketB                    {channel="rfxcom:lighting4:238adf67:17745b:command"}
```

#### Known commandIds

These are some commandIds from the field that may match your devices.

| Brand | What          | Action      | Command ID | Source | 
|-------|---------------|-------------|------------|--------|
| Kerui | Motion Sensor | Motion      | 10         | [#3103](https://github.com/openhab/openhab-addons/issues/3103) |
| Kerui | Door Contact  | door open   | 14         | [#3103](https://github.com/openhab/openhab-addons/issues/3103) |
| Kerui | Door Contact  | door closed | 7          | [#3103](https://github.com/openhab/openhab-addons/issues/3103) |
| Kerui | Door Contact  | tamper      | 11         | [#3103](https://github.com/openhab/openhab-addons/issues/3103) |
| Energenie | 4 Socket Power Bar | Socket 1 on  | 15 | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | Socket 1 off | 14 | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | Socket 2 on  | 7  | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | Socket 2 off | 6  | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | Socket 3 on  | 11 | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | Socket 3 off | 10 | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | Socket 4 on  | 3  | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | Socket 4 off | 2  | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | All on       | 13 | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |
| Energenie | 4 Socket Power Bar | All off      | 12 | [Community](https://community.openhab.org/t/rfxcom-looking-to-improve-lighting4-call-for-users/123674) |

### lighting5 - RFXCOM Lighting5 Actuator

A Lighting5 device

#### Channels

| Name              | Channel Type                        | Item Type | Remarks  |
|-------------------|-------------------------------------|-----------|----------|
| command           | [command](#channels)                | Switch    |          |
| commandString\*\* | [commandString](#channels)          | String    |          |
| contact           | [command](#channels)                | Contact   |          |
| dimmingLevel      | [dimminglevel](#channels)           | Dimmer    |          |
| mood              | [mood](#channels)                   | Number    |          |
| signalLevel       | [system.signal-strength](#channels) | Number    |          |

\*\* `commandString` supports:

* OFF
* ON
* GROUP_OFF
* LEARN
* GROUP_ON
* MOOD1
* MOOD2
* MOOD3
* MOOD4
* MOOD5
* RESERVED1
* RESERVED2
* UNLOCK
* LOCK
* ALL_LOCK
* CLOSE_RELAY
* STOP_RELAY
* OPEN_RELAY
* SET_LEVEL
* COLOUR_PALETTE
* COLOUR_TONE
* COLOUR_CYCLE

#### Configuration Options

*   deviceId - Device Id
    *   Remote/switch/unit Id + unit code, separated by dot. Example 10001.1

*   subType - Sub Type
    *   Specifies device sub type.

        *   AOKE - Aoke Relay
        *   AVANTEK - Avantek
        *   BBSB\_NEW - BBSB new types
        *   CONRAD\_RSL2 - Conrad RSL2
        *   EMW100 - EMW100 GAO/Everflourish
        *   EURODOMEST - Eurodomest
        *   IT - IT
        *   KANGTAI - Kangtai, Cotech
        *   LIGHTWAVERF - LightwaveRF, Siemens
        *   LIVOLO - Livolo Dimmer or On/Off 1-3
        *   LIVOLO\_APPLIANCE - Livolo Appliance On/Off 1-10
        *   MDREMOTE - MDREMOTE LED dimmer v106
        *   MDREMOTE\_107 - MDREMOTE v107
        *   MDREMOTE\_108 - MDREMOTE v108, EKAB-10KRF
        *   RGB\_TRC02 - RGB TRC02 (2 batt)
        *   RGB\_TRC02\_2 - RGB TRC02\_2 (3 batt)


### lighting6 - RFXCOM Lighting6 Actuator

A Lighting6 device

#### Channels

| Name        | Channel Type                        | Item Type | Remarks  |
|-------------|-------------------------------------|-----------|----------|
| command     | [command](#channels)                | Switch    |          |
| contact     | [command](#channels)                | Contact   |          |
| signalLevel | [system.signal-strength](#channels) | Number    |          |


#### Configuration Options

*   deviceId - Device Id
    *   Remote/switch/unit Id + group code + unit code, separated by dot. Example 100.A.1

*   subType - Sub Type
    *   Specifies device sub type.

        *   BLYSS - Blyss


### rain - RFXCOM Rain Sensor

A Rain device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks  |
|--------------|-------------------------------------|-----------|----------|
| rainRate     | [rainrate](#channels)               | Number    |          |
| rainTotal    | [raintotal](#channels)              | Number    |          |
| signalLevel  | [system.signal-strength](#channels) | Number    |          |
| batteryLevel | [system.battery-level](#channels)   | Number    |          |
| lowBattery   | [system.low-battery](#channels)     | Switch    |          |

#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 56923

*   subType - Sub Type
    *   Specifies device sub type.

        *   RAIN1 - RGR126/682/918/928
        *   RAIN2 - PCR800
        *   RAIN3 - TFA
        *   RAIN4 - UPM RG700
        *   RAIN5 - WS2300
        *   RAIN6 - La Crosse TX5
        *   RAIN9 - TFA 30.3233.1


### raw - RFXCOM Raw Messages

Raw messages. These messages are included in the Pro firmware and represent messages
for which the device does not understand the protocol. The raw message is a list of the
length of the RF pulses before they have been interpreted as bytes.

You can also send raw messages by recording the pulses of an incoming message and
using them to configure a raw thing item.

#### Channels

| Name       | Channel Type              | Item Type | Remarks     |
|------------|---------------------------|-----------|-------------|
| rawMessage | [rawmessage](#channels)   | String    |             |
| rawPayload | [rawpayload](#channels)   | String    |             |
| pulses     | [pulses](#channels)       | String    |             |

#### Configuration Options

*   deviceId - Device Id
    *   Raw items cannot provide a device ID, so to receive RAW messages use
        a Device Id of RAW. For transmit only devices, use any Device Id.

*   subType - Sub Type
    *   Specifies message sub type.

        *   RAW_PACKET1
        *   RAW_PACKET2
        *   RAW_PACKET3
        *   RAW_PACKET4

*   repeat - Repeat
    *   Number of times to repeat message on transmit. Defaults to 5.

*   onPulses - On Pulses
    *   Pulses to send for an ON command. Space delimited pulse lengths
        in usec. Must be an even number of pulse lengths, with a maximum
        of 142 total pulses. Max pulse length is 65535. Pulses of value 0
        will be transmitted as 10000. See the RFXtfx user guide for more
        information.

*   offPulses - Off Pulses
    *   Pulses to send for an OFF command. Space delimited pulse lengths
        in usec. Must be an even number of pulse lengths, with a maximum
        of 142 total pulses. Max pulse length is 65535. Pulses of value 0
        will be transmitted as 10000. See the RFXtfx user guide for more
        information.

*   openPulses - Open Pulses
    *   Pulses to send for an OPEN command. Space delimited pulse lengths
        in usec. Must be an even number of pulse lengths, with a maximum
        of 142 total pulses. Max pulse length is 65535. Pulses of value 0
        will be transmitted as 10000. See the RFXtfx user guide for more
        information.

*   closedPulses - Closed Pulses
    *   Pulses to send for an CLOSED command. Space delimited pulse lengths
        in usec. Must be an even number of pulse lengths, with a maximum
        of 142 total pulses. Max pulse length is 65535. Pulses of value 0
        will be transmitted as 10000. See the RFXtfx user guide for more
        information.

#### Examples

This can be used to transmit raw messages.

The first step is to work out the right pulses for the device. You can do this using RFXmngr, or
you can do this using openhab:

1. Set up a RAW thing to receive raw pulses:

    ```
    Bridge rfxcom:tcpbridge:rfxtrx0 [ host="192.168.42.10", port=10001, enableUndecoded=true ] {
        Thing raw RAW [ deviceId="RAW", subType="RAW_PACKET1" ]
    }
    ```

2. Add an item to see what the pulses are:

    ```
    String RawPulses { channel="rfxcom:raw:rfxtrx0:RAW:pulses" }
    ```

3. Activate the device and look at the pulses that are set. Look for a higher value in the pulses, that is
   likely to be a gap for a repeat. Take the pulses from before the gap. Make sure there are an
   even number, and if not, drop a 0 on the end.

Now you have the pulses, set up a send device:

1. Set up a RAW thing to send a command:

    ```
    Bridge rfxcom:tcpbridge:rfxtrx0 [ host="192.168.42.10", port=10001, enableUndecoded=true ] {
        Thing raw MySwitch [ deviceId="MySwitch", subType="RAW_PACKET1", onPulses="100 200 300 0", offPulses="400 500 600 0" ]
    }
    ```

2. Add an item to send the command:

    ```
    Switch MySwitch { channel="rfxcom:raw:rfxtrx0:MySwitch:command" }
    ```

3. Use the command to send the raw message.

### rfxsensor - RFXCOM RFXSensor 

A RFXSensor sensor

#### Channels

| Name             | Channel Type                        | Item Type  | Remarks |
|------------------|-------------------------------------|------------|---------|
| pressure         | [pressure](#channels)               | Number     |         |
| humidity         | [humidity](#channels)               | Number     |         |
| referenceVoltage | [voltage](#channels)                | Number     |         |
| voltage          | [voltage](#channels)                | Number     |         |
| temperature      | [temperature](#channels)            | Number     |         |
| signalLevel      | [system.signal-strength](#channels) | Number     |         |

#### Configuration Options

*   deviceId - Device Id
    *   Unit Id. Example 100

### rfy - RFXCOM Rfy Actuator

A Rfy device

#### Channels

| Name            | Channel Type                        | Item Type     | Remarks                                                                     |
|-----------------|-------------------------------------|---------------|-----------------------------------------------------------------------------|
| command         | [command](#channels)                | Switch        | Sends a program command to pair with a device when switched from off to on. |
| program         | [command](#channels)                | Switch        | Send Program Command                                                        |
| shutter         | [shutter](#channels)                | Rollershutter | Shutter                                                                     |
| venetianBlind   | [venetianBlind](#channels)          | Dimmer        |                                                                             |
| sunWindDetector | [command](#channels)                | Switch        | Enable the sun+wind detector.                                               |
| signalLevel     | [system.signal-strength](#channels) | Number        |                                                                             |

#### Configuration Options

*   deviceId - Device Id
    *   Unit Id + unit code, separated by dot. Example 100.1

*   subType - Sub Type
    *   Specifies device sub type.

        *   RFY - RFY
        *   RFY\_EXT - RFY Ext


### security1 - RFXCOM Security1 Sensor

A Security1 device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks  |
|--------------|-------------------------------------|-----------|----------|
| status       | [status](#channels)                 | String    |          |
| contact      | [command](#channels)                | Contact   |          |
| motion       | [motion](#channels)                 | Switch    |          |
| signalLevel  | [system.signal-strength](#channels) | Number    |          |
| batteryLevel | [system.battery-level](#channels)   | Number    |          |
| lowBattery   | [system.low-battery](#channels)     | Switch    |          |


#### Configuration Options

*   deviceId - Device Id
    *   Remote/sensor Id. Example 10001

*   subType - Sub Type
    *   Specifies device sub type.

        *   X10\_SECURITY - X10 security door/window sensor
        *   X10\_SECURITY\_MOTION - X10 security motion sensor
        *   X10\_SECURITY\_REMOTE - X10 security remote (no alive packets)
        *   KD101 - KD101 (no alive packets)
        *   VISONIC\_POWERCODE\_SENSOR\_PRIMARY\_CONTACT - Visonic PowerCode door/window sensor – primary contact (with alive packets)
        *   VISONIC\_POWERCODE\_MOTION - Visonic PowerCode motion sensor (with alive packets)
        *   VISONIC\_CODESECURE - Visonic CodeSecure (no alive packets)
        *   VISONIC\_POWERCODE\_SENSOR\_AUX\_CONTACT - Visonic PowerCode door/window sensor – auxiliary contact (no alive packets)
        *   MEIANTECH - Meiantech
        *   SA30 - SA30 (no alive packets)


### temperaturehumiditybarometric - RFXCOM Temperature-Humidity-Barometric Sensor

A Temperature-Humidity-Barometric device

#### Channels

| Name           | Channel Type                        | Item Type | Remarks |
|----------------|-------------------------------------|-----------|---------|
| temperature    | [temperature](#channels)            | Number    |         |
| humidity       | [humidity](#channels)               | Number    |         |
| humidityStatus | [humiditystatus](#channels)         | String    |         |
| pressure       | [pressure](#channels)               | Number    |         |
| forecast       | [forecast](#channels)               | String    |         |
| signalLevel    | [system.signal-strength](#channels) | Number    |         |
| batteryLevel   | [system.battery-level](#channels)   | Number    |         |
| lowBattery     | [system.low-battery](#channels)     | Switch    |         |


#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 59648

*   subType - Sub Type
    *   Specifies device sub type.

        *   THB1 - BTHR918, BTHGN129
        *   THB2 - BTHR918N, BTHR968


### temperaturehumidity - RFXCOM Temperature-Humidity Sensor

A Temperature-Humidity device

#### Channels

| Name           | Channel Type                        | Item Type | Remarks  |
|----------------|-------------------------------------|-----------|----------|
| temperature    | [temperature](#channels)            | Number    |          |
| humidity       | [humidity](#channels)               | Number    |          |
| humidityStatus | [humiditystatus](#channels)         | String    |          |
| signalLevel    | [system.signal-strength](#channels) | Number    |          |
| batteryLevel   | [system.battery-level](#channels)   | Number    |          |
| lowBattery     | [system.low-battery](#channels)     | Switch    |          |


#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 56923

*   subType - Sub Type
    *   Specifies device sub type.

        *   TH1 - THGN122/123, THGN132, THGR122/228/238/268
        *   TH2 - THGR810, THGN800
        *   TH3 - RTGR328
        *   TH4 - THGR328
        *   TH5 - WTGR800
        *   TH6 - THGR918/928, THGRN228, THGN500
        *   TH7 - TFA TS34C, Cresta
        *   TH8 - WT260,WT260H,WT440H,WT450,WT450H
        *   TH9 - Viking 02035,02038 (02035 has no humidity), Proove TSS320, 311501
        *   TH10 - Rubicson
        *   TH11 - EW109
        *   TH12 - Imagintronix/Opus XT300 Soil sensor
        *   TH13 - Alecto WS1700 and compatibles


### temperaturerain - RFXCOM Temperature-Rain Sensor

A Temperature-Rain device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks  |
|--------------|-------------------------------------|-----------|----------|
| temperature  | [temperature](#channels)            | Number    |          |
| rainTotal    | [raintotal](#channels)              | Number    |          |
| signalLevel  | [system.signal-strength](#channels) | Number    |          |
| batteryLevel | [system.battery-level](#channels)   | Number    |          |
| lowBattery   | [system.low-battery](#channels)     | Switch    |          |

#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 56923

*   subType - Sub Type
    *   Specifies device sub type.

        *   WS1200 - WS1200


### temperature - RFXCOM Temperature Sensor

A Temperature device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks  |
|--------------|-------------------------------------|-----------|----------|
| temperature  | [temperature](#channels)            | Number    |          |
| signalLevel  | [system.signal-strength](#channels) | Number    |          |
| batteryLevel | [system.battery-level](#channels)   | Number    |          |
| lowBattery   | [system.low-battery](#channels)     | Switch    |          |

#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 56923

*   subType - Sub Type
    *   Specifies device sub type.

        *   TEMP1 - THR128/138, THC138
        *   TEMP2 - THC238/268,THN132,THWR288,THRN122,THN122,AW129/131
        *   TEMP3 - THWR800
        *   TEMP4 - RTHN318
        *   TEMP5 - La Crosse TX2, TX3, TX4, TX17
        *   TEMP6 - TS15C. UPM temp only
        *   TEMP7 - Viking 02811, Proove TSS330, 311346
        *   TEMP8 - La Crosse WS2300
        *   TEMP9 - Rubicson
        *   TEMP10 - TFA 30.3133
        *   TEMP11 - WT0122


### thermostat1 - RFXCOM Thermostat1 Sensor

A Thermostat1 device

#### Channels

| Name        | Channel Type                        | Item Type | Remarks  |
|-------------|-------------------------------------|-----------|----------|
| contact     | [command](#channels)                | Contact   |          |
| setpoint    | [setpoint](#channels)               | Number    |          |
| temperature | [temperature](#channels)            | Number    |          |
| signalLevel | [system.signal-strength](#channels) | Number    |          |

#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 56923

*   subType - Sub Type
    *   Specifies device sub type.

        *   DIGIMAX - Digimax, TLX7506
        *   DIGIMAX\_SHORT - Digimax with short format (no set point)



### thermostat3 - RFXCOM Thermostat3 Sensor

A Thermostat3 device.

#### Channels

| Name              | Channel Type                        | Item Type     | Remarks  |
|-------------------|-------------------------------------|---------------|----------|
| command           | [command](#channels)                | Switch        |          |
| command2nd        | [command](#channels)                | Switch        |          |
| control\*         | [tempcontrol](#channels)            | Rollershutter |          |
| commandString\*\* | [commandString](#channels)          | String        |          |
| signalLevel       | [system.signal-strength](#channels) | Number        |          |

\* `control` supports:

* UP
* DOWN
* STOP

\*\* `commandString` supports:

* OFF
* ON
* UP
* DOWN
* RUN_UP
* RUN_DOWN
* SECOND_ON
* SECOND_OFF
* STOP

#### Configuration Options

 *   deviceId - Device Id
    *   Sensor Id. Example 106411

 *   subType - Sub Type
    *   Specifies device sub type.

        *   MERTIK\_\_G6R\_H4T1 - Mertik (G6R H4T1)
        *   MERTIK\_\_G6R\_H4TB\_\_G6_H4T\_\_G6R\_H4T21\_Z22 - Mertik (G6R H4TB, G6R H4T, or G6R H4T21\-Z22)
        *   MERTIK\_\_G6R\_H4TD\_\_G6R\_H4T16 - Mertik (G6R H4TD or G6R H4T16)
        *   MERTIK\_\_G6R\_H4S\_TRANSMIT\_ONLY - Mertik (G6R H4S \- transmit only)

#### Examples


### undecoded - RFXCOM Undecoded RF Messages

Undecoded messages are messages where RFCOM understands the protocol and has converted
the raw RF pulses into bytes, but has not attempted to decode the bytes into meaningful
data.

Undecoded message are receive only, there is not way to transmit an undecoded message.
If you need to repeat an undecoded message, consider looking at Raw messages instead.

#### Channels

| Name       | Channel Type              | Item Type | Remarks     |
|------------|---------------------------|-----------|-------------|
| rawMessage | [rawmessage](#channels)   | String    |             |
| rawPayload | [rawpayload](#channels)   | String    |             |

#### Configuration Options

*   deviceId - Device Id
    *   Undecoded items cannot provide a device ID, so this value is always UNDECODED.

*   subType - Sub Type
    *   Specifies device sub type.

        *   AC - AC
        *   ARC - ARC
        *   ATI - ATI
        *   HIDEKI\_UPM - Hideki, UPM
        *   LACROSSE\_VIKING - La Crosse, Viking
        *   AD - AD
        *   MERTIK - Mertik Maxitrol Fireplace controllers
        *   OREGON1 - Oregon Scientific 1
        *   OREGON2 - Oregon Scientific 2
        *   OREGON3 - Oregon Scientific 3
        *   PROGUARD - ProGuard
        *   VISONIC - Visonic
        *   NEC - NEC
        *   FS20 - FS20
        *   RESERVED - Reserved
        *   BLINDS - Blinds
        *   RUBICSON - Rubicson
        *   AE - AE
        *   FINE\_OFFSET - Fine Offset
        *   RGB - RGB
        *   RTS - RTS
        *   SELECT\_PLUS - Select Plus
        *   HOME\_CONFORT - Home Confort
        *   EDISIO - Edisio
        *   HONEYWELL - Honeywell
        *   FUNKBUS - Gira Funk-Bussystem
        *   BYRONSX - Byron SX

### uv - RFXCOM UV/Temperature Sensor

A UV/Temperature device

#### Channels

| Name         | Channel Type                        | Item Type | Remarks  |
|--------------|-------------------------------------|-----------|----------|
| uv           | [uv](#channels)                     | Number    |          |
| temperature  | [temperature](#channels)            | Number    |          |
| signalLevel  | [system.signal-strength](#channels) | Number    |          |
| batteryLevel | [system.battery-level](#channels)   | Number    |          |
| lowBattery   | [system.low-battery](#channels)     | Switch    |          |


#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 56923

*   subType - Sub Type
    *   Specifies device sub type.

        *   UV1 - UVN128, UV138
        *   UV2 - UVN800
        *   UV3 - TFA


### wind - RFXCOM Wind Sensor

A Wind device

#### Channels

| Name             | Channel Type                        | Item Type | Remarks                               |
|------------------|-------------------------------------|-----------|---------------------------------------|
| avgWindSpeed     | [windspeed](#channels)              | Number    | Average Wind Speed                    |
| windSpeed        | [windspeed](#channels)              | Number    | Wind Gust                             |
| windDirection    | [winddirection](#channels)          | Number    | Wind Direction                        |
| temperature      | [temperature](#channels)            | Number    | Current temperature in degree Celsius |
| chillTemperature | [temperature](#channels)            | Number    | Chill temperature in degree Celsius   |
| signalLevel      | [system.signal-strength](#channels) | Number    |                                       |
| batteryLevel     | [system.battery-level](#channels)   | Number    |                                       |
| lowBattery       | [system.low-battery](#channels)     | Switch    |                                       |


#### Configuration Options

*   deviceId - Device Id
    *   Sensor Id. Example 2983

*   subType - Sub Type
    *   Specifies device sub type.

        *   WIND1 - WTGR800
        *   WIND2 - WGR800
        *   WIND3 - STR918, WGR918, WGR928
        *   WIND4 - TFA
        *   WIND5 - UPM WDS500
        *   WIND6 - WS2300
        *   WIND7 - Alecto WS4500, Auriol H13726, Hama EWS1500, Meteoscan W155/W160, Ventus WS155
