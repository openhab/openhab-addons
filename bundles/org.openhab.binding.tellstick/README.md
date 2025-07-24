# Tellstick Binding

This is an openHAB binding for Tellstick devices produced by Telldus, a Swedish company based in Lund.

The original Tellstick focused on controlling 433 MHz devices like switches, dimmers and reading sensors from different brands.
Many of the supported devices are cheaper and "low-end" and support have been made by reverse engineer the transmission protocols.
All of these 433 MHz devices is one-way, so some versions of the Tellstick monitoring the air to keep the state of all devices.

The latest versions have also implemented Z-Wave as transmission protocol which open up for more robust transmission due two-ways communication.

![Tellstick Duo device](doc/tellstick_duo.jpg)

![Tellstick ZNet device](doc/znet.jpeg)

## Supported Things

This binding supports the following thing types:

- _Dimmable Device_ - Usually for controlling lamps.  `dimmer`
- _Switchable Device_ - On/Off only could be lamps or other electronic equipment. `switch`
- _Sensors_ - Temperature- and humidity-sensors. `sensor`

Additionally the binding have two types of bridge things which correspond to available API types:

- _Telldus Core Bridge_ - Oldest API, used by USB devices. `telldus-core`
- _Telldus Live Bridge_ - Telldus Cloud service, all devices with online access. `telldus-live`
- _Telldus Local Bridge_ - Telldus Local API, Tellstick Net v2/Tellstick ZNet Lite v1/v2. `telldus-local`

### Switchbased sensors workaround

- Some 433MHz magnetic & PIR sensors, for example, magnetic door sensors, are detected as regular `switch` Things instead of type `contact`. There is technically no way of distinguish them apart from regulur `switch` Things. For using them as sensors only (not paired to a lamp) please consult the workaround in the channel section.

## Discovery

Devices which is added to _Telldus Core_, _Telldus Live_ and _Telldus Local_ can be discovered by openHAB.

When you add this binding it will try to discover the _Telldus Core Bridge_.
If it is installed correct its devices will show up.

If you want to use the _Telldus Live_ or _Telldus Local_, their bridges, _Telldus Live bridge_ or _Tellstick Local_, needs to be added manually.

## Binding Configuration

The binding itself requires no configuration.

## Thing Configuration

Only the bridges require manual configuration.
It is preferable that devices and sensors are discovered automatically; let the discovery initially configure them.

### Dimmers & switches

There is an option to override the resend count of the commands.
Use the option `repeat` for that. Default resend count is 2.

### Bridges

Depending on your Tellstick device type there is different ways of using this binding.
The binding implements three different APIs:
**1)** _Telldus Core_ which is a local only interface supported by USB based device. <br>
**2)** _Telldus Live_ which is a REST based cloud service maintained by Telldus.
**3)** _Telldus Local_ which is a REST based local service maintained by Telldus.
<br>

Depending on your Tellstick model, different bridge-types are available:

| Model                   | Telldus Core | Telldus Live | Local REST API | Verified working with openHAB |
|-------------------------|:------------:|:------------:|:--------------:|:-----------------------------:|
| Tellstick Basic         |       X      |       X      |                |               X               |
| Tellstick Duo           |       X      |       X      |                |               X               |
| Tellstick Net v.1       |              |       X      |                |               X               |
| Tellstick Net v.2       |              |       X      |        X       |                               |
| Tellstick ZNet Lite v.1 |              |       X      |        X       |               X               |
| Tellstick ZNet Lite v.2 |              |       X      |        X       |                               |

#### Telldus Core Bridge

> To enable communication between openHAB and tellstick-core service (Telldus center) they must use same architecture, eg. 32-bit or 64-bit. The supplied version from Telldus is compiled against 32-bit architecture. Therefore, it is better to use 32-bit java for openHAB. To check which version of Java is currently in use, run: `java -d32 -version`
>
> _For changing architecture in Linux check out: `dpkg --add-architecture`_

The telldus-core bridge uses a library on the local computer which is a `.dll` file for Windows and a `.so` file for Linux. The default one is usually correct.

```java
Bridge tellstick:telldus-core:1 "Tellstick Duo" [resendInterval=200,libraryPath="C:/Program Files/Telldus/;C:/Program Files (x86)/Telldus/"]
```

Optional:

- **libraryPath:** The path to tellduscore.dll/so semicolon separated list of folders.
- **resendInterval:** The interval between each transmission of command in ms, default 100ms.

#### Telldus Live Bridge

To configure Telldus Live you have request OAuth tokens from Telldus.
Goto this page <https://pa-api.telldus.com/keys/index> and request your keys and update the config.

```java
Bridge tellstick:telldus-live:2 "Tellstick ZWave" [publicKey="XXX", privateKey="YYYY", token= "ZZZZ", tokenSecret="UUUU"]
```

Required:

- **privateKey:** Private key
- **publicKey:** Public key
- **token:** Token
- **tokenSecret:** Token secret

Optional:

- **refreshInterval:** How often we should contact _Telldus Live_ to check for updates (in ms)

#### Telldus Local Bridge

To configure Telldus Local you need to know the local IP address of your Tellstick device and also request an access token.

Goto this page:
<https://tellstick-server.readthedocs.io/en/latest/api/authentication.html>
and follow steps 1), 2) and 3) to generate an access token.

In step 2) when you authenticate the application in your favorite browser, choose the options '1 year' and 'Auto renew access'.

Copy the 'token' returned in Step 3) and use that as accessToken in the local bridge config.

