# Tellstick Binding

This is an openHAB binding for Tellstick devices produced by Telldus, a Swedish company based in Lund.

The original Tellstick focused on controlling 433 MHz devices like switches, dimmers and reading sensors from different brands.
Many of the supported devices are cheaper and "low-end" and support have been made by reverse engineer the transmission protocols.
All of these 433 MHz devices is one-way, so some versions of the Tellstick monitoring the air to keep the state of all devices.

The latest versions have also implemented Z-Wave as transmission protocol which open up for more robust transmission due two-ways communication.

<p align="center">
<img src="doc/tellstick_duo.jpg" alt="Tellstick Duo with device" width="300px"/>
</p>

## Supported Things

This binding supports the following thing types:

*   *Dimmable Device* - Usually for controlling lamps.  `dimmer`
*   *Switchable Device* - On/Off only could be lamps or other electronic equipment. `switch`
*   *Sensors* - Temperature- and humidity-sensors. `sensor`

Additionally the binding have two types of bridge things which correspond to available API types:

*   *Telldus Core Bridge* - Oldest API, used by USB devices. `telldus-core`
*   *Telldus Live Bridge* - Telldus Cloud service, all devices with online access. `telldus-live`

The attentive reader discovers that there is many missing sensor types; `UV`, `Luminance`, `Dew point`, `Barometic pressure` `Rainrate`, `Raintotal`, `Winddirection`, `Windaverage` and `Windgust` which is supported by the Tellstick devices.
Support have not been implemented on the openhab side yet, contributions are welcome.  

***Switchbased sensors workaround*** <br>
*   Some 433MHz magnetic & PIR sensors for example magnetic door sensors are detected as a regular `switch` things instead of a separate type. There is technically no way of distinguish them apart from regulur `switch` things.
For using them as sensors only (not paired to a lamp) please consult the workaround in the channel section.

## Discovery

Devices which is added to *Telldus Core* and *Telldus Live* can be discovered by openHAB.

When you add this binding it will try to discover the *Telldus Core Bridge*.
If it's installed correct its devices will show up.
If you want to use the *Telldus Live* its bridge, *Telldus Live bridge* need to be added manually.

## Binding Configuration

***For USB connected tellsticks only, eg. Basic and DUO***
First of all you need to make sure that your JVM is matching your installed Telldus Center.
This normally means openHab must run on a 32bit JVM for windows and a 64bit JVM for linux.
For windows the binding is hardcoded to look for Telldus Center in Programs Files ("C:/Program Files/Telldus/;C:/Program Files (x86)/Telldus/").
If you have trouble getting the telldus core library to work you can modify the library path using

## Thing Configuration

Only the bridges require manual configuration.
The devices and sensors should not be added by hand, let the discovery/inbox initially configure these.

### Dimmers & switches

There is an option to override the resend count of the commands.
Use the option `repeat` for that. Default resend count is 2.

### Bridges

Depending on your tellstick device type there is different ways of using this binding.
The binding implements two different API:  
**1)** *Telldus Core* which is a local only interface supported by USB based device. <br>
**2)** *Telldus Live* which is a REST based cloud service maintained by Telldus. <br>

> Not implemented yet but supported by some new devices: <br>
> **3)** *Local Rest API* is a local API which would work similar to Telldus Live but local.

Depending on your Tellstick model different API methods is available:

<table>
<tr><td><b>Model</b></td> <td><b>Telldus Core</b></td> <td><b>Telldus Live</b></td> <td>Local REST API</td> <td><b>Verified working with openHAB</b></td></tr>
<tr><td>Tellstick Basic</td><td>X</td><td>X</td><td></td><td></td></tr>
<tr><td>Tellstick Duo</td><td>X</td><td>X</td><td></td><td>X</td></tr>
<tr><td>Tellstick Net v.1</td><td></td><td>X</td><td></td><td></td></tr>
<tr><td>Tellstick Net v.2</td><td></td><td>X</td><td>X</td><td></td></tr>
<tr><td>Tellstick ZNet Lite v.1</td><td></td><td>X</td><td>X</td><td>X</td></tr>
<tr><td>Tellstick ZNet Lite v.2</td><td></td><td>X</td><td>X</td><td></td></tr>
<tr><td><i>Tellstick ZNet Pro (Not released)</i></td><td></td><td>X</td><td>X</td><td></td></tr>
</table>

