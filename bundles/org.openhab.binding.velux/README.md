
# Velux Binding

This binding integrates the <B>Velux</B> devices with help of a gateway, the <B>Velux Bridge KLF200</B>, which is able to control 200 actuators.
The Velux Binding interacts via the Velux Bridge with any [io-homecontrol](https://www.io-homecontrol.com/)-based
devices like window openers, shutters and others.

![Velux](doc/veluxlogo.jpg)

Based on the VELUX API this binding integrates <B>Velux</B> and other io-homecontrol devices directly into the openHAB, avoiding the necessity of any cloud-based mediation infrastructures. The complete home-automation will work even without any Internet connectivity.

For details about the features, see the following websites:

- [Velux](https://www.velux.com)
- [Velux API](https://www.velux.com/api/klf200)

## Initial Configuration of Devices in the Hub

This guide assumes that you have already configured your devices in the KLF200 hub.
When the KLF200 hub is started it provides a temporary private Wi-Fi Access Point to facilitate this configuration.
The Velux leaflet B) explains how to access the configuration web page via this temporary private Wi-Fi Access Point and configure your devices.
Note: ending the configuration process prematurely might lead to misconfiguration and require factory resetting your hub and/or devices.

If you want to add devices to the hub later, you have to access the configuration web page via the temporary private Wi-Fi Access Point once more.
See the chapter "FAQ and Troubleshooting" below if you have any problems setting up the connection to openHAB again afterwards.

Note: if any device connects to the temporary private Wi-Fi Access Point, it disables the normal LAN connection, thus preventing the binding from connecting.
So make sure this Wi-Fi AP is not permanently running (the default setting is that the AP will turn off after some time).

## Supported Things

The binding supports the following types of Thing.

| Thing Type    | Description                                                                       | Note |
|---------------|-----------------------------------------------------------------------------------|------|
| bridge        | A Velux KLF200 which acts as a gateway to all Velux / IO-home controlled devices. |      |
| window        | A Velux / IO-home control window.                                                 |  1.  |
| rollershutter | A Velux / IO-home control roller shutter.                                         |  1.  |
| actuator      | Generic Velux / IO-home device.                                                   |  1.  |
| scene         | A Velux Scene which commands Velux / IO-home devices to specific positions.       |      |
| vshutter      | A Velux virtual shutter.                                                          |  2.  |
| information   | A Thing that provides overall information about the binding itself.               |      |

1. Only supported in hubs with firmware v0.2.x.x or above
2. Only needed in hubs with firmware v0.1.x.x (due to note 1. above)

## Discovery

To simplify the initial provisioning, the binding provides one thing which can be found by autodiscovery.
The binding will automatically discover Velux Bridges within the local network, and place them in the Inbox.
Once a Velux Bridge has been discovered, you will need to enter the `password` Configuration Parameter (see below) before the binding can communicate with it.
And once the Velux Bridge is fully configured, the binding will automatically discover all its respective scenes and actuators (like windows and rollershutters), and place them in the Inbox.

## Thing Configuration

### Thing Configuration for "bridge"

The bridge Thing connects to the KLF-200 hub (bridge) to communicate with any respective connected Velux or IO-home device Things.
It signs on to the hub using the supplied connection parameters, and it polls the hub at regular intervals to read and write the data for each connected device.
The KLF-200 supports two Application Programming Interfaces "API" (a SLIP based one, and a JSON based one), and this binding can use either of them to communicate with it.
Before the binding can communicate with the hub, the Configuration Parameters `ipAddress` and `password` must be entered.
In addition there are some optional Configuration Parameters.

| Configuration Parameter | Default          | Required | Description                                                  |
|-------------------------|------------------|:--------:|--------------------------------------------------------------|
| ipAddress               |                  |   Yes    | Hostname or address for accessing the Velux Bridge.          |
| password                | velux123         |   Yes    | Password for authentication against the Velux Bridge.(\*\*)  |
| timeoutMsecs            | 3000             |    No    | Communication timeout in milliseconds.                       |
| protocol                | slip             |    No    | Underlying communication protocol (http/https/slip).         |
| tcpPort                 | 51200            |    No    | TCP port (80 or 51200) for accessing the Velux Bridge.       |
| retries                 | 5                |    No    | Number of retries during I/O.                                |
| refreshMsecs            | 10000            |    No    | Refresh interval in milliseconds.                            |
| isBulkRetrievalEnabled  | yes              |    No    | Load all scenes and actuators in one step.                   |
| isSequentialEnforced    | no               |    No    | Enforce Sequential Actuator Control even for long operations.|
| isProtocolTraceEnabled  | no               |    No    | Show any protocol interaction (loglevel INFO).               |

(\*\*) Note: This password is the API password that is printed on the back of the unit.
Normally it differs from the password of the web frontend.

Advice: if you see a significant number of messages per day as follows, you should increase the parameters `retries` or/and `timeoutMsecs`...

```
 communicate(): socket I/O failed continuously (x times).
```

For your convenience you'll see a log entry for the recognized configuration within the log file i.e.

```
2018-07-23 20:40:24.746 [INFO ] [.b.velux.internal.VeluxBinding] - veluxConfig[ipAddress=192.168.42.1,tcpPort=80,password=********,timeoutMsecs=2000,retries=10]
```

### Thing Configuration for "actuator", "window", "rollershutter"

These types of Thing only supported in the Velux Bridge in API version two or higher (firmware version > 0.2.*.*).
These types of Thing are configured by means of their serial number in the hub.
In addition there are some optional Configuration Parameters.

| Configuration Parameter | Default | Type    | Required | Description                                                                            |
|-------------------------|---------|---------|:--------:|----------------------------------------------------------------------------------------|
| serial                  |         | custom  |   Yes    | Serial number of the device in the hub (custom format 00:00:00:00:00:00:00:00)         |
| name                    |         | text    |    No    | Name of the device in the hub.                                                         |
| inverted                | false   | boolean |    No    | The `position` and `state` (if available) are inverted (i.e. 0% <-> 100%, OFF <-> ON). |

Notes:

1. To enable a complete inversion of all parameter values (i.e. for Velux windows), use the property `inverted` or add a trailing star to the eight-byte serial number.
For an example, see the Thing definition for 'Bathroom_Roof_Window' below.

2. Somfy devices do not provide a valid serial number to the Velux KLF200 Bridge.
For such devices you have to enter the special all-zero serial number 00:00:00:00:00:00:00:00 in the `serial` parameter.
This special serial number complies with the serial number validation checks, but also makes the binding use the `name` parameter value instead of the `serial` parameter value when it communicates with the KLF Bridge.
The `name` parameter must therefore contain the name that you gave to the actuator when you first registered it in the KLF200 Bridge.
For an example, see the Thing definition for 'Living_Room_Awning' below.

### Thing Configuration for "scene"

The Velux Bridge in API version one (firmware version 0.1.1.*) allows activating a set of predefined actions, so called scenes.
So besides the bridge, only one real Thing type exists, namely "scene".
This type of Thing is configured by means of its scene name in the hub.

| Configuration Parameter | Default   | Type | Required | Description                                                                 |
|-------------------------|-----------|------|:--------:|-----------------------------------------------------------------------------|
| sceneName               |           | text |   Yes    | Name of the scene in the hub.                                               |
| velocity                | 'default' | text |    No    | The speed at which the scene will be executed ('default', 'silent', 'fast') |

### Thing Configuration for "vshutter"

The Velux Bridge in API version one (firmware version 0.1.1.*) does not support a real rollershutter interaction.
So besides the bridge, this binding provides a virtual rollershutter Thing consisting of different scenes which set a specific shutter level.
Therefore the respective Item definition contains multiple pairs of rollershutter levels each followed by a scene name.
The virtual shutter Thing must be configured with pairs of level (0..10%) combined with the appropriate scene names (text) as follows.

| Configuration Parameter | Default | Type    | Required | Description                             |
|-------------------------|---------|---------|:--------:|-----------------------------------------|
| sceneLevels             |         | text    |   Yes    | {Level1},{Scene1},{Level2},{Scene2},..  |
| currentLevel            | 0       | integer |    No    | Inverts any device values (0..100).     |

## Supported Channels for Thing Types

### Channels for "bridge" Things

The supported Channels and their associated channel types are shown below.

| Channel     | Data Type | Description                                                                     |
|-------------|-----------|---------------------------------------------------------------------------------|
| status      | String    | Description of current Bridge State.                                            |
| reload      | Switch    | Command to force reload of the bridge information.                              |
| downtime    | Number    | Time interval (sec) between last successful and most recent device interaction. |
| doDetection | Switch    | Command to activate bridge detection mode.                                      |

### Channels for "window" Things

The supported Channels and their associated channel types are shown below.

| Channel      | Data Type     | Description                                     |
|--------------|---------------|-------------------------------------------------|
| position     | Rollershutter | Actual position of the window or device.        |
| limitMinimum | Rollershutter | Minimum limit position of the window or device. |
| limitMaximum | Rollershutter | Maximum limit position of the window or device. |

The `position` Channel indicates the open/close state of the window (resp. roller shutter) in percent (0% .. 100%) as follows..

- As a general rule the display is the actual physical position.
- If it is moving towards a new target position, the display is the target position.
- After the movement has completed, the display is the final physical position.
- If a window is opened manually, the display is `UNDEF`.
- In case of errors (e.g. window jammed) the display is `UNDEF`.
- If a Somfy actuator is commanded to its 'favorite' position via a Somfy remote control, under some circumstances the display is `UNDEF`. See also Rules below.

### Channels for "rollershutter" Things

The supported Channels and their associated channel types are shown below.

| Channel      | Data Type     | Description                                     |
|--------------|---------------|-------------------------------------------------|
| position     | Rollershutter | Actual position of the window or device.        |
| limitMinimum | Rollershutter | Minimum limit position of the window or device. |
| limitMaximum | Rollershutter | Maximum limit position of the window or device. |
| vanePosition | Dimmer        | Vane position of a Venetian blind. (optional)   |

The `position`, `limitMinimum`, and `limitMaximum` are the same as described above for "window" Things.

The `vanePosition` Channel only applies to Venetian blinds that have tiltable slats.
The binding detects whether the device supports a vane position, and if so, it adds the `vanePosition` Channel automatically.

### Channels for "actuator" Things

The supported Channels and their associated channel types are shown below.

| Channel      | Data Type     | Description                                     |
|--------------|---------------|-------------------------------------------------|
| position     | Rollershutter | Actual position of the window or device.        |
| state        | Switch        | Device control (ON, OFF).                       |
| limitMinimum | Rollershutter | Minimum limit position of the window or device. |
| limitMaximum | Rollershutter | Maximum limit position of the window or device. |

See the section above for "window" / "rollershutter" Things for further information concerning the `position` Channel.

### Channels for "scene" Things

The supported Channels and their associated channel types are shown below.

| Channel    | Data Type | Description                                                    |
|------------|-----------|----------------------------------------------------------------|
| action     | Switch    | Activates the scene (moves devices to their preset positions). |
| silentMode | Switch    | Enables silent mode.                                           |

### Channels for "vshutter" Things

The supported Channel and its associated channel type is shown below.

| Channel      | Data Type     | Description                             |
|--------------|---------------|-----------------------------------------|
| position     | Rollershutter | Position of the virtual roller shutter. |

See the section above for "window" / "rollershutter" Things for further information concerning the `position` Channel.

### Channels for "information" Thing

The supported Channel and its associated channel type is shown below.

| Channel     | Data Type | Description                    |
|-------------|-----------|--------------------------------|
| information | String    | Information about the binding. |

## Rain Sensor

Unfortunately Velux has decided to closely integrate the rain sensor into the window device.
The rain sensor is therefore not displayed in the device list.
On the other hand, the 'limitMinimum' channel of a roof window provides information about rainy weather:
If it is set internally by the Velux control unit to a value other than zero, it rains. (Joke!!)

## Properties of the "bridge" Thing

The bridge Thing provides the following properties.

| Property          | Description                                                     |
|-------------------|-----------------------------------------------------------------|
| address           | IP address of the Bridge                                        |
| check             | Result of the check of current item configuration               |
| connectionAttempt | Date-Time of last connection attampt                            |
| connectionSuccess | Date-Time of last successful connection attampt                 |
| defaultGW         | IP address of the Default Gateway of the Bridge                 |
| DHCP              | Flag whether automatic IP configuration is enabled              |
| firmware          | Software version of the Bridge                                  |
| products          | List of all recognized products                                 |
| scenes            | List of all defined scenes                                      |
| subnetMask        | IP subnetmask of the Bridge                                     |
| vendor            | Vendor name                                                     |
| WLANSSID          | Name of the wireless network (not suported any more)            |
| WLANPassword      | WLAN Authentication Password (not suported any more)            |

## Full Example

### Things

```
Bridge velux:klf200:g24 "Velux KLF200 Hub" @ "Under Stairs" [ipAddress="192.168.1.xxx", password="secret"] {
	// Velux (standard) window (with serial number)
    Thing window Bathroom_Roof_Window "Bathroom Roof Window" @ "Bathroom" [serial="56:36:13:5A:11:2A:05:70", inverted=true]

	// Somfy (non-standard) rollershutter (without serial number)
    Thing rollershutter Living_Room_Awning "Living Room Awning" @ "Living Room" [serial="00:00:00:00:00:00:00:00", name="Living Room Awning"]
}
```

See [velux.things](doc/conf/things/velux.things) for more examples.

### Items

```
Rollershutter Bathroom_Roof_Window_Position "Bathroom Roof Window Position [%.0f %%]" {channel="velux:window:g24:w56-36-13-5A-11-2A-05-70:position"}
```

See [velux.items](doc/conf/items/velux.items) for more examples.

### Sitemap

```
Frame label="Velux Windows" {
	Slider item=Bathroom_Roof_Window_Position
}
```

See [velux.sitemap](doc/conf/sitemaps/velux.sitemap) for more examples.

### Rule for simultaneously moving the main position and the vane position

This applies to shades or shutters that have both a main position and a vane / tilt position.
On such shades if one sends a vane position command followed shortly by a main position command (or vice versa) the second command will cause the first command to stop.
This problem is most problematic when the two commands are issued simultaneously by a single rule.
In order to solve this problem, there is a rule action to simultaneously set the main position and the vane position.

_Warning: use this command carefully..._

The action is a command method that is called from within a rule.
The method is called with the following syntax `moveMainAndVane(thingName, mainPercent, vanePercent)`.
The meaning of the arguments is described in the table below.
The method returns a `Boolean` whose meaning is also described in the table below.

| Argument    | Type    | Example                             | Description                                                                             |
|-------------|---------|-------------------------------------|-----------------------------------------------------------------------------------------|
| thingName   | String  | "velux:rollershutter:hubid:thingid" | The thing name of the shutter. Must be a valid configured thing in the hub.             |
| mainPercent | Integer | 75                                  | The target main position in percent. Integer between 0 and 100.                         |
| vanePercent | Integer | 25                                  | The target vane position in percent. Integer between 0 and 100.                         |
| return      | Boolean | `true`                              | Is `true` if the command was sent sucessfully or `false` if any arguments were invalid. |

Example:

```java
rule "Simultaneously Move Main and Vane Positions"
when
	...
then
    // note: "velux:klf200:hubid" shall be the thing name of your KLF 200 hub
	val veluxActions = getActions("velux", "velux:klf200:hubid")
	if (veluxActions !== null) {
		val succeeded = veluxActions.moveMainAndVane("velux:rollershutter:hubid:thingid", 75, 25)
	}
end
```

### Rule for closing windows after a period of time

Especially in the colder months, it is advisable to close the window after adequate ventilation.
Therefore, automatic closing after one minute is good to save on heating costs.
However, to allow the case of intentional prolonged opening, an automatic closure is made only with the window fully open.

Example:

```java
rule "V_WINDOW_changed"
when
	Item V_WINDOW changed
then
	logInfo("rules.V_WINDOW",	"V_WINDOW_changes() called.")
	// Get the sensor value
	val Number windowState = V_WINDOW.state as DecimalType
	logWarn("rules.V_WINDOW", "Window state is " + windowState + ".")
	if (windowState < 80) {
		if (windowState == 0) {
			logWarn("rules.V_WINDOW", "V-WINDOW changed to fully open.")
			var int interval = 1
			createTimer(now.plusMinutes(interval)) [ |
					logWarn("rules.V_WINDOW:event", "event-V_WINDOW(): setting V-WINDOW to 100.")
					sendCommand(V_WINDOW, 100)
					V_WINDOW.postUpdate(100)
    				logWarn("rules.V_WINDOW:event", "event-V_WINDOW done.")
        		]
    		} else {
			logWarn("rules.V_WINDOW", "V-WINDOW changed to partially open.")
		}
    	}
	// Check type of item
	logDebug("rules.V_WINDOW",	"V_WINDOW_changes finished.")
end
```

See [velux.rules](doc/conf/rules/velux.rules) for more examples.

### Rule for rebooting the Bridge

This binding includes a rule action to reboot the Velux Bridge by remote command:

- `boolean isRebooting = rebootBridge()`

_Warning: use this command carefully..._

Example:

```java
rule "Reboot KLF 200"
when
	...
then
	val veluxActions = getActions("velux", "velux:klf200:myhubname")
	if (veluxActions !== null) {
		val isRebooting = veluxActions.rebootBridge()
		logWarn("Rules", "Velux KLF 200 rebooting: " + isRebooting)
	} else {
		logWarn("Rules", "Velux KLF 200 actions not found, check thing ID")
	}
end
```

### Rule for checking if a Window has been manually opened

In the case that a window has been manually opened, and you then try to move it via the binding, its `position` will become `UNDEF`.
You can exploit this behaviour in a rule to check regularly if a window has been manually opened.

```java
rule "Every 10 minutes, check if window is in manual mode"
when
	Time cron "0 0/10 * * * ?" // every 10 minutes
then
	if (Velux_Window.state != UNDEF) {
		// command the window to its actual position; this will either
		// - succeed: the actual position will not change, or
		// - fail: the position becomes UNDEF (logged next time this rule executes)
		Velux_Window.sendCommand(Velux_Window.state)
	} else {
		logWarn("Rules", "Velux in Manual mode, trying to close again")
		// try to close it
		Velux_Window.sendCommand(0)
	}
end
```

### Rule for Somfy actuators

If a Somfy actuator is commanded to its 'favorite' position via a Somfy remote control, under some circumstances the display is `UNDEF`.
You can resolve this behaviour in a rule that detects the `UNDEF` position and (re-)commands it to its favorite position.

```java
rule "Somfy Actuator: resolve undefined position"
when
    Item Somfy_Actuator changed to UNDEF
then
    val favoritePosition = 91
    Somfy_Actuator.sendCommand(favoritePosition)
end
```

## Debugging

For those who are interested in more detailed insight of the processing of this binding, a deeper look can be achieved by increased loglevel.

With Karaf you can use the following command sequence:

```
log:set TRACE org.openhab.binding.velux
log:tail
```

This, of course, is possible on command line with the commands:

```
% openhab-cli console log:set TRACE org.openhab.binding.velux
% openhab-cli console log:tail org.openhab.binding.velux
```

On the other hand, if you prefer a textual configuration, you can append the logging definition with:

```
	<logger name="org.openhab.binding.velux" level="TRACE">
		<appender-ref ref="FILE" />
	</logger>
```

During startup of normal operations, there should be only some few messages within the logfile, like:

```
[INFO ] [nal.VeluxValidatedBridgeConfiguration] - veluxConfig[protocol=slip,ipAddress=192.168.45.9,tcpPort=51200,password=********,timeoutMsecs=1000,retries=5,refreshMsecs=15000,isBulkRetrievalEnabled=true]
[INFO ] [ng.velux.bridge.slip.io.SSLconnection] - Starting velux bridge connection.
[INFO ] [hab.binding.velux.bridge.slip.SClogin] - velux bridge connection successfully established (login succeeded).
[INFO ] [ding.velux.handler.VeluxBridgeHandler] - Found velux scenes:
        Scene "V_Shutter_West_100" (index 5) with non-silent mode and 0 actions
        Scene "V_Shutter_West_000" (index 4) with non-silent mode and 0 actions
        Scene "V_Shutter_Ost_090" (index 10) with non-silent mode and 0 actions
        Scene "V_Window_Mitte_005" (index 3) with non-silent mode and 0 actions
        Scene "V_Window_Mitte_000" (index 1) with non-silent mode and 0 actions
        Scene "V_Window_Mitte_100" (index 2) with non-silent mode and 0 actions
        Scene "V_Shutter_West_090" (index 7) with non-silent mode and 0 actions
        Scene "V_Window_Mitte_010" (index 0) with non-silent mode and 0 actions
        Scene "V_Shutter_Ost_000" (index 8) with non-silent mode and 0 actions
        Scene "V_Shutter_Ost_100" (index 9) with non-silent mode and 0 actions       .
[INFO ] [ding.velux.handler.VeluxBridgeHandler] - Found velux actuators:
        Product "M_Rollershutter" / ROLLER_SHUTTER (bridgeIndex=4,serial=43:12:14:5A:12:1C:05:5F,position=0010)
        Product "O_Rollershutter" / ROLLER_SHUTTER (bridgeIndex=3,serial=43:12:40:5A:0C:23:0A:6E,position=0000)
        Product "M_Window" / WINDOW_OPENER (bridgeIndex=0,serial=43:12:3E:26:0C:1B:00:10,position=C800)
        Product "W-Rollershutter" / ROLLER_SHUTTER (bridgeIndex=1,serial=43:12:40:5A:0C:2A:05:64,position=0000)      .
[INFO ] [ding.velux.handler.VeluxBridgeHandler] - velux Bridge is online with 10 scenes and 4 actuators, now.
```

However if you have set the configuration parameter isProtocolTraceEnabled to true, you'll see the complete sequence of exchanged messages:

```
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Sending command GW_PASSWORD_ENTER_REQ.
[INFO ] [nternal.bridge.slip.io.SSLconnection] - Starting velux bridge connection.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_PASSWORD_ENTER_CFM.
[INFO ] [g.velux.internal.bridge.slip.SClogin] - velux bridge connection successfully established (login succeeded).
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Sending command GW_COMMAND_SEND_REQ.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_COMMAND_SEND_CFM.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Sending command GW_GET_LIMITATION_STATUS_REQ.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_NODE_STATE_POSITION_CHANGED_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_COMMAND_RUN_STATUS_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_COMMAND_RUN_STATUS_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_SESSION_FINISHED_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_NODE_STATE_POSITION_CHANGED_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_LIMITATION_STATUS_CFM.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_LIMITATION_STATUS_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Sending command GW_GET_NODE_INFORMATION_REQ.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_COMMAND_RUN_STATUS_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_SESSION_FINISHED_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_NODE_INFORMATION_CFM.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_NODE_INFORMATION_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Sending command GW_GET_NODE_INFORMATION_REQ.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_NODE_INFORMATION_CFM.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_NODE_INFORMATION_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Sending command GW_GET_NODE_INFORMATION_REQ.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_NODE_INFORMATION_CFM.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_NODE_INFORMATION_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Sending command GW_GET_NODE_INFORMATION_REQ.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_NODE_INFORMATION_CFM.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Received answer GW_GET_NODE_INFORMATION_NTF.
[INFO ] [internal.bridge.slip.SlipVeluxBridge] - Sending command GW_GET_LIMITATION_STATUS_REQ.
```

## Supported/Tested Firmware Revisions

The Velux Bridge in API version one (firmware version 0.1.1.*) allows activating a set of predefined actions, so called scenes.
Therefore beside the bridge, only one main thing exists, the scene element.

The next-generation firmware version two is not backward compatible, and does not provide a public web frontend, but version two does provide full access to any IO-Home compatible devices not limited to Velux and includes many different features.

| Firmware revision | Release date | Description                                                             |
|:-----------------:|:------------:|-------------------------------------------------------------------------|
| 0.1.1.0.41.0      | 2016-06-01   | Default factory shipping revision.                                      |
| 0.1.1.0.42.0      | 2017-07-01   | Public Web Frontend w/ JSON-API.                                        |
| 0.1.1.0.44.0      | 2017-12-14   | Public Web Frontend w/ JSON-API.                                        |
| 0.2.0.0.71.0      | 2018-09-27   | Public SLIP-API w/ private-only WLAN-based Web Frontend w/ JSON-API.    |

Notes:

- Velux bridges cannot be returned to version one of the firmware after being upgraded to version two.

## FAQ and troubleshooting

### Is it possible to run the both communication methods in parallel?

For environments with the firmware version 0.1.* on the gateway, the interaction with the bridge is limited to the HTTP/JSON based communication, of course.
On the other hand, after upgrading the gateway firmware to version 2, it is possible to run the binding either using HTTP/JSON if there is a permanent connectivity towards the WLAN interface of the KLF200 or using SLIP towards the LAN interface of the gateway.
For example the Raspberry PI can directly be connected via WLAN to the Velux gateway and providing the other services via the LAN interface (but not vice versa).

### Known Limitations

The communication based on HTTP/JSON is limited to one connection: If the binding is operational, you won't get access to the Web Frontend in parallel.

The SLIP communication is limited to two connections in parallel, i.e. two different openHAB bindings - or - one openHAB binding and another platform connection.

Both interfacing methods, HTTP/JSON and SLIP, can be run in parallel.
Therefore, on the one hand you can use the Web Frontend for manual control and on the other hand a binding can do all automatic jobs.

### Login sequence fails and Connection Refused

If you get this error first make sure that you entered the right password (the one below SSID on the back of the hub).
If the error persists, it may be due to the temporary Wi-Fi Access Point blocking the LAN (as described above).
To recover from this, first disable the bridge in the UI, disconnect the LAN cable, power cycle your KLF200 and wait a few minutes.
Then reconnect the LAN cable and re-enable the bridge in the UI again.
DO NOT try to connect anything to the temporary Wi-Fi Access Point during this process!!

### Unknown Velux devices

All known <B>Velux</B> devices can be handled by this binding.
However, there might be some new ones which will be reported within the logfiles.
Therefore, error messages like the one below should be reported to the maintainers so that the new Velux device type can be incorporated.

```
[ERROR] [g.velux.things.VeluxProductReference] - PLEASE REPORT THIS TO MAINTAINER: VeluxProductReference(3) has found an unregistered ProductTypeId.
```
