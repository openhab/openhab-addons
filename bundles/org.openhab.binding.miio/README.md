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
Use the one of the regional servers: cn,de,i2,tw,ru,sg,us.
Multiple servers can be separated with comma, or leave blank to test all known servers.

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
The list of the known countries and related severs is [here](#Country-Servers).

After successful Xiaomi cloud login, the binding will use the connection to retrieve the required device tokens from the cloud. 
For Xiaomi vacuums the map can be visualized in openHAB using the cloud connection.

![Binding Config](doc/miioBindingConfig.jpg)

## Thing Configuration

Each Xiaomi device (thing) needs the IP address and token configured to be able to communicate. See discovery for details.
Optional configuration is the refresh interval and the deviceID. Note that the deviceID is automatically retrieved when it is left blank.
The configuration for model is automatically retrieved from the device in normal operation. 
However, for devices that are unsupported, you may override the value and try to use a model string from a similar device to experimentally use your device with the binding.

| Parameter       | Type    | Required | Description                                                         |
|-----------------|---------|----------|---------------------------------------------------------------------|
| host            | text    | true     | Device IP address                                                   |
| token           | text    | true     | Token for communication (in Hex)                                    |
| deviceId        | text    | true     | Device ID number for communication (in Hex)                         |
| model           | text    | false    | Device model string, used to determine the subtype                  |
| refreshInterval | integer | false    | Refresh interval for refreshing the data in seconds. (0=disabled)   |
| timeout         | integer | false    | Timeout time in milliseconds                                        |
| communication   | test    | false    | Communicate direct or via cloud (options values: 'direct', 'cloud') |

Note: Suggest to use the cloud communication only for devices that require it. It is unknown at this time if Xiaomi has a rate limit or other limitations on the cloud usage. e.g. if having many devices would trigger some throttling from the cloud side.

### Example Thing file

`Thing miio:basic:light "My Light" [ host="192.168.x.x", token="put here your token", deviceId="0326xxxx", model="philips.light.bulb", communication="direct"  ]` 

or in case of unknown models include the model information of a similar device that is supported:

`Thing miio:vacuum:s50 "vacuum" @ "livingroom" [ host="192.168.15.20", token="xxxxxxx", deviceId=“0470DDAA”, model="roborock.vacuum.s4", communication="cloud"]`

# Mi IO Devices

Currently the miio binding supports more than 250 different models.

| Device                       | ThingType        | Device Model           | Supported | Remark     |
|------------------------------|------------------|------------------------|-----------|------------|
| AUX Smart Air Conditioner    | miio:unsupported | aux.aircondition.v1    | No        |            |
| Jinxing Smart Air Conditioner | miio:unsupported | idelan.aircondition.v1 | No        |            |
| Midea AC-i Youth             | miio:unsupported | midea.aircondition.v1  | No        |            |
| Midea Air Conditioner v2     | miio:unsupported | midea.aircondition.v2  | No        |            |
| Midea AC-Cool Golden         | miio:unsupported | midea.aircondition.xa1 | No        |            |
| Mi PM2.5 Air Quality Monitor | miio:basic       | [zhimi.airmonitor.v1](#zhimi-airmonitor-v1) | Yes       |            |
| Mi Multifunction Air Monitor | miio:basic       | [cgllc.airmonitor.b1](#cgllc-airmonitor-b1) | Yes       |            |
| Qingping Air Monitor         | miio:basic       | [cgllc.airmonitor.s1](#cgllc-airmonitor-s1) | Yes       |            |
| Smartmi Humidifier           | miio:basic       | [zhimi.humidifier.v1](#zhimi-humidifier-v1) | Yes       |            |
| Smartmi Evaporative Humidifier | miio:basic       | [zhimi.humidifier.ca1](#zhimi-humidifier-ca1) | Yes       |            |
| Smartmi Evaporative Humidifer 2 | miio:basic       | [zhimi.humidifier.ca4](#zhimi-humidifier-ca4) | Yes       |            |
| Smartmi Evaporative Humidifier | miio:basic       | [zhimi.humidifier.cb1](#zhimi-humidifier-cb1) | Yes       |            |
| Mi Smart Humidifier          | miio:basic       | [deerma.humidifier.mjjsq](#deerma-humidifier-mjjsq) | Yes       |            |
| Mi Air Purifier v1           | miio:basic       | [zhimi.airpurifier.v1](#zhimi-airpurifier-v1) | Yes       |            |
| Mi Air Purifier v2           | miio:basic       | [zhimi.airpurifier.v2](#zhimi-airpurifier-v2) | Yes       |            |
| Mi Air Purifier v3           | miio:basic       | [zhimi.airpurifier.v3](#zhimi-airpurifier-v3) | Yes       |            |
| Mi Air Purifier v5           | miio:basic       | [zhimi.airpurifier.v5](#zhimi-airpurifier-v5) | Yes       |            |
| Mi Air Purifier Pro v6       | miio:basic       | [zhimi.airpurifier.v6](#zhimi-airpurifier-v6) | Yes       |            |
| Mi Air Purifier Pro v7       | miio:basic       | [zhimi.airpurifier.v7](#zhimi-airpurifier-v7) | Yes       |            |
| Mi Air Purifier 2 (mini)     | miio:basic       | [zhimi.airpurifier.m1](#zhimi-airpurifier-m1) | Yes       |            |
| Mi Air Purifier 2            | miio:basic       | [zhimi.airpurifier.m2](#zhimi-airpurifier-m2) | Yes       |            |
| Mi Air Purifier 2S           | miio:basic       | [zhimi.airpurifier.ma1](#zhimi-airpurifier-ma1) | Yes       |            |
| Mi Air Purifier 2S           | miio:basic       | [zhimi.airpurifier.ma2](#zhimi-airpurifier-ma2) | Yes       |            |
| Mi Air Purifier 3            | miio:basic       | [zhimi.airpurifier.ma4](#zhimi-airpurifier-ma4) | Yes       |            |
| Mi Air Purifier 3/3H         | miio:basic       | [zhimi.airpurifier.mb3](#zhimi-airpurifier-mb3) | Yes       |            |
| Mi Air Purifier Super        | miio:basic       | [zhimi.airpurifier.sa1](#zhimi-airpurifier-sa1) | Yes       |            |
| Mi Air Purifier MAX / MAX Pro | miio:basic       | [zhimi.airpurifier.sa2](#zhimi-airpurifier-sa2) | Yes       |            |
| Mi Fresh Air Ventilator      | miio:basic       | [dmaker.airfresh.t2017](#dmaker-airfresh-t2017) | Yes       |            |
| Mi Fresh Air Ventilator A1-150 | miio:basic       | [dmaker.airfresh.a1](#dmaker-airfresh-a1) | Yes       |            |
| Mi AI Alarm                  | miio:unsupported | zimi.clock.myk01       | No        |            |
| Yeelight Smart Bath Heater   | miio:unsupported | yeelight.bhf_light.v2  | No        |            |
| Gosund Smart Plug            | miio:basic       | [cuco.plug.cp1](#cuco-plug-cp1) | Yes       |            |
| WIDETECH WDH318EFW1 Internet Dehumidifier | miio:unsupported | nwt.derh.wdh318efw1    | No        |            |
| Mi Robot Vacuum Mop 1C STYTJ01ZHM | miio:basic       | [dreame.vacuum.mc1808](#dreame-vacuum-mc1808) | Yes       | Identified manual actions for execution<br />`action{"did":"battery-start-charge","siid":2,"aiid":1,"in":[]}`<br />`action{"did":"vacuum-start-sweep","siid":3,"aiid":1,"in":[]}`<br />`action{"did":"vacuum-stop-sweeping","siid":3,"aiid":2,"in":[]}`<br />`action{"did":"brush-cleaner-reset-brush-life","siid":26,"aiid":1,"in":[]}`<br />`action{"did":"filter-reset-filter-life","siid":27,"aiid":1,"in":[]}`<br />`action{"did":"brush-cleaner-reset-brush-life","siid":28,"aiid":1,"in":[]}`<br />`action{"did":"clean-start-clean","siid":18,"aiid":1,"in":[]}`<br />`action{"did":"clean-stop-clean","siid":18,"aiid":2,"in":[]}`<br />`action{"did":"remote-start-remote","siid":21,"aiid":1,"in":[1.0, 2.0]}`<br />`action{"did":"remote-stop-remote","siid":21,"aiid":2,"in":[]}`<br />`action{"did":"remote-exit-remote","siid":21,"aiid":3,"in":[]}`<br />`action{"did":"map-map-req","siid":23,"aiid":1,"in":[2.0]}`<br />`action{"did":"audio-position","siid":24,"aiid":1,"in":[]}`<br />`action{"did":"audio-set-voice","siid":24,"aiid":2,"in":[]}`<br />`action{"did":"audio-play-sound","siid":24,"aiid":3,"in":[]}`<br />Please test and feedback if they are working to they can be linked to a channel.<br />Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Dreame Robot Vacuum-Mop F9   | miio:basic       | [dreame.vacuum.p2008](#dreame-vacuum-p2008) | Yes       | Identified manual actions for execution<br />`action{"did":"vacuum-start-sweep","siid":2,"aiid":1,"in":[]}`<br />`action{"did":"vacuum-stop-sweeping","siid":2,"aiid":2,"in":[]}`<br />`action{"did":"battery-start-charge","siid":3,"aiid":1,"in":[]}`<br />`action{"did":"brush-cleaner-reset-brush-life","siid":9,"aiid":1,"in":[]}`<br />`action{"did":"brush-cleaner-reset-brush-life","siid":10,"aiid":1,"in":[]}`<br />`action{"did":"filter-reset-filter-life","siid":11,"aiid":1,"in":[]}`<br />`action{"did":"vacuum-extend-start-clean","siid":4,"aiid":1,"in":[10.0]}`<br />`action{"did":"vacuum-extend-stop-clean","siid":4,"aiid":2,"in":[]}`<br />`action{"did":"map-map-req","siid":6,"aiid":1,"in":[2.0]}`<br />`action{"did":"map-update-map","siid":6,"aiid":2,"in":[4.0]}`<br />`action{"did":"audio-position","siid":7,"aiid":1,"in":[]}`<br />`action{"did":"audio-play-sound","siid":7,"aiid":2,"in":[]}`<br />`action{"did":"time-delete-timer","siid":8,"aiid":1,"in":[3.0]}`<br />Please test and feedback if they are working to they can be linked to a channel.<br />Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Air Purifier 2S           | miio:basic       | [zhimi.airpurifier.mb1](#zhimi-airpurifier-mb1) | Yes       |            |
| Mi Air Purifier 2S           | miio:basic       | [zhimi.airpurifier.mc1](#zhimi-airpurifier-mc1) | Yes       |            |
| Mi Air Purifier 2H           | miio:basic       | [zhimi.airpurifier.mc2](#zhimi-airpurifier-mc2) | Yes       |            |
| Mi Air Purifier virtual      | miio:unsupported | zhimi.airpurifier.virtual | No        |            |
| Mi Air Purifier 2(Virtual)   | miio:unsupported | zhimi.airpurifier.vtl_m1 | No        |            |
| Mi Universal Remote          | miio:unsupported | chuangmi.ir.v2         | No        |            |
| Mi Remote                    | miio:unsupported | chuangmi.remote.v2     | No        |            |
| Mi IH Rice Cooker            | miio:unsupported | chunmi.cooker.normal1  | No        |            |
| Mi IH Rice Cooker            | miio:unsupported | chunmi.cooker.normal2  | No        |            |
| MiJia Rice Cooker            | miio:unsupported | hunmi.cooker.normal3   | No        |            |
| Mi IH Rice Cooker 4L         | miio:unsupported | chunmi.cooker.normal4  | No        |            |
| Mi IH Pressure Rice Cooker   | miio:unsupported | chunmi.cooker.press1   | No        |            |
| Mi IH Pressure Rice Cooker   | miio:unsupported | chunmi.cooker.press2   | No        |            |
| Mi Smart Fan                 | miio:basic       | [zhimi.fan.v1](#zhimi-fan-v1) | Yes       |            |
| Smartmi DC Pedestal Fan      | miio:basic       | [zhimi.fan.v2](#zhimi-fan-v2) | Yes       |            |
| Smartmi DC Pedestal Fan      | miio:basic       | [zhimi.fan.v3](#zhimi-fan-v3) | Yes       |            |
| Mi Standing Fan              | miio:basic       | [zhimi.fan.sa1](#zhimi-fan-sa1) | Yes       |            |
| Smartmi Inverter Pedestal Fan | miio:basic       | [zhimi.fan.za1](#zhimi-fan-za1) | Yes       |            |
| Smartmi Standing Fan 2       | miio:basic       | [zhimi.fan.za3](#zhimi-fan-za3) | Yes       |            |
| Smartmi Standing Fan 2S      | miio:basic       | [zhimi.fan.za4](#zhimi-fan-za4) | Yes       |            |
| Mi Smart Standing Fan 2 Lite | miio:basic       | [dmaker.fan.1c](#dmaker-fan-1c) | Yes       |            |
| Mi Smart Standing Fan 1X     | miio:basic       | [dmaker.fan.p5](#dmaker-fan-p5) | Yes       |            |
| Mi Smart Standing Fan 1C     | miio:basic       | [dmaker.fan.p8](#dmaker-fan-p8) | Yes       |            |
| Mi Smart Tower Fan           | miio:basic       | [dmaker.fan.p9](#dmaker-fan-p9) | Yes       |            |
| Mi Smart Standing Fan 2      | miio:basic       | [dmaker.fan.p10](#dmaker-fan-p10) | Yes       |            |
| Viomi Internet refrigerator iLive(French style 462L) | miio:unsupported | viomi.fridge.v3        | No        |            |
| Mi smart Home Gateway Hub v1 | miio:basic       | [lumi.gateway.v1](#lumi-gateway-v1) | Yes       | Used to control the gateway itself. Use the mihome binding to control devices connected to the Xiaomi gateway.<br />Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi smart Home GatewayHub v2  | miio:basic       | [lumi.gateway.v2](#lumi-gateway-v2) | Yes       | Used to control the gateway itself. Use the mihome binding to control devices connected to the Xiaomi gateway.<br />Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi mart Home Gateway Hub v3  | miio:basic       | [lumi.gateway.v3](#lumi-gateway-v3) | Yes       | Used to control the gateway itself. Use the mihome binding to control devices connected to the Xiaomi gateway.<br />Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Air Purifier virtual      | miio:basic       | [lumi.gateway.mgl03](#lumi-gateway-mgl03) | Yes       | Used to control the gateway itself. Use the mihome binding to control devices connected to the Xiaomi gateway.<br />Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO ARIES For Bedroom     | miio:basic       | [huayi.light.ari013](#huayi-light-ari013) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO ARIES For Living Room | miio:basic       | [huayi.light.aries](#huayi-light-aries) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO Fan Light             | miio:basic       | [huayi.light.fanwy](#huayi-light-fanwy) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO Fan Light(2020)       | miio:basic       | [huayi.light.fanwy2](#huayi-light-fanwy2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO PEGASUS For Living Room | miio:basic       | [huayi.light.peg091](#huayi-light-peg091) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO PEGASUS For Bedroom   | miio:basic       | [huayi.light.peg093](#huayi-light-peg093) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO PISCES For Bedroom    | miio:basic       | [huayi.light.pis123](#huayi-light-pis123) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO PISCES For Living Room | miio:basic       | [huayi.light.pisces](#huayi-light-pisces) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO TAURUS For Bedroom    | miio:basic       | [huayi.light.tau023](#huayi-light-tau023) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO TAURUS For Living Room | miio:basic       | [huayi.light.taurus](#huayi-light-taurus) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO VIRGO For Bedroom     | miio:basic       | [huayi.light.vir063](#huayi-light-vir063) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO VIRGO For Living Room | miio:basic       | [huayi.light.virgo](#huayi-light-virgo) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO Ceiling Light         | miio:basic       | [huayi.light.wy](#huayi-light-wy) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO LIANGCHEN(BLE Mesh)   | miio:basic       | [huayi.light.wy200](#huayi-light-wy200) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO SAG Downlight (BLE Mesh) | miio:basic       | [huayi.light.wy201](#huayi-light-wy201) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO Bulb (BLE Mesh)       | miio:basic       | [huayi.light.wy202](#huayi-light-wy202) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO YONG Downlight (BLE Mesh) | miio:basic       | [huayi.light.wy203](#huayi-light-wy203) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| huayi.light.wy204            | miio:basic       | [huayi.light.wy204](#huayi-light-wy204) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO Heating Lamp          | miio:basic       | [huayi.light.wyheat](#huayi-light-wyheat) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| HUIZUO ZIWEI Ceiling Lamp    | miio:basic       | [huayi.light.zw131](#huayi-light-zw131) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Aqara Wall Switch(No Neutral, Single Rocker) | miio:unsupported | lumi.ctrl_neutral1.v1  | No        |            |
| Aqara Wall Switch (No Neutral, Double Rocker) | miio:unsupported | lumi.ctrl_neutral2.v1  | No        |            |
| Xiaomiyoupin Curtain Controller (Wi-Fi) | miio:basic       | [lumi.curtain.hagl05](#lumi-curtain-hagl05) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| MR.BOND                      | miio:basic       | [mrbond.airer.m1pro](#mrbond-airer-m1pro) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| MR.BOND                      | miio:basic       | [mrbond.airer.m1s](#mrbond-airer-m1s) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| MR.BOND                      | miio:basic       | [mrbond.airer.m1super](#mrbond-airer-m1super) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Robot Vacuum-Mop Essential | miio:basic       | [mijia.vacuum.v2](#mijia-vacuum-v2) | Yes       | This device may be overwhelmed if refresh is too frequent, slowing down the responses. Suggest to increase refresh time to 120 seconds |
| SCISHARE Smart Capsule Coffee Machine | miio:basic       | [scishare.coffee.s1102](#scishare-coffee-s1102) | Yes       | This device does not allow for regular querying for the status. Hence the status is not updated for the action channels.<br />Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Xiaomi Scishare smart capsule coffee machine | miio:basic       | [scishare.coffee.s1301](#scishare-coffee-s1301) | Yes       | This device does not allow for regular querying for the status. Hence the status is not updated for the action channels.<br />Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips EyeCare connected desk lamp gen2. | miio:basic       | [philips.light.sread1](#philips-light-sread1) | Yes       |            |
| Mijia Philips Desk Lamp 2S   | miio:basic       | [philips.light.sread2](#philips-light-sread2) | Yes       |            |
| Philips connected ceiling    | miio:basic       | [philips.light.ceiling](#philips-light-ceiling) | Yes       |            |
| Philips ZhiYi ceiling lamp   | miio:basic       | [philips.light.zyceiling](#philips-light-zyceiling) | Yes       |            |
| Philips ZhiRui E27 bulb      | miio:basic       | [philips.light.bulb](#philips-light-bulb) | Yes       |            |
| Philips Wi-Fi bulb E27 White | miio:basic       | [philips.light.hbulb](#philips-light-hbulb) | Yes       |            |
| Philips ZhiRui E14 candle lamp Frosted version | miio:basic       | [philips.light.candle](#philips-light-candle) | Yes       |            |
| Philips ZhiRui downlight     | miio:basic       | [philips.light.downlight](#philips-light-downlight) | Yes       |            |
| Philips ZhiRui bedside lamp  | miio:basic       | [philips.light.moonlight](#philips-light-moonlight) | Yes       |            |
| Philips Zhirui Ceiling Lamp Bedroom 40W | miio:basic       | [philips.light.bceiling1](#philips-light-bceiling1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Zhirui Ceiling Lamp Bedroom 28W | miio:basic       | [philips.light.bceiling2](#philips-light-bceiling2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mijia Philips Color Bulb     | miio:basic       | [philips.light.cbulb](#philips-light-cbulb) | Yes       |            |
| Philips Light                | miio:basic       | [philips.light.cbulbs](#philips-light-cbulbs) | Yes       |            |
| Philips Light                | miio:basic       | [philips.light.dcolor](#philips-light-dcolor) | Yes       |            |
| Mijia Philips Study Desk Lamp | miio:basic       | [philips.light.rwread](#philips-light-rwread) | Yes       |            |
| Philips ZhiYi Ceiling lamp FL 40W | miio:basic       | [philips.light.lnblight1](#philips-light-lnblight1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips ZhiYi Ceiling lamp FL 28W | miio:basic       | [philips.light.lnblight2](#philips-light-lnblight2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips ZhiYi Ceiling lamp FL 80W | miio:basic       | [philips.light.lnlrlight](#philips-light-lnlrlight) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips Zhirui Ceiling Lamp Living room 80W | miio:basic       | [philips.light.lrceiling](#philips-light-lrceiling) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips ZhiRui E14 candle lamp crystal version | miio:basic       | [philips.light.candle2](#philips-light-candle2) | Yes       |            |
| Philips Smart Lamp           | miio:basic       | [philips.light.mono1](#philips-light-mono1) | Yes       |            |
| ZhiRui dimmable downlight    | miio:basic       | [philips.light.dlight](#philips-light-dlight) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Nordic 80W | miio:basic       | [philips.light.mceil](#philips-light-mceil) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Nordic 40W | miio:basic       | [philips.light.mceilm](#philips-light-mceilm) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Nordic 28W | miio:basic       | [philips.light.mceils](#philips-light-mceils) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Black 80W | miio:basic       | [philips.light.obceil](#philips-light-obceil) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
|  Zhirui Ceiling Lamp Black 40W | miio:basic       | [philips.light.obceim](#philips-light-obceim) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Black 28W | miio:basic       | [philips.light.obceis](#philips-light-obceis) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Starry 80W | miio:basic       | [philips.light.sceil](#philips-light-sceil) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Starry 40W | miio:basic       | [philips.light.sceilm](#philips-light-sceilm) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Starry 28W | miio:basic       | [philips.light.sceils](#philips-light-sceils) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Gorgeous 80W | miio:basic       | [philips.light.xzceil](#philips-light-xzceil) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Gorgeous 40W | miio:basic       | [philips.light.xzceim](#philips-light-xzceim) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Zhirui Ceiling Lamp Gorgeous 28W | miio:basic       | [philips.light.xzceis](#philips-light-xzceis) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Philips connected lights     | miio:basic       | [philips.light.virtual](#philips-light-virtual) | Yes       |            |
| Philips ZhiYi desk lamp      | miio:basic       | [philips.light.zysread](#philips-light-zysread) | Yes       |            |
| Philips ZhiYi strip          | miio:basic       | [philips.light.zystrip](#philips-light-zystrip) | Yes       |            |
| Mi Plug Mini                 | miio:basic       | [chuangmi.plug.m1](#chuangmi-plug-m1) | Yes       |            |
| Mi Smart Power Plug          | miio:basic       | [chuangmi.plug.v1](#chuangmi-plug-v1) | Yes       |            |
| Mi Smart Power Plug v2       | miio:basic       | [chuangmi.plug.v2](#chuangmi-plug-v2) | Yes       |            |
| MIJIA Smart  Plug Enhanced   | miio:basic       | [chuangmi.plug.v3](#chuangmi-plug-v3) | Yes       |            |
| Mi Smart Plug (Wi-Fi) Basic  | miio:basic       | [chuangmi.plug.m3](#chuangmi-plug-m3) | Yes       |            |
| Mi Smart Plug WiFi           | miio:basic       | [chuangmi.plug.hmi205](#chuangmi-plug-hmi205) | Yes       |            |
| Mi Smart Plug (WiFi)         | miio:basic       | [chuangmi.plug.hmi206](#chuangmi-plug-hmi206) | Yes       |            |
| Mi Smart Wi-Fi Plug (Bluetooth Gateway) | miio:basic       | [chuangmi.plug.hmi208](#chuangmi-plug-hmi208) | Yes       |            |
| CHINGMI Smart Power Strip v1 | miio:basic       | [qmi.powerstrip.v1](#qmi-powerstrip-v1) | Yes       |            |
| Mi Smart Power Strip         | miio:basic       | [zimi.powerstrip.v2](#zimi-powerstrip-v2) | Yes       |            |
| Soocare Electric Toothbrush  | miio:unsupported | soocare.toothbrush.x3  | No        |            |
| Mi Robot Vacuum              | miio:vacuum      | [rockrobo.vacuum.v1](#rockrobo-vacuum-v1) | Yes       |            |
| Xiaowa C1                    | miio:vacuum      | [roborock.vacuum.c1](#roborock-vacuum-c1) | Yes       |            |
| Roborock S6 Pure             | miio:vacuum      | [roborock.vacuum.a08](#roborock-vacuum-a08) | Yes       |            |
| Roborock T7 Pro              | miio:vacuum      | [roborock.vacuum.a09](#roborock-vacuum-a09) | Yes       |            |
| Roborock S6 MaxV             | miio:vacuum      | [roborock.vacuum.a10](#roborock-vacuum-a10) | Yes       |            |
| Roborock T7                  | miio:vacuum      | [roborock.vacuum.a11](#roborock-vacuum-a11) | Yes       |            |
| Roborock P5                  | miio:vacuum      | [roborock.vacuum.p5](#roborock-vacuum-p5) | Yes       |            |
| Roborock S5                  | miio:vacuum      | [roborock.vacuum.s5](#roborock-vacuum-s5) | Yes       |            |
| Mi Robot Vacuum 1S           | miio:vacuum      | [roborock.vacuum.m1s](#roborock-vacuum-m1s) | Yes       |            |
| Roborock S4                  | miio:vacuum      | [roborock.vacuum.s4](#roborock-vacuum-s4) | Yes       |            |
| Roborock Vacuum S4v2         | miio:vacuum      | [roborock.vacuum.s4v2](#roborock-vacuum-s4v2) | Yes       |            |
| Roborock T6                  | miio:vacuum      | [roborock.vacuum.t6](#roborock-vacuum-t6) | Yes       |            |
| Roborock Vacuum T6 v2        | miio:vacuum      | [roborock.vacuum.t6v2](#roborock-vacuum-t6v2) | Yes       |            |
| Roborock Vacuum T6 v3        | miio:vacuum      | [roborock.vacuum.t6v3](#roborock-vacuum-t6v3) | Yes       |            |
| Roborock T4                  | miio:vacuum      | [roborock.vacuum.t4](#roborock-vacuum-t4) | Yes       |            |
| Roborock Vacuum T4 v2        | miio:vacuum      | [roborock.vacuum.t4v2](#roborock-vacuum-t4v2) | Yes       |            |
| Roborock Vacuum T4 v3        | miio:vacuum      | [roborock.vacuum.t4v3](#roborock-vacuum-t4v3) | Yes       |            |
| Roborock Vacuum T7           | miio:vacuum      | [roborock.vacuum.t7](#roborock-vacuum-t7) | Yes       |            |
| Roborock Vacuum T7 v2        | miio:vacuum      | [roborock.vacuum.t7v2](#roborock-vacuum-t7v2) | Yes       |            |
| Roborock Vacuum T7 v3        | miio:vacuum      | [roborock.vacuum.t7v3](#roborock-vacuum-t7v3) | Yes       |            |
| Roborock Vacuum T7p          | miio:vacuum      | [roborock.vacuum.t7p](#roborock-vacuum-t7p) | Yes       |            |
| Roborock Vacuum T7 v2        | miio:vacuum      | [roborock.vacuum.t7pv2](#roborock-vacuum-t7pv2) | Yes       |            |
| Roborock Vacuum T7 v3        | miio:vacuum      | [roborock.vacuum.t7pv3](#roborock-vacuum-t7pv3) | Yes       |            |
| Roborock S5 Max              | miio:vacuum      | [roborock.vacuum.s5e](#roborock-vacuum-s5e) | Yes       |            |
| Roborock Vacuum S6           | miio:vacuum      | [rockrobo.vacuum.s6](#rockrobo-vacuum-s6) | Yes       |            |
| Roborock S6                  | miio:vacuum      | [roborock.vacuum.s6](#roborock-vacuum-s6) | Yes       |            |
| Roborock Xiaowa E Series Vacuum v2 | miio:unsupported | roborock.vacuum.e2     | No        |            |
| Viomi Cleaning Robot V-RVCLM21B | miio:basic       | [viomi.vacuum.v6](#viomi-vacuum-v6) | Yes       |            |
| Mi Robot Vacuum-Mop P        | miio:basic       | [viomi.vacuum.v7](#viomi-vacuum-v7) | Yes       |            |
| Mi Robot Vacuum-Mop P        | miio:basic       | [viomi.vacuum.v8](#viomi-vacuum-v8) | Yes       |            |
| Rockrobo Xiaowa Sweeper v2   | miio:unsupported | roborock.sweeper.e2v2  | No        |            |
| Rockrobo Xiaowa Sweeper v3   | miio:unsupported | roborock.sweeper.e2v3  | No        |            |
| PTX OneKey Switch (WIFI)     | miio:basic       | [090615.switch.xswitch01](#090615-switch-xswitch01) | Yes       |            |
| PTX Twokey switch(wifi)      | miio:basic       | [090615.switch.xswitch02](#090615-switch-xswitch02) | Yes       |            |
| PTX ThreeKey Switch (WIFI)   | miio:basic       | [090615.switch.xswitch03](#090615-switch-xswitch03) | Yes       |            |
| Mi Water Purifier v1         | miio:basic       | [yunmi.waterpurifier.v1](#yunmi-waterpurifier-v1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier v2         | miio:basic       | [yunmi.waterpurifier.v2](#yunmi-waterpurifier-v2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier (Under sink) v3 | miio:basic       | [yunmi.waterpurifier.v3](#yunmi-waterpurifier-v3) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier v4         | miio:basic       | [yunmi.waterpurifier.v4](#yunmi-waterpurifier-v4) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx2        | miio:basic       | [yunmi.waterpuri.lx2](#yunmi-waterpuri-lx2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier (Under Counter) | miio:basic       | [yunmi.waterpuri.lx3](#yunmi-waterpuri-lx3) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx4        | miio:basic       | [yunmi.waterpuri.lx4](#yunmi-waterpuri-lx4) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier 1A/400G Pro | miio:basic       | [yunmi.waterpuri.lx5](#yunmi-waterpuri-lx5) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier (Under Counter) | miio:basic       | [yunmi.waterpuri.lx6](#yunmi-waterpuri-lx6) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier 500G/500G Pro | miio:basic       | [yunmi.waterpuri.lx7](#yunmi-waterpuri-lx7) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier 600G       | miio:basic       | [yunmi.waterpuri.lx8](#yunmi-waterpuri-lx8) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier D1         | miio:basic       | [yunmi.waterpuri.lx9](#yunmi-waterpuri-lx9) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier lx10       | miio:basic       | [yunmi.waterpuri.lx10](#yunmi-waterpuri-lx10) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier C1 (Triple Setting) | miio:basic       | [yunmi.waterpuri.lx11](#yunmi-waterpuri-lx11) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Water Purifier S1         | miio:basic       | [yunmi.waterpuri.lx12](#yunmi-waterpuri-lx12) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Inverter Air Conditioner (1.5HP) | miio:basic       | [xiaomi.aircondition.ma1](#xiaomi-aircondition-ma1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Inverter Air Conditioner (1.5HP, China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.ma2](#xiaomi-aircondition-ma2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Vertical Air Conditioner (2HP) | miio:basic       | [xiaomi.aircondition.ma4](#xiaomi-aircondition-ma4) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Vertical Air Conditioner C1 (2HP / Inverter / China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.ma5](#xiaomi-aircondition-ma5) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner C1 (1.5HP / Conventional / China Energy Label Level 3) | miio:basic       | [xiaomi.aircondition.ma6](#xiaomi-aircondition-ma6) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner C1 (1HP / Inverter / China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.ma9](#xiaomi-aircondition-ma9) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Ultra Electricity Saving Vertical Air Conditioner (2HP/Inverter/New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.c10](#xiaomi-aircondition-c10) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Ultra Electricity Saving Vertical Air Conditioner (3HP/Inverter/New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.c11](#xiaomi-aircondition-c11) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner A (1HP / Inverter / China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mc1](#xiaomi-aircondition-mc1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner A (1.5HP / Inverter / China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mc2](#xiaomi-aircondition-mc2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner A (1HP / Inverter / China Energy Label Level <1) | miio:basic       | [xiaomi.aircondition.mc4](#xiaomi-aircondition-mc4) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner A (1.5HP / Inverter / China Energy Label Level <1) | miio:basic       | [xiaomi.aircondition.mc5](#xiaomi-aircondition-mc5) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Vertical Air Conditioner A (2HP / Inverter / China Energy Label Level <1) | miio:basic       | [xiaomi.aircondition.mc6](#xiaomi-aircondition-mc6) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Vertical Air Conditioner A (3HP / Inverter / China Energy Label Level <1) | miio:basic       | [xiaomi.aircondition.mc7](#xiaomi-aircondition-mc7) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Ultra Electricity Saving Air Conditioner(1.5HP/Inverter/New China Energy Label Level 3) | miio:basic       | [xiaomi.aircondition.mc8](#xiaomi-aircondition-mc8) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Ultra Electricity Saving Vertical Air Conditioner(2HP/Inverter/New China Energy Label Level 3) | miio:basic       | [xiaomi.aircondition.mc9](#xiaomi-aircondition-mc9) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner C (1HP / Inverter / New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mh1](#xiaomi-aircondition-mh1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner C (1.5HP / Inverter / New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mh2](#xiaomi-aircondition-mh2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Ultra Electricity Saving Air Conditioner(1HP/Inverter/New China Energy Label Level 3) | miio:basic       | [xiaomi.aircondition.mh3](#xiaomi-aircondition-mh3) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner X (1HP / Inverter / New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mt1](#xiaomi-aircondition-mt1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Air Conditioner X (1.5HP / Inverter / New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mt2](#xiaomi-aircondition-mt2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Gentle Breeze Air Conditioner (1HP / Inverter / New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mt3](#xiaomi-aircondition-mt3) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Gentle Breeze Air Conditioner (1.5HP / Inverter / New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mt4](#xiaomi-aircondition-mt4) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Gentle Breeze Vertical Air Conditioner (3HP / Inverter / New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mt5](#xiaomi-aircondition-mt5) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Ultra Electricity Saving Air Conditioner (1HP/Inverter/New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mt7](#xiaomi-aircondition-mt7) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Smart Ultra Electricity Saving Air Conditioner (1.5HP/Inverter/New China Energy Label Level 1) | miio:basic       | [xiaomi.aircondition.mt8](#xiaomi-aircondition-mt8) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Wi-Fi Repeater 2          | miio:unsupported | xiaomi.repeater.v2     | No        |            |
| Mi Network Speaker           | miio:unsupported | xiaomi.wifispeaker.v1  | No        |            |
| Uclean Smart Toilet Seat     | miio:basic       | [xjx.toilet.pro](#xjx-toilet-pro) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Uclean smart toilet relax    | miio:basic       | [xjx.toilet.relax](#xjx-toilet-relax) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Uclean smart toilet pure     | miio:basic       | [xjx.toilet.pure](#xjx-toilet-pure) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Whale Spout Smart Toilet Zero | miio:basic       | [xjx.toilet.zero](#xjx-toilet-zero) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Mi Bedside Lamp              | miio:basic       | [yeelink.light.bslamp1](#yeelink-light-bslamp1) | Yes       |            |
| Mi Bedside Lamp 2            | miio:basic       | [yeelink.light.bslamp2](#yeelink-light-bslamp2) | Yes       |            |
| Yeelight Bedside Lamp II     | miio:basic       | [yeelink.light.bslamp3](#yeelink-light-bslamp3) | Yes       |            |
| Yeelight Smart Bath Heater Pro | miio:basic       | [yeelink.bhf_light.v1](#yeelink-bhf_light-v1) | Yes       |            |
| Yeelight Smart Bath Heater   | miio:basic       | [yeelink.bhf_light.v2](#yeelink-bhf_light-v2) | Yes       |            |
| Yeelight Ceiling Light       | miio:basic       | [yeelink.light.ceiling1](#yeelink-light-ceiling1) | Yes       |            |
| Yeelight Ceiling Light SE    | miio:basic       | [yeelink.light.ceiling2](#yeelink-light-ceiling2) | Yes       |            |
| Yeelight LED Ceiling Light   | miio:basic       | [yeelink.light.ceiling3](#yeelink-light-ceiling3) | Yes       |            |
| Yeelight LED Ceiling Light   | miio:basic       | [yeelink.light.ceiling4](#yeelink-light-ceiling4) | Yes       |            |
| Yeelight LED Ceiling Ambi Lamp | miio:basic       | [yeelink.light.ceiling4.ambi](#yeelink-light-ceiling4-ambi) | Yes       |            |
| Mi LED Ceiling Light         | miio:basic       | [yeelink.light.ceiling5](#yeelink-light-ceiling5) | Yes       |            |
| Yeelight HaoShi LED Ceiling Lamp Pro | miio:basic       | [yeelink.light.ceiling6](#yeelink-light-ceiling6) | Yes       |            |
| Yeelight Haoshi Ceiling Lamp | miio:basic       | [yeelink.light.ceiling7](#yeelink-light-ceiling7) | Yes       |            |
| LED Ceiling Light Crystal Plus | miio:basic       | [yeelink.light.ceiling8](#yeelink-light-ceiling8) | Yes       |            |
| Yeelight HaoShi LED Ceiling Lamp Pro | miio:basic       | [yeelink.light.ceiling9](#yeelink-light-ceiling9) | Yes       |            |
| Yeelight Crystal Pendant Lamp | miio:basic       | [yeelink.light.ceiling10](#yeelink-light-ceiling10) | Yes       |            |
| Yeelight Ceiling Light 320 1S | miio:basic       | [yeelink.light.ceiling11](#yeelink-light-ceiling11) | Yes       |            |
| Yeelight stylized Ceiling Light  Pro | miio:basic       | [yeelink.light.ceiling12](#yeelink-light-ceiling12) | Yes       |            |
| Yeelight Ceiling Light       | miio:basic       | [yeelink.light.ceiling13](#yeelink-light-ceiling13) | Yes       |            |
| Yeelight Ceiling Light Mini  | miio:basic       | [yeelink.light.ceiling14](#yeelink-light-ceiling14) | Yes       |            |
| Yeelight Ceiling Light 480 1S | miio:basic       | [yeelink.light.ceiling15](#yeelink-light-ceiling15) | Yes       |            |
| Yeelight Xingyu Ceiling Light | miio:basic       | [yeelink.light.ceiling16](#yeelink-light-ceiling16) | Yes       |            |
| Yeelight ShaoHua Celing Light | miio:basic       | [yeelink.light.ceiling17](#yeelink-light-ceiling17) | Yes       |            |
| Yeelight Ceiling Light Pro   | miio:basic       | [yeelink.light.ceiling18](#yeelink-light-ceiling18) | Yes       |            |
| Yeelight Ceiling Light Pro   | miio:basic       | [yeelink.light.ceiling19](#yeelink-light-ceiling19) | Yes       |            |
| Yeelight Ceiling Light       | miio:basic       | [yeelink.light.ceiling20](#yeelink-light-ceiling20) | Yes       |            |
| Mi Smart LED Living Room Ceiling Light | miio:basic       | [yeelink.light.ceiling21](#yeelink-light-ceiling21) | Yes       |            |
| Mi Smart LED Ceiling Light   | miio:basic       | [yeelink.light.ceiling22](#yeelink-light-ceiling22) | Yes       |            |
| Mi Smart LED Ceiling Light (350mm) | miio:basic       | [yeelink.light.ceiling23](#yeelink-light-ceiling23) | Yes       |            |
| Yeelight LED Ceiling Ambi Lamp | miio:basic       | [yeelink.light.ceiling10.ambi](#yeelink-light-ceiling10-ambi) | Yes       |            |
| Yeelight LED Ceiling Ambi Lamp | miio:basic       | [yeelink.light.ceiling19.ambi](#yeelink-light-ceiling19-ambi) | Yes       |            |
| Yeelight LED Ceiling Ambi Lamp | miio:basic       | [yeelink.light.ceiling20.ambi](#yeelink-light-ceiling20-ambi) | Yes       |            |
| Yeelight LED Bulb (Tunable)  | miio:basic       | [yeelink.light.ct2](#yeelink-light-ct2) | Yes       |            |
| Yeelight Bulb                | miio:basic       | [yeelink.light.mono1](#yeelink-light-mono1) | Yes       |            |
| Yeelight White Bulb v2       | miio:basic       | [yeelink.light.mono2](#yeelink-light-mono2) | Yes       |            |
| Yeelight LED Filament Bulb   | miio:basic       | [yeelink.light.mono5](#yeelink-light-mono5) | Yes       |            |
| Yeelight Smart Speaker       | miio:unsupported | yeelink.wifispeaker.v1 | No        |            |
| Mi LED Desk Lamp             | miio:basic       | [yeelink.light.lamp1](#yeelink-light-lamp1) | Yes       |            |
| Mi Smart LED Desk Lamp Pro   | miio:basic       | [yeelink.light.lamp2](#yeelink-light-lamp2) | Yes       |            |
| Yeelight LED Lamp            | miio:basic       | [yeelink.light.lamp3](#yeelink-light-lamp3) | Yes       |            |
| Mi LED Desk Lamp 1S          | miio:basic       | [yeelink.light.lamp4](#yeelink-light-lamp4) | Yes       |            |
| Yeelight Smart Desk Lamp Prime | miio:basic       | [yeelink.light.lamp5](#yeelink-light-lamp5) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp6](#yeelink-light-lamp6) | Yes       |            |
| Yeelight LED Light Sensor Desk Lamp V1 | miio:basic       | [yeelink.light.lamp7](#yeelink-light-lamp7) | Yes       |            |
| Yeelight                     | miio:basic       | [yeelink.light.lamp8](#yeelink-light-lamp8) | Yes       |            |
| Yeelight Whiteglow Panel Light | miio:basic       | [yeelink.light.panel1](#yeelink-light-panel1) | Yes       |            |
| Yeelight Lightstrip          | miio:basic       | [yeelink.light.strip1](#yeelink-light-strip1) | Yes       |            |
| Yeelight Lightstrip Plus     | miio:basic       | [yeelink.light.strip2](#yeelink-light-strip2) | Yes       |            |
| Yeelight Willow LED Lightstrip | miio:basic       | [yeelink.light.strip4](#yeelink-light-strip4) | Yes       |            |
| Light Group (Mi & Yeelight)  | miio:basic       | [yeelink.light.virtual](#yeelink-light-virtual) | Yes       |            |
| Yeelight Color Bulb          | miio:basic       | [yeelink.light.color1](#yeelink-light-color1) | Yes       |            |
| Yeelight LED Bulb (Color)    | miio:basic       | [yeelink.light.color2](#yeelink-light-color2) | Yes       |            |
| Mi LED Smart Bulb (White and Color) | miio:basic       | [yeelink.light.color3](#yeelink-light-color3) | Yes       |            |
| Yeelight LED Bulb 1S（Color）  | miio:basic       | [yeelink.light.color4](#yeelink-light-color4) | Yes       |            |
| Yeelight Smart Dual Control Module | miio:basic       | [yeelink.switch.sw1](#yeelink-switch-sw1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Yilai Ceiling Light Aiyue 480 | miio:basic       | [yilai.light.ceiling1](#yilai-light-ceiling1) | Yes       |            |
| Yilai Ceiling Lamp Hefeng 430 | miio:basic       | [yilai.light.ceiling2](#yilai-light-ceiling2) | Yes       |            |
| Yilai Ceiling Lamp Hefeng Pro | miio:basic       | [yilai.light.ceiling3](#yilai-light-ceiling3) | Yes       |            |
| Smartmi Radiant Heater Smart Version | miio:basic       | [zhimi.heater.za1](#zhimi-heater-za1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Smartmi Smart Convector Heater 1S | miio:basic       | [zhimi.heater.za2](#zhimi-heater-za2) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |
| Smartmi Smart Convector Heater 1S | miio:basic       | [zhimi.heater.zb1](#zhimi-heater-zb1) | Yes       | Experimental support. Please report back if all channels are functional. Preferably share the debug log of property refresh and command responses |


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

## Supported property test for unsupported devices

The unsupported device has a test channel with switch. When switching on, all known properties are tested, this may take few minutes.
A test report will be shown in the log and is saved in the `userdata/miio` folder with a filename `test-[your model]-[timestamp].txt`.
If supported properties are found, an experimental database file is saved to the conf/misc/miio folder (see below chapter).
The thing will go offline and will come back online as basic device, supporting the found channels.
The database file may need to be modified to display the right channel names.
After validation, please share the logfile and json files on the openHAB forum or the openHAB GitHub to build future support for this model.

## Advanced: adding local database files to support new devices

Things using the basic handler (miio:basic things) are driven by json 'database' files.
This instructs the binding which channels to create, which properties and actions are associated with the channels etc.
The conf/misc/miio (e.g. in Linux `/opt/openhab/conf/misc/miio/`) is scanned for database files and will be used for your devices. 
During the start of the binding the exact path used in your system will be printed in the debug log. 
Watch for a line containing `Started miio basic devices local databases watch service. Watching for database files at path: …`
If this folder is created after the start of the binding, you may need to restart the binding (or openHAB) to be able to use the local files. 
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
Set the communication in the thing configuration to 'cloud'.

_Cloud connectivity is not working_
The most common problem is a wrong userId/password. Try to fix your userId/password.
If it still fails, you're bit out of luck. You may try to restart OpenHAB (not just the binding) to clean the cookies. 
As the cloud logon process is still little understood, your only luck might be to enable trace logging and see if you can translate the Chinese error code that it returns.

_My Roborock vacuum is not found or not reacting_
Did you link the vacuum with the Roborock app? 
This won't work, the Roborock app is using a different communication method. 
Reset your vacuum and connect it to the Xiaomi MiHome app. 
This will change the communication method and the Mi IO binding can communicate with the vacuum.


# Channels

Depending on the device, different channels are available.

All devices have available the following channels (marked as advanced) besides the device specific channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| network#ssid     | String  | Network SSID                        |
| network#bssid    | String  | Network BSSID                       |
| network#rssi     | Number  | Network RSSI                        |
| network#life     | Number  | Network Life                        |
| actions#commands | String  | send commands direct. see below     |
| actions#rpc      | String  | send commands via cloud. see below  |

note: the ADVANCED  `actions#commands` and `actions#rpc` channels can be used to send commands that are not automated via the binding. This is available for all devices
e.g. `openhab:send actionCommand 'upd_timer["1498595904821", "on"]'` would enable a pre-configured timer. See https://github.com/marcelrv/XiaomiRobotVacuumProtocol for all known available commands.


### Mi PM2.5 Air Quality Monitor (<a name="zhimi-airmonitor-v1">zhimi.airmonitor.v1</a>) Channels

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

### Mi Multifunction Air Monitor (<a name="cgllc-airmonitor-b1">cgllc.airmonitor.b1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| battery          | Number  | Battery                             |            |
| pm25             | Number  | PM2.5                               |            |
| co2              | Number  | CO2e                                |            |
| tvoc             | Number  | tVOC                                |            |
| humidity         | Number  | Humidity                            |            |
| temperature      | Number:Temperature | Temperature                         |            |

### Qingping Air Monitor (<a name="cgllc-airmonitor-s1">cgllc.airmonitor.s1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| battery          | Number  | Battery                             |            |
| pm25             | Number  | PM2.5                               |            |
| co2              | Number  | CO2                                 |            |
| tvoc             | Number  | tVOC                                |            |
| humidity         | Number  | Humidity                            |            |
| temperature      | Number:Temperature | Temperature                         |            |

### Smartmi Humidifier (<a name="zhimi-humidifier-v1">zhimi.humidifier.v1</a>) Channels

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
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| temperature      | Number:Temperature | Temperature                         |            |
| childlock        | Switch  | Child Lock                          |            |

### Smartmi Evaporative Humidifier (<a name="zhimi-humidifier-ca1">zhimi.humidifier.ca1</a>) Channels

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
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| temperature      | Number:Temperature | Temperature                         |            |
| childlock        | Switch  | Child Lock                          |            |

### Smartmi Evaporative Humidifer 2 (<a name="zhimi-humidifier-ca4">zhimi.humidifier.ca4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| mode             | Number  | Mode                                |            |
| Fault            | Number  | Humidifier Device Fault             |            |
| humidity         | Number  | Humidity                            |            |
| targetHumidity   | Number  | Target Humidity                     |            |
| waterlevel       | Number  | Water Level                         |            |
| bright           | Number  | LED Brightness                      |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| dry              | Switch  | Dry                                 |            |
| usedhours        | Number:Time | Run Time                            |            |
| powerhours       | Number:Time | Power Time                          |            |
| targetmotorspeed | Number  | Target Motor Speed                  |            |
| actualmotorspeed | Number  | Actual Motor Speed                  |            |
| temperature      | Number:Temperature | Temperature                         |            |
| childlock        | Switch  | Child Lock                          |            |
| ButtonPressed    | Number  | Button Pressed                      |            |
| clean            | Switch  | Clean Mode                          |            |
| countryCode      | Number  | Country Code                        |            |

### Smartmi Evaporative Humidifier (<a name="zhimi-humidifier-cb1">zhimi.humidifier.cb1</a>) Channels

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
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| filterhours      | Number:Time | Filter Hours used                   |            |
| motorspeed       | Number  | Motor Speed                         |            |
| motorspeed2      | Number  | Motor Speed 2                       |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier 2 (<a name="zhimi-airpurifier-m2">zhimi.airpurifier.m2</a>) Channels

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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier 2S (<a name="zhimi-airpurifier-ma1">zhimi.airpurifier.ma1</a>) Channels

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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier 2S (<a name="zhimi-airpurifier-ma2">zhimi.airpurifier.ma2</a>) Channels

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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Air Purifier 3/3H (<a name="zhimi-airpurifier-mb3">zhimi.airpurifier.mb3</a>) Channels

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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier MAX / MAX Pro (<a name="zhimi-airpurifier-sa2">zhimi.airpurifier.sa2</a>) Channels

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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Fresh Air Ventilator A1-150 (<a name="dmaker-airfresh-a1">dmaker.airfresh.a1</a>) Channels

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

### Gosund Smart Plug (<a name="cuco-plug-cp1">cuco.plug.cp1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| FirmwareRevision | String  | Device Information-CurrentFirmware Version |            |
| Manufacturer     | String  | Device Information-Device Manufacturer |            |
| Model            | String  | Device Information-Device Model     |            |
| SerialNumber     | String  | Device Information-Device Serial Number |            |
| On               | Switch  | Switch-Switch Status                |            |

### Mi Robot Vacuum Mop 1C STYTJ01ZHM (<a name="dreame-vacuum-mc1808">dreame.vacuum.mc1808</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| vacuumaction     | String  | Vacuum Action                       |            |
| BatteryLevel     | Number  | Battery-Battery Level               |            |
| ChargingState    | Number  | Battery-Charging State              | Value mapping ["1"="Charging","2"="Not Charging","4"="Charging","5"="Go Charging"] |
| Fault            | Number  | Robot Cleaner-Device Fault          | Value mapping ["0"="No faults"] |
| Status           | Number  | Robot Cleaner-Status                | Value mapping ["1"="Sweeping","2"="Idle","3"="Paused","4"="Error","5"="Go Charging","6"="Charging"] |
| BrushLeftTime    | Number:Time | Main Cleaning Brush-Brush Left Time |            |
| BrushLifeLevel   | Number  | Main Cleaning Brush-Brush Life Level |            |
| FilterLifeLevel  | Number  | Filter - Filter Life Level          |            |
| FilterLeftTime   | Number:Time | Filter-Filter Left Time             |            |
| BrushLeftTime1   | Number:Time | Side Cleaning Brush-Brush Left Time |            |
| BrushLifeLevel1  | Number  | Side Cleaning Brush-Brush Life Level |            |
| WorkMode         | Number  | clean-workmode                      |            |
| Area             | String  | clean-area                          |            |
| Timer            | String  | clean-timer                         |            |
| Mode             | Number  | clean-mode                          | Value mapping ["0"="quiet","1"="standard","2"="medium","3"="strong"] |
| TotalCleanTime   | Number:Time | Clean - Total Clean Time            |            |
| TotalCleanTimes  | Number  | Clean - Total Clean Times           |            |
| TotalCleanArea   | Number:Area | Clean - Total Clean Area            |            |
| CleanLogStartTime | Number  | Clean - Clean Log Start Time        |            |
| ButtonLed        | Number  | Clean - Button Led                  |            |
| TaskDone         | Number  | Clean - Task Done                   | Value mapping ["0"="in progress","1"="done"] |
| LifeSieve        | String  | Consumable - Life Sieve             |            |
| LifeBrushSide    | String  | Consumable - Life Brush Side        |            |
| LifeBrushMain    | String  | Consumable - Life Brush Main        |            |
| Enable           | Switch  | Annoy - Enable                      |            |
| StartTime        | String  | Annoy - Start Time                  |            |
| StopTime         | String  | Annoy - Stop Time                   |            |
| MapView          | String  | Map - Map View                      |            |
| Volume           | Number  | Audio - Volume                      |            |
| VoicePackets     | String  | Audio - Voice Packets               |            |
| TimeZone         | String  | Time - Time Zone                    |            |

### Dreame Robot Vacuum-Mop F9 (<a name="dreame-vacuum-p2008">dreame.vacuum.p2008</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| status           | Number  | Robot Cleaner - Status              | Value mapping ["1"="Sweeping","2"="Idle","3"="Paused","4"="Error","5"="Go Charging","6"="Charging"] |
| fault            | Number  | Robot Cleaner - Device Fault        | Value mapping ["0"="No Faults"] |
| battery-level    | Number  | Battery - Battery Level             |            |
| charging-state   | Number  | Battery - Charging State            | Value mapping ["1"="Charging","2"="Not Charging","5"="Go Charging"] |
| brush-left-time  | Number:Time | Main Cleaning Brush - Brush Left Time |            |
| brush-life-level | Number:Dimensionless | Main Cleaning Brush - Brush Life Level |            |
| brush-left-time1 | Number:Time | Side Cleaning Brush - Brush Left Time |            |
| brush-life-level1 | Number:Dimensionless | Side Cleaning Brush - Brush Life Level |            |
| filter-life-level | Number:Dimensionless | Filter - Filter Life Level          |            |
| filter-left-time | Number:Time | Filter - Filter Left Time           |            |
| work-mode        | Number  | Vacuum Extend - Work Mode           |            |
| cleaning-time    | Number:Time | Vacuum Extend - Cleaning Time       |            |
| cleaning-area    | Number:Area | Vacuum Extend - Cleaning Area       |            |
| cleaning-mode    | Number  | Vacuum Extend - Cleaning Mode       | Value mapping ["0"="mode 0","1"="mode 1","2"="mode 2","3"="mode 3"] |
| mop-mode         | Number  | Vacuum Extend - Mop Mode            | Value mapping ["1"="low water","2"="medium water","3"="high water"] |
| waterbox-status  | Number  | Vacuum Extend - Waterbox Status     | Value mapping ["0"="Status 0","1"="Status 1"] |
| task-status      | Number  | Vacuum Extend - Task Status         | Value mapping ["0"="Status 0","1"="Status 1"] |
| break-point-restart | Number  | Vacuum Extend - Break Point Restart | Value mapping ["0"="Off","1"="On"] |
| carpet-press     | Number  | Vacuum Extend - Carpet Press        | Value mapping ["0"="Off","1"="On"] |
| enable           | Switch  | Do Not Disturb - Enable             |            |
| start-time       | String  | Do Not Disturb - Start Time         |            |
| end-time         | String  | Do Not Disturb - End Time           |            |
| volume           | Number  | Audio - Volume                      |            |
| voice-packet-id  | String  | Audio - Voice Packet Id             |            |
| voice-change-state | String  | Audio - Voice Change State          |            |
| time-zone        | String  | Time - Time Zone                    |            |
| timer-clean      | String  | Time - Timer Clean                  |            |
| first-clean-time | Number  | Clean Logs - First Clean Time       |            |
| total-clean-time | Number:Time | Clean Logs - Total Clean Time       |            |
| total-clean-times | Number  | Clean Logs - Total Clean Times      |            |
| total-clean-area | Number  | Clean Logs - Total Clean Area       |            |
| save-map-status  | Number  | Vslam Extend - Save Map Status      | Value mapping ["0"="Off","1"="On","-1"="Not Enabled"] |

### Mi Air Purifier 2S (<a name="zhimi-airpurifier-mb1">zhimi.airpurifier.mb1</a>) Channels

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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Air Purifier 2H (<a name="zhimi-airpurifier-mc2">zhimi.airpurifier.mc2</a>) Channels

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
| filterhours      | Number:Time | Filter Hours used                   |            |
| usedhours        | Number:Time | Run Time                            |            |
| motorspeed       | Number  | Motor Speed                         |            |
| filterlife       | Number  | Filter Life                         |            |
| favoritelevel    | Number  | Favorite Level                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
| purifyvolume     | Number  | Purivied Volume                     |            |
| childlock        | Switch  | Child Lock                          |            |

### Mi Smart Fan (<a name="zhimi-fan-v1">zhimi.fan.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number:Time | Run Time                            |            |
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

### Smartmi DC Pedestal Fan (<a name="zhimi-fan-v2">zhimi.fan.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number:Time | Run Time                            |            |
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

### Smartmi DC Pedestal Fan (<a name="zhimi-fan-v3">zhimi.fan.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number:Time | Run Time                            |            |
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

### Mi Standing Fan (<a name="zhimi-fan-sa1">zhimi.fan.sa1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number:Time | Run Time                            |            |
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

### Smartmi Inverter Pedestal Fan (<a name="zhimi-fan-za1">zhimi.fan.za1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number:Time | Run Time                            |            |
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

### Smartmi Standing Fan 2 (<a name="zhimi-fan-za3">zhimi.fan.za3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number:Time | Run Time                            |            |
| angle            | Number  | Angle                               |            |
| poweroffTime     | Number  | Timer                               |            |
| buzzer           | Number  | Buzzer                              |            |
| led_b            | Number  | LED                                 |            |
| child_lock       | Switch  | Child Lock                          |            |
| speedLevel       | Number  | Speed Level                         |            |
| speed            | Number  | Speed                               |            |
| naturalLevel     | Number  | Natural Level                       |            |
| move             | String  | Move Direction                      |            |

### Smartmi Standing Fan 2S (<a name="zhimi-fan-za4">zhimi.fan.za4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| angleEnable      | Switch  | Rotation                            |            |
| usedhours        | Number:Time | Run Time                            |            |
| angle            | Number  | Angle                               |            |
| poweroffTime     | Number  | Timer                               |            |
| buzzer           | Number  | Buzzer                              |            |
| led_b            | Number  | LED                                 |            |
| child_lock       | Switch  | Child Lock                          |            |
| speedLevel       | Number  | Speed Level                         |            |
| speed            | Number  | Speed                               |            |
| naturalLevel     | Number  | Natural Level                       |            |
| move             | String  | Move Direction                      |            |

### Mi Smart Standing Fan 2 Lite (<a name="dmaker-fan-1c">dmaker.fan.1c</a>) Channels

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

### Mi Smart Standing Fan 1X (<a name="dmaker-fan-p5">dmaker.fan.p5</a>) Channels

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

### Mi Smart Standing Fan 1C (<a name="dmaker-fan-p8">dmaker.fan.p8</a>) Channels

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

### Mi Smart Tower Fan (<a name="dmaker-fan-p9">dmaker.fan.p9</a>) Channels

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

### Mi Smart Standing Fan 2 (<a name="dmaker-fan-p10">dmaker.fan.p10</a>) Channels

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

### Mi smart Home Gateway Hub v1 (<a name="lumi-gateway-v1">lumi.gateway.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| telnetEnable     | Switch  | Enable Telnet                       | Access the device with telnet to further unlock developer mode. See forum |
| doorbellVol      | Number  | Doorbell Volume                     |            |
| gatewayVol       | Number  | Gateway Volume                      |            |
| alarmingVol      | Number  | Alarming Volume                     |            |
| doorbellPush     | String  | Doorbell Push                       |            |

### Mi smart Home GatewayHub v2 (<a name="lumi-gateway-v2">lumi.gateway.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| telnetEnable     | Switch  | Enable Telnet                       | Access the device with telnet to further unlock developer mode. See forum |
| doorbellVol      | Number  | Doorbell Volume                     |            |
| gatewayVol       | Number  | Gateway Volume                      |            |
| alarmingVol      | Number  | Alarming Volume                     |            |
| doorbellPush     | String  | Doorbell Push                       |            |

### Mi mart Home Gateway Hub v3 (<a name="lumi-gateway-v3">lumi.gateway.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| telnetEnable     | Switch  | Enable Telnet                       | Access the device with telnet to further unlock developer mode. See forum |
| doorbellVol      | Number  | Doorbell Volume                     |            |
| gatewayVol       | Number  | Gateway Volume                      |            |
| alarmingVol      | Number  | Alarming Volume                     |            |
| doorbellPush     | String  | Doorbell Push                       |            |

### Mi Air Purifier virtual (<a name="lumi-gateway-mgl03">lumi.gateway.mgl03</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| telnetEnable     | Switch  | Enable Telnet                       | Access the device with telnet to further unlock developer mode. See forum |
| doorbellVol      | Number  | Doorbell Volume                     |            |
| gatewayVol       | Number  | Gateway Volume                      |            |
| alarmingVol      | Number  | Alarming Volume                     |            |
| doorbellPush     | String  | Doorbell Push                       |            |

### HUIZUO ARIES For Bedroom (<a name="huayi-light-ari013">huayi.light.ari013</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO ARIES For Living Room (<a name="huayi-light-aries">huayi.light.aries</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO Fan Light (<a name="huayi-light-fanwy">huayi.light.fanwy</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |
| on1              | Switch  | Fan - Power                         |            |
| fan-level        | Number:Dimensionless | Fan - Fan Level                     |            |
| motor-reverse    | Switch  | Fan - Motor Reverse                 |            |
| mode             | Number  | Fan - Mode                          | Value mapping ["1"="Normal Wind","2"="Natural Wind"] |

### HUIZUO Fan Light(2020) (<a name="huayi-light-fanwy2">huayi.light.fanwy2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |
| on1              | Switch  | Fan - Power                         |            |
| fan-level        | Number:Dimensionless | Fan - Fan Level                     |            |
| mode             | Number  | Fan - Mode                          | Value mapping ["0"="Basic","1"="Natural Wind"] |
| time-off         | Number:Time | Presets - Time Off                  |            |
| pre-colortemp    | Number:Temperature | Presets - Pre Colortemp             |            |
| pre-brightness   | Number:Dimensionless | Presets - Pre Brightness            |            |
| pre-speed        | Number:Dimensionless | Presets - Pre Speed                 |            |
| reversal         | Number  | Presets - Reversal                  | Value mapping ["0"="Postitive","1"="Reverse"] |
| pre-custom       | Number  | Presets - Pre Custom                | Value mapping ["0"="Switch Off","1"="Open"] |
| flabellum        | Switch  | Presets - Flabellum                 |            |

### HUIZUO PEGASUS For Living Room (<a name="huayi-light-peg091">huayi.light.peg091</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO PEGASUS For Bedroom (<a name="huayi-light-peg093">huayi.light.peg093</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO PISCES For Bedroom (<a name="huayi-light-pis123">huayi.light.pis123</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO PISCES For Living Room (<a name="huayi-light-pisces">huayi.light.pisces</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO TAURUS For Bedroom (<a name="huayi-light-tau023">huayi.light.tau023</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO TAURUS For Living Room (<a name="huayi-light-taurus">huayi.light.taurus</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO VIRGO For Bedroom (<a name="huayi-light-vir063">huayi.light.vir063</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO VIRGO For Living Room (<a name="huayi-light-virgo">huayi.light.virgo</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO Ceiling Light (<a name="huayi-light-wy">huayi.light.wy</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO LIANGCHEN(BLE Mesh) (<a name="huayi-light-wy200">huayi.light.wy200</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO SAG Downlight (BLE Mesh) (<a name="huayi-light-wy201">huayi.light.wy201</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO Bulb (BLE Mesh) (<a name="huayi-light-wy202">huayi.light.wy202</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO YONG Downlight (BLE Mesh) (<a name="huayi-light-wy203">huayi.light.wy203</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### huayi.light.wy204 (<a name="huayi-light-wy204">huayi.light.wy204</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### HUIZUO Heating Lamp (<a name="huayi-light-wyheat">huayi.light.wyheat</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |
| on1              | Switch  | Heater - Power                      |            |
| fault            | Number  | Heater - Device Fault               | Value mapping ["0"="No Faults"] |
| heat-level       | Number  | Heater - Heat Level                 |            |
| screenshow       | Switch  | Other - Screenshow                  |            |

### HUIZUO ZIWEI Ceiling Lamp (<a name="huayi-light-zw131">huayi.light.zw131</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Light - Power                       |            |
| brightness       | Dimmer  | Light - Brightness                  |            |
| color-temperature | Number:Temperature | Light - Color Temperature           |            |

### Xiaomiyoupin Curtain Controller (Wi-Fi) (<a name="lumi-curtain-hagl05">lumi.curtain.hagl05</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| fault            | Number  | Curtain - Device Fault              | Value mapping ["0"="No faults"] |
| current-position | Number  | Curtain - Current Position          |            |
| status           | Number  | Curtain - Status                    | Value mapping ["0"="Stopped","1"="Opening","2"="Closing"] |
| target-position  | Number  | Curtain - Target Position           |            |
| manual-enabled   | Number  | curtain_cfg - Manual Enabled        | Value mapping ["0"="Disable","1"="Enable"] |
| polarity         | Number  | curtain_cfg - Polarity              | Value mapping ["0"="Positive","1"="Reverse"] |
| pos-limit        | Number  | curtain_cfg - Position Limit        | Value mapping ["0"="Unlimit","1"="Limit"] |
| en-night-tip-light | Switch  | Set Night Tip Light                 | Value mapping ["0"="Disable","1"="Enable"] |
| run-time         | Number  | curtain_cfg - Run-time              |            |

### MR.BOND (<a name="mrbond-airer-m1pro">mrbond.airer.m1pro</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| dry              | Switch  | Dry                                 |            |
| led              | Switch  | LED Status                          |            |
| motor            | Number  | Motor                               |            |
| drytime          | Number  | Dry Time                            |            |
| airer_location   | Number  | Airer Location                      |            |
| disinfect        | Switch  | disinfect                           |            |
| distime          | Number  | Disinfect Time                      |            |

### MR.BOND (<a name="mrbond-airer-m1s">mrbond.airer.m1s</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| dry              | Switch  | Dry                                 |            |
| led              | Switch  | LED Status                          |            |
| motor            | Number  | Motor                               |            |
| drytime          | Number  | Dry Time                            |            |
| airer_location   | Number  | Airer Location                      |            |
| disinfect        | Switch  | disinfect                           |            |
| distime          | Number  | Disinfect Time                      |            |

### MR.BOND (<a name="mrbond-airer-m1super">mrbond.airer.m1super</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| dry              | Switch  | Dry                                 |            |
| led              | Switch  | LED Status                          |            |
| motor            | Number  | Motor                               |            |
| drytime          | Number  | Dry Time                            |            |
| airer_location   | Number  | Airer Location                      |            |
| disinfect        | Switch  | disinfect                           |            |
| distime          | Number  | Disinfect Time                      |            |

### Mi Robot Vacuum-Mop Essential (<a name="mijia-vacuum-v2">mijia.vacuum.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| vacuumaction     | String  | Vacuum Action                       | This channel accepts `start`, `stop`, `findme` and `dock`. |
| status           | Number  | Robot Cleaner - Status              | Value mapping ["1"="Idle","2"="Sweeping","3"="Paused","4"="Error","5"="Charging","6"="Go Charging"] |
| fault            | Number  | Robot Cleaner - Device Fault        | Value mapping ["0"="No Faults","1"="Left-wheel-error","2"="Right-wheel-error","3"="Cliff-error","4"="Low-battery-error","5"="Bump-error","6"="Main-brush-error","7"="Side-brush-error","8"="Fan-motor-error","9"="Dustbin-error","10"="Charging-error","11"="No-wate-error","12"="Pick-up-error"] |
| mode             | Number  | Robot Cleaner - Mode                | Value mapping ["1"="Auto-clean","2"="Spot-clean","3"="Wallflow-clean"] |
| target-water-level | Number  | Robot Cleaner - Target Water Level  | Value mapping ["1"="Level1","2"="Level2","3"="Level3"] |
| fan-level        | Number  | Robot Cleaner - Fan Level           | Value mapping ["0"="Silence","1"="Stanrd","2"="Middle","3"="Enchance"] |
| battery-level    | Number  | Battery - Battery Level             |            |
| charging-state   | Number  | Battery - Charging State            | Value mapping ["0"="Not-charging","1"="Charging","2"="Charging-competely"] |
| alarm            | Switch  | Alarm - Alarm                       |            |
| volume           | Number  | Alarm - Volume                      |            |
| filter-life-level | Number  | Filter - Filter Life Level          |            |
| filter-left-time | Number  | Filter - Filter Left Time           |            |
| brush-life-level | Number  | Brush Cleaner - Brush Life Level    |            |
| brush-left-time  | Number  | Brush Cleaner - Brush Left Time     |            |
| brush-life-level1 | Number  | Brush Cleaner - Brush Life Level    |            |
| brush-left-time1 | Number  | Brush Cleaner - Brush Left Time     |            |
| clean-area       | Number:Area | Clean Record - Clean Area           |            |
| clean-time       | Number:Time | Clean Record - Clean Time           |            |
| total-clean-area | Number  | Clean Record - Total Clean Area     |            |
| total-clean-time | Number  | Clean Record - Total Clean Time     |            |
| total-clean-count | Number  | Clean Record - Total Clean Count    |            |
| language         | Number  | Language - Language                 | Value mapping ["0"="English","1"="简体中文","2"="Español","3"="Русский","4"="Italiano","5"="Français","6"="Deutsch","7"="한국어","8"="Polski"] |
| not-disturb-switch | Switch  | Language - Not Disturb Switch       |            |
| mop-status       | Number  | Other Status - Mop Status           | Value mapping ["0"="Mop Uninstall","1"="Mop Install"] |

### SCISHARE Smart Capsule Coffee Machine (<a name="scishare-coffee-s1102">scishare.coffee.s1102</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| Status           | String  | status                              |            |
| expresso         | Number  | Brew Expresso                       | Value is the amount of coffee in ml. 2nd parameter in the json db is the water temperature |
| expresso         | Number  | Brew Americano                      | Value is the amount of coffee in ml. parameters in json are cupAmount: [set by OH], coffee: 40,water: 80, temp: 90 |
| boil             | Number  | Boil water                          | Value is the amount of water (ml) to heat. 2nd parameter in the json db is the water temperature |

### Xiaomi Scishare smart capsule coffee machine (<a name="scishare-coffee-s1301">scishare.coffee.s1301</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| Status           | String  | status                              |            |
| expresso         | Number  | Brew Expresso                       | Value is the amount of coffee in ml. 2nd parameter in the json db is the water temperature |
| expresso         | Number  | Brew Americano                      | Value is the amount of coffee in ml. parameters in json are cupAmount: [set by OH], coffee: 40,water: 80, temp: 90 |
| boil             | Number  | Boil water                          | Value is the amount of water (ml) to heat. 2nd parameter in the json db is the water temperature |

### Philips EyeCare connected desk lamp gen2. (<a name="philips-light-sread1">philips.light.sread1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| ambientPower     | Switch  | Ambient Power                       |            |
| ambientBrightness | Number  | Ambient Brightness                  |            |
| illumination     | Number  | Ambient Illumination                |            |
| eyecare          | Switch  | Eyecare                             |            |
| bl               | Switch  | Night Light                         |            |

### Mijia Philips Desk Lamp 2S (<a name="philips-light-sread2">philips.light.sread2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| ambientPower     | Switch  | Ambient Power                       |            |
| ambientBrightness | Number  | Ambient Brightness                  |            |
| illumination     | Number  | Ambient Illumination                |            |
| eyecare          | Switch  | Eyecare                             |            |
| bl               | Switch  | Night Light                         |            |

### Philips connected ceiling (<a name="philips-light-ceiling">philips.light.ceiling</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| switchscene      | Switch  | Switch Scene                        |            |
| toggle           | Switch  | Toggle                              |            |

### Philips ZhiYi ceiling lamp (<a name="philips-light-zyceiling">philips.light.zyceiling</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| switchscene      | Switch  | Switch Scene                        |            |
| toggle           | Switch  | Toggle                              |            |

### Philips ZhiRui E27 bulb (<a name="philips-light-bulb">philips.light.bulb</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Philips Wi-Fi bulb E27 White (<a name="philips-light-hbulb">philips.light.hbulb</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Philips ZhiRui E14 candle lamp Frosted version (<a name="philips-light-candle">philips.light.candle</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| delayoff         | Switch  | Delay Off                           |            |
| toggle           | Switch  | Toggle                              |            |

### Philips ZhiRui downlight (<a name="philips-light-downlight">philips.light.downlight</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Philips ZhiRui bedside lamp (<a name="philips-light-moonlight">philips.light.moonlight</a>) Channels

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

### Philips Zhirui Ceiling Lamp Bedroom 40W (<a name="philips-light-bceiling1">philips.light.bceiling1</a>) Channels

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

### Philips Zhirui Ceiling Lamp Bedroom 28W (<a name="philips-light-bceiling2">philips.light.bceiling2</a>) Channels

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

### Mijia Philips Color Bulb (<a name="philips-light-cbulb">philips.light.cbulb</a>) Channels

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

### Mijia Philips Study Desk Lamp (<a name="philips-light-rwread">philips.light.rwread</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| scene            | Number  | Scene                               |            |
| flm              | Number  | Follow Me                           |            |
| dv               | Number  | DV                                  |            |

### Philips ZhiYi Ceiling lamp FL 40W (<a name="philips-light-lnblight1">philips.light.lnblight1</a>) Channels

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

### Philips ZhiYi Ceiling lamp FL 28W (<a name="philips-light-lnblight2">philips.light.lnblight2</a>) Channels

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

### Philips ZhiYi Ceiling lamp FL 80W (<a name="philips-light-lnlrlight">philips.light.lnlrlight</a>) Channels

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

### Philips Zhirui Ceiling Lamp Living room 80W (<a name="philips-light-lrceiling">philips.light.lrceiling</a>) Channels

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

### Philips ZhiRui E14 candle lamp crystal version (<a name="philips-light-candle2">philips.light.candle2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| delayoff         | Switch  | Delay Off                           |            |
| toggle           | Switch  | Toggle                              |            |

### Philips Smart Lamp (<a name="philips-light-mono1">philips.light.mono1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| scene            | Number  | Scene                               |            |

### ZhiRui dimmable downlight (<a name="philips-light-dlight">philips.light.dlight</a>) Channels

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

### Zhirui Ceiling Lamp Nordic 80W (<a name="philips-light-mceil">philips.light.mceil</a>) Channels

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

### Zhirui Ceiling Lamp Nordic 40W (<a name="philips-light-mceilm">philips.light.mceilm</a>) Channels

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

### Zhirui Ceiling Lamp Nordic 28W (<a name="philips-light-mceils">philips.light.mceils</a>) Channels

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

### Zhirui Ceiling Lamp Black 80W (<a name="philips-light-obceil">philips.light.obceil</a>) Channels

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

###  Zhirui Ceiling Lamp Black 40W (<a name="philips-light-obceim">philips.light.obceim</a>) Channels

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

### Zhirui Ceiling Lamp Black 28W (<a name="philips-light-obceis">philips.light.obceis</a>) Channels

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

### Zhirui Ceiling Lamp Starry 80W (<a name="philips-light-sceil">philips.light.sceil</a>) Channels

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

### Zhirui Ceiling Lamp Starry 40W (<a name="philips-light-sceilm">philips.light.sceilm</a>) Channels

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

### Zhirui Ceiling Lamp Starry 28W (<a name="philips-light-sceils">philips.light.sceils</a>) Channels

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

### Zhirui Ceiling Lamp Gorgeous 80W (<a name="philips-light-xzceil">philips.light.xzceil</a>) Channels

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

### Zhirui Ceiling Lamp Gorgeous 40W (<a name="philips-light-xzceim">philips.light.xzceim</a>) Channels

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

### Zhirui Ceiling Lamp Gorgeous 28W (<a name="philips-light-xzceis">philips.light.xzceis</a>) Channels

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

### Philips connected lights (<a name="philips-light-virtual">philips.light.virtual</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Philips ZhiYi desk lamp (<a name="philips-light-zysread">philips.light.zysread</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Philips ZhiYi strip (<a name="philips-light-zystrip">philips.light.zystrip</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| cct              | Dimmer  | Correlated Color Temperature        |            |
| scene            | Number  | Scene                               |            |
| dv               | Number  | DV                                  |            |
| switchscene      | Switch  | Switch Scene                        |            |
| delayoff         | Switch  | Delay Off                           |            |

### Mi Plug Mini (<a name="chuangmi-plug-m1">chuangmi.plug.m1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| temperature      | Number:Temperature | Temperature                         |            |
| led              | Switch  | Indicator light                     |            |

### Mi Smart Power Plug (<a name="chuangmi-plug-v1">chuangmi.plug.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| usb              | Switch  | USB                                 |            |
| temperature      | Number:Temperature | Temperature                         |            |

### Mi Smart Power Plug v2 (<a name="chuangmi-plug-v2">chuangmi.plug.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| usb              | Switch  | USB                                 |            |

### MIJIA Smart  Plug Enhanced (<a name="chuangmi-plug-v3">chuangmi.plug.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               | If this channel does not respond to on/off replace the model with chuangmi.plug.v3old in the config or upgrade firmware |
| usb              | Switch  | USB                                 |            |
| temperature      | Number:Temperature | Temperature                         |            |
| led              | Switch  | Wifi LED                            |            |

### Mi Smart Plug (Wi-Fi) Basic (<a name="chuangmi-plug-m3">chuangmi.plug.m3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| temperature      | Number:Temperature | Temperature                         |            |
| led              | Switch  | Indicator light                     |            |

### Mi Smart Plug WiFi (<a name="chuangmi-plug-hmi205">chuangmi.plug.hmi205</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| temperature      | Number:Temperature | Temperature                         |            |
| led              | Switch  | Indicator light                     |            |

### Mi Smart Plug (WiFi) (<a name="chuangmi-plug-hmi206">chuangmi.plug.hmi206</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               | If this channel does not respond to on/off replace the model with chuangmi.plug.v3old in the config or upgrade firmware |
| usb              | Switch  | USB                                 |            |
| temperature      | Number:Temperature | Temperature                         |            |
| led              | Switch  | Wifi LED                            |            |

### Mi Smart Wi-Fi Plug (Bluetooth Gateway) (<a name="chuangmi-plug-hmi208">chuangmi.plug.hmi208</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               | If this channel does not respond to on/off replace the model with chuangmi.plug.v3old in the config or upgrade firmware |
| usb              | Switch  | USB                                 |            |
| temperature      | Number:Temperature | Temperature                         |            |
| led              | Switch  | Wifi LED                            |            |

### CHINGMI Smart Power Strip v1 (<a name="qmi-powerstrip-v1">qmi.powerstrip.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| powerUsage       | Number  | Power Consumption                   |            |
| led              | Switch  | wifi LED                            |            |
| power_price      | Number  | power_price                         |            |
| current          | Number  | Current                             |            |
| temperature      | Number:Temperature | Temperature                         |            |
| lp_autooff       | Number  | Low Power Auto Off                  |            |
| lp_autooff_delay | Number  | Low Power Limit Time                |            |
| lp_threshold     | Number  | Low Power Threshold                 |            |

### Mi Smart Power Strip (<a name="zimi-powerstrip-v2">zimi.powerstrip.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| powerUsage       | Number  | Power Consumption                   |            |
| led              | Switch  | wifi LED                            |            |
| power_price      | Number  | power_price                         |            |
| current          | Number  | Current                             |            |
| temperature      | Number:Temperature | Temperature                         |            |
| lp_autooff       | Number  | Low Power Auto Off                  |            |
| lp_autooff_delay | Number  | Low Power Limit Time                |            |
| lp_threshold     | Number  | Low Power Threshold                 |            |

### Viomi Cleaning Robot V-RVCLM21B (<a name="viomi-vacuum-v6">viomi.vacuum.v6</a>) Channels

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

### Mi Robot Vacuum-Mop P (<a name="viomi-vacuum-v7">viomi.vacuum.v7</a>) Channels

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

### Mi Robot Vacuum-Mop P (<a name="viomi-vacuum-v8">viomi.vacuum.v8</a>) Channels

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

### PTX OneKey Switch (WIFI) (<a name="090615-switch-xswitch01">090615.switch.xswitch01</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| switch1state     | Number  | Switch 1                            |            |
| switch1name      | String  | Switch Name 1                       |            |

### PTX Twokey switch(wifi) (<a name="090615-switch-xswitch02">090615.switch.xswitch02</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| switch1state     | Number  | Switch 1                            |            |
| switch2state     | Number  | Switch 2                            |            |
| switch1name      | String  | Switch Name 1                       |            |
| switch2name      | String  | Switch Name 2                       |            |

### PTX ThreeKey Switch (WIFI) (<a name="090615-switch-xswitch03">090615.switch.xswitch03</a>) Channels

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
| temperature      | Number:Temperature | Temperature                         |            |
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
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier (Under sink) v3 (<a name="yunmi-waterpurifier-v3">yunmi.waterpurifier.v3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| temperature      | Number:Temperature | Temperature                         |            |
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
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier (Under Counter) (<a name="yunmi-waterpuri-lx3">yunmi.waterpuri.lx3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier 1A/400G Pro (<a name="yunmi-waterpuri-lx5">yunmi.waterpuri.lx5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier (Under Counter) (<a name="yunmi-waterpuri-lx6">yunmi.waterpuri.lx6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier 500G/500G Pro (<a name="yunmi-waterpuri-lx7">yunmi.waterpuri.lx7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier 600G (<a name="yunmi-waterpuri-lx8">yunmi.waterpuri.lx8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier D1 (<a name="yunmi-waterpuri-lx9">yunmi.waterpuri.lx9</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier C1 (Triple Setting) (<a name="yunmi-waterpuri-lx11">yunmi.waterpuri.lx11</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Water Purifier S1 (<a name="yunmi-waterpuri-lx12">yunmi.waterpuri.lx12</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| run_status       | Number  | Run Status                          |            |
| temperature      | Number:Temperature | Temperature                         |            |
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

### Mi Inverter Air Conditioner (1.5HP) (<a name="xiaomi-aircondition-ma1">xiaomi.aircondition.ma1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |

### Mi Inverter Air Conditioner (1.5HP, China Energy Label Level 1) (<a name="xiaomi-aircondition-ma2">xiaomi.aircondition.ma2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |

### Mi Vertical Air Conditioner (2HP) (<a name="xiaomi-aircondition-ma4">xiaomi.aircondition.ma4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |

### Mi Smart Vertical Air Conditioner C1 (2HP / Inverter / China Energy Label Level 1) (<a name="xiaomi-aircondition-ma5">xiaomi.aircondition.ma5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |

### Mi Smart Air Conditioner C1 (1.5HP / Conventional / China Energy Label Level 3) (<a name="xiaomi-aircondition-ma6">xiaomi.aircondition.ma6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |

### Mi Smart Air Conditioner C1 (1HP / Inverter / China Energy Label Level 1) (<a name="xiaomi-aircondition-ma9">xiaomi.aircondition.ma9</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |

### Mi Smart Ultra Electricity Saving Vertical Air Conditioner (2HP/Inverter/New China Energy Label Level 1) (<a name="xiaomi-aircondition-c10">xiaomi.aircondition.c10</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Ultra Electricity Saving Vertical Air Conditioner (3HP/Inverter/New China Energy Label Level 1) (<a name="xiaomi-aircondition-c11">xiaomi.aircondition.c11</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Air Conditioner A (1HP / Inverter / China Energy Label Level 1) (<a name="xiaomi-aircondition-mc1">xiaomi.aircondition.mc1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Air Conditioner A (1.5HP / Inverter / China Energy Label Level 1) (<a name="xiaomi-aircondition-mc2">xiaomi.aircondition.mc2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Air Conditioner A (1HP / Inverter / China Energy Label Level <1) (<a name="xiaomi-aircondition-mc4">xiaomi.aircondition.mc4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Air Conditioner A (1.5HP / Inverter / China Energy Label Level <1) (<a name="xiaomi-aircondition-mc5">xiaomi.aircondition.mc5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Vertical Air Conditioner A (2HP / Inverter / China Energy Label Level <1) (<a name="xiaomi-aircondition-mc6">xiaomi.aircondition.mc6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Vertical Air Conditioner A (3HP / Inverter / China Energy Label Level <1) (<a name="xiaomi-aircondition-mc7">xiaomi.aircondition.mc7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Ultra Electricity Saving Air Conditioner(1.5HP/Inverter/New China Energy Label Level 3) (<a name="xiaomi-aircondition-mc8">xiaomi.aircondition.mc8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Ultra Electricity Saving Vertical Air Conditioner(2HP/Inverter/New China Energy Label Level 3) (<a name="xiaomi-aircondition-mc9">xiaomi.aircondition.mc9</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Air Conditioner C (1HP / Inverter / New China Energy Label Level 1) (<a name="xiaomi-aircondition-mh1">xiaomi.aircondition.mh1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Air Conditioner C (1.5HP / Inverter / New China Energy Label Level 1) (<a name="xiaomi-aircondition-mh2">xiaomi.aircondition.mh2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Ultra Electricity Saving Air Conditioner(1HP/Inverter/New China Energy Label Level 3) (<a name="xiaomi-aircondition-mh3">xiaomi.aircondition.mh3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Air Conditioner X (1HP / Inverter / New China Energy Label Level 1) (<a name="xiaomi-aircondition-mt1">xiaomi.aircondition.mt1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Air Conditioner X (1.5HP / Inverter / New China Energy Label Level 1) (<a name="xiaomi-aircondition-mt2">xiaomi.aircondition.mt2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Gentle Breeze Air Conditioner (1HP / Inverter / New China Energy Label Level 1) (<a name="xiaomi-aircondition-mt3">xiaomi.aircondition.mt3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Gentle Breeze Air Conditioner (1.5HP / Inverter / New China Energy Label Level 1) (<a name="xiaomi-aircondition-mt4">xiaomi.aircondition.mt4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Gentle Breeze Vertical Air Conditioner (3HP / Inverter / New China Energy Label Level 1) (<a name="xiaomi-aircondition-mt5">xiaomi.aircondition.mt5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Ultra Electricity Saving Air Conditioner (1HP/Inverter/New China Energy Label Level 1) (<a name="xiaomi-aircondition-mt7">xiaomi.aircondition.mt7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Mi Smart Ultra Electricity Saving Air Conditioner (1.5HP/Inverter/New China Energy Label Level 1) (<a name="xiaomi-aircondition-mt8">xiaomi.aircondition.mt8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | Power                               |            |
| mode             | Number  | Air Conditioner - Mode              | Value mapping [2="Cool",3="Dry",4="Fan",5="Heat"] |
| target-temperature | Number:Temperature | Air Conditioner - Target Temperature |            |
| eco              | Switch  | Air Conditioner - Eco               |            |
| heater           | Switch  | Air Conditioner - Heater            |            |
| dryer            | Switch  | Air Conditioner - Dryer             |            |
| sleep-mode       | Switch  | Air Conditioner - Sleep Mode        |            |
| fan-level        | Number  | Fan Control - Fan Level             | Value mapping [0="Auto",1="Level1",2="Level2",3="Level3",4="Level4",5="Level5",6="Level6",7="Level7"] |
| vertical-swing   | Switch  | Fan Control - Vertical Swing        |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| on1              | Switch  | Indicator Light - Switch Status     |            |
| electricity      | Number:Energy | Power consumption accumulation in kWh |            |
| elec-count       | Number  | Electricity - Count                 |            |
| clean            | String  | Maintenance - Clean                 |            |
| examine          | String  | Maintenance - Examine               |            |
| running-duration | Number:Duration | Maintenance - Running Duration      |            |
| fan-percent      | Number:Dimentionless | Fan Speed %                         |            |
| timer            | String  | Enhance - Timer                     |            |

### Uclean Smart Toilet Seat (<a name="xjx-toilet-pro">xjx.toilet.pro</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| seat_temp        | Number  | Seat Temperature                    |            |
| status_seatheat  | Number  | Seat Status                         |            |
| water_temp_t     | Number  | Water Temperature                   |            |
| fan_temp         | Number  | Fan Temperature                     |            |
| status_led       | Number  | Night Light                         |            |

### Uclean smart toilet relax (<a name="xjx-toilet-relax">xjx.toilet.relax</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| seat_temp        | Number  | Seat Temperature                    |            |
| status_seatheat  | Number  | Seat Status                         |            |
| water_temp_t     | Number  | Water Temperature                   |            |
| fan_temp         | Number  | Fan Temperature                     |            |
| status_led       | Number  | Night Light                         |            |

### Uclean smart toilet pure (<a name="xjx-toilet-pure">xjx.toilet.pure</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| seat_temp        | Number  | Seat Temperature                    |            |
| status_seatheat  | Number  | Seat Status                         |            |
| water_temp_t     | Number  | Water Temperature                   |            |
| fan_temp         | Number  | Fan Temperature                     |            |
| status_led       | Number  | Night Light                         |            |

### Whale Spout Smart Toilet Zero (<a name="xjx-toilet-zero">xjx.toilet.zero</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| seat_temp        | Number  | Seat Temperature                    |            |
| status_seatheat  | Number  | Seat Status                         |            |
| water_temp_t     | Number  | Water Temperature                   |            |
| fan_temp         | Number  | Fan Temperature                     |            |
| status_led       | Number  | Night Light                         |            |

### Mi Bedside Lamp (<a name="yeelink-light-bslamp1">yeelink.light.bslamp1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Mi Bedside Lamp 2 (<a name="yeelink-light-bslamp2">yeelink.light.bslamp2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Bedside Lamp II (<a name="yeelink-light-bslamp3">yeelink.light.bslamp3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Smart Bath Heater Pro (<a name="yeelink-bhf_light-v1">yeelink.bhf_light.v1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| bh_mode          | String  | Bath Heater mode                    |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Smart Bath Heater (<a name="yeelink-bhf_light-v2">yeelink.bhf_light.v2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| bh_mode          | String  | Bath Heater mode                    |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| temperature      | Number:Temperature | Temperature                         |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light (<a name="yeelink-light-ceiling1">yeelink.light.ceiling1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light SE (<a name="yeelink-light-ceiling2">yeelink.light.ceiling2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Light (<a name="yeelink-light-ceiling3">yeelink.light.ceiling3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Light (<a name="yeelink-light-ceiling4">yeelink.light.ceiling4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| ambientBrightness | Number  | Ambient Brightness                  |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| ambientPower     | Switch  | Ambient Power                       |            |
| ambientColor     | Color   | Ambient Color                       |            |
| ambientColorTemperature | Number  | Ambient Color Temperature           |            |
| customScene      | String  | Set Scene                           |            |
| ambientColorMode | Number  | Ambient Color Mode                  |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Ambi Lamp (<a name="yeelink-light-ceiling4-ambi">yeelink.light.ceiling4.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Mi LED Ceiling Light (<a name="yeelink-light-ceiling5">yeelink.light.ceiling5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight HaoShi LED Ceiling Lamp Pro (<a name="yeelink-light-ceiling6">yeelink.light.ceiling6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Haoshi Ceiling Lamp (<a name="yeelink-light-ceiling7">yeelink.light.ceiling7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### LED Ceiling Light Crystal Plus (<a name="yeelink-light-ceiling8">yeelink.light.ceiling8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight HaoShi LED Ceiling Lamp Pro (<a name="yeelink-light-ceiling9">yeelink.light.ceiling9</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Crystal Pendant Lamp (<a name="yeelink-light-ceiling10">yeelink.light.ceiling10</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| ambientBrightness | Number  | Ambient Brightness                  |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| ambientPower     | Switch  | Ambient Power                       |            |
| ambientColor     | Color   | Ambient Color                       |            |
| ambientColorTemperature | Number  | Ambient Color Temperature           |            |
| customScene      | String  | Set Scene                           |            |
| ambientColorMode | Number  | Ambient Color Mode                  |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light 320 1S (<a name="yeelink-light-ceiling11">yeelink.light.ceiling11</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight stylized Ceiling Light  Pro (<a name="yeelink-light-ceiling12">yeelink.light.ceiling12</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light (<a name="yeelink-light-ceiling13">yeelink.light.ceiling13</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light Mini (<a name="yeelink-light-ceiling14">yeelink.light.ceiling14</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light 480 1S (<a name="yeelink-light-ceiling15">yeelink.light.ceiling15</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Xingyu Ceiling Light (<a name="yeelink-light-ceiling16">yeelink.light.ceiling16</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight ShaoHua Celing Light (<a name="yeelink-light-ceiling17">yeelink.light.ceiling17</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light Pro (<a name="yeelink-light-ceiling18">yeelink.light.ceiling18</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light Pro (<a name="yeelink-light-ceiling19">yeelink.light.ceiling19</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Ceiling Light (<a name="yeelink-light-ceiling20">yeelink.light.ceiling20</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Mi Smart LED Living Room Ceiling Light (<a name="yeelink-light-ceiling21">yeelink.light.ceiling21</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Mi Smart LED Ceiling Light (<a name="yeelink-light-ceiling22">yeelink.light.ceiling22</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Mi Smart LED Ceiling Light (350mm) (<a name="yeelink-light-ceiling23">yeelink.light.ceiling23</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Ambi Lamp (<a name="yeelink-light-ceiling10-ambi">yeelink.light.ceiling10.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Ambi Lamp (<a name="yeelink-light-ceiling19-ambi">yeelink.light.ceiling19.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Ceiling Ambi Lamp (<a name="yeelink-light-ceiling20-ambi">yeelink.light.ceiling20.ambi</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight LED Bulb (Tunable) (<a name="yeelink-light-ct2">yeelink.light.ct2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight Bulb (<a name="yeelink-light-mono1">yeelink.light.mono1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight White Bulb v2 (<a name="yeelink-light-mono2">yeelink.light.mono2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight LED Filament Bulb (<a name="yeelink-light-mono5">yeelink.light.mono5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Mi LED Desk Lamp (<a name="yeelink-light-lamp1">yeelink.light.lamp1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Mi Smart LED Desk Lamp Pro (<a name="yeelink-light-lamp2">yeelink.light.lamp2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight LED Lamp (<a name="yeelink-light-lamp3">yeelink.light.lamp3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Mi LED Desk Lamp 1S (<a name="yeelink-light-lamp4">yeelink.light.lamp4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight Smart Desk Lamp Prime (<a name="yeelink-light-lamp5">yeelink.light.lamp5</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp6">yeelink.light.lamp6</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight LED Light Sensor Desk Lamp V1 (<a name="yeelink-light-lamp7">yeelink.light.lamp7</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight (<a name="yeelink-light-lamp8">yeelink.light.lamp8</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight Whiteglow Panel Light (<a name="yeelink-light-panel1">yeelink.light.panel1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yeelight Lightstrip (<a name="yeelink-light-strip1">yeelink.light.strip1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Lightstrip Plus (<a name="yeelink-light-strip2">yeelink.light.strip2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Willow LED Lightstrip (<a name="yeelink-light-strip4">yeelink.light.strip4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Light Group (Mi & Yeelight) (<a name="yeelink-light-virtual">yeelink.light.virtual</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |

### Yeelight Color Bulb (<a name="yeelink-light-color1">yeelink.light.color1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight LED Bulb (Color) (<a name="yeelink-light-color2">yeelink.light.color2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Mi LED Smart Bulb (White and Color) (<a name="yeelink-light-color3">yeelink.light.color3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight LED Bulb 1S（Color） (<a name="yeelink-light-color4">yeelink.light.color4</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| rgbColor         | Color   | RGB Color                           |            |
| name             | String  | Name                                |            |

### Yeelight Smart Dual Control Module (<a name="yeelink-switch-sw1">yeelink.switch.sw1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| on               | Switch  | First Switch - Switch Status        |            |
| mode             | Number  | First Switch Default - Mode         | Value mapping ["0"="Off","1"="On"] |
| mode1            | Number  | First Switch - Delay                |            |
| on1              | Switch  | Second Switch - Switch Status       |            |
| mode2            | Number  | Second Switch Default - Mode        | Value mapping ["0"="Off","1"="On"] |
| mode3            | Number  | Second Switch Service - Delay       |            |
| interlock        | Switch  | Extension - Interlock               |            |
| flash            | Number  | Extension - Flash                   |            |
| rc-list          | String  | Extension - Rc List                 |            |

### Yilai Ceiling Light Aiyue 480 (<a name="yilai-light-ceiling1">yilai.light.ceiling1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yilai Ceiling Lamp Hefeng 430 (<a name="yilai-light-ceiling2">yilai.light.ceiling2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Yilai Ceiling Lamp Hefeng Pro (<a name="yilai-light-ceiling3">yilai.light.ceiling3</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| brightness       | Dimmer  | Brightness                          |            |
| delayoff         | Number:Time | Shutdown Timer                      |            |
| colorTemperature | Number:Temperature | Color Temperature                   |            |
| colorMode        | Number  | Color Mode                          |            |
| name             | String  | Name                                |            |
| customScene      | String  | Set Scene                           |            |
| nightlightBrightness | Number  | Nightlight Brightness               |            |

### Smartmi Radiant Heater Smart Version (<a name="zhimi-heater-za1">zhimi.heater.za1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| power            | Switch  | Power                               |            |
| target_temperature | Number:Temperature | Target Temperature                  |            |
| brightness       | Number  | Brightness                          |            |
| buzzer           | Switch  | Buzzer Status                       |            |
| relative_humidity | Number  | Relative Humidity                   |            |
| childlock        | Switch  | Child Lock                          |            |
| HWSwitch         | Switch  | HW Switch                           |            |
| temperature      | Number:Temperature | Temperature                         |            |
| usedhours        | Number:Time | Run Time                            |            |

### Smartmi Smart Convector Heater 1S (<a name="zhimi-heater-za2">zhimi.heater.za2</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| fault            | Number  | Heater - Device Fault               |            |
| on               | Switch  | Heater - Power                      |            |
| target-temperature | Number:Temperature | Heater - Target Temperature         |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| countdown-time   | Number:Time | Countdown - Countdown Time          |            |
| relative-humidity | Number  | Environment - Relative Humidity     |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| brightness       | Dimmer  | Indicator Light - Brightness        |            |
| physical-controls-locked | Switch  | Physical Control Locked - Physical Controls Locked |            |
| use-time         | Number:Time | Private-Service - Use Time          |            |

### Smartmi Smart Convector Heater 1S (<a name="zhimi-heater-zb1">zhimi.heater.zb1</a>) Channels

| Channel          | Type    | Description                         | Comment    |
|------------------|---------|-------------------------------------|------------|
| fault            | Number  | Heater - Device Fault               |            |
| on               | Switch  | Heater - Power                      |            |
| target-temperature | Number:Temperature | Heater - Target Temperature         |            |
| alarm            | Switch  | Alarm - Alarm                       |            |
| countdown-time   | Number:Time | Countdown - Countdown Time          |            |
| relative-humidity | Number  | Environment - Relative Humidity     |            |
| temperature      | Number:Temperature | Environment - Temperature           |            |
| brightness       | Dimmer  | Indicator Light - Brightness        |            |
| physical-controls-locked | Switch  | Physical Control Locked - Physical Controls Locked |            |
| use-time         | Number:Time | Private-Service - Use Time          |            |
| country-code     | Number  | Private-Service - Country-Code      | Value mapping ["0"="Unknown","1"="US","82"="KR","44"="EU","81"="JP","7"="RU","86"="CN","852"="HK","886"="TW","33"="FR"] |




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



### Mi PM2.5 Air Quality Monitor (zhimi.airmonitor.v1) item file lines

note: Autogenerated example. Replace the id (airmonitor) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airmonitor "Mi PM2.5 Air Quality Monitor" <status>
Switch power "Power" (G_airmonitor) {channel="miio:basic:airmonitor:power"}
Number aqi "Air Quality Index" (G_airmonitor) {channel="miio:basic:airmonitor:aqi"}
Number battery "Battery" (G_airmonitor) {channel="miio:basic:airmonitor:battery"}
Switch usb_state "USB State" (G_airmonitor) {channel="miio:basic:airmonitor:usb_state"}
Switch time_state "Time State" (G_airmonitor) {channel="miio:basic:airmonitor:time_state"}
Switch night_state "Night State" (G_airmonitor) {channel="miio:basic:airmonitor:night_state"}
Number night_begin "Night Begin Time" (G_airmonitor) {channel="miio:basic:airmonitor:night_begin"}
Number night_end "Night End Time" (G_airmonitor) {channel="miio:basic:airmonitor:night_end"}
```

### Mi Multifunction Air Monitor (cgllc.airmonitor.b1) item file lines

note: Autogenerated example. Replace the id (airmonitor) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airmonitor "Mi Multifunction Air Monitor" <status>
Number battery "Battery" (G_airmonitor) {channel="miio:basic:airmonitor:battery"}
Number pm25 "PM2.5" (G_airmonitor) {channel="miio:basic:airmonitor:pm25"}
Number co2 "CO2e" (G_airmonitor) {channel="miio:basic:airmonitor:co2"}
Number tvoc "tVOC" (G_airmonitor) {channel="miio:basic:airmonitor:tvoc"}
Number humidity "Humidity" (G_airmonitor) {channel="miio:basic:airmonitor:humidity"}
Number:Temperature temperature "Temperature" (G_airmonitor) {channel="miio:basic:airmonitor:temperature"}
```

### Qingping Air Monitor (cgllc.airmonitor.s1) item file lines

note: Autogenerated example. Replace the id (airmonitor) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airmonitor "Qingping Air Monitor" <status>
Number battery "Battery" (G_airmonitor) {channel="miio:basic:airmonitor:battery"}
Number pm25 "PM2.5" (G_airmonitor) {channel="miio:basic:airmonitor:pm25"}
Number co2 "CO2" (G_airmonitor) {channel="miio:basic:airmonitor:co2"}
Number tvoc "tVOC" (G_airmonitor) {channel="miio:basic:airmonitor:tvoc"}
Number humidity "Humidity" (G_airmonitor) {channel="miio:basic:airmonitor:humidity"}
Number:Temperature temperature "Temperature" (G_airmonitor) {channel="miio:basic:airmonitor:temperature"}
```

### Smartmi Humidifier (zhimi.humidifier.v1) item file lines

note: Autogenerated example. Replace the id (humidifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_humidifier "Smartmi Humidifier" <status>
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
Number:Time usedhours "Run Time" (G_humidifier) {channel="miio:basic:humidifier:usedhours"}
Number motorspeed "Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:motorspeed"}
Number:Temperature temperature "Temperature" (G_humidifier) {channel="miio:basic:humidifier:temperature"}
Switch childlock "Child Lock" (G_humidifier) {channel="miio:basic:humidifier:childlock"}
```

### Smartmi Evaporative Humidifier (zhimi.humidifier.ca1) item file lines

note: Autogenerated example. Replace the id (humidifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_humidifier "Smartmi Evaporative Humidifier" <status>
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
Number:Time usedhours "Run Time" (G_humidifier) {channel="miio:basic:humidifier:usedhours"}
Number motorspeed "Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:motorspeed"}
Number:Temperature temperature "Temperature" (G_humidifier) {channel="miio:basic:humidifier:temperature"}
Switch childlock "Child Lock" (G_humidifier) {channel="miio:basic:humidifier:childlock"}
```

### Smartmi Evaporative Humidifer 2 (zhimi.humidifier.ca4) item file lines

note: Autogenerated example. Replace the id (humidifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_humidifier "Smartmi Evaporative Humidifer 2" <status>
Switch power "Power" (G_humidifier) {channel="miio:basic:humidifier:power"}
Number mode "Mode" (G_humidifier) {channel="miio:basic:humidifier:mode"}
Number Fault "Humidifier Device Fault" (G_humidifier) {channel="miio:basic:humidifier:Fault"}
Number humidity "Humidity" (G_humidifier) {channel="miio:basic:humidifier:humidity"}
Number targetHumidity "Target Humidity" (G_humidifier) {channel="miio:basic:humidifier:targetHumidity"}
Number waterlevel "Water Level" (G_humidifier) {channel="miio:basic:humidifier:waterlevel"}
Number bright "LED Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
Switch buzzer "Buzzer Status" (G_humidifier) {channel="miio:basic:humidifier:buzzer"}
Switch dry "Dry" (G_humidifier) {channel="miio:basic:humidifier:dry"}
Number:Time usedhours "Run Time" (G_humidifier) {channel="miio:basic:humidifier:usedhours"}
Number:Time powerhours "Power Time" (G_humidifier) {channel="miio:basic:humidifier:powerhours"}
Number targetmotorspeed "Target Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:targetmotorspeed"}
Number actualmotorspeed "Actual Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:actualmotorspeed"}
Number:Temperature temperature "Temperature" (G_humidifier) {channel="miio:basic:humidifier:temperature"}
Switch childlock "Child Lock" (G_humidifier) {channel="miio:basic:humidifier:childlock"}
Number ButtonPressed "Button Pressed" (G_humidifier) {channel="miio:basic:humidifier:ButtonPressed"}
Switch clean "Clean Mode" (G_humidifier) {channel="miio:basic:humidifier:clean"}
Number countryCode "Country Code" (G_humidifier) {channel="miio:basic:humidifier:countryCode"}
```

### Smartmi Evaporative Humidifier (zhimi.humidifier.cb1) item file lines

note: Autogenerated example. Replace the id (humidifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_humidifier "Smartmi Evaporative Humidifier" <status>
Switch power "Power" (G_humidifier) {channel="miio:basic:humidifier:power"}
String humidifierMode "Humidifier Mode" (G_humidifier) {channel="miio:basic:humidifier:humidifierMode"}
Number humidity "Humidity" (G_humidifier) {channel="miio:basic:humidifier:humidity"}
Number setHumidity "Humidity Set" (G_humidifier) {channel="miio:basic:humidifier:setHumidity"}
Number bright "LED Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
Switch buzzer "Buzzer Status" (G_humidifier) {channel="miio:basic:humidifier:buzzer"}
Number depth "Depth" (G_humidifier) {channel="miio:basic:humidifier:depth"}
Switch dry "Dry" (G_humidifier) {channel="miio:basic:humidifier:dry"}
Number:Time usedhours "Run Time" (G_humidifier) {channel="miio:basic:humidifier:usedhours"}
Number motorspeed "Motor Speed" (G_humidifier) {channel="miio:basic:humidifier:motorspeed"}
Number:Temperature temperature "Temperature" (G_humidifier) {channel="miio:basic:humidifier:temperature"}
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number motorspeed2 "Motor Speed 2" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed2"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier 2 (zhimi.airpurifier.m2) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 2" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier 2S (zhimi.airpurifier.ma1) item file lines

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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier 2S (zhimi.airpurifier.ma2) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 2S" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Number bright "LED Brightness" (G_airpurifier) {channel="miio:basic:airpurifier:bright"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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

### Mi Air Purifier 3/3H (zhimi.airpurifier.mb3) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 3/3H" <status>
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier MAX / MAX Pro (zhimi.airpurifier.sa2) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier MAX / MAX Pro" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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

### Mi Fresh Air Ventilator A1-150 (dmaker.airfresh.a1) item file lines

note: Autogenerated example. Replace the id (airfresh) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airfresh "Mi Fresh Air Ventilator A1-150" <status>
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

### Gosund Smart Plug (cuco.plug.cp1) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Gosund Smart Plug" <status>
String FirmwareRevision "Device Information-CurrentFirmware Version" (G_plug) {channel="miio:basic:plug:FirmwareRevision"}
String Manufacturer "Device Information-Device Manufacturer" (G_plug) {channel="miio:basic:plug:Manufacturer"}
String Model "Device Information-Device Model" (G_plug) {channel="miio:basic:plug:Model"}
String SerialNumber "Device Information-Device Serial Number" (G_plug) {channel="miio:basic:plug:SerialNumber"}
Switch On "Switch-Switch Status" (G_plug) {channel="miio:basic:plug:On"}
```

### Mi Robot Vacuum Mop 1C STYTJ01ZHM (dreame.vacuum.mc1808) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Mi Robot Vacuum Mop 1C STYTJ01ZHM" <status>
String vacuumaction "Vacuum Action" (G_vacuum) {channel="miio:basic:vacuum:vacuumaction"}
Number BatteryLevel "Battery-Battery Level" (G_vacuum) {channel="miio:basic:vacuum:BatteryLevel"}
Number ChargingState "Battery-Charging State" (G_vacuum) {channel="miio:basic:vacuum:ChargingState"}
Number Fault "Robot Cleaner-Device Fault" (G_vacuum) {channel="miio:basic:vacuum:Fault"}
Number Status "Robot Cleaner-Status" (G_vacuum) {channel="miio:basic:vacuum:Status"}
Number:Time BrushLeftTime "Main Cleaning Brush-Brush Left Time" (G_vacuum) {channel="miio:basic:vacuum:BrushLeftTime"}
Number BrushLifeLevel "Main Cleaning Brush-Brush Life Level" (G_vacuum) {channel="miio:basic:vacuum:BrushLifeLevel"}
Number FilterLifeLevel "Filter - Filter Life Level" (G_vacuum) {channel="miio:basic:vacuum:FilterLifeLevel"}
Number:Time FilterLeftTime "Filter-Filter Left Time" (G_vacuum) {channel="miio:basic:vacuum:FilterLeftTime"}
Number:Time BrushLeftTime1 "Side Cleaning Brush-Brush Left Time" (G_vacuum) {channel="miio:basic:vacuum:BrushLeftTime1"}
Number BrushLifeLevel1 "Side Cleaning Brush-Brush Life Level" (G_vacuum) {channel="miio:basic:vacuum:BrushLifeLevel1"}
Number WorkMode "clean-workmode" (G_vacuum) {channel="miio:basic:vacuum:WorkMode"}
String Area "clean-area" (G_vacuum) {channel="miio:basic:vacuum:Area"}
String Timer "clean-timer" (G_vacuum) {channel="miio:basic:vacuum:Timer"}
Number Mode "clean-mode" (G_vacuum) {channel="miio:basic:vacuum:Mode"}
Number:Time TotalCleanTime "Clean - Total Clean Time" (G_vacuum) {channel="miio:basic:vacuum:TotalCleanTime"}
Number TotalCleanTimes "Clean - Total Clean Times" (G_vacuum) {channel="miio:basic:vacuum:TotalCleanTimes"}
Number:Area TotalCleanArea "Clean - Total Clean Area" (G_vacuum) {channel="miio:basic:vacuum:TotalCleanArea"}
Number CleanLogStartTime "Clean - Clean Log Start Time" (G_vacuum) {channel="miio:basic:vacuum:CleanLogStartTime"}
Number ButtonLed "Clean - Button Led" (G_vacuum) {channel="miio:basic:vacuum:ButtonLed"}
Number TaskDone "Clean - Task Done" (G_vacuum) {channel="miio:basic:vacuum:TaskDone"}
String LifeSieve "Consumable - Life Sieve" (G_vacuum) {channel="miio:basic:vacuum:LifeSieve"}
String LifeBrushSide "Consumable - Life Brush Side" (G_vacuum) {channel="miio:basic:vacuum:LifeBrushSide"}
String LifeBrushMain "Consumable - Life Brush Main" (G_vacuum) {channel="miio:basic:vacuum:LifeBrushMain"}
Switch Enable "Annoy - Enable" (G_vacuum) {channel="miio:basic:vacuum:Enable"}
String StartTime "Annoy - Start Time" (G_vacuum) {channel="miio:basic:vacuum:StartTime"}
String StopTime "Annoy - Stop Time" (G_vacuum) {channel="miio:basic:vacuum:StopTime"}
String MapView "Map - Map View" (G_vacuum) {channel="miio:basic:vacuum:MapView"}
Number Volume "Audio - Volume" (G_vacuum) {channel="miio:basic:vacuum:Volume"}
String VoicePackets "Audio - Voice Packets" (G_vacuum) {channel="miio:basic:vacuum:VoicePackets"}
String TimeZone "Time - Time Zone" (G_vacuum) {channel="miio:basic:vacuum:TimeZone"}
```

### Dreame Robot Vacuum-Mop F9 (dreame.vacuum.p2008) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Dreame Robot Vacuum-Mop F9" <status>
Number status "Robot Cleaner - Status" (G_vacuum) {channel="miio:basic:vacuum:status"}
Number fault "Robot Cleaner - Device Fault" (G_vacuum) {channel="miio:basic:vacuum:fault"}
Number battery-level "Battery - Battery Level" (G_vacuum) {channel="miio:basic:vacuum:battery-level"}
Number charging-state "Battery - Charging State" (G_vacuum) {channel="miio:basic:vacuum:charging-state"}
Number:Time brush-left-time "Main Cleaning Brush - Brush Left Time" (G_vacuum) {channel="miio:basic:vacuum:brush-left-time"}
Number:Dimensionless brush-life-level "Main Cleaning Brush - Brush Life Level" (G_vacuum) {channel="miio:basic:vacuum:brush-life-level"}
Number:Time brush-left-time1 "Side Cleaning Brush - Brush Left Time" (G_vacuum) {channel="miio:basic:vacuum:brush-left-time1"}
Number:Dimensionless brush-life-level1 "Side Cleaning Brush - Brush Life Level" (G_vacuum) {channel="miio:basic:vacuum:brush-life-level1"}
Number:Dimensionless filter-life-level "Filter - Filter Life Level" (G_vacuum) {channel="miio:basic:vacuum:filter-life-level"}
Number:Time filter-left-time "Filter - Filter Left Time" (G_vacuum) {channel="miio:basic:vacuum:filter-left-time"}
Number work-mode "Vacuum Extend - Work Mode" (G_vacuum) {channel="miio:basic:vacuum:work-mode"}
Number:Time cleaning-time "Vacuum Extend - Cleaning Time" (G_vacuum) {channel="miio:basic:vacuum:cleaning-time"}
Number:Area cleaning-area "Vacuum Extend - Cleaning Area" (G_vacuum) {channel="miio:basic:vacuum:cleaning-area"}
Number cleaning-mode "Vacuum Extend - Cleaning Mode" (G_vacuum) {channel="miio:basic:vacuum:cleaning-mode"}
Number mop-mode "Vacuum Extend - Mop Mode" (G_vacuum) {channel="miio:basic:vacuum:mop-mode"}
Number waterbox-status "Vacuum Extend - Waterbox Status" (G_vacuum) {channel="miio:basic:vacuum:waterbox-status"}
Number task-status "Vacuum Extend - Task Status" (G_vacuum) {channel="miio:basic:vacuum:task-status"}
Number break-point-restart "Vacuum Extend - Break Point Restart" (G_vacuum) {channel="miio:basic:vacuum:break-point-restart"}
Number carpet-press "Vacuum Extend - Carpet Press" (G_vacuum) {channel="miio:basic:vacuum:carpet-press"}
Switch enable "Do Not Disturb - Enable" (G_vacuum) {channel="miio:basic:vacuum:enable"}
String start-time "Do Not Disturb - Start Time" (G_vacuum) {channel="miio:basic:vacuum:start-time"}
String end-time "Do Not Disturb - End Time" (G_vacuum) {channel="miio:basic:vacuum:end-time"}
Number volume "Audio - Volume" (G_vacuum) {channel="miio:basic:vacuum:volume"}
String voice-packet-id "Audio - Voice Packet Id" (G_vacuum) {channel="miio:basic:vacuum:voice-packet-id"}
String voice-change-state "Audio - Voice Change State" (G_vacuum) {channel="miio:basic:vacuum:voice-change-state"}
String time-zone "Time - Time Zone" (G_vacuum) {channel="miio:basic:vacuum:time-zone"}
String timer-clean "Time - Timer Clean" (G_vacuum) {channel="miio:basic:vacuum:timer-clean"}
Number first-clean-time "Clean Logs - First Clean Time" (G_vacuum) {channel="miio:basic:vacuum:first-clean-time"}
Number:Time total-clean-time "Clean Logs - Total Clean Time" (G_vacuum) {channel="miio:basic:vacuum:total-clean-time"}
Number total-clean-times "Clean Logs - Total Clean Times" (G_vacuum) {channel="miio:basic:vacuum:total-clean-times"}
Number total-clean-area "Clean Logs - Total Clean Area" (G_vacuum) {channel="miio:basic:vacuum:total-clean-area"}
Number save-map-status "Vslam Extend - Save Map Status" (G_vacuum) {channel="miio:basic:vacuum:save-map-status"}
```

### Mi Air Purifier 2S (zhimi.airpurifier.mb1) item file lines

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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
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
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Air Purifier 2H (zhimi.airpurifier.mc2) item file lines

note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 2H" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "LED Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Switch buzzer "Buzzer Status" (G_airpurifier) {channel="miio:basic:airpurifier:buzzer"}
Number filtermaxlife "Filter Max Life" (G_airpurifier) {channel="miio:basic:airpurifier:filtermaxlife"}
Number:Time filterhours "Filter Hours used" (G_airpurifier) {channel="miio:basic:airpurifier:filterhours"}
Number:Time usedhours "Run Time" (G_airpurifier) {channel="miio:basic:airpurifier:usedhours"}
Number motorspeed "Motor Speed" (G_airpurifier) {channel="miio:basic:airpurifier:motorspeed"}
Number filterlife "Filter Life" (G_airpurifier) {channel="miio:basic:airpurifier:filterlife"}
Number favoritelevel "Favorite Level" (G_airpurifier) {channel="miio:basic:airpurifier:favoritelevel"}
Number:Temperature temperature "Temperature" (G_airpurifier) {channel="miio:basic:airpurifier:temperature"}
Number purifyvolume "Purivied Volume" (G_airpurifier) {channel="miio:basic:airpurifier:purifyvolume"}
Switch childlock "Child Lock" (G_airpurifier) {channel="miio:basic:airpurifier:childlock"}
```

### Mi Smart Fan (zhimi.fan.v1) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number:Time usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
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

### Smartmi DC Pedestal Fan (zhimi.fan.v2) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Smartmi DC Pedestal Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number:Time usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
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

### Smartmi DC Pedestal Fan (zhimi.fan.v3) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Smartmi DC Pedestal Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number:Time usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
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

### Mi Standing Fan (zhimi.fan.sa1) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Standing Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number:Time usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
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

### Smartmi Inverter Pedestal Fan (zhimi.fan.za1) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Smartmi Inverter Pedestal Fan" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number:Time usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
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

### Smartmi Standing Fan 2 (zhimi.fan.za3) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Smartmi Standing Fan 2" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number:Time usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
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

### Smartmi Standing Fan 2S (zhimi.fan.za4) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Smartmi Standing Fan 2S" <status>
Switch power "Power" (G_fan) {channel="miio:basic:fan:power"}
Switch angleEnable "Rotation" (G_fan) {channel="miio:basic:fan:angleEnable"}
Number:Time usedhours "Run Time" (G_fan) {channel="miio:basic:fan:usedhours"}
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

### Mi Smart Standing Fan 2 Lite (dmaker.fan.1c) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Standing Fan 2 Lite" <status>
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

### Mi Smart Standing Fan 1X (dmaker.fan.p5) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Standing Fan 1X" <status>
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

### Mi Smart Standing Fan 1C (dmaker.fan.p8) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Standing Fan 1C" <status>
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

### Mi Smart Tower Fan (dmaker.fan.p9) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Tower Fan" <status>
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

### Mi Smart Standing Fan 2 (dmaker.fan.p10) item file lines

note: Autogenerated example. Replace the id (fan) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_fan "Mi Smart Standing Fan 2" <status>
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

### Mi smart Home Gateway Hub v1 (lumi.gateway.v1) item file lines

note: Autogenerated example. Replace the id (gateway) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_gateway "Mi smart Home Gateway Hub v1" <status>
Switch telnetEnable "Enable Telnet" (G_gateway) {channel="miio:basic:gateway:telnetEnable"}
Number doorbellVol "Doorbell Volume" (G_gateway) {channel="miio:basic:gateway:doorbellVol"}
Number gatewayVol "Gateway Volume" (G_gateway) {channel="miio:basic:gateway:gatewayVol"}
Number alarmingVol "Alarming Volume" (G_gateway) {channel="miio:basic:gateway:alarmingVol"}
String doorbellPush "Doorbell Push" (G_gateway) {channel="miio:basic:gateway:doorbellPush"}
```

### Mi smart Home GatewayHub v2 (lumi.gateway.v2) item file lines

note: Autogenerated example. Replace the id (gateway) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_gateway "Mi smart Home GatewayHub v2" <status>
Switch telnetEnable "Enable Telnet" (G_gateway) {channel="miio:basic:gateway:telnetEnable"}
Number doorbellVol "Doorbell Volume" (G_gateway) {channel="miio:basic:gateway:doorbellVol"}
Number gatewayVol "Gateway Volume" (G_gateway) {channel="miio:basic:gateway:gatewayVol"}
Number alarmingVol "Alarming Volume" (G_gateway) {channel="miio:basic:gateway:alarmingVol"}
String doorbellPush "Doorbell Push" (G_gateway) {channel="miio:basic:gateway:doorbellPush"}
```

### Mi mart Home Gateway Hub v3 (lumi.gateway.v3) item file lines

note: Autogenerated example. Replace the id (gateway) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_gateway "Mi mart Home Gateway Hub v3" <status>
Switch telnetEnable "Enable Telnet" (G_gateway) {channel="miio:basic:gateway:telnetEnable"}
Number doorbellVol "Doorbell Volume" (G_gateway) {channel="miio:basic:gateway:doorbellVol"}
Number gatewayVol "Gateway Volume" (G_gateway) {channel="miio:basic:gateway:gatewayVol"}
Number alarmingVol "Alarming Volume" (G_gateway) {channel="miio:basic:gateway:alarmingVol"}
String doorbellPush "Doorbell Push" (G_gateway) {channel="miio:basic:gateway:doorbellPush"}
```

### Mi Air Purifier virtual (lumi.gateway.mgl03) item file lines

note: Autogenerated example. Replace the id (gateway) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_gateway "Mi Air Purifier virtual" <status>
Switch telnetEnable "Enable Telnet" (G_gateway) {channel="miio:basic:gateway:telnetEnable"}
Number doorbellVol "Doorbell Volume" (G_gateway) {channel="miio:basic:gateway:doorbellVol"}
Number gatewayVol "Gateway Volume" (G_gateway) {channel="miio:basic:gateway:gatewayVol"}
Number alarmingVol "Alarming Volume" (G_gateway) {channel="miio:basic:gateway:alarmingVol"}
String doorbellPush "Doorbell Push" (G_gateway) {channel="miio:basic:gateway:doorbellPush"}
```

### HUIZUO ARIES For Bedroom (huayi.light.ari013) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO ARIES For Bedroom" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO ARIES For Living Room (huayi.light.aries) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO ARIES For Living Room" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO Fan Light (huayi.light.fanwy) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO Fan Light" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
Switch on1 "Fan - Power" (G_light) {channel="miio:basic:light:on1"}
Number:Dimensionless fan-level "Fan - Fan Level" (G_light) {channel="miio:basic:light:fan-level"}
Switch motor-reverse "Fan - Motor Reverse" (G_light) {channel="miio:basic:light:motor-reverse"}
Number mode "Fan - Mode" (G_light) {channel="miio:basic:light:mode"}
```

### HUIZUO Fan Light(2020) (huayi.light.fanwy2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO Fan Light(2020)" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
Switch on1 "Fan - Power" (G_light) {channel="miio:basic:light:on1"}
Number:Dimensionless fan-level "Fan - Fan Level" (G_light) {channel="miio:basic:light:fan-level"}
Number mode "Fan - Mode" (G_light) {channel="miio:basic:light:mode"}
Number:Time time-off "Presets - Time Off" (G_light) {channel="miio:basic:light:time-off"}
Number:Temperature pre-colortemp "Presets - Pre Colortemp" (G_light) {channel="miio:basic:light:pre-colortemp"}
Number:Dimensionless pre-brightness "Presets - Pre Brightness" (G_light) {channel="miio:basic:light:pre-brightness"}
Number:Dimensionless pre-speed "Presets - Pre Speed" (G_light) {channel="miio:basic:light:pre-speed"}
Number reversal "Presets - Reversal" (G_light) {channel="miio:basic:light:reversal"}
Number pre-custom "Presets - Pre Custom" (G_light) {channel="miio:basic:light:pre-custom"}
Switch flabellum "Presets - Flabellum" (G_light) {channel="miio:basic:light:flabellum"}
```

### HUIZUO PEGASUS For Living Room (huayi.light.peg091) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO PEGASUS For Living Room" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO PEGASUS For Bedroom (huayi.light.peg093) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO PEGASUS For Bedroom" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO PISCES For Bedroom (huayi.light.pis123) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO PISCES For Bedroom" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO PISCES For Living Room (huayi.light.pisces) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO PISCES For Living Room" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO TAURUS For Bedroom (huayi.light.tau023) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO TAURUS For Bedroom" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO TAURUS For Living Room (huayi.light.taurus) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO TAURUS For Living Room" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO VIRGO For Bedroom (huayi.light.vir063) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO VIRGO For Bedroom" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO VIRGO For Living Room (huayi.light.virgo) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO VIRGO For Living Room" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO Ceiling Light (huayi.light.wy) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO Ceiling Light" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO LIANGCHEN(BLE Mesh) (huayi.light.wy200) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO LIANGCHEN(BLE Mesh)" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO SAG Downlight (BLE Mesh) (huayi.light.wy201) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO SAG Downlight (BLE Mesh)" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO Bulb (BLE Mesh) (huayi.light.wy202) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO Bulb (BLE Mesh)" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO YONG Downlight (BLE Mesh) (huayi.light.wy203) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO YONG Downlight (BLE Mesh)" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### huayi.light.wy204 (huayi.light.wy204) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "huayi.light.wy204" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### HUIZUO Heating Lamp (huayi.light.wyheat) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO Heating Lamp" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
Switch on1 "Heater - Power" (G_light) {channel="miio:basic:light:on1"}
Number fault "Heater - Device Fault" (G_light) {channel="miio:basic:light:fault"}
Number heat-level "Heater - Heat Level" (G_light) {channel="miio:basic:light:heat-level"}
Switch screenshow "Other - Screenshow" (G_light) {channel="miio:basic:light:screenshow"}
```

### HUIZUO ZIWEI Ceiling Lamp (huayi.light.zw131) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "HUIZUO ZIWEI Ceiling Lamp" <status>
Switch on "Light - Power" (G_light) {channel="miio:basic:light:on"}
Dimmer brightness "Light - Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Temperature color-temperature "Light - Color Temperature" (G_light) {channel="miio:basic:light:color-temperature"}
```

### Xiaomiyoupin Curtain Controller (Wi-Fi) (lumi.curtain.hagl05) item file lines

note: Autogenerated example. Replace the id (curtain) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_curtain "Xiaomiyoupin Curtain Controller (Wi-Fi)" <status>
Number fault "Curtain - Device Fault" (G_curtain) {channel="miio:basic:curtain:fault"}
Number current-position "Curtain - Current Position" (G_curtain) {channel="miio:basic:curtain:current-position"}
Number status "Curtain - Status" (G_curtain) {channel="miio:basic:curtain:status"}
Number target-position "Curtain - Target Position" (G_curtain) {channel="miio:basic:curtain:target-position"}
Number manual-enabled "curtain_cfg - Manual Enabled" (G_curtain) {channel="miio:basic:curtain:manual-enabled"}
Number polarity "curtain_cfg - Polarity" (G_curtain) {channel="miio:basic:curtain:polarity"}
Number pos-limit "curtain_cfg - Position Limit" (G_curtain) {channel="miio:basic:curtain:pos-limit"}
Switch en-night-tip-light "Set Night Tip Light" (G_curtain) {channel="miio:basic:curtain:en-night-tip-light"}
Number run-time "curtain_cfg - Run-time" (G_curtain) {channel="miio:basic:curtain:run-time"}
```

### MR.BOND (mrbond.airer.m1pro) item file lines

note: Autogenerated example. Replace the id (airer) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airer "MR.BOND" <status>
Switch dry "Dry" (G_airer) {channel="miio:basic:airer:dry"}
Switch led "LED Status" (G_airer) {channel="miio:basic:airer:led"}
Number motor "Motor" (G_airer) {channel="miio:basic:airer:motor"}
Number drytime "Dry Time" (G_airer) {channel="miio:basic:airer:drytime"}
Number airer_location "Airer Location" (G_airer) {channel="miio:basic:airer:airer_location"}
Switch disinfect "disinfect" (G_airer) {channel="miio:basic:airer:disinfect"}
Number distime "Disinfect Time" (G_airer) {channel="miio:basic:airer:distime"}
```

### MR.BOND (mrbond.airer.m1s) item file lines

note: Autogenerated example. Replace the id (airer) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airer "MR.BOND" <status>
Switch dry "Dry" (G_airer) {channel="miio:basic:airer:dry"}
Switch led "LED Status" (G_airer) {channel="miio:basic:airer:led"}
Number motor "Motor" (G_airer) {channel="miio:basic:airer:motor"}
Number drytime "Dry Time" (G_airer) {channel="miio:basic:airer:drytime"}
Number airer_location "Airer Location" (G_airer) {channel="miio:basic:airer:airer_location"}
Switch disinfect "disinfect" (G_airer) {channel="miio:basic:airer:disinfect"}
Number distime "Disinfect Time" (G_airer) {channel="miio:basic:airer:distime"}
```

### MR.BOND (mrbond.airer.m1super) item file lines

note: Autogenerated example. Replace the id (airer) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airer "MR.BOND" <status>
Switch dry "Dry" (G_airer) {channel="miio:basic:airer:dry"}
Switch led "LED Status" (G_airer) {channel="miio:basic:airer:led"}
Number motor "Motor" (G_airer) {channel="miio:basic:airer:motor"}
Number drytime "Dry Time" (G_airer) {channel="miio:basic:airer:drytime"}
Number airer_location "Airer Location" (G_airer) {channel="miio:basic:airer:airer_location"}
Switch disinfect "disinfect" (G_airer) {channel="miio:basic:airer:disinfect"}
Number distime "Disinfect Time" (G_airer) {channel="miio:basic:airer:distime"}
```

### Mi Robot Vacuum-Mop Essential (mijia.vacuum.v2) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Mi Robot Vacuum-Mop Essential" <status>
String vacuumaction "Vacuum Action" (G_vacuum) {channel="miio:basic:vacuum:vacuumaction"}
Number status "Robot Cleaner - Status" (G_vacuum) {channel="miio:basic:vacuum:status"}
Number fault "Robot Cleaner - Device Fault" (G_vacuum) {channel="miio:basic:vacuum:fault"}
Number mode "Robot Cleaner - Mode" (G_vacuum) {channel="miio:basic:vacuum:mode"}
Number target-water-level "Robot Cleaner - Target Water Level" (G_vacuum) {channel="miio:basic:vacuum:target-water-level"}
Number fan-level "Robot Cleaner - Fan Level" (G_vacuum) {channel="miio:basic:vacuum:fan-level"}
Number battery-level "Battery - Battery Level" (G_vacuum) {channel="miio:basic:vacuum:battery-level"}
Number charging-state "Battery - Charging State" (G_vacuum) {channel="miio:basic:vacuum:charging-state"}
Switch alarm "Alarm - Alarm" (G_vacuum) {channel="miio:basic:vacuum:alarm"}
Number volume "Alarm - Volume" (G_vacuum) {channel="miio:basic:vacuum:volume"}
Number filter-life-level "Filter - Filter Life Level" (G_vacuum) {channel="miio:basic:vacuum:filter-life-level"}
Number filter-left-time "Filter - Filter Left Time" (G_vacuum) {channel="miio:basic:vacuum:filter-left-time"}
Number brush-life-level "Brush Cleaner - Brush Life Level" (G_vacuum) {channel="miio:basic:vacuum:brush-life-level"}
Number brush-left-time "Brush Cleaner - Brush Left Time" (G_vacuum) {channel="miio:basic:vacuum:brush-left-time"}
Number brush-life-level1 "Brush Cleaner - Brush Life Level" (G_vacuum) {channel="miio:basic:vacuum:brush-life-level1"}
Number brush-left-time1 "Brush Cleaner - Brush Left Time" (G_vacuum) {channel="miio:basic:vacuum:brush-left-time1"}
Number:Area clean-area "Clean Record - Clean Area" (G_vacuum) {channel="miio:basic:vacuum:clean-area"}
Number:Time clean-time "Clean Record - Clean Time" (G_vacuum) {channel="miio:basic:vacuum:clean-time"}
Number total-clean-area "Clean Record - Total Clean Area" (G_vacuum) {channel="miio:basic:vacuum:total-clean-area"}
Number total-clean-time "Clean Record - Total Clean Time" (G_vacuum) {channel="miio:basic:vacuum:total-clean-time"}
Number total-clean-count "Clean Record - Total Clean Count" (G_vacuum) {channel="miio:basic:vacuum:total-clean-count"}
Number language "Language - Language" (G_vacuum) {channel="miio:basic:vacuum:language"}
Switch not-disturb-switch "Language - Not Disturb Switch" (G_vacuum) {channel="miio:basic:vacuum:not-disturb-switch"}
Number mop-status "Other Status - Mop Status" (G_vacuum) {channel="miio:basic:vacuum:mop-status"}
```

### SCISHARE Smart Capsule Coffee Machine (scishare.coffee.s1102) item file lines

note: Autogenerated example. Replace the id (coffee) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_coffee "SCISHARE Smart Capsule Coffee Machine" <status>
Switch power "Power" (G_coffee) {channel="miio:basic:coffee:power"}
String Status "status" (G_coffee) {channel="miio:basic:coffee:Status"}
Number expresso "Brew Expresso" (G_coffee) {channel="miio:basic:coffee:expresso"}
Number expresso "Brew Americano" (G_coffee) {channel="miio:basic:coffee:expresso"}
Number boil "Boil water" (G_coffee) {channel="miio:basic:coffee:boil"}
```

### Xiaomi Scishare smart capsule coffee machine (scishare.coffee.s1301) item file lines

note: Autogenerated example. Replace the id (coffee) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_coffee "Xiaomi Scishare smart capsule coffee machine" <status>
Switch power "Power" (G_coffee) {channel="miio:basic:coffee:power"}
String Status "status" (G_coffee) {channel="miio:basic:coffee:Status"}
Number expresso "Brew Expresso" (G_coffee) {channel="miio:basic:coffee:expresso"}
Number expresso "Brew Americano" (G_coffee) {channel="miio:basic:coffee:expresso"}
Number boil "Boil water" (G_coffee) {channel="miio:basic:coffee:boil"}
```

### Philips EyeCare connected desk lamp gen2. (philips.light.sread1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips EyeCare connected desk lamp gen2." <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Switch ambientPower "Ambient Power" (G_light) {channel="miio:basic:light:ambientPower"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number illumination "Ambient Illumination" (G_light) {channel="miio:basic:light:illumination"}
Switch eyecare "Eyecare" (G_light) {channel="miio:basic:light:eyecare"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
```

### Mijia Philips Desk Lamp 2S (philips.light.sread2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mijia Philips Desk Lamp 2S" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Switch ambientPower "Ambient Power" (G_light) {channel="miio:basic:light:ambientPower"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number illumination "Ambient Illumination" (G_light) {channel="miio:basic:light:illumination"}
Switch eyecare "Eyecare" (G_light) {channel="miio:basic:light:eyecare"}
Switch bl "Night Light" (G_light) {channel="miio:basic:light:bl"}
```

### Philips connected ceiling (philips.light.ceiling) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips connected ceiling" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Philips ZhiYi ceiling lamp (philips.light.zyceiling) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiYi ceiling lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Philips ZhiRui E27 bulb (philips.light.bulb) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiRui E27 bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Philips Wi-Fi bulb E27 White (philips.light.hbulb) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Wi-Fi bulb E27 White" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Philips ZhiRui E14 candle lamp Frosted version (philips.light.candle) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiRui E14 candle lamp Frosted version" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Philips ZhiRui downlight (philips.light.downlight) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiRui downlight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Philips ZhiRui bedside lamp (philips.light.moonlight) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiRui bedside lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch gonight "Go Night" (G_light) {channel="miio:basic:light:gonight"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Philips Zhirui Ceiling Lamp Bedroom 40W (philips.light.bceiling1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Zhirui Ceiling Lamp Bedroom 40W" <status>
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

### Philips Zhirui Ceiling Lamp Bedroom 28W (philips.light.bceiling2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Zhirui Ceiling Lamp Bedroom 28W" <status>
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

### Mijia Philips Color Bulb (philips.light.cbulb) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mijia Philips Color Bulb" <status>
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

### Mijia Philips Study Desk Lamp (philips.light.rwread) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mijia Philips Study Desk Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number flm "Follow Me" (G_light) {channel="miio:basic:light:flm"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
```

### Philips ZhiYi Ceiling lamp FL 40W (philips.light.lnblight1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiYi Ceiling lamp FL 40W" <status>
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

### Philips ZhiYi Ceiling lamp FL 28W (philips.light.lnblight2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiYi Ceiling lamp FL 28W" <status>
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

### Philips ZhiYi Ceiling lamp FL 80W (philips.light.lnlrlight) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiYi Ceiling lamp FL 80W" <status>
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

### Philips Zhirui Ceiling Lamp Living room 80W (philips.light.lrceiling) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Zhirui Ceiling Lamp Living room 80W" <status>
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

### Philips ZhiRui E14 candle lamp crystal version (philips.light.candle2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiRui E14 candle lamp crystal version" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Philips Smart Lamp (philips.light.mono1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips Smart Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
```

### ZhiRui dimmable downlight (philips.light.dlight) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "ZhiRui dimmable downlight" <status>
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

### Zhirui Ceiling Lamp Nordic 80W (philips.light.mceil) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Nordic 80W" <status>
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

### Zhirui Ceiling Lamp Nordic 40W (philips.light.mceilm) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Nordic 40W" <status>
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

### Zhirui Ceiling Lamp Nordic 28W (philips.light.mceils) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Nordic 28W" <status>
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

### Zhirui Ceiling Lamp Black 80W (philips.light.obceil) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Black 80W" <status>
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

###  Zhirui Ceiling Lamp Black 40W (philips.light.obceim) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light " Zhirui Ceiling Lamp Black 40W" <status>
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

### Zhirui Ceiling Lamp Black 28W (philips.light.obceis) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Black 28W" <status>
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

### Zhirui Ceiling Lamp Starry 80W (philips.light.sceil) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Starry 80W" <status>
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

### Zhirui Ceiling Lamp Starry 40W (philips.light.sceilm) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Starry 40W" <status>
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

### Zhirui Ceiling Lamp Starry 28W (philips.light.sceils) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Starry 28W" <status>
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

### Zhirui Ceiling Lamp Gorgeous 80W (philips.light.xzceil) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Gorgeous 80W" <status>
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

### Zhirui Ceiling Lamp Gorgeous 40W (philips.light.xzceim) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Gorgeous 40W" <status>
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

### Zhirui Ceiling Lamp Gorgeous 28W (philips.light.xzceis) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Zhirui Ceiling Lamp Gorgeous 28W" <status>
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

### Philips connected lights (philips.light.virtual) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips connected lights" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Philips ZhiYi desk lamp (philips.light.zysread) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiYi desk lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Philips ZhiYi strip (philips.light.zystrip) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Philips ZhiYi strip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Dimmer cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
```

### Mi Plug Mini (chuangmi.plug.m1) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Plug Mini" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number:Temperature temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Indicator light" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Smart Power Plug (chuangmi.plug.v1) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Power Plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
Number:Temperature temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
```

### Mi Smart Power Plug v2 (chuangmi.plug.v2) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Power Plug v2" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
```

### MIJIA Smart  Plug Enhanced (chuangmi.plug.v3) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "MIJIA Smart  Plug Enhanced" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
Number:Temperature temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Wifi LED" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Smart Plug (Wi-Fi) Basic (chuangmi.plug.m3) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Plug (Wi-Fi) Basic" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number:Temperature temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Indicator light" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Smart Plug WiFi (chuangmi.plug.hmi205) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Plug WiFi" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number:Temperature temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Indicator light" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Smart Plug (WiFi) (chuangmi.plug.hmi206) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Plug (WiFi)" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
Number:Temperature temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Wifi LED" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Smart Wi-Fi Plug (Bluetooth Gateway) (chuangmi.plug.hmi208) item file lines

note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Wi-Fi Plug (Bluetooth Gateway)" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Switch usb "USB" (G_plug) {channel="miio:basic:plug:usb"}
Number:Temperature temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
Switch led "Wifi LED" (G_plug) {channel="miio:basic:plug:led"}
```

### CHINGMI Smart Power Strip v1 (qmi.powerstrip.v1) item file lines

note: Autogenerated example. Replace the id (powerstrip) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_powerstrip "CHINGMI Smart Power Strip v1" <status>
Switch power "Power" (G_powerstrip) {channel="miio:basic:powerstrip:power"}
Number powerUsage "Power Consumption" (G_powerstrip) {channel="miio:basic:powerstrip:powerUsage"}
Switch led "wifi LED" (G_powerstrip) {channel="miio:basic:powerstrip:led"}
Number power_price "power_price" (G_powerstrip) {channel="miio:basic:powerstrip:power_price"}
Number current "Current" (G_powerstrip) {channel="miio:basic:powerstrip:current"}
Number:Temperature temperature "Temperature" (G_powerstrip) {channel="miio:basic:powerstrip:temperature"}
Number lp_autooff "Low Power Auto Off" (G_powerstrip) {channel="miio:basic:powerstrip:lp_autooff"}
Number lp_autooff_delay "Low Power Limit Time" (G_powerstrip) {channel="miio:basic:powerstrip:lp_autooff_delay"}
Number lp_threshold "Low Power Threshold" (G_powerstrip) {channel="miio:basic:powerstrip:lp_threshold"}
```

### Mi Smart Power Strip (zimi.powerstrip.v2) item file lines

note: Autogenerated example. Replace the id (powerstrip) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_powerstrip "Mi Smart Power Strip" <status>
Switch power "Power" (G_powerstrip) {channel="miio:basic:powerstrip:power"}
Number powerUsage "Power Consumption" (G_powerstrip) {channel="miio:basic:powerstrip:powerUsage"}
Switch led "wifi LED" (G_powerstrip) {channel="miio:basic:powerstrip:led"}
Number power_price "power_price" (G_powerstrip) {channel="miio:basic:powerstrip:power_price"}
Number current "Current" (G_powerstrip) {channel="miio:basic:powerstrip:current"}
Number:Temperature temperature "Temperature" (G_powerstrip) {channel="miio:basic:powerstrip:temperature"}
Number lp_autooff "Low Power Auto Off" (G_powerstrip) {channel="miio:basic:powerstrip:lp_autooff"}
Number lp_autooff_delay "Low Power Limit Time" (G_powerstrip) {channel="miio:basic:powerstrip:lp_autooff_delay"}
Number lp_threshold "Low Power Threshold" (G_powerstrip) {channel="miio:basic:powerstrip:lp_threshold"}
```

### Viomi Cleaning Robot V-RVCLM21B (viomi.vacuum.v6) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Viomi Cleaning Robot V-RVCLM21B" <status>
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

### Mi Robot Vacuum-Mop P (viomi.vacuum.v7) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Mi Robot Vacuum-Mop P" <status>
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

### Mi Robot Vacuum-Mop P (viomi.vacuum.v8) item file lines

note: Autogenerated example. Replace the id (vacuum) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_vacuum "Mi Robot Vacuum-Mop P" <status>
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

### PTX OneKey Switch (WIFI) (090615.switch.xswitch01) item file lines

note: Autogenerated example. Replace the id (switch) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_switch "PTX OneKey Switch (WIFI)" <status>
Number switch1state "Switch 1" (G_switch) {channel="miio:basic:switch:switch1state"}
String switch1name "Switch Name 1" (G_switch) {channel="miio:basic:switch:switch1name"}
```

### PTX Twokey switch(wifi) (090615.switch.xswitch02) item file lines

note: Autogenerated example. Replace the id (switch) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_switch "PTX Twokey switch(wifi)" <status>
Number switch1state "Switch 1" (G_switch) {channel="miio:basic:switch:switch1state"}
Number switch2state "Switch 2" (G_switch) {channel="miio:basic:switch:switch2state"}
String switch1name "Switch Name 1" (G_switch) {channel="miio:basic:switch:switch1name"}
String switch2name "Switch Name 2" (G_switch) {channel="miio:basic:switch:switch2name"}
```

### PTX ThreeKey Switch (WIFI) (090615.switch.xswitch03) item file lines

note: Autogenerated example. Replace the id (switch) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_switch "PTX ThreeKey Switch (WIFI)" <status>
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
Number:Temperature temperature "Temperature" (G_waterpurifier) {channel="miio:basic:waterpurifier:temperature"}
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
Number:Temperature temperature "Temperature" (G_waterpurifier) {channel="miio:basic:waterpurifier:temperature"}
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

### Mi Water Purifier (Under sink) v3 (yunmi.waterpurifier.v3) item file lines

note: Autogenerated example. Replace the id (waterpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpurifier "Mi Water Purifier (Under sink) v3" <status>
Switch power "Power" (G_waterpurifier) {channel="miio:basic:waterpurifier:power"}
Number run_status "Run Status" (G_waterpurifier) {channel="miio:basic:waterpurifier:run_status"}
Number:Temperature temperature "Temperature" (G_waterpurifier) {channel="miio:basic:waterpurifier:temperature"}
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
Number:Temperature temperature "Temperature" (G_waterpurifier) {channel="miio:basic:waterpurifier:temperature"}
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
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Water Purifier (Under Counter) (yunmi.waterpuri.lx3) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier (Under Counter)" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Water Purifier 1A/400G Pro (yunmi.waterpuri.lx5) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier 1A/400G Pro" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Water Purifier (Under Counter) (yunmi.waterpuri.lx6) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier (Under Counter)" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Water Purifier 500G/500G Pro (yunmi.waterpuri.lx7) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier 500G/500G Pro" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Water Purifier 600G (yunmi.waterpuri.lx8) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier 600G" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Water Purifier D1 (yunmi.waterpuri.lx9) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier D1" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Water Purifier C1 (Triple Setting) (yunmi.waterpuri.lx11) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier C1 (Triple Setting)" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Water Purifier S1 (yunmi.waterpuri.lx12) item file lines

note: Autogenerated example. Replace the id (waterpuri) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_waterpuri "Mi Water Purifier S1" <status>
Switch power "Power" (G_waterpuri) {channel="miio:basic:waterpuri:power"}
Number run_status "Run Status" (G_waterpuri) {channel="miio:basic:waterpuri:run_status"}
Number:Temperature temperature "Temperature" (G_waterpuri) {channel="miio:basic:waterpuri:temperature"}
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

### Mi Inverter Air Conditioner (1.5HP) (xiaomi.aircondition.ma1) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Inverter Air Conditioner (1.5HP)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
```

### Mi Inverter Air Conditioner (1.5HP, China Energy Label Level 1) (xiaomi.aircondition.ma2) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Inverter Air Conditioner (1.5HP, China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
```

### Mi Vertical Air Conditioner (2HP) (xiaomi.aircondition.ma4) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Vertical Air Conditioner (2HP)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
```

### Mi Smart Vertical Air Conditioner C1 (2HP / Inverter / China Energy Label Level 1) (xiaomi.aircondition.ma5) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Vertical Air Conditioner C1 (2HP / Inverter / China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
```

### Mi Smart Air Conditioner C1 (1.5HP / Conventional / China Energy Label Level 3) (xiaomi.aircondition.ma6) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner C1 (1.5HP / Conventional / China Energy Label Level 3)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
```

### Mi Smart Air Conditioner C1 (1HP / Inverter / China Energy Label Level 1) (xiaomi.aircondition.ma9) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner C1 (1HP / Inverter / China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
```

### Mi Smart Ultra Electricity Saving Vertical Air Conditioner (2HP/Inverter/New China Energy Label Level 1) (xiaomi.aircondition.c10) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Ultra Electricity Saving Vertical Air Conditioner (2HP/Inverter/New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Ultra Electricity Saving Vertical Air Conditioner (3HP/Inverter/New China Energy Label Level 1) (xiaomi.aircondition.c11) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Ultra Electricity Saving Vertical Air Conditioner (3HP/Inverter/New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Air Conditioner A (1HP / Inverter / China Energy Label Level 1) (xiaomi.aircondition.mc1) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner A (1HP / Inverter / China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Air Conditioner A (1.5HP / Inverter / China Energy Label Level 1) (xiaomi.aircondition.mc2) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner A (1.5HP / Inverter / China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Air Conditioner A (1HP / Inverter / China Energy Label Level <1) (xiaomi.aircondition.mc4) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner A (1HP / Inverter / China Energy Label Level <1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Air Conditioner A (1.5HP / Inverter / China Energy Label Level <1) (xiaomi.aircondition.mc5) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner A (1.5HP / Inverter / China Energy Label Level <1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Vertical Air Conditioner A (2HP / Inverter / China Energy Label Level <1) (xiaomi.aircondition.mc6) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Vertical Air Conditioner A (2HP / Inverter / China Energy Label Level <1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Vertical Air Conditioner A (3HP / Inverter / China Energy Label Level <1) (xiaomi.aircondition.mc7) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Vertical Air Conditioner A (3HP / Inverter / China Energy Label Level <1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Ultra Electricity Saving Air Conditioner(1.5HP/Inverter/New China Energy Label Level 3) (xiaomi.aircondition.mc8) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Ultra Electricity Saving Air Conditioner(1.5HP/Inverter/New China Energy Label Level 3)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Ultra Electricity Saving Vertical Air Conditioner(2HP/Inverter/New China Energy Label Level 3) (xiaomi.aircondition.mc9) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Ultra Electricity Saving Vertical Air Conditioner(2HP/Inverter/New China Energy Label Level 3)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Air Conditioner C (1HP / Inverter / New China Energy Label Level 1) (xiaomi.aircondition.mh1) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner C (1HP / Inverter / New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Air Conditioner C (1.5HP / Inverter / New China Energy Label Level 1) (xiaomi.aircondition.mh2) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner C (1.5HP / Inverter / New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Ultra Electricity Saving Air Conditioner(1HP/Inverter/New China Energy Label Level 3) (xiaomi.aircondition.mh3) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Ultra Electricity Saving Air Conditioner(1HP/Inverter/New China Energy Label Level 3)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Air Conditioner X (1HP / Inverter / New China Energy Label Level 1) (xiaomi.aircondition.mt1) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner X (1HP / Inverter / New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Air Conditioner X (1.5HP / Inverter / New China Energy Label Level 1) (xiaomi.aircondition.mt2) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Air Conditioner X (1.5HP / Inverter / New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Gentle Breeze Air Conditioner (1HP / Inverter / New China Energy Label Level 1) (xiaomi.aircondition.mt3) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Gentle Breeze Air Conditioner (1HP / Inverter / New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Gentle Breeze Air Conditioner (1.5HP / Inverter / New China Energy Label Level 1) (xiaomi.aircondition.mt4) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Gentle Breeze Air Conditioner (1.5HP / Inverter / New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Gentle Breeze Vertical Air Conditioner (3HP / Inverter / New China Energy Label Level 1) (xiaomi.aircondition.mt5) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Gentle Breeze Vertical Air Conditioner (3HP / Inverter / New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Ultra Electricity Saving Air Conditioner (1HP/Inverter/New China Energy Label Level 1) (xiaomi.aircondition.mt7) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Ultra Electricity Saving Air Conditioner (1HP/Inverter/New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Mi Smart Ultra Electricity Saving Air Conditioner (1.5HP/Inverter/New China Energy Label Level 1) (xiaomi.aircondition.mt8) item file lines

note: Autogenerated example. Replace the id (aircondition) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_aircondition "Mi Smart Ultra Electricity Saving Air Conditioner (1.5HP/Inverter/New China Energy Label Level 1)" <status>
Switch on "Power" (G_aircondition) {channel="miio:basic:aircondition:on"}
Number mode "Air Conditioner - Mode" (G_aircondition) {channel="miio:basic:aircondition:mode"}
Number:Temperature target-temperature "Air Conditioner - Target Temperature" (G_aircondition) {channel="miio:basic:aircondition:target-temperature"}
Switch eco "Air Conditioner - Eco" (G_aircondition) {channel="miio:basic:aircondition:eco"}
Switch heater "Air Conditioner - Heater" (G_aircondition) {channel="miio:basic:aircondition:heater"}
Switch dryer "Air Conditioner - Dryer" (G_aircondition) {channel="miio:basic:aircondition:dryer"}
Switch sleep-mode "Air Conditioner - Sleep Mode" (G_aircondition) {channel="miio:basic:aircondition:sleep-mode"}
Number fan-level "Fan Control - Fan Level" (G_aircondition) {channel="miio:basic:aircondition:fan-level"}
Switch vertical-swing "Fan Control - Vertical Swing" (G_aircondition) {channel="miio:basic:aircondition:vertical-swing"}
Number:Temperature temperature "Environment - Temperature" (G_aircondition) {channel="miio:basic:aircondition:temperature"}
Switch alarm "Alarm - Alarm" (G_aircondition) {channel="miio:basic:aircondition:alarm"}
Switch on1 "Indicator Light - Switch Status" (G_aircondition) {channel="miio:basic:aircondition:on1"}
Number:Energy electricity "Power consumption accumulation in kWh" (G_aircondition) {channel="miio:basic:aircondition:electricity"}
Number elec-count "Electricity - Count" (G_aircondition) {channel="miio:basic:aircondition:elec-count"}
String clean "Maintenance - Clean" (G_aircondition) {channel="miio:basic:aircondition:clean"}
String examine "Maintenance - Examine" (G_aircondition) {channel="miio:basic:aircondition:examine"}
Number:Duration running-duration "Maintenance - Running Duration" (G_aircondition) {channel="miio:basic:aircondition:running-duration"}
Number:Dimentionless fan-percent "Fan Speed %" (G_aircondition) {channel="miio:basic:aircondition:fan-percent"}
String timer "Enhance - Timer" (G_aircondition) {channel="miio:basic:aircondition:timer"}
```

### Uclean Smart Toilet Seat (xjx.toilet.pro) item file lines

note: Autogenerated example. Replace the id (toilet) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_toilet "Uclean Smart Toilet Seat" <status>
Number seat_temp "Seat Temperature" (G_toilet) {channel="miio:basic:toilet:seat_temp"}
Number status_seatheat "Seat Status" (G_toilet) {channel="miio:basic:toilet:status_seatheat"}
Number water_temp_t "Water Temperature" (G_toilet) {channel="miio:basic:toilet:water_temp_t"}
Number fan_temp "Fan Temperature" (G_toilet) {channel="miio:basic:toilet:fan_temp"}
Number status_led "Night Light" (G_toilet) {channel="miio:basic:toilet:status_led"}
```

### Uclean smart toilet relax (xjx.toilet.relax) item file lines

note: Autogenerated example. Replace the id (toilet) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_toilet "Uclean smart toilet relax" <status>
Number seat_temp "Seat Temperature" (G_toilet) {channel="miio:basic:toilet:seat_temp"}
Number status_seatheat "Seat Status" (G_toilet) {channel="miio:basic:toilet:status_seatheat"}
Number water_temp_t "Water Temperature" (G_toilet) {channel="miio:basic:toilet:water_temp_t"}
Number fan_temp "Fan Temperature" (G_toilet) {channel="miio:basic:toilet:fan_temp"}
Number status_led "Night Light" (G_toilet) {channel="miio:basic:toilet:status_led"}
```

### Uclean smart toilet pure (xjx.toilet.pure) item file lines

note: Autogenerated example. Replace the id (toilet) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_toilet "Uclean smart toilet pure" <status>
Number seat_temp "Seat Temperature" (G_toilet) {channel="miio:basic:toilet:seat_temp"}
Number status_seatheat "Seat Status" (G_toilet) {channel="miio:basic:toilet:status_seatheat"}
Number water_temp_t "Water Temperature" (G_toilet) {channel="miio:basic:toilet:water_temp_t"}
Number fan_temp "Fan Temperature" (G_toilet) {channel="miio:basic:toilet:fan_temp"}
Number status_led "Night Light" (G_toilet) {channel="miio:basic:toilet:status_led"}
```

### Whale Spout Smart Toilet Zero (xjx.toilet.zero) item file lines

note: Autogenerated example. Replace the id (toilet) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_toilet "Whale Spout Smart Toilet Zero" <status>
Number seat_temp "Seat Temperature" (G_toilet) {channel="miio:basic:toilet:seat_temp"}
Number status_seatheat "Seat Status" (G_toilet) {channel="miio:basic:toilet:status_seatheat"}
Number water_temp_t "Water Temperature" (G_toilet) {channel="miio:basic:toilet:water_temp_t"}
Number fan_temp "Fan Temperature" (G_toilet) {channel="miio:basic:toilet:fan_temp"}
Number status_led "Night Light" (G_toilet) {channel="miio:basic:toilet:status_led"}
```

### Mi Bedside Lamp (yeelink.light.bslamp1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi Bedside Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Mi Bedside Lamp 2 (yeelink.light.bslamp2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi Bedside Lamp 2" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Bedside Lamp II (yeelink.light.bslamp3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Bedside Lamp II" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Smart Bath Heater Pro (yeelink.bhf_light.v1) item file lines

note: Autogenerated example. Replace the id (bhf_light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_bhf_light "Yeelight Smart Bath Heater Pro" <status>
Switch power "Power" (G_bhf_light) {channel="miio:basic:bhf_light:power"}
String bh_mode "Bath Heater mode" (G_bhf_light) {channel="miio:basic:bhf_light:bh_mode"}
Dimmer brightness "Brightness" (G_bhf_light) {channel="miio:basic:bhf_light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_bhf_light) {channel="miio:basic:bhf_light:delayoff"}
Number:Temperature temperature "Temperature" (G_bhf_light) {channel="miio:basic:bhf_light:temperature"}
Number nightlightBrightness "Nightlight Brightness" (G_bhf_light) {channel="miio:basic:bhf_light:nightlightBrightness"}
```

### Yeelight Smart Bath Heater (yeelink.bhf_light.v2) item file lines

note: Autogenerated example. Replace the id (bhf_light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_bhf_light "Yeelight Smart Bath Heater" <status>
Switch power "Power" (G_bhf_light) {channel="miio:basic:bhf_light:power"}
String bh_mode "Bath Heater mode" (G_bhf_light) {channel="miio:basic:bhf_light:bh_mode"}
Dimmer brightness "Brightness" (G_bhf_light) {channel="miio:basic:bhf_light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_bhf_light) {channel="miio:basic:bhf_light:delayoff"}
Number:Temperature temperature "Temperature" (G_bhf_light) {channel="miio:basic:bhf_light:temperature"}
Number nightlightBrightness "Nightlight Brightness" (G_bhf_light) {channel="miio:basic:bhf_light:nightlightBrightness"}
```

### Yeelight Ceiling Light (yeelink.light.ceiling1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Ceiling Light SE (yeelink.light.ceiling2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light SE" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Light (yeelink.light.ceiling3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Light (yeelink.light.ceiling4) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
Switch ambientPower "Ambient Power" (G_light) {channel="miio:basic:light:ambientPower"}
Color ambientColor "Ambient Color" (G_light) {channel="miio:basic:light:ambientColor"}
Number ambientColorTemperature "Ambient Color Temperature" (G_light) {channel="miio:basic:light:ambientColorTemperature"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number ambientColorMode "Ambient Color Mode" (G_light) {channel="miio:basic:light:ambientColorMode"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Ambi Lamp (yeelink.light.ceiling4.ambi) item file lines

note: Autogenerated example. Replace the id (ceiling4) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_ceiling4 "Yeelight LED Ceiling Ambi Lamp" <status>
Switch power "Power" (G_ceiling4) {channel="miio:basic:ceiling4:power"}
Dimmer brightness "Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:brightness"}
Number:Time delayoff "Shutdown Timer" (G_ceiling4) {channel="miio:basic:ceiling4:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_ceiling4) {channel="miio:basic:ceiling4:colorTemperature"}
Number colorMode "Color Mode" (G_ceiling4) {channel="miio:basic:ceiling4:colorMode"}
String name "Name" (G_ceiling4) {channel="miio:basic:ceiling4:name"}
String customScene "Set Scene" (G_ceiling4) {channel="miio:basic:ceiling4:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:nightlightBrightness"}
```

### Mi LED Ceiling Light (yeelink.light.ceiling5) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi LED Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight HaoShi LED Ceiling Lamp Pro (yeelink.light.ceiling6) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight HaoShi LED Ceiling Lamp Pro" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Haoshi Ceiling Lamp (yeelink.light.ceiling7) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Haoshi Ceiling Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### LED Ceiling Light Crystal Plus (yeelink.light.ceiling8) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "LED Ceiling Light Crystal Plus" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight HaoShi LED Ceiling Lamp Pro (yeelink.light.ceiling9) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight HaoShi LED Ceiling Lamp Pro" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Crystal Pendant Lamp (yeelink.light.ceiling10) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Crystal Pendant Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
Switch ambientPower "Ambient Power" (G_light) {channel="miio:basic:light:ambientPower"}
Color ambientColor "Ambient Color" (G_light) {channel="miio:basic:light:ambientColor"}
Number ambientColorTemperature "Ambient Color Temperature" (G_light) {channel="miio:basic:light:ambientColorTemperature"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number ambientColorMode "Ambient Color Mode" (G_light) {channel="miio:basic:light:ambientColorMode"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Ceiling Light 320 1S (yeelink.light.ceiling11) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light 320 1S" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight stylized Ceiling Light  Pro (yeelink.light.ceiling12) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight stylized Ceiling Light  Pro" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Ceiling Light (yeelink.light.ceiling13) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Ceiling Light Mini (yeelink.light.ceiling14) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light Mini" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Ceiling Light 480 1S (yeelink.light.ceiling15) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light 480 1S" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Xingyu Ceiling Light (yeelink.light.ceiling16) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Xingyu Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight ShaoHua Celing Light (yeelink.light.ceiling17) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight ShaoHua Celing Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Ceiling Light Pro (yeelink.light.ceiling18) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light Pro" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Ceiling Light Pro (yeelink.light.ceiling19) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light Pro" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Ceiling Light (yeelink.light.ceiling20) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Mi Smart LED Living Room Ceiling Light (yeelink.light.ceiling21) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi Smart LED Living Room Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Mi Smart LED Ceiling Light (yeelink.light.ceiling22) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi Smart LED Ceiling Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Mi Smart LED Ceiling Light (350mm) (yeelink.light.ceiling23) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi Smart LED Ceiling Light (350mm)" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight LED Ceiling Ambi Lamp (yeelink.light.ceiling10.ambi) item file lines

note: Autogenerated example. Replace the id (ceiling10) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_ceiling10 "Yeelight LED Ceiling Ambi Lamp" <status>
Switch power "Power" (G_ceiling10) {channel="miio:basic:ceiling10:power"}
Dimmer brightness "Brightness" (G_ceiling10) {channel="miio:basic:ceiling10:brightness"}
Number:Time delayoff "Shutdown Timer" (G_ceiling10) {channel="miio:basic:ceiling10:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_ceiling10) {channel="miio:basic:ceiling10:colorTemperature"}
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
Number:Time delayoff "Shutdown Timer" (G_ceiling19) {channel="miio:basic:ceiling19:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_ceiling19) {channel="miio:basic:ceiling19:colorTemperature"}
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
Number:Time delayoff "Shutdown Timer" (G_ceiling20) {channel="miio:basic:ceiling20:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_ceiling20) {channel="miio:basic:ceiling20:colorTemperature"}
Number colorMode "Color Mode" (G_ceiling20) {channel="miio:basic:ceiling20:colorMode"}
String name "Name" (G_ceiling20) {channel="miio:basic:ceiling20:name"}
String customScene "Set Scene" (G_ceiling20) {channel="miio:basic:ceiling20:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_ceiling20) {channel="miio:basic:ceiling20:nightlightBrightness"}
```

### Yeelight LED Bulb (Tunable) (yeelink.light.ct2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Bulb (Tunable)" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Bulb (yeelink.light.mono1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight White Bulb v2 (yeelink.light.mono2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight White Bulb v2" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight LED Filament Bulb (yeelink.light.mono5) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Filament Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Mi LED Desk Lamp (yeelink.light.lamp1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi LED Desk Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Mi Smart LED Desk Lamp Pro (yeelink.light.lamp2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi Smart LED Desk Lamp Pro" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight LED Lamp (yeelink.light.lamp3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Mi LED Desk Lamp 1S (yeelink.light.lamp4) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi LED Desk Lamp 1S" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Smart Desk Lamp Prime (yeelink.light.lamp5) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Smart Desk Lamp Prime" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.lamp6) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight LED Light Sensor Desk Lamp V1 (yeelink.light.lamp7) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Light Sensor Desk Lamp V1" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.lamp8) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Whiteglow Panel Light (yeelink.light.panel1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Whiteglow Panel Light" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yeelight Lightstrip (yeelink.light.strip1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Lightstrip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Lightstrip Plus (yeelink.light.strip2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Lightstrip Plus" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Willow LED Lightstrip (yeelink.light.strip4) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Willow LED Lightstrip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Light Group (Mi & Yeelight) (yeelink.light.virtual) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Light Group (Mi & Yeelight)" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Color Bulb (yeelink.light.color1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Color Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight LED Bulb (Color) (yeelink.light.color2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Bulb (Color)" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Mi LED Smart Bulb (White and Color) (yeelink.light.color3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Mi LED Smart Bulb (White and Color)" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight LED Bulb 1S（Color） (yeelink.light.color4) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight LED Bulb 1S（Color）" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Smart Dual Control Module (yeelink.switch.sw1) item file lines

note: Autogenerated example. Replace the id (switch) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_switch "Yeelight Smart Dual Control Module" <status>
Switch on "First Switch - Switch Status" (G_switch) {channel="miio:basic:switch:on"}
Number mode "First Switch Default - Mode" (G_switch) {channel="miio:basic:switch:mode"}
Number mode1 "First Switch - Delay" (G_switch) {channel="miio:basic:switch:mode1"}
Switch on1 "Second Switch - Switch Status" (G_switch) {channel="miio:basic:switch:on1"}
Number mode2 "Second Switch Default - Mode" (G_switch) {channel="miio:basic:switch:mode2"}
Number mode3 "Second Switch Service - Delay" (G_switch) {channel="miio:basic:switch:mode3"}
Switch interlock "Extension - Interlock" (G_switch) {channel="miio:basic:switch:interlock"}
Number flash "Extension - Flash" (G_switch) {channel="miio:basic:switch:flash"}
String rc-list "Extension - Rc List" (G_switch) {channel="miio:basic:switch:rc-list"}
```

### Yilai Ceiling Light Aiyue 480 (yilai.light.ceiling1) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yilai Ceiling Light Aiyue 480" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yilai Ceiling Lamp Hefeng 430 (yilai.light.ceiling2) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yilai Ceiling Lamp Hefeng 430" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Yilai Ceiling Lamp Hefeng Pro (yilai.light.ceiling3) item file lines

note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yilai Ceiling Lamp Hefeng Pro" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Dimmer brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number:Time delayoff "Shutdown Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number:Temperature colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
String customScene "Set Scene" (G_light) {channel="miio:basic:light:customScene"}
Number nightlightBrightness "Nightlight Brightness" (G_light) {channel="miio:basic:light:nightlightBrightness"}
```

### Smartmi Radiant Heater Smart Version (zhimi.heater.za1) item file lines

note: Autogenerated example. Replace the id (heater) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_heater "Smartmi Radiant Heater Smart Version" <status>
Switch power "Power" (G_heater) {channel="miio:basic:heater:power"}
Number:Temperature target_temperature "Target Temperature" (G_heater) {channel="miio:basic:heater:target_temperature"}
Number brightness "Brightness" (G_heater) {channel="miio:basic:heater:brightness"}
Switch buzzer "Buzzer Status" (G_heater) {channel="miio:basic:heater:buzzer"}
Number relative_humidity "Relative Humidity" (G_heater) {channel="miio:basic:heater:relative_humidity"}
Switch childlock "Child Lock" (G_heater) {channel="miio:basic:heater:childlock"}
Switch HWSwitch "HW Switch" (G_heater) {channel="miio:basic:heater:HWSwitch"}
Number:Temperature temperature "Temperature" (G_heater) {channel="miio:basic:heater:temperature"}
Number:Time usedhours "Run Time" (G_heater) {channel="miio:basic:heater:usedhours"}
```

### Smartmi Smart Convector Heater 1S (zhimi.heater.za2) item file lines

note: Autogenerated example. Replace the id (heater) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_heater "Smartmi Smart Convector Heater 1S" <status>
Number fault "Heater - Device Fault" (G_heater) {channel="miio:basic:heater:fault"}
Switch on "Heater - Power" (G_heater) {channel="miio:basic:heater:on"}
Number:Temperature target-temperature "Heater - Target Temperature" (G_heater) {channel="miio:basic:heater:target-temperature"}
Switch alarm "Alarm - Alarm" (G_heater) {channel="miio:basic:heater:alarm"}
Number:Time countdown-time "Countdown - Countdown Time" (G_heater) {channel="miio:basic:heater:countdown-time"}
Number relative-humidity "Environment - Relative Humidity" (G_heater) {channel="miio:basic:heater:relative-humidity"}
Number:Temperature temperature "Environment - Temperature" (G_heater) {channel="miio:basic:heater:temperature"}
Dimmer brightness "Indicator Light - Brightness" (G_heater) {channel="miio:basic:heater:brightness"}
Switch physical-controls-locked "Physical Control Locked - Physical Controls Locked" (G_heater) {channel="miio:basic:heater:physical-controls-locked"}
Number:Time use-time "Private-Service - Use Time" (G_heater) {channel="miio:basic:heater:use-time"}
```

### Smartmi Smart Convector Heater 1S (zhimi.heater.zb1) item file lines

note: Autogenerated example. Replace the id (heater) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_heater "Smartmi Smart Convector Heater 1S" <status>
Number fault "Heater - Device Fault" (G_heater) {channel="miio:basic:heater:fault"}
Switch on "Heater - Power" (G_heater) {channel="miio:basic:heater:on"}
Number:Temperature target-temperature "Heater - Target Temperature" (G_heater) {channel="miio:basic:heater:target-temperature"}
Switch alarm "Alarm - Alarm" (G_heater) {channel="miio:basic:heater:alarm"}
Number:Time countdown-time "Countdown - Countdown Time" (G_heater) {channel="miio:basic:heater:countdown-time"}
Number relative-humidity "Environment - Relative Humidity" (G_heater) {channel="miio:basic:heater:relative-humidity"}
Number:Temperature temperature "Environment - Temperature" (G_heater) {channel="miio:basic:heater:temperature"}
Dimmer brightness "Indicator Light - Brightness" (G_heater) {channel="miio:basic:heater:brightness"}
Switch physical-controls-locked "Physical Control Locked - Physical Controls Locked" (G_heater) {channel="miio:basic:heater:physical-controls-locked"}
Number:Time use-time "Private-Service - Use Time" (G_heater) {channel="miio:basic:heater:use-time"}
Number country-code "Private-Service - Country-Code" (G_heater) {channel="miio:basic:heater:country-code"}
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