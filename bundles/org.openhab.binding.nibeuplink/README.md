# NibeUplink Binding

The NibeUplink binding is used to get "live data" from from Nibe heat pumps without plugging any custom devices into your heat pump.
This avoids the risk of losing your warranty.
Instead data is retrieved from Nibe Uplink.
This binding should in general be compatible with heat pump models that support Nibe Uplink.
In general read access is supported for all channels.
Write access is only supported for a small subset of channels.
Write access will only be available with a paid subscription for "manage" at NibeUplink.

## Supported Things

This binding provides only one thing type: The Nibe heat pump.
Create one Nibe heat pump thing per physical heat pump installation available in your home(s).
If your setup contains an outdoor unit such as F2030 or F2040 and an indoor unit such as VVM320 this is one installation where the indoor unit is the master that has access to all data produced by the outdoor unit (slave).

## Discovery

Auto-Discovery is not supported, as credentials are necessary to login into NibeUplink.

## Thing Configuration

The syntax for a heat pump thing is:

```java
nibeuplink:<THING TYPE>:<NAME>
```

- **nibeuplink** the binding id, fixed
- **thing type** the heatpump thing type
- **name** the name of the heatpump (choose any name)

Following models (indoor / main units) are currently supported:

| Nibe Model(s)     | Thing Type | Description                                         |
|-------------------|------------|-----------------------------------------------------|
| VVM310 / 500      | vvm310     | reduced set of channels based on NibeUplink website |
| VVM320 / 325      | vvm320     | reduced set of channels based on NibeUplink website |
| F730              | f730       | reduced set of channels based on NibeUplink website |
| F750              | f750       | reduced set of channels based on NibeUplink website |
| F1145 / 1245      | f1145      | reduced set of channels based on NibeUplink website |
| F1155 / 1255      | f1155      | reduced set of channels based on NibeUplink website |

The following configuration parameters are available for this thing:

- **user** (required)  
username used to login on NibeUplink

- **password** (required)  
password used to login on NibeUplink

- **nibeId** (required)  
Id of your heatpump in NibeUplink (can be found in the URL after successful login: `https://www.nibeuplink.com/System/**<nibeId>>**/Status/Overview`)

- **pollingInterval**  
interval (seconds) in which values are retrieved from NibeUplink.
Setting less than 60 seconds does not make any sense as the heat pump only provides periodic updates to NibeUplink.
(default = 60)

- **houseKeepingInterval**  
interval (seconds) in which list of "dead channels" (channels that do not return any data or invalid data) should be purged (default = 3600).
Usually this settings should not be changed.

### Examples

- minimum configuration

```java
nibeuplink:vvm320:mynibe [ user="...", password="...", nibeId="..."]
```

- with pollingInterval

```java
nibeuplink:vvm320:mynibe[ user="...", password="...", nibeId="...", pollingInterval=... ]
```

- multiple heat pumps

```java
nibeuplink:vvm320:home1 [ user="...", password="...", nibeId="..."]
nibeuplink:vvm320:home2  [ user="...", password="...", nibeId="..."]
```

## Channels

Available channels depend on the specific heatpump model.
Following models/channels are currently available:

### All Models

