# OpenTherm Gateway Binding

This binding is used to integrate the OpenTherm Gateway into openHAB.
The OpenTherm Gateway is a module designed by Schelte Bron that can be connected to units that support communication using the OpenTherm protocol, such as boiler or ventilation / heat recovery unit.

More information on the OpenTherm Gateway device can be found at <https://otgw.tclcode.com/>

## Discovery

The binding does not support auto discovery.

## Binding Configuration

The binding does not require any configuration.

## Supported Things

The OpenTherm Gateway binding supports three Things:

- `openthermgateway` which is the bridge that handles communication with the OpenTherm Gateway device.
- `boiler` which represents a central heating boiler unit.
- `ventilationheatrecovery` which represents a ventilation / heat recovery unit.

## Thing Configuration

### Thing Configuration for `openthermgateway`

The `openthermgateway` bridge is designed to support various ways of connecting to the OpenTherm Gateway device, but currently only supports a TCP socket connection.
The configuration settings for the bridge are Hostname/IP address and Port, which are used to connect to the gateway, and an automatic connection retry interval in case the connection to the OpenTherm Gateway device is lost.

| Parameter                 | Name                      | Description                                                                      | Required | Default |
|---------------------------|---------------------------|----------------------------------------------------------------------------------|----------|---------|
| `ipaddress`               | Hostname or IP address    | The hostname or IP address to connect to the OpenTherm Gateway.                  | yes      |         |
| `port`                    | Port                      | The port used to connect to the OpenTherm Gateway.                               | yes      |         |
| `connectionRetryInterval` | Connection Retry Interval | The interval in seconds to retry connecting (0 = disabled).                      | yes      | 60      |
| `connectTimeoutSeconds`   | Connect Timeout           | The maximum time (seconds) to wait for establishing a connection to the gateway. | yes      | 5       |
| `readTimeoutSeconds`      | Read Timeout              | The maximum time (seconds) to wait for reading responses from the gateway.       | yes      | 20      |

### Thing Configuration for `boiler` and `ventilationheatrecovery`

The `boiler` and `ventilationheatrecovery` things do not require any configuration settings.

## Channels

### Channels for `openthermgateway`

The `openthermgateway` bridge supports the following channels:

| Channel Type ID           | Item Type            | Description                                              | Access |
|---------------------------|----------------------|----------------------------------------------------------|--------|
| sendcommand               | Text                 | Channel to send commands to the OpenTherm Gateway device | W      |

### Channels for `boiler`

The `boiler` thing supports the following channels:

