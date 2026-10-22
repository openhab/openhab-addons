# Samsung SmartThings Binding

The Samsung SmartThings binding controls and monitors compatible Samsung appliances over the local network using authenticated CoAP over DTLS. It does not require a SmartThings Hub or cloud access.

## Supported Things

| Thing type | Thing Type UID | Description |
|---|---|---|
| Air conditioner | `smartthings:airconditioner` | Samsung air conditioner. |
| Air purifier | `smartthings:airpurifier` | Samsung air purifier. |
| Dehumidifier | `smartthings:dehumidifier` | Samsung dehumidifier. |
| Dryer | `smartthings:dryer` | Samsung dryer. |
| Washer | `smartthings:washer` | Samsung washer. |
| Generic appliance | `smartthings:appliance` | Diagnostic Thing for unclassified devices; it is not a general-purpose fallback for unsupported appliances. |

Compatibility depends on appliance model and firmware. Discovery chooses a concrete Thing when the appliance advertises a supported OCF device type. Unclassified devices are excluded unless enabled in the binding configuration.

## Discovery

Start a **Samsung SmartThings** scan from the Inbox. Discovery does not run automatically or in the background. By default, it scans the private IPv4 subnet selected through **Settings → Network**. If that subnet cannot be used, configure `discoverySubnet` below. Scans cover at most 256 addresses and accept private IPv4 CIDR ranges from `/24` to `/32`.

The appliance must be reachable from openHAB over UDP and advertise discovery information including its device UUID and secure CoAP port. Firewalls, guest-network isolation, VLANs, or firmware that omits discovery information may prevent automatic discovery; in that case, add the Thing manually.

## Binding Configuration

Configure these settings under **Settings → Bindings → Samsung SmartThings Binding**, or in `$OPENHAB_CONF/services/smartthings.cfg` (see [`cfg/smartthings.cfg`](cfg/smartthings.cfg)). They affect Inbox scans only; they do not initiate scans.

| Name | Type | Description | Default | Required |
|---|---|---|---|---|
| `discoverySubnet` | text | Optional private IPv4 subnet in CIDR notation (`/24`–`/32`). When empty, discovery uses the primary IPv4 interface's subnet from Network settings. | Empty | no |
| `discoveryConcurrency` | integer | Maximum number of addresses probed simultaneously. Range: `1`–`32`. | `16` | no |
| `discoverUnclassified` | boolean | Include devices that do not advertise a supported appliance family as generic diagnostic Things. This does not make them compatible. | `false` | no |

Example override:

```properties
discoverySubnet=192.168.1.0/24
discoveryConcurrency=16
```

## Thing Configuration

Discovery creates Things with their address, secure CoAP port, and expected device UUID. To add a Thing manually, select the appliance family and enter its IP address or host name. The advanced `port` setting defaults to `0`, which discovers the secure port from that host.

The appliance must be reachable from openHAB over UDP. Enable its **Remote Control** setting when required for commands. By default, the binding creates and stores a client identity under `$OPENHAB_USERDATA/etc/smartthings/`. Some firmware may require credentials that have already been authorized; the binding does not pair appliances or provision credentials. Certificate and OwnerPSK settings are advanced options for users who already have suitable credentials. In certificate mode, the first valid server certificate is trusted on first use and saved only after the authenticated `/oic/d` identity matches the Thing. This first connection is not independently authenticated, so an attacker able to intercept it could be pinned; configure an independently obtained fingerprint to avoid that risk.

All Thing types use the following configuration parameters:

| Name | Type | Description | Default | Required | Advanced |
|---|---|---|---|---|---|
| `host` | text | Appliance IP address or host name. | — | yes | no |
| `port` | integer | Secure CoAP/DTLS port (`0` discovers it from the appliance). Range: `0`–`65535`. | `0` | no | yes |
| `clientPort` | integer | Local UDP port (`0` selects a stable port automatically). Specify an unused port if multiple Things conflict. Range: `0`–`65535`. | `0` | no | yes |
| `refreshInterval` | integer | Interval in seconds for fallback polling when event subscriptions are unavailable. Minimum: `10`. | `60` | no | no |
| `timeout` | integer | Maximum time in seconds for each discovery or authenticated CoAP request. Range: `1`–`60`. | `12` | no | yes |
| `deviceId` | text | Expected appliance UUID. If omitted, the first authenticated UUID is remembered and checked on later connections. | — | no | yes |
| `keyStore` | text | Absolute path to a PKCS12 key store containing an already-authorized client private key and certificate chain. Leave empty to use the generated identity. | — | no | yes |
| `keyStorePassword` | password | Password for the imported key store and private key. | — | no | yes |
| `serverFingerprint` | text | Optional SHA-256 fingerprint of the appliance's leaf certificate. Leave empty to trust and remember the first valid certificate after the authenticated appliance identity matches this Thing. The first connection is not independently authenticated; set a fingerprint to avoid the trust-on-first-use risk. | — | no | yes |
| `ownerId` | text | Owner UUID associated with an imported `ownerPsk`; this is not the appliance UUID. | — | no | yes |
| `ownerPsk` | password | Previously provisioned, authorized OwnerPSK: 16 or 32 bytes encoded as hexadecimal. Mutually exclusive with certificate authentication. | — | no | yes |

