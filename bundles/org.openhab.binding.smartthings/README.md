# Samsung SmartThings Binding

This binding supports direct, authenticated local control of compatible Samsung OCF appliances, with particular support for air conditioners.
This is a local-only replacement: the former SmartThings Hub bridge, capability Things, servlet, and SmartApp integration are no longer supported.
Existing hub-based configurations must be removed and replaced with standalone local appliances; arbitrary hub-connected devices cannot be migrated to this protocol.

## Local Samsung OCF Appliances

The `localAppliance` Thing communicates directly with an appliance using CoAP over DTLS and CBOR, following the local protocol used by [localthings](https://github.com/mbillow/localthings).
It does not require a SmartThings Hub, SmartApp, cloud token, or cloud API for operation.
This is **not** a general replacement for the SmartThings Hub or a means of controlling every device registered in SmartThings.
Support depends on the appliance exposing compatible Samsung OCF resources and accepting the supplied local credentials.

### Local Prerequisites

- Give the appliance a stable address on the same reachable local network as openHAB.
- Allow UDP traffic between openHAB and the appliance.
  With `port=0`, the binding uses public CoAP discovery at that host to obtain the advertised secure DTLS port; it does not scan the network.
  Alternatively, configure the advertised secure port explicitly.
- Import credentials you are authorized to use, in one of the two modes below.
  The binding does not obtain credentials from the cloud, pair with an appliance, provision ownership, or reset its security database.
- Enable the appliance's Remote Control setting where required for writes.
  This is separate from DTLS authentication: a working read connection does not prove that writes are permitted.

Do not reset the appliance, remove its SmartThings registration, or disable certificate verification to enable this integration.
Credential compatibility varies by firmware; cloud-free operation after configuration does not imply universally cloud-free credential provisioning.

### Local Authentication

**Client certificate mode:** Set `keyStore` to an absolute path to a PKCS12 file on the openHAB server.
It must contain one authorized private key and its client certificate chain.
Set `keyStorePassword` and explicitly provide `serverFingerprint`, the SHA-256 fingerprint of the appliance's leaf certificate, obtained from a trusted, authenticated out-of-band source.
The fingerprint may be hexadecimal with colon separators.
The binding pins this certificate; it never automatically trusts the first certificate observed on the network.
Protect the key store and its password, and restrict file access to the openHAB service account.

Some compatible Samsung firmware accepts an offline-generated, UUID-compatible self-signed client identity, as used by localthings.
That profile uses the published Samsung service UUID `ab0b0ac4-aae9-4958-a04d-8ec36fe1b2f9` in the client certificate organizational unit, together with a UUID-formatted client identity in the common name.
This is a **firmware-specific compatibility option**, not a universal authorization or provisioning mechanism.
An imported client identity must still be accepted by the appliance, and the appliance certificate must still be explicitly pinned.
Strict newer OCF firmware may require a previously provisioned OwnerPSK instead.

For firmware known to accept that offline profile, create a client identity locally with OpenSSL:

```shell
umask 077
openssl req -x509 -newkey rsa:2048 -sha256 -days 365 \
  -keyout client.key -out client.pem \
  -subj "/OU=uuid:ab0b0ac4-aae9-4958-a04d-8ec36fe1b2f9/CN=urn:uuid:ab0b0ac4-aae9-4958-a04d-8ec36fe1b2f9" \
  -addext "basicConstraints=critical,CA:FALSE" \
  -addext "keyUsage=critical,digitalSignature,keyEncipherment" \
  -addext "extendedKeyUsage=clientAuth" \
  -addext "subjectAltName=URI:urn:uuid:ab0b0ac4-aae9-4958-a04d-8ec36fe1b2f9"
openssl pkcs12 -export -inkey client.key -in client.pem -out appliance.p12
```

Enter passwords interactively rather than putting them on the command line.
Use the PKCS12 export password for `keyStorePassword` and store `appliance.p12` where only the openHAB service account can read it.
This creates only a client credential; it does not establish trust in the appliance.
Obtain its certificate through an independently authenticated source, or verify its chain against an independently trusted Samsung OCF root before calculating the leaf fingerprint:

```shell
openssl x509 -in verified-appliance-leaf.pem -noout -fingerprint -sha256
```

Do not pin a certificate merely because it was returned by the configured IP address.
A replacement appliance certificate requires a newly verified fingerprint.

**OwnerPSK mode:** Set `ownerId` to the owner UUID associated with a previously provisioned, authorized OwnerPSK and set `ownerPsk` to the key in hexadecimal (16 or 32 bytes, hence 32 or 64 hexadecimal characters).
This owner UUID is not the device UUID.
Leave `keyStore` unset in this mode; OwnerPSK and client certificate modes are mutually exclusive.
The binding does not derive or provision an OwnerPSK.

### Local Thing Configuration

A local appliance is a standalone Thing without a bridge.
Create it manually in Main UI or in a `.things` file:

```java
Thing smartthings:localAppliance:bedroom "Bedroom Air Conditioner" [ host="192.168.1.50", port=0, keyStore="/etc/openhab/secrets/appliance.p12", keyStorePassword="YOUR_KEYSTORE_PASSWORD", serverFingerprint="YOUR_TRUSTED_SHA256_FINGERPRINT" ]
```

Replace the password and fingerprint placeholders with your authorized credentials.
Never publish your key store, passwords, OwnerPSK, or owner UUID in logs, support reports, or shared examples.

| Parameter           | Default | Description                                                                                          |
|---------------------|---------|------------------------------------------------------------------------------------------------------|
| `host`              | —       | Required appliance IP address or host name.                                                           |
| `port`              | `0`     | Secure CoAP port; `0` discovers the advertised port from this host.                                    |
| `localPort`         | `0`     | Local UDP port; `0` selects a deterministic stable port. Override if multiple Things conflict.        |
| `refreshInterval`   | `60`    | Poll delay in seconds, minimum `10`.                                                                  |
| `timeout`           | `12`    | Per-request timeout in seconds, from `1` to `60`.                                                      |
| `deviceId`          | —       | Optional expected appliance UUID, checked against authenticated `/oic/d`.                             |
| `keyStore`          | —       | Client PKCS12 file path, required in certificate mode.                                                 |
| `keyStorePassword`  | —       | PKCS12/private-key password.                                                                         |
| `serverFingerprint` | —       | Trusted SHA-256 appliance leaf certificate fingerprint, required in certificate mode.                 |
| `ownerId`           | —       | Previously provisioned owner UUID, required in OwnerPSK mode.                                         |
| `ownerPsk`          | —       | Authorized 16- or 32-byte OwnerPSK encoded as hexadecimal, required in OwnerPSK mode.                   |

If `deviceId` is omitted, the binding remembers the first authenticated device UUID in the Thing's `deviceid` property and checks subsequent reads against it.
A configured or remembered identity mismatch prevents state publication and writes.
Changing the host alone does not authorize a different appliance identity.

### Local Channels and Air Conditioners

Channels are created from authenticated resource representations rather than a fixed SmartThings capability list.
Their identifiers are derived deterministically from the resource path and field, so reconnecting does not rename channels.
Use the channels displayed on the Thing's Channels tab when linking Items.

On compatible air conditioners, the following resources are recognized:

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
Other known appliance resources are mapped to appropriately typed channels; unknown non-security resources are available as read-only JSON diagnostics.
Security resources are never exposed as diagnostic channels.

The binding polls `/device/0`, requests the batch interface only when necessary, and reads linked resource stubs individually.
Partial representations preserve previously received fields rather than turning missing readings into zero or empty states.
`REFRESH` only reads; it does not write to the appliance.
Every write is serialized, revalidates live capabilities and Remote Control, and is followed by a readback instead of an optimistic state update.
A failed POST is not retried automatically because the appliance may already have applied it.
Some appliances acknowledge a write before their readings change; an immediate readback may still show the previous value until a subsequent poll.
Communication failures set the Thing offline, and subsequent polls can restore it online.

For troubleshooting, first check network reachability, the advertised port, imported credentials, the pinned fingerprint where applicable, and the appliance's Remote Control setting.
Do not share credentials or bypass DTLS verification.
Firmware that rejects both authorized credential profiles is not supported by this local implementation.
