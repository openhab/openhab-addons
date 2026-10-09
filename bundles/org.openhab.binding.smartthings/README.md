# Samsung SmartThings Binding

This binding supports direct, authenticated local control of compatible Samsung OCF appliances.

## Samsung OCF Appliances

The `appliance` Thing communicates directly with an appliance using CoAP over DTLS and CBOR.
It does not require a SmartThings Hub, SmartApp, cloud token, or cloud API for operation.
This is **not** a general replacement for the SmartThings Hub or a means of controlling every device registered in SmartThings.
Support depends on the appliance exposing compatible Samsung OCF resources and accepting the supplied local credentials.

### Prerequisites

- Give the appliance a stable address on the same reachable local network as openHAB.
- Allow UDP traffic between openHAB and the appliance.
  With `port=0`, the binding uses public CoAP discovery at that host to obtain the advertised secure DTLS port; this single-host lookup does not scan a subnet.
  Alternatively, configure the advertised secure port explicitly.
- Normally, no credential configuration is needed: the binding generates a persistent client identity and verifies the appliance certificate against its bundled Samsung OCF root.
  This requires firmware that accepts the Samsung service client profile described below.
  The binding does not obtain credentials from the cloud, pair with an appliance, provision ownership, or reset its security database.
- Enable the appliance's Remote Control setting where required for writes.
  This is separate from DTLS authentication: a working read connection does not prove that writes are permitted.

Do not reset the appliance, remove its SmartThings registration, or disable certificate verification to enable this integration.
Credential compatibility varies by firmware; cloud-free operation after configuration does not imply universally cloud-free credential provisioning.

### Discovery

Inbox discovery is an explicit, bounded subnet scan, not mDNS or multicast discovery.
There is no automatic or background network scanning, and no subnet is inferred from the openHAB network interfaces.
In Main UI, open **Settings → Bindings → Samsung SmartThings Binding** and set **Discovery Subnet** (`discoverySubnet`).
Then start a Samsung SmartThings scan in the Inbox.
Alternatively, set the binding configuration in `services/smartthings.cfg`:

```properties
binding.smartthings:discoverySubnet=192.168.1.0/24
```

Use a literal IPv4 CIDR with a prefix between `/24` and `/32`, entirely within the private ranges `10.0.0.0/8`, `172.16.0.0/12`, or `192.168.0.0/16`.
Public, loopback, link-local, multicast, unspecified, hostname-based, larger, and malformed ranges are rejected.
Host bits are normalized to the subnet boundary.
Each scan covers at most 256 total addresses; network and broadcast addresses are omitted for `/24` through `/30`, while both `/31` addresses and the single `/32` address are eligible.
Use `/32` to discover only one known appliance.
Leaving the setting empty disables Inbox discovery.

The scan uses read-only public CoAP requests to UDP ports `5683`, `49154`, and `49153`, with at most four concurrent probes and a three-second budget per host.
A complete `/24` scan can take about three minutes and is stopped after 210 seconds; results are published when the scan completes.
Cancelling, restarting, or reconfiguring a scan discards its incomplete results.
The appliance must expose its device UUID, an unambiguous secure DTLS port, and Samsung manufacturer metadata (`mnmn`) through `/oic/p`.
Unknown manufacturers and conflicting advertisements of the same UUID at different addresses are not published.
Some firmware does not provide public discovery metadata; create its Thing manually instead.
UDP firewalls, guest-network isolation, VLAN routing, and appliance sleep modes may prevent discovery.

Results have a stable UUID-based Thing identity and prefill `host`, `port`, and the expected `deviceId`.
These unauthenticated advertisements do **not** establish trust, obtain credentials, pair, or change appliance ownership or state.
The default certificate authentication is configured automatically; alternative credentials and certificate pins are advanced options described below.
Discovery does not update an existing Thing's configured host after a DHCP address change; use a DHCP reservation or update the host manually.

### Authentication

