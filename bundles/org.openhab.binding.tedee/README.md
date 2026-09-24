# Tedee Binding for openHAB

The Tedee binding integrates Tedee Smart Locks with openHAB through either:

- the local Tedee Bridge API, or
- the Tedee Cloud API.

The binding is developed and tested against openHAB 4.3.x / 4.3.5.

Local communication does not require the Tedee Cloud API for normal operation. Cloud access is available as an optional alternative and can be used without a local Tedee Bridge.

## Supported Features

- Tedee local Bridge
- Tedee Cloud
- Tedee Lock
- Automatic Lock discovery
- Lock, unlock and pull commands
- `UNLOCK_NO_PULL`
- Lock status polling
- Manual `REFRESH`
- Battery, charging, jammed and connection state
- Firmware/software version and Thing properties
- Tedee device settings as read-only channels
- Local Tedee Bridge webhooks for `lock-status-changed`
- Automatic local webhook callback registration
- Cloud polling without local webhooks

## Supported Things

### Tedee Local Bridge

**Thing type:** `tedee:bridge`

The Bridge represents the physical Tedee Bridge used for local API communication with Tedee locks.

#### Configuration

| Parameter | Type | Required | Default | Description |
|---|---|---:|---:|---|
| `ip` | text | Yes | — | IP address of the Tedee Bridge |
| `port` | integer | No | `80` | HTTP port of the Tedee Bridge |
| `apiToken` | password | Yes | — | Tedee Bridge API token |
| `pollInterval` | integer | No | `300` | Configured interval in seconds for bridge monitoring |

The Bridge must be reachable from the openHAB server. The Tedee Bridge API is local to the LAN.

Tedee also requires the Bridge itself to have internet connectivity so that it can obtain and refresh access certificates for paired devices.

### Tedee Cloud

**Thing type:** `tedee:cloud`

The Cloud Bridge provides direct communication with the Tedee Cloud API and does not require a local Tedee Bridge.

#### Configuration

| Parameter | Type | Required | Default | Description |
|---|---|---:|---:|---|
| `personalAccessKey` | password | Yes | — | Tedee Personal Access Key used for Cloud API authentication |
| `pollInterval` | integer | No | `300` | Cloud status and discovery polling interval in seconds |

The Personal Access Key should be treated as a secret and must not be published in repositories, screenshots or logs.

### Tedee Lock

**Thing type:** `tedee:lock`

A Lock represents an individual Tedee lock.

A Lock can be configured below either a local `tedee:bridge` or a `tedee:cloud` Bridge.

#### Configuration

| Parameter | Type | Required | Default | Description |
|---|---|---:|---:|---|
| `deviceId` | integer | Yes | — | Numeric Tedee device ID |
| `pollInterval` | integer | No | `300` | Lock status polling interval in seconds |

Example device ID:

```text
deviceId=14196
```

Locks discovered from either Local or Cloud use the Tedee `deviceId` as their unique device representation.

## Channels

The Lock Thing exposes the following channels.

### Lock Control

| Channel ID | Item Type | Read | Command | Description |
|---|---|---:|---|---|
| `lock` | Switch | Yes | `ON` / `OFF` | Lock or unlock the Tedee lock |
| `action` | String | No | `LOCK`, `UNLOCK`, `UNLOCK_NO_PULL`, `PULL` | Execute a specific Tedee action |

The `lock` channel is the simple Switch interface:

- `ON` → `LOCK`
- `OFF` → normal `UNLOCK`

The `action` channel provides explicit actions:

- `LOCK`
- `UNLOCK`
- `UNLOCK_NO_PULL`
- `PULL`

When the Tedee lock has automatic pull spring enabled, normal `UNLOCK` can also perform the pull spring operation. `UNLOCK_NO_PULL` unlocks without pulling the spring.

### Lock State

| Channel ID | Item Type | Description |
|---|---|---|
| `state` | String | Internal Tedee lock state |
| `doorState` | String | Door sensor state, if supported by the lock/sensor setup |
| `battery` | Number | Battery level in percent |
| `charging` | Switch | Whether the lock is charging |
| `jammed` | Switch | Whether the lock reports a jam |
| `connected` | Switch | Whether the lock is connected to its Tedee transport |

