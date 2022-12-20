# NovaFineDust Binding

This binding is for the fine dust sensor (PM Sensor) from Nova Fitness.
Currently only one model is supported, the SDS011.

It basically implements the protocol specified in [this document](https://cdn.sparkfun.com/assets/parts/1/2/2/7/5/Laser_Dust_Sensor_Control_Protocol_V1.3.pdf).
One can measure the PM 2.5 and PM 10 values with this device.
It comes very handy for detecting air pollution like neighbors firing their oven with wet wood etc. so one can deactivate the ventilation system.

## Supported Things

There is only one Thing type for this binding, which is `SDS011`.

## Discovery

There is no automatic discovery.

## Thing Configuration

There are 2 different working modes for the `SDS011` thing: Reporting and Polling.

### Reporting

This is the preferred mode and thus also configured as a default.
In this mode the sensor wakes up every `reportingInterval` minutes, performs a measurement for 30 seconds and sleeps for `reportingInterval` minus 30 seconds.
Remember: According to the [datasheet](https://www-sd-nf.oss-cn-beijing.aliyuncs.com/%E5%AE%98%E7%BD%91%E4%B8%8B%E8%BD%BD/SDS011%20laser%20PM2.5%20sensor%20specification-V1.4.pdf) the sensor has a lifetime of 8000 hours. Using a 0 as `reportingInterval` will make the sensor report its data as fast as possible.

### Polling

If one needs data in different intervals, i.e. not as fast as possible and not in intervals that are a multiple of full minutes, polling can be configured.
The `pollingInterval` parameter specifies the time in seconds when data will be polled from the sensor.

In addition to the mode one has to provide the port to which the device is connected.

A full overview about the parameters of the `SDS011` thing is given in the following table:

| parameter name    | mandatory | description                                                                           |
|-------------------|-----------|---------------------------------------------------------------------------------------|
| port              | yes       | the port the sensor is connected to, i.e. /dev/ttyUSB0.                              |
| reporting         | no        | whether the reporting mode (value=true) or polling mode should be used.               |
| reportingInterval | no        | the time in minutes between reportings from the sensor (default=1, min=0, max=30).    |
| pollingInterval   | no        | the time in seconds between data polls from the device. (default=10, min=3, max=3600) |

## Channels

Since the supported device is a sensor, both channels are read-only channels.

| channel  | type           | description                   |
|----------|----------------|-------------------------------|
| pm25     | Number:Density | This provides the PM2.5 value |
| pm10     | Number:Density | This provides the PM10 value  |

## Full Example

demo.things:

```java
Thing novafinedust:SDS011:mySDS011Report "My SDS011 Fine Dust Sensor with reporting" [ port="/dev/ttyUSB0", reporting=true, reportingInterval=1 ]
Thing novafinedust:SDS011:mySDS011Poll "My SDS011 Fine Dust Sensor with polling" [ port="/dev/ttyUSB0", reporting=false, pollingInterval=10 ]
```

demo.items:

```java
Number:Density PM25 "My PM 2.5 value" { channel="novafinedust:SDS011:mySDS011Report:pm25" }
Number:Density PM10 "My PM 10 value" { channel="novafinedust:SDS011:mySDS011Report:pm10" }
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Frame {
        Text item=PM25 label="My PM 2.5 value"
        Text item=PM10 label="My PM 10 value"
    }
}
```

## Limitations

In theory one can have multiple sensors connected and distinguish them via their device ID. However, this is currently not implemented and the binding always configures any device and accepts data reportings from any device too.
However, it is implemented that one can attach one sensor to one serial port, like `/dev/ttyUSB0` and a second sensor on a different serial port, like `/dev/ttyUSB1`.
