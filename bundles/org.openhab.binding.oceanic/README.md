# Oceanic Binding

This binding integrates the Oceanic water softener and management system (www.oceanic.be, but also distributed by Syr in Germany (www.syr.de)).
The binding supports the Limex IQ and Limex Pro water softeners and require the optional CAN-Serial gateway has to be installed

## Supported Things

- **serial** - A water softener connected to the openHAB host by means of a serial port
- **network** - A water softener that can be reached through a TCP proxy. See Known Issues below as when to use this kind of configuration

## Thing Configuration

The **serial** Thing configuration requires the name of the serial port that is used to connect openHAB with the Oceanic unit, and the interval period in seconds to poll the Oceanic unit

The **network** Thing configuration requires the hostname or ip address of the proxy, the TCP port number to connect to, and the interval period in seconds to poll the Oceanic unit

## Channels

All things support the following channels (non-exhaustive):

| Channel Type ID                      | Item Type | Description                                                     |
|--------------------------------------|-----------|-----------------------------------------------------------------|
| alarm                                | String    | Current alarm description, if any                               |
| alert                                | String    | Current alert description, if any, to notify a shortage of salt |
| totalflow                            | Number    | Current flow in l/min                                           |
| maxflow                              | Number    | Maximum flow recorded, in l/min                                 |
| reserve                              | Number    | Water reserve in l before regeneration has to start             |
| cycle                                | String    | Indicates the stage of the regeneration cycle                   |
| endofcycle                           | String    | Indicates the time to the end of the current cycle              |
| endofgeneration                      | String    | Indicates the time to the end of the current generation         |
| inlethardness                        | Number    | Water hardness at the inlet                                     |
| outlethardness                       | Number    | Water hardness at the outlet                                    |
| salt                                 | String    | Volume of salt remaining, in kg                                 |
| consumption(today)(currentweek)(...) | String    | Water consumption, in l, for that period                        |
| regeneratenow                        | Switch    | Start an immediate regeneration                                 |
| regeneratelater                      | Switch    | Start a delayed regeneration                                    |
| lastgeneration                       | DateTime  | Date and Time of the last regeneration cycle                    |
| pressure                             | Number    | Water pressure, in bar                                          |
| minpressure                          | Number    | Minimum water pressure recorded, in bar                         |
| maxpressure                          | Number    | Maximum water pressure recorded, in bar                         |
| normalregenerations                  | Number    | Number of regenerations completed                               |
| serviceregenerations                 | Number    | Number of service regenerations completed                       |
| incompleteregenerations              | Number    | Number of incomplete regenerations                              |
| allregenerations                     | Number    | Number of all regenerations                                     |

## Full Example

.things

```java
Thing oceanic:serial:s1 [ port="/dev/tty.usbserial-FTWGX64N", interval=60]
Thing oceanic:network:s2 [ ipAddress="192.168.0.6", portNumber=9000, interval=60]

```

.items

