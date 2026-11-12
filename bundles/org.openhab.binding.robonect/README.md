# Robonect Binding

Robonect is a piece of hardware which has to be put into your Husqvarna, Gardena and other branded automower and makes it accessible in your internal network.
More details about the Robonect module can be found at [robonect.de](https://forum.robonect.de/)

This binding integrates mowers having the robonect module installed as a Thing into the home automation solution, allowing to control the mower and react on mower status changes in rules.

## Supported Things

The binding supports one Thing type which is the `mower`.

Tested mowers

| Mower                   | Robonect module  | Robonect firmware version |
|-------------------------|------------------|---------------------------|
| Gardena SILENO city 250 | Robonect Hx      | 2.0                       |

## Discovery

Automatic discovery is not supported.

## Thing Configuration

The `mower` Thing supports the following configuration parameters. All parameters except `host` are optional.

| Parameter              | Required | Description |
| ---------------------- | -------- | ----------- |
| `host`                 | Yes      | Host name or IP address of the Robonect module. |
| `user`                 | No       | Username for mower authentication, if enabled in the mower firmware. |
| `password`             | No       | Password for mower authentication, if enabled in the mower firmware. |
| `pollInterval`         | No       | Interval in seconds between normal status polls (default: `30`). |
| `offlineTimeout`       | No       | Time in seconds before the offline trigger fires when the mower cannot be reached (default: `30`). |
| `timezone`             | No       | Time zone configured on the mower (default: `Europe/Berlin`). |
| `pollIntervalExtended` | No       | Interval in seconds between extended polls. Set to `0` to disable extended polling (default: `0`). Extended polling includes push-service mode when `useWebApi` is enabled. |
| `useAjaxApi`           | No       | Read job timing and end-of-day mode from the localized Robonect Ajax API. Disabled by default. |
| `useWebApi`            | No       | Enable web-interface operations for remote-start settings and push-service mode; also reads the weather-service interval when the weather API omits it. Disabled by default. |
| `pushUser`             | No       | Username used by the mower when it sends push notifications to openHAB; Basic authentication reserves `:` as the username/password separator. |
| `pushPass`             | No       | Password used by the mower when it sends push notifications to openHAB. |

- Both `pushUser` and `pushPass` must be set to enable the push callback endpoint.
- Motor, battery, and wire diagnostics use normal polling while the mower reports `MOWING`, `SEARCH_CHARGING_STATION`, or
  `SEARCHING`; at other times they use the extended polling schedule.

An example `.things` configuration with every optional parameter is:

```java
Thing robonect:mower:automower "Mower" @ "Garden" [
    host="192.168.2.1",
    user="mower-user",
    password="mower-password",
    pollInterval=30,
    offlineTimeout=30,
    timezone="Europe/Berlin",
    pollIntervalExtended=1800,
    useAjaxApi=true,
    useWebApi=true,
    pushUser="openhab",
    pushPass="callback-password"
]
```

## Channels

Static channels remain listed when extended polling is disabled; dynamic channels are created when their API response is received.

### Main Channels

| Channel ID | Item Type | Description | Read/Write | Notes |
| ----------- | --------- | ----------- | ---------- | ----- |
| `battery` | `Number:Dimensionless [%]` | Battery charge level as a percentage. | Read only | — |
| `blades-quality` | `Number:Dimensionless [%]` | Remaining blade quality estimated by Robonect, as a percentage. | Read only | — |
| `blades-replacement-days` | `Number:Time [d]` | Number of days since the blade replacement date recorded in Robonect. | Read only | — |
| `blades-usage-hours` | `Number:Time [h]` | Accumulated blade operating time since the last replacement recorded in Robonect. | Read only | — |
| `error-code` | `Number` | Manufacturer-defined code of the mower's current error, if the mower is in status 7 (error). The binding resets this to UNDEF, once the mower is not in error status anymore. | Read only | — |
| `error-date` | `DateTime` | Date and time of the mower's current error, if the mower is in status 7 (error). The binding resets this to UNDEF, once the mower is not in error status anymore. | Read only | — |
| `error-message` | `String` | Description of the mower's current error, if the mower is in status 7 (error). The binding resets this to UNDEF, once the mower is not in error status anymore. | Read only | — |
| `health-humidity` | `Number:Dimensionless [%]` | Relative humidity measured inside the Robonect module. | Read only | — |
| `health-temperature` | `Number:Temperature [°C]` | Temperature measured inside the Robonect module. | Read only | — |
| `home` | `Switch` | Whether the mower reports that it is at home in the charging station. | Read only | — |
| `id` | `String` | Identifier reported by the mower. | Read only | — |
| `job` | `Switch` | Send `ON` to start a configured job or `OFF` to stop the active job. | Read/Write (See notes) | Active job detection uses the localized Ajax response when `useAjaxApi` is enabled; otherwise the binding tracks job state. See [Thing Actions and Custom Channels](#thing-actions-and-custom-channels) for the shared job settings. |
| `job-end` | `DateTime` | End time of the active mowing job, estimated from the accepted command or read from the Ajax interface when `useAjaxApi` is enabled. Cleared when the job ends. | Read only | Added at initialization; uses `timezone`. |
| `job-start` | `DateTime` | Start time of the active mowing job, estimated from the accepted command or read from the Ajax interface when `useAjaxApi` is enabled. Cleared when the job ends. | Read only | Added at initialization; uses `timezone`. |
| `last-error-code` | `Number` | Manufacturer-defined code of the latest error in the mower's error history. | Read only | — |
| `last-error-date` | `DateTime` | Date and time of the latest error in the mower's error history. | Read only | — |
| `last-error-message` | `String` | Description of the latest error in the mower's error history. | Read only | — |
| `mode` | `String` | Retrieves or  sets the mode of the mower. Possible values are <ul><li>HOME</li><li>AUTO</li><li>MANUAL</li><li>EOD: "end of day" mode. </li><li>JOB</li></ul> | Read/Write | `EOD` can only retrieved, if `useAjaxApi` is enabled. Otherwise the mower will switch to the HOME mode and stay in this mode for the rest of the day. After midnight it will switch back to the mode which was set previously. Directly setting the mode to `JOB` immediately starts a 60 minute job. See [Thing Actions and Custom Channels](#thing-actions-and-custom-channels) for custom job settings. |
| `mowing-hours` | `Number:Time [h]` | Accumulated mower operating hours. | Read only | — |
| `name` | `String` | Name displayed for the mower in Robonect. Accepts a new name as a command. | Read/Write | — |
| `offlineTrigger` | `—` | Reports that the configured offline timeout has elapsed. | Read only | Publishes `OFFLINE_TIMEOUT` as a state update, not as a channel-trigger event. |
| `prevent` | `Number` | Raw code identifying the condition preventing mowing. | Read only | — |
| `reset-error` | `Switch` | Send `ON` to reset the current error. This action does not report whether an error is present. | Write only | — |
| `service-operation` | `String` | Send `SLEEP`, `SHUTDOWN`, or `REBOOT` to control the Robonect module. | Write only | — |
| `start` | `Switch` | `ON` releases automatic starts and requests a start (analog to pressing the start button on mower); `OFF` blocks automatic starts and requests a stop (analog to the stop button on mower). Actual mower activity is reported by `status`. | Read/Write | — |
| `status` | `Number` | Retrieves the current mower status. Possible values are <ul><li>0 : DETECTING_STATUS</li><li>1 : PARKING</li><li>2 : MOWING</li><li>3 : SEARCH_CHARGING_STATION</li><li>4 : CHARGING</li><li>5 : SEARCHING</li><li>7 : ERROR_STATUS</li><li>8 : LOST_SIGNAL</li><li>16 : OFF</li><li>17 : SLEEPING</li><li>18 : DOORDELAY</li><li>99 : UNKNOWN</li></ul> | Read only | — |
| `status-distance` | `Number:Length [m]` | Distance travelled from the charging station while travelling to a remote-start point. | Read only | — |
| `status-duration` | `Number:Time [min]` | Time spent in the current mower status. | Read only | — |
| `timer-next` | `DateTime` | Date and Time of the next timer set. This is just valid if there is an ACTIVE timer status (see `timer-status`). | Read only | Uses `timezone`. |
| `timer-status` | `String` | Current mowing schedule status. Possible values are <ul><li>INACTIVE: no timer set</li><li>ACTIVE: timer set and currently running</li><li>STANDBY: timer set but not triggered/running yet</li></ul> | Read only | — |
| `timestamp` | `DateTime` | Current date and time reported by the Robonect clock. | Read only | Uses `timezone`. |
| `wlan-ap-channel` | `Number` | Radio channel configured for the access point. The setting applies only while Robonect is not connected to a WLAN. | Read only | — |
| `wlan-ap-dhcp` | `Switch` | Whether the access point assigns IP addresses to connected clients through DHCP. | Read only | — |
| `wlan-ap-dhcp-end` | `String` | Last IP address in the access-point DHCP address pool. | Read only | — |
| `wlan-ap-dhcp-start` | `String` | First IP address in the access-point DHCP address pool. | Read only | — |
| `wlan-ap-enabled` | `Switch` | Whether the Robonect access point is enabled. | Read only | — |
| `wlan-ap-encryption` | `String` | Authentication and encryption mode reported for the access point. | Read only | — |
| `wlan-ap-gateway` | `String` | Gateway address configured for the Robonect access-point interface. | Read only | — |
| `wlan-ap-hidden` | `Switch` | Whether the access-point network name is hidden from WLAN scans. | Read only | — |
| `wlan-ap-ip` | `String` | IP address of the Robonect access-point interface. | Read only | — |
| `wlan-ap-mac` | `String` | MAC address of the Robonect access-point interface. | Read only | — |
| `wlan-ap-max-connections` | `Number` | Maximum number of clients allowed to connect to the access point. | Read only | — |
| `wlan-ap-netmask` | `String` | Subnet mask of the Robonect access-point interface. | Read only | — |
| `wlan-ap-ssid` | `String` | Network name broadcast by the Robonect access point. | Read only | — |
| `wlan-dhcp` | `Switch` | Whether the WLAN interface obtains its IP configuration through DHCP. | Read only | — |
| `wlan-enabled` | `Switch` | Whether Robonect is configured to connect to the WLAN. | Read only | — |
| `wlan-gateway` | `String` | Gateway address used by the Robonect WLAN interface. | Read only | — |
| `wlan-ip` | `String` | IP address of the Robonect WLAN interface. | Read only | — |
| `wlan-mac` | `String` | MAC address of the Robonect WLAN interface. | Read only | — |
| `wlan-netmask` | `String` | Subnet mask of the Robonect WLAN interface. | Read only | — |
| `wlan-ping` | `Switch` | Whether gateway connection monitoring is enabled. Robonect pings the gateway every 30 seconds and reconnects the WLAN after three consecutive missed replies. | Read only | — |
| `wlan-ping-time` | `String` | Gateway ping response time as text, including the unit supplied by Robonect. | Read only | — |
| `wlan-signal` | `Number` | WLAN signal quality, converted to a scale from 0 (weakest) to 4 (strongest). | Read only | — |
| `wlan-signal-strength` | `Number:Power [dBm]` | Received WLAN signal strength in dBm. | Read only | — |
| `wlan-ssid` | `String` | Network name of the WLAN configured in Robonect. | Read only | — |

### Extended Channels

<details>

<summary>Show extended channels</summary>

| Channel ID | Item Type | Description | Read/Write | Notes |
| ----------- | --------- | ----------- | ---------- | ----- |
| `battery-<n>-capacity` | `Number:ElectricCharge [mAh]` | Full battery capacity reported for battery slot <n>. | Read only | Dynamically added from the battery API response; `n` start at 1. |
| `battery-<n>-charge` | `Number:Dimensionless [%]` | Battery charge level for battery slot <n>, as a percentage. | Read only | Dynamically added from the battery API response; `n` start at 1. |
| `battery-<n>-current` | `Number:ElectricCurrent [mA]` | Signed battery current for battery slot <n>. | Read only | Dynamically added from the battery API response; `n` start at 1. |
| `battery-<n>-id` | `Number` | Identifier reported for battery slot <n>. | Read only | Dynamically added from the battery API response; `n` start at 1. |
| `battery-<n>-remaining-capacity` | `Number:ElectricCharge [mAh]` | Remaining battery capacity reported for battery slot <n>. | Read only | Dynamically added from the battery API response; `n` start at 1. |
| `battery-<n>-temperature` | `Number:Temperature [°C]` | Battery temperature for battery slot <n>. | Read only | Dynamically added from the battery API response; `n` start at 1. |
| `battery-<n>-voltage` | `Number:ElectricPotential [V]` | Battery terminal voltage for battery slot <n>. | Read only | Dynamically added from the battery API response; `n` start at 1. |
| `blade-motor-average` | `Number` | Unscaled average diagnostic value reported for the blade motor. | Read only | Also refreshed on normal polls while mowing or searching. |
| `blade-motor-current` | `Number:ElectricCurrent [mA]` | Electric current drawn by the blade motor. | Read only | Also refreshed on normal polls while mowing or searching. |
| `blade-motor-speed` | `Number:Frequency [rpm]` | Rotational speed of the blade motor. | Read only | Also refreshed on normal polls while mowing or searching. |
| `door-arrested` | `Switch` | Whether the optional garage-door system reports that the door is arrested. | Read only | — |
| `door-enabled` | `Switch` | Whether the optional garage-door system is enabled. | Read only | — |
| `door-state` | `Rollershutter [%]` | Position of the optional garage door: 0% open, 100% closed, 75% opening, and 25% closing. UP opens, DOWN closes, and STOP releases the door. | Read/Write (See notes) | Send `UP`, `DOWN`, or `STOP`. |
| `extension-gpio-1-flash` | `Switch` | Whether the extension is configured to flash when the mower reports an error. Read-only in this binding. | Read only | — |
| `extension-gpio-1-inverted` | `Switch` | Whether the extension signal is configured as active-low. Read-only in this binding. | Read only | — |
| `extension-gpio-1-status` | `Switch` | Current on/off state reported for the extension. Read-only in this binding. | Read only | — |
| `extension-gpio-2-flash` | `Switch` | Whether the extension is configured to flash when the mower reports an error. Read-only in this binding. | Read only | — |
| `extension-gpio-2-inverted` | `Switch` | Whether the extension signal is configured as active-low. Read-only in this binding. | Read only | — |
| `extension-gpio-2-status` | `Switch` | Current on/off state reported for the extension. Read-only in this binding. | Read only | — |
| `extension-output-1-flash` | `Switch` | Whether the extension is configured to flash when the mower reports an error. Read-only in this binding. | Read only | — |
| `extension-output-1-inverted` | `Switch` | Whether the extension signal is configured as active-low. Read-only in this binding. | Read only | — |
| `extension-output-1-status` | `Switch` | Current on/off state reported for the extension. Read-only in this binding. | Read only | — |
| `extension-output-2-flash` | `Switch` | Whether the extension is configured to flash when the mower reports an error. Read-only in this binding. | Read only | — |
| `extension-output-2-inverted` | `Switch` | Whether the extension signal is configured as active-low. Read-only in this binding. | Read only | — |
| `extension-output-2-status` | `Switch` | Current on/off state reported for the extension. Read-only in this binding. | Read only | — |
| `left-drive-current` | `Number:ElectricCurrent [mA]` | Electric current drawn by the motor. | Read only | Also refreshed on normal polls while mowing or searching. |
| `left-drive-power` | `Number:Dimensionless [%]` | Power level of the drive motor as a percentage. | Read only | Also refreshed on normal polls while mowing or searching. |
| `left-drive-speed` | `Number:Speed [cm/s]` | Speed of the drive wheel. | Read only | Also refreshed on normal polls while mowing or searching. |
| `portal-connected` | `Switch` | Whether Robonect is connected to the remote-access service. | Read only | — |
| `portal-enabled` | `Switch` | Whether the mower.online remote-access service is enabled. | Read only | — |
| `portal-host` | `String` | Host name reported for access through the remote-access service. | Read only | — |
| `portal-url` | `String` | URL for remote access to the Robonect web interface; returned only while the remote service is connected. | Read only | — |
| `push-interval` | `Number:Time [s]` | Interval for cyclic push-service requests. | Read/Write | — |
| `push-mode` | `Number` | Push-service request method: 0 disables push, 1 uses GET, and 2 uses POST. | Read/Write (See notes) | Created only when `useWebApi` is enabled; command updates the web-interface setting. |
| `push-password` | `String` | Write-only password for push-service authentication. Accepts a new password as a command; its state remains undefined and automatic state prediction is disabled. | Write only (See notes) | Must match Thing `pushPass`; write-only, so set Item `autoupdate="false"`. |
| `push-trigger-<id>-<name>-enter` | `Switch` | Whether Robonect sends a push request when entering the named mower state. | Read/Write (See notes) | Dynamically added from push API responses; commands preserve the other trigger setting. |
| `push-trigger-<id>-<name>-leave` | `Switch` | Whether Robonect sends a push request when leaving the named mower state. | Read/Write (See notes) | Dynamically added from push API responses; commands preserve the other trigger setting. |
| `push-url` | `String` | Destination URL for Robonect push-service requests. | Read/Write (See notes) | Set callback URL to `http://<openhab-host>:8080/robonect/<thing-id>`; Basic credentials use plain HTTP. |
| `push-username` | `String` | Username used to authenticate push-service requests. | Read/Write (See notes) | Must match Thing `pushUser`. |
| `remote-<n>-corridor-cut` | `Switch` | Whether CorridorCut is enabled for the remote-start point. Commands require useWebApi and save the setting through a web-interface request. | Read/Write (See notes) | Requires `useWebApi`. |
| `remote-<n>-distance` | `Number:Length [m]` | Distance from the charging station to the remote-start point along its selected route. | Read/Write (See notes) | Range: 0–300 m. |
| `remote-<n>-name` | `String` | Name of the remote-start point, limited to 16 characters. | Read/Write (See notes) | Commands longer than 16 characters are truncated. |
| `remote-<n>-path` | `Number` | Route followed to the remote-start point, using the path code reported by the mower. Commands require useWebApi and a web-interface request. | Read/Write (See notes) | Requires `useWebApi`. |
| `remote-<n>-proportion` | `Number:Dimensionless [%]` | Frequency with which the mower selects this remote-start point, as a percentage of starts. | Read/Write (See notes) | Range: 0–100%. |
| `remote-<n>-visible` | `Switch` | Whether the remote-start point is visible in the Robonect web interface. Commands require useWebApi and save the setting through a web-interface request. | Read/Write (See notes) | Requires `useWebApi`. |
| `right-drive-current` | `Number:ElectricCurrent [mA]` | Electric current drawn by the motor. | Read only | Also refreshed on normal polls while mowing or searching. |
| `right-drive-power` | `Number:Dimensionless [%]` | Power level of the drive motor as a percentage. | Read only | Also refreshed on normal polls while mowing or searching. |
| `right-drive-speed` | `Number:Speed [cm/s]` | Speed of the drive wheel. | Read only | Also refreshed on normal polls while mowing or searching. |
| `statistics-charges` | `Number` | Number of full battery charges. | Read only | — |
| `statistics-charging-hours` | `Number:Time [h]` | Total time spent charging. | Read only | — |
| `statistics-errors` | `Number` | Number of errors recorded by the mower. | Read only | — |
| `statistics-mowing` | `String` | Recent mowing durations as a comma-separated list of minutes, in the order returned by the mower. | Read only | — |
| `statistics-mowing-average` | `Number:Time [min]` | Average recent mowing duration. | Read only | — |
| `statistics-mowing-hours` | `Number:Time [h]` | Total time spent mowing. | Read only | — |
| `statistics-mowing-max` | `Number:Time [min]` | Longest recent mowing duration. | Read only | — |
| `statistics-mowing-min` | `Number:Time [min]` | Shortest recent mowing duration. | Read only | — |
| `statistics-running-hours` | `Number:Time [h]` | Total mower running time. | Read only | — |
| `statistics-searching-hours` | `Number:Time [h]` | Total time spent searching for the charging station. | Read only | — |
| `statistics-seek` | `String` | Recent charging-station search durations as a comma-separated list of minutes, in the order returned by the mower. | Read only | — |
| `statistics-seek-average` | `Number:Time [min]` | Average recent charging-station search duration. | Read only | — |
| `statistics-seek-max` | `Number:Time [min]` | Longest recent charging-station search duration. | Read only | — |
| `statistics-seek-min` | `Number:Time [min]` | Shortest recent charging-station search duration. | Read only | — |
| `timer-<id>-<day>` | `Switch` | Whether timer <id> applies on the named weekday (`monday` through `sunday`). | Read/Write | Dynamically added from timer API responses. |
| `timer-<id>-enabled` | `Switch` | Whether timer <id> allows mowing during its configured time window on selected weekdays. Other mower restrictions and charging pauses still apply. | Read/Write | Dynamically added from timer API responses. |
| `timer-<id>-end` | `DateTime` | End time of timer <id>’s allowed mowing window; only the time of day is meaningful. | Read/Write | Dynamically added from timer API responses; times use `timezone`. |
| `timer-<id>-start` | `DateTime` | Start time of timer <id>’s allowed mowing window; only the time of day is meaningful. | Read/Write | Dynamically added from timer API responses; times use `timezone`. |
| `weather-break` | `Switch` | Whether the weather service is currently preventing mowing. | Read only | — |
| `weather-city` | `String` | City name resolved by the weather service. | Read only | — |
| `weather-condition-day` | `Switch` | Whether the weather service reports an active daytime mowing restriction. | Read only | — |
| `weather-condition-night` | `Switch` | Whether the weather service reports an active nighttime mowing restriction. | Read only | — |
| `weather-condition-too-cold` | `Switch` | Whether the weather service reports that low temperature prevents mowing. | Read only | — |
| `weather-condition-too-dry` | `Switch` | Whether the weather service reports that low air humidity prevents mowing. | Read only | — |
| `weather-condition-too-rainy` | `Switch` | Whether the weather service reports that rain prevents mowing. | Read only | — |
| `weather-condition-too-warm` | `Switch` | Whether the weather service reports that high temperature prevents mowing. | Read only | — |
| `weather-condition-too-wet` | `Switch` | Whether the weather service reports that high air humidity prevents mowing. | Read only | — |
| `weather-config-max-humidity` | `Number:Dimensionless [%]` | Maximum relative air humidity allowed for mowing. | Read/Write (See notes) | — |
| `weather-config-max-rain` | `Number:Dimensionless [%]` | Maximum rain intensity allowed before Robonect pauses mowing. | Read/Write (See notes) | — |
| `weather-config-max-temperature` | `Number:Temperature [°C]` | Maximum outdoor temperature allowed for mowing. | Read/Write (See notes) | — |
| `weather-config-min-humidity` | `Number:Dimensionless [%]` | Minimum relative air humidity allowed for mowing. | Read/Write (See notes) | — |
| `weather-config-min-temperature` | `Number:Temperature [°C]` | Minimum outdoor temperature allowed for mowing. | Read/Write (See notes) | — |
| `weather-config-skip-day` | `Switch` | Prevent mowing during daytime as determined by the weather service. | Read/Write (See notes) | — |
| `weather-config-skip-night` | `Switch` | Prevent mowing during nighttime as determined by the weather service. | Read/Write (See notes) | — |
| `weather-day` | `Switch` | Whether the weather service currently reports daytime. | Read only | — |
| `weather-humidity` | `Number:Dimensionless [%]` | Current relative air humidity reported by the weather service. | Read only | — |
| `weather-icon` | `String` | Weather icon URL or URL template returned by the service, including any %s placeholder. | Read only | — |
| `weather-location-cityid` | `Number` | City identifier used to select the weather-service location. Accepts a non-negative integer. | Read/Write (See notes) | Command: `cmd=weather&cityid=<value>`. |
| `weather-location-country` | `String` | Country returned for the weather-service location. | Read only | — |
| `weather-location-zip` | `String` | Postal code used to select the weather-service location. Sent as text to preserve leading zeroes. | Read/Write (See notes) | Command: `cmd=weather&zip=<value>`; ZIP is sent as text to preserve leading zeroes. |
| `weather-rain` | `Number:Dimensionless [%]` | Current rain intensity reported by the weather service, as a percentage. | Read only | — |
| `weather-remaining` | `Number:Time [min]` | Remaining waiting time before mowing may resume after a weather event. | Read only | — |
| `weather-service-enabled` | `Switch` | Enable or disable the weather service used by Robonect. | Read/Write (See notes) | — |
| `weather-service-interval` | `Number:Time [min]` | Interval at which Robonect requests new data from the weather service. | Read/Write (See notes) | With `useWebApi`, reads the web-interface value if the weather API omits it. |
| `weather-sunrise` | `DateTime` | Sunrise time returned by the weather service. | Read only | Uses `timezone`. |
| `weather-sunset` | `DateTime` | Sunset time returned by the weather service. | Read only | Uses `timezone`. |
| `weather-temperature` | `Number:Temperature [°C]` | Current outdoor temperature reported by the weather service. | Read only | — |
| `weather-timestamp` | `DateTime` | Time of the weather data. | Read only | Uses `timestamp.unix` and `timezone`. |
| `wire-sensor-<n>-a` | `Number` | Unscaled A loop-signal diagnostic value reported by boundary-wire sensor <n>. | Read only | Dynamically added from the wire-sensor API response; `n` start at 1. |
| `wire-sensor-<n>-f` | `Number` | Unscaled F loop-signal diagnostic value reported by boundary-wire sensor <n>. | Read only | Dynamically added from the wire-sensor API response; `n` start at 1. |
| `wire-sensor-<n>-g1` | `Number` | Unscaled G1 loop-signal diagnostic value reported by boundary-wire sensor <n>. | Read only | Dynamically added from the wire-sensor API response; `n` start at 1. |
| `wire-sensor-<n>-g2` | `Number` | Unscaled G2 loop-signal diagnostic value reported by boundary-wire sensor <n>. | Read only | Dynamically added from the wire-sensor API response; `n` start at 1. |
| `wire-sensor-<n>-label` | `String` | Position or description reported for boundary-wire sensor <n>. | Read only | Dynamically added from the wire-sensor API response; `n` start at 1. |
| `wire-sensor-<n>-n` | `Number` | Unscaled N loop-signal diagnostic value reported by boundary-wire sensor <n>. | Read only | Dynamically added from the wire-sensor API response; `n` start at 1. |
| `wire-sensor-<n>-quality` | `Number:Dimensionless [%]` | Signal quality of boundary-wire sensor <n>, as a percentage. | Read only | Dynamically added from the wire-sensor API response; `n` start at 1. |

</details>

## Thing Actions and Custom Channels

Use a `jobType` or `directType` Switch channel for a reusable preset, or call the matching Thing Action from a rule. Each operation has one set of parameter names and meanings shared by its channel and action; the table calls out any difference in accepted values.

### Job

A `jobType` channel starts a job on `ON` and stops the active job on `OFF`. The **Mowing Job** Thing Action starts a job and returns whether Robonect accepted the request. Both use these settings:

| Setting | Values and defaults |
| ------- | ------------------- |
| `durationMinutes` | Job duration in minutes, from 1 to 1439. Defaults to 60 minutes, if `endTime` is also omitted. |
| `startTime` | Optional local mower time in quoted `HH:mm` format. Defaults to current time, if `endTime` or `durationMinutes` is also omitted.  |
| `endTime` | Optional local mower time in quoted `HH:mm` format. |
| `remoteStart` | `STANDARD`, `DOCKING_STATION`, or `REMOTE_1` through `REMOTE_5`; defaults to `STANDARD`. |
| `afterMode` | `AUTO`, `HOME`, `EOD`, or `MANUAL`. When omitted, the mower returns to its previous mode after the job is finished. |

If both `startTime` and `endTime` are set, they determine the binding's estimated job window; a configured `durationMinutes` is still sent to the mower but does not affect that estimate.

The function definition for the **Mowing Job** Thing Action looks like this:

```java
public boolean startJob(@Nullable Integer durationMinutes, @Nullable String startTime, @Nullable String endTime, @Nullable String remoteStart, @Nullable String afterMode) {...}
```

### Drive Wheel

For a wheel command, `directType` sends its preset once when the Switch receives `ON`; `OFF` does nothing. The **Drive Wheels** Thing Action sends the same command directly and returns whether Robonect accepted it. Both use these required settings:

| Setting | Values |
| ------- | ------ |
| `left` | Left wheel speed, -100 to 100 percent. |
| `right` | Right wheel speed, -100 to 100 percent. |
| `timeout` | Command duration, 1 to 5000 milliseconds. |

The function definition for the **Drive Wheels** Thing Action looks like this:

```java
public boolean drive(@Nullable Integer left, @Nullable Integer right, @Nullable Integer timeout) {...}
```

### Example Custom Channel

Add custom channels to the mower Thing's `Channels` block and link Items to the IDs:

```java
Thing robonect:mower:automower "Mower" [host="192.168.1.10"] {
    Channels:
        Type jobType : job#oneHour [durationMinutes=60, remoteStart="REMOTE_1", afterMode="AUTO"]
        Type directType : direct#forward [left=100, right=100, timeout=5000]
        Type directType : direct#rotateRight90 [left=100, right=-100, timeout=1000]
        Type directType : direct#rotateLeft90 [left=-100, right=100, timeout=1000]
}
```

```java
Switch OneHourJob "One-hour mowing job" {channel="robonect:mower:automower:job#oneHour"}
Switch DriveForward "Drive forward" {channel="robonect:mower:automower:direct#forward"}
Switch TurnRight "Turn right" {channel="robonect:mower:automower:direct#rotateRight90"}
Switch TurnLeft "Turn left" {channel="robonect:mower:automower:direct#rotateLeft90"}
```

### Example Thing Action

Call either action with the shared settings from a rule. This Rules DSL example starts a one-hour job and then sends a 1.5-second forward command:

```java
val mowerActions = getActions("robonect", "robonect:mower:automower")
val startJob = mowerActions.getClass().getMethod("startJob", Integer, String, String, String, String)
startJob.invoke(mowerActions, 60, null, null, "REMOTE_1", "AUTO")
val drive = mowerActions.getClass().getMethod("drive", Integer, Integer, Integer)
drive.invoke(mowerActions, 100, 100, 1500)
```

## Full Example

<details>

<summary>Things file `.things`</summary>

```java
Thing robonect:mower:automower "Mower" @ "Garden" [ host="192.168.1.123", pollInterval=60, user="user", password = "pass", pollIntervalExtended = 1800, pushUser = "user2", pushPass = "pass2", useAjaxApi = true, useWebApi = true]
{
    Channels:
      Type jobType : job#own1  [durationMinutes=60]
      Type jobType : job#own2  [durationMinutes=180, startTime="17:36", remoteStart="REMOTE_5", afterMode="AUTO"]
      Type jobType : job#own3  [startTime="12:36", endTime="23:59", remoteStart="REMOTE_3"]
      Type jobType : job#own4  [durationMinutes=60, endTime="22:35", remoteStart="DOCKING_STATION"]
      Type directType : direct#rotateRight90 [left=100, right=-100, timeout=1000]
      Type directType : direct#rotateRight180 [left=100, right=-100, timeout=1500]
      Type directType : direct#rotateLeft90 [left=-100, right=100, timeout=1000]
      Type directType : direct#rotateLeft180 [left=-100, right=100, timeout=1500]
      Type directType : direct#turnRightSmall90 [left=100, right=0, timeout=1300]
      Type directType : direct#turnLeftSmall90 [left=0, right=100, timeout=1300]
      Type directType : direct#turnRightBig90 [left=100, right=75, timeout=5000]
      Type directType : direct#turnLeftBig90 [left=75, right=100, timeout=5000]
      Type directType : direct#forwardSmall [left=100, right=100, timeout=1500]
      Type directType : direct#forwardBig [left=100, right=100, timeout=5000]
      Type directType : direct#reverseSmall [left=-100, right=-100, timeout=1500]
      Type directType : direct#reverseBig [left=-100, right=-100, timeout=5000]

}
```

</details>

<details>

<summary>Items file `.items`</summary>

```java
// ===== Service commands (service operations) =====
String Robonect_ServiceOperation "Robonect Service Operation" <settings> { channel="robonect:mower:automower:service-operation" }

// ===== Jobs (/json?cmd=mode and AJAX job data) =====
DateTime Robonect_JobStart "Mowing Job Start Time [%1$tH:%1$tM]" <time> { channel="robonect:mower:automower:job-start" }
DateTime Robonect_JobEnd "Mowing Job End Time [%1$tH:%1$tM]" <time> { channel="robonect:mower:automower:job-end" }
Switch Robonect_Job "Mowing Job" <lawnmower> { channel="robonect:mower:automower:job" }
Switch Robonect_Job_Own1 "Own Mowing Job 1" <lawnmower> { channel="robonect:mower:automower:job#own1" }
Switch Robonect_Job_Own2 "Own Mowing Job 2" <lawnmower> { channel="robonect:mower:automower:job#own2" }
Switch Robonect_Job_Own3 "Own Mowing Job 3" <lawnmower> { channel="robonect:mower:automower:job#own3" }
Switch Robonect_Job_Own4 "Own Mowing Job 4" <lawnmower> { channel="robonect:mower:automower:job#own4" }

// ===== Direct wheel control (/json?cmd=direct) =====
Switch Robonect_DirectRotateRight90 "Rotate Right 90°" <motion> { channel="robonect:mower:automower:direct#rotateRight90" }
Switch Robonect_DirectRotateRight180 "Rotate Right 180°" <motion> { channel="robonect:mower:automower:direct#rotateRight180" }
Switch Robonect_DirectRotateLeft90 "Rotate Left 90°" <motion> { channel="robonect:mower:automower:direct#rotateLeft90" }
Switch Robonect_DirectRotateLeft180 "Rotate Left 180°" <motion> { channel="robonect:mower:automower:direct#rotateLeft180" }
Switch Robonect_DirectTurnRightSmall90 "Small Right Turn" <motion> { channel="robonect:mower:automower:direct#turnRightSmall90" }
Switch Robonect_DirectTurnLeftSmall90 "Small Left Turn" <motion> { channel="robonect:mower:automower:direct#turnLeftSmall90" }
Switch Robonect_DirectTurnRightBig90 "Large Right Turn" <motion> { channel="robonect:mower:automower:direct#turnRightBig90" }
Switch Robonect_DirectTurnLeftBig90 "Large Left Turn" <motion> { channel="robonect:mower:automower:direct#turnLeftBig90" }
Switch Robonect_DirectForwardSmall "Short Forward" <motion> { channel="robonect:mower:automower:direct#forwardSmall" }
Switch Robonect_DirectForwardBig "Long Forward" <motion> { channel="robonect:mower:automower:direct#forwardBig" }
Switch Robonect_DirectReverseSmall "Short Reverse" <motion> { channel="robonect:mower:automower:direct#reverseSmall" }
Switch Robonect_DirectReverseBig "Long Reverse" <motion> { channel="robonect:mower:automower:direct#reverseBig" }

// ===== Error reset (/json?cmd=error&reset) =====
Switch Robonect_ResetError "Reset Mower Error" <error> { channel="robonect:mower:automower:reset-error" }

// ===== Status (/json?cmd=status) =====
String Robonect_Name "Mower Name" <lawnmower> { channel="robonect:mower:automower:name" }
String Robonect_Id "Mower ID" <text> { channel="robonect:mower:automower:id" }
DateTime Robonect_Timestamp "Mower Time" <time> { channel="robonect:mower:automower:timestamp" }
Number:Dimensionless Robonect_Battery "Mower Battery Level" <battery> { unit="%", channel="robonect:mower:automower:battery" }
Number:Time Robonect_MowingHours "Mower Operating Hours" <time> { unit="h", channel="robonect:mower:automower:mowing-hours" }
String Robonect_Mode "Mower Mode" <lawnmower> { channel="robonect:mower:automower:mode" }
Switch Robonect_Start "Mower Start" { channel="robonect:mower:automower:start" }
Number Robonect_Status "Mower Status" <lawnmower> { channel="robonect:mower:automower:status" }
Number Robonect_Prevent "Mowing Prevention Code" { channel="robonect:mower:automower:prevent" }
Switch Robonect_Home "Mower at Charging Station [%s]" <switch> { channel="robonect:mower:automower:home" }
Number:Time Robonect_StatusDuration "Current Status Duration" <time> { unit="min", channel="robonect:mower:automower:status-duration" }
Number:Length Robonect_StatusDistance "Current Remote-Start Distance" <time> { unit="m", channel="robonect:mower:automower:status-distance" }
String Robonect_TimerStatus "Mowing Schedule Status" <time> { channel="robonect:mower:automower:timer-status" }
DateTime Robonect_TimerNext "Next Scheduled Start" <time> { channel="robonect:mower:automower:timer-next" }
Number Robonect_ErrorCode "Current Error Code" <error> { channel="robonect:mower:automower:error-code" }
String Robonect_ErrorMessage "Current Error Message" <error> { channel="robonect:mower:automower:error-message" }
DateTime Robonect_ErrorDate "Current Error Time" <error> { channel="robonect:mower:automower:error-date" }
Number Robonect_LastErrorCode "Last Recorded Error Code" <error> { channel="robonect:mower:automower:last-error-code" }
String Robonect_LastErrorMessage "Last Recorded Error Message" <error> { channel="robonect:mower:automower:last-error-message" }
DateTime Robonect_LastErrorDate "Last Recorded Error Time" <error> { channel="robonect:mower:automower:last-error-date" }
Number:Temperature Robonect_HealthTemperature "Robonect Internal Temperature" <temperature> { unit="°C", channel="robonect:mower:automower:health-temperature" }
Number:Dimensionless Robonect_HealthHumidity "Robonect Internal Humidity" <humidity> { unit="%", channel="robonect:mower:automower:health-humidity" }
Number:Dimensionless Robonect_BladesQuality "Blade Remaining Quality" <status> { unit="%", channel="robonect:mower:automower:blades-quality" }
Number:Time Robonect_BladesReplacementDays "Blade Age" <time> { unit="d", channel="robonect:mower:automower:blades-replacement-days" }
Number:Time Robonect_BladesUsageHours "Blade Operating Time" <time> { unit="h", channel="robonect:mower:automower:blades-usage-hours" }

// ===== Door (/json?cmd=door) =====
Switch Robonect_DoorEnabled "Garage Door Enabled [%s]" <switch> { channel="robonect:mower:automower:door-enabled" }
Rollershutter Robonect_DoorState "Garage Door Position" { channel="robonect:mower:automower:door-state" }
Switch Robonect_DoorArrested "Garage Door Arrested [%s]" <switch> { channel="robonect:mower:automower:door-arrested" }

// ===== WLAN (/json?cmd=wlan) =====
Number Robonect_WlanSignal "WLAN Signal Quality" <qualityofservice> { channel="robonect:mower:automower:wlan-signal" }
Number:Power Robonect_WlanSignalStrength "WLAN Signal Strength" <network> { unit="dBm", channel="robonect:mower:automower:wlan-signal-strength" }
Switch Robonect_WlanEnabled "WLAN Enabled [%s]" <network> { channel="robonect:mower:automower:wlan-enabled" }
String Robonect_WlanSsid "WLAN SSID" <network> { channel="robonect:mower:automower:wlan-ssid" }
String Robonect_WlanIp "WLAN IP Address" <network> { channel="robonect:mower:automower:wlan-ip" }
String Robonect_WlanMac "WLAN MAC Address" <network> { channel="robonect:mower:automower:wlan-mac" }
Switch Robonect_WlanDhcp "WLAN DHCP Client [%s]" <switch> { channel="robonect:mower:automower:wlan-dhcp" }
Switch Robonect_WlanPing "Gateway Ping Monitoring [%s]" <switch> { channel="robonect:mower:automower:wlan-ping" }
String Robonect_WlanPingTime "Gateway Ping Time" <time> { channel="robonect:mower:automower:wlan-ping-time" }
String Robonect_WlanNetmask "WLAN Netmask" <network> { channel="robonect:mower:automower:wlan-netmask" }
String Robonect_WlanGateway "WLAN Gateway" <network> { channel="robonect:mower:automower:wlan-gateway" }
Switch Robonect_WlanApEnabled "Access Point Enabled [%s]" <switch> { channel="robonect:mower:automower:wlan-ap-enabled" }
String Robonect_WlanApSsid "Access Point SSID" <network> { channel="robonect:mower:automower:wlan-ap-ssid" }
String Robonect_WlanApMac "Access Point MAC Address" <network> { channel="robonect:mower:automower:wlan-ap-mac" }
Switch Robonect_WlanApHidden "Access Point Hidden [%s]" <switch> { channel="robonect:mower:automower:wlan-ap-hidden" }
Number Robonect_WlanApChannel "Access Point Radio Channel" <network> { channel="robonect:mower:automower:wlan-ap-channel" }
String Robonect_WlanApEncryption "Access Point Encryption" <network> { channel="robonect:mower:automower:wlan-ap-encryption" }
Number Robonect_WlanApMaxConnections "Access Point Client Limit" <network> { channel="robonect:mower:automower:wlan-ap-max-connections" }
String Robonect_WlanApIp "Access Point IP Address" <network> { channel="robonect:mower:automower:wlan-ap-ip" }
String Robonect_WlanApNetmask "Access Point Netmask" <network> { channel="robonect:mower:automower:wlan-ap-netmask" }
String Robonect_WlanApGateway "Access Point Gateway" <network> { channel="robonect:mower:automower:wlan-ap-gateway" }
Switch Robonect_WlanApDhcp "Access Point DHCP Server [%s]" <switch> { channel="robonect:mower:automower:wlan-ap-dhcp" }
String Robonect_WlanApDhcpStart "Access Point DHCP Range Start" <network> { channel="robonect:mower:automower:wlan-ap-dhcp-start" }
String Robonect_WlanApDhcpEnd "Access Point DHCP Range End" <network> { channel="robonect:mower:automower:wlan-ap-dhcp-end" }

// ===== Battery (/json?cmd=battery) =====
// Battery 1 (dynamic channels)
Number Robonect_Battery1Id "Battery 1 ID" <text> { channel="robonect:mower:automower:battery-1-id" }
Number:Dimensionless Robonect_Battery1Charge "Battery 1 Charge" <battery> { unit="%", channel="robonect:mower:automower:battery-1-charge" }
Number:ElectricPotential Robonect_Battery1Voltage "Battery 1 Voltage" <energy> { unit="V", channel="robonect:mower:automower:battery-1-voltage" }
Number:ElectricCurrent Robonect_Battery1Current "Battery 1 Current" <energy> { unit="mA", channel="robonect:mower:automower:battery-1-current" }
Number:Temperature Robonect_Battery1Temperature "Battery 1 Temperature" <temperature> { unit="°C", channel="robonect:mower:automower:battery-1-temperature" }
Number:ElectricCharge Robonect_Battery1Capacity "Battery 1 Capacity" <energy> { unit="mAh", channel="robonect:mower:automower:battery-1-capacity" }
Number:ElectricCharge Robonect_Battery1RemainingCapacity "Battery 1 Remaining Capacity" <energy> { unit="mAh", channel="robonect:mower:automower:battery-1-remaining-capacity" }

// ===== Motors (/json?cmd=motor) =====
Number:Dimensionless Robonect_LeftDrivePower "Left Drive Power Level" <motion> { unit="%", channel="robonect:mower:automower:left-drive-power" }
Number:Speed Robonect_LeftDriveSpeed "Left Drive Speed" <motion> { unit="cm/s", channel="robonect:mower:automower:left-drive-speed" }
Number:ElectricCurrent Robonect_LeftDriveCurrent "Left Drive Current" <energy> { unit="mA", channel="robonect:mower:automower:left-drive-current" }
Number:Dimensionless Robonect_RightDrivePower "Right Drive Power Level" <motion> { unit="%", channel="robonect:mower:automower:right-drive-power" }
Number:Speed Robonect_RightDriveSpeed "Right Drive Speed" <motion> { unit="cm/s", channel="robonect:mower:automower:right-drive-speed" }
Number:ElectricCurrent Robonect_RightDriveCurrent "Right Drive Current" <energy> { unit="mA", channel="robonect:mower:automower:right-drive-current" }
Number Robonect_BladeMotorAverage "Blade Motor Average" <motion> { channel="robonect:mower:automower:blade-motor-average" } // what average is this?
Number:Frequency Robonect_BladeMotorSpeed "Blade Motor Speed" <motion> { unit="rpm", channel="robonect:mower:automower:blade-motor-speed" }
Number:ElectricCurrent Robonect_BladeMotorCurrent "Blade Motor Current" <energy> { unit="mA", channel="robonect:mower:automower:blade-motor-current" }

// ===== Statistics (/json?cmd=hour) =====
Number:Time Robonect_StatisticsRunningHours "Total Running Time" <time> { unit="h", channel="robonect:mower:automower:statistics-running-hours" }
Number:Time Robonect_StatisticsMowingHours "Total Mowing Time" <time> { unit="h", channel="robonect:mower:automower:statistics-mowing-hours" }
Number:Time Robonect_StatisticsSearchingHours "Total Charging-Station Search Time" <time> { unit="h", channel="robonect:mower:automower:statistics-searching-hours" }
Number:Time Robonect_StatisticsChargingHours "Total Charging Time" <time> { unit="h", channel="robonect:mower:automower:statistics-charging-hours" }
Number Robonect_StatisticsCharges "Full Charge Count" <energy> { channel="robonect:mower:automower:statistics-charges" }
Number Robonect_StatisticsErrors "Recorded Error Count" <error> { channel="robonect:mower:automower:statistics-errors" }
String Robonect_StatisticsSeek "Recent Charging-Station Search Durations" { channel="robonect:mower:automower:statistics-seek" }
Number:Time Robonect_StatisticsSeekMin "Minimum Charging-Station Search Duration" <time> { unit="min", channel="robonect:mower:automower:statistics-seek-min" }
Number:Time Robonect_StatisticsSeekMax "Maximum Charging-Station Search Duration" <time> { unit="min", channel="robonect:mower:automower:statistics-seek-max" }
Number:Time Robonect_StatisticsSeekAverage "Average Charging-Station Search Duration" <time> { unit="min", channel="robonect:mower:automower:statistics-seek-average" }
String Robonect_StatisticsMowing "Recent Mowing Durations" { channel="robonect:mower:automower:statistics-mowing" }
Number:Time Robonect_StatisticsMowingMin "Minimum Mowing Duration" <time> { unit="min", channel="robonect:mower:automower:statistics-mowing-min" }
Number:Time Robonect_StatisticsMowingMax "Maximum Mowing Duration" <time> { unit="min", channel="robonect:mower:automower:statistics-mowing-max" }
Number:Time Robonect_StatisticsMowingAverage "Average Mowing Duration" <time> { unit="min", channel="robonect:mower:automower:statistics-mowing-average" }

// ===== Remote starts (/json?cmd=remote) =====
String Robonect_Remote1Name "Remote Start 1 Name" { channel="robonect:mower:automower:remote-1-name", autoupdate="false" }
Switch Robonect_Remote1Visible "Remote Start 1 Visible" { channel="robonect:mower:automower:remote-1-visible", autoupdate="false" }
Number Robonect_Remote1Path "Remote Start 1 Route" { channel="robonect:mower:automower:remote-1-path", autoupdate="false" }
Number:Dimensionless Robonect_Remote1Proportion "Remote Start 1 Selection Frequency" { unit="%", channel="robonect:mower:automower:remote-1-proportion", autoupdate="false" }
Number:Length Robonect_Remote1Distance "Remote Start 1 Distance" { unit="m", channel="robonect:mower:automower:remote-1-distance", autoupdate="false" }
Switch Robonect_Remote1CorridorCut "Remote Start 1 CorridorCut" { channel="robonect:mower:automower:remote-1-corridor-cut", autoupdate="false" }
String Robonect_Remote2Name "Remote Start 2 Name" { channel="robonect:mower:automower:remote-2-name", autoupdate="false" }
Switch Robonect_Remote2Visible "Remote Start 2 Visible" { channel="robonect:mower:automower:remote-2-visible", autoupdate="false" }
Number Robonect_Remote2Path "Remote Start 2 Route" { channel="robonect:mower:automower:remote-2-path", autoupdate="false" }
Number:Dimensionless Robonect_Remote2Proportion "Remote Start 2 Selection Frequency" { unit="%", channel="robonect:mower:automower:remote-2-proportion", autoupdate="false" }
Number:Length Robonect_Remote2Distance "Remote Start 2 Distance" { unit="m", channel="robonect:mower:automower:remote-2-distance", autoupdate="false" }
Switch Robonect_Remote2CorridorCut "Remote Start 2 CorridorCut" { channel="robonect:mower:automower:remote-2-corridor-cut", autoupdate="false" }
String Robonect_Remote3Name "Remote Start 3 Name" { channel="robonect:mower:automower:remote-3-name", autoupdate="false" }
Switch Robonect_Remote3Visible "Remote Start 3 Visible" { channel="robonect:mower:automower:remote-3-visible", autoupdate="false" }
Number Robonect_Remote3Path "Remote Start 3 Route" { channel="robonect:mower:automower:remote-3-path", autoupdate="false" }
Number:Dimensionless Robonect_Remote3Proportion "Remote Start 3 Selection Frequency" { unit="%", channel="robonect:mower:automower:remote-3-proportion", autoupdate="false" }
Number:Length Robonect_Remote3Distance "Remote Start 3 Distance" { unit="m", channel="robonect:mower:automower:remote-3-distance", autoupdate="false" }
Switch Robonect_Remote3CorridorCut "Remote Start 3 CorridorCut" { channel="robonect:mower:automower:remote-3-corridor-cut", autoupdate="false" }
String Robonect_Remote4Name "Remote Start 4 Name" { channel="robonect:mower:automower:remote-4-name", autoupdate="false" }
Switch Robonect_Remote4Visible "Remote Start 4 Visible" { channel="robonect:mower:automower:remote-4-visible", autoupdate="false" }
Number Robonect_Remote4Path "Remote Start 4 Route" { channel="robonect:mower:automower:remote-4-path", autoupdate="false" }
Number:Dimensionless Robonect_Remote4Proportion "Remote Start 4 Selection Frequency" { unit="%", channel="robonect:mower:automower:remote-4-proportion", autoupdate="false" }
Number:Length Robonect_Remote4Distance "Remote Start 4 Distance" { unit="m", channel="robonect:mower:automower:remote-4-distance", autoupdate="false" }
Switch Robonect_Remote4CorridorCut "Remote Start 4 CorridorCut" { channel="robonect:mower:automower:remote-4-corridor-cut", autoupdate="false" }
String Robonect_Remote5Name "Remote Start 5 Name" { channel="robonect:mower:automower:remote-5-name", autoupdate="false" }
Switch Robonect_Remote5Visible "Remote Start 5 Visible" { channel="robonect:mower:automower:remote-5-visible", autoupdate="false" }
Number Robonect_Remote5Path "Remote Start 5 Route" { channel="robonect:mower:automower:remote-5-path", autoupdate="false" }
Number:Dimensionless Robonect_Remote5Proportion "Remote Start 5 Selection Frequency" { unit="%", channel="robonect:mower:automower:remote-5-proportion", autoupdate="false" }
Number:Length Robonect_Remote5Distance "Remote Start 5 Distance" { unit="m", channel="robonect:mower:automower:remote-5-distance", autoupdate="false" }
Switch Robonect_Remote5CorridorCut "Remote Start 5 CorridorCut" { channel="robonect:mower:automower:remote-5-corridor-cut", autoupdate="false" }

// ===== Boundary wire (/json?cmd=wire) =====
String Robonect_WireSensor1Label "Wire Sensor 1 Label" <energy> { channel="robonect:mower:automower:wire-sensor-1-label" }
Number:Dimensionless Robonect_WireSensor1Quality "Wire Sensor 1 Signal Quality" <energy> { unit="%", channel="robonect:mower:automower:wire-sensor-1-quality" }
Number Robonect_WireSensor1A "Wire Sensor 1 Signal A" <energy> { channel="robonect:mower:automower:wire-sensor-1-a" }
Number Robonect_WireSensor1N "Wire Sensor 1 Signal N" <energy> { channel="robonect:mower:automower:wire-sensor-1-n" }
Number Robonect_WireSensor1F "Wire Sensor 1 Signal F" <energy> { channel="robonect:mower:automower:wire-sensor-1-f" }
Number Robonect_WireSensor1G1 "Wire Sensor 1 Signal G1" <energy> { channel="robonect:mower:automower:wire-sensor-1-g1" }
Number Robonect_WireSensor1G2 "Wire Sensor 1 Signal G2" <energy> { channel="robonect:mower:automower:wire-sensor-1-g2" }
String Robonect_WireSensor2Label "Wire Sensor 2 Label" <energy> { channel="robonect:mower:automower:wire-sensor-2-label" }
Number:Dimensionless Robonect_WireSensor2Quality "Wire Sensor 2 Signal Quality" <energy> { unit="%", channel="robonect:mower:automower:wire-sensor-2-quality" }
Number Robonect_WireSensor2A "Wire Sensor 2 Signal A" <energy> { channel="robonect:mower:automower:wire-sensor-2-a" }
Number Robonect_WireSensor2N "Wire Sensor 2 Signal N" <energy> { channel="robonect:mower:automower:wire-sensor-2-n" }
Number Robonect_WireSensor2F "Wire Sensor 2 Signal F" <energy> { channel="robonect:mower:automower:wire-sensor-2-f" }
Number Robonect_WireSensor2G1 "Wire Sensor 2 Signal G1" <energy> { channel="robonect:mower:automower:wire-sensor-2-g1" }
Number Robonect_WireSensor2G2 "Wire Sensor 2 Signal G2" <energy> { channel="robonect:mower:automower:wire-sensor-2-g2" }
String Robonect_WireSensor3Label "Wire Sensor 3 Label" <energy> { channel="robonect:mower:automower:wire-sensor-3-label" }
Number:Dimensionless Robonect_WireSensor3Quality "Wire Sensor 3 Signal Quality" <energy> { unit="%", channel="robonect:mower:automower:wire-sensor-3-quality" }
Number Robonect_WireSensor3A "Wire Sensor 3 Signal A" <energy> { channel="robonect:mower:automower:wire-sensor-3-a" }
Number Robonect_WireSensor3N "Wire Sensor 3 Signal N" <energy> { channel="robonect:mower:automower:wire-sensor-3-n" }
Number Robonect_WireSensor3F "Wire Sensor 3 Signal F" <energy> { channel="robonect:mower:automower:wire-sensor-3-f" }
Number Robonect_WireSensor3G1 "Wire Sensor 3 Signal G1" <energy> { channel="robonect:mower:automower:wire-sensor-3-g1" }
Number Robonect_WireSensor3G2 "Wire Sensor 3 Signal G2" <energy> { channel="robonect:mower:automower:wire-sensor-3-g2" }
String Robonect_WireSensor4Label "Wire Sensor 4 Label" <energy> { channel="robonect:mower:automower:wire-sensor-4-label" }
Number:Dimensionless Robonect_WireSensor4Quality "Wire Sensor 4 Signal Quality" <energy> { unit="%", channel="robonect:mower:automower:wire-sensor-4-quality" }
Number Robonect_WireSensor4A "Wire Sensor 4 Signal A" <energy> { channel="robonect:mower:automower:wire-sensor-4-a" }
Number Robonect_WireSensor4N "Wire Sensor 4 Signal N" <energy> { channel="robonect:mower:automower:wire-sensor-4-n" }
Number Robonect_WireSensor4F "Wire Sensor 4 Signal F" <energy> { channel="robonect:mower:automower:wire-sensor-4-f" }
Number Robonect_WireSensor4G1 "Wire Sensor 4 Signal G1" <energy> { channel="robonect:mower:automower:wire-sensor-4-g1" }
Number Robonect_WireSensor4G2 "Wire Sensor 4 Signal G2" <energy> { channel="robonect:mower:automower:wire-sensor-4-g2" }

// ===== Portal (/json?cmd=portal) =====
Switch Robonect_PortalEnabled "Remote Portal Enabled [%s]" <switch> { channel="robonect:mower:automower:portal-enabled" }
Switch Robonect_PortalConnected "Remote Portal Connected [%s]" <switch> { channel="robonect:mower:automower:portal-connected" }
String Robonect_PortalUrl "Remote Portal URL" { channel="robonect:mower:automower:portal-url" }
String Robonect_PortalHost "Remote Portal Host Name" { channel="robonect:mower:automower:portal-host" }

// ===== Timers (/json?cmd=timer) =====
Switch Robonect_Timer1Enabled "Timer 1 Enabled" { channel="robonect:mower:automower:timer-1-enabled" }
DateTime Robonect_Timer1Start "Timer 1 Start Time" { channel="robonect:mower:automower:timer-1-start" }
DateTime Robonect_Timer1End "Timer 1 End Time" { channel="robonect:mower:automower:timer-1-end" }
Switch Robonect_Timer1Monday "Timer 1 Monday" { channel="robonect:mower:automower:timer-1-monday" }
Switch Robonect_Timer1Tuesday "Timer 1 Tuesday" { channel="robonect:mower:automower:timer-1-tuesday" }
Switch Robonect_Timer1Wednesday "Timer 1 Wednesday" { channel="robonect:mower:automower:timer-1-wednesday" }
Switch Robonect_Timer1Thursday "Timer 1 Thursday" { channel="robonect:mower:automower:timer-1-thursday" }
Switch Robonect_Timer1Friday "Timer 1 Friday" { channel="robonect:mower:automower:timer-1-friday" }
Switch Robonect_Timer1Saturday "Timer 1 Saturday" { channel="robonect:mower:automower:timer-1-saturday" }
Switch Robonect_Timer1Sunday "Timer 1 Sunday" { channel="robonect:mower:automower:timer-1-sunday" }
Switch Robonect_Timer2Enabled "Timer 2 Enabled" { channel="robonect:mower:automower:timer-2-enabled" }
DateTime Robonect_Timer2Start "Timer 2 Start Time" { channel="robonect:mower:automower:timer-2-start" }
DateTime Robonect_Timer2End "Timer 2 End Time" { channel="robonect:mower:automower:timer-2-end" }
Switch Robonect_Timer2Monday "Timer 2 Monday" { channel="robonect:mower:automower:timer-2-monday" }
Switch Robonect_Timer2Tuesday "Timer 2 Tuesday" { channel="robonect:mower:automower:timer-2-tuesday" }
Switch Robonect_Timer2Wednesday "Timer 2 Wednesday" { channel="robonect:mower:automower:timer-2-wednesday" }
Switch Robonect_Timer2Thursday "Timer 2 Thursday" { channel="robonect:mower:automower:timer-2-thursday" }
Switch Robonect_Timer2Friday "Timer 2 Friday" { channel="robonect:mower:automower:timer-2-friday" }
Switch Robonect_Timer2Saturday "Timer 2 Saturday" { channel="robonect:mower:automower:timer-2-saturday" }
Switch Robonect_Timer2Sunday "Timer 2 Sunday" { channel="robonect:mower:automower:timer-2-sunday" }
Switch Robonect_Timer3Enabled "Timer 3 Enabled" { channel="robonect:mower:automower:timer-3-enabled" }
DateTime Robonect_Timer3Start "Timer 3 Start Time" { channel="robonect:mower:automower:timer-3-start" }
DateTime Robonect_Timer3End "Timer 3 End Time" { channel="robonect:mower:automower:timer-3-end" }
Switch Robonect_Timer3Monday "Timer 3 Monday" { channel="robonect:mower:automower:timer-3-monday" }
Switch Robonect_Timer3Tuesday "Timer 3 Tuesday" { channel="robonect:mower:automower:timer-3-tuesday" }
Switch Robonect_Timer3Wednesday "Timer 3 Wednesday" { channel="robonect:mower:automower:timer-3-wednesday" }
Switch Robonect_Timer3Thursday "Timer 3 Thursday" { channel="robonect:mower:automower:timer-3-thursday" }
Switch Robonect_Timer3Friday "Timer 3 Friday" { channel="robonect:mower:automower:timer-3-friday" }
Switch Robonect_Timer3Saturday "Timer 3 Saturday" { channel="robonect:mower:automower:timer-3-saturday" }
Switch Robonect_Timer3Sunday "Timer 3 Sunday" { channel="robonect:mower:automower:timer-3-sunday" }
Switch Robonect_Timer4Enabled "Timer 4 Enabled" { channel="robonect:mower:automower:timer-4-enabled" }
DateTime Robonect_Timer4Start "Timer 4 Start Time" { channel="robonect:mower:automower:timer-4-start" }
DateTime Robonect_Timer4End "Timer 4 End Time" { channel="robonect:mower:automower:timer-4-end" }
Switch Robonect_Timer4Monday "Timer 4 Monday" { channel="robonect:mower:automower:timer-4-monday" }
Switch Robonect_Timer4Tuesday "Timer 4 Tuesday" { channel="robonect:mower:automower:timer-4-tuesday" }
Switch Robonect_Timer4Wednesday "Timer 4 Wednesday" { channel="robonect:mower:automower:timer-4-wednesday" }
Switch Robonect_Timer4Thursday "Timer 4 Thursday" { channel="robonect:mower:automower:timer-4-thursday" }
Switch Robonect_Timer4Friday "Timer 4 Friday" { channel="robonect:mower:automower:timer-4-friday" }
Switch Robonect_Timer4Saturday "Timer 4 Saturday" { channel="robonect:mower:automower:timer-4-saturday" }
Switch Robonect_Timer4Sunday "Timer 4 Sunday" { channel="robonect:mower:automower:timer-4-sunday" }
Switch Robonect_Timer5Enabled "Timer 5 Enabled" { channel="robonect:mower:automower:timer-5-enabled" }
DateTime Robonect_Timer5Start "Timer 5 Start Time" { channel="robonect:mower:automower:timer-5-start" }
DateTime Robonect_Timer5End "Timer 5 End Time" { channel="robonect:mower:automower:timer-5-end" }
Switch Robonect_Timer5Monday "Timer 5 Monday" { channel="robonect:mower:automower:timer-5-monday" }
Switch Robonect_Timer5Tuesday "Timer 5 Tuesday" { channel="robonect:mower:automower:timer-5-tuesday" }
Switch Robonect_Timer5Wednesday "Timer 5 Wednesday" { channel="robonect:mower:automower:timer-5-wednesday" }
Switch Robonect_Timer5Thursday "Timer 5 Thursday" { channel="robonect:mower:automower:timer-5-thursday" }
Switch Robonect_Timer5Friday "Timer 5 Friday" { channel="robonect:mower:automower:timer-5-friday" }
Switch Robonect_Timer5Saturday "Timer 5 Saturday" { channel="robonect:mower:automower:timer-5-saturday" }
Switch Robonect_Timer5Sunday "Timer 5 Sunday" { channel="robonect:mower:automower:timer-5-sunday" }
Switch Robonect_Timer6Enabled "Timer 6 Enabled" { channel="robonect:mower:automower:timer-6-enabled" }
DateTime Robonect_Timer6Start "Timer 6 Start Time" { channel="robonect:mower:automower:timer-6-start" }
DateTime Robonect_Timer6End "Timer 6 End Time" { channel="robonect:mower:automower:timer-6-end" }
Switch Robonect_Timer6Monday "Timer 6 Monday" { channel="robonect:mower:automower:timer-6-monday" }
Switch Robonect_Timer6Tuesday "Timer 6 Tuesday" { channel="robonect:mower:automower:timer-6-tuesday" }
Switch Robonect_Timer6Wednesday "Timer 6 Wednesday" { channel="robonect:mower:automower:timer-6-wednesday" }
Switch Robonect_Timer6Thursday "Timer 6 Thursday" { channel="robonect:mower:automower:timer-6-thursday" }
Switch Robonect_Timer6Friday "Timer 6 Friday" { channel="robonect:mower:automower:timer-6-friday" }
Switch Robonect_Timer6Saturday "Timer 6 Saturday" { channel="robonect:mower:automower:timer-6-saturday" }
Switch Robonect_Timer6Sunday "Timer 6 Sunday" { channel="robonect:mower:automower:timer-6-sunday" }
Switch Robonect_Timer7Enabled "Timer 7 Enabled" { channel="robonect:mower:automower:timer-7-enabled" }
DateTime Robonect_Timer7Start "Timer 7 Start Time" { channel="robonect:mower:automower:timer-7-start" }
DateTime Robonect_Timer7End "Timer 7 End Time" { channel="robonect:mower:automower:timer-7-end" }
Switch Robonect_Timer7Monday "Timer 7 Monday" { channel="robonect:mower:automower:timer-7-monday" }
Switch Robonect_Timer7Tuesday "Timer 7 Tuesday" { channel="robonect:mower:automower:timer-7-tuesday" }
Switch Robonect_Timer7Wednesday "Timer 7 Wednesday" { channel="robonect:mower:automower:timer-7-wednesday" }
Switch Robonect_Timer7Thursday "Timer 7 Thursday" { channel="robonect:mower:automower:timer-7-thursday" }
Switch Robonect_Timer7Friday "Timer 7 Friday" { channel="robonect:mower:automower:timer-7-friday" }
Switch Robonect_Timer7Saturday "Timer 7 Saturday" { channel="robonect:mower:automower:timer-7-saturday" }
Switch Robonect_Timer7Sunday "Timer 7 Sunday" { channel="robonect:mower:automower:timer-7-sunday" }
Switch Robonect_Timer8Enabled "Timer 8 Enabled" { channel="robonect:mower:automower:timer-8-enabled" }
DateTime Robonect_Timer8Start "Timer 8 Start Time" { channel="robonect:mower:automower:timer-8-start" }
DateTime Robonect_Timer8End "Timer 8 End Time" { channel="robonect:mower:automower:timer-8-end" }
Switch Robonect_Timer8Monday "Timer 8 Monday" { channel="robonect:mower:automower:timer-8-monday" }
Switch Robonect_Timer8Tuesday "Timer 8 Tuesday" { channel="robonect:mower:automower:timer-8-tuesday" }
Switch Robonect_Timer8Wednesday "Timer 8 Wednesday" { channel="robonect:mower:automower:timer-8-wednesday" }
Switch Robonect_Timer8Thursday "Timer 8 Thursday" { channel="robonect:mower:automower:timer-8-thursday" }
Switch Robonect_Timer8Friday "Timer 8 Friday" { channel="robonect:mower:automower:timer-8-friday" }
Switch Robonect_Timer8Saturday "Timer 8 Saturday" { channel="robonect:mower:automower:timer-8-saturday" }
Switch Robonect_Timer8Sunday "Timer 8 Sunday" { channel="robonect:mower:automower:timer-8-sunday" }
Switch Robonect_Timer9Enabled "Timer 9 Enabled" { channel="robonect:mower:automower:timer-9-enabled" }
DateTime Robonect_Timer9Start "Timer 9 Start Time" { channel="robonect:mower:automower:timer-9-start" }
DateTime Robonect_Timer9End "Timer 9 End Time" { channel="robonect:mower:automower:timer-9-end" }
Switch Robonect_Timer9Monday "Timer 9 Monday" { channel="robonect:mower:automower:timer-9-monday" }
Switch Robonect_Timer9Tuesday "Timer 9 Tuesday" { channel="robonect:mower:automower:timer-9-tuesday" }
Switch Robonect_Timer9Wednesday "Timer 9 Wednesday" { channel="robonect:mower:automower:timer-9-wednesday" }
Switch Robonect_Timer9Thursday "Timer 9 Thursday" { channel="robonect:mower:automower:timer-9-thursday" }
Switch Robonect_Timer9Friday "Timer 9 Friday" { channel="robonect:mower:automower:timer-9-friday" }
Switch Robonect_Timer9Saturday "Timer 9 Saturday" { channel="robonect:mower:automower:timer-9-saturday" }
Switch Robonect_Timer9Sunday "Timer 9 Sunday" { channel="robonect:mower:automower:timer-9-sunday" }
Switch Robonect_Timer10Enabled "Timer 10 Enabled" { channel="robonect:mower:automower:timer-10-enabled" }
DateTime Robonect_Timer10Start "Timer 10 Start Time" { channel="robonect:mower:automower:timer-10-start" }
DateTime Robonect_Timer10End "Timer 10 End Time" { channel="robonect:mower:automower:timer-10-end" }
Switch Robonect_Timer10Monday "Timer 10 Monday" { channel="robonect:mower:automower:timer-10-monday" }
Switch Robonect_Timer10Tuesday "Timer 10 Tuesday" { channel="robonect:mower:automower:timer-10-tuesday" }
Switch Robonect_Timer10Wednesday "Timer 10 Wednesday" { channel="robonect:mower:automower:timer-10-wednesday" }
Switch Robonect_Timer10Thursday "Timer 10 Thursday" { channel="robonect:mower:automower:timer-10-thursday" }
Switch Robonect_Timer10Friday "Timer 10 Friday" { channel="robonect:mower:automower:timer-10-friday" }
Switch Robonect_Timer10Saturday "Timer 10 Saturday" { channel="robonect:mower:automower:timer-10-saturday" }
Switch Robonect_Timer10Sunday "Timer 10 Sunday" { channel="robonect:mower:automower:timer-10-sunday" }
Switch Robonect_Timer11Enabled "Timer 11 Enabled" { channel="robonect:mower:automower:timer-11-enabled" }
DateTime Robonect_Timer11Start "Timer 11 Start Time" { channel="robonect:mower:automower:timer-11-start" }
DateTime Robonect_Timer11End "Timer 11 End Time" { channel="robonect:mower:automower:timer-11-end" }
Switch Robonect_Timer11Monday "Timer 11 Monday" { channel="robonect:mower:automower:timer-11-monday" }
Switch Robonect_Timer11Tuesday "Timer 11 Tuesday" { channel="robonect:mower:automower:timer-11-tuesday" }
Switch Robonect_Timer11Wednesday "Timer 11 Wednesday" { channel="robonect:mower:automower:timer-11-wednesday" }
Switch Robonect_Timer11Thursday "Timer 11 Thursday" { channel="robonect:mower:automower:timer-11-thursday" }
Switch Robonect_Timer11Friday "Timer 11 Friday" { channel="robonect:mower:automower:timer-11-friday" }
Switch Robonect_Timer11Saturday "Timer 11 Saturday" { channel="robonect:mower:automower:timer-11-saturday" }
Switch Robonect_Timer11Sunday "Timer 11 Sunday" { channel="robonect:mower:automower:timer-11-sunday" }
Switch Robonect_Timer12Enabled "Timer 12 Enabled" { channel="robonect:mower:automower:timer-12-enabled" }
DateTime Robonect_Timer12Start "Timer 12 Start Time" { channel="robonect:mower:automower:timer-12-start" }
DateTime Robonect_Timer12End "Timer 12 End Time" { channel="robonect:mower:automower:timer-12-end" }
Switch Robonect_Timer12Monday "Timer 12 Monday" { channel="robonect:mower:automower:timer-12-monday" }
Switch Robonect_Timer12Tuesday "Timer 12 Tuesday" { channel="robonect:mower:automower:timer-12-tuesday" }
Switch Robonect_Timer12Wednesday "Timer 12 Wednesday" { channel="robonect:mower:automower:timer-12-wednesday" }
Switch Robonect_Timer12Thursday "Timer 12 Thursday" { channel="robonect:mower:automower:timer-12-thursday" }
Switch Robonect_Timer12Friday "Timer 12 Friday" { channel="robonect:mower:automower:timer-12-friday" }
Switch Robonect_Timer12Saturday "Timer 12 Saturday" { channel="robonect:mower:automower:timer-12-saturday" }
Switch Robonect_Timer12Sunday "Timer 12 Sunday" { channel="robonect:mower:automower:timer-12-sunday" }
Switch Robonect_Timer13Enabled "Timer 13 Enabled" { channel="robonect:mower:automower:timer-13-enabled" }
DateTime Robonect_Timer13Start "Timer 13 Start Time" { channel="robonect:mower:automower:timer-13-start" }
DateTime Robonect_Timer13End "Timer 13 End Time" { channel="robonect:mower:automower:timer-13-end" }
Switch Robonect_Timer13Monday "Timer 13 Monday" { channel="robonect:mower:automower:timer-13-monday" }
Switch Robonect_Timer13Tuesday "Timer 13 Tuesday" { channel="robonect:mower:automower:timer-13-tuesday" }
Switch Robonect_Timer13Wednesday "Timer 13 Wednesday" { channel="robonect:mower:automower:timer-13-wednesday" }
Switch Robonect_Timer13Thursday "Timer 13 Thursday" { channel="robonect:mower:automower:timer-13-thursday" }
Switch Robonect_Timer13Friday "Timer 13 Friday" { channel="robonect:mower:automower:timer-13-friday" }
Switch Robonect_Timer13Saturday "Timer 13 Saturday" { channel="robonect:mower:automower:timer-13-saturday" }
Switch Robonect_Timer13Sunday "Timer 13 Sunday" { channel="robonect:mower:automower:timer-13-sunday" }
Switch Robonect_Timer14Enabled "Timer 14 Enabled" { channel="robonect:mower:automower:timer-14-enabled" }
DateTime Robonect_Timer14Start "Timer 14 Start Time" { channel="robonect:mower:automower:timer-14-start" }
DateTime Robonect_Timer14End "Timer 14 End Time" { channel="robonect:mower:automower:timer-14-end" }
Switch Robonect_Timer14Monday "Timer 14 Monday" { channel="robonect:mower:automower:timer-14-monday" }
Switch Robonect_Timer14Tuesday "Timer 14 Tuesday" { channel="robonect:mower:automower:timer-14-tuesday" }
Switch Robonect_Timer14Wednesday "Timer 14 Wednesday" { channel="robonect:mower:automower:timer-14-wednesday" }
Switch Robonect_Timer14Thursday "Timer 14 Thursday" { channel="robonect:mower:automower:timer-14-thursday" }
Switch Robonect_Timer14Friday "Timer 14 Friday" { channel="robonect:mower:automower:timer-14-friday" }
Switch Robonect_Timer14Saturday "Timer 14 Saturday" { channel="robonect:mower:automower:timer-14-saturday" }
Switch Robonect_Timer14Sunday "Timer 14 Sunday" { channel="robonect:mower:automower:timer-14-sunday" }

// ===== Weather (/json?cmd=weather) =====
Number Robonect_WeatherLocationCityId "Weather City ID" <text> { channel="robonect:mower:automower:weather-location-cityid", autoupdate="false" }
String Robonect_WeatherLocationZip "Weather Postal Code" { channel="robonect:mower:automower:weather-location-zip", autoupdate="false" }
String Robonect_WeatherLocationCountry "Weather Country" <text> { channel="robonect:mower:automower:weather-location-country" }
DateTime Robonect_WeatherSunrise "Weather Sunrise" <sunrise> { channel="robonect:mower:automower:weather-sunrise" }
DateTime Robonect_WeatherSunset "Weather Sunset" <sunset> { channel="robonect:mower:automower:weather-sunset" }
String Robonect_WeatherIcon "Weather Icon URL" <text> { channel="robonect:mower:automower:weather-icon" }
DateTime Robonect_WeatherTimestamp "Weather Observation Time" <time> { channel="robonect:mower:automower:weather-timestamp" }
Switch Robonect_WeatherConditionTooRainy "Rain Restriction Active [%s]" <rain> { channel="robonect:mower:automower:weather-condition-too-rainy" }
Switch Robonect_WeatherConditionTooCold "Cold Restriction Active [%s]" <temperature_cold> { channel="robonect:mower:automower:weather-condition-too-cold" }
Switch Robonect_WeatherConditionTooWarm "Heat Restriction Active [%s]" <temperature_hot> { channel="robonect:mower:automower:weather-condition-too-warm" }
Switch Robonect_WeatherConditionTooDry "Dry-Air Restriction Active [%s]" <humidity> { channel="robonect:mower:automower:weather-condition-too-dry" }
Switch Robonect_WeatherConditionTooWet "Humid-Air Restriction Active [%s]" <humidity> { channel="robonect:mower:automower:weather-condition-too-wet" }
Switch Robonect_WeatherConditionDay "Daytime Restriction Active [%s]" <sun> { channel="robonect:mower:automower:weather-condition-day" }
Switch Robonect_WeatherConditionNight "Nighttime Restriction Active [%s]" <moon> { channel="robonect:mower:automower:weather-condition-night" }
Switch Robonect_WeatherBreak "Weather Mowing Pause [%s]" <switch> { channel="robonect:mower:automower:weather-break" }
Number:Dimensionless Robonect_WeatherRain "Weather Rain Intensity" <rain> { unit="%", channel="robonect:mower:automower:weather-rain" }
Number:Temperature Robonect_WeatherTemperature "Weather Temperature" <temperature> { unit="°C", channel="robonect:mower:automower:weather-temperature" }
Number:Dimensionless Robonect_WeatherHumidity "Weather Relative Humidity" <humidity> { unit="%", channel="robonect:mower:automower:weather-humidity" }
Switch Robonect_WeatherDay "Weather Day [%s]" <sun> { channel="robonect:mower:automower:weather-day" }
String Robonect_WeatherCity "Weather City" <time> { channel="robonect:mower:automower:weather-city" }
Number:Time Robonect_WeatherRemaining "Weather Pause Remaining" <time> { unit="min", channel="robonect:mower:automower:weather-remaining" }
Switch Robonect_WeatherServiceEnabled "Weather Service Enabled" <switch> { channel="robonect:mower:automower:weather-service-enabled" }
Number:Time Robonect_WeatherServiceInterval "Weather Service Update Interval" <time> { unit="min", channel="robonect:mower:automower:weather-service-interval" }
Number:Dimensionless Robonect_WeatherConfigMaxRain "Weather Config Maximum Rain Intensity" <rain> { unit="%", channel="robonect:mower:automower:weather-config-max-rain" }
Number:Temperature Robonect_WeatherConfigMinTemperature "Weather Config Minimum Temperature" <temperature_cold> { unit="°C", channel="robonect:mower:automower:weather-config-min-temperature" }
Number:Temperature Robonect_WeatherConfigMaxTemperature "Weather Config Maximum Temperature" <temperature_hot> { unit="°C", channel="robonect:mower:automower:weather-config-max-temperature" }
Number:Dimensionless Robonect_WeatherConfigMinHumidity "Weather Config Minimum Humidity" <humidity> { unit="%", channel="robonect:mower:automower:weather-config-min-humidity" }
Number:Dimensionless Robonect_WeatherConfigMaxHumidity "Weather Config Maximum Humidity" <humidity> { unit="%", channel="robonect:mower:automower:weather-config-max-humidity" }
Switch Robonect_WeatherConfigSkipDay "Weather Config Prevent Daytime Mowing" <sun> { channel="robonect:mower:automower:weather-config-skip-day" }
Switch Robonect_WeatherConfigSkipNight "Weather Config Prevent Nighttime Mowing" <moon> { channel="robonect:mower:automower:weather-config-skip-night" }

// ===== Extensions (/json?cmd=ext) =====
Switch Robonect_ExtensionGpio1Flash "GPIO 1 Flash on Error [%s]" <switch> { channel="robonect:mower:automower:extension-gpio-1-flash" }
Switch Robonect_ExtensionGpio1Inverted "GPIO 1 Active-Low Signal [%s]" <switch> { channel="robonect:mower:automower:extension-gpio-1-inverted" }
Switch Robonect_ExtensionGpio1Status "GPIO 1 State [%s]" <switch> { channel="robonect:mower:automower:extension-gpio-1-status" }
Switch Robonect_ExtensionGpio2Flash "GPIO 2 Flash on Error [%s]" <switch> { channel="robonect:mower:automower:extension-gpio-2-flash" }
Switch Robonect_ExtensionGpio2Inverted "GPIO 2 Active-Low Signal [%s]" <switch> { channel="robonect:mower:automower:extension-gpio-2-inverted" }
Switch Robonect_ExtensionGpio2Status "GPIO 2 State [%s]" <switch> { channel="robonect:mower:automower:extension-gpio-2-status" }
Switch Robonect_ExtensionOutput1Flash "Output 1 Flash on Error [%s]" <switch> { channel="robonect:mower:automower:extension-output-1-flash" }
Switch Robonect_ExtensionOutput1Inverted "Output 1 Active-Low Signal [%s]" <switch> { channel="robonect:mower:automower:extension-output-1-inverted" }
Switch Robonect_ExtensionOutput1Status "Output 1 State [%s]" <switch> { channel="robonect:mower:automower:extension-output-1-status" }
Switch Robonect_ExtensionOutput2Flash "Output 2 Flash on Error [%s]" <switch> { channel="robonect:mower:automower:extension-output-2-flash" }
Switch Robonect_ExtensionOutput2Inverted "Output 2 Active-Low Signal [%s]" <switch> { channel="robonect:mower:automower:extension-output-2-inverted" }
Switch Robonect_ExtensionOutput2Status "Output 2 State [%s]" <switch> { channel="robonect:mower:automower:extension-output-2-status" }

// ===== Push (/json?cmd=push) =====
// Available only with useWebApi enabled.
Number Robonect_PushMode "Push Service Mode" <text> { channel="robonect:mower:automower:push-mode", autoupdate="false" }
String Robonect_PushUrl "Push Service URL" <text> { channel="robonect:mower:automower:push-url" }
String Robonect_PushUsername "Push Service Username" <text> { channel="robonect:mower:automower:push-username" }
String Robonect_PushPassword "Push Service Password" <text> { channel="robonect:mower:automower:push-password", autoupdate="false" }
Number:Time Robonect_PushInterval "Push Service Interval" <time> { unit="s", channel="robonect:mower:automower:push-interval" }
// Push triggers (dynamic channels named from the mower response)
Switch Robonect_PushTriggerParkedEnter "Push Trigger 0 Parked Enter" { channel="robonect:mower:automower:push-trigger-0-parked-enter" }
Switch Robonect_PushTriggerParkedLeave "Push Trigger 0 Parked Leave" { channel="robonect:mower:automower:push-trigger-0-parked-leave" }
Switch Robonect_PushTriggerMowingEnter "Push Trigger 1 Mowing Enter" { channel="robonect:mower:automower:push-trigger-1-mowing-enter" }
Switch Robonect_PushTriggerMowingLeave "Push Trigger 1 Mowing Leave" { channel="robonect:mower:automower:push-trigger-1-mowing-leave" }
Switch Robonect_PushTriggerWayHomeEnter "Push Trigger 2 Way home Enter" { channel="robonect:mower:automower:push-trigger-2-way_home-enter" }
Switch Robonect_PushTriggerWayHomeLeave "Push Trigger 2 Way home Leave" { channel="robonect:mower:automower:push-trigger-2-way_home-leave" }
Switch Robonect_PushTriggerChargingEnter "Push Trigger 3 Charging Enter" { channel="robonect:mower:automower:push-trigger-3-charging-enter" }
Switch Robonect_PushTriggerChargingLeave "Push Trigger 3 Charging Leave" { channel="robonect:mower:automower:push-trigger-3-charging-leave" }
Switch Robonect_PushTriggerSearchingEnter "Push Trigger 4 Searching Enter" { channel="robonect:mower:automower:push-trigger-4-searching-enter" }
Switch Robonect_PushTriggerSearchingLeave "Push Trigger 4 Searching Leave" { channel="robonect:mower:automower:push-trigger-4-searching-leave" }
Switch Robonect_PushTriggerFaultEnter "Push Trigger 5 Fault Enter" { channel="robonect:mower:automower:push-trigger-5-fault-enter" }
Switch Robonect_PushTriggerFaultLeave "Push Trigger 5 Fault Leave" { channel="robonect:mower:automower:push-trigger-5-fault-leave" }
Switch Robonect_PushTriggerLostLoopEnter "Push Trigger 6 Lost loop Enter" { channel="robonect:mower:automower:push-trigger-6-lost_loop-enter" }
Switch Robonect_PushTriggerLostLoopLeave "Push Trigger 6 Lost loop Leave" { channel="robonect:mower:automower:push-trigger-6-lost_loop-leave" }
Switch Robonect_PushTriggerOffEnter "Push Trigger 7 Off Enter" { channel="robonect:mower:automower:push-trigger-7-off-enter" }
Switch Robonect_PushTriggerOffLeave "Push Trigger 7 Off Leave" { channel="robonect:mower:automower:push-trigger-7-off-leave" }
Switch Robonect_PushTriggerSleepingEnter "Push Trigger 8 Sleeping Enter" { channel="robonect:mower:automower:push-trigger-8-sleeping-enter" }
Switch Robonect_PushTriggerSleepingLeave "Push Trigger 8 Sleeping Leave" { channel="robonect:mower:automower:push-trigger-8-sleeping-leave" }
Switch Robonect_PushTriggerDoorOpensEnter "Push Trigger 9 Door opens Enter" { channel="robonect:mower:automower:push-trigger-9-door_opens-enter" }
Switch Robonect_PushTriggerDoorOpensLeave "Push Trigger 9 Door opens Leave" { channel="robonect:mower:automower:push-trigger-9-door_opens-leave" }
```

</details>