| Channel ID | Item Type | Description | Access |
|------------|-----------|-------------|--------|
| roomtemp | Number:Temperature | Current sensed room temperature | R |
| roomsetpoint | Number:Temperature | Current room temperature setpoint | R |
| temperaturetemporary | Number:Temperature | Temporary override room temperature setpoint | R/W |
| temperatureconstant | Number:Temperature | Constant override room temperature setpoint | R/W |
| controlsetpoint | Number:Temperature | Central heating water setpoint set at boiler | R |
| controlsetpointrequested | Number:Temperature | Central heating water setpoint requested by Thermostat | R |
| controlsetpointoverride | Number:Temperature | Central heating water setpoint configured on OTGW | R/W |
| controlsetpoint2 | Number:Temperature | Central heating 2 water setpoint set at boiler | R |
| controlsetpoint2requested | Number:Temperature | Central heating 2 water setpoint requested by Thermostat | R |
| controlsetpoint2override | Number:Temperature | Central heating 2 water setpoint configured on OTGW | R/W |
| dhwtemp | Number:Temperature | Domestic hot water temperature | R |
| tdhwset | Number:Temperature | Domestic hot water temperature setpoint | R |
| overridedhwsetpoint | Number:Temperature | Domestic hot water temperature setpoint override | R/W |
| flowtemp | Number:Temperature | Boiler water temperature | R |
| returntemp | Number:Temperature | Return water temperature | R |
| outsidetemp | Number:Temperature | Outside temperature | R/W |
| waterpressure | Number:Dimensionless | Central heating water pressure | R |
| ch_enable | Switch | Central heating enabled set at boiler | R |
| ch_enablerequested | Switch | Central heating enabled requested by thermostat | R |
| ch_enableoverride | Switch | Central heating enabled overridden at OTGW | R/W |
| ch2_enable | Switch | Central heating 2 enabled set at boiler | R |
| ch2_enablerequested | Switch | Central heating 2 enabled requested by thermostat | R |
| ch2_enableoverride | Switch | Central heating 2 enabled overridden at OTGW | R/W |
| ch_mode | Switch | Central heating active | R |
| dhw_enable | Switch | Domestic hot water enabled | R |
| dhw_mode | Switch | Domestic hot water active | R |
| flame | Switch | Burner active | R |
| modulevel | Number:Dimensionless | Relative modulation level | R |
| maxrelmdulevel | Number:Dimensionless | Maximum relative modulation level | R |
| fault | Switch | Fault indication | R |
| servicerequest | Switch | Service required | R |
| lockout-reset | Switch | Lockout-reset enabled | R |
| lowwaterpress | Switch | Low water pressure fault | R |
| gasflamefault | Switch | Gas or flame fault | R |
| airpressfault | Switch | Air pressure fault | R |
| waterovtemp | Switch | Water over-temperature fault | R |
| oemfaultcode | Number:Dimensionless | OEM fault code | R |
| diag | Switch | Diagnostics indication | R |
| unsuccessfulburnerstarts | Number:Dimensionless | Unsuccessful burner starts | R |
| burnerstarts | Number:Dimensionless | Burner starts | R |
| chpumpstarts | Number:Dimensionless | Central heating pump starts | R |
| dhwpvstarts | Number:Dimensionless | Domestic hot water pump/valve starts | R |
| dhwburnerstarts | Number:Dimensionless | Domestic hot water burner starts | R |
| burnerhours | Number:Time | Burner hours | R |
| chpumphours | Number:Time | Central heating pump hours | R |
| dhwpvhours | Number:Time | Domestic hot water pump/valve hours | R |
| dhwburnerhours | Number:Time | Domestic hot water burner hours | R |
| tspnumber | Number:Dimensionless | Number of transparant slave parameter entries | R |
| tspentry | Number:Dimensionless | Transparent slave parameter entry | R |
| fhbnumber | Number:Dimensionless | Number of fault history buffer entries | R |
| fhbentry | Number:Dimensionless | Fault history buffer entry | R |

### Channels for `ventilationheatrecovery`

The `ventilationheatrecovery` thing supports the following channels:

| Channel ID | Item Type | Description | Access |
|------------|-----------|-------------|--------|
| vh_ventilationenable | Switch | Ventilation enabled | R |
| vh_bypassposition | Number:Dimensionless | Bypass position | R |
| vh_bypassmode | Number:Dimensionless | Bypass mode | R |
| vh_freeventilationmode | Switch | Free ventilation mode | R |
| vh_faultindication | Switch | Fault indication | R |
| vh_ventilationmode | Switch | Ventilation mode | R |
| vh_bypassstatus | Switch | Bypass status | R |
| vh_bypassautomaticstatus | Number:Dimensionless | Bypass automatic status | R |
| vh_freeventilationstatus | Switch | Free ventilation status | R |
| vh_filtercheck | Switch | Filter Check enabled | R |
| vh_diagnosticindication | Switch | Diagnostic indication | R |
| vh_controlsetpoint | Number:Dimensionless | Control setpoint | R |
| vh_servicerequest | Switch | Service request | R |
| vh_exhaustfanfault | Switch | Exhaust fan fault | R |
| vh_inletfanfault | Switch | Inlet fan fault | R |
| vh_frostprotection | Switch | Frost protection | R |
| vh_faultcode | Number:Dimensionless | Fault code | R |
| vh_diagnosticcode | Number:Dimensionless | Diagnostic code | R |
| vh_systemtype | Number:Dimensionless | System type | R |
| vh_bypass | Switch | Bypass | R |
| vh_speedcontrol | Number:Dimensionless | Speed control | R |
| vh_memberid | Number:Dimensionless | Member ID | R |
| vh_openthermversion | Number:Dimensionless | OpenTherm version | R |
| vh_versiontype | Number:Dimensionless | Version type | R |
| vh_relativeventilation | Number:Dimensionless | Relative ventilation position | R |
| vh_relativehumidity | Number:Dimensionless | Relative humidity exhaust air | R |
| vh_co2level | Number:Dimensionless | CO2 level exhaust air | R |
| vh_supplyinlettemp | Number:Temperature | Supply inlet temperature | R |
| vh_supplyoutlettemp | Number:Temperature | Supply outlet temperature | R |
| vh_exhaustinlettemp | Number:Temperature | Exhaust inlet temperature | R |
| vh_exhaustoutlettemp | Number:Temperature | Exhaust outlet temperature | R |
| vh_actualexhaustfanspeed | Number:Dimensionless | Actual exhaust fan speed | R |
| vh_actualinletfanspeed | Number:Dimensionless | Actual inlet fan speed | R |
| vh_nominalventenable | Switch | Nominal ventilation value transfer enabled | R |
| vh_nominalventrw | Number:Dimensionless | Nominal ventilation value | R |
| vh_nominalventilationvalue | Number:Dimensionless | Nominal ventilation value | R |
| vh_ventilationsetpoint | Number:Dimensionless | Ventilation setpoint override | R/W |
| vh_tspnumber | Number:Dimensionless | Number of transparent slave parameter entries | R |
| vh_tspentry | Number:Dimensionless | Transparent slave parameter entry | R |
| vh_fhbnumber | Number:Dimensionless | Number of fault history buffer entries | R |
| vh_fhbentry | Number:Dimensionless | Fault history buffer entry | R |

