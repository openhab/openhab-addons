# OpenTherm Gateway binding

This binding is used to integrate the OpenTherm Gateway into openHAB2. The OpenTherm Gateway is a module designed by Schelte Bron that is connected in between a boiler and a thermostat that communicates using the OpenTherm protocol.

More information on the OpenTherm Gateway can be found at http://otgw.tclcode.com/

This binding is still under development. Please visit https://github.com/ArjenKorevaar/openhab2-openthermgateway-binary to download the lastest compiled test version.

Comments can be submitted to the topic on openHAB community at https://community.openhab.org/t/39160

## Supported Things

The OpenTherm Gateway binding currently only supports one thing, and that's the gateway itself.

## Discovery

The binding does not support auto discovery.

## Binding Configuration

The binding itself does not require any configuration.

## Thing Configuration

The binding is designed to support various ways of connecting to the OpenTherm Gateway device, but currently only supports a TCP socket connection. The configuration settings for the thing are Hostname/IP address and Port, which are used to connect to the gateway, and an automatic connection retry interval in case the connection to the OpenTherm Gateway device is lost.

## Channels

The OpenTherm Gateway binding supports the following channels:

- Room temperature
- Room setpoint
- Temporary room setpoint override
- Constant room setpoint override
- Control setpoint
- Domestic hot water temperature
- Domestic hot water setpoint
- Domestic hot water setpoint override
- Boiler water temperature
- Return water temperature
- Outside temperature
- Central heating water pressure
- Central heating enabled
- Central heating active
- Domestic hot water enabled
- Domestic hot water active
- Burner active
- Relative modulation level
- Maximum relative modulation level
- Fault indication
- Service required
- Lockout-reset
- Low water pressure fault
- Gas/flame fault
- Air pressure fault
- Water over-temperature fault
- OEM fault code
- Send command channel


## Full Example

### demo.things

```
Thing openthermgateway:otgw:1 [ipaddress="192.168.1.100", port="8000"]
```

### demo.items

```
Number RoomTemperature "Room temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:roomtemp" }
Number RoomSetpoint "Room setpoint [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:roomsetpoint" }
Number TemporaryRoomSetpointOverride "Temporary room setpoint override [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:temperaturetemporary" }
Number ConstantRoomSetpointOverride "Constant room setpoint override [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:temperatureconstant" }
Number ControlSetpoint "Control setpoint [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:controlsetpoint" }
Number DomesticHotWaterTemperature "Domestic hot water temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:dhwtemp" }
Number DomesticHotWaterSetpoint "Domestic hot water setpoint [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:tdhwset" }
Number DomesticHotWaterSetpointOverride "Domestic hot water setpoint override [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:overridedhwsetpoint" }
Number BoilerWaterTemperature "Boiler water temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:flowtemp" }
Number ReturnWaterTemperature "Return water temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:returntemp" }
Number OutsideTemperature "Outside temperature [%.1f °C]" <temperature> { channel="openthermgateway:otgw:1:outsidetemp" }
Number CentralHeatingWaterPressure "Central heating water pressure [%.1f bar]" { channel="openthermgateway:otgw:1:waterpressure" }
Switch CentralHeatingEnabled "Central heating enabled" <switch> { channel="openthermgateway:otgw:1:ch_enable" }
Switch CentralHeatingActive "Central heating active" <switch> { channel="openthermgateway:otgw:1:ch_mode" }
Switch DomesticHotWaterEnabled "Domestic hot water enabled" <switch> { channel="openthermgateway:otgw:1:dhw_enable" }
Switch DomesticHotWaterActive "Domestic hot water active" <switch> { channel="openthermgateway:otgw:1:dhw_mode" }
Switch BurnerActive "Burner active" <switch> { channel="openthermgateway:otgw:1:flame" }
Number RelativeModulationLevel "Relative modulation level [%.1f %%]" { channel="openthermgateway:otgw:1:modulevel" }
Number MaximumRelativeModulationLevel "Maximum relative modulation level [%.1f %%]" { channel="openthermgateway:otgw:1:maxrelmdulevel" }
Switch Fault "Fault indication" <switch> { channel="openthermgateway:otgw:1:fault" }
Switch ServiceRequest "Service required" <switch> { channel="openthermgateway:otgw:1:servicerequest" }
Switch LockoutReset "Lockout-reset" <switch> { channel="openthermgateway:otgw:1:lockout-reset" }
Switch LowWaterPress "Low water pressure fault" <switch> { channel="openthermgateway:otgw:1:lowwaterpress" }
Switch GasFlameFault "Gas or flame fault" <switch> { channel="openthermgateway:otgw:1:gasflamefault" }
Switch AirPressFault "Air pressure fault" <switch> { channel="openthermgateway:otgw:1:airpressfault" }
Switch waterOvTemp "Water over-temperature fault" <switch> { channel="openthermgateway:otgw:1:waterovtemp" }
Number OemFaultCode "OEM fault code" { channel="openthermgateway:otgw:1:oemfaultcode" }
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
        Text item="DomesticHotWaterTemperature" icon="temperature" label="Domestic hot water temperature [%.1f °C]"
        Text item="DomesticHotWaterSetpoint" icon="temperature" label="Domestic hot water setpoint [%.1f °C]"
        Setpoint item="DomesticHotWaterSetpointOverride" icon="temperature" label="Domestic hot water setpoint override [%.1f °C]" minValue="0" maxValue="100" step="0.1"
        Text item="BoilerWaterTemperature" icon="temperature" label="Boiler water temperature [%.1f °C]"
        Text item="ReturnWaterTemperature" icon="temperature" label="Return water temperature [%.1f °C]"
        Setpoint item="OutsideTemperature" icon="temperature" label="Outside temperature [%.1f °C]" minValue="-40" maxValue="100" step="0.1"
        Text item="CentralHeatingWaterPressure" icon="" label="Central heating water pressure [%.1f bar]"
        Switch item="CentralHeatingEnabled" icon="switch" label="Central heating enabled"
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
    }
}

```
