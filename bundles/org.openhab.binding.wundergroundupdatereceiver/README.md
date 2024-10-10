# Wunderground Update Receiver Binding

Many personal weather stations or similar devices are only capable of submitting measurements to the wunderground.com update site.

This binding enables acting as a receiver of updates from devices that post measurements to <https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php>.
If the hostname is configurable - as on weather stations based on the Fine Offset Electronics WH2600-IP - this is simple, otherwise you have to set up dns such that it resolves the above hostname to your server, without preventing the server from resolving the proper ip if you want to forward the request.

The server thus listens at `http(s)://<your-openHAB-server>:<openHAB-port>/weatherstation/updateweatherstation.php` and the device needs to be pointed at this address.
If you can't configure the device itself to submit to an alternate hostname you would need to set up a dns server that resolves rtupdate.wunderground.com to the IP-address of your server and provide it as the DHCP dns-server to the device.
Make sure not to use this dns server instance for any other DHCP clients.

The request is in itself simple to parse, so by redirecting it to your openHAB server you can intercept the values and use them to control items in your home.
E.g. use measured wind-speed to close an awning or turn on the sprinkler system after some time without rain.
This binding allows you to mix and match products from various manufacturers that otherwise have a closed system.

If you wish to pass the measurements on to rtupdate.wunderground.com, you can use a simple rule that triggers on the `wundergroundupdatereceiver:wundergroundUpdateReceiver:<channel-id>:metadata#last-query-trigger` to do so.
It can also be used to submit the same measurements to multiple weather services via multiple rules.

## Supported Things

Any device that sends weather measurement updates to the wunderground.com update URLs is supported.
Multiple devices submitting to the same wunderground account ID can be aggregated.
It is easiest to use with devices that have a configurable target address, but can be made to work with any internet-connected device, that gets its dns server via DHCP or where the DNS server can be set.

## Discovery

The binding starts listening at the above-mentioned URI as soon as it is initialized.
Any request with an unregistered stationId is recorded and if auto-discovery is enabled appears in the inbox, otherwise can be registered when a manual scan is initiated.
For each request parameter a channel is generated, based on a list of known parameters from <https://support.weather.com/s/article/PWS-Upload-Protocol?language=en_US> and other observed parameters from various devices.
If you have a device that submits a parameter that is unknown in the current version of the binding please feel free to submit an issue to have it added.

While discovery is active, either in the background or during a manual scan, any request parameters that don't have a channel associated with them are added to the thing's channels.
This supports using multiple devices that submit measurements to the same station ID.
The thing is the wunderground account, not the individual devices submitting measurements.

## Thing Configuration

The only configurable value is the station id, which should match the one configured on the device.
If you don't plan on submitting measurements to wunderground.com, it can be any unique non-empty string value, otherwise it must be the actual station ID.

## Channels

Each measurement type the wunderground.com update service accepts has a channel.
The channels must be named exactly as the request parameter they receive.
I.e. the wind speed channel must be named `windspeedmph` as that is the request parameter name defined by Wunderground in their API.
Illegal channel id characters are converted to -.
For example, AqPM2.5 has a channel named `AqPM2-5`.
The channel name set up in the binding should be considered an id with no semantic content other than pointing to the wunderground API.
Additionally there is a receipt timestamp and a trigger channel.

### Request parameters are mapped to one of the following channel-types

#### Normal channel-types

