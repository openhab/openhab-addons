# HDanywhere Binding

This binding integrates with [HDanywhere](https://www.hdanywhere.co.uk) HDMI matrices.

HDanywhere is a manufacturer of multiroom/distributed audio/video equipment.
This binding supports their V3 of the Multiroom+ HDMI matrix running firmware V1.2(20131222), as well as the newer MHUB series of matrices.
These matrices support the highest HD resolutions, including 1080p 3D & 4K, use a single Cat5e/6/7 wiring structure with reliable performance up to 100m,
have IR passback to allow you to select and control what you watch from every room and are fully compatible with universal remote controls

The matrices can be controlled by either UDP/IP and/or Serial connections,
but due to the lack of feedback on the actual state of the HDMI matrix when using those methods, this binding operates by controlling the built-in webserver of the matrix.

## Supported Things

This binding currently supports the following thing types:

- _multiroomplus_ : Multiroom+ V3 (**Note:** This product is no longer sold by HDanywhere)
- _mhub4k431_ : MHUB 4K (4X3+1)

## Discovery

The binding does not support a discovery feature.

## Binding Configuration

This binding does not require any special configuration.

## Thing Configuration

Each thing requires the IP address of the matrix, and the interval in between status updates that are fetched from the matrix.
Additionally, the _multiroomplus_ has an additional required parameter 'ports' to specify the number of physical ports (e.g. 4x4, 8x8,...) of the matrix.

```java
Thing hdanywhere:mhub4k431:m1 [ipAddress="192.168.0.89",interval=15]
Thing hdanywhere:multiroomplus:m2 [ipAddress="192.168.0.88", ports=4, interval=15]
```

## Channels

The following channels are supported (actual number of channels is a function of the number of physical ports on the matrix):

| Thing Type | Item Type | Description                                                         |
|------------|-----------|---------------------------------------------------------------------|
| port1      | Number    | The number of the input port that is connected to the output port 1 |
| port2      | Number    | The number of the input port that is connected to the output port 2 |
| port3      | Number    | The number of the input port that is connected to the output port 3 |
| ...        | Number    | ...                                                                 |
| port8      | Number    | The number of the input port that is connected to the output port 8 |

## Example

demo.Things:

```java
hdanywhere:mhub4k431:m1 [ipAddress="192.168.0.89",interval=15]
```

demo.items:

```java
Number OutputPort1 "Output port 1 is currently connected to Source port [%d]" { channel="hdanywhere:mhub4k431:m1:port1" }
Number OutputPort2 "Output port 2 is currently connected to Source port [%d]" { channel="hdanywhere:mhub4k431:m1:port2" }
Number OutputPort3 "Output port 3 is currently connected to Source port [%d]" { channel="hdanywhere:mhub4k431:m1:port3" }
Number OutputPort4 "Output port 4 is currently connected to Source port [%d]" { channel="hdanywhere:mhub4k431:m1:port4" }
```
