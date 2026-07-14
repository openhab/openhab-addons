# Peblar Binding

This binding integrates the [Peblar EV Charger](https://www.peblar.com) via its local REST API.
It requires firmware **1.6 or newer** and the local API enabled in the charger web interface under **Settings → API**.

## Supported Things

| Thing ID  | Description       |
|-----------|-------------------|
| `charger` | Peblar EV Charger |

## Discovery

Auto-discovery is not supported. The thing must be added manually.

## Thing Configuration

| Parameter         | Type      | Required | Default | Description                                                  |
|-------------------|-----------|----------|---------|--------------------------------------------------------------|
| `hostname`        | `text`    | yes      | —       | Hostname or IP address of the charger on the local network   |
| `apiToken`        | `text`    | yes      | —       | API token set in the charger web interface                   |
| `refreshInterval` | `integer` | no       | 30      | How often to poll the charger in seconds (min: 1, max: 3600) |

## Channels

### Group: `meter` — Electrical Meter

| Channel ID       | Type                       | R/W | Unit | Description                                      |
|------------------|----------------------------|-----|------|--------------------------------------------------|
| `current-p1`     | `Number:ElectricCurrent`   | R   | mA   | Current on phase 1                               |
| `current-p2`     | `Number:ElectricCurrent`   | R   | mA   | Current on phase 2                               |
| `current-p3`     | `Number:ElectricCurrent`   | R   | mA   | Current on phase 3                               |
| `voltage-p1`     | `Number:ElectricPotential` | R   | V    | Voltage on phase 1                               |
| `voltage-p2`     | `Number:ElectricPotential` | R   | V    | Voltage on phase 2                               |
| `voltage-p3`     | `Number:ElectricPotential` | R   | V    | Voltage on phase 3                               |
| `power-p1`       | `Number:Power`             | R   | W    | Active power on phase 1                          |
| `power-p2`       | `Number:Power`             | R   | W    | Active power on phase 2                          |
| `power-p3`       | `Number:Power`             | R   | W    | Active power on phase 3                          |
| `power-total`    | `Number:Power`             | R   | W    | Total active power across all phases             |
| `energy-total`   | `Number:Energy`            | R   | Wh   | Lifetime total energy delivered                  |
| `energy-session` | `Number:Energy`            | R   | Wh   | Energy delivered in the current session          |

### Group: `ev-interface` — EV Interface

| Channel ID                    | Type                     | R/W | Description                                                                          |
|-------------------------------|--------------------------|-----|--------------------------------------------------------------------------------------|
| `cp-state`                    | `String`                 | R   | IEC 61851 Control Pilot state (`State A`–`State F`)                                  |
| `lock-state`                  | `Switch`                 | R   | Cable lock state; ON = locked                                                        |
| `charge-current-limit`        | `Number:ElectricCurrent` | R/W | Configured charge current limit in mA. Values 0–5999 mA pause charging per IEC 61851 |
| `charge-current-limit-source` | `String`                 | R   | Source currently limiting the charge current (e.g. `Smart charging`, `User`)         |
| `charge-current-limit-actual` | `Number:ElectricCurrent` | R   | Actual applied charge current limit in mA after all constraints                      |
| `force1-phase`                | `Switch`                 | R/W | ON = force single-phase charging                                                     |

**IEC 61851 Control Pilot State values:**

| Value     | Meaning                         |
|-----------|---------------------------------|
| `State A` | No vehicle connected            |
| `State B` | Vehicle connected, not charging |
| `State C` | Charging                        |
| `State D` | Charging with ventilation       |
| `State E` | Error                           |
| `State F` | EVSE not available              |

### Group: `system` — System Information

Polled from `GET /api/wlac/v1/system`.

| Channel ID                 | Type           | R/W | Description                          |
|--------------------------00|----------------|-----|--------------------------------------|
| `product-pn`               | `String`       | R   | Product part number                  |
| `product-sn`               | `String`       | R   | Product serial number                |
| `firmware-version`         | `String`       | R   | Currently installed firmware version |
| `wlan-signal-strength`     | `Number:Power` | R   | Wi-Fi signal strength in dBm         |
| `cellular-signal-strength` | `Number:Power` | R   | Cellular signal strength in dBm      |
| `uptime`                   | `Number:Time`  | R   | Device uptime in seconds             |
| `phase-count`              | `Number`       | R   | Number of connected phases (1 or 3)  |

## Full Configuration Example

### `things/peblar.things`

```java
Thing peblar:charger:mypeblar "Peblar EV Charger" [
    hostname        = "192.168.1.100",
    apiToken        = "your-api-token-here",
    refreshInterval = 30
]
```

### `items/peblar.items`

```java
// ── Meter ──────────────────────────────────────────────────────────────────

Number:ElectricCurrent  Peblar_Current_L1       "Current L1 [%.1f A]"     <energy>  { channel="peblar:charger:mypeblar:meter#current-p1" }
Number:ElectricCurrent  Peblar_Current_L2       "Current L2 [%.1f A]"     <energy>  { channel="peblar:charger:mypeblar:meter#current-p2" }
Number:ElectricCurrent  Peblar_Current_L3       "Current L3 [%.1f A]"     <energy>  { channel="peblar:charger:mypeblar:meter#current-p3" }

Number:ElectricPotential Peblar_Voltage_L1      "Voltage L1 [%.0f V]"     <energy>  { channel="peblar:charger:mypeblar:meter#voltage-p1" }
Number:ElectricPotential Peblar_Voltage_L2      "Voltage L2 [%.0f V]"     <energy>  { channel="peblar:charger:mypeblar:meter#voltage-p2" }
Number:ElectricPotential Peblar_Voltage_L3      "Voltage L3 [%.0f V]"     <energy>  { channel="peblar:charger:mypeblar:meter#voltage-p3" }

Number:Power            Peblar_Power_L1         "Power L1 [%.0f W]"       <energy>  { channel="peblar:charger:mypeblar:meter#power-p1" }
Number:Power            Peblar_Power_L2         "Power L2 [%.0f W]"       <energy>  { channel="peblar:charger:mypeblar:meter#power-p2" }
Number:Power            Peblar_Power_L3         "Power L3 [%.0f W]"       <energy>  { channel="peblar:charger:mypeblar:meter#power-p3" }
Number:Power            Peblar_Power_Total      "Total Power [%.0f W]"    <energy>  { channel="peblar:charger:mypeblar:meter#powerTotal" }

Number:Energy           Peblar_Energy_Total     "Total Energy [%.2f kWh]" <energy>  { channel="peblar:charger:mypeblar:meter#energy-total" }
Number:Energy           Peblar_Energy_Session   "Session Energy [%.2f kWh]" <energy> { channel="peblar:charger:mypeblar:meter#energy-session" }

// ── EV Interface ───────────────────────────────────────────────────────────

String                  Peblar_CP_State         "CP State [%s]"           <energy>  { channel="peblar:charger:mypeblar:ev-interface#cp-state" }
Switch                  Peblar_Lock_State       "Cable Locked [%s]"       <lock>    { channel="peblar:charger:mypeblar:ev-interface#lock-state" }
Number:ElectricCurrent  Peblar_Current_Limit    "Charge Limit [%.0f A]"   <energy>  { channel="peblar:charger:mypeblar:ev-interface#charge-current-limit" }
String                  Peblar_Limit_Source     "Limit Source [%s]"       <energy>  { channel="peblar:charger:mypeblar:ev-interface#charge-current-limit-source" }
Number:ElectricCurrent  Peblar_Current_Actual   "Actual Limit [%.0f A]"   <energy>  { channel="peblar:charger:mypeblar:ev-interface#charge-current-limit-actual" }
Switch                  Peblar_Force_1Phase     "Force Single Phase [%s]" <energy>  { channel="peblar:charger:mypeblar:ev-interface#force1Phase" }

// ── System ─────────────────────────────────────────────────────────────────

String                  Peblar_Product_PN       "Part Number [%s]"                  { channel="peblar:charger:mypeblar:system#product-pn" }
String                  Peblar_Product_SN       "Serial Number [%s]"                { channel="peblar:charger:mypeblar:system#product-sn" }
String                  Peblar_Firmware         "Firmware [%s]"                     { channel="peblar:charger:mypeblar:system#firmware-version" }
Number:Power            Peblar_WLAN_RSSI        "WLAN Signal [%.0f dBm]"            { channel="peblar:charger:mypeblar:system#wlan-signal-strength" }
Number:Power            Peblar_Cell_RSSI        "Cellular Signal [%.0f dBm]"        { channel="peblar:charger:mypeblar:system#cellular-signal-strength" }
Number:Time             Peblar_Uptime           "Uptime [%d s]"                     { channel="peblar:charger:mypeblar:system#uptime" }
Number:Dimensionless    Peblar_Phase_Count      "Phases [%d]"                       { channel="peblar:charger:mypeblar:system#phase-count" }
```

### `sitemaps/peblar.sitemap`

```java
sitemap peblar label="Peblar EV Charger" {
    Frame label="Status" {
        Default item=Peblar_CP_State
        Default item=Peblar_Lock_State
        Default item=Peblar_Power_Total
        Default item=Peblar_Energy_Session
        Default item=Peblar_Energy_Total
    }
    Frame label="Control" {
        Setpoint item=Peblar_Current_Limit minValue=6 maxValue=16 step=1
        Default  item=Peblar_Current_Actual
        Default  item=Peblar_Limit_Source
        Switch   item=Peblar_Force_1Phase
    }
    Frame label="Phases" {
        Default item=Peblar_Current_L1   label="Current L1"
        Default item=Peblar_Current_L2   label="Current L2"
        Default item=Peblar_Current_L3   label="Current L3"
        Default item=Peblar_Voltage_L1   label="Voltage L1"
        Default item=Peblar_Voltage_L2   label="Voltage L2"
        Default item=Peblar_Voltage_L3   label="Voltage L3"
    }
    Frame label="System" {
        Default item=Peblar_Firmware     label="Firmware"
        Default item=Peblar_WLAN_RSSI    label="Wi-Fi Signal"
        Default item=Peblar_Uptime       label="Uptime"
        Default item=Peblar_Phase_Count  label="Phases"
    }
}
```

### `rules/peblar.rules` — Example: Limit charging when solar power is low

```java
rule "Peblar - adjust charge limit based on solar"
when
    Item Solar_Power changed
then
    val solarWatts = (Solar_Power.state as Number).intValue
    // Convert available watts to milliamps on 230V single phase
    val limitMa = Math.max(6000, Math.min(32000, (solarWatts / 230 * 1000).intValue))
    Peblar_Current_Limit.sendCommand(limitMa + " mA")
end
```

## Enabling the Local API on the Charger

1. Open the charger web interface at `http://<charger-ip>`
1. Navigate to **Settings → API**
1. Enable **Local REST API**
1. Copy the generated **API Token** into the thing configuration
