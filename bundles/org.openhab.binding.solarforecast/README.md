# SolarForecast Binding

This binding provides data from Solar Forecast services. 
Use it to estimate your daily production, plan electric consumers like Electric Vehicle charging, heating or HVAC.
Look ahead the next days in order to identify surplus / shortages in your energy planning.

Supported Services

- [Solcast](https://solcast.com/)
    - Free [Hobbyist Plan](https://toolkit.solcast.com.au/register/hobbyist) with registration
- [Forecast.Solar](https://forecast.solar/)
    - Public, Personal and Professional [plans](https://forecast.solar/#accounts) available 

Display Power values of Forecast and PV Inverter items

<img src="./doc/SolcastPower.png" width="640" height="400"/>

Display Energy values of Forecast and PV inverter items
Yellow line shows *Daily Total Forecast*.

<img src="./doc/SolcastCumulated.png" width="640" height="400"/>

## Supported Things

Each service needs one `xx-site` for your location and at least one photovoltaic `xx-plane`.  

| Name                              | Thing Type ID |
|-----------------------------------|---------------|
| Solcast service site definition   | sc-site       |
| Solcast PV Plane                  | sc-plane      |
| Forecast Solar site location      | fs-site       |
| Forecast Solar PV Plane           | fs-plane      |

## Solcast Configuration

[Solcast service](https://solcast.com/) requires a personal registration with an e-mail address.
A free version for your personal home PV system is available in [Hobbyist Plan](https://toolkit.solcast.com.au/register/hobbyist)
You need to configure your home photovoltaic system within the web interface.
The `resourceId` for each PV plane is provided afterwards.

In order to receive proper timestamps double check your time zone in *openHAB - Settings - Regional Settings*.
Correct time zone is necessary to show correct forecast times in UI. 

### Solcast Tuning

You have the opportunity to [send your own measurements back to Solcast API](https://legacy-docs.solcast.com.au/#measurements-rooftop-site).
This data is used internally to improve the forecast for your specific site.
Configuration and channels can be set after checking the *Show advanced* checkbox.
You need an item which reports the electric power for the specific rooftop. 
If item isn't set, no measures will be sent.
As described in [Solcast Rooftop Measurement](https://legacy-docs.solcast.com.au/#measurements-rooftop-site) check in beforehand if your measures are *sane*.

- item is delivering correct values and they are stored in persistence
- time zone setting in openHAB is correct to deliver correct timestamp

After measurement is sent the `raw-tuning` channel is reporting the result.

### Solcast Bridge Configuration

| Name                   | Type    | Description                           | Default     | Required | Advanced |
|------------------------|---------|---------------------------------------|-------------|----------|----------|
| apiKey                 | text    | API Key                               | N/A         | yes      | no       |
| channelRefreshInterval | integer | Channel Refresh Interval in minutes   | 1           | yes      | no       |
| timeZone               | text    | Time Zone of forecast location        | auto-detect | no       | yes      |

`apiKey` can be obtained in your [Account Settings](https://toolkit.solcast.com.au/account)

`timeZone` is set to `auto-detect` to evaluate Regional Settings of your openHAB installation. 
See [DateTime](#date-time) section for more information.

### Solcast Plane Configuration

| Name            | Type    | Description                                            | Default         | Required | Advanced |
|-----------------|---------|--------------------------------------------------------|-----------------|----------|----------|
| resourceId      | text    | Resource Id of Solcast rooftop site                    | N/A             | yes      | no       |
| refreshInterval | integer | Forecast Refresh Interval in minutes                   | 120             | yes      | no       |
| powerItem       | text    | Power item from your solar inverter for this rooftop   | N/A             | no       | yes      |
| powerUnit       | text    | Unit selection of the powerItem                        | auto-detect     | no       | yes      |

`resourceId` for each plane can be obtained in your [Rooftop Sites](https://toolkit.solcast.com.au/rooftop-sites)

`refreshInterval` of forecast data needs to respect the throttling of the Solcast service. 
If you have 25 free calls per day, each plane needs 2 calls per update a refresh interval of 120 minutes will result in 24 calls per day.

Note: `channelRefreshInterval` from [Bridge Configuration](#solcast-bridge-configuration) will calculate intermediate values without requesting new forecast data.

`powerItem` shall reflect the power for this specific rooftop. 
It's an optional setting and the [measure is sent to Solcast API in order to tune the forecast](https://legacy-docs.solcast.com.au/#measurements-rooftop-site) in the future.
If you don't want to send measures to Solcast, leave this configuration item empty.

`powerUnit` is set to `auto-detect`. 
In case the `powerItem` is delivering a valid `QuantityType<Power>` state, this setting is fine.
If the item delivers a raw number without unit please select `powerUnit` accordingly if item state, is Watt or Kilowatt unit. 

## Solcast Channels

Each `sc-plane` reports its own values including a `raw` channel holding JSON content.
The `sc-site` bridge sums up all attached `sc-plane` values and provides the total forecast for your home location.  

Channels are covering today's actual data with current, remaining and today's total prediction.
Forecasts are delivered up to 6 days in advance including 

- a pessimistic scenario: 10th percentile 
- an optimistic scenario: 90th percentile

Day*X* channels are referring to forecasts plus *X* days: 1 = tomorrow, 2 = day after tomorrow, ...

| Channel                 | Type          | Unit | Description                              | Advanced |
|-------------------------|---------------|------|------------------------------------------|----------|
| actual                  | Number:Energy | kWh  | Today's forecast till now                | no       |
| actual-power            | Number:Power  | W    | Predicted power in this moment           | no       |
| remaining               | Number:Energy | kWh  | Forecast of today's remaining production | no       |
| today                   | Number:Energy | kWh  | Today's forecast in total                | no       |
| day*X*                  | Number:Energy | kWh  | Day *X* forecast in total                | no       |
| day*X*-low              | Number:Energy | kWh  | Day *X* pessimistic forecast             | no       |
| day*X*-high             | Number:Energy | kWh  | Day *X* optimistic forecast              | no       |
| raw                     | String        | -    | Plain JSON response without conversions  | yes      |
| raw-tuning              | String        | -    | JSON response from tuning call           | yes      |

## ForecastSolar Configuration

[ForecastSolar service](https://forecast.solar/) provides a [public free](https://forecast.solar/#accounts) plan.
You can try it without any registration or other preconditions.

### ForecastSolar Bridge Configuration

| Name                   | Type    | Description                           | Default      | Required |
|------------------------|---------|---------------------------------------|--------------|----------|
| location               | text    | Location of Photovoltaic system       | auto-detect  | yes      |
| channelRefreshInterval | integer | Channel Refresh Interval in minutes   | 1            | yes      |
| apiKey                 | text    | API Key                               | N/A          | no       |

`location` defines latitude, longitude values of your PV system.
In case of auto-detect the location configured in openHAB is obtained.

`apiKey` can be given in case you subscribed to a paid plan

### ForecastSolar Plane Configuration

| Name            | Type    | Description                                                                  | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------------------|---------|----------|----------|
| refreshInterval | integer | Forecast Refresh Interval in minutes                                         | 30      | yes      | false    |
| declination     | integer | Plane Declination: 0 for horizontal till 90 for vertical declination         | N/A     | yes      | false    |
| azimuth         | integer | Plane Azimuth: -180 = north, -90 = east, 0 = south, 90 = west, 180 = north   | N/A     | yes      | false    |
| kwp             | decimal | Installed Kilowatt Peak                                                      | N/A     | yes      | false    |
| dampAM          | decimal | Damping factor of morning hours                                              | N/A     | no       | true     |
| dampPM          | decimal | Damping factor of evening hours                                              | N/A     | no       | true     |
| horizon         | text    | Horizon definition as comma separated integer values                         | N/A     | no       | true     |

`refreshInterval` of forecast data needs to respect the throttling of the ForecastSolar service. 
12 calls per hour allowed from your caller IP address so for 2 planes lowest possible refresh rate is 10 minutes.

Note: `channelRefreshInterval` from [Bridge Configuration](#forecastsolar-bridge-configuration) will calculate intermediate values without requesting new forecast data.

#### Advanced Configuration

Advanced configuration parameters are available to *fine tune* your forecast data.
Read linked documentation in order to know what you're doing.

[Damping factors](https://doc.forecast.solar/doku.php?id=damping) for morning and evening.

[Horizon information](https://doc.forecast.solar/doku.php?id=api) as comma separated integer list.
This configuration item is aimed to expert users.
You need to understand the [horizon concept](https://joint-research-centre.ec.europa.eu/pvgis-photovoltaic-geographical-information-system/getting-started-pvgis/pvgis-user-manual_en#ref-2-using-horizon-information).
Shadow obstacles like mountains, hills, buildings can be expressed here.
First step can be a download from [PVGIS tool](https://re.jrc.ec.europa.eu/pvg_tools/en/) and downloading the *terrain shadows*.
But it doesn't fit 100% to the required configuration.
Currently there's no tool available which is providing the configuration information 1 to 1.
So you need to know what you're doing.

## ForecastSolar Channels

Each `fs-plane` reports it's own values including a `raw` channel holding JSON content.
The `fs-site` bridge sums up all attached `fs-plane` values and provides the total forecast for your home location.  

Channels are covering today's actual data with current, remaining and total prediction.
Forecasts are delivered up to 3 days for paid personal plans.

Day*X* channels are referring to forecasts plus *X* days: 1 = tomorrow, 2 = day after tomorrow, ...

| Channel                 | Type          | Unit | Description                              | Advanced |
|-------------------------|---------------|------|------------------------------------------|----------|
| actual                  | Number:Energy | kWh  | Today's forecast till now                | no       |
| actual-power            | Number:Power  | W    | Predicted power in this moment           | no       |
| remaining               | Number:Energy | kWh  | Forecast of today's remaining production | no       |
| today                   | Number:Energy | kWh  | Today's forecast in total                | no       |
| day*X*                  | Number:Energy | kWh  | Day *X* forecast in total                | no       |
| raw                     | String        | -    | Plain JSON response without conversions  | yes      |

## Thing Actions

All things `sc-site`, `sc-plane`, `fs-site` and `fs-plane` are providing the same Actions.
Channels are providing actual forecast data and daily forecasts in future.
Actions provides an interface to execute more sophisticated handling in rules.
You can execute this for each `xx-plane` for specific plane values or `xx-site` to sum up all attached planes.

See [Date Time](#date-time) section for more information.
Double check your time zone in *openHAB - Settings - Regional Settings* which is crucial for calculation.

### `getForecastBegin`

Returns `Instant` of the earliest possible forecast data available.
It's located in the past, e.g. Solcast provides data from the last 7 days.
`Instant.MAX` is returned in case of no forecast data is available.

### `getForecastEnd`

Returns `Instant` of the latest possible forecast data available.
`Instant.MIN` is returned in case of no forecast data is available.

### `getPower`

| Parameter | Type          | Description                                                                                                  |
|-----------|---------------|--------------------------------------------------------------------------------------------------------------|
| timestamp | Instant       | Timestamp of power query                                                                                     |
| mode      | String        | Choose `optimistic` or `pessimistic` to get values for a positive or negative future scenario. Only Solcast. |

Returns `QuantityType<Power>` at the given `Instant` timestamp.
Respect `getForecastBegin` and `getForecastEnd` to get a valid value.
Check for `UndefType.UNDEF` in case of errors.

### `getDay`

| Parameter | Type          | Description                                                                                                  |
|-----------|---------------|--------------------------------------------------------------------------------------------------------------|
| date      | LocalDate     | Date of the day                                                                                              |
| mode      | String        | Choose `optimistic` or `pessimistic` to get values for a positive or negative future scenario. Only Solcast. |

Returns `QuantityType<Energy>` at the given `localDate`.
Respect `getForecastBegin` and `getForecastEnd` to avoid ambiguous values.
Check for `UndefType.UNDEF` in case of errors.

### `getEnergy`

| Parameter       | Type          | Description                                                                                                  |
|-----------------|---------------|--------------------------------------------------------------------------------------------------------------|
| startTimestamp  | Instant       | Start timestamp of energy query                                                                              |
| endTimestamp    | Instant       | End timestamp of energy query                                                                                |
| mode            | String        | Choose `optimistic` or `pessimistic` to get values for a positive or negative future scenario. Only Solcast. |

Returns `QuantityType<Energy>` between the timestamps `startTimestamp` and `endTimestamp`.
Respect `getForecastBegin` and `getForecastEnd` to avoid ambiguous values.
Check for `UndefType.UNDEF` in case of errors.

## Date Time

Each forecast is bound to a certain location which automatically defines the time zone.
Most common use case is forecast and your locarion are matching the same time zone.
Action interface is using `Instant` as timestamps which enables you translating to any time zone.
This allows you with an easy conversion to query also foreign forecast locations.  

Examples are showing

- how to translate `Instant` to `ZonedDateTime` objects and
- how to translate `ZonedDateTime` to `Instant` objects

## Example

Example is based on Forecast.Solar service without any registration.
Exchange the configuration data in [thing file](#thing-file) and you're ready to go.

### Thing file

```java
Bridge solarforecast:fs-site:homeSite   "ForecastSolar Home" [ location="54.321,8.976", channelRefreshInterval=1] {
         Thing fs-plane homeSouthWest   "ForecastSolar Home South-West" [ refreshInterval=10, azimuth=45, declination=35, kwp=5.5]
         Thing fs-plane homeNorthEast   "ForecastSolar Home North-East" [ refreshInterval=10, azimuth=-145, declination=35, kwp=4.425]
}
```

### Items file

```java
Number:Energy           ForecastSolarHome_Actual           "Actual Forecast Today [%.3f %unit%]"             {channel="solarforecast:fs-site:homeSite:actual" }                                                                           
Number:Power            ForecastSolarHome_Actual_Power     "Actual Power Forecast [%.3f %unit%]"             {channel="solarforecast:fs-site:homeSite:actual-power" }                                                                           
Number:Energy           ForecastSolarHome_Remaining        "Remaining Forecast Today [%.3f %unit%]"          {channel="solarforecast:fs-site:homeSite:remaining" }                                                                           
Number:Energy           ForecastSolarHome_Today            "Today Total Forecast [%.3f %unit%]"              {channel="solarforecast:fs-site:homeSite:today" }                                                                           
Number:Energy           ForecastSolarHome_Day1             "Tomorrow Total Forecast [%.3f %unit%]"           {channel="solarforecast:fs-site:homeSite:day1" }                                                                           

Number:Energy           ForecastSolarHome_Actual_NE        "Actual NE Forecast Today [%.3f %unit%]"          {channel="solarforecast:fs-plane:homeSite:homeNorthEast:actual" }                                                                           
Number:Power            ForecastSolarHome_Actual_Power_NE  "Actual NE Power Forecast [%.3f %unit%]"          {channel="solarforecast:fs-plane:homeSite:homeNorthEast:actual-power" }                                                                           
Number:Energy           ForecastSolarHome_Remaining_NE     "Remaining NE Forecast Today [%.3f %unit%]"       {channel="solarforecast:fs-plane:homeSite:homeNorthEast:remaining" }                                                                           
Number:Energy           ForecastSolarHome_Today_NE         "Total NE Forecast Today [%.3f %unit%]"           {channel="solarforecast:fs-plane:homeSite:homeNorthEast:today" }                                                                           
Number:Energy           ForecastSolarHome_Day_NE           "Tomorrow NE Forecast [%.3f %unit%]"              {channel="solarforecast:fs-plane:homeSite:homeNorthEast:day1" }                                                                           

Number:Energy           ForecastSolarHome_Actual_SW        "Actual SW Forecast Today [%.3f %unit%]"          {channel="solarforecast:fs-plane:homeSite:homeSouthWest:actual" }                                                                           
Number:Power            ForecastSolarHome_Actual_Power_SW  "Actual SW Power Forecast [%.3f %unit%]"          {channel="solarforecast:fs-plane:homeSite:homeSouthWest:actual-power" }                                                                           
Number:Energy           ForecastSolarHome_Remaining_SW     "Remaining SW Forecast Today [%.3f %unit%]"       {channel="solarforecast:fs-plane:homeSite:homeSouthWest:remaining" }                                                                           
Number:Energy           ForecastSolarHome_Today_SW         "Total SW Forecast Today [%.3f %unit%]"           {channel="solarforecast:fs-plane:homeSite:homeSouthWest:today" }                                                                           
Number:Energy           ForecastSolarHome_Day_SW           "Tomorrow SW Forecast [%.3f %unit%]"              {channel="solarforecast:fs-plane:homeSite:homeSouthWest:day1" }                                                                           
```

### Actions rule

```java
import java.time.temporal.ChronoUnit

rule "Forecast Solar Actions"
    when
        Time cron "0 0 23 * * ?" // trigger whatever you like
    then 
        // get Actions for specific fs-site
        val solarforecastActions = getActions("solarforecast","solarforecast:fs-site:homeSite")
        val timeZone = "Europe/Berlin"

        // get earliest and latest forecast dates
        val beginDT = solarforecastActions.getForecastBegin
        val endDT = solarforecastActions.getForecastEnd
        logInfo("SF Tests","Begin: "+ beginDT.atZone(ZoneId.of(timeZone))+" End: "+endDT.atZone(ZoneId.of(timeZone)))
 
        // get forecast for tomorrow    
        val fcTomorrowState = solarforecastActions.getDay(LocalDate.now.plusDays(1))
        logInfo("SF Tests","Forecast tomorrow state: "+ fcTomorrowState.toString)
        val fcToTomorrowDouble = (fcTomorrowState as Number).doubleValue
        logInfo("SF Tests","Forecast tomorrow value: "+ fcToTomorrowDouble)
        
        // get power forecast in one hour
        val hourPlusOnePowerState = solarforecastActions.getPower(Instant.now.plus(1,ChronoUnit.HOURS))
        logInfo("SF Tests","Hour+1 power state: "+ hourPlusOnePowerState.toString)
        val hourPlusOnePowerValue = (hourPlusOnePowerState as Number).doubleValue
        logInfo("SF Tests","Hour+1 power value: "+ hourPlusOnePowerValue)
        
        // get energy forecast at specific time: Nov 18th 2023, 16:00 
        val startDT = LocalDateTime.of(2023,11,18,16,00).atZone(ZoneId.of(timeZone))
        val stopDT = startDT.plusDays(2)
        val twoDaysForecastFromNowState = solarforecastActions.getEnergy(startDT.toInstant, stopDT.toInstant)
        logInfo("SF Tests","Forecast 2 days state: "+ twoDaysForecastFromNowState.toString)
        val twoDaysForecastFromNowValue = (twoDaysForecastFromNowState as Number).doubleValue
        logInfo("SF Tests","Forecast 2 days value: "+ twoDaysForecastFromNowValue)
end
```

shall produce following output

```
2023-11-18 22:00:59.250 [INFO ] [g.openhab.core.model.script.SF Tests] - Begin: 2023-11-18T07:34:23+01:00[Europe/Berlin] End: 2023-11-19T16:26:50+01:00[Europe/Berlin]
2023-11-18 22:00:59.262 [INFO ] [g.openhab.core.model.script.SF Tests] - Forecast tomorrow state: 3.861 kWh
2023-11-18 22:00:59.267 [INFO ] [g.openhab.core.model.script.SF Tests] - Forecast tomorrow value: 3.861
2023-11-18 22:00:59.275 [INFO ] [g.openhab.core.model.script.SF Tests] - Hour+1 power state: 0 kW
2023-11-18 22:00:59.280 [INFO ] [g.openhab.core.model.script.SF Tests] - Hour+1 power value: 0.0
2023-11-18 22:00:59.296 [INFO ] [g.openhab.core.model.script.SF Tests] - Forecast 2 days state: 3.865 kWh
2023-11-18 22:00:59.300 [INFO ] [g.openhab.core.model.script.SF Tests] - Forecast 2 days value: 3.865
```

### Actions rule with Arguments

Only Solcast is delivering `optimistic` and `pessimistic` scenario data.
If arguments are used on ForecastSolar `UNDEF` state is returned

```java
import java.time.temporal.ChronoUnit

rrule "Solcast Actions"
    when
        Time cron "0 0 23 * * ?" // trigger whatever you like
    then 
        val solarforecastActions = getActions("solarforecast","solarforecast:sc-site:homeSite")
        val startTimestamp = Instant.now
        val endTimestamp = Instant.now.plus(6, ChronoUnit.DAYS)
        val sixDayForecast = solarforecastActions.getEnergy(startTimestamp,endTimestamp)
        logInfo("SF Tests","Forecast Estimate  6 days "+ sixDayForecast)
        val sixDayOptimistic = solarforecastActions.getEnergy(startTimestamp,endTimestamp, "optimistic")
        logInfo("SF Tests","Forecast Optimist  6 days "+ sixDayOptimistic)
        val sixDayPessimistic = solarforecastActions.getEnergy(startTimestamp,endTimestamp, "pessimistic")
        logInfo("SF Tests","Forecast Pessimist 6 days "+ sixDayPessimistic)
end
```

shall produce following output

```
2022-08-10 00:02:16.569 [INFO ] [g.openhab.core.model.script.SF Tests] - Forecast Estimate  6 days 309.424 kWh
2022-08-10 00:02:16.574 [INFO ] [g.openhab.core.model.script.SF Tests] - Forecast Optimist  6 days 319.827 kWh
2022-08-10 00:02:16.578 [INFO ] [g.openhab.core.model.script.SF Tests] - Forecast Pessimist 6 days 208.235 kWh
```