## Transparent Slave Parameters and Fault History Buffer channels

The transparent slave parameters (TSP) and fault history buffer (FHB) use a variable number of entries.
The number of entries is determined by a TSP or FHB size message.
Channels for TSP and FHB entries are automatically created when a TSP or FHB size message is received.
An index number is added to the base channel name to create a unique channel name for each entry.

For example, if a TSP size message is received for a boiler unit (OpenTherm DATA-ID 10) with value 60, then channels `tspentry_0` through `tspentry_59` will be automatically created and linked to the corresponding TSP entry (OpenTherm DATA-ID 11).

## Using OpenTherm Gateway as a master device

When using OpenTherm with a boiler and a thermostat, the thermostat (master) periodically sends messages to the boiler to request data.
The boiler (slave) then sends a response message with the requested data which is then used by the OpenTherm Gateway binding to update the channel values in openHAB.

If you have a setup without a master device requesting data, then the slave device may send fewer or even no OpenTherm mesages at all.

In this case, you can make the OpenTherm Gateway act as a master device by sending Priority Message (PM) commands.
With openHAB rules, you can use the `sendcommand` channel of the `openthermgateway` bridge to periodically send PM commands to the OpenTherm Gateway.

Example:

```java
SendCommand.sendCommand("PM=10")
```

This will cause the OpenTherm Gateway to send a READ-DATA message to the slave device with DATA-ID 10. If supported, the slave device will respond with a READ-ACK message and the current value.

## Full Example

### demo.things

```java
Bridge openthermgateway:openthermgateway:1 "OpenTherm Gateway" [ ipaddress="192.168.1.100", port="8000", connectionRetryInterval=60 ] {
    Thing boiler remeha "Remeha Avanta 28c"
    Thing ventilationheatrecovery brink "Brink Renovent Excellent 300"
}
```

### demo.items for `openthermgateway`

```java
Text SendCommand "Send command channel" { channel="openthermgateway:openthermgateway:1:sendcommand" }
```

### demo.items for `boiler`

