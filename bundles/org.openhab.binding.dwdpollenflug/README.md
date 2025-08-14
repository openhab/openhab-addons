# DWD Pollenflug Binding

The "Deutsche Wetterdienst" (DWD) reports the current pollen count index for Germany on a daily base and a forecast for tomorrow and the day after tomorrow.
This binding allows you to retrieve this data for your region or partregion.
You can find a map of the data in the [DWD Pollenflug map](https://www.dwd.de/DE/leistungen/gefahrenindizespollen/gefahrenindexpollen.html)

## Supported Things

This binding supports a bridge thing (`bridge`), which polls the dataset for Germany in an adjustable interval.
And it supports a region thing (`region`), representing the data for all pollen types of a region or partregion.

## Discovery

This binding adds a default `bridge` thing to the Inbox.
This can be used for `region` things you may add manually.

## Thing Configuration

### Bridge

| Parameter | Default | Required | Description                                                                          |
| --------- | :-----: | :------: | ------------------------------------------------------------------------------------ |
| `refresh` |   30    |    no    | Define the interval for polling the data from DWD in minutes. Minimum is 15 minutes. |

### Region

| Parameter  | Default | Required | Description                                                         |
| ---------- | :-----: | :------: | ------------------------------------------------------------------- |
| `regionID` |    -    |   yes    | The id of the region. The available options are in the table below. |

#### Manual region or partregion selection

The region ID is the partregion_id or if there is no partregion the region_id from the [DWD pollenflug JSON data](https://opendata.dwd.de/climate_environment/health/alerts/s31fg.json).
You can refer to the following table.

| regionID | Region                         | Partregion                                         |
| -------- | ------------------------------ | -------------------------------------------------- |
| 11       | Schleswig-Holstein und Hamburg | Inseln und Marschen                                |
| 12       | Schleswig-Holstein und Hamburg | Geest, Schleswig-Holstein und Hamburg              |
| 20       | Mecklenburg-Vorpommern         | -                                                  |
| 31       | Niedersachsen und Bremen       | Westl. Niedersachsen/Bremen                        |
| 32       | Niedersachsen und Bremen       | Östl. Niedersachsen                                |
| 41       | Nordrhein-Westfalen            | Rhein.-Westfäl. Tiefland                           |
| 42       | Nordrhein-Westfalen            | Ostwestfalen                                       |
| 43       | Nordrhein-Westfalen            | Mittelgebirge NRW                                  |
| 50       | Brandenburg und Berlin         | -                                                  |
| 61       | Sachsen-Anhalt                 | Tiefland Sachsen-Anhalt                            |
| 62       | Sachsen-Anhalt                 | Harz                                               |
| 71       | Thüringen                      | Tiefland Thüringen                                 |
| 72       | Thüringen                      | Mittelgebirge Thüringen                            |
| 81       | Sachsen                        | Tiefland Sachsen                                   |
| 82       | Sachsen                        | Mittelgebirge Sachsen                              |
| 91       | Hessen                         | Nordhessen und hess. Mittelgebirge                 |
| 92       | Hessen                         | Rhein-Main                                         |
| 101      | Rheinland-Pfalz und Saarland   | Rhein, Pfalz, Nahe und Mosel                       |
| 102      | Rheinland-Pfalz und Saarland   | Mittelgebirgsbereich Rheinland-Pfalz               |
| 103      | Rheinland-Pfalz und Saarland   | Saarland                                           |
| 111      | Baden-Württemberg              | Oberrhein und unteres Neckartal                    |
| 112      | Baden-Württemberg              | Hohenlohe/mittlerer Neckar/Oberschwaben            |
| 113      | Baden-Württemberg              | Mittelgebirge Baden-Württemberg                    |
| 121      | Bayern                         | Allgäu/Oberbayern/Bay. Wald                        |
| 122      | Bayern                         | Donauniederungen                                   |
| 123      | Bayern                         | Bayern n. der Donau, o. Bayr. Wald, o. Mainfranken |
| 124      | Bayern                         | Mainfranken                                        |

## Channels

The bridge thing has following channels:

| channel               | description                                    |
| --------------------- | ---------------------------------------------- |
| `updates#refreshed`   | Hold the time of the bridge's last refresh     |
| `updates#last_update` | The time when data was last updated by DWD     |
| `updates#next_update` | The time when data will be updated next by DWD |

For each of the eight pollen types reported by DWD the region thing has three channels for today, tomorrow and the day after tomorrow.

| channels               | pollen type | german name |
| ---------------------- | ----------- | ----------- |
| `alder#today`          | alder       | Erle        |
| `alder#tomorrow`       |             |             |
| `alder#dayafter_to`    |             |             |
| `ambrosia#today`       | ambrosia    | Ambrosia    |
| `ambrosia#tomorrow`    |             |             |
| `ambrosia#dayafter_to` |             |             |
| `ash#today`            | ash-tree    | Esche       |
| `ash#tomorrow`         |             |             |
| `ash#dayafter_to`      |             |             |
| `birch#today`          | birch       | Birke       |
| `birch#tomorrow`       |             |             |
| `birch#dayafter_to`    |             |             |
| `grasses#today`        | grasses     | Gräser      |
| `grasses#tomorrow`     |             |             |
| `grasses#dayafter_to`  |             |             |
| `hazel#today`          | hazel       | Hasel       |
| `hazel#tomorrow`       |             |             |
| `hazel#dayafter_to`    |             |             |
| `mugwort#today`        | mugwort     | Beifuß      |
| `mugwort#tomorrow`     |             |             |
| `mugwort#dayafter_to`  |             |             |
| `rye#today`            | rye         | Roggen      |
| `rye#tomorrow`         |             |             |
| `rye#dayafter_to`      |             |             |

There are the following possible string values:

| value | description                 |
| ----- | --------------------------- |
| -1    | not specified               |
| 0     | no pollen pollution         |
| 0-1   | no to low pollen count      |
| 1     | low pollen count            |
| 1-2   | low to medium pollen count  |
| 2     | medium pollen count         |
| 2-3   | medium to high pollen count |
| 3     | high pollen count           |

## Full Example

### Things file for region "Brandenburg und Berlin" and partregion "Bayern - Mainfranken"

```java
Bridge dwdpollenflug:bridge:dwd "DWD pollen count Bridge" [refresh="15"] {
    Thing region region50 "DWD pollen count region" @ "APIS" [regionID="50"]
    Thing region partregion124 "DWD pollen count partregion" @ "APIS" [regionID="124"]
}
```

### Items example for region "Brandenburg und Berlin" and pollen type ash-tree

```java
String pollenTodayEsche
    "Esche [MAP(pollen.map):%s]"
    {channel="dwdpollenflug:region:dwd:region50:ash#today"}

String pollenTomorrowEsche
    "Esche morgen [MAP(pollen.map):%s]"
    {channel="dwdpollenflug:region:dwd:region50:ash#tomorrow"}

String pollenDayAfterTomorrowEsche
    "Esche übermorgen [MAP(pollen.map):%s]"
    {channel="dwdpollenflug:region:dwd:region50:ash#dayafter_to"}

```

### Transform map file pollen.map

```text
0=keine (0)
0-1=keine bis gering (0-1)
1=gering (1)
1-2=gering bis mittel (1-2)
2=mittel (2)
2-3=mittel bis hoch (2-3)
3=hoch (3)
-1=keine Daten
-=keine Daten
NULL=keine Daten
```

### Sitemap example for region "Brandenburg und Berlin" and pollen type ash-tree

```perl
Text label="Pollenflugindex" {
    Frame {
        Text item=pollenTodayEsche
            valuecolor=[=="3"="#f00014",
                        =="2-3"="#f00014",
                        =="2"="#ff9900",
                        =="1-2"="#ff9900",
                        =="1"="#ffff00",
                        =="0-1"="#00c83c"] {
            Frame {
                Text item=pollenTodayEsche
                    valuecolor=[=="3"="#f00014",
                                =="2-3"="#f00014",
                                =="2"="#ff9900",
                                =="1-2"="#ff9900",
                                =="1"="#ffff00",
                                =="0-1"="#00c83c"]

                Text item=pollenTomorrowEsche
                    valuecolor=[=="3"="#f00014",
                                =="2-3"="#f00014",
                                =="2"="#ff9900",
                                =="1-2"="#ff9900",
                                =="1"="#ffff00",
                                =="0-1"="#00c83c"]

                Text item=pollenDayAfterTomorrowEsche
                    valuecolor=[=="3"="#f00014",
                                =="2-3"="#f00014",
                                =="2"="#ff9900",
                                =="1-2"="#ff9900",
                                =="1"="#ffff00",
                                =="0-1"="#00c83c"]
            }
        }
    }
}
```
