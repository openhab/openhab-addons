# Modbus Sungrow Binding

<img src="./doc/sungrow_logo_25_pc.png" alt="Sungrow logo" align="right"/>

This binding integrates the sungrow inverters into openHAB.
It is based on the Sungrow specification "Communication Protocol of Residential Hybrid Inverter V1.0.23",
which can be found here: https://github.com/bohdan-s/SunGather/issues/36.

## Supported inverters

As said within the spec the following inverters are supported (but not all are tested yet):

- SH3K6
- SH4K6
- SH5K-20
- SH5K-V13
- SH3K6-30
- SH4K6-30
- SH5K-30
- SH3.0RS
- SH3.6RS
- SH4.0RS
- SH5.0RS
- SH6.0RS
- SH5.0RT
- SH6.0RT
- SH8.0RT
- SH10RT

## Supported Things

The binding supports only one thing:

- `sungrowInverter`: The sungrow inverter

## Preparation

The data from the inverter is read via Modbus. So you need to configure a Modbus Serial Slave `serial` or Modbus TCP Slave `tcp` as bridge first.
If you are using a Modbus TCP Slave and the WiNet-S Communication Module please ensure:

- that you have the correct IP-Address of your WiNet-S Device
- that Modbus is enabled within the Communication Module
- that you've the correct port number
- that the white list is disabled or your openHAB instance IP is listed

<img src="./doc/WiNet-S_Modbus.PNG" alt="WiNet-S Modbus configuration"/>

## Thing Configuration

Once you've configured the Modbus TCP Slave or Modbus Serial Slave as Bridge you can configure the Sungrow inverter thing.
You just have to select the configured bridge and give the 

### `sungrowInverter` Thing Configuration

| Name            | Type    | Description                          | Default | Required | Advanced |
|-----------------|---------|--------------------------------------|---------|----------|----------|
| pollInterval    | integer | Interval the device is polled in ms.  | 5000    | no       | no       |

## Channels

The `sungrowInverter` thing has channels that serve the current state of the sungrow inverter,
as you are used to from the iSolareCloud Website and App.
