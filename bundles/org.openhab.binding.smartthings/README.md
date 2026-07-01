# Samsung SmartThings Binding

This binding integrates devices and scenes from the Samsung SmartThings Cloud into openHAB.
It communicates with SmartThings through the public SmartThings API and represents each SmartThings account as an openHAB bridge.
Devices such as lights, sensors, TVs and appliances are then added as Things below that account bridge.

A physical SmartThings hub is not required.
Devices that are already connected to SmartThings through Wi-Fi or another SmartThings-supported connection can be used as long as they are available in the SmartThings account.

## Supported Things

The binding provides a `smartthings:account` bridge type for one SmartThings user account.
Multiple account bridges can be configured when different SmartThings user accounts should be integrated into the same openHAB instance.

The following static Thing types are included:

| Thing Type UID | Description |
|----------------|-------------|
| `smartthings:generic-color-light-bulb` | SmartThings color light bulb with power, brightness, color and color temperature channels |
| `smartthings:generic-light-bulb` | SmartThings light bulb with power and brightness channels |
| `smartthings:generic-light-sensor` | SmartThings illuminance/light sensor |
| `smartthings:generic-presence-sensor` | SmartThings presence sensor or mobile phone presence state |
| `smartthings:generic-scene` | SmartThings scene trigger |
| `smartthings:generic-television` | Samsung TV connected through SmartThings |
| `smartthings:generic-washer` | Samsung washing machine connected through SmartThings |
| `smartthings:Samsung_Oven` | Samsung oven connected through SmartThings |
| `smartthings:Samsung_Room_A_C` | Samsung room air conditioner |
| `smartthings:Samsung_Soundbar` | Samsung soundbar connected through SmartThings |
| `smartthings:Samsung_The_Frame` | Samsung The Frame TV connected through SmartThings |

The binding can also create dynamic Thing types from device capabilities.
Dynamic discovery is an advanced option and is mainly useful for unsupported devices or while improving the binding.

## Discovery

Discovery requires an authorized `smartthings:account` bridge.
After the account is online, start a scan from the Inbox to discover devices and scenes from the SmartThings account.

By default, discovery maps supported SmartThings device categories to the included static Thing definitions.
Unsupported device categories are skipped unless dynamic Thing discovery is enabled.
If the bridge option `useDynamicThings` is enabled, the binding also registers dynamic Thing types and channels from the capabilities reported by SmartThings.
Dynamic Thing types can expose more device-specific channels, but they are less stable than static Thing definitions.

## Thing Configuration

### `account` Bridge Configuration

Create one `smartthings:account` bridge for each SmartThings user account.
Leave `appName`, `clientId` and `clientSecret` empty during first setup; the binding fills them automatically during authorization.

| Name | Type | Description | Default | Required | Advanced |
|------|------|-------------|---------|----------|----------|
| `appName` | text | SmartThings app name created for this bridge | N/A | no | yes |
| `clientId` | text | OAuth client ID of the SmartThings app | N/A | no | yes |
| `clientSecret` | text | OAuth client secret of the SmartThings app | N/A | no | yes |
| `callbackUrl` | text | Public HTTPS URL for SmartThings event callbacks; filled automatically when openHAB Cloud webhooks are available | N/A | no | no |
| `pollingTime` | integer | Polling interval in seconds; `-1` disables polling | `-1` | no | yes |
| `useDynamicThings` | boolean | Create dynamic Thing types from SmartThings capabilities | `false` | no | yes |

### Authorization

The binding uses OAuth to authorize each SmartThings account bridge.
After creating the bridge, open the authorization link shown in the bridge status.
The link uses the bridge UID as path, for example `/smartthings/home` for `smartthings:account:home`.

During authorization, the binding creates or updates the SmartThings API app that belongs to the bridge and stores the OAuth client values in the bridge configuration.
When the flow completes, the bridge should become online and devices can be discovered from the Inbox.