#### Telldus Core Bridge

```
Bridge tellstick:telldus-core:1 "Tellstick Duo" [resendInterval=200]
```

Optional:

-   **libraryPath:** The path to tellduscore.dll/so,
-   **resendInterval:** The interval between each transmission of command in ms, default 100ms.

#### Telldus Live Bridge

To configure Telldus Live you have request OAuth tokens from Telldus. Goto this page
<http://api.telldus.com/keys/index> and request your keys and update the config.

```
Bridge tellstick:telldus-live:2 "Tellstick ZWave" [publicKey="XXX", privateKey="YYYY", token= "ZZZZ", tokenSecret="UUUU"]
```

Required:

-   **privateKey:** Private key
-   **publicKey:** Public key
-   **token:** Token
-   **tokenSecret:** Token secret

Optional:

-   **refreshInterval:** How often we should contact *Telldus Live* to check for updates (in ms)

## Channels

Actuators (&lsqb;dimmer&rsqb;/&lsqb;switch&rsqb;) support the following channels:

<table>
<tr><td><b>Channel Type ID</b></td> <td><b>Item Type</b></td> <td><b>Description</b></td> </tr>
<tr><td>dimmer</td><td>Number</td><td>This channel indicates the current dim level</td></tr>
<tr><td>state</td><td>Switch</td><td>This channel indicates whether a device is turned on or off.</td></tr>
<tr><td>timestamp</td><td>DateTime</td><td>This channel reports the last time this device state changed.</td></tr>
</table>

Sensors (&lsqb;sensor&rsqb;) support the following channels:

<table>
<tr><td><b>Channel Type ID</b></td> <td><b>Item Type</b></td> <td><b>Description</b></td> </tr>
<tr><td>humidity</td><td>Number</td><td>This channel reports the current humidity in percentage.</td></tr>
<tr><td>temperature</td><td>Number</td><td>This channel reports the current temperature in celsius.</td></tr>
<tr><td>timestamp</td><td>DateTime</td><td> This channel reports the last time this sensor was updates.</td></tr>
</table>

### Switchbased sensor workaround

All switchbased sensors are binary and the goal is to represent them as a `contact` item in openhab. Eg. a door is open or closed and can't be altered by sending a radio signal.
To achive that we will create a proxy item which is updated by a rule.


First create another proxy item for every sensor:

```
Switch front_door_sensor	"Front door"  <door>  {channel="tellstick:switch:1:7:state"}
Contact front_door_proxy	"Front door"  <door>
```

Then create a rule which updates the proxy item:

```
rule "proxy_front_door_on"
when
	Item front_door_sensor changed to ON
then
	postUpdate(front_door_proxy, OPEN);		
end

rule "proxy_front_door_off"
when
	Item front_door_sensor changed to OFF
then
	postUpdate(front_door_proxy, CLOSED);		
end
```

## Full Example

### tellstick.things

```
Bridge tellstick:telldus-core:1 "Tellstick Duo" [resendInterval=200]
Bridge tellstick:telldus-live:2 "Tellstick ZWave" [refreshInterval=10000, publicKey="XXXXXXXX", privateKey="YYYYYY", token= "ZZZZZZZZ", tokenSecret="UUUUUUUUUU"]
```

Devices are preferable discovered automatically.
Add them either with karaf: `inbox approve <thingId>` or in paperUI. The bridges can also be added with PaperUI.

### tellstick.items

List available devices in karaf with `things` or get the channels in paperUI.

```
Slider living_room_ceiling  "Living room ceiling"              <light>                 {channel="tellstick:dimmer:1:3:state"}
Switch living_room_table    "Living room table"                <light>                 {channel="tellstick:switch:1:3:state"}
Number inside_temperature   "Inside temperature [%.1f °C]"     <temperature>           {channel="tellstick:sensor:1:47_temperaturehumidity_fineoffset:temperature"}
Number inside_humidity      "Inside humidity [%.1f RH]"        <humidity>              {channel="tellstick:sensor:1:47_temperaturehumidity_fineoffset:humidity"}
```
