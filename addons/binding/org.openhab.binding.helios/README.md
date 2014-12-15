# Helios Binding

This binding integrates the Heliop door/videophone system (http://www.2n.cz).

## Supported Things

Currently, the Helios IP Vario is supported by this binding, running the 2.7 or 2.13 version of the firmware


## Binding Configuration

There is no specific binding configuration


## Thing Configuration

The ipvario213 (or ipvario27) Thing requires the IP address of the videophone, and the username and password as a configuration value in order for the binding to log into the videophone.

In the thing file, this looks e.g. like

```
Thing helios:ipvario213:gate [ipAddress="192.168.0.14", username="admin", password="mypassword"]
```

## Channels

All devices support the following channels:

| Channel Type ID | Item Type | Description                                                                   |
|-----------------|-----------|-------------------------------------------------------------------------------|
| keypressed      | String    | Code of a key pressed on the videophone keyboard                              |
| keyreleased     | String    | Code of a key released on the videophone keyboard                             |
| callstate       | String    | State of the call being made                                                  |
| calldirection   | String    | Direction (e.g. inbound, outbound) of the call being made                     |
| card            | String    | ID of the card presented to the RFID reader                                   |
| cardvalid       | Switch    | The card presented to the RFID reader is valid (i.e. registered in the Vario) |
| code            | String    | Numerical PIN code (i.e. 1234) entered on the keyboard                        |
| codevalid       | Switch    | The code entered is valid (i.e. registerd in the Vario)                       |
| devicestate     | String    | State of the device                                                           |

In addition, devices running the v2.13 firmware support the following channels as well:

| Channel Type ID | Item Type | Description                                                                   |
|-----------------|-----------|-------------------------------------------------------------------------------|
| audiolooptest   | Switch    | Initiate a audio loop test                                                    |
| motion          | Switch    | Indicates if motion was detected in from of the videophone                    |

For most of the channels a "stamp" channel (of Type DateTime) (e.g. "keypressedstamp") is available and will be updated with the time stamp the relevant event happened on the device

## Full Example

demo.Things:

```
Thing helios:ipvario213:gate [ipAddress="192.168.0.14", username="admin", password="mypassword"]
```

demo.items:

```
String GateKeyPressed "[%s]" (helios) {channel="helios:ipvario213:gate:keypressed"}
String GateKeyStamp "[%s]" (helios) {channel="helios:ipvario213:gate:keypressedstamp"}
String GateCardSwiped "[%s]" (helios) {channel="helios:ipvario213:gate:card"}
String GateCardStamp "[%s]" (helios) {channel="helios:ipvario213:gate:cardstamp"}
String GateCardValid "[%s]" (helios) {channel="helios:ipvario213:gate:cardvalid"}
String GateCodeEntered "[%s]" (helios) {channel="helios:ipvario213:gate:code"}
String GateCodeStamp "[%s]" (helios) {channel="helios:ipvario213:gate:codestamp"}
String GateCodeValid "[%s]" (helios) {channel="helios:ipvario213:gate:codevalid"}
```