```text
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImF1ZCI6IkV4YW1wbGUgYXBwIiwiZXhwIjoxNDUyOTUxNTYyfQ.eyJyZW5ldyI6dHJ1ZSwidHRsIjo4NjQwMH0.HeqoFM6-K5IuQa08Zr9HM9V2TKGRI9VxXlgdsutP7sg"
```

```java
Bridge tellstick:telldus-local:3 "Tellstick Local ZWave" [ipAddress="x.y.z.w" , accesToken= "XYZ...W"]
```

**NOTE**: This binding does not automatically update the the token, even if 'Auto renew access' is chosen in step 2) above. This has to be done manually at least once a year, as described in the 'Refreshing a token' section in the above page.

Required:

- **ipAddress:** Local IP address of your Tellstick device
- **accessToken:** Access Token

Optional:

- **refreshInterval:** How often we should contact _Telldus Local_ to check for updates (in ms)

## Channels

Actuators (dimmer/switch) support the following channels:

| Channel Type ID | Item Type | Description                                                   |
|-----------------|-----------|---------------------------------------------------------------|
| dimmer          | Number    | This channel indicates the current dim level                  |
| state           | Switch    | This channel indicates whether a device is turned on or off.  |
| timestamp       | DateTime  | This channel reports the last time this device state changed. |

Sensors (sensor) support the following channels:

| Channel Type ID | Item Type           | Description                                                 |
|-----------------|---------------------|-------------------------------------------------------------|
| humidity        | Number:Dimensionless| This channel reports the current humidity in percentage.    |
| temperature     | Number:Temperature  | This channel reports the current temperature.               |
| timestamp       | DateTime            | This channel reports the last time this sensor was updates. |

PowerSensors ([powersensor]) support the following channels:

| Channel Type ID | Item Type              | Description                                                 |
|-----------------|------------------------|-------------------------------------------------------------|
| watt            | Number:Power           | This channel reports the current watt.                      |
| ampere          | Number:ElectricCurrent | This channel reports the current ampere.                    |
| timestamp       | DateTime               | This channel reports the last time this sensor was updates. |

WindSensors ([windsensor]) support the following channels:

| Channel Type ID | Item Type    | Description                  |
|-----------------|--------------|------------------------------|
| windgust        | Number:Speed | The current peak wind gust.  |
| winddirection   | Number:Angle | The current wind direction.  |
| windaverage     | Number:Speed | The current wind average.    |

RainSensors ([rainsensor]) support the following channels:

| Channel Type ID | Item Type     | Description                |
|-----------------|---------------|----------------------------|
| rainrate        | Number:Length | This current rate of rain. |
| raintotal       | Number:Length | The total rain.            |

### Switchbased sensor workaround

All switchbased sensors are binary and the goal is to represent them as a `contact` item in openHAB. Eg. a door is open or closed and can't be altered by sending a radio signal.
To achieve that we will create a proxy item which is updated by a rule.

First create another proxy item for every sensor:

```java
Switch front_door_sensor    "Front door"  <door>  {channel="tellstick:switch:1:7:state"}
Contact front_door_proxy    "Front door"  <door>
```

Then create a rule which updates the proxy item:

```java
rule "proxy_front_door_on"
when
    Item front_door_sensor changed to ON
then
    front_door_proxy.postUpdate(OPEN);
end

rule "proxy_front_door_off"
when
    Item front_door_sensor changed to OFF
then
    front_door_proxy.postUpdate(CLOSED);
end
```

## Full Example

### tellstick.things

```java
Bridge tellstick:telldus-core:1 "Tellstick Duo" [resendInterval=200] {
    dimmer BedroomCeilingLamp1 [protocol="arctech",model="selflearning-dimmer",name="BedroomCeilingLamp1",deviceId="8"]
    switch LivingTV [protocol="arctech",name="LivingTV",deviceId="5"]
    sensor OutsideSensor1 [protocol="fineoffset",model="temperaturehumidity",name="temperaturehumidity:125",deviceId="125_temperaturehumidity_fineoffset"]
}
Bridge tellstick:telldus-live:2 "Tellstick ZWave" [refreshInterval=10000, publicKey="XXXXXXXX", privateKey="YYYYYY", token= "ZZZZZZZZ", tokenSecret="UUUUUUUUUU"] {
    sensor OutsideSensor2 [protocol="fineoffset",model="temperaturehumidity",name="temperaturehumidity:120",deviceId="120_temperaturehumidity_fineoffset"]
}
Bridge tellstick:telldus-local:3 "Tellstick Local ZWave" [ipAddress="192.168.50.17" , accesToken= "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImF1ZCI6IkV4YW1wbGUgYXBwIiwiZXhwIjoxNDUyOTUxNTYyfQ.eyJyZW5ldyI6dHJ1ZSwidHRsIjo4NjQwMH0.HeqoFM6-K5IuQa08Zr9HM9V2TKGRI9VxXlgdsutP7sg"] {
    sensor OutsideSensor3 [protocol="fineoffset",model="temperaturehumidity",name="temperaturehumidity:120",deviceId="120_temperaturehumidity_fineoffset"]
}
```

### tellstick.items

```java
Number OutsideSensor1_Temperture <temperature> { channel="tellstick:sensor:tellstickgateway:OutsideSensor1:temperature"}
Number OutsideSensor1_Humidity <humidity> { channel="tellstick:sensor:tellstickgateway:OutsideSensor1:humidity"}

Switch LivingTV_Power <screen> { channel="tellstick:switch:tellstickgateway:LivingTV:switch"}
Dimmer BedroomCeilingLamp1_Brightness <lightbulb> { channel="tellstick:dimmer:tellstickgateway:BedroomCeilingLamp1:dimmer"}
```
