# Samsung SmartThings Binding

Control and monitor compatible Samsung appliances directly over your local network.
Depending on your appliance, you can switch power, read temperatures, change the target temperature, and select operating, fan, swing, or comfort modes.
State changes are delivered through event subscriptions where supported, with polling used only as a fallback.
No SmartThings Hub, SmartApp, cloud token, or cloud API is required for operation.

## Supported Things

| Thing Type | Description |
|------------|-------------|
| `appliance` | Samsung appliance with a compatible local OCF interface (`smartthings:appliance`). No bridge is required. |

The binding recognizes resources used by Samsung air conditioners, air purifiers, and dehumidifiers.
Available channels and controls depend on the model and firmware; not every appliance provides every setting or accepts commands.
Local readings and temperature-change notifications have been tested on a Samsung air conditioner.
This does not establish compatibility with every Samsung model or firmware version.

The appliance must expose compatible Samsung OCF resources and accept the binding's client credentials.
The default authentication works with firmware that accepts the Samsung service client profile; other firmware requires already authorized credentials or is unsupported.
This binding is **not** a general replacement for the SmartThings Hub and cannot control every device registered in SmartThings.
It does not pair appliances, obtain credentials from the cloud, or provision ownership.
Do not reset your appliance or remove its SmartThings registration to enable this integration.

## Discovery

Discovery runs only when you start a scan; there is no automatic or background network scanning.

