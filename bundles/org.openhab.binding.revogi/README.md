# Revogi Binding

This binding is written to control Revogi devices.
The first thing implemented is the [Revogi Smart Power Strip](https://www.revogi.com/smart-power/smart-power-strip-eu/#section6).
The device has 6 power plugs that can be switched independently, or all together.
It also provides information like power consumption and electric current for each plug.

It was hard to find out how to control it without internet access, but there's a way to use UDP packets.
See the following [support document](https://github.com/andibraeu/revogismartstripcontrol/blob/master/doc/LAN%20UDP%20Control.pdf) for details. This was the only document the Revogi support provided.

## Supported Things

Currently only the model `SOW019` is supported.

## Discovery

If your smart strip is within your network (broadcast domain), discovery can work.
The discovery service will send udp packets to the broadcast address and waits for a feedback.

It is required to integrate your power strip into your network first, maybe with the official app.

## Thing Configuration

You need to know the serial number. Usually you can find it on the back.
The serial number will also be discovered.
The IP address of the device is also necessary, this address should be set static.
There's a fallback to broadcast status and switch requests.
That may be unreliable if you have more than one smart plug in your network.
They all react on UDP packets.

## Channels

| channel            | type                   | description                               |
|--------------------|------------------------|-------------------------------------------|
| overallPlug#switch | Switch                 | Switches all plugs                        |
| plug1#switch       | Switch                 | Switch plug 1                             |
| plug1#watt         | Number:Power           | Contains currently used power of plug 1   |
| plug1#amp          | Number:ElectricCurrent | Contains currently used current of plug 1 |
| plug2#switch       | Switch                 | Switch plug 2                             |
| plug2#watt         | Number:Power           | Contains currently used power of plug 2   |
| plug2#amp          | Number:ElectricCurrent | Contains currently used current of plug 2 |
| plug3#switch       | Switch                 | Switch plug 3                             |
| plug3#watt         | Number:Power           | Contains currently used power of plug 3   |
| plug3#amp          | Number:ElectricCurrent | Contains currently used current of plug 3 |
| plug4#switch       | Switch                 | Switch plug 4                             |
| plug4#watt         | Number:Power           | Contains currently used power of plug 4   |
| plug4#amp          | Number:ElectricCurrent | Contains currently used current of plug 4 |
| plug5#switch       | Switch                 | Switch plug 5                             |
| plug5#watt         | Number:Power           | Contains currently used power of plug 5   |
| plug5#amp          | Number:ElectricCurrent | Contains currently used current of plug 5 |
| plug6#switch       | Switch                 | Switch plug 6                             |
| plug6#watt         | Number:Power           | Contains currently used power of plug 6   |
| plug6#amp          | Number:ElectricCurrent | Contains currently used current of plug 6 |

## Full Example

Example Thing configuration:

```java
Thing revogi:smartstrip:<serialNumber> "<Name>" @ "<Location>" [serialNumber="<serialNumnber>", ipAddress=<ipaddress>, pollIntervall=45]
```

Example Items configuration:

```java
Group revogi (LivingRoom)

Group plug1 (revogi)
Group plug2 (revogi)

Switch All_Plugs "Steckdosen komplett" <switch> (revogi) {channel="revogi:smartstrip:<serialNumnber>:overallPlug#switch"}

Switch Plug_1 "Steckdose 1" <switch> (plug1) {channel="revogi:smartstrip:<serialNumnber>:plug1#switch"}
Number Plug_1_Watt "Steckdose 1 Leistung" <chart> (plug1) {channel="revogi:smartstrip:<serialNumnber>:plug1#watt"}
Number Plug_1_Amp "Steckdose 1 Strom" <chart> (plug1) {channel="revogi:smartstrip:<serialNumnber>:plug1#amp"}

...
```