| Channel Type ID | Item Type              | Min    | Max        | Writable | Description                                | Allowed Values (write access)                 |
|-----------------|------------------------|--------|------------|----------|--------------------------------------------|-----------------------------------------------|
| base#40004      | Number:Temperature     | -32767 | 32767      | No       | BT1 Outdoor Temperature                    |                                               |
| base#40067      | Number:Temperature     | -32767 | 32767      | No       | BT1 Average                                |                                               |
| base#43005      | Number:Dimensionless   | -30000 | 30000      | Yes      | Degree Minutes (16 bit)                    | any integer                                   |
| base#43009      | Number:Temperature     | -32767 | 32767      | No       | Calc. Supply S1                            |                                               |
| base#40071      | Number:Temperature     | -32767 | 32767      | No       | BT25 Ext. Supply                           |                                               |
| base#40033      | Number:Temperature     | -32767 | 32767      | No       | BT50 Room Temp S1                          |                                               |
| base#43161      | Switch                 | ---    | ---        | No       | External adjustment activated via input S1 |                                               |
| base#40008      | Number:Temperature     | -32767 | 32767      | No       | BT2 Supply temp S1                         |                                               |
| base#40012      | Number:Temperature     | -32767 | 32767      | No       | EB100-EP14-BT3 Return temp                 |                                               |
| base#40072      | Number:Dimensionless   | -32767 | 32767      | No       | BF1 EP14 Flow                              |                                               |
| base#43437      | Number:Dimensionless   | 0      | 100        | No       | Supply Pump Speed EP14                     |                                               |
| base#40079      | Number:ElectricCurrent | 0      | 4294967295 | No       | EB100-BE3 Current                          |                                               |
| base#40081      | Number:ElectricCurrent | 0      | 4294967295 | No       | EB100-BE2 Current                          |                                               |
| base#40083      | Number:ElectricCurrent | 0      | 4294967295 | No       | EB100-BE1 Current                          |                                               |
| base#10033      | Switch                 | ---    | ---        | No       | Int. el.add. blocked                       |                                               |
| base#43081      | Number:Time            | 0      | 1000000    | No       | Tot. op.time add.                          |                                               |
| base#43084      | Number:Power           | -32767 | 32767      | No       | Int. el.add. Power                         |                                               |
| base#47212      | Number:Power           | 0      | 4500       | No       | Max int add. power                         |                                               |
| base#48914      | Number:Power           | 0      | 4500       | No       | Max int add. power, SG Ready               |                                               |
| base#44308      | Number:Energy          | 0      | 9999999    | No       | Heat Meter - Heat Cpr EP14                 |                                               |
| base#44304      | Number:Energy          | 0      | 9999999    | No       | Heat Meter - Pool Cpr EP14                 |                                               |
| base#44300      | Number:Energy          | 0      | 9999999    | No       | Heat Meter - Heat Cpr and Add EP14         |                                               |
| base#48043      | Switch                 | ---    | ---        | Yes      | vacation mode                              |                                               |
| base#10012      | Switch                 | ---    | ---        | No       | Compressor blocked                         |                                               |
| hotwater#40013  | Number:Temperature     | -32767 | 32767      | No       | BT7 HW Top                                 |                                               |
| hotwater#40014  | Number:Temperature     | -32767 | 32767      | No       | BT6 HW Load                                |                                               |
| hotwater#44306  | Number:Energy          | 0      | 9999999    | No       | Heat Meter - HW Cpr EP14                   |                                               |
| hotwater#44298  | Number:Energy          | 0      | 9999999    | No       | Heat Meter - HW Cpr and Add EP14           |                                               |
| hotwater#48132  | Number                 | ---    | ---        | Yes      | Temporary Lux                              | 0=Off, 1=3h, 2=6h, 3=12h, 4=One time increase |
| hotwater#47041  | Number                 | ---    | ---        | Yes      | Hot water mode                             | 0=Economy, 1=Normal, 2=Luxury                 |
| hotwater#47045  | Number                 | 5      | 70         | No       | Start temperature HW Economy               |                                               |
| hotwater#47049  | Number                 | 5      | 70         | No       | Stop temperature HW Economy                |                                               |
| hotwater#47044  | Number                 | 5      | 70         | No       | Start temperature HW Normal                |                                               |
| hotwater#47048  | Number                 | 5      | 70         | No       | Stop temperature HW Normal                 |                                               |
| hotwater#47043  | Number                 | 5      | 70         | No       | Start temperature HW Luxury                |                                               |
| hotwater#47047  | Number                 | 5      | 70         | No       | Stop temperature HW Luxury                 |                                               |
| hotwater#47046  | Number                 | 55     | 70         | No       | Stop temperature Periodic HW               |                                               |

### F730