```java
Number:Temperature RoomTemperature "Room Temperature [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:roomtemp }
Number:Temperature RoomSetpoint "Room Setpoint [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:roomsetpoint }
Number:Temperature TemporaryRoomSetpointOverride "Temporary Room Setpoint Override [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:temperaturetemporary }
Number:Temperature ConstantRoomSetpointOverride "Constant Room Setpoint Override [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:temperatureconstant }
Number:Temperature ControlSetpoint "Control Setpoint [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:controlsetpoint }
Number:Temperature ControlSetpointRequested "Control Setpoint Requested [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:controlsetpointrequested }
Number:Temperature ControlSetpointOverride "Control Setpoint Override [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:controlsetpointoverride }
Number:Temperature ControlSetpoint2 "Control Setpoint 2 [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:controlsetpoint2 }
Number:Temperature ControlSetpoint2Requested "Control Setpoint 2 Requested [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:controlsetpoint2requested }
Number:Temperature ControlSetpoint2Override "Control Setpoint 2 Override [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:controlsetpoint2override }
Number:Temperature DomesticHotWaterTemperature "Domestic Hot Water Temperature [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:dhwtemp }
Number:Temperature DomesticHotWaterSetpoint "Domestic Hot Water Setpoint [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:tdhwset }
Number:Temperature DomesticHotWaterSetpointOverride "Domestic Hot Water Setpoint Override [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:overridedhwsetpoint }
Number:Temperature BoilerWaterTemperature "Boiler Water Temperature [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:flowtemp }
Number:Temperature ReturnWaterTemperature "Return Water Temperature [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:returntemp }
Number:Temperature OutsideTemperature "Outside Temperature [%.1f %unit%]" <temperature> { channel="openthermgateway:boiler:1:remeha:outsidetemp }
Number:Dimensionless CentralHeatingWaterPressure "Central Heating Water Pressure [%.1f bar]" { channel="openthermgateway:boiler:1:remeha:waterpressure }
Switch CentralHeatingEnabled "Central Heating Enabled" <switch> { channel="openthermgateway:boiler:1:remeha:ch_enable }
Switch CentralHeatingEnabledThermostat "Central Heating Enabled Thermostat" <switch> { channel="openthermgateway:boiler:1:remeha:ch_enablerequested }
Switch CentralHeatingOverridden "Central Heating Overridden" <switch> { channel="openthermgateway:boiler:1:remeha:ch_enableoverride }
Switch CentralHeating2Enabled "Central Heating 2 Enabled" <switch> { channel="openthermgateway:boiler:1:remeha:ch2_enable }
Switch CentralHeating2EnabledThermostat "Central Heating 2 Enabled Thermostat" <switch> { channel="openthermgateway:boiler:1:remeha:ch2_enablerequested }
Switch CentralHeating2Overridden "Central Heating 2 Overridden" <switch> { channel="openthermgateway:boiler:1:remeha:ch2_enableoverride }
Switch CentralHeatingActive "Central Heating Active" <switch> { channel="openthermgateway:boiler:1:remeha:ch_mode }
Switch DomesticHotWaterEnabled "Domestic Hot Water Enabled" <switch> { channel="openthermgateway:boiler:1:remeha:dhw_enable }
Switch DomesticHotWaterActive "Domestic Hot Water Active" <switch> { channel="openthermgateway:boiler:1:remeha:dhw_mode }
Switch BurnerActive "Burner Active" <switch> { channel="openthermgateway:boiler:1:remeha:flame }
Number:Dimensionless RelativeModulationLevel "Relative Modulation Level [%.1f %%]" { channel="openthermgateway:boiler:1:remeha:modulevel }
Number:Dimensionless MaximumRelativeModulationLevel "Maximum Relative Modulation Level [%.1f %%]" { channel="openthermgateway:boiler:1:remeha:maxrelmdulevel }
Switch FaultIndication "Fault Indication" <switch> { channel="openthermgateway:boiler:1:remeha:fault }
Switch ServiceRequired "Service Required" <switch> { channel="openthermgateway:boiler:1:remeha:servicerequest }
Switch LockoutResetEnabled "Lockout-Reset Enabled" <switch> { channel="openthermgateway:boiler:1:remeha:lockout-reset }
Switch LowWaterPressureFault "Low Water Pressure Fault" <switch> { channel="openthermgateway:boiler:1:remeha:lowwaterpress }
Switch GasOrFlameFault "Gas Or Flame Fault" <switch> { channel="openthermgateway:boiler:1:remeha:gasflamefault }
Switch AirPressureFault "Air Pressure Fault" <switch> { channel="openthermgateway:boiler:1:remeha:airpressfault }
Switch WaterOverTemperatureFault "Water Over-Temperature Fault" <switch> { channel="openthermgateway:boiler:1:remeha:waterovtemp }
Number:Dimensionless OEMFaultCode "OEM Fault Code" { channel="openthermgateway:boiler:1:remeha:oemfaultcode }
Number:Dimensionless UnsuccessfulBurnerStarts "Unsuccessful Burner Starts" { channel="openthermgateway:boiler:1:remeha:unsuccessfulburnerstarts }
Number:Dimensionless BurnerStarts "Burner Starts" { channel="openthermgateway:boiler:1:remeha:burnerstarts }
Number:Dimensionless CentralHeatingPumpStarts "Central Heating Pump Starts" { channel="openthermgateway:boiler:1:remeha:chpumpstarts }
Number:Dimensionless DomesticHotWaterPump/ValveStarts "Domestic Hot Water Pump/Valve Starts" { channel="openthermgateway:boiler:1:remeha:dhwpvstarts }
Number:Dimensionless DomesticHotWaterBurnerStarts "Domestic Hot Water Burner Starts" { channel="openthermgateway:boiler:1:remeha:dhwburnerstarts }
Number:Time BurnerHours "Burner Hours" { channel="openthermgateway:boiler:1:remeha:burnerhours }
Number:Time CentralHeatingPumpHours "Central Heating Pump Hours" { channel="openthermgateway:boiler:1:remeha:chpumphours }
Number:Time DomesticHotWaterPumpValveHours "Domestic Hot Water Pump/Valve Hours" { channel="openthermgateway:boiler:1:remeha:dhwpvhours }
Number:Time DomesticHotWaterBurnerHours "Domestic Hot Water Burner Hours" { channel="openthermgateway:boiler:1:remeha:dhwburnerhours }
Number:Dimensionless TransparentSlaveParameterNumber "Transparent Slave Parameter Number" { channel="openthermgateway:boiler:1:remeha:tspnumber }
Number:Dimensionless TransparentSlaveParameterEntry "Transparent Slave Parameter Entry" { channel="openthermgateway:boiler:1:remeha:tspentry }
Number:Dimensionless FaultHistoryBufferNumber "Fault History Buffer Number" { channel="openthermgateway:boiler:1:remeha:fhbnumber }
Number:Dimensionless FaultHistoryBufferEntry "Fault History Buffer Entry" { channel="openthermgateway:boiler:1:remeha:fhbentry }
```

