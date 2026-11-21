# Tibber Pulse Local Binding

This Binding reads the live data of a Tibber Pulse directly from the Tibber bridge on your local network.
It needs no Tibber account and no Internet access.
The bridge sends a new reading about every 3 seconds, and the Binding publishes it as net power, import and export energy and, if your meter reports them, voltage, current, power per phase and grid frequency.

## Related Bindings

- Use the [`tibber`](https://www.openhab.org/addons/bindings/tibber/) Binding if you need prices, consumption history, or the cloud-based Pulse live data.
- Use the [`smartmeter`](https://www.openhab.org/addons/bindings/smartmeter/) Binding if you read your meter directly through a serial or USB optical head, without the bridge.

## Supported Things

| Thing Type ID | Type   | Label               | Description                                                      |
|---------------|--------|---------------------|------------------------------------------------------------------|
| `bridge`      | Bridge | Tibber Pulse Bridge | Connection to one Tibber bridge.                                 |
| `meter`       | Thing  | Tibber Pulse Meter  | One electricity meter, identified by the device id of its Pulse. |

Add the `bridge` Thing first, then the `meter` Thing below it.
Both are offered in the Inbox, see [Discovery](#discovery).

## Prerequisites

- The bridge must be reachable from openHAB in your local network.
  Give it a fixed IP address, for example with a DHCP reservation in your router.
- The password is the code on the QR label of the bridge.
- The web interface of the bridge must stay enabled permanently, see the next section.
- Only Pulse models that read the meter in the SML format are supported, which depends on the model you own.
  Pulse models for other meter types, such as impulse counters, are not supported.

## Keeping the Bridge's Web Interface Enabled

The bridge delivers its readings through its web server, so a bridge whose web interface is switched off cannot be used.
The steps below follow the guide of the Home Assistant project `marq24/ha-tibber-pulse-local`, and they can differ by hardware revision and firmware.

1. Put the bridge into access point mode by unplugging it and plugging it in again, as described in the guide.
   The LED turns green.
1. Connect a phone or laptop to the Wi-Fi network `Tibber Bridge`.
   The Wi-Fi password is the code on the label of the bridge.
1. Open the web interface of the bridge in a browser and log in with the same code.
1. Set the parameter `webserver_force_enable` to `true`, using the console or the parameters page depending on your firmware, and store the parameters.
1. Restart the bridge so that it returns to your home network.

See the [preparation guide](https://github.com/marq24/ha-tibber-pulse-local/blob/main/preparation.md) for details, screenshots and troubleshooting.

## Discovery

### Bridge Discovery

The bridge appears in the Inbox by itself while its web interface is enabled.
The entry is named "Tibber Pulse Bridge (`bridge01234567`)" and contains the IP address of the bridge.

### Meter Discovery

When the `bridge` Thing is online, the meters that it receives appear in the Inbox as "Tibber Pulse `<deviceId>`".
Approve the entry to create the `meter` Thing.
A manual scan offers the meters again, for example after you removed a `meter` Thing.

## Thing Configuration

### `bridge` Thing Configuration

| Name       | Type | Description                                                                  | Default | Required | Advanced |
|------------|------|------------------------------------------------------------------------------|---------|----------|----------|
| `hostname` | text | Host name or IP address of the bridge, without `http://` and without path.   | N/A     | yes      | no       |
| `password` | text | Password of the bridge web interface, printed as a code on the bridge label. | N/A     | yes      | no       |

Do not configure two `bridge` Things for the same bridge.

### `meter` Thing Configuration

| Name           | Type    | Description                                                                                              | Default | Required | Advanced |
|----------------|---------|----------------------------------------------------------------------------------------------------------|---------|----------|----------|
| `deviceId`     | text    | Device id of the Pulse, 16 hexadecimal digits. The Inbox entry of the meter contains it.                 | N/A     | yes      | no       |
| `staleTimeout` | integer | Seconds without a valid reading after which the meter goes offline and its live values become undefined. | 60      | no       | yes      |

The `staleTimeout` must be at least 10.

## Channels

All Channels of the `meter` Thing are read-only.
The per-phase Channels and `frequency` are advanced Channels.
A quantity that your meter does not report stays `UNDEF`.

| Channel ID      | Type                     | Unit | Description                                                                        | Advanced |
|-----------------|--------------------------|------|------------------------------------------------------------------------------------|----------|
| `power`         | Number:Power             | W    | Net active power, positive when drawn from the grid and negative when fed into it. | no       |
| `energy-import` | Number:Energy            | kWh  | Total energy drawn from the grid.                                                  | no       |
| `energy-export` | Number:Energy            | kWh  | Total energy fed into the grid.                                                    | no       |
| `power-l1`      | Number:Power             | W    | Active power on phase L1.                                                          | yes      |
| `power-l2`      | Number:Power             | W    | Active power on phase L2.                                                          | yes      |
| `power-l3`      | Number:Power             | W    | Active power on phase L3.                                                          | yes      |
| `voltage-l1`    | Number:ElectricPotential | V    | Voltage on phase L1.                                                               | yes      |
| `voltage-l2`    | Number:ElectricPotential | V    | Voltage on phase L2.                                                               | yes      |
| `voltage-l3`    | Number:ElectricPotential | V    | Voltage on phase L3.                                                               | yes      |
| `current-l1`    | Number:ElectricCurrent   | A    | Current on phase L1.                                                               | yes      |
| `current-l2`    | Number:ElectricCurrent   | A    | Current on phase L2.                                                               | yes      |
| `current-l3`    | Number:ElectricCurrent   | A    | Current on phase L3.                                                               | yes      |
| `frequency`     | Number:Frequency         | Hz   | Grid frequency.                                                                    | yes      |
| `last-update`   | DateTime                 | -    | Time of the latest reading received for this meter.                                | no       |

The energy counters and `last-update` keep their last value when the connection is lost, all other Channels become `UNDEF`.

## Thing Statuses

### `bridge` Thing Statuses

| Status  | Detail              | Cause                                                                                |
|---------|---------------------|--------------------------------------------------------------------------------------|
| UNKNOWN | NONE                | Waiting for the first data from the bridge                                           |
| ONLINE  | NONE                | The bridge delivers data                                                             |
| OFFLINE | CONFIGURATION_ERROR | The host name is invalid or the password is empty or was rejected by the bridge      |
| OFFLINE | COMMUNICATION_ERROR | The bridge cannot be reached, does not deliver data, or another client took the data |

The Binding reconnects by itself after a communication error.
After a rejected password it waits until you change the configuration of the `bridge` Thing.
A `bridge` Thing that was approved without a password stays UNINITIALIZED until you enter the password.

### `meter` Thing Statuses

| Status  | Detail              | Cause                                 |
|---------|---------------------|---------------------------------------|
| UNKNOWN | NONE                | No reading has arrived yet            |
| ONLINE  | NONE                | The meter delivers readings           |
| OFFLINE | COMMUNICATION_ERROR | No reading for `staleTimeout` seconds |
| OFFLINE | BRIDGE_OFFLINE      | The `bridge` Thing is not online      |
| OFFLINE | CONFIGURATION_ERROR | Invalid `deviceId` or `staleTimeout`  |

## Limits and Notes

- Only Pulse models that read the meter in the SML format are supported, which depends on the model you own.
  Models for other meter types, such as impulse counters, are not supported.
- The bridge serves only one client at a time.
  If you open the live page of the bridge, a browser tab or a Home Assistant integration while the Binding is connected, one of them loses the data.
- The communication with the bridge is not encrypted, so the password can be read by anyone who can listen on your local network.
- The Binding was tested with a Tibber Pulse IR on a bridge with firmware ESP 1811-97099eed / EFR 835-942ac28e.
  Other firmware versions and meters may behave differently.
- Meters that report no power leave the power Channels `UNDEF`.
- Bridges that announce themselves differently from the tested firmware are not discovered, and the API token of newer firmware is not supported.

## Full Example

### `tibberpulselocal.things` Example

```java
Bridge tibberpulselocal:bridge:home "Tibber Pulse Bridge" [ hostname="192.0.2.20", password="<QR code>" ] {
    Thing meter main "Electricity Meter" [ deviceId="0123456789abcdef", staleTimeout=60 ]
}
```

Replace the host name and the password with the values of your bridge.
The password is the code on its label.

### `tibberpulselocal.items` Example

```java
Number:Power             Grid_Power         "Grid power [%.0f %unit%]"      {channel="tibberpulselocal:meter:home:main:power"}
Number:Energy            Grid_Energy_Import "Energy imported [%.3f %unit%]" {channel="tibberpulselocal:meter:home:main:energy-import"}
Number:Energy            Grid_Energy_Export "Energy exported [%.3f %unit%]" {channel="tibberpulselocal:meter:home:main:energy-export"}
Number:Power             Grid_Power_L1      "Power L1 [%.0f %unit%]"        {channel="tibberpulselocal:meter:home:main:power-l1"}
Number:ElectricPotential Grid_Voltage_L1    "Voltage L1 [%.1f %unit%]"      {channel="tibberpulselocal:meter:home:main:voltage-l1"}
Number:ElectricCurrent   Grid_Current_L1    "Current L1 [%.2f %unit%]"      {channel="tibberpulselocal:meter:home:main:current-l1"}
Number:Frequency         Grid_Frequency     "Frequency [%.1f %unit%]"       {channel="tibberpulselocal:meter:home:main:frequency"}
DateTime                 Grid_Last_Update   "Last update [%1$tF %1$tT]"     {channel="tibberpulselocal:meter:home:main:last-update"}
```

## Acknowledgements

This Binding is an independent implementation.
It is not affiliated with or supported by Tibber, and it contains no code from the projects below or from the SmlLib library that they use.
It builds on the experience that these projects documented about the Tibber bridge:

- [`marq24/ha-tibber-pulse-local`](https://github.com/marq24/ha-tibber-pulse-local) (Apache-2.0): how to read the data stream of the bridge and how to interpret its readings.
- [`jimmyw/ha-tibber-pulse-local`](https://github.com/jimmyw/ha-tibber-pulse-local) (MIT): how to reconnect after a lost connection, and how the bridge announces itself on the network.
- [`ProfDrYoMan/tibber_pulse_local`](https://github.com/ProfDrYoMan/tibber_pulse_local) (MIT): the collection of knowledge about the bridge.
