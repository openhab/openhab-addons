# Somfy URTSI II Binding

The addressable Universal RTS Interface II (URTSI II) can be used to communicate between home automation or other third-party systems and Somfy’s RTS motors and controls.
It is capable of individual or group control, and can be operated via infrared remote, RS232 and RS485 serial communication.
Once an input is activated, an RTS radio command is sent to the automated window treatment.

The binding supports RS‑232 communication.

## Supported Things

There are two supported Things:

- URTSI II device: Bridge
- RTS device (e.g., roller shutter): Thing

## Discovery

Discovery is not supported.

## Thing Configuration

### URTSI II device

- Port: The port used to access the device (e.g., /dev/ttyUSB0)
- Command execution interval: The time in milliseconds the binding waits between sending commands to the device

### RTS device (e.g., roller shutter)

- Channel: The URTSI II channel the RTS device is assigned to on the URTSI II.

## Channels

- Position: Change the position of a device. This is used to interact with the device (move it up, down, or stop).
