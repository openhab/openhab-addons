# CoolMasterNet Binding

The CoolMasterNet binding is used to control [CoolMasterNet HVAC bridge devices](https://coolautomation.com/products/coolmasternet/), using the "ASCII I/F" plaintext TCP control protocol.

## Discovery

The CoolMasterNet protocol does not support automatic discovery.

## Thing Configuration

*  `controller` is a openHAB "bridge", and represents a single CoolMasterNet device. A single controller supports one or more HVAC units.

*  `hvac` is an HVAC device connected to a controller. Each `hvac` thing is identified by a CoolMasterNet UID (refer to CoolMasterNet controller documentation).

Example demo.things configuration for two HVAC devices connected to a CoolMasterNet device found at IP 192.168.0.100:

```
Bridge coolmasternet:controller:main [ host="192.168.0.100" ] {
  Thing hvac a [ uid="L1.100" ]
  Thing hvac b [ uid="L1.101" ]
}
```

## Channels

<table>
	<tr><td><b>Channel</b></td><td><b>Item Type</b></td><td><b>Description</b></td></tr>
	<tr>
		<td>on</td>
		<td>Switch</td>
		<td>Turn HVAC unit on and off.</td>
	</tr>
	<tr>
		<td>mode</td>
		<td>String</td>
		<td>HVAC Mode. "Auto", "Cool", "Heat", "Dry" or "Fan Only". Unit may not support all modes.</td>
	</tr>
	<tr>
		<td>fan</td>
		<td>String</td>
		<td>Fan Mode. "Auto", "Low", "Medium", "High" or "Top". Unit may not support all speeds.</td>
	</tr>
	<tr>
		<td>set_temp</td>
		<td>Number</td>
		<td>Temperature target setpoint.</td>
	</tr>
	<tr>
		<td>current_temp</td>
		<td>Number</td>
		<td>Current temperature at HVAC unit.</td>
	</tr>
	<tr>
		<td>louvre</td>
		<td>String</td>
		<td>Louvre angle. "No Control", "Auto Swing", "Horizontal", "30 degrees", "45 degrees", "60 degrees" or "Vertical". Unit may not support all angles.</td>
	</tr>
</table>

## Item Configuration

```
Switch ACOn "Lounge AC ON/OFF" { channel="coolmasternet:hvac:main:a:on"}
String ACMode "Lounge AC Mode" { channel="coolmasternet:hvac:main:a:mode" }
Number ACTemp "Lounge Temp" { channel="coolmasternet:hvac:main:a:current_temp" }
Number ACSet "Lounge AC Set" { channel="coolmasternet:hvac:main:a:set_temp" }
String ACFan "Lounge AC Fan" { channel="coolmasternet:hvac:main:a:fan_speed" }
String ACLouvre "Lounge AC Louvre" { channel="coolmasternet:hvac:main:a:louvre_angle" }
```
