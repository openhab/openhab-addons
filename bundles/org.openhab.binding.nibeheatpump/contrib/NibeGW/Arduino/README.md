# NibeGW Hardware and Compiling hints


## RS-485 Modules

For settting up a NibeGW you need a RS485 module. 
While ProDiNo already have RS-485 support included, you need a separate module for Arduino Uno.
Most cheap modules out there are compatible with 5V voltage and therefore compatible with Arduino based hardware.
Mostly you will get one of two commonly used designs:

* Modules based on Max1348 chip, which has 2 pins on the Arduino side (RXD, TXD) + VCC + GND
* Modules based on Max485 chip, which has 4 pins on the Arduino side (RO, RE, DE, DI) + VCC + GND

Both types of modules work fine with NibeGW.
The difference between the two is, that the Max485 chip needs to be switched between RX mode and TX mode manually while the Max1348 chip do this automatically.
That is why you need an extra "direction pin" on the Arduino to switch the module with Max485 chip between the two modes.

#### Wiring diagram for Max1348 based modules:

```
 TX  RX   5V  GND    Arduino
 |   |    |    |
 |   |    |    |
 |   |    |    |
 RX  TX  VCC  GND    Max1348 based module
```

#### Wiring diagram for Max485 (PIN2 is used as direction pin here):

```
 TX  RX  PIN2      5V  GND    Arduino
 |   |    |        |    |
 |   |    |        |    |
 |   |    |---|    |    |
 DI  RO   DE  RE  VCC  GND    Max485 bases module
```


## Ethernet Shield W5100

This Ethernet shield is based on Wiznet W5100 Ethernet Chip.
It is supported by the Arduino Ehternet Library.
No special configuration is needed, NibeGW supports this shield out of the box.

ProDiNo already have Ethernet included, so there's no need for a separate Ethernet Shield.
Also the ProDiNo Ethernet is supported by NibeGW out of the box.


## Arduino Uno

Arduino Uno has only one serial port which is shared with USB. 
So make sure to disconnect all hardware (ethernet shield, RS485 module, etc.) while uploading the compiled sketch to the Arduino.
Furthermore do not use the USB port while Arduino is communicating with the Nibe heatpump.

For compiling NibeGW, you have to make the following changes to the code:

#### Config.h:

Comment out support for all special boards:

```
//#define PRODINO_BOARD
//#define PRODINO_BOARD_ESP32
//#define TRANSPORT_ETH_ENC28J6A0
//#define ENABLE_DYNAMIC_CONFIG
```

Comment out debugging on the serial console:

```
//#define ENABLE_SERIAL_DEBUG
```

Adjust the settings for your ethernet connection, target ip and ports and modbus module to simulate.
Leave the serial configuration untouched - it is fine for Arduino Uno.

#### NibeGW.h:

Enable support for HARDWARE_SERIAL:

```
//#define HARDWARE_SERIAL_WITH_PINS
#define HARDWARE_SERIAL
```

##  ProDiNo ESP32 Ethernet v1 

NibeGW default settings are valid for ProDiNo ESP32 Ethernet v1 board.
Dynamic configuration is enabled by default.

### Config.h:

```c
//#define PRODINO_BOARD
#define PRODINO_BOARD_ESP32
//#define TRANSPORT_ETH_ENC28J6A0
```

### NibeGW.h:

```c
#define HARDWARE_SERIAL_WITH_PINS
//#define HARDWARE_SERIAL
```

### Install libraries

Install [ProDinoESP32](https://github.com/kmpelectronics/ProDinoESP32) library (tested with version 2.0.0).

NibeGW code is compatible with ESP32 v2.0.x board library by Espressif Systems.
Install correct ESP32 library via Arduino IDE board manager.


## ProDiNo Ethernet V2

Todo


## Debugging

Debugging messages are available by connecting to port 23 to your NibeGW via telnet.
Enable debugging in Config.h:

```
#define ENABLE_DEBUG
#define VERBOSE_LEVEL 5
#define ENABLE_REMOTE_DEBUG     // Remote debug is available in telnet port 23
```

You can connect to NibeGW with any telnet client.
You can also set some options via telnet.
With 'h' you get a menu with all available options:

```
Arduino NibeGW
Commands:
 E -> exit
 i -> info
 1 -> set verbose level to 1
 2 -> set verbose level to 2
 3 -> set verbose level to 3
 4 -> set verbose level to 4
 5 -> set verbose level to 5
```

On the target IP you can see the receiving udp messages with netcat (if you changed the default target port 9999, you also have to adjust it here):

```
nc -lu 9999 | hexdump -C
```

## Dynamic Configuration

When dynamic configuration is enabled (only ESP32 boards), NibeGW can be configured via Wi-Fi connection.
Also OTA firmware update is supported.

The following libraries are required:
 * Bleeper (tested with version 1.1.0)
 * ElegantOTA (tested with version 2.2.9)

Dynamic configuration mode is loaded if input 0 is ON during boot.
When dynamic configuration mode is activated, login to the 'Bleeper' Wi-Fi access point.
Configuration page is available on port 80.
OTA update page on port 8080.

### Disable Dynamic Configuration

Dynamic configuration can be disabled by commenting out following line from config.h file.

```c
//#define ENABLE_DYNAMIC_CONFIG
```
