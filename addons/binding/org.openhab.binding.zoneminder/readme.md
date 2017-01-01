# Zoneminder Binding

This binding offers integration to a ZoneMinder Server. It currently only offers to integrate to monitors (eg. cameras in ZoneMinder). It also only offers access to a limited set of values, as well as a even more limited option to update values in ZoneMinder. It requires at least ZoneMinder 1.29 with API enabled (option 'OPT_USE_API' in ZoneMinder must be enabled). The option 'OPT_TRIGGERS' must be anabled to allow OpenHAB to trip the ForceAlarm in ZoneMinder.

## Supported Things

This binding supports the following thing types

<table>
<tr><td><b>Thing</b></td><td><b>Thing Type</b></td><td><b>Discovery</b></td><td><b>Description</b></td></tr>
<tr><td>ZoneMinder Server</td><td>Bridge</td><td>No</td><td>A ZoneMinder Server. Required version is minimum 1.29</td></tr>
<tr><td>ZoneMinder Monitor</td><td>Thing</td><td>Yes</td><td>Monitor as defined in ZoneMinder Server</td></tr>
</table>

## Getting started /  Discovery
The binding consists of a Bridge (the ZoneMinder Server it self), and a number of Things, which relates to the induvidual monitors in ZoneMinder. ZoneMinder things can be configured either through the online configuration utility via discovery, or manually through the 'zoneminder.things' configuration file. The Bridge will not be autodiscovered, this behaviour is by design. That is because the ZoneMinder API can be configured to communicate on custom ports, you can even change the url from the default /zm/ to something userdefined. That makes it meaningless to scan for a ZoneMinder Server. The Bridge must therefore be added manually, this can be done from PaperUI. After adding the Bridge it will go ONLINE, and after a short while and the discovery process for monitors will start. When a new monitor is discovered it will appear in the Inbox.


### Bridge ###
 Channel       | Type      | Description
-------------- | --------- | ----------------------------------
Is Alive       | Switch    | Parameter indicationg if the server IsAlive
CPU load       | Text      | Current CPU Load of server
Disk Usage     | text      | Current Disk Usage on server
Server Version | Text      | Version of ZoneMinder
API Version    | Text      | Version of API 

### Thing ###

 Channel       | Type      | Description
-------------- | --------- | ----------------------------------
Is Alive       | Switch    | Parameter indicationg if the monitor IsAlive
Enabled        | Switch    | Parameter indicationg if the monitor is enabled
Name           | Text      | Name of Monitor
SourceType     | Text      | As stated in ZoneMinder (Local, Remote, File, Ffmpeg, Libvlc, cUrl)
Trigger        | Switch    | State of the ForceAlarm in ZoneMidner. This can both be read and set from OpenHAB.
Function       | Text      | Text corresponding the value in ZoneMinder: None, Monitor, Modect, Record, Mocord, Nodect
ZMC Daemon State    | Switch      | Run state of ZMC Daemon 
ZMA Daemon State    | Switch      | Run state of ZMA Daemon 
ZMF Daemon State    | Switch      | Run state of ZMF Daemon 

