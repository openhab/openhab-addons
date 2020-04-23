# Xiaomi Mi IO Binding

This binding is used to control Xiaomi products implementing the Mi IO protocol. 
This is a set of wifi devices from Xiaomi that are part of the Mi Ecosystem which is branded as MiJia.

![MIIO logo](doc/miio.png)

## Supported Things

The following things types are available:

| ThingType        | Description                                                                                                              |
|------------------|--------------------------------------------------------------------------------------------------------------------------|
| miio:generic     | Generic type for discovered devices. Once the token is available and the device model is determined, this ThingType will automatically change to the appropriate ThingType |
| miio:vacuum      | For Xiaomi Robot Vacuum products                                                                                         |
| miio:basic       | For several basic devices like yeelights, airpurifiers. Channels and commands are determined by database configuration   |
| miio:unsupported | For experimenting with other devices which use the Mi IO protocol                                                        |

# Discovery

The binding has 2 methods for discovering devices. Depending on your network setup and the device model, your device may be discovered by one or both methods. If both methods discover your device, 2 discovery results may be in your inbox for the same device.

The mDNS discovery method will discover your device type, but will not discover a (required) token.
The basic discovery will not discovery the type, but will discover a token for models that support it.
Accept only one of the 2 discovery results, the alternate one can further be ignored.

## Tokens

The binding needs a token from the Xiaomi Mi Device in order to be able to control it.
The binding can retrieve the needed tokens from the Xiaomi cloud. 
Go to the binding config page and enter your cloud username and password. 
The server(s) to which your devices are connected need to be entered as well. 
Use the one of the regional servers: ru,us,tw,sg,cn,de. Multiple servers can be separated with comma, or leave blank to test all known servers.

## Tokens without cloud access

Some devices provide the token upon discovery. This may depends on the firmware version.
If the device does not discover your token, it needs to be retrieved from the Mi Home app.

The easiest way to obtain tokens is to browse through log files of the Mi Home app version 5.4.49 for Android. 
It seems that version was released with debug messages turned on by mistake. 
An APK file with the old version can be easily found using one of the popular web search engines. 
After downgrading use a file browser to navigate to directory SmartHome/logs/plug_DeviceManager, then open the most recent file and search for the token. When finished, use Google Play to get the most recent version back.

For iPhone, use an un-encrypted iTunes-Backup and unpack it and use a sqlite tool to view the files in it: 
Then search in "RAW, com.xiaomi.home," for "USERID_mihome.sqlite" and look for the 32-digit-token or 96 digit encrypted token.

Note. The Xiaomi devices change the token when inclusion is done. Hence if you get your token after reset and than include it with the Mi Home app, the token will change.

## Binding Configuration

No binding configuration is required. However to enable cloud functionality enter your Xiaomi username, password and server(s)

## Thing Configuration

Each Xiaomi device (thing) needs the IP address and token configured to be able to communicate. See discovery for details.
Optional configuration is the refresh interval and the deviceID. Note that the deviceID is automatically retrieved when it is left blank.
The configuration for model is automatically retrieved from the device in normal operation. 
However, for devices that are unsupported, you may override the value and try to use a model string from a similar device to experimentally use your device with the binding.

| Parameter       | Type    | Required | Description                                                       |
|-----------------|---------|----------|-------------------------------------------------------------------|
| host            | text    | true     | Device IP address                                                 |
| token           | text    | true     | Token for communication (in Hex)                                  |
| deviceId        | text    | true     | Device ID number for communication (in Hex)                       |
| model           | text    | false    | Device model string, used to determine the subtype                |
| refreshInterval | integer | false    | Refresh interval for refreshing the data in seconds. (0=disabled) |
| timeout         | integer | false    | Timeout time in milliseconds                                      |

### Example Thing file

`Thing miio:basic:light "My Light" [ host="192.168.x.x", token="put here your token", deviceId="0326xxxx" ]` 

## Mi IO Devices

