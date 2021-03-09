# EcoTouch Binding

The openHAB EcoTouch binding allows interaction with a [Waterkotte](https://www.waterkotte.de/) heat pump.

## Supported Things

| Thing type      | Description                                      |
|-----------------|--------------------------------------------------|
| geo             | Waterkotte EcoTouch Geo + EcoVent                |
| air             | Waterkotte EcoTouch Air + EcoVent                |

This binding was tested with a Waterkotte DS 5027 Ai DS 5010.5Ai geothermal heat pump.

## Discovery

Discovery is not supported. You need to provide the IP address of the display unit of the heatpump.

## Binding Configuration

No Binding configuration required.

## Thing Configuration

Things can be fully configured via the UI. The following information is useful when configuring things via thing configuration files.

```
Thing ecotouch:geo:9a0ec0bbbd "Waterkotte Heatpump" @ "basement" [ ip="192.168.1.100", username="admin", password="wtkadmin", refresh=120 ]
```

| Property                        | Type    | Default | Required | Description |
|---------------------------------|---------|---------|----------|-------------|
| ip                              | String  |         | yes      | IP address or hostname of the display unit of the heat pump |
| username                        | String  |         | yes      | since software version 1.6.xx of the display unit: "waterkotte"; previously "admin". |
| password                        | String  |         | yes      | since software version 1.6.xx of the display unit: "waterkotte"; previously "wtkadmin". |
| refresh                         | Integer |      60 | no       | time in s after which all channels will be requested again from the heat pump |

## Channels

All available channels can be seen inside PaperUI. Here is a small extract:

| Channel ID          | Type               | Read-Only | Description |
|---------------------|--------------------|-----------|-------------|
| temperature_outside | Number:Temperature | yes       | The current outside temperature |
| enable_cooling      | Switch             | no        | Enable Cooling |
| state               | Number             | yes       | A Bitfield which encodes all valves, pumps and compressors |
| state_compressor1   | Switch             | yes       | The current state of compressor 1 |

If the Ecovent Unit is attached to the heat pump, the following additional channels are available:

| Channel ID          | Type               | Read-Only | Description |
|---------------------|--------------------|-----------|-------------|
| ecovent_temp_exhaust_air | Number:Temperature | yes  | EcoVent Temperature Exhaust Air |
| ecovent_temp_exit_air    | Number:Temperature | yes  | EcoVent Temperature Exit Air |
| ecovent_temp_outdoor_air | Number:Temperature | yes  | EcoVent Temperature Outdoor Air |
| ecovent_temp_supply_air  | Number:Temperature | yes  | EcoVent Temperature Supply Air |
| ecovent_CO2_value        | Number:Dimensionless | yes | EcoVent CO2 |
| ecovent_moisture_value   | Number:Dimensionless | yes | EcoVent Air Moisture |
| ecovent_output_y1        | Number:Dimensionless | yes | EcoVent Fan |
| ecovent_mode             | Number:Dimensionless | no | EcoVent Mode (0..5: Day, Night, Timer, Party, Vacation, Bypass) |

The air heatpump has the following additional channels:

| Channel ID          | Type               | Read-Only |
|---------------------|--------------------|-----------|
| temperature_surrounding | Number:Temperature | yes | 
| temperature_suction_air | Number:Temperature | yes | 
| temperature_sump | Number:Temperature | yes | 