**Automatic certificate mode (default):** Leave `keyStore`, `keyStorePassword`, `serverFingerprint`, `ownerId`, and `ownerPsk` empty.
The binding generates an RSA client key and a self-signed client certificate using the Samsung service UUID `ab0b0ac4-aae9-4958-a04d-8ec36fe1b2f9`.
You do not need OpenSSL, certificates, keys, or a password to configure a compatible appliance.
This is a **firmware-specific compatibility profile**, not a universal authorization or provisioning mechanism.
Firmware that rejects this profile needs already authorized imported credentials or is unsupported.

The identity is shared by this binding's appliances and stored in `$OPENHAB_USERDATA/etc/smartthings/client.p12`.
It is reused across restarts and renewed near certificate expiry without changing its private key.
The directory and files are restricted to the openHAB service account; filesystems that cannot enforce owner-only permissions are rejected.
The automatically managed PKCS12 uses an empty password because filesystem access control, rather than a built-in password, protects the private key.
Protect userdata backups accordingly.
A corrupt or unreadable identity is not silently replaced; restore it from a protected backup or resolve the storage problem.

Appliance certificate signatures, validity and authentication usage are checked against the bundled Samsung Electronics OCF Root CA.
Its DER SHA-256 fingerprint is `E363FD4CC50380266D757321469E9ADEC15E5ECBEE28201447ECE02A52ED627F`.
The public certificate was obtained from the community-maintained [SmartThings-Local repository](https://github.com/QuiteYellow/SmartThings-Local/blob/main/smartthings_local/protocol/ocf_root_ca.pem); source and license attribution are in `NOTICE`.
Its manufacturer provenance has not been independently authenticated.
The binding does not download trust anchors at runtime, use system roots, or trust an unverified first connection.
Samsung certificates identify OCF UUIDs rather than IP/DNS names, so host-name verification is not used; the logical device UUID is checked separately through authenticated `/oic/d`.

**Advanced certificate options:** To use your own authorized identity, set `keyStore` to an absolute PKCS12 path containing one private key and its certificate chain, and set `keyStorePassword` if needed.
Protect this file and its password, and restrict access to the openHAB service account.
To override CA trust, set `serverFingerprint` to the SHA-256 fingerprint of the appliance's leaf certificate, obtained through an independently authenticated, out-of-band source.
Hexadecimal with optional colon separators is accepted.
This pins the valid, signing leaf certificate instead of using CA path validation, and works with either generated or imported client identities.
Never pin a certificate merely because it was returned by the configured IP address.
A replacement pinned certificate requires a newly verified fingerprint.

**OwnerPSK mode:** Set `ownerId` to the owner UUID associated with a previously provisioned, authorized OwnerPSK and set `ownerPsk` to the key in hexadecimal (16 or 32 bytes, hence 32 or 64 hexadecimal characters).
This owner UUID is not the device UUID.
Leave `keyStore`, `keyStorePassword`, and `serverFingerprint` unset in this mode; OwnerPSK and client certificate modes are mutually exclusive.
The binding does not derive or provision an OwnerPSK.

### Thing Configuration

An appliance is a standalone Thing without a bridge.
Create it manually in Main UI or in a `.things` file:

```java
Thing smartthings:appliance:bedroom "Bedroom Appliance" [ host="192.168.1.50", port=0 ]
```

Never publish your key store, passwords, OwnerPSK, or owner UUID in logs, support reports, or shared examples.

| Parameter           | Default | Description                                                                                          |
|---------------------|---------|------------------------------------------------------------------------------------------------------|
| `host`              | —       | Required appliance IP address or host name.                                                           |
| `port`              | `0`     | Secure CoAP port; `0` discovers the advertised port from this host.                                    |
| `clientPort`         | `0`     | Client UDP port; `0` selects a deterministic stable port. Override if multiple Things conflict.        |
| `refreshInterval`   | `60`    | Poll delay in seconds, minimum `10`.                                                                  |
| `timeout`           | `12`    | Per-request timeout in seconds, from `1` to `60`.                                                      |
| `deviceId`          | —       | Optional expected appliance UUID, checked against authenticated `/oic/d`.                             |
| `keyStore`          | —       | Optional imported client PKCS12 path; empty generates a persistent identity.                                                 |
| `keyStorePassword`  | —       | PKCS12/private-key password.                                                                         |
| `serverFingerprint` | —       | Optional trusted SHA-256 leaf pin; empty verifies against the bundled Samsung OCF CA.                 |
| `ownerId`           | —       | Previously provisioned owner UUID, required in OwnerPSK mode.                                         |
| `ownerPsk`          | —       | Authorized 16- or 32-byte OwnerPSK encoded as hexadecimal, required in OwnerPSK mode.                   |

If `deviceId` is omitted, the binding remembers the first authenticated device UUID in the Thing's `deviceId` property and checks subsequent reads against it.
Previously stored `deviceid` properties remain checked and are migrated after successful authentication.
A configured or remembered identity mismatch prevents state publication and writes.
Changing the host alone does not authorize a different appliance identity.

### Channels

Channels are created from authenticated resource representations rather than a fixed SmartThings capability list.
Their identifiers are derived deterministically from the resource path and field, so reconnecting does not rename channels.
Use the channels displayed on the Thing's Channels tab when linking Items.

Depending on the appliance's advertised capabilities, the following resources are recognized:

| Setting | Samsung OCF resource | Behavior |
|---------|----------------------|----------|
| Power | `/power/vs/0` | `On`/`Off`, exposed as a Switch. |
| Current temperature | `/temperature/current/0` | Read-only quantity in the advertised unit. |
| Target temperature | `/temperature/desired/0` | Quantity checked against the advertised range and increment. |
| Temperature fallback | `/temperatures/vs/0` | Supports a single zone with identifier `0` when standard temperature resources are unavailable. |
| Operating mode | `/mode/vs/0` | Only advertised modes; supports scalar or single-mode array representations. |
| Fan strength | `/wind/strength/vs/0` | Raw advertised choices, which may be numeric strings. |
| Swing direction | `/wind/direction/vs/0` | Raw advertised choices such as `Fix`, `All`, `Up_And_Low`, or `Left_And_Right`. |
| Comfort mode | `/mode/convenient/vs/0` | Only advertised choices; `Nano` means WindFree and `NanoSleep` combines WindFree and sleep on supporting Samsung firmware. |

The resource paths can vary by appliance; channels are derived from the resources actually returned.
Do not send guessed or unsupported modes.
Not every model exposes every setting or permits every write.
Temperatures use device-reported units; setpoints and other commands are checked against the device's live capabilities, limits, and increments.
No generic temperature range, fan scale, or fixed mode choices are assumed.
The channels expose appliance-specific state and command descriptions: advertised mode choices, supported switch and fan commands, and temperature units, limits, and increments.
These descriptions are refreshed alongside resource readings; read-only channels expose no command choices.
Other known appliance resources are mapped to appropriately typed channels; unknown non-security resources are available as read-only JSON diagnostics.
Security resources are never exposed as diagnostic channels.

The binding polls `/device/0`, requests the batch interface only when necessary, and reads linked resource stubs individually.
Partial representations preserve previously received fields rather than turning missing readings into zero or empty states.
`REFRESH` only reads; it does not write to the appliance.
Every write is serialized, revalidates live capabilities and Remote Control, and is followed by a readback instead of an optimistic state update.
A failed POST is not retried automatically because the appliance may already have applied it.
Some appliances acknowledge a write before their readings change; an immediate readback may still show the previous value until a subsequent poll.
Communication failures set the Thing offline, and subsequent polls can restore it online.

For troubleshooting, first check network reachability, the advertised port, identity storage permissions, any imported credentials or certificate pin, and the appliance's Remote Control setting.
Do not share credentials or bypass DTLS verification.
Firmware that rejects both authorized credential profiles is not supported by this local implementation.
