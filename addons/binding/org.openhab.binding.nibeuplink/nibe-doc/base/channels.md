| Channel Type ID | Item Type    | Min          | Max          | Writable | Description                         | Allowed Values (write access)  |
|-----------------|--------------|--------------|--------------|----------|-------------------------------------|--------------------------------|
| base#40004 | Number:Temperature | -32767 | 32767 | No | BT1 Outdoor Temperature |  |
| base#40067 | Number:Temperature | -32767 | 32767 | No | BT1 Average |  |
| base#43005 | Number:Dimensionless | -30000 | 30000 | Yes | Degree Minutes (16 bit) | any integer |
| base#43009 | Number:Temperature | -32767 | 32767 | No | Calc. Supply S1 |  |
| base#40071 | Number:Temperature | -32767 | 32767 | No | BT25 Ext. Supply |  |
| base#40033 | Number:Temperature | -32767 | 32767 | No | BT50 Room Temp S1 |  |
| base#43161 | Switch | --- | --- | No | External adjustment activated via input S1 |  |
| base#40008 | Number:Temperature | -32767 | 32767 | No | BT2 Supply temp S1 |  |
| base#40012 | Number:Temperature | -32767 | 32767 | No | EB100-EP14-BT3 Return temp |  |
| base#40072 | Number:Dimensionless | -32767 | 32767 | No | BF1 EP14 Flow |  |
| base#40079 | Number:ElectricCurrent | 0 | 4294967295 | No | EB100-BE3 Current |  |
| base#40081 | Number:ElectricCurrent | 0 | 4294967295 | No | EB100-BE2 Current |  |
| base#40083 | Number:ElectricCurrent | 0 | 4294967295 | No | EB100-BE1 Current |  |
| base#10033 | Switch | --- | --- | No | Int. el.add. blocked |  |
| base#43081 | Number:Time | 0 | 1000000 | No | Tot. op.time add. |  |
| base#43084 | Number:Power | -32767 | 32767 | No | Int. el.add. Power |  |
| base#47212 | Number:Power | 0 | 4500 | No | Max int add. power |  |
| base#48914 | Number:Power | 0 | 4500 | No | Max int add. power, SG Ready |  |
| base#44308 | Number:Energy | 0 | 9999999 | No | Heat Meter - Heat Cpr EP14 |  |
| base#44304 | Number:Energy | 0 | 9999999 | No | Heat Meter - Pool Cpr EP14 |  |
| base#44300 | Number:Energy | 0 | 9999999 | No | Heat Meter - Heat Cpr and Add EP14 |  |
| base#48043 | Switch | --- | --- | Yes | vacation mode |  |
| base#10012 | Switch | --- | --- | No | Compressor blocked |  |
| hotwater#40013 | Number:Temperature | -32767 | 32767 | No | BT7 HW Top |  |
| hotwater#40014 | Number:Temperature | -32767 | 32767 | No | BT6 HW Load |  |
| hotwater#44306 | Number:Energy | 0 | 9999999 | No | Heat Meter - HW Cpr EP14 |  |
| hotwater#44298 | Number:Energy | 0 | 9999999 | No | Heat Meter - HW Cpr and Add EP14 |  |
| hotwater#48132 | Number | --- | --- | Yes | Temporary Lux | 0=Off, 1=3h, 2=6h, 3=12h, 4=One time increase |
| hotwater#47041 | Number | --- | --- | Yes | Hot water mode | 0=Economy, 1=Normal, 2=Luxury |
