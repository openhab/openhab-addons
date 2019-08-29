# Foobot Binding

This binding fetches the Indoor Air Quality data of each of your Foobot devices from the Foobot cloud service.

To use this binding, you first need to [register and get your API key](https://api.foobot.io/apidoc/index.html).

## Supported Things

The binding supports the following binding:

| Thing type                | Name
|---------------------------|---------------
| bridge                    | Foobot Account


## Discovery

The binding requires you to have a Foobot account and an API key.
The discovery process is able to automatically discover all devices associated with your Foobot account.

## Binding Configuration

The binding has no configuration options, all configuration is done at Bridge level.

## Bridge Configuration

Bridge has the following configuration parameters:

| Parameter                 | Description                                           | Required
|---------------------------|-------------------------------------------------------|---------
| apikey                    | API Key from https://api.foobot.io/apidoc/index.html  | Mandatory
| username                  | Your UserName for the foobot App                      | Mandatory
| refreshIntervalInMinutes  | Refresh interval in minutes                           | Optional, the default value is 7 minutes.



## Channels

The AirQuality information that is retrieved is available as these channels:

| Channel ID        | Item Type | Description
|-------------------|-----------|---------------------------------------
| pm                | Number    | Fine particles pollution level (ug/m3)
| co2               | Number    | Carbon diOxide level (ppm)
| humidity          | Number    | Humidity level (pc)
| voc               | Number    | Volatile Organic Compounds level (ppb)
| gpi               | Number    | Global Pollution Index
| temperature       | Number    | Temperature in Celsius degrees


## Full Example

demo.things:

```
// Bridge configuration:
foobotbinding:foobot:account [apiKey="XXXXXX", username="XXXXXX", refreshIntervalInMinutes=7]

demo.items:

```
Number Temperature  "Temperature" <temperature> { channel= "foobotbinding:foobot:mac:temperature" }
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
