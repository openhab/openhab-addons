# OpenTherm Gateway Binding

This binding is used to integrate the OpenTherm Gateway into openHAB.
The OpenTherm Gateway is a module designed by Schelte Bron that is connected in between a boiler and a thermostat and communicates using the OpenTherm protocol.

More information on the OpenTherm Gateway device can be found at http://otgw.tclcode.com/

## Supported Things

The OpenTherm Gateway binding currently only supports one thing, and that's the gateway itself.

## Discovery

The binding does not support auto discovery.

## Binding Configuration

The binding itself does not require any configuration.

## Thing Configuration

The binding is designed to support various ways of connecting to the OpenTherm Gateway device, but currently only supports a TCP socket connection.
The configuration settings for the thing are Hostname/IP address and Port, which are used to connect to the gateway, and an automatic connection retry interval in case the connection to the OpenTherm Gateway device is lost.

| Parameter                 | Name                      | Description                                                     | Required | Default |
|---------------------------|---------------------------|-----------------------------------------------------------------|----------|---------|
| `ipaddress`               | Hostname or IP address    | The hostname or IP address to connect to the OpenTherm Gateway. | yes      |         |
| `port`                    | Port                      | The port used to connect to the OpenTherm Gateway.              | yes      |         |
| `connectionRetryInterval` | Connection Retry Interval | The interval in seconds to retry connecting (0 = disabled).     | yes      | 60      |

## Channels

The OpenTherm Gateway binding supports the following channels:

| Channel Type ID           | Item Type            | Description                                              | Access |
|---------------------------|----------------------|----------------------------------------------------------|--------|
| roomtemp                  | Number:Temperature   | Current sensed room temperature                          | R      |
| roomsetpoint              | Number:Temperature   | Current room temperature setpoint                        | R      |
| temperaturetemporary      | Number:Temperature   | Temporary override room temperature setpoint             | R/W    |
| temperatureconstant       | Number:Temperature   | Constant override room temperature setpoint              | R/W    |
| controlsetpoint           | Number:Temperature   | Central heating water setpoint set at boiler             | R      |
| controlsetpointrequested  | Number:Temperature   | Central heating water setpoint requested by thermostat   | R      |
| controlsetpointoverride   | Number:Temperature   | Central heating water setpoint configured at gateway     | R/W    |
| controlsetpoint2          | Number:Temperature   | Central heating 2 water setpoint set at boiler           | R      |
| controlsetpoint2requested | Number:Temperature   | Central heating 2 water setpoint requested by thermostat | R      |
| controlsetpoint2override  | Number:Temperature   | Central heating 2 water setpoint configured at gateway   | R/W    |
| dhwtemp                   | Number:Temperature   | Domestic hot water temperature                           | R      |
| tdhwset                   | Number:Temperature   | Domestic hot water temperature setpoint                  | R      |
| overridedhwsetpoint       | Number:Temperature   | Domestic hot water temperature setpoint override         | R/W    |
| flowtemp                  | Number:Temperature   | Boiler water temperature                                 | R      |
| returntemp                | Number:Temperature   | Return water temperature                                 | R      |
| outsidetemp               | Number:Temperature   | Outside temperature                                      | R/W    |
| waterpressure             | Number:Pressure      | Central heating water pressure                           | R      |
| ch_enable                 | Switch               | Central heating enabled set at boiler                    | R      |
| ch_enablerequested        | Switch               | Central heating enabled requested by thermostat          | R      |
| ch_enableoverride         | Switch               | Central heating enabled overridden at gateway            | R      |
| ch2_enable                | Switch               | Central heating 2 enabled set at boiler                  | R      |
| ch2_enablerequested       | Switch               | Central heating 2 enabled requested by thermostat        | R      |
| ch2_enableoverride        | Switch               | Central heating 2 enabled overridden at gateway          | R      |
| ch_mode                   | Switch               | Central heating active                                   | R      |
| dhw_enable                | Switch               | Domestic hot water enabled                               | R      |
| dhw_mode                  | Switch               | Domestic hot water active                                | R      |
| flame                     | Switch               | Burner active                                            | R      |
| modulevel                 | Number:Dimensionless | Relative modulation level                                | R      |
| maxrelmdulevel            | Number:Dimensionless | Maximum relative modulation level                        | R      |
| fault                     | Switch               | Fault indication                                         | R      |
| servicerequest            | Switch               | Service required                                         | R      |
| lockout-reset             | Switch               | Lockout-reset enabled                                    | R      |
| lowwaterpress             | Switch               | Low water pressure fault                                 | R      |
| gasflamefault             | Switch               | Gas or flame fault                                       | R      |
| airpressfault             | Switch               | Air pressure fault                                       | R      |
| waterovtemp               | Switch               | Water over-temperature fault                             | R      |
| oemfaultcode              | Switch               | OEM fault code                                           | R      |
| diag                      | Switch               | Diagr / wstics indication                                | R      |
| sendcommand               | Text                 | Channel to send commands to the OpenTherm Gateway device | W      |

