# Open UV Binding

This binding uses the [OpenUV Index API service](https://www.openuv.io/) for providing UV Index information for any location worldwide.

To use this binding, you first need to [register and get your API token](https://www.openuv.io/auth/google).

## Supported Things

## Discovery

Once a bridge with the api Key has been created, Local UV Index informations can be autodiscovered based on system location.

## Binding Configuration

The binding has no configuration options, all configuration is done at Bridge and Thing level.

## Bridge Configuration

The bridge has only one configuration parameter :

| Parameter | Description                                                  |
|-----------|--------------------------------------------------------------|
| apikey    | Data-platform token to access the OpenUV service. Mandatory. |

## Thing Configuration

The thing has a few configuration parameters :

| Parameter | Description                                                  |
|-----------|--------------------------------------------------------------|
| location  | Geo coordinates to be considered by the service.             |
| refresh   | Refresh interval in minutes. Optional.                       |

For the location parameter, the following syntax is allowed (comma separated latitude, longitude and optional altitude):

```java
37.8,-122.4
37.8255,-122.456
37.8,-122.4,177
```

## Channels

The OpenUV Report thing that is retrieved has these channels:

| Channel ID   | Item Type   | Description                                    |
|--------------|-------------|------------------------------------------------|
| UVIndex      | Number      | UV Index                                       |
| UVColor      | Color       | Color associated to given UV Index.            |
| UVMax        | Number      | Max UV Index for the day (at solar noon)       |
| UVMaxTime    | DateTime    | Max UV Index datetime (solar noon)             |
| Ozone        | Number      | Ozone level in du (Dobson Units) from OMI data |
| OzoneTime    | DateTime    | Latest OMI ozone update datetime               |
| UVTime       | DateTime    | UV Index datetime                              |
| SafeExposure | Number:Time | Safe exposure time for Fitzpatrick Skin Types  |

## Examples

demo.things:

```xtend
Bridge openuv:openuvapi:local "OpenUV Api" [ apikey="xxxxYYYxxxx" ] {
    Thing uvreport city1 "UV In My City" [ location="52.5200066,13.4049540", refresh=10 ]
}

```

demo.items:

```xtend
Number UVIndex                  "UV Index"   { channel = "openuv:uvreport:local:city1:UVIndex" }   
Number UVMax                    "UV Max"  { channel = "openuv:uvreport:local:city1:UVMaxEvent" }   
Number Ozone                    "Ozone"  { channel = "openuv:uvreport:local:city1:Ozone" }   
```

