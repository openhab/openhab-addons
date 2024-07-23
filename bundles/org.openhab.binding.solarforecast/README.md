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

### Solcast Bridge Configuration

| Name                   | Type    | Description                           | Default     | Required | Advanced |
|------------------------|---------|---------------------------------------|-------------|----------|----------|
| apiKey                 | text    | API Key                               | N/A         | yes      | no       |
| timeZone               | text    | Time Zone of forecast location        | empty       | no       | yes      |

`apiKey` can be obtained in your [Account Settings](https://toolkit.solcast.com.au/account)

`timeZone` can be left empty to evaluate Regional Settings of your openHAB installation. 
See [DateTime](#date-time) section for more information.

### Solcast Plane Configuration

| Name            | Type    | Description                                            | Default         | Required | Advanced |
|-----------------|---------|--------------------------------------------------------|-----------------|----------|----------|
| resourceId      | text    | Resource Id of Solcast rooftop site                    | N/A             | yes      | no       |
| refreshInterval | integer | Forecast Refresh Interval in minutes                   | 120             | yes      | no       |

`resourceId` for each plane can be obtained in your [Rooftop Sites](https://toolkit.solcast.com.au/rooftop-sites)

`refreshInterval` of forecast data needs to respect the throttling of the Solcast service. 
If you have 25 free calls per day, each plane needs 2 calls per update a refresh interval of 120 minutes will result in 24 calls per day.

## Solcast Channels

Each `sc-plane` reports its own values including a `json` channel holding JSON content.
The `sc-site` bridge sums up all attached `sc-plane` values and provides total forecast for your home location.  

Channels are covering today's actual data with current, remaining and today's total prediction.
Forecasts are delivered up to 6 days in advance.
Scenarios are clustered in groups: 

- `average` scenario
- `pessimistic` scenario: 10th percentile 
- `optimistic` scenario: 90th percentile

| Channel                 | Type          | Unit | Description                                     | Advanced |
|-------------------------|---------------|------|-------------------------------------------------|----------|
| power-estimate          | Number:Power  | W    | Power forecast for next hours/days              | no       |
| energy-estimate         | Number:Energy | kWh  | Energy forecast for next hours/days             | no       |
| power-actual            | Number:Power  | W    | Power prediction for this moment                | no       |
| energy-actual           | Number:Energy | kWh  | Today's forecast till now                       | no       |
| energy-remain           | Number:Energy | kWh  | Today's remaining forecast till sunset          | no       |
| energy-today            | Number:Energy | kWh  | Today's forecast in total                       | no       |
| json                    | String        | -    | Plain JSON response without conversions         | yes      |

## ForecastSolar Configuration

[ForecastSolar service](https://forecast.solar/) provides a [public free](https://forecast.solar/#accounts) plan.
You can try it without any registration or other preconditions.

### ForecastSolar Bridge Configuration

| Name                   | Type    | Description                           | Default      | Required |
|------------------------|---------|---------------------------------------|--------------|----------|
| location               | text    | Location of Photovoltaic system.      | empty        | no       |
| apiKey                 | text    | API Key                               | N/A          | no       |

`location` defines latitude, longitude values of your PV system.
In case of empty the location configured in openHAB is obtained.

`apiKey` can be given in case you subscribed to a paid plan.

### ForecastSolar Plane Configuration

| Name            | Type    | Description                                                                  | Default | Required | Advanced |
|-----------------|---------|------------------------------------------------------------------------------|---------|----------|----------|
| refreshInterval | integer | Forecast Refresh Interval in minutes                                         | 30      | yes      | false    |
| declination     | integer | Plane Declination: 0 for horizontal till 90 for vertical declination         | N/A     | yes      | false    |
| azimuth         | integer | Plane Azimuth: -180 = north, -90 = east, 0 = south, 90 = west, 180 = north   | N/A     | yes      | false    |
| kwp             | decimal | Installed Kilowatt Peak                                                      | N/A     | yes      | false    |
| dampAM          | decimal | Damping factor of morning hours                                              | 0       | no       | true     |
| dampPM          | decimal | Damping factor of evening hours                                              | 0       | no       | true     |
| horizon         | text    | Horizon definition as comma separated integer values                         | N/A     | no       | true     |

`refreshInterval` of forecast data needs to respect the throttling of the ForecastSolar service. 
12 calls per hour allowed from your caller IP address so for 2 planes lowest possible refresh rate is 10 minutes.

#### Advanced Configuration

Advanced configuration parameters are available to *fine tune* your forecast data.
Read linked documentation in order to know what you're doing.

[Damping factors](https://doc.forecast.solar/doku.php?id=damping) for morning and evening.

[Horizon information](https://doc.forecast.solar/doku.php?id=api) as comma-separated integer list.
This configuration item is aimed to expert users.
You need to understand the [horizon concept](https://joint-research-centre.ec.europa.eu/pvgis-photovoltaic-geographical-information-system/getting-started-pvgis/pvgis-user-manual_en#ref-2-using-horizon-information).
Shadow obstacles like mountains, hills, buildings can be expressed here.
First step can be a download from [PVGIS tool](https://re.jrc.ec.europa.eu/pvg_tools/en/) and downloading the *terrain shadows*.
But it doesn't fit 100% to the required configuration.
Currently there's no tool available which is providing the configuration information 1 to 1.
So you need to know what you're doing.

## ForecastSolar Channels

Each `fs-plane` reports its own values including a `json` channel holding JSON content.
The `fs-site` bridge sums up all attached `fs-plane` values and provides the total forecast for your home location.  

Channels are covering today's actual data with current, remaining and total prediction.
Forecasts are delivered up to 3 days for paid personal plans.

| Channel                 | Type          | Unit | Description                                     | Advanced |
|-------------------------|---------------|------|-------------------------------------------------|----------|
| power-estimate          | Number:Power  | W    | Power forecast for next hours/days              | no       |
| energy-estimate         | Number:Energy | kWh  | Energy forecast for next hours/days             | no       |
| power-actual            | Number:Power  | W    | Power prediction for this moment                | no       |
| energy-actual           | Number:Energy | kWh  | Today's forecast till now                       | no       |
| energy-remain           | Number:Energy | kWh  | Today's remaining forecast till sunset          | no       |
| energy-today            | Number:Energy | kWh  | Today's forecast in total                       | no       |
| json                    | String        | -    | Plain JSON response without conversions         | yes      |

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

| Parameter | Type          | Description                                                                                |
|-----------|---------------|--------------------------------------------------------------------------------------------|
| timestamp | Instant       | Timestamp of power query                                                                   |
| mode      | String        | Choose `average`, `optimistic` or `pessimistic` to select forecast scenario. Only Solcast. |

Returns `QuantityType<Power>` at the given `Instant` timestamp.
Respect `getForecastBegin` and `getForecastEnd` to get a valid value.

Check log or catch exceptions for error handling

- `IllegalArgumentException` thrown in case of problems with call arguments
- `SolarForecastException` thrown in case of problems with timestamp and available forecast data

### `getDay`

| Parameter | Type          | Description                                                                                |
|-----------|---------------|--------------------------------------------------------------------------------------------|
| date      | LocalDate     | Date of the day                                                                            |
| mode      | String        | Choose `average`, `optimistic` or `pessimistic` to select forecast scenario. Only Solcast. |

Returns `QuantityType<Energy>` at the given `localDate`.
Respect `getForecastBegin` and `getForecastEnd` to avoid ambiguous values.

Check log or catch exceptions for error handling

- `IllegalArgumentException` thrown in case of problems with call arguments
- `SolarForecastException` thrown in case of problems with timestamp and available forecast data

### `getEnergy`

| Parameter       | Type          | Description                                                                                                  |
|-----------------|---------------|--------------------------------------------------------------------------------------------------------------|
| startTimestamp  | Instant       | Start timestamp of energy query                                                                              |
| endTimestamp    | Instant       | End timestamp of energy query                                                                                |
| mode            | String        | Choose `optimistic` or `pessimistic` to get values for a positive or negative future scenario. Only Solcast. |

Returns `QuantityType<Energy>` between the timestamps `startTimestamp` and `endTimestamp`.
Respect `getForecastBegin` and `getForecastEnd` to avoid ambiguous values.

Check log or catch exceptions for error handling

- `IllegalArgumentException` thrown in case of problems with call arguments
- `SolarForecastException` thrown in case of problems with timestamp and available forecast data

## Date Time

Each forecast is bound to a certain location which automatically defines the time zone.
Most common use case is forecast and your location are matching the same time zone.
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
Bridge solarforecast:fs-site:homeSite   "ForecastSolar Home" [ location="54.321,8.976"] {
         Thing fs-plane homeSouthWest   "ForecastSolar Home South-West" [ refreshInterval=15, azimuth=45, declination=35, kwp=5.5]
         Thing fs-plane homeNorthEast   "ForecastSolar Home North-East" [ refreshInterval=15, azimuth=-145, declination=35, kwp=4.425]
}
```

### Items file

```java
// channel items
Number:Power            ForecastSolarHome_Actual_Power      "Power prediction for this moment"              { channel="solarforecast:fs-site:homeSite:power-actual", stateDescription=" "[ pattern="%.0f %unit%" ], unit="W" }                                                                           
Number:Energy           ForecastSolarHome_Actual            "Today's forecast till now"                     { channel="solarforecast:fs-site:homeSite:energy-actual", stateDescription=" "[ pattern="%.3f %unit%" ], unit="kWh" }                                                                           
Number:Energy           ForecastSolarHome_Remaining         "Today's remaining forecast till sunset"        { channel="solarforecast:fs-site:homeSite:energy-remain", stateDescription=" "[ pattern="%.3f %unit%" ], unit="kWh" }                                                                           
Number:Energy           ForecastSolarHome_Today             "Today's total energy forecast"                 { channel="solarforecast:fs-site:homeSite:energy-today", stateDescription=" "[ pattern="%.1f %unit%" ], unit="kWh" }   
// calculated by rule                                                                        
Number:Energy           ForecastSolarHome_Tomorrow          "Tomorrow's total energy forecast"              { stateDescription=" "[ pattern="%.1f %unit%" ], unit="kWh" }                                                                           

Number:Power            ForecastSolarHome_Actual_Power_NE   "NE Power prediction for this moment"           { channel="solarforecast:fs-plane:homeSite:homeNorthEast:power-actual", stateDescription=" "[ pattern="%.0f %unit%" ], unit="W" }   
Number:Energy           ForecastSolarHome_Actual_NE         "NE Today's forecast till now"                  { channel="solarforecast:fs-plane:homeSite:homeNorthEast:energy-actual", stateDescription=" "[ pattern="%.3f %unit%" ], unit="kWh" }                                                                           
Number:Energy           ForecastSolarHome_Remaining_NE      "NE Today's remaining forecast till sunset"     { channel="solarforecast:fs-plane:homeSite:homeNorthEast:energy-remain", stateDescription=" "[ pattern="%.3f %unit%" ], unit="kWh" }                                                                           
Number:Energy           ForecastSolarHome_Today_NE          "NE Today's total energy forecast"              { channel="solarforecast:fs-plane:homeSite:homeNorthEast:energy-today", stateDescription=" "[ pattern="%.1f %unit%" ], unit="kWh" }                                                                           

Number:Power            ForecastSolarHome_Actual_Power_SW   "SW Power prediction for this moment"           { channel="solarforecast:fs-plane:homeSite:homeSouthWest:power-actual", stateDescription=" "[ pattern="%.0f %unit%" ], unit="W" }                                                                           
Number:Energy           ForecastSolarHome_Actual_SW         "SW Today's forecast till now"                  { channel="solarforecast:fs-plane:homeSite:homeSouthWest:energy-actual", stateDescription=" "[ pattern="%.3f %unit%" ], unit="kWh" }                                                                           
Number:Energy           ForecastSolarHome_Remaining_SW      "SW Today's remaining forecast till sunset"     { channel="solarforecast:fs-plane:homeSite:homeSouthWest:energy-remain", stateDescription=" "[ pattern="%.3f %unit%" ], unit="kWh" }                                                                           
Number:Energy           ForecastSolarHome_Today_SW          "SW Today's total energy forecast"              { channel="solarforecast:fs-plane:homeSite:homeSouthWest:energy-today", stateDescription=" "[ pattern="%.1f %unit%" ], unit="kWh" }                                                                           

// estimation items
Group influxdb
Number:Power            ForecastSolarHome_Power_Estimate        "Power estimations"                         (influxdb)  { channel="solarforecast:fs-site:homeSite:power-estimate", stateDescription=" "[ pattern="%.0f %unit%" ], unit="W" }                                                                           
Number:Energy           ForecastSolarHome_Energy_Estimate       "Energy estimations"                        (influxdb)  { channel="solarforecast:fs-site:homeSite:energy-estimate", stateDescription=" "[ pattern="%.3f %unit%" ], unit="kWh" }                                                                           
Number:Power            ForecastSolarHome_Power_Estimate_SW     "SW Power estimations"                      (influxdb)  { channel="solarforecast:fs-plane:homeSite:homeSouthWest:power-estimate", stateDescription=" "[ pattern="%.0f %unit%" ], unit="W" }                                                                           
Number:Energy           ForecastSolarHome_Energy_Estimate_SW    "SW Energy estimations"                     (influxdb)  { channel="solarforecast:fs-plane:homeSite:homeSouthWest:energy-estimate", stateDescription=" "[ pattern="%.3f %unit%" ], unit="kWh" }                                                                           
```

### Persistence file

```java
// persistence strategies have a name and definition and are referred to in the "Items" section
Strategies {
        everyHour : "0 0 * * * ?"
        everyDay  : "0 0 0 * * ?"
}

/*
 * Each line in this section defines for which Item(s) which strategy(ies) should be applied.
 * You can list single items, use "*" for all items or "groupitem*" for all members of a group
 * Item (excl. the group Item itself).
 */
Items {
        influxdb* : strategy = restoreOnStartup, forecast
}
```

### Actions rule

```java
rule "Tomorrow Forecast Calculation"
    when
        Item ForecastSolarHome_Today received update
    then 
        val solarforecastActions = getActions("solarforecast","solarforecast:fs-site:homeSite")
        val energyState = solarforecastActions.getDay(LocalDate.now.plusDays(1))
        logInfo("SF Tests","{}",energyState)
        ForecastSolarHome_Tomorrow.postUpdate(energyState) 
end
```

### Handle exceptions

```java
import java.time.temporal.ChronoUnit

rule "Exception Handling"
    when
        System started
    then 
        val solcastActions = getActions("solarforecast","solarforecast:sc-site:3cadcde4dc")
        try {
            val forecast = solcastActions.getPower(solcastActions.getForecastEnd.plus(30,ChronoUnit.MINUTES))
        } catch(RuntimeException e) {
            logError("Exception","Handle {}",e.getMessage)
        }
end
```

### Actions rule with Arguments

```java
import java.time.temporal.ChronoUnit

rule "Solcast Actions"
    when
        Time cron "0 0 23 * * ?" // trigger whatever you like
    then 
        // Query forecast via Actions
        val solarforecastActions = getActions("solarforecast","solarforecast:sc-site:homeSite")
        val startTimestamp = Instant.now
        val endTimestamp = Instant.now.plus(6, ChronoUnit.DAYS)
        val sixDayForecast = solarforecastActions.getEnergy(startTimestamp,endTimestamp)
        logInfo("SF Tests","Forecast Average 6 days "+ sixDayForecast)
        val sixDayOptimistic = solarforecastActions.getEnergy(startTimestamp,endTimestamp, "optimistic")
        logInfo("SF Tests","Forecast Optimist 6 days "+ sixDayOptimistic)
        val sixDayPessimistic = solarforecastActions.getEnergy(startTimestamp,endTimestamp, "pessimistic")
        logInfo("SF Tests","Forecast Pessimist 6 days "+ sixDayPessimistic)

        // Query forecast TimesSeries Items via historicStata
        val energyAverage =  (Solcast_Site_Average_Energyestimate.historicState(now.plusDays(1)).state as Number)
        logInfo("SF Tests","Average energy {}",energyAverage)
        val energyOptimistic =  (Solcast_Site_Optimistic_Energyestimate.historicState(now.plusDays(1)).state as Number)
        logInfo("SF Tests","Optimist energy {}",energyOptimistic)
end
```
