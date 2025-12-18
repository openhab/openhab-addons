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

Things will be set up automatically when a bridge has been configured and will appear in your inbox.
It is not possible to add things manually in the UI.
Nevertheless, you can add them manually via things file.

### Thing Site

The Site Thing represents the overall site data of your evcc instance.
No configuration parameters are required.
Renamed property siteTitle -> site-title

### Thing Battery

The Battery Thing represents a battery configured in your evcc instance.
Needs the index of the battery as configuration parameter.

### Thing PV

The PV Thing represents a photovoltaic system configured in your evcc instance.
Needs the index of the photovoltaic system as configuration parameter.

### Thing Loadpoint

The Loadpoint Thing represents a loadpoint configured in your evcc instance.
Needs the index of the loadpoint as configuration parameter.

### Thing Vehicle

The Vehicle Thing represents a vehicle configured in your evcc instance.
Needs the database id of the vehicle as configuration parameter.
Renamed property id -> vehicle-id and renamed parameter id -> vehicle-id, you may need to parametrize you thing.

### Thing Plan

The Plan Thing represents a charging plan for a vehicle configured in your evcc instance.
Needs the index of the plan and the database id of the vehicle as configuration parameters.
The index 0 is always the One-time plan, higher indices are repeating plans.

Any changes made to the plan channels will not be sent to evcc automatically, but cached.
If you want to update the plan, you have to use the update plan channel of the thing to send it to evcc.
**Updating the plan will only work when setting the update plan channel to state ON**.
Afterwards, the update plan channel will be automatically reset to state OFF.
The weekdays will be localized based on the language settings of your openHAB instance.

Here is an example to update a One-time charging plan via DLSRule script:

```DSLRule
Repeating_charging_plan_1_for_BMW_iX3_Plan_Time.sendCommand("2025-12-19T06:00:00.000Z");
Repeating_charging_plan_1_for_BMW_iX3_Plan_SoC.sendCommand(85);
Repeating_charging_plan_1_for_BMW_iX3_Precondition_Time.sendCommand(1800);
Repeating_charging_plan_1_for_BMW_iX3_Update_Plan.sendCommand("ON");
```


Here is an example to update a repeating charging plan via DLSRule script:

```DSLRule
Repeating_charging_plan_1_for_BMW_iX3_Plan_Weekdays.sendCommand("Monday;Tuesday;Wednesday");
Repeating_charging_plan_1_for_BMW_iX3_Plan_Time.sendCommand("09:00");
Repeating_charging_plan_1_for_BMW_iX3_Plan_SoC.sendCommand(85);
Repeating_charging_plan_1_for_BMW_iX3_Precondition_Time.sendCommand(1800);
Repeating_charging_plan_1_for_BMW_iX3_Update_Plan.sendCommand("ON");
```

## Channels

evcc Things can have several channels based corresponding on their capabilities.
These channels are dynamically added to the Thing during their initialization; therefore, there is no list of possible channels in this documentation.

## Full Example

### `demo.things` Example

```java
Bridge evcc:server:demo-server "Demo" [scheme="https", host="demo.evcc.io", port=443, refreshInterval=30] {
    // This thing will only exist once per evcc instance
    Thing site demo-site "Site - evcc Demo"
    // You can define as many Battery things as you have batteries configured in your evcc instance
    Thing battery demo-battery1 "Battery - evcc Demo Battery 1"[index=0]
    ..
    // You can define as many PV things as you have photovoltaics configured in your evcc instance
    Thing pv demo-pv1 "PV - evcc Demo Photovoltaic 1"[index=0]
    ..
    // You can define as many Loadpoint things as you have loadpoints configured in your evcc instance
    Thing loadpoint demo-loadpoint-carport "Loadpoint - evcc Demo Loadpoint 1"[index=0]
    ..
    // You can define as many Vehicle things as you have vehicles configured in your evcc instance
    Thing vehicle demo-vehicle1 "Vehicle - evcc Demo Vehicle 1"[vehicle-id="vehicle_1"]
    ..
    // You can define as many Plan things as you have plans for your vehicle configured
    Thing plan demo-one-time-plan-for-vehicle1 "One-time plan for vehicle 1"[index=0, vehicle-id="vehicle_1"]
    Thing plan demo-repeating-plan-1-for-vehicle1 "Repeating plan 1 for vehicle 1"[index=1, vehicle-id="vehicle_1"]..
    ..
}
```

### `demo.items` Example

```java
Number GridPower "Grid Power" { channel="evcc:site:demo-server:demo-site:site-grid-power" }
```

## Troubleshooting

If you need additional data that can be read out of the API response please reach out to the openHAB community.
