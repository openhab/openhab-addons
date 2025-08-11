# Broadlink Binding

This binding supports sending infrared (IR) and radio frequency (RF) commands using a range of devices for made by (and occasionally OEM licensed from) [Broadlink](https://www.ibroadlink.com/).

The highlevel overview is:

1. learn codes from your existing IR (and RF) remote controls, associating each of them with a command that you choose such as `AC_OFF`;
1. making openHAB rules that send the command e.g. `AC_OFF` to the device;
1. the binding will look up the learned code associated with your command and then send it.

## Supported Things

| Thing Type | Device                                                                        |
|------------|-------------------------------------------------------------------------------|
| a1         | Broadlink A1 multi sensor                                                     |
| mp1        | Broadlink MP1 WiFi Smart Power Strip (4 sockets)                              |
| mp1-1k3s2u | Broadlink MP1 1K3S2U WiFi Smart Power Strip (3 sockets, 2 USB)                |
| mp2        | Broadlink MP2 WiFi Smart Power Strip (3 sockets, 3 USB)                       |
| sp1        | Broadlink SP1 WiFi Smart Socket                                               |
| sp2        | Broadlink SP2 WiFi Smart Socket with night light                              |
| sp2-s      | OEM SP2 Mini WiFi Smart Socket with night light                               |
| sp3        | Broadlink SP3/Mini WiFi Smart Socket with night light                         |
| sp3-s      | Broadlink SP3s WiFi Smart Socket with Power Meter                             |
| rm-pro     | Broadline RM Pro WiFi IR/RF Transmitter with temperature sensor               |
| rm3        | Broadlink RM3/Mini WiFi IR Transmitter                                        |
| rm3-q      | Broadlink RM3 WiFi IR Transmitter with Firmware v44057                        |
| rm4-pro    | Broadlink RM4 Pro WiFi RF/IR Transmitter with temperature and humidity sensors|
| rm4-mini   | Broadlink RM4 mini WiFi IR Transmitter                                        |

## Discovery

Devices in the above list that are set up and working in the Broadlink mobile app should be discoverable by initiating a discovery from the openHAB UI.

> The `Lock Device` setting must be switched off for your device via the Broadlink app to be discoverable in openHAB.

## Thing Configuration

| Name                | Type    | Default       | Description                                                                       |
|---------------------|---------|---------------|-----------------------------------------------------------------------------------|
| ipAddress           | String  |               | Sets the IP address of the Broadlink device                                       |
| staticIp            | Boolean | true          | Enabled if your broadlink device has a Static IP set                              |
| port                | Integer | 80            | The network port for the device                                                   |
| macAddress          | String  |               | The device's MAC Address                                                          |
| pollingInterval     | Integer | 30            | The interval in seconds for polling the status of the device                      |
| nameOfCommandToLearn| String  | DEVICE_ON     | The name of the IR or RF command to learn when using the learn command channel    |

## Channels

These are the channels that are available, depending on the device.

| Channel           | Supported Devices        | Type                 | Description                                     |
|-------------------|--------------------------|----------------------|-------------------------------------------------|
| power-on          | MP2, all SPx             | Switch               | Power on/off for switches/strips                |
| night-light       | SP3                      | Switch               | Night light on/off                              |
| temperature       | A1, RM Pro, RM4          | Number:Temperature   | Temperature                                     |
| humidity          | A1, RM4                  | Number:Dimensionless | Air humidity percentage                         |
| noise             | A1                       | String               | Noise level: `QUIET`/`NORMAL`/`NOISY`/`EXTREME` |
| light             | A1                       | String               | Light level: `DARK`/`DIM`/`NORMAL`/`BRIGHT`     |
| air               | A1                       | String               | Air quality: `PERFECT`/`GOOD`/`NORMAL`/`BAD`    |
| power-on-s1       | MP1, MP1_1k3s2u          | Switch               | Socket 1 power                                  |
| power-on-s2       | MP1, MP1_1k3s2u          | Switch               | Socket 2 power                                  |
| power-on-s3       | MP1, v_1k3s2u            | Switch               | Socket 3 power                                  |
| power-on-s4       | MP1                      | Switch               | Socket 4 power                                  |
| power-on-usb      | MP1_1k3s2u               | Switch               | USB power                                       |
| power-consumption | MP2, SP2s,SP3s           | Number:Power         | Power consumption                               |
| command           | all RMx                  | String               | IR Command code to transmit                     |
| rf-command        | RM Pro, RM4 Pro          | String               | RF Command code to transmit                     |
| learning-control  | all RMx                  | String               | Learn mode command channel (see below)          |

Note that there are different channels for sending IR and RF codes.

## Learning Remote Codes

Associate remote codes with your commands, you can get this binding to put your Broadlink RMx device into "learn mode".
Here are the steps:

1. In the openHAB web UI, navigate to your RMx Thing
1. Set the _Name of IR/RF command to learn_ property to the command you want the device to learn. This can be any text that you want, but you will use it in your rules to send commands, so for example 'AC_OFF' or 'TV_VOLUME_UP' would be good commands.
1. Click on its _Channels_ tab
1. For IR find the _Remote Learning Control_ channel and create an Item for it, for RF use the _Remote RF Learning Control_ channel instead. (Only needed the first time)
1. Click the item, and click the rectangular area that is marked NULL
1. In the pop-up menu that appears, select _Learn IR command_ for IR or _Learn RF command_ for RF
1. _The LED on your RM device will illuminate solidly_
1. Point your IR/RF remote control at your RM device and keep pressing the button you'd like to learn. For RF, this can take 10-30 seconds
1. _The LED on your RM device will extinguish once it has identified the command_
1. If the command has been identified succesfully, the channel will have changed it name to "Learn command" or _RF command learnt_
1. If no success, the channel will be named "NULL". Look in the logs on your openHAB server for any issues - try under the Developer Tools section of Main UI.
1. Check and save the IR/RF command by clicking the item once more and select "Check and save command"
1. Keep pressing the remote control with the command to check and save
1. If successfull, the channel will change name to the command saved
1. If no success, the channel be named "NULL", restart from step 3.

### Sending Remote Codes

In a rule, send your command to a String-type Item linked to the command (IR) or rf-command (RF) channel of your device. The binding will look up the learned IR or RF remote code you associated with that command and send it.

### Modify or Delete Remote Codes

The binding is also capable of modifying a previously stored code, and to delete a previously stored code.

To modify a previously stored code, the procedure is the same as the one shown above, except that in step 4, the option to choose is _Modify IR command_ or _Modify RF Command_

**Note**: The "Learn command" will not modify a previously existent code, and the "Modify" command will not create a new command.
This is done to avoid accidentally overwriting commands

In order to delete a previously stored code, the procedure is as follows:

1. In the openHAB web UI, navigate to your RMx Thing
1. Set the _Name of IR/RF command to learn_ property to the name of the command you want the device to learn
1. Click on its _Channels_ tab
1. For IR find the _Remote Learning Control_ channel and create an Item for it, for RF use the _Remote RF Learning Control_ channel instead (Only needed the first time).
1. Click the item, and click the rectangular area that is marked NULL
1. In the pop-up menu that appears, select _Delete IR command_ for IR or _Delete RF command_ for RF

**VERY IMPORTANT NOTE**: As of openHAB version 4.3.0, writing the codes into the files is handled by openHAB. While it is possible to create a file externally, copy it in the proper location and use it as a remote codes database (As it is done in the case of Remote codes file migration) IT IS STRONGLY DISCOURAGED to modify the file while the binding is acive. Please make sure the binding is stopped before you modify a remote codes file manually. Also, have the following things in mind:

- openHAB does not interpret a missing code file as empty. It will assume the file is corrupt and try to read from one of the backups, which can lead to confusion. if you want to empty your code file, create an empty file with a set of culry brackets, one per line
- Remember if you manipulate the code file manually, remember to provide the proper location, and the proper ownership and permissions (Location is `$OPENHAB_USERDATA`, and permissions are `-rw-r--r-- 1 openhab openhab`

## Full Example

Items file example; `sockets.items`:

```java
Switch BroadlinkSP3 "Christmas Lights" [ "Lighting" ] { channel="broadlink:sp3:34-ea-34-22-44-66:power-on" } 
```

Thing file example; `rm.things`:

```java
Thing broadlink:rm4pro:IR_Downstairs "RM 4 Pro IR controller"  [ macAddress="e8:16:56:1c:7e:b9", ipAddress="192.168.178.234" ]
Thing broadlink:rm3q:IR_Upstairs "RM 3 IR controller"  [ macAddress="24:df:a7:df:0d:53", ipAddress="192.168.178.232" ]
```

Items file example; `rm.items`:

```java
Switch DownstairsAC

Number:Temperature DownstairsTemperature "Temperature downstairs" <temperature> ["Temperature", "Measurement"] { channel="broadlink:rm4pro:IR_Downstairs:temperature", unit="°C", stateDescription=" " [pattern="%.1f %unit%"]} 
Number:Dimensionless DownstairsHumidity "Humidity downstairs" <humidity> { channel="broadlink:rm4pro:IR_Downstairs:humidity", unit="%" }

String IR_Downstairs "Downstairs IR control" { channel="broadlink:rm4pro:IR_Downstairs:command" }
```

Rule file example; `AC.rules`:

```java
rule " AC_Control started"
when
  System started
then
  DownstairsAC.sendCommand(OFF)
  IR_Downstairs.sendCommand("AC_OFF")
end

rule "Downstairs AC Off"
when
  Item DownstairsAC changed to OFF
then
  IR_Downstairs.sendCommand("AC_OFF")
end

rule "Downstairs AC On"
when
  Item DownstairsAC changed to ON
then
  IR_Downstairs.sendCommand("AC_ON")
end
```

This rule file assumes you previously have learned the "AC_ON" and "AC_OFF" IR commands.

## Migrating Legacy Map File

Up to openHAB version 3.3, there was a previous version of this binding that was not part of the openHAB distribution.
It stored the IR/RF commands in a different place and a different format.
If you want to mirgrate from those versions to this version of the binding, please read this section.

The map file contains a list of IR command codes to send via the device; there is a separate map file for RF codes.

### openHAB < 4.3.0

Before openHAB version 4.3.0, the file used the [Java Properties File format](https://en.wikipedia.org/wiki/.properties) and was stored in the `<OPENHAB_CONF>/transform` folder.
By default, the file name was `broadlink.map` for the IR codes, but could be changed using the `mapFile` setting.
In similar fashion, the RM pro models stored the RF codes in the `broadlinkrf.map` file.

Here is a map file example of the previous file format:

```text
TV_POWER=26008c0092961039103a1039101510151014101510151039103a10391015101411141015101510141139101510141114101510151014103a10141139103911391037123a10391000060092961039103911391014111410151015101411391039103a101411141015101510141015103911141015101510141015101510391015103911391039103a1039103911000d05000000000000000000000000
heatpump_off=2600760069380D0C0D0C0D290D0C0D290D0C0D0C0D0C0D290D290D0C0D0C0D0C0D290D290D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D290D290D290D290D290D290E0002900000
```

The above codes are power on/off for Samsung TVs and Power Off for a Fujitsu heat pump.
To send either code, the command string `TV_POWER` or `heatpump_off` must be sent to the `command` channel for the device.
For RF, the `rf-command` channel is used.

### openHAB >= 4.3.0

Since openHAB version 4.3.0, codes are stored stored in the `$OPENHAB_USERDATA/jsondb` directory. IR codes are stored in `$OPENHAB_USERDATA/jsondb/broadlink_ir.json`. For the RM Pro series of devices the RF codes are stored in `$OPENHAB_USERDATA/jsondb/broadlink_rf.json`

The advantage of this change is that the files are now backed up by openHAB, which is more practical for migrations, data robustness, etc. Having the storage of the codes handled by openHAB also provides uniformity with other openHAB configuration through Main UI.

With the change of the storage mechanism, the files are also changing format, and codes are now stored in json, like this:

```json
{
  "TV_POWER": {
    "class": "java.lang.String",
    "value": "26008c0092961039103a1039101510151014101510151039103a10391015101411141015101510141139101510141114101510151014103a10141139103911391037123a10391000060092961039103911391014111410151015101411391039103a101411141015101510141015103911141015101510141015101510391015103911391039103a1039103911000d05000000000000000000000000"
  },
  "heatpump_off": {
    "class": "java.lang.String",
    "value": "2600760069380D0C0D0C0D290D0C0D290D0C0D0C0D0C0D290D290D0C0D0C0D0C0D290D290D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D290D290D290D290D290D290E0002900000"
  }
}
```

### Migrating from openHAB < 4.3.0 to > 4.3.0

Below is a Python script that can be used to convert from the old format to the new one:

```python
import csv
import json
import sys
import argparse

parser=argparse.ArgumentParser(description= "Broadlink converter argument parser")
parser.add_argument('-i','--input_filename', help='Input File Name', required=True)
parser.add_argument('-o','--output_filename', help='Output File Name')
args=parser.parse_args()

result={}
with open(args.input_filename,'r') as f:
    red=csv.reader(f, delimiter='=')
    for d in red:
        result[d[0]] = { "class": "java.lang.String" , "value":d[1]}
if args.output_filename:
    with open(args.output_filename, 'w', encoding='utf-8') as f:
        json.dump(result, f, ensure_ascii=False, indent=2)
else:
    print(json.dumps(result,indent=2))
```

## Credits

- [Cato Sognen](https://community.openhab.org/u/cato_sognen)
- [JAD](http://www.javadecompilers.com/jad) (Java Decompiler)
- [Ricardo](https://github.com/rlarranaga)
