| Channel Type ID | Item Type    | Min          | Max          | Writable | Description                         | Allowed Values (write access)  |
|-----------------|--------------|--------------|--------------|----------|-------------------------------------|--------------------------------|
| general#40004 | Number | -32767 | 32767 | No | BT1 Outdoor Temperature |  |
| general#40067 | Number | -32767 | 32767 | No | BT1 Average |  |
| general#43005 | Number | -30000 | 30000 | Yes | Degree Minutes (16 bit) | any integer |
| general#43009 | Number | -32767 | 32767 | No | Calc. Supply S1 |  |
| general#40033 | Number | -32767 | 32767 | No | BT50 Room Temp S1 |  |
| general#43161 | String | --- | --- | No | External adjustment activated via input S1 |  |
| general#40008 | Number | -32767 | 32767 | No | BT2 Supply temp S1 |  |
| general#40012 | Number | -32767 | 32767 | No | EB100-EP14-BT3 Return temp |  |
| general#40071 | Number | -32767 | 32767 | No | BT25 Ext. Supply |  |
| general#40072 | Number | -32767 | 32767 | No | BF1 EP14 Flow |  |
| general#44270 | Number | -32767 | 32767 | No | Calc. Cooling Supply S1 |  |
| general#43081 | Number | 0 | 1000000 | No | Tot. op.time add. |  |
| general#43084 | Number | -32767 | 32767 | No | Int. el.add. Power |  |
| general#47212 | Number | 0 | 4500 | No | Max int add. power |  |
| general#48914 | Number | 0 | 4500 | No | Max int add. power, SG Ready |  |
| general#40121 | Number | -32767 | 32767 | No | BT63 Add Supply Temp |  |
| general#43437 | Number | 0 | 255 | No | Supply Pump Speed EP14 |  |
| general#44308 | Number | 0 | 9999999 | No | Heat Meter - Heat Cpr EP14 |  |
| general#44304 | Number | 0 | 9999999 | No | Heat Meter - Pool Cpr EP14 |  |
| general#44302 | Number | 0 | 9999999 | No | Heat Meter - Cooling Cpr EP14 |  |
| general#44300 | Number | 0 | 9999999 | No | Heat Meter - Heat Cpr and Add EP14 |  |
| general#47011 | Number | -10 | 10 | Yes | Heat Offset S1 | values between -10 and 10 |
| hotwater#40013 | Number | -32767 | 32767 | No | BT7 HW Top |  |
| hotwater#40014 | Number | -32767 | 32767 | No | BT6 HW Load |  |
| hotwater#44306 | Number | 0 | 9999999 | No | Heat Meter - HW Cpr EP14 |  |
| hotwater#44298 | Number | 0 | 9999999 | No | Heat Meter - HW Cpr and Add EP14 |  |
| hotwater#48132 | String | --- | --- | Yes | Temporary Lux | 0=Off, 1=3h, 2=6h, 3=12h, 4=One time increase |
| hotwater#47041 | String | --- | --- | Yes | Hot water mode | 0=Economy, 1=Normal, 2=Luxury |
| compressor#44362 | Number | -32767 | 32767 | No | EB101-EP14-BT28 Outdoor Temp |  |
| compressor#44396 | Number | 0 | 255 | No | EB101 Speed charge pump |  |
| compressor#44703 | String | --- | --- | No | EB101-EP14 Defrosting Outdoor Unit | 0=No, 1=Active, 2=Passive |
| compressor#44073 | Number | 0 | 9999999 | No | EB101-EP14 Tot. HW op.time compr |  |
| compressor#40737 | Number | 0 | 9999999 | No | EB101-EP14 Tot. Cooling op.time compr |  |
| compressor#44071 | Number | 0 | 9999999 | No | EB101-EP14 Tot. op.time compr |  |
| compressor#44069 | Number | 0 | 9999999 | No | EB101-EP14 Compressor starts |  |
| compressor#44061 | Number | -32767 | 32767 | No | EB101-EP14-BT17 Suction |  |
| compressor#44060 | Number | -32767 | 32767 | No | EB101-EP14-BT15 Liquid Line |  |
| compressor#44059 | Number | -32767 | 32767 | No | EB101-EP14-BT14 Hot Gas Temp |  |
| compressor#44058 | Number | -32767 | 32767 | No | EB101-EP14-BT12 Condensor Out |  |
| compressor#44055 | Number | -32767 | 32767 | No | EB101-EP14-BT3 Return Temp. |  |
| compressor#44363 | Number | -32767 | 32767 | No | EB101-EP14-BT16 Evaporator |  |
| compressor#44699 | Number | -32767 | 32767 | No | EB101-EP14-BP4 Pressure Sensor |  |
| compressor#40782 | Number | 0 | 255 | No | EB101 Cpr Frequency Desired F2040 |  |
| compressor#44701 | Number | -32767 | 32767 | No | EB101-EP14 Actual Cpr Frequency Outdoor Unit |  |
| compressor#44702 | String | --- | --- | No | EB101-EP14 Protection Status Register Outdoor Unit |  |
| compressor#44700 | Number | -32767 | 32767 | No | EB101-EP14 Low Pressure Sensor Outdoor Unit |  |
| compressor#44457 | Number | 0 | 255 | No | EB101-EP14 Compressor State |  |
| airsupply#40025 | Number | -32767 | 32767 | No | BT20 Exhaust air temp. 1 |  |
| airsupply#40026 | Number | -32767 | 32767 | No | BT21 Vented air temp. 1 |  |
| airsupply#40075 | Number | -32767 | 32767 | No | BT22 Supply air temp. |  |
| airsupply#40183 | Number | -32767 | 32767 | No | AZ30-BT23 Outdoor temp. ERS |  |
| airsupply#40311 | Number | 0 | 255 | No | External ERS accessory GQ2 speed |  |
| airsupply#40312 | Number | 0 | 255 | No | External ERS accessory GQ3 speed |  |
| airsupply#40942 | String | --- | --- | No | External ERS accessory block status |  |
| airsupply#47260 | String | --- | --- | Yes | Selected fan speed | 0=normal, 1=speed 1, 2=speed 2, 3=speed 3, 4=speed 4 |
