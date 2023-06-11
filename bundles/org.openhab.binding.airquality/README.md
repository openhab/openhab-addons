# Air Quality Binding

This binding uses the [AQIcn.org service](https://aqicn.org) for providing air quality information for any location worldwide.

The World Air Quality Index project is a social enterprise project started in 2007.
Its mission is to promote Air Pollution awareness and provide a unified Air Quality information for the whole world.

The project is proving a transparent Air Quality information for more than 70 countries, covering more than 9000 stations in 600 major cities, via those two websites: [aqicn.org](https://aqicn.org) and [waqi.info](https://waqi.info).

To use this binding, you first need to [register and get your API token](https://aqicn.org/data-platform/token/).

## Supported Things

Bridge: The binding supports a bridge to connect to the [AQIcn.org service](https://aqicn.org). A bridge uses the thing ID "api".

Station: Represents the air quality information for an observation location.

Of course, you can add multiple Stations, e.g. for measuring AQI for different locations.

## Discovery

Local Air Quality can be autodiscovered based on system location.
You will created a Bridge with your apiKey.

## Bridge Configuration

The bridge configuration only holds the api key :

| Parameter | Description                                                             |
|-----------|-------------------------------------------------------------------------|
| apiKey    | Data-platform token to access the AQIcn.org service. Mandatory.         |

## Thing Configuration

The 'Station' thing has a few configuration parameters:

| Parameter | Description                                                             |
|-----------|-------------------------------------------------------------------------|
| location  | Geo coordinates to be considered by the service.                        |
| stationId | Unique ID of the measuring station.                                     |
| refresh   | Refresh interval in minutes. Optional, the default value is 60 minutes. |

For the location parameter, the following syntax is allowed (comma separated latitude and longitude):

```java
37.8,-122.4
37.8255,-122.456
```

If you always want to receive data from specific station and you know its unique ID, you can enter it instead of the coordinates.

This `stationId` can be found by using the following link:
`https://api.waqi.info/search/?token=TOKEN&keyword=NAME`, replacing TOKEN by your apiKey and NAME by the station you are looking for.

### Thing properties

Once created, at first execution, the station's properties will be filled with informations gathered from the web service :

- Nearest measuring station location
- Measuring station ID
- Latitude/longitude of measuring station

## Channels

The AirQuality information that is retrieved for a given is available as these channels:

### AQI Channels Group - Global Results

| Channel ID      | Item Type            | Description                                  |
|-----------------|----------------------|----------------------------------------------|
| alert-level     | Number               | Alert level (*) associated to AQI Index.     |
| index           | Number               | Air Quality Index                            |
| timestamp       | DateTime             | Observation date and time                    |
| dominent        | String               | Dominent Pollutant                           |
| icon            | Image                | Pictogram associated to alert-level          |
| color           | Color                | Color associated to alert level.             |

### Weather Channels Group

| Channel ID      | Item Type            | Description                                  |
|-----------------|----------------------|----------------------------------------------|
| temperature     | Number:Temperature   | Temperature in Celsius degrees               |
| pressure        | Number:Pressure      | Pressure level                               |
| humidity        | Number:Dimensionless | Humidity level                               |
| dew-point       | Number:Temperature   | Dew point temperature                        |
| wind-speed      | Number:Speed         | Wind speed                                   |

### Pollutants Channels Group

For each pollutant (PM25, PM10, O3, NO2, CO, SO2) , depending upon availability of the station,
you will be provided with the following informations

| Channel ID      | Item Type            | Description                                  |
|-----------------|----------------------|----------------------------------------------|
| value           | Number:Density       | Measured density of the pollutant            |
| index           | Number               | AQI Index of the single pollutant            |
| alert-level     | Number               | Alert level associate to the index           |

(*) The alert level is described by a color :

| Code | Color  | Description                    |
|------|--------|--------------------------------|
| 0    | Green  | Good                           |
| 1    | Yellow | Moderate                       |
| 2    | Orange | Unhealthy for Sensitive Groups |
| 3    | Red    | Unhealthy                      |
| 4    | Purple | Very Unhealthy                 |
| 5    | Maroon | Hazardous                      |

## Full Example

airquality.things:

```java
Bridge airquality:api:main "Bridge" [apiKey="xxxyyyzzz"] {
    station home "Krakow"[location="50.06465,19.94498", refresh=60]
}
```

airquality.items:

```java
Group AirQuality <flow>

Number   Aqi_Level           "Air Quality Index" <flow> (AirQuality) { channel="airquality:station:main:home:aqi#index" }
Number   Aqi_Pm25            "PM\u2082\u2085 Level" <line> (AirQuality) { channel="airquality:station:main:home:pm25#value" }
Number   Aqi_Pm10            "PM\u2081\u2080 Level" <line> (AirQuality) { channel="airquality:station:main:home:pm10#value" }
Number   Aqi_O3              "O\u2083 Level" <line> (AirQuality) { channel="airquality:station:main:home:o3#value" }
Number   Aqi_No2             "NO\u2082 Level" <line> (AirQuality) { channel="airquality:station:main:home:no2#value" }
Number   Aqi_Co              "CO Level" <line> (AirQuality) { channel="airquality:station:main:home:co#value" }
Number   Aqi_So2             "SO\u2082 Level" <line> (AirQuality) { channel="airquality:station:main:home:so2#value" }

DateTime Aqi_ObservationTime "Time of observation [%1$tH:%1$tM]" <clock> (AirQuality) { channel="airquality:station:main:home:aqi#timestamp" }

Number:Temperature  Aqi_Temperature     "Temperature" <temperature> (AirQuality) { channel="airquality:station:main:home:weather#temperature" }
Number:Pressure     Aqi_Pressure        "Pressure" <pressure> (AirQuality) { channel="airquality:station:main:home:weather#pressure" }
Number:Dimensionless Aqi_Humidity        "Humidity" <humidity> (AirQuality) { channel="airquality:station:main:home:weather#humidity" }
```

airquality.sitemap:

```perl
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
                =="UNHEALTHY_FSG"="#ff9933",
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
        Text item=Aqi_So2
    }

    Frame {
        Text item=Aqi_ObservationTime
        Text item=Aqi_Temperature
        Text item=Aqi_Pressure
        Text item=Aqi_Humidity
    }

}

```

airquality.rules:

```java
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
        case "UNHEALTHY_FSG":
            hsb = "30,80,100"
        case "MODERATE":
            hsb = "50,80,100"
        case "GOOD":
            hsb = "160,100,60"
    }

    Lamp_Color.sendCommand(hsb)
end
```