The binding first tries to register a SmartThings SSE subscription for device state updates.
If SSE registration is not available, it tries callback subscriptions.
Callback subscriptions require a public HTTPS URL.
Polling can be enabled as a fallback when neither event mechanism is available.
The following modes only describe how device state updates are received after authorization.
They do not change the initial app bootstrap described below.

| Mode | Configuration | Notes |
|------|---------------|-------|
| SSE subscription | No additional bridge setting | Tried automatically before callback registration |
| Public openHAB URL | Set `callbackUrl` to your public HTTPS callback endpoint, for example `https://openhab.example.org/smartthings/home/cb` | Requires a reverse proxy or port forwarding that reaches openHAB from the internet |
| openHAB Cloud webhook | Install and connect the openHAB Cloud Connector add-on, then leave `callbackUrl` empty | The binding requests a cloud webhook and fills `callbackUrl` automatically |
| Polling | Set `pollingTime` to a positive value | Works without public callbacks, but state updates can be delayed |

#### Initial App Bootstrap Redirect

During first authorization, the binding creates a bridge-specific SmartThings app through the fixed SmartThings CLI OAuth client.
That client always redirects to `http://localhost:61973/finish`, and the binding temporarily listens on port `61973` on the openHAB server.
This bootstrap is the same for public openHAB URLs, openHAB Cloud webhooks and polling when no `clientId` and `clientSecret` are stored yet.

If the browser runs on another machine, replace only `localhost` with the openHAB server host name or IP address when the redirect appears.
Keep the port, path and query string unchanged:

```text
http://localhost:61973/finish?code=...&state=...
http://openhab-server:61973/finish?code=...&state=...
```

The browser must be able to reach the openHAB server on TCP port `61973` while the authorization window is open.
This temporary redirect is only used for the app bootstrap; it is independent of SmartThings event callbacks, openHAB Cloud webhooks and HTTPS.
After the app has been created, the binding stores the generated `clientId` and `clientSecret` in the bridge configuration.
If a later authorization step also redirects to `localhost:61973`, repeat the same replacement.

### Device Thing Configuration

Static device Things require the SmartThings device ID.
The device ID is the SmartThings UUID of the device.
It can be obtained through discovery, the SmartThings API or the SmartThings CLI.

| Name | Type | Description | Default | Required | Advanced |
|------|------|-------------|---------|----------|----------|
| `deviceId` | text | SmartThings device UUID | N/A | yes | no |

### Scene Thing Configuration

Scene Things trigger SmartThings scenes.

| Name | Type | Description | Default | Required | Advanced |
|------|------|-------------|---------|----------|----------|
| `sceneId` | text | SmartThings scene UUID | N/A | yes | no |
| `locationId` | text | SmartThings location UUID; needed only when the account has multiple locations | N/A | no | no |

## Channels

Static Thing types define channel groups such as `main`, `control`, `picture`, or `remote`.
Item links therefore use the channel format `smartthings:<thingTypeId>:<bridgeId>:<thingId>:<group>#<channelId>`.

### Light Channels

| Thing Type UID | Channel | Type | Read/Write | Description |
|----------------|---------|------|------------|-------------|
| `generic-light-bulb` | `main#switch` | Switch | RW | Switches the light on or off |
| `generic-light-bulb` | `main#level` | Dimmer | RW | Brightness level |
| `generic-color-light-bulb` | `main#switch` | Switch | RW | Switches the light on or off |
| `generic-color-light-bulb` | `main#level` | Dimmer | RW | Brightness level |
| `generic-color-light-bulb` | `main#color` | Color | RW | Light color |
| `generic-color-light-bulb` | `main#color_temperature` | Dimmer | RW | Color temperature |

### Sensor and Scene Channels

