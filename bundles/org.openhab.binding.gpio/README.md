# GPIO Binding

This binding adds GPIO support via the pigpio daemon to openhab.
It requires the pigpio (http://abyz.me.uk/rpi/pigpio/) to be running on the pi that should be controlled.

## Supported Things

### pigpio-remote

This thing represents a remote pigpio instance running as daemon on a raspberry pi.

## Thing Configuration

### Pigpio Remote  (`pigpio-remote`)

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

Set `host` to the address of the pi and the `port` to the port of pigpio (default: 8888).

## Channels

### Pigpio Remote

| channel               | type   | description                     |
|-----------------------|--------|---------------------------------|
| pigpio-digital-input  | Switch | Read-only value of the gpio pin |
| pigpio-digital-output | Switch | Controls the gpio pin           |

### GPIO digital input channel

Set the number of the pin in `gpioId`.
If you want to invert the value, set `invert` to true.
To prevent incorrect change events, you can adjust the `debouncingTime`.

### GPIO digital output channel

Set the number of the pin in `gpioId`.
If you want to invert the value, set `invert` to true.

## Full Example


demo.things:

```
Thing gpio:pigpio-remote:sample-pi-1 "Sample-Pi 1" [host="192.168.2.36", port=8888] {
    Channels:
        Type pigpio-digital-input : sample-input-1 [ gpioId=10]
        Type pigpio-digital-input : sample-input-2 [ gpioId=14, invert=true]
        Type pigpio-digital-output : sample-output-1 [ gpioId=3]
}

Thing gpio:pigpio-remote:sample-pi-2 "Sample-Pi 2" [host="192.168.2.37", port=8888] {
    Channels:
        Type pigpio-digital-input : sample-input-3 [ gpioId=16, debouncingTime=20]
        Type pigpio-digital-input : sample-input-4 [ gpioId=17, invert=true, debouncingTime=5]
        Type pigpio-digital-output : sample-output-2 [ gpioId=4, invert=true]
}
```

demo.items:

```
Switch SampleInput1 {channel="gpio:pigpio-remote:sample-pi-1:sample-input-1"}
Switch SampleOutput1 {channel="gpio:pigpio-remote:sample-pi-1:sample-output-1"}
```

demo.sitemap:

```
sitemap demo label="Main Menu"
{
    Switch item=SampleInput1
    Switch item=SampleOutput1
}
```
