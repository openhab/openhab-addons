# GPIO Binding

This binding adds GPIO support via the jpigpio libary for openhab. It requires pigpio (http://abyz.me.uk/rpi/pigpio/) to be running on the pi that should be controlled.

## Supported Things

### pigpio remote bridge

This bridge represents a pigpio instance. (See pigpio install for install instructions)

### digital input thing

Get the digital input of an GPIO pin

### digital output thing

Set a GPIO Pin to on or off

## Thing Configuration

### Pigpio Remote Bridge (`pigpio-remote-bridge`)

On a raspberry (or a compatible device) you have to install pigpio:

```
sudo apt-get install pigpiod
sudo raspi-config 
```

-> Interfacing Options --> Remote GPIO --> YES --> OK --> Finish

```
sudo systemctl enable pigpiod 
sudo systemctl start pigpiod
```

Set `ipAddress` to the address of the pi and the `port` to the port of pigpio (default: 8888).

### GPIO digital input thing

Set the number of the pin in `gpioId` . If you want to invert the value, set `invert` to true. To prevent incorrect change events, you can adjust the `debouncingTime`.

### GPIO digital output thing

Set the number of the pin in `gpioId` . If you want to invert the value, set `invert` to true. 
 
## Channels

### GPIO digital input thing

| channel             | type   | description                     |
|---------------------|--------|---------------------------------|
| gpio-digital-input  | Switch | Read-only value of the gpio pin |

### GPIO digital output thing

| channel             | type   | description           |
|---------------------|--------|-----------------------|
| gpio-digital-output | Switch | Controls the gpio pin |

## Full Example


demo.Things:

```
Bridge gpio:pigpio-remote-bridge:mybridge "MyBridge" [ ipAddress="192.168.1.2", port=8888 ] {
    gpio-digital-output myoutput "My Output Pin" [ gpioId=4 ]
    gpio-digital-output myoutputinv "My Output Pin inverted" [ gpioId=5, invert=true ]
    gpio-digital-input myinput "My Input Pin " [ gpioId=6, debouncingTime=10 ]
}
```

demo.items:

```
Switch  MyOutput    {channel="gpio:gpio-digital-output:myoutput:gpio-digital-output"}
Switch  MyOutputInv {channel="gpio:gpio-digital-output:myoutputinv:gpio-digital-output"}
Switch  MyInput     {channel="gpio:gpio-digital-input:myinput:gpio-digital-input"}
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Switch item=MyOutput
    Switch item=MyOutputInv
    Switch item=MyInput
}
```