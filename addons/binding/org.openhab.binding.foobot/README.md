# Foobot Binding

This binding fetches the Indoor Air Quality data of your Foobot device from the Foobot cloud service.

To use this binding, you first need to [register and get your API key](https://api.foobot.io/apidoc/index.html).

## Supported Things

There is exactly one supported thing type, which provides the Indoor air quality information from the foobot device.
It has the `foobot` id.
If you have multiple foobots in your home or office, you can add multiple Things, using the unique MAC Address for each device.

## Discovery

If you don't manually create things in the *.things file, the Foboot Binding is able to discover automatically all devices associated with your Foobot account.

## Binding Configuration

The binding has no configuration options, all configuration is done at Thing level.

## Bridge and Thing Configuration

You can either create a Bridge (i.e. your Foobot account) or a Thing (i.e. your Foobot device).

Bridge and Thing have the following configuration parameters:

Bridge:<br />
| Parameter                 | Description                                                               |
|---------------------------|---------------------------------------------------------------------------|
| apikey                    | API Key from https://api.foobot.io/apidoc/index.html. Mandatory.          |
| username                  | your UserName for the foobot App. Mandatory.                              |
| refreshIntervalInMinutes  | Refresh interval in minutes. Optional, the default value is 7 minutes.    |

Thing:<br />
| Parameter                 | Description                                                               |
|---------------------------|---------------------------------------------------------------------------|
| apikey                    | API Key from https://api.foobot.io/apidoc/index.html. Mandatory.          |
| username                  | your UserName for the foobot App. Mandatory.                              |
| mac                       | Unique ID of your foobot device. Mandatory.                               |
| refreshIntervalInMinutes  | Refresh interval in minutes. Optional, the default value is 7 minutes.    |


## Channels

The AirQuality information that is retrieved is available as these channels:

| Channel ID      | Item Type | Description                                  |
|-----------------|-----------|----------------------------------------------|
| pm              | Number    | Fine particles pollution level (ug/m3)       |
| co2             | Number    | Carbon diOxide level (ppm)                   |
| humidity        | Number    | Humidity level (pc)                          |
| voc             | Number    | Volatile Organic Compounds level (ppb)       |
| gpi             | Number    | Global Pollution Index                       |
| temperature     | Number    | Temperature in Celsius degrees               |


## Full Example

demo.things:

```
// Bridge configuration:
foobotbinding:foobot:account [apikey="XXXXXX", username="XXXXXX", refreshIntervalInMinutes=7]

// Thing configuration:
foobotbinding:foobot:mac [apikey="XXXXXX", username="XXXXXX", mac="XXXXXX", refreshIntervalInMinutes=7]
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