| Device                       | ThingType        | Device Model           | Supported | Remark     |
|------------------------------|------------------|------------------------|-----------|------------|
| AUX Air Conditioner          | miio:unsupported | aux.aircondition.v1    | No        |            |
| Idelan Air Conditioner       | miio:unsupported | idelan.aircondition.v1 | No        |            |
| Midea Air Conditioner v2     | miio:unsupported | midea.aircondition.v1  | No        |            |
| Midea Air Conditioner v2     | miio:unsupported | midea.aircondition.v2  | No        |            |
| Midea Air Conditioner xa1    | miio:unsupported | midea.aircondition.xa1 | No        |            |
| Mi Air Monitor v1            | miio:basic       | [zhimi.airmonitor.v1](#zhimi-airmonitor-v1) | Yes       |            |
| Mi Air Quality Monitor 2gen  | miio:basic       | [cgllc.airmonitor.b1](#cgllc-airmonitor-b1) | Yes       |            |
| Mi Air Quality Monitor S1    | miio:basic       | [cgllc.airmonitor.s1](#cgllc-airmonitor-s1) | Yes       |            |
| Mi Air Humidifier            | miio:basic       | [zhimi.humidifier.v1](#zhimi-humidifier-v1) | Yes       |            |
| Mi Air Humidifier            | miio:basic       | [zhimi.humidifier.ca1](#zhimi-humidifier-ca1) | Yes       |            |
| Mi Air Humidifier 2          | miio:basic       | [zhimi.humidifier.cb1](#zhimi-humidifier-cb1) | Yes       |            |
| Mija Smart humidifier        | miio:basic       | [deerma.humidifier.mjjsq](#deerma-humidifier-mjjsq) | Yes       |            |
| Mi Air Purifier v1           | miio:basic       | [zhimi.airpurifier.v1](#zhimi-airpurifier-v1) | Yes       |            |
| Mi Air Purifier v2           | miio:basic       | [zhimi.airpurifier.v2](#zhimi-airpurifier-v2) | Yes       |            |
| Mi Air Purifier v3           | miio:basic       | [zhimi.airpurifier.v3](#zhimi-airpurifier-v3) | Yes       |            |
| Mi Air Purifier v5           | miio:basic       | [zhimi.airpurifier.v5](#zhimi-airpurifier-v5) | Yes       |            |
| Mi Air Purifier Pro v6       | miio:basic       | [zhimi.airpurifier.v6](#zhimi-airpurifier-v6) | Yes       |            |
| Mi Air Purifier Pro v7       | miio:basic       | [zhimi.airpurifier.v7](#zhimi-airpurifier-v7) | Yes       |            |
| Mi Air Purifier 2 (mini)     | miio:basic       | [zhimi.airpurifier.m1](#zhimi-airpurifier-m1) | Yes       |            |
| Mi Air Purifier (mini)       | miio:basic       | [zhimi.airpurifier.m2](#zhimi-airpurifier-m2) | Yes       |            |
| Mi Air Purifier MS1          | miio:basic       | [zhimi.airpurifier.ma1](#zhimi-airpurifier-ma1) | Yes       |            |
| Mi Air Purifier MS2          | miio:basic       | [zhimi.airpurifier.ma2](#zhimi-airpurifier-ma2) | Yes       |            |
| Mi Air Purifier 3            | miio:basic       | [zhimi.airpurifier.ma4](#zhimi-airpurifier-ma4) | Yes       |            |
| Mi Air Purifier 3            | miio:basic       | [zhimi.airpurifier.mb3](#zhimi-airpurifier-mb3) | Yes       |            |
| Mi Air Purifier Super        | miio:basic       | [zhimi.airpurifier.sa1](#zhimi-airpurifier-sa1) | Yes       |            |
| Mi Air Purifier Super 2      | miio:basic       | [zhimi.airpurifier.sa2](#zhimi-airpurifier-sa2) | Yes       |            |
| Mi Fresh Air Ventilator      | miio:basic       | [dmaker.airfresh.t2017](#dmaker-airfresh-t2017) | Yes       |            |
| Mi Fresh Air Ventilator A1   | miio:basic       | [dmaker.airfresh.a1](#dmaker-airfresh-a1) | Yes       |            |
| Xiao AI Smart Alarm Clock    | miio:unsupported | zimi.clock.myk01       | No        |            |
| Yeelight Smart Bath Heater   | miio:unsupported | yeelight.bhf_light.v2  | No        |            |
| Gosund Plug                  | miio:basic       | [cuco.plug.cp1](#cuco-plug-cp1) | Yes       |            |
| XIAOMI MIJIA WIDETECH WDH318EFW1 Dehumidifier | miio:unsupported | nwt.derh.wdh318efw1    | No        |            |
| Mi Air Purifier mb1          | miio:basic       | [zhimi.airpurifier.mb1](#zhimi-airpurifier-mb1) | Yes       |            |
| Mi Air Purifier 2S           | miio:basic       | [zhimi.airpurifier.mc1](#zhimi-airpurifier-mc1) | Yes       |            |
| Mi Air Purifier virtual      | miio:unsupported | zhimi.airpurifier.virtual | No        |            |
| Mi Air Purifier vtl m1       | miio:unsupported | zhimi.airpurifier.vtl_m1 | No        |            |
| Mi Remote v2                 | miio:unsupported | chuangmi.ir.v2         | No        |            |
| Xiaomi IR Remote             | miio:unsupported | chuangmi.remote.v2     | No        |            |
| MiJia Rice Cooker            | miio:unsupported | chunmi.cooker.normal1  | No        |            |
| MiJia Rice Cooker            | miio:unsupported | chunmi.cooker.normal2  | No        |            |
| MiJia Rice Cooker            | miio:unsupported | hunmi.cooker.normal3   | No        |            |
| MiJia Rice Cooker            | miio:unsupported | chunmi.cooker.normal4  | No        |            |
| MiJia Heating Pressure Rice Cooker | miio:unsupported | chunmi.cooker.press1   | No        |            |
| MiJia Heating Pressure Rice Cooker | miio:unsupported | chunmi.cooker.press2   | No        |            |
| Mi Smart Fan                 | miio:basic       | [zhimi.fan.v1](#zhimi-fan-v1) | Yes       |            |
| Mi Smart Fan                 | miio:basic       | [zhimi.fan.v2](#zhimi-fan-v2) | Yes       |            |
| Mi Smart Pedestal Fan        | miio:basic       | [zhimi.fan.v3](#zhimi-fan-v3) | Yes       |            |
| Xiaomi Mi Smart Pedestal Fan | miio:basic       | [zhimi.fan.sa1](#zhimi-fan-sa1) | Yes       |            |
| Xiaomi Mi Smart Pedestal Fan | miio:basic       | [zhimi.fan.za1](#zhimi-fan-za1) | Yes       |            |
| Viomi Internet refrigerator iLive | miio:unsupported | viomi.fridge.v3        | No        |            |
| Mi Smart Home Gateway v1     | miio:unsupported | lumi.gateway.v1        | No        |            |
| Mi Smart Home Gateway v2     | miio:unsupported | lumi.gateway.v2        | No        |            |
| Mi Smart Home Gateway v3     | miio:unsupported | lumi.gateway.v3        | No        |            |
| Mi Humdifier                 | miio:basic       | [zhimi.humidifier.v1](#zhimi-humidifier-v1) | Yes       |            |
| Light Control (Wall Switch)  | miio:unsupported | lumi.ctrl_neutral1.v1  | No        |            |
| Light Control (Wall Switch)  | miio:unsupported | lumi.ctrl_neutral2.v1  | No        |            |
| Xiaomi Philips Eyecare Smart Lamp 2 | miio:basic       | [philips.light.sread1](#philips-light-sread1) | Yes       |            |
| Xiaomi Philips LED Ceiling Lamp | miio:basic       | [philips.light.ceiling](#philips-light-ceiling) | Yes       |            |
| Xiaomi Philips LED Ceiling Lamp | miio:basic       | [philips.light.zyceiling](#philips-light-zyceiling) | Yes       |            |
| Xiaomi Philips Bulb          | miio:basic       | [philips.light.bulb](#philips-light-bulb) | Yes       |            |
| PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp | miio:basic       | [philips.light.candle](#philips-light-candle) | Yes       |            |
| Xiaomi Philips Downlight     | miio:basic       | [philips.light.downlight](#philips-light-downlight) | Yes       |            |
| Xiaomi Philips ZhiRui bedside lamp | miio:basic       | [philips.light.moonlight](#philips-light-moonlight) | Yes       |            |
| Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal | miio:basic       | [philips.light.candle2](#philips-light-candle2) | Yes       |            |
| philips.light.mono1          | miio:basic       | [philips.light.mono1](#philips-light-mono1) | Yes       |            |
| philips.light.virtual        | miio:basic       | [philips.light.virtual](#philips-light-virtual) | Yes       |            |
| philips.light.zysread        | miio:basic       | [philips.light.zysread](#philips-light-zysread) | Yes       |            |
| philips.light.zystrip        | miio:basic       | [philips.light.zystrip](#philips-light-zystrip) | Yes       |            |
| Mi Power-plug                | miio:basic       | [chuangmi.plug.m1](#chuangmi-plug-m1) | Yes       |            |
| Mi Power-plug v1             | miio:basic       | [chuangmi.plug.v1](#chuangmi-plug-v1) | Yes       |            |
| Mi Power-plug v2             | miio:basic       | [chuangmi.plug.v2](#chuangmi-plug-v2) | Yes       |            |
| Mi Power-plug v3             | miio:basic       | [chuangmi.plug.v3](#chuangmi-plug-v3) | Yes       |            |
| Mi Power-plug                | miio:basic       | [chuangmi.plug.m3](#chuangmi-plug-m3) | Yes       |            |
| Mi Smart Plug                | miio:basic       | [chuangmi.plug.hmi205](#chuangmi-plug-hmi205) | Yes       |            |
| Qing Mi Smart Power Strip v1 | miio:basic       | [qmi.powerstrip.v1](#qmi-powerstrip-v1) | Yes       |            |
| Mi Power-strip v2            | miio:basic       | [zimi.powerstrip.v2](#zimi-powerstrip-v2) | Yes       |            |
| Mi Toothbrush                | miio:unsupported | soocare.toothbrush.x3  | No        |            |
| Mi Robot Vacuum              | miio:vacuum      | [rockrobo.vacuum.v1](#rockrobo-vacuum-v1) | Yes       |            |
| Mi Xiaowa Vacuum c1          | miio:vacuum      | [roborock.vacuum.c1](#roborock-vacuum-c1) | Yes       |            |
| Mi Robot Vacuum v2           | miio:vacuum      | [roborock.vacuum.s5](#roborock-vacuum-s5) | Yes       |            |
| Mi Robot Vacuum 1S           | miio:vacuum      | [roborock.vacuum.m1s](#roborock-vacuum-m1s) | Yes       |            |
| Mi Robot Vacuum S4           | miio:vacuum      | [roborock.vacuum.s4](#roborock-vacuum-s4) | Yes       |            |
| Roborock Vacuum S4v2         | miio:vacuum      | [roborock.vacuum.s4v2](#roborock-vacuum-s4v2) | Yes       |            |
| Roborock Vacuum T6           | miio:vacuum      | [roborock.vacuum.t6](#roborock-vacuum-t6) | Yes       |            |
| Roborock Vacuum T6 v2        | miio:vacuum      | [roborock.vacuum.t6v2](#roborock-vacuum-t6v2) | Yes       |            |
| Roborock Vacuum T6 v3        | miio:vacuum      | [roborock.vacuum.t6v3](#roborock-vacuum-t6v3) | Yes       |            |
| Roborock Vacuum T4           | miio:vacuum      | [roborock.vacuum.t4](#roborock-vacuum-t4) | Yes       |            |
| Roborock Vacuum T4 v2        | miio:vacuum      | [roborock.vacuum.t4v2](#roborock-vacuum-t4v2) | Yes       |            |
| Roborock Vacuum T4 v3        | miio:vacuum      | [roborock.vacuum.t4v3](#roborock-vacuum-t4v3) | Yes       |            |
| Roborock Vacuum T7           | miio:vacuum      | [roborock.vacuum.t7](#roborock-vacuum-t7) | Yes       |            |
| Roborock Vacuum T7 v2        | miio:vacuum      | [roborock.vacuum.t7v2](#roborock-vacuum-t7v2) | Yes       |            |
| Roborock Vacuum T7 v3        | miio:vacuum      | [roborock.vacuum.t7v3](#roborock-vacuum-t7v3) | Yes       |            |
| Roborock Vacuum T7p          | miio:vacuum      | [roborock.vacuum.t7p](#roborock-vacuum-t7p) | Yes       |            |
| Roborock Vacuum T7 v2        | miio:vacuum      | [roborock.vacuum.t7pv2](#roborock-vacuum-t7pv2) | Yes       |            |
| Roborock Vacuum T7 v3        | miio:vacuum      | [roborock.vacuum.t7pv3](#roborock-vacuum-t7pv3) | Yes       |            |
| Roborock Vacuum S5 Max       | miio:vacuum      | [roborock.vacuum.s5e](#roborock-vacuum-s5e) | Yes       |            |
| Roborock Vacuum S6           | miio:vacuum      | [rockrobo.vacuum.s6](#rockrobo-vacuum-s6) | Yes       |            |
| Roborock Vacuum S6           | miio:vacuum      | [roborock.vacuum.s6](#roborock-vacuum-s6) | Yes       |            |
| Rockrobo Xiaowa Vacuum v2    | miio:unsupported | roborock.vacuum.e2     | No        |            |
| Xiaomi Mijia vacuum V-RVCLM21B | miio:unsupported | viomi.vacuum.v6        | No        |            |
| Xiaomi Mijia vacuum STYJ02YM | miio:unsupported | viomi.vacuum.v7        | No        |            |
| Vacuum 1C STYTJ01ZHM         | miio:basic       | [dreame.vacuum.mc1808](#dreame-vacuum-mc1808) | Yes       |            |
| roborock.vacuum.c1           | miio:unsupported | roborock.vacuum.c1     | No        |            |
| Rockrobo Xiaowa Sweeper v2   | miio:unsupported | roborock.sweeper.e2v2  | No        |            |
| Rockrobo Xiaowa Sweeper v3   | miio:unsupported | roborock.sweeper.e2v3  | No        |            |
|  Mijia 1 Gang Wall Smart Switch (WIFI) - PTX switch | miio:basic       | [090615.switch.xswitch01](#090615-switch-xswitch01) | Yes       |            |
|  Mijia 2 Gang Wall Smart Switch (WIFI) - PTX switch | miio:basic       | [090615.switch.xswitch02](#090615-switch-xswitch02) | Yes       |            |
|  Mijia 3 Gang Wall Smart Switch (WIFI) - PTX switch | miio:basic       | [090615.switch.xswitch03](#090615-switch-xswitch03) | Yes       |            |
| Mi Water Purifier v2         | miio:basic       | [yunmi.waterpuri.v2](#yunmi-waterpuri-v2) | Yes       |            |
| Mi Water Purifier lx2        | miio:basic       | [yunmi.waterpuri.lx2](#yunmi-waterpuri-lx2) | Yes       |            |
| Mi Water Purifier lx3        | miio:basic       | [yunmi.waterpuri.lx3](#yunmi-waterpuri-lx3) | Yes       |            |
| Mi Water Purifier lx4        | miio:basic       | [yunmi.waterpuri.lx4](#yunmi-waterpuri-lx4) | Yes       |            |
| Mi Water Purifier v2         | miio:basic       | [yunmi.waterpurifier.v2](#yunmi-waterpurifier-v2) | Yes       |            |
| Mi Water Purifier v3         | miio:basic       | [yunmi.waterpurifier.v3](#yunmi-waterpurifier-v3) | Yes       |            |
| Mi Water Purifier v4         | miio:basic       | [yunmi.waterpurifier.v4](#yunmi-waterpurifier-v4) | Yes       |            |
| Xiaomi Wifi Extender         | miio:unsupported | xiaomi.repeater.v2     | No        |            |
| Mi Internet Speaker          | miio:unsupported | xiaomi.wifispeaker.v1  | No        |            |
| Yeelight Lamp                | miio:basic       | [yeelink.light.bslamp1](#yeelink-light-bslamp1) | Yes       |            |
| Yeelight Lamp                | miio:basic       | [yeelink.light.bslamp2](#yeelink-light-bslamp2) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling1](#yeelink-light-ceiling1) | Yes       |            |
| Yeelight LED Ceiling Lamp v2 | miio:basic       | [yeelink.light.ceiling2](#yeelink-light-ceiling2) | Yes       |            |
| Yeelight LED Ceiling Lamp v3 | miio:basic       | [yeelink.light.ceiling3](#yeelink-light-ceiling3) | Yes       |            |
| Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB) | miio:basic       | [yeelink.light.ceiling4](#yeelink-light-ceiling4) | Yes       |            |
| Yeelight LED Ceiling Lamp v4 | miio:basic       | [yeelink.light.ceiling4.ambi](#yeelink-light-ceiling4-ambi) | Yes       |            |
| Yeelight LED Ceiling Lamp v5 | miio:basic       | [yeelink.light.ceiling5](#yeelink-light-ceiling5) | Yes       |            |
| Yeelight LED Ceiling Lamp v6 | miio:basic       | [yeelink.light.ceiling6](#yeelink-light-ceiling6) | Yes       |            |
| Yeelight LED Ceiling Lamp v7 | miio:basic       | [yeelink.light.ceiling7](#yeelink-light-ceiling7) | Yes       |            |
| Yeelight LED Ceiling Lamp v8 | miio:basic       | [yeelink.light.ceiling8](#yeelink-light-ceiling8) | Yes       |            |
| Yeelight LED Ceiling Lamp v9 | miio:basic       | [yeelink.light.ceiling9](#yeelink-light-ceiling9) | Yes       |            |
| Yeelight LED Meteorite lamp  | miio:basic       | [yeelink.light.ceiling10](#yeelink-light-ceiling10) | Yes       |            |
| Yeelight LED Ceiling Lamp v11 | miio:basic       | [yeelink.light.ceiling11](#yeelink-light-ceiling11) | Yes       |            |
| Yeelight LED Ceiling Lamp v12 | miio:basic       | [yeelink.light.ceiling12](#yeelink-light-ceiling12) | Yes       |            |
| Yeelight LED Ceiling Lamp v13 | miio:basic       | [yeelink.light.ceiling13](#yeelink-light-ceiling13) | Yes       |            |
| Yeelight ct2                 | miio:basic       | [yeelink.light.ct2](#yeelink-light-ct2) | Yes       |            |
| Yeelight White Bulb          | miio:basic       | [yeelink.light.mono1](#yeelink-light-mono1) | Yes       |            |
| Yeelight White Bulb v2       | miio:basic       | [yeelink.light.mono2](#yeelink-light-mono2) | Yes       |            |
| Yeelight Wifi Speaker        | miio:unsupported | yeelink.wifispeaker.v1 | No        |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp1](#yeelink-light-lamp1) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp2](#yeelink-light-lamp2) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp3](#yeelink-light-lamp3) | Yes       |            |
| Yeelight Strip               | miio:basic       | [yeelink.light.strip1](#yeelink-light-strip1) | Yes       |            |
| Yeelight Strip               | miio:basic       | [yeelink.light.strip2](#yeelink-light-strip2) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.virtual](#yeelink-light-virtual) | Yes       |            |
| Yeelight Color Bulb          | miio:basic       | [yeelink.light.color1](#yeelink-light-color1) | Yes       |            |
| Yeelight Color Bulb YLDP06YL 10W | miio:basic       | [yeelink.light.color2](#yeelink-light-color2) | Yes       |            |
| Yeelight Color Bulb YLDP02YL 9W | miio:basic       | [yeelink.light.color3](#yeelink-light-color3) | Yes       |            |
| Yeelight Bulb YLDP13YL (8,5W) | miio:basic       | [yeelink.light.color4](#yeelink-light-color4) | Yes       |            |


# Advanced: Unsupported devices

Newer devices may not yet be supported.
However, many devices share large similarities with existing devices.
The binding allows to try/test if your new device is working with database files of older devices as well.
For this, first remove your unsupported thing. Manually add a miio:basic thing. 
Besides the regular configuration (like ip address, token) the modelId needs to be provided.
Normally the modelId is populated with the model of your device, however in this case, use the modelId of a similar device.
Look at the openhab forum, or the openhab github repository for the modelId of similar devices.

# Advanced: adding local database files to support new devices

Things using the basic handler (miio:basic things) are driven by json 'database' files.
This instructs the binding which channels to create, which properties and actions are associated with the channels etc.
The conf/misc/miio (e.g. in Linux `/opt/openhab2/conf/misc/miio/`) is scanned for database files and will be used for your devices. 
Note that local database files take preference over build-in ones, hence if a json file is local and in the database the local file will be used. 
For format, please check the current database files in Openhab github.

## Channels

Depending on the device, different channels are available.

All devices have available the following channels (marked as advanced) besides the device specific channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| network#ssid     | String  | Network SSID                        |
| network#bssid    | String  | Network BSSID                       |
| network#rssi     | Number  | Network RSSI                        |
| network#life     | Number  | Network Life                        |
| actions#commands | String  | send commands. see below            |

note: the ADVANCED  `actions#commands` channel can be used to send commands that are not automated via the binding. This is available for all devices
e.g. `smarthome:send actionCommand 'upd_timer["1498595904821", "on"]'` would enable a pre-configured timer. See https://github.com/marcelrv/XiaomiRobotVacuumProtocol for all known available commands.


### Mi Air Monitor v1 (<a name="zhimi-airmonitor-v1">zhimi.airmonitor.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| aqi              | Number  | Air Quality Index                   |
| battery          | Number  | Battery                             |
| usb_state        | Switch  | USB State                           |
| time_state       | Switch  | Time State                          |
| night_state      | Switch  | Night State                         |
| night_begin      | Number  | Night Begin Time                    |
| night_end        | Number  | Night End Time                      |

### Mi Air Quality Monitor 2gen (<a name="cgllc-airmonitor-b1">cgllc.airmonitor.b1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| battery          | Number  | Battery                             |
| pm25             | Number  | PM2.5                               |
| co2              | Number  | CO2e                                |
| tvoc             | Number  | tVOC                                |
| humidity         | Number  | Humidity                            |
| temperature      | Number  | Temperature                         |

### Mi Air Quality Monitor S1 (<a name="cgllc-airmonitor-s1">cgllc.airmonitor.s1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| battery          | Number  | Battery                             |
| pm25             | Number  | PM2.5                               |
| co2              | Number  | CO2                                 |
| tvoc             | Number  | tVOC                                |
| humidity         | Number  | Humidity                            |
| temperature      | Number  | Temperature                         |

### Mi Air Humidifier (<a name="zhimi-humidifier-v1">zhimi.humidifier.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| setHumidity      | Number  | Humidity Set                        |
| aqi              | Number  | Air Quality Index                   |
| translevel       | Number  | Trans_level                         |
| bright           | Number  | LED Brightness                      |
| buzzer           | Switch  | Buzzer Status                       |
| depth            | Number  | Depth                               |
| dry              | Switch  | Dry                                 |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| temperature      | Number  | Temperature                         |
| childlock        | Switch  | Child Lock                          |

### Mi Air Humidifier (<a name="zhimi-humidifier-ca1">zhimi.humidifier.ca1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| setHumidity      | Number  | Humidity Set                        |
| aqi              | Number  | Air Quality Index                   |
| translevel       | Number  | Trans_level                         |
| bright           | Number  | LED Brightness                      |
| buzzer           | Switch  | Buzzer Status                       |
| depth            | Number  | Depth                               |
| dry              | Switch  | Dry                                 |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| temperature      | Number  | Temperature                         |
| childlock        | Switch  | Child Lock                          |

### Mi Air Humidifier 2 (<a name="zhimi-humidifier-cb1">zhimi.humidifier.cb1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| humidifierMode   | String  | Humidifier Mode                     |
| humidity         | Number  | Humidity                            |
| setHumidity      | Number  | Humidity Set                        |
| bright           | Number  | LED Brightness                      |
| buzzer           | Switch  | Buzzer Status                       |
| depth            | Number  | Depth                               |
| dry              | Switch  | Dry                                 |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| temperature      | Number  | Temperature                         |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier v1 (<a name="zhimi-airpurifier-v1">zhimi.airpurifier.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier v2 (<a name="zhimi-airpurifier-v2">zhimi.airpurifier.v2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier v3 (<a name="zhimi-airpurifier-v3">zhimi.airpurifier.v3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier v5 (<a name="zhimi-airpurifier-v5">zhimi.airpurifier.v5</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier Pro v6 (<a name="zhimi-airpurifier-v6">zhimi.airpurifier.v6</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| bright           | Number  | LED Brightness                      |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier Pro v7 (<a name="zhimi-airpurifier-v7">zhimi.airpurifier.v7</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| volume           | Number  | Volume                              |
| led              | Switch  | LED Status                          |
| illuminance      | Number  | Illuminance                         |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| motorspeed       | Number  | Motor Speed                         |
| motorspeed2      | Number  | Motor Speed 2                       |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier 2 (mini) (<a name="zhimi-airpurifier-m1">zhimi.airpurifier.m1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier (mini) (<a name="zhimi-airpurifier-m2">zhimi.airpurifier.m2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier MS1 (<a name="zhimi-airpurifier-ma1">zhimi.airpurifier.ma1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier MS2 (<a name="zhimi-airpurifier-ma2">zhimi.airpurifier.ma2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| bright           | Number  | LED Brightness                      |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier 3 (<a name="zhimi-airpurifier-ma4">zhimi.airpurifier.ma4</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| Fault            | Number  | Air Purifier-Device Fault           |
| On               | Switch  | Air Purifier-Switch Status          |
| FanLevel         | Number  | Air Purifier-Fan Level              |
| Mode             | Number  | Air Purifier-Mode                   |
| FirmwareRevision | String  | Device Information-Current Firmware Version |
| Manufacturer     | String  | Device Information-Device Manufacturer |
| Model            | String  | Device Information-Device Model     |
| SerialNumber     | String  | Device Information-Device Serial Number |
| Pm25Density      | Number  | Environment-PM2.5 Density           |
| RelativeHumidity | Number  | Environment-Relative Humidity       |
| Temperature      | Number  | Environment-Temperature             |
| FilterLifeLevel  | Number  | Filter-Filter Life Level            |
| FilterUsedTime   | String  | Filter-Filter Used Time             |
| Alarm            | Switch  | Alarm-Alarm                         |
| Brightness       | Number  | Indicator Light-Brightness          |
| On1              | Switch  | Indicator Light-Switch Status       |
| PhysicalControlsLocked | Switch  | Physical Control Locked-Physical Control Locked |
| ButtonPressed    | String  | button-button_pressed               |
| FilterMaxTime    | Number  | filter-time-filter-max-time         |
| FilterHourUsedDebug | Number  | filter-time-filter-hour-used-debug  |
| M1Strong         | Number  | motor-speed-m1-strong               |
| M1High           | Number  | motor-speed-m1-high                 |
| M1Med            | Number  | motor-speed-m1-med                  |
| M1MedL           | Number  | motor-speed-m1-med-l                |
| M1Low            | Number  | motor-speed-m1-low                  |
| M1Silent         | Number  | motor-speed-m1-silent               |
| M1Favorite       | Number  | motor-speed-m1-favorite             |
| Motor1Speed      | Number  | motor-speed-motor1-speed            |
| Motor1SetSpeed   | Number  | motor-speed-motor1-set-speed        |
| FavoriteFanLevel | Number  | motor-speed-favorite fan level      |
| UseTime          | Number  | use-time-use-time                   |
| PurifyVolume     | Number  | aqi-purify-volume                   |
| AverageAqi       | Number  | aqi-average-aqi                     |
| AverageAqiCnt    | Number  | aqi-average-aqi-cnt                 |
| AqiZone          | String  | aqi-aqi-zone                        |
| SensorState      | String  | aqi-sensor-state                    |
| AqiGoodh         | Number  | aqi-aqi-goodh                       |
| AqiRunstate      | Number  | aqi-aqi-runstate                    |
| AqiState         | Number  | aqi-aqi-state                       |
| AqiUpdataHeartbeat | Number  | aqi-aqi-updata-heartbeat            |
| RfidTag          | String  | rfid-rfid-tag                       |
| RfidFactoryId    | String  | rfid-rfid-factory-id                |
| RfidProductId    | String  | rfid-rfid-product-id                |
| RfidTime         | String  | rfid-rfid-time                      |
| RfidSerialNum    | String  | rfid-rfid-serial-num                |
| AppExtra         | Number  | others-app-extra                    |
| MainChannel      | Number  | others-main-channel                 |
| SlaveChannel     | Number  | others-slave-channel                |
| Cola             | String  | others-cola                         |
| ButtomDoor       | Switch  | others-buttom-door                  |
| RebootCause      | Number  | others-reboot_cause                 |
| HwVersion        | Number  | others-hw-version                   |
| I2cErrorCount    | Number  | others-i2c-error-count              |
| ManualLevel      | Number  | others-manual-level                 |

### Mi Air Purifier 3 (<a name="zhimi-airpurifier-mb3">zhimi.airpurifier.mb3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| Fault            | Number  | Air Purifier-fault                  |
| On               | Switch  | Air Purifier-Switch Status          |
| FanLevel         | Number  | Air Purifier-Fan Level              |
| Mode             | Number  | Air Purifier-Mode                   |
| FirmwareRevision | String  | Device Information-Current Firmware Version |
| Manufacturer     | String  | Device Information-Device Manufacturer |
| Model            | String  | Device Information-Device Model     |
| SerialNumber     | String  | Device Information-Device Serial Number |
| Pm25Density      | Number  | Environment-PM2.5                   |
| RelativeHumidity | Number  | Environment-Relative Humidity       |
| Temperature      | Number  | Environment-Temperature             |
| FilterLifeLevel  | Number  | Filter-Filter Life Level            |
| FilterUsedTime   | String  | Filter-Filter Used Time             |
| Alarm            | Switch  | Alarm-Alarm                         |
| Brightness       | Number  | Indicator Light-brightness          |
| On1              | Switch  | Indicator Light-Switch Status       |
| PhysicalControlsLocked | Switch  | Physical Control Locked-Physical Control Locked |
| ButtonPressed    | String  | Button-button-pressed               |
| FilterMaxTime    | Number  | filter-time-filter-max-time         |
| FilterHourDebug  | Number  | filter-time-filter-hour-debug       |
| MotorStrong      | Number  | motor-speed-motor-strong            |
| MotorHigh        | Number  | motor-speed-motor-high              |
| MotorMed         | Number  | motor-speed-motor-med               |
| MotorMedL        | Number  | motor-speed-motor-med-l             |
| MotorLow         | Number  | motor-speed-motor-low               |
| MotorSilent      | Number  | motor-speed-motor-silent            |
| MotorFavorite    | Number  | motor-speed-motor-favorite          |
| MotorSpeed       | Number  | motor-speed-motor-speed             |
| MotorSetSpeed    | Number  | motor-speed-motor-set-speed         |
| FavoriteFanLevel | Number  | motor-speed-favorite-fan-level      |
| UseTime          | Number  | use-time-use-time                   |
| PurifyVolume     | Number  | aqi-purify-volume                   |
| AverageAqi       | Number  | aqi-average-aqi                     |
| AverageAqiCnt    | Number  | aqi-average-aqi-cnt                 |
| AqiZone          | String  | aqi-aqi-zone                        |
| SensorState      | String  | aqi-sensor-state                    |
| AqiGoodh         | Number  | aqi-aqi-goodh                       |
| AqiRunstate      | Number  | aqi-aqi-runstate                    |
| AqiState         | Number  | aqi-aqi-state                       |
| AqiUpdataHeartbeat | Number  | aqi-aqi-updata-heartbeat            |
| RfidTag          | String  | rfid-rfid-tag                       |
| RfidFactoryId    | String  | rfid-rfid-factory-id                |
| RfidProductId    | String  | rfid-rfid-product-id                |
| RfidTime         | String  | rfid-rfid-time                      |
| RfidSerialNum    | String  | rfid-rfid-serial-num                |
| AppExtra         | Number  | others-app-extra                    |
| MainChannel      | Number  | others-main-channel                 |
| SlaveChannel     | Number  | others-slave-channel                |
| Cola             | String  | others-cola                         |
| ButtomDoor       | Switch  | others-buttom-door                  |
| RebootCause      | Number  | others-reboot-cause                 |
| HwVersion        | Number  | others-hw-version                   |
| IicErrorCount    | Number  | others-iic-error-count              |
| ManualLevel      | Number  | others-manual-level                 |
| CountryCode      | Number  | others-National code                |

### Mi Air Purifier Super (<a name="zhimi-airpurifier-sa1">zhimi.airpurifier.sa1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier Super 2 (<a name="zhimi-airpurifier-sa2">zhimi.airpurifier.sa2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Fresh Air Ventilator (<a name="dmaker-airfresh-t2017">dmaker.airfresh.t2017</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| airFreshMode     | String  | Mode                                |
| airFreshPTCPower | Switch  | PTC                                 |
| airFreshPtcLevel | String  | PTC Level                           |
| airFreshPTCStatus | Switch  | PTC Status                          |
| airFreshDisplayDirection | String  | Screen direction                    |
| airFreshDisplay  | Switch  | Display                             |
| airFreshChildLock | Switch  | Child Lock                          |
| airFreshSound    | Switch  | Sound                               |
| airFreshPM25     | Number  | PM2.5                               |
| airFreshCO2      | Number  | CO2                                 |
| airFreshCurrentSpeed | Number  | Current Speed                       |
| airFreshFavoriteSpeed | Number  | Favorite Speed                      |
| airFreshTemperature | Number  | Temperature Outside                 |
| airFreshFilterPercents | Number  | Filter Percents Remaining           |
| airFreshFilterDays | Number  | Filter Days Remaining               |
| airFreshFilterProPercents | Number  | Filter Pro Percents Remaining       |
| airFreshFilterProDays | Number  | Filter Pro Days Remaining           |
| airFreshResetFilter | String  | Reset Filter                        |

### Mi Fresh Air Ventilator A1 (<a name="dmaker-airfresh-a1">dmaker.airfresh.a1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| airFreshMode     | String  | Mode                                |
| airFreshPTCPower | Switch  | PTC                                 |
| airFreshPTCStatus | Switch  | PTC Status                          |
| airFreshDisplay  | Switch  | Display                             |
| airFreshChildLock | Switch  | Child Lock                          |
| airFreshSound    | Switch  | Sound                               |
| airFreshPM25     | Number  | PM2.5                               |
| airFreshCO2      | Number  | CO2                                 |
| airFreshCurrentSpeed | Number  | Current Speed                       |
| airFreshFavoriteSpeed | Number  | Favorite Speed                      |
| airFreshTemperature | Number  | Temperature Outside                 |
| airFreshFilterPercents | Number  | Filter Percents Remaining           |
| airFreshFilterDays | Number  | Filter Days Remaining               |
| airFreshResetFilterA1 | String  | Reset Filter                        |

### Gosund Plug (<a name="cuco-plug-cp1">cuco.plug.cp1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| FirmwareRevision | String  | Device Information-CurrentFirmware Version |
| Manufacturer     | String  | Device Information-Device Manufacturer |
| Model            | String  | Device Information-Device Model     |
| SerialNumber     | String  | Device Information-Device Serial Number |
| On               | Switch  | Switch-Switch Status                |

### Mi Air Purifier mb1 (<a name="zhimi-airpurifier-mb1">zhimi.airpurifier.mb1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier 2S (<a name="zhimi-airpurifier-mc1">zhimi.airpurifier.mc1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | LED Status                          |
| buzzer           | Switch  | Buzzer Status                       |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Smart Fan (<a name="zhimi-fan-v1">zhimi.fan.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| angleEnable      | Switch  | Rotation                            |
| usedhours        | Number  | Run Time                            |
| angle            | Number  | Angle                               |
| poweroffTime     | Number  | Timer                               |
| buzzer           | Switch  | Buzzer                              |
| led_b            | Number  | LED                                 |
| child_lock       | Switch  | Child Lock                          |
| speedLevel       | Number  | Speed Level                         |
| speed            | Number  | Speed                               |
| naturalLevel     | Number  | Natural Level                       |
| temp_dec         | Number  | Temperature                         |
| humidity         | Number  | Humidity                            |
| acPower          | String  | AC Power                            |
| mode             | String  | Battery Charge                      |
| battery          | Number  | Battery                             |
| move             | String  | Move Direction                      |

### Mi Smart Fan (<a name="zhimi-fan-v2">zhimi.fan.v2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| angleEnable      | Switch  | Rotation                            |
| usedhours        | Number  | Run Time                            |
| angle            | Number  | Angle                               |
| poweroffTime     | Number  | Timer                               |
| buzzer           | Switch  | Buzzer                              |
| led_b            | Number  | LED                                 |
| child_lock       | Switch  | Child Lock                          |
| speedLevel       | Number  | Speed Level                         |
| speed            | Number  | Speed                               |
| naturalLevel     | Number  | Natural Level                       |
| temp_dec         | Number  | Temperature                         |
| humidity         | Number  | Humidity                            |
| acPower          | String  | AC Power                            |
| mode             | String  | Battery Charge                      |
| battery          | Number  | Battery                             |
| move             | String  | Move Direction                      |

### Mi Smart Pedestal Fan (<a name="zhimi-fan-v3">zhimi.fan.v3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| angleEnable      | Switch  | Rotation                            |
| usedhours        | Number  | Run Time                            |
| angle            | Number  | Angle                               |
| poweroffTime     | Number  | Timer                               |
| buzzer           | Switch  | Buzzer                              |
| led_b            | Number  | LED                                 |
| child_lock       | Switch  | Child Lock                          |
| speedLevel       | Number  | Speed Level                         |
| speed            | Number  | Speed                               |
| naturalLevel     | Number  | Natural Level                       |
| temp_dec         | Number  | Temperature                         |
| humidity         | Number  | Humidity                            |
| acPower          | String  | AC Power                            |
| mode             | String  | Battery Charge                      |
| battery          | Number  | Battery                             |
| move             | String  | Move Direction                      |

### Xiaomi Mi Smart Pedestal Fan (<a name="zhimi-fan-sa1">zhimi.fan.sa1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| angleEnable      | Switch  | Rotation                            |
| usedhours        | Number  | Run Time                            |
| angle            | Number  | Angle                               |
| poweroffTime     | Number  | Timer                               |
| buzzer           | Switch  | Buzzer                              |
| led_b            | Number  | LED                                 |
| child_lock       | Switch  | Child Lock                          |
| speedLevel       | Number  | Speed Level                         |
| speed            | Number  | Speed                               |
| naturalLevel     | Number  | Natural Level                       |
| acPower          | Switch  | AC Power                            |
| move             | String  | Move Direction                      |

### Xiaomi Mi Smart Pedestal Fan (<a name="zhimi-fan-za1">zhimi.fan.za1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| angleEnable      | Switch  | Rotation                            |
| usedhours        | Number  | Run Time                            |
| angle            | Number  | Angle                               |
| poweroffTime     | Number  | Timer                               |
| buzzer           | Switch  | Buzzer                              |
| led_b            | Number  | LED                                 |
| child_lock       | Switch  | Child Lock                          |
| speedLevel       | Number  | Speed Level                         |
| speed            | Number  | Speed                               |
| naturalLevel     | Number  | Natural Level                       |
| acPower          | Switch  | AC Power                            |
| move             | String  | Move Direction                      |

### Mi Humdifier (<a name="zhimi-humidifier-v1">zhimi.humidifier.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| setHumidity      | Number  | Humidity Set                        |
| aqi              | Number  | Air Quality Index                   |
| translevel       | Number  | Trans_level                         |
| bright           | Number  | LED Brightness                      |
| buzzer           | Switch  | Buzzer Status                       |
| depth            | Number  | Depth                               |
| dry              | Switch  | Dry                                 |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| temperature      | Number  | Temperature                         |
| childlock        | Switch  | Child Lock                          |

### Xiaomi Philips Eyecare Smart Lamp 2 (<a name="philips-light-sread1">philips.light.sread1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| ambientPower     | Switch  | Ambient Power                       |
| ambientBrightness | Number  | Ambient Brightness                  |
| illumination     | Number  | Ambient Illumination                |
| eyecare          | Switch  | Eyecare                             |

### Xiaomi Philips LED Ceiling Lamp (<a name="philips-light-ceiling">philips.light.ceiling</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| switchscene      | Switch  | Switch Scene                        |
| toggle           | Switch  | Toggle                              |

### Xiaomi Philips LED Ceiling Lamp (<a name="philips-light-zyceiling">philips.light.zyceiling</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| switchscene      | Switch  | Switch Scene                        |
| toggle           | Switch  | Toggle                              |

### Xiaomi Philips Bulb (<a name="philips-light-bulb">philips.light.bulb</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |

### PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp (<a name="philips-light-candle">philips.light.candle</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### Xiaomi Philips Downlight (<a name="philips-light-downlight">philips.light.downlight</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |

### Xiaomi Philips ZhiRui bedside lamp (<a name="philips-light-moonlight">philips.light.moonlight</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| gonight          | Switch  | Go Night                            |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal (<a name="philips-light-candle2">philips.light.candle2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### philips.light.mono1 (<a name="philips-light-mono1">philips.light.mono1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| scene            | Number  | Scene                               |

### philips.light.virtual (<a name="philips-light-virtual">philips.light.virtual</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |

### philips.light.zysread (<a name="philips-light-zysread">philips.light.zysread</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |

### philips.light.zystrip (<a name="philips-light-zystrip">philips.light.zystrip</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| cct              | Dimmer  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |

### Mi Power-plug (<a name="chuangmi-plug-m1">chuangmi.plug.m1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| temperature      | Number  | Temperature                         |
| led              | Switch  | Indicator light                     |

### Mi Power-plug v1 (<a name="chuangmi-plug-v1">chuangmi.plug.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| usb              | Switch  | USB                                 |

### Mi Power-plug v2 (<a name="chuangmi-plug-v2">chuangmi.plug.v2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| usb              | Switch  | USB                                 |

### Mi Power-plug v3 (<a name="chuangmi-plug-v3">chuangmi.plug.v3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| usb              | Switch  | USB                                 |
| temperature      | Number  | Temperature                         |
| led              | Switch  | Wifi LED                            |

### Mi Power-plug (<a name="chuangmi-plug-m3">chuangmi.plug.m3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| temperature      | Number  | Temperature                         |
| led              | Switch  | Indicator light                     |

### Mi Smart Plug (<a name="chuangmi-plug-hmi205">chuangmi.plug.hmi205</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| temperature      | Number  | Temperature                         |
| led              | Switch  | Indicator light                     |

### Qing Mi Smart Power Strip v1 (<a name="qmi-powerstrip-v1">qmi.powerstrip.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| powerUsage       | Number  | Power Consumption                   |
| led              | Switch  | wifi LED                            |
| power_price      | Number  | power_price                         |
| current          | Number  | Current                             |
| temperature      | Number  | Temperature                         |

### Mi Power-strip v2 (<a name="zimi-powerstrip-v2">zimi.powerstrip.v2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| powerUsage       | Number  | Power Consumption                   |
| led              | Switch  | wifi LED                            |
| power_price      | Number  | power_price                         |
| current          | Number  | Current                             |
| temperature      | Number  | Temperature                         |

### Vacuum 1C STYTJ01ZHM (<a name="dreame-vacuum-mc1808">dreame.vacuum.mc1808</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| BatteryLevel     | Number  | Battery-Battery Level               |
| ChargingState    | Number  | Battery-Charging State              |
| FirmwareRevision | String  | Device Information-Current Firmware Version |
| Manufacturer     | String  | Device Information-Device Manufacturer |
| Model            | String  | Device Information-Device Model     |
| SerialNumber     | String  | Device Information-Device Serial Number |
| Fault            | Number  | Robot Cleaner-Device Fault          |
| Status           | Number  | Robot Cleaner-Status                |
| BrushLeftTime    | String  | Main Cleaning Brush-Brush Left Time |
| BrushLifeLevel   | Number  | Main Cleaning Brush-Brush Life Level |
| FilterLifeLevel  | Number  | Filter-Filter Life Level            |
| FilterLeftTime   | String  | Filter-Filter Left Time             |
| BrushLeftTime1   | String  | Side Cleaning Brush-Brush Left Time |
| BrushLifeLevel1  | Number  | Side Cleaning Brush-Brush Life Level |
| WorkMode         | Number  | clean-workmode                      |
| Area             | String  | clean-area                          |
| Timer            | String  | clean-timer                         |
| Mode             | Number  | clean-mode                          |
| TotalCleanTime   | String  | clean-total time                    |
| TotalCleanTimes  | String  | clean-total times                   |
| TotalCleanArea   | String  | clean-Total area                    |
| CleanLogStartTime | String  | clean-Start Time                    |
| ButtonLed        | String  | clean-led                           |
| TaskDone         | Number  | clean-task done                     |
| LifeSieve        | String  | consumable-life-sieve               |
| LifeBrushSide    | String  | consumable-life-brush-side          |
| LifeBrushMain    | String  | consumable-life-brush-main          |
| Enable           | Switch  | annoy-enable                        |
| StartTime        | String  | annoy-start-time                    |
| StopTime         | String  | annoy-stop-time                     |
| MapView          | String  | map-map-view                        |
| Volume           | Number  | audio-volume                        |
| VoicePackets     | String  | audio-voiceId                       |
| TimeZone         | String  | timezone                            |

###  Mijia 1 Gang Wall Smart Switch (WIFI) - PTX switch (<a name="090615-switch-xswitch01">090615.switch.xswitch01</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| switch1state     | Number  | Switch 1                            |
| switch1name      | String  | Switch Name 1                       |

###  Mijia 2 Gang Wall Smart Switch (WIFI) - PTX switch (<a name="090615-switch-xswitch02">090615.switch.xswitch02</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| switch1state     | Number  | Switch 1                            |
| switch2state     | Number  | Switch 2                            |
| switch1name      | String  | Switch Name 1                       |
| switch2name      | String  | Switch Name 2                       |

###  Mijia 3 Gang Wall Smart Switch (WIFI) - PTX switch (<a name="090615-switch-xswitch03">090615.switch.xswitch03</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| switch1state     | Number  | Switch 1                            |
| switch2state     | Number  | Switch 2                            |
| switch3state     | Number  | Switch 3                            |
| switch1name      | String  | Switch Name 1                       |
| switch2name      | String  | Switch Name 2                       |
| switch3name      | String  | Switch Name 3                       |

### Mi Water Purifier v2 (<a name="yunmi-waterpuri-v2">yunmi.waterpuri.v2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |

### Mi Water Purifier lx2 (<a name="yunmi-waterpuri-lx2">yunmi.waterpuri.lx2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |

### Mi Water Purifier lx3 (<a name="yunmi-waterpuri-lx3">yunmi.waterpuri.lx3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |

### Mi Water Purifier lx4 (<a name="yunmi-waterpuri-lx4">yunmi.waterpuri.lx4</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |

### Mi Water Purifier v2 (<a name="yunmi-waterpurifier-v2">yunmi.waterpurifier.v2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |

### Mi Water Purifier v3 (<a name="yunmi-waterpurifier-v3">yunmi.waterpurifier.v3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |

### Mi Water Purifier v4 (<a name="yunmi-waterpurifier-v4">yunmi.waterpurifier.v4</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |

### Yeelight Lamp (<a name="yeelink-light-bslamp1">yeelink.light.bslamp1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Lamp (<a name="yeelink-light-bslamp2">yeelink.light.bslamp2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling1">yeelink.light.ceiling1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v2 (<a name="yeelink-light-ceiling2">yeelink.light.ceiling2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v3 (<a name="yeelink-light-ceiling3">yeelink.light.ceiling3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB) (<a name="yeelink-light-ceiling4">yeelink.light.ceiling4</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| ambientBrightness | Number  | Ambient Brightness                  |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| ambientPower     | Switch  | Ambient Power                       |
| ambientColor     | Color   | Ambient Color                       |
| ambientColorTemperature | Number  | Ambient Color Temperature           |
| customScene      | String  | Set Scene                           |
| ambientColorMode | Number  | Ambient Color Mode                  |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v4 (<a name="yeelink-light-ceiling4-ambi">yeelink.light.ceiling4.ambi</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| ambientBrightness | Number  | Ambient Brightness                  |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| ambientPower     | Switch  | Ambient Power                       |
| ambientColor     | Color   | Ambient Color                       |
| ambientColorTemperature | Number  | Ambient Color Temperature           |
| customScene      | String  | Set Scene                           |
| ambientColorMode | Number  | Ambient Color Mode                  |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v5 (<a name="yeelink-light-ceiling5">yeelink.light.ceiling5</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v6 (<a name="yeelink-light-ceiling6">yeelink.light.ceiling6</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v7 (<a name="yeelink-light-ceiling7">yeelink.light.ceiling7</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v8 (<a name="yeelink-light-ceiling8">yeelink.light.ceiling8</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v9 (<a name="yeelink-light-ceiling9">yeelink.light.ceiling9</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Meteorite lamp (<a name="yeelink-light-ceiling10">yeelink.light.ceiling10</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| ambientBrightness | Number  | Ambient Brightness                  |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| ambientPower     | Switch  | Ambient Power                       |
| ambientColor     | Color   | Ambient Color                       |
| ambientColorTemperature | Number  | Ambient Color Temperature           |
| customScene      | String  | Set Scene                           |
| ambientColorMode | Number  | Ambient Color Mode                  |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v11 (<a name="yeelink-light-ceiling11">yeelink.light.ceiling11</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v12 (<a name="yeelink-light-ceiling12">yeelink.light.ceiling12</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v13 (<a name="yeelink-light-ceiling13">yeelink.light.ceiling13</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight ct2 (<a name="yeelink-light-ct2">yeelink.light.ct2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight White Bulb (<a name="yeelink-light-mono1">yeelink.light.mono1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight White Bulb v2 (<a name="yeelink-light-mono2">yeelink.light.mono2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight (<a name="yeelink-light-lamp1">yeelink.light.lamp1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight (<a name="yeelink-light-lamp2">yeelink.light.lamp2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight (<a name="yeelink-light-lamp3">yeelink.light.lamp3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight Strip (<a name="yeelink-light-strip1">yeelink.light.strip1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Strip (<a name="yeelink-light-strip2">yeelink.light.strip2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight (<a name="yeelink-light-virtual">yeelink.light.virtual</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight Color Bulb (<a name="yeelink-light-color1">yeelink.light.color1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Color Bulb YLDP06YL 10W (<a name="yeelink-light-color2">yeelink.light.color2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Color Bulb YLDP02YL 9W (<a name="yeelink-light-color3">yeelink.light.color3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Bulb YLDP13YL (8,5W) (<a name="yeelink-light-color4">yeelink.light.color4</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Dimmer  | Brightness                          |
| delayoff         | Number  | Shutdown Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |




## Example item file Rockrobo vacuum

```
Group  gVac     "Xiaomi Robot Vacuum"      <fan>
Group  gVacStat "Status Details"           <status> (gVac)
Group  gVacCons "Consumables Usage"        <line-increase> (gVac)
Group  gVacDND  "Do Not Disturb Settings"  <moon> (gVac)
Group  gVacHist "Cleaning History"         <calendar> (gVac)
Group  gVacLast "Last Cleaning Details"       <calendar> (gVac)

String actionControl  "Vacuum Control"          {channel="miio:vacuum:034F0E45:actions#control" }
String actionCommand  "Vacuum Command"          {channel="miio:vacuum:034F0E45:actions#commands" }

Number statusBat    "Battery Level [%1.0f%%]" <battery>   (gVac,gVacStat) {channel="miio:vacuum:034F0E45:status#battery" }
Number statusArea    "Cleaned Area [%1.0fm²]" <zoom>   (gVac,gVacStat) {channel="miio:vacuum:034F0E45:status#clean_area" }
Number statusTime    "Cleaning Time [%1.0f']" <clock>   (gVac,gVacStat) {channel="miio:vacuum:034F0E45:status#clean_time" }
String  statusError    "Error [%s]"  <error>  (gVac,gVacStat) {channel="miio:vacuum:034F0E45:status#error_code" }
Number statusFanPow    "Fan Power [%1.0f%%]"  <signal>   (gVacStat) {channel="miio:vacuum:034F0E45:status#fan_power" } 
Number statusClean    "In Cleaning Status [%1.0f]"   <switch>  (gVacStat) {channel="miio:vacuum:034F0E45:status#in_cleaning" }
Switch statusDND    "DND Activated"    (gVacStat) {channel="miio:vacuum:034F0E45:status#dnd_enabled" }
Number statusStatus    "Status [%1.0f]"  <status>  (gVacStat) {channel="miio:vacuum:034F0E45:status#state"} 

Number consumableMain    "Main Brush [%1.0f]"    (gVacCons) {channel="miio:vacuum:034F0E45:consumables#main_brush_time"}
Number consumableSide    "Side Brush [%1.0f]"    (gVacCons) {channel="miio:vacuum:034F0E45:consumables#side_brush_time"}
Number consumableFilter    "Filter Time[%1.0f]"    (gVacCons) {channel="miio:vacuum:034F0E45:consumables#filter_time" }
Number consumableSensor    "Sensor [%1.0f]"    (gVacCons) {channel="miio:vacuum:034F0E45:consumables#sensor_dirt_time"}

Switch dndFunction   "DND Function" <moon>   (gVacDND) {channel="miio:vacuum:034F0E45:dnd#dnd_function"}
String dndStart   "DND Start Time [%s]" <clock>   (gVacDND) {channel="miio:vacuum:034F0E45:dnd#dnd_start"}
String dndEnd   "DND End Time [%s]"   <clock-on>  (gVacDND) {channel="miio:vacuum:034F0E45:dnd#dnd_end"}

Number historyArea    "Total Cleaned Area [%1.0fm²]" <zoom>    (gVacHist) {channel="miio:vacuum:034F0E45:history#total_clean_area"}
String historyTime    "Total Clean Time [%s]"   <clock>     (gVacHist) {channel="miio:vacuum:034F0E45:history#total_clean_time"}
Number historyCount    "Total # Cleanings [%1.0f]"  <office>  (gVacHist) {channel="miio:vacuum:034F0E45:history#total_clean_count"}

String lastStart   "Last Cleaning Start time [%s]" <clock> (gVacLast) {channel="miio:vacuum:034F0E45:cleaning#last_clean_start_time"}
String lastEnd     "Last Cleaning End time [%s]" <clock> (gVacLast) {channel="miio:vacuum:034F0E45:cleaning#last_clean_end_time"}
Number lastArea    "Last Cleaned Area [%1.0fm²]" <zoom>    (gVacLast) {channel="miio:vacuum:034F0E45:cleaning#last_clean_area"}
Number lastTime    "Last Clean Time [%1.0f']"   <clock>     (gVacLast) {channel="miio:vacuum:034F0E45:cleaning#last_clean_duration"}
Number lastError    "Error [%s]"  <error>  (gVacLast) {channel="miio:vacuum:034F0E45:cleaning#last_clean_error" }
Switch lastCompleted  "Last Cleaning Completed"    (gVacLast) {channel="miio:vacuum:034F0E45:cleaning#last_clean_finish" }


Image map "Cleaning Map" (gVacLast) {channel="miio:vacuum:034F0E45:cleaning#map"}
```

Note: cleaning map is only available with cloud access.

### Mi Air Monitor v1 (zhimi.airmonitor.v1) item file lines

note: Autogenerated example. Replace the id (airmonitor) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airmonitor "Mi Air Monitor v1" <status>
Switch power "Power" (G_airmonitor) {channel="miio:basic:airmonitor:power"}
Number aqi "Air Quality Index" (G_airmonitor) {channel="miio:basic:airmonitor:aqi"}
Number battery "Battery" (G_airmonitor) {channel="miio:basic:airmonitor:battery"}
Switch usb_state "USB State" (G_airmonitor) {channel="miio:basic:airmonitor:usb_state"}
Switch time_state "Time State" (G_airmonitor) {channel="miio:basic:airmonitor:time_state"}
Switch night_state "Night State" (G_airmonitor) {channel="miio:basic:airmonitor:night_state"}
Number night_begin "Night Begin Time" (G_airmonitor) {channel="miio:basic:airmonitor:night_begin"}
Number night_end "Night End Time" (G_airmonitor) {channel="miio:basic:airmonitor:night_end"}
```

### Mi Air Quality Monitor 2gen (cgllc.airmonitor.b1) item file lines

note: Autogenerated example. Replace the id (airmonitor) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airmonitor "Mi Air Quality Monitor 2gen" <status>
Number battery "Battery" (G_airmonitor) {channel="miio:basic:airmonitor:battery"}
Number pm25 "PM2.5" (G_airmonitor) {channel="miio:basic:airmonitor:pm25"}
Number co2 "CO2e" (G_airmonitor) {channel="miio:basic:airmonitor:co2"}
Number tvoc "tVOC" (G_airmonitor) {channel="miio:basic:airmonitor:tvoc"}
Number humidity "Humidity" (G_airmonitor) {channel="miio:basic:airmonitor:humidity"}
Number temperature "Temperature" (G_airmonitor) {channel="miio:basic:airmonitor:temperature"}
```

### Mi Air Quality Monitor S1 (cgllc.airmonitor.s1) item file lines

note: Autogenerated example. Replace the id (airmonitor) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airmonitor "Mi Air Quality Monitor S1" <status>
Number battery "Battery" (G_airmonitor) {channel="miio:basic:airmonitor:battery"}
Number pm25 "PM2.5" (G_airmonitor) {channel="miio:basic:airmonitor:pm25"}
Number co2 "CO2" (G_airmonitor) {channel="miio:basic:airmonitor:co2"}
Number tvoc "tVOC" (G_airmonitor) {channel="miio:basic:airmonitor:tvoc"}
Number humidity "Humidity" (G_airmonitor) {channel="miio:basic:airmonitor:humidity"}
Number temperature "Temperature" (G_airmonitor) {channel="miio:basic:airmonitor:temperature"}
```

### Mi Air Humidifier (zhimi.humidifier.v1) item file lines

note: Autogenerated example. Replace the id (humidifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_humidifier "Mi Air Humidifier" <status>
Switch power "Power" (G_humidifier) {channel="miio:basic:humidifier:power"}
String mode "Mode" (G_humidifier) {channel="miio:basic:humidifier:mode"}
Number humidity "Humidity" (G_humidifier) {channel="miio:basic:humidifier:humidity"}
Number setHumidity "Humidity Set" (G_humidifier) {channel="miio:basic:humidifier:setHumidity"}
Number aqi "Air Quality Index" (G_humidifier) {channel="miio:basic:humidifier:aqi"}
Number translevel "Trans_level" (G_humidifier) {channel="miio:basic:humidifier:translevel"}
Number bright "LED Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
Switch buzzer "Buzzer Status" (G_humidifier) {channel="miio:basic:humidifier:buzzer"}
Number depth "Depth" (G_humidifier) {channel="miio:basic:humidifier:depth"}
Switch dry "Dry" (G_humidifier) {channel="miio:basic:humidifier:dry"}
Number usedhours "Run Time" (G_humidifier) {channel="miio:basic:humidifier:usedhours"}
Number motorspeed "Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:motorspeed"}
Number temperature "Temperature" (G_humidifier) {channel="miio:basic:humidifier:temperature"}
Switch childlock "Child Lock" (G_humidifier) {channel="miio:basic:humidifier:childlock"}
```

### Mi Air Humidifier (zhimi.humidifier.ca1) item file lines

note: Autogenerated example. Replace the id (humidifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_humidifier "Mi Air Humidifier" <status>
Switch power "Power" (G_humidifier) {channel="miio:basic:humidifier:power"}
String mode "Mode" (G_humidifier) {channel="miio:basic:humidifier:mode"}
Number humidity "Humidity" (G_humidifier) {channel="miio:basic:humidifier:humidity"}
Number setHumidity "Humidity Set" (G_humidifier) {channel="miio:basic:humidifier:setHumidity"}
Number aqi "Air Quality Index" (G_humidifier) {channel="miio:basic:humidifier:aqi"}
Number translevel "Trans_level" (G_humidifier) {channel="miio:basic:humidifier:translevel"}
Number bright "LED Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
Switch buzzer "Buzzer Status" (G_humidifier) {channel="miio:basic:humidifier:buzzer"}
Number depth "Depth" (G_humidifier) {channel="miio:basic:humidifier:depth"}
Switch dry "Dry" (G_humidifier) {channel="miio:basic:humidifier:dry"}
Number usedhours "Run Time" (G_humidifier) {channel="miio:basic:humidifier:usedhours"}
Number motorspeed "Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:motorspeed"}
Number temperature "Temperature" (G_humidifier) {channel="miio:basic:humidifier:temperature"}
Switch childlock "Child Lock" (G_humidifier) {channel="miio:basic:humidifier:childlock"}
```

### Mi Air Humidifier 2 (zhimi.humidifier.cb1) item file lines

note: Autogenerated example. Replace the id (humidifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_humidifier "Mi Air Humidifier 2" <status>
Switch power "Power" (G_humidifier) {channel="miio:basic:humidifier:power"}
String humidifierMode "Humidifier Mode" (G_humidifier) {channel="miio:basic:humidifier:humidifierMode"}
Number humidity "Humidity" (G_humidifier) {channel="miio:basic:humidifier:humidity"}
Number setHumidity "Humidity Set" (G_humidifier) {channel="miio:basic:humidifier:setHumidity"}
Number bright "LED Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
Switch buzzer "Buzzer Status" (G_humidifier) {channel="miio:basic:humidifier:buzzer"}
Number depth "Depth" (G_humidifier) {channel="miio:basic:humidifier:depth"}
Switch dry "Dry" (G_humidifier) {channel="miio:basic:humidifier:dry"}
Number usedhours "Run Time" (G_humidifier) {channel="miio:basic:humidifier:usedhours"}
Number motorspeed "Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:motorspeed"}
Number temperature "Temperature" (G_humidifier) {channel="miio:basic:humidifier:temperature"}
Switch childlock "Child Lock" (G_humidifier) {channel="miio:basic:humidifier:childlock"}
```

### Mi Air Purifier v1 (zhimi.airpurifier.v1) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier v1" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier v2 (zhimi.airpurifier.v2) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier v2" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier v3 (zhimi.airpurifier.v3) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier v3" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier v5 (zhimi.airpurifier.v5) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier v5" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier Pro v6 (zhimi.airpurifier.v6) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier Pro v6" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Number bright "LED Brightness" (G_airpurifier) {channel="miio:basic:airpurifier:bright"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier Pro v7 (zhimi.airpurifier.v7) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier Pro v7" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Number volume "Volume" (G_airpurifier) {channel="miio:basic:airpurifier:volume"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Number illuminance "Illuminance" (G_airpurifier) {channel="miio:basic:airpurifier:illuminance"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number motorspeed2 "Motor Speed 2" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed2"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier 2 (mini) (zhimi.airpurifier.m1) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 2 (mini)" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier (mini) (zhimi.airpurifier.m2) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier (mini)" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier MS1 (zhimi.airpurifier.ma1) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier MS1" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier MS2 (zhimi.airpurifier.ma2) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier MS2" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Number bright "LED Brightness" (G_airpurifier) {channel="miio:basic:airpurifier:bright"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier 3 (zhimi.airpurifier.ma4) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 3" <status>
Number Fault "Air Purifier-Device Fault" (G_airpurifier) {channel="miio:basic:airpurifier:Fault"}
Switch On "Air Purifier-Switch Status" (G_airpurifier) {channel="miio:basic:airpurifier:On"}
Number FanLevel "Air Purifier-Fan Level" (G_airpurifier) {channel="miio:basic:airpurifier:FanLevel"}
Number Mode "Air Purifier-Mode" (G_airpurifier) {channel="miio:basic:airpurifier:Mode"}
String FirmwareRevision "Device Information-Current Firmware Version" (G_airpurifier) {channel="miio:basic:airpurifier:FirmwareRevision"}
String Manufacturer "Device Information-Device Manufacturer" (G_airpurifier) {channel="miio:basic:airpurifier:Manufacturer"}
String Model "Device Information-Device Model" (G_airpurifier) {channel="miio:basic:airpurifier:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_airpurifier) {channel="miio:basic:airpurifier:SerialNumber"}
Number Pm25Density "Environment-PM2.5 Density" (G_airpurifier) {channel="miio:basic:airpurifier:Pm25Density"}
Number RelativeHumidity "Environment-Relative Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:RelativeHumidity"}
Number Temperature "Environment-Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:Temperature"}
Number FilterLifeLevel "Filter-Filter Life Level" (G_airpurifier) {channel="miio:basic:airpurifier:FilterLifeLevel"}
String FilterUsedTime "Filter-Filter Used Time" (G_airpurifier) {channel="miio:basic:airpurifier:FilterUsedTime"}
Switch Alarm "Alarm-Alarm" (G_airpurifier) {channel="miio:basic:airpurifier:Alarm"}
Number Brightness "Indicator Light-Brightness" (G_airpurifier) {channel="miio:basic:airpurifier:Brightness"}
Switch On1 "Indicator Light-Switch Status" (G_airpurifier) {channel="miio:basic:airpurifier:On1"}
Switch PhysicalControlsLocked "Physical Control Locked-Physical Control Locked" (G_airpurifier) {channel="miio:basic:airpurifier:PhysicalControlsLocked"}
String ButtonPressed "button-button_pressed" (G_airpurifier) {channel="miio:basic:airpurifier:ButtonPressed"}
Number FilterMaxTime "filter-time-filter-max-time" (G_airpurifier) {channel="miio:basic:airpurifier:FilterMaxTime"}
Number FilterHourUsedDebug "filter-time-filter-hour-used-debug" (G_airpurifier) {channel="miio:basic:airpurifier:FilterHourUsedDebug"}
Number M1Strong "motor-speed-m1-strong" (G_airpurifier) {channel="miio:basic:airpurifier:M1Strong"}
Number M1High "motor-speed-m1-high" (G_airpurifier) {channel="miio:basic:airpurifier:M1High"}
Number M1Med "motor-speed-m1-med" (G_airpurifier) {channel="miio:basic:airpurifier:M1Med"}
Number M1MedL "motor-speed-m1-med-l" (G_airpurifier) {channel="miio:basic:airpurifier:M1MedL"}
Number M1Low "motor-speed-m1-low" (G_airpurifier) {channel="miio:basic:airpurifier:M1Low"}
Number M1Silent "motor-speed-m1-silent" (G_airpurifier) {channel="miio:basic:airpurifier:M1Silent"}
Number M1Favorite "motor-speed-m1-favorite" (G_airpurifier) {channel="miio:basic:airpurifier:M1Favorite"}
Number Motor1Speed "motor-speed-motor1-speed" (G_airpurifier) {channel="miio:basic:airpurifier:Motor1Speed"}
Number Motor1SetSpeed "motor-speed-motor1-set-speed" (G_airpurifier) {channel="miio:basic:airpurifier:Motor1SetSpeed"}
Number FavoriteFanLevel "motor-speed-favorite fan level" (G_airpurifier) {channel="miio:basic:airpurifier:FavoriteFanLevel"}
Number UseTime "use-time-use-time" (G_airpurifier) {channel="miio:basic:airpurifier:UseTime"}
Number PurifyVolume "aqi-purify-volume" (G_airpurifier) {channel="miio:basic:airpurifier:PurifyVolume"}
Number AverageAqi "aqi-average-aqi" (G_airpurifier) {channel="miio:basic:airpurifier:AverageAqi"}
Number AverageAqiCnt "aqi-average-aqi-cnt" (G_airpurifier) {channel="miio:basic:airpurifier:AverageAqiCnt"}
String AqiZone "aqi-aqi-zone" (G_airpurifier) {channel="miio:basic:airpurifier:AqiZone"}
String SensorState "aqi-sensor-state" (G_airpurifier) {channel="miio:basic:airpurifier:SensorState"}
Number AqiGoodh "aqi-aqi-goodh" (G_airpurifier) {channel="miio:basic:airpurifier:AqiGoodh"}
Number AqiRunstate "aqi-aqi-runstate" (G_airpurifier) {channel="miio:basic:airpurifier:AqiRunstate"}
Number AqiState "aqi-aqi-state" (G_airpurifier) {channel="miio:basic:airpurifier:AqiState"}
Number AqiUpdataHeartbeat "aqi-aqi-updata-heartbeat" (G_airpurifier) {channel="miio:basic:airpurifier:AqiUpdataHeartbeat"}
String RfidTag "rfid-rfid-tag" (G_airpurifier) {channel="miio:basic:airpurifier:RfidTag"}
String RfidFactoryId "rfid-rfid-factory-id" (G_airpurifier) {channel="miio:basic:airpurifier:RfidFactoryId"}
String RfidProductId "rfid-rfid-product-id" (G_airpurifier) {channel="miio:basic:airpurifier:RfidProductId"}
String RfidTime "rfid-rfid-time" (G_airpurifier) {channel="miio:basic:airpurifier:RfidTime"}
String RfidSerialNum "rfid-rfid-serial-num" (G_airpurifier) {channel="miio:basic:airpurifier:RfidSerialNum"}
Number AppExtra "others-app-extra" (G_airpurifier) {channel="miio:basic:airpurifier:AppExtra"}
Number MainChannel "others-main-channel" (G_airpurifier) {channel="miio:basic:airpurifier:MainChannel"}
Number SlaveChannel "others-slave-channel" (G_airpurifier) {channel="miio:basic:airpurifier:SlaveChannel"}
String Cola "others-cola" (G_airpurifier) {channel="miio:basic:airpurifier:Cola"}
Switch ButtomDoor "others-buttom-door" (G_airpurifier) {channel="miio:basic:airpurifier:ButtomDoor"}
Number RebootCause "others-reboot_cause" (G_airpurifier) {channel="miio:basic:airpurifier:RebootCause"}
Number HwVersion "others-hw-version" (G_airpurifier) {channel="miio:basic:airpurifier:HwVersion"}
Number I2cErrorCount "others-i2c-error-count" (G_airpurifier) {channel="miio:basic:airpurifier:I2cErrorCount"}
Number ManualLevel "others-manual-level" (G_airpurifier) {channel="miio:basic:airpurifier:ManualLevel"}
```

### Mi Air Purifier 3 (zhimi.airpurifier.mb3) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 3" <status>
Number Fault "Air Purifier-fault" (G_airpurifier) {channel="miio:basic:airpurifier:Fault"}
Switch On "Air Purifier-Switch Status" (G_airpurifier) {channel="miio:basic:airpurifier:On"}
Number FanLevel "Air Purifier-Fan Level" (G_airpurifier) {channel="miio:basic:airpurifier:FanLevel"}
Number Mode "Air Purifier-Mode" (G_airpurifier) {channel="miio:basic:airpurifier:Mode"}
String FirmwareRevision "Device Information-Current Firmware Version" (G_airpurifier) {channel="miio:basic:airpurifier:FirmwareRevision"}
String Manufacturer "Device Information-Device Manufacturer" (G_airpurifier) {channel="miio:basic:airpurifier:Manufacturer"}
String Model "Device Information-Device Model" (G_airpurifier) {channel="miio:basic:airpurifier:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_airpurifier) {channel="miio:basic:airpurifier:SerialNumber"}
Number Pm25Density "Environment-PM2.5" (G_airpurifier) {channel="miio:basic:airpurifier:Pm25Density"}
Number RelativeHumidity "Environment-Relative Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:RelativeHumidity"}
Number Temperature "Environment-Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:Temperature"}
Number FilterLifeLevel "Filter-Filter Life Level" (G_airpurifier) {channel="miio:basic:airpurifier:FilterLifeLevel"}
String FilterUsedTime "Filter-Filter Used Time" (G_airpurifier) {channel="miio:basic:airpurifier:FilterUsedTime"}
Switch Alarm "Alarm-Alarm" (G_airpurifier) {channel="miio:basic:airpurifier:Alarm"}
Number Brightness "Indicator Light-brightness" (G_airpurifier) {channel="miio:basic:airpurifier:Brightness"}
Switch On1 "Indicator Light-Switch Status" (G_airpurifier) {channel="miio:basic:airpurifier:On1"}
Switch PhysicalControlsLocked "Physical Control Locked-Physical Control Locked" (G_airpurifier) {channel="miio:basic:airpurifier:PhysicalControlsLocked"}
String ButtonPressed "Button-button-pressed" (G_airpurifier) {channel="miio:basic:airpurifier:ButtonPressed"}
Number FilterMaxTime "filter-time-filter-max-time" (G_airpurifier) {channel="miio:basic:airpurifier:FilterMaxTime"}
Number FilterHourDebug "filter-time-filter-hour-debug" (G_airpurifier) {channel="miio:basic:airpurifier:FilterHourDebug"}
Number MotorStrong "motor-speed-motor-strong" (G_airpurifier) {channel="miio:basic:airpurifier:MotorStrong"}
Number MotorHigh "motor-speed-motor-high" (G_airpurifier) {channel="miio:basic:airpurifier:MotorHigh"}
Number MotorMed "motor-speed-motor-med" (G_airpurifier) {channel="miio:basic:airpurifier:MotorMed"}
Number MotorMedL "motor-speed-motor-med-l" (G_airpurifier) {channel="miio:basic:airpurifier:MotorMedL"}
Number MotorLow "motor-speed-motor-low" (G_airpurifier) {channel="miio:basic:airpurifier:MotorLow"}
Number MotorSilent "motor-speed-motor-silent" (G_airpurifier) {channel="miio:basic:airpurifier:MotorSilent"}
Number MotorFavorite "motor-speed-motor-favorite" (G_airpurifier) {channel="miio:basic:airpurifier:MotorFavorite"}
Number MotorSpeed "motor-speed-motor-speed" (G_airpurifier) {channel="miio:basic:airpurifier:MotorSpeed"}
Number MotorSetSpeed "motor-speed-motor-set-speed" (G_airpurifier) {channel="miio:basic:airpurifier:MotorSetSpeed"}
Number FavoriteFanLevel "motor-speed-favorite-fan-level" (G_airpurifier) {channel="miio:basic:airpurifier:FavoriteFanLevel"}
Number UseTime "use-time-use-time" (G_airpurifier) {channel="miio:basic:airpurifier:UseTime"}
Number PurifyVolume "aqi-purify-volume" (G_airpurifier) {channel="miio:basic:airpurifier:PurifyVolume"}
Number AverageAqi "aqi-average-aqi" (G_airpurifier) {channel="miio:basic:airpurifier:AverageAqi"}
Number AverageAqiCnt "aqi-average-aqi-cnt" (G_airpurifier) {channel="miio:basic:airpurifier:AverageAqiCnt"}
String AqiZone "aqi-aqi-zone" (G_airpurifier) {channel="miio:basic:airpurifier:AqiZone"}
String SensorState "aqi-sensor-state" (G_airpurifier) {channel="miio:basic:airpurifier:SensorState"}
Number AqiGoodh "aqi-aqi-goodh" (G_airpurifier) {channel="miio:basic:airpurifier:AqiGoodh"}
Number AqiRunstate "aqi-aqi-runstate" (G_airpurifier) {channel="miio:basic:airpurifier:AqiRunstate"}
Number AqiState "aqi-aqi-state" (G_airpurifier) {channel="miio:basic:airpurifier:AqiState"}
Number AqiUpdataHeartbeat "aqi-aqi-updata-heartbeat" (G_airpurifier) {channel="miio:basic:airpurifier:AqiUpdataHeartbeat"}
String RfidTag "rfid-rfid-tag" (G_airpurifier) {channel="miio:basic:airpurifier:RfidTag"}
String RfidFactoryId "rfid-rfid-factory-id" (G_airpurifier) {channel="miio:basic:airpurifier:RfidFactoryId"}
String RfidProductId "rfid-rfid-product-id" (G_airpurifier) {channel="miio:basic:airpurifier:RfidProductId"}
String RfidTime "rfid-rfid-time" (G_airpurifier) {channel="miio:basic:airpurifier:RfidTime"}
String RfidSerialNum "rfid-rfid-serial-num" (G_airpurifier) {channel="miio:basic:airpurifier:RfidSerialNum"}
Number AppExtra "others-app-extra" (G_airpurifier) {channel="miio:basic:airpurifier:AppExtra"}
Number MainChannel "others-main-channel" (G_airpurifier) {channel="miio:basic:airpurifier:MainChannel"}
Number SlaveChannel "others-slave-channel" (G_airpurifier) {channel="miio:basic:airpurifier:SlaveChannel"}
String Cola "others-cola" (G_airpurifier) {channel="miio:basic:airpurifier:Cola"}
Switch ButtomDoor "others-buttom-door" (G_airpurifier) {channel="miio:basic:airpurifier:ButtomDoor"}
Number RebootCause "others-reboot-cause" (G_airpurifier) {channel="miio:basic:airpurifier:RebootCause"}
Number HwVersion "others-hw-version" (G_airpurifier) {channel="miio:basic:airpurifier:HwVersion"}
Number IicErrorCount "others-iic-error-count" (G_airpurifier) {channel="miio:basic:airpurifier:IicErrorCount"}
Number ManualLevel "others-manual-level" (G_airpurifier) {channel="miio:basic:airpurifier:ManualLevel"}
Number CountryCode "others-National code" (G_airpurifier) {channel="miio:basic:airpurifier:CountryCode"}
```

### Mi Air Purifier Super (zhimi.airpurifier.sa1) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier Super" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier Super 2 (zhimi.airpurifier.sa2) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier Super 2" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Fresh Air Ventilator (dmaker.airfresh.t2017) item file lines

note: Autogenerated example. Replace the id (airfresh) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airfresh "Mi Fresh Air Ventilator" <status>
Switch power "Power" (G_airfresh) {channel="miio:basic:airfresh:power"}
String airFreshMode "Mode" (G_airfresh) {channel="miio:basic:airfresh:airFreshMode"}
Switch airFreshPTCPower "PTC" (G_airfresh) {channel="miio:basic:airfresh:airFreshPTCPower"}
String airFreshPtcLevel "PTC Level" (G_airfresh) {channel="miio:basic:airfresh:airFreshPtcLevel"}
Switch airFreshPTCStatus "PTC Status" (G_airfresh) {channel="miio:basic:airfresh:airFreshPTCStatus"}
String airFreshDisplayDirection "Screen direction" (G_airfresh) {channel="miio:basic:airfresh:airFreshDisplayDirection"}
Switch airFreshDisplay "Display" (G_airfresh) {channel="miio:basic:airfresh:airFreshDisplay"}
Switch airFreshChildLock "Child Lock" (G_airfresh) {channel="miio:basic:airfresh:airFreshChildLock"}
Switch airFreshSound "Sound" (G_airfresh) {channel="miio:basic:airfresh:airFreshSound"}
Number airFreshPM25 "PM2.5" (G_airfresh) {channel="miio:basic:airfresh:airFreshPM25"}
Number airFreshCO2 "CO2" (G_airfresh) {channel="miio:basic:airfresh:airFreshCO2"}
Number airFreshCurrentSpeed "Current Speed" (G_airfresh) {channel="miio:basic:airfresh:airFreshCurrentSpeed"}
Number airFreshFavoriteSpeed "Favorite Speed" (G_airfresh) {channel="miio:basic:airfresh:airFreshFavoriteSpeed"}
Number airFreshTemperature "Temperature Outside" (G_airfresh) {channel="miio:basic:airfresh:airFreshTemperature"}
Number airFreshFilterPercents "Filter Percents Remaining" (G_airfresh) {channel="miio:basic:airfresh:airFreshFilterPercents"}
Number airFreshFilterDays "Filter Days Remaining" (G_airfresh) {channel="miio:basic:airfresh:airFreshFilterDays"}
Number airFreshFilterProPercents "Filter Pro Percents Remaining" (G_airfresh) {channel="miio:basic:airfresh:airFreshFilterProPercents"}
Number airFreshFilterProDays "Filter Pro Days Remaining" (G_airfresh) {channel="miio:basic:airfresh:airFreshFilterProDays"}
String airFreshResetFilter "Reset Filter" (G_airfresh) {channel="miio:basic:airfresh:airFreshResetFilter"}
```

### Mi Fresh Air Ventilator A1 (dmaker.airfresh.a1) item file lines

note: Autogenerated example. Replace the id (airfresh) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airfresh "Mi Fresh Air Ventilator A1" <status>
Switch power "Power" (G_airfresh) {channel="miio:basic:airfresh:power"}
String airFreshMode "Mode" (G_airfresh) {channel="miio:basic:airfresh:airFreshMode"}
Switch airFreshPTCPower "PTC" (G_airfresh) {channel="miio:basic:airfresh:airFreshPTCPower"}
Switch airFreshPTCStatus "PTC Status" (G_airfresh) {channel="miio:basic:airfresh:airFreshPTCStatus"}
Switch airFreshDisplay "Display" (G_airfresh) {channel="miio:basic:airfresh:airFreshDisplay"}
Switch airFreshChildLock "Child Lock" (G_airfresh) {channel="miio:basic:airfresh:airFreshChildLock"}
Switch airFreshSound "Sound" (G_airfresh) {channel="miio:basic:airfresh:airFreshSound"}
Number airFreshPM25 "PM2.5" (G_airfresh) {channel="miio:basic:airfresh:airFreshPM25"}
Number airFreshCO2 "CO2" (G_airfresh) {channel="miio:basic:airfresh:airFreshCO2"}
Number airFreshCurrentSpeed "Current Speed" (G_airfresh) {channel="miio:basic:airfresh:airFreshCurrentSpeed"}
Number airFreshFavoriteSpeed "Favorite Speed" (G_airfresh) {channel="miio:basic:airfresh:airFreshFavoriteSpeed"}
Number airFreshTemperature "Temperature Outside" (G_airfresh) {channel="miio:basic:airfresh:airFreshTemperature"}
Number airFreshFilterPercents "Filter Percents Remaining" (G_airfresh) {channel="miio:basic:airfresh:airFreshFilterPercents"}
Number airFreshFilterDays "Filter Days Remaining" (G_airfresh) {channel="miio:basic:airfresh:airFreshFilterDays"}
String airFreshResetFilterA1 "Reset Filter" (G_airfresh) {channel="miio:basic:airfresh:airFreshResetFilterA1"}
```

### Gosund Plug (cuco.plug.cp1) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Gosund Plug" <status>
String FirmwareRevision "Device Information-CurrentFirmware Version" (G_plug) {channel="miio:basic:plug:FirmwareRevision"}
String Manufacturer "Device Information-Device Manufacturer" (G_plug) {channel="miio:basic:plug:Manufacturer"}
String Model "Device Information-Device Model" (G_plug) {channel="miio:basic:plug:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_plug) {channel="miio:basic:plug:SerialNumber"}
Switch On "Switch-Switch Status" (G_plug) {channel="miio:basic:plug:On"}
```

### Mi Air Purifier mb1 (zhimi.airpurifier.mb1) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier mb1" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier 2S (zhimi.airpurifier.mc1) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 2S" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter  Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Smart Fan (zhimi.fan.v1) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
Number angle "Angle" (G_fan) {channel="miio:basic:fan:angle"}
Number poweroffTime "Timer" (G_fan) {channel="miio:basic:fan:poweroffTime"}
Switch buzzer "Buzzer" (G_fan) {channel="miio:basic:fan:buzzer"}
Number led_b "LED" (G_fan) {channel="miio:basic:fan:led_b"}
Switch child_lock "Child Lock" (G_fan) {channel="miio:basic:fan:child_lock"}
Number speedLevel "Speed Level" (G_fan) {channel="miio:basic:fan:speedLevel"}
Number speed "Speed" (G_fan) {channel="miio:basic:fan:speed"}
Number naturalLevel "Natural Level" (G_fan) {channel="miio:basic:fan:naturalLevel"}
Number temp_dec "Temperature" (G_fan) {channel="miio:basic:fan:temp_dec"}
Number humidity "Humidity" (G_fan) {channel="miio:basic:fan:humidity"}
String acPower "AC Power" (G_fan) {channel="miio:basic:fan:acPower"}
String mode "Battery Charge" (G_fan) {channel="miio:basic:fan:mode"}
Number battery "Battery" (G_fan) {channel="miio:basic:fan:battery"}
String move "Move Direction" (G_fan) {channel="miio:basic:fan:move"}
```

### Mi Smart Fan (zhimi.fan.v2) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
Number angle "Angle" (G_fan) {channel="miio:basic:fan:angle"}
Number poweroffTime "Timer" (G_fan) {channel="miio:basic:fan:poweroffTime"}
Switch buzzer "Buzzer" (G_fan) {channel="miio:basic:fan:buzzer"}
Number led_b "LED" (G_fan) {channel="miio:basic:fan:led_b"}
Switch child_lock "Child Lock" (G_fan) {channel="miio:basic:fan:child_lock"}
Number speedLevel "Speed Level" (G_fan) {channel="miio:basic:fan:speedLevel"}
Number speed "Speed" (G_fan) {channel="miio:basic:fan:speed"}
Number naturalLevel "Natural Level" (G_fan) {channel="miio:basic:fan:naturalLevel"}
Number temp_dec "Temperature" (G_fan) {channel="miio:basic:fan:temp_dec"}
Number humidity "Humidity" (G_fan) {channel="miio:basic:fan:humidity"}
String acPower "AC Power" (G_fan) {channel="miio:basic:fan:acPower"}
String mode "Battery Charge" (G_fan) {channel="miio:basic:fan:mode"}
Number battery "Battery" (G_fan) {channel="miio:basic:fan:battery"}
String move "Move Direction" (G_fan) {channel="miio:basic:fan:move"}
```

### Mi Smart Pedestal Fan (zhimi.fan.v3) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Pedestal Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
Number angle "Angle" (G_fan) {channel="miio:basic:fan:angle"}
Number poweroffTime "Timer" (G_fan) {channel="miio:basic:fan:poweroffTime"}
Switch buzzer "Buzzer" (G_fan) {channel="miio:basic:fan:buzzer"}
Number led_b "LED" (G_fan) {channel="miio:basic:fan:led_b"}
Switch child_lock "Child Lock" (G_fan) {channel="miio:basic:fan:child_lock"}
Number speedLevel "Speed Level" (G_fan) {channel="miio:basic:fan:speedLevel"}
Number speed "Speed" (G_fan) {channel="miio:basic:fan:speed"}
Number naturalLevel "Natural Level" (G_fan) {channel="miio:basic:fan:naturalLevel"}
Number temp_dec "Temperature" (G_fan) {channel="miio:basic:fan:temp_dec"}
Number humidity "Humidity" (G_fan) {channel="miio:basic:fan:humidity"}
String acPower "AC Power" (G_fan) {channel="miio:basic:fan:acPower"}
String mode "Battery Charge" (G_fan) {channel="miio:basic:fan:mode"}
Number battery "Battery" (G_fan) {channel="miio:basic:fan:battery"}
String move "Move Direction" (G_fan) {channel="miio:basic:fan:move"}
```

### Xiaomi Mi Smart Pedestal Fan (zhimi.fan.sa1) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Xiaomi Mi Smart Pedestal Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
Number angle "Angle" (G_fan) {channel="miio:basic:fan:angle"}
Number poweroffTime "Timer" (G_fan) {channel="miio:basic:fan:poweroffTime"}
Switch buzzer "Buzzer" (G_fan) {channel="miio:basic:fan:buzzer"}
Number led_b "LED" (G_fan) {channel="miio:basic:fan:led_b"}
Switch child_lock "Child Lock" (G_fan) {channel="miio:basic:fan:child_lock"}
Number speedLevel "Speed Level" (G_fan) {channel="miio:basic:fan:speedLevel"}
Number speed "Speed" (G_fan) {channel="miio:basic:fan:speed"}
Number naturalLevel "Natural Level" (G_fan) {channel="miio:basic:fan:naturalLevel"}
Switch acPower "AC Power" (G_fan) {channel="miio:basic:fan:acPower"}
String move "Move Direction" (G_fan) {channel="miio:basic:fan:move"}
```

### Xiaomi Mi Smart Pedestal Fan (zhimi.fan.za1) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Xiaomi Mi Smart Pedestal Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
Number angle "Angle" (G_fan) {channel="miio:basic:fan:angle"}
Number poweroffTime "Timer" (G_fan) {channel="miio:basic:fan:poweroffTime"}
Switch buzzer "Buzzer" (G_fan) {channel="miio:basic:fan:buzzer"}
Number led_b "LED" (G_fan) {channel="miio:basic:fan:led_b"}
Switch child_lock "Child Lock" (G_fan) {channel="miio:basic:fan:child_lock"}
Number speedLevel "Speed Level" (G_fan) {channel="miio:basic:fan:speedLevel"}
Number speed "Speed" (G_fan) {channel="miio:basic:fan:speed"}
Number naturalLevel "Natural Level" (G_fan) {channel="miio:basic:fan:naturalLevel"}
Switch acPower "AC Power" (G_fan) {channel="miio:basic:fan:acPower"}
String move "Move Direction" (G_fan) {channel="miio:basic:fan:move"}
```

### Mi Humdifier (zhimi.humidifier.v1) item file lines

note: Autogenerated example. Replace the id (humidifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_humidifier "Mi Humdifier" <status>
Switch power "Power" (G_humidifier) {channel="miio:basic:humidifier:power"}
String mode "Mode" (G_humidifier) {channel="miio:basic:humidifier:mode"}
Number humidity "Humidity" (G_humidifier) {channel="miio:basic:humidifier:humidity"}
Number setHumidity "Humidity Set" (G_humidifier) {channel="miio:basic:humidifier:setHumidity"}
Number aqi "Air Quality Index" (G_humidifier) {channel="miio:basic:humidifier:aqi"}
Number translevel "Trans_level" (G_humidifier) {channel="miio:basic:humidifier:translevel"}
Number bright "LED Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
Switch buzzer "Buzzer Status" (G_humidifier) {channel="miio:basic:humidifier:buzzer"}
Number depth "Depth" (G_humidifier) {channel="miio:basic:humidifier:depth"}
Switch dry "Dry" (G_humidifier) {channel="miio:basic:humidifier:dry"}
Number usedhours "Run Time" (G_humidifier) {channel="miio:basic:humidifier:usedhours"}
Number motorspeed "Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:motorspeed"}
Number temperature "Temperature" (G_humidifier) {channel="miio:basic:humidifier:temperature"}
Switch childlock "Child Lock" (G_humidifier) {channel="miio:basic:humidifier:childlock"}
```

### Xiaomi Philips Eyecare Smart Lamp 2 (philips.light.sread1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips Eyecare Smart Lamp 2" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Switch ambientPower "Ambient Power" (G_light) {channel="miio:basic:light:ambientPower"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number illumination "Ambient Illumination" (G_light) {channel="miio:basic:light:illumination"}
Switch eyecare "Eyecare" (G_light) {channel="miio:basic:light:eyecare"}
```

### Xiaomi Philips LED Ceiling Lamp (philips.light.ceiling) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips LED Ceiling Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Xiaomi Philips LED Ceiling Lamp (philips.light.zyceiling) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips LED Ceiling Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Xiaomi Philips Bulb (philips.light.bulb) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp (philips.light.candle) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Xiaomi Philips Downlight (philips.light.downlight) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips Downlight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Xiaomi Philips ZhiRui bedside lamp (philips.light.moonlight) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips ZhiRui bedside lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch gonight "Go Night" (G_light) {channel="miio:basic:light:gonight"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal (philips.light.candle2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### philips.light.mono1 (philips.light.mono1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "philips.light.mono1" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
```

### philips.light.virtual (philips.light.virtual) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "philips.light.virtual" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### philips.light.zysread (philips.light.zysread) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "philips.light.zysread" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### philips.light.zystrip (philips.light.zystrip) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "philips.light.zystrip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Mi Power-plug (chuangmi.plug.m1) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Power-plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Indicator light" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Power-plug v1 (chuangmi.plug.v1) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Power-plug v1" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
```

### Mi Power-plug v2 (chuangmi.plug.v2) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Power-plug v2" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
```

### Mi Power-plug v3 (chuangmi.plug.v3) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Power-plug v3" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Wifi LED" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Power-plug (chuangmi.plug.m3) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Power-plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Indicator light" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Smart Plug (chuangmi.plug.hmi205) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Indicator light" (G_plug) {channel="miio:basic:plug:led"}
```

### Qing Mi Smart Power Strip v1 (qmi.powerstrip.v1) item file lines

note: Autogenerated example. Replace the id (powerstrip) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_powerstrip "Qing Mi Smart Power Strip v1" <status>
Switch power "Power" (G_powerstrip) {channel="miio:basic:powerstrip:power"}
Number powerUsage "Power Consumption" (G_powerstrip) {channel="miio:basic:powerstrip:powerUsage"}
Switch led "wifi LED" (G_powerstrip) {channel="miio:basic:powerstrip:led"}
Number power_price "power_price" (G_powerstrip) {channel="miio:basic:powerstrip:power_price"}
Number current "Current" (G_powerstrip) {channel="miio:basic:powerstrip:current"}
Number temperature "Temperature" (G_powerstrip) {channel="miio:basic:powerstrip:temperature"}
```

### Mi Power-strip v2 (zimi.powerstrip.v2) item file lines

note: Autogenerated example. Replace the id (powerstrip) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_powerstrip "Mi Power-strip v2" <status>
Switch power "Power" (G_powerstrip) {channel="miio:basic:powerstrip:power"}
Number powerUsage "Power Consumption" (G_powerstrip) {channel="miio:basic:powerstrip:powerUsage"}
Switch led "wifi LED" (G_powerstrip) {channel="miio:basic:powerstrip:led"}
Number power_price "power_price" (G_powerstrip) {channel="miio:basic:powerstrip:power_price"}
Number current "Current" (G_powerstrip) {channel="miio:basic:powerstrip:current"}
Number temperature "Temperature" (G_powerstrip) {channel="miio:basic:powerstrip:temperature"}
```

### Vacuum 1C STYTJ01ZHM (dreame.vacuum.mc1808) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Vacuum 1C STYTJ01ZHM" <status>
Number BatteryLevel "Battery-Battery Level" (G_vacuum) {channel="miio:basic:vacuum:BatteryLevel"}
Number ChargingState "Battery-Charging State" (G_vacuum) {channel="miio:basic:vacuum:ChargingState"}
String FirmwareRevision "Device Information-Current Firmware Version" (G_vacuum) {channel="miio:basic:vacuum:FirmwareRevision"}
String Manufacturer "Device Information-Device Manufacturer" (G_vacuum) {channel="miio:basic:vacuum:Manufacturer"}
String Model "Device Information-Device Model" (G_vacuum) {channel="miio:basic:vacuum:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_vacuum) {channel="miio:basic:vacuum:SerialNumber"}
Number Fault "Robot Cleaner-Device Fault" (G_vacuum) {channel="miio:basic:vacuum:Fault"}
Number Status "Robot Cleaner-Status" (G_vacuum) {channel="miio:basic:vacuum:Status"}
String BrushLeftTime "Main Cleaning Brush-Brush Left Time" (G_vacuum) {channel="miio:basic:vacuum:BrushLeftTime"}
Number BrushLifeLevel "Main Cleaning Brush-Brush Life Level" (G_vacuum) {channel="miio:basic:vacuum:BrushLifeLevel"}
Number FilterLifeLevel "Filter-Filter Life Level" (G_vacuum) {channel="miio:basic:vacuum:FilterLifeLevel"}
String FilterLeftTime "Filter-Filter Left Time" (G_vacuum) {channel="miio:basic:vacuum:FilterLeftTime"}
String BrushLeftTime1 "Side Cleaning Brush-Brush Left Time" (G_vacuum) {channel="miio:basic:vacuum:BrushLeftTime1"}
Number BrushLifeLevel1 "Side Cleaning Brush-Brush Life Level" (G_vacuum) {channel="miio:basic:vacuum:BrushLifeLevel1"}
Number WorkMode "clean-workmode" (G_vacuum) {channel="miio:basic:vacuum:WorkMode"}
String Area "clean-area" (G_vacuum) {channel="miio:basic:vacuum:Area"}
String Timer "clean-timer" (G_vacuum) {channel="miio:basic:vacuum:Timer"}
Number Mode "clean-mode" (G_vacuum) {channel="miio:basic:vacuum:Mode"}
String TotalCleanTime "clean-total time" (G_vacuum) {channel="miio:basic:vacuum:TotalCleanTime"}
String TotalCleanTimes "clean-total times" (G_vacuum) {channel="miio:basic:vacuum:TotalCleanTimes"}
String TotalCleanArea "clean-Total area" (G_vacuum) {channel="miio:basic:vacuum:TotalCleanArea"}
String CleanLogStartTime "clean-Start Time" (G_vacuum) {channel="miio:basic:vacuum:CleanLogStartTime"}
String ButtonLed "clean-led" (G_vacuum) {channel="miio:basic:vacuum:ButtonLed"}
Number TaskDone "clean-task done" (G_vacuum) {channel="miio:basic:vacuum:TaskDone"}
String LifeSieve "consumable-life-sieve" (G_vacuum) {channel="miio:basic:vacuum:LifeSieve"}
String LifeBrushSide "consumable-life-brush-side" (G_vacuum) {channel="miio:basic:vacuum:LifeBrushSide"}
String LifeBrushMain "consumable-life-brush-main" (G_vacuum) {channel="miio:basic:vacuum:LifeBrushMain"}
Switch Enable "annoy-enable" (G_vacuum) {channel="miio:basic:vacuum:Enable"}
String StartTime "annoy-start-time" (G_vacuum) {channel="miio:basic:vacuum:StartTime"}
String StopTime "annoy-stop-time" (G_vacuum) {channel="miio:basic:vacuum:StopTime"}
String MapView "map-map-view" (G_vacuum) {channel="miio:basic:vacuum:MapView"}
Number Volume "audio-volume" (G_vacuum) {channel="miio:basic:vacuum:Volume"}
String VoicePackets "audio-voiceId" (G_vacuum) {channel="miio:basic:vacuum:VoicePackets"}
String TimeZone "timezone" (G_vacuum) {channel="miio:basic:vacuum:TimeZone"}
```

###  Mijia 1 Gang Wall Smart Switch (WIFI) - PTX switch (090615.switch.xswitch01) item file lines

note: Autogenerated example. Replace the id (switch) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_switch " Mijia 1 Gang Wall Smart Switch (WIFI) - PTX switch" <status>
Number switch1state "Switch 1" (G_switch) {channel="miio:basic:switch:switch1state"}
String switch1name "Switch Name 1" (G_switch) {channel="miio:basic:switch:switch1name"}
```

###  Mijia 2 Gang Wall Smart Switch (WIFI) - PTX switch (090615.switch.xswitch02) item file lines

note: Autogenerated example. Replace the id (switch) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_switch " Mijia 2 Gang Wall Smart Switch (WIFI) - PTX switch" <status>
Number switch1state "Switch 1" (G_switch) {channel="miio:basic:switch:switch1state"}
Number switch2state "Switch 2" (G_switch) {channel="miio:basic:switch:switch2state"}
String switch1name "Switch Name 1" (G_switch) {channel="miio:basic:switch:switch1name"}
String switch2name "Switch Name 2" (G_switch) {channel="miio:basic:switch:switch2name"}
```

###  Mijia 3 Gang Wall Smart Switch (WIFI) - PTX switch (090615.switch.xswitch03) item file lines

note: Autogenerated example. Replace the id (switch) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_switch " Mijia 3 Gang Wall Smart Switch (WIFI) - PTX switch" <status>
Number switch1state "Switch 1" (G_switch) {channel="miio:basic:switch:switch1state"}
Number switch2state "Switch 2" (G_switch) {channel="miio:basic:switch:switch2state"}
Number switch3state "Switch 3" (G_switch) {channel="miio:basic:switch:switch3state"}
String switch1name "Switch Name 1" (G_switch) {channel="miio:basic:switch:switch1name"}
String switch2name "Switch Name 2" (G_switch) {channel="miio:basic:switch:switch2name"}
String switch3name "Switch Name 3" (G_switch) {channel="miio:basic:switch:switch3name"}
```

### Mi Water Purifier v2 (yunmi.waterpuri.v2) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier v2" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
```

### Mi Water Purifier lx2 (yunmi.waterpuri.lx2) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx2" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
```

### Mi Water Purifier lx3 (yunmi.waterpuri.lx3) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx3" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
```

### Mi Water Purifier lx4 (yunmi.waterpuri.lx4) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx4" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
```

### Mi Water Purifier v2 (yunmi.waterpurifier.v2) item file lines

note: Autogenerated example. Replace the id (waterpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpurifier "Mi Water Purifier v2" <status>
Switch power "Power" (G_waterpurifier) {channel="miio:basic:waterpurifier:power"}
```

### Mi Water Purifier v3 (yunmi.waterpurifier.v3) item file lines

note: Autogenerated example. Replace the id (waterpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpurifier "Mi Water Purifier v3" <status>
Switch power "Power" (G_waterpurifier) {channel="miio:basic:waterpurifier:power"}
```

### Mi Water Purifier v4 (yunmi.waterpurifier.v4) item file lines

note: Autogenerated example. Replace the id (waterpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpurifier "Mi Water Purifier v4" <status>
Switch power "Power" (G_waterpurifier) {channel="miio:basic:waterpurifier:power"}
```

### Yeelight Lamp (yeelink.light.bslamp1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Lamp (yeelink.light.bslamp2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v2 (yeelink.light.ceiling2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v2" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v3 (yeelink.light.ceiling3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v3" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB) (yeelink.light.ceiling4) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB)" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
Switch ambientPower "Ambient Power" (G_light) {channel="miio:basic:light:ambientPower"}
Color ambientColor "Ambient Color" (G_light) {channel="miio:basic:light:ambientColor"}
Number ambientColorTemperature "Ambient Color Temperature" (G_light) {channel="miio:basic:light:ambientColorTemperature"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number ambientColorMode "Ambient Color Mode" (G_light) {channel="miio:basic:light:ambientColorMode"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v4 (yeelink.light.ceiling4.ambi) item file lines

note: Autogenerated example. Replace the id (ceiling4) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_ceiling4 "Yeelight LED Ceiling Lamp v4" <status>
Switch power "Power" (G_ceiling4) {channel="miio:basic:ceiling4:power"}
Dimmer brightness "Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:brightness"}
Number ambientBrightness "Ambient Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:ambientBrightness"}
Number delayoff "Shutdown Timer" (G_ceiling4) {channel="miio:basic:ceiling4:delayoff"}
Number colorTemperature "Color Temperature" (G_ceiling4) {channel="miio:basic:ceiling4:colorTemperature"}
Number colorMode "Color Mode" (G_ceiling4) {channel="miio:basic:ceiling4:colorMode"}
String name "Name" (G_ceiling4) {channel="miio:basic:ceiling4:name"}
Switch ambientPower "Ambient Power" (G_ceiling4) {channel="miio:basic:ceiling4:ambientPower"}
Color ambientColor "Ambient Color" (G_ceiling4) {channel="miio:basic:ceiling4:ambientColor"}
Number ambientColorTemperature "Ambient Color Temperature" (G_ceiling4) {channel="miio:basic:ceiling4:ambientColorTemperature"}
String customScene "Set Scene" (G_ceiling4) {channel="miio:basic:ceiling4:customScene"}
Number ambientColorMode "Ambient Color Mode" (G_ceiling4) {channel="miio:basic:ceiling4:ambientColorMode"}
Number nightlightBrightness "Nightlight Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v5 (yeelink.light.ceiling5) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v5" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v6 (yeelink.light.ceiling6) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v6" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v7 (yeelink.light.ceiling7) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v7" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v8 (yeelink.light.ceiling8) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v8" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v9 (yeelink.light.ceiling9) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v9" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Meteorite lamp (yeelink.light.ceiling10) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Meteorite lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
Switch ambientPower "Ambient Power" (G_light) {channel="miio:basic:light:ambientPower"}
Color ambientColor "Ambient Color" (G_light) {channel="miio:basic:light:ambientColor"}
Number ambientColorTemperature "Ambient Color Temperature" (G_light) {channel="miio:basic:light:ambientColorTemperature"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number ambientColorMode "Ambient Color Mode" (G_light) {channel="miio:basic:light:ambientColorMode"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v11 (yeelink.light.ceiling11) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v11" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v12 (yeelink.light.ceiling12) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v12" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Lamp v13 (yeelink.light.ceiling13) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Lamp v13" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight ct2 (yeelink.light.ct2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight ct2" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight White Bulb (yeelink.light.mono1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight White Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight White Bulb v2 (yeelink.light.mono2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight White Bulb v2" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.lamp1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.lamp2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.lamp3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Strip (yeelink.light.strip1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Strip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Strip (yeelink.light.strip2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Strip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.virtual) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Color Bulb (yeelink.light.color1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Color Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Color Bulb YLDP06YL 10W (yeelink.light.color2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Color Bulb YLDP06YL 10W" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Color Bulb YLDP02YL 9W (yeelink.light.color3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Color Bulb YLDP02YL 9W" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Bulb YLDP13YL (8,5W) (yeelink.light.color4) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Bulb YLDP13YL (8,5W)" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```


