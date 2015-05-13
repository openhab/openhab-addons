# Compatibility of openHAB 1.x add-ons with openHAB 2

Here you can find the compatibility matrices in order to find out, if the current openHAB 2 SNAPSHOT is compatible with the openHAB 1.x addons (either 1.6.0 release or 1.7.0-SNAPSHOT).

This page is a community effort - please help filling the gaps and analysing potential problems!

## Bindings

| Binding | 2.0-SNAPSHOT + 1.6.0 | 2.0-SNAPSHOT + 1.7.0-SNAPSHOT | Remarks |
|-------|:----------------------:|:-------------------------------:|---|
| AlarmDecoder |  |  |  |
| Anel |  |  |  |
| Asterisk |  |  |  |
| Astro |  |  |  |
| Bluetooth |  |  |  |
| Comfo Air |  |  |  |
| Config Admin |  |  |  |
| CUL | X | X |  |
| CUPS |  |  | 2.0 version available (renamed to IPP) |
| Davis |  |  |  |
| digitalSTROM |  |  |  |
| DSC Alarm |  |  |  |
| DMX512 |  |  |  |
| EDS OWSever |  |  |  |
| Energenie | X | X |  |
| EnOcean | X | X |  |
| Epson Projector | X | X |  |
| Exec | X | X |   |
| Freebox |  |  |  |
| Freeswitch | X | X |  |
| Fritz!Box |  |  |  |
| Fritz AHA |  |  |  |
| FS20 | X | X |  |
| Global Cache IR |  |  |  |
| GPIO |  |  |  |
| HAI/Leviton OmniLink |  |  |  |
| HDAnywhere |  |  |  |
| Heatmiser |  | X |  |
| Homematic / Homegear | X | X |  |
| HTTP | X | X |  |
| IEC 62056-21 |  |  |  |
| IHC / ELKO | X | X |  |
| Insteon Hub |  |  |  |
| Insteon PLM |  | X |  |
| IRtrans |  |  |  |
| jointSPACE-Binding |  |  |  |
| KNX | X | X | |
| Koubachi |  |  |  |
| Leviton/HAI Omnilink |  |  |  |
| MAX!Cube |  |  | 2.0 version available |
| MAX! CUL |  |  |  |
| MiLight | X | X |  |
| Modbus TCP |  |  |  |
| Westaflex Modbus |  |  |  |
| MPD |  |  |  |
| MQTT |   |  |  |
| MQTTitude |  |  |  |
| Neohub |  |  |  |
| Netatmo |  |  |  |
| Network Health | X | X |  |
| Nibe Heatpump | X | X |  |
| Nikobus |  |  |  |
| Novelan/Luxtronic Heatpump |  |  |  |
| NTP | X | X |  |
| One-Wire |  |  |  |
| Onkyo AV Receiver | X | X |  |
| Open Energy Monitor | X | X |  |
| OpenPaths presence detection |  |  |  |
| OpenSprinkler |  |  |  |
| OSGi Configuration Admin |  |  |  |
| OWServer |  | X |  |
| Philips Hue |  |  | 2.0 version available |
| Piface | |  |  |
| Pioneer-AVR-Binding | |  |  |
| Plugwise |  |  |  | 
| PLCBus |  |  |  | 
| Pulseaudio |  |  | 2.0 version available |
| RFXCOM | X | X |  |
| Samsung TV | X | X |  |
| Serial |  |  |  | 
| SNMP |  | X |  |
| Squeezebox |  |  |  |
| System Info | X | X |  |
| Somfy URTSI II |  |  |  |
| Sonos |  |  | 2.0 version available |
| Swegon ventilation | X | X |  |
| TCP/UDP |  |  |  |
| Tellstick |  |  |  |
| TinkerForge |  |  |  |
| VDR |  |  |  |
| Velleman-K8055 |  |  |  |
| Wake-on-LAN |  |  |  |
| Waterkotte EcoTouch Heatpump |  |  |  |
| Wemo |  |  | 2.0 version available |
| Withings |  |  |  |
| XBMC |  |  |  |
| xPL |  |  |  |
| Z-Wave | X | X |  |

## Persistence Services

| Persistence Service | 2.0-SNAPSHOT + 1.6.0 | 2.0-SNAPSHOT + 1.7.0-SNAPSHOT | Remarks |
|-------|:----------------------:|:-------------------------------:|---|
| db4o |  |  |  |
| rrd4j | - | X | rrd4j chart provider is supported as well |
| MySQL |  | X |  |
| MongoDB |  |  |  |
| Sen.Se |  |  |  |
| Cosm |  |  |  |
| Logging | X | X |  |
| Exec |  |  |  |
| MQTT |  |  |  |
| InfluxDB |  |  |  |
| JPA | X | X |  |

## Actions

| Action | 2.0-SNAPSHOT + 1.6.0 | 2.0-SNAPSHOT + 1.7.0-SNAPSHOT | Remarks |
|-------|:----------------------:|:-------------------------------:|---|
| Mail | X | X |  |
| XMPP |  |  |  |
| Prowl |  |  |  |
| Twitter |  |  |  |
| Cosm |  |  |  |
| XBMC | X | X |  |
| NotifyMyAndroid |  |  |  |
| Squeezebox |  |  |  |
| Pushover |  |  |  |
| OpenWebIf |  |  |  |

## Text-to-speech engines

| Binding | 2.0-SNAPSHOT + 1.6.0 | 2.0-SNAPSHOT + 1.7.0-SNAPSHOT | Remarks |
|-------|:----------------------:|:-------------------------------:|---|
| Macintalk |  |  | 2.0 version available |
| FreeTTS |  |  |  |
| GoogleTTS |  |  |  |
| MaryTTS | X | X |  |
| Speechdispatcher |  |  |  |