Changing `host` does not authorize a different appliance. If `deviceId` is omitted, the first authenticated UUID is saved and enforced on subsequent connections. Family-specific Things also check that the authenticated OCF device type matches the selected family.

## Channels

The tables list the channels declared by each concrete Thing. A declared channel is removed when the connected appliance does not support it. A `y` marks a read-only channel. An `n` marks a channel that accepts commands when supported; the appliance's current capabilities and Remote Control setting may limit write access. Consult the channel description for valid commands and limits. Reported units and command options are derived from live appliance capabilities when they are advertised; the dryer course channel is read-only because supported-course capabilities are not advertised.

### Air conditioner (`smartthings:airconditioner`)

| Channel | Item type | Read-only | Description |
|---|---|---|---|
| `power` | Switch | n | Switch the appliance on or off. |
| `remote-control` | Switch | y | Whether remote control is enabled. |
| `current-temperature` | Number:Temperature | y | Measured temperature. |
| `target-temperature` | Number:Temperature | n | Desired temperature, subject to the reported range and increment. |
| `mode` | String | n | Operating mode. |
| `wind-strength` | String | n | Fan-strength setting. |
| `wind-direction` | String | n | Airflow direction or swing setting. |
| `mode-convenient` | String | n | Comfort setting. |
| `power-consumption` | Number:Power | y | Instantaneous electrical power. |
| `cumulative-energy` | Number:Energy | y | Cumulative energy in the unit reported by the appliance. |

### Air purifier (`smartthings:airpurifier`)

| Channel | Item type | Read-only | Description |
|---|---|---|---|
| `power` | Switch | n | Switch the appliance on or off. |
| `remote-control` | Switch | y | Whether remote control is enabled. |
| `airflow` | Number | n | Fan speed. |
| `mode` | String | n | Operating mode. |
| `power-consumption` | Number:Power | y | Instantaneous electrical power. |
| `cumulative-energy` | Number:Energy | y | Cumulative energy in the unit reported by the appliance. |

### Dehumidifier (`smartthings:dehumidifier`)

| Channel | Item type | Read-only | Description |
|---|---|---|---|
| `power` | Switch | n | Switch the appliance on or off. |
| `remote-control` | Switch | y | Whether remote control is enabled. |
| `current-temperature` | Number:Temperature | y | Measured temperature. |
| `target-temperature` | Number:Temperature | n | Desired temperature, subject to the reported range and increment. |
| `mode` | String | n | Operating mode. |
| `power-consumption` | Number:Power | y | Instantaneous electrical power. |
| `cumulative-energy` | Number:Energy | y | Cumulative energy in the unit reported by the appliance. |
| `water-consumption` | Number | y | Raw water-consumption reading; no unit is inferred. |
| `kidslock` | String | y | Child-lock state. |

### Dryer (`smartthings:dryer`)

| Channel | Item type | Read-only | Description |
|---|---|---|---|
| `power` | Switch | n | Switch the appliance on or off. |
| `remote-control` | Switch | y | Whether remote control is enabled. |
| `power-consumption` | Number:Power | y | Instantaneous electrical power. |
| `cumulative-energy` | Number:Energy | y | Cumulative energy in the unit reported by the appliance. |
| `dryer-mode` | String | y | Current dryer course. Known course codes are displayed with readable names. |
| `cycle-control` | String | n | Set the cycle to `Run`, `Pause`, or `Ready`. |
| `delay-end-time` | Number:Time | n | Delay end in seconds, from 0 to 24 hours in 30-minute increments. |
| `dry-level` | String | n | Select a live value from the dryer's `supportedDryLevel` list. |
| `wrinkle-prevent` | String | n | Set wrinkle prevention to `On` or `Off`. |
| `machine-state` | String | y | Current machine state. |
| `job-state` | String | y | Current job state. |
| `remaining-time` | Number:Time | y | Remaining cycle duration in seconds. |
| `progress` | Number:Dimensionless | y | Cycle progress from 0 to 100%. |
| `kidslock` | String | y | Child-lock state. |

### Washer (`smartthings:washer`)

| Channel | Item type | Read-only | Description |
|---|---|---|---|
| `power` | Switch | n | Switch the appliance on or off. |
| `remote-control` | Switch | y | Whether remote control is enabled. |
| `power-consumption` | Number:Power | y | Instantaneous electrical power. |
| `cumulative-energy` | Number:Energy | y | Cumulative energy in the unit reported by the appliance. |
| `machine-state` | String | y | Current machine state. |
| `job-state` | String | y | Current job state. |
| `remaining-time` | Number:Time | y | Remaining cycle duration in seconds. |
| `progress` | Number:Dimensionless | y | Cycle progress from 0 to 100%. |
| `water-consumption` | Number | y | Raw water-consumption reading; no unit is inferred. |
| `kidslock` | String | y | Child-lock state. |

The generic `smartthings:appliance` Thing is intended for diagnostics of unclassified devices; no channel list is provided for it.

