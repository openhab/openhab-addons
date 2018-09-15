# OpenTherm Gateway Binding

This binding is used to integrate the OpenTherm Gateway into OpenHAB2. The OpenTherm Gateway is a module designed by Schelte Bron that is connected in between a boiler and a thermostat that communicates using the OpenTherm protocol.

This binding is still under development. Please visit https://github.com/ArjenKorevaar/openhab2-openthermgateway-binary to download the lastest compiled test version.

More info on the OpenTherm Gateway can be found at http://otgw.tclcode.com/

## Supported Things

The OpenTherm Gateway binding currently only supports one thing, and that's the gateway itself.

## Discovery

The binding supports auto discovery.

## Binding Configuration

The binding itself does not require any configuration.

## Thing Configuration

The binding is designed to support various ways of connecting to the OpenTherm Gateway, but currently only supports a TCP socket connection. The configuration settings for the thing are Hostname and Port, which are used to open the socket to the gateway.

Future types of connections may require other settings, such as a COM port.

## Channels

The thing currently supports the follow channels:

![](Screenshot.png)

## Full Example

### demo.things:

```
Thing openthermgateway:main:1 [ipaddress="192.168.1.100", port="8000"]
```

### demo.items:

```
Number:Temperature RoomTemperature {channel="openthermgateway:main:1:roomtemp"}
Number:Temperature RoomSetpoint {channel="openthermgateway:main:1:roomsetpoint"}
Number:Temperature BoilerWaterTemperature {channel="openthermgateway:main:1:flowtemp"}
Number:Temperature ReturnWaterTemperature {channel="openthermgateway:main:1:returntemp"}
Number:Temperature OutsideTemperature {channel="openthermgateway:main:1:outsidetemp"}
Switch CentralHeatingEnabled {channel="openthermgateway:main:1:ch_enable"}
Switch CentralHeatingActive {channel="openthermgateway:main:1:ch_mode"}
Switch DomesticHotWaterEnabled {channel="openthermgateway:main:1:dhw_enable"}
Switch DomesticHotWaterActive {channel="openthermgateway:main:1:dhw_mode"}
Switch BurnerActive {channel="openthermgateway:main:1:flame"}
Number RelativeModulationLevel {channel="openthermgateway:main:1:modulevel"}
Number MaximumRelativeModulationLevel {channel="openthermgateway:main:1:maxrelmdulevel"}
```

### demo.sitemap:

```
sitemap demo label="Main Menu" {
    Frame label="OpenTherm Gateway" {
        Text item=RoomTemperature icon="temperature" label="Room temperature [%.1f °C]"        
        Setpoint item=RoomSetpoint minValue=0.0 maxValue=99.0 step=0.5 icon="heating" label="Room setpoint [%.1f °C]"
        Text item=BoilerWaterTemperature icon="temperature" label="Boiler water temperature [%.1f °C]"
        Text item=ReturnWaterTemperature icon="temperature" label="Return water temperature [%.1f °C]"
        Setpoint item=OutsideTemperature minValue=0.0 maxValue=99.0 step=0.5 icon="temperature" label="Outside temperature [%.1f °C]"
        
        Switch item=CentralHeatingEnabled icon="radiator" label="Central heating enabled"
        Switch item=CentralHeatingActive icon="radiator" label="Central heating active"
        Switch item=DomesticHotWaterEnabled icon="faucet" label="Domestic hot water enabled"
        Switch item=DomesticHotWaterActive icon="faucet" label="Domestic hot water active"
        Switch item=BurnerActive icon="fire" label="Burner active"
        
        Text item=RelativeModulationLevel icon="heating" label="Relative modulation level"
        Text item=MaximumRelativeModulationLevel icon="heating" label="Maximum relative modulation level"
    }
}
```
