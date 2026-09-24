Tedee Binding for openHAB

The Tedee binding integrates Tedee Smart Locks with openHAB through the local Tedee Bridge API.

The binding is currently developed and tested against openHAB 4.3.x / 4.3.5, a Tedee Bridge, and a Tedee PRO Lock. Communication with the lock is performed locally through the bridge; no Tedee Cloud API is required for normal operation.

The binding currently supports:

Tedee Bridge

Tedee Lock

Lock, unlock and pull commands

Lock status polling

Manual REFRESH

Battery, charging, jammed and connection state

Firmware version and other Thing properties

Tedee device settings as read-only channels

Tedee Bridge webhooks for lock-status-changed

Webhook registration

Supported Things

Tedee Bridge

Thing type:

tedee:bridge

The Bridge represents the physical Tedee Bridge used for local API communication with the locks.

Configuration

Parameter

Type

Required

Default

Description

ip

text

yes



IP address of the Tedee Bridge

port

integer

no

80

HTTP port of the Tedee Bridge

apiToken

text

yes



Tedee Bridge API token

pollInterval

integer

no

10

Bridge status check interval in seconds

The Bridge must be reachable from the openHAB server. The Tedee Bridge API is local to the LAN. Tedee notes that the bridge itself still needs internet connectivity to obtain and refresh access certificates for paired devices.

Tedee Lock

Thing type:

tedee:lock

The Lock represents an individual Tedee lock connected to a Tedee Bridge.

A Lock Thing must be configured below a Tedee Bridge.

Configuration

Parameter

Type

Required

Default

Description

deviceId

integer

yes



Numeric Tedee device ID

pollInterval

integer

no

10

Lock status polling interval in seconds

Example:

deviceId=14196

Channels

The Lock Thing exposes the following channels.

Lock control

Channel ID

Item type

Read

Command

Description

lock

Switch

yes

ON/OFF

Lock or unlock the Tedee lock

action

String

no

LOCK, UNLOCK, UNLOCK_NO_PULL, PULL

Execute a specific Tedee action

The lock channel is the simple Switch interface:

ON → LOCK

OFF → normal UNLOCK

The action channel provides explicit actions:

LOCK

UNLOCK

UNLOCK_NO_PULL

PULL

When the Tedee lock has automatic pull spring enabled, normal UNLOCK can also perform the pull spring operation. Tedee documents UNLOCK_NO_PULL as the mode that unlocks without pulling the spring.

Lock state

Channel ID

Item type

Description

state

String

Internal Tedee lock state

doorState

String

Door sensor state, if supported by the lock/sensor setup

battery

Number

Battery level in percent

charging

Switch

Whether the lock is charging

jammed

Switch

Whether the lock reports a jam

connected

Switch

Whether the lock is connected to the Tedee Bridge

Tedee documents these lock states:

State

Meaning

0

UNCALIBRATED

1

CALIBRATION

2

OPEN

3

PARTIALLY_OPEN

4

OPENING

5

CLOSING

6

CLOSED

7

PULL_SPRING

8

PULLING

9

UNKNOWN

255

UNPULLING

Tedee documents these door states:

Value

Meaning

0

NOT_PAIRED

1

DISCONNECTED

2

OPENED

3

CLOSED

4

UNCALIBRATED

A physical door sensor is required for meaningful door-state testing. The current test installation does not have such a sensor.

Device settings

The following Tedee device settings are exposed as read-only channels:

Channel ID

Item type

Description

autoLockEnabled

Switch

Automatic lock enabled

autoLockDelay

Number

Automatic lock delay

autoLockImplicitEnabled

Switch

Implicit automatic lock enabled

autoLockImplicitDelay

Number

Implicit automatic lock delay

pullSpringEnabled

Switch

Pull spring feature enabled

pullSpringDuration

Number

Pull spring duration

autoPullSpringEnabled

Switch

Automatic pull spring enabled

postponedLockEnabled

Switch

Postponed lock enabled

postponedLockDelay

Number

Postponed lock delay

buttonLockEnabled

Switch

Physical button locking enabled

buttonUnlockEnabled

Switch

Physical button unlocking enabled

These settings are currently read from the normal Tedee lock response and refreshed by polling or REFRESH. Writing settings is not currently implemented.

Thing properties

