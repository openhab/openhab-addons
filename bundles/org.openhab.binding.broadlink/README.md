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
| rm         | Broadlink RM WiFI IR Transmitter                                        |
| rm2        | Broadline RM2/Pro WiFi IR/RF Transmitter with temperature sensor        |
| rm3        | Broadlink RM3/Mini WiFi IR Transmitter                                  |
| rm3q       | Broadlink RM3 WiFi IR Transmitter with Firmware v44057                  |
| rm4        | Broadlink RM4 WiFi IR Transmitter with temperature and humidity sensors |

## Discovery

Devices in the above list that are set up and working in the Broadlink mobile app should be discoverable by initiating a discovery from the OpenHAB UI. 

> The `Lock Device` setting must be switched off for your device via the Broadlink app to be discoverable in openHAB.

## Thing Configuration

| Name                | Type    | Default       | description                                                                       |
|---------------------|---------|---------------|-----------------------------------------------------------------------------------|
| ipAddress           | String  |               | Sets the IP address of the Broadlink device                                       |
| staticIp            | Boolean | true          | Enabled if your broadlink device has a Static IP set                              |
| port                | Integer | 80            | The network port for the device                                                   |
| mac                 | String  |               | The devices MAC Address                                                           |
| pollingInterval     | Integer | 30            | The interval in seconds for polling the status of the device                      |
| retries             | Integer | 1             | The number of re-attempts for a request before the device is considered `OFFLINE` |
| ignoreFailedUpdates | Boolean | false         | Is enabled, failed status requests put the device `OFFLINE`                       |
| mapFilename         | String  | broadlink.map | The map file that contains remote codes to send via IR                            |

> The `mapFilename` setting is applicable to the RM series of devices only.

## Channels

| Channel          | Supported Devices        | Type   | Description                                     |
|------------------|--------------------------|--------|-------------------------------------------------|
| powerOn          | mp2, sp1, sp2, sp3, sp3s | Switch | Power on/off for switches/strips                |
| nightLight       | sp2, sp3, sp3s           | Switch | Night light on/off                              |
| temperature      | a1, rm2, rm4             | Number | Temperature in degrees Celsius                  |
| humidity         | a1, rm4                  | Number | Air humidity percentage                         |
| noise            | a1                       | String | Noise level: `QUIET`/`NORMAL`/`NOISY`/`EXTREME` |
| light            | a1                       | String | Light level: `DARK`/`DIM`/`NORMAL`/`BRIGHT`     |
| air              | a1                       | String | Air quality: `PERFECT`/`GOOD`/`NORMAL`/`BAD`    |
| s1powerOn        | mp1, mp1_1k3s2u          | Switch | Socket 1 power                                  |
| s2powerOn        | mp1, mp1_1k3s2u          | Switch | Socket 2 power                                  |
| s3powerOn        | mp1, mp1_1k3s2u          | Switch | Socket 3 power                                  |
| s4powerOn        | mp1                      | Switch | Socket 4 power                                  |
| usbPowerOn       | mp1_1k3s2u               | Switch | USB power                                       |
| powerConsumption | mp2, sp2,sp3s            | Number | Power consumption in Watts                      |
| command          | rm, rm2, rm3, rm3q, rm4  | String | IR Command code to transmit                     |

## Map File

The Broadlink RM family of devices can transmit IR codes.
The map file contains a list of IR command codes to send via the device.
The file uses the *Map Transformation* and is stored in the `<OPENHAB_CONF>/transform` folder.
By default, the file name is `broadlink.map` but can be changed using the `mapFile` setting.

Here is a map file example:

```
TV_POWER=26008c0092961039103a1039101510151014101510151039103a10391015101411141015101510141139101510141114101510151014103a10141139103911391037123a10391000060092961039103911391014111410151015101411391039103a101411141015101510141015103911141015101510141015101510391015103911391039103a1039103911000d05000000000000000000000000
heatpump_off=2600760069380D0C0D0C0D290D0C0D290D0C0D0C0D0C0D290D290D0C0D0C0D0C0D290D290D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D290D0C0D0C0D0C0D0C0D0C0D0C0D290D0C0D290D290D290D290D290D290E0002900000

```

The above codes are power on/off for Samsung TVs and Power Off for a Fujitsu heat pump.
To send either code, the string `TV_POWER` or `heatpump_off` must be sent to the `command` channel for the device.

## Full Example

Items file example; `sockets.items`:

```
Switch BroadlinkSP3 "Christmas Lights" [ "Lighting" ] { channel="broadlink:sp3:34-ea-34-22-44-66:powerOn" } 
```