| Request parameter |  Channel type id             | Type                 | Label                          | Description                                                                            | Group       |
|-------------------|------------------------------|----------------------|--------------------------------|----------------------------------------------------------------------------------------|-------------|
| winddir           | wind-direction               | Number:Angle         | Current Wind Direction         | Current wind direction                                                                 | Wind        |
| windspeedmph      | wind-speed                   | Number:Speed         | Current Wind Speed             | Current wind speed, using software specific time period.                               | Wind        |
| windgustmph       | wind-gust-speed              | Number:Speed         | Current Gust Speed             | Current wind gust speed, using software specific time period.                          | Wind        |
| windgustdir       | wind-gust-direction          | Number:Angle         | Gust Direction                 | Current wind gust direction expressed as an angle using software specific time period. | Wind        |
| tempf             | temperature                  | Number:Temperature   | Outdoor Temperature            | Current outdoor temperature                                                            | Temperature |
| indoortempf       | indoor-temperature           | Number:Temperature   | Indoor Temperature             | Indoor temperature.                                                                    | Temperature |
| rainin            | rain                         | Number:Length        | Hourly Rain                    | Rain over the past hour.                                                               | Rain        |
| dailyrainin       | rain-daily                   | Number:Length        | Daily Rain                     | Rain since the start of the day.                                                       | Rain        |
| solarradiation    | solarradiation               | Number:Intensity     | Solar Radiation                | Solar radiation                                                                        | Sun         |
| UV                | uv                           | Number:Dimensionless | UV Index                       | UV index.                                                                              | Sun         |
| humidity          | humidity                     | Number:Dimensionless | Humidity                       | Humidity in %.                                                                         | Humidity    |
| indoorhumidity    | indoor-humidity              | Number:Dimensionless | Indoor Humidity                | Indoor humidity in %.                                                                  | Humidity    |
| baromin           |

#### Advanced channel-types

| Request parameter |  Channel type id             | Type                 | Label                          | Description                                                                                         | Group       |
|-------------------|------------------------------|----------------------|--------------------------------|-----------------------------------------------------------------------------------------------------|-------------|
| windspdmph_avg2m  | wind-speed-avg-2min          | Number:Speed         | Wind Speed 2min Average        | 2 minute average wind speed.                                                                        | Wind        |
| winddir_avg2m     | wind-direction-avg-2min      | Number:Angle         | Wind Direction 2min Average    | 2 minute average wind direction.                                                                    | Wind        |
| windgustmph_10m   | wind-gust-speed-10min        | Number:Speed         | Gust Speed 10min Average       | 10 minute average gust speed.                                                                       | Wind        |
| windgustdir_10m   | wind-gust-direction-10min    | Number:Angle         | Gust Direction 10min Average   | 10 minute average gust direction.                                                                   | Wind        |
| windchillf        | wind-chill                   | Number:Temperature   | Wind Chill                     | The apparent wind chill temperature.                                                                | Temperature |
| soiltempf         | soil-temperature             | Number:Temperature   | Soil Temperature               | Soil temperature.                                                                                   | Temperature |
| weeklyrainin      | rain-weekly                  | Number:Length        | Weekly Rain                    | Rain since the start of this week.                                                                  | Rain        |
| monthlyrainin     | rain-monthly                 | Number:Length        | Monthly Rain                   | Rain since the start if this month.                                                                 | Rain        |
| yearlyrainin      | rain-yearly                  | Number:Length        | Yearly Rain                    | Rain since the start of this year.                                                                  | Rain        |
| weather           | metar                        | String               | METAR Weather Report           | METAR formatted weather report                                                                      | Sun_Clouds  |
| clouds            | clouds                       | String               | Cloud Cover                    | METAR style cloud cover.                                                                            | Sun_Clouds  |
| visibility        | visibility                   | Number:Length        | Visibility                     | Visibility.                                                                                         | Sun_Clouds  |
| dewptf            | dew-point                    | Number:Temperature   | Dew Point                      | Outdoor dew point.                                                                                  | Humidity    |
| soilmoisture      | soil-moisture                | Number:Dimensionless | Soil Moisture                  | Soil moisture in %.                                                                                 | Moisture    |
| leafwetness       | leafwetness                  | Number:Dimensionless | Leaf Wetness                   | Leaf wetness in %.                                                                                  | Moisture    |
| AqNO              | nitric-oxide                 | Number:Dimensionless | Nitric Oxide                   | Nitric Oxide ppm.                                                                                   | Pollution   |
| AqNO2T            | nitrogen-dioxide-measured    | Number:Dimensionless | Nitrogen Dioxide               | Nitrogen Dioxide, true measure ppb.                                                                 | Pollution   |
| AqNO2             | nitrogen-dioxide-nox-no      | Number:Dimensionless | NO2 X computed                 | NO2 computed, NOx-NO ppb.                                                                           | Pollution   |
| AqNO2Y            | nitrogen-dioxide-noy-no      | Number:Dimensionless | NO2 Y computed, NOy-NO ppb     | NO2 computed, NOy-NO ppb.                                                                           | Pollution   |
| AqNOX             | nitrogen-oxides              | Number:Dimensionless | Nitrogen Oxides                | Nitrogen Oxides ppb.                                                                                | Pollution   |
| AqNOY             | total-reactive-nitrogen      | Number:Dimensionless | Total Reactive Nitrogen        | Total reactive nitrogen.                                                                            | Pollution   |
| AqNO3             | no3-ion                      | Number:Density       | NO3 ion                        | NO3 ion (nitrate, not adjusted for ammonium ion) µG/m3.                                             | Pollution   |
| AqSO4             | so4-ion                      | Number:Density       | SO4 ion                        | SO4 ion (sulfate, not adjusted for ammonium ion) µG/m3.                                             | Pollution   |
| AqSO2             | sulfur-dioxide               | Number:Dimensionless | Sulfur Dioxide                 | Sulfur Dioxide, conventional ppb.                                                                   | Pollution   |
| AqSO2T            | sulfur-dioxide-trace-levels  | Number:Dimensionless | Sulfur Dioxide Trace Levels    | Sulfur Dioxide, trace levels ppb.                                                                   | Pollution   |
| AqCO              | carbon-monoxide              | Number:Dimensionless | Carbon Monoxide                | Carbon Monoxide, conventional ppm.                                                                  | Pollution   |
| AqCOT             | carbon-monoxide-trace-levels | Number:Dimensionless | Carbon Monoxide Trace Levels   | Carbon Monoxide, trace levels ppb.                                                                  | Pollution   |
| AqEC              | elemental-carbon             | Number:Density       | Elemental Carbon               | Elemental Carbon, PM2.5 µG/m3.                                                                      | Pollution   |
| AqOC              | organic-carbon               | Number:Density       | Organic Carbon                 | Organic Carbon, not adjusted for oxygen and hydrogen, PM2.5 µG/m3.                                  | Pollution   |
| AqBC              | black-carbon                 | Number:Density       | Black Carbon                   | Black Carbon at 880 nm, µG/m3.                                                                      | Pollution   |
| AqUV-AETH         | aethalometer                 | Number:Density       | Second Channel of Aethalometer | second channel of Aethalometer at 370 nm, µG/m3.                                                    | Pollution   |
| AqPM2.5           | pm2_5-mass                   | Number:Density       | PM2.5 Mass                     | PM2.5 mass, µG/m3.                                                                                  | Pollution   |
| AqPM10            | pm10-mass                    | Number:Density       | PM10 Mass                      | PM10 mass, µG/m3.                                                                                   | Pollution   |
| AqOZONE           | ozone                        | Number:Dimensionless | Ozone                          | Ozone, ppb.                                                                                         | Pollution   |

