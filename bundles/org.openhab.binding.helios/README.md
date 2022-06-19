# Helios Binding

This binding integrates the Heliop door/videophone system (https://www.2n.cz).

## Supported Things

Currently, the Helios IP Vario is supported by this binding, running the 2.21 version of the firmware


## Binding Configuration

There is no specific binding configuration


## Thing Configuration

The ipvario221 Thing requires the IP address of the videophone, and the username and password as a configuration value in order for the binding to log into the videophone.

In the thing file, this looks e.g. like

```
Thing helios:ipvario213:gate [ipAddress="192.168.0.14", username="admin", password="mypassword"]
```

## Channels

All devices support the following channels:

| Channel Type ID | Item Type | Description                                                                   |
|-----------------|-----------|-------------------------------------------------------------------------------|
| keypressed      | Trigger   | Code of a key pressed on the videophone keyboard                              |
| keyreleased     | Trigger   | Code of a key released on the videophone keyboard                             |
| callstate       | String    | State of the call being made                                                  |
| calldirection   | String    | Direction (e.g. inbound, outbound) of the call being made                     |
| card            | Trigger   | ID of the card presented to the RFID reader                                   |
| cardvalid       | Switch    | The card presented to the RFID reader is valid (i.e. registered in the Vario) |
| code            | Trigger   | Numerical PIN code (i.e. 1234) entered on the keyboard                        |
| codevalid       | Switch    | The code entered is valid (i.e. registerd in the Vario)                       |
| devicestate     | String    | State of the device                                                           |

In addition, devices running the v2.13 firmware support the following channels as well:

| Channel Type ID | Item Type | Description                                                                   |
|-----------------|-----------|-------------------------------------------------------------------------------|
| audiolooptest   | Switch    | Initiate an audio loop test                                                   |
| motion          | Switch    | Indicates if motion was detected by the videophone                            |
| noise           | Switch    | Indicates if noise was detected by the videophone                             |
| switchstate     | Switch    | Indicates the state of an internal switch in the videophone                   |

For most of the channels a "stamp" channel (of Type DateTime) (e.g. "keypressedstamp") is available and will be updated with the time stamp the relevant event happened on the device.
For switchstate, there are as well the switchstateswitch and switchstateoriginator channels indicating the number of the switch that changed state (1 to 4, depending on the hardware configuration) and the source of the switch state change (keypad, DTMF signal,...)

## Full Example

demo.Things:

```
Thing helios:ipvario221:gate [ipAddress="192.168.0.14", username="admin", password="mypassword"]
```

demo.items:

```
String GateKeyStamp "[%s]" (helios) {channel="helios:ipvario221:gate:keypressedstamp"}
String GateCardSwiped "[%s]" (helios) {channel="helios:ipvario221:gate:card"}
String GateCardStamp "[%s]" (helios) {channel="helios:ipvario221:gate:cardstamp"}
String GateCardValid "[%s]" (helios) {channel="helios:ipvario221:gate:cardvalid"}
String GateCodeEntered "[%s]" (helios) {channel="helios:ipvario221:gate:code"}
String GateCodeStamp "[%s]" (helios) {channel="helios:ipvario221:gate:codestamp"}
String GateCodeValid "[%s]" (helios) {channel="helios:ipvario221:gate:codevalid"}
```

demo.rules:

```
rule SomeRule
when
    Channel "helios:ipvario221:gate:keypressed" triggered 
then
    var actionName = receivedEvent.getEvent()
    logInfo("org.openhab","Rule trigger " + actionName)
end
```
