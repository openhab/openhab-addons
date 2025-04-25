# Studer

This extension adds support for the Studer protocol.

Studer Innotec, founded in 1987 by Roland Studer, is an ISO-certified company that develops and manufactures inverters, inverter/chargers and MPPT solar charge controllers to communicate over the Modbus protocol entirely in Switzerland.

For a list of certified products see this page: <https://www.studer-innotec.com/>

## Supported Things

This bundle adds the following thing type to the Modbus binding.
Note, that the things will show up under the Modbus binding.

| Thing Type IDs | Description                                                                                                          | Picture                             |
|----------------|----------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| bsp            | For BSP that offers a highly precise measuring for Xtender, VarioTrack and VarioString systems                       | ![BSP](doc/bsp.png)                 |
| xtender        | For the Xtender models for system capacities from 0.5kVA to 72kVA that allow for the optimal use of available energy | ![Xtender](doc/xtender.png)         |
| variotrack     | For the VarioTrack models of MPPT solar charge controllers for systems with solar PV capacity from 1 - 75kWp         | ![VarioTrack](doc/variotrack.png)   |
| variostring    | For the VarioString models of MPPT solar charge controllers for systems with solar PV capacity from 4                | ![VarioString](doc/variostring.png) |

## Thing Configuration

You need first to set up a Serial Modbus bridge according to the Modbus documentation.
Things in this extension will use the selected bridge to connect to the device.

For defining a thing textually, you have to find out the start address of the model block and the length of it.
While the length is usually fixed, the address is not.
Please refer to your device's vendor documentation how model blocks are laid for your equipment.

OR: If there is no offset configured (default config) on the dip switch in RS-485, the following are mostly interesting for getting things up and running:

| Offset | Device                |
|--------|-----------------------|
| 10     | Multicast Xtender     |
| 11-19  | Xtender 1-9           |
| 20     | Multicast Variotrack  |
| 21-35  | Variotrack 1-15       |
| 40     | Multicast Variostring |
| 41-55  | Variostring 1-15      |
| 61     | BSP/Xcom-CAN          |

More Details about that can be found in the technical specification and appendix for Studer RTU Modbus protocol. Check the default config (dip switches 1 and 2 off) while configuring the pin-out on the RS-485!

Multicast writes on any devices of given class, but reads only on the first available device (Not Summary!). Currently, there are no writes available, 10/20/40 is useless for now.

The following parameters are valid for all thing types:

| Parameter | Type    | Required | Default if omitted      | Description                                                                |
|-----------|---------|----------|-------------------------|----------------------------------------------------------------------------|
| address   | integer | yes      | `first slave of device` | Address of slave                                                           |
| refresh   | integer | yes      | 5                       | Poll interval in seconds. Increase this if you encounter connection errors |

## Channels

The following channels and their associated channel types are shown below divided by device.

### BSP

All channels read for a BSP device

| Channel            | Type                     | Description           |
| ------------------ | ------------------------ | --------------------- |
| power              | Number:Power             | Power                 |
| batteryVoltage     | Number:ElectricPotential | Battery voltage       |
| batteryCurrent     | Number:ElectricCurrent   | Battery current       |
| stateOfCharge      | Number:Dimensionless     | State of Charge       |
| batteryTemperature | Number:Temperature       | Battery temperature   |

### Xtender

All channels read for an Xtender device

| Channel           | Type                     | Description             |
| ----------------- | ------------------------ | ----------------------- |
| inputVoltage      | Number:ElectricPotential | Input voltage           |
| inputCurrent      | Number:ElectricCurrent   | Input current           |
| inputActivePower  | Number:Power             | Input active power      |
| inputFrequency    | Number:Frequency         | Input frequency         |
| outputVoltage     | Number:ElectricPotential | Output voltage          |
| outputCurrent     | Number:ElectricCurrent   | Output current          |
| outputActivePower | Number:Power             | Output active power     |
| outputFrequency   | Number:Frequency         | Output frequency        |
| operatingState    | String                   | Operating state         |
| stateInverter     | String                   | State of the inverter   |

### VarioTrack

All channels read for a VarioTrack device

| Channel              | Type                     | Description                               |
| -------------------- | ------------------------ | ----------------------------------------- |
| modelVarioTrack      | String                   | Model of VarioTrack                       |
| voltagePVGenerator   | Number:ElectricPotential | Voltage of the PV generator               |
| powerPVGenerator     | Number:Power             | Power of the PV generator                 |
| productionCurrentDay | Number:Energy            | Production in (kWh) for the current day   |
| batteryVoltage       | Number:ElectricPotential | Battery voltage                           |
| batteryCurrent       | Number:ElectricCurrent   | Battery current                           |
| operatingMode        | String                   | Operating mode                            |
| stateVarioTrack      | String                   | State of the VarioTrack                   |

### VarioString

