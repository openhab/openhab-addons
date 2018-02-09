# Zoneminder Binding

This binding offers integration to a ZoneMinder Server. It currently only offers to integrate to monitors (eg. cameras in ZoneMinder).
It also only offers access to a limited set of values, as well as a even more limited option to update values in ZoneMinder.
It requires at least ZoneMinder 1.29 with API enabled (option 'OPT_USE_API' in ZoneMinder must be enabled).
The option 'OPT_TRIGGERS' must be anabled to allow openHAB to trip the ForceAlarm in ZoneMinder.

## Supported Things

This binding supports the following thing types

<table>
<tr><td><b>Thing</b></td><td><b>Thing Type</b></td><td><b>Discovery</b></td><td><b>Description</b></td></tr>
<tr><td>ZoneMinder Server</td><td>Bridge</td><td>Manual</td><td>A ZoneMinder Server. Required version is minimum 1.29</td></tr>
<tr><td>ZoneMinder Monitor</td><td>Thing</td><td>Automatic</td><td>Monitor as defined in ZoneMinder Server</td></tr>
</table>

## Getting started /  Discovery

The binding consists of a Bridge (the ZoneMinder Server it self), and a number of Things, which relates to the induvidual monitors in ZoneMinder.
ZoneMinder things can be configured either through the online configuration utility via discovery, or manually through the 'zoneminder.things' configuration file.
The Bridge will not be autodiscovered, this behavior is by design.
That is because the ZoneMinder API can be configured to communicate on custom ports, you can even change the url from the default /zm/ to something userdefined.
That makes it meaningless to scan for a ZoneMinder Server.
The Bridge must therefore be added manually, this can be done from PaperUI.
After adding the Bridge it will go ONLINE, and after a short while and the discovery process for monitors will start.
When a new monitor is discovered it will appear in the Inbox.

### Bridge

| Channel    | Type   | Description                                  |
|------------|--------|----------------------------------------------|
| online     | Switch | Parameter indicating if the server is online |
| CPU load   | Text   | Current CPU Load of server                   |
| Disk Usage | text   | Current Disk Usage on server                 |

### Thing

| Channel         | Type   | Description                                                                                |
|-----------------|--------|--------------------------------------------------------------------------------------------|
| online          | Switch | Parameter indicating if the monitor is online                                              |
| enabled         | Switch | Parameter indicating if the monitor is enabled                                             |
| force-alarm     | Switch | Parameter indicating if Force Alarm for the the monitor is active                          |
| alarm           | Switch | true if monitor has an active alarm                                                        |
| recording       | Text   | true if monitor is recording                                                               |
| detailed-status | Text   | Detailed status of monitor (Idle, Pre-alarm, Alarm, Alert, Recording)                      |
| event-cause     | Text   | Empty when there is no active event, else it contains the text with the cause of the event |
| function        | Text   | Text corresponding the value in ZoneMinder: None, Monitor, Modect, Record, Mocord, Nodect  |
| capture-daemon  | Switch | Run state of ZMC Daemon                                                                    |
| analysis-daemon | Switch | Run state of ZMA Daemon                                                                    |
| frame-daemon    | Switch | Run state of ZMF Daemon                                                                    |

## Manual configuration

### Things configuration

```
Bridge zoneminder:server:ZoneMinderSample [ hostname="192.168.1.55", user="<USERNAME>", password="<PASSWORD>", telnet_port=6802, refresh_interval_disk_usage=1 ]
{
	Thing monitor monitor_1 [ monitorId=1, monitorTriggerTimeout=120, monitorEventText="Trigger activated from openHAB" ]
}

```

### Items configuration

```
/* *****************************************
 * SERVER
 * *****************************************/
Switch zmServer_Online 			"Zoneminder Online [%s]"			<switch>	{channel="zoneminder:server:ZoneMinderSample:online"}
Number zmServer_CpuLoad 			"ZoneMinder Server Load [%s]"				{channel="zoneminder:server:ZoneMinderSample:cpu-load"}

Number zmServer_DiskUsage			"ZoneMinder Disk Usage [%s]"				{channel="zoneminder:server:ZoneMinderSample:disk-usage"}

/* *****************************************
 * MONITOR 1
 * *****************************************/
Switch zmMonitor1_Online 		"Online [%s]" 				<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:online"}
Switch zmMonitor1_Enabled 		"Enabled [%s]" 				<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:enabled"}
Switch zmMonitor1_ForceAlarm 		"Force Alarm [%s]"	 			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:force-alarm"}
Switch zmMonitor1_EventState 		"Alarm [%s]"	 			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:alarm"}
Switch zmMonitor1_Recording 		"Recording [%s]"	 		<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:recording"}
String zmMonitor1_DetailedStatus	"Detailed Status [%s]"	 				{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:detailed-status"}
String zmMonitor1_EventCause 		"Event Cause [%s]"	 		<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:event-cause"}
String zmMonitor1_Function 		"Function [%s]" 					{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:function"}
Switch zmMonitor1_CaptureState	 	"Capture Daemon [%s]" 			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:capture-daemon"}
Switch zmMonitor1_AnalysisState 	"Analysis Daemon [%s]" 			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:analysis-daemon"}
Switch zmMonitor1_FrameState		"Frame Daemon [%s]"			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor-1:frame-daemon"}


// Helpers
Switch zmMonitor1_Mode			"Monitor active [%s]"
```

