# Zoneminder Binding #

This binding offers integration to a ZoneMinder Server. It currently only offers to integrate to monitors (eg. cameras in ZoneMinder). It also only offers access to a limited set of values, as well as a even more limited option to update values in ZoneMinder. It requires at least ZoneMinder 1.29 with API enabled (option 'OPT_USE_API' in ZoneMinder must be enabled). The option 'OPT_TRIGGERS' must be anabled to allow OpenHAB to trip the ForceAlarm in ZoneMinder.

## Getting started /  Discovery ##
The binding consists of a Bridge (the ZoneMinder Server it self), and a number of Things, which relates to the induvidual monitors in ZoneMinder. The Bridge will not be autodiscovered, since the ZoneMinder API can be configured to communicate on custom ports, thus making it meaningless to scan for a ZoneMinder Server. The Bridge must instead be added manually. After adding the Bridge it will become accesible after a short while and it will the auto discover the monitors (they will simply appear in the Inbox).


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
SourceType     | Text      | As stated in ZoneMinder (Local, remote, File, Ffmpeg, Libvlc, cUrl)
Trigger        | Switch    | 
Function       | Text      | Version of ZoneMinder
ZMC Daemon State    | Switch      | Run state of ZMC Daemon 
ZMA Daemon State    | Switch      | Run state of ZMA Daemon 
ZMF Daemon State    | Switch      | Run state of ZMF Daemon 
