# List of openHAB 2 Add-ons

All optional add-ons for openHAB 2 are [available in a separate download](https://bintray.com/artifact/download/openhab/bin/openhab-2.0.0.alpha2-addons.zip). This file contains all new 2.0 bindings as well as all 1.x add-ons that were reported to be compatible. If you are successfully using a 1.x add-on with the 2.0 runtime, which is not yet on this list, please create a PR for adding it.

## 2.0 Bindings

| Binding | Description |
|-------|----------------------|
| [Astro Binding](../../addons/binding/org.openhab.binding.astro/README.md) | Astronomical calculations for sun and moon positions |
| [Autelis Binding](../../addons/binding/org.openhab.binding.autelis/README.md) | Pool controller |
| [AVM Fritz!Box Binding](../../addons/binding/org.openhab.binding.avmfritz/README.md) | currently only supports FRITZ AHA devices |
| [Freebox Binding](../../addons/binding/org.openhab.binding.freebox/README.md) | the french [Freebox Revolution](http://www.free.fr/adsl/freebox-revolution.html) server |
| [HDanywhere Binding](../../addons/binding/org.openhab.binding.hdanywhere/) | HDMI matrix |
| [IPP Binding](../../addons/binding/org.openhab.binding.ipp/README.md) | Internet Printing Protocol (replaces 1.x CUPS Binding) |
| [KEBA Binding](../../addons/binding/org.openhab.binding.keba/README.md) | Electric vehicle charging station |
| [LIFX Binding](https://github.com/eclipse/smarthome/blob/20150525/addons/binding/org.eclipse.smarthome.binding.lifx/README.md) | Wifi-enabled LED bulbs |
| [MAX! Binding](../../addons/binding/org.openhab.binding.max/README.md) | Heater control solution by eQ-3 |
| [Network Binding](../../addons/binding/org.openhab.binding.network/) | Scans local network (replaces 1.x networkhealth Binding) |
| [PioneerAVR Binding](../../addons/binding/org.openhab.binding.pioneeravr/README.md) | AV receivers by Pioneer |
| [Philips Hue Binding](https://github.com/eclipse/smarthome/blob/20150525/addons/binding/org.eclipse.smarthome.binding.hue/README.md) | LED lighting system |
| [Pulseaudio Binding](../../addons/binding/org.openhab.binding.pulseaudio/README.md) | software-based audio distribution |
| [Squeezebox Binding](../../addons/binding/org.openhab.binding.squeezebox/README.md) | Logitech's connected speakers |
| [Sonos Binding](../../addons/binding/org.openhab.binding.sonos/README.md) | Multi-room audio system |
| [Tesla Binding](../../addons/binding/org.openhab.binding.tesla/README.md) | Teslas Model S Electric Vehicle |
| [Vitotronic Binding](../../addons/binding/org.openhab.binding.vitotronic/README.md) | Heating systems by Viessmann |
| [WeMo Binding](https://github.com/eclipse/smarthome/blob/20150525/addons/binding/org.eclipse.smarthome.binding.wemo/README.md) | Switchable sockets by Belkin |
| [YahooWeather Binding](https://github.com/eclipse/smarthome/blob/20150525/addons/binding/org.eclipse.smarthome.binding.yahooweather/README.md) | Weather information from Yahoo |

## Compatible 1.x Add-ons

| Add-on | Type |
|--------|------|
| Anel | Binding |
| Astro | Binding |
| Denon | Binding |
| DMX (OLA) | Binding |
| EDS OWServer | Binding |
| Energenie | Binding |
| Enocean | Binding |
| Epsonprojector | Binding |
| Exec | Binding |
| Freeswitch | Binding |
| FS20 | Binding |
| Heatmiser | Binding |
| Homematic | Binding |
| HTTP | Binding |
| IHC | Binding |
| InsteonPLM | Binding |
| KNX | Binding |
| Milight | Binding |
| Modbus | Binding |
| Networkhealth | Binding |
| Nibeheatpump | Binding |
| NTP | Binding |
| Onkyo | Binding |
| OpenEnergyMonitor | Binding |
| OneWire | Binding |
| RFXCOM | Binding |
| Samsung AC | Binding |
| Satel | Binding |
| SNMP | Binding |
| SwegonVentilation | Binding |
| SystemInfo | Binding |
| Tinkerforge | Binding |
| Tellstick | Binding |
| Weather | Binding |
| WOL | Binding |
| XBMC | Binding |
| ZWave | Binding |
| rrd4j | Persistence |
| MySQL | Persistence |
| Logging | Persistence |
| JPA | Persistence |
| Mail | Action |
| Pushover | Action |
| XBMC | Action |
| XMPP | Action |
| GoogleTTS | TTS engine |
| MaryTTS | TTS engine |

## Currently incompatible 1.x Add-ons:

| Add-on | Type | Reason
|--------|------|------|
| MySQL | Persistence | [Unsynchronized change in openHAB 1 core](https://github.com/openhab/openhab/issues/2661)
| InfluxDB | Persistence | [Unsynchronized change in openHAB 1 core](https://github.com/openhab/openhab/issues/2661)
| MongoDB | Persistence | [Unsynchronized change in openHAB 1 core](https://github.com/openhab/openhab/issues/2661)


## Compatible Applications

| Application | Description |
|-------|----------------------|
| [iot_bridge](https://github.com/openhab/openhab/wiki/ROS-Robot-Operating-System) | Bridge between ROS Robot Operating System and OpenHAB |
