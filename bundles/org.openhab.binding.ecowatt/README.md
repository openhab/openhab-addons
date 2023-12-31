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

1. Open this [page](https://data.rte-france.com/catalog/-/api/consumption/Ecowatt/v5.0), find the "Ecowatt" tile and click on the "Abonnez-vous à l'API" button.
1. Create an account by following the instructions (you will receive an email to validate your new account).
1. Once logged in, create an application by entering a name (for example "openHAB Integration"), choosing "Web Server" as type, entering any description of your choice and finally clicking on the "Valider" button.
1. You will then see your application details, in particular the "ID client" and "ID Secret" information which you will need later to set up your binding thing.

Note that you are subscribed to a particular version of the API.
When a new version of the API is released, you will have to subscribe to this new version and create a new application.
You will then get new information "ID client" and "ID Secret" and you will have to update your thing configuration in openHAB.
After changing version, you will have to wait for your authentication token to be renewed (max 2 hours) to get a successful response from the API.

## Binding Configuration

There are no overall binding configuration settings that need to be set.
All settings are through thing configuration parameters.

## Thing Configuration

| Name       | Type    | Description                                                               | Required | Default |
|------------|---------|---------------------------------------------------------------------------|----------|---------|
| apiVersion | integer | The version of the Ecowatt tile to which you subscribed in the RTE portal | no       | 4       |
| idClient   | text    | ID client provided with the application you created in the RTE portal     | yes      |         |
| idSecret   | text    | ID secret provided with the application you created in the RTE portal     | yes      |         |

Take care to select the API version corresponding to the one to which you subscribed in the RTE portal.

## Channels

All channels are read-only.

| Channel           | Type   | Description                                                      |
|-------------------|--------|------------------------------------------------------------------|
| todaySignal       | Number | The signal relating to the forecast consumption level for today. Values are 1 for normal consumption (green), 2 for strained electrical system (orange) and 3 for very strained electrical system (red). |
| tomorrowSignal    | Number | The signal relating to the forecast consumption level for tomorrow. Values are 1 for normal consumption (green), 2 for strained electrical system (orange) and 3 for very strained electrical system (red). |
| currentHourSignal | Number | The signal relating to the forecast consumption level for the current hour. Values are 0 for normal consumption (green) and carbon-free production, 1 for normal consumption (green), 2 for strained electrical system (orange) and 3 for very strained electrical system (red). |

## Full Example

example.things:

```java
Thing ecowatt:signals:signals "Ecowatt Signals" [ apiVersion=4, idClient="xxxxx", idSecret="yyyyy"]
```

example.items:

```java
Number TodaySignal "Today [%s]" { channel="ecowatt:signals:signals:todaySignal" }
Number TomorrowSignal "Tomorrow [%s]" { channel="ecowatt:signals:signals:tomorrowSignal" }
Number CurrentHourSignal "Current hour [%s]" { channel="ecowatt:signals:signals:currentHourSignal" }
```

example.sitemap:

```perl
    Frame label="Ecowatt" {
        Default item=TodaySignal
        Default item=TomorrowSignal
        Default item=CurrentHourSignal
    }
```
