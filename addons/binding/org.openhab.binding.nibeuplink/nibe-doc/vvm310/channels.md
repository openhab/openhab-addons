| Channel Type ID | Item Type    | Min          | Max          | Writable | Description                         | Allowed Values (write access)  |
|-----------------|--------------|--------------|--------------|----------|-------------------------------------|--------------------------------|
| general#44270 | Number:Temperature | -32767 | 32767 | No | Calc. Cooling Supply S1 |  |
| general#40121 | Number:Temperature | -32767 | 32767 | No | BT63 Add Supply Temp |  |
| general#43437 | Number:Dimensionless | 0 | 255 | No | Supply Pump Speed EP14 |  |
| general#44302 | Number:Energy | 0 | 9999999 | No | Heat Meter - Cooling Cpr EP14 |  |
| general#47011 | Number | -10 | 10 | Yes | Heat Offset S1 | values between -10 and 10 |
| general#47394 | Switch | --- | --- | Yes | Use room sensor S1 | 0=off, 1=on |
| general#47402 | Number | 0 | 60 | Yes | Room sensor factor S1 | Values between 0 and 6 |
| general#48793 | Number | 0 | 60 | Yes | Room sensor cool factor S1 | Values between 0 and 6 |
| compressor#44362 | Number:Temperature | -32767 | 32767 | No | EB101-EP14-BT28 Outdoor Temp |  |
| compressor#44396 | Number:Dimensionless | 0 | 255 | No | EB101 Speed charge pump |  |
| compressor#44703 | Number | --- | --- | No | EB101-EP14 Defrosting Outdoor Unit | 0=No, 1=Active, 2=Passive |
| compressor#44073 | Number:Time | 0 | 9999999 | No | EB101-EP14 Tot. HW op.time compr |  |
| compressor#40737 | Number:Time | 0 | 9999999 | No | EB101-EP14 Tot. Cooling op.time compr |  |
| compressor#44071 | Number:Time | 0 | 9999999 | No | EB101-EP14 Tot. op.time compr |  |
| compressor#44069 | Number | 0 | 9999999 | No | EB101-EP14 Compressor starts |  |
| compressor#44061 | Number:Temperature | -32767 | 32767 | No | EB101-EP14-BT17 Suction |  |
| compressor#44060 | Number:Temperature | -32767 | 32767 | No | EB101-EP14-BT15 Liquid Line |  |
| compressor#44059 | Number:Temperature | -32767 | 32767 | No | EB101-EP14-BT14 Hot Gas Temp |  |
| compressor#44058 | Number:Temperature | -32767 | 32767 | No | EB101-EP14-BT12 Condensor Out |  |
| compressor#44055 | Number:Temperature | -32767 | 32767 | No | EB101-EP14-BT3 Return Temp. |  |
| compressor#44363 | Number:Temperature | -32767 | 32767 | No | EB101-EP14-BT16 Evaporator |  |
| compressor#44699 | Number:Pressure | -32767 | 32767 | No | EB101-EP14-BP4 Pressure Sensor |  |
| compressor#40782 | Number:Frequency | 0 | 255 | No | EB101 Cpr Frequency Desired F2040 |  |
| compressor#44701 | Number:Frequency | -32767 | 32767 | No | EB101-EP14 Actual Cpr Frequency Outdoor Unit |  |
| compressor#44702 | Number | --- | --- | No | EB101-EP14 Protection Status Register Outdoor Unit |  |
| compressor#44700 | Number:Pressure | -32767 | 32767 | No | EB101-EP14 Low Pressure Sensor Outdoor Unit |  |
| compressor#44457 | Number | 0 | 255 | No | EB101-EP14 Compressor State |  |
| airsupply#40025 | Number:Temperature | -32767 | 32767 | No | BT20 Exhaust air temp. 1 |  |
| airsupply#40026 | Number:Temperature | -32767 | 32767 | No | BT21 Vented air temp. 1 |  |
| airsupply#40075 | Number:Temperature | -32767 | 32767 | No | BT22 Supply air temp. |  |
| airsupply#40183 | Number:Temperature | -32767 | 32767 | No | AZ30-BT23 Outdoor temp. ERS |  |
| airsupply#40311 | Number:Dimensionless | 0 | 255 | No | External ERS accessory GQ2 speed |  |
| airsupply#40312 | Number:Dimensionless | 0 | 255 | No | External ERS accessory GQ3 speed |  |
| airsupply#40942 | Switch | --- | --- | No | External ERS accessory block status |  |
| airsupply#47260 | Number | --- | --- | Yes | Selected fan speed | 0=normal, 1=speed 1, 2=speed 2, 3=speed 3, 4=speed 4 |
