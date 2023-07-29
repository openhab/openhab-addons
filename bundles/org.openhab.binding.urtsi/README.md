# Somfy URTSI II Binding

The addressable Universal RTS Interface II (URTSI II) can be used to communicate between home automation or other third party systems and SOMFY’s RTS Motors and controls.
It is capable of individual or group control, and can be operated via infrared remote, RS232 and RS485 serial communication.
Once an input is activated, an RTS radio command is sent to the automated window treatment.

The binding supports the RS232 communication.

## Supported Things

There are two supported things:

- URTSI II Device: Bridge
- RTS Device (e.g. Rollershutter): Thing

## Discovery

Discovery is not supported.

## Thing Configuration

### URTSI II Device:

- Port: The port which is used to access the device (e.g. /dev/ttyUSB0)
- Command execution interval: The time (in ms) the binding should wait between sending commands to the device

### RTS Device (e.g. rollershutter):

- Channel: The URTSI II channel the RTS device is assigned to at URTSI II.

## Channels

- Position: Change the position of a device. This is used in order to interact with the device (move it up, down or stop).