| Channel Type ID  | Item Type            | Min    | Max     | Writable | Description                       | Allowed Values (write access) |
|------------------|----------------------|--------|---------|----------|-----------------------------------|-------------------------------|
| compressor#43181 | Number:Dimensionless | 0      | 100     | No       | Chargepump speed                  |                               |
| compressor#43424 | Number:Time          | 0      | 9999999 | No       | Tot. HW op.time compr. EB100-EP14 |                               |
| compressor#43420 | Number:Time          | 0      | 9999999 | No       | Tot. op.time compr. EB100-EP14    |                               |
| compressor#43416 | Number               | 0      | 9999999 | No       | Compressor starts EB100-EP14      |                               |
| compressor#40022 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT17 Suction           |                               |
| compressor#40019 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT15 Liquid Line       |                               |
| compressor#40018 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT14 Hot Gas Temp      |                               |
| compressor#40017 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT12 Condensor Out     |                               |
| compressor#40020 | Number:Temperature   | -32767 | 32767   | No       | EB100-BT16 Evaporator temp        |                               |
| compressor#43136 | Number:Frequency     | 0      | 65535   | No       | Compressor Frequency, Actual      |                               |
| compressor#43122 | Number:Frequency     | -32767 | 32767   | No       | Compr. current min.freq.          |                               |
| compressor#43123 | Number:Frequency     | -32767 | 32767   | No       | Compr. current max.freq.          |                               |
| compressor#43066 | Number:Time          | -32767 | 32767   | No       | Defrosting time                   |                               |
| airsupply#10001  | Number:Dimensionless | 0      | 100     | No       | Fan speed current                 |                               |
| airsupply#40025  | Number:Temperature   | -32767 | 32767   | No       | BT20 Exhaust air temp. 1          |                               |
| airsupply#40026  | Number:Temperature   | -32767 | 32767   | No       | BT21 Vented air temp. 1           |                               |
| airsupply#43124  | Number               | -32767 | 32767   | No       | Airflow ref.                      |                               |
| airsupply#41026  | Number               | -32767 | 32767   | No       | EB100-Adjusted BS1 Air flow       |                               |
| airsupply#43125  | Number               | 0      | 100     | No       | Airflow reduction                 |                               |
| airsupply#40919  | Number               | ---    | ---     | No       | Air mix                           |                               |
| airsupply#40101  | Number:Temperature   | -32767 | 32767   | No       | BT28 Airmix Temp                  |                               |

### F750

| Channel Type ID  | Item Type            | Min    | Max     | Writable | Description                       | Allowed Values (write access)                        |
|------------------|----------------------|--------|---------|----------|-----------------------------------|------------------------------------------------------|
| compressor#43181 | Number:Dimensionless | 0      | 100     | No       | Chargepump speed                  |                                                      |
| compressor#43424 | Number:Time          | 0      | 9999999 | No       | Tot. HW op.time compr. EB100-EP14 |                                                      |
| compressor#43420 | Number:Time          | 0      | 9999999 | No       | Tot. op.time compr. EB100-EP14    |                                                      |
| compressor#43416 | Number               | 0      | 9999999 | No       | Compressor starts EB100-EP14      |                                                      |
| compressor#40022 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT17 Suction           |                                                      |
| compressor#40019 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT15 Liquid Line       |                                                      |
| compressor#40018 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT14 Hot Gas Temp      |                                                      |
| compressor#40017 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT12 Condensor Out     |                                                      |
| compressor#40020 | Number:Temperature   | -32767 | 32767   | No       | EB100-BT16 Evaporator temp        |                                                      |
| compressor#43136 | Number:Frequency     | 0      | 65535   | No       | Compressor Frequency, Actual      |                                                      |
| compressor#43122 | Number:Frequency     | -32767 | 32767   | No       | Compr. current min.freq.          |                                                      |
| compressor#43123 | Number:Frequency     | -32767 | 32767   | No       | Compr. current max.freq.          |                                                      |
| airsupply#40025  | Number:Temperature   | -32767 | 32767   | No       | BT20 Exhaust air temp. 1          |                                                      |
| airsupply#40026  | Number:Temperature   | -32767 | 32767   | No       | BT21 Vented air temp. 1           |                                                      |
| airsupply#43124  | Number               | -32767 | 32767   | No       | Airflow ref.                      |                                                      |
| airsupply#41026  | Number               | -32767 | 32767   | No       | EB100-Adjusted BS1 Air flow       |                                                      |
| airsupply#47260  | Number               | ---    | ---     | Yes      | Current fan speed                 | 0=normal, 1=speed 1, 2=speed 2, 3=speed 3, 4=speed 4 |