### Tedee Lock States

| State | Meaning |
|---:|---|
| `0` | `UNCALIBRATED` |
| `1` | `CALIBRATION` |
| `2` | `OPEN` |
| `3` | `PARTIALLY_OPEN` |
| `4` | `OPENING` |
| `5` | `CLOSING` |
| `6` | `CLOSED` |
| `7` | `PULL_SPRING` |
| `8` | `PULLING` |
| `9` | `UNKNOWN` |
| `255` | `UNPULLING` |

### Tedee Door States

| Value | Meaning |
|---:|---|
| `0` | `NOT_PAIRED` |
| `1` | `DISCONNECTED` |
| `2` | `OPENED` |
| `3` | `CLOSED` |
| `4` | `UNCALIBRATED` |

A physical door sensor is required for meaningful door-state testing. The current test installation does not have such a sensor.

### Device Settings

The following Tedee device settings are exposed as read-only channels:

| Channel ID | Item Type | Description |
|---|---|---|
| `autoLockEnabled` | Switch | Automatic lock enabled |
| `autoLockDelay` | Number | Automatic lock delay |
| `autoLockImplicitEnabled` | Switch | Implicit automatic lock enabled |
| `autoLockImplicitDelay` | Number | Implicit automatic lock delay |
| `pullSpringEnabled` | Switch | Pull spring feature enabled |
| `pullSpringDuration` | Number | Pull spring duration |
| `autoPullSpringEnabled` | Switch | Automatic pull spring enabled |
| `postponedLockEnabled` | Switch | Postponed lock enabled |
| `postponedLockDelay` | Number | Postponed lock delay |
| `buttonLockEnabled` | Switch | Physical button locking enabled |
| `buttonUnlockEnabled` | Switch | Physical button unlocking enabled |

These settings are read from the Tedee lock response and refreshed by polling or `REFRESH`. Writing settings is not currently implemented.

### Thing Properties

The Lock Thing exposes these properties:

| Property | Description |
|---|---|
| `name` | Tedee lock name |
| `serialNumber` | Tedee serial number |
| `deviceType` | Tedee device type |
| `firmwareVersion` | Firmware/software version reported by the Tedee API |

## Configuration Through MainUI

The binding can be configured through the openHAB UI.

### Local Connection

1. Install the Tedee binding.
2. Add a **Tedee Bridge**.
3. Enter the Bridge IP address and API token.
4. Wait until the Bridge becomes `ONLINE`.
5. Add a Tedee Lock below the Bridge or use autodiscovery.

### Cloud Connection

1. Install the Tedee binding.
2. Add a **Tedee Cloud** Bridge.
3. Enter the Tedee Personal Access Key.
4. Wait until the Cloud Bridge becomes `ONLINE`.
5. Add a Tedee Lock below the Cloud Bridge or use Cloud discovery.

## Text `.things` Configuration

The same Things can be defined in a `.things` file.

### Local Tedee Bridge

```text
Bridge tedee:bridge:home "Tedee Bridge" [
    ip="10.194.83.85",
    port=80,
    apiToken="YOUR_API_TOKEN",
    pollInterval=300
] {
    Thing lock:foxyhome "Foxy Home" [
        deviceId=14196,
        pollInterval=300
    ]
}
```

### Tedee Cloud

```text
Bridge tedee:cloud:cloud "Tedee Cloud" [
    personalAccessKey="YOUR_PERSONAL_ACCESS_KEY",
    pollInterval=300
] {
    Thing lock:foxyhomecloud "Foxy Home Cloud" [
        deviceId=14196,
        pollInterval=300
    ]
}
```

Do not publish a real API token or Personal Access Key.

## Items

Channels need to be linked to Items before their states are consumed by openHAB rules, UI pages or other automation.

### Main Lock Items

