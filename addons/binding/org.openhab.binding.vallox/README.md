# Vallox Binding

Interface to a vallox central venting unit.

Tested with 
* Vallox KWL 90 SE and 
* USR-TCP232-410 RS485-to-ethernet interface

## Prerequisite

This Openhab2 bundle so far only works with a RS485-to-ethernet gateway.
It does not support a local serial interface of the device executing OH2.

I do not have any experience with direct serial RS485 communication under Java and
do not have any device for testing it. So if you can contribute any code for direct
RS485 serial write and read, I might easily merge that.

## Configuration

Configure the serial interface such that it acts as TCP server on some port of your choice. 
Configure the serial side as 
* baudrate: 9600
* data size: 8 bit
* parity: none
* stop bits: 1 

## Install

1. open paper ui in browser
2. Goto Configuration -> System and disable Item Linking Simple mode (so no items will be created automatically)
3. check that Configuration -> Bindings lists the Vallox binding
4. go to Configuration -> Things
5. add Vallox KWL Serial Binding Thing (exactly one)
6. add textual configuration (see below) or enter configuration parameters (host and port of serial gateway)
7. The thing should show status `ONLINE`; if not, go into the details of the thing. The status there should include some error message.
8. make sure you create items for all the many channels of the binding
  * see items example below
  * after adding explicit items to your items file, you need to link them to the vallox thing channels (see below for textual example)
  * managing the items manually allows you to correctly reference the items in rules and to group them in groups
9. make the items available in some sitemap and you should be able to control the vallox via the OH2 UIs
  * see example below

## Features

The binding currently supports the following Features
* 14 major properties, some read-only, some writeable
* 35 advanced properties, so far read-only most of them

Note that there might be more properties that can be read or written which might be not implemented yet. Contributions welcome.
Also not all channels are perfectly documented. See [thing-types.xml](ESH-INF/thing/thing-types.xml) for details.

* the binding will automatically detect loss of connection (at latest by a heartbeat poll to the vallox every minute)
* the binding will try to reconnect regularly until connection is re-established

## Examples

### vallox.things

It's easiest to define the thing textually and map the items textually to the thing's channels.
Parameters are
* host: Host name or IP address of RS485-to-Ethernet gateway. The gateway needs to be configured as TCP server and the serial connection to the vallox with 9600 baud, 8-N-1. Default: lwip
* port: TCP Port that the RS485-to-Ethernet Gateway listens to. Default: 26

```
Thing vallox.kwl90se.main [ host="lwip", port="26" ]
```

### vallox.items

You can use the following example to copy/paste the available items to your own item file

