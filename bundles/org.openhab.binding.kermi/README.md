# Kermi Binding

This binding connects to [Kermi x-center controller](https://www.kermi.com/en/de/indoor-climate/products/heat-pumps-and-storage/x-center-controller/ "x-center-controller") for heat pumps.

# Kermi Binding

Current support is developed and tested on 

* a franchised version of the Kermi heatpump, namely the [Heizbösch MOZART13AC-RW60](https://www.boesch.at/produkte/heizen/waermepumpe/luft/modulierende-luft-wasser-waermepumpe-mozart-aussenaufstellung~495589) heatpump manager version  _1.6.0.118_ .

No official documentation could be found or gathered. This plug-in is based
on reverse engineering the protocol.

## Supported Things

The x-center consists of a heat-pump manager that provides the communication bridge. 

- `bridge`: The communication bridge, provided by the heat pump manager
- `drinkingwater-heating`: The storage module responsible for heating the drinking water (Default bus address = 51)
- `room-heating`: The storage module responsible for heating rooms (Default bus address = 50)
- `heatpump-manager`: As thing
- `heatpump`: The heatpump element itself (Default bus address = 40)

## Discovery

There is no obvious way to get a listing of all Datapoints for a given Device. Due to this, on first
connection to a site, the API for the UI User Interface is used, to iterate over all menu entries to
collect the datapoints - which may take a while.

The gathered data is then stored in `OH_USERDATA/binding.kermi` and loaded on subsequent binding lifecycle
changes. The cache data is bound to the device-uuid and its serial number. If these values change,
the datapoints are automatically re-initialized.

## Binding Configuration

The following samples are provided, representing the current state of my usage.

```
// kermi.things
Bridge kermi:bridge:heatpumpbridge [hostname="xcenter-url",password="password",refreshInterval=60] {
    Thing drinkingwater-heating dwheating [ address=51 ]
    Thing room-heating rheating [ address=50 ]
    Thing heatpump heatpump [ address=40 ]
}

```

The plugin is configured to collect all `WellKnownName` datapoints, so generally
all of them should be supported. At the moment I only test the following items in read-only mode.

```
// kermi.items
Number:Temperature Drinking_water_temperature  {channel="kermi:drinkingwater-heating:heatpumpbridge:dwheating:BufferSystem_TweTemperatureActual"}

Number:Temperature Heating_current_temperature_buffer {channel="kermi:room-heating:heatpumpbridge:rheating:BufferSystem_HeatingTemperatureActual"}
Number:Temperature Cooling_current_temperature_buffer {channel="kermi:room-heating:heatpumpbridge:rheating:BufferSystem_CoolingTemperatureActual"}

Number:Temperature Outside_temperature {channel="kermi:room-heating:heatpumpbridge:rheating:LuftTemperatur"}

Number:Power Current_Power_Inverter {channel="kermi:heatpump:heatpumpbridge:heatpump:Rubin_CurrentPowerInverter"}
```

# Changelog

20.10.23 
* Support numeric values for datapointType = 0
* Support string values for datapointType = 3
* Support string values for datapointType = 4

# ToDo / Future Tasks

* Change default query time, resemble webinterface behaviour (every 10 seconds to GetFavorites)
* Support channels for bridge
* Somehow add DatapointConfigId to channel, seems not supported
* Collection of statistics providing virtual channels
    * 24/h power consumption (all, heating, drinking-water)
    * number of cycles (all, heating, drinking-water)
    * time between cycles
