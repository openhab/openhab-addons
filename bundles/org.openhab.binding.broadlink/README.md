# Broadlink Binding

This binding builds on the original work of Cato Sognen, and supports a range of home
networking devices made by (and occasionally OEMs licensed from) [Broadlink](https://www.ibroadlink.com/).

## Supported Things

#### Environmental Sensors
- **A1** Temperature/Humidity/Noise/Light/Air quality sensor

#### Smart Strips
- **MP1** 4-port power strip
- **MP1 1K3S2U** 3-port plus 2-USB power strip
- **MP2** power strip with power consumption

#### IR/RF Transmitter/Blasters
- **RM** Home IR transmitter
- **RM2** IR/RF transmitter
- **RM3** IR transmitter
- **RM4** IR transmitter with temp/humidity sensor

#### Smart Sockets
- **SP1** socket
- **SP2** socket with night light and power consumption
- **SP3** socket with night light


## Discovery

Devices in the above list that are set up and 
working in the Broadlink mobile app should be discoverable
by initiating a discovery from the OpenHAB UI.

## Thing Configuration

Discovered Broadlink `Thing`s should work with no further configuration required,
but if needed, some extra configuration can be set:

| name                  | type    | description |
|-----------------------|---------|-------------|
| Static IP             | Boolean | Will the device always be given this network address? (This affects the "rediscovery" process and hence time taken to declare a Thing `OFFLINE`)|
| Polling Interval      | Integer | The interval in seconds for polling the status of the device |
| Ignore Failed Updates | Boolean | Should failed status requests force the device offline? |



## Channels

_Here you should provide information about available channel types, what their meaning is and how they can be used._

_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/OH-INF/thing``` of your binding._

| channel     | type   | description                      |
|-------------|--------|----------------------------------|
| powerOn     | Switch | Power on/off for switches/strips |
| nightLight  | Switch | Night light on/off               |
| temperature | Number | Temperature in degrees Celsius   |
| humidity    | Number | Air humidity percentage          |
| noise       | String | Noise level: `QUIET`/`NORMAL`/`NOISY`/`EXTREME` |
| light       | String | Light level: `DARK`/`DIM`/`NORMAL`/`BRIGHT` |
| air         | String | Air quality: `PERFECT`/`GOOD`/`NORMAL`/`BAD` |
| powerConsumption | Number | Power consumption in Watts |

## Full Example

Items file example; `sockets.items`:
```
Switch BroadlinkSP3 "Christmas Lights" [ "Lighting" ] { channel="broadlink:sp3:34-ea-34-22-44-66:powerOn" } 
```