# Seneye Binding

This binding integrates the [Seneye aquarium monitoring system](https://www.seneye.com).

## Introduction

The seneye monitor monitors what is happening inside your aquarium to ensure that the aquatic life remains healthy.
The monitor allows you to continuously track the changes in the water parameters, alerting you to the problems before they affect the fish. 

At least one Seneye monitor is required ([Home / pond or reef](https://www.seneye.com/devices/compare)) and the measure results must be synced to the seneye cloud by using a seneye web server (see [shop](https://www.seneye.com/store), there is one for wifi and one for a wired network)
Each monitor is represented by one seneye thing.

## Supported Things

This binding provides one thing type: 'seneye'. 
You can have multiple seneye devices in your home, just make sure that your aquarium_name is properly set for each seneye thing.

## Discovery

Discovery is not supported, the seneye monitor must be configured manually

## Thing Configuration

The following settings must be configured in order to make your seneye binding work:

| Setting              |                                                                                 |
|----------------------|---------------------------------------------------------------------------------|
| aquarium_name        | The name of the aquarium, as specified in [seneye.me](https://www.seneye.me/).  |
|                      | Useful to distinguish multiple seneye installations.                            |
| username             | Your login name for [seneye.me](https://www.seneye.me/)                         |
| password             | Your password for [seneye.me](https://www.seneye.me/)                           |
| poll_time            | How often (in minutes) should the seneye account be checked.                    |

## Channels

The following channels are supported:

| Channel Type ID         | Item Type    | Description                                                      |
|-------------------------|--------------|------------------------------------------------------------------|
| temperature             | String       | The water temperature                                            |
| ph                      | String       | The PH level of the water                                        |
| nh3                     | String       | The level of Ammonia (NH3) in the water                          |
| nh4                     | String       | The level of Ammonium (NH4) in the water                         |
| O2                      | String       | The level of oxygene in the water                                |
| lux                     | String       | The lux level of your aquarium lightning, if available           |
| par                     | String       | The par level of your aquarium lightning, if available           |
| kelvin                  | String       | The kelvin level of your aquarium lightning, if available        |
| lastreading             | DateTime     | The moment when the last readings are received from the monitor  |
| slideexpires            | DateTime     | The moment when the current slide will expire                    |

## Full example

A manual configuration through a `things/seneye.things` file could look like this:

```
Thing seneye:seneye:mySeneye "Seneye" @ "Living Room" [aquarium_name="MyAquarium", username="mail@example.com", password="xxx", poll_time=5]
```

A manual configuration through a `demo.items` file could look like this:

```
String mySeneye_Temperature  "Temp [%s] C"        { channel="seneye:seneye:mySeneye:temperature" }
String mySeneye_PH           "PH [%s]"            { channel="seneye:seneye:mySeneye:ph" }
String mySeneye_NH3          "NH3 [%s]"           { channel="seneye:seneye:mySeneye:nh3" }
```

The sitemap could look like this:

```
sitemap home label="My home" {
    Frame label="Aquarium" {
        Text item=mySeneye_Temperature label="Temperature [%.1f °C]" icon="temperature"
        Text item=mySeneye_PH label="PH [%.1f]" icon="water"
        Text item=mySeneye_NH3 label="NH3 [%.1f]" icon="water"
    }
}
```