| Item | Type | Channel | Description |
|---|---|---|---|
| `Tedee_Lock` | Switch | `lock` | Lock/unlock control |
| `Tedee_State` | String | `state` | Lock state |
| `Tedee_Door_State` | String | `doorState` | Door sensor state |
| `Tedee_Battery` | Number | `battery` | Battery level |
| `Tedee_Charging` | Switch | `charging` | Charging state |
| `Tedee_Jammed` | Switch | `jammed` | Jam status |
| `Tedee_Connected` | Switch | `connected` | Connection status |
| `Tedee_Action` | String | `action` | Explicit Tedee actions |

### Main Lock Items Example

```text
Switch Tedee_Lock "Lock" {
    channel="tedee:lock:home:foxyhome:lock"
}

String Tedee_State "Lock State [%s]" {
    channel="tedee:lock:home:foxyhome:state"
}

String Tedee_Door_State "Door State [%s]" {
    channel="tedee:lock:home:foxyhome:doorState"
}

Number Tedee_Battery "Battery [%.0f %%]" {
    channel="tedee:lock:home:foxyhome:battery"
}

Switch Tedee_Charging "Charging" {
    channel="tedee:lock:home:foxyhome:charging"
}

Switch Tedee_Jammed "Jammed" {
    channel="tedee:lock:home:foxyhome:jammed"
}

Switch Tedee_Connected "Connected" {
    channel="tedee:lock:home:foxyhome:connected"
}

String Tedee_Action "Action" {
    channel="tedee:lock:home:foxyhome:action"
}
```

The same Item definitions can be used for a Cloud Lock by replacing the Thing UID:

```text
tedee:lock:cloud:foxyhomecloud:<channel>
```

For example:

```text
Switch Tedee_Cloud_Connected "Cloud Lock Connected" {
    channel="tedee:lock:cloud:foxyhomecloud:connected"
}
```

### Device Settings Items

| Item | Type | Channel | Description |
|---|---|---|---|
| `Tedee_AutoLockEnabled` | Switch | `autoLockEnabled` | Automatic lock |
| `Tedee_AutoLockDelay` | Number | `autoLockDelay` | Automatic lock delay |
| `Tedee_AutoLockImplicitEnabled` | Switch | `autoLockImplicitEnabled` | Implicit automatic lock |
| `Tedee_AutoLockImplicitDelay` | Number | `autoLockImplicitDelay` | Implicit automatic lock delay |
| `Tedee_PullSpringEnabled` | Switch | `pullSpringEnabled` | Pull spring enabled |
| `Tedee_PullSpringDuration` | Number | `pullSpringDuration` | Pull spring duration |
| `Tedee_AutoPullSpringEnabled` | Switch | `autoPullSpringEnabled` | Automatic pull spring |
| `Tedee_PostponedLockEnabled` | Switch | `postponedLockEnabled` | Postponed locking |
| `Tedee_PostponedLockDelay` | Number | `postponedLockDelay` | Postponed lock delay |
| `Tedee_ButtonLockEnabled` | Switch | `buttonLockEnabled` | Button locking |
| `Tedee_ButtonUnlockEnabled` | Switch | `buttonUnlockEnabled` | Button unlocking |

### Device Settings Items Example

```text
Switch Tedee_AutoLockEnabled "Auto Lock Enabled" {
    channel="tedee:lock:home:foxyhome:autoLockEnabled"
}

Number Tedee_AutoLockDelay "Auto Lock Delay" {
    channel="tedee:lock:home:foxyhome:autoLockDelay"
}

Switch Tedee_AutoLockImplicitEnabled "Auto Lock Implicit Enabled" {
    channel="tedee:lock:home:foxyhome:autoLockImplicitEnabled"
}

Number Tedee_AutoLockImplicitDelay "Auto Lock Implicit Delay" {
    channel="tedee:lock:home:foxyhome:autoLockImplicitDelay"
}

Switch Tedee_PullSpringEnabled "Pull Spring Enabled" {
    channel="tedee:lock:home:foxyhome:pullSpringEnabled"
}

Number Tedee_PullSpringDuration "Pull Spring Duration" {
    channel="tedee:lock:home:foxyhome:pullSpringDuration"
}

Switch Tedee_AutoPullSpringEnabled "Auto Pull Spring Enabled" {
    channel="tedee:lock:home:foxyhome:autoPullSpringEnabled"
}

Switch Tedee_PostponedLockEnabled "Postponed Lock Enabled" {
    channel="tedee:lock:home:foxyhome:postponedLockEnabled"
}

Number Tedee_PostponedLockDelay "Postponed Lock Delay" {
    channel="tedee:lock:home:foxyhome:postponedLockDelay"
}

Switch Tedee_ButtonLockEnabled "Button Lock Enabled" {
    channel="tedee:lock:home:foxyhome:buttonLockEnabled"
}

Switch Tedee_ButtonUnlockEnabled "Button Unlock Enabled" {
    channel="tedee:lock:home:foxyhome:buttonUnlockEnabled"
}
```

