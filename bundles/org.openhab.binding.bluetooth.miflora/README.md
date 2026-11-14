# Mi Flora

This extension reads the Xiaomi Mi Flora plant sensor (model HHCCJCY01), also sold as Flower Care and under other labels.
It decodes the readings the sensor broadcasts and never connects to it, so it needs no app, account or cloud service.

## Supported Things

| Thing Type ID | Description                                       |
|---------------|---------------------------------------------------|
| miflora       | Mi Flora plant sensor (HHCCJCY01, product 0x0098) |

The sensor broadcasts its readings in unencrypted Xiaomi "MiBeacon" service data (UUID `0xFE95`).
Only the BlueZ bridge passes service data on, so the thing works with a `bluez` bridge, or a `roaming` bridge over BlueZ adapters.

## Discovery

Sensors are discovered by the name they advertise, `Flower care`.

## Thing Configuration

| Parameter   | Type    | Default | Required | Description                                                    |
|-------------|---------|---------|----------|----------------------------------------------------------------|
| address     | text    |         | Yes      | Bluetooth address of the sensor, in `XX:XX:XX:XX:XX:XX` format |
| dataTimeout | integer | 10      | No       | Minutes without a reading before the thing goes offline        |

## Channels

| Channel      | Type                        | Description                                        |
|--------------|-----------------------------|----------------------------------------------------|
| temperature  | Number:Temperature          | Ambient temperature in _°C_                        |
| illuminance  | Number:Illuminance          | Ambient illuminance in _lx_                        |
| moisture     | Number:Dimensionless        | Soil moisture in _%_                               |
| conductivity | Number:ElectricConductivity | Soil conductivity in _µS/cm_ (indicates fertility) |
| rssi         | Number:Power                | Received signal strength in _dBm_                  |

The sensor broadcasts one reading at a time and cycles through all four, so each channel updates about every 40 seconds.
The battery level is not broadcast; reading it would need a connection, which this extension does not make.

## Full Example

miflora.things:

```java
Bridge bluetooth:bluez:hci0 [ address="00:11:22:33:44:55" ] {
    Thing miflora basil "Basil" [ address="C4:7C:8D:12:34:56" ]
}
```

miflora.items:

```java
Number:Temperature          Basil_Temperature  "Temperature [%.1f %unit%]"  { channel="bluetooth:miflora:hci0:basil:temperature" }
Number:Illuminance          Basil_Light        "Light [%.0f %unit%]"        { channel="bluetooth:miflora:hci0:basil:illuminance" }
Number:Dimensionless        Basil_Moisture     "Moisture [%.0f %unit%]"     { channel="bluetooth:miflora:hci0:basil:moisture", unit="%" }
Number:ElectricConductivity Basil_Conductivity "Conductivity [%.0f %unit%]" { channel="bluetooth:miflora:hci0:basil:conductivity", unit="µS/cm" }
```