1. Set **Discovery Subnet** in **Settings → Bindings → Samsung SmartThings Binding**, as described in [Binding Configuration](#binding-configuration).
1. Start a Samsung SmartThings scan in the Inbox.
1. Wait for the scan to finish, then add your appliance from the results.

Use `192.168.1.0/24` to scan a typical home subnet, or `192.168.1.50/32` to look for a single known appliance.
A full `/24` scan can take about three minutes; results appear when it completes.
Cancelling, restarting, or changing the scan configuration discards incomplete results.

Discovery fills in the appliance address, secure port, and expected device UUID.
It uses read-only, unauthenticated advertisements and does not pair the appliance, change its settings, or establish trust.
The appliance must advertise a device UUID, a secure port, and Samsung manufacturer information.
Some firmware does not provide this information; if your appliance is not found, try [manual Thing configuration](#thing-configuration).
UDP firewalls, guest-network isolation, VLAN routing, and appliance sleep modes may prevent discovery.

Use a DHCP reservation to keep the appliance address stable.
Discovery does not update an existing Thing's address after a DHCP change.

## Binding Configuration

The only binding-wide setting is the optional subnet used for Inbox scans.
Leave it empty if you intend to add appliances manually.

| Name | Type | Description | Default | Required |
|------|------|-------------|---------|----------|
| `discoverySubnet` | text | Private IPv4 subnet in CIDR notation, with a prefix from `/24` to `/32`. | N/A | no |

Configure it in **Settings → Bindings → Samsung SmartThings Binding**, or use the [configuration example](cfg/smartthings.cfg) to create `$OPENHAB_CONF/services/smartthings.cfg` with the following setting:

```properties
binding.smartthings:discoverySubnet=192.168.1.0/24
```

The subnet must be entirely within `10.0.0.0/8`, `172.16.0.0/12`, or `192.168.0.0/16`.
Only literal IPv4 addresses are accepted, not host names or public addresses.
Scans cover at most 256 addresses and use UDP ports `5683`, `49154`, and `49153`.

## Thing Configuration

### Prerequisites

- Give your appliance a stable IP address reachable from the openHAB server.
- Allow UDP traffic between openHAB and the appliance, including public discovery and its secure CoAP port.
- Enable the appliance's **Remote Control** setting where required to accept commands.
- Ensure the openHAB service account can store its client identity in `$OPENHAB_USERDATA/etc/smartthings/` with owner-only permissions.

### `appliance` Thing Configuration

To add an appliance manually in Main UI, select the Samsung SmartThings binding, add a **Samsung OCF Appliance**, and enter its host name or IP address.
Leave **Secure CoAP Port** at `0` to discover the secure port from that appliance.
This single-host lookup works without a configured discovery subnet.
If automatic port discovery fails, enter the appliance's known secure DTLS port explicitly.
Do not use a public discovery port as the secure port.

Normally, leave all credential settings empty: the binding generates and retains a client identity automatically.
See [Advanced Authentication](#advanced-authentication) if you already have authorized credentials or a verified certificate fingerprint.

| Name | Type | Description | Default | Required | Advanced |
|------|------|-------------|---------|----------|----------|
| `host` | text | Appliance IP address or host name. | N/A | yes | no |
| `port` | integer | Secure CoAP port; `0` discovers it from this host. Range: `0`–`65535`. | `0` | no | no |
| `clientPort` | integer | Local UDP port; `0` selects a stable port automatically. Set an unused port if Things conflict. Range: `0`–`65535`. | `0` | no | yes |
| `refreshInterval` | integer | Delay between fallback polls in seconds, used when event subscriptions are unavailable. Minimum: `10`. | `60` | no | no |
| `timeout` | integer | Per-request timeout in seconds. Range: `1`–`60`. | `12` | no | yes |
| `deviceId` | text | Expected appliance UUID. If omitted, the first authenticated UUID is remembered. | N/A | no | yes |
| `keyStore` | text | Absolute path to an authorized client PKCS12 key store on the openHAB server. Leave empty to use the generated identity. | N/A | no | yes |
| `keyStorePassword` | text | Password for the imported key store and private key. | N/A | no | yes |
| `serverFingerprint` | text | Independently verified SHA-256 fingerprint of the appliance's leaf certificate. Leave empty to use the bundled Samsung OCF CA. | N/A | no | yes |
| `ownerId` | text | Previously provisioned owner UUID, required when using `ownerPsk`. Not the appliance UUID. | N/A | no | yes |
| `ownerPsk` | text | Authorized OwnerPSK: 16 or 32 bytes encoded as hexadecimal. Mutually exclusive with imported certificate and fingerprint settings. | N/A | no | yes |

If `deviceId` is omitted, the authenticated UUID is saved in the Thing's properties and checked on subsequent connections.
Changing the host alone does not authorize a different appliance.
An identity mismatch prevents state updates and commands.

## Channels

Channels are created after the binding connects to your appliance and reads its capabilities.
Open the Thing's **Channels** tab to see which channels are available, their Item types, and their supported commands.
Only recognized appliance settings are exposed; unknown and security resources do not create channels.

The following table lists typical channel IDs for resources without an appliance-specific path prefix.
Actual IDs can differ, particularly for appliances with multiple units or settings sharing a resource path.
Always use the IDs shown on your Thing when linking Items.

| Channel | Type | Read/Write | Description |
|---------|------|------------|-------------|
| `power` | Switch | RW* | Appliance power. Use `ON` or `OFF`. |
| `remotectrl` | Switch | R | Whether Remote Control is enabled on the appliance. |
| `temperature-current` | Number:Temperature | R | Measured temperature in the appliance's reported unit. |
| `temperature-desired` | Number:Temperature | RW* | Target temperature, subject to the reported range and increment. |
| `mode` | String | RW* | Operating mode. Use the choices advertised by the appliance. |
| `wind-strength` | String | RW* | Fan setting. Commands may be numeric strings or names, depending on the appliance. |
| `wind-direction` | String | RW* | Swing setting, such as `Fix`, `All`, `Up_And_Low`, or `Left_And_Right`, if advertised. |
| `mode-convenient` | String | RW* | Comfort setting. On supporting firmware, `Nano` means WindFree and `NanoSleep` combines WindFree and sleep. |
| `airflow` | Number | RW* | Fan speed. Commands are limited to integers from `0` to `4` on supported air purifiers; otherwise read-only. |
| `energy-consumption` | Number:Power | R | Instantaneous power consumption. |
| `water-consumption` | Number | R | Raw cumulative water reading; no unit is inferred. |
| `kidslock` | String | R | Child-lock state. |

`RW*` means writable only when the appliance's current capabilities and Remote Control setting permit it.
A working read connection does not guarantee that the appliance accepts commands.

Some air conditioners provide temperatures through a combined resource instead of separate current and desired channels.
For these appliances, temperature channels are derived from `/temperatures/vs/0`, for a single zone with identifier `0`.
A raw cumulative power reading may be available when instantaneous consumption is absent; it uses a `Number` Item without an inferred unit.

Channel descriptions provide the supported mode commands, display names where available, and temperature units, ranges, and increments.
Use these values rather than assuming a generic temperature range or fan scale.
Read-only channels provide no command choices.
If upgrading from a version with long resource-based channel IDs, check and relink your existing Items to the new IDs.

### State Updates and Commands

The binding subscribes to appliance events using CoAP Observe over an authenticated, encrypted DTLS connection.
Changes made on the appliance or through another controller update linked Items as notifications arrive.
Polling stops when all subscriptions for successfully read resources are established.

If a subscription fails or is unsupported, the binding polls only the affected resources at `refreshInterval`; working subscriptions continue delivering updates.
Failed subscriptions are retried with increasing delays, starting at `refreshInterval` and capped at five minutes, or at `refreshInterval` if it is longer.
Registrations without an initial response expire after `timeout` and are recovered through fallback polling.
An unchanged setting is not treated as failed just because it sends no notifications.
Subscriptions are renewed automatically.
If the connection fails, the Thing goes OFFLINE and the binding attempts to reconnect using fallback polling.
An unavailable optional resource alone does not take an otherwise reachable appliance offline.

Sending `REFRESH` to a linked Item requests a full resource refresh without changing appliance settings.
After a command, the binding reads the appliance state back rather than assuming the command succeeded.
Some appliances report the new value only in a later notification or fallback poll.
Failed write requests are not retried automatically because the appliance may already have applied them.

## Full Example

This example assumes an air conditioner at `192.168.1.50` that exposes the listed channels.
Replace the address and channel IDs with those of your appliance, and omit Items for settings it does not expose.
The example uses automatic authentication and secure-port discovery.

### Thing Configuration

`$OPENHAB_CONF/things/smartthings.things`:

```java
Thing smartthings:appliance:bedroom "Bedroom Air Conditioner" [ host="192.168.1.50", port=0 ]
```

### Item Configuration

`$OPENHAB_CONF/items/smartthings.items`:

```java
Switch BedroomAC_Power "Power" { channel="smartthings:appliance:bedroom:power" }
Number:Temperature BedroomAC_Temperature "Room Temperature [%.1f %unit%]" { channel="smartthings:appliance:bedroom:temperature-current" }
Number:Temperature BedroomAC_Target "Target Temperature [%.1f %unit%]" { channel="smartthings:appliance:bedroom:temperature-desired" }
String BedroomAC_Mode "Operating Mode [%s]" { channel="smartthings:appliance:bedroom:mode" }
String BedroomAC_Fan "Fan Setting [%s]" { channel="smartthings:appliance:bedroom:wind-strength" }
```

### Sitemap Configuration

`$OPENHAB_CONF/sitemaps/smartthings.sitemap`:

```perl
sitemap smartthings label="Samsung Appliances" {
    Frame label="Bedroom Air Conditioner" {
        Switch item=BedroomAC_Power
        Text item=BedroomAC_Temperature
        Text item=BedroomAC_Target
        Text item=BedroomAC_Mode
        Text item=BedroomAC_Fan
    }
}
```

The sitemap provides a power switch and displays the remaining settings without assuming supported command values.
To add setpoint or mode controls, use the range, increment, and command choices shown in your Thing's channel descriptions.

## Advanced Authentication

Never share your key store, passwords, OwnerPSK, or owner UUID in logs, support reports, or configuration examples.
Do not disable certificate verification to make a connection work.

### Automatic Client Identity

Leave `keyStore`, `keyStorePassword`, `serverFingerprint`, `ownerId`, and `ownerPsk` empty to use the default authentication.
No OpenSSL setup, external certificate, key, or password is needed for compatible firmware.
The binding generates an RSA key and self-signed certificate using the Samsung service UUID `ab0b0ac4-aae9-4958-a04d-8ec36fe1b2f9`.
This firmware-specific profile is not a universal authorization or provisioning mechanism.

The identity is shared by the binding's appliances and stored in `$OPENHAB_USERDATA/etc/smartthings/client.p12`.
It is reused across restarts and renewed near expiry without changing its private key.
The generated key store uses an empty password; owner-only filesystem permissions protect it instead.
Protect userdata backups, and use a filesystem that can enforce these permissions.
A corrupt or unreadable identity is not silently replaced; restore a protected backup or resolve the storage problem.

The binding verifies appliance certificates against its bundled Samsung Electronics OCF Root CA, including signatures, validity, and authentication usage.
The CA's DER SHA-256 fingerprint is `E363FD4CC50380266D757321469E9ADEC15E5ECBEE28201447ECE02A52ED627F`.
The public certificate comes from the community-maintained [SmartThings-Local repository](https://github.com/QuiteYellow/SmartThings-Local/blob/main/smartthings_local/protocol/ocf_root_ca.pem); attribution is in [NOTICE](NOTICE).
Its manufacturer provenance has not been independently authenticated.
The binding does not download trust anchors, use system roots, or trust an unverified first connection.
Samsung certificates identify OCF UUIDs rather than IP addresses or DNS names, so the binding checks the authenticated device UUID rather than a certificate host name.

### Imported Client Certificate or Certificate Pin

To use an already authorized client identity, set `keyStore` to an absolute PKCS12 path containing one private key and its certificate chain.
Set `keyStorePassword` if needed, and restrict access to the file and password to the openHAB service account.

To replace CA validation with a certificate pin, set `serverFingerprint` to an independently verified SHA-256 fingerprint of the appliance's leaf certificate.
Hexadecimal fingerprints with optional colon separators are accepted.
Pins work with generated or imported client identities and still require a valid signing certificate.
Do not trust a fingerprint simply because it was returned by the configured IP address.
If the appliance certificate is replaced, obtain and verify its new fingerprint before updating the pin.

### OwnerPSK

Use this mode only if you already have a provisioned, authorized OwnerPSK.
Set `ownerId` to its owner UUID, not the appliance UUID, and `ownerPsk` to 32 or 64 hexadecimal characters representing a 16- or 32-byte key.
Leave `keyStore`, `keyStorePassword`, and `serverFingerprint` empty; OwnerPSK and certificate modes are mutually exclusive.
The binding does not derive or provision this key.

## Troubleshooting

| Symptom | What to check |
|---------|---------------|
| Appliance is not found | Check `discoverySubnet`, wait for the scan to finish, and allow UDP traffic. If the firmware lacks public discovery metadata, add the Thing manually. |
| Thing is OFFLINE | Check the address and advertised secure port, network isolation, identity-file permissions, and any imported credentials or certificate pin. Firmware that rejects the default profile needs authorized credentials or is unsupported. |
| Device identity mismatch | Verify that the configured address still belongs to the intended appliance and that `deviceId` matches it. Changing the host alone does not replace the remembered identity. |
| Readings work, but commands do not | Enable Remote Control if required, and check the channel's advertised commands and limits. A successful connection does not establish write permission; some firmware may acknowledge a command without applying it. |
| A setting is missing | Check the Thing's Channels tab. Only recognized resources returned by your appliance are exposed, and not all models support every setting. |
| Updates are delayed | The affected setting may be using fallback polling. Check `refreshInterval` and network reachability; failed subscriptions are retried automatically. |
| A command still shows the old value | Wait for the appliance's next notification or fallback poll, or request `REFRESH`. The displayed state comes from the appliance rather than an assumed command result. |