#### Metadata channel-types

| Request parameter |  Channel type id             | Type                 | Label                             | Description                                                                                | Group       |
|-------------------|------------------------------|----------------------|-----------------------------------|--------------------------------------------------------------------------------------------|-------------|
| dateutc           | dateutc                      | String               | Last Updated                      | The date and time of the last update in UTC as submitted by the device. This can be 'now'. | Metadata    |
| softwaretype      | softwaretype                 | String               | Software Type                     | A software type string from the device                                                     | Metadata    |
| rtfreq            | realtime-frequency           | Number               | Realtime Frequency                | How often does the device submit measurements                                              | Metadata    |
| lowbatt           | system:low-battery           | Switch               | Low Battery                       | Low battery warning with possible values on (low battery) and off (battery ok)             | Metadata    |

#### Synthetic channel-types. These are programmatically added

|  Channel type id       | Type                 | Channel type | Label                    | Description                                                                                                                                       | Group    |
|------------------------|----------------------|--------------|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| dateutc-datetime       | dateutc-datetime     | state        | Last Updated as DateTime | The date and time of the last update in UTC as submitted by the device converted to a DateTime value. In case of 'now', the current time is used. | Metadata |
| last-received-datetime | DateTime             | state        | Last Received            | The date and time of the last update.                                                                                                             | Metadata |
| last-query-state       | String               | state        | The last query           | The query part of the last request from the device                                                                                                | Metadata |
| last-query-trigger     | String               | trigger      | The last query           | The query part of the last request from the device                                                                                                | Metadata |

