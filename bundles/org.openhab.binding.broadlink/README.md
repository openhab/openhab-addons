# Broadlink Binding

This binding supports a range of home networking devices made by (and occasionally OEM licensed from) [Broadlink](https://www.ibroadlink.com/).

## Supported Things

| Thing ID   | Description                                                             |
|------------|-------------------------------------------------------------------------|
| a1         | Broadlink A1 multi sensor                                               |
| mp1        | Broadlink MP1 WiFi Smart Power Strip (4 sockets)                        |
| mp1_1k3s2u | Broadlink MP1 1K3S2U WiFi Smart Power Strip (3 sockets, 2 USB)          |
| mp2        | Broadlink MP2 WiFi Smart Power Strip (3 sockets, 3 USB)                 |
| sp1        | Broadlink SP1 WiFi Smart Socket                                         |
| sp2        | Broadlink SP2 WiFi Smart Socket with night light                        |
| sp3        | Broadlink SP3/Mini WiFi Smart Socket with night light                   |
| sp3s       | Broadlink SP3s WiFi Smart Socket with Power Meter                       |
| rm2        | Broadline RM2/Pro WiFi IR/RF Transmitter with temperature sensor        |
| rm3        | Broadlink RM3/Mini WiFi IR Transmitter                                  |
| rm3q       | Broadlink RM3 WiFi IR Transmitter with Firmware v44057                  |
| rm4        | Broadlink RM4 WiFi IR Transmitter with temperature and humidity sensors |

## Discovery

Devices in the above list that are set up and working in the Broadlink mobile app should be discoverable by initiating a discovery from the openHAB UI.

> The `Lock Device` setting must be switched off for your device via the Broadlink app to be discoverable in openHAB.

## Thing Configuration

| Name                | Type    | Default       | description                                                                       |
|---------------------|---------|---------------|-----------------------------------------------------------------------------------|
| ipAddress           | String  |               | Sets the IP address of the Broadlink device                                       |
| staticIp            | Boolean | true          | Enabled if your broadlink device has a Static IP set                              |
| port                | Integer | 80            | The network port for the device                                                   |
| macAddress          | String  |               | The device's MAC Address                                                           |
| pollingInterval     | Integer | 30            | The interval in seconds for polling the status of the device                      |
| ignoreFailedUpdates | Boolean | false         | If enabled, failed status requests won't put the device `OFFLINE`                       |
| mapFilename         | String  | broadlink.map | The map file that contains remote codes to send via IR                            |

> The `mapFilename` setting is applicable to the RM series of devices only.

## Channels

| Channel          | Supported Devices        | Type                 | Description                                     |
|------------------|--------------------------|----------------------|-------------------------------------------------|
| powerOn          | MP2, all SPx             | Switch               | Power on/off for switches/strips                |
| nightLight       | SP3                      | Switch               | Night light on/off                              |
| temperature      | A1, RM2, RM4             | Number:Temperature   | Temperature                                     |
| humidity         | A1, RM4                  | Number:Dimensionless | Air humidity percentage                         |
| noise            | A1                       | String               | Noise level: `QUIET`/`NORMAL`/`NOISY`/`EXTREME` |
| light            | A1                       | String               | Light level: `DARK`/`DIM`/`NORMAL`/`BRIGHT`     |
| air              | A1                       | String               | Air quality: `PERFECT`/`GOOD`/`NORMAL`/`BAD`    |
| s1powerOn        | MP1, MP1_1k3s2u          | Switch               | Socket 1 power                                  |
| s2powerOn        | MP1, MP1_1k3s2u          | Switch               | Socket 2 power                                  |
| s3powerOn        | MP1, v_1k3s2u            | Switch               | Socket 3 power                                  |
| s4powerOn        | MP1                      | Switch               | Socket 4 power                                  |
| usbPowerOn       | MP1_1k3s2u               | Switch               | USB power                                       |
| powerConsumption | MP2, SP2s,SP3s           | Number:Power         | Power consumption                               |
| command          | all RMx                  | String               | IR Command code to transmit                     |
| learningControl  | all RMx                  | String               | Learn mode command channel (see below)          |

## Map File

The Broadlink RM family of devices can transmit IR codes.
The map file contains a list of IR command codes to send via the device.
The file uses the [Java Properties File format](https://en.wikipedia.org/wiki/.properties) and is stored in the `<OPENHAB_CONF>/transform` folder.
By default, the file name is `broadlink.map` but can be changed using the `mapFile` setting.

Here is a map file example:

```
TV_POWER=26008c0092961039103a1039101510151014101510151039103a10391015101411141015101510141139101510141114101510151014103a10141139103911391037123a10391000060092961039103911391014111410151015101411391039103a101411141015101510141015103911141015101510141015101510391015103911391039103a1039103911000d05000000000000000000000000
heatpump_off=2600760069380D0C0D0C0D290D0C0D290D0C0D0C0D0C0D290D290D0C0D0C0D0C0D290D290D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D290D290D290D290D290D290E0002900000

```

The above codes are power on/off for Samsung TVs and Power Off for a Fujitsu heat pump.
To send either code, the string `TV_POWER` or `heatpump_off` must be sent to the `command` channel for the device.

## Learning new remote codes

To obtain the command codes, you can get this binding to put your Broadlink RMx device into 
"learn mode" and then ask it for the code it learnt. Here are the steps:

1. In the openHAB web UI, navigate to your RMx Thing, and click on its *Channels* tab
2. Find the *Remote Learning Control* channel and create an Item for it
3. Click the item, and click the rectangular area that is marked NULL
4. In the pop-up menu that appears, select "Enter infrared learn mode"
5. *The LED on your RM device will illuminate solidly*
6. Point your IR remote control at your RM device and press the button you'd like to learn
7. *The LED on your RM device will extinguish once it has learnt the command*
8. Now click the rectangular area again (which will now show "Enter infrared learn mode")
9. Select the "Check last captured IR code" menu option in the pop-up menu
10. Inspect the `openhab.log` file on your openHAB server - you should see the following:

```
[BroadlinkRemoteHandler] - BEGIN LAST LEARNT CODE (976 bytes)
[BroadlinkRemoteHandler] - 2600bc017239100d0e2b0e0f100c107f55747be51a3e1d4ff4......f5be8b3d4ff4b
4d77c44d105fa530546becaa2bcfbd348b30145447f55747be51a3747be51a3e1d4ff4b3f4f4f......a3e1d4ff4b348
0145447f55747be51a3e1d4ff4b
[BroadlinkRemoteHandler] - END LAST LEARNT CODE (1944 characters)
```

11. If you carefully copy the log line between the `BEGIN` and `END` messages, 
    it should have exactly the number of characters as advised in the `END` message.
    
12. You can now paste a new entry into your `map` file, with the name of your choice; for example:

```
BLURAY_ON=2600bc017239100d0e2b0e0f100c107f55747be51a3e1d4ff4...0145447f55747be51a3e1d4ff4b
```


## Full Example

Items file example; `sockets.items`:

```
Switch BroadlinkSP3 "Christmas Lights" [ "Lighting" ] { channel="broadlink:sp3:34-ea-34-22-44-66:powerOn" } 
```

## Credits

- [Cato Sognen](https://community.openhab.org/u/cato_sognen)
- [JAD](http://www.javadecompilers.com/jad) (Java Decompiler)

