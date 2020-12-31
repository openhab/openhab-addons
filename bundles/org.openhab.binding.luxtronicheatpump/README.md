# LuxtronicHeatpump Binding

This binding gives the possibility to integrate any Heatpump that is based on the Luxtronic 2 contol unit of Alpha Innotec. This includes heatpumps of:

* Alpha InnoTec
* Buderus (Logamatic HMC20, HMC20 Z)
* CTA All-In-One (Aeroplus)
* Elco
* Nibe (AP-AW10)
* Roth (ThermoAura®, ThermoTerra)
* (Siemens) Novelan (WPR NET)
* Wolf Heiztechnik (BWL/BWS)

This binding is based on the [Novelan/Luxtronic Heat Pump Binding](https://v2.openhab.org/addons/bindings/luxtronicheatpump1/) for Open Hab 1. And some other Luxtronic tools like [Luxtronik2 for NodeJS](https://github.com/coolchip/luxtronik2) and the detailed parameter descriptions for the Java Webinterface in the [Loxwiki](https://www.loxwiki.eu/display/LOX/Java+Webinterface)

This binding was tested with:

* Siemens Novelan LD 7

_If you have another heatpump the binding works with, let us know, so we can extend the list_

Note: The whole functionality is baed on data that was reverse engineered, so use it at your own risk. 

## Supported Things

This binding only supports one thing type "Luxtronic Heatpump".

## Discovery

Not implemented yet.

## Thing Configuration

Each heatpump requires the following configuration parameters:

| parameter  | required | default | description |
|------------|----------|---------|-------------|
| ipAddress  | yes      |         | IP address of the heatpump |
| port       | no       | 8889    | Port number to connect to. This should be `8889` for most heatpumps. For heatpumps using a firmware version before V1.73 port `8888` needs to be used. |
| refresh    | no       | 300 | Interval (in seconds) to refresh the channel values. |

## Channels

As the Luxtronic 2 control is able to handle multiple heat pumps with different features (like heating, hot water, cooling, solar, photovoltaics, swimming pool,...), the binding has a lot channels. Depending on the heatpump it is used with, various channels might not hold any (useful) values.

The following channels are holding read only values:

| channel  | type   | advanced | description                  |
|----------|--------|----------|------------------------------|
| control  | Switch |    X     | This is the control channel  |


The following channels are read & writable:

| channel  | type   | advanced | description                  |
|----------|--------|----------|------------------------------|
| control  | Switch |    X     | This is the control channel  |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._
