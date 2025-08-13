# evcc Binding

This binding integrates [evcc](https://evcc.io), an extensible **E**lectric **V**ehicle **C**harge **C**ontroller and home energy management system.
The binding is compatible to evcc [version 0.123.1](https://github.com/evcc-io/evcc/releases/tag/0.123.1) or newer and was tested with [version 0.205.0](https://github.com/evcc-io/evcc/releases/tag/0.205.0).

You can easily install and upgrade evcc on openHABian using `sudo openhabian-config`.

evcc controls your wallbox(es) with multiple charging modes and allows you to charge your ev with your photovoltaic's excess current.
To provide an intelligent charging control, evcc supports over 30 wallboxes and over 20 energy meters/home energy management systems from many manufacturers as well as electric vehicles from over 20 car manufacturers.
Furthermore, evcc calculates your money savings.

This binding enables openHAB to retrieve status data from your evcc instance and to control the charging process.
For more advanced features like calculated savings, you have to visit the web UI of evcc.

This binding will create a file in your userdata folder, when it detects new datapoints in the response from evcc.
This will help to add them to the binding and make them available with the next version of the binding.

## Supported Things

- `server`: A running evcc instance. It will be used as the bridge for the other things
- `battery`: A battery configured in your evcc instance.
- `heating`: A heating loadpoint configured in your evcc instance.
- `loadpoint`: A loadpoint configured in your evcc instance.
- `pv`: A photovoltaic system configured in your evcc instance.
- `site`: The relevant site data from your evcc instance.
- `vehicle`: A vehicle configured in your evcc instance

## Discovery

The bridge will discover the things automatically in the background.

## `server` Bridge Configuration

| Parameter       | Type    | Description                                              | Advanced | Required |
|-----------------|---------|----------------------------------------------------------|----------|----------|
| schema          | String  | Schema to connect to your instance (http or https)       | No       | Yes      |
| host            | String  | IP or hostname running your evcc instance                | No       | Yes      |
| port            | Integer | Port of your evcc instance                               | Yes      | Yes      |
| refreshInterval | Number  | Interval the status is polled in seconds (minimum is 15) | Yes      | Yes      |

Default value for _refreshInterval_ is 30 seconds.

## Thing(s) Configuration

The Things don't have a configuration. They will be set up automatically when a bridge has been configured.
You can add them via file configuration or in the openHAB UI.

## Channels

evcc Things can have several channels based corresponding on their capabilities.
These channels are dynamically added to the Thing during their initialization; therefore, there is no list of possible channels in this documentation.

## Full Example

### `demo.things` Example

```java
Bridge evcc:server:demo-server "Demo" [scheme="https", url="demo.evcc.io", port=80, refreshInterval=30] {
    // This thing will only exist once per evcc instance
    Thing site demo-site "Site - evcc Demo"
    // You can define as many Battery things as you have batteries configured in your evcc instance
    Thing battery demo-battery1 "Battery - evcc Demo Battery 1"
    ..
    // You can define as many PV things as you have photovoltaics configured in your evcc instance
    Thing pv demo-pv1 "PV - evcc Demo Photovoltaic 1"
    ..
    // You can define as many Loadpoint things as you have loadpoints configured in your evcc instance
    Thing loadpoint demo-loadpoint-carport "Loadpoint - evcc Demo Loadpoint 1"
    ..
    // You can define as many Vehicle things as you have vehicles configured in your evcc instance
    Thing vehicle demo-vehicle1 "Vehicle - evcc Demo Vehicle 1"
    ..
}
```

### `demo.items` Example

```java
Number GridPower "Grid Power" { channel="evcc:site:demo-server:demo-site:site-grid-power" }
```

## Troubleshooting

If you need additional data that can be read out of the API response please reach out to the openHAB community.