All channels read for a VarioString device

| Channel                 | Type                     | Description                                   |
| ----------------------- | ------------------------ | --------------------------------------------- |
| PVVoltage               | Number:ElectricPotential | PV voltage                                    |
| PVCurrent               | Number:ElectricCurrent   | PV current                                    |
| PVPower                 | Number:Power             | PV power                                      |
| ProductionPVCurrentDay  | Number:Energy            | Production PV in (kWh) for the current day    |
| PV1Voltage              | Number:ElectricPotential | PV1 voltage                                   |
| PV1Current              | Number:ElectricCurrent   | PV1 current                                   |
| PV1Power                | Number:Power             | PV1 power                                     |
| ProductionPV1CurrentDay | Number:Energy            | Production PV1 in (kWh) for the current day   |
| PV2Voltage              | Number:ElectricPotential | PV2 voltage                                   |
| PV2Current              | Number:ElectricCurrent   | PV2 current                                   |
| PV2Power                | Number:Power             | PV2 power                                     |
| ProductionPV2CurrentDay | Number:Energy            | Production PV2 in (kWh) for the current day   |
| batteryVoltage          | Number:ElectricPotential | Battery voltage                               |
| batteryCurrent          | Number:ElectricCurrent   | Battery current                               |
| PVMode                  | String                   | PV operating mode                             |
| PV1Mode                 | String                   | PV1 operating mode                            |
| PV2Mode                 | String                   | PV2 operating mode                            |
| stateVarioString        | String                   | State of the VarioString                      |

## Example

### Thing Configuration

```java
Bridge modbus:serial:bridge [port="/dev/ttyUSB0",baud=9600,dataBits=8,parity="even",stopBits="1.0",encoding="rtu"]
..or..
Bridge modbus:tcp:bridge [host="192.168.178.56", port=502, rtuEncoded=true]

...

Thing modbus:xtender:bridge:xtender_Phase1 "Xtender" (modbus:serial:modbusbridge) [ slaveAddress=11, refresh=5 ]
Thing modbus:variostring:bridge:variostring_left "Xtender" (modbus:serial:modbusbridge) [ slaveAddress=41, refresh=5 ]
Thing modbus:variostring:bridge:variostring_right "Xtender" (modbus:serial:modbusbridge) [ slaveAddress=42, refresh=5 ]
Thing modbus:bsp:bridge:byd "BydBox" (modbus:serial:modbusbridge) [ slaveAddress=61, refresh=5 ]
```

Note: Make sure that refresh and slave address are numerical, without quotes.

### Item Configuration

```java
Number Studer_Xtender_Phase1_InputVoltage "Input Voltage [%.2f V]"          {channel="modbus:xtender:bridge:xtender_Phase1:inputVoltage"}
Number Studer_Xtender_Phase1_InputCurrent "Input Current [%.2f A]"          {channel="modbus:xtender:bridge:xtender_Phase1:inputCurrent"}
String Studer_Xtender_Phase1_StateInverter "State: [%s]"                    {channel="modbus:xtender:bridge:xtender_Phase1:stateInverter"}

Number Studer_PVCurrent_Left               "Current [%.2f]"                 {channel="modbus:variostring:bridge:variostring_left:PVCurrent"}
Number Studer_PVPower_Left                 "Power"                          {channel="modbus:variostring:bridge:variostring_left:PVPower"}
Number Studer_ProductionPVCurrentDay_Left  "ProductionCurrentDay [%.3f kW]" {channel="modbus:variostring:bridge:variostring_left:ProductionPVCurrentDay"}
String Studer_PVMode_Left                  "Mode: [%s]"                     {channel="modbus:variostring:bridge:variostring_left:PVMode"}
String Studer_stateVarioString_Left        "State: [%s]"                    {channel="modbus:variostring:bridge:variostring_left:stateVarioString"}

Number Studer_BSP_SOC                "State: [%s]"                          {channel="modbus:bsp:bridge:byd:stateOfCharge"}
Number Studer_BSP_batteryVoltage     "Battery Voltage: [%s]"                {channel="modbus:bsp:bridge:byd:batteryVoltage"}
```

### Sitemap Configuration

```perl
Text item=Studer_Xtender_Phase1_InputVoltage
Text item=Studer_Xtender_Phase1_InputCurrent
Text item=Studer_Xtender_Phase1_StateInverter

Chart item=Studer_Xtender_Phase1_InputVoltage period=D refresh=600000
Chart item=Studer_Xtender_Phase1_InputCurrent period=D refresh=30000

Text item=Studer_BSP_SOC
Text item=Studer_BSP_batteryVoltage

Text item=Studer_PVCurrent_Left
Text item=Studer_PVPower_Left
Text item=Studer_ProductionPVCurrentDay_Left
Text item=Studer_PVMode_Left
Text item=Studer_stateVarioString_Left
```
