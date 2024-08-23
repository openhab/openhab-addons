# GPIO Binding

This binding adds GPIO support via the pigpiod daemon to openHAB.
It requires the pigpiod daemon (<http://abyz.me.uk/rpi/pigpio/>) to be installed on the pi that should be controlled.

## Supported Things

### pigpio-remote

This thing represents a remote pigpiod instance running as daemon on a raspberry pi.

## Thing Configuration

### Pigpio Remote  (`pigpio-remote`)

On a raspberry (or a compatible device) you have to install pigpiod.

```shell
sudo apt-get install pigpiod
sudo raspi-config 
```

-> Interfacing Options --> Remote GPIO --> YES --> OK --> Finish

Note: if you are setting this up on a Raspberry Pi without `raspi-config` you can create the service config file manually:

```shell
sudo mkdir -p /etc/systemd/system/pigpiod.service.d/
sudo nano /etc/systemd/system/pigpiod.service.d/public.conf
```

```text
[Service]
ExecStart=
ExecStart=/usr/bin/pigpiod
```

```shell
sudo systemctl daemon-reload
```

Now that Remote GPIO is enabled, get the pigpiod daemon going (even if installed with apt-get):

```shell
sudo systemctl enable pigpiod 
sudo systemctl start pigpiod
```

## General Configuration

Binding general configuration options. If you do not see all options, ensure `Show Advanced` is selected.

### Host

Set `Host` to the address of the Pi that pigpiod is running on. Default is 127.0.0.1 (IPV4).
Note: If you are running pigpiod on same host as openHAB, set the host to 127.0.0.1 (IPV4) or ::1 (IPV6).

### Port

Set `Port` to the network port that pigpiod is listening on. Default is 8888.

### Heart Beat Interval

The binding will poll pigpiod running on the Pi to determine if a network disconnect has occurred.
This is the interval in milliseconds that the heart beat poll occurs. Defaults to 30000 (30 seconds).

## Input Channel Connect Action

Input Channel Connect Action determines what happens when the binding initially connects to pigpiod.
This action only occurs once after binding startup.

- **Do Nothing:** The default, do nothing. Input channels will retain their default value (UNDEF).
- **Refresh Channel:** Issues a refresh command on the input channels. This will refresh the channels from pigpiod causing the gpio pin state to reflect on the channel state.

Input Channel Disconnect Connect Action:

### Input Channel Disconnect Connect Action

Input Channel Disconnect Connect Action determines what happens when the binding disconnects from pigpiod.

- **Do Nothing:** The default, do nothing. Input channels will retain their current value.
- **Set Undef:** Sets the input channel states to UNDEF to indicate that pigpiod has disconnected.

### Input Channel Reconnect Connect Action

Input Channel Reconnect Action determines what happens when the binding reconnects to pigpiod
after a disconnect. This action does not occur on the initial binding connect to pigpiod.
startup.
  
- **Do Nothing:** The default, do nothing. Input channels will retain their current value.
- **Refresh Channel:** Issues a refresh command on the input channels. This will refresh the channels from
                    pigpiod causing the gpio pin state to reflect on the channel state.

### Output Channel Connect Action

Output Channel Connect Action determines what happens when the binding initially connects to pigpiod.
This action only occurs once after binding startup.
  
- **Do Nothing:** The default, do nothing. Output channels will retain their default value (UNDEF).
- **All On:** Issues a ON command to all configured output channels.
- **All Off:** Issues a OFF command to all configured output channels.
- **Refresh Channel:** Issues a refresh command on the output channels. This will refresh the channels from
                    pigpiod causing the gpio pin state to reflect on the channel state. NOTE: This does
                    not update the gpio pin state on the Pi itself. It only updates the channel state
                    within openHAB.

### Output Channel Disconnect Connect Action

Output Channel Disconnect Connect Action determines what happens when the binding disconnects from pigpiod.
  
- **Do Nothing:** he default, do nothing. Input channels will retain their current value.
- **Set Undef:** Sets the output channel states to UNDEF to indicate that pigpiod has disconnected.

### Output Channel Reconnect Connect Action

Output Channel Reconnect Action determines what happens when the binding reconnects to pigpiod
after a disconnect. This action does not occur on the initial binding connect to pigpiod.
  
- **Do Nothing:** The default, do nothing. Output channels will retain their current value.
- **Refresh Channel:** Issues a refresh command on the output channels. This will refresh the channels from
                    pigpiod causing the gpio pin state to reflect on the channel state. NOTE: This does
                    not update the gpio pin state on the Pi itself. It only updates the channel state
                    within openHAB.

## Channels

The binding has two channel types.
One for gpio input pins, and another for gpio output pins.

| channel               | type   | description                     |
|-----------------------|--------|---------------------------------|
| pigpio-digital-input  | Switch | Read-only value of the gpio pin |
| pigpio-digital-output | Switch | Controls the gpio pin           |

### GPIO pigpio-digital-input channel configuration

Input channels provide a read-only value of the gpio pin state using the `OnOffType` datatype.

GPIO Pin:

The gpio pin number on the Pi that the channel will monitor.

Invert:

Inverts the value of the gpio pin before reflecting the value on the channel.
Useful for active low gpio pins where you want the channel state to reflect an ON value when the pin is low (OFF).

Delay Time:

Sets a delay value in milliseconds that the gpio pin must remain at prior to updating the channel state.
This is the same as switch debouncing or hysteresis.
Default value is 10 milliseconds.
Increase this value for noisy inputs.

Pull Up/Down Resistor:

Sets the mode of operation for the internal pull up/ down resistor on the gpio pin.
Set this to OFF if you use external pull up/down resistors.

Edge Detection Mode:

Sets the mode of operation for the pin edge detection mode.
If you are not sure what the use case is for this, leave at the default value of `Either Edge`.
This is the most common mode that gpio inputs are used.

### GPIO pigpio-digital-output channel configuration

Output channels provide a means of controlling the output value of the gpio pin using the `OnOffType` datatype.

GPIO Pin:

The gpio pin number on the Pi that the channel will control.

Invert:

Inverts the value of the channel state before commanding the gpio pin.
Useful to simulate active low gpio pins.

Pulse:

Time in milliseconds that must elapse before the Pulse Command is sent to the channel.
Default value is 0, which disables the Pulse feature.

Pulse Command:

Together with the Pulse configuration, can be used to create a one shot or momentary output.
This is useful to simulate momentary button presses or to drive motors for a predefined amount
of time.

- **Off:** When the ON command is issued to the channel. The Pulse feature will send an OFF command
                    after the Pulse duration.
- **On:** When the OFF command is issued to the channel. The Pulse feature will send an
                    ON command after the Pulse duration.
- **Blink:** Cycles the channel ON, OFF, ON indefinitely with a 50% duty cycle. The Blink
                    operation continues regardless of the commanded channel state. This was originaly
                    developed as a way to flash a status LED to visually confirm that a remote pigpiod
                    instance has connectivity to openHAB.

## Config file example

Example for users who still prefer configuration files.

demo.things:

```java
Thing gpio:pigpio-remote:mypi "MyPi GPIO" [ host="192.168.1.5", port=8888,
                                                heartBeatInterval=10000,
                                                inputConnectAction="REFRESH",      # REFRESH,NOTHING
                                                inputDisconnectAction="NOTHING",   # SETUNDEF,NOTHING
                                                inputReconnectAction="REFRESH",    # REFRESH,NOTHING
                                                outputConnectAction="REFRESH",     # ALLOFF,ALLON,REFRESH,NOTHING
                                                outputDisconnectAction="SETUNDEF", # SETUNDEF,NOTHING
                                                outputReconnectAction="REFRESH" ]  # REFRESH,NOTHING
    {
        Channels:
                Type pigpio-digital-output : BCM18 [ gpioId=18,invert=false,pulse=3000,pulseCommand="BLINK" ] # OFF,ON,BLINK

                Type pigpio-digital-output : GPO4  [ gpioId=4, invert=true,pulse=5000,pulseCommand="OFF" ]
                Type pigpio-digital-output : GPO17 [ gpioId=17,invert=false,pulse=500,pulseCommand="ON" ]
                Type pigpio-digital-output : GPO27 [ gpioId=27,invert=false ]
                Type pigpio-digital-output : GPO22 [ gpioId=22,invert=true ]

                Type pigpio-digital-input  : GPI23 [ gpioId=23,debouncingTime=50,pullupdown="UP",invert=true ] # OFF,DOWN,UP
                Type pigpio-digital-input  : GPI24 [ gpioId=24,debouncingTime=50,pullupdown="UP",invert=true ]
                Type pigpio-digital-input  : GPI25 [ gpioId=25,debouncingTime=50,pullupdown="UP",invert=true ]
                Type pigpio-digital-input  : GPI12 [ gpioId=12,debouncingTime=50,pullupdown="UP",invert=true ]
                Type pigpio-digital-input  : GPI16 [ gpioId=16,debouncingTime=50,pullupdown="UP",invert=true ]
                Type pigpio-digital-input  : GPI20 [ gpioId=20,debouncingTime=50,pullupdown="UP",invert=true,edgeMode="EDGE_EITHER" ] # EITHER,RISING,FALLING
                Type pigpio-digital-input  : GPI21 [ gpioId=21,debouncingTime=50,pullupdown="UP",invert=true,edgeMode="EDGE_RISING" ]
                Type pigpio-digital-input  : GPI5  [ gpioId=5, debouncingTime=50,pullupdown="UP",invert=true,edgeMode="EDGE_FALLING" ]
                Type pigpio-digital-input  : GPI6  [ gpioId=6, debouncingTime=50,pullupdown="UP",invert=true ]
                Type pigpio-digital-input  : GPI13 [ gpioId=13,debouncingTime=50,pullupdown="DOWN",invert=false ]
                Type pigpio-digital-input  : GPI26 [ gpioId=26,debouncingTime=50,pullupdown="OFF",invert=false ]
    } 
```

demo.items:

```java
Switch SampleInput1 {channel="gpio:pigpio-remote:mypi:GPI23"}
Switch SampleOutput1 {channel="gpio:pigpio-remote:mypi:GPO4"}
```