### F1145 / 1245

| Channel Type ID  | Item Type            | Min    | Max     | Writable | Description                              | Allowed Values (write access) |
|------------------|----------------------|--------|---------|----------|------------------------------------------|-------------------------------|
| general#44302    | Number:Energy        | 0      | 9999999 | No       | Heat Meter - Cooling Cpr EP14            |                               |
| general#44270    | Number:Temperature   | -32767 | 32767   | No       | Calculated Cooling Supply Temperature S1 |                               |
| general#43103    | Number               | 10     | 70      | No       | HPAC state                               |                               |
| compressor#43424 | Number:Time          | 0      | 9999999 | No       | Tot. HW op.time compr. EB100-EP14        |                               |
| compressor#43420 | Number:Time          | 0      | 9999999 | No       | Tot. op.time compr. EB100-EP14           |                               |
| compressor#43416 | Number               | 0      | 9999999 | No       | Compressor starts EB100-EP14             |                               |
| compressor#40022 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT17 Suction                  |                               |
| compressor#40019 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT15 Liquid Line              |                               |
| compressor#40018 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT14 Hot Gas Temp             |                               |
| compressor#40017 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT12 Condensor Out            |                               |
| compressor#40015 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT10 Brine In Temperature     |                               |
| compressor#40016 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT11 Brine Out Temperature    |                               |
| compressor#43439 | Number:Dimensionless | 0      | 100     | No       | EP14-GP2 Brine Pump Speed                |                               |
| airsupply#40025  | Number:Temperature   | -32767 | 32767   | No       | BT20 Exhaust air temp. 1                 |                               |
| airsupply#40026  | Number:Temperature   | -32767 | 32767   | No       | BT21 Vented air temp. 1                  |                               |

### F1155 / 1255

| Channel Type ID  | Item Type            | Min    | Max     | Writable | Description                           | Allowed Values (write access) |
|------------------|----------------------|--------|---------|----------|---------------------------------------|-------------------------------|
| general#44302    | Number:Energy        | 0      | 9999999 | No       | Heat Meter - Cooling Cpr EP14         |                               |
| compressor#43424 | Number:Time          | 0      | 9999999 | No       | Tot. HW op.time compr. EB100-EP14     |                               |
| compressor#43420 | Number:Time          | 0      | 9999999 | No       | Tot. op.time compr. EB100-EP14        |                               |
| compressor#43416 | Number               | 0      | 9999999 | No       | Compressor starts EB100-EP14          |                               |
| compressor#40022 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT17 Suction               |                               |
| compressor#40019 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT15 Liquid Line           |                               |
| compressor#40018 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT14 Hot Gas Temp          |                               |
| compressor#40017 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT12 Condensor Out         |                               |
| compressor#43136 | Number:Frequency     | 0      | 65535   | No       | Compressor Frequency, Actual          |                               |
| compressor#43122 | Number:Frequency     | -32767 | 32767   | No       | Compr. current min.freq.              |                               |
| compressor#43123 | Number:Frequency     | -32767 | 32767   | No       | Compr. current max.freq.              |                               |
| compressor#40015 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT10 Brine In Temperature  |                               |
| compressor#40016 | Number:Temperature   | -32767 | 32767   | No       | EB100-EP14-BT11 Brine Out Temperature |                               |
| compressor#43439 | Number:Dimensionless | 0      | 100     | No       | EP14-GP2 Brine Pump Speed             |                               |
| airsupply#40025  | Number:Temperature   | -32767 | 32767   | No       | BT20 Exhaust air temp. 1              |                               |
| airsupply#40026  | Number:Temperature   | -32767 | 32767   | No       | BT21 Vented air temp. 1               |                               |