The Lock Thing currently exposes these properties:

Property

Description

name

Tedee lock name

serialNumber

Tedee serial number

deviceType

Tedee device type

firmwareVersion

Firmware/software version reported by the local Bridge API

Configuration through MainUI

The binding can be configured through the openHAB UI.

Install the Tedee binding.

Add a Tedee Bridge .

Enter the Bridge IP address and API token.

After the Bridge becomes ONLINE, add a Tedee Lock below that Bridge or use autodiscovery.



Text .things configuration

The same Things can be defined in a .things file.

Example:

Bridge tedee:bridge:bridgename "Tedee Bridge" [
    ip="<yourbridgeIP>",
    port=80,
    apiToken="<YOUR_API_TOKEN>",
    pollInterval=60
] {
    Thing tedee:lock:lockname "Lockname" [
        deviceId=<your id>,
        pollInterval=300
    ]
}
Bridge tedee:cloud:bridgename "Tedee Cloud Bridge" [
    personalAccessKey="<YOUR_PERSONAL_ACCESS_KEY>", 
    pollInterval=300
] {
    Thing tedee:lock:lockname "Lockname" [
        deviceId=<your id>,
        pollInterval=300
    ]
}

The binding is built to work in API Token encrypted mode.

Items

Channels need to be linked to Items before their states are consumed by openHAB rules, UI pages or other automation.

The following example links the main lock functions:

Switch Tedee_Lock "Lock" {
    channel="tedee:lock:bridgename:lockname:lock"
}

String Tedee_State "Lock State [%s]" {
    channel="tedee:lock:bridgename:lockname:state"
}

String Tedee_Door_State "Door State [%s]" {
    channel="tedee:lock:bridgename:lockname:doorState"
}

Number Tedee_Battery "Battery [%.0f %%]" {
    channel="tedee:lock:bridgename:lockname:battery"
}

Switch Tedee_Charging "Charging" {
    channel="tedee:lock:bridgename:lockname:charging"
}

Switch Tedee_Jammed "Jammed" {
    channel="tedee:lock:bridgename:lockname:jammed"
}

Switch Tedee_Connected "Connected" {
    channel="tedee:lock:bridgename:lockname:connected"
}

String Tedee_Action "Action" {
    channel="tedee:lock:bridgename:lockname:action"
}

Device settings Items

Example read-only Items for the device settings:

Switch Tedee_AutoLockEnabled "Auto Lock Enabled" {
    channel="tedee:lock:bridgename:lockname:autoLockEnabled"
}

Number Tedee_AutoLockDelay "Auto Lock Delay" {
    channel="tedee:lock:bridgename:lockname:autoLockDelay"
}

Switch Tedee_PullSpringEnabled "Pull Spring Enabled" {
    channel="tedee:lock:bridgename:lockname:pullSpringEnabled"
}

Number Tedee_PullSpringDuration "Pull Spring Duration" {
    channel="tedee:lock:bridgename:lockname:pullSpringDuration"
}

Switch Tedee_AutoPullSpringEnabled "Auto Pull Spring Enabled" {
    channel="tedee:lock:bridgename:lockname:autoPullSpringEnabled"
}

Switch Tedee_PostponedLockEnabled "Postponed Lock Enabled" {
    channel="tedee:lock:bridgename:lockname:postponedLockEnabled"
}

Number Tedee_PostponedLockDelay "Postponed Lock Delay" {
    channel="tedee:lock:bridgename:lockname:postponedLockDelay"
}

Switch Tedee_ButtonLockEnabled "Button Lock Enabled" {
    channel="tedee:lock:bridgename:lockname:buttonLockEnabled"
}

Switch Tedee_ButtonUnlockEnabled "Button Unlock Enabled" {
    channel="tedee:lock:bridgename:lockname:buttonUnlockEnabled"
}

The current binding also exposes:

autoLockImplicitEnabled

and

autoLockImplicitDelay

in the same way.

Commands

Lock / unlock through the Switch channel

With:

Switch Tedee_Lock

send:

ON

to lock the device.

Send:

OFF

to perform a normal unlock.

Explicit actions through the Action channel

The action Item accepts:

LOCK
UNLOCK
UNLOCK_NO_PULL
PULL


UNLOCK_NO_PULL is useful when automatic pull spring is enabled but the desired operation is to unlock the lock without pulling the spring.