```
Group Vallox
Group ValloxAdvanced

Number FanSpeed (Vallox) { channel="vallox.kwl90se.main.FanSpeed" } 
Number FanSpeedMax (Vallox) { channel="vallox.kwl90se.main.FanSpeedMax" }
Number FanSpeedMin (Vallox) { channel="vallox.kwl90se.main.FanSpeedMin" }
Number TempInside (Vallox) { channel="vallox.kwl90se.main.TempInside" }
Number TempOutside (Vallox) { channel="vallox.kwl90se.main.TempOutside" }
Number TempExhaust (Vallox) { channel="vallox.kwl90se.main.TempExhaust" }
Number TempIncomming (Vallox) { channel="vallox.kwl90se.main.TempIncomming" }
Number InEfficiency (Vallox) { channel="vallox.kwl90se.main.InEfficiency" }
Number OutEfficiency  (Vallox) { channel="vallox.kwl90se.main.OutEfficiency" }
Number AverageEfficiency (Vallox) { channel="vallox.kwl90se.main.AverageEfficiency" }
Switch PowerState  (Vallox) { channel="vallox.kwl90se.main.PowerState" }
Number DCFanInputAdjustment (ValloxAdvanced) { channel="vallox.kwl90se.main.DCFanInputAdjustment" }
Number DCFanOutputAdjustment (ValloxAdvanced) { channel="vallox.kwl90se.main.DCFanOutputAdjustment" }
Number HrcBypassThreshold (ValloxAdvanced) { channel="vallox.kwl90se.main.HrcBypassThreshold" }
Number InputFanStopThreshold (ValloxAdvanced) { channel="vallox.kwl90se.main.InputFanStopThreshold" }
Number HeatingSetPoint (ValloxAdvanced) { channel="vallox.kwl90se.main.HeatingSetPoint" }
Number PreHeatingSetPoint (ValloxAdvanced) { channel="vallox.kwl90se.main.PreHeatingSetPoint" }
Number CellDefrostingThreshold (ValloxAdvanced) { channel="vallox.kwl90se.main.CellDefrostingThreshold" }
Switch CO2AdjustState (ValloxAdvanced) { channel="vallox.kwl90se.main.CO2AdjustState" }
Switch HumidityAdjustState (ValloxAdvanced) { channel="vallox.kwl90se.main.HumidityAdjustState" }
Switch HeatingState (ValloxAdvanced) { channel="vallox.kwl90se.main.HeatingState" }
Switch FilterGuardIndicator (ValloxAdvanced) { channel="vallox.kwl90se.main.FilterGuardIndicator" }
Switch HeatingIndicator (ValloxAdvanced) { channel="vallox.kwl90se.main.HeatingIndicator" }
Switch FaultIndicator (ValloxAdvanced) { channel="vallox.kwl90se.main.FaultIndicator" }
Switch ServiceReminderIndicator (ValloxAdvanced) { channel="vallox.kwl90se.main.ServiceReminderIndicator" }
Number Humidity (ValloxAdvanced) { channel="vallox.kwl90se.main.Humidity" }
Number BasicHumidityLevel (ValloxAdvanced) { channel="vallox.kwl90se.main.BasicHumidityLevel" }
Number HumiditySensor1 (ValloxAdvanced) { channel="vallox.kwl90se.main.HumiditySensor1" }
Number HumiditySensor2 (ValloxAdvanced) { channel="vallox.kwl90se.main.HumiditySensor2" }
Number CO2High (ValloxAdvanced) { channel="vallox.kwl90se.main.CO2High" }
Number CO2Low (ValloxAdvanced) { channel="vallox.kwl90se.main.CO2Low" }
Number CO2SetPointHigh (ValloxAdvanced) { channel="vallox.kwl90se.main.CO2SetPointHigh" }
Number CO2SetPointLow (ValloxAdvanced) { channel="vallox.kwl90se.main.CO2SetPointLow" }
Number AdjustmentIntervalMinutes (ValloxAdvanced) { channel="vallox.kwl90se.main.AdjustmentIntervalMinutes" }
Switch AutomaticHumidityLevelSeekerState (ValloxAdvanced) { channel="vallox.kwl90se.main.AutomaticHumidityLevelSeekerState" }
Switch BoostSwitchMode (ValloxAdvanced) { channel="vallox.kwl90se.main.BoostSwitchMode" }
Switch RadiatorType (ValloxAdvanced) { channel="vallox.kwl90se.main.RadiatorType" }
Switch CascadeAdjust (ValloxAdvanced) { channel="vallox.kwl90se.main.CascadeAdjust" }
Switch MaxSpeedLimitMode (ValloxAdvanced) { channel="vallox.kwl90se.main.MaxSpeedLimitMode" }
Number ServiceReminder (ValloxAdvanced) { channel="vallox.kwl90se.main.ServiceReminder" }
Switch PostHeatingOn (ValloxAdvanced) { channel="vallox.kwl90se.main.PostHeatingOn" }
Switch DamperMotorPosition (ValloxAdvanced) { channel="vallox.kwl90se.main.DamperMotorPosition" }
Switch FaultSignalRelayClosed (ValloxAdvanced) { channel="vallox.kwl90se.main.FaultSignalRelayClosed" }
Switch SupplyFanOff (ValloxAdvanced) { channel="vallox.kwl90se.main.SupplyFanOff" }
Switch PreHeatingOn (ValloxAdvanced) { channel="vallox.kwl90se.main.PreHeatingOn" }
Switch ExhaustFanOff (ValloxAdvanced) { channel="vallox.kwl90se.main.ExhaustFanOff" }
Switch FirePlaceBoosterClosed (ValloxAdvanced) { channel="vallox.kwl90se.main.FirePlaceBoosterClosed" }
Number IncommingCurrent (ValloxAdvanced) { channel="vallox.kwl90se.main.IncommingCurrent" }
Number LastErrorNumber (ValloxAdvanced) { channel="vallox.kwl90se.main.LastErrorNumber" }
```

### vallox.sitemap

See an example below for a sitemap that uses the vallox items. As there are quite many channels in the binding, you should use groups for main and advanced items (and maybe more). You can still address items directly.

```
sitemap demo label="Demo Sitemap" {
    
    Frame label="Vallox" {
        Setpoint item=FanSpeed minValue="1" maxValue="8" step="1"
        Setpoint item=FanSpeedMin minValue="1" maxValue="8" step="1"
        Setpoint item=FanSpeedMax minValue="1" maxValue="8" step="1"
    }
    Group item=Vallox
    Group item=ValloxAdvanced
}

```
