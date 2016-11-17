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
The binding consists of a Bridge (the ZoneMinder Server it self), and a number of Things, which relates to the induvidual monitors in ZoneMinder. The Bridge will not be autodiscovered, this behaviour is by design. That is because  the ZoneMinder API can be configured to communicate on custom ports, you can even change the url from the default /zm/ to something userdefined. That makes it meaningless to scan for a ZoneMinder Server. The Bridge must instead be added manually, which can be done from PaperUI. After adding the Bridge it will become accesible after a short while and the discovery process for monitors will start. When a new monitor is discovered it will appear in the Inbox.

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



##Troubleshooting##
<table>
<tr><td><b>Problem</b></td><td><b>Solution</b></td></tr>
<tr><td>Cannot connect to ZoneMinder Bridge</td><td>Check if you can logon to ZoneMinder from your OpenHAB server (with http).</td></tr>
<tr><td></td><td>Check that it is possible to establish a Telnet connection from OpenHAB server to Zoneminder Server</td></tr>
</table>
                                    
