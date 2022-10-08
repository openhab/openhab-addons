# Ecowatt Binding

This binding uses the Ecowatt API to expose clear signals to adopt the right gestures and to ensure a good supply of electricity for all in France.

You can find more information about Ecowatt on this [site](https://www.monecowatt.fr).

## Supported Things

This binding supports only one thing type: `signals`.

## Discovery

Discovery is not supported.
You have to add the thing manually.

## Prerequisites before configuration

You must create an account and an application on the RTE portal to obtain the OAuth2 credentials required to access the API.

1. Open this [page](https://data.rte-france.com/catalog/-/api/consumption/Ecowatt/v4.0), find the "Ecowatt" tile and click on the "Abonnez-vous à l'API" button.
2. Create an account by following the instructions (you will receive an email to validate your new account).
3. Once logged in, create an application by entering a name (for example "openHAB Integration"), choosing "Web Server" as type, entering any description of your choice and finally clicking on the "Valider" button.
4. You will then see your application details, in particular the "ID client" and "ID Secret" information which you will need later to set up your binding thing.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

| Name      | Type    | Description                                                           | Required |
|-----------|---------|-----------------------------------------------------------------------|----------|
| idClient  | text    | ID client provided with the application you created in the RTE portal | yes      |
| idSecret  | text    | ID secret provided with the application you created in the RTE portal | yes      |

## Channels

All channels are read-only.

| Channel           | Type   | Description                                                      |
|-------------------|--------|------------------------------------------------------------------|
| todaySignal       | Number | The signal relating to the forecast consumption level for today. Values are 1 for normal consumption (green), 2 for strained electrical system (orange) and 3 for very strained electrical system (red). |
| tomorrowSignal    | Number | The signal relating to the forecast consumption level for tomorrow. Values are 1 for normal consumption (green), 2 for strained electrical system (orange) and 3 for very strained electrical system (red). |
| currentHourSignal | Number | The signal relating to the forecast consumption level for the current hour. Values are 1 for normal consumption (green), 2 for strained electrical system (orange) and 3 for very strained electrical system (red). |

## Full Example

example.things:

```
Thing ecowatt:signals:signals "Ecowatt Signals" [ idClient="xxxxx", idSecret="yyyyy"]
```

example.items:

```
Number TodaySignal "Today [%s]" { channel="ecowatt:signals:signals:todaySignal" }
Number TomorrowSignal "Tomorrow [%s]" { channel="ecowatt:signals:signals:tomorrowSignal" }
Number CurrentHourSignal "Current hour [%s]" { channel="ecowatt:signals:signals:currentHourSignal" }
```

example.sitemap:

```
    Frame label="Ecowatt" {
        Default item=TodaySignal
        Default item=TomorrowSignal
        Default item=CurrentHourSignal
    }
```