The Tedee Bridge API returns HTTP 204 as soon as the action has started. The final state is reported later through polling and/or callbacks.

Rules examples

Unlock without pull

DSL-style rule:

sendCommand(Tedee_Action, "UNLOCK_NO_PULL")

JS Scripting:

items.getItem("Tedee_Action").sendCommand("UNLOCK_NO_PULL");

Lock

sendCommand(Tedee_Action, "LOCK")

Pull spring

sendCommand(Tedee_Action, "PULL")

Refresh

The binding supports RefreshType.

A linked Item can receive:

openhab:send Tedee_Battery REFRESH

or:

openhab:send Tedee_Action REFRESH

A refresh triggers a complete lock refresh rather than updating only the requested channel. The Tedee Bridge response contains multiple values, so all lock channels and properties are updated together.

The normal polling interval is currently 300 seconds by default.

Webhooks

The binding contains an HTTP webhook endpoint:

/tedee/webhook

The Tedee Bridge can POST status events to this endpoint.

The tested callback configuration is:

{
  "url": "http://OPENHAB_IP:8080/tedee/webhook",
  "method": "POST",
  "headers": []
}

For example:

{
  "url": "http://10.194.82.111:8080/tedee/webhook",
  "method": "POST",
  "headers": []
}

The callback must point to the LAN address of the openHAB server.

Currently implemented webhook event

The current servlet processes:

lock-status-changed

Example payload received from a real Tedee Bridge:

{
  "event": "lock-status-changed",
  "timestamp": "2026-09-22T21:34:40.395Z",
  "data": {
    "deviceType": 2,
    "deviceId": XXX,
    "serialNumber": "XXXX",
    "state": 4,
    "jammed": 0,
    "source": 18,
    "user_id": 0,
    "doorState": 0
  }
}

The handler applies the following values immediately:

state

doorState

jammed


Other Tedee webhook events

Tedee documents additional events that are useful for this binding:

device-connection-changed

device-settings-changed

device-battery-level-changed

device-battery-start-charging

device-battery-stop-charging

device-battery-fully-charged

These are not yet processed by the current servlet implementation.

The planned implementation is:

connection event → update connected

battery-level event → update battery

charging events → update charging

settings-changed event → perform one lock GET and refresh all settings


Callback management during development

Tedee Bridge callback entries are managed through the local API:

GET /v1.0/callback

POST /v1.0/callback

DELETE /v1.0/callback/{callbackId}


A single callback for the openHAB endpoint is sufficient for the current binding.

Local API

The binding communicates with the Tedee Bridge through:

http://<BRIDGE-IP>/v1.0

Authentication uses the api_token mechanism implemented by the binding.

Main endpoints currently used:

GET  /v1.0/bridge
GET  /v1.0/lock/{id}

POST /v1.0/lock/{id}/lock
POST /v1.0/lock/{id}/unlock
POST /v1.0/lock/{id}/pull
POST /v1.0/lock/{id}/unlock?mode=3

The mode=3 unlock operation is used for UNLOCK_NO_PULL.

Tedee documents lock, unlock and pull as asynchronous operations. The initial HTTP response confirms that the operation has started; the final state can be observed through a subsequent sync or callback event.

Development status

The current implementation has been tested successfully with:

openHAB 4.3.5

Tedee Bridge

Tedee PRO Lock


Known limitations / next steps


Door state cannot be fully validated without a supported door sensor.

Additional device information such as RSSI and device revision not exposed yet.

Cloud API not yet integrated

References

openHAB 4.3 binding development:

https://v43.openhab.org/docs/developer/bindings/

openHAB 4.3 Thing and Channel definitions:

https://v43.openhab.org/docs/developer/bindings/thing-xml

openHAB 4.3 Things configuration:

https://v43.openhab.org/docs/configuration/things.html

Tedee Bridge API getting started:

https://github.com/tedee-com/tedee-documentation/blob/master/bridge-api/overview/getting_started.md

Tedee Bridge API lock operations:

https://github.com/tedee-com/tedee-documentation/blob/master/bridge-api/howtos/operate_locks.md

Tedee Bridge API webhook events:

https://github.com/tedee-com/tedee-documentation/blob/master/bridge-api/webhooks/events.md