### demo.items for `ventilationheatrecovery`

```java
Switch VentilationEnabled "Ventilation Enabled" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_ventilationenable }
Number:Dimensionless BypassPosition "Bypass Position" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_bypassposition }
Number:Dimensionless BypassMode "Bypass Mode" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_bypassmode }
Switch FreeVentilationMode "Free Ventilation Mode" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_freeventilationmode }
Switch FaultIndication "Fault Indication" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_faultindication }
Switch VentilationMode "Ventilation Mode" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_ventilationmode }
Switch BypassStatus "Bypass Status" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_bypassstatus }
Number:Dimensionless BypassAutomaticStatus "Bypass Automatic Status" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_bypassautomaticstatus }
Switch FreeVentilationStatus "Free Ventilation Status" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_freeventilationstatus }
Switch FilterCheck "Filter Check" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_filtercheck }
Switch DiagnosticIndication "Diagnostic Indication" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_diagnosticindication }
Number:Dimensionless ControlSetpoint "Control Setpoint" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_controlsetpoint }
Switch ServiceRequest "Service Request" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_servicerequest }
Switch ExhaustFanFault "Exhaust Fan Fault" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_exhaustfanfault }
Switch InletFanFault "Inlet Fan Fault" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_inletfanfault }
Switch FrostProtection "Frost Protection" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_frostprotection }
Number:Dimensionless FaultCode "Fault Code" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_faultcode }
Number:Dimensionless DiagnosticCode "Diagnostic Code" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_diagnosticcode }
Number:Dimensionless SystemType "System Type" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_systemtype }
Switch Bypass "Bypass" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_bypass }
Number:Dimensionless SpeedControl "Speed Control" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_speedcontrol }
Number:Dimensionless MemberID "Member ID" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_memberid }
Number:Dimensionless OpenThermVersion "OpenTherm Version" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_openthermversion }
Number:Dimensionless VersionType "Version Type" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_versiontype }
Number:Dimensionless RelativeVentilation "Relative Ventilation [%d %%]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_relativeventilation }
Number:Dimensionless RelativeHumidity "Relative Humidity [%d %%]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_relativehumidity }
Number:Dimensionless CO2Level "CO2 Level [%d ppm]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_co2level }
Number:Temperature SupplyInletTemperature "Supply Inlet Temperature [%.1f %unit%]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_supplyinlettemp }
Number:Temperature SupplyOutletTemperature "Supply Outlet Temperature [%.1f %unit%]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_supplyoutlettemp }
Number:Temperature ExhaustInletTemperature "Exhaust Inlet Temperature [%.1f %unit%]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_exhaustinlettemp }
Number:Temperature ExhaustOutletTemperature "Exhaust Outlet Temperature [%.1f %unit%]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_exhaustoutlettemp }
Number:Dimensionless ActualExhaustFanSpeed "Actual Exhaust Fan Speed [%d rpm]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_actualexhaustfanspeed }
Number:Dimensionless ActualInletFanSpeed "Actual Inlet Fan Speed [%d rpm]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_actualinletfanspeed }
Switch NominalVentilationValueTransfer "Nominal Ventilation Value Transfer" <switch> { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_nominalventenable }
Number:Dimensionless NominalVentilationValue "Nominal Ventilation Value" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_nominalventrw }
Number:Dimensionless NominalVentilationValue "Nominal Ventilation Value [%d %%]" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_nominalventilationvalue }
Number:Dimensionless VentilationSetpoint "Ventilation Setpoint" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_ventilationsetpoint }
Number:Dimensionless TransparentSlaveParameterNumber "Transparent Slave Parameter Number" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_tspnumber }
Number:Dimensionless TransparentSlaveParameterEntry "Transparent Slave Parameter Entry" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_tspentry }
Number:Dimensionless FaultHistoryBufferNumber "Fault History Buffer Number" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_fhbnumber }
Number:Dimensionless FaultHistoryBufferEntry "Fault History Buffer Entry" { channel="openthermgateway:ventilationheatrecovery:1:brink:vh_fhbentry }
```

