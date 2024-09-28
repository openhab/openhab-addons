# Nuki Binding

This is the binding for the [Nuki Smart Lock](https://nuki.io).
This binding allows you to integrate, view, control and configure the Nuki Bridge, Nuki Smart Lock and Nuki Opener.

## Prerequisites

1. At least one Nuki Smart Lock or Nuki Opener which is paired via Bluetooth with a Nuki Bridge. For this go and get either:
    - [Nuki Smart Lock](https://nuki.io/en/smart-lock/) and a [Nuki Bridge](https://nuki.io/en/bridge/)
    - [Nuki Combo](https://nuki.io/en/shop/nuki-combo/)
1. The Bridge HTTP-API has to be enabled during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/).

It is absolutely recommended to configure static IP addresses for both, the openHAB server and the Nuki Bridge!

### Nuki Bridge Callback

The Nuki Binding will manage the required callback from the Nuki Bridge to the openHAB server if _manageCallbacks_ is set to `true`.
If _manageCallbacks_ is not set it will default to `true`.
Make sure that you've selected the correct primary address in the [network settings](https://www.openhab.org/docs/settings/services_system.html#network-settings).

If you want to manage the callbacks from the Nuki Bridge to the openHAB server by yourself, you need to set _manageCallbacks_ to `false`.
Then add the callback on the Nuki Bridge via Bridge API Endpoint _/callback/add_ in the format `http://<openHAB_IP>:<openHAB_PORT>/nuki/bcb`.
The Sheet [NukiBridgeAPI](https://docs.google.com/spreadsheets/d/1SGKWhqwqRyOGbv4NEq-8PAPjBORRixvEjRuzO-nVabQ) is a helpfull tool for listing, adding and removing callbacks.

## Supported Bridges

This binding supports just one bridge type: The Nuki Bridge (`nuki:bridge`). Create one `bridge` per Nuki Bridge available in your home automation environment.

The following configuration options are available:

| Parameter       | Description                                                                                                                                                                                        | Comment      |
| --------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------ |
| ip              | The IP address of the Nuki Bridge. Look it up on your router. It is recommended to set a static IP address lease for the Nuki Bridge (and for your openHAB server too) on your router.             | Required     |
| port            | The Port which you configured during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/).                                                                 | Default 8080 |
| apiToken        | The API Token which you configured during [Initial Bridge setup](https://nuki.io/en/support/bridge/bridge-setup/initial-bridge-setup/).                                                            | Required     |
| manageCallbacks | Let the Nuki Binding manage the callbacks on the Nuki Bridge. It will add the required callback on the Nuki Bridge. If there are already 3 callbacks, it **will delete** the callback with ID `0`. | Default true |
| secureToken     | Whether hashed token should be used when communicating with Nuki Bridge. If disabled, API token will be sent in plaintext with each request.                                                       | Default true |

### Bridge discovery

Bridges on local network can be discovered automatically if both Nuki Bridge and openHAB have working internet connection. You can check whether discovery
is working by checking [discovery API endpoint](https://api.nuki.io/discover/bridges). To discover bridges do the following:

- In openHAB UI add new thing, select Nuki Binding and start scan. LED on bridge should light up.
- Within 30s press button on Nuki Bridge you want to discover.
- Bridge should appear in inbox.

Pressing bridge button is required for binding to obtain valid API token. If the button isn't pressed during discovery, bridge will
be created but token must be set manually for binding to work.

If bridge is connected to network but not discovered, enter [Manage Bridge](https://support.nuki.io/hc/en-us/articles/360016489018-Manage-Bridge) menu
in Nuki mobile app, check server connection then disconnect and let the bridge restart.

## Supported Things

This binding supports 2 things - Nuki Smart Lock (`nuki:smartlock`) and Nuki Opener (`nuki:opener`). Both devices can be added using discovery after bridge they are
connected to is configured and online.

### Nuki Smart Lock

This is a common thing for all Nuki smart lock products - Nuki Smart Lock 1.0/2.0/3.0 (Pro) and Nuki Smart Door. The following configuration options are available:

| Parameter  | Description                                                                                                                                                                                               | Comment                                                          |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------- |
| nukiId     | The decimal or hexadecimal string that identifies the Nuki Smartlock.                                                                                                                                     | Only available in textual configuration, cannot be edited in UI. |
| deviceType | Numeric device type as specified by bridge HTTP API - 0 = Nuki Smart Lock 1.0/2.0, 3 = Nuki Smart Door, 4 = Nuki Smart Lock 3.0 (Pro).                                                                    | Only available in textual configuration, cannot be edited in UI. |
| unlatch    | If set to `true` the Nuki Smart Lock will unlock the door but then also automatically pull the latch of the door lock. Usually, if the door hinges are correctly adjusted, the door will then swing open. | Default false                                                    |

#### Supported Channels

| Channel          | Type   | Description                                                                                                                                                                             |
| ---------------- | ------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| lock             | Switch | Switch to lock and unlock doors. If `unlatch` configuration parameter is set, unlocking will also unlatch the door.                                                                     |
| lockState        | Number | Channel which accepts [Supported commands](#supported-lockstate-commands) for performing actions, and produces [supported values](#supported-lockstate-values) when lock state changes. |
| lowBattery       | Switch | Low battery warning channel                                                                                                                                                             |
| keypadLowBattery | Switch | Indicates if keypad connected to Nuki Lock has low battery                                                                                                                              |
| batteryLevel     | Number | Current battery level                                                                                                                                                                   |
| batteryCharging  | Swtich | Flag indicating if the batteries of the Nuki device are charging at the moment                                                                                                          |
| doorsensorState  | Number | Read only channel for monitoring door sensor state, see [supported values](#supported-doorsensorstate-values)                                                                           |

##### Supported lockState commands

These values can be sent to _lockState_ channel as a commands:

| Command | Name                     |
| ------- | ------------------------ |
| 1       | Unlock                   |
| 2       | Lock                     |
| 3       | Unlatch                  |
| 4       | Lock 'n' Go              |
| 5       | Lock 'n' Go with Unlatch |

##### Supported lockState values

| State | Name                    |
| ----- | ----------------------- |
| 0     | Uncalibrated            |
| 1     | Locked                  |
| 2     | Unlocking               |
| 3     | Unlocked                |
| 4     | Locking                 |
| 5     | Unlatched               |
| 6     | Unlatched (Lock 'n' Go) |
| 7     | Unlatching              |
| 254   | Motor blocked           |
| 255   | Undefined               |

Unfortunately the Nuki Bridge is not reporting any transition states (e.g. for Lock 'n' Go).

##### Supported doorSensorState values

| State | Name                |
| ----- | ------------------- |
| 1     | Deactivated         |
| 2     | Closed              |
| 3     | Open                |
| 4     | Door state unknonwn |
| 5     | Calibrating         |
| 16    | Uncalibrated        |
| 240   | Removed             |
| 255   | Unknown             |

### Nuki Opener

| Parameter | Description                                                        | Comment                                                          |
| --------- | ------------------------------------------------------------------ | ---------------------------------------------------------------- |
| nukiId    | The decimal or hexadecimal string that identifies the Nuki Opener. | Only available in textual configuration, cannot be edited in UI. |

#### Supported channels

| Channel             | Type     | Description                                                                                                                                                                      |
| ------------------- | -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| openerState         | Number   | Channel for sending [supported commands](#supported-openerstate-commands) to Opener, produces one of [supported values](#supported-openerstate-values) when Opener state changes |
| openerMode          | Number   | Id of current Opener mode, see [Supported values](#supported-openermode-values)                                                                                                  |
| openerLowBattery    | Switch   | Low battery warning channel                                                                                                                                                      |
| ringActionState     | Trigger  | Channel triggers 'RINGING' event when the doorbell is being rung. This can trigger at most once every 30s                                                                        |
| ringActionTimestamp | DateTime | Timestamp of last time doorbell was rung.                                                                                                                                        |

##### Supported openerState commands

| Command | Name                       |
| ------- | -------------------------- |
| 1       | Activate ring to open      |
| 2       | Deactivate ring to open    |
| 3       | Electric strike actuation  |
| 4       | Activate continuous mode   |
| 5       | Deactivate continuous mode |

##### Supported openerState values

| State | Name                |
| ----- | ------------------- |
| 0     | Untrained           |
| 1     | Online              |
| 3     | Ring to open active |
| 5     | Open                |
| 7     | Opening             |
| 253   | Boot run            |
| 255   | Undefined           |

##### Supported openerMode values

| Mode | Name            |
| ---- | --------------- |
| 2    | Door mode       |
| 3    | Continuous mode |

## Troubleshooting

### Bridge and devices are offline with error 403

If secureToken property is enabled, make sure that time on device running openHAB and Nuki Bridge are synchronized. When secureToken
is enabled, all requests contain timestamp and bridge will only accept requests with small time difference. If it is not possible to
keep time synchronized, disable secureToken feature.

### NukiId conversion when migrating from old binding version

Older versions of binding used nukiId in hexadecimal format (as displayed in Nuki app, e.g. 5C4BC4B3). The new version
expects nukiId to be in decimal format (e.g. 1548469427), since that's the format returned from API.
The binding does the conversion automatically, but only if your nukiId contains any letters A-F, otherwise the binding
has no way to tell whether the id is in hexadecimal or decimal format. If your nukiId in hexadecimal format
contains only numbers, you'll have to convert it to decimal format manually, or preferably delete the old Thing
and use discovery to recreate it.

## Full Example

A manual setup through files could look like this:

### things/nuki.things

```java
Bridge nuki:bridge:NB1 "Bridge Name" [ ip="192.168.0.50", port=8080, apiToken="myS3cr3t!", manageCallbacks=true, secureToken=true ] {
    Thing smartlock SL1 "Nuki Smartlock Name" [ nukiId="12AB89EF", deviceType=0, unlatch=false ]
    Thing opener OP1 "Nuki Opener Name" [ nukiId="254CF45A" ]
}
```

### items/nuki.items

```java
Switch Frontdoor_Lock       "Frontdoor (Unlock / Lock)" <nukiwhite>     { channel="nuki:smartlock:NB1:SL1:lock" }
Number Frontdoor_LockState  "Frontdoor (Lock State)"    <nukisl>        { channel="nuki:smartlock:NB1:SL1:lockState" }
Switch Frontdoor_LowBattery "Frontdoor Low Battery"     <nukibattery>   { channel="nuki:smartlock:NB1:SL1:lowBattery" }
Number Frontdoor_DoorState  "Frontdoor (Door State)"    <door>          { channel="nuki:smartlock:NB1:SL1:doorsensorState" }
```

### sitemaps/nuki.sitemap

```perl
sitemap nuki label="Nuki Smart Lock" {
    Frame label="Channel Lock" {
        Switch item=Frontdoor_Lock
    }
    Frame label="Channel State used for lock actions" {
        Switch item=Frontdoor_LockState mappings=[1="Unlock", 2="Lock", 3="Unlatch", 4="LnGo", 5="LnGoU"]
    }
    Frame label="Channel State" {
        Text item=Frontdoor_LockState label="Lock State [MAP(nukilockstates.map):%s]"
    }
    Frame label="Channel Low Battery" {
        Text item=Frontdoor_LowBattery  label="Low Battery [%s]"
    }
    Frame label="Channel Door State" {
        Text item=Frontdoor_DoorState label="Door State [MAP(nukidoorsensorstates.map):%s]"
    }
}
```
