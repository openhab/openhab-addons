# Air Quality Binding

This binding uses the [AQIcn.org service](http://aqicn.org) for providing air quality information for any location worldwide.

The World Air Quality Index project is a social enterprise project started in 2007. Its mission is to promote Air Pollution awareness and provide a unified Air Quality information for the whole world.

The project is proving a transparent Air Quality information for more than 70 countries, covering more than 9000 stations in 600 major cities, via those two websites: [aqicn.org](http://aqicn.org) and [waqi.info](http://waqi.info).

To use this binding, you first need to [register and get your API token](http://aqicn.org/data-platform/token/).

## Supported Things

There is exactly one supported thing type, which represents the air quality information for an observation location.
It has the `aqi` id.
Of course, you can add multiple Things, e.g. for measuring AQI for different locations.

## Discovery

There is no discovery implemented.
You have to create your things manually.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                             |
|-----------|-------------------------------------------------------------------------|
| apikey    | Data-platform token to access the AQIcn.org service. Mandatory.         |
| location  | Geo coordinates to be considered by the service.                        |
| stationId | Unique ID of the measuring station.                                     |
| refresh   | Refresh interval in minutes. Optional, the default value is 60 minutes. |


For the location parameter, the following syntax is allowed (comma separated latitude and longitude):

```
37.8,-122.4
37.8255,-122.456
```

If you always want to receive data from specific station and you know its unique ID, you can enter it instead of the coordinates.


## Channels

The AirQuality information that is retrieved is available as these channels:


| Channel ID      | Item Type | Description                                  |
|-----------------|-----------|----------------------------------------------|
| aqiLevel        | Number    | Air Quality Index                            |
| aqiDescription  | String    | AQI Description                              |
| locationName    | String    | Nearest measuring station location           |
| stationId       | Number    | Measuring station ID                         |
| stationLocation | Location  | Latitude/longitude of measuring station      |
| pm25            | Number    | Fine particles pollution level (PM2.5)       |
| pm10            | Number    | Coarse dust particles pollution level (PM10) |
| o3              | Number    | Ozone level (O3)                             |
| no2             | Number    | Nitrogen Dioxide level (NO2)                 |
| co              | Number    | Carbon monoxide level (CO)                   |
| observationTime | DateTime  | Observation date and time                    |
| temperature     | Number    | Temperature in Celsius degrees               |
| pressure        | Number    | Pressure level                               |
| humidity        | Number    | Humidity level                               |


`AQI Description` item provides a human-readable output that can be interpreted e.g. by MAP transformation.

*Note that channels like* `pm25`, `pm10`, `o3`, `no2`, `co` *can sometimes return* `UNDEF` *value due to the fact that some stations don't provide measurements for them.*


## Full Example

airquality.map:

```
-=-
UNDEF=No data
NULL=No data
NO_DATA=No data
GOOD=Good
MODERATE=Moderate
UNHEALTHY_FOR_SENSITIVE=Unhealthy for sensitive groups
UNHEALTHY=Unhealthy
VERY_UNHEALTHY=Very unhealthy
HAZARDOUS=Hazardous
```

airquality.things:

```
airquality:aqi:home "AirQuality" @ "Krakow" [ apikey="XXXXXXXXXXXX", location="50.06465,19.94498", refresh=60 ]
airquality:aqi:warsaw "AirQuality in Warsaw" [ apikey="XXXXXXXXXXXX", location="52.22,21.01", refresh=60 ]
airquality:aqi:brisbane "AirQuality in Brisbane" [ apikey="XXXXXXXXXXXX", stationId=5115 ]
```

airquality.items:

```
Group AirQuality <flow>

Number   Aqi_Level           "Air Quality Index" <flow> (AirQuality) { channel="airquality:aqi:home:aqiLevel" }
String   Aqi_Description     "AQI Level [MAP(airquality.map):%s]" <flow> (AirQuality) { channel="airquality:aqi:home:aqiDescription" }

Number   Aqi_Pm25            "PM\u2082\u2085 Level" <line> (AirQuality) { channel="airquality:aqi:home:pm25" }
Number   Aqi_Pm10            "PM\u2081\u2080 Level" <line> (AirQuality) { channel="airquality:aqi:home:pm10" }
Number   Aqi_O3              "O\u2083 Level" <line> (AirQuality) { channel="airquality:aqi:home:o3" }
Number   Aqi_No2             "NO\u2082 Level" <line> (AirQuality) { channel="airquality:aqi:home:no2" }
Number   Aqi_Co              "CO Level" <line> (AirQuality) { channel="airquality:aqi:home:co" }

String   Aqi_LocationName    "Measuring Location" <settings> (AirQuality) { channel="airquality:aqi:home:locationName" }
Location Aqi_StationGeo      "Station Location" <office> (AirQuality) { channel="airquality:aqi:home:stationLocation" }
Number   Aqi_StationId       "Station ID" <pie> (AirQuality) { channel="airquality:aqi:home:stationId" }
DateTime Aqi_ObservationTime "Time of observation [%1$tH:%1$tM]" <clock> (AirQuality) { channel="airquality:aqi:home:observationTime" }

Number   Aqi_Temperature     "Temperature" <temperature> (AirQuality) { channel="airquality:aqi:home:temperature" }
Number   Aqi_Pressure        "Pressure" <pressure> (AirQuality) { channel="airquality:aqi:home:pressure" }
Number   Aqi_Humidity        "Humidity" <humidity> (AirQuality) { channel="airquality:aqi:home:humidity" }
```

airquality.sitemap:

```
sitemap airquality label="Air Quality" {
    Frame {
        Text item=Aqi_Level valuecolor=[
                Aqi_Level=="-"="lightgray",
                Aqi_Level>=300="#7e0023",
                >=201="#660099",
                >=151="#cc0033",
                >=101="#ff9933",
                >=51="#ffde33",
                >=0="#009966"
            ]
        Text item=Aqi_Description valuecolor=[
                Aqi_Description=="HAZARDOUS"="#7e0023",
                =="VERY_UNHEALTHY"="#660099",
                =="UNHEALTHY"="#cc0033",
                =="UNHEALTHY_FOR_SENSITIVE"="#ff9933",
                =="MODERATE"="#ffde33",
                =="GOOD"="#009966"
            ]
    }

    Frame {
        Text item=Aqi_Pm25
        Text item=Aqi_Pm10
        Text item=Aqi_O3
        Text item=Aqi_No2
        Text item=Aqi_Co
    }

    Frame {
        Text item=Aqi_LocationName
        Text item=Aqi_ObservationTime
        Text item=Aqi_Temperature
        Text item=Aqi_Pressure
        Text item=Aqi_Humidity
    }

    Frame label="Station Location" {
        Mapview item=Aqi_StationGeo height=10
    }
}

```

airquality.rules:

```
rule "Change lamp color to reflect Air Quality"
when
    Item Aqi_Description changed
then
    var String hsb

    switch Aqi_Description.state {
        case "HAZARDOUS":
            hsb = "343,100,49"
        case "VERY_UNHEALTHY":
            hsb = "280,100,60"
        case "UNHEALTHY":
            hsb = "345,100,80"
        case "UNHEALTHY_FOR_SENSITIVE":
            hsb = "30,80,100"
        case "MODERATE":
            hsb = "50,80,100"
        case "GOOD":
            hsb = "160,100,60"
    }

    sendCommand(Lamp_Color, hsb)
end
```
