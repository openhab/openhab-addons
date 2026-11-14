# VELUX ACTIVE Binding

This binding supports VELUX ACTIVE with NETATMO accounts, gateways, indoor climate sensors, departure switches and connected windows/covers.
For KLF 200 gateways, use the [Velux binding](https://www.openhab.org/addons/bindings/velux/).
The KIG 300 gateway supports HomeKit, although only for a subset of the available features.
For local integration, use the [HomeKit binding](https://www.openhab.org/addons/bindings/homekit/)

## Supported Things

| Thing                 | Thing type         | API module type | VELUX product                                      | Description                                                 |
| --------------------- | ------------------ | --------------- | -------------------------------------------------- | ----------------------------------------------------------- |
| Account               | `account`          |                 | VELUX ACTIVE with NETATMO account                  | OAuth authentication and shared home status polling         |
| Gateway               | `gateway`          | `NXG`           | KIG 300                                            | Gateway status and departure locking/unlocking              |
| Indoor Climate Sensor | `indoor-sensor`    | `NXS`           | KLA 300                                            | Diagnostics and climate measurements from its assigned room |
| Departure Switch      | `departure-switch` | `NXD`           | KLN 300                                            | Battery and connectivity diagnostics                        |
| Window/Cover          | `opening`          | `NXO`           | Connected window, blind or shutter; various models | Position, operating mode and movement commands              |

**KIX 300** is the VELUX ACTIVE starter kit, containing a gateway, an indoor climate sensor and a departure switch.
See the [VELUX product overview](https://www.velux.co.uk/professionals/technical-library/smart-home-systems/operation-and-remote-controls) for the commercial product references.

Gateway Bridges are children of the Account Bridge.
Indoor sensors, departure switches and windows/covers are children of their Gateway Bridge and inherit its home ID.
An ONLINE account Bridge means authentication succeeded; gateway connectivity is represented by each Gateway Bridge.

## Discovery

Create and configure an Account Bridge, wait for it to become ONLINE, then start a VELUX ACTIVE discovery scan.
The account discovers NXG gateways, including gateways that are currently unreachable.
Accept the gateways from the Inbox, then scan again to discover their NXS sensors, NXD departure switches and NXO windows/covers.
Accept a discovered module from the Inbox to start polling.

## Account Configuration

| Parameter       | Required | Default | Description                                         |
| --------------- | -------- | ------- | --------------------------------------------------- |
| username        | Yes      |         | Email address of the VELUX ACTIVE account           |
| password        | Yes      |         | Password of the VELUX ACTIVE account                |
| refreshInterval | No       | 60      | Home status polling interval in seconds; minimum 30 |

## Gateway Configuration

Discovery supplies the module identifier.
The binding resolves the home automatically from account topology.
An unresolved or ambiguous gateway is OFFLINE until its home is resolved and a status response confirms connectivity.
Its children remain OFFLINE until the gateway recovers.

| Parameter | Required | Description                                               |
| --------- | -------- | --------------------------------------------------------- |
| moduleId  | Yes      | ID of the NXG gateway, usually formatted as a MAC address |

### Gateway Pairing

Pair each gateway once to enable departure unlocking and window-opening commands.
The gateway must be ONLINE, and openHAB must be able to reach its LAN IP address on TCP port 25050.
The binding uses a temporary encrypted local connection to retrieve the signing key; ordinary commands continue through the cloud API.
The gateway protocol reuses ChaCha20-Poly1305 nonce values across both directions and does not authenticate the ephemeral key exchange.
Pair only on a trusted network; this connection must not be treated as secure transport.

1. Get the gateway's IP address from your router. Use the address of the gateway represented by this Bridge.
1. Invoke the gateway Thing action `pair(host)` using its IP address.
1. When `pairingStatus()` returns `WAITING_FOR_BUTTON`, briefly press the gateway's physical button to authorize retrieval.
1. Check `pairingStatus()` until it returns `PAIRED`. The attempt times out after 60 seconds.

The action returns immediately. Progress is STARTING, WAITING_FOR_BUTTON, then PAIRED, FAILED or TIMED_OUT.
Removing, reinitializing or disconnecting the gateway cancels an active attempt.
If pairing fails, check the IP address, LAN access and physical button, then invoke the action again.
Signed commands require a recent server clock sample and reported Netatmo firmware support (27 or later).
Firmware 60 and later uses HMAC-SHA512 with a nonce; earlier supported firmware uses the legacy SHA-512 signature.

Keys and replay counters are retained in private openHAB storage across normal restarts.
Protect openHAB's userdata and backups as they contain these credentials.
Removing the last configured gateway Thing for a physical gateway removes its locally stored key; removing one of several Things for that gateway preserves the shared key.
A gateway factory reset requires pairing again.
Deleting the local key does not revoke it on the gateway.
After pairing, send OFF to the gateway's `locked` channel to leave departure mode, or send position commands to its window Things.

## Channels

The Account Bridge has no channels.

### Gateway

Gateway Bridges provide:

| Channel   | Item type | Access     | Description                                                                   |
| --------- | --------- | ---------- | ----------------------------------------------------------------------------- |
| locked    | Switch    | Read/write | Reported departure lock state; send ON to lock or OFF to unlock after pairing |
| last-seen | DateTime  | Read-only  | Last contact timestamp reported by the service                                |

Departure locking sends the unsigned `away` scenario through the gateway.
This blocks movement through the VELUX system; it is not a door-lock command.
OFF sends the signed `home` scenario after gateway pairing.
Without pairing it is ignored with a warning.

Unavailable or missing channel values are UNDEF, rather than interpreted as unlocked.

### Indoor Climate Sensor

The physical NXS sensor combines module diagnostics and measurements from its assigned room.
Discovery provides `moduleId`, which is the only required parameter for manual configuration under a Gateway Bridge.

| Channel         | Item type            | Description                                   |
| --------------- | -------------------- | --------------------------------------------- |
| temperature     | Number:Temperature   | Room temperature                              |
| humidity        | Number:Dimensionless | Relative humidity in percent                  |
| co2             | Number:Dimensionless | CO₂ concentration in ppm                      |
| illuminance     | Number:Illuminance   | Room illuminance in lux                       |
| battery-level   | Number               | Battery percentage                            |
| low-battery     | Switch               | ON for low or very low reported battery state |
| signal-strength | Number               | Radio quality from 0 (very low) to 4 (full)   |
| last-seen       | DateTime             | Last contact reported by the service          |

All sensor channels are read-only.
An unreachable sensor yields UNDEF for all its channels.
If multiple sensors belong to one room, their climate channels show the same room-level readings; battery and signal values remain per-device.

## Departure Switch

The physical NXD departure switch is discovered automatically as a child of its Gateway Bridge.
Manual configuration under a Gateway Bridge requires `moduleId`.

| Channel         | Item type            | Description                                   |
| --------------- | -------------------- | --------------------------------------------- |
| battery-level   | Number               | Battery percentage                            |
| low-battery     | Switch               | ON for low or very low reported battery state |
| signal-strength | Number               | Radio quality from 0 (very low) to 4 (full)   |
| last-seen       | DateTime             | Last contact reported by the service          |

Missing values become UNDEF; an unreachable switch clears all four channel states.

Departure lock state and locking commands remain on the Gateway Bridge's `locked` channel.

## Window/Cover

NXO modules are discovered as Window/Cover Things (`opening`), covering roof windows, blinds, venetian blinds, shutters and awning blinds.
Manual configuration under a Gateway Bridge requires `moduleId`.

| Channel       | Item type     | Access     | Description                                                                 |
| ------------- | ------------- | ---------- | --------------------------------------------------------------------------- |
| position      | Rollershutter | Read/write | Current position: 0% open, 100% closed                                      |
| mode          | String        | Read-only  | Reported operating mode, for example `algo-available` or `algo-disabled`    |
| battery-state | String        | Read-only  | Battery state, when available (`full`, `high`, `medium`, `low`, `very-low`) |
| last-seen     | DateTime      | Read-only  | Last contact timestamp reported by the service                              |

UP requests fully open, DOWN requests fully closed, and percentage commands set intermediate positions.
Blinds and shutters support unsigned position commands.
Roof windows support closing (DOWN or 100%) without pairing.
Opening or partial positioning requires pairing the parent gateway.

**STOP stops all movements on the associated gateway**, including other windows and covers.
It is the gateway-wide stop operation, not an individual actuator stop.
The binding does not force movement past rain restrictions.

Unreachable modules go OFFLINE and clear their channels.
Modules absent from a partial response clear their channels and retain ONLINE status if previously online; otherwise they are OFFLINE until connectivity is confirmed.

## Example

### Thing Configuration

```java
Bridge veluxactive:account:home "VELUX ACTIVE Account" [
    username="YOUR_ACCOUNT_EMAIL",
    password="YOUR_ACCOUNT_PASSWORD",
    refreshInterval=60
] {
    Bridge gateway main "VELUX Gateway" [
        moduleId="YOUR_GATEWAY_ID"
    ] {
        Thing indoor-sensor climate [ moduleId="YOUR_SENSOR_ID" ]
        Thing departure-switch departure [ moduleId="YOUR_SWITCH_ID" ]
        Thing opening window [ moduleId="YOUR_OPENING_ID" ]
    }
}
```

### Item Configuration

```java
Rollershutter VeluxWindow "Position" <blinds> { channel="veluxactive:opening:home:main:window:position" }
Switch VeluxDepartureLocked "Departure Locked" <lock> { channel="veluxactive:gateway:home:main:locked" }
DateTime VeluxGatewayLastSeen "Last Seen" <time> { channel="veluxactive:gateway:home:main:last-seen" }
```
