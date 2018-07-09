# Foobot Binding

This binding fetches the Indoor Air Quality data of your Foobot device from the Foobot cloud service.

To use this binding, you first need to [register and get your API key](https://api.foobot.io/apidoc/index.html).

## Supported Things

There is exactly one supported thing type, which provides the Indoor air quality information from the foobot device.
It has the `foobot` id.
If you have multiple foobots in your home or office, you can add multiple Things, using the unique MAC Address for each device.

## Discovery

There is no discovery implemented.
You have to create your things manually.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Thing Configuration

The thing has a few configuration parameters:

| Parameter | Description                                                             |
|-----------|-------------------------------------------------------------------------|
| apikey    | API Key from https://api.foobot.io/apidoc/index.html. Mandatory.        |
| username  | your UserName for the foobot App. Mandatory.                            |
| MAC Id    | Unique ID of your foobot device. Mandatory.                             |
| refresh   | Refresh interval in minutes. Optional, the default value is 30 minutes. |


## Channels

The AirQuality information that is retrieved is available as these channels:

| Channel ID      | Item Type | Description                                  |
|-----------------|-----------|----------------------------------------------|
| pm              | Number    | Fine particles pollution level (ug/m3)       |
| co2             | Number    | Carbon diOxide level (ppm)                   |
| humidity        | Number    | Humidity level (pc)                          |
| voc             | Number    | Volatice Organic Compounds level (ppb)       |
| gpi             | Number    | Global Pollution Index                       |
| temperature     | Number    | Temperature in Celsius degrees               |


## Full Example

demo.things:

```
foobotbinding:foobot:mac [apikey="XXXXXX", username="XXXXXX", mac="XXXXXX", refresh=5]
```

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