```java
Number oceanicVolume "volume [%d]" (oceanic) {channel="oceanic:serial:s1:totalflow"}
String oceanicAlarm "alarm: [%s]" (oceanic) {channel="oceanic:serial:s1:alarm"}
String oceanicAlert "alert: [%s]" (oceanic) {channel="oceanic:serial:s1:alert"}
Number oceanicReserve (oceanic) {channel="oceanic:serial:s1:reserve"}
String oceanicCycle (oceanic) {channel="oceanic:serial:s1:cycle"}
String oceanicEOC (oceanic) {channel="oceanic:serial:s1:endofcycle"}
String oceanicEOG (oceanic) {channel="oceanic:serial:s1:endofgeneration"}
String oceanicHU (oceanic) {channel="oceanic:serial:s1:hardnessunit"}
Number oceanicInletHardness (oceanic) {channel="oceanic:serial:s1:inlethardness"}
Number oceanicOutletHardness (oceanic) {channel="oceanic:serial:s1:outlethardness"}
String oceanicCylState (oceanic) {channel="oceanic:serial:s1:cylinderstate"}
Number oceanicSalt (oceanic) {channel="oceanic:serial:s1:salt"}
Number oceanicConsToday "volume today is [%d]" (oceanic) {channel="oceanic:serial:s1:consumptiontoday"}
Number oceanicConsYday "volume yesterday was [%d]"(oceanic) {channel="oceanic:serial:s1:consumptionyesterday"}
Number oceanicPressure (oceanic) {channel="oceanic:serial:s1:pressure"}
DateTime oceanicLastGeneration (oceanic) {channel="oceanic:serial:s1:lastgeneration"}
Number oceanicAllGen (oceanic) {channel="oceanic:serial:s1:allregenerations"}
Number oceanicMaxFlow (oceanic) {channel="oceanic:serial:s1:maxflow"}
Number oceanicConsThisWk "volume this week is [%d]"(oceanic) {channel="oceanic:serial:s1:consumptioncurrentweek"}
Number oceanicConsThisMnth "volume this month is [%d]"(oceanic) {channel="oceanic:serial:s1:consumptioncurrentmonth"}
Number oceanicConsLastMnth "volume last month is [%d]"(oceanic) {channel="oceanic:serial:s1:consumptionlastmonth"}
Number oceanicConsComplete "volume all time is [%d]"(oceanic) {channel="oceanic:serial:s1:consumptioncomplete"}
Number oceanicConsUntreated "volume untreated is [%d]"(oceanic) {channel="oceanic:serial:s1:consumptionuntreated"}
Number oceanicConsLastWk "volume last week is [%d]"(oceanic) {channel="oceanic:serial:s1:consumptionlastweek"}
```

## Known issues

The Oceanic binding makes use of the nrjavaserial library, and unfortunately java and serial ports never have been a great marriage.

Although some work is being done to improve things (<https://github.com/eclipse/smarthome/issues/4465>), the best thing is to avoid serial ports as much as possible, as some issues (<https://github.com/NeuronRobotics/nrjavaserial/issues/96>) are not resolved.

For example, On Ubuntu 17.10 nrjavaserial seems to return only HEX 00 characters through the InputStream of the SerialPort.

Within the Oceanic binding two routes are provided:

1. Connect to the Oceanic softener over a serial port that is outside the scope of the Java Virtual Machine, setup a TCP "proxy" on the host that is connected to the softener, and make openHAB connect to that proxy over a plain TCP connection. This can be achieved with `socat`:

    ```shell
    /usr/bin/socat -v TCP-LISTEN:9000 /dev/ttyUSB0,raw,echo=0
    ```

    In the above example, the name of the host running socat, and the TCP port number 9000, will be part of the **network** Thing configuration
1. Connect to the Oceanic softener over a serial port on the openHAB host and use `socat` to pipe the data from that serial port to a pseudo tty, which has to be manipulated in a CommPortIdentifier.PORT_RAW manner.

    ```shell
    /usr/bin/socat -v /dev/ttyUSB0,raw,echo=0 pty,link=/dev/ttyS1,raw,echo=0
    ```

Both workarounds can be implemented using a systemd system manager script, for example:

```ini
[Install]
WantedBy=multi-user.target

[Service]
#Type=forking
ExecStart=/usr/bin/socat -v /dev/ttyUSB0,raw,echo=0 pty,link=/dev/ttyS1,raw,echo=0
#PIDFile=/var/run/socat.pid
User=root
Restart=always
RestartSec=10
```

However, in order to fix permissions at the OS level, one has to issue following commands in order to make /dev/ttyS1 accessible by the 'openhab' system user (that is used to start up the openHAB runtime), and to make the tty both readable and writable.

```shell
sudo useradd  -G dialout openhab
sudo chgrp dialout /dev/ttyS1
sudo chmod 777 /dev/ttyS1
```

Alternatively, these commands can be executed through a script that is attached to the systemd system manager script.