## Full Example

### demo.things

```
Thing openthermgateway:otgw:1 [ ipaddress="192.168.1.100", port=8000, connectionRetryInterval=60 ]
```

### demo.items

```
Number:Temperature RoomTemperature "Room temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:roomtemp" }
Number:Temperature RoomSetpoint "Room setpoint [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:roomsetpoint" }
Number:Temperature TemporaryRoomSetpointOverride "Temporary room setpoint override [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:temperaturetemporary" }
Number:Temperature ConstantRoomSetpointOverride "Constant room setpoint override [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:temperatureconstant" }
Number:Temperature ControlSetpoint "Control setpoint [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:controlsetpoint" }
Number:Temperature ControlSetpointRequested "Control setpoint requested [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:controlsetpointrequested" }
Number:Temperature ControlSetpointOverride "Control setpoint override [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:controlsetpointoverride" }
Number:Temperature ControlSetpoint2 "Control setpoint 2 [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:controlsetpoint2" }
Number:Temperature ControlSetpoint2Requested "Control setpoint 2 requested [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:controlsetpoint2requested" }
Number:Temperature ControlSetpoint2Override "Control setpoint 2 override [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:controlsetpoint2override" }
Number:Temperature DomesticHotWaterTemperature "Domestic hot water temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:dhwtemp" }
Number:Temperature DomesticHotWaterSetpoint "Domestic hot water setpoint [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:tdhwset" }
Number:Temperature DomesticHotWaterSetpointOverride "Domestic hot water setpoint override [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:overridedhwsetpoint" }
Number:Temperature BoilerWaterTemperature "Boiler water temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:flowtemp" }
Number:Temperature ReturnWaterTemperature "Return water temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:returntemp" }
Number:Temperature OutsideTemperature "Outside temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:outsidetemp" }
Number:Pressure CentralHeatingWaterPressure "Central heating water pressure [%.1f bar]" { channel="openthermgateway:otgw:1:waterpressure" }
Switch CentralHeatingEnabled "Central heating enabled" <switch> { channel="openthermgateway:otgw:1:ch_enable" }
Switch CentralHeatingEnabledRequested "Central heating enabled requested" <switch> { channel="openthermgateway:otgw:1:ch_enablerequested" }
Switch CentralHeatingEnabledOverride "Central heating enabled override" <switch> { channel="openthermgateway:otgw:1:ch_enableoverride" }
Switch CentralHeating2Enabled "Central heating 2 enabled" <switch> { channel="openthermgateway:otgw:1:ch2_enable" }
Switch CentralHeating2EnabledRequested "Central 2 heating enabled requested" <switch> { channel="openthermgateway:otgw:1:ch2_enablerequested" }
Switch CentralHeating2EnabledOverride "Central 2 heating enabled override" <switch> { channel="openthermgateway:otgw:1:ch2_enableoverride" }
Switch CentralHeatingActive "Central heating active" <switch> { channel="openthermgateway:otgw:1:ch_mode" }
Switch DomesticHotWaterEnabled "Domestic hot water enabled" <switch> { channel="openthermgateway:otgw:1:dhw_enable" }
Switch DomesticHotWaterActive "Domestic hot water active" <switch> { channel="openthermgateway:otgw:1:dhw_mode" }
Switch BurnerActive "Burner active" <switch> { channel="openthermgateway:otgw:1:flame" }
Number:Dimensionless RelativeModulationLevel "Relative modulation level [%.1f %%]" { channel="openthermgateway:otgw:1:modulevel" }
Number:Dimensionless MaximumRelativeModulationLevel "Maximum relative modulation level [%.1f %%]" { channel="openthermgateway:otgw:1:maxrelmdulevel" }
Switch Fault "Fault indication" <switch> { channel="openthermgateway:otgw:1:fault" }
Switch ServiceRequest "Service required" <switch> { channel="openthermgateway:otgw:1:servicerequest" }
Switch LockoutReset "Lockout-reset" <switch> { channel="openthermgateway:otgw:1:lockout-reset" }
Switch LowWaterPress "Low water pressure fault" <switch> { channel="openthermgateway:otgw:1:lowwaterpress" }
Switch GasFlameFault "Gas or flame fault" <switch> { channel="openthermgateway:otgw:1:gasflamefault" }
Switch AirPressFault "Air pressure fault" <switch> { channel="openthermgateway:otgw:1:airpressfault" }
Switch WaterOvTemp "Water over-temperature fault" <switch> { channel="openthermgateway:otgw:1:waterovtemp" }
Number OemFaultCode "OEM fault code" { channel="openthermgateway:otgw:1:oemfaultcode" }
Switch Diagnostics "Diagnostics indication" { channel="openthermgateway:otgw:1:diag" }
Text SendCommand "Send command channel" { channel="openthermgateway:otgw:1:sendcommand" }
```

