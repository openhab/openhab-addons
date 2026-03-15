# Foobot Binding

This binding fetches the Indoor Air Quality data of each of your Foobot devices from the Foobot cloud service.

To use this binding, you first need to [register and get your API key](https://api.foobot.io/apidoc/index.html).
The api is rate limited to 200 calls per day. If you need a higher rate limit please contact Foobot.

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

| Parameter        | Description                                            | Required
|------------------|--------------------------------------------------------|----------
| apikey           | API Key from <https://api.foobot.io/apidoc/index.html> | Mandatory
| username         | The e-mail address used to log into the Foobot App     | Mandatory
| refreshInterval  | Refresh interval in minutes, minimal 5 minutes         | Optional, the default value is 8 minutes.

The minimal refresh rate is 5 minutes because the device only sends data every 5 minutes.
The default is 8 minutes. This will get you through the day with the default rate limit of 200 calls per day.

## Channels

The bridge has one channel:

| Channel ID           | Item Type | Description
|----------------------|-----------|-----------------------------------------------
| apiKeyLimitRemaining | Number    | The remaining number of API requests for today

The AirQuality sensors information that is retrieved is available as these channels:

| Channel ID        | Item Type            | Description
|-------------------|----------------------|---------------------------------------------
| time              | DateTime             | Last time the sensor data was send to Foobot
| pm                | Number:Density       | Particulate Matter level (ug/m3)
| temperature       | Number:Temperature   | Temperature in Celsius or Fahrenheit
| humidity          | Number:Dimensionless | Humidity level (%)
| co2               | Number:Dimensionless | Carbon diOxide level (ppm)
| voc               | Number:Dimensionless | Volatile Organic Compounds level (ppb)
| gpi               | Number:Dimensionless | Global Pollution index (%)

## Full Example

demo.things:

```java
// Bridge configuration:
Bridge foobot:account:myfoobotaccount "Foobot Account" [apiKey="XXXXXX", username="XXXXXX", refreshInterval=8] {
  Things:
    device myfoobot "Foobot sensor" [uuid="XXXXXXXXXXXXXXXX"]
```

demo.items:

```java
Number:Temperature Temperature "Temperature" <temperature> { channel="foobot:myfoobotaccount:device:myfoobot:temperature" }
```
