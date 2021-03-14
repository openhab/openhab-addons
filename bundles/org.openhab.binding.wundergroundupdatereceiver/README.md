# Wunderground Update Receiver Binding

Many personal weather stations are only capable of submitting measurements to the wunderground.com update site.

This binding enables acting as a receiver of updates from weather stations that post measurements
to https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php. If the hostname is configurable - as on
weather stations based on the Fine Offset Electronics WH2600-IP - this is simple, otherwise you have to set up dns
such that it resolves the above hostname to your server, without preventing the server from resolving the proper ip if
you want to forward the request.

The server thus listens at http(s)://<your-openHAB-server>:<openHAB-port>/weatherstation/updateweatherstation.php and the weather station
needs to be pointed at this address. If you can't configure the weather station itself to submit to an alternate hostname you would need
to set up a dns server that resolves rtupdate.wunderground.com to the IP-address of your server and provide as dns to the weather station
does DHCP. Make sure not to use this dns server instance for any other DHCP clients. 

The request is in itself simple to parse, so by redirecting it to your openhab server you can intercept the values and use them to
control items in your home. E.g. use measured wind-speed to close an awning or turn on the sprinkler system after some time without rain.
This binding allows you to mix and match products from various manufacturers that otherwise have a closed system.

If you wish to pass the measurements on to rtupdate.wunderground.com, you can use a simple rule that triggers on the
wundergroundupdatereceiver:wundergroundUpdateReceiver:<channel-id>:metadata#last-query-trigger to do so. It can also be used to
submit the same measurements to multiple weather services via multiple rules.

## Supported Things

Any weather station that sends weather measurement updates to the wunderground.com update URLs is supported.
It is easiest to use with stations that have a configurable target address, but can be made to
work with any internet-connected weather station, that gets it's dns server via DHCP or where the DNS server can be
set. 

## Discovery

In the initial version there is no discovery, but it is planned for a later release. This will also automatically 
generate channels base on the update request.  
In this version you need to manually add a thing and configure the station id, and only create items for channels the
weather station submits values for. E.g. the above-mentioned Fine Offset-based stations doesn't submit values for the averages over time.
These values can be calculated in rules if desirable and even appended to the wunderground submission if the other measurements
are forwarded to wunderground.com in a rule.

## Thing Configuration

The only configurable value is the station id, which should match the one configured on the weather station. If you don't plan on submitting
measurements to wunderground.com, it can be any unique non-empty string value.

## Channels

Each measurement type the wunderground.com update service accepts has a channel. The channels are named the same as the request parameters they receive.
Additionally there is a receipt timestamp and a trigger channel.

##### State channels:
The names match the possible parameters as documented at https://support.weather.com/s/article/PWS-Upload-Protocol?language=en_US
in addition to some undocumented ones that the WH2650 weather station does include.
   
| channel          | type                 | description                                                                                        |
|------------------|----------------------|----------------------------------------------------------------------------------------------------|
| lastReceived     | DateTime             | The date and time of the last update.                                                              |
| dateutc          | String               | The date and time of the last update in UTC as submitted by the weather station. This can be 'now' |
| softwaretype     | String               | A software type string from the weather station                                                    |
| lastQueryState   | String               | The part of the last query after the first unurlencoded ?                                          |   
| windspeedmph     | Number:Speed         | Current wind speed, using software specific time period                                            |
| winddir          | Number:Angle         | Current wind direction angle                                                                       |
| windgustmph      | Number:Speed         | Current wind gust speed                                                                            |
| windgustdir      | Number:Angle         | Wind gust direction angle                                                                          |
| windspdmph_avg2m | Number:Speed         | 2 minute average wind speed                                                                        |
| winddir_avg2m    | Number:Angle         | 2 minute average wind direction angle                                                              |
| windgustmph_10m  | Number:Speed         | 10 minute average gust speed                                                                       |
| windgustdir_10m  | Number:Angle         | 10 minute average gust direction angle                                                             |
| tempf            | Number:Temperature   | Current outdoor temperature                                                                        |
| indoortempf      | Number:Temperature   | Current indoor temperature                                                                         |
| soiltempf        | Number:Temperature   | Current soil temperature                                                                           |
| humidity         | Number:Dimensionless | Current humidity in percent                                                                        |
| indoorhumidity   | Number:Dimensionless | Current indoor humidity                                                                            |
| dewptf           | Number:Temperature   | Dew point                                                                                          |
| soilmoisture     | Number:Dimensionless | Soil moisture in percent                                                                           |
| leafwetness      | Number:Dimensionless | Leaf wetness in percent                                                                            |
| rainin           | Number:Length        | Rain over the past hour                                                                            |
| dailyrainin      | Number:Length        | Rain since the start of the day                                                                    |
| weeklyrainin     | Number:Length        | Rain since the start of the week                                                                   |
| monthlyrainin    | Number:Length        | Rain since the start of the month                                                                  |
| yearlyrainin     | Number:Length        | Rain since the start of the year                                                                   |
| weather          | String               | METAR formatted weather report                                                                     |
| clouds           | String               | METAR style cloud cover                                                                            |
| solarradiation   | Number:Intensity     | Solar radiation                                                                                    |
| uv               | Number:Dimensionless | UV index.                                                                                          |
| visibility       | Number:Length        | Visibility.                                                                                        |
| baromin          | Number:Pressure      | Outside barometric pressure                                                                        |


##### Advanced state channels:

