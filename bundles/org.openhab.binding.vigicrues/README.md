# VigiCrues Binding


## Supported Things

There is exactly one supported thing type, which represents a river level measurement station.
It is identified by the `id`.

To get your station id :

1- open https://www.vigicrues.gouv.fr/

2- Select your region on the France map

3- Select the station nearest to your location

4- In the 'Info Station' tab you'll get the id just near the station name (e.g. X9999999299)

Of course, you can add multiple Things, e.g. for getting measures for different locations.


## Discovery

This binding does not handle auto-discovery.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                             |
|-----------|-------------------------------------------------------------------------|
| id        | Id of the station.                                                      |
| refresh   | Refresh interval in minutes. Optional, the default value is 30 minutes. |

https://voiprovider.wordpress.com/2019/01/02/pyvigicrues-un-module-python-pour-collecter-les-data-des-cours-deau-en-france/

## Channels

The Météo Alerte information that are retrieved is available as these channels:

| Channel ID      | Item Type                 | Description                   |
|-----------------|---------------------------|-------------------------------|
| observationTime | DateTime                  | Date and time of measurement  |
| flow            | Number:VolumetricFlowRate | Volume of water per time unit |
| height          | Number:Length             | Water height of the river     |


## Full Example

vigicrues.things:

```
Thing vigicrues:station:poissy @ "Station Poissy" [id="H300000201", refresh=30]
```

