# Windhagerbiowin Binding

The Windhagerbiowin binding reads numeric datapoints from the BioWin webserver of a Windhager heating system.

Communication uses the BioWin HTTP API and HTTP Digest authentication.

## Supported Things

| Thing Type ID | Description |
|---------------|-------------|
| `biowin` | BioWin webserver connection for a Windhager heating system. |

## Discovery

Things are not discovered automatically and must be configured manually.

## Thing Configuration

| Parameter | Required | Default | Description |
|-----------|----------|---------|-------------|
| `hostname` | Yes | | Hostname or IP address of the BioWin webserver. |
| `port` | No | `80` | TCP port of the BioWin webserver. |
| `username` | Yes | | Username for the BioWin webserver. |
| `password` | Yes | | Password for the BioWin webserver. |

## Channels

The binding supports numeric, read-only channels.

Each channel uses the `biowin-value` channel type and requires an `oid` configuration parameter.

The optional `refreshInterval` parameter sets the polling interval in seconds and defaults to `60`.

The OIDs below are from a tested BioWin installation.

| Channel ID | Type | Access | OID |
|------------|------|--------|-----|
| `PelletTotal` | `Number` | Read-only | `1/60/0/23/103/0` |
| `UnknownTemp` | `Number` | Read-only | `1/60/0/0/11/0` |

Writing values and automatic discovery are not supported.

## Full Example

Replace the hostname, username, and password with the values for your installation.

### Thing Configuration

```java
Thing windhagerbiowin:biowin:myheater [
hostname="192.0.2.10",
port=8888,
username="your-user",
password="your-password"
] {
Channels:
Type biowin-value : PelletTotal [
oid="1/60/0/23/103/0",
refreshInterval=3600
]
Type biowin-value : UnknownTemp [
oid="1/60/0/0/11/0",
refreshInterval=60
]
}
```

### Item Configuration

```java
Number BioWin_Pellets "Pellets Total [%.1f]" { channel="windhagerbiowin:biowin:myheater:PelletTotal" }
Number BioWin_Temp "Temp [%.1f]" { channel="windhagerbiowin:biowin:myheater:UnknownTemp" }
```
