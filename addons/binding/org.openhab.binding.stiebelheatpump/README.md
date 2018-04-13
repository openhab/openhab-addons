# Stiebel Eltron LWZ Binding

This binding is used to communicate Stiebel Eltron LWZ heat pumps via a serial interface.  The binding is inspired by the work of [Monitoring a Stiebel Eltron LWZ](http://robert.penz.name/heat-pump-lwz) which is hosted at [Heatpumpmonitor](https://launchpad.net/heatpumpmonitor) and is written in Python.

The following functionality has been implemented:

* reading all settings, status, time, and sensor data from the heat pump
* protocol parse for different versions of LWZ is configurable via xml file.  Other versions like 2.16, 4.09, 4.19, 4.38, 5.39 should be easy to extend as parser configuration can be derived from [protocol versions](http://bazaar.launchpad.net/~robert-penz-name/heatpumpmonitor/trunk/files/head:/protocolVersions/).
* changing settings of number setting parameter in the heat pump, all seeting parameters are configured as advanced channels
* updating the time in the heat pump


The binding has been successfully tested with these hardware configurations:

* [Stiebel Eltron LWZ 303](https://www.stiebel-eltron.de/content/dam/ste/de/de/products/downloads/erneuerbare_energien/lueftung/Bedienungs-_u._Installationsanleitungen__LWZ_303-403__DM0000017729-ome.pdf) connected with [Mate-N-Lok RS232](http://robert.penz.name/heat-pump-lwz/)
Many other varaints of the heat pump should also work: 
    Stiebel Eltron LWZ 303
    Stiebel Eltron LWZ 403
    Stiebel Eltron LWZ 303 SOL
    Stiebel Eltron LWZ 403 SOL
    Tecalor THZ 303
    Tecalor THZ 403
    Tecalor THZ 303 SOL
    Tecalor THZ 403 SOL

## Binding Configuration

The binding requires no special configuration

## Thing Configuration

This binding provides a read and write functionality to  heatpump Stiebel Eltron / Tecalor THZ/LWZ 303/304/403/404

Different heat pump types can be created by thing type, which can be configured in PaperUI or in Thing file.
The binding will verify if the firmware version you choosed during thing creation matches the firmware version within the heat pump.

| Property | Default | Required | Description |
|----------|---------|:--------:|-------------|
| `Portname |/dev/ttyS0 | Yes | the serial port to use for connecting to the heat pump device e.g. COM1 for Windows and /dev/ttyS0 or /dev/ttyUSB0 for Linux |
| `Baud rate | 9600 | yes | Baud rate, default is 9600.     |
| `Waiting time between requests | 1200 | No | Time between polling requests in [ms], it depends on the speed of the heat pump CPU and interface . |
| `Heat pump refresh rate | 60 | No | Refresh rate in [s] to query the heat pump data. |

## Item Configuration

Default channels are automatically created then creating the thing in paper UI

In case you want to get debug log of available registers in the heatpump you can trigger this by advanced channel
