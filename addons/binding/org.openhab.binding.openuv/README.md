# Open UV Binding

This binding uses the [OpenUV Index API service](https://www.openuv.io/) for providing UV Index information for any location worldwide.

To use this binding, you first need to [register and get your API token](https://www.openuv.io/auth/google).

## Supported Things

## Discovery

Local UV Index informations can be autodiscovered based on system location.
You'll have to complete default configuration with your apiKey.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                             |
|-----------|-------------------------------------------------------------------------|
| apikey    | Data-platform token to access the AQIcn.org service. Mandatory.         |
| location  | Geo coordinates to be considered by the service.                        |
| refresh   | Refresh interval in minutes. Optional, the default value is 60 minutes. |

For the location parameter, the following syntax is allowed (comma separated latitude, longitude and optional altitude):

```java
37.8,-122.4
37.8255,-122.456
37.8,-122.4,177
```

## Channels

The OpenUV information that is retrieved is available as these channels:

| Channel ID      | Item Type            | Description                                  |
|-----------------|----------------------|----------------------------------------------|
| Current         | UVIndex              | Number | UV Index |

## Full Example

airquality.map:

```text
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

```java
airquality:aqi:home "AirQuality" @ "Krakow" [ apikey="XXXXXXXXXXXX", location="50.06465,19.94498", refresh=60 ]
airquality:aqi:warsaw "AirQuality in Warsaw" [ apikey="XXXXXXXXXXXX", location="52.22,21.01", refresh=60 ]
airquality:aqi:brisbane "AirQuality in Brisbane" [ apikey="XXXXXXXXXXXX", stationId=5115 ]
```

airquality.items:

```java
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

Number:Temperature  Aqi_Temperature     "Temperature" <temperature> (AirQuality) { channel="airquality:aqi:home:temperature" }
Number:Pressure     Aqi_Pressure        "Pressure" <pressure> (AirQuality) { channel="airquality:aqi:home:pressure" }
Number:DimensionLess Aqi_Humidity        "Humidity" <humidity> (AirQuality) { channel="airquality:aqi:home:humidity" }
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
