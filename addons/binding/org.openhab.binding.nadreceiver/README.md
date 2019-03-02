# <bindingName> Binding

If you own a NAD receiver such as the T758v3 (together with the BluOS module in that case) and your device is connected to your internal network, this binding is potentially for you.<br />
This binding connects to your NAD receiver using the Telnet protocol and exchanging text messages with it. Theoretically all NAD receivers should support this protocol.<br />
My old T756 even already supported it via a MDC module having a LAN interface.

## Supported Things

For the moment only one ``Thing`` that should support all NAD receivers (e.g. T758v3), see example below:

```
# Thing nadreceiver:receiver:t758v3 [hostname="<xxx.xxx.xxx.xxx>"]

```

## Discovery

_Currently no discovery implemented (not sure how to do it)_<br />
_You have to know your NAD receiver IP address but this shouldn't be a big deal if you are reading these lines ;-) _

## Binding Configuration

_Currently no specific configuration needed_

## Thing Configuration

Several ``Thing`` configuration 

 * hostname (mandatory) - hostname or IP address of your NAD receiver 
 * port (optional, default:23) - port of your NAD receiver
 * reconnectInterval (optional, default:5) - In minutes the period after which the binding tries to reconnect
 * heartbeatInterval (optional, default:5) - In minutes the period after which the binding reconnects (kind of keepAlive)
 * maxSources (optional, default:9) - Maximum number of sources over which the binding iterates (from 1 to maxSources), it might differ depending on your receiver 

```
Thing nadreceiver:receiver:t758v3 [hostname="192.168.1.103", port="23", reconnectInterval="5", heartbeatInterval="5", maxSources="9"]
```
Configuration via PaperUI is probably much faster and easier, but manual configuration as above is also possible.

## Channels

Currently supported channels:

 * ``model`` - Text of the model name returned by the receiver (e.g. T758)
 * ``power`` - Power status (ON or OFF)
 * ``mute`` - Mute status (ON or OFF)
 * ``volume`` - Dimmer of the volume (From 0% to 100%, corresponds to -99dB to 11dB)
 * ``source`` - Currently selected source on the receiver (Note that only "enabled" sources on the receiver are shown in the options list)
 
## Full Example

Example of a ``*.things`` file:

```
Thing nadreceiver:receiver:t758v3 [hostname="192.168.1.103" port="23" reconnectInterval="5" heartbeatInterval="5" maxSources="9"]

```

Example of a ``*.items`` file:

```
Group GroupNad "NAD T758v3" (GroupLivingRoom)

String NAD_Model 	"Model"		(GroupNad)	{channel="nadreceiver:receiver:t758v3:model"}
Switch NAD_Power	"Power"		(GroupNad)	{channel="nadreceiver:receiver:t758v3:power"}
Switch NAD_Mute		"Mute"			(GroupNad)	{channel="nadreceiver:receiver:t758v3:mute"}
Dimmer NAD_Volume	"Volume"		(GroupNad)	{channel="nadreceiver:receiver:t758v3:volume"}
String NAD_Source	"Source"		(GroupNad)	{channel="nadreceiver:receiver:t758v3:source"}

```

_With this configuration you will have a group of items (``GroupNad``) grouping all currently supported channels._<br />
_Note that the group is also under another general group (``GroupLivingRoom``)_

## Example of usage

_For my own purpose, I use this binding to enable a smart power plug powering my amplifier (It doesn't have any useful standby mode)._<br />
_I save 11W consumption with this method! The idea is to trigger power ON and power OFF to enable/disable the smart power plug._<br />
_With this binding and a mystorm binding I created following rules : _ (file ``*.rules``)

```
// Rule ON
rule "Power on mystrom when NAD is starting"

when
	Item NAD_Power changed from OFF to ON
then
	MyStrom_Switch.sendCommand(ON)
	logInfo("NAD Rules", "Send ON to mystrom because NAD is starting")
end


// Rule OFF
rule "Power off mystrom when NAD is shuting down"

when
	Item NAD_Power changed from ON to OFF
then
	MyStrom_Switch.sendCommand(OFF)
	logInfo("NAD Rules", "Send OFF to mystrom because NAD is shutting down")
end
```