| Thing Type UID | Channel | Type | Read/Write | Description |
|----------------|---------|------|------------|-------------|
| `generic-light-sensor` | `main#illuminance` | Number:Illuminance | R | Measured illuminance |
| `generic-light-sensor` | `main#brightnessLevel` | Number | R | Relative brightness level |
| `generic-presence-sensor` | `main#presence` | Switch | R | Presence state |
| `generic-scene` | `main#trigger` | Switch | W | Sends `ON` to execute the scene |

### Appliance Channels

| Thing Type UID | Channel | Type | Read/Write | Description |
|----------------|---------|------|------------|-------------|
| `Samsung_Room_A_C` | `control#switch` | Switch | RW | Switches the air conditioner on or off |
| `Samsung_Oven` | `status#completion-time` | DateTime | R | Current completion time |
| `Samsung_Oven` | `status#operating-state` | String | R | Current operating state |
| `Samsung_Oven` | `status#progress` | Number:Dimensionless | R | Current cooking progress |
| `Samsung_Oven` | `status#oven-job-state` | String | R | Current oven job state |
| `Samsung_Oven` | `status#operation-time` | DateTime | R | Current operation time |
| `Samsung_Soundbar` | `control#switch` | Switch | RW | Switches the soundbar on or off |
| `Samsung_Soundbar` | `control#volume` | Number | RW | Soundbar volume |
| `Samsung_Soundbar` | `control#mute` | Switch | RW | Soundbar mute state |
| `Samsung_Soundbar` | `control#input-source` | String | RW | Active soundbar input source |
| `Samsung_Soundbar` | `control#playback` | Player | W | Media playback control for play, pause, and stop |
| `generic-television`, `Samsung_The_Frame` | `control#switch` | Switch | RW | Switches the TV on or off |
| `generic-television`, `Samsung_The_Frame` | `control#volume` | Number | RW | TV volume |
| `generic-television`, `Samsung_The_Frame` | `control#mute` | Switch | RW | TV mute state |
| `generic-television`, `Samsung_The_Frame` | `control#input-source` | String | RW | Active input source |
| `generic-television`, `Samsung_The_Frame` | `control#channel` | String | RW | Current TV channel |
| `generic-television`, `Samsung_The_Frame` | `control#playback` | Player | RW | Media playback control |
| `generic-television`, `Samsung_The_Frame` | `picture#picture-mode` | String | RW | TV picture mode |
| `generic-television`, `Samsung_The_Frame` | `picture#sound-mode` | String | RW | TV sound mode |
| `generic-television`, `Samsung_The_Frame` | `remote#channel-up` | Switch | W | Sends `ON` to select the next channel |
| `generic-television`, `Samsung_The_Frame` | `remote#channel-down` | Switch | W | Sends `ON` to select the previous channel |
| `Samsung_The_Frame` | `control#art-mode` | Switch | W | Sends `ON` to activate art mode |
| `generic-washer` | `main#switch` | Switch | RW | Switches the washer on or off |
| `generic-washer` | `main#machineState` | String | RW | Overall machine state |
| `generic-washer` | `main#jobState` | String | R | Current wash cycle phase |
| `generic-washer` | `main#completionTime` | DateTime | R | Expected finish time |
| `generic-washer` | `main#running` | Switch | R | Indicates whether the washer is running |
| `generic-washer` | `main#remaining` | Number | R | Remaining minutes |
| `generic-washer` | `main#power` | Switch | RW | Washer power control |
| `generic-washer` | `main#remoteEnabled` | Switch | R | Remote-control availability |
| `generic-washer` | `main#mode` | String | RW | Active wash program |
| `generic-washer` | `main#rinseMode` | String | RW | Rinse mode |
| `generic-washer` | `main#spinSpeed` | String | RW | Spin speed |
| `generic-washer` | `main#temperature` | String | RW | Water temperature |
| `generic-washer` | `main#bubbleSoak` | Switch | RW | Bubble soak mode |
| `generic-washer` | `main#volume` | Number | RW | Buzzer volume |
| `generic-washer` | `main#extraCare` | String | RW | Extra care mode |
| `generic-washer` | `main#extraCareLocation` | String | RW | Extra care location |
| `generic-washer` | `main#watt` | Number:Power | R | Current power draw |
| `generic-washer` | `main#kwh` | Number:Energy | R | Accumulated energy usage |
| `generic-washer` | `main#waterLiters` | Number | R | Water consumption |
| `generic-washer` | `main#kidsLock` | Switch | R | Child lock state |
| `generic-washer` | `main#currentCycle` | String | R | Active program code |
| `generic-washer` | `main#operatingState` | String | R | Operating state |
| `generic-washer` | `main#progress` | Number | R | Cycle progress |
| `generic-washer` | `main#detergentRemaining` | Number | R | Remaining detergent amount |
| `generic-washer` | `main#softenerRemaining` | Number | R | Remaining fabric softener amount |
| `generic-washer` | `main#delayEnd` | Number | R | Remaining delayed-start time |
| `generic-washer` | `main#supportedCourses` | String | R | Available wash program codes |
| `generic-washer` | `main#remainingTimeStr` | String | R | Remaining cycle time as text |
| `generic-washer` | `main#operationTime` | Number:Time | R | Total selected cycle time |
| `generic-washer` | `main#updateAvailable` | Switch | R | Firmware update availability |