## Full Example

This example configures an air conditioner, dryer, and washer. Replace the addresses and omit Items for channels not provided by your appliance. The default secure-port discovery and client identity are used.

### Thing Configuration

`$OPENHAB_CONF/things/smartthings.things`:

```java
Thing smartthings:airconditioner:livingroom "Living Room Air Conditioner" [ host="192.168.1.50", port=0 ]
Thing smartthings:dryer:laundry "Laundry Dryer" [ host="192.168.1.51", port=0 ]
Thing smartthings:washer:utility "Utility Washer" [ host="192.168.1.52", port=0 ]
```

### Item Configuration

`$OPENHAB_CONF/items/smartthings.items`:

```java
Switch LivingRoomAC_Power "Air Conditioner Power" { channel="smartthings:airconditioner:livingroom:power" }
Number:Temperature LivingRoomAC_Current "Current Temperature [%.1f %unit%]" { channel="smartthings:airconditioner:livingroom:current-temperature" }
Number:Temperature LivingRoomAC_Target "Target Temperature [%.1f %unit%]" { channel="smartthings:airconditioner:livingroom:target-temperature" }
String LivingRoomAC_Mode "Mode [%s]" { channel="smartthings:airconditioner:livingroom:mode" }
String LivingRoomAC_Fan "Fan Strength [%s]" { channel="smartthings:airconditioner:livingroom:wind-strength" }
Number:Power LivingRoomAC_PowerConsumption "Power [%.0f %unit%]" { channel="smartthings:airconditioner:livingroom:power-consumption" }

Switch LaundryDryer_Power "Dryer Power" { channel="smartthings:dryer:laundry:power" }
String LaundryDryer_Mode "Dryer Program [%s]" { channel="smartthings:dryer:laundry:dryer-mode" }
String LaundryDryer_Cycle "Dryer Cycle [%s]" { channel="smartthings:dryer:laundry:cycle-control" }
Number:Time LaundryDryer_DelayEnd "Delay End [%d %unit%]" { channel="smartthings:dryer:laundry:delay-end-time" }
String LaundryDryer_DryLevel "Dry Level [%s]" { channel="smartthings:dryer:laundry:dry-level" }
String LaundryDryer_WrinklePrevent "Wrinkle Prevention [%s]" { channel="smartthings:dryer:laundry:wrinkle-prevent" }
String LaundryDryer_MachineState "Dryer State [%s]" { channel="smartthings:dryer:laundry:machine-state" }
String LaundryDryer_JobState "Dryer Job [%s]" { channel="smartthings:dryer:laundry:job-state" }
Number:Time LaundryDryer_Remaining "Dryer Remaining [%d %unit%]" { channel="smartthings:dryer:laundry:remaining-time" }
Number:Dimensionless LaundryDryer_Progress "Dryer Progress [%.0f %%]" { channel="smartthings:dryer:laundry:progress" }

Switch LaundryWasher_Power "Washer Power" { channel="smartthings:washer:utility:power" }
String LaundryWasher_MachineState "Machine State [%s]" { channel="smartthings:washer:utility:machine-state" }
String LaundryWasher_JobState "Job State [%s]" { channel="smartthings:washer:utility:job-state" }
Number:Time LaundryWasher_Remaining "Remaining Time [%d %unit%]" { channel="smartthings:washer:utility:remaining-time" }
Number:Dimensionless LaundryWasher_Progress "Progress [%.0f %%]" { channel="smartthings:washer:utility:progress" }
```

### Sitemap Configuration

`$OPENHAB_CONF/sitemaps/smartthings.sitemap`:

```perl
sitemap smartthings label="Samsung Appliances" {
    Frame label="Living Room Air Conditioner" {
        Switch item=LivingRoomAC_Power
        Setpoint item=LivingRoomAC_Target minValue=16 maxValue=30 step=1
        Text item=LivingRoomAC_Current
        Text item=LivingRoomAC_Mode
        Text item=LivingRoomAC_Fan
        Text item=LivingRoomAC_PowerConsumption
    }
    Frame label="Laundry Dryer" {
        Switch item=LaundryDryer_Power
        Selection item=LaundryDryer_Mode
        Selection item=LaundryDryer_Cycle
        Setpoint item=LaundryDryer_DelayEnd minValue=0 maxValue=86400 step=1800
        Selection item=LaundryDryer_DryLevel
        Selection item=LaundryDryer_WrinklePrevent
        Text item=LaundryDryer_MachineState
        Text item=LaundryDryer_JobState
        Text item=LaundryDryer_Remaining
        Text item=LaundryDryer_Progress
    }
    Frame label="Utility Washer" {
        Switch item=LaundryWasher_Power
        Text item=LaundryWasher_MachineState
        Text item=LaundryWasher_JobState
        Text item=LaundryWasher_Remaining
        Text item=LaundryWasher_Progress
    }
}
```

Use the target-temperature range and increment reported by the appliance when configuring a control; the sitemap values above are examples only. State changes use CoAP Observe when available and fall back to polling at `refreshInterval`. Sending `REFRESH` to a linked Item requests a resource refresh.