| channel          | type                 | description                                                                                        |
|------------------|----------------------|----------------------------------------------------------------------------------------------------|
| AqNO             | Number:Dimensionless | Nitric Oxide ppm.                                                                                  |
| AqNO2T           | Number:Dimensionless | Nitrogen Dioxide, true measure ppb.                                                                |
| AqNO2            | Number:Dimensionless | NO2 computed, NOx-NO ppb.                                                                          |
| AqNO2Y           | Number:Dimensionless | NO2 computed, NOy-NO ppb.                                                                          |
| AqNOX            | Number:Dimensionless | Nitrogen Oxides ppb.                                                                               |
| AqNOY            | Number:Dimensionless | Total reactive nitrogen.                                                                           |
| AqNO3            | Number:Density       | NO3 ion (nitrate, not adjusted for ammonium ion) µG/m3.                                            |
| AqSO4            | Number:Density       | SO4 ion (sulfate, not adjusted for ammonium ion) µG/m3.                                            |
| AqSO2            | Number:Dimensionless | Sulfur Dioxide, conventional ppb.                                                                  |
| AqSO2T           | Number:Dimensionless | Sulfur Dioxide, trace levels ppb.                                                                  |
| AqCO             | Number:Dimensionless | Carbon Monoxide, conventional ppm.                                                                 |
| AqCOT            | Number:Dimensionless | Carbon Monoxide, trace levels ppb.                                                                 |
| AqEC             | Number:Density       | Elemental Carbon, PM2.5 µG/m3.                                                                     |
| AqOC             | Number:Density       | Organic Carbon, not adjusted for oxygen and hydrogen, PM2.5 µG/m3.                                 |
| AqBC             | Number:Density       | Black Carbon at 880 nm, µG/m3.                                                                     |
| AqUV-AETH        | Number:Density       | second channel of Aethalometer at 370 nm, µG/m3.                                                   |
| AqPM2_5          | Number:Density       | PM2.5 mass, µG/m3.                                                                                 |
| AqPM10           | Number:Density       | PM10 mass, µG/m3.                                                                                  |
| AqOZONE          | Number:Dimensionless | Ozone, ppb.                                                                                        |

The naming is meant to make the mapping of values by the weather station
unambigous for the user. 

##### Trigger channels:

| channel          | type                 | description                                                                                        |
|------------------|----------------------|----------------------------------------------------------------------------------------------------|
| lastQueryTrigger | String               | The part of the last query after the first unurlencoded ?                                          |

The trigger channel's payload is the last querystring, so the following dsl rule script
would send the measurements on to wunderground.com:

```
val requestQuery = receivedEvent
sendHttpGetRequest("https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php?" + requestQuery)
```

### Rule examples
You can use the trigger channel to create a rule to calculate additional values.
Create an new manual Item with a meaningful id, fx. WundergroundUpdateReceiverBinging_HeatIndex with a Number type.
Create a rule that triggers when the trigger channel is updated and the following DSL:

```
    if ( (WH2k6_OutdoorTemperature.state != NULL) && (WH2k6_OutdoorTemperature.state != UNDEF) &&
         (WH2k6_Humidity.state != NULL)           && (WH2k6_Humidity.state != UNDEF)
       )
    {
        val Double tempf    = (WH2k6_OutdoorTemperature.state as QuantityType<Number>).toUnit("°F").doubleValue
        val Double humidity = (WH2k6_Humidity.state           as QuantityType<Number>).toUnit("%").doubleValue

        // https://www.wpc.ncep.noaa.gov/html/heatindex_equation.shtml
        if ( tempf>=80.0 ) {
            var Double heatindex = -42.379 + 2.04901523*tempf + 10.14333127*humidity - 0.22475541*tempf*humidity - 0.00683783*tempf*tempf - 0.05481717*humidity*humidity + 0.00122874*tempf*tempf*humidity + 0.00085282*tempf*humidity*humidity - 0.00000199*tempf*tempf*humidity*humidity;

            if ( (humidity <= 13.0) && (tempf >= 80.0) && (tempf <= 112.0) ) {
                heatindex = heatindex - ((13.0 - humidity)/4.0) * (Math::sqrt(17.0-Math::abs(tempf-95.0)/17.0));
            } else if ( (humidity >= 85.0) && (tempf >= 80.0) && (tempf <= 87.0) )  {
                heatindex = heatindex + ((humidity - 85.0)/10.0)*((87.0-tempf)/5.0);
            }
            WundergroundUpdateReceiverBinging_HeatIndex.postUpdate(heatindex + "°F")
        } else {
            WundergroundUpdateReceiverBinging_HeatIndex.postUpdate(UNDEF)
        }
    }
```

You would then have to trigger another rule to submit the original request withany calculated values appended.

You can also define a transformation to fx. get a cardinal direction (N, S, W, E):
```
(function(s){ 
  if ( (s == "NULL") || (s == "UNDEF") )
  {
      return undefined;
  }
  else
  {
    var dir = ["N ⬆️", "NNO ⬆️", "NO ↗️", "ONO ➡️", "O ➡️", "OSO ➡️", "SO ↘️", "SSO ⬇️", "S ⬇️", "SSW ⬇️", "SW ↙️", "WSW ⬅️", "W ⬅️", "WNW ⬅️", "NW ↖️", "NNW ⬆️"];
    var wind          = parseInt(s.split(" ")[0]);
    var winddiroffset = (wind + (360.0/32.0)) % 360.0;
    var winddiridx    = Math.floor(winddiroffset / (360.0/16.0));
    var winddir       = dir[winddiridx];
    
    return winddir + ' ('+ wind +'°)';
  }
})(input)
```
The examples were kindly provided by MikeTheTux.

