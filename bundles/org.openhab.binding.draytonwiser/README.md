# Drayton Wiser Binding

This binding integrates the [Drayton Wiser Smart Heating System](https://wiser.draytoncontrols.co.uk/). The integration happens through the HeatHub, which acts as an IP gateway to the ZigBee devices (thermostats and TRVs).

## Supported Things

The Drayton Wiser binding supports the following things:
* Bridge - The network device in the controller that allows us to interact with the other devices in the system
* Controller - The HeatHub attached to the boiler. This also acts as the hub device.
* Rooms - Virtual groups of Room Stats and TRVs that can have temperatures and schedules
* Room Stats - Wireless thermostats which monitor temperature and humidity, and call for heat
* Smart TRVs - Wireless TRVs that monitor temperature and can alter the radiator valve state and call for heat

## Discovery

The HeatHub can be discovered automatically via mDNS, however the `SECRET` cannot be determined automatically. Once the `SECRET` has been configured, all other devices can be discovered by triggering device discovery again.

## Binding Configuration

None required

## Thing Configuration

### HeatHub Configuration

Once discovered, the HeatHub `SECRET` needs to be configured. There are a few ways to obtain this, assuming you have already configured the system using the Wiser App.

* Temporarily install a packet sniffing tool on your mobile device. Every request made includes the `SECRET` in the header.
* Enable setup mode on the HeatHub. Connect a machine temporarily to the `WiserHeat_XXXXX` network and browse to `http://192.168.8.1/secret` to obtain the key.

## Channels

TODO List available channels

## Full Example

TODO