### VVM310 / VVM 500

| Channel Type ID  | Item Type            | Min    | Max     | Writable | Description                                        | Allowed Values (write access)                        |
|------------------|----------------------|--------|---------|----------|----------------------------------------------------|------------------------------------------------------|
| general#44270    | Number:Temperature   | -32767 | 32767   | No       | Calc. Cooling Supply S1                            |                                                      |
| general#40121    | Number:Temperature   | -32767 | 32767   | No       | BT63 Add Supply Temp                               |                                                      |
| general#44302    | Number:Energy        | 0      | 9999999 | No       | Heat Meter - Cooling Cpr EP14                      |                                                      |
| general#47011    | Number               | -10    | 10      | Yes      | Heat Offset S1                                     | values between -10 and 10                            |
| general#47394    | Switch               | ---    | ---     | Yes      | Use room sensor S1                                 | 0=off, 1=on                                          |
| general#47402    | Number               | 0      | 60      | Yes      | Room sensor factor S1                              | Values between 0 and 6                               |
| general#48793    | Number               | 0      | 60      | Yes      | Room sensor cool factor S1                         | Values between 0 and 6                               |
| compressor#44362 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT28 Outdoor Temp                       |                                                      |
| compressor#44396 | Number:Dimensionless | 0      | 255     | No       | EB101 Speed charge pump                            |                                                      |
| compressor#44703 | Number               | ---    | ---     | No       | EB101-EP14 Defrosting Outdoor Unit                 | 0=No, 1=Active, 2=Passive                            |
| compressor#44073 | Number:Time          | 0      | 9999999 | No       | EB101-EP14 Tot. HW op.time compr                   |                                                      |
| compressor#40737 | Number:Time          | 0      | 9999999 | No       | EB101-EP14 Tot. Cooling op.time compr              |                                                      |
| compressor#44071 | Number:Time          | 0      | 9999999 | No       | EB101-EP14 Tot. op.time compr                      |                                                      |
| compressor#44069 | Number               | 0      | 9999999 | No       | EB101-EP14 Compressor starts                       |                                                      |
| compressor#44061 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT17 Suction                            |                                                      |
| compressor#44060 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT15 Liquid Line                        |                                                      |
| compressor#44059 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT14 Hot Gas Temp                       |                                                      |
| compressor#44058 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT12 Condensor Out                      |                                                      |
| compressor#44055 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT3 Return Temp.                        |                                                      |
| compressor#44363 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT16 Evaporator                         |                                                      |
| compressor#44699 | Number:Pressure      | -32767 | 32767   | No       | EB101-EP14-BP4 Pressure Sensor                     |                                                      |
| compressor#40782 | Number:Frequency     | 0      | 255     | No       | EB101 Cpr Frequency Desired F2040                  |                                                      |
| compressor#44701 | Number:Frequency     | -32767 | 32767   | No       | EB101-EP14 Actual Cpr Frequency Outdoor Unit       |                                                      |
| compressor#44702 | Number               | ---    | ---     | No       | EB101-EP14 Protection Status Register Outdoor Unit |                                                      |
| compressor#44700 | Number:Pressure      | -32767 | 32767   | No       | EB101-EP14 Low Pressure Sensor Outdoor Unit        |                                                      |
| compressor#44457 | Number               | 0      | 255     | No       | EB101-EP14 Compressor State                        |                                                      |
| airsupply#40025  | Number:Temperature   | -32767 | 32767   | No       | BT20 Exhaust air temp. 1                           |                                                      |
| airsupply#40026  | Number:Temperature   | -32767 | 32767   | No       | BT21 Vented air temp. 1                            |                                                      |
| airsupply#40075  | Number:Temperature   | -32767 | 32767   | No       | BT22 Supply air temp.                              |                                                      |
| airsupply#40183  | Number:Temperature   | -32767 | 32767   | No       | AZ30-BT23 Outdoor temp. ERS                        |                                                      |
| airsupply#40311  | Number:Dimensionless | 0      | 255     | No       | External ERS accessory GQ2 speed                   |                                                      |
| airsupply#40312  | Number:Dimensionless | 0      | 255     | No       | External ERS accessory GQ3 speed                   |                                                      |
| airsupply#40942  | Switch               | ---    | ---     | No       | External ERS accessory block status                |                                                      |
| airsupply#47260  | Number               | ---    | ---     | Yes      | Selected fan speed                                 | 0=normal, 1=speed 1, 2=speed 2, 3=speed 3, 4=speed 4 |