The trigger channel's payload is the last querystring, so the following dsl rule script would send the measurements on to wunderground.com:

```java
val requestQuery = receivedEvent
sendHttpGetRequest("https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php?" + requestQuery)
```

The PASSWORD, action and realtime parameters are ignored, as they are wunderground technical constants, that devices must send.
The ID parameter is used to identify the correct thing the request pertains to, i.e. the stationId configuration value.

As described by the wunderground specification a device can submit multiple values for the outdoor temperature, soil temperature, soil moisture and leaf wetness channels by inserting an index number into the name of the request parameter, fx. tempf can be temp1f, temp2f, etc.
This is supported by the discovery mechanism, creating a channel for each of the values.

# Examples

## Thing file

Configuration using thing and item files is not the recommended method, as you have to manually replicate the configuration discovery produces.
Channels _must_ be named as the request parameters in the channel type table, otherwise the binding will not be able to update with values from requests.
So the request parameter names submitted by your particular device(s) need to be found before being able to write appropriate thing files.
You need to intercept a request from your devices(s) using something like wireshark.
Both thing and item files must be created manually to produce a result practically identical to the one produced through automatic discovery.

Assuming you have intercepted a request such as `https://rtupdate.wunderground.com/weatherstation/updateweatherstation.php?ID=MYSTATIONID&PASSWORD=XXXXXX&windspeedmph=3.11&dateutc=2021-02-07%2014:04:03&softwaretype=WH2600%20V2.2.8&action=updateraw&realtime=1&rtfreq=5`, you can configure a thing to intercept the request thus:

```java
Thing wundergroundupdatereceiver:wundergroundUpdateReceiver:ATHINGID "Foo" [stationId="MYSTATIONID"] {
    Channels:
        Type wind-speed : windspeedmph []
        Type dateutc : dateutc []
        Type softwaretype : softwaretype []
        Type realtime-frequency : rtfreq []
        Type dateutc-datetime : dateutc-datetime []
        Type last-received-datetime : last-received-datetime []
        Type last-query-state : last-query-state []
        Type last-query-trigger : last-query-trigger []
}
```

The pattern for a given channel is `Type <channel type id> : <request parameter> []` from the channel types table.
Casing of the request parameter is significant.
None of the current channels take config.

## Item file

```java
Number:Speed WuBinding_WeatherStation_WindSpeed "Current Wind Speed [%.2f %unit%]" <wind> { channel="wundergroundupdatereceiver:wundergroundUpdateReceiver:ATHINGID:windspeedmph" }
DateTime WuBinding_LastRecieved "Last Recieved Time [%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS]" <time> { channel="wundergroundupdatereceiver:wundergroundUpdateReceiver:ATHINGID:last-received-datetime" }
```

The binding tries to post received values as the item types described in the channel types table, so attaching a channel that takes a given type to an item of a different type has undefined behaviour.

## Rule examples

You can use the trigger channel to create a rule to calculate additional values.
Create a new manual Item with a meaningful id, fx. WundergroundUpdateReceiverBinging_HeatIndex with a Number type.
Create a rule that triggers when the trigger channel is updated and the following DSL:

```java
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

You would then have to trigger another rule to submit the original request with any calculated values appended.

You can also define a transformation to fx. get a cardinal direction (N, S, W, E):

```javascript
(function(s){
  if ( (s == "NULL") || (s == "UNDEF") )
  {
      return undefined;
  }
  else
  {
    var dir = ["N ⬇️", "NNO ⬇️", "NO ↙️", "ONO ⬅️", "O ⬅️", "OSO ⬅️", "SO ↖️", "SSO ⬆️", "S ⬆️", "SSW ⬆️", "SW ↗️", "WSW ➡️", "W ➡️", "WNW ➡️", "NW ↘️", "NNW ⬇️"];   var wind          = parseInt(s.split(" ")[0]);
    var winddiroffset = (wind + (360.0/32.0)) % 360.0;
    var winddiridx    = Math.floor(winddiroffset / (360.0/16.0));
    var winddir       = dir[winddiridx];

    return winddir + ' ('+ wind +'°)';
  }
})(input)
```

The examples were kindly provided by MikeTheTux.
