# Foobot Binding

This binding fetches the Indoor Air Quality data of each of your Foobot devices from the Foobot cloud service.

To use this binding, you first need to [register and get your API key](https://api.foobot.io/apidoc/index.html).

## Supported Things

The binding supports the following things:

| Thing type  | Name
|-------------|------------------------------------------
| account     | The bridge with connection configuration
| device      | The sensor thing

## Discovery

The binding requires you to have a Foobot account and an API key.
The discovery process is able to automatically discover all devices associated with your Foobot account.

## Bridge Configuration

Bridge has the following configuration parameters:

| Parameter        | Description                                           | Required
|------------------|-------------------------------------------------------|----------
| apikey           | API Key from https://api.foobot.io/apidoc/index.html  | Mandatory
| username         | The e-mail address used to log into the Foobot App    | Mandatory
| refreshInterval  | Refresh interval in minutes, minimal 5 minutes        | Optional, the default value is 7 minutes.

The minimal refresh rate is 5 minutes because the device only sends data every 5 minutes.

## Channels

The bridge has one channel:

| Channel ID           | Item Type | Description
|----------------------|-----------|-----------------------------------------------
| apiKeyLimitRemaining | Number    | The remaining number of API requests for today


The AirQuality senors information that is retrieved is available as these channels:

| Channel ID        | Item Type            | Description
|-------------------|----------------------|---------------------------------------
| pm                | Number:Density       | Particulate Matter level (ug/m3)
| temperature       | Number:Temperature   | Temperature in Celsius or Fahrenheit
| humidity          | Number:Dimensionless | Humidity level (%)
| co2               | Number:Dimensionless | Carbon diOxide level (ppm)
| voc               | Number:Dimensionless | Volatile Organic Compounds level (ppb)
| gpi               | Number:Dimensionless | Global Pollution index (%)

## Full Example

demo.things:

```
// Bridge configuration:
foobotbinding:foobot:account [apiKey="XXXXXX", username="XXXXXX", refreshInterval=7]
```

demo.items:

```
Number:Temperature Temperature "Temperature" <temperature> { channel= "foobotbinding:foobot:mac:temperature" }
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Frame label="mac1"  {
                Text item=Foobot_mac1_Temp label="Temperature"
                Text item=Foobot_mac1_Hum label="Humidity"
                Text item=Foobot_mac1_GPI label="Global Pollution Index"
    }
    Frame label="mac2"  {
                Text item=Foobot_mac2_VOC label="Volatile Compounds"
                Text item=Foobot_mac2_CO2 label="Carbon Dioxide"
                Text item=Foobot_mac2_GPI label="Global Pollution Index"
    }
}
```
