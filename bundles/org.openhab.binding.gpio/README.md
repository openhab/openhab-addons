# GPIO Binding

This binding adds GPIO support via the pigpio daemon to openhab.
It requires the pigpio (<http://abyz.me.uk/rpi/pigpio/>) to be running on the pi that should be controlled.

## Supported Things

### pigpio-remote

This thing represents a remote pigpio instance running as daemon on a raspberry pi.

## Thing Configuration

### Pigpio Remote  (`pigpio-remote`)

On a raspberry (or a compatible device) you have to install pigpio:

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

Now that Remote GPIO is enabled, get the daemon going (even if installed with apt-get):

```shell
sudo systemctl enable pigpiod 
sudo systemctl start pigpiod
```

In openHAB, set `host` to the address of the pi and the `port` to the port of pigpio (default: 8888).

Note: If you are running Pigpio on same host as openHAB, then set host to **::1**.

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
Using `pullupdown` you can enable pull up or pull down resistor (OFF = Off, DOWN = Pull Down, UP = Pull Up).

### GPIO digital output channel

Set the number of the pin in `gpioId`.
If you want to invert the value, set `invert` to true.

## Full Example

demo.things:

```java
Thing gpio:pigpio-remote:sample-pi-1 "Sample-Pi 1" [host="192.168.2.36", port=8888] {
    Channels:
        Type pigpio-digital-input : sample-input-1 [ gpioId=10]
        Type pigpio-digital-input : sample-input-2 [ gpioId=14, invert=true]
        Type pigpio-digital-output : sample-output-1 [ gpioId=3]
}

Thing gpio:pigpio-remote:sample-pi-2 "Sample-Pi 2" [host="192.168.2.37", port=8888] {
    Channels:
        Type pigpio-digital-input : sample-input-3 [ gpioId=16, debouncingTime=20]
        Type pigpio-digital-input : sample-input-4 [ gpioId=17, invert=true, debouncingTime=5, pullupdown="UP"]
        Type pigpio-digital-output : sample-output-2 [ gpioId=4, invert=true]
}
```

demo.items:

```java
Switch SampleInput1 {channel="gpio:pigpio-remote:sample-pi-1:sample-input-1"}
Switch SampleOutput1 {channel="gpio:pigpio-remote:sample-pi-1:sample-output-1"}
```

demo.sitemap:

```perl
sitemap demo label="Main Menu"
{
    Switch item=SampleInput1
    Switch item=SampleOutput1
}
```
