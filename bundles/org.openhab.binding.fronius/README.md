# Fronius Binding

This binding uses the [Fronius Solar API V1](https://www.fronius.com/en/photovoltaics/products/all-products/system-monitoring/open-interfaces/fronius-solar-api-json-) to obtain data from Fronius devices.

It supports Fronius inverters and Fronius Smart Meter. Supports:
* Fronius Symo
* Fronius Symo Gen24
* Fronius Smart Meter 63A
* Fronius Smart Meter TS 65A-3
* Fronius Ohmpilot

## Supported Things

| Thing Type      | Description                                                                                                                                                                                                                           |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `bridge`        | The Bridge                                                                                                                                                                                                                            |
| `powerinverter` | Fronius Galvo, Symo and other Fronius inverters in combination with the Fronius Datamanager 1.0 / 2.0 or Fronius Datalogger. You can add multiple inverters that depend on the same datalogger with different device ids. (Default 1) |
| `meter`         | Fronius Smart Meter. You can add multiple smart meters with different device ids. (The default id = 0)                                                                                                                                
| `ohmpilot`      | Fronius Ohmpilot. (The default id = 0)     


## Discovery

There is no discovery implemented. You have to create your things manually and specify the hostname or IP address of the Datalogger and the device id.

## Binding Configuration

The binding has no configuration options, all configuration is done at `bridge`, `powerinverter`, `meter` or `ohmpilot` level.

## Thing Configuration

### Bridge Thing Configuration

| Parameter         | Description                                           |
| ----------------- | ----------------------------------------------------- |
| `hostname`        | The hostname or IP address of your Fronius Datalogger |
| `refreshInterval` | Refresh interval in seconds                           |

### Powerinverter Thing Configuration

| Parameter  | Description                                |
| ---------- | ------------------------------------------ |
| `deviceId` | The identifier of your device (Default: 1) |

### Meter Thing Configuration

| Parameter  | Description                                     |
| ---------- | ----------------------------------------------- |
| `deviceId` | The identifier of your smart meter (Default: 0) |

### Ohmpilot Thing Configuration

| Parameter  | Description                                     |
| ---------- | ----------------------------------------------- |
| `deviceId` | The identifier of your ohmpilot (Default: 0) |

## Channels

### Channels for `powerinverter` Thing

| Channel ID                           | Item Type | Description                                                                                                       |
| ------------------------------------ | --------- | ----------------------------------------------------------------------------------------------------------------- |
| `inverterdatachanneldayenergy`       | Number    | Energy generated on current day                                                                                   |
| `inverterdatachannelpac`             | Number    | AC power                                                                                                          |
| `inverterdatachanneltotal`           | Number    | Energy generated overall                                                                                          |
| `inverterdatachannelyear`            | Number    | Energy generated in current year                                                                                  |
| `inverterdatachannelfac`             | Number    | AC frequency                                                                                                      |
| `inverterdatachanneliac`             | Number    | AC current                                                                                                        |
| `inverterdatachannelidc`             | Number    | DC current                                                                                                        |
| `inverterdatachanneluac`             | Number    | AC voltage                                                                                                        |
| `inverterdatachanneludc`             | Number    | DC voltage                                                                                                        |
| `inverterdatadevicestatuserrorcode`  | Number    | Device error code                                                                                                 |
| `inverterdatadevicestatusstatuscode` | Number    | Device status code<br />`0` - `6` Startup<br />`7` Running <br />`8` Standby<br />`9` Bootloading<br />`10` Error |
| `powerflowchannelpgrid`              | Number    | Power + from grid, - to grid                                                                                      |
| `powerflowchannelpload`              | Number    | Power + generator, - consumer                                                                                     |
| `powerflowchannelpakku`              | Number    | Power + charge, - discharge                                                                                       |

### Channels for `meter` Thing

| Channel ID              | Item Type                | Description                                                                                                                                                                                                              |
| ----------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `enable`                | Number                   | 1 = enabled, 0 = disabled                                                                                                                                                                                                |
| `location`              | Number                   | 0 = grid interconnection point (primary meter)<br/> 1 = load (primary meter)   <br />3 = external generator (secondary meters)(multiple)<br />256-511 = subloads (secondary meters)(unique). Refer to Fronius Solar API. |
| `currentacphase1`       | Number:ElectricCurrent   | AC Current on Phase 1                                                                                                                                                                                                    |
| `currentacphase2`       | Number:ElectricCurrent   | AC Current on Phase 2                                                                                                                                                                                                    |
| `currentacphase3`       | Number:ElectricCurrent   | AC Current on Phase 3                                                                                                                                                                                                    |
| `voltageacphase1`       | Number:ElectricPotential | AC Voltage on Phase 1                                                                                                                                                                                                    |
| `voltageacphase2`       | Number:ElectricPotential | AC Voltage on Phase 2                                                                                                                                                                                                    |
| `voltageacphase3`       | Number:ElectricPotential | AC Voltage on Phase 3                                                                                                                                                                                                    |
| `powerrealphase1`       | Number:Power             | Real Power on Phase 1                                                                                                                                                                                                    |
| `powerrealphase2`       | Number:Power             | Real Power on Phase 2                                                                                                                                                                                                    |
| `powerrealphase3`       | Number:Power             | Real Power on Phase 3                                                                                                                                                                                                    |
| `powerrealsum`          | Number:Power             | Real Power summed up                                                                                                                                                                                                    |
| `powerfactorphase1`     | Number                   | Power Factor on Phase 1                                                                                                                                                                                                  |
| `powerfactorphase2`     | Number                   | Power Factor on Phase 2                                                                                                                                                                                                  |
| `powerfactorphase3`     | Number                   | Power Factor on Phase 3                                                                                                                                                                                                  |
| `energyrealsumconsumed` | Number:Energy            | Real Energy consumed                                                                                                                                                                                                     |
| `energyrealsumproduced` | Number:Energy            | Real Energy produced                                                                                                                                                                                                     |
|                         |


### Channels for `ohmpilot` Thing

| Channel ID              | Item Type                | Description                                                                                                                                                                                                              |
| ----------------------- | ------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `energyrealsumconsumed` | Number:Energy            | Real Energy consumed                                                                                                                                                                                                     |
| `powerrealsum` | Number:Power            | Real Power                                                                                                                                                                                                     |
| `temperaturechannel1` | Number:Temperature            | Temperature                                                                                                                                                                                                     |
| `errorcode`  | Number    | Device error code                                                                                                 |
| `statecode` | Number    | Device state code<br />`0` up and running <br />`1` keep minimum temperature <br />`2` legionella protection <br />`3` critical fault<br />`4` fault<br />`5` boost mode |
|                         |


## Properties

### The `meter` thing has the following properties:

| Property | Description                    |
| -------- | ------------------------------ |
| `modelId`  | The model name of the meter    |
| `serialNumber` | The serial number of the meter |

### The `ohmpilot` thing has the following property:

| Property | Description                    |
| -------- | ------------------------------ |
| `modelId`  | The model name of the ohmpilot    |
| `serialNumber` | The serial number of the ohmpilot |

## Full Example

demo.things:

```
Bridge fronius:bridge:mybridge [hostname="192.168.66.148", refreshInterval=5] {
    Thing powerinverter myinverter [deviceId=1]
    Thing meter mymeter [deviceId=0]
    Thing ohmpilot myohmpilot [deviceId=0]    
}
```

demo.items:

```
Number AC_Power { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachannelpac" }
Number Day_Energy { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneldayenergy" }
Number Total_Energy { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneltotal" }
Number Year_Energy { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachannelyear" }
Number FAC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachannelfac" }
Number IAC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneliac" }
Number IDC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachannelidc" }
Number UAC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneluac" }
Number UDC { channel="fronius:powerinverter:mybridge:myinverter:inverterdatachanneludc" }
Number ErrorCode { channel="fronius:powerinverter:mybridge:myinverter:inverterdatadevicestatuserrorcode" }
Number StatusCode { channel="fronius:powerinverter:mybridge:myinverter:inverterdatadevicestatusstatuscode" }
Number Grid_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpgrid" }
Number Load_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpload" }
Number Battery_Power { channel="fronius:powerinverter:mybridge:myinverter:powerflowchannelpakku" }

Number Meter_Enable { channel="fronius:meter:mybridge:mymeter:enable" }
Number Meter_Location { channel="fronius:meter:mybridge:mymeter:location" }
Number:ElectricCurrent Meter_CurrentPhase1 { channel="fronius:meter:mybridge:mymeter:currentacphase1" }
Number:ElectricCurrent Meter_CurrentPhase2 { channel="fronius:meter:mybridge:mymeter:currentacphase2" }
Number:ElectricCurrent Meter_CurrentPhase3 { channel="fronius:meter:mybridge:mymeter:currentacphase3" }
Number:Voltage Meter_VoltagePhase1 { channel="fronius:meter:mybridge:mymeter:voltageacphase1" }
Number:Voltage Meter_VoltagePhase2 { channel="fronius:meter:mybridge:mymeter:voltageacphase2" }
Number:Voltage Meter_VoltagePhase3 { channel="fronius:meter:mybridge:mymeter:voltageacphase3" }
Number:Power Meter_PowerPhase1 { channel="fronius:meter:mybridge:mymeter:powerrealphase1" }
Number:Power Meter_PowerPhase2 { channel="fronius:meter:mybridge:mymeter:powerrealphase2" }
Number:Power Meter_PowerPhase3 { channel="fronius:meter:mybridge:mymeter:powerrealphase3" }
Number:Power Meter_PowerSum    { channel="fronius:meter:mybridge:mymeter:powerrealsum" }
Number Meter_PowerFactorPhase1 { channel="fronius:meter:mybridge:mymeter:powerfactorphase1" }
Number Meter_PowerFactorPhase2 { channel="fronius:meter:mybridge:mymeter:powerfactorphase2" }
Number Meter_PowerFactorPhase3 { channel="fronius:meter:mybridge:mymeter:powerfactorphase3" }
Number:Energy Meter_EnergyConsumed { channel="fronius:meter:mybridge:mymeter:energyrealsumconsumed" }
Number:Energy Meter_EnergyProduced { channel="fronius:meter:mybridge:mymeter:energyrealsumproduced" }

Number:Energy Ohmpilot_EnergyConsumed { channel="fronius:ohmpilot:mybridge:myohmpilot:energyrealsumconsumed" }
Number:Power Ohmpilot_PowerSum { channel="fronius:ohmpilot:mybridge:myohmpilot:powerrealsum" }
Number:Temperature Ohmpilot_Temperature { channel="fronius:ohmpilot:mybridge:myohmpilot:temperaturechannel1" }
Number Ohmpilot_State { channel="fronius:ohmpilot:mybridge:myohmpilot:statecode" }
Number Ohmpilot_Errorcode { channel="fronius:ohmpilot:mybridge:myohmpilot:errorcode" }

```
