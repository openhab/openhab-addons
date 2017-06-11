# Xiaomi Robot Vacuum Binding

This Binding is used to control a Xiaomi Robot Vacuum.

## Supported Things

This Binding supports Xiaomi Robot Vacuum devices.

## Discovery

The binding needs a token from the Xiaomi Robot Vacuum in order to be able to control it.
To obtain this token, Reset the vacuum (the reset button is under the dust compartment cover)
Than trigger the discover

## Binding Configuration

To use the binding the token needs to be retreived from the device.

## Thing Configuration

The thing mandatory configuration is the IP & tokenID.
Optional configuration is the refresh interval.

## Channels

- **windSpeed** Measured current wind speed 
- **windDirection** Current wind direction
- **powerAbsTot** Total power
- **powerAbsWd** Power provided for your wind shares
- **powerRel** Relative power
- **kwh** Current energy
- **kwhForecast** Energy forecast
- **runPercentage** Run percentage this year
- **timestamp** Timestamp of the last update


## Full example

```
Group   gVaccum "Xiaomi Robot Vacuum"   <fan>

Number  ReigerWindSpeed         "Windsnelheid [%1.0f Bft]"        <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:windSpeed")
String  ReigerWindDirection     "Windrichting [%s]"               <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:windDirection")
Number  ReigerPowerAbsTot       "Productie molen [%1.0f kW]"      <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:powerAbsTot")
Number  ReigerPowerAbsWd        "WD power [%1.0f W]"              <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:powerAbsWd")
Number  ReigerPowerRel          "Productie vermogen [%1.0f %%]"   <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:powerRel")
Number  ReigerKwh               "kwh [%1.0f]"                     <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:kwh")
Number  ReigerKwhForecast       "Productie forecast [%1.0f]"      <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:kwhForecast")
Number  ReigerRunPercentage     "Run percentage [%1.0f %%]"       <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:runPercentage")
Number  ReigerTimestamp         "Update timestamp [%1$ta %1$tR]"  <wind>    (gReiger) {channel="xiaomivacuum:mill:reiger:timestamp")

```
