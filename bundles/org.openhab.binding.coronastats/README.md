# CoronaStats Binding

This binding provides the statistic about cases of COVID-19 from the website [https://corona-stats.online/](https://corona-stats.online/).

## Supported Things

This binding supports a `world` thing, which polls the dataset in an adjustable interval as a bridge and provides the statistics for the whole world.
The `country` thing, representing the statistics for a specified country.

## Discovery

This binding adds a default `world` thing to the Inbox.
This can be used as bridge for country things you may add manually.

## Thing Configuration

### World

| Parameter | Default | Required | Description                                                                              |
| --------- | :-----: | :------: | ---------------------------------------------------------------------------------------- |
| `refresh` |   30    |    no    | Define the interval for polling the data from website in minutes. Minimum is 15 minutes. |

### Country

| Parameter     | Default | Required | Description                                       |
| ------------- | :-----: | :------: | ------------------------------------------------- |
| `countryCode` |    -    |   yes    | 2-letter code for the country you want to display |

For the correct 2-letter country code have a look at the website [https://corona-stats.online/](https://corona-stats.online/)

## Channels

### World and Country

| channels       | type                 | description                                    |
| -------------- | -------------------- | ---------------------------------------------- |
| `cases`        | Number:Dimensionless | Total cases                                    |
| `today_cases`  | Number:Dimensionless | Increase of total cases today                  |
| `deaths`       | Number:Dimensionless | Deaths                                         |
| `today_deaths` | Number:Dimensionless | Increase of deaths                             |
| `recovered`    | Number:Dimensionless | Recovered cases                                |
| `active`       | Number:Dimensionless | Active cases                                   |
| `critical`     | Number:Dimensionless | Critical cases                                 |
| `tests`        | Number:Dimensionless | Count of reported tests (`country` thing only) |
| `updated`      | Number:Dimensionless | Data last update time (`country` thing only)   |

## Full Example

### Things

```java
Bridge coronastats:world:stats "Corona Stats World" @ "Corona" [refresh=15] {
    Thing country usa "Corona Stats USA" @ "Corona" [countryCode="US"]
    Thing country germany "Corona Stats Germany" @ "Corona" [countryCode="DE"]
    Thing country austria "Corona Stats Austria" @ "Corona" [countryCode="AT"]
    Thing country italy "Corona Stats Italy" @ "Corona" [countryCode="IT"]
    Thing country spain "Corona Stats Spain" @ "Corona" [countryCode="ES"]
    Thing country uk "Corona Stats United Kingdom" @ "Corona" [countryCode="GB"]
}
```

### Items

```java
Number:Dimensionless coronaCasesWorld "Total Cases World [%,d]"
    {channel="coronastats:world:stats:cases"}

Number:Dimensionless coronaDeathsWorld "Deaths World [%,d]"
    {channel="coronastats:world:stats:deaths"}

Number:Dimensionless coronaRecoveredWorld "Recovered Cases World [%,d]"
    {channel="coronastats:world:stats:recovered"}

Number:Dimensionless coronaActiveWorld "Active Cases World [%,d]"
    {channel="coronastats:world:stats:active"}

Number:Dimensionless coronaCriticalWorld "Critical Cases World [%,d]"
    {channel="coronastats:world:stats:critical"}

Number:Dimensionless coronaCasesUSA "Total Cases USA [%,d]"
    {channel="coronastats:country:stats:usa:cases"}

Number:Dimensionless coronaDeathsUSA "Deaths USA [%,d]"
    {channel="coronastats:country:stats:usa:deaths"}

Number:Dimensionless coronaRecoveredUSA "Recovered Cases USA [%,d]"
    {channel="coronastats:country:stats:usa:recovered"}

Number:Dimensionless coronaActiveUSA "Active Cases USA [%,d]"
    {channel="coronastats:country:stats:usa:active"}

Number:Dimensionless coronaCriticalUSA "Critical Cases USA [%,d]"
    {channel="coronastats:country:stats:usa:critical"}

Number:Dimensionless coronaTestsUSA "Tests USA [%d]"
    {channel="coronastats:country:stats:usa:cases"}

DateTime coronaUpdatedUSA "Updated USA [%1$tA, %1$td.%1$tm.%1$tY %1$tH:%1$tM]"
    {channel="coronastats:country:stats:usa:updated"}
```

### Sitemap

```perl
Text label="Corona" {
    Frame label="World" {
        Text item=coronaCasesWorld
        Text item=coronaActiveWorld
        Text item=coronaRecoveredWorld
        Text item=coronaDeathsWorld
        Text item=coronaCriticalWorld
        Text item=coronaTestUSA
    }

    Frame label="USA" {
        Text item=coronaCasesUSA
        Text item=coronaActiveUSA
        Text item=coronaRecoveredUSA
        Text item=coronaDeathsUSA
        Text item=coronaCriticalUSA
        Text item=coronaTestsUSA
        Text item=coronaUpdatedUSA
    }
}

```