##Manual configuration##
###Things configuration###
```
Bridge zoneminder:server:ZoneMinderSample [ hostname="192.168.1.55", user="<USERNAME>", password="<PASSWORD>", telnet_port=6802, refresh_interval_disk_usage=1 ]
{
	Thing monitor monitor_1 [ monitorId=1, monitorTriggerTimeout=120, monitorEventText="Trigger activated from OpenHAB" ]
}

```
###Items configuration###
```
// SERVER
Switch zmsIsAlive 			"Zoneminder alive [%s]"			<switch>	{channel="zoneminder:server:ZoneMinderSample:is-alive"}
Number zmsCpuLoad 			"ZoneMinder Server Load [%d]"				{channel="zoneminder:server:ZoneMinderSample:cpu-load"}
Number zmsDiskUsage			"ZoneMinder Disk Usage [%d]"				{channel="zoneminder:server:ZoneMinderSample:disk-usage"}

String zmsServerVersion			"ZoneMinder Server Version [%s]"			{channel="zoneminder:server:ZoneMinderSample:zm-version"}
String zmsServerApiVersion 		"ZoneMinder Server Version [%s]"			{channel="zoneminder:server:ZoneMinderSample:zm-api-version"}

// MONITOR 1
String zmMonitor1_Name 			"Name [%s]" 						{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:name"}
Switch zmMonitor1_IsAlive 		"Alive [%s]" 				<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:is-alive"}
Switch zmMonitor1_Enabled 		"Enabled [%s]" 				<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:enabled"}

String zmMonitor1_SourceType 		"Source [%s]" 						{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:sourcetype"}
Switch zmMonitor1_TriggerEvent 		"Trigger [%s]"	 			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:trigger-event"}

String zmMonitor1_Function 		"Function [%s]" 					{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:function"}

Switch zmMonitor1_CaptureStatus 	"Capture Daemon [%s]" 			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:daemon-capture-state"}
String zmMonitor1_CaptureStatusText 	"Capture statustext [%s]"  				{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:daemon-capture-statustext"}


Switch zmMonitor1_AnalysisStatus 	"Analysis Daemon [%s]" 			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:daemon-analysis-state"}
String zmMonitor1_AnalysisStatusText 	"Analysis StatusText [%s]" 				{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:daemon-analysis-statustext"}


Switch zmMonitor1_FrameStatus		"Frame Daemon [%s]"			<switch>	{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:daemon-frame-state"}
String zmMonitor1_FrameStatusText 	"Frame StatusText [%s]"	 				{channel="zoneminder:monitor:ZoneMinderSample:monitor_1:daemon-frame-statustext"}


// Helpers
Switch zmMonitor1_Mode			"Monitor active [%s]"
```

###Sitemap configuration###
```
sitemap zoneminder label="Zoneminder"
{
	Frame {
		Text item=zmsIsAlive label="ZoneMinder Server [%s]" {
			Frame {
				Switch item=zmsIsAlive
				Text  item=zmsCpuLoad
				Text  item=zmsDiskUsage
				Text  item=zmsServerVersion
				Text  item=zmsServerApiVersion
			}
		}

		Text item=zmMonitor1_Function label="(Monitor-1) [%s]" {
			Frame {
				Text 	item=zmMonitor1_Name
				Switch	item=zmMonitor1_Enabled
				Text 	item=zmMonitor1_SourceType
				Switch	item=zmMonitor1_TriggerEvent
				Selection item=zmMonitor1_Function mappings=["None"=None, "Modect"=Modect, "Monitor"=Monitor, "Record"=Record, "Mocord"=Mocord, "Nodect"=Nodect]
				Text 	item=zmMonitor1_IsAlive
				Text 	item=zmMonitor1_CaptureStatus
				Text 	item=zmMonitor1_CaptureStatusText
				Text 	item=zmMonitor1_AnalysisStatus
				Text 	item=zmMonitor1_AnalysisStatusText
				Text 	item=zmMonitor1_FrameStatus
				Text 	item=zmMonitor1_FrameStatusText
			}
		}
		Frame label="Monitor Helpers" {
			Switch item=zmMonitor1_Mode
		}
	}
}
```
##Troubleshooting##
<table>
<tr><td><b>Problem</b></td><td><b>Solution</b></td></tr>
<tr><td>Cannot connect to ZoneMinder Bridge</td><td>Check if you can logon to ZoneMinder from your OpenHAB server (with http).</td></tr>
<tr><td></td><td>Check that it is possible to establish a Telnet connection from OpenHAB server to Zoneminder Server</td></tr>
<tr><td>Cannot connect to ZoneMinder Bridge via HTTPS, using Letsencrypt certificate</td><td>Verify your Java version, if Java is below build 101, letsencrypt certificate isn't known by Java. Either use HTTP or upgrade Java to newest build.</td></tr>
</table>
                  
		  
