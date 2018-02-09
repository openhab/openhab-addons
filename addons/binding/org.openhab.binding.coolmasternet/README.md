# CoolMasterNet Binding

The CoolMasterNet binding is used to control [CoolMasterNet HVAC bridge devices](https://coolautomation.com/products/coolmasternet/), using the "ASCII I/F" plaintext TCP control protocol.

## Discovery

The CoolMasterNet protocol does not support automatic discovery.

## Thing Configuration

- `controller` is a openHAB "bridge", and represents a single CoolMasterNet device. A single controller supports one or more HVAC units.
- `hvac` is an HVAC device connected to a controller. Each `hvac` thing is identified by a CoolMasterNet UID (refer to CoolMasterNet controller documentation).

Example demo.things configuration for two HVAC devices connected to a CoolMasterNet device found at IP 192.168.0.100:

```perl
Bridge coolmasternet:controller:main [ host="192.168.0.100" ] {
  Thing hvac a [ uid="L1.100" ]
  Thing hvac b [ uid="L1.101" ]
}
```

## Channels

| Channel      | Item Type | Description                                                                                                                                      |
|--------------|-----------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| on           | Switch    | Turn HVAC unit on and off.                                                                                                                       |
| mode         | String    | HVAC Mode. "Auto", "Cool", "Heat", "Dry" or "Fan Only". Unit may not support all modes.                                                          |
| fan          | String    | Fan Mode. "Auto", "Low", "Medium", "High" or "Top". Unit may not support all speeds.                                                             |
| set_temp     | Number    | Temperature target setpoint.                                                                                                                     |
| current_temp | Number    | Current temperature at HVAC unit.                                                                                                                |
| louvre       | String    | Louvre angle. "No Control", "Auto Swing", "Horizontal", "30 degrees", "45 degrees", "60 degrees" or "Vertical". Unit may not support all angles. |

## Item Configuration

```java
Switch ACOn "Lounge AC ON/OFF" { channel="coolmasternet:hvac:main:a:on"}
String ACMode "Lounge AC Mode" { channel="coolmasternet:hvac:main:a:mode" }
Number ACTemp "Lounge Temp" { channel="coolmasternet:hvac:main:a:current_temp" }
Number ACSet "Lounge AC Set" { channel="coolmasternet:hvac:main:a:set_temp" }
String ACFan "Lounge AC Fan" { channel="coolmasternet:hvac:main:a:fan_speed" }
String ACLouvre "Lounge AC Louvre" { channel="coolmasternet:hvac:main:a:louvre_angle" }
```
