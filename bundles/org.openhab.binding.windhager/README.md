# Windhager Binding

The Windhager binding reads numeric datapoints from the BioWin 2 webserver of a Windhager heating system.

Communication uses the BioWin HTTP API and HTTP Digest authentication.

## Supported Things

| Thing Type ID | Description                                                    |
|---------------|----------------------------------------------------------------|
| `biowin2`     | BioWin 2 webserver connection for a Windhager heating system. |

## Discovery

Things are not discovered automatically and must be configured manually.

## Thing Configuration

| Parameter  | Required | Default | Description                                      |
|------------|----------|---------|--------------------------------------------------|
| `hostname` | Yes      |         | Hostname or IP address of the BioWin webserver.  |
| `port`     | No       | `80`    | TCP port of the BioWin webserver.                |
| `username` | Yes      |         | Username for the BioWin webserver.               |
| `password` | Yes      |         | Password for the BioWin webserver.               |

## Channels

The binding supports numeric, read-only channels. Each channel uses the `biowin-value` channel type and requires an `oid` configuration parameter. The optional `refreshInterval` parameter sets the polling interval in seconds and defaults to `60`.

An OID is the hierarchical identifier used by the BioWin API to address a datapoint. It does not describe the datapoint's meaning or unit by itself. This [Windhager tool](https://github.com/sessl3r/windhager) includes `windhager-getall.py`, which can list datapoints exposed by the recursive lookup on a controller:

```shell
./windhager-getall.py --windhager <heater-ip> --csv > oids.csv
```

Use the `--wuser` and `--wpass` options if the controller uses different credentials. The script only lists datapoints returned by the API lookup; some OIDs may not be discoverable this way. My heater firmware does not include the text files under `api-doc` that would map OIDs to human-readable datapoint names, so some datapoints may need to be identified by observing their values.

The following example OIDs and names were checked against my BioWin system. Values and available datapoints can vary with controller firmware and configuration.

| Channel ID               | Type     | Access    | OID                | Description                                      |
|--------------------------|----------|-----------|--------------------|--------------------------------------------------|
| `fuel-consumption-total` | `Number` | Read-only | `1/60/0/23/103/0`  | Total fuel consumption, in tonnes (`t`).         |
| `flue-gas-temperature`   | `Number` | Read-only | `1/60/0/0/11/0`    | Flue gas temperature, in degrees Celsius (`°C`). |

Writing values and automatic discovery are not supported.

## Full Example

Replace the hostname, username, and password with the values for your installation.

### Thing Configuration

```java
Thing windhager:biowin2:myheater [
hostname="192.0.2.10",
port=8888,
username="your-user",
password="your-password"
] {
Channels:
Type biowin-value : fuel-consumption-total [
oid="1/60/0/23/103/0",
refreshInterval=3600
]
Type biowin-value : flue-gas-temperature [
oid="1/60/0/0/11/0",
refreshInterval=60
]
}
```

### Item Configuration

```java
Number BioWin_FuelConsumption "Fuel consumption [%.2f t]" { channel="windhager:biowin2:myheater:fuel-consumption-total" }
Number BioWin_FlueGasTemperature "Flue gas temperature [%.1f °C]" { channel="windhager:biowin2:myheater:flue-gas-temperature" }
```