## Commands

### Lock / Unlock Through the Switch Channel

With:

```text
Switch Tedee_Lock
```

send:

```text
ON
```

to lock the device.

Send:

```text
OFF
```

to perform a normal unlock.

### Explicit Actions Through the Action Channel

The action Item accepts:

```text
LOCK
UNLOCK
UNLOCK_NO_PULL
PULL
```

`UNLOCK_NO_PULL` is useful when automatic pull spring is enabled but the desired operation is to unlock without pulling the spring.

The Tedee APIs return an acknowledgement as soon as the operation has started. The final state is reported later through polling and/or local callback events.

## Rules Examples

### Unlock Without Pull

DSL-style rule:

```text
sendCommand(Tedee_Action, "UNLOCK_NO_PULL")
```

JS Scripting:

```text
items.getItem("Tedee_Action").sendCommand("UNLOCK_NO_PULL");
```

### Lock

```text
sendCommand(Tedee_Action, "LOCK")
```

### Pull Spring

```text
sendCommand(Tedee_Action, "PULL")
```

## Refresh

The binding supports `RefreshType`.

A linked Item can receive:

```text
openhab:send Tedee_Battery REFRESH
```

or:

```text
openhab:send Tedee_Action REFRESH
```

A refresh triggers a complete lock refresh rather than updating only the requested channel. The API response contains multiple values, so all lock channels and properties are updated together.

The default lock polling interval is currently 300 seconds.

## Local Webhooks

Webhooks apply to the local Tedee Bridge connection.

The binding contains an HTTP webhook endpoint:

```text
/tedee/webhook
```

The Tedee Bridge can POST status events to this endpoint.

### Callback Configuration

```json
{
  "url": "http://OPENHAB_IP:8080/tedee/webhook",
  "method": "POST",
  "headers": []
}
```

For example:

```json
{
  "url": "http://10.194.82.111:8080/tedee/webhook",
  "method": "POST",
  "headers": []
}
```

The callback must point to the LAN address of the openHAB server, not `localhost` or `127.0.0.1`.

The local Bridge Handler manages the callback automatically and removes stale or duplicate binding callbacks.

### Implemented Webhook Event

The current servlet processes:

```text
lock-status-changed
```

Example payload received from a real Tedee Bridge:

```json
{
  "event": "lock-status-changed",
  "timestamp": "2026-09-22T21:34:40.395Z",
  "data": {
    "deviceType": 2,
    "deviceId": 14196,
    "serialNumber": "21180201-000004",
    "state": 4,
    "jammed": 0,
    "source": 18,
    "user_id": 0,
    "doorState": 0
  }
}
```

The handler applies these values immediately:

- `state`
- `doorState`
- `jammed`

### Additional Tedee Webhook Events

Tedee documents additional events that are useful for the binding:

- `device-connection-changed`
- `device-settings-changed`
- `device-battery-level-changed`
- `device-battery-start-charging`
- `device-battery-stop-charging`
- `device-battery-fully-charged`

These events are not yet processed individually by the current servlet implementation. The normal polling/refresh mechanism remains available as the fallback for the corresponding values.

## Local API

The local binding communicates with the Tedee Bridge through:

```text
http://<BRIDGE-IP>/v1.0
```

Authentication uses the `api_token` mechanism implemented by the binding.