### Sample Rule

```
rule "Monitor1 Alarm State"
when
    Item zmMonitor1_EventState changed
then
	if (zmMonitor1_EventState.state == ON) {
		logInfo("zoneminder.rules", "ZoneMinder Alarm started")
	}
	else if (zmMonitor1_EventState.state == OFF) {
		logInfo("zoneminder.rules", "ZoneMinder Alarm stopped")
	}
end

rule "Monitor1 Recording State"
when
    Item zmMonitor1_Recording changed
then
	if (zmMonitor1_Recording.state == ON) {
		logInfo("zoneminder.rules", "ZoneMinder recording started")
	}
	else if (zmMonitor1_Recording.state == OFF) {
		logInfo("zoneminder.rules", "ZoneMinder recording stopped")
	}
end


rule "Change Monitor1 Mode"
when
    Item zmMonitor1_Mode changed
then
	if (zmMonitor1_Mode.state==ON) {
		sendCommand(zmMonitor1_Function, "Modect")
		sendCommand(zmMonitor1_Enabled, ON)
	}
	else {
		sendCommand(zmMonitor1_Function, "Monitor")
		sendCommand(zmMonitor1_Enabled, OFF)
	}
end
```

### Sitemap configuration

```
sitemap zoneminder label="Zoneminder"
{
	Frame {
		Text item=zmServer_Online label="ZoneMinder Server [%s]" {
			Frame {
				Text item=zmServer_Online
				Text  item=zmServer_CpuLoad
				Text  item=zmServer_DiskUsage
			}
		}

		Text item=zmMonitor1_Function label="(Monitor-1) [%s]" {
			Frame {
				Switch 		item=zmMonitor1_Enabled
				Switch 		item=zmMonitor1_ForceAlarm
				Text		item=zmMonitor1_Online
				Selection	item=zmMonitor1_Function 	mappings=["None"=None, "Modect"=Modect, "Monitor"=Monitor, "Record"=Record, "Mocord"=Mocord, "Nodect"=Nodect]
				Text  		item=zmMonitor1_EventState 	
				Text 		item=zmMonitor1_Recording 	
				Text		item=zmMonitor1_DetailedStatus
				Text 		item=zmMonitor1_EventCause
				Text 		item=zmMonitor1_CaptureState
				Text 		item=zmMonitor1_AnalysisState
				Text 		item=zmMonitor1_FrameState
			}
		}
		Frame label="Monitor Helpers" {
			Switch item=zmMonitor1_Mode
		}
	}
}
```

## Troubleshooting

<table>
<tr><td><b>Problem</b></td><td><b>Solution</b></td></tr>
<tr><td>Cannot connect to ZoneMinder Bridge</td><td>Check if you can logon to ZoneMinder from your openHAB server (with http).</td></tr>
<tr><td></td><td>Check that it is possible to establish a Telnet connection from openHAB server to Zoneminder Server</td></tr>
<tr><td>ZoneMinder Bridge is not comming ONLINE. It says: 'OFFLINE - COMMUNICATION_ERROR Cannot access ZoneMinder Server. Check provided usercredentials'</td><td>Check that the hostname is valid, if using a DNS name, make sure name is correct resolved. Also check that the given host can be accessed from a browser. Finally make sure to change the additional path from '/zm', if not using standard setup.</td></tr>
<tr><td>Cannot connect to ZoneMinder Bridge via HTTPS, using Letsencrypt certificate</td><td>Verify your Java version, if Java is below build 101, letsencrypt certificate isn't known by Java. Either use HTTP or upgrade Java to newest build. Please be aware that https support is provided as an experimental feature.</td></tr>
<tr><td>I have tried all of the above, it still doesn't work</td><td>Try to execute this from a commandline (on your openHAB server): curl -d "<username>=XXXX&<password>=YYYY&action=login&view=console" -c cookies.txt  http://<yourzmip>/zm/index.php. Change <yourzmip>, <username> and <password> to the actual values. This will check if your server is accessible from the openHAB server.</td></tr>
</table>
