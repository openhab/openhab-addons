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

- `server`: A running evcc instance.

## Discovery

The bridge will discover the things automatically in the background.

## Thing Configuration

### `server` Thing Configuration

| Parameter       | Type    | Description                                              | Advanced | Required |
|-----------------|---------|----------------------------------------------------------|----------|----------|
| schema          | String  | Schema to connect to your instance (http or https)       | No       | Yes      |
| host            | String  | IP or hostname running your  evcc instance               | No       | Yes      |
| port            | Integer | Port of your evcc instance                               | No       | Yes      |
| refreshInterval | Number  | Interval the status is polled in seconds (minimum is 15) | No       | Yes      |

Default value for _refreshInterval_ is 30 seconds.

### Thing(s)


### Channels and Items

Channels will be created dynamically!

## Example file creation

```java
Bridge evcc:bridge:demo "Demo" [schema="https", url="demo.evcc.io", port=80, refreshInterval=30] {
    // This thing will only exist once per evcc instance
    Thing site demo_site "Site - evcc Demo"
    // You can define as many Battery things as you have batteries configured in your evcc instance
    Thing battery demo_battery "Battery - evcc Demo Battery 1"
    ..
    // You can define as many PV things as you have photovoltaics configured in your evcc instance
    Thing pv demo_pv "PV - evcc Demo Photovoltaik 1"
    ..
    // You can define as many Loadpoint things as you have loadpoints configured in your evcc instance
    Thing loadpoint demo_loadpoint_carport "Loadpoint - evcc Demo Loadpoint 1"
    ..
    // You can define as many Vehicle things as you have vehicles configured in your evcc instance
    Thing vehicle demo_vehicle_1 "Vehicle - evcc Demo Vehicle 1"
    ..
}
```