Dynamic Thing types expose channels based on the capabilities returned by SmartThings.
Check the generated Thing in the openHAB UI for the exact channel list.

## Full Example

### Thing Configuration

```java
Bridge smartthings:account:home [ callbackUrl="https://openhab.example.org/smartthings/home/cb", pollingTime=60 ] {
    Thing generic-color-light-bulb living_room_lamp [ deviceId="11111111-2222-3333-4444-555555555555" ]
    Thing generic-light-sensor hallway_sensor [ deviceId="22222222-3333-4444-5555-666666666666" ]
    Thing generic-scene good_night [ sceneId="33333333-4444-5555-6666-777777777777" ]
}
```

### Item Configuration

```java
Switch LivingRoomLampPower "Living room lamp" { channel="smartthings:generic-color-light-bulb:home:living_room_lamp:main#switch" }
Dimmer LivingRoomLampLevel "Living room lamp brightness" { channel="smartthings:generic-color-light-bulb:home:living_room_lamp:main#level" }
Color LivingRoomLampColor "Living room lamp color" { channel="smartthings:generic-color-light-bulb:home:living_room_lamp:main#color" }
Number:Illuminance HallwayIlluminance "Hallway illuminance" { channel="smartthings:generic-light-sensor:home:hallway_sensor:main#illuminance" }
Switch GoodNightScene "Good night" { channel="smartthings:generic-scene:home:good_night:main#trigger" }
```

### Sitemap Configuration

```perl
sitemap smartthings label="SmartThings" {
    Frame label="Living Room" {
        Switch item=LivingRoomLampPower
        Slider item=LivingRoomLampLevel
        Colorpicker item=LivingRoomLampColor
    }
    Frame label="Scenes" {
        Switch item=GoodNightScene mappings=[ON="Run"]
    }
}
```

## Troubleshooting

If the account bridge stays offline, open the authorization link from the bridge status and complete the SmartThings authorization flow again.
If callbacks cannot be registered, make sure `callbackUrl` is a public HTTPS URL, install and connect the openHAB Cloud Connector add-on so the binding can fill `callbackUrl`, or enable polling with `pollingTime`.
If a discovered device is not matched to a useful static Thing type, enable the advanced `useDynamicThings` bridge option and scan again.

## References

- [SmartThings API Documentation](https://developer.smartthings.com/docs/api/public)
- [SmartThings Capabilities Reference](https://developer.smartthings.com/docs/devices/capabilities/capabilities-reference/)
- [openHAB Thing Documentation](https://www.openhab.org/docs/concepts/things.html)
- [openHAB Items Documentation](https://www.openhab.org/docs/configuration/items.html)