### VVM320 / VVM325

| Channel Type ID  | Item Type            | Min    | Max     | Writable | Description                                        | Allowed Values (write access)                        |
|------------------|----------------------|--------|---------|----------|----------------------------------------------------|------------------------------------------------------|
| general#44270    | Number:Temperature   | -32767 | 32767   | No       | Calc. Cooling Supply S1                            |                                                      |
| general#40121    | Number:Temperature   | -32767 | 32767   | No       | BT63 Add Supply Temp                               |                                                      |
| general#44302    | Number:Energy        | 0      | 9999999 | No       | Heat Meter - Cooling Cpr EP14                      |                                                      |
| general#47011    | Number               | -10    | 10      | Yes      | Heat Offset S1                                     | values between -10 and 10                            |
| general#47394    | Switch               | ---    | ---     | Yes      | Use room sensor S1                                 | 0=off, 1=on                                          |
| general#47402    | Number               | 0      | 60      | Yes      | Room sensor factor S1                              | Values between 0 and 6                               |
| general#48793    | Number               | 0      | 60      | Yes      | Room sensor cool factor S1                         | Values between 0 and 6                               |
| general#47374    | Number:Temperature   | 10     | 40      | Yes      | Start temperature cooling                          | Values between 10 and 40                             |
| general#47375    | Number:Temperature   | 0      | 30      | Yes      | Stop temperature heating                           | Values between 0 and 30                              |
| general#47376    | Number:Temperature   | -20    | 10      | Yes      | Stop temperature additive                          | Values between -20 and 10                            |
| general#47377    | Number:Time          | 1      | 48      | Yes      | Outdoor Filter Time                                | Values between 1 and 48                              |
| compressor#44362 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT28 Outdoor Temp                       |                                                      |
| compressor#44396 | Number:Dimensionless | 0      | 255     | No       | EB101 Speed charge pump                            |                                                      |
| compressor#44703 | Number               | ---    | ---     | No       | EB101-EP14 Defrosting Outdoor Unit                 | 0=No, 1=Active, 2=Passive                            |
| compressor#44073 | Number:Time          | 0      | 9999999 | No       | EB101-EP14 Tot. HW op.time compr                   |                                                      |
| compressor#40737 | Number:Time          | 0      | 9999999 | No       | EB101-EP14 Tot. Cooling op.time compr              |                                                      |
| compressor#44071 | Number:Time          | 0      | 9999999 | No       | EB101-EP14 Tot. op.time compr                      |                                                      |
| compressor#44069 | Number               | 0      | 9999999 | No       | EB101-EP14 Compressor starts                       |                                                      |
| compressor#44061 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT17 Suction                            |                                                      |
| compressor#44060 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT15 Liquid Line                        |                                                      |
| compressor#44059 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT14 Hot Gas Temp                       |                                                      |
| compressor#44058 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT12 Condensor Out                      |                                                      |
| compressor#44055 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT3 Return Temp.                        |                                                      |
| compressor#44363 | Number:Temperature   | -32767 | 32767   | No       | EB101-EP14-BT16 Evaporator                         |                                                      |
| compressor#44699 | Number:Pressure      | -32767 | 32767   | No       | EB101-EP14-BP4 Pressure Sensor                     |                                                      |
| compressor#40782 | Number:Frequency     | 0      | 255     | No       | EB101 Cpr Frequency Desired F2040                  |                                                      |
| compressor#44701 | Number:Frequency     | -32767 | 32767   | No       | EB101-EP14 Actual Cpr Frequency Outdoor Unit       |                                                      |
| compressor#44702 | Number               | ---    | ---     | No       | EB101-EP14 Protection Status Register Outdoor Unit |                                                      |
| compressor#44700 | Number:Pressure      | -32767 | 32767   | No       | EB101-EP14 Low Pressure Sensor Outdoor Unit        |                                                      |
| compressor#44457 | Number               | 0      | 255     | No       | EB101-EP14 Compressor State                        |                                                      |
| airsupply#40025  | Number:Temperature   | -32767 | 32767   | No       | BT20 Exhaust air temp. 1                           |                                                      |
| airsupply#40026  | Number:Temperature   | -32767 | 32767   | No       | BT21 Vented air temp. 1                            |                                                      |
| airsupply#40075  | Number:Temperature   | -32767 | 32767   | No       | BT22 Supply air temp.                              |                                                      |
| airsupply#40183  | Number:Temperature   | -32767 | 32767   | No       | AZ30-BT23 Outdoor temp. ERS                        |                                                      |
| airsupply#40311  | Number:Dimensionless | 0      | 255     | No       | External ERS accessory GQ2 speed                   |                                                      |
| airsupply#40312  | Number:Dimensionless | 0      | 255     | No       | External ERS accessory GQ3 speed                   |                                                      |
| airsupply#40942  | Switch               | ---    | ---     | No       | External ERS accessory block status                |                                                      |
| airsupply#47260  | Number               | ---    | ---     | Yes      | Selected fan speed                                 | 0=normal, 1=speed 1, 2=speed 2, 3=speed 3, 4=speed 4 |

