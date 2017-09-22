# CO2 Signal Binding

Inspired by Henrik Knibergs attempt to save the climate http://climate.crisp.se/ here is a a binding that tries to help reduce the CO2 footprint via OpenHab

This binding uses the [co2signal.com service](https://www.co2signal.com) for providing CO2 Signal information for any location worldwide.

By using electricity at the right time, your device favors low-carbon sources of electricity

The project is providing informantion on how clean the current electricity is produced, via those two websites: [co2signal.com](http://www.co2signal.com) and [www.electricitymap.org](https://www.electricitymap.org).

To use this binding, you first need to [register and get your API token](https://www.co2signal.com/).

Credits go to the authors of the airquality binding, most code was copied from there.

## Supported Things

There is exactly one supported thing type, which represents the CO2 Signal information for an observation location. It has the `co2signal` id. Of course, you can add multiple Things, e.g. for measuring co2 for different locations.

## Discovery

There is no discovery implemented. You have to create your things manually.

## Binding Configuration
 
The binding has no configuration options, all configuration is done at Thing level.
 
## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                              |
|-----------|------------------------------------------------------------------------- |
| apikey    | Data-platform token to access the co2signal.com service. Mandatory. |
| location  | Geo coordinates to be considered by the service. |
| countryCode | two-letter iso country code |
| refresh   | Refresh interval in minutes. Optional, the default value is 60 minutes.  |

You need to set either location or countryCode. If a location is set countryCode is ignored
For the location parameter, the following syntax is allowed (comma separated latitude and longitude):

```
37.8,-122.4
37.8255,-122.456
```

If you always want to receive data from specific station and you know its unique ID, you can enter it
instead of the coordinates. 


## Channels

The CO2 information that is retrieved is available as these channels:


| Channel ID | Item Type    | Description              |
|------------|--------------|------------------------- |
| countryCode | String | two-letter iso country code |
| carbonIntensity | Number | Carbon Intensity in gCO2eq/kWh |
| fossilFuelPercentage | Number | Fossil Fuel Percentage |


## Full Example


co2signal.things:

```
co2signal:co2signal:home "CO2Signal" @ "Krakow" [ apikey="XXXXXXXXXXXX", location="50.06465,19.94498", refresh=60 ]
co2signal:co2signal:warsaw "CO2Signal in Warsaw" [ apikey="XXXXXXXXXXXX", location="52.22,21.01", refresh=60 ]
co2signal:co2signal:germany "CO2Signal in Germany" [ apikey="XXXXXXXXXXXX", countryCode="de" ]
```

co2signal.items:

```
Group CO2Signal <energy>

Switch  co2_LowCO2Intensity "Low CO2 Level"
String   co2_CountryCode           "CO2 Signal Country Code" <energy> (CO2Signal) { channel="co2signal:co2signal:home:countryCode" }
Number   co2_CarbonIntensity           "CO2 Signal Carbon Intensity" <energy> (CO2Signal) { channel="co2signal:co2signal:home:carbonIntensity" }
Number   co2_FossilFuelPercentage      "CO2 Signal Fossil Fuel Percentage" <energy> (CO2Signal) { channel="co2signal:co2signal:home:fossilFuelPercentage" }
```

co2signal.sitemap:

```
sitemap co2signal label="CO2 Signal" {
    Frame {
        Text item=co2_CountryCode
        Text item=co2_CarbonIntensity
        Text item=co2_FossilFuelPercentage
        Text item=co2_LowCO2Intensity
    }
}
```

co2signal.rules:

```
rule "Switch Item on CO2 threshold"
when
    Item co2_CarbonIntensity changed
then
    if (co2_CarbonIntensity.state > 500) {
       sendCommand(co2_LowCO2Intensity, OFF)
    } else {
       sendCommand(co2_LowCO2Intensity, ON)
    }
end
```