### Main Local Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/v1.0/bridge` | Bridge status |
| `GET` | `/v1.0/lock/{id}` | Read one lock |
| `GET` | `/v1.0/lock` | Read available locks for discovery |
| `POST` | `/v1.0/lock/{id}/lock` | Lock |
| `POST` | `/v1.0/lock/{id}/unlock` | Unlock |
| `POST` | `/v1.0/lock/{id}/pull` | Pull spring |
| `POST` | `/v1.0/lock/{id}/unlock?mode=3` | Unlock without pull |
| `GET` | `/v1.0/callback` | Read callbacks |
| `POST` | `/v1.0/callback` | Register callback |
| `DELETE` | `/v1.0/callback/{callbackId}` | Remove callback |

The `mode=3` unlock operation is used for `UNLOCK_NO_PULL`.

Tedee documents lock, unlock and pull as asynchronous operations. The initial HTTP response confirms that the operation has started; the final state can be observed through a subsequent refresh, polling cycle or callback event.

## Cloud API

The Cloud binding communicates directly with the Tedee Cloud API.

### Cloud Authentication

Cloud requests use the Personal Access Key configured on the `tedee:cloud` Bridge.

The Access Key is stored as a password-type configuration parameter and must not be exposed in documentation or source control.

### Cloud Operations

The Cloud transport supports the same lock operations exposed by the local transport:

- `LOCK`
- `UNLOCK`
- `UNLOCK_NO_PULL`
- `PULL`

Cloud lock status is retrieved by polling. No local webhook or callback registration is required for Cloud Things.

### Cloud Discovery

The Cloud Bridge provides automatic lock discovery from the Tedee account.

Discovered locks use their Tedee `deviceId` as the representation property and are created below the Cloud Bridge.

## Local and Cloud in Parallel

Local and Cloud bridges can be configured at the same time.

For example, the same physical lock can be represented through both transports:

```text
Bridge tedee:bridge:home "Tedee Bridge" [
    ip="10.194.83.85",
    port=80,
    apiToken="YOUR_API_TOKEN",
    pollInterval=300
] {
    Thing lock:foxyhome "Foxy Home Local" [
        deviceId=14196,
        pollInterval=300
    ]
}

Bridge tedee:cloud:cloud "Tedee Cloud" [
    personalAccessKey="YOUR_PERSONAL_ACCESS_KEY",
    pollInterval=300
] {
    Thing lock:foxyhomecloud "Foxy Home Cloud" [
        deviceId=14196,
        pollInterval=300
    ]
}
```

The two Things are independent and use different transport paths to the same Tedee device.

## Development Status

The current implementation has been tested successfully with:

- openHAB 4.3.5
- Tedee Bridge
- Tedee PRO Lock
- local webhook events
- local lock discovery
- lock, unlock and pull commands
- `UNLOCK_NO_PULL`
- polling and `REFRESH`
- device settings
- Cloud API support and Cloud discovery

## Known Limitations

- Door state cannot be fully validated without a supported door sensor.
- Additional device information such as RSSI and device revision is not currently exposed as channels or properties.
- Device settings are currently read-only.
- Local webhook processing is currently focused on `lock-status-changed`; other documented webhook events continue to be handled through polling/refresh.

## References

### openHAB

- [openHAB 4.3 binding development](https://v43.openhab.org/docs/developer/bindings/)
- [openHAB 4.3 Thing and Channel definitions](https://v43.openhab.org/docs/developer/bindings/thing-xml)
- [openHAB 4.3 Things configuration](https://v43.openhab.org/docs/configuration/things.html)

### Tedee

- [Tedee Bridge API](https://github.com/tedee-com/tedee-documentation)
- [Tedee Bridge API – getting started](https://github.com/tedee-com/tedee-documentation/blob/master/bridge-api/overview/getting_started.md)
- [Tedee Bridge API – lock operations](https://github.com/tedee-com/tedee-documentation/blob/master/bridge-api/howtos/operate_locks.md)
- [Tedee Bridge API – webhook events](https://github.com/tedee-com/tedee-documentation/blob/master/bridge-api/webhooks/events.md)
