# evcc Binding

![](doc/evcc-hero.svg)

This binding integrates [evcc](https://evcc.io), an extensible **E**lectric **V**ehicle **C**harge **C**ontroller and home energy management system.
The binding is compatible to evcc [version 0.123.1](https://github.com/evcc-io/evcc/releases/tag/0.123.1) or newer and was tested with [version 0.205.0](https://github.com/evcc-io/evcc/releases/tag/0.205.0).

You can easily install and upgrade evcc on openHABian using `sudo openhabian-config`.

evcc controls your wallbox(es) with multiple charging modes and allows you to charge your ev with your photovoltaik's excess current.
To provide an intelligent charging control, evcc supports over 30 wallboxes and over 20 energy meters/home energy management systems from many manufacturers as well as electric vehicles from over 20 car manufacturers.
Furthermore, evcc calculates your money savings.

This binding enables openHAB to retrieve status data from your evcc instance and to control the charging process.
For more advanced features like calculated savings, you have to visit the web UI of evcc.

This binding will create a file in your userdata folder, when it detects new datapoints in the response from evcc.
This will help to add them to the binding make them available with the next version of the binding.

## Supported Things

- `device`: A running evcc instance.

## Discovery

The bridge will discover the things automatically in the background.

## Thing Configuration

### `device` Thing Configuration

| Parameter       | Type    | Description                                              | Advanced | Required |
|-----------------|---------|----------------------------------------------------------|----------|----------|
| schema          | String  | Schema to connect to your instance (http or https)       | No       | Yes      |
| host            | String  | IP or hostname running your  evcc instance               | No       | Yes      |
| port            | Integer | Port of your evcc instance                               | No       | Yes      |
| refreshInterval | Number  | Interval the status is polled in seconds (minimum is 15) | No       | Yes      |

Default value for _refreshInterval_ is 30 seconds.

## Channels

Channels will be created dynamically

## Full Example

### Thing(s)

```java
Bridge evcc:bridge:demo "evcc Demo" [url="https://demo.evcc.io", refreshInterval=30]
Thing evcc:site:demo:demo_site "evcc Site - evcc Demo"
Thing evcc:battery:demo:demo_battery "evcc Battery - evcc Demo Battery"
Thing evcc:pv:demo:demo_bpv "evcc PV - evcc Demo Photovoltaik"
Thing evcc:loadpoint:demo:demo_loadpoint_carport "evcc Loadpoint - evcc Demo Loadpoint 1"
Thing evcc:loadpoint:demo:demo_loadpoint_garage "evcc Battery - evcc Demo Loadpoint 2"
Thing evcc:vehicle:demo:demo_vehicle_1 "evcc Battery - evcc Demo Vehicle 1"
Thing evcc:vehicle:demo:demo_vehicle_2 "evcc Battery - evcc Demo Vehicle 2"
```
