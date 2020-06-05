# VigiCrues Binding

This binding allows you to get data regarding water flow and water height on major French rivers.
These data are made public through OpenDataSoft website. 

## Supported Things

There is exactly one supported thing type, which represents a river level measurement station.
It is identified by the `id`.

To get your station id :

1. open https://www.vigicrues.gouv.fr/

2. Select your region on the France map

3. Select the station nearest to your location

4. In the 'Info Station' tab you'll get the id just near the station name (e.g. X9999999299)

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


## Channels

The VigiCrues information that retrieved are made available with these channels:

| Channel ID       | Item Type                 | Description                   |
|------------------|---------------------------|-------------------------------|
| observation-time | DateTime                  | Date and time of measurement  |
| flow             | Number:VolumetricFlowRate | Volume of water per time unit |
| height           | Number:Length             | Water height of the river     |


## Full Example

vigicrues.things:

```
Thing vigicrues:station:poissy "Station Poissy" @ "VigiCrues" [id="H300000201", refresh=30]
Thing vigicrues:station:vernon "Station Vernon" @ "VigiCrues" [id="H320000104", refresh=30]
```

vigicrues.items:

```
Group gVigiCrues "VigiCrues" <flow>
    Number:Length VC_hauteur "Hauteur Eau Poissy [%.2f %unit%]"  <none> (gVigiCrues) {channel="vigicrues:station:poissy:height"}
    Number:VolumetricFlowRate VC_debit "Débit Eau Poissy [%.2f %unit%]" <flow> (gVigiCrues) {channel="vigicrues:station:poissy:flow"}
    DateTime VC_ObservationPTS "Timestamp [%1$tH:%1$tM]" <time> (gVigiCrues) {channel="vigicrues:station:poissy:observation-time" }
```

vigicrues.sitemap:

```
sitemap vigicrues label="VigiCrues" {
    Frame {
        Default item=VC_hauteur
        Default item=VC_debit
        Default item=VC_ObservationPTS
    }
}
```
