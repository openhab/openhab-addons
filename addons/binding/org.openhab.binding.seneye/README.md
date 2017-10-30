# Seneye Binding

This binding integrates the [Seneye aquarium monitoring system](https://www.seneye.com).

## Introduction

Understanding what is happening inside your aquarium is vital to ensuring that the aquatic life remains healthy. This revolutionary water monitoring device allows you to continuously track the changes in the water parameters, alerting you to the problems before they affect the fish. Protect your fish with a seneye monitor.

## Supported Things

At least one Seneye monitor is required ([Home / pond or reef](https://www.seneye.com/devices/compare)) and the measure results must be synced to the seneye cloud by using a seneye web server (see [shop](https://www.seneye.com/store), there is one for wifi and one for a wired network)
Each monitor is represented by 1 openhab 2 seneye thing.

## Discovery

Discovery is not supported, the seneye monitor must be configured manually

## Binding Configuration

There is nothing to be configured on the binding 

## Thing Configuration

Following settings must be configured in order to make your seneye binding work :

Aquariumname : the name of the aquarium, as specified in [seneye.me] (https://www.seneye.me/). Usefull to distinguish multiple seneye installations.
Username : your login name for [seneye.me] (https://www.seneye.me/)
Password : your password for [seneye.me] (https://www.seneye.me/)

## Channels

All devices support some of the following channels:

| Channel Type ID         | Item Type    | Description  |
|-------------------------|------------------------|--------------|----------------- |------------- |
| temperature             | String       | The water temperature |
| ph                      | String       | The PH level of the water |
| nh3                     | String       | The level of Ammonia (NH3) in the water |
| nh4                     | String       | The level of Ammonium (NH4) in the water |
| O2                      | String       | The level of oxygene in the water |
| lux                     | String       | The lux level of your aquarium lightning, if available |
| par                     | String       | The par level of your aquarium lightning, if available |
| kelvin                  | String       | The kelvin level of your aquarium lightning, if available |
| lastreading             | DateTime     | The moment when the last readings are received from the monitor |
| slideexpires            | DateTime     | The moment when the current slide will expire |

