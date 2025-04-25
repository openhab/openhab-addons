# VigiCrues Binding

This binding allows you to get data regarding water flow and water height on major French rivers.
These data are made public through OpenDataSoft website.

## Supported Things

There is exactly one supported thing type, which represents a river level measurement station.
It is identified by the `id`.

To get your station id:

1. open <https://www.vigicrues.gouv.fr/>

1. Select your region on the France map

1. Select the station nearest to your location

1. In the 'Info Station' tab you'll get the id just near the station name (e.g. X9999999299)

Of course, you can add multiple Things, e.g. for getting measures for different locations.

## Discovery

You can discover stations based upon the system location.
Select Vigicrues binding and click scan in order to discover new stations.
The first scan will proceed with stations located in a radius of 10 km.
This radius will increase by 10 km at each new scan.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                             |
|-----------|-------------------------------------------------------------------------|
| id        | Id of the station.                                                      |
| refresh   | Refresh interval in minutes. Optional, the default value is 30 minutes. |

## Channels

Once created, at first initialization, the thing will discover its capabilities (available data) using the webservices apis.
Channels will be presented depending upon actual available data.

The VigiCrues information that retrieved are made available with these channels:

| Channel ID       | Item Type                 | Description                                                |
|------------------|---------------------------|------------------------------------------------------------|
| observation-time | DateTime                  | Date and time of measurement                               |
| flow             | Number:VolumetricFlowRate | Volume of water per time unit                              |
| height           | Number:Length             | Water height of the river                                  |
| relative-height  | Number:Dimensionless      | Current water level toward lowest historical flood         |
| relative-flow    | Number:Dimensionless      | Current water flow tower lowest historical flood           |
| alert (*)        | Number                    | Flooding alert level of the portion related to the station |
| alert-icon       | Image                     | Pictogram associated to the alert level                    |
| short-comment    | String                    | Description of the alert level                             |
| comment          | String                    | Detailed informations regarding the ongoing event          |

(*) Each alert level is described by a color:

| Code | Color  | Description                               |
|------|--------|-------------------------------------------|
| 0    | Green  | No particular vigilance                   |
| 1    | Yellow | Be attentive to the flooding situation    |
| 2    | Orange | Be "very vigilant" in the concerned areas |
| 3    | Red    | Absolute vigilance required               |

## Full Example

vigicrues.things:

```java
Thing vigicrues:station:poissy "Station Poissy" @ "VigiCrues" [id="H300000201", refresh=30]
Thing vigicrues:station:vernon "Station Vernon" @ "VigiCrues" [id="H320000104", refresh=30]
```

vigicrues.items:

```java
Group gVigiCrues "VigiCrues" <flow>
    Number:Length VC_hauteur "Hauteur Eau Poissy [%.2f %unit%]"  <none> (gVigiCrues) {channel="vigicrues:station:poissy:height"}
    Number:VolumetricFlowRate VC_debit "Débit Eau Poissy [%.2f %unit%]" <flow> (gVigiCrues) {channel="vigicrues:station:poissy:flow"}
    DateTime VC_ObservationPTS "Timestamp [%1$tH:%1$tM]" <time> (gVigiCrues) {channel="vigicrues:station:poissy:observation-time" }
```

vigicrues.sitemap:

```perl
sitemap vigicrues label="VigiCrues" {
    Frame {
        Default item=VC_hauteur
        Default item=VC_debit
        Default item=VC_ObservationPTS
    }
}
```
