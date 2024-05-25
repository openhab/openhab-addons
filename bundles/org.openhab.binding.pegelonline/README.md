# PegelOnline Binding

Binding to observe water level from german rivers. 
Data is provided by german **Water-Route and Shipping Agency** [WSV](https://www.pegelonline.wsv.de/).
Goal is to monitor actual water levels from rivers nearby your home. 
In case of changing water levels the corresponding warning level is lowered or raised.

## Supported Things

| Label               | Description                                                                     | ID      |
|---------------------|---------------------------------------------------------------------------------|---------|
| Measurement Station | Station providing water level measurements                                      | station |


## Discovery

In case your home location coordinates are set the discovery will recognize all measurement stations within a radius of 50 km.
Found Things are added in your Inbox.


## Thing Configuration

Thing configuration contains 3 sections

* Station selection
* Warning Levels of selected station
* Refresh rate

### Station selection

Stations can be selected with an Universally Unique Identifier (uuid). 
It's automatically added by the Discovery. 
Configure a station manually with the [list of all available stations](https://www.pegelonline.wsv.de/webservices/rest-api/v2/stations.json) and choose the uuid of your desired measurement station.

### Warning Levels

<img align="right" src="./doc/Marburg.png" width="450" height="500"/>

Each station has specific warning levels

* Warning Levels 1 to 3
* Flooding Levels frequent, occasionally and extreme

Unfortunately these levels cannot be queried automatically. 
Please select your [federal state](https://www.hochwasserzentralen.de/) and check if which levels they provide.
The picture shows the levels of [measurement station Marburg of federal state Hesse](https://www.hlnug.de/static/pegel/wiskiweb2/stations/25830056/station.html?v=20210802152952)

| configuration    | content   | unit | description               |
|------------------|-----------|------|---------------------------|
| uuid             | text      |  -   | Unique Station Identifier |
| warningLevel1    | integer   |  cm  | Warning Level 1           |
| warningLevel2    | integer   |  cm  | Warning Level 2           |
| warningLevel3    | integer   |  cm  | Warning Level 3           |
| hq10             | integer   |  cm  | Frequent Flooding Level   |
| hq100            | integer   |  cm  | Century Flooding Level    |
| hqEx             | integer   |  cm  | Extreme Flooding Level    |
| refreshInterval  | integer   |  min | Refresh Interval          |

## Channels


| channel              | type                 | description                    |
|----------------------|----------------------|--------------------------------|
| timestamp            | DateTime             | Timestamp of Last Measurement  |
| measure              | Number:Length        | Water Level in cm              |
| trend                | String               | Water Level Trend              |
| level                | String               | Actual Level                   |
| warning-levels       | Number:Dimensionless | Available Warning Levels       |
| actual-warning-level | Number:Dimensionless | Actual Warning Level           |

### Trend

Possible values:

* Rising
* Constant
* Lowering
* Unknown

### Level

Possible values:

* Low
* Normal
* High
* Unknown


## Full Example

```
Thing pegelonline:station:giessen "Measurement Station Giessen" [uuid="4b386a6a-996e-4a4a-a440-15d6b40226d4", refreshInterval=15, warningLevel1=550, warningLevel2=600, warningLevel3=650, hq10=732, hq100=786]
```

## Links

[PegelOnine API Documentation](https://www.pegelonline.wsv.de/webservice/dokuRestapi#caching)

