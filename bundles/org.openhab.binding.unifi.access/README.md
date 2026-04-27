# UniFi Access Binding

This binding integrates [Ubiquiti UniFi Access](https://ui.com/door-access) door access control systems with openHAB.
It connects to your UniFi console using the internal API, providing real-time door events via WebSocket and channels to monitor and control door locks, access methods, and doorbell events.

## Supported Things

| Thing Type | Description                                                                     |
|------------|---------------------------------------------------------------------------------|
| `bridge`   | The UniFi console running UniFi Access. Required to discover and manage things. |
| `door`     | A door with lock control, position sensor, and access event triggers.           |
| `device`   | An access reader or intercom with access method toggles and doorbell events.    |

> **Bridge hierarchy.** The `unifiaccess:bridge` thing is a **child of the shared `unifi:controller` bridge** from the parent `org.openhab.binding.unifi` binding.
> The Access bridge itself does not carry `host` / `username` / `password` — host and credentials come from the parent controller bridge.
> If you are upgrading from an earlier version of the UniFi Access Binding, see the [UniFi binding README — Upgrading from earlier UniFi bindings](../org.openhab.binding.unifi/README.md#upgrading-from-earlier-unifi-bindings) section for the reparenting steps.

## Discovery

1. First create a `unifi:controller` bridge from the parent UniFi binding pointing at your UniFi console with a local user account.
1. Then add a `bridge` as a child of that controller bridge.
1. Once the Access `bridge` is ONLINE, `door`s and reader `device`s are discovered automatically and appear in the Inbox.
1. Approve discovered items or create them manually using `deviceId`.

Hub devices (UA-Hub, UA-Hub-Door-Mini, UA-Ultra, etc.) are not discovered as separate things because the door thing already represents the hub's lock and position sensor functions.
Only reader/intercom devices (G6 Entry Pro, UA-LITE, UA-G2-PRO, etc.) appear as `device` things since they control access methods like NFC, face unlock, and PIN.

## Thing Configuration

### Bridge (`bridge`)

The Access bridge is a child of the `unifi:controller` bridge.
Host, username, and password come from the parent controller bridge — the Access bridge itself only carries Access-specific settings.

| Parameter       | Type    | Description                                            | Default | Required | Advanced |
|-----------------|---------|--------------------------------------------------------|---------|----------|----------|
| refreshInterval | integer | Polling interval in seconds for periodic state sync.   | 300     | no       | yes      |

Authentication is handled by the parent `unifi:controller` bridge — configure the local console user once on the controller bridge.
Cloud-only accounts are not supported; a local user account on the console is required.

### Door (`door`)

| Parameter | Type | Description                                              | Required |
|-----------|------|----------------------------------------------------------|----------|
| deviceId  | text | Unique door identifier from the UniFi Access controller. | yes      |

### Device (`device`)

| Parameter | Type | Description                                                | Required |
|-----------|------|------------------------------------------------------------|----------|
| deviceId  | text | Unique device identifier from the UniFi Access controller. | yes      |

## Channels

### Bridge Channels

| Channel ID       | Item Type | RW | Description                                                                     |
|------------------|-----------|----|---------------------------------------------------------------------------------|
| emergency-status | String    | RW | System-wide emergency mode: `normal`, `lockdown`, or `evacuation`.              |
| log-insight      | Trigger   | -  | Fires for insight log events. Payload is JSON with event details.               |
| log              | Trigger   | -  | Fires for access log events. Payload is JSON with event details.                |

### Door Channels

| Channel ID               | Item Type     | RW   | Description                                                                        |
| ------------------------ | ------------- | ---- | ---------------------------------------------------------------------------------- |
| lock                     | Switch        | RW   | Lock state. ON = locked, OFF = unlocked.                                           |
| position                 | Contact       | R    | Door position sensor. OPEN or CLOSED.                                              |
| last-unlock              | DateTime      | R    | Timestamp of the last unlock event.                                                |
| last-actor               | String        | R    | Name of the person who last unlocked the door.                                     |
| lock-rule                | String        | R    | Current lock rule: `schedule`, `custom`, `keep_unlock`, `keep_lock`, `none`, etc.  |
| keep-unlocked            | Switch        | RW   | Keep door unlocked indefinitely. ON to activate, OFF to reset.                     |
| keep-locked              | Switch        | RW   | Keep door locked indefinitely. ON to activate, OFF to reset.                       |
| unlock-minutes           | Number:Time   | W    | Unlock for the specified number of minutes.                                        |
| unlock-until             | DateTime      | W    | Unlock until the specified date/time.                                              |
| thumbnail                | Image         | R    | Latest door camera thumbnail image.                                                |
| access-attempt-success   | Trigger       | -    | Fires on successful access. JSON payload with actor, credential provider, etc.     |
| access-attempt-failure   | Trigger       | -    | Fires on failed access attempt. JSON payload with actor details.                   |
| remote-unlock            | Trigger       | -    | Fires when an admin remotely unlocks the door.                                     |
| doorbell-status          | Trigger       | -    | Fires on doorbell status changes (timeout, answered, rejected, etc.).              |
| alarm                    | Trigger       | -    | Fires on door alarms (e.g., `force_unlock` when door is forced open while locked). |
| log-insight              | Trigger       | -    | Fires for insight log events referencing this door.                                |

### Device Channels

Access method channels are populated for reader/intercom devices (e.g., G6 Entry Pro, UA-LITE, UA-G2-PRO).
Hub devices are not discovered as device things — their lock and sensor state is on the door thing.

| Channel ID            | Item Type | RW | Description                                                   |
|-----------------------|-----------|----|---------------------------------------------------------------|
| nfc-enabled           | Switch    | RW | Enable/disable NFC card access.                               |
| pin-enabled           | Switch    | RW | Enable/disable PIN code access.                               |
| pin-shuffle           | Switch    | RW | Enable/disable PIN keypad shuffle.                            |
| face-enabled          | Switch    | RW | Enable/disable face unlock.                                   |
| mobile-tap-enabled    | Switch    | RW | Enable/disable mobile tap (Bluetooth).                        |
| mobile-button-enabled | Switch    | RW | Enable/disable mobile app unlock button.                      |
| mobile-shake-enabled  | Switch    | RW | Enable/disable mobile shake gesture.                          |
| mobile-wave-enabled   | Switch    | RW | Enable/disable mobile wave gesture.                           |
| wave-enabled          | Switch    | RW | Enable/disable hand wave gesture.                             |
| qr-code-enabled       | Switch    | RW | Enable/disable QR code access.                                |
| touch-pass-enabled    | Switch    | RW | Enable/disable Touch Pass / Apple Pass access.                |
| face-anti-spoofing    | String    | RW | Face anti-spoofing level: `high`, `medium`, or `no`.          |
| face-detect-distance  | String    | RW | Face detection distance: `near`, `medium`, or `far`.          |
| door-sensor           | Contact   | R  | Door position sensor from the associated door.                |
| doorbell-contact      | Contact   | R  | Doorbell contact state. OPEN during a ring, CLOSED otherwise. |
| online                | Switch    | R  | Whether the device is currently online.                       |

Device trigger channels:

| Channel ID      | Events / Payload                                           | Description                                  |
|-----------------|------------------------------------------------------------|----------------------------------------------|
| doorbell        | `pressed`, `incoming`, `incoming-ren`, `completed`         | Fires on doorbell events (see below).        |
| doorbell-status | `DOORBELL_TIMED_OUT`, `ADMIN_UNLOCK_SUCCEEDED`, etc.       | Fires on intercom call resolution.           |
| log-insight     | JSON payload with logKey, eventType, message, result, etc. | Fires for insight log events on this device. |

## Trigger Events — Detailed Reference

### Doorbell Event Sequence

When someone presses the doorbell button on a reader/intercom, the following events fire in order on the **device** thing:

| Step | Channel            | Event          | Description                                                                               |
|------|--------------------|----------------|-------------------------------------------------------------------------------------------|
| 1    | `doorbell`         | `pressed`      | Hardware button physically pressed. Fires immediately. No corresponding "released" event. |
| 2    | `doorbell`         | `incoming`     | Intercom call session started. Admins are notified and can answer.                        |
| 2a   | `doorbell`         | `incoming-ren` | (Alternative) Request-to-Enter button pressed instead of regular doorbell.                |
| 3    | `doorbell-contact` | `OPEN`         | Contact channel opens when intercom call is active.                                       |
| 4    | `doorbell-status`  | (reason)       | Intercom call resolved. See status events below.                                          |
| 5    | `doorbell`         | `completed`    | Intercom call session ended.                                                              |
| 6    | `doorbell-contact` | `CLOSED`       | Contact channel closes when call ends.                                                    |

**`pressed` vs `incoming`**: The `pressed` event is the raw hardware button press — it fires instantly with no follow-up. The `incoming` event is the intercom call session that follows, which has a full lifecycle (incoming → status → completed). Use `pressed` for instant doorbell notifications. Use `incoming`/`completed` to track the intercom call lifecycle.

**`doorbell-status` events** (fired at step 4):

| Event                       | Description                                               |
|-----------------------------|-----------------------------------------------------------|
| `DOORBELL_TIMED_OUT`        | No admin answered within the timeout period.              |
| `ADMIN_REJECTED_UNLOCK`     | An admin declined to unlock the door.                     |
| `ADMIN_UNLOCK_SUCCEEDED`    | An admin unlocked the door via the intercom.              |
| `VISITOR_CANCELED_DOORBELL` | The visitor canceled the doorbell before anyone answered. |
| `ANSWERED_BY_ANOTHER_ADMIN` | Another admin already answered the call.                  |
| `UNKNOWN`                   | Unrecognized reason code.                                 |

### Door Alarm Events

The `alarm` trigger channel fires on the **door** thing when a physical alarm condition is detected:

| Event          | Description                                                                                                                     |
|----------------|---------------------------------------------------------------------------------------------------------------------------------|
| `force_unlock` | Door was physically forced open while locked. This is a security event — the lock was engaged but the door was opened by force. |

The alarm fires as part of the `access.data.v2.location.update` and `access.data.v2.device.update` WebSocket events. It will fire on every location update that includes the alarm, so you may want to debounce in your rule.

### Door Access Events

These fire on the **door** thing when someone uses the door:

| Channel                  | Payload                                                     | When                                                                                          |
|--------------------------|-------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| `access-attempt-success` | JSON: `actorName`, `credentialProvider`, `message`          | Someone successfully accessed the door (NFC tap, PIN entry, face unlock, remote unlock, etc.) |
| `access-attempt-failure` | JSON: `actorName`, `credentialProvider`, `message`          | An access attempt was denied (invalid card, wrong PIN, etc.)                                  |
| `remote-unlock`          | JSON: `deviceId`, `name`, `fullName`, `level`, `workTimeId` | An admin remotely unlocked the door from the UniFi app or API.                                |
| `doorbell-status`        | Same events as device doorbell-status                       | Doorbell status changes associated with this door.                                            |

The `credentialProvider` field in access events indicates how the person authenticated: `NFC`, `PIN_CODE`, `FACE`, `REMOTE_THROUGH_UAH`, `TOUCH_PASS`, etc.

### Bridge-Level Events

These fire on the **bridge** thing for all doors/devices:

| Channel       | Payload                                                                                                                  | When                                                                                                                    |
|---------------|--------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| `log-insight` | JSON: `logKey`, `eventType`, `message`, `published`, `result`, `actorName`, `doorId`, `doorName`, `deviceId`, `cameraId` | Any significant access event occurs (unlock, deny, doorbell, etc.). This is a superset — every access event fires here. |
| `log`         | JSON: `type`, `displayMessage`, `result`, `published`, `logKey`, `logCategory`, `actorName`                              | Raw access log entries. Lower-level than insight logs.                                                                  |

**Choosing which trigger to use:**

- **"Someone rang the doorbell"** → Use device `doorbell` channel with `pressed` or `incoming` event
- **"The doorbell was answered/timed out"** → Use device `doorbell-status` channel
- **"Someone unlocked the door"** → Use door `access-attempt-success` channel
- **"Access was denied"** → Use door `access-attempt-failure` channel
- **"An admin remotely unlocked"** → Use door `remote-unlock` channel
- **"Door was forced open"** → Use door `alarm` channel with `force_unlock` event
- **"Any access event on any door"** → Use bridge `log-insight` channel

## Full Examples

### Things (`.things`)

The Access bridge is a child of the `unifi:controller` bridge.
Host and credentials live on the controller bridge; the Access bridge only carries the polling interval.

```java
Bridge unifi:controller:home "UniFi Console" [ host="192.168.1.1", username="localadmin", password="secret" ] {
    Bridge unifiaccess:bridge access "UniFi Access" [ refreshInterval=300 ] {
        Thing door frontdoor "Front Door" [ deviceId="296f6fcb-9da6-416e-85fb-cadcd8842850" ]
        Thing device intercom "Front Intercom" [ deviceId="69c70f420080a003e40d8190" ]
    }
}
```

With the Access bridge nested under the controller, existing item channel UIDs like `unifiaccess:door:home:access:frontdoor:lock` embed the parent bridge ID (`home`) and the Access bridge ID (`access`).
Update your `.items` file's channel references accordingly if you were previously using the flat `unifiaccess:bridge:home` layout — or use the openHAB UI to re-link items, which handles the new UIDs automatically.

### Items (`.items`)

```java
// Door status
Switch      UA_FrontDoor_Lock         "Front Door Lock"                      { channel="unifiaccess:door:home:frontdoor:lock" }
Contact     UA_FrontDoor_Position     "Front Door [%s]"                      { channel="unifiaccess:door:home:frontdoor:position" }
DateTime    UA_FrontDoor_LastUnlock   "Last Unlock [%1$ta %1$tR]"            { channel="unifiaccess:door:home:frontdoor:last-unlock" }
String      UA_FrontDoor_LastActor    "Last Actor [%s]"                      { channel="unifiaccess:door:home:frontdoor:last-actor" }
String      UA_FrontDoor_LockRule     "Lock Rule [%s]"                       { channel="unifiaccess:door:home:frontdoor:lock-rule" }

// Door controls
Switch      UA_FrontDoor_KeepUnlocked "Keep Unlocked"                        { channel="unifiaccess:door:home:frontdoor:keep-unlocked" }
Switch      UA_FrontDoor_KeepLocked   "Keep Locked"                          { channel="unifiaccess:door:home:frontdoor:keep-locked" }
Number:Time UA_FrontDoor_UnlockMins   "Unlock Minutes [%.0f min]"            { channel="unifiaccess:door:home:frontdoor:unlock-minutes" }

// Intercom access methods
Switch      UA_Intercom_NFC           "NFC Access"                           { channel="unifiaccess:device:home:intercom:nfc-enabled" }
Switch      UA_Intercom_Face          "Face Unlock"                          { channel="unifiaccess:device:home:intercom:face-enabled" }
Switch      UA_Intercom_PIN           "PIN Access"                           { channel="unifiaccess:device:home:intercom:pin-enabled" }
Switch      UA_Intercom_QR            "QR Code Access"                       { channel="unifiaccess:device:home:intercom:qr-code-enabled" }
Switch      UA_Intercom_Online        "Intercom Online"                      { channel="unifiaccess:device:home:intercom:online" }

// Bridge
String      UA_Emergency              "Emergency Status [%s]"                { channel="unifiaccess:bridge:home:emergency-status" }
```

### Sitemap (`.sitemap`)

```perl
sitemap unifiaccess label="UniFi Access" {
    Frame label="Front Door" {
        Switch    item=UA_FrontDoor_Lock
        Text      item=UA_FrontDoor_Position
        Text      item=UA_FrontDoor_LastUnlock
        Text      item=UA_FrontDoor_LastActor
        Text      item=UA_FrontDoor_LockRule
        Switch    item=UA_FrontDoor_KeepUnlocked
        Switch    item=UA_FrontDoor_KeepLocked
    }
    Frame label="Intercom" {
        Switch    item=UA_Intercom_NFC
        Switch    item=UA_Intercom_Face
        Switch    item=UA_Intercom_PIN
        Switch    item=UA_Intercom_QR
        Text      item=UA_Intercom_Online
    }
    Frame label="System" {
        Text      item=UA_Emergency
    }
}
```

### Rules (JS Scripting)

```javascript
// Instant doorbell notification (hardware button press)
rules.when().channel('unifiaccess:device:home:intercom:doorbell').triggered('pressed').then(e => {
    console.log("Doorbell button pressed!");
    // Send notification, turn on porch light, etc.
}).build('Doorbell Pressed');

// Track intercom call lifecycle
rules.when().channel('unifiaccess:device:home:intercom:doorbell-status').triggered().then(e => {
    const status = e.payload.event;
    if (status === 'ADMIN_UNLOCK_SUCCEEDED') {
        console.log("Door was unlocked via intercom");
    } else if (status === 'DOORBELL_TIMED_OUT') {
        console.log("Nobody answered the doorbell");
    }
}).build('Doorbell Status');

// Door forced open alarm (security alert)
rules.when().channel('unifiaccess:door:home:frontdoor:alarm').triggered('force_unlock').then(e => {
    console.log("SECURITY: Door was forced open while locked!");
    // Send push notification, trigger siren, etc.
}).build('Door Forced Open');

// Door access events
rules.when().channel('unifiaccess:door:home:frontdoor:access-attempt-success').triggered().then(e => {
    const data = JSON.parse(e.payload.event);
    console.log("Access granted:", data.actorName, "via", data.credentialProvider);
}).build('Access Success');

rules.when().channel('unifiaccess:door:home:frontdoor:access-attempt-failure').triggered().then(e => {
    const data = JSON.parse(e.payload.event);
    console.log("Access DENIED:", data.actorName);
    // Send security alert
}).build('Access Denied');

// Global access log (fires for all doors)
rules.when().channel('unifiaccess:bridge:home:log-insight').triggered().then(e => {
    const data = JSON.parse(e.payload.event);
    console.log("Access event:", data.eventType, data.message, "at door:", data.doorName);
}).build('Global Access Log');
```