### Custom Channels

An arbitrary number of custom channels can be added via the UI or using file based configuration.
There are three custom channel types available, which allow different scaling of the raw values retrieved from the NIBE API:

- type-number-unscaled
- type-number-scale10
- type-number-scale100

## Full Example

### Thing

```java
nibeuplink:vvm320:mynibe     [ user="nibe@my-domain.de", password="secret123", nibeId="4711", pollingInterval=300] {
   Channels:
        Type type-number-scale10  : 47015 "min supply temp heating"
        Type type-number-unscaled : 48177 "min supply temp cooling"
}
```

### Items

As the binding supports UoM you might define units in the item's label.
An automatic conversion is applied e.g. from °C to °F then.
Channels which represent two states (such as on/off) are represented as Switch.
Channels which have more than two states are internally represented as number.
You need to define a map file which also gives you the opportunity to translate the state into your preferred language.

```java
Number:Temperature      NIBE_SUPPLY            "Vorlauf"                                 { channel="nibeuplink:vvm320:mynibe:base#40008" }
Number:Temperature      NIBE_RETURN            "Rücklauf [%.2f °F]"                      { channel="nibeuplink:vvm320:mynibe:base#40012" }
Number:Temperature      NIBE_HW_TOP            "Brauchwasser oben"                       { channel="nibeuplink:vvm320:mynibe:hotwater#40013" }
Number:Energy           NIBE_HM_HEAT           "WM Heizung"                              { channel="nibeuplink:vvm320:mynibe:base#44308" }
Switch                  NIBE_COMP_DEFROST      "Enteisung"                               { channel="nibeuplink:vvm320:mynibe:compressor#44703" }
Number                  NIBE_HW_MODE           "Modus [MAP(hwmode.map):%s]"              { channel="nibeuplink:vvm320:mynibe:hotwater#47041" }

Number                  NIBE_MIN_SUP_HEAT      "min supply temp. heating [%.1f °C]"      { channel="nibeuplink:vvm320:mynibe:47015" }
Number                  NIBE_MIN_SUP_COOL      "min supply temp. cooling [%d °C]"        { channel="nibeuplink:vvm320:mynibe:48177" }
```

### Transformations

Please define each state as integer.

```text
0=Eco
1=Norm
2=Lux
```

### Sitemaps

Please take care of the status channels.
If you use selection items an automatic mapping will be applied.
If you prefer switch items a mapping must be applied like this:

```java
Switch item=NIBE_HW_MODE mappings=[0="Eco", 1="Norm"]
```