### demo.sitemap

```perl
sitemap demo label="Main Menu" {
    Frame label="Boiler" {
        Text item="RoomTemperature" icon="temperature" label="Room Temperature [%.1f %unit%]"
        Text item="RoomSetpoint" icon="temperature" label="Room Setpoint [%.1f %unit%]"
        Text item="TemporaryRoomSetpointOverride" icon="temperature" label="Temporary Room Setpoint Override [%.1f %unit%]" minValue="0" maxValue="30" step="0.1"
        Text item="ConstantRoomSetpointOverride" icon="temperature" label="Constant Room Setpoint Override [%.1f %unit%]" minValue="0" maxValue="30" step="0.1"
        Text item="ControlSetpoint" icon="temperature" label="Control Setpoint [%.1f %unit%]"
        Text item="ControlSetpointRequested" icon="temperature" label="Control Setpoint Requested [%.1f %unit%]"
        Text item="ControlSetpointOverride" icon="temperature" label="Control Setpoint Override [%.1f %unit%]" minValue="0" maxValue="100" step="0.1"
        Text item="ControlSetpoint2" icon="temperature" label="Control Setpoint 2 [%.1f %unit%]"
        Text item="ControlSetpoint2Requested" icon="temperature" label="Control Setpoint 2 Requested [%.1f %unit%]"
        Text item="ControlSetpoint2Override" icon="temperature" label="Control Setpoint 2 Override [%.1f %unit%]" minValue="0" maxValue="100" step="0.1"
        Text item="DomesticHotWaterTemperature" icon="temperature" label="Domestic Hot Water Temperature [%.1f %unit%]"
        Text item="DomesticHotWaterSetpoint" icon="temperature" label="Domestic Hot Water Setpoint [%.1f %unit%]"
        Text item="DomesticHotWaterSetpointOverride" icon="temperature" label="Domestic Hot Water Setpoint Override [%.1f %unit%]" minValue="0" maxValue="100" step="0.1"
        Text item="BoilerWaterTemperature" icon="temperature" label="Boiler Water Temperature [%.1f %unit%]"
        Text item="ReturnWaterTemperature" icon="temperature" label="Return Water Temperature [%.1f %unit%]"
        Text item="OutsideTemperature" icon="temperature" label="Outside Temperature [%.1f %unit%]" minValue="-40" maxValue="100" step="0.1"
        Text item="CentralHeatingWaterPressure" label="Central Heating Water Pressure [%.1f bar]"
        Switch item="CentralHeatingEnabled" icon="switch" label="Central Heating Enabled"
        Switch item="CentralHeatingEnabledThermostat" icon="switch" label="Central Heating Enabled Thermostat"
        Switch item="CentralHeatingOverridden" icon="switch" label="Central Heating Overridden"
        Switch item="CentralHeating2Enabled" icon="switch" label="Central Heating 2 Enabled"
        Switch item="CentralHeating2EnabledThermostat" icon="switch" label="Central Heating 2 Enabled Thermostat"
        Switch item="CentralHeating2Overridden" icon="switch" label="Central Heating 2 Overridden"
        Switch item="CentralHeatingActive" icon="switch" label="Central Heating Active"
        Switch item="DomesticHotWaterEnabled" icon="switch" label="Domestic Hot Water Enabled"
        Switch item="DomesticHotWaterActive" icon="switch" label="Domestic Hot Water Active"
        Switch item="BurnerActive" icon="switch" label="Burner Active"
        Text item="RelativeModulationLevel" label="Relative Modulation Level [%.1f %%]"
        Text item="MaximumRelativeModulationLevel" label="Maximum Relative Modulation Level [%.1f %%]"
        Switch item="FaultIndication" icon="switch" label="Fault Indication"
        Switch item="ServiceRequired" icon="switch" label="Service Required"
        Switch item="Lockout-ResetEnabled" icon="switch" label="Lockout-Reset Enabled"
        Switch item="LowWaterPressureFault" icon="switch" label="Low Water Pressure Fault"
        Switch item="GasOrFlameFault" icon="switch" label="Gas Or Flame Fault"
        Switch item="AirPressureFault" icon="switch" label="Air Pressure Fault"
        Switch item="WaterOver-TemperatureFault" icon="switch" label="Water Over-Temperature Fault"
        Text item="OEMFaultCode" label="OEM Fault Code"
        Text item="UnsuccessfulBurnerStarts" label="Unsuccessful Burner Starts"
        Text item="BurnerStarts" label="Burner Starts"
        Text item="CentralHeatingPumpStarts" label="Central Heating Pump Starts"
        Text item="DomesticHotWaterPump/ValveStarts" label="Domestic Hot Water Pump/Valve Starts"
        Text item="DomesticHotWaterBurnerStarts" label="Domestic Hot Water Burner Starts"
        Text item="BurnerHours" label="Burner Hours"
        Text item="CentralHeatingPumpHours" label="Central Heating Pump Hours"
        Text item="DomesticHotWaterPumpValveHours" label="Domestic Hot Water Pump/Valve Hours"
        Text item="DomesticHotWaterBurnerHours" label="Domestic Hot Water Burner Hours"
        Text item="TransparentSlaveParameterNumber" label="Transparent Slave Parameter Number"
        Text item="TransparentSlaveParameterEntry" label="Transparent Slave Parameter Entry"
        Text item="FaultHistoryBufferNumber" label="Fault History Buffer Number"
        Text item="FaultHistoryBufferEntry" label="Fault History Buffer Entry"
    }

    Frame label="Ventilation / Heat Recovery" {
        Switch item="VentilationEnabled" icon="switch" label="Ventilation Enabled"
        Text item="BypassPosition" label="Bypass Position"
        Text item="BypassMode" label="Bypass Mode"
        Switch item="FreeVentilationMode" icon="switch" label="Free Ventilation Mode"
        Switch item="FaultIndication" icon="switch" label="Fault Indication"
        Switch item="VentilationMode" icon="switch" label="Ventilation Mode"
        Switch item="BypassStatus" icon="switch" label="Bypass Status"
        Text item="BypassAutomaticStatus" label="Bypass Automatic Status"
        Switch item="FreeVentilationStatus" icon="switch" label="Free Ventilation Status"
        Switch item="FilterCheck" icon="switch" label="Filter Check"
        Switch item="DiagnosticIndication" icon="switch" label="Diagnostic Indication"
        Text item="ControlSetpoint" label="Control Setpoint"
        Switch item="ServiceRequest" icon="switch" label="Service Request"
        Switch item="ExhaustFanFault" icon="switch" label="Exhaust Fan Fault"
        Switch item="InletFanFault" icon="switch" label="Inlet Fan Fault"
        Switch item="FrostProtection" icon="switch" label="Frost Protection"
        Text item="FaultCode" label="Fault Code"
        Text item="DiagnosticCode" label="Diagnostic Code"
        Text item="SystemType" label="System Type"
        Switch item="Bypass" icon="switch" label="Bypass"
        Text item="SpeedControl" label="Speed Control"
        Text item="MemberID" label="Member ID"
        Text item="OpenThermVersion" label="OpenTherm Version"
        Text item="VersionType" label="Version Type"
        Text item="RelativeVentilation" label="Relative Ventilation [%d %%]"
        Text item="RelativeHumidity" label="Relative Humidity [%d %%]"
        Text item="CO2Level" label="CO2 Level [%d ppm]"
        Text item="SupplyInletTemperature" label="Supply Inlet Temperature [%.1f %unit%]"
        Text item="SupplyOutletTemperature" label="Supply Outlet Temperature [%.1f %unit%]"
        Text item="ExhaustInletTemperature" label="Exhaust Inlet Temperature [%.1f %unit%]"
        Text item="ExhaustOutletTemperature" label="Exhaust Outlet Temperature [%.1f %unit%]"
        Text item="ActualExhaustFanSpeed" label="Actual Exhaust Fan Speed [%d rpm]"
        Text item="ActualInletFanSpeed" label="Actual Inlet Fan Speed [%d rpm]"
        Switch item="NominalVentilationValueTransfer" icon="switch" label="Nominal Ventilation Value Transfer"
        Text item="NominalVentilationValue" label="Nominal Ventilation Value"
        Text item="NominalVentilationValue" label="Nominal Ventilation Value [%d %%]"
        Text item="VentilationSetpoint" label="Ventilation Setpoint" minValue="0" maxValue="100" step="1"
        Text item="TransparentSlaveParameterNumber" label="Transparent Slave Parameter Number"
        Text item="TransparentSlaveParameterEntry" label="Transparent Slave Parameter Entry"
        Text item="FaultHistoryBufferNumber" label="Fault History Buffer Number"
        Text item="FaultHistoryBufferEntry" label="Fault History Buffer Entry"
    }
 }
```

