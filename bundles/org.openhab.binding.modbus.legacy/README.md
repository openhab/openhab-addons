# Modbus Binding (All Devices)

This add-on installs the Modbus core and all Modbus device add-ons that existed before the Modbus binding was split into individual add-ons.
It contains no functionality of its own.
Its ID is `modbus`, so installations that already have `modbus` in their configuration keep working after an update.

New devices are not added to this add-on.
It is deprecated for new installations and will be removed in a future major release.

## Lean Installation

Install only what you need instead:

- **Modbus Core Binding** for generic Modbus TCP and serial devices.
- The add-on for your device, which installs the Modbus core automatically.

| Add-on                               | Device                                  |
|--------------------------------------|-----------------------------------------|
| Modbus Anker SOLIX Binding           | Anker SOLIX devices                     |
| Modbus E3DC Binding                  | E3/DC Home Power Plants                 |
| Modbus Fox ESS Binding               | FoxESS inverters                        |
| Modbus Helios easyControls Binding   | Helios easyControls ventilation devices |
| Modbus Kermi Binding                 | Kermi heat pumps                        |
| Modbus Lambda Binding                | Lambda heat pumps                       |
| Modbus Saia Burgess Controls Binding | Saia Burgess Controls energy meters     |
| Modbus Stiebel Eltron Binding        | Stiebel Eltron ISG heat pumps           |
| Modbus Studer Binding                | Studer inverters and charge controllers |
| Modbus Sungrow Binding               | Sungrow inverters and iHomeManager      |
| Modbus SunSpec Binding               | SunSpec inverters and meters            |

Things, channels and item links are not affected by the switch, because the thing type IDs and the binding ID `modbus` stay the same.
