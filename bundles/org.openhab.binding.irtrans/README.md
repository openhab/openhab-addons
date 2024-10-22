# IRtrans Binding

This binding integrates infrared receivers and blasters manufactured by IRtrans <www.irtrans.de>

## Supported Things

This binding supports the following Thing types

| Thing    | Thing Type | Description                                                                |
|----------|------------|----------------------------------------------------------------------------|
| ethernet | Bridge     | Ethernet (PoE) IRtrans transceiver equipped with an on-board IRDB database |
| blaster  | Thing      | Child thing representing an IR Blaster                                     |

## Discovery

There is no Discovery feature available.

## Binding Configuration

There is no specific binding configuration required.

## Thing Configuration

### `ethernet` Bridge Configuration

| Parameter         | Description                                                                                           | Config   | Default |
|-------------------|-------------------------------------------------------------------------------------------------------|----------|---------|
| ipAddress         | The IP address or hostname of the bridge                                                              | Required | -       |
| portNumber        | The port number the bridge listens on                                                                 | Required | -       |
| bufferSize        | Buffer size used by the TCP socket when sending and receiving commands to the transceiver             | Optional | 1024    |
| responseTimeOut   | Specifies the time milliseconds to wait for a response from the transceiver when sending a command    | Optional | 100     |
| pingTimeOut       | Specifies the time milliseconds to wait for a response from the transceiver when pinging the device   | Optional | 1000    |
| reconnectInterval | Specifies the time seconds to wait before reconnecting to a transceiver after a communication failure | Optional | 10      |

### `blaster` Thing Configuration

The `blaster` Thing reuires an `ethernet` (Bridge) before it can be used.

| Parameter | Description                                                                                                                                                                      | Config   | Default |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------|
| led       | Specifies the led on which infrared commands will be emitted                                                                                                                     | Required | -       |
| remote    | The remote or manufacturer name which's commands will be allowed, as defined in the IRtrans server database that is flashed into the transceiver (can be ' \*' for 'any' remote) | Required | -       |
| command   | The name of the command will be allowed, as defined in the IRtrans server database that is flashed into the transceiver (can be '*' for 'any' command)                           | Required | -       |

## Channels

### `ethernet` Bridge Channels

| Channel Type ID | Item Type | Description                                                                         |                                            |
|-----------------|-----------|-------------------------------------------------------------------------------------|--------------------------------------------|
| blaster         | String    | Send (filtered) infrared commands over the specified blaster LED of the transceiver | [Channel configuration](#blaster-channel)  |
| receiver        | String    | Receive (filtered) infrared commands on the receiver LED of the transceiver         | [Channel configuration](#receiver-channel) |

### `ethernet` Bridge Channel Configuration

#### `blaster` Channel

| Parameter | Description                                                                                                                                                                      | Config   | Default |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------|
| led       | Specifies the led on which infrared commands will be emitted                                                                                                                     | Required | -       |
| remote    | The remote or manufacturer name which's commands will be allowed, as defined in the IRtrans server database that is flashed into the transceiver (can be ' \*' for 'any' remote) | Required | -       |
| command   | The name of the command will be allowed, as defined in the IRtrans server database that is flashed into the transceiver (can be '*' for 'any' command)                           | Required | -       |

#### `receiver` Channel

| Parameter | Description                                                                                                                                                                      | Config   | Default |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|---------|
| remote    | The remote or manufacturer name which's commands will be allowed, as defined in the IRtrans server database that is flashed into the transceiver (can be ' \*' for 'any' remote) | Required | -       |
| command   | The name of the command will be allowed, as defined in the IRtrans server database that is flashed into the transceiver (can be '*' for 'any' command)                           | Required | -       |

### `blaster` Thing Channels

| Channel Type ID | Item Type | Description                                                                                                               |                                      |
|-----------------|-----------|---------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| io              | String    | Allows to read infrared commands received by the blaster, as well as to write infrared commands to be sent by the blaster | [Channel configuration](#io-channel) |

*note*
The IRtrans transceivers store infrared commands in a "remote,command" table, e.g. "telenet,power". Sending the literal text string "telenet,power" to the transceiver will make the transceiver "translate" that into the actual infrared command that will be emitted by the transceiver.  A "remote,command" string sent to a Channel that does not match the defined filter will be ignored.

## Full Example

demo.things:

```java
Bridge irtrans:ethernet:kitchen [ ipAddress="192.168.0.56", portNumber=21000, bufferSize=1024, responseTimeOut=100, pingTimeOut=2000, reconnectInterval=10 ]
{
Channels:
Type receiver : any [remote="*", command="*"]
Type receiver : telenet_power [remote="telenet", command="power"]
Type blaster : samsung [led="E", remote="samsung", command="*"]
}
```

In the above example, the first channel will be updated when any IR command from any type of device is received. The second channel will only be updated if a "power" infrared command from the remote/device type "telenet" is received. The third channel can be used to feed any type of infrared command to a Samsung television by means of the "E" emitter of the IRtrans device.
The led can be "E"-External, "I"-Internal, "B"-Both, and a numeric for a selected led.
Depending on the number of remotes, the bufferSize must be adjusted. E.g. for 7 remotes and 47 commands a bufferSize of 2048 is needed.

```java
Bridge irtrans:ethernet:technicalfacilities [ ipAddress="192.168.0.58", portNumber=21000, bufferSize=1024, responseTimeOut=100, pingTimeOut=2000, reconnectInterval=10 ]
{
Channels:
Type receiver : any [remote="*", command="*"]
Type blaster : telenet1 [led="2", remote="telenet", command="*"]
Type blaster : telenet2 [led="1", remote="telenet", command="*"]
Type blaster : appletv [led="3", remote="appletv", command="*"]
}
```

In the above channel a single IRtrans transceiver has 3 output LEDs in use, 2 to drive 2 DTV SetTopBoxes, and a third one to drive an Apple TV device.

demo.items:

```java
String KitchenIRReceiverAny {channel="irtrans:ethernet:kitchen:any"}
String KitchenIRReceiverTelenetPower {channel="irtrans:ethernet:kitchen:telenet_power"}
String KitchenIRBlasterSamsung {channel="irtrans:ethernet:kitchen:samsung"}

String TechnicalFacilitiesIRReceiverAny {channel="irtrans:ethernet:technicalfacilities:any"}
String TechnicalFacilitiesIRBlasterTelenet2 {channel="irtrans:ethernet:technicalfacilities:telenet2"}
String TechnicalFacilitiesIRBlasterTelenet1 {channel="irtrans:ethernet:technicalfacilities:telenet1"}
String TechnicalFacilitiesIRBlasterAppleTV {channel="irtrans:ethernet:technicalfacilities:appletv"}
```

demo.rules:

```java
rule "Kitchen switch IR rule"
when
    Item KitchenIRReceiverTelenetPower received update
then
    createTimer(now.plusSeconds(5)) [|
        KitchenIRBlasterSamsung.sendCommand("samsung,power")
        ]
end
```