### demo.sitemap

```
sitemap demo label="Main Menu" {
    Frame label="OpenTherm Gateway" {
        Text item="RoomTemperature" icon="temperature" label="Room temperature [%.1f °C]"
        Text item="RoomSetpoint" icon="temperature" label="Room setpoint [%.1f °C]"
        Setpoint item="TemporaryRoomSetpointOverride" icon="temperature" label="Temporary room setpoint override [%.1f °C]" minValue="0" maxValue="30" step="0.1"
        Setpoint item="ConstantRoomSetpointOverride" icon="temperature" label="Constant room setpoint override [%.1f °C]" minValue="0" maxValue="30" step="0.1"
        Text item="ControlSetpoint" icon="temperature" label="Control setpoint [%.1f °C]"
        Text item="ControlSetpointRequested" icon="temperature" label="Control setpoint requested [%.1f °C]"
        Setpoint item="ControlSetpointOverride" icon="temperature" label="Control setpoint override [%.1f °C]" minValue="0" maxValue="100" step="1"
        Text item="DomesticHotWaterTemperature" icon="temperature" label="Domestic hot water temperature [%.1f °C]"
        Text item="DomesticHotWaterSetpoint" icon="temperature" label="Domestic hot water setpoint [%.1f °C]"
        Setpoint item="DomesticHotWaterSetpointOverride" icon="temperature" label="Domestic hot water setpoint override [%.1f °C]" minValue="0" maxValue="100" step="0.1"
        Text item="BoilerWaterTemperature" icon="temperature" label="Boiler water temperature [%.1f °C]"
        Text item="ReturnWaterTemperature" icon="temperature" label="Return water temperature [%.1f °C]"
        Setpoint item="OutsideTemperature" icon="temperature" label="Outside temperature [%.1f °C]" minValue="-40" maxValue="100" step="0.1"
        Text item="CentralHeatingWaterPressure" icon="" label="Central heating water pressure [%.1f bar]"
        Switch item="CentralHeatingEnabled" icon="switch" label="Central heating enabled"
        Switch item="CentralHeatingEnabledRequested" icon="switch" label="Central heating enabled requested"
        Switch item="CentralHeatingEnabledOverride" icon="switch" label="Central heating enabled override"
        Switch item="CentralHeatingActive" icon="switch" label="Central heating active"
        Switch item="DomesticHotWaterEnabled" icon="switch" label="Domestic hot water enabled"
        Switch item="DomesticHotWaterActive" icon="switch" label="Domestic hot water active"
        Switch item="BurnerActive" icon="switch" label="Burner active"
        Text item="RelativeModulationLevel" icon="" label="Relative modulation level [%.1f %%]"
        Text item="MaximumRelativeModulationLevel" icon="" label="Maximum relative modulation level [%.1f %%]"        
        Switch item="Fault" icon="" label="Fault indication"
        Switch item="ServiceRequest" icon="" label="Service required"
        Switch item="LockoutReset" icon="" label="Lockout-reset"
        Switch item="LowWaterPress" icon="" label="Low water pressure fault"
        Switch item="GasFlameFault" icon="" label="Gas or flame fault"
        Switch item="AirPressFault" icon="" label="Air pressure fault"
        Switch item="waterOvTemp" icon="" label="Water over-temperature fault"
        Text item="OemFaultCode" icon="" label="OEM fault code"
        Switch item="Diagnostics" icon="" label="Diagnostics indication"
    }
}
```
