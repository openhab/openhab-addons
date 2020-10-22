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

No binding configuration is required. However to enable cloud functionality enter your Xiaomi username, password and server(s).
The list of the known countries and related severs is [here](#Country-Servers)
After successful Xiaomi cloud login, the binding will use the connection to retrieve the required device tokens from the cloud. 
For Xiaomi vacuums the map can be visualized in openHAB using the cloud connection.

![Binding Config](doc/miioBindingConfig.jpg)

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

`Thing miio:basic:light "My Light" [ host="192.168.x.x", token="put here your token", deviceId="0326xxxx", model="philips.light.bulb" ]` 

or in case of unknown models include the model information of a similar device that is supported:

`Thing miio:vacuum:s50 "vacuum" @ "livingroom" [ host="192.168.15.20", token="xxxxxxx", deviceId=“0470DDAA”, model="roborock.vacuum.s4" ]`

# Mi IO Devices

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
| Mi Air Purifier 2S           | miio:basic       | [zhimi.airpurifier.mc2](#zhimi-airpurifier-mc2) | Yes       |            |
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
| Xiaomi Mi Smart Pedestal Fan | miio:basic       | [zhimi.fan.za4](#zhimi-fan-za4) | Yes       |            |
| Xiaomi Mijia Smart Tower Fan | miio:basic       | [dmaker.fan.1c](#dmaker-fan-1c) | Yes       |            |
| Xiaomi Mijia Smart Tower Fan | miio:basic       | [dmaker.fan.p5](#dmaker-fan-p5) | Yes       |            |
| Xiaomi Mijia Smart Tower Fan | miio:basic       | [dmaker.fan.p8](#dmaker-fan-p8) | Yes       |            |
| Xiaomi Mijia Smart Tower Fan | miio:basic       | [dmaker.fan.p9](#dmaker-fan-p9) | Yes       |            |
| Xiaomi Mijia Smart Tower Fan | miio:basic       | [dmaker.fan.p10](#dmaker-fan-p10) | Yes       |            |
| Viomi Internet refrigerator iLive | miio:unsupported | viomi.fridge.v3        | No        |            |
| Mi Smart Home Gateway v1     | miio:basic       | [lumi.gateway.v1](#lumi-gateway-v1) | Yes       | Used to control the gateway itself. Use the mihome binding to control devices connected to the Xiaomi gateway. Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Home Gateway v2     | miio:basic       | [lumi.gateway.v2](#lumi-gateway-v2) | Yes       | Used to control the gateway itself. Use the mihome binding to control devices connected to the Xiaomi gateway. Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Home Gateway v3     | miio:basic       | [lumi.gateway.v3](#lumi-gateway-v3) | Yes       | Used to control the gateway itself. Use the mihome binding to control devices connected to the Xiaomi gateway. Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Xiaomi Mi Mijia Gateway V3 ZNDMWG03LM | miio:basic       | [lumi.gateway.mgl03](#lumi-gateway-mgl03) | Yes       | Used to control the gateway itself. Use the mihome binding to control devices connected to the Xiaomi gateway. Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Humdifier                 | miio:basic       | [zhimi.humidifier.v1](#zhimi-humidifier-v1) | Yes       |            |
| Light Control (Wall Switch)  | miio:unsupported | lumi.ctrl_neutral1.v1  | No        |            |
| Light Control (Wall Switch)  | miio:unsupported | lumi.ctrl_neutral2.v1  | No        |            |
| Mr Bond M1 Pro Smart Clothes Dryer | miio:basic       | [mrbond.airer.m1pro](#mrbond-airer-m1pro) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mr Bond M1 Smart Clothes Dryer | miio:basic       | [mrbond.airer.m1s](#mrbond-airer-m1s) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mr Bond M1 Super Smart Clothes Dryer | miio:basic       | [mrbond.airer.m1super](#mrbond-airer-m1super) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Xiaomi Philips Eyecare Smart Lamp 2 | miio:basic       | [philips.light.sread1](#philips-light-sread1) | Yes       |            |
| Xiaomi Philips Eyecare Smart Lamp 2 | miio:basic       | [philips.light.sread2](#philips-light-sread2) | Yes       |            |
| Xiaomi Philips LED Ceiling Lamp | miio:basic       | [philips.light.ceiling](#philips-light-ceiling) | Yes       |            |
| Xiaomi Philips LED Ceiling Lamp | miio:basic       | [philips.light.zyceiling](#philips-light-zyceiling) | Yes       |            |
| Xiaomi Philips Bulb          | miio:basic       | [philips.light.bulb](#philips-light-bulb) | Yes       |            |
| Xiaomi Philips Wi-Fi Bulb E27 White | miio:basic       | [philips.light.hbulb](#philips-light-hbulb) | Yes       |            |
| PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp | miio:basic       | [philips.light.candle](#philips-light-candle) | Yes       |            |
| Xiaomi Philips Downlight     | miio:basic       | [philips.light.downlight](#philips-light-downlight) | Yes       |            |
| Xiaomi Philips ZhiRui bedside lamp | miio:basic       | [philips.light.moonlight](#philips-light-moonlight) | Yes       |            |
| Philips Ceiling Light        | miio:basic       | [philips.light.bceiling1](#philips-light-bceiling1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.bceiling2](#philips-light-bceiling2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Light                | miio:basic       | [philips.light.cbulb](#philips-light-cbulb) | Yes       |            |
| Philips Light                | miio:basic       | [philips.light.cbulbs](#philips-light-cbulbs) | Yes       |            |
| Philips Light                | miio:basic       | [philips.light.dcolor](#philips-light-dcolor) | Yes       |            |
| Philips Light                | miio:basic       | [philips.light.rwread](#philips-light-rwread) | Yes       |            |
| Philips Light                | miio:basic       | [philips.light.lnblight1](#philips-light-lnblight1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Light                | miio:basic       | [philips.light.lnblight2](#philips-light-lnblight2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Light                | miio:basic       | [philips.light.lnlrlight](#philips-light-lnlrlight) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Light                | miio:basic       | [philips.light.lrceiling](#philips-light-lrceiling) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal | miio:basic       | [philips.light.candle2](#philips-light-candle2) | Yes       |            |
| philips.light.mono1          | miio:basic       | [philips.light.mono1](#philips-light-mono1) | Yes       |            |
| Philips Down Light           | miio:basic       | [philips.light.dlight](#philips-light-dlight) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.mceil](#philips-light-mceil) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.mceilm](#philips-light-mceilm) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.mceils](#philips-light-mceils) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.obceil](#philips-light-obceil) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.obceim](#philips-light-obceim) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.obceis](#philips-light-obceis) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.sceil](#philips-light-sceil) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.sceilm](#philips-light-sceilm) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.sceils](#philips-light-sceils) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.xzceil](#philips-light-xzceil) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.xzceim](#philips-light-xzceim) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Ceiling Light        | miio:basic       | [philips.light.xzceis](#philips-light-xzceis) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| philips.light.virtual        | miio:basic       | [philips.light.virtual](#philips-light-virtual) | Yes       |            |
| philips.light.zysread        | miio:basic       | [philips.light.zysread](#philips-light-zysread) | Yes       |            |
| philips.light.zystrip        | miio:basic       | [philips.light.zystrip](#philips-light-zystrip) | Yes       |            |
| Mi Power-plug                | miio:basic       | [chuangmi.plug.m1](#chuangmi-plug-m1) | Yes       |            |
| Mi Power-plug v1             | miio:basic       | [chuangmi.plug.v1](#chuangmi-plug-v1) | Yes       |            |
| Mi Power-plug v2             | miio:basic       | [chuangmi.plug.v2](#chuangmi-plug-v2) | Yes       |            |
| Mi Power-plug v3             | miio:basic       | [chuangmi.plug.v3](#chuangmi-plug-v3) | Yes       |            |
| Mi Power-plug                | miio:basic       | [chuangmi.plug.m3](#chuangmi-plug-m3) | Yes       |            |
| Mi Smart Plug                | miio:basic       | [chuangmi.plug.hmi205](#chuangmi-plug-hmi205) | Yes       |            |
| Mi Smart Plug                | miio:basic       | [chuangmi.plug.hmi206](#chuangmi-plug-hmi206) | Yes       |            |
| Mi Smart Plug                | miio:basic       | [chuangmi.plug.hmi208](#chuangmi-plug-hmi208) | Yes       |            |
| Qing Mi Smart Power Strip v1 | miio:basic       | [qmi.powerstrip.v1](#qmi-powerstrip-v1) | Yes       |            |
| Mi Power-strip v2            | miio:basic       | [zimi.powerstrip.v2](#zimi-powerstrip-v2) | Yes       |            |
| Mi Toothbrush                | miio:unsupported | soocare.toothbrush.x3  | No        |            |
| Mi Robot Vacuum              | miio:vacuum      | [rockrobo.vacuum.v1](#rockrobo-vacuum-v1) | Yes       |            |
| Mi Xiaowa Vacuum c1          | miio:vacuum      | [roborock.vacuum.c1](#roborock-vacuum-c1) | Yes       |            |
| Roborock Vacuum S6 pure      | miio:vacuum      | [roborock.vacuum.a08](#roborock-vacuum-a08) | Yes       |            |
| Roborock S6 MaxV / T7 Pro    | miio:vacuum      | [roborock.vacuum.a09](#roborock-vacuum-a09) | Yes       |            |
| Roborock S6 MaxV / T7 Pro    | miio:vacuum      | [roborock.vacuum.a10](#roborock-vacuum-a10) | Yes       |            |
| Roborock S6 MaxV / T7 Pro    | miio:vacuum      | [roborock.vacuum.a11](#roborock-vacuum-a11) | Yes       |            |
| Roborock Vacuum S6 pure      | miio:vacuum      | [roborock.vacuum.p5](#roborock-vacuum-p5) | Yes       |            |
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
| Xiaomi Mijia vacuum V-RVCLM21B | miio:basic       | [viomi.vacuum.v6](#viomi-vacuum-v6) | Yes       |            |
| Xiaomi Mijia vacuum mop STYJ02YM | miio:basic       | [viomi.vacuum.v7](#viomi-vacuum-v7) | Yes       |            |
| Xiaomi Mijia vacuum mop STYJ02YM v2 | miio:basic       | [viomi.vacuum.v8](#viomi-vacuum-v8) | Yes       |            |
| Vacuum 1C STYTJ01ZHM         | miio:basic       | [dreame.vacuum.mc1808](#dreame-vacuum-mc1808) | Yes       |            |
| roborock.vacuum.c1           | miio:unsupported | roborock.vacuum.c1     | No        |            |
| Rockrobo Xiaowa Sweeper v2   | miio:unsupported | roborock.sweeper.e2v2  | No        |            |
| Rockrobo Xiaowa Sweeper v3   | miio:unsupported | roborock.sweeper.e2v3  | No        |            |
|  Mijia 1 Gang Wall Smart Switch (WIFI) - PTX switch | miio:basic       | [090615.switch.xswitch01](#090615-switch-xswitch01) | Yes       |            |
|  Mijia 2 Gang Wall Smart Switch (WIFI) - PTX switch | miio:basic       | [090615.switch.xswitch02](#090615-switch-xswitch02) | Yes       |            |
|  Mijia 3 Gang Wall Smart Switch (WIFI) - PTX switch | miio:basic       | [090615.switch.xswitch03](#090615-switch-xswitch03) | Yes       |            |
| Mi Water Purifier v1         | miio:basic       | [yunmi.waterpurifier.v1](#yunmi-waterpurifier-v1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier v2         | miio:basic       | [yunmi.waterpurifier.v2](#yunmi-waterpurifier-v2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier v3         | miio:basic       | [yunmi.waterpurifier.v3](#yunmi-waterpurifier-v3) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier v4         | miio:basic       | [yunmi.waterpurifier.v4](#yunmi-waterpurifier-v4) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx2        | miio:basic       | [yunmi.waterpuri.lx2](#yunmi-waterpuri-lx2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx3        | miio:basic       | [yunmi.waterpuri.lx3](#yunmi-waterpuri-lx3) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx4        | miio:basic       | [yunmi.waterpuri.lx4](#yunmi-waterpuri-lx4) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx5        | miio:basic       | [yunmi.waterpuri.lx5](#yunmi-waterpuri-lx5) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx6        | miio:basic       | [yunmi.waterpuri.lx6](#yunmi-waterpuri-lx6) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx7        | miio:basic       | [yunmi.waterpuri.lx7](#yunmi-waterpuri-lx7) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx8        | miio:basic       | [yunmi.waterpuri.lx8](#yunmi-waterpuri-lx8) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx9        | miio:basic       | [yunmi.waterpuri.lx9](#yunmi-waterpuri-lx9) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx10       | miio:basic       | [yunmi.waterpuri.lx10](#yunmi-waterpuri-lx10) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx11       | miio:basic       | [yunmi.waterpuri.lx11](#yunmi-waterpuri-lx11) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx12       | miio:basic       | [yunmi.waterpuri.lx12](#yunmi-waterpuri-lx12) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Xiaomi Wifi Extender         | miio:unsupported | xiaomi.repeater.v2     | No        |            |
| Mi Internet Speaker          | miio:unsupported | xiaomi.wifispeaker.v1  | No        |            |
| Xiaomi Mijia Whale Smart Toilet Cover | miio:basic       | [xjx.toilet.pro](#xjx-toilet-pro) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Xiaomi Mijia Smart Toilet Cover | miio:basic       | [xjx.toilet.relax](#xjx-toilet-relax) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Xiaomi Mijia Smart Toilet Cover | miio:basic       | [xjx.toilet.pure](#xjx-toilet-pure) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Xiaomi Mijia Smart Toilet Cover | miio:basic       | [xjx.toilet.zero](#xjx-toilet-zero) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Yeelight Lamp                | miio:basic       | [yeelink.light.bslamp1](#yeelink-light-bslamp1) | Yes       |            |
| Yeelight Lamp                | miio:basic       | [yeelink.light.bslamp2](#yeelink-light-bslamp2) | Yes       |            |
| Yeelight Lamp                | miio:basic       | [yeelink.light.bslamp3](#yeelink-light-bslamp3) | Yes       |            |
| Yeelight BadHeater           | miio:basic       | [yeelink.bhf_light.v1](#yeelink-bhf_light-v1) | Yes       |            |
| Yeelight BadHeater           | miio:basic       | [yeelink.bhf_light.v2](#yeelink-bhf_light-v2) | Yes       |            |
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
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling14](#yeelink-light-ceiling14) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling15](#yeelink-light-ceiling15) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling16](#yeelink-light-ceiling16) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling17](#yeelink-light-ceiling17) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling18](#yeelink-light-ceiling18) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling19](#yeelink-light-ceiling19) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling20](#yeelink-light-ceiling20) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling21](#yeelink-light-ceiling21) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling22](#yeelink-light-ceiling22) | Yes       |            |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling23](#yeelink-light-ceiling23) | Yes       |            |
| Yeelight LED Ceiling Ambi Lamp | miio:basic       | [yeelink.light.ceiling4.ambi](#yeelink-light-ceiling4-ambi) | Yes       |            |
| Yeelight LED Ceiling Ambi Lamp | miio:basic       | [yeelink.light.ceiling10.ambi](#yeelink-light-ceiling10-ambi) | Yes       |            |
| Yeelight LED Ceiling Ambi Lamp | miio:basic       | [yeelink.light.ceiling19.ambi](#yeelink-light-ceiling19-ambi) | Yes       |            |
| Yeelight LED Ceiling Ambi Lamp | miio:basic       | [yeelink.light.ceiling20.ambi](#yeelink-light-ceiling20-ambi) | Yes       |            |
| Yeelight ct2                 | miio:basic       | [yeelink.light.ct2](#yeelink-light-ct2) | Yes       |            |
| Yeelight White Bulb          | miio:basic       | [yeelink.light.mono1](#yeelink-light-mono1) | Yes       |            |
| Yeelight White Bulb v2       | miio:basic       | [yeelink.light.mono2](#yeelink-light-mono2) | Yes       |            |
| Yeelight White               | miio:basic       | [yeelink.light.mono5](#yeelink-light-mono5) | Yes       |            |
| Yeelight Wifi Speaker        | miio:unsupported | yeelink.wifispeaker.v1 | No        |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp1](#yeelink-light-lamp1) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp2](#yeelink-light-lamp2) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp3](#yeelink-light-lamp3) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp4](#yeelink-light-lamp4) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp5](#yeelink-light-lamp5) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp6](#yeelink-light-lamp6) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp7](#yeelink-light-lamp7) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp8](#yeelink-light-lamp8) | Yes       |            |
| Yeelight Panel               | miio:basic       | [yeelink.light.panel1](#yeelink-light-panel1) | Yes       |            |
| Yeelight Strip               | miio:basic       | [yeelink.light.strip1](#yeelink-light-strip1) | Yes       |            |
| Yeelight Strip               | miio:basic       | [yeelink.light.strip2](#yeelink-light-strip2) | Yes       |            |
| Yeelight Strip               | miio:basic       | [yeelink.light.strip4](#yeelink-light-strip4) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.virtual](#yeelink-light-virtual) | Yes       |            |
| Yeelight Color Bulb          | miio:basic       | [yeelink.light.color1](#yeelink-light-color1) | Yes       |            |
| Yeelight Color Bulb YLDP06YL 10W | miio:basic       | [yeelink.light.color2](#yeelink-light-color2) | Yes       |            |
| Yeelight Color Bulb YLDP02YL 9W | miio:basic       | [yeelink.light.color3](#yeelink-light-color3) | Yes       |            |
| Yeelight Bulb YLDP13YL (8,5W) | miio:basic       | [yeelink.light.color4](#yeelink-light-color4) | Yes       |            |
| Yeelight yilai ceiling       | miio:basic       | [yilai.light.ceiling1](#yilai-light-ceiling1) | Yes       |            |
| Yeelight yilai ceiling       | miio:basic       | [yilai.light.ceiling2](#yilai-light-ceiling2) | Yes       |            |
| Yeelight yilai ceiling       | miio:basic       | [yilai.light.ceiling3](#yilai-light-ceiling3) | Yes       |            |
| Zhimi Heater                 | miio:basic       | [zhimi.heater.za1](#zhimi-heater-za1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |


# Advanced: Unsupported devices

Newer devices may not yet be supported.
However, many devices share large similarities with existing devices.
The binding allows to try/test if your new device is working with database files of older devices as well.

There are 2 ways to get unsupported devices working, by overriding the model with the model of a supported item or by test all known properties to see which are supported by your device.

## Substitute model for unsupported devices

Replace the model with the model which is already supported.
For this, first remove your unsupported thing. Manually add a miio:basic thing. 
Besides the regular configuration (like ip address, token) the modelId needs to be provided.
Normally the modelId is populated with the model of your device, however in this case, use the modelId of a similar device.
Look at the openHAB forum, or the openHAB GitHub repository for the modelId of similar devices.

## Supported property test

The unsupported device has a test channel with switch. When switching on, all known properties are tested, this may take few minutes.
A test report will be shown in the log and is saved in the userdata/miio folder.
If supported properties are found, an experimental database file is saved to the conf/misc/miio folder (see below chapter).
The thing will go offline and will come back online as basic device, supporting the found channels.
The database file may need to be modified to display the right channel names.
After validation, please share the logfile and json files on the openHAB forum or the openHAB GitHub to build future support for this model.

## Advanced: adding local database files to support new devices

Things using the basic handler (miio:basic things) are driven by json 'database' files.
This instructs the binding which channels to create, which properties and actions are associated with the channels etc.
The conf/misc/miio (e.g. in Linux `/opt/openhab2/conf/misc/miio/`) is scanned for database files and will be used for your devices. 
Note that local database files take preference over build-in ones, hence if a json file is local and in the database the local file will be used. 
For format, please check the current database files in openHAB GitHub.

# FAQ.. what to do in case of problems

If your device is not getting online:

_Are you using text config?_
Make sure you define all the fields as per above example. 
Or, better, try to get it going first without text config.

_The token is wrong_
The most common cause of non responding devices is a wrong token.
When you reset, or change wifi or update firmware, and possibly other cases as well, the token may change. You'll need to get a refreshed token.

_My token is coming from the cloud... how can it be wrong?_
Is not very likely but still can happen._
This can happen e.g. if your device is defined on multiple country servers. 
The binding may pull the token from the wrong country server.
First try to get the token from all country servers by leave the county setting empty.
If that does not solve it, you define only the country that the device is on in the binding config page (where the cloud userid/pwd is entered) this should pull the right token.

_You have the same device added multiple times._
The communication each time send a sequential number. 
If the device is twice defined, the numbers received by the device are no longer sequential and it will stop responding for some time.

_The connection is not too good, so you have timeouts etc._
Position your device closer to wifi / check in the mihome app if the wifi strength is good enough.
Alternatively as described above, double check for multiple connections for single device.

_Your device is on a different subnet?_
This is in most cases not working. 
Firmware of the device don't accept commands coming from other subnets.

_Cloud connectivity is not working_
The most common problem is a wrong userId/password. Try to fix your userId/password.
If it still fails, you're bit out of luck. You may try to restart OpenHAB (not just the binding) to clean the cookies. 
As the cloud logon process is still little understood, your only luck might be to enable trace logging and see if you can translate the Chinese error code that it returns.


# Channels

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

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| aqi              | Number  | Air Quality Index                   |            |
| battery          | Number  | Battery                             |            |
| usb_state        | Switch  | USB State                           |            |
| time_state       | Switch  | Time State                          |            |
| night_state      | Switch  | Night State                         |            |
| night_begin      | Number  | Night Begin Time                    |            |
| night_end        | Number  | Night End Time                      |            |

### Mi Air Quality Monitor 2gen (<a name="cgllc-airmonitor-b1">cgllc.airmonitor.b1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| battery          | Number  | Battery                             |            |
| pm25             | Number  | PM2.5                               |            |
| co2              | Number  | CO2e                                |            |
| tvoc             | Number  | tVOC                                |            |
| humidity         | Number  | Humidity                            |            |
| temperature      | Number  | Temperature                         |            |

### Mi Air Quality Monitor S1 (<a name="cgllc-airmonitor-s1">cgllc.airmonitor.s1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| battery          | Number  | Battery                             |            |
| pm25             | Number  | PM2.5                               |            |
| co2              | Number  | CO2                                 |            |
| tvoc             | Number  | tVOC                                |            |
| humidity         | Number  | Humidity                            |            |
| temperature      | Number  | Temperature                         |            |

### Mi Air Humidifier (<a name="zhimi-humidifier-v1">zhimi.humidifier.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| setHumidity      | Number  | Humidity Set                        |            |
| aqi              | Number  | Air Quality Index                   |            |
| translevel       | Number  | Trans_level                         |            |
| bright           | Number  | LED Brightness                      |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| depth            | Number  | Depth                               |            |
| dry              | Switch  | Dry                                 |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| temperature      | Number  | Temperature                         |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Humidifier (<a name="zhimi-humidifier-ca1">zhimi.humidifier.ca1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| setHumidity      | Number  | Humidity Set                        |            |
| aqi              | Number  | Air Quality Index                   |            |
| translevel       | Number  | Trans_level                         |            |
| bright           | Number  | LED Brightness                      |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| depth            | Number  | Depth                               |            |
| dry              | Switch  | Dry                                 |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| temperature      | Number  | Temperature                         |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Humidifier 2 (<a name="zhimi-humidifier-cb1">zhimi.humidifier.cb1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| humidifierMode   | String  | Humidifier Mode                     |            |
| humidity         | Number  | Humidity                            |            |
| setHumidity      | Number  | Humidity Set                        |            |
| bright           | Number  | LED Brightness                      |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| depth            | Number  | Depth                               |            |
| dry              | Switch  | Dry                                 |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| temperature      | Number  | Temperature                         |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier v1 (<a name="zhimi-airpurifier-v1">zhimi.airpurifier.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier v2 (<a name="zhimi-airpurifier-v2">zhimi.airpurifier.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier v3 (<a name="zhimi-airpurifier-v3">zhimi.airpurifier.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier v5 (<a name="zhimi-airpurifier-v5">zhimi.airpurifier.v5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier Pro v6 (<a name="zhimi-airpurifier-v6">zhimi.airpurifier.v6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| bright           | Number  | LED Brightness                      |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier Pro v7 (<a name="zhimi-airpurifier-v7">zhimi.airpurifier.v7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| volume           | Number  | Volume                              |            |
| led              | Switch  | LED Status                          |            |
| illuminance      | Number  | Illuminance                         |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| motorspeed       | Number  | Motor Speed                         |            |
| motorspeed2      | Number  | Motor Speed 2                       |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier 2 (mini) (<a name="zhimi-airpurifier-m1">zhimi.airpurifier.m1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier (mini) (<a name="zhimi-airpurifier-m2">zhimi.airpurifier.m2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier MS1 (<a name="zhimi-airpurifier-ma1">zhimi.airpurifier.ma1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier MS2 (<a name="zhimi-airpurifier-ma2">zhimi.airpurifier.ma2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| bright           | Number  | LED Brightness                      |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier 3 (<a name="zhimi-airpurifier-ma4">zhimi.airpurifier.ma4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| Fault            | Number  | Air Purifier-Device Fault           |            |
| On               | Switch  | Air Purifier-Switch Status          |            |
| FanLevel         | Number  | Air Purifier-Fan Level              |            |
| Mode             | Number  | Air Purifier-Mode                   |            |
| FirmwareRevision | String  | Device Information-Current Firmware Version |            |
| Manufacturer     | String  | Device Information-Device Manufacturer |            |
| Model            | String  | Device Information-Device Model     |            |
| SerialNumber     | String  | Device Information-Device Serial Number |            |
| Pm25Density      | Number  | Environment-PM2.5 Density           |            |
| RelativeHumidity | Number  | Environment-Relative Humidity       |            |
| Temperature      | Number  | Environment-Temperature             |            |
| FilterLifeLevel  | Number  | Filter-Filter Life Level            |            |
| FilterUsedTime   | String  | Filter-Filter Used Time             |            |
| Alarm            | Switch  | Alarm-Alarm                         |            |
| Brightness       | Number  | Indicator Light-Brightness          |            |
| On1              | Switch  | Indicator Light-Switch Status       |            |
| PhysicalControlsLocked | Switch  | Physical Control Locked-Physical Control Locked |            |
| ButtonPressed    | String  | button-button_pressed               |            |
| FilterMaxTime    | Number  | filter-time-filter-max-time         |            |
| FilterHourUsedDebug | Number  | filter-time-filter-hour-used-debug  |            |
| M1Strong         | Number  | motor-speed-m1-strong               |            |
| M1High           | Number  | motor-speed-m1-high                 |            |
| M1Med            | Number  | motor-speed-m1-med                  |            |
| M1MedL           | Number  | motor-speed-m1-med-l                |            |
| M1Low            | Number  | motor-speed-m1-low                  |            |
| M1Silent         | Number  | motor-speed-m1-silent               |            |
| M1Favorite       | Number  | motor-speed-m1-favorite             |            |
| Motor1Speed      | Number  | motor-speed-motor1-speed            |            |
| Motor1SetSpeed   | Number  | motor-speed-motor1-set-speed        |            |
| FavoriteFanLevel | Number  | motor-speed-favorite fan level      |            |
| UseTime          | Number  | use-time-use-time                   |            |
| PurifyVolume     | Number  | aqi-purify-volume                   |            |
| AverageAqi       | Number  | aqi-average-aqi                     |            |
| AverageAqiCnt    | Number  | aqi-average-aqi-cnt                 |            |
| AqiZone          | String  | aqi-aqi-zone                        |            |
| SensorState      | String  | aqi-sensor-state                    |            |
| AqiGoodh         | Number  | aqi-aqi-goodh                       |            |
| AqiRunstate      | Number  | aqi-aqi-runstate                    |            |
| AqiState         | Number  | aqi-aqi-state                       |            |
| AqiUpdataHeartbeat | Number  | aqi-aqi-updata-heartbeat            |            |
| RfidTag          | String  | rfid-rfid-tag                       |            |
| RfidFactoryId    | String  | rfid-rfid-factory-id                |            |
| RfidProductId    | String  | rfid-rfid-product-id                |            |
| RfidTime         | String  | rfid-rfid-time                      |            |
| RfidSerialNum    | String  | rfid-rfid-serial-num                |            |
| AppExtra         | Number  | others-app-extra                    |            |
| MainChannel      | Number  | others-main-channel                 |            |
| SlaveChannel     | Number  | others-slave-channel                |            |
| Cola             | String  | others-cola                         |            |
| ButtomDoor       | Switch  | others-buttom-door                  |            |
| RebootCause      | Number  | others-reboot_cause                 |            |
| HwVersion        | Number  | others-hw-version                   |            |
| I2cErrorCount    | Number  | others-i2c-error-count              |            |
| ManualLevel      | Number  | others-manual-level                 |            |

### Mi Air Purifier 3 (<a name="zhimi-airpurifier-mb3">zhimi.airpurifier.mb3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| Fault            | Number  | Air Purifier-fault                  |            |
| On               | Switch  | Air Purifier-Switch Status          |            |
| FanLevel         | Number  | Air Purifier-Fan Level              |            |
| Mode             | Number  | Air Purifier-Mode                   |            |
| FirmwareRevision | String  | Device Information-Current Firmware Version |            |
| Manufacturer     | String  | Device Information-Device Manufacturer |            |
| Model            | String  | Device Information-Device Model     |            |
| SerialNumber     | String  | Device Information-Device Serial Number |            |
| Pm25Density      | Number  | Environment-PM2.5                   |            |
| RelativeHumidity | Number  | Environment-Relative Humidity       |            |
| Temperature      | Number  | Environment-Temperature             |            |
| FilterLifeLevel  | Number  | Filter-Filter Life Level            |            |
| FilterUsedTime   | String  | Filter-Filter Used Time             |            |
| Alarm            | Switch  | Alarm-Alarm                         |            |
| Brightness       | Number  | Indicator Light-brightness          |            |
| On1              | Switch  | Indicator Light-Switch Status       |            |
| PhysicalControlsLocked | Switch  | Physical Control Locked-Physical Control Locked |            |
| ButtonPressed    | String  | Button-button-pressed               |            |
| FilterMaxTime    | Number  | filter-time-filter-max-time         |            |
| FilterHourDebug  | Number  | filter-time-filter-hour-debug       |            |
| MotorStrong      | Number  | motor-speed-motor-strong            |            |
| MotorHigh        | Number  | motor-speed-motor-high              |            |
| MotorMed         | Number  | motor-speed-motor-med               |            |
| MotorMedL        | Number  | motor-speed-motor-med-l             |            |
| MotorLow         | Number  | motor-speed-motor-low               |            |
| MotorSilent      | Number  | motor-speed-motor-silent            |            |
| MotorFavorite    | Number  | motor-speed-motor-favorite          |            |
| MotorSpeed       | Number  | motor-speed-motor-speed             |            |
| MotorSetSpeed    | Number  | motor-speed-motor-set-speed         |            |
| FavoriteFanLevel | Number  | motor-speed-favorite-fan-level      |            |
| UseTime          | Number  | use-time-use-time                   |            |
| PurifyVolume     | Number  | aqi-purify-volume                   |            |
| AverageAqi       | Number  | aqi-average-aqi                     |            |
| AverageAqiCnt    | Number  | aqi-average-aqi-cnt                 |            |
| AqiZone          | String  | aqi-aqi-zone                        |            |
| SensorState      | String  | aqi-sensor-state                    |            |
| AqiGoodh         | Number  | aqi-aqi-goodh                       |            |
| AqiRunstate      | Number  | aqi-aqi-runstate                    |            |
| AqiState         | Number  | aqi-aqi-state                       |            |
| AqiUpdataHeartbeat | Number  | aqi-aqi-updata-heartbeat            |            |
| RfidTag          | String  | rfid-rfid-tag                       |            |
| RfidFactoryId    | String  | rfid-rfid-factory-id                |            |
| RfidProductId    | String  | rfid-rfid-product-id                |            |
| RfidTime         | String  | rfid-rfid-time                      |            |
| RfidSerialNum    | String  | rfid-rfid-serial-num                |            |
| AppExtra         | Number  | others-app-extra                    |            |
| MainChannel      | Number  | others-main-channel                 |            |
| SlaveChannel     | Number  | others-slave-channel                |            |
| Cola             | String  | others-cola                         |            |
| ButtomDoor       | Switch  | others-buttom-door                  |            |
| RebootCause      | Number  | others-reboot-cause                 |            |
| HwVersion        | Number  | others-hw-version                   |            |
| IicErrorCount    | Number  | others-iic-error-count              |            |
| ManualLevel      | Number  | others-manual-level                 |            |
| CountryCode      | Number  | others-National code                |            |

### Mi Air Purifier Super (<a name="zhimi-airpurifier-sa1">zhimi.airpurifier.sa1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier Super 2 (<a name="zhimi-airpurifier-sa2">zhimi.airpurifier.sa2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Fresh Air Ventilator (<a name="dmaker-airfresh-t2017">dmaker.airfresh.t2017</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| airFreshMode     | String  | Mode                                |            |
| airFreshPTCPower | Switch  | PTC                                 |            |
| airFreshPtcLevel | String  | PTC Level                           |            |
| airFreshPTCStatus | Switch  | PTC Status                          |            |
| airFreshDisplayDirection | String  | Screen direction                    |            |
| airFreshDisplay  | Switch  | Display                             |            |
| airFreshChildLock | Switch  | Child Lock                          |            |
| airFreshSound    | Switch  | Sound                               |            |
| airFreshPM25     | Number  | PM2.5                               |            |
| airFreshCO2      | Number  | CO2                                 |            |
| airFreshCurrentSpeed | Number  | Current Speed                       |            |
| airFreshFavoriteSpeed | Number  | Favorite Speed                      |            |
| airFreshTemperature | Number  | Temperature Outside                 |            |
| airFreshFilterPercents | Number  | Filter Percents Remaining           |            |
| airFreshFilterDays | Number  | Filter Days Remaining               |            |
| airFreshFilterProPercents | Number  | Filter Pro Percents Remaining       |            |
| airFreshFilterProDays | Number  | Filter Pro Days Remaining           |            |
| airFreshResetFilter | String  | Reset Filter                        |            |

### Mi Fresh Air Ventilator A1 (<a name="dmaker-airfresh-a1">dmaker.airfresh.a1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| airFreshMode     | String  | Mode                                |            |
| airFreshPTCPower | Switch  | PTC                                 |            |
| airFreshPTCStatus | Switch  | PTC Status                          |            |
| airFreshDisplay  | Switch  | Display                             |            |
| airFreshChildLock | Switch  | Child Lock                          |            |
| airFreshSound    | Switch  | Sound                               |            |
| airFreshPM25     | Number  | PM2.5                               |            |
| airFreshCO2      | Number  | CO2                                 |            |
| airFreshCurrentSpeed | Number  | Current Speed                       |            |
| airFreshFavoriteSpeed | Number  | Favorite Speed                      |            |
| airFreshTemperature | Number  | Temperature Outside                 |            |
| airFreshFilterPercents | Number  | Filter Percents Remaining           |            |
| airFreshFilterDays | Number  | Filter Days Remaining               |            |
| airFreshResetFilterA1 | String  | Reset Filter                        |            |

### Gosund Plug (<a name="cuco-plug-cp1">cuco.plug.cp1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| FirmwareRevision | String  | Device Information-CurrentFirmware Version |            |
| Manufacturer     | String  | Device Information-Device Manufacturer |            |
| Model            | String  | Device Information-Device Model     |            |
| SerialNumber     | String  | Device Information-Device Serial Number |            |
| On               | Switch  | Switch-Switch Status                |            |

### Mi Air Purifier mb1 (<a name="zhimi-airpurifier-mb1">zhimi.airpurifier.mb1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier 2S (<a name="zhimi-airpurifier-mc1">zhimi.airpurifier.mc1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier 2S (<a name="zhimi-airpurifier-mc2">zhimi.airpurifier.mc2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| aqi              | Number  | Air Quality Index                   |            |
| averageaqi       | Number  | Average Air Quality Index           |            |
| led              | Switch  | LED Status                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| filtermaxlife    | Number  | Filter Max Life                     |            |
| filterhours      | Number  | Filter Hours used                   |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter  Life                        |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number  | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Smart Fan (<a name="zhimi-fan-v1">zhimi.fan.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number  | Run Time                            |            |
| angle            | Number  | Angle                               |            |
| poweroffTime     | Number  | Timer                               |            |
| buzzer           | Switch  | Buzzer                              |            |
| led_b            | Number  | LED                                 |            |
| child_lock       | Switch  | Child Lock                          |            |
| speedLevel       | Number  | Speed Level                         |            |
| speed            | Number  | Speed                               |            |
| naturalLevel     | Number  | Natural Level                       |            |
| temp_dec         | Number  | Temperature                         |            |
| humidity         | Number  | Humidity                            |            |
| acPower          | String  | AC Power                            |            |
| mode             | String  | Battery Charge                      |            |
| battery          | Number  | Battery                             |            |
| move             | String  | Move Direction                      |            |

### Mi Smart Fan (<a name="zhimi-fan-v2">zhimi.fan.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number  | Run Time                            |            |
| angle            | Number  | Angle                               |            |
| poweroffTime     | Number  | Timer                               |            |
| buzzer           | Switch  | Buzzer                              |            |
| led_b            | Number  | LED                                 |            |
| child_lock       | Switch  | Child Lock                          |            |
| speedLevel       | Number  | Speed Level                         |            |
| speed            | Number  | Speed                               |            |
| naturalLevel     | Number  | Natural Level                       |            |
| temp_dec         | Number  | Temperature                         |            |
| humidity         | Number  | Humidity                            |            |
| acPower          | String  | AC Power                            |            |
| mode             | String  | Battery Charge                      |            |
| battery          | Number  | Battery                             |            |
| move             | String  | Move Direction                      |            |

### Mi Smart Pedestal Fan (<a name="zhimi-fan-v3">zhimi.fan.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number  | Run Time                            |            |
| angle            | Number  | Angle                               |            |
| poweroffTime     | Number  | Timer                               |            |
| buzzer           | Switch  | Buzzer                              |            |
| led_b            | Number  | LED                                 |            |
| child_lock       | Switch  | Child Lock                          |            |
| speedLevel       | Number  | Speed Level                         |            |
| speed            | Number  | Speed                               |            |
| naturalLevel     | Number  | Natural Level                       |            |
| temp_dec         | Number  | Temperature                         |            |
| humidity         | Number  | Humidity                            |            |
| acPower          | String  | AC Power                            |            |
| mode             | String  | Battery Charge                      |            |
| battery          | Number  | Battery                             |            |
| move             | String  | Move Direction                      |            |

### Xiaomi Mi Smart Pedestal Fan (<a name="zhimi-fan-sa1">zhimi.fan.sa1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number  | Run Time                            |            |
| angle            | Number  | Angle                               |            |
| poweroffTime     | Number  | Timer                               |            |
| buzzer           | Switch  | Buzzer                              |            |
| led_b            | Number  | LED                                 |            |
| child_lock       | Switch  | Child Lock                          |            |
| speedLevel       | Number  | Speed Level                         |            |
| speed            | Number  | Speed                               |            |
| naturalLevel     | Number  | Natural Level                       |            |
| acPower          | Switch  | AC Power                            |            |
| move             | String  | Move Direction                      |            |

### Xiaomi Mi Smart Pedestal Fan (<a name="zhimi-fan-za1">zhimi.fan.za1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number  | Run Time                            |            |
| angle            | Number  | Angle                               |            |
| poweroffTime     | Number  | Timer                               |            |
| buzzer           | Switch  | Buzzer                              |            |
| led_b            | Number  | LED                                 |            |
| child_lock       | Switch  | Child Lock                          |            |
| speedLevel       | Number  | Speed Level                         |            |
| speed            | Number  | Speed                               |            |
| naturalLevel     | Number  | Natural Level                       |            |
| acPower          | Switch  | AC Power                            |            |
| move             | String  | Move Direction                      |            |

### Xiaomi Mi Smart Pedestal Fan (<a name="zhimi-fan-za4">zhimi.fan.za4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number  | Run Time                            |            |
| angle            | Number  | Angle                               |            |
| poweroffTime     | Number  | Timer                               |            |
| buzzer           | Number  | Buzzer                              |            |
| led_b            | Number  | LED                                 |            |
| child_lock       | Switch  | Child Lock                          |            |
| speedLevel       | Number  | Speed Level                         |            |
| speed            | Number  | Speed                               |            |
| naturalLevel     | Number  | Natural Level                       |            |
| move             | String  | Move Direction                      |            |

### Xiaomi Mijia Smart Tower Fan (<a name="dmaker-fan-1c">dmaker.fan.1c</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| Manufacturer     | String  | Device Information-Device Manufacturer |            |
| Model            | String  | Device Information-Device Model     |            |
| SerialNumber     | String  | Device Information-Device Serial Number |            |
| FirmwareRevision | String  | Device Information-Current Firmware Version |            |
| On               | Switch  | Fan-Switch Status                   |            |
| FanLevel         | Number  | Fan-Fan Level                       |            |
| HorizontalSwing  | Switch  | Fan-Horizontal Swing                |            |
| Mode             | Number  | Fan-Mode                            |            |
| OffDelayTime     | Number  | Fan-Power Off Delay Time            |            |
| Alarm            | Switch  | Fan-Alarm                           |            |
| Brightness       | Switch  | Fan-Brightness                      |            |
| PhysicalControlsLocked | Switch  | Physical Control Locked-Physical Control Locked |            |

### Xiaomi Mijia Smart Tower Fan (<a name="dmaker-fan-p5">dmaker.fan.p5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| roll             | Switch  | Rotation                            |            |
| mode             | Number  | Mode                                |            |
| angle            | Number  | Angle                               |            |
| timer            | Number  | Timer                               |            |
| beep             | Switch  | Beep Sound                          |            |
| light            | Number  | Light                               |            |
| child_lock       | Switch  | Child Lock                          |            |
| speed            | Number  | Speed                               |            |

### Xiaomi Mijia Smart Tower Fan (<a name="dmaker-fan-p8">dmaker.fan.p8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| Manufacturer     | String  | Device Information-Device Manufacturer |            |
| Model            | String  | Device Information-Device Model     |            |
| SerialNumber     | String  | Device Information-Device Serial Number |            |
| FirmwareRevision | String  | Device Information-Current Firmware Version |            |
| On               | Switch  | Fan-Switch Status                   |            |
| FanLevel         | Number  | Fan-Fan Level                       |            |
| HorizontalSwing  | Switch  | Fan-Horizontal Swing                |            |
| Mode             | Number  | Fan-Mode                            |            |
| OffDelayTime     | Number  | Fan-Power Off Delay Time            |            |
| Alarm            | Switch  | Fan-Alarm                           |            |
| Brightness       | Switch  | Fan-Brightness                      |            |
| PhysicalControlsLocked | Switch  | Physical Control Locked-Physical Control Locked |            |

### Xiaomi Mijia Smart Tower Fan (<a name="dmaker-fan-p9">dmaker.fan.p9</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| Manufacturer     | String  | Device Information-Device Manufacturer |            |
| Model            | String  | Device Information-Device Model     |            |
| SerialNumber     | String  | Device Information-Device Serial Number |            |
| FirmwareRevision | String  | Device Information-Current Firmware Version |            |
| On               | Switch  | Fan-Switch Status                   |            |
| FanLevel         | Number  | Fan-Fan Level                       |            |
| Mode             | Number  | Fan-Mode                            |            |
| HorizontalSwing  | Switch  | Fan-Horizontal Swing                |            |
| HorizontalAngle  | Number  | Fan-Horizontal Angle                |            |
| Alarm            | Switch  | Fan-Alarm                           |            |
| OffDelayTime     | Number  | Fan-Power Off Delay Time            |            |
| Brightness       | Switch  | Fan-Brightness                      |            |
| MotorControl     | Number  | Fan-Motor Control                   |            |
| SpeedLevel       | Number  | Fan-Speed Level                     |            |
| PhysicalControlsLocked | Switch  | Physical Control Locked-Physical Control Locked |            |

### Xiaomi Mijia Smart Tower Fan (<a name="dmaker-fan-p10">dmaker.fan.p10</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| Manufacturer     | String  | Device Information-Device Manufacturer |            |
| Model            | String  | Device Information-Device Model     |            |
| SerialNumber     | String  | Device Information-Device Serial Number |            |
| FirmwareRevision | String  | Device Information-Current Firmware Version |            |
| On               | Switch  | Fan-Switch Status                   |            |
| FanLevel         | Number  | Fan-Fan Level                       |            |
| Mode             | Number  | Fan-Mode                            |            |
| HorizontalSwing  | Switch  | Fan-Horizontal Swing                |            |
| HorizontalAngle  | Number  | Fan-Horizontal Angle                |            |
| Alarm            | Switch  | Fan-Alarm                           |            |
| OffDelayTime     | Number  | Fan-Power Off Delay Time            |            |
| Brightness       | Switch  | Fan-Brightness                      |            |
| MotorControl     | Number  | Fan-Motor Control                   |            |
| SpeedLevel       | Number  | Fan-Speed Level                     |            |
| PhysicalControlsLocked | Switch  | Physical Control Locked-Physical Control Locked |            |

### Mi Smart Home Gateway v1 (<a name="lumi-gateway-v1">lumi.gateway.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| telnetEnable     | Switch  | Enable Telnet                       | Access the device with telnet to further unlock developer mode. See forum |
| doorbellVol      | Number  | Doorbell Volume                     |            |
| gatewayVol       | Number  | Gateway Volume                      |            |
| alarmingVol      | Number  | Alarming Volume                     |            |
| doorbellPush     | String  | Doorbell Push                       |            |

### Mi Smart Home Gateway v2 (<a name="lumi-gateway-v2">lumi.gateway.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| telnetEnable     | Switch  | Enable Telnet                       | Access the device with telnet to further unlock developer mode. See forum |
| doorbellVol      | Number  | Doorbell Volume                     |            |
| gatewayVol       | Number  | Gateway Volume                      |            |
| alarmingVol      | Number  | Alarming Volume                     |            |
| doorbellPush     | String  | Doorbell Push                       |            |

### Mi Smart Home Gateway v3 (<a name="lumi-gateway-v3">lumi.gateway.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| telnetEnable     | Switch  | Enable Telnet                       | Access the device with telnet to further unlock developer mode. See forum |
| doorbellVol      | Number  | Doorbell Volume                     |            |
| gatewayVol       | Number  | Gateway Volume                      |            |
| alarmingVol      | Number  | Alarming Volume                     |            |
| doorbellPush     | String  | Doorbell Push                       |            |

### Xiaomi Mi Mijia Gateway V3 ZNDMWG03LM (<a name="lumi-gateway-mgl03">lumi.gateway.mgl03</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| telnetEnable     | Switch  | Enable Telnet                       | Access the device with telnet to further unlock developer mode. See forum |
| doorbellVol      | Number  | Doorbell Volume                     |            |
| gatewayVol       | Number  | Gateway Volume                      |            |
| alarmingVol      | Number  | Alarming Volume                     |            |
| doorbellPush     | String  | Doorbell Push                       |            |

### Mi Humdifier (<a name="zhimi-humidifier-v1">zhimi.humidifier.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | String  | Mode                                |            |
| humidity         | Number  | Humidity                            |            |
| setHumidity      | Number  | Humidity Set                        |            |
| aqi              | Number  | Air Quality Index                   |            |
| translevel       | Number  | Trans_level                         |            |
| bright           | Number  | LED Brightness                      |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| depth            | Number  | Depth                               |            |
| dry              | Switch  | Dry                                 |            |
| usedhours        | Number  | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| temperature      | Number  | Temperature                         |            |
| childlock        | Switch  | Child Lock                          |            |

### Mr Bond M1 Pro Smart Clothes Dryer (<a name="mrbond-airer-m1pro">mrbond.airer.m1pro</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| dry              | Switch  | Dry                                 |            |
| led              | Switch  | LED Status                          |            |
| motor            | Number  | Motor                               |            |
| drytime          | Number  | Dry Time                            |            |
| airer_location   | Number  | Airer Location                      |            |
| disinfect        | Switch  | disinfect                           |            |
| distime          | Number  | Disinfect Time                      |            |

### Mr Bond M1 Smart Clothes Dryer (<a name="mrbond-airer-m1s">mrbond.airer.m1s</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| dry              | Switch  | Dry                                 |            |
| led              | Switch  | LED Status                          |            |
| motor            | Number  | Motor                               |            |
| drytime          | Number  | Dry Time                            |            |
| airer_location   | Number  | Airer Location                      |            |
| disinfect        | Switch  | disinfect                           |            |
| distime          | Number  | Disinfect Time                      |            |

### Mr Bond M1 Super Smart Clothes Dryer (<a name="mrbond-airer-m1super">mrbond.airer.m1super</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| dry              | Switch  | Dry                                 |            |
| led              | Switch  | LED Status                          |            |
| motor            | Number  | Motor                               |            |
| drytime          | Number  | Dry Time                            |            |
| airer_location   | Number  | Airer Location                      |            |
| disinfect        | Switch  | disinfect                           |            |
| distime          | Number  | Disinfect Time                      |            |

### Xiaomi Philips Eyecare Smart Lamp 2 (<a name="philips-light-sread1">philips.light.sread1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| ambientPower     | Switch  | Ambient Power                       |            |
| ambientBrightness | Number  | Ambient Brightness                  |            |
| illumination     | Number  | Ambient Illumination                |            |
| eyecare          | Switch  | Eyecare                             |            |
| bl               | Switch  | Night Light                         |            |

### Xiaomi Philips Eyecare Smart Lamp 2 (<a name="philips-light-sread2">philips.light.sread2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| ambientPower     | Switch  | Ambient Power                       |            |
| ambientBrightness | Number  | Ambient Brightness                  |            |
| illumination     | Number  | Ambient Illumination                |            |
| eyecare          | Switch  | Eyecare                             |            |
| bl               | Switch  | Night Light                         |            |

### Xiaomi Philips LED Ceiling Lamp (<a name="philips-light-ceiling">philips.light.ceiling</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| switchscene      | Switch  | Switch Scene                        |            |
| toggle           | Switch  | Toggle                              |            |

### Xiaomi Philips LED Ceiling Lamp (<a name="philips-light-zyceiling">philips.light.zyceiling</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| switchscene      | Switch  | Switch Scene                        |            |
| toggle           | Switch  | Toggle                              |            |

### Xiaomi Philips Bulb (<a name="philips-light-bulb">philips.light.bulb</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Xiaomi Philips Wi-Fi Bulb E27 White (<a name="philips-light-hbulb">philips.light.hbulb</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp (<a name="philips-light-candle">philips.light.candle</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| delayoff         | Switch  | Delay Off                           |            |
| toggle           | Switch  | Toggle                              |            |

### Xiaomi Philips Downlight (<a name="philips-light-downlight">philips.light.downlight</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Xiaomi Philips ZhiRui bedside lamp (<a name="philips-light-moonlight">philips.light.moonlight</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| gonight          | Switch  | Go Night                            |            |
| delayoff         | Switch  | Delay Off                           |            |
| toggle           | Switch  | Toggle                              |            |

### Philips Ceiling Light (<a name="philips-light-bceiling1">philips.light.bceiling1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| sw               | Switch  | Switch                              |            |
| bl               | Switch  | Night Light                         |            |
| ms               | Switch  | MiBand Notifications                |            |
| ac               | Switch  | Auto Ambiance                       |            |
| delayoff         | Switch  | Delay Off                           |            |
| mb               | Switch  | MiBand                              |            |

### Philips Ceiling Light (<a name="philips-light-bceiling2">philips.light.bceiling2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| sw               | Switch  | Switch                              |            |
| bl               | Switch  | Night Light                         |            |
| ms               | Switch  | MiBand Notifications                |            |
| ac               | Switch  | Auto Ambiance                       |            |
| delayoff         | Switch  | Delay Off                           |            |
| mb               | Switch  | MiBand                              |            |

### Philips Light (<a name="philips-light-cbulb">philips.light.cbulb</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| cid              | Color   | Color                               |            |
| switchscene      | Switch  | Switch Scene                        |            |
| switch_en        | Switch  | Switch Enabled                      |            |
| delayoff         | Switch  | Delay Off                           |            |

### Philips Light (<a name="philips-light-cbulbs">philips.light.cbulbs</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| cid              | Color   | Color                               |            |
| switchscene      | Switch  | Switch Scene                        |            |
| switch_en        | Switch  | Switch Enabled                      |            |
| delayoff         | Switch  | Delay Off                           |            |

### Philips Light (<a name="philips-light-dcolor">philips.light.dcolor</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| cid              | Color   | Color                               |            |
| switchscene      | Switch  | Switch Scene                        |            |
| switch_en        | Switch  | Switch Enabled                      |            |
| delayoff         | Switch  | Delay Off                           |            |

### Philips Light (<a name="philips-light-rwread">philips.light.rwread</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| scene            | Number  | Scene                               |            |
| flm              | Number  | Follow Me                           |            |
| dv               | Number  | DV                                  |            |

### Philips Light (<a name="philips-light-lnblight1">philips.light.lnblight1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| sw               | Switch  | Switch                              |            |
| bl               | Switch  | Night Light                         |            |
| ms               | Switch  | MiBand Notifications                |            |
| ac               | Switch  | Auto Ambiance                       |            |
| delayoff         | Switch  | Delay Off                           |            |
| mb               | Switch  | MiBand                              |            |

### Philips Light (<a name="philips-light-lnblight2">philips.light.lnblight2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| sw               | Switch  | Switch                              |            |
| bl               | Switch  | Night Light                         |            |
| ms               | Switch  | MiBand Notifications                |            |
| ac               | Switch  | Auto Ambiance                       |            |
| delayoff         | Switch  | Delay Off                           |            |
| mb               | Switch  | MiBand                              |            |

### Philips Light (<a name="philips-light-lnlrlight">philips.light.lnlrlight</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| sw               | Switch  | Switch                              |            |
| bl               | Switch  | Night Light                         |            |
| ms               | Switch  | MiBand Notifications                |            |
| ac               | Switch  | Auto Ambiance                       |            |
| delayoff         | Switch  | Delay Off                           |            |
| mb               | Switch  | MiBand                              |            |

### Philips Light (<a name="philips-light-lrceiling">philips.light.lrceiling</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| sw               | Switch  | Switch                              |            |
| bl               | Switch  | Night Light                         |            |
| ms               | Switch  | MiBand Notifications                |            |
| ac               | Switch  | Auto Ambiance                       |            |
| delayoff         | Switch  | Delay Off                           |            |
| mb               | Switch  | MiBand                              |            |

### Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal (<a name="philips-light-candle2">philips.light.candle2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| delayoff         | Switch  | Delay Off                           |            |
| toggle           | Switch  | Toggle                              |            |

### philips.light.mono1 (<a name="philips-light-mono1">philips.light.mono1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| scene            | Number  | Scene                               |            |

### Philips Down Light (<a name="philips-light-dlight">philips.light.dlight</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-mceil">philips.light.mceil</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-mceilm">philips.light.mceilm</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-mceils">philips.light.mceils</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-obceil">philips.light.obceil</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-obceim">philips.light.obceim</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-obceis">philips.light.obceis</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-sceil">philips.light.sceil</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-sceilm">philips.light.sceilm</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-sceils">philips.light.sceils</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-xzceil">philips.light.xzceil</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-xzceim">philips.light.xzceim</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### Philips Ceiling Light (<a name="philips-light-xzceis">philips.light.xzceis</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| brightness       | Number  | Brightness                          |            |
| cct              | Number  | Color Temperature                   |            |
| dv               | Number  | Delayed Turn-off                    |            |
| WallSceneEn      | Switch  | Wall Scene Enable                   |            |
| WallScene        | String  | Wall Scene                          |            |
| autoCct          | String  | Auto CCT                            |            |
| dimmingPeriod    | Number  | Dimming Period                      |            |
| MibandStatus     | String  | Mi Band Status                      |            |

### philips.light.virtual (<a name="philips-light-virtual">philips.light.virtual</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### philips.light.zysread (<a name="philips-light-zysread">philips.light.zysread</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### philips.light.zystrip (<a name="philips-light-zystrip">philips.light.zystrip</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Mi Power-plug (<a name="chuangmi-plug-m1">chuangmi.plug.m1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| temperature      | Number  | Temperature                         |            |
| led              | Switch  | Indicator light                     |            |

### Mi Power-plug v1 (<a name="chuangmi-plug-v1">chuangmi.plug.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| usb              | Switch  | USB                                 |            |
| temperature      | Number  | Temperature                         |            |

### Mi Power-plug v2 (<a name="chuangmi-plug-v2">chuangmi.plug.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| usb              | Switch  | USB                                 |            |

### Mi Power-plug v3 (<a name="chuangmi-plug-v3">chuangmi.plug.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               | If this channel does not respond to on/off replace the model with chuangmi.plug.v3old in the config or upgrade firmware |
| usb              | Switch  | USB                                 |            |
| temperature      | Number  | Temperature                         |            |
| led              | Switch  | Wifi LED                            |            |

### Mi Power-plug (<a name="chuangmi-plug-m3">chuangmi.plug.m3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| temperature      | Number  | Temperature                         |            |
| led              | Switch  | Indicator light                     |            |

### Mi Smart Plug (<a name="chuangmi-plug-hmi205">chuangmi.plug.hmi205</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| temperature      | Number  | Temperature                         |            |
| led              | Switch  | Indicator light                     |            |

### Mi Smart Plug (<a name="chuangmi-plug-hmi206">chuangmi.plug.hmi206</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               | If this channel does not respond to on/off replace the model with chuangmi.plug.v3old in the config or upgrade firmware |
| usb              | Switch  | USB                                 |            |
| temperature      | Number  | Temperature                         |            |
| led              | Switch  | Wifi LED                            |            |

### Mi Smart Plug (<a name="chuangmi-plug-hmi208">chuangmi.plug.hmi208</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               | If this channel does not respond to on/off replace the model with chuangmi.plug.v3old in the config or upgrade firmware |
| usb              | Switch  | USB                                 |            |
| temperature      | Number  | Temperature                         |            |
| led              | Switch  | Wifi LED                            |            |

### Qing Mi Smart Power Strip v1 (<a name="qmi-powerstrip-v1">qmi.powerstrip.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| powerUsage       | Number  | Power Consumption                   |            |
| led              | Switch  | wifi LED                            |            |
| power_price      | Number  | power_price                         |            |
| current          | Number  | Current                             |            |
| temperature      | Number  | Temperature                         |            |

### Mi Power-strip v2 (<a name="zimi-powerstrip-v2">zimi.powerstrip.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| powerUsage       | Number  | Power Consumption                   |            |
| led              | Switch  | wifi LED                            |            |
| power_price      | Number  | power_price                         |            |
| current          | Number  | Current                             |            |
| temperature      | Number  | Temperature                         |            |

### Xiaomi Mijia vacuum V-RVCLM21B (<a name="viomi-vacuum-v6">viomi.vacuum.v6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| vacuumaction     | Number  | Vacuum Action                       |            |
| state            | Number  | State                               |            |
| mode             | Number  | Mode                                |            |
| err_state        | Number  | Error                               |            |
| battery_life     | Number  | Battery                             |            |
| box_type         | Number  | Box type                            |            |
| mop_type         | Number  | mop_type                            |            |
| s_time           | Number  | Clean time                          |            |
| s_area           | Number  | Clean Area                          |            |
| suction_grade    | Number  | suction_grade                       |            |
| water_grade      | Number  | water_grade                         |            |
| remember_map     | Number  | remember_map                        |            |
| has_map          | Number  | has_map                             |            |
| is_mop           | Number  | is_mop                              |            |
| has_newmap       | Number  | has_newmap                          |            |

### Xiaomi Mijia vacuum mop STYJ02YM (<a name="viomi-vacuum-v7">viomi.vacuum.v7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| vacuumaction     | Number  | Vacuum Action                       |            |
| state            | Number  | State                               |            |
| mode             | Number  | Mode                                |            |
| err_state        | Number  | Error                               |            |
| battery_life     | Number  | Battery                             |            |
| box_type         | Number  | Box type                            |            |
| mop_type         | Number  | mop_type                            |            |
| s_time           | Number  | Clean time                          |            |
| s_area           | Number  | Clean Area                          |            |
| suction_grade    | Number  | suction_grade                       |            |
| water_grade      | Number  | water_grade                         |            |
| remember_map     | Number  | remember_map                        |            |
| has_map          | Number  | has_map                             |            |
| is_mop           | Number  | is_mop                              |            |
| has_newmap       | Number  | has_newmap                          |            |

### Xiaomi Mijia vacuum mop STYJ02YM v2 (<a name="viomi-vacuum-v8">viomi.vacuum.v8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| vacuumaction     | Number  | Vacuum Action                       |            |
| state            | Number  | State                               |            |
| mode             | Number  | Mode                                |            |
| err_state        | Number  | Error                               |            |
| battery_life     | Number  | Battery                             |            |
| box_type         | Number  | Box type                            |            |
| mop_type         | Number  | mop_type                            |            |
| s_time           | Number  | Clean time                          |            |
| s_area           | Number  | Clean Area                          |            |
| suction_grade    | Number  | suction_grade                       |            |
| water_grade      | Number  | water_grade                         |            |
| remember_map     | Number  | remember_map                        |            |
| has_map          | Number  | has_map                             |            |
| is_mop           | Number  | is_mop                              |            |
| has_newmap       | Number  | has_newmap                          |            |

### Vacuum 1C STYTJ01ZHM (<a name="dreame-vacuum-mc1808">dreame.vacuum.mc1808</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| vacuumaction     | String  | Vacuum Action                       |            |
| BatteryLevel     | Number  | Battery-Battery Level               |            |
| ChargingState    | Number  | Battery-Charging State              |            |
| Fault            | Number  | Robot Cleaner-Device Fault          |            |
| Status           | Number  | Robot Cleaner-Status                |            |
| BrushLeftTime    | String  | Main Cleaning Brush-Brush Left Time |            |
| BrushLifeLevel   | Number  | Main Cleaning Brush-Brush Life Level |            |
| FilterLifeLevel  | Number  | Filter-Filter Life Level            |            |
| FilterLeftTime   | String  | Filter-Filter Left Time             |            |
| BrushLeftTime1   | String  | Side Cleaning Brush-Brush Left Time |            |
| BrushLifeLevel1  | Number  | Side Cleaning Brush-Brush Life Level |            |
| WorkMode         | Number  | clean-workmode                      |            |
| Area             | String  | clean-area                          |            |
| Timer            | String  | clean-timer                         |            |
| Mode             | Number  | clean-mode                          |            |
| TotalCleanTime   | String  | clean-total time                    |            |
| TotalCleanTimes  | String  | clean-total times                   |            |
| TotalCleanArea   | String  | clean-Total area                    |            |
| CleanLogStartTime | String  | clean-Start Time                    |            |
| ButtonLed        | String  | clean-led                           |            |
| TaskDone         | Number  | clean-task done                     |            |
| LifeSieve        | String  | consumable-life-sieve               |            |
| LifeBrushSide    | String  | consumable-life-brush-side          |            |
| LifeBrushMain    | String  | consumable-life-brush-main          |            |
| Enable           | Switch  | annoy-enable                        |            |
| StartTime        | String  | annoy-start-time                    |            |
| StopTime         | String  | annoy-stop-time                     |            |
| MapView          | String  | map-map-view                        |            |
| Volume           | Number  | audio-volume                        |            |
| VoicePackets     | String  | audio-voiceId                       |            |
| TimeZone         | String  | timezone                            |            |

###  Mijia 1 Gang Wall Smart Switch (WIFI) - PTX switch (<a name="090615-switch-xswitch01">090615.switch.xswitch01</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| switch1state     | Number  | Switch 1                            |            |
| switch1name      | String  | Switch Name 1                       |            |

###  Mijia 2 Gang Wall Smart Switch (WIFI) - PTX switch (<a name="090615-switch-xswitch02">090615.switch.xswitch02</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| switch1state     | Number  | Switch 1                            |            |
| switch2state     | Number  | Switch 2                            |            |
| switch1name      | String  | Switch Name 1                       |            |
| switch2name      | String  | Switch Name 2                       |            |

###  Mijia 3 Gang Wall Smart Switch (WIFI) - PTX switch (<a name="090615-switch-xswitch03">090615.switch.xswitch03</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| switch1state     | Number  | Switch 1                            |            |
| switch2state     | Number  | Switch 2                            |            |
| switch3state     | Number  | Switch 3                            |            |
| switch1name      | String  | Switch Name 1                       |            |
| switch2name      | String  | Switch Name 2                       |            |
| switch3name      | String  | Switch Name 3                       |            |

### Mi Water Purifier v1 (<a name="yunmi-waterpurifier-v1">yunmi.waterpurifier.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier v2 (<a name="yunmi-waterpurifier-v2">yunmi.waterpurifier.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier v3 (<a name="yunmi-waterpurifier-v3">yunmi.waterpurifier.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier v4 (<a name="yunmi-waterpurifier-v4">yunmi.waterpurifier.v4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx2 (<a name="yunmi-waterpuri-lx2">yunmi.waterpuri.lx2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx3 (<a name="yunmi-waterpuri-lx3">yunmi.waterpuri.lx3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx4 (<a name="yunmi-waterpuri-lx4">yunmi.waterpuri.lx4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx5 (<a name="yunmi-waterpuri-lx5">yunmi.waterpuri.lx5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx6 (<a name="yunmi-waterpuri-lx6">yunmi.waterpuri.lx6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx7 (<a name="yunmi-waterpuri-lx7">yunmi.waterpuri.lx7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx8 (<a name="yunmi-waterpuri-lx8">yunmi.waterpuri.lx8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx9 (<a name="yunmi-waterpuri-lx9">yunmi.waterpuri.lx9</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx10 (<a name="yunmi-waterpuri-lx10">yunmi.waterpuri.lx10</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx11 (<a name="yunmi-waterpuri-lx11">yunmi.waterpuri.lx11</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Mi Water Purifier lx12 (<a name="yunmi-waterpuri-lx12">yunmi.waterpuri.lx12</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number  | Temperature                         |            |
| rinse            | Number  | Rinse                               |            |
| tds_in           | Number  | TDS in                              |            |
| tds_out          | Number  | TDS out                             |            |
| f1_totalflow     | Number  | Filter 1 Total Flow                 |            |
| f1_totaltime     | Number  | Filter 1 Total Time                 |            |
| f1_usedflow      | Number  | Filter 1 Used Flow                  |            |
| f1_usedtime      | Number  | Filter 1 Used Time                  |            |
| f2_totalflow     | Number  | Filter 2 Total Flow                 |            |
| f2_totaltime     | Number  | Filter 2 Total Time                 |            |
| f2_usedflow      | Number  | Filter 2 Used Flow                  |            |
| f2_usedtime      | Number  | Filter 2 Used Time                  |            |
| f3_totalflow     | Number  | Filter 3 Total Flow                 |            |
| f3_totaltime     | Number  | Filter 3 Total Time                 |            |
| f3_usedflow      | Number  | Filter 3 Used Flow                  |            |
| f3_usedtime      | Number  | Filter 3 Used Time                  |            |

### Xiaomi Mijia Whale Smart Toilet Cover (<a name="xjx-toilet-pro">xjx.toilet.pro</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| seat_temp        | Number  | Seat Temperature                    |            |
| status_seatheat  | Number  | Seat Status                         |            |
| water_temp_t     | Number  | Water Temperature                   |            |
| fan_temp         | Number  | Fan Temperature                     |            |
| status_led       | Number  | Night Light                         |            |

### Xiaomi Mijia Smart Toilet Cover (<a name="xjx-toilet-relax">xjx.toilet.relax</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| seat_temp        | Number  | Seat Temperature                    |            |
| status_seatheat  | Number  | Seat Status                         |            |
| water_temp_t     | Number  | Water Temperature                   |            |
| fan_temp         | Number  | Fan Temperature                     |            |
| status_led       | Number  | Night Light                         |            |

### Xiaomi Mijia Smart Toilet Cover (<a name="xjx-toilet-pure">xjx.toilet.pure</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| seat_temp        | Number  | Seat Temperature                    |            |
| status_seatheat  | Number  | Seat Status                         |            |
| water_temp_t     | Number  | Water Temperature                   |            |
| fan_temp         | Number  | Fan Temperature                     |            |
| status_led       | Number  | Night Light                         |            |

### Xiaomi Mijia Smart Toilet Cover (<a name="xjx-toilet-zero">xjx.toilet.zero</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| seat_temp        | Number  | Seat Temperature                    |            |
| status_seatheat  | Number  | Seat Status                         |            |
| water_temp_t     | Number  | Water Temperature                   |            |
| fan_temp         | Number  | Fan Temperature                     |            |
| status_led       | Number  | Night Light                         |            |

### Yeelight Lamp (<a name="yeelink-light-bslamp1">yeelink.light.bslamp1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Lamp (<a name="yeelink-light-bslamp2">yeelink.light.bslamp2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Lamp (<a name="yeelink-light-bslamp3">yeelink.light.bslamp3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight BadHeater (<a name="yeelink-bhf_light-v1">yeelink.bhf_light.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| bh_mode          | String  | Bath Heater mode                    |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| temperature      | Number  | Temperature                         |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight BadHeater (<a name="yeelink-bhf_light-v2">yeelink.bhf_light.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| bh_mode          | String  | Bath Heater mode                    |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| temperature      | Number  | Temperature                         |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling1">yeelink.light.ceiling1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v2 (<a name="yeelink-light-ceiling2">yeelink.light.ceiling2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v3 (<a name="yeelink-light-ceiling3">yeelink.light.ceiling3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB) (<a name="yeelink-light-ceiling4">yeelink.light.ceiling4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| ambientBrightness | Number  | Ambient Brightness                  |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| ambientPower     | Switch  | Ambient Power                       |            |
| ambientColor     | Color   | Ambient Color                       |            |
| ambientColorTemperature | Number  | Ambient Color Temperature           |            |
| customScene      | String  | Set Scene                           |            |
| ambientColorMode | Number  | Ambient Color Mode                  |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v4 (<a name="yeelink-light-ceiling4-ambi">yeelink.light.ceiling4.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v5 (<a name="yeelink-light-ceiling5">yeelink.light.ceiling5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v6 (<a name="yeelink-light-ceiling6">yeelink.light.ceiling6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v7 (<a name="yeelink-light-ceiling7">yeelink.light.ceiling7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v8 (<a name="yeelink-light-ceiling8">yeelink.light.ceiling8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v9 (<a name="yeelink-light-ceiling9">yeelink.light.ceiling9</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Meteorite lamp (<a name="yeelink-light-ceiling10">yeelink.light.ceiling10</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| ambientBrightness | Number  | Ambient Brightness                  |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| ambientPower     | Switch  | Ambient Power                       |            |
| ambientColor     | Color   | Ambient Color                       |            |
| ambientColorTemperature | Number  | Ambient Color Temperature           |            |
| customScene      | String  | Set Scene                           |            |
| ambientColorMode | Number  | Ambient Color Mode                  |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v11 (<a name="yeelink-light-ceiling11">yeelink.light.ceiling11</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v12 (<a name="yeelink-light-ceiling12">yeelink.light.ceiling12</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp v13 (<a name="yeelink-light-ceiling13">yeelink.light.ceiling13</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling14">yeelink.light.ceiling14</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling15">yeelink.light.ceiling15</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling16">yeelink.light.ceiling16</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling17">yeelink.light.ceiling17</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling18">yeelink.light.ceiling18</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling19">yeelink.light.ceiling19</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling20">yeelink.light.ceiling20</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling21">yeelink.light.ceiling21</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling22">yeelink.light.ceiling22</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling23">yeelink.light.ceiling23</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Ambi Lamp (<a name="yeelink-light-ceiling4-ambi">yeelink.light.ceiling4.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Ambi Lamp (<a name="yeelink-light-ceiling10-ambi">yeelink.light.ceiling10.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Ambi Lamp (<a name="yeelink-light-ceiling19-ambi">yeelink.light.ceiling19.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Ambi Lamp (<a name="yeelink-light-ceiling20-ambi">yeelink.light.ceiling20.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight ct2 (<a name="yeelink-light-ct2">yeelink.light.ct2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight White Bulb (<a name="yeelink-light-mono1">yeelink.light.mono1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight White Bulb v2 (<a name="yeelink-light-mono2">yeelink.light.mono2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight White (<a name="yeelink-light-mono5">yeelink.light.mono5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp1">yeelink.light.lamp1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp2">yeelink.light.lamp2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp3">yeelink.light.lamp3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp4">yeelink.light.lamp4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp5">yeelink.light.lamp5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp6">yeelink.light.lamp6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp7">yeelink.light.lamp7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp8">yeelink.light.lamp8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight Panel (<a name="yeelink-light-panel1">yeelink.light.panel1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Strip (<a name="yeelink-light-strip1">yeelink.light.strip1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Strip (<a name="yeelink-light-strip2">yeelink.light.strip2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Strip (<a name="yeelink-light-strip4">yeelink.light.strip4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-virtual">yeelink.light.virtual</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight Color Bulb (<a name="yeelink-light-color1">yeelink.light.color1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Color Bulb YLDP06YL 10W (<a name="yeelink-light-color2">yeelink.light.color2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Color Bulb YLDP02YL 9W (<a name="yeelink-light-color3">yeelink.light.color3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Bulb YLDP13YL (8,5W) (<a name="yeelink-light-color4">yeelink.light.color4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | String  | Color Mode                          |            |
| toggle           | Switch  | toggle                              |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight yilai ceiling (<a name="yilai-light-ceiling1">yilai.light.ceiling1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight yilai ceiling (<a name="yilai-light-ceiling2">yilai.light.ceiling2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight yilai ceiling (<a name="yilai-light-ceiling3">yilai.light.ceiling3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number  | Shutdown Timer                      |            |
| colorTemperature | Number  | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Zhimi Heater (<a name="zhimi-heater-za1">zhimi.heater.za1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| target_temperature | Number  | Target Temperature                  |            |
| brightness       | Number  | Brightness                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| relative_humidity | Number  | Relative Humidity                   |            |
| childlock        | Switch  | Child Lock                          |            |
| HWSwitch         | Switch  | HW Switch                           |            |
| temperature      | Number  | Temperature                         |            |
| usedhours        | Number  | Run Time                            |            |




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

Additionally depending on the capabilities of your robot vacuum other channels may be enabled at runtime


| Type    | Channel                           | Description                |
|---------|-----------------------------------|----------------------------|
| Switch  | status#water_box_status           | Water Box Status           |
| Switch  | status#lock_status                | Lock Status                |
| Number  | status#water_box_mode             | Water Box Mode             |
| Switch  | status#water_box_carriage_status  | Water Box Carriage Status  |
| Switch  | status#mop_forbidden_enable       | Mop Forbidden              |
| Number  | actions#segment                   | Room Clean  (enter room #) |



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

### Mi Air Purifier 2S (zhimi.airpurifier.mc2) item file lines

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

### Xiaomi Mi Smart Pedestal Fan (zhimi.fan.za4) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Xiaomi Mi Smart Pedestal Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
Number angle "Angle" (G_fan) {channel="miio:basic:fan:angle"}
Number poweroffTime "Timer" (G_fan) {channel="miio:basic:fan:poweroffTime"}
Number buzzer "Buzzer" (G_fan) {channel="miio:basic:fan:buzzer"}
Number led_b "LED" (G_fan) {channel="miio:basic:fan:led_b"}
Switch child_lock "Child Lock" (G_fan) {channel="miio:basic:fan:child_lock"}
Number speedLevel "Speed Level" (G_fan) {channel="miio:basic:fan:speedLevel"}
Number speed "Speed" (G_fan) {channel="miio:basic:fan:speed"}
Number naturalLevel "Natural Level" (G_fan) {channel="miio:basic:fan:naturalLevel"}
String move "Move Direction" (G_fan) {channel="miio:basic:fan:move"}
```

### Xiaomi Mijia Smart Tower Fan (dmaker.fan.1c) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Xiaomi Mijia Smart Tower Fan" <status>
String Manufacturer "Device Information-Device Manufacturer" (G_fan) {channel="miio:basic:fan:Manufacturer"}
String Model "Device Information-Device Model" (G_fan) {channel="miio:basic:fan:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_fan) {channel="miio:basic:fan:SerialNumber"}
String FirmwareRevision "Device Information-Current Firmware Version" (G_fan) {channel="miio:basic:fan:FirmwareRevision"}
Switch On "Fan-Switch Status" (G_fan) {channel="miio:basic:fan:On"}
Number FanLevel "Fan-Fan Level" (G_fan) {channel="miio:basic:fan:FanLevel"}
Switch HorizontalSwing "Fan-Horizontal Swing" (G_fan) {channel="miio:basic:fan:HorizontalSwing"}
Number Mode "Fan-Mode" (G_fan) {channel="miio:basic:fan:Mode"}
Number OffDelayTime "Fan-Power Off Delay Time" (G_fan) {channel="miio:basic:fan:OffDelayTime"}
Switch Alarm "Fan-Alarm" (G_fan) {channel="miio:basic:fan:Alarm"}
Switch Brightness "Fan-Brightness" (G_fan) {channel="miio:basic:fan:Brightness"}
Switch PhysicalControlsLocked "Physical Control Locked-Physical Control Locked" (G_fan) {channel="miio:basic:fan:PhysicalControlsLocked"}
```

### Xiaomi Mijia Smart Tower Fan (dmaker.fan.p5) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Xiaomi Mijia Smart Tower Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch roll "Rotation" (G_fan) {channel="miio:basic:fan:roll"}
Number mode "Mode" (G_fan) {channel="miio:basic:fan:mode"}
Number angle "Angle" (G_fan) {channel="miio:basic:fan:angle"}
Number timer "Timer" (G_fan) {channel="miio:basic:fan:timer"}
Switch beep "Beep Sound" (G_fan) {channel="miio:basic:fan:beep"}
Number light "Light" (G_fan) {channel="miio:basic:fan:light"}
Switch child_lock "Child Lock" (G_fan) {channel="miio:basic:fan:child_lock"}
Number speed "Speed" (G_fan) {channel="miio:basic:fan:speed"}
```

### Xiaomi Mijia Smart Tower Fan (dmaker.fan.p8) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Xiaomi Mijia Smart Tower Fan" <status>
String Manufacturer "Device Information-Device Manufacturer" (G_fan) {channel="miio:basic:fan:Manufacturer"}
String Model "Device Information-Device Model" (G_fan) {channel="miio:basic:fan:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_fan) {channel="miio:basic:fan:SerialNumber"}
String FirmwareRevision "Device Information-Current Firmware Version" (G_fan) {channel="miio:basic:fan:FirmwareRevision"}
Switch On "Fan-Switch Status" (G_fan) {channel="miio:basic:fan:On"}
Number FanLevel "Fan-Fan Level" (G_fan) {channel="miio:basic:fan:FanLevel"}
Switch HorizontalSwing "Fan-Horizontal Swing" (G_fan) {channel="miio:basic:fan:HorizontalSwing"}
Number Mode "Fan-Mode" (G_fan) {channel="miio:basic:fan:Mode"}
Number OffDelayTime "Fan-Power Off Delay Time" (G_fan) {channel="miio:basic:fan:OffDelayTime"}
Switch Alarm "Fan-Alarm" (G_fan) {channel="miio:basic:fan:Alarm"}
Switch Brightness "Fan-Brightness" (G_fan) {channel="miio:basic:fan:Brightness"}
Switch PhysicalControlsLocked "Physical Control Locked-Physical Control Locked" (G_fan) {channel="miio:basic:fan:PhysicalControlsLocked"}
```

### Xiaomi Mijia Smart Tower Fan (dmaker.fan.p9) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Xiaomi Mijia Smart Tower Fan" <status>
String Manufacturer "Device Information-Device Manufacturer" (G_fan) {channel="miio:basic:fan:Manufacturer"}
String Model "Device Information-Device Model" (G_fan) {channel="miio:basic:fan:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_fan) {channel="miio:basic:fan:SerialNumber"}
String FirmwareRevision "Device Information-Current Firmware Version" (G_fan) {channel="miio:basic:fan:FirmwareRevision"}
Switch On "Fan-Switch Status" (G_fan) {channel="miio:basic:fan:On"}
Number FanLevel "Fan-Fan Level" (G_fan) {channel="miio:basic:fan:FanLevel"}
Number Mode "Fan-Mode" (G_fan) {channel="miio:basic:fan:Mode"}
Switch HorizontalSwing "Fan-Horizontal Swing" (G_fan) {channel="miio:basic:fan:HorizontalSwing"}
Number HorizontalAngle "Fan-Horizontal Angle" (G_fan) {channel="miio:basic:fan:HorizontalAngle"}
Switch Alarm "Fan-Alarm" (G_fan) {channel="miio:basic:fan:Alarm"}
Number OffDelayTime "Fan-Power Off Delay Time" (G_fan) {channel="miio:basic:fan:OffDelayTime"}
Switch Brightness "Fan-Brightness" (G_fan) {channel="miio:basic:fan:Brightness"}
Number MotorControl "Fan-Motor Control" (G_fan) {channel="miio:basic:fan:MotorControl"}
Number SpeedLevel "Fan-Speed Level" (G_fan) {channel="miio:basic:fan:SpeedLevel"}
Switch PhysicalControlsLocked "Physical Control Locked-Physical Control Locked" (G_fan) {channel="miio:basic:fan:PhysicalControlsLocked"}
```

### Xiaomi Mijia Smart Tower Fan (dmaker.fan.p10) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Xiaomi Mijia Smart Tower Fan" <status>
String Manufacturer "Device Information-Device Manufacturer" (G_fan) {channel="miio:basic:fan:Manufacturer"}
String Model "Device Information-Device Model" (G_fan) {channel="miio:basic:fan:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_fan) {channel="miio:basic:fan:SerialNumber"}
String FirmwareRevision "Device Information-Current Firmware Version" (G_fan) {channel="miio:basic:fan:FirmwareRevision"}
Switch On "Fan-Switch Status" (G_fan) {channel="miio:basic:fan:On"}
Number FanLevel "Fan-Fan Level" (G_fan) {channel="miio:basic:fan:FanLevel"}
Number Mode "Fan-Mode" (G_fan) {channel="miio:basic:fan:Mode"}
Switch HorizontalSwing "Fan-Horizontal Swing" (G_fan) {channel="miio:basic:fan:HorizontalSwing"}
Number HorizontalAngle "Fan-Horizontal Angle" (G_fan) {channel="miio:basic:fan:HorizontalAngle"}
Switch Alarm "Fan-Alarm" (G_fan) {channel="miio:basic:fan:Alarm"}
Number OffDelayTime "Fan-Power Off Delay Time" (G_fan) {channel="miio:basic:fan:OffDelayTime"}
Switch Brightness "Fan-Brightness" (G_fan) {channel="miio:basic:fan:Brightness"}
Number MotorControl "Fan-Motor Control" (G_fan) {channel="miio:basic:fan:MotorControl"}
Number SpeedLevel "Fan-Speed Level" (G_fan) {channel="miio:basic:fan:SpeedLevel"}
Switch PhysicalControlsLocked "Physical Control Locked-Physical Control Locked" (G_fan) {channel="miio:basic:fan:PhysicalControlsLocked"}
```

### Mi Smart Home Gateway v1 (lumi.gateway.v1) item file lines

note: Autogenerated example. Replace the id (gateway) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_gateway "Mi Smart Home Gateway v1" <status>
Switch telnetEnable "Enable Telnet" (G_gateway) {channel="miio:basic:gateway:telnetEnable"}
Number doorbellVol "Doorbell Volume" (G_gateway) {channel="miio:basic:gateway:doorbellVol"}
Number gatewayVol "Gateway Volume" (G_gateway) {channel="miio:basic:gateway:gatewayVol"}
Number alarmingVol "Alarming Volume" (G_gateway) {channel="miio:basic:gateway:alarmingVol"}
String doorbellPush "Doorbell Push" (G_gateway) {channel="miio:basic:gateway:doorbellPush"}
```

### Mi Smart Home Gateway v2 (lumi.gateway.v2) item file lines

note: Autogenerated example. Replace the id (gateway) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_gateway "Mi Smart Home Gateway v2" <status>
Switch telnetEnable "Enable Telnet" (G_gateway) {channel="miio:basic:gateway:telnetEnable"}
Number doorbellVol "Doorbell Volume" (G_gateway) {channel="miio:basic:gateway:doorbellVol"}
Number gatewayVol "Gateway Volume" (G_gateway) {channel="miio:basic:gateway:gatewayVol"}
Number alarmingVol "Alarming Volume" (G_gateway) {channel="miio:basic:gateway:alarmingVol"}
String doorbellPush "Doorbell Push" (G_gateway) {channel="miio:basic:gateway:doorbellPush"}
```

### Mi Smart Home Gateway v3 (lumi.gateway.v3) item file lines

note: Autogenerated example. Replace the id (gateway) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_gateway "Mi Smart Home Gateway v3" <status>
Switch telnetEnable "Enable Telnet" (G_gateway) {channel="miio:basic:gateway:telnetEnable"}
Number doorbellVol "Doorbell Volume" (G_gateway) {channel="miio:basic:gateway:doorbellVol"}
Number gatewayVol "Gateway Volume" (G_gateway) {channel="miio:basic:gateway:gatewayVol"}
Number alarmingVol "Alarming Volume" (G_gateway) {channel="miio:basic:gateway:alarmingVol"}
String doorbellPush "Doorbell Push" (G_gateway) {channel="miio:basic:gateway:doorbellPush"}
```

### Xiaomi Mi Mijia Gateway V3 ZNDMWG03LM (lumi.gateway.mgl03) item file lines

note: Autogenerated example. Replace the id (gateway) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_gateway "Xiaomi Mi Mijia Gateway V3 ZNDMWG03LM" <status>
Switch telnetEnable "Enable Telnet" (G_gateway) {channel="miio:basic:gateway:telnetEnable"}
Number doorbellVol "Doorbell Volume" (G_gateway) {channel="miio:basic:gateway:doorbellVol"}
Number gatewayVol "Gateway Volume" (G_gateway) {channel="miio:basic:gateway:gatewayVol"}
Number alarmingVol "Alarming Volume" (G_gateway) {channel="miio:basic:gateway:alarmingVol"}
String doorbellPush "Doorbell Push" (G_gateway) {channel="miio:basic:gateway:doorbellPush"}
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

### Mr Bond M1 Pro Smart Clothes Dryer (mrbond.airer.m1pro) item file lines

note: Autogenerated example. Replace the id (airer) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airer "Mr Bond M1 Pro Smart Clothes Dryer" <status>
Switch dry "Dry" (G_airer) {channel="miio:basic:airer:dry"}
Switch led "LED Status" (G_airer) {channel="miio:basic:airer:led"}
Number motor "Motor" (G_airer) {channel="miio:basic:airer:motor"}
Number drytime "Dry Time" (G_airer) {channel="miio:basic:airer:drytime"}
Number airer_location "Airer Location" (G_airer) {channel="miio:basic:airer:airer_location"}
Switch disinfect "disinfect" (G_airer) {channel="miio:basic:airer:disinfect"}
Number distime "Disinfect Time" (G_airer) {channel="miio:basic:airer:distime"}
```

### Mr Bond M1 Smart Clothes Dryer (mrbond.airer.m1s) item file lines

note: Autogenerated example. Replace the id (airer) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airer "Mr Bond M1 Smart Clothes Dryer" <status>
Switch dry "Dry" (G_airer) {channel="miio:basic:airer:dry"}
Switch led "LED Status" (G_airer) {channel="miio:basic:airer:led"}
Number motor "Motor" (G_airer) {channel="miio:basic:airer:motor"}
Number drytime "Dry Time" (G_airer) {channel="miio:basic:airer:drytime"}
Number airer_location "Airer Location" (G_airer) {channel="miio:basic:airer:airer_location"}
Switch disinfect "disinfect" (G_airer) {channel="miio:basic:airer:disinfect"}
Number distime "Disinfect Time" (G_airer) {channel="miio:basic:airer:distime"}
```

### Mr Bond M1 Super Smart Clothes Dryer (mrbond.airer.m1super) item file lines

note: Autogenerated example. Replace the id (airer) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airer "Mr Bond M1 Super Smart Clothes Dryer" <status>
Switch dry "Dry" (G_airer) {channel="miio:basic:airer:dry"}
Switch led "LED Status" (G_airer) {channel="miio:basic:airer:led"}
Number motor "Motor" (G_airer) {channel="miio:basic:airer:motor"}
Number drytime "Dry Time" (G_airer) {channel="miio:basic:airer:drytime"}
Number airer_location "Airer Location" (G_airer) {channel="miio:basic:airer:airer_location"}
Switch disinfect "disinfect" (G_airer) {channel="miio:basic:airer:disinfect"}
Number distime "Disinfect Time" (G_airer) {channel="miio:basic:airer:distime"}
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
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
```

### Xiaomi Philips Eyecare Smart Lamp 2 (philips.light.sread2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips Eyecare Smart Lamp 2" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Switch ambientPower "Ambient Power" (G_light) {channel="miio:basic:light:ambientPower"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number illumination "Ambient Illumination" (G_light) {channel="miio:basic:light:illumination"}
Switch eyecare "Eyecare" (G_light) {channel="miio:basic:light:eyecare"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
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

### Xiaomi Philips Wi-Fi Bulb E27 White (philips.light.hbulb) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips Wi-Fi Bulb E27 White" <status>
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

### Philips Ceiling Light (philips.light.bceiling1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch sw "Switch" (G_light) {channel="miio:basic:light:sw"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
Switch ms "MiBand Notifications" (G_light) {channel="miio:basic:light:ms"}
Switch ac "Auto Ambiance" (G_light) {channel="miio:basic:light:ac"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch mb "MiBand" (G_light) {channel="miio:basic:light:mb"}
```

### Philips Ceiling Light (philips.light.bceiling2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch sw "Switch" (G_light) {channel="miio:basic:light:sw"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
Switch ms "MiBand Notifications" (G_light) {channel="miio:basic:light:ms"}
Switch ac "Auto Ambiance" (G_light) {channel="miio:basic:light:ac"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch mb "MiBand" (G_light) {channel="miio:basic:light:mb"}
```

### Philips Light (philips.light.cbulb) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Color cid "Color" (G_light) {channel="miio:basic:light:cid"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch switch_en "Switch Enabled" (G_light) {channel="miio:basic:light:switch_en"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Philips Light (philips.light.cbulbs) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Color cid "Color" (G_light) {channel="miio:basic:light:cid"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch switch_en "Switch Enabled" (G_light) {channel="miio:basic:light:switch_en"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Philips Light (philips.light.dcolor) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Color cid "Color" (G_light) {channel="miio:basic:light:cid"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch switch_en "Switch Enabled" (G_light) {channel="miio:basic:light:switch_en"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Philips Light (philips.light.rwread) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number flm "Follow Me" (G_light) {channel="miio:basic:light:flm"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
```

### Philips Light (philips.light.lnblight1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch sw "Switch" (G_light) {channel="miio:basic:light:sw"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
Switch ms "MiBand Notifications" (G_light) {channel="miio:basic:light:ms"}
Switch ac "Auto Ambiance" (G_light) {channel="miio:basic:light:ac"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch mb "MiBand" (G_light) {channel="miio:basic:light:mb"}
```

### Philips Light (philips.light.lnblight2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch sw "Switch" (G_light) {channel="miio:basic:light:sw"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
Switch ms "MiBand Notifications" (G_light) {channel="miio:basic:light:ms"}
Switch ac "Auto Ambiance" (G_light) {channel="miio:basic:light:ac"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch mb "MiBand" (G_light) {channel="miio:basic:light:mb"}
```

### Philips Light (philips.light.lnlrlight) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch sw "Switch" (G_light) {channel="miio:basic:light:sw"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
Switch ms "MiBand Notifications" (G_light) {channel="miio:basic:light:ms"}
Switch ac "Auto Ambiance" (G_light) {channel="miio:basic:light:ac"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch mb "MiBand" (G_light) {channel="miio:basic:light:mb"}
```

### Philips Light (philips.light.lrceiling) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch sw "Switch" (G_light) {channel="miio:basic:light:sw"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
Switch ms "MiBand Notifications" (G_light) {channel="miio:basic:light:ms"}
Switch ac "Auto Ambiance" (G_light) {channel="miio:basic:light:ac"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch mb "MiBand" (G_light) {channel="miio:basic:light:mb"}
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

### Philips Down Light (philips.light.dlight) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Down Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.mceil) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.mceilm) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.mceils) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.obceil) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.obceim) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.obceis) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.sceil) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.sceilm) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.sceils) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.xzceil) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.xzceim) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
```

### Philips Ceiling Light (philips.light.xzceis) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Ceiling Light" <status>
Switch on "Power" (G_light) {channel="miio:basic:light:on"}
Number mode "Mode" (G_light) {channel="miio:basic:light:mode"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number dv "Delayed Turn-off" (G_light) {channel="miio:basic:light:dv"}
Switch WallSceneEn "Wall Scene Enable" (G_light) {channel="miio:basic:light:WallSceneEn"}
String WallScene "Wall Scene" (G_light) {channel="miio:basic:light:WallScene"}
String autoCct "Auto CCT" (G_light) {channel="miio:basic:light:autoCct"}
Number dimmingPeriod "Dimming Period" (G_light) {channel="miio:basic:light:dimmingPeriod"}
String MibandStatus "Mi Band Status" (G_light) {channel="miio:basic:light:MibandStatus"}
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
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
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

### Mi Smart Plug (chuangmi.plug.hmi206) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Wifi LED" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Smart Plug (chuangmi.plug.hmi208) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Wifi LED" (G_plug) {channel="miio:basic:plug:led"}
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

### Xiaomi Mijia vacuum V-RVCLM21B (viomi.vacuum.v6) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Xiaomi Mijia vacuum V-RVCLM21B" <status>
Number vacuumaction "Vacuum Action" (G_vacuum) {channel="miio:basic:vacuum:vacuumaction"}
Number state "State" (G_vacuum) {channel="miio:basic:vacuum:state"}
Number mode "Mode" (G_vacuum) {channel="miio:basic:vacuum:mode"}
Number err_state "Error" (G_vacuum) {channel="miio:basic:vacuum:err_state"}
Number battery_life "Battery" (G_vacuum) {channel="miio:basic:vacuum:battery_life"}
Number box_type "Box type" (G_vacuum) {channel="miio:basic:vacuum:box_type"}
Number mop_type "mop_type" (G_vacuum) {channel="miio:basic:vacuum:mop_type"}
Number s_time "Clean time" (G_vacuum) {channel="miio:basic:vacuum:s_time"}
Number s_area "Clean Area" (G_vacuum) {channel="miio:basic:vacuum:s_area"}
Number suction_grade "suction_grade" (G_vacuum) {channel="miio:basic:vacuum:suction_grade"}
Number water_grade "water_grade" (G_vacuum) {channel="miio:basic:vacuum:water_grade"}
Number remember_map "remember_map" (G_vacuum) {channel="miio:basic:vacuum:remember_map"}
Number has_map "has_map" (G_vacuum) {channel="miio:basic:vacuum:has_map"}
Number is_mop "is_mop" (G_vacuum) {channel="miio:basic:vacuum:is_mop"}
Number has_newmap "has_newmap" (G_vacuum) {channel="miio:basic:vacuum:has_newmap"}
```

### Xiaomi Mijia vacuum mop STYJ02YM (viomi.vacuum.v7) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Xiaomi Mijia vacuum mop STYJ02YM" <status>
Number vacuumaction "Vacuum Action" (G_vacuum) {channel="miio:basic:vacuum:vacuumaction"}
Number state "State" (G_vacuum) {channel="miio:basic:vacuum:state"}
Number mode "Mode" (G_vacuum) {channel="miio:basic:vacuum:mode"}
Number err_state "Error" (G_vacuum) {channel="miio:basic:vacuum:err_state"}
Number battery_life "Battery" (G_vacuum) {channel="miio:basic:vacuum:battery_life"}
Number box_type "Box type" (G_vacuum) {channel="miio:basic:vacuum:box_type"}
Number mop_type "mop_type" (G_vacuum) {channel="miio:basic:vacuum:mop_type"}
Number s_time "Clean time" (G_vacuum) {channel="miio:basic:vacuum:s_time"}
Number s_area "Clean Area" (G_vacuum) {channel="miio:basic:vacuum:s_area"}
Number suction_grade "suction_grade" (G_vacuum) {channel="miio:basic:vacuum:suction_grade"}
Number water_grade "water_grade" (G_vacuum) {channel="miio:basic:vacuum:water_grade"}
Number remember_map "remember_map" (G_vacuum) {channel="miio:basic:vacuum:remember_map"}
Number has_map "has_map" (G_vacuum) {channel="miio:basic:vacuum:has_map"}
Number is_mop "is_mop" (G_vacuum) {channel="miio:basic:vacuum:is_mop"}
Number has_newmap "has_newmap" (G_vacuum) {channel="miio:basic:vacuum:has_newmap"}
```

### Xiaomi Mijia vacuum mop STYJ02YM v2 (viomi.vacuum.v8) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Xiaomi Mijia vacuum mop STYJ02YM v2" <status>
Number vacuumaction "Vacuum Action" (G_vacuum) {channel="miio:basic:vacuum:vacuumaction"}
Number state "State" (G_vacuum) {channel="miio:basic:vacuum:state"}
Number mode "Mode" (G_vacuum) {channel="miio:basic:vacuum:mode"}
Number err_state "Error" (G_vacuum) {channel="miio:basic:vacuum:err_state"}
Number battery_life "Battery" (G_vacuum) {channel="miio:basic:vacuum:battery_life"}
Number box_type "Box type" (G_vacuum) {channel="miio:basic:vacuum:box_type"}
Number mop_type "mop_type" (G_vacuum) {channel="miio:basic:vacuum:mop_type"}
Number s_time "Clean time" (G_vacuum) {channel="miio:basic:vacuum:s_time"}
Number s_area "Clean Area" (G_vacuum) {channel="miio:basic:vacuum:s_area"}
Number suction_grade "suction_grade" (G_vacuum) {channel="miio:basic:vacuum:suction_grade"}
Number water_grade "water_grade" (G_vacuum) {channel="miio:basic:vacuum:water_grade"}
Number remember_map "remember_map" (G_vacuum) {channel="miio:basic:vacuum:remember_map"}
Number has_map "has_map" (G_vacuum) {channel="miio:basic:vacuum:has_map"}
Number is_mop "is_mop" (G_vacuum) {channel="miio:basic:vacuum:is_mop"}
Number has_newmap "has_newmap" (G_vacuum) {channel="miio:basic:vacuum:has_newmap"}
```

### Vacuum 1C STYTJ01ZHM (dreame.vacuum.mc1808) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Vacuum 1C STYTJ01ZHM" <status>
String vacuumaction "Vacuum Action" (G_vacuum) {channel="miio:basic:vacuum:vacuumaction"}
Number BatteryLevel "Battery-Battery Level" (G_vacuum) {channel="miio:basic:vacuum:BatteryLevel"}
Number ChargingState "Battery-Charging State" (G_vacuum) {channel="miio:basic:vacuum:ChargingState"}
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

### Mi Water Purifier v1 (yunmi.waterpurifier.v1) item file lines

note: Autogenerated example. Replace the id (waterpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpurifier "Mi Water Purifier v1" <status>
Switch power "Power" (G_waterpurifier) {channel="miio:basic:waterpurifier:power"}
Number run_status "Run Status" (G_waterpurifier) {channel="miio:basic:waterpurifier:run_status"}
Number temperature "Temperature" (G_waterpurifier) {channel="miio:basic:waterpurifier:temperature"}
Number rinse "Rinse" (G_waterpurifier) {channel="miio:basic:waterpurifier:rinse"}
Number tds_in "TDS in" (G_waterpurifier) {channel="miio:basic:waterpurifier:tds_in"}
Number tds_out "TDS out" (G_waterpurifier) {channel="miio:basic:waterpurifier:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_usedtime"}
```

### Mi Water Purifier v2 (yunmi.waterpurifier.v2) item file lines

note: Autogenerated example. Replace the id (waterpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpurifier "Mi Water Purifier v2" <status>
Switch power "Power" (G_waterpurifier) {channel="miio:basic:waterpurifier:power"}
Number run_status "Run Status" (G_waterpurifier) {channel="miio:basic:waterpurifier:run_status"}
Number temperature "Temperature" (G_waterpurifier) {channel="miio:basic:waterpurifier:temperature"}
Number rinse "Rinse" (G_waterpurifier) {channel="miio:basic:waterpurifier:rinse"}
Number tds_in "TDS in" (G_waterpurifier) {channel="miio:basic:waterpurifier:tds_in"}
Number tds_out "TDS out" (G_waterpurifier) {channel="miio:basic:waterpurifier:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_usedtime"}
```

### Mi Water Purifier v3 (yunmi.waterpurifier.v3) item file lines

note: Autogenerated example. Replace the id (waterpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpurifier "Mi Water Purifier v3" <status>
Switch power "Power" (G_waterpurifier) {channel="miio:basic:waterpurifier:power"}
Number run_status "Run Status" (G_waterpurifier) {channel="miio:basic:waterpurifier:run_status"}
Number temperature "Temperature" (G_waterpurifier) {channel="miio:basic:waterpurifier:temperature"}
Number rinse "Rinse" (G_waterpurifier) {channel="miio:basic:waterpurifier:rinse"}
Number tds_in "TDS in" (G_waterpurifier) {channel="miio:basic:waterpurifier:tds_in"}
Number tds_out "TDS out" (G_waterpurifier) {channel="miio:basic:waterpurifier:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_usedtime"}
```

### Mi Water Purifier v4 (yunmi.waterpurifier.v4) item file lines

note: Autogenerated example. Replace the id (waterpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpurifier "Mi Water Purifier v4" <status>
Switch power "Power" (G_waterpurifier) {channel="miio:basic:waterpurifier:power"}
Number run_status "Run Status" (G_waterpurifier) {channel="miio:basic:waterpurifier:run_status"}
Number temperature "Temperature" (G_waterpurifier) {channel="miio:basic:waterpurifier:temperature"}
Number rinse "Rinse" (G_waterpurifier) {channel="miio:basic:waterpurifier:rinse"}
Number tds_in "TDS in" (G_waterpurifier) {channel="miio:basic:waterpurifier:tds_in"}
Number tds_out "TDS out" (G_waterpurifier) {channel="miio:basic:waterpurifier:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpurifier) {channel="miio:basic:waterpurifier:f3_usedtime"}
```

### Mi Water Purifier lx2 (yunmi.waterpuri.lx2) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx2" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx3 (yunmi.waterpuri.lx3) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx3" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx4 (yunmi.waterpuri.lx4) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx4" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx5 (yunmi.waterpuri.lx5) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx5" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx6 (yunmi.waterpuri.lx6) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx6" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx7 (yunmi.waterpuri.lx7) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx7" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx8 (yunmi.waterpuri.lx8) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx8" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx9 (yunmi.waterpuri.lx9) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx9" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx10 (yunmi.waterpuri.lx10) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx10" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx11 (yunmi.waterpuri.lx11) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx11" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Mi Water Purifier lx12 (yunmi.waterpuri.lx12) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier lx12" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
Number rinse "Rinse" (G_waterpuri) {channel="miio:basic:waterpuri:rinse"}
Number tds_in "TDS in" (G_waterpuri) {channel="miio:basic:waterpuri:tds_in"}
Number tds_out "TDS out" (G_waterpuri) {channel="miio:basic:waterpuri:tds_out"}
Number f1_totalflow "Filter 1 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totalflow"}
Number f1_totaltime "Filter 1 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_totaltime"}
Number f1_usedflow "Filter 1 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedflow"}
Number f1_usedtime "Filter 1 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f1_usedtime"}
Number f2_totalflow "Filter 2 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totalflow"}
Number f2_totaltime "Filter 2 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_totaltime"}
Number f2_usedflow "Filter 2 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedflow"}
Number f2_usedtime "Filter 2 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f2_usedtime"}
Number f3_totalflow "Filter 3 Total Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totalflow"}
Number f3_totaltime "Filter 3 Total Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_totaltime"}
Number f3_usedflow "Filter 3 Used Flow" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedflow"}
Number f3_usedtime "Filter 3 Used Time" (G_waterpuri) {channel="miio:basic:waterpuri:f3_usedtime"}
```

### Xiaomi Mijia Whale Smart Toilet Cover (xjx.toilet.pro) item file lines

note: Autogenerated example. Replace the id (toilet) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_toilet "Xiaomi Mijia Whale Smart Toilet Cover" <status>
Number seat_temp "Seat Temperature" (G_toilet) {channel="miio:basic:toilet:seat_temp"}
Number status_seatheat "Seat Status" (G_toilet) {channel="miio:basic:toilet:status_seatheat"}
Number water_temp_t "Water Temperature" (G_toilet) {channel="miio:basic:toilet:water_temp_t"}
Number fan_temp "Fan Temperature" (G_toilet) {channel="miio:basic:toilet:fan_temp"}
Number status_led "Night Light" (G_toilet) {channel="miio:basic:toilet:status_led"}
```

### Xiaomi Mijia Smart Toilet Cover (xjx.toilet.relax) item file lines

note: Autogenerated example. Replace the id (toilet) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_toilet "Xiaomi Mijia Smart Toilet Cover" <status>
Number seat_temp "Seat Temperature" (G_toilet) {channel="miio:basic:toilet:seat_temp"}
Number status_seatheat "Seat Status" (G_toilet) {channel="miio:basic:toilet:status_seatheat"}
Number water_temp_t "Water Temperature" (G_toilet) {channel="miio:basic:toilet:water_temp_t"}
Number fan_temp "Fan Temperature" (G_toilet) {channel="miio:basic:toilet:fan_temp"}
Number status_led "Night Light" (G_toilet) {channel="miio:basic:toilet:status_led"}
```

### Xiaomi Mijia Smart Toilet Cover (xjx.toilet.pure) item file lines

note: Autogenerated example. Replace the id (toilet) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_toilet "Xiaomi Mijia Smart Toilet Cover" <status>
Number seat_temp "Seat Temperature" (G_toilet) {channel="miio:basic:toilet:seat_temp"}
Number status_seatheat "Seat Status" (G_toilet) {channel="miio:basic:toilet:status_seatheat"}
Number water_temp_t "Water Temperature" (G_toilet) {channel="miio:basic:toilet:water_temp_t"}
Number fan_temp "Fan Temperature" (G_toilet) {channel="miio:basic:toilet:fan_temp"}
Number status_led "Night Light" (G_toilet) {channel="miio:basic:toilet:status_led"}
```

### Xiaomi Mijia Smart Toilet Cover (xjx.toilet.zero) item file lines

note: Autogenerated example. Replace the id (toilet) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_toilet "Xiaomi Mijia Smart Toilet Cover" <status>
Number seat_temp "Seat Temperature" (G_toilet) {channel="miio:basic:toilet:seat_temp"}
Number status_seatheat "Seat Status" (G_toilet) {channel="miio:basic:toilet:status_seatheat"}
Number water_temp_t "Water Temperature" (G_toilet) {channel="miio:basic:toilet:water_temp_t"}
Number fan_temp "Fan Temperature" (G_toilet) {channel="miio:basic:toilet:fan_temp"}
Number status_led "Night Light" (G_toilet) {channel="miio:basic:toilet:status_led"}
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

### Yeelight Lamp (yeelink.light.bslamp3) item file lines

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

### Yeelight BadHeater (yeelink.bhf_light.v1) item file lines

note: Autogenerated example. Replace the id (bhf_light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_bhf_light "Yeelight BadHeater" <status>
Switch power "Power" (G_bhf_light) {channel="miio:basic:bhf_light:power"}
String bh_mode "Bath Heater mode" (G_bhf_light) {channel="miio:basic:bhf_light:bh_mode"}
Dimmer brightness "Brightness" (G_bhf_light) {channel="miio:basic:bhf_light:brightness"}
Number delayoff "Shutdown Timer" (G_bhf_light) {channel="miio:basic:bhf_light:delayoff"}
Number temperature "Temperature" (G_bhf_light) {channel="miio:basic:bhf_light:temperature"}
Number nightlightBrightness "Nightlight Brightness" (G_bhf_light) {channel="miio:basic:bhf_light:nightlightBrightness"}
```

### Yeelight BadHeater (yeelink.bhf_light.v2) item file lines

note: Autogenerated example. Replace the id (bhf_light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_bhf_light "Yeelight BadHeater" <status>
Switch power "Power" (G_bhf_light) {channel="miio:basic:bhf_light:power"}
String bh_mode "Bath Heater mode" (G_bhf_light) {channel="miio:basic:bhf_light:bh_mode"}
Dimmer brightness "Brightness" (G_bhf_light) {channel="miio:basic:bhf_light:brightness"}
Number delayoff "Shutdown Timer" (G_bhf_light) {channel="miio:basic:bhf_light:delayoff"}
Number temperature "Temperature" (G_bhf_light) {channel="miio:basic:bhf_light:temperature"}
Number nightlightBrightness "Nightlight Brightness" (G_bhf_light) {channel="miio:basic:bhf_light:nightlightBrightness"}
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
Number delayoff "Shutdown Timer" (G_ceiling4) {channel="miio:basic:ceiling4:delayoff"}
Number colorTemperature "Color Temperature" (G_ceiling4) {channel="miio:basic:ceiling4:colorTemperature"}
Number colorMode "Color Mode" (G_ceiling4) {channel="miio:basic:ceiling4:colorMode"}
String name "Name" (G_ceiling4) {channel="miio:basic:ceiling4:name"}
String customScene "Set Scene" (G_ceiling4) {channel="miio:basic:ceiling4:customScene"}
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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling14) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling15) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling16) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling17) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling18) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling19) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling20) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling21) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling22) item file lines

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

### Yeelight LED Ceiling Lamp (yeelink.light.ceiling23) item file lines

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

### Yeelight LED Ceiling Ambi Lamp (yeelink.light.ceiling4.ambi) item file lines

note: Autogenerated example. Replace the id (ceiling4) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_ceiling4 "Yeelight LED Ceiling Ambi Lamp" <status>
Switch power "Power" (G_ceiling4) {channel="miio:basic:ceiling4:power"}
Dimmer brightness "Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:brightness"}
Number delayoff "Shutdown Timer" (G_ceiling4) {channel="miio:basic:ceiling4:delayoff"}
Number colorTemperature "Color Temperature" (G_ceiling4) {channel="miio:basic:ceiling4:colorTemperature"}
Number colorMode "Color Mode" (G_ceiling4) {channel="miio:basic:ceiling4:colorMode"}
String name "Name" (G_ceiling4) {channel="miio:basic:ceiling4:name"}
String customScene "Set Scene" (G_ceiling4) {channel="miio:basic:ceiling4:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:nightlightBrightness"}
```

### Yeelight LED Ceiling Ambi Lamp (yeelink.light.ceiling10.ambi) item file lines

note: Autogenerated example. Replace the id (ceiling10) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_ceiling10 "Yeelight LED Ceiling Ambi Lamp" <status>
Switch power "Power" (G_ceiling10) {channel="miio:basic:ceiling10:power"}
Dimmer brightness "Brightness" (G_ceiling10) {channel="miio:basic:ceiling10:brightness"}
Number delayoff "Shutdown Timer" (G_ceiling10) {channel="miio:basic:ceiling10:delayoff"}
Number colorTemperature "Color Temperature" (G_ceiling10) {channel="miio:basic:ceiling10:colorTemperature"}
Number colorMode "Color Mode" (G_ceiling10) {channel="miio:basic:ceiling10:colorMode"}
String name "Name" (G_ceiling10) {channel="miio:basic:ceiling10:name"}
String customScene "Set Scene" (G_ceiling10) {channel="miio:basic:ceiling10:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_ceiling10) {channel="miio:basic:ceiling10:nightlightBrightness"}
```

### Yeelight LED Ceiling Ambi Lamp (yeelink.light.ceiling19.ambi) item file lines

note: Autogenerated example. Replace the id (ceiling19) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_ceiling19 "Yeelight LED Ceiling Ambi Lamp" <status>
Switch power "Power" (G_ceiling19) {channel="miio:basic:ceiling19:power"}
Dimmer brightness "Brightness" (G_ceiling19) {channel="miio:basic:ceiling19:brightness"}
Number delayoff "Shutdown Timer" (G_ceiling19) {channel="miio:basic:ceiling19:delayoff"}
Number colorTemperature "Color Temperature" (G_ceiling19) {channel="miio:basic:ceiling19:colorTemperature"}
Number colorMode "Color Mode" (G_ceiling19) {channel="miio:basic:ceiling19:colorMode"}
String name "Name" (G_ceiling19) {channel="miio:basic:ceiling19:name"}
String customScene "Set Scene" (G_ceiling19) {channel="miio:basic:ceiling19:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_ceiling19) {channel="miio:basic:ceiling19:nightlightBrightness"}
```

### Yeelight LED Ceiling Ambi Lamp (yeelink.light.ceiling20.ambi) item file lines

note: Autogenerated example. Replace the id (ceiling20) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_ceiling20 "Yeelight LED Ceiling Ambi Lamp" <status>
Switch power "Power" (G_ceiling20) {channel="miio:basic:ceiling20:power"}
Dimmer brightness "Brightness" (G_ceiling20) {channel="miio:basic:ceiling20:brightness"}
Number delayoff "Shutdown Timer" (G_ceiling20) {channel="miio:basic:ceiling20:delayoff"}
Number colorTemperature "Color Temperature" (G_ceiling20) {channel="miio:basic:ceiling20:colorTemperature"}
Number colorMode "Color Mode" (G_ceiling20) {channel="miio:basic:ceiling20:colorMode"}
String name "Name" (G_ceiling20) {channel="miio:basic:ceiling20:name"}
String customScene "Set Scene" (G_ceiling20) {channel="miio:basic:ceiling20:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_ceiling20) {channel="miio:basic:ceiling20:nightlightBrightness"}
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

### Yeelight White (yeelink.light.mono5) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight White" <status>
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

### Yeelight (yeelink.light.lamp4) item file lines

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

### Yeelight (yeelink.light.lamp5) item file lines

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

### Yeelight (yeelink.light.lamp6) item file lines

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

### Yeelight (yeelink.light.lamp7) item file lines

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

### Yeelight (yeelink.light.lamp8) item file lines

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

### Yeelight Panel (yeelink.light.panel1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Panel" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
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

### Yeelight Strip (yeelink.light.strip4) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Strip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
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

### Yeelight yilai ceiling (yilai.light.ceiling1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight yilai ceiling" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight yilai ceiling (yilai.light.ceiling2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight yilai ceiling" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight yilai ceiling (yilai.light.ceiling3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight yilai ceiling" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Zhimi Heater (zhimi.heater.za1) item file lines

note: Autogenerated example. Replace the id (heater) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_heater "Zhimi Heater" <status>
Switch power "Power" (G_heater) {channel="miio:basic:heater:power"}
Number target_temperature "Target Temperature" (G_heater) {channel="miio:basic:heater:target_temperature"}
Number brightness "Brightness" (G_heater) {channel="miio:basic:heater:brightness"}
Switch buzzer "Buzzer Status" (G_heater) {channel="miio:basic:heater:buzzer"}
Number relative_humidity "Relative Humidity" (G_heater) {channel="miio:basic:heater:relative_humidity"}
Switch childlock "Child Lock" (G_heater) {channel="miio:basic:heater:childlock"}
Switch HWSwitch "HW Switch" (G_heater) {channel="miio:basic:heater:HWSwitch"}
Number temperature "Temperature" (G_heater) {channel="miio:basic:heater:temperature"}
Number usedhours "Run Time" (G_heater) {channel="miio:basic:heater:usedhours"}
```



### <a name="Country-Servers">Country Servers</a>

Known country Servers: cn, de, i2, ru, sg, us
Mapping of countries in mihome app to server:

| Country                  | Country Code | Server |
|--------------------------|--------------|--------|    
| Afghanistan              | AF           | sg     |
| Albania                  | AL           | de     |
| Algeria                  | DZ           | sg     |
| American Samoa           | AS           | sg     |
| Andorra                  | AD           | de     |
| Angola                   | AO           | sg     |
| Anguilla                 | AI           | us     |
| Antarctica               | AQ           | sg     |
| Antigua and Barbuda      | AG           | us     |
| Argentina                | AR           | us     |
| Armenia                  | AM           | sg     |
| Aruba                    | AW           | us     |
| Ascension Island         | AC           | sg     |
| Australia                | AU           | sg     |
| Austria                  | AT           | de     |
| Azerbaijan               | AZ           | sg     |
| Bahamas                  | BS           | us     |
| Bahrain                  | BH           | sg     |
| Bangladesh               | BD           | sg     |
| Barbados                 | BB           | us     |
| Belarus                  | BY           | de     |
| Belgium                  | BE           | de     |
| Belize                   | BZ           | us     |
| Benin                    | BJ           | sg     |
| Bermuda                  | BM           | us     |
| Bhutan                   | BT           | sg     |
| Bolivia                  | BO           | us     |
| Bosnia and Herzegovina   | BA           | de     |
| Botswana                 | BW           | sg     |
| Bouvet Island            | BV           | sg     |
| Brazil                   | BR           | us     |
| British Indian Ocean Territory | IO     | sg     |
| British Virgin Islands   | VG           | us     |
| Brunei                   | BN           | sg     |
| Bulgaria                 | BG           | de     |
| Burkina Faso             | BF           | sg     |
| Burundi                  | BI           | sg     |
| Cambodia                 | KH           | sg     |
| Cameroon                 | CM           | sg     |
| Canada                   | CA           | us     |
| Canary Islands           | IC           | sg     |
| Cape Verde               | CV           | sg     |
| Cayman Islands           | KY           | us     |
| Central African Republic | CF           | sg     |
| Ceuta and Melilla        | EA           | de     |
| Chad                     | TD           | sg     |
| Chile                    | CL           | us     |
| Chinese mainland         | CN           | cn     |
| Christmas Island         | CX           | sg     |
| Cocos Islands            | CC           | sg     |
| Colombia                 | CO           | us     |
| Comoros                  | KM           | sg     |
| Congo - Brazzaville      | CG           | sg     |
| Congo - Kinshasa         | CD           | sg     |
| Cook Islands             | CK           | sg     |
| Costa Rica               | CR           | us     |
| Croatia                  | HR           | de     |
| Curaçao                  | CW           | us     |
| Cyprus                   | CY           | de     |
| Czechia                  | CZ           | de     |
| Côte d'Ivoire            | CI           | sg     |
| Denmark                  | DK           | de     |
| Diego Garcia             | DG           | sg     |
| Djibouti                 | DJ           | sg     |
| Dominica                 | DM           | us     |
| Dominican Republic       | DO           | us     |
| Dutch Caribbean          | BQ           | us     |
| Ecuador                  | EC           | us     |
| Egypt                    | EG           | sg     |
| El Salvador              | SV           | us     |
| Equatorial Guinea        | GQ           | sg     |
| Eritrea                  | ER           | sg     |
| Estonia                  | EE           | de     |
| Ethiopia                 | ET           | sg     |
| Falkland Islands         | FK           | us     |
| Faroe Islands            | FO           | de     |
| Fiji                     | FJ           | sg     |
| Finland                  | FI           | de     |
| France                   | FR           | de     |
| French Guiana            | GF           | de     |
| French Southern Territories | TF        | sg     |
| French polynesia         | PF           | sg     |
| Gabon                    | GA           | sg     |
| Gambia                   | GM           | sg     |
| Georgia                  | GE           | sg     |
| Germany                  | DE           | de     |
| Ghana                    | GH           | sg     |
| Gibraltar                | GI           | de     |
| Greece                   | GR           | de     |
| Greenland                | GL           | us     |
| Grenada                  | GD           | us     |
| Guadeloupe               | GP           | us     |
| Guam                     | GU           | sg     |
| Guatemala                | GT           | us     |
| Guernsey                 | GG           | de     |
| Guinea                   | GN           | sg     |
| Guinea-Bissau            | GW           | sg     |
| Guyana                   | GY           | us     |
| Haiti                    | HT           | us     |
| Honduras                 | HN           | us     |
| Hong Kong                | HK           | sg     |
| Hungary                  | HU           | de     |
| Iceland                  | IS           | de     |
| India                    | IN           | i2     |
| Indonesia                | ID           | sg     |
| Iraq                     | IQ           | sg     |
| Ireland                  | IE           | de     |
| Isle of Man              | IM           | de     |
| Israel                   | IL           | sg     |
| Italy                    | IT           | de     |
| Jamaica                  | JM           | us     |
| Japan                    | JP           | sg     |
| Jersey                   | JE           | de     |
| Jordan                   | JO           | sg     |
| Kazakhstan               | KZ           | sg     |
| Kenya                    | KE           | sg     |
| Kiribati                 | KI           | sg     |
| Kosovo                   | XK           | de     |
| Kuwait                   | KW           | sg     |
| Kyrgyzstan               | KG           | sg     |
| Laos                     | LA           | sg     |
| Latvia                   | LV           | de     |
| Lebanon                  | LB           | sg     |
| Lesotho                  | LS           | sg     |
| Liberia                  | LR           | sg     |
| Libya                    | LY           | sg     |
| Liechtenstein            | LI           | de     |
| Lithuania                | LT           | de     |
| Luxembourg               | LU           | de     |
| Macao                    | MO           | sg     |
| Macedonia                | MK           | de     |
| Madagascar               | MG           | sg     |
| Malawi                   | MW           | sg     |
| Malaysia                 | MY           | sg     |
| Maldives                 | MV           | sg     |
| Mali                     | ML           | sg     |
| Malta                    | MT           | de     |
| Marshall islands         | MH           | sg     |
| Martinique               | MQ           | us     |
| Mauritania               | MR           | sg     |
| Mauritius                | MU           | sg     |
| Mayotte                  | YT           | sg     |
| Mexico                   | MX           | us     |
| Micronesia               | FM           | sg     |
| Moldova                  | MD           | de     |
| Monaco                   | MC           | de     |
| Mongolia                 | MN           | sg     |
| Montenegro               | ME           | de     |
| Montserrat               | MS           | us     |
| Morocco                  | MA           | sg     |
| Mozambique               | MZ           | sg     |
| Myanmar (Burma)          | MM           | sg     |
| Namibia                  | NA           | sg     |
| Nauru                    | NR           | sg     |
| Nepal                    | NP           | sg     |
| Netherlands              | NL           | de     |
| New Caledonia            | NC           | sg     |
| New Zealand              | NZ           | sg     |
| Nicaragua                | NI           | us     |
| Niger                    | NE           | sg     |
| Nigeria                  | NG           | sg     |
| Niue                     | NU           | sg     |
| Norfolk Island           | NF           | sg     |
| Northern Mariana Islands | MP           | sg     |
| Norway                   | NO           | de     |
| Oman                     | OM           | sg     |
| Pakistan                 | PK           | sg     |
| Palau                    | PW           | sg     |
| Palestinian Territories  | PS           | sg     |
| Panama                   | PA           | us     |
| Papua New Guinea         | PG           | sg     |
| Paraguay                 | PY           | us     |
| Peru                     | PE           | us     |
| Philippines              | PH           | sg     |
| Pitcairn Islands         | PN           | sg     |
| Poland                   | PL           | de     |
| Portugal                 | PT           | de     |
| Puerto Rico              | PR           | us     |
| Qatar                    | QA           | sg     |
| Romania                  | RO           | de     |
| Russia                   | RU           | ru     |
| Rwanda                   | RW           | sg     |
| Réunion                  | RE           | sg     |
| Saint Barthélemy         | BL           | us     |
| Saint Helena             | SH           | sg     |
| Saint Kitts and Nevis    | KN           | us     |
| Saint Lucia              | LC           | us     |
| Saint Martin             | MF           | de     |
| Saint Pierre and Miquelon | PM          | us     |
| Saint Vincent and The Grenadines | VC   | us     |
| Samoa                    | WS           | sg     |
| San Marino               | SM           | de     |
| Saudi Arabia             | SA           | sg     |
| Senegal                  | SN           | sg     |
| Serbia                   | RS           | de     |
| Seychelles               | SC           | sg     |
| Sierra Leone             | SL           | sg     |
| Singapore                | SG           | sg     |
| Slovakia                 | SK           | de     |
| Slovenia                 | SI           | de     |
| Solomon Islands          | SB           | sg     |
| Somalia                  | SO           | sg     |
| South Africa             | ZA           | sg     |
| South Georgia and South Sandwich Islands | GS    | us     |
| South Korea              | KR           | sg     |
| South Sudan              | SS           | sg     |
| Spain                    | ES           | de     |
| Sri Lanka                | LK           | sg     |
| Suriname                 | SR           | us     |
| Svalbard and Jan Mayen   | SJ           | de     |
| Swaziland                | SZ           | sg     |
| Sweden                   | SE           | de     |
| Switzerland              | CH           | de     |
| São Tomé and Príncipe    | ST           | sg     |
| Taiwan                   | TW           | sg     |
| Tajikistan               | TJ           | sg     |
| Tanzania                 | TZ           | sg     |
| Thailand                 | TH           | sg     |
| Timor-Leste              | TL           | sg     |
| Togo                     | TG           | sg     |
| Tokelau                  | TK           | sg     |
| Tonga                    | TO           | sg     |
| Trinidad and Tobago      | TT           | us     |
| Tristan da Cunha         | TA           | sg     |
| Tunisia                  | TN           | sg     |
| Turkey                   | TR           | sg     |
| Turkmenistan             | TM           | sg     |
| Turks and Caicos Islands | TC           | us     |
| Tuvalu                   | TV           | sg     |
| U.S. Virgin Islands      | VI           | us     |
| Uganda                   | UG           | sg     |
| Ukraine                  | UA           | de     |
| United Arab Emirates     | AE           | sg     |
| United Kingdom           | GB           | de     |
| United States            | US           | us     |
| United States Minor Outlying Islands | UM | us   |
| Uruguay                  | UY           | us     |
| Uzbekistan               | UZ           | sg     |
| Vanuatu                  | VU           | sg     |
| Vatican                  | VA           | de     |
| Vietnam                  | VN           | sg     |
| Wallis and Futuna        | WF           | sg     |
| Western Sahara           | EH           | sg     |
| Yemen                    | YE           | sg     |
| Zambia                   | ZM           | sg     |
| Zimbabwe                 | ZW           | sg     |
| Åland Islands            | AX           | de     |