## Migration from Prior Versions of the Binding

Between openHAB v3.2 and v3.3 the structure of Things and Channels was changed.
This means that Things and their respective Channels and Items that were created on v3.2 or earlier will no longer function on version v3.3 or later.
To be specific the change is as follows..

- **openHAB Versions v3.2 or earlier**: There was just one single `otgw` Thing that combined the functions that communicate with the OpenTherm Gateway together with the functions of the Channels that monitor/control the connected Boiler.

- **openHAB Versions v3.3 or later**: The communication functions have been moved to a new `openthermgateway` Bridge Thing. The connected Boiler functions have been moved to a new `boiler` Thing. And in addition (if needed) new functions for a connected Ventilation / Heat-Recovery system have been added in a new `ventilationheatrecovery` Thing.

So if you upgrade your system from openHAB v3.2 (or lower) to v3.3 (or higher), then your Thing and Item definitions must be migrated as shown below..

- Divide the contents of your old `openthermgateway:otgw:yourGatewayId` Thing definition into two new parts, namely 1) a new `openthermgateway:openthermgateway:yourGatewayId` Bridge definition for the OpenTherm Gateway, and 2) a new `openthermgateway:boiler:yourGatewayId:yourBoilerId` Thing definition for the connected Boiler.

- Change the `channel=".."` configuration entries of all your Items from referring to the ThingUID of the old `otgw` Thing to refer instead to the ThingUID of the respective newly created `boiler` Thing.

### Old Thing Definition and respective Item Definition (example)

```java
Thing openthermgateway:otgw:yourGatewayId [ ipaddress="192.168.1.100", port=8000, connectionRetryInterval=60 ]

e.g.
Number:Temperature Boiler_DHW_Temperature "Boiler DHW Temperature [%.1f %unit%]" <temperature> {channel="openthermgateway:otgw:yourGatewayId:dhwtemp"}
&c.
```

### New Thing Definition and respective and Item Definition (example)

```java
Bridge openthermgateway:openthermgateway:yourGatewayId "OpenTherm Gateway" @ "Kitchen" [ipaddress="192.168.1.100", port=20108, connectionRetryInterval=60] {
    Thing boiler remeha "Boiler" @ "Kitchen"
}

e.g.
Number:Temperature Boiler_DHW_Temperature "Boiler DHW Temperature [%.1f %unit%]" <temperature> {channel="openthermgateway:boiler:yourGatewayId:remeha:dhwtemp"}
&c.
```
