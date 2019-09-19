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

## Mi IO Devices

| Device                       | ThingType        | Device Model           | Supported | Remark     |
|------------------------------|------------------|------------------------|-----------|------------|
| AUX Air Conditioner          | miio:unsupported | aux.aircondition.v1    | No        |           |
| Idelan Air Conditioner       | miio:unsupported | idelan.aircondition.v1 | No        |           |
| Midea Air Conditioner v2     | miio:unsupported | midea.aircondition.v1  | No        |           |
| Midea Air Conditioner v2     | miio:unsupported | midea.aircondition.v2  | No        |           |
| Midea Air Conditioner xa1    | miio:unsupported | midea.aircondition.xa1 | No        |           |
| Mi Air Monitor v1            | miio:basic       | [zhimi.airmonitor.v1](#zhimi-airmonitor-v1) | Yes       |           |
| Mi Air Quality Monitor 2gen  | miio:basic       | [cgllc.airmonitor.b1](#cgllc-airmonitor-b1) | Yes       |           |
| Mi Air Humidifier            | miio:basic       | [zhimi.humidifier.v1](#zhimi-humidifier-v1) | Yes       |           |
| Mi Air Humidifier            | miio:basic       | [zhimi.humidifier.ca1](#zhimi-humidifier-ca1) | Yes       |           |
| Mi Air Purifier v1           | miio:basic       | [zhimi.airpurifier.v1](#zhimi-airpurifier-v1) | Yes       |           |
| Mi Air Purifier v2           | miio:basic       | [zhimi.airpurifier.v2](#zhimi-airpurifier-v2) | Yes       |           |
| Mi Air Purifier v3           | miio:basic       | [zhimi.airpurifier.v3](#zhimi-airpurifier-v3) | Yes       |           |
| Mi Air Purifier v5           | miio:basic       | [zhimi.airpurifier.v5](#zhimi-airpurifier-v5) | Yes       |           |
| Mi Air Purifier Pro v6       | miio:basic       | [zhimi.airpurifier.v6](#zhimi-airpurifier-v6) | Yes       |           |
| Mi Air Purifier Pro v7       | miio:basic       | [zhimi.airpurifier.v7](#zhimi-airpurifier-v7) | Yes       |           |
| Mi Air Purifier 2 (mini)     | miio:basic       | [zhimi.airpurifier.m1](#zhimi-airpurifier-m1) | Yes       |           |
| Mi Air Purifier (mini)       | miio:basic       | [zhimi.airpurifier.m2](#zhimi-airpurifier-m2) | Yes       |           |
| Mi Air Purifier MS1          | miio:basic       | [zhimi.airpurifier.ma1](#zhimi-airpurifier-ma1) | Yes       |           |
| Mi Air Purifier MS2          | miio:basic       | [zhimi.airpurifier.ma2](#zhimi-airpurifier-ma2) | Yes       |           |
| Mi Air Purifier Super        | miio:basic       | [zhimi.airpurifier.sa1](#zhimi-airpurifier-sa1) | Yes       |           |
| Mi Air Purifier Super 2      | miio:basic       | [zhimi.airpurifier.sa2](#zhimi-airpurifier-sa2) | Yes       |           |
| Mi Air Purifier mb1          | miio:basic       | [zhimi.airpurifier.mb1](#zhimi-airpurifier-mb1) | Yes       |           |
| Mi Air Purifier mc1          | miio:basic       | [zhimi.airpurifier.mc1](#zhimi-airpurifier-mc1) | Yes       |           |
| Mi Air Purifier virtual      | miio:unsupported | zhimi.airpurifier.virtual | No        |           |
| Mi Air Purifier vtl m1       | miio:unsupported | zhimi.airpurifier.vtl_m1 | No        |           |
| Mi Remote v2                 | miio:unsupported | chuangmi.ir.v2         | No        |           |
| MiJia Rice Cooker            | miio:unsupported | chunmi.cooker.normal1  | No        |           |
| MiJia Rice Cooker            | miio:unsupported | chunmi.cooker.normal2  | No        |           |
| MiJia Rice Cooker            | miio:unsupported | hunmi.cooker.normal3   | No        |           |
| MiJia Rice Cooker            | miio:unsupported | chunmi.cooker.normal4  | No        |           |
| MiJia Heating Pressure Rice Cooker | miio:unsupported | chunmi.cooker.press1   | No        |           |
| MiJia Heating Pressure Rice Cooker | miio:unsupported | chunmi.cooker.press2   | No        |           |
| Mi Smart Fan                 | miio:basic       | [zhimi.fan.v1](#zhimi-fan-v1) | Yes       |           |
| Mi Smart Fan                 | miio:basic       | [zhimi.fan.v2](#zhimi-fan-v2) | Yes       |           |
| Mi Smart Pedestal Fan        | miio:basic       | [zhimi.fan.v3](#zhimi-fan-v3) | Yes       |           |
| Xiaomi Mi Smart Pedestal Fan | miio:basic       | [zhimi.fan.sa1](#zhimi-fan-sa1) | Yes       |           |
| Xiaomi Mi Smart Pedestal Fan | miio:basic       | [zhimi.fan.za1](#zhimi-fan-za1) | Yes       |           |
| Mi Smart Home Gateway v1     | miio:unsupported | lumi.gateway.v1        | No        |           |
| Mi Smart Home Gateway v2     | miio:unsupported | lumi.gateway.v2        | No        |           |
| Mi Smart Home Gateway v3     | miio:unsupported | lumi.gateway.v3        | No        |           |
| Mi Humdifier                 | miio:basic       | [zhimi.humidifier.v1](#zhimi-humidifier-v1) | Yes       |           |
| Light Control (Wall Switch)  | miio:unsupported | lumi.ctrl_neutral1.v1  | No        |           |
| Light Control (Wall Switch)  | miio:unsupported | lumi.ctrl_neutral2.v1  | No        |           |
| Xiaomi Philips Eyecare Smart Lamp 2 | miio:basic       | [philips.light.sread1](#philips-light-sread1) | Yes       |           |
| Xiaomi Philips LED Ceiling Lamp | miio:basic       | [philips.light.ceiling](#philips-light-ceiling) | Yes       |           |
| Xiaomi Philips LED Ceiling Lamp | miio:basic       | [philips.light.zyceiling](#philips-light-zyceiling) | Yes       |           |
| Xiaomi Philips Bulb          | miio:basic       | [philips.light.bulb](#philips-light-bulb) | Yes       |           |
| PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp | miio:basic       | [philips.light.candle](#philips-light-candle) | Yes       |           |
| Xiaomi Philips Downlight     | miio:basic       | [philips.light.downlight](#philips-light-downlight) | Yes       |           |
| Xiaomi Philips ZhiRui bedside lamp | miio:basic       | [philips.light.moonlight](#philips-light-moonlight) | Yes       |           |
| Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal | miio:basic       | [philips.light.candle2](#philips-light-candle2) | Yes       |           |
| philips.light.mono1          | miio:basic       | [philips.light.mono1](#philips-light-mono1) | Yes       |           |
| philips.light.virtual        | miio:basic       | [philips.light.virtual](#philips-light-virtual) | Yes       |           |
| philips.light.zysread        | miio:basic       | [philips.light.zysread](#philips-light-zysread) | Yes       |           |
| philips.light.zystrip        | miio:basic       | [philips.light.zystrip](#philips-light-zystrip) | Yes       |           |
| Mi Power-plug                | miio:basic       | [chuangmi.plug.m1](#chuangmi-plug-m1) | Yes       |           |
| Mi Power-plug v1             | miio:basic       | [chuangmi.plug.v1](#chuangmi-plug-v1) | Yes       |           |
| Mi Power-plug v2             | miio:basic       | [chuangmi.plug.v2](#chuangmi-plug-v2) | Yes       |           |
| Mi Power-plug v3             | miio:basic       | [chuangmi.plug.v3](#chuangmi-plug-v3) | Yes       |           |
| Mi Power-plug                | miio:basic       | [chuangmi.plug.m3](#chuangmi-plug-m3) | Yes       |           |
| Mi Smart Plug                | miio:basic       | [chuangmi.plug.hmi205](#chuangmi-plug-hmi205) | Yes       |           |
| Qing Mi Smart Power Strip v1 | miio:basic       | [qmi.powerstrip.v1](#qmi-powerstrip-v1) | Yes       |           |
| Mi Power-strip v2            | miio:basic       | [zimi.powerstrip.v2](#zimi-powerstrip-v2) | Yes       |           |
| Mi Toothbrush                | miio:unsupported | soocare.toothbrush.x3  | No        |           |
| Mi Robot Vacuum              | miio:vacuum      | [rockrobo.vacuum.v1](#rockrobo-vacuum-v1) | Yes       |           |
| Mi Robot Vacuum v2           | miio:vacuum      | [roborock.vacuum.s5](#roborock-vacuum-s5) | Yes       |           |
| Rockrobo Xiaowa Vacuum v2    | miio:unsupported | roborock.vacuum.e2     | No        |           |
| roborock.vacuum.c1           | miio:unsupported | roborock.vacuum.c1     | No        |           |
| Rockrobo Xiaowa Sweeper v2   | miio:unsupported | roborock.sweeper.e2v2  | No        |           |
| Rockrobo Xiaowa Sweeper v3   | miio:unsupported | roborock.sweeper.e2v3  | No        |           |
| Mi Water Purifier v2         | miio:basic       | [yunmi.waterpuri.v2](#yunmi-waterpuri-v2) | Yes       |           |
| Mi Water Purifier lx2        | miio:basic       | [yunmi.waterpuri.lx2](#yunmi-waterpuri-lx2) | Yes       |           |
| Mi Water Purifier lx3        | miio:basic       | [yunmi.waterpuri.lx3](#yunmi-waterpuri-lx3) | Yes       |           |
| Mi Water Purifier lx4        | miio:basic       | [yunmi.waterpuri.lx4](#yunmi-waterpuri-lx4) | Yes       |           |
| Mi Water Purifier v2         | miio:basic       | [yunmi.waterpurifier.v2](#yunmi-waterpurifier-v2) | Yes       |           |
| Mi Water Purifier v3         | miio:basic       | [yunmi.waterpurifier.v3](#yunmi-waterpurifier-v3) | Yes       |           |
| Mi Water Purifier v4         | miio:basic       | [yunmi.waterpurifier.v4](#yunmi-waterpurifier-v4) | Yes       |           |
| Xiaomi Wifi Extender         | miio:unsupported | xiaomi.repeater.v2     | No        |           |
| Mi Internet Speaker          | miio:unsupported | xiaomi.wifispeaker.v1  | No        |           |
| Yeelight Lamp                | miio:basic       | [yeelink.light.bslamp1](#yeelink-light-bslamp1) | Yes       |           |
| Yeelight Lamp                | miio:basic       | [yeelink.light.bslamp2](#yeelink-light-bslamp2) | Yes       |           |
| Yeelight LED Ceiling Lamp    | miio:basic       | [yeelink.light.ceiling1](#yeelink-light-ceiling1) | Yes       |           |
| Yeelight LED Ceiling Lamp v2 | miio:basic       | [yeelink.light.ceiling2](#yeelink-light-ceiling2) | Yes       |           |
| Yeelight LED Ceiling Lamp v3 | miio:basic       | [yeelink.light.ceiling3](#yeelink-light-ceiling3) | Yes       |           |
| Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB) | miio:basic       | [yeelink.light.ceiling4](#yeelink-light-ceiling4) | Yes       |           |
| Yeelight LED Ceiling Lamp v4 | miio:basic       | [yeelink.light.ceiling4.ambi](#yeelink-light-ceiling4-ambi) | Yes       |           |
| Yeelight LED Ceiling Lamp v5 | miio:basic       | [yeelink.light.ceiling5](#yeelink-light-ceiling5) | Yes       |           |
| Yeelight LED Ceiling Lamp v6 | miio:basic       | [yeelink.light.ceiling6](#yeelink-light-ceiling6) | Yes       |           |
| Yeelight LED Ceiling Lamp v7 | miio:basic       | [yeelink.light.ceiling7](#yeelink-light-ceiling7) | Yes       |           |
| Yeelight LED Ceiling Lamp v8 | miio:basic       | [yeelink.light.ceiling8](#yeelink-light-ceiling8) | Yes       |           |
| Yeelight ct2                 | miio:basic       | [yeelink.light.ct2](#yeelink-light-ct2) | Yes       |           |
| Yeelight White Bulb          | miio:basic       | [yeelink.light.mono1](#yeelink-light-mono1) | Yes       |           |
| Yeelight White Bulb v2       | miio:basic       | [yeelink.light.mono2](#yeelink-light-mono2) | Yes       |           |
| Yeelight Wifi Speaker        | miio:unsupported | yeelink.wifispeaker.v1 | No        |           |
| Yeelight                     | miio:basic       | [yeelink.light.lamp1](#yeelink-light-lamp1) | Yes       |           |
| Yeelight                     | miio:basic       | [yeelink.light.lamp2](#yeelink-light-lamp2) | Yes       |           |
| Yeelight                     | miio:basic       | [yeelink.light.lamp3](#yeelink-light-lamp3) | Yes       |           |
| Yeelight Strip               | miio:basic       | [yeelink.light.strip1](#yeelink-light-strip1) | Yes       |           |
| Yeelight Strip               | miio:basic       | [yeelink.light.strip2](#yeelink-light-strip2) | Yes       |           |
| Yeelight                     | miio:basic       | [yeelink.light.virtual](#yeelink-light-virtual) | Yes       |           |
| Yeelight Color Bulb          | miio:basic       | [yeelink.light.color1](#yeelink-light-color1) | Yes       |           |
| Yeelight Color Bulb YLDP06YL 10W | miio:basic       | [yeelink.light.color2](#yeelink-light-color2) | Yes       |           |
| Yeelight Color Bulb          | miio:basic       | [yeelink.light.color3](#yeelink-light-color3) | Yes       |           |


# Discovery

The binding has 2 methods for discovering devices. Depending on your network setup and the device model, your device may be discovered by one or both methods. If both methods discover your device, 2 discovery results may be in your inbox for the same device.

The MDNS discovery method will discover your device type, but won't discover a (required) token.
The basic discovery will not discovery the type, but will discover a token for models that support it.
Accept only one of the 2 discovery results, the alternate one can further be ignored.

## Tokens

The binding needs a token from the Xiaomi Mi Device in order to be able to control it.
Some devices provide the token upon discovery. This may depends on the firmware version.

If the device does not discover your token, it needs to be retrieved from the Mi Home app.
Note: latest Android MiHome no longer has the tokens in the database. Use 5.0.19 version or lower 
The token needs to be retrieved from the application database. The easiest way on Android to do is by using [MiToolkit](https://github.com/ultrara1n/MiToolkit/releases).

Alternatively, on Android open a backup file, or browse a rooted device, find the mio2db file with and read it sqlite.

For iPhone, use an un-encrypted iTunes-Backup and unpack it and use a sqlite tool to view the files in it: 
Then search in "RAW, com.xiaomi.home," for "USERID_mihome.sqlite" and look for the 32-digit-token or 96 digit encrypted token.

Note. The Xiaomi devices change the token when inclusion is done. Hence if you get your token after reset and than include it with the Mi Home app, the token will change.

## Binding Configuration

No binding configuration is required.

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
| power            | Switch  | Power                               |
| aqi              | Number  | Air Quality Index                   |
| battery          | Number  | Battery                             |
| usb_state        | Switch  | USB State                           |
| time_state       | Switch  | Time State                          |
| night_state      | Switch  | Night State                         |
| night_begin      | Number  | Night Begin Time                    |
| night_end        | Number  | Night End Time                      |

### Mi Air Humidifier (<a name="zhimi-humidifier-v1">zhimi.humidifier.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| setHumidity      | Number  | Humidity Set                        |
| aqi              | Number  | Air Quality Index                   |
| translevel       | Number  | Trans_level                         |
| bright           | Number  | Led Brightness                      |
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
| bright           | Number  | Led Brightness                      |
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
| led              | Switch  | Led Status                          |
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
| led              | Switch  | Led Status                          |
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
| led              | Switch  | Led Status                          |
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
| led              | Switch  | Led Status                          |
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
| led              | Switch  | Led Status                          |
| bright           | Number  | Led Brightness                      |
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
| led              | Switch  | Led Status                          |
| bright           | Number  | Led Brightness                      |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier 2 (mini) (<a name="zhimi-airpurifier-m1">zhimi.airpurifier.m1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | Led Status                          |
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
| led              | Switch  | Led Status                          |
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
| led              | Switch  | Led Status                          |
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
| led              | Switch  | Led Status                          |
| bright           | Number  | Led Brightness                      |
| filtermaxlife    | Number  | Filter Max Life                     |
| filterhours      | Number  | Filter Hours used                   |
| usedhours        | Number  | Run Time                            |
| motorspeed       | Number  | Motor Speed                         |
| filterlife       | Number  | Filter  Life                        |
| favoritelevel    | Number  | Favorite Level                      |
| temperature      | Number  | Temperature                         |
| purifyvolume     | Number  | Purivied Volume                     |
| childlock        | Switch  | Child Lock                          |

### Mi Air Purifier Super (<a name="zhimi-airpurifier-sa1">zhimi.airpurifier.sa1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | Led Status                          |
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
| led              | Switch  | Led Status                          |
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

### Mi Air Purifier mb1 (<a name="zhimi-airpurifier-mb1">zhimi.airpurifier.mb1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | Led Status                          |
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

### Mi Air Purifier mc1 (<a name="zhimi-airpurifier-mc1">zhimi.airpurifier.mc1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| mode             | String  | Mode                                |
| humidity         | Number  | Humidity                            |
| aqi              | Number  | Air Quality Index                   |
| averageaqi       | Number  | Average Air Quality Index           |
| led              | Switch  | Led Status                          |
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
| led_b            | Number  | Led                                 |
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
| led_b            | Number  | Led                                 |
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
| led_b            | Number  | Led                                 |
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
| led_b            | Number  | Led                                 |
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
| led_b            | Number  | Led                                 |
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
| bright           | Number  | Led Brightness                      |
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
| brightness       | Number  | Brightness                          |
| ambientPower     | Switch  | Ambient Power                       |
| ambientBrightness | Number  | Ambient Brightness                  |
| illumination     | Number  | Ambient Illumination                |
| eyecare          | Switch  | Eyecare                             |

### Xiaomi Philips LED Ceiling Lamp (<a name="philips-light-ceiling">philips.light.ceiling</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| switchscene      | Switch  | Switch Scene                        |
| toggle           | Switch  | Toggle                              |

### Xiaomi Philips LED Ceiling Lamp (<a name="philips-light-zyceiling">philips.light.zyceiling</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| switchscene      | Switch  | Switch Scene                        |
| toggle           | Switch  | Toggle                              |

### Xiaomi Philips Bulb (<a name="philips-light-bulb">philips.light.bulb</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp (<a name="philips-light-candle">philips.light.candle</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### Xiaomi Philips Downlight (<a name="philips-light-downlight">philips.light.downlight</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### Xiaomi Philips ZhiRui bedside lamp (<a name="philips-light-moonlight">philips.light.moonlight</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| gonight          | Switch  | Go Night                            |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### Xiaomi PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp White Crystal (<a name="philips-light-candle2">philips.light.candle2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### philips.light.mono1 (<a name="philips-light-mono1">philips.light.mono1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### philips.light.virtual (<a name="philips-light-virtual">philips.light.virtual</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### philips.light.zysread (<a name="philips-light-zysread">philips.light.zysread</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### philips.light.zystrip (<a name="philips-light-zystrip">philips.light.zystrip</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| cct              | Number  | Correlated Color Temperature        |
| scene            | Number  | Scene                               |
| dv               | Number  | DV                                  |
| switchscene      | Switch  | Switch Scene                        |
| delayoff         | Switch  | Delay Off                           |
| toggle           | Switch  | Toggle                              |

### Mi Power-plug (<a name="chuangmi-plug-m1">chuangmi.plug.m1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| temperature      | Number  | Temperature                         |

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
| led              | Switch  | Wifi led                            |

### Mi Power-plug (<a name="chuangmi-plug-m3">chuangmi.plug.m3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| temperature      | Number  | Temperature                         |

### Mi Smart Plug (<a name="chuangmi-plug-hmi205">chuangmi.plug.hmi205</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| temperature      | Number  | Temperature                         |

### Qing Mi Smart Power Strip v1 (<a name="qmi-powerstrip-v1">qmi.powerstrip.v1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| powerUsage       | Number  | Power Consumption                   |
| led              | Switch  | wifi_led                            |
| power_price      | Number  | power_price                         |
| current          | Number  | Current                             |
| temperature      | Number  | Temperature                         |

### Mi Power-strip v2 (<a name="zimi-powerstrip-v2">zimi.powerstrip.v2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| powerUsage       | Number  | Power Consumption                   |
| led              | Switch  | wifi_led                            |
| power_price      | Number  | power_price                         |
| current          | Number  | Current                             |
| temperature      | Number  | Temperature                         |

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
| brightness       | Number  | Brightness                          |
| delayoff         | String  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Lamp (<a name="yeelink-light-bslamp2">yeelink.light.bslamp2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | String  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight LED Ceiling Lamp (<a name="yeelink-light-ceiling1">yeelink.light.ceiling1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v2 (<a name="yeelink-light-ceiling2">yeelink.light.ceiling2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v3 (<a name="yeelink-light-ceiling3">yeelink.light.ceiling3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v4 (JIAOYUE 650 RGB) (<a name="yeelink-light-ceiling4">yeelink.light.ceiling4</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| ambientBrightness | Number  | Ambient Brightness                  |
| delayoff         | Number  | Shutdowm Timer                      |
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
| brightness       | Number  | Brightness                          |
| ambientBrightness | Number  | Ambient Brightness                  |
| delayoff         | Number  | Shutdowm Timer                      |
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
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v6 (<a name="yeelink-light-ceiling6">yeelink.light.ceiling6</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v7 (<a name="yeelink-light-ceiling7">yeelink.light.ceiling7</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight LED Ceiling Lamp v8 (<a name="yeelink-light-ceiling8">yeelink.light.ceiling8</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |
| customScene      | String  | Set Scene                           |
| nightlightBrightness | Number  | Nightlight Brightness               |

### Yeelight ct2 (<a name="yeelink-light-ct2">yeelink.light.ct2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight White Bulb (<a name="yeelink-light-mono1">yeelink.light.mono1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight White Bulb v2 (<a name="yeelink-light-mono2">yeelink.light.mono2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight (<a name="yeelink-light-lamp1">yeelink.light.lamp1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight (<a name="yeelink-light-lamp2">yeelink.light.lamp2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight (<a name="yeelink-light-lamp3">yeelink.light.lamp3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight Strip (<a name="yeelink-light-strip1">yeelink.light.strip1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | String  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Strip (<a name="yeelink-light-strip2">yeelink.light.strip2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | String  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight (<a name="yeelink-light-virtual">yeelink.light.virtual</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | Number  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | Number  | Color Mode                          |
| name             | String  | Name                                |

### Yeelight Color Bulb (<a name="yeelink-light-color1">yeelink.light.color1</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | String  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Color Bulb YLDP06YL 10W (<a name="yeelink-light-color2">yeelink.light.color2</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | String  | Shutdowm Timer                      |
| colorTemperature | Number  | Color Temperature                   |
| colorMode        | String  | Color Mode                          |
| toggle           | Switch  | toggle                              |
| rgbColor         | Color   | RGB Color                           |
| name             | String  | Name                                |

### Yeelight Color Bulb (<a name="yeelink-light-color3">yeelink.light.color3</a>) Channels

| Channel          | Type    | Description                         |
|------------------|---------|-------------------------------------|
| power            | Switch  | Power                               |
| brightness       | Number  | Brightness                          |
| delayoff         | String  | Shutdowm Timer                      |
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
```


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
Switch power "Power" (G_airmonitor) {channel="miio:basic:airmonitor:power"}
Number aqi "Air Quality Index" (G_airmonitor) {channel="miio:basic:airmonitor:aqi"}
Number battery "Battery" (G_airmonitor) {channel="miio:basic:airmonitor:battery"}
Switch usb_state "USB State" (G_airmonitor) {channel="miio:basic:airmonitor:usb_state"}
Switch time_state "Time State" (G_airmonitor) {channel="miio:basic:airmonitor:time_state"}
Switch night_state "Night State" (G_airmonitor) {channel="miio:basic:airmonitor:night_state"}
Number night_begin "Night Begin Time" (G_airmonitor) {channel="miio:basic:airmonitor:night_begin"}
Number night_end "Night End Time" (G_airmonitor) {channel="miio:basic:airmonitor:night_end"}
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
Number bright "Led Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
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
Number bright "Led Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Number bright "Led Brightness" (G_airpurifier) {channel="miio:basic:airpurifier:bright"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Number bright "Led Brightness" (G_airpurifier) {channel="miio:basic:airpurifier:bright"}
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

### Mi Air Purifier 2 (mini) (zhimi.airpurifier.m1) item file lines
note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier 2 (mini)" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
Number bright "Led Brightness" (G_airpurifier) {channel="miio:basic:airpurifier:bright"}
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

### Mi Air Purifier Super (zhimi.airpurifier.sa1) item file lines
note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier Super" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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

### Mi Air Purifier mb1 (zhimi.airpurifier.mb1) item file lines
note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier mb1" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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

### Mi Air Purifier mc1 (zhimi.airpurifier.mc1) item file lines
note: Autogenerated example. Replace the id (airpurifier) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_airpurifier "Mi Air Purifier mc1" <status>
Switch power "Power" (G_airpurifier) {channel="miio:basic:airpurifier:power"}
String mode "Mode" (G_airpurifier) {channel="miio:basic:airpurifier:mode"}
Number humidity "Humidity" (G_airpurifier) {channel="miio:basic:airpurifier:humidity"}
Number aqi "Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:aqi"}
Number averageaqi "Average Air Quality Index" (G_airpurifier) {channel="miio:basic:airpurifier:averageaqi"}
Switch led "Led Status" (G_airpurifier) {channel="miio:basic:airpurifier:led"}
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
Number led_b "Led" (G_fan) {channel="miio:basic:fan:led_b"}
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
Number led_b "Led" (G_fan) {channel="miio:basic:fan:led_b"}
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
Number led_b "Led" (G_fan) {channel="miio:basic:fan:led_b"}
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
Number led_b "Led" (G_fan) {channel="miio:basic:fan:led_b"}
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
Number led_b "Led" (G_fan) {channel="miio:basic:fan:led_b"}
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
Number bright "Led Brightness" (G_humidifier) {channel="miio:basic:humidifier:bright"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Xiaomi Philips LED Ceiling Lamp (philips.light.zyceiling) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips LED Ceiling Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Xiaomi Philips Bulb (philips.light.bulb) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp (philips.light.candle) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "PHILIPS Zhirui Smart LED Bulb E14 Candle Lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Xiaomi Philips Downlight (philips.light.downlight) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips Downlight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Xiaomi Philips ZhiRui bedside lamp (philips.light.moonlight) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Xiaomi Philips ZhiRui bedside lamp" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### philips.light.mono1 (philips.light.mono1) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "philips.light.mono1" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### philips.light.virtual (philips.light.virtual) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "philips.light.virtual" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### philips.light.zysread (philips.light.zysread) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "philips.light.zysread" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### philips.light.zystrip (philips.light.zystrip) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "philips.light.zystrip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number cct "Correlated Color Temperature" (G_light) {channel="miio:basic:light:cct"}
Number scene "Scene" (G_light) {channel="miio:basic:light:scene"}
Number dv "DV" (G_light) {channel="miio:basic:light:dv"}
Switch switchscene "Switch Scene" (G_light) {channel="miio:basic:light:switchscene"}
Switch delayoff "Delay Off" (G_light) {channel="miio:basic:light:delayoff"}
Switch toggle "Toggle" (G_light) {channel="miio:basic:light:toggle"}
```

### Mi Power-plug (chuangmi.plug.m1) item file lines
note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Power-plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
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
Switch led "Wifi led" (G_plug) {channel="miio:basic:plug:led"}
```

### Mi Power-plug (chuangmi.plug.m3) item file lines
note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Power-plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
```

### Mi Smart Plug (chuangmi.plug.hmi205) item file lines
note: Autogenerated example. Replace the id (plug) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_plug "Mi Smart Plug" <status>
Switch power "Power" (G_plug) {channel="miio:basic:plug:power"}
Number temperature "Temperature" (G_plug) {channel="miio:basic:plug:temperature"}
```

### Qing Mi Smart Power Strip v1 (qmi.powerstrip.v1) item file lines
note: Autogenerated example. Replace the id (powerstrip) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_powerstrip "Qing Mi Smart Power Strip v1" <status>
Switch power "Power" (G_powerstrip) {channel="miio:basic:powerstrip:power"}
Number powerUsage "Power Consumption" (G_powerstrip) {channel="miio:basic:powerstrip:powerUsage"}
Switch led "wifi_led" (G_powerstrip) {channel="miio:basic:powerstrip:led"}
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
Switch led "wifi_led" (G_powerstrip) {channel="miio:basic:powerstrip:led"}
Number power_price "power_price" (G_powerstrip) {channel="miio:basic:powerstrip:power_price"}
Number current "Current" (G_powerstrip) {channel="miio:basic:powerstrip:current"}
Number temperature "Temperature" (G_powerstrip) {channel="miio:basic:powerstrip:temperature"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
String delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
String delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number ambientBrightness "Ambient Brightness" (G_light) {channel="miio:basic:light:ambientBrightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:brightness"}
Number ambientBrightness "Ambient Brightness" (G_ceiling4) {channel="miio:basic:ceiling4:ambientBrightness"}
Number delayoff "Shutdowm Timer" (G_ceiling4) {channel="miio:basic:ceiling4:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight White Bulb (yeelink.light.mono1) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight White Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight White Bulb v2 (yeelink.light.mono2) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight White Bulb v2" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.lamp1) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.lamp2) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight (yeelink.light.lamp3) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Strip (yeelink.light.strip1) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Strip" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
String delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
String delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
Number delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
Number colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Color Bulb (yeelink.light.color1) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Color Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
String delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
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
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
String delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```

### Yeelight Color Bulb (yeelink.light.color3) item file lines
note: Autogenerated example. Replace the id (light) in the channel with your own. Replace `basic` with `generic` in the thing UID depending on how your thing was discovered.

```java
Group G_light "Yeelight Color Bulb" <status>
Switch power "Power" (G_light) {channel="miio:basic:light:power"}
Number brightness "Brightness" (G_light) {channel="miio:basic:light:brightness"}
String delayoff "Shutdowm Timer" (G_light) {channel="miio:basic:light:delayoff"}
Number colorTemperature "Color Temperature" (G_light) {channel="miio:basic:light:colorTemperature"}
String colorMode "Color Mode" (G_light) {channel="miio:basic:light:colorMode"}
Switch toggle "toggle" (G_light) {channel="miio:basic:light:toggle"}
Color rgbColor "RGB Color" (G_light) {channel="miio:basic:light:rgbColor"}
String name "Name" (G_light) {channel="miio:basic:light:name"}
```



