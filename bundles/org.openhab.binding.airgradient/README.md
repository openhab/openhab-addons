# AirGradient Binding

AirGradient provide open source and open hardware air quality monitors.

This binding reads air quality data from the the AirGradient (https://www.airgradient.com/) API.

This API is documented at https://api.airgradient.com/public/docs/api/v1/

## Supported Things

This binding supports all the different AirGradient sensors, with most of the data. If you are missing something; please let the author know.

- `bridge`: Connection to the API
- `location`: Location in the API to read values for

## Discovery

Auto-discovery is not implemented, but autodiscovery of locations is feasible in the future.

## Thing Configuration

The connection to the API needs setup and configuration

1. Log in to the AirGradient Dashboard: https://app.airgradient.com/dashboard
2. Navigate to Place->Connectivity Settings from the upper left hamburger menu.
3. Enable API access, and take a copy of the Token, which will be used in the token setting to configure the connection to the API.

To add a location, you need to know the location ID. To get the location ID, you

1. Log in to the AirGradient Dashboard: https://app.airgradient.com/dashboard
2. Navigate to Locations from the upper left hamburger menu.
3. Here you will find a list of all of your sensors, with a location ID in the left column. Use that id when you add new Location things.

This binding supports changing the hostname you are reading the data from, and supports reading a single object instead of an array of objects.
This makes it possible to use this binding to read from the sensor on your local network instead of from the API if you flash your own AirGradient sensor with a web server that return the postToServer() data on GET requests.

### `API` Thing Configuration

| Name            | Type    | Description                           | Default | Required | Advanced |
|-----------------|---------|---------------------------------------|---------|----------|----------|
| token           | text    | Token to access the device            | N/A     | yes     | no        |
| hostname        | text    | Hostname or IP address of the API     | https://api.airgradient.com/      | no      | yes      |
| refreshInterval | integer | Interval the device is polled in sec. | 600     | no       | yes      |

### `Location` Thing Configuration

| Name            | Type    | Description                                                       | Default | Required | Advanced |
|-----------------|---------|-------------------------------------------------------------------|---------|----------|----------|
| location        | text    | A number identifying the location id in the AirGradient Dashboard | N/A     | yes     | no        |

## Channels

For more information about the data in the channels, please refer to the models in https://api.airgradient.com/public/docs/api/v1/

| Channel    | Type                 | Read/Write | Description                                                                      |
|------------|----------------------|------------|----------------------------------------------------------------------------------|
| pm01       | Number:Density       | Read       | Particulate Matter 1 (0.001mm)                                                   |
| pm02       | Number:Density       | Read       | Particulate Matter 2 (0.002mm)                                                   |
| pm10       | Number:Density       | Read       | Particulate Matter 10 (0.01mm)                                                   |
| pm003Count | Switch               | Read       | The number of particles with a diameter beyond 0.3 microns in 1 deciliter of air |
| rco2       | Number:Density       | Read       | Carbon dioxide PPM                                                               |
| tvoc       | Number:Density       | Read       | Total Volatile Organic Compounds                                                 |
| atmp       | Number:Temperature   | Read       | Ambient Temperature                                                              |
| rhum       | Number:Dimensionless | Read       | Relative Humidity Percentage                                                     |
| wifi       | Number               | Read       | Received signal strength indicator                                               |

## Full Example

### Thing Configuration

```java
Bridge airgradient:airgradientapi:home "My Home" [ token="abc123...." ] {
    Thing location      "654321"               "Outside"      [ location="654321" ]
}
```

### Item Configuration

```java
Number:Density      AirGradient_Location_PM2        "%.0f kg/m³"                         <density>       {channel="airgradient:location:654321:pm2"}"
Number:Temperature  AirGradient_Location_PM2        "Temperature [%.1f °C]"              <temperature>   {channel="airgradient:location:654321:atmp"}"
```


