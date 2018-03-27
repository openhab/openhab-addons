# Windcentrale Binding

This Binding is used to display the details of a Windcentrale windmill.

## Supported Things

This Binding supports Windcentrale mill devices.

## Discovery

There is no discovery available for this binding.

## Binding Configuration

No binding configuration required.

## Thing Configuration

The thing mandatory configuration is the selection of the mill.
Optional configuration is the number of wind shares ("Winddelen") and the refresh interval.

## Channels

-   **windSpeed** Measured current wind speed
-   **windDirection** Current wind direction
-   **powerAbsTot** Total power
-   **powerAbsWd** Power provided for your wind shares
-   **powerRel** Relative power
-   **kwh** Current energy
-   **kwhForecast** Energy forecast
-   **runPercentage** Run percentage this year
-   **timestamp** Timestamp of the last update


## Example

```
Group   gReiger "Windcentrale Reiger"   <wind>

Number  ReigerWindSpeed         "Windsnelheid [%1.0f Bft]"        <wind>    (gReiger) {channel="windcentrale:mill:reiger:windSpeed")
String  ReigerWindDirection     "Windrichting [%s]"               <wind>    (gReiger) {channel="windcentrale:mill:reiger:windDirection")
Number  ReigerPowerAbsTot       "Productie molen [%1.0f kW]"      <wind>    (gReiger) {channel="windcentrale:mill:reiger:powerAbsTot")
Number  ReigerPowerAbsWd        "WD power [%1.0f W]"              <wind>    (gReiger) {channel="windcentrale:mill:reiger:powerAbsWd")
Number  ReigerPowerRel          "Productie vermogen [%1.0f %%]"   <wind>    (gReiger) {channel="windcentrale:mill:reiger:powerRel")
Number  ReigerKwh               "kwh [%1.0f]"                     <wind>    (gReiger) {channel="windcentrale:mill:reiger:kwh")
Number  ReigerKwhForecast       "Productie forecast [%1.0f]"      <wind>    (gReiger) {channel="windcentrale:mill:reiger:kwhForecast")
Number  ReigerRunPercentage     "Run percentage [%1.0f %%]"       <wind>    (gReiger) {channel="windcentrale:mill:reiger:runPercentage")
Number  ReigerTimestamp         "Update timestamp [%1$ta %1$tR]"  <wind>    (gReiger) {channel="windcentrale:mill:reiger:timestamp